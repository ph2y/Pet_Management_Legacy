package com.sju18001.petmanagement.ui.community

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.restapi.dao.Post
import com.sju18001.petmanagement.ui.community.comment.CommunityCommentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentCommunityBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.community.createUpdatePost.CreateUpdatePostActivity
import com.sju18001.petmanagement.ui.community.CommunityViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

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
            startActivity(Intent(context, CreateUpdatePostActivity::class.java))
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
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if(isViewDestroyed){
                            return
                        }

                        if(response.isSuccessful){
                            // 영상
                            if(url.endsWith(".mp4") || url.endsWith(".webm")){
                                // Save file by byte array
                                val dir = File(requireContext().getExternalFilesDir(null).toString() + "/pet_management")
                                if(! dir.exists()){
                                    dir.mkdir()
                                }

                                val file = if(url.endsWith(".mp4")){
                                    File.createTempFile("post_media", ".mp4", dir)
                                }else{
                                    File.createTempFile("post_media", ".webm", dir)
                                }
                                val os = FileOutputStream(file)
                                os.write(response.body()!!.byteStream().readBytes())
                                os.close()

                                // View
                                val postMediaVideo = holder.postMediaVideo
                                postMediaVideo.visibility = View.VISIBLE

                                // Controller
                                val mediaController = MediaController(requireContext())
                                mediaController.setAnchorView(postMediaVideo)

                                postMediaVideo.setMediaController(mediaController)
                                postMediaVideo.requestFocus()
                                postMediaVideo.setVideoURI(Uri.fromFile(file))
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