package com.sju18001.petmanagement.ui.community.comment

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCommunityCommentBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.dao.Comment
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.community.comment.updateComment.UpdateCommentActivity
import com.sju18001.petmanagement.ui.community.createUpdatePost.CreateUpdatePostActivity
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
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

    // 현재 로그인된 계정
    private lateinit var loggedInAccount: Account

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
        setLoggedInAccountIdAndFetchAccountPhoto()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        resetCommentData()
        updateAdapterDataSetByFetchComment(FetchCommentReqDto(
            null, null, postId, null, null
        ))
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
        adapter = CommunityCommentListAdapter(arrayListOf(), arrayListOf(), arrayListOf())
        adapter.communityCommentListAdapterInterface = object: CommunityCommentListAdapterInterface{
            override fun getActivity(): Activity {
                return requireActivity()
            }

            override fun onClickReply(id: Long, nickname: String) {
                setViewForReply(id, nickname)
            }

            override fun onLongClickComment(authorId: Long, commentId: Long, commentContents: String){
                if(loggedInAccount.id == authorId){
                    showCommentDialog(commentId, commentContents)
                }
            }

            override fun setAccountPhoto(id: Long, holder: CommunityCommentListAdapter.ViewHolder) {
                setAccountPhotoToImageView(id, holder.profileImage)
            }

            override fun setAccountDefaultPhoto(holder: CommunityCommentListAdapter.ViewHolder) {
                holder.profileImage.setImageDrawable(requireContext().getDrawable(R.drawable.ic_baseline_account_circle_24))
            }

            override fun fetchReplyComment(pageIndex: Int, topCommentId: Long, parentCommentId: Long, position: Int){
                val body = FetchCommentReqDto(pageIndex, topCommentId, null, parentCommentId, null)
                val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!).fetchCommentReq(body)
                call.enqueue(object: Callback<FetchCommentResDto> {
                    override fun onResponse(
                        call: Call<FetchCommentResDto>,
                        response: Response<FetchCommentResDto>
                    ) {
                        if(isViewDestroyed){
                            return
                        }
                        
                        if(response.isSuccessful){
                            // Add replies to RecyclerView
                            response.body()!!.commentList?.let{
                                val replyCount = it.count()
                                for(i in 0 until replyCount){
                                    it[i].contents = it[i].contents.replace("\n", "")
                                    adapter.addItemOnPosition(it[i], position+1)
                                }

                                // 더이상 불러올 답글이 없을 시 topCommentId 초기화 -> 답글 불러오기 제거
                                if(replyCount == 0){
                                    adapter.setTopCommentIdList(-1, position)
                                    adapter.notifyItemChanged(position)

                                    Toast.makeText(requireContext(), getString(R.string.no_more_reply), Toast.LENGTH_SHORT).show()
                                }

                                adapter.notifyItemRangeInserted(position + 1, replyCount)
                            }
                        }else{
                            val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<FetchCommentResDto>, t: Throwable) {
                        if(isViewDestroyed){
                            return
                        }

                        Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                    }
                })
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
        Util.showKeyboard(requireActivity(), binding.editTextComment)

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

    private fun showCommentDialog(id: Long, contents: String){
        val builder = AlertDialog.Builder(requireActivity())
        builder.setItems(arrayOf("수정", "삭제"), DialogInterface.OnClickListener{ _, which ->
            when(which){
                0 -> {
                    // 수정
                    val updateCommunityActivityIntent = Intent(context, UpdateCommentActivity::class.java)
                    updateCommunityActivityIntent.putExtra("id", id)
                    updateCommunityActivityIntent.putExtra("contents", contents)

                    startActivity(updateCommunityActivityIntent)
                    requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
                }
                1 -> {
                    // 삭제
                    deleteComment(id)
                }
            }
        })
            .create().show()
    }

    private fun deleteComment(id: Long){
        val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!).deleteCommentReq(
            DeleteCommentReqDto(id)
        )
        call!!.enqueue(object: Callback<DeleteCommentResDto> {
            override fun onResponse(
                call: Call<DeleteCommentResDto>,
                response: Response<DeleteCommentResDto>
            ) {
                if(isViewDestroyed){
                    return
                }

                if(response.isSuccessful){
                    Toast.makeText(context, context?.getText(R.string.delete_comment_success), Toast.LENGTH_SHORT).show()

                    resetCommentData()
                    updateAdapterDataSetByFetchComment(FetchCommentReqDto(
                        null, null, postId, null, null
                    ))
                }else{
                    Toast.makeText(context, Util.getMessageFromErrorBody(response.errorBody()!!), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeleteCommentResDto>, t: Throwable) {
                if(isViewDestroyed){
                    return
                }

                Toast.makeText(context, t.message.toString(), Toast.LENGTH_SHORT).show()
            }
        })
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
                            // Set topCommentId
                            if(topCommentId == null){
                                topCommentId = it.first().id
                            }

                            it.map { item ->
                                item.contents = item.contents.replace("\n", "")
                                adapter.addItem(item)
                                setTopCommentId(item.id, adapter.itemCount-1)
                            }

                            // 데이터셋 변경 알림
                            binding.recyclerViewComment.post{
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }else{
                    Toast.makeText(context, Util.getMessageFromErrorBody(response.errorBody()!!), Toast.LENGTH_SHORT).show()
                }

                // 새로고침 아이콘 제거
                binding.layoutSwipeRefresh.isRefreshing = false
            }

            override fun onFailure(call: Call<FetchCommentResDto>, t: Throwable) {
                if(isViewDestroyed){
                    return
                }

                Toast.makeText(context, t.message.toString(), Toast.LENGTH_SHORT).show()

                // 새로고침 아이콘 제거
                binding.layoutSwipeRefresh.isRefreshing = false
            }
        })
    }

    private fun setTopCommentId(parentCommentId: Long, position: Int){
        // TODO: Comment에 reply_count Column이 생기면 그것에 맞춰 변경
        val body = FetchCommentReqDto(null, null, null, parentCommentId, null)
        val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!).fetchCommentReq(body)
        call!!.enqueue(object: Callback<FetchCommentResDto> {
            override fun onResponse(
                call: Call<FetchCommentResDto>,
                response: Response<FetchCommentResDto>
            ) {
                if(isViewDestroyed){
                    return
                }

                if(response.isSuccessful){
                    // 답글 불러오기 버튼
                    if(response.body()!!.commentList!!.count() > 0){
                        adapter.setTopCommentIdList(response.body()!!.commentList!!.first().id, position)
                        adapter.notifyItemChanged(position)
                    }
                }else{
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<FetchCommentResDto>, t: Throwable) {
                if(isViewDestroyed){
                    return
                }

                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(context, context?.getText(R.string.create_comment_success), Toast.LENGTH_SHORT).show()
                }else{
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }

                setCommentInputToNormal()
                setViewForReplyCancel()
            }

            override fun onFailure(call: Call<CreateCommentResDto>, t: Throwable) {
                if(isViewDestroyed){
                    return
                }

                Toast.makeText(context, t.message.toString(), Toast.LENGTH_SHORT).show()

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

    private fun setLoggedInAccountIdAndFetchAccountPhoto(){
        val body = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")
        val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!).fetchAccountReq(body)
        call!!.enqueue(object: Callback<FetchAccountResDto> {
            override fun onResponse(
                call: Call<FetchAccountResDto>,
                response: Response<FetchAccountResDto>
            ) {
                if(isViewDestroyed){
                    return
                }

                if(response.isSuccessful){
                    response.body()?.let{
                        loggedInAccount = Account(it.id, it.username, it.email, it.phone, null, it.marketing, it.nickname, it.photoUrl, it.userMessage)
                        if(!it.photoUrl.isNullOrEmpty()){
                            setAccountPhotoToImageView(it.id, binding.imageProfile)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<FetchAccountResDto>, t: Throwable) {
                // Do nothing
            }
        })
    }

    private fun setAccountPhotoToImageView(id: Long, imageView: ImageView) {
        val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .fetchAccountPhotoReq(FetchAccountPhotoReqDto(id))
        call.enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(isViewDestroyed){
                    return
                }

                if(response.isSuccessful) {
                    // convert photo to byte array + get bitmap
                    val photoByteArray = response.body()!!.byteStream().readBytes()
                    val photoBitmap = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)

                    // set account photo
                    imageView.setImageBitmap(photoBitmap)
                }
                else {
                    // get error message
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                    // Toast + Log
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // log error message
                Log.d("error", t.message.toString())
            }
        })
    }
}