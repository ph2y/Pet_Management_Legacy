package com.sju18001.petmanagement.ui.community.followerFollowing

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    // variable for storing API call(for cancel)
    private var fetchFollowerApiCall: Call<FetchFollowerResDto>? = null
    private var fetchFollowingApiCall: Call<FetchFollowingResDto>? = null

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentFollowerFollowingBinding.inflate(inflater, container, false)
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
        // create empty body
        val emptyBody = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")

        // API call
        fetchFollowingApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .fetchFollowingReq(emptyBody)
        fetchFollowingApiCall!!.enqueue(object: Callback<FetchFollowingResDto> {
            override fun onResponse(
                call: Call<FetchFollowingResDto>,
                response: Response<FetchFollowingResDto>
            ) {
                if(response.isSuccessful) {
                    val followerCount = response.body()!!.followingList.size

                    // set follower count
                    val followerText = requireContext().getText(R.string.follower_fragment_title).toString() +
                            ' ' + followerCount.toString()
                    followerFollowingViewModel.setFollowerTitle(followerText)
                }
                else {
                    // get error message
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                    // Toast + Log
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<FetchFollowingResDto>, t: Throwable) {
                // if the view was destroyed(API call canceled) -> return
                if(_binding == null) {
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    private fun setFollowingCount() {
        // create empty body
        val emptyBody = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")

        // API call
        fetchFollowerApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .fetchFollowerReq(emptyBody)
        fetchFollowerApiCall!!.enqueue(object: Callback<FetchFollowerResDto> {
            override fun onResponse(
                call: Call<FetchFollowerResDto>,
                response: Response<FetchFollowerResDto>
            ) {
                if(response.isSuccessful) {
                    val followingCount = response.body()!!.followerList.size

                    // set following count
                    val followingText = requireContext().getText(R.string.following_fragment_title).toString() +
                            ' ' + followingCount.toString()
                    followerFollowingViewModel.setFollowingTitle(followingText)
                }
                else {
                    // get error message
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                    // Toast + Log
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<FetchFollowerResDto>, t: Throwable) {
                // if the view was destroyed(API call canceled) -> return
                if(_binding == null) {
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // stop api call when fragment is destroyed
        fetchFollowerApiCall?.cancel()
        fetchFollowingApiCall?.cancel()
    }
}