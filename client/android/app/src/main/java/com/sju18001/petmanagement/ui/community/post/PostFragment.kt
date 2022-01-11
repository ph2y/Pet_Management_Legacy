package com.sju18001.petmanagement.ui.community.post

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.CustomProgressBar
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentPostBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Post
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.community.CommunityViewModel
import com.sju18001.petmanagement.ui.community.comment.CommentActivity
import com.sju18001.petmanagement.ui.community.post.createUpdatePost.CreateUpdatePostActivity
import com.sju18001.petmanagement.ui.community.post.generalFiles.GeneralFilesActivity
import wseemann.media.FFmpegMediaMetadataRetriever
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder

class PostFragment : Fragment() {
    // 외부에서 petId를 지정해줄 수 있다.
    companion object{
        @JvmStatic
        fun newInstance(petId: Long) = PostFragment().apply{
            arguments = Bundle().apply{
                putLong("petId", petId)
            }
        }
    }

    val communityViewModel: CommunityViewModel by activityViewModels()

    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!

    // constant variables
    private var COMMUNITY_DIRECTORY: String = "community"

    // 리싸이클러뷰
    private lateinit var adapter: PostListAdapter

    private var isViewDestroyed = false

    // 글 새로고침
    private var isLast = false
    private var topPostId: Long? = null
    private var pageIndex: Int = 1

    // For starting create post activity
    private val startForCreateResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_OK){
            result.data?.let{
                val postId = it.getLongExtra("postId", -1)
                if(postId != (-1).toLong()){
                    fetchOnePostAndInvoke(postId) { item ->
                        adapter.addItemToTop(item)
                        adapter.notifyItemInserted(0)
                        adapter.notifyItemRangeChanged(0, adapter.itemCount)

                        binding.recyclerViewPost.scrollToPosition(0)

                        setEmptyNotificationView(1)
                    }
                }
            }
        }
    }

    // For starting update post activity
    private val startForUpdateResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_OK){
            result.data?.let{
                val postId = it.getLongExtra("postId", -1)
                val position = it.getIntExtra("position", -1)

                if(postId != (-1).toLong() && position != -1){
                    fetchOnePostAndInvoke(postId) { item ->
                        adapter.setPost(position, item)
                        adapter.notifyItemChanged(position)
                    }
                }
            }
        }
    }

    private fun fetchOnePostAndInvoke(postId: Long, callback: ((Post)->Unit)){
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPostReq(FetchPostReqDto(null, null, null, postId))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            response.body()?.postList?.get(0)?.let{ item ->
                // 펫이 지정되어있지만 pet id가 다를 경우에는 invoke 하지 않음
                val petId = arguments?.getLong("petId")
                if(!(petId != null && petId != item.pet.id)) {
                    callback.invoke(item)
                }
            }
        }, {}, {})
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        initializeAdapter()

        // 첫 Fetch가 끝나기 전까지 ProgressBar 표시
        CustomProgressBar.addProgressBar(requireContext(), binding.fragmentPostParentLayout, 80, R.color.white)

        // 초기 Post 추가
        resetPostData()
        updateAdapterDataSet(FetchPostReqDto(null, null, arguments?.getLong("petId"), null))

        // SwipeRefreshLayout
        binding.layoutSwipeRefresh.setOnRefreshListener {
            resetPostData()
            updateAdapterDataSet(FetchPostReqDto(null, null, arguments?.getLong("petId"), null))
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true

        // Delete saved files
        Util.deleteCopiedFiles(requireContext(), COMMUNITY_DIRECTORY)
    }

    private fun initializeAdapter(){
        // 빈 배열로 초기화
        adapter = PostListAdapter(arrayListOf(), arrayListOf(), arrayListOf())

        // 인터페이스 구현
        adapter.communityPostListAdapterInterface = object: PostListAdapterInterface {
            override fun startCommentActivity(postId: Long) {
                val commentActivityIntent = Intent(context, CommentActivity::class.java)
                commentActivityIntent.putExtra("postId", postId)

                startActivity(commentActivityIntent)
                requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
            }

            override fun createLike(postId: Long, holder: PostListAdapter.ViewHolder, position: Int){
                val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                    .createLikeReq(CreateLikeReqDto(postId, null))
                ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
                    holder.likeCountTextView.text = ((holder.likeCountTextView.text).toString().toLong() + 1).toString()

                    adapter.setIsPostLiked(position, true)
                    adapter.showDeleteLikeButton(holder)
                }, {
                    adapter.setIsPostLiked(position, true)
                    adapter.showDeleteLikeButton(holder)
                }, {})
            }

            override fun deleteLike(postId: Long, holder: PostListAdapter.ViewHolder, position: Int) {
                val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                    .deleteLikeReq(DeleteLikeReqDto(postId, null))
                ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
                    holder.likeCountTextView.text = ((holder.likeCountTextView.text).toString().toLong() - 1).toString()

                    adapter.setIsPostLiked(position, false)
                    adapter.showCreateLikeButton(holder)
                }, {
                    adapter.setIsPostLiked(position, false)
                    adapter.showCreateLikeButton(holder)
                }, {})
            }

            override fun onClickPostFunctionButton(post: Post, position: Int) {
                val loggedInAccount = SessionManager.fetchLoggedInAccount(requireContext())!!

                if(loggedInAccount.id == post.author.id){
                    createPostDialogForAuthor(post, position)
                }else{
                    createPostDialogForNonAuthor()
                }
            }

            override fun setAccountPhoto(id: Long, holder: PostListAdapter.ViewHolder){
                val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                    .fetchAccountPhotoReq(FetchAccountPhotoReqDto(id))
                ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
                    // convert photo to byte array + get bitmap
                    val photoByteArray = response.body()!!.byteStream().readBytes()
                    val photoBitmap = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)

                    // set account photo
                    holder.accountPhotoImage.setImageBitmap(photoBitmap)
                }, {}, {})
            }

            override fun setAccountDefaultPhoto(holder: PostListAdapter.ViewHolder) {
                holder.accountPhotoImage.setImageDrawable(requireContext().getDrawable(R.drawable.ic_baseline_account_circle_24))
            }

            override fun setPostMedia(
                holder: PostListAdapter.PostMediaItemCollectionAdapter.ViewPagerHolder,
                id: Long,
                index: Int,
                url: String,
                dummyImageView: ConstraintLayout
            ) {
                // 더미 이미지 제거
                dummyImageView.visibility = View.GONE

                if(Util.isUrlVideo(url)){
                    // View
                    val postMediaVideo = holder.postMediaVideo
                    postMediaVideo.visibility = View.VISIBLE

                    // 영상의 비율을 유지한 채로, 영상의 사이즈를 가로로 꽉 채웁니다.
                    val encodedUrl = RetrofitBuilder.BASE_URL + "/api/post/video/fetch?url=" + URLEncoder.encode(url, "UTF8")

                    val retriever = FFmpegMediaMetadataRetriever()
                    retriever.setDataSource(encodedUrl)

                    val videoWidth = Integer.parseInt(retriever.extractMetadata(
                        FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
                    val videoHeight = Integer.parseInt(retriever.extractMetadata(
                        FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))

                    val screenWidth = Util.getScreenWidthInPixel(requireActivity())
                    val ratio: Float = screenWidth.toFloat() / videoWidth.toFloat()

                    postMediaVideo.layoutParams.height = (videoHeight.toFloat() * ratio).toInt()

                    // 반복 재생
                    postMediaVideo.setOnCompletionListener {
                        postMediaVideo.start()
                    }

                    // 재생
                    postMediaVideo.setVideoPath(encodedUrl)
                    postMediaVideo.requestFocus()
                    postMediaVideo.setOnPreparedListener {
                        postMediaVideo.start()
                    }
                }
                // 이미지
                else{
                    val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                        .fetchPostImageReq(FetchPostImageReqDto(id, index))
                    ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
                        // Convert photo to byte array + get bitmap
                        val photoByteArray = response.body()!!.byteStream().readBytes()
                        val photoBitmap = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)

                        // Set post image
                        val postMediaImage = holder.postMediaImage
                        postMediaImage.visibility = View.VISIBLE
                        postMediaImage.setImageBitmap(photoBitmap)

                        // 영상의 사이즈를 가로로 꽉 채우되, 비율을 유지합니다.
                        val screenWidth = Util.getScreenWidthInPixel(requireActivity())
                        val ratio: Float = screenWidth.toFloat() / photoBitmap.width.toFloat()
                        postMediaImage.layoutParams.height = (photoBitmap.height.toFloat() * ratio).toInt()
                    }, { dummyImageView.visibility = View.GONE }, { dummyImageView.visibility = View.GONE })
                }
            }

            override fun startGeneralFilesActivity(postId: Long, fileAttachments: String) {
                val generalFilesActivityIntent = Intent(context, GeneralFilesActivity::class.java)
                generalFilesActivityIntent.putExtra("postId", postId)
                generalFilesActivityIntent.putExtra("fileAttachments", fileAttachments)
                startActivity(generalFilesActivityIntent)
            }

            override fun getContext(): Context {
                return requireContext()
            }
        }

        binding.recyclerViewPost?.let{
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(activity)

            it.setItemViewCacheSize(30)

            // 스크롤하여, 최하단에 위치할 시 post 추가 로드
            it.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if(!recyclerView.canScrollVertically(1) && adapter.itemCount != 0 && !isLast){
                        updateAdapterDataSet(
                            FetchPostReqDto(
                            pageIndex, topPostId, arguments?.getLong("petId"), null
                        )
                        )
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

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                setEmptyNotificationView(adapter.itemCount)
            }
        })
    }

    private fun updateAdapterDataSet(body: FetchPostReqDto){
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPostReq(body)
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            isLast = response.body()!!.isLast == true

            response.body()!!.postList?.let {
                if(it.isNotEmpty()){
                    // Set topPostId
                    if(topPostId == null){
                        topPostId = it.first().id
                    }

                    // 데이터 추가
                    it.map { item ->
                        adapter.addItem(item)
                        setLiked(adapter.itemCount-1, item.id)
                    }
                    adapter.notifyDataSetChanged()

                    setEmptyNotificationView(response.body()?.postList?.size)
                }

                CustomProgressBar.removeProgressBar(binding.fragmentPostParentLayout)
                binding.layoutSwipeRefresh.isRefreshing = false
            }
        }, {
            CustomProgressBar.removeProgressBar(binding.fragmentPostParentLayout)
            binding.layoutSwipeRefresh.isRefreshing = false
        }, {
            CustomProgressBar.removeProgressBar(binding.fragmentPostParentLayout)
            binding.layoutSwipeRefresh.isRefreshing = false
        })
    }

    private fun setLiked(position: Int, postId: Long){
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchLikeReq(FetchLikeReqDto(postId, null))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            adapter.setLikedCount(position, response.body()!!.likedCount!!)

            val flag = response.body()!!.likedAccountIdList?.contains(SessionManager.fetchLoggedInAccount(requireContext())!!.id)?: false
            adapter.setIsPostLiked(position, flag)

            adapter.notifyItemChanged(position)
        }, {}, {})
    }

    private fun resetPostData(){
        topPostId = null
        pageIndex = 1
        adapter.resetItem()

        // 데이터셋 변경 알림
        binding.recyclerViewPost.post{
            adapter.notifyDataSetChanged()
        }
    }


    private fun createPostDialogForAuthor(post: Post, position: Int){
        val builder = AlertDialog.Builder(requireActivity())
        builder.setItems(arrayOf("수정", "삭제"), DialogInterface.OnClickListener{ _, which ->
            when(which){
                0 -> {
                    // 수정
                    val createUpdatePostActivityIntent = Intent(context, CreateUpdatePostActivity::class.java)
                    createUpdatePostActivityIntent.putExtra("fragmentType", "update_post")
                    createUpdatePostActivityIntent.putExtra("postId", post.id)
                    createUpdatePostActivityIntent.putExtra("position", position)
                    createUpdatePostActivityIntent.putExtra("originalMediaCount", Util.getArrayFromMediaAttachments(post.imageAttachments).size)
                    createUpdatePostActivityIntent.putExtra("originalGeneralFilesCount", Util.getArrayFromMediaAttachments(post.fileAttachments).size)

                    startForUpdateResult.launch(createUpdatePostActivityIntent)
                    requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
                }
                1 -> {
                    // 삭제
                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setMessage(getString(R.string.delete_post_dialog))
                        .setPositiveButton(
                            R.string.confirm,
                            DialogInterface.OnClickListener { _, _ -> deletePost(post.id, position) }
                        )
                        .setNegativeButton(
                            R.string.cancel,
                            DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() }
                        )
                        .create().show()
                }
            }
        })
            .create().show()
    }

    private fun deletePost(id: Long, position: Int){
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .deletePostReq(DeletePostReqDto(id))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), {
            // 데이터셋에서 삭제
            adapter.removeItem(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, adapter.itemCount)

            Toast.makeText(context, getString(R.string.delete_post_successful), Toast.LENGTH_LONG).show()
        }, {}, {})
    }

    private fun createPostDialogForNonAuthor(){
        val builder = AlertDialog.Builder(requireActivity())
        builder.setItems(arrayOf("신고"), DialogInterface.OnClickListener{ _, which ->
            when(which){
                0 -> {
                    // TODO: 기능 추가
                }
            }
        })
            .create().show()
    }


    fun startAllVideos(){
        val layoutManager = (binding.recyclerViewPost.layoutManager as LinearLayoutManager)
        val firstIndex = layoutManager.findFirstVisibleItemPosition()
        val lastIndex = layoutManager.findLastVisibleItemPosition()

        for(i in firstIndex..lastIndex){
            val videoPostMedia = layoutManager.findViewByPosition(i)?.findViewById<VideoView>(R.id.video_post_media)
            videoPostMedia?.start()
        }
    }

    fun pauseAllVideos(){
        val layoutManager = (binding.recyclerViewPost.layoutManager as LinearLayoutManager)
        val firstIndex = layoutManager.findFirstVisibleItemPosition()
        val lastIndex = layoutManager.findLastVisibleItemPosition()

        for(i in firstIndex..lastIndex){
            val videoPostMedia = layoutManager.findViewByPosition(i)?.findViewById<VideoView>(R.id.video_post_media)
            videoPostMedia?.pause()
        }
    }

    fun startCreatePostFragment(){
        val createUpdatePostActivityIntent = Intent(context, CreateUpdatePostActivity::class.java)
        createUpdatePostActivityIntent.putExtra("fragmentType", "create_post")

        startForCreateResult.launch(createUpdatePostActivityIntent)
        requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
    }

    private fun setEmptyNotificationView(itemCount: Int?) {
        // set notification view
        val visibility = if(itemCount != 0) View.GONE else View.VISIBLE
        binding.emptyPostListNotification.visibility = visibility
    }
}