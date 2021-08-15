package com.sju18001.petmanagement.ui.community.comment

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCommunityCommentBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.FetchCommentReqDto
import com.sju18001.petmanagement.restapi.dto.FetchCommentResDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommunityCommentFragment : Fragment() {

    private var _binding: FragmentCommunityCommentBinding? = null
    private val binding get() = _binding!!

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    // 리싸이클러뷰
    private lateinit var adapter: CommunityCommentListAdapter

    private var isViewDestroyed: Boolean = false

    // 댓글 새로고침
    private var topCommentId: Long? = null
    private var pageIndex: Int = 0

    // 현재 게시글의 postId
    private var postId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityCommentBinding.inflate(inflater, container, false)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!

        // postId 지정
        postId = requireActivity().intent.getLongExtra("postId", -1)

        // 어뎁터 초기화
        initializeAdapter()

        // 초기 댓글 추가
        updateAdapterDataSetByFetchComment(FetchCommentReqDto(
            null, null, postId, null, null
        ))

        // 리스너 추가
        setListenerOnViews()

        // SwipeRefreshLayout
        binding.layoutSwipeRefresh.setOnRefreshListener {
            resetCommentData()
            updateAdapterDataSetByFetchComment(FetchCommentReqDto(
                null, null, postId, null, null
            ))
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true
    }

    private fun initializeAdapter(){
        adapter = CommunityCommentListAdapter(arrayListOf())
        adapter.communityCommentListAdapterInterface = object: CommunityCommentListAdapterInterface{
            override fun getActivity(): Activity {
                return requireActivity()
            }
        }

        binding.recyclerViewComment?.let{
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(activity)

            // 스크롤하여, 최하단에 위치할 시 comment 추가 로드
            it.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if(!recyclerView.canScrollVertically(1)){
                        updateAdapterDataSetByFetchComment(FetchCommentReqDto(
                            pageIndex, topCommentId, postId, null, null
                        ))
                        pageIndex += 1
                    }
                }
            })
        }
    }

    private fun updateAdapterDataSetByFetchComment(body: FetchCommentReqDto){
        val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .fetchCommentReq(body)
        call!!.enqueue(object: Callback<FetchCommentResDto> {
            override fun onResponse(
                call: Call<FetchCommentResDto>,
                response: Response<FetchCommentResDto>
            ) {
                if(isViewDestroyed){
                    return
                }

                if(response.isSuccessful){
                    response.body()!!.commentList?.let {
                        if(it.isNotEmpty()){
                            it.map { item ->
                                adapter.addItem(item)
                            }

                            topCommentId = it.last().id

                            // 데이터셋 변경 알림
                            binding.recyclerViewComment.post{
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

            override fun onFailure(call: Call<FetchCommentResDto>, t: Throwable) {
                if(isViewDestroyed){
                    return
                }

                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()

                // 새로고침 아이콘 제거
                binding.layoutSwipeRefresh.isRefreshing = false
            }
        })
    }

    private fun resetCommentData(){
        pageIndex = 0
        adapter.resetDataSet()

        // 데이터셋 변경 알림
        binding.recyclerViewComment.post{
            adapter.notifyDataSetChanged()
        }
    }

    private fun setListenerOnViews(){
        // 뒤로가기 버튼
        binding.buttonBack.setOnClickListener {
            activity?.finish()
        }

        // 편의 기능: 키보드 내리기
        Util.setupViewsForHideKeyboard(requireActivity(), binding.fragmentCommunityCommentParentLayout)
        
        // 댓글 / 답글 생성
        binding.buttonCreateComment.setOnClickListener {
            // TODO: 댓글/답글 CREATE
        }
    }
}