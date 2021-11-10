package com.sju18001.petmanagement.ui.community.followerFollowing

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentFollowerFollowingBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.FetchFollowerResDto
import com.sju18001.petmanagement.restapi.dto.FetchFollowingResDto
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FollowerFollowingFragment : Fragment() {

    // for shared ViewModel
    private lateinit var followerFollowingViewModel: FollowerFollowingViewModel

    // variables for view binding
    private var _binding: FragmentFollowerFollowingBinding? = null
    private val binding get() = _binding!!

    private var isViewDestroyed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentFollowerFollowingBinding.inflate(inflater, container, false)
        isViewDestroyed = false
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize tab elements
        val TAB_ELEMENTS = listOf(requireContext().getText(R.string.follower_fragment_title),
            requireContext().getText(R.string.following_fragment_title))

        // initialize ViewPager2
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager.also{
            it.adapter = FollowerFollowingCollectionAdapter(this)
            it.currentItem = requireActivity().intent.getIntExtra("pageIndex", 0)
            it.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    requireActivity().intent.putExtra("pageIndex", position)
                }
            })
        }

        TabLayoutMediator(tabLayout, viewPager){ tab, position ->
            tab.text = TAB_ELEMENTS[position]
        }.attach()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // initialize ViewModel
        followerFollowingViewModel = ViewModelProvider(requireActivity(),
            SavedStateViewModelFactory(requireActivity().application, requireActivity()))
            .get(FollowerFollowingViewModel::class.java)

        // observe live data and set title when change
        followerFollowingViewModel.getFollowerTitle().observe(viewLifecycleOwner, object: Observer<String> {
            override fun onChanged(followerTitle: String?) {
                binding.tabLayout.getTabAt(0)!!.text = followerTitle
            }
        })
        followerFollowingViewModel.getFollowingTitle().observe(viewLifecycleOwner, object: Observer<String> {
            override fun onChanged(followingTitle: String?) {
                binding.tabLayout.getTabAt(1)!!.text = followingTitle
            }
        })
    }

    override fun onStart() {
        super.onStart()

        // for search button
        binding.searchButton.setOnClickListener {
            // start search activity
            val searchActivityIntent = Intent(context, SearchActivity::class.java)
            startActivity(searchActivityIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        // for back button
        binding.backButton.setOnClickListener {
            activity?.finish()
        }

        // set tab layout titles
        setFollowerCount()
        setFollowingCount()
    }

    class FollowerFollowingCollectionAdapter(fragment: Fragment): FragmentStateAdapter(fragment){
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> FollowerFragment()
                else -> FollowingFragment()
            }
        }
    }

    private fun setFollowerCount() {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchFollowingReq(ServerUtil.getEmptyBody())

        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            val followerCount = response.body()!!.followingList.size

            // set follower count
            val followerText = requireContext().getText(R.string.follower_fragment_title).toString() +
                    ' ' + followerCount.toString()
            followerFollowingViewModel.setFollowerTitle(followerText)
        }, {}, {})
    }

    private fun setFollowingCount() {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchFollowerReq(ServerUtil.getEmptyBody())

        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            val followingCount = response.body()!!.followerList.size

            // set following count
            val followingText = requireContext().getText(R.string.following_fragment_title).toString() +
                    ' ' + followingCount.toString()
            followerFollowingViewModel.setFollowingTitle(followingText)
        }, {}, {})
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true
    }
}