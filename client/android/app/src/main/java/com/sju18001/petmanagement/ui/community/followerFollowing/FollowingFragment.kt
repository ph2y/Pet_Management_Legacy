package com.sju18001.petmanagement.ui.community.followerFollowing

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentFollowingBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.FetchFollowerResDto
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FollowingFragment : Fragment() {

    // for shared ViewModel
    private lateinit var followerFollowingViewModel: FollowerFollowingViewModel

    // variables for view binding
    private var _binding: FragmentFollowingBinding? = null
    private val binding get() = _binding!!

    // variables for RecyclerView
    private lateinit var followingAdapter: FollowingAdapter
    private var followingList: MutableList<FollowerFollowingListItem> = mutableListOf()

    // variable for storing API call(for cancel)
    private var fetchFollowerApiCall: Call<FetchFollowerResDto>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // view binding
        _binding = FragmentFollowingBinding.inflate(inflater, container, false)
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
    }

    override fun onResume() {
        super.onResume()

        // update RecyclerView
        fetchFollowing()
    }

    private fun fetchFollowing() {
        // reset list
        followingList = mutableListOf()

        // create empty body
        val emptyBody = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")

        // API call
        fetchFollowerApiCall = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchFollowerReq(emptyBody)
        fetchFollowerApiCall!!.enqueue(object: Callback<FetchFollowerResDto> {
            override fun onResponse(
                call: Call<FetchFollowerResDto>,
                response: Response<FetchFollowerResDto>
            ) {
                if(response.isSuccessful) {
                    response.body()!!.followerList.map {
                        val hasPhoto = it.photoUrl != null
                        val id = it.id
                        val nickname = it.nickname

                        val item = FollowerFollowingListItem()
                        item.setValues(hasPhoto, null, id, nickname!!, true)
                        followingList.add(item)
                    }

                    // set following count
                    val followingText = requireContext().getText(R.string.following_fragment_title).toString() +
                            ' ' + followingList.size.toString()
                    followerFollowingViewModel.setFollowingTitle(followingText)

                    // set RecyclerView
                    followingAdapter.setResult(followingList)

                    // set swipe isRefreshing to false
                    binding.followingSwipeRefreshLayout.isRefreshing = false
                }
                else {
                    // set swipe isRefreshing to false
                    binding.followingSwipeRefreshLayout.isRefreshing = false

                    // get error message
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                    // Toast + Log
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<FetchFollowerResDto>, t: Throwable) {
                // set swipe isRefreshing to false
                binding.followingSwipeRefreshLayout.isRefreshing = false

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
        followingAdapter.onDestroy()

        // stop api call when fragment is destroyed
        fetchFollowerApiCall?.cancel()
    }
}