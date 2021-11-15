package com.sju18001.petmanagement.ui.community.comment

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.CustomProgressBar
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCommentBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.community.CommunityUtil
import com.sju18001.petmanagement.ui.community.comment.updateComment.UpdateCommentActivity

class CommentFragment : Fragment() {
    private var _binding: FragmentCommentBinding? = null
    private val binding get() = _binding!!

    // variable for ViewModel
    val commentViewModel: CommentViewModel by activityViewModels()

    // For starting update comment activity
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_OK){
            result.data?.let{
                val newContents = it.getStringExtra("newContents")?: ""
                val position = it.getIntExtra("position", -1)

                adapter.updateCommentContents(newContents, position)
                adapter.notifyItemChanged(position)
            }
        }
    }

    // 리싸이클러뷰
    private lateinit var adapter: CommentListAdapter

    private var isViewDestroyed = false

    // 댓글 새로고침
    private var isLast = false
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
        _binding = FragmentCommentBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        // postId 지정
        postId = requireActivity().intent.getLongExtra("postId", -1)

        // 초기화
        initializeViewForViewModel()
        initializeAdapter()

        // 첫 Fetch가 끝나기 전까지 ProgressBar 표시
        CustomProgressBar.addProgressBar(requireContext(), binding.fragmentCommentParentLayout, 80, R.color.white)

        // 초기 Comment 추가
        resetCommentData()
        updateAdapterDataSetByFetchComment(FetchCommentReqDto(
            null, null, postId, null, null
        ))

        setListenerOnViews()
        setLoggedInAccountIdAndFetchAccountPhoto()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true
    }

    private fun initializeViewForViewModel(){
        val idForReply = commentViewModel.idForReply
        val nicknameForReply = commentViewModel.nicknameForReply

        if(idForReply != null && nicknameForReply != null){
            setViewForReply(idForReply, nicknameForReply)
        }
    }

    private fun initializeAdapter(){
        adapter = CommentListAdapter(arrayListOf(), arrayListOf(), arrayListOf())
        adapter.commentListAdapterInterface = object: CommentListAdapterInterface{
            override fun getActivity(): Activity {
                return requireActivity()
            }

            override fun onClickReply(id: Long, nickname: String) {
                setViewForReply(id, nickname)
            }

            override fun onLongClickComment(authorId: Long, commentId: Long, commentContents: String, position: Int){
                if(loggedInAccount.id == authorId){
                    showCommentDialog(commentId, commentContents, position)
                }
            }

            override fun setAccountPhoto(id: Long, holder: CommentListAdapter.ViewHolder) {
                setAccountPhotoToImageView(id, holder.profileImage)
            }

            override fun setAccountDefaultPhoto(holder: CommentListAdapter.ViewHolder) {
                holder.profileImage.setImageDrawable(requireContext().getDrawable(R.drawable.ic_baseline_account_circle_24))
            }

            override fun fetchReplyComment(pageIndex: Int, topReplyId: Long?, parentCommentId: Long, position: Int){
                val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                    .fetchCommentReq(FetchCommentReqDto(pageIndex, topReplyId, null, parentCommentId, null))
                ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
                    response.body()?.let{
                        // Set topReplyId
                        if(topReplyId == null){
                            adapter.setTopReplyIdList(it.commentList?.first()?.id, position)
                        }

                        // 더이상 불러올 답글이 없을 시
                        if(it.isLast == true){
                            adapter.setTopReplyIdList(-1, position)
                            adapter.notifyItemChanged(position)

                            Toast.makeText(requireContext(), getString(R.string.no_more_reply), Toast.LENGTH_SHORT).show()
                        }

                        // Add replies to RecyclerView
                        it.commentList?.let{ item ->
                            val replyCount = item.count()
                            for(i in 0 until replyCount){
                                item[i].contents = item[i].contents.replace("\n", "")
                                adapter.addItemOnPosition(item[i], position+1)
                            }

                            adapter.notifyItemRangeInserted(position + 1, replyCount)
                        }
                    }
                }, {}, {})
            }

            override fun startPetProfile(author: Account) {
                CommunityUtil.fetchRepresentativePetAndStartPetProfile(requireContext(), author, isViewDestroyed)
            }
        }

        binding.recyclerViewComment?.let{
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(activity)

            // 스크롤하여, 최하단에 위치할 시 comment 추가 로드
            it.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if(!recyclerView.canScrollVertically(1) && !isLast){
                        updateAdapterDataSetByFetchComment(FetchCommentReqDto(
                            pageIndex, topCommentId, postId, null, null
                        ))
                        pageIndex += 1
                    }
                }
            })
        }

        // set adapter item change observer
        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()

                setEmptyNotificationView(adapter.itemCount)
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)

                setEmptyNotificationView(adapter.itemCount)
            }
        })
    }

    private fun setViewForReply(id: Long, nickname: String) {
        Util.showKeyboard(requireActivity(), binding.editTextComment)

        // Set view, data for layout_reply_description
        binding.layoutReplyDescription.visibility = View.VISIBLE
        nickname?.let{
            binding.textReplyNickname.text = it
            commentViewModel.nicknameForReply = it
        }
        commentViewModel.idForReply = id

        binding.buttonReplyCancel.setOnClickListener {
            setViewForReplyCancel()
        }
    }

    private fun showCommentDialog(id: Long, contents: String, position: Int){
        val builder = AlertDialog.Builder(requireActivity())
        builder.setItems(arrayOf("수정", "삭제"), DialogInterface.OnClickListener{ _, which ->
            when(which){
                0 -> {
                    // 수정
                    val updateCommunityActivityIntent = Intent(context, UpdateCommentActivity::class.java)
                    updateCommunityActivityIntent.putExtra("id", id)
                    updateCommunityActivityIntent.putExtra("contents", contents)
                    updateCommunityActivityIntent.putExtra("position", position)

                    startForResult.launch(updateCommunityActivityIntent)
                    requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
                }
                1 -> {
                    // 삭제
                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setMessage(getString(R.string.delete_comment_dialog))
                        .setPositiveButton(R.string.confirm,
                            DialogInterface.OnClickListener { _, _ -> deleteComment(id, position) }
                        )
                        .setNegativeButton(R.string.cancel,
                            DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() }
                        )
                        .create().show()
                }
            }
        })
            .create().show()
    }

    private fun deleteComment(id: Long, position: Int){
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .deleteCommentReq(DeleteCommentReqDto(id))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
            Toast.makeText(context, context?.getText(R.string.delete_comment_success), Toast.LENGTH_SHORT).show()

            adapter.removeItem(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, adapter.itemCount)
        }, {}, {})
    }

    private fun setViewForReplyCancel(){
        binding.layoutReplyDescription.visibility = View.GONE
        commentViewModel.idForReply = null
        commentViewModel.nicknameForReply = ""
    }

    private fun updateAdapterDataSetByFetchComment(body: FetchCommentReqDto){
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchCommentReq(body)
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            isLast = response.body()!!.isLast == true

            response.body()!!.commentList?.let {
                if(it.isNotEmpty()){
                    // Set topCommentId
                    if(topCommentId == null){
                        topCommentId = it.first().id
                    }

                    it.map { item ->
                        item.contents = item.contents.replace("\n", "")
                        adapter.addItem(item)
                        if(item.childCommentCnt > 0){
                            adapter.setTopReplyIdList(null, adapter.itemCount-1)
                            adapter.notifyItemChanged(adapter.itemCount-1)
                        }
                    }

                    // 데이터셋 변경 알림
                    binding.recyclerViewComment.post{
                        adapter.notifyDataSetChanged()
                    }
                }
            }

            // 새로고침 아이콘 제거
            CustomProgressBar.removeProgressBar(binding.fragmentCommentParentLayout)
            binding.layoutSwipeRefresh.isRefreshing = false
        }, {
            CustomProgressBar.removeProgressBar(binding.fragmentCommentParentLayout)
            binding.layoutSwipeRefresh.isRefreshing = false
        }, {
            CustomProgressBar.removeProgressBar(binding.fragmentCommentParentLayout)
            binding.layoutSwipeRefresh.isRefreshing = false
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
        Util.setupViewsForHideKeyboard(requireActivity(), binding.fragmentCommentParentLayout)
        
        // 댓글 / 답글 생성
        binding.buttonCreateComment.setOnClickListener {
            createComment(CreateCommentReqDto(postId, commentViewModel.idForReply, binding.editTextComment.text.toString()))
        }

        // 키보드 동작
        binding.editTextComment.setOnEditorActionListener{ _, _, _ ->
            createComment(CreateCommentReqDto(postId, commentViewModel.idForReply, binding.editTextComment.text.toString()))
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
        lockViews()

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .createCommentReq(body)
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
            // 생성된 댓글의 id로 FetchComment
            val callForFetchingComment = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .fetchCommentReq(FetchCommentReqDto(null, null, null, null, it.body()!!.id))
            ServerUtil.enqueueApiCall(callForFetchingComment, {isViewDestroyed}, requireContext(), { it2 ->
                // RecyclerView에 추가
                it2.body()!!.commentList?.get(0)?.let { item ->
                    // 생성된 댓글이 답글일 경우
                    if(item.parentCommentId != null){
                        resetCommentData()
                        updateAdapterDataSetByFetchComment(FetchCommentReqDto(
                            null, null, postId, null, null
                        ))
                    }else{
                        adapter.addItemOnPosition(item, 0)
                        adapter.notifyItemInserted(0)
                        adapter.notifyItemRangeChanged(0, adapter.itemCount)

                        binding.recyclerViewComment.scrollToPosition(0)
                    }
                }

                binding.editTextComment.text = null
                Toast.makeText(context, context?.getText(R.string.create_comment_success), Toast.LENGTH_SHORT).show()

                unlockViews()
                setViewForReplyCancel()
            }, {}, {})
        }, {
            unlockViews()
            setViewForReplyCancel()
        }, {
            unlockViews()
            setViewForReplyCancel()
        })
    }

    private fun lockViews(){
        binding.buttonCreateComment.visibility = View.GONE
        binding.progressBarComment.visibility = View.VISIBLE

        binding.editTextComment.isEnabled = false
        binding.editTextComment.isEnabled = false
    }

    private fun unlockViews(){
        binding.buttonCreateComment.visibility = View.VISIBLE
        binding.progressBarComment.visibility = View.GONE

        binding.editTextComment.isEnabled = true
        binding.editTextComment.isEnabled = true
    }

    private fun setLoggedInAccountIdAndFetchAccountPhoto(){
        loggedInAccount = SessionManager.fetchLoggedInAccount(requireContext())!!
        if(!loggedInAccount.photoUrl.isNullOrEmpty()){
            setAccountPhotoToImageView(loggedInAccount.id, binding.imageProfile)
        }
    }

    private fun setAccountPhotoToImageView(id: Long, imageView: ImageView) {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchAccountPhotoReq(FetchAccountPhotoReqDto(id))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            // convert photo to byte array + get bitmap
            val photoByteArray = response.body()!!.byteStream().readBytes()
            val photoBitmap = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)

            // set account photo
            imageView.setImageBitmap(photoBitmap)
        }, {}, {})
    }

    private fun setEmptyNotificationView(itemCount: Int?) {
        // set notification view
        val visibility = if(itemCount != 0) View.GONE else View.VISIBLE
        binding.emptyCommentListNotification.visibility = visibility
    }
}