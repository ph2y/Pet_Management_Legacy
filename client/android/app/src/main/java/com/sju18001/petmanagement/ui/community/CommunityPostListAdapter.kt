package com.sju18001.petmanagement.ui.community

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.restapi.dao.Post
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Attachment

interface CommunityPostListAdapterInterface{
    fun startCommunityCommentActivity(postId: Long)
    fun startCreateUpdatePostActivity(postId: Long)
    fun setAccountPhoto(id: Long, holder: CommunityPostListAdapter.ViewHolder)
    fun setAccountDefaultPhoto(holder: CommunityPostListAdapter.ViewHolder)
    fun setPostMedia(holder: CommunityPostListAdapter.PostMediaItemCollectionAdapter.ViewPagerHolder, id: Long, index: Int, url: String)
}

private const val MAX_LINE = 5

class CommunityPostListAdapter(private var dataSet: ArrayList<Post>) : RecyclerView.Adapter<CommunityPostListAdapter.ViewHolder>() {
    lateinit var communityPostListAdapterInterface: CommunityPostListAdapterInterface

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val petPhotoImage: ImageView = view.findViewById(R.id.pet_photo)
        val nicknameTextView: TextView = view.findViewById(R.id.nickname)
        val petNameTextView: TextView = view.findViewById(R.id.pet_name)
        val viewPager: ViewPager2 = view.findViewById(R.id.view_pager)
        val contentsTextView: TextView = view.findViewById(R.id.contents)
        val viewMoreTextView: TextView = view.findViewById(R.id.view_more)
        val likeButton: ImageButton = view.findViewById(R.id.like_button)
        val commentButton: ImageButton = view.findViewById(R.id.comment_button)
        val likeCountTextView: TextView = view.findViewById(R.id.like_count)
        val updatePostButton: ImageButton = view.findViewById(R.id.update_post_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.community_post_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        updateDataSetToViewHolder(holder, dataSet[position])
        setViewMore(holder.contentsTextView, holder.viewMoreTextView)

        // 댓글 버튼
        holder.commentButton.setOnClickListener {
            communityPostListAdapterInterface.startCommunityCommentActivity(dataSet[position].id)
        }

        // update post button
        holder.updatePostButton.setOnClickListener {
            communityPostListAdapterInterface.startCreateUpdatePostActivity(dataSet[position].id)
        }
    }

    override fun getItemCount(): Int = dataSet.size

    private fun updateDataSetToViewHolder(holder: ViewHolder, data: Post){
        holder.nicknameTextView.text = data.author.nickname
        holder.petNameTextView.text = data.pet.name
        holder.contentsTextView.text = data.contents
        holder.likeCountTextView.text = "0"

        if(!data.author.photoUrl.isNullOrEmpty()){
            communityPostListAdapterInterface.setAccountPhoto(data.author.id, holder)
        }else{
            communityPostListAdapterInterface.setAccountDefaultPhoto(holder)
        }

        // Set ViewPager2
        if(!data.mediaAttachments.isNullOrEmpty()){
            val mediaAttachments: Array<Attachment> = Gson().fromJson(data.mediaAttachments, Array<Attachment>::class.java)

            holder.viewPager.adapter = CommunityPostListAdapter.PostMediaItemCollectionAdapter(communityPostListAdapterInterface, data.id, mediaAttachments, holder.viewPager)
            holder.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }else{
            holder.viewPager.adapter = CommunityPostListAdapter.PostMediaItemCollectionAdapter(communityPostListAdapterInterface, 0, arrayOf(), holder.viewPager)
        }
    }

    private fun setViewMore(contentsTextView: TextView, viewMoreTextView: TextView){
        // TextView 초기화
        contentsTextView.maxLines = MAX_LINE
        viewMoreTextView.visibility = View.GONE

        // getEllipsisCount()을 통한 더보기 표시 및 구현
        contentsTextView.post{
            val lineCount = contentsTextView.layout.lineCount
            if (lineCount > 0) {
                if (contentsTextView.layout.getEllipsisCount(lineCount - 1) > 0) {
                    // 더보기 표시
                    viewMoreTextView.visibility = View.VISIBLE

                    // 더보기 클릭 이벤트
                    viewMoreTextView.setOnClickListener {
                        contentsTextView.maxLines = Int.MAX_VALUE
                        viewMoreTextView.visibility = View.GONE
                    }
                }
            }
        }

    }

    fun addItem(item: Post){
        dataSet.add(item)
    }

    fun resetDataSet(){
        dataSet = arrayListOf()
    }

    class PostMediaItemCollectionAdapter(
        private var communityPostListAdapterInterface: CommunityPostListAdapterInterface,
        private val id: Long,
        private val mediaAttachments: Array<Attachment>,
        private val viewPager: ViewPager2
        ): RecyclerView.Adapter<PostMediaItemCollectionAdapter.ViewPagerHolder>() {
        override fun getItemCount(): Int = mediaAttachments.size

        inner class ViewPagerHolder(parent: ViewGroup): RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.post_media_item, parent, false)
        ){
            val postMediaImage: ImageView = itemView.findViewById(R.id.image_post_media)
            val postMediaVideo: VideoView = itemView.findViewById(R.id.video_post_media)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewPagerHolder(parent)

        override fun onBindViewHolder(holder: ViewPagerHolder, position: Int) {
            communityPostListAdapterInterface.setPostMedia(holder, id, position, mediaAttachments[position].url)

            // 페이지 전환 시 자동 재생
            viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if(holder.postMediaVideo.isVisible){
                        holder.postMediaVideo.start()
                    }
                }
            })

            // 클릭 -> 재생, 일시정지
            val postMediaVideo = holder.postMediaVideo
            postMediaVideo.setOnClickListener {
                if(postMediaVideo.isVisible){
                    if(postMediaVideo.isPlaying){
                        postMediaVideo.pause()
                    }else{
                        postMediaVideo.start()
                    }
                }
            }
        }
    }
}