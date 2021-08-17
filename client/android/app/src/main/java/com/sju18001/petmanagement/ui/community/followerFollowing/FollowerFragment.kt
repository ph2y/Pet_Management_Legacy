package com.sju18001.petmanagement.ui.community.followerFollowing

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentFollowerBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    // variables for storing API call(for cancel)
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
        _binding = FragmentFollowerBinding.inflate(inflater, container, false)
        val root = binding.root

        // initialize RecyclerView
        followerAdapter = FollowerAdapter(requireContext(), sessionManager)
        binding.followerRecyclerView.setHasFixedSize(true)
        binding.followerRecyclerView.adapter = followerAdapter
        binding.followerRecyclerView.layoutManager = LinearLayoutManager(activity)

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

        // update RecyclerView
        updateRecyclerView()
    }

    private fun updateRecyclerView() {  // update followingIdList -> fetch follower
        // reset list
        followingIdList = mutableListOf()

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
                    response.body()!!.followerList.map {
                        followingIdList.add(it.id)
                    }

                    // fetch follower
                    fetchFollower()
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

    private fun fetchFollower() {
        // reset list
        followerList = mutableListOf()

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
                    response.body()!!.followingList.map {
                        val hasPhoto = it.photoUrl != null
                        val id = it.id
                        val nickname = it.nickname
                        val isFollowing = it.id in followingIdList

                        val item = FollowerFollowingListItem()
                        item.setValues(hasPhoto, null, id, nickname!!, isFollowing)
                        followerList.add(item)
                    }

                    // set follower count
                    val followerText = requireContext().getText(R.string.follower_fragment_title).toString() +
                            ' ' + followerList.size.toString()
                    followerFollowingViewModel.setFollowerTitle(followerText)

                    // set RecyclerView
                    followerAdapter.setResult(followerList)

                    // set swipe isRefreshing to false
                    binding.followerSwipeRefreshLayout.isRefreshing = false
                }
                else {
                    // set swipe isRefreshing to false
                    binding.followerSwipeRefreshLayout.isRefreshing = false

                    // get error message
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                    // Toast + Log
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<FetchFollowingResDto>, t: Throwable) {
                // set swipe isRefreshing to false
                binding.followerSwipeRefreshLayout.isRefreshing = false

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

        // call onDestroy inside adapter(for API cancel)
        followerAdapter.onDestroy()

        // stop api call when fragment is destroyed
        fetchFollowerApiCall?.cancel()
        fetchFollowingApiCall?.cancel()
    }
}