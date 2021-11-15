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
import com.sju18001.petmanagement.databinding.FragmentFollowerBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager

class FollowerFragment : Fragment() {

    // for shared ViewModel
    private lateinit var followerFollowingViewModel: FollowerFollowingViewModel

    // variables for view binding
    private var _binding: FragmentFollowerBinding? = null
    private val binding get() = _binding!!

    // variables for RecyclerView
    private lateinit var followerAdapter: FollowerAdapter
    private var followingIdList: MutableList<Long> = mutableListOf()
    private var followerList: MutableList<FollowerFollowingListItem> = mutableListOf()

    private var isViewDestroyed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentFollowerBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        val root = binding.root

        // initialize RecyclerView
        followerAdapter = FollowerAdapter(requireContext())
        binding.followerRecyclerView.setHasFixedSize(true)
        binding.followerRecyclerView.adapter = followerAdapter
        binding.followerRecyclerView.layoutManager = LinearLayoutManager(activity)

        // Set adapter item change observer
        followerAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                setEmptyFollowerView(followerAdapter.itemCount)
            }
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                setEmptyFollowerView(followerAdapter.itemCount)
            }
        })

        // for swipe refresh
        binding.followerSwipeRefreshLayout.setOnRefreshListener {
            updateRecyclerView()
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // initialize ViewModel
        followerFollowingViewModel = ViewModelProvider(requireActivity(),
            SavedStateViewModelFactory(requireActivity().application, requireActivity())
        )
            .get(FollowerFollowingViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()

        // 첫 Fetch가 끝나기 전까지 ProgressBar 표시
        CustomProgressBar.addProgressBar(requireContext(), binding.fragmentFollowerParentLayout, 80, R.color.white)

        updateRecyclerView()
    }

    private fun setEmptyFollowerView(itemCount: Int){
        val visibility = if(itemCount != 0) View.GONE else View.VISIBLE
        binding.emptyFollowerList.visibility = visibility
    }

    private fun updateRecyclerView() {  // update followingIdList -> fetch follower
        // reset list
        followingIdList = mutableListOf()

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchFollowerReq(ServerUtil.getEmptyBody())
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            response.body()!!.followerList.map {
                followingIdList.add(it.id)
            }

            fetchFollower()
        }, {}, {})
    }

    private fun fetchFollower() {
        // reset list
        followerList = mutableListOf()

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchFollowingReq(ServerUtil.getEmptyBody())
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            response.body()!!.followingList.map {
                val hasPhoto = it.photoUrl != null
                val id = it.id
                val username = it.username
                val nickname = it.nickname
                val isFollowing = it.id in followingIdList
                val representativePetId = it.representativePetId

                val item = FollowerFollowingListItem()
                item.setValues(hasPhoto, null, id, username, nickname!!, isFollowing, representativePetId)
                followerList.add(item)
            }

            // set follower count
            val followerText = requireContext().getText(R.string.follower_fragment_title).toString() +
                    ' ' + followerList.size.toString()
            followerFollowingViewModel.setFollowerTitle(followerText)

            // set RecyclerView
            followerAdapter.setResult(followerList)

            CustomProgressBar.removeProgressBar(binding.fragmentFollowerParentLayout)
            // set swipe isRefreshing to false
            binding.followerSwipeRefreshLayout.isRefreshing = false
        }, {
            CustomProgressBar.removeProgressBar(binding.fragmentFollowerParentLayout)
            binding.followerSwipeRefreshLayout.isRefreshing = false
        }, {
            CustomProgressBar.removeProgressBar(binding.fragmentFollowerParentLayout)
            binding.followerSwipeRefreshLayout.isRefreshing = false
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // call onDestroy inside adapter(for API cancel)
        followerAdapter.onDestroy()

        isViewDestroyed = true
    }
}