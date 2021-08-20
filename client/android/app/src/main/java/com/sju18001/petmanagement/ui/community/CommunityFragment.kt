package com.sju18001.petmanagement.ui.community

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
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
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
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.community.comment.CommunityCommentActivity
import com.sju18001.petmanagement.ui.community.comment.updateComment.UpdateCommentActivity
import com.sju18001.petmanagement.ui.community.createUpdatePost.CreateUpdatePostActivity
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
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)

        // get session manager
        sessionManager = context?.let { SessionManager(it) }!!

        // 어뎁터 초기화
        initializeAdapter()

        // 초기 post 추가
        resetPostData()
        updateAdapterDataSetByFetchPost(FetchPostReqDto(null, null, null, null))

        // SwipeRefreshLayout
        binding.layoutSwipeRefresh.setOnRefreshListener {
            resetPostData()
            updateAdapterDataSetByFetchPost(FetchPostReqDto(null, null, null, null))
        }

        // for create post FAB
        binding.createPostFab.setOnClickListener {
            val createUpdatePostActivityIntent = Intent(context, CreateUpdatePostActivity::class.java)
            createUpdatePostActivityIntent.putExtra("fragmentType", "create_post")
            startActivity(createUpdatePostActivityIntent)
            requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true

        // Delete saved files
        val dir = File(requireContext().getExternalFilesDir(null).toString() + "/pet_management")
        dir.deleteRecursively()
    }

    private fun initializeAdapter(){
        // 빈 배열로 초기화
        adapter = CommunityPostListAdapter(arrayListOf())

        // 인터페이스 구현
        adapter.communityPostListAdapterInterface = object: CommunityPostListAdapterInterface {
            override fun startCommunityCommentActivity(postId: Long) {
                val communityCommentActivityIntent = Intent(context, CommunityCommentActivity::class.java)
                communityCommentActivityIntent.putExtra("postId", postId)

                startActivity(communityCommentActivityIntent)
                requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
            }

            override fun onClickPostFunctionButton(postId: Long, authorId: Long, position: Int) {
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
                            // 글 작성자 == 현재 로그인해있는 계정
                            if(response.body()!!.id == authorId){
                                createPostDialogForAuthor(postId, position)
                            }else{
                                createPostDialogForNonAuthor()
                            }
                        }
                    }

                    override fun onFailure(call: Call<FetchAccountResDto>, t: Throwable) {
                        createPostDialogForNonAuthor()
                    }
                })
            }

            override fun setAccountPhoto(id: Long, holder: CommunityPostListAdapter.ViewHolder){
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

                            // set account photo + save photo value
                            holder.petPhotoImage.setImageBitmap(photoBitmap)
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
                val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!).fetchPostMediaReq(body)
                call!!.enqueue(object: Callback<ResponseBody> {
                    @RequiresApi(Build.VERSION_CODES.R)
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if(isViewDestroyed){
                            return
                        }

                        if(response.isSuccessful){
                            // 영상
                            if(Util.isUrlVideo(url)){
                                // Save file
                                val dir = File(requireContext().getExternalFilesDir(null).toString() + "/pet_management")
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
                            // get error message
                            val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                            // Toast + Log
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        if(isViewDestroyed){
                            return
                        }

                        Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                    }
                })
            }
        }

        binding.recyclerViewPost?.let{
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(activity)
            
            // 스크롤하여, 최하단에 위치할 시 post 추가 로드
            it.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if(!recyclerView.canScrollVertically(1) && adapter.itemCount != 0){
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


    private fun createPostDialogForAuthor(postId: Long, position: Int){
        val builder = AlertDialog.Builder(requireActivity())
        builder.setItems(arrayOf("수정", "삭제"), DialogInterface.OnClickListener{ _, which ->
            when(which){
                0 -> {
                    // 수정
                    startCreateUpdatePostActivity(postId)
                }
                1 -> {
                    // 삭제
                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setMessage(getString(R.string.post_delete_dialog))
                        .setPositiveButton(R.string.confirm,
                            DialogInterface.OnClickListener { _, _ -> deletePost(postId, position) }
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

    private fun startCreateUpdatePostActivity(postId: Long) {
        val createUpdatePostActivityIntent = Intent(context, CreateUpdatePostActivity::class.java)
        createUpdatePostActivityIntent.putExtra("fragmentType", "update_post")
        createUpdatePostActivityIntent.putExtra("postId", postId)
        startActivity(createUpdatePostActivityIntent)
        requireActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
    }

    private fun deletePost(id: Long, position: Int){
        val body = DeletePostReqDto(id)
        val call = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!).deletePostReq(body)
        call!!.enqueue(object: Callback<DeletePostResDto> {
            override fun onResponse(
                call: Call<DeletePostResDto>,
                response: Response<DeletePostResDto>
            ) {
                if(isViewDestroyed){
                    return
                }

                if(response.isSuccessful){
                    // 데이터셋에서 삭제
                    adapter.removeItem(position)
                    adapter.notifyItemRemoved(position)

                    Toast.makeText(context, getString(R.string.delete_post_successful), Toast.LENGTH_LONG).show()
                }else{
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<DeletePostResDto>, t: Throwable) {
                if(isViewDestroyed){
                    return
                }

                Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
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
}