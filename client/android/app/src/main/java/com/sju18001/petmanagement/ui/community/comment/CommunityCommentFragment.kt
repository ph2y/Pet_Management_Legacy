package com.sju18001.petmanagement.ui.community.comment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCommunityCommentBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.dto.CreateCommentReqDto
import com.sju18001.petmanagement.restapi.dto.CreateCommentResDto
import com.sju18001.petmanagement.restapi.dto.FetchCommentReqDto
import com.sju18001.petmanagement.restapi.dto.FetchCommentResDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommunityCommentFragment : Fragment() {

    private var _binding: FragmentCommunityCommentBinding? = null
    private val binding get() = _binding!!

    // variable for ViewModel
    val communityCommentViewModel: CommunityCommentViewModel by activityViewModels()

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    // 리싸이클러뷰
    private lateinit var adapter: CommunityCommentListAdapter

    private var isViewDestroyed: Boolean = false

    // 댓글 새로고침
    private var topCommentId: Long? = null
    private var pageIndex: Int = 1

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

        // 초기화
        initializeViewForViewModel()
        initializeAdapter()
        setListenerOnViews()

        // 초기 댓글 추가
        updateAdapterDataSetByFetchComment(FetchCommentReqDto(
            null, null, postId, null, null
        ))

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true
    }

    private fun initializeViewForViewModel(){
        val idForReply = communityCommentViewModel.idForReply
        val nicknameForReply = communityCommentViewModel.nicknameForReply

        if(idForReply != null && nicknameForReply != null){
            setViewForReply(idForReply, nicknameForReply)
        }
    }

    private fun initializeAdapter(){
        adapter = CommunityCommentListAdapter(arrayListOf())
        adapter.communityCommentListAdapterInterface = object: CommunityCommentListAdapterInterface{
            override fun getActivity(): Activity {
                return requireActivity()
            }

            override fun onClickReply(author: Account) {
                author.nickname?.let { setViewForReply(author.id, it) }
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

    private fun setViewForReply(id: Long, nickname: String) {
        // Show keyboard
        val editTextComment = binding.editTextComment
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        editTextComment.requestFocus()
        imm.showSoftInput(editTextComment, 0)

        // Set view, data for layout_reply_description
        binding.layoutReplyDescription.visibility = View.VISIBLE
        nickname?.let{
            binding.textReplyNickname.text = it
            communityCommentViewModel.nicknameForReply = it
        }
        communityCommentViewModel.idForReply = id

        binding.buttonReplyCancel.setOnClickListener {
            setViewForReplyCancel()
        }
    }

    private fun setViewForReplyCancel(){
        binding.layoutReplyDescription.visibility = View.GONE
        communityCommentViewModel.idForReply = null
        communityCommentViewModel.nicknameForReply = ""
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

                            if(topCommentId == null){
                                topCommentId = it.first().id
                            }

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
        pageIndex = 1
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
            createComment(CreateCommentReqDto(postId, communityCommentViewModel.idForReply, binding.editTextComment.text.toString()))
        }

        // 키보드 동작
        binding.editTextComment.setOnEditorActionListener{ _, _, _ ->
            createComment(CreateCommentReqDto(postId, communityCommentViewModel.idForReply, binding.editTextComment.text.toString()))
            true
        }

        // SwipeRefreshLayout
        binding.layoutSwipeRefresh.setOnRefreshListener {
            resetCommentData()
            updateAdapterDataSetByFetchComment(FetchCommentReqDto(
                null, null, postId, null, null
            ))
        }
    }

    private fun createComment(body: CreateCommentReqDto){
        setCommentInputToLoading()

        val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .createCommentReq(body)
        call!!.enqueue(object: Callback<CreateCommentResDto> {
            override fun onResponse(
                call: Call<CreateCommentResDto>,
                response: Response<CreateCommentResDto>
            ) {
                if(isViewDestroyed){
                    return
                }

                if(response.isSuccessful){
                    // 새로고침
                    resetCommentData()
                    updateAdapterDataSetByFetchComment(FetchCommentReqDto(
                        null, null, postId, null, null
                    ))

                    binding.editTextComment.text = null
                    Toast.makeText(context, "댓글을 작성하였습니다.", Toast.LENGTH_LONG).show()
                }else{
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }

                setCommentInputToNormal()
                setViewForReplyCancel()
            }

            override fun onFailure(call: Call<CreateCommentResDto>, t: Throwable) {
                if(isViewDestroyed){
                    return
                }

                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()

                setCommentInputToNormal()
                setViewForReplyCancel()
            }
        })
    }

    private fun setCommentInputToLoading(){
        binding.buttonCreateComment.visibility = View.GONE
        binding.progressBarComment.visibility = View.VISIBLE
        binding.editTextComment.isEnabled = false
    }

    private fun setCommentInputToNormal(){
        binding.buttonCreateComment.visibility = View.VISIBLE
        binding.progressBarComment.visibility = View.GONE
        binding.editTextComment.isEnabled = true
    }
}