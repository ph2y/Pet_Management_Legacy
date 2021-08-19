package com.sju18001.petmanagement.ui.community

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.restapi.dao.Post
import com.sju18001.petmanagement.ui.community.comment.CommunityCommentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCommunityBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.FetchPostReqDto
import com.sju18001.petmanagement.restapi.dto.FetchPostResDto
import com.sju18001.petmanagement.ui.community.createUpdatePost.CreateUpdatePostActivity
import com.sju18001.petmanagement.ui.community.CommunityViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommunityFragment : Fragment() {

    private lateinit var communityViewModel: CommunityViewModel
    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    // 리싸이클러뷰
    private lateinit var adapter: CommunityPostListAdapter

    private var isViewDestroyed: Boolean = false
    
    // 글 새로고침
    private var topPostId: Long? = null
    private var pageIndex: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        communityViewModel =
            ViewModelProvider(this).get(CommunityViewModel::class.java)

        _binding = FragmentCommunityBinding.inflate(inflater, container, false)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!

        // 어뎁터 초기화
        initializeAdapter()

        // 초기 post 추가
        updateAdapterDataSetByFetchPost(FetchPostReqDto(null, null, null, null))

        // SwipeRefreshLayout
        binding.layoutSwipeRefresh.setOnRefreshListener {
            resetPostData()
            updateAdapterDataSetByFetchPost(FetchPostReqDto(null, null, null, null))
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // for create post FAB
        binding.createPostFab.setOnClickListener {
            val createUpdatePostActivityIntent = Intent(context, CreateUpdatePostActivity::class.java)
            createUpdatePostActivityIntent.putExtra("fragmentType", "create_post")
            startActivity(createUpdatePostActivityIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true
    }

    private fun initializeAdapter(){
        adapter = CommunityPostListAdapter(arrayListOf())
        adapter.communityPostListAdapterInterface = object: CommunityPostListAdapterInterface {
            override fun startCommunityCommentActivity(postId: Long) {
                val communityCommentActivityIntent = Intent(context, CommunityCommentActivity::class.java)
                communityCommentActivityIntent.putExtra("postId", postId)

                startActivity(communityCommentActivityIntent)
                requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
            }

            override fun startCreateUpdatePostActivity(postId: Long) {
                val createUpdatePostActivityIntent = Intent(context, CreateUpdatePostActivity::class.java)
                createUpdatePostActivityIntent.putExtra("fragmentType", "update_post")
                createUpdatePostActivityIntent.putExtra("postId", postId)
                startActivity(createUpdatePostActivityIntent)
                requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
            }
        }
        binding.recyclerViewPost?.let{
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(activity)
            
            // 스크롤하여, 최하단에 위치할 시 post 추가 로드
            it.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if(!recyclerView.canScrollVertically(1)){
                        updateAdapterDataSetByFetchPost(FetchPostReqDto(
                            pageIndex, topPostId, null, null
                        ))
                        pageIndex += 1
                    }
                }
            })
        }
    }

    private fun updateAdapterDataSetByFetchPost(body: FetchPostReqDto){
        val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .fetchPostReq(body)
        call!!.enqueue(object: Callback<FetchPostResDto> {
            override fun onResponse(
                call: Call<FetchPostResDto>,
                response: Response<FetchPostResDto>
            ) {
                if(isViewDestroyed){
                    return
                }

                if(response.isSuccessful){
                    response.body()!!.postList?.let {
                        if(it.isNotEmpty()){
                            it.map { item ->
                                adapter.addItem(item)
                            }

                            if(topPostId == null){
                                topPostId = it.first().id
                            }

                            // 데이터셋 변경 알림
                            binding.recyclerViewPost.post{
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }else{
                    Toast.makeText(context, Util.getMessageFromErrorBody(response.errorBody()!!), Toast.LENGTH_LONG).show()
                }

                // 새로고침 아이콘 제거
                binding.layoutSwipeRefresh.isRefreshing = false
            }

            override fun onFailure(call: Call<FetchPostResDto>, t: Throwable) {
                if(isViewDestroyed){
                    return
                }

                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()

                // 새로고침 아이콘 제거
                binding.layoutSwipeRefresh.isRefreshing = false
            }
        })
    }

    private fun resetPostData(){
        pageIndex = 1
        adapter.resetDataSet()

        // 데이터셋 변경 알림
        binding.recyclerViewPost.post{
            adapter.notifyDataSetChanged()
        }
    }
}