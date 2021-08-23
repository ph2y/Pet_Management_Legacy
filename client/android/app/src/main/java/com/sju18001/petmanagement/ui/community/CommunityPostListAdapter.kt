package com.sju18001.petmanagement.ui.community

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.restapi.dao.Post
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.restapi.global.FileMetaData

interface CommunityPostListAdapterInterface{
    fun startCommunityCommentActivity(postId: Long)
    fun createLike(postId: Long, holder: CommunityPostListAdapter.ViewHolder)
    fun deleteLike(postId: Long, holder: CommunityPostListAdapter.ViewHolder)
    fun onClickPostFunctionButton(postId: Long, authorId: Long, position: Int)
    fun setAccountPhoto(id: Long, holder: CommunityPostListAdapter.ViewHolder)
    fun setAccountDefaultPhoto(holder: CommunityPostListAdapter.ViewHolder)
    fun setPostMedia(holder: CommunityPostListAdapter.PostMediaItemCollectionAdapter.ViewPagerHolder, id: Long, index: Int, url: String)
    fun getContext(): Context
}

private const val MAX_LINE = 5

class CommunityPostListAdapter(private var dataSet: ArrayList<Post>, private var likedCounts: ArrayList<Long>) : RecyclerView.Adapter<CommunityPostListAdapter.ViewHolder>() {
    lateinit var communityPostListAdapterInterface: CommunityPostListAdapterInterface

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val petPhotoImage: ImageView = view.findViewById(R.id.pet_photo)
        val nicknameTextView: TextView = view.findViewById(R.id.nickname)
        val petNameTextView: TextView = view.findViewById(R.id.pet_name)
        val dialogButton: ImageButton = view.findViewById(R.id.dialog_button)

        val viewPager: ViewPager2 = view.findViewById(R.id.view_pager)
        val contentsTextView: TextView = view.findViewById(R.id.text_contents)
        val viewMoreTextView: TextView = view.findViewById(R.id.view_more)
        val tagRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_tag)

        val createLikeButton: ImageButton = view.findViewById(R.id.create_like_button)
        val deleteLikeButton: ImageButton = view.findViewById(R.id.delete_like_button)
        val commentButton: ImageButton = view.findViewById(R.id.comment_button)
        val likeCountTextView: TextView = view.findViewById(R.id.like_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val safePosition = holder.adapterPosition

        updateDataSetToViewHolder(holder, dataSet[safePosition], likedCounts[safePosition])
        setViewMore(holder.contentsTextView, holder.viewMoreTextView)

        // 댓글 버튼
        holder.commentButton.setOnClickListener {
            communityPostListAdapterInterface.startCommunityCommentActivity(dataSet[safePosition].id)
        }

        // 좋아요 버튼
        holder.createLikeButton.setOnClickListener {
            communityPostListAdapterInterface.createLike(dataSet[safePosition].id, holder)
        }
        
        // 좋아요 취소 버튼
        holder.deleteLikeButton.setOnClickListener {
            communityPostListAdapterInterface.deleteLike(dataSet[safePosition].id, holder)
        }

        // ... 버튼 -> Dialog 띄우기
        holder.dialogButton.setOnClickListener {
            communityPostListAdapterInterface.onClickPostFunctionButton(dataSet[safePosition].id, dataSet[safePosition].author.id, safePosition)
        }
    }

    override fun getItemCount(): Int = dataSet.size

    private fun updateDataSetToViewHolder(holder: ViewHolder, data: Post, likedCount: Long){
        holder.nicknameTextView.text = data.author.nickname
        holder.petNameTextView.text = data.pet.name
        holder.contentsTextView.text = data.contents
        holder.likeCountTextView.text = likedCount.toString()

        // Set account photo
        if(!data.author.photoUrl.isNullOrEmpty()){
            communityPostListAdapterInterface.setAccountPhoto(data.author.id, holder)
        }else{
            communityPostListAdapterInterface.setAccountDefaultPhoto(holder)
        }

        // Set ViewPager2
        if(!data.mediaAttachments.isNullOrEmpty()){
            val mediaAttachments: Array<FileMetaData> = Gson().fromJson(data.mediaAttachments, Array<FileMetaData>::class.java)

            holder.viewPager.adapter = CommunityPostListAdapter.PostMediaItemCollectionAdapter(communityPostListAdapterInterface, data.id, mediaAttachments, holder.viewPager)
            holder.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }else{
            holder.viewPager.adapter = CommunityPostListAdapter.PostMediaItemCollectionAdapter(communityPostListAdapterInterface, 0, arrayOf(), holder.viewPager)
        }

        // Set tag
        if(!data.serializedHashTags.isNullOrEmpty() && !data.serializedHashTags.isNullOrEmpty()){
            holder.tagRecyclerView.apply{
                visibility = View.VISIBLE
                adapter = PostTagListAdapter(ArrayList(data.serializedHashTags.split(',')))
                layoutManager = LinearLayoutManager(communityPostListAdapterInterface.getContext())
                (layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.HORIZONTAL
            }
        }else{
            holder.tagRecyclerView.apply {
                // 태그 있는 글을, 수정을 통해 태그를 제거 -> 글 새로고침 시 어뎁터가 그대로 남습니다.
                visibility = View.GONE
                adapter = PostTagListAdapter(arrayListOf())
            }
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

    fun addItem(post: Post){
        dataSet.add(post)

        // 기본값으로 추가
        likedCounts.add(0)
    }

    fun setLikedCount(position: Int, value: Long){
        likedCounts[position] = value
    }

    fun addLikedCount(position: Int, value: Int){
        likedCounts[position] = likedCounts[position] + value
    }

    fun removeItem(index: Int){
        dataSet.removeAt(index)
        likedCounts.removeAt(index)
    }

    fun resetItem(){
        dataSet = arrayListOf()
        likedCounts = arrayListOf()
    }

    fun showCreateLikeButton(holder: ViewHolder){
        holder.deleteLikeButton.visibility = View.GONE
        holder.createLikeButton.visibility = View.VISIBLE
    }

    fun showDeleteLikeButton(holder: ViewHolder){
        holder.createLikeButton.visibility = View.GONE
        holder.deleteLikeButton.visibility = View.VISIBLE
    }

    class PostMediaItemCollectionAdapter(
        private var communityPostListAdapterInterface: CommunityPostListAdapterInterface,
        private val id: Long,
        private val mediaAttachments: Array<FileMetaData>,
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