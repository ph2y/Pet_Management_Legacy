package com.sju18001.petmanagement.ui.community.followerFollowing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.CustomProgressBar
import com.sju18001.petmanagement.databinding.FragmentFollowingBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager

class FollowingFragment : Fragment() {

    // for shared ViewModel
    private lateinit var followerFollowingViewModel: FollowerFollowingViewModel

    // variables for view binding
    private var _binding: FragmentFollowingBinding? = null
    private val binding get() = _binding!!

    // variables for RecyclerView
    private lateinit var followingAdapter: FollowingAdapter
    private var followingList: MutableList<FollowerFollowingListItem> = mutableListOf()

    private var isViewDestroyed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentFollowingBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        val root = binding.root

        // for swipe refresh
        binding.followingSwipeRefreshLayout.setOnRefreshListener {
            fetchFollowing()
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // initialize ViewModel
        followerFollowingViewModel = ViewModelProvider(requireActivity(),
            SavedStateViewModelFactory(requireActivity().application, requireActivity())
        ).get(FollowerFollowingViewModel::class.java)

        // initialize RecyclerView
        followingAdapter = FollowingAdapter(requireContext(), followerFollowingViewModel)
        binding.followingRecyclerView.setHasFixedSize(true)
        binding.followingRecyclerView.adapter = followingAdapter
        binding.followingRecyclerView.layoutManager = LinearLayoutManager(activity)

        // Set adapter item change observer
        followingAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                setEmptyFollowingView(followingAdapter.itemCount)
            }
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                setEmptyFollowingView(followingAdapter.itemCount)
            }
        })
    }

    override fun onResume() {
        super.onResume()

        // 첫 Fetch가 끝나기 전까지 ProgressBar 표시
        CustomProgressBar.addProgressBar(requireContext(), binding.fragmentFollowingParentLayout, 80, R.color.white)

        fetchFollowing()
    }

    private fun setEmptyFollowingView(itemCount: Int){
        val visibility = if(itemCount != 0) View.GONE else View.VISIBLE
        binding.emptyFollowingList.visibility = visibility
    }

    private fun fetchFollowing() {
        // reset list
        followingList = mutableListOf()

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchFollowerReq(ServerUtil.getEmptyBody())

        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            response.body()!!.followerList.map {
                val hasPhoto = it.photoUrl != null
                val id = it.id
                val username = it.username
                val nickname = it.nickname
                val representativePetId = it.representativePetId

                val item = FollowerFollowingListItem()
                item.setValues(hasPhoto, null, id, username, nickname!!, true, representativePetId)
                followingList.add(item)
            }

            // set following count
            val followingText = requireContext().getText(R.string.following_fragment_title).toString() +
                    ' ' + followingList.size.toString()
            followerFollowingViewModel.setFollowingTitle(followingText)

            // set RecyclerView
            followingAdapter.setResult(followingList)

            // set swipe isRefreshing to false
            CustomProgressBar.removeProgressBar(binding.fragmentFollowingParentLayout)
            binding.followingSwipeRefreshLayout.isRefreshing = false
        }, {
            CustomProgressBar.removeProgressBar(binding.fragmentFollowingParentLayout)
            binding.followingSwipeRefreshLayout.isRefreshing = false
        }, {
            CustomProgressBar.removeProgressBar(binding.fragmentFollowingParentLayout)
            binding.followingSwipeRefreshLayout.isRefreshing = false
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // call onDestroy inside adapter(for API cancel)
        followingAdapter.onDestroy()

        isViewDestroyed = true
    }
}