package com.sju18001.petmanagement.ui.community

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCommunityBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Post
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.community.comment.CommunityCommentActivity
import com.sju18001.petmanagement.ui.community.comment.updateComment.UpdateCommentActivity
import com.sju18001.petmanagement.ui.community.createUpdatePost.CreateUpdatePostActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil


class CommunityFragment : Fragment() {
    val communityViewModel: CommunityViewModel by activityViewModels()

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    // constant variables
    private var COMMUNITY_DIRECTORY: String = "community"

    // 리싸이클러뷰
    private lateinit var adapter: CommunityPostListAdapter

    private var isViewDestroyed = false
    
    // 글 새로고침
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
        call.enqueue(object: Callback<FetchPostResDto> {
            override fun onResponse(
                call: Call<FetchPostResDto>,
                response: Response<FetchPostResDto>
            ) {
                if(isViewDestroyed) return

                if(response.isSuccessful){
                    response.body()?.postList?.get(0)?.let{ item ->
                        callback.invoke(item)
                    }
                }
            }

            override fun onFailure(call: Call<FetchPostResDto>, t: Throwable) {
                if(isViewDestroyed) return

                Util.showToastAndLog(requireContext(), t.message.toString())
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        initializeAdapter()

        // 초기 Post 추가
        resetPostData()
        updateAdapterDataSet(FetchPostReqDto(null, null, null, null))

        // SwipeRefreshLayout
        binding.layoutSwipeRefresh.setOnRefreshListener {
            resetPostData()
            updateAdapterDataSet(FetchPostReqDto(null, null, null, null))
        }

        // for create post FAB
        binding.createPostFab.setOnClickListener {
            val createUpdatePostActivityIntent = Intent(context, CreateUpdatePostActivity::class.java)
            createUpdatePostActivityIntent.putExtra("fragmentType", "create_post")

            startForCreateResult.launch(createUpdatePostActivityIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
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
        adapter = CommunityPostListAdapter(arrayListOf(), arrayListOf(), arrayListOf())

        // 인터페이스 구현
        adapter.communityPostListAdapterInterface = object: CommunityPostListAdapterInterface {
            override fun startCommunityCommentActivity(postId: Long) {
                val communityCommentActivityIntent = Intent(context, CommunityCommentActivity::class.java)
                communityCommentActivityIntent.putExtra("postId", postId)

                startActivity(communityCommentActivityIntent)
                requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
            }

            override fun createLike(postId: Long, holder: CommunityPostListAdapter.ViewHolder, position: Int){
                val body = CreateLikeReqDto(postId, null)
                val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!).createLikeReq(body)
                call.enqueue(object: Callback<CreateLikeResDto> {
                    override fun onResponse(
                        call: Call<CreateLikeResDto>,
                        response: Response<CreateLikeResDto>
                    ) {
                        if(isViewDestroyed) return

                        if(response.isSuccessful){
                            holder.likeCountTextView.text = ((holder.likeCountTextView.text).toString().toLong() + 1).toString()
                        }else{
                            Util.showToastAndLogForFailedResponse(requireContext(), response.errorBody())
                        }

                        adapter.setIsPostLiked(position, true)
                        adapter.showDeleteLikeButton(holder)
                    }

                    override fun onFailure(call: Call<CreateLikeResDto>, t: Throwable) {
                        if(isViewDestroyed) return

                        Util.showToastAndLog(requireContext(), t.message.toString())
                    }
                })
            }

            override fun deleteLike(postId: Long, holder: CommunityPostListAdapter.ViewHolder, position: Int) {
                val body = DeleteLikeReqDto(postId, null)
                val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!).deleteLikeReq(body)
                call.enqueue(object: Callback<DeleteLikeResDto> {
                    override fun onResponse(
                        call: Call<DeleteLikeResDto>,
                        response: Response<DeleteLikeResDto>
                    ) {
                        if(isViewDestroyed) return

                        if(response.isSuccessful){
                            holder.likeCountTextView.text = ((holder.likeCountTextView.text).toString().toLong() - 1).toString()
                        }else{
                            Util.showToastAndLogForFailedResponse(requireContext(), response.errorBody())
                        }

                        adapter.setIsPostLiked(position, false)
                        adapter.showCreateLikeButton(holder)
                    }

                    override fun onFailure(call: Call<DeleteLikeResDto>, t: Throwable) {
                        if(isViewDestroyed) return

                        Util.showToastAndLog(requireContext(), t.message.toString())
                    }
                })
            }

            override fun onClickPostFunctionButton(post: Post, position: Int) {
                val loggedInAccount = SessionManager.fetchLoggedInAccount(requireContext())!!

                if(loggedInAccount.id == post.author.id){
                    createPostDialogForAuthor(post, position)
                }else{
                    createPostDialogForNonAuthor()
                }
            }

            override fun setAccountPhoto(id: Long, holder: CommunityPostListAdapter.ViewHolder){
                val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                    .fetchAccountPhotoReq(FetchAccountPhotoReqDto(id))
                call.enqueue(object: Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if(isViewDestroyed) return

                        if(response.isSuccessful) {
                            // convert photo to byte array + get bitmap
                            val photoByteArray = response.body()!!.byteStream().readBytes()
                            val photoBitmap = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)

                            // set account photo
                            holder.petPhotoImage.setImageBitmap(photoBitmap)
                        }
                        else {
                            Util.showToastAndLogForFailedResponse(requireContext(), response.errorBody())
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Util.showToastAndLog(requireContext(), t.message.toString())
                    }
                })
            }

            override fun setAccountDefaultPhoto(holder: CommunityPostListAdapter.ViewHolder) {
                holder.petPhotoImage.setImageDrawable(requireContext().getDrawable(R.drawable.ic_baseline_account_circle_24))
            }

            override fun setPostMedia(
                holder: CommunityPostListAdapter.PostMediaItemCollectionAdapter.ViewPagerHolder,
                id: Long,
                index: Int,
                url: String
            ) {
                val body = FetchPostMediaReqDto(id, index)
                val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!).fetchPostMediaReq(body)
                call.enqueue(object: Callback<ResponseBody> {
                    @RequiresApi(Build.VERSION_CODES.R)
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if(isViewDestroyed) return

                        if(response.isSuccessful){
                            if(Util.isUrlVideo(url)){
                                // Save file
                                val dir = File(requireContext().getExternalFilesDir(null).toString() +
                                        File.separator + COMMUNITY_DIRECTORY)
                                if(! dir.exists()){
                                    dir.mkdir()
                                }

                                // 해당 파일 검색
                                val filePrefix = "${id}_${index}_"
                                val prevUri = dir.walk().find { it.name.startsWith(filePrefix) }

                                // 파일이 없을 때만 파일 생성
                                val uri: Uri = if(prevUri != null){
                                    prevUri.toUri()
                                }else{
                                    val file = File.createTempFile(
                                        filePrefix,
                                        ".${url.substringAfterLast(".", "")}",
                                        dir
                                    )

                                    val os = FileOutputStream(file)
                                    os.write(response.body()!!.byteStream().readBytes())
                                    os.close()

                                    Uri.fromFile(file)
                                }


                                // View
                                val postMediaVideo = holder.postMediaVideo
                                postMediaVideo.visibility = View.VISIBLE

                                // 영상의 비율을 유지한 채로, 영상의 사이즈를 가로로 꽉 채웁니다.
                                val retriever = MediaMetadataRetriever()
                                retriever.setDataSource(requireContext(), uri)

                                val videoWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
                                val videoHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))

                                val screenWidth = Util.getScreenWidthInPixel(requireActivity())
                                val ratio: Float = screenWidth.toFloat() / videoWidth.toFloat()

                                postMediaVideo.layoutParams.height = (videoHeight.toFloat() * ratio).toInt()

                                // 반복 재생
                                postMediaVideo.setOnCompletionListener {
                                    postMediaVideo.start()
                                }

                                // 재생
                                postMediaVideo.setVideoURI(uri)
                                postMediaVideo.requestFocus()
                                postMediaVideo.start()
                            }
                            // 이미지
                            else{
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
                            }
                        }else{
                            Util.showToastAndLogForFailedResponse(requireContext(), response.errorBody())
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        if(isViewDestroyed) return

                        Util.showToastAndLog(requireContext(), t.message.toString())
                    }
                })
            }

            override fun getContext(): Context {
                return requireContext()
            }
        }

        binding.recyclerViewPost?.let{
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(activity)
            
            // 스크롤하여, 최하단에 위치할 시 post 추가 로드
            it.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if(!recyclerView.canScrollVertically(1) && adapter.itemCount != 0){
                        updateAdapterDataSet(FetchPostReqDto(
                            pageIndex, topPostId, null, null
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

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                setEmptyNotificationView(adapter.itemCount)
            }
        })
    }

    private fun updateAdapterDataSet(body: FetchPostReqDto){
        // Fetch post
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .fetchPostReq(body)
        call.enqueue(object: Callback<FetchPostResDto> {
            override fun onResponse(
                call: Call<FetchPostResDto>,
                response: Response<FetchPostResDto>
            ) {
                if(isViewDestroyed) return

                if(response.isSuccessful){
                    // set notification view
                    val visibility = if(response.body()?.postList?.size != 0) View.GONE else View.VISIBLE
                    binding.emptyPostListNotification.visibility = visibility

                    response.body()!!.postList?.let {
                        if(it.isNotEmpty()){
                            // 추가로, 로딩 중에 뷰가 제거되면 오류(Inconsistency detected)가 나는데, 칼럼이 생긴 이후에도 발생 시 fix할 것

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
                        }
                    }
                }else{
                    Util.showToastAndLogForFailedResponse(requireContext(), response.errorBody())
                }

                binding.layoutSwipeRefresh.isRefreshing = false
            }

            override fun onFailure(call: Call<FetchPostResDto>, t: Throwable) {
                if(isViewDestroyed) return

                binding.layoutSwipeRefresh.isRefreshing = false

                Util.showToastAndLog(requireContext(), t.message.toString())
            }
        })
    }

    private fun setLiked(position: Int, postId: Long){
        val body = FetchLikeReqDto(postId, null)
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!).fetchLikeReq(body)
        call.enqueue(object: Callback<FetchLikeResDto> {
            override fun onResponse(
                call: Call<FetchLikeResDto>,
                response: Response<FetchLikeResDto>
            ) {
                if(isViewDestroyed) return

                if(response.isSuccessful){
                    adapter.setLikedCount(position, response.body()!!.likedCount!!)

                    val flag = response.body()!!.likedAccountIdList?.contains(SessionManager.fetchLoggedInAccount(requireContext())!!.id)?: false
                    adapter.setIsPostLiked(position, flag)

                    adapter.notifyItemChanged(position)
                }
            }

            override fun onFailure(call: Call<FetchLikeResDto>, t: Throwable) {
                // Do nothing
            }
        })
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
                    createUpdatePostActivityIntent.putExtra("originalMediaCount", Util.getArrayFromMediaAttachments(post.mediaAttachments).size)

                    startForUpdateResult.launch(createUpdatePostActivityIntent)
                    requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
                }
                1 -> {
                    // 삭제
                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setMessage(getString(R.string.delete_post_dialog))
                        .setPositiveButton(R.string.confirm,
                            DialogInterface.OnClickListener { _, _ -> deletePost(post.id, position) }
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

    private fun deletePost(id: Long, position: Int){
        val body = DeletePostReqDto(id)
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!).deletePostReq(body)
        call.enqueue(object: Callback<DeletePostResDto> {
            override fun onResponse(
                call: Call<DeletePostResDto>,
                response: Response<DeletePostResDto>
            ) {
                if(isViewDestroyed) return

                if(response.isSuccessful){
                    // 데이터셋에서 삭제
                    adapter.removeItem(position)
                    adapter.notifyItemRemoved(position)
                    adapter.notifyItemRangeChanged(position, adapter.itemCount)

                    Toast.makeText(context, getString(R.string.delete_post_successful), Toast.LENGTH_LONG).show()
                }else{
                    Util.showToastAndLogForFailedResponse(requireContext(), response.errorBody())
                }
            }

            override fun onFailure(call: Call<DeletePostResDto>, t: Throwable) {
                if(isViewDestroyed) return

                Util.showToastAndLog(requireContext(), t.message.toString())
            }
        })
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

    private fun setEmptyNotificationView(itemCount: Int?) {
        // set notification view
        val visibility = if(itemCount != 0) View.GONE else View.VISIBLE
        binding.emptyPostListNotification.visibility = visibility
    }
}