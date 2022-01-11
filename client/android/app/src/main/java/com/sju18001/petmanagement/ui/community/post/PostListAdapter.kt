package com.sju18001.petmanagement.ui.community.post

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.restapi.dao.Post
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.CustomProgressBar
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.restapi.global.FileMetaData
import com.sju18001.petmanagement.ui.community.CommunityUtil

interface PostListAdapterInterface{
    fun startCommentActivity(postId: Long)
    fun createLike(postId: Long, holder: PostListAdapter.ViewHolder, position: Int)
    fun deleteLike(postId: Long, holder: PostListAdapter.ViewHolder, position: Int)
    fun onClickPostFunctionButton(post: Post, position: Int)
    fun setAccountPhoto(id: Long, holder: PostListAdapter.ViewHolder)
    fun setAccountDefaultPhoto(holder: PostListAdapter.ViewHolder)
    fun setPostMedia(holder: PostListAdapter.PostMediaItemCollectionAdapter.ViewPagerHolder, id: Long, index: Int, url: String, dummyImageView: ConstraintLayout)
    fun startGeneralFilesActivity(postId: Long, fileAttachments: String)
    fun getContext(): Context
}

private const val MAX_LINE = 5

class PostListAdapter(private var dataSet: ArrayList<Post>, private var likedCounts: ArrayList<Long>, private var isPostLiked: ArrayList<Boolean>) : RecyclerView.Adapter<PostListAdapter.ViewHolder>() {
    lateinit var communityPostListAdapterInterface: PostListAdapterInterface

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val accountPhotoImage: ImageView = view.findViewById(R.id.account_photo)
        val nicknameTextView: TextView = view.findViewById(R.id.nickname)
        val petNameTextView: TextView = view.findViewById(R.id.pet_name)
        val layoutUserInfo: ConstraintLayout = view.findViewById(R.id.layout_user_info)
        val dialogButton: ImageButton = view.findViewById(R.id.dialog_button)

        val dummyLayout: ConstraintLayout = view.findViewById(R.id.layout_dummy)
        val viewPager: ViewPager2 = view.findViewById(R.id.view_pager)
        val contentsTextView: TextView = view.findViewById(R.id.text_contents)
        val viewMoreTextView: TextView = view.findViewById(R.id.view_more)
        val tagRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view_tag)

        val createLikeButton: ImageButton = view.findViewById(R.id.create_like_button)
        val deleteLikeButton: ImageButton = view.findViewById(R.id.delete_like_button)
        val commentButton: ImageButton = view.findViewById(R.id.comment_button)
        val generalFilesButton: ImageButton = view.findViewById(R.id.general_files_button)
        val likeCountTextView: TextView = view.findViewById(R.id.like_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_item, parent, false)

        val holder = ViewHolder(view)
        setListenerOnView(holder)

        return holder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val safePosition = holder.absoluteAdapterPosition
        updateDataSetToViewHolder(holder, dataSet[safePosition], likedCounts[safePosition], safePosition)
    }

    override fun getItemCount(): Int = dataSet.size

    private fun updateDataSetToViewHolder(holder: ViewHolder, data: Post, likedCount: Long, position: Int){
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

        // 좋아요 버튼 세팅
        if(isPostLiked[position]){
            showDeleteLikeButton(holder)
        }else{
            showCreateLikeButton(holder)
        }

        // set general files button
        holder.generalFilesButton.visibility = if(data.fileAttachments.isNullOrEmpty()) View.GONE else View.VISIBLE

        // 저장된 데이터에 따라, ViewPager, Tag, ViewMore 셋팅
        setViewPager(holder, data)
        setTag(holder, data)
        setViewMore(holder.contentsTextView, holder.viewMoreTextView)
    }

    private fun setViewPager(holder: ViewHolder, data: Post){
        if(!data.imageAttachments.isNullOrEmpty() || !data.videoAttachments.isNullOrEmpty()){
            val imageAttachments = Util.getArrayFromMediaAttachments(data.imageAttachments)
            val videoAttachments = Util.getArrayFromMediaAttachments(data.videoAttachments)
            val mediaAttachments = imageAttachments + videoAttachments

            // 더미 이미지 생성
            holder.dummyLayout.visibility = View.VISIBLE

            holder.viewPager.visibility = View.VISIBLE
            holder.viewPager.adapter = PostListAdapter.PostMediaItemCollectionAdapter(communityPostListAdapterInterface, data.id, mediaAttachments, holder)
            holder.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }else{
            holder.viewPager.visibility = View.GONE
            holder.viewPager.adapter = PostListAdapter.PostMediaItemCollectionAdapter(communityPostListAdapterInterface, 0, arrayOf(), holder)
        }
    }

    private fun setTag(holder: ViewHolder, data: Post){
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
        contentsTextView.post {
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setListenerOnView(holder: ViewHolder){
        // 프로필 이동
        holder.accountPhotoImage.setOnClickListener {
            val item = dataSet[holder.absoluteAdapterPosition]
            CommunityUtil.startPetProfileFragmentFromCommunity(holder.itemView.context, item.pet, item.author)
        }
        holder.layoutUserInfo.setOnClickListener {
            val item = dataSet[holder.absoluteAdapterPosition]
            CommunityUtil.startPetProfileFragmentFromCommunity(holder.itemView.context, item.pet, item.author)
        }

        // 댓글 버튼
        holder.commentButton.setOnClickListener {
            val item = dataSet[holder.absoluteAdapterPosition]
            communityPostListAdapterInterface.startCommentActivity(item.id)
        }

        // 좋아요 버튼
        holder.createLikeButton.setOnClickListener {
            val position = holder.absoluteAdapterPosition
            val item = dataSet[position]
            communityPostListAdapterInterface.createLike(item.id, holder, position)
        }

        // 좋아요 취소 버튼
        holder.deleteLikeButton.setOnClickListener {
            val position = holder.absoluteAdapterPosition
            val item = dataSet[position]
            communityPostListAdapterInterface.deleteLike(item.id, holder, position)
        }

        // general files button
        holder.generalFilesButton.setOnClickListener {
            val item = dataSet[holder.absoluteAdapterPosition]
            communityPostListAdapterInterface.startGeneralFilesActivity(item.id, item.fileAttachments!!)
        }

        // ... 버튼 -> Dialog 띄우기
        holder.dialogButton.setOnClickListener {
            val position = holder.absoluteAdapterPosition
            val item = dataSet[position]
            communityPostListAdapterInterface.onClickPostFunctionButton(item, position)
        }
    }

    fun addItem(post: Post){
        dataSet.add(post)

        // 기본값으로 추가
        likedCounts.add(0)
        isPostLiked.add(false)
    }

    fun addItemToTop(post: Post){
        dataSet.add(0, post)

        // 기본값으로 추가
        likedCounts.add(0, 0)
        isPostLiked.add(0, false)
    }

    fun removeItem(index: Int){
        dataSet.removeAt(index)
        likedCounts.removeAt(index)
        isPostLiked.removeAt(index)
    }

    fun resetItem(){
        dataSet = arrayListOf()
        likedCounts = arrayListOf()
        isPostLiked = arrayListOf()
    }

    fun setLikedCount(position: Int, value: Long){
        likedCounts[position] = value
    }

    fun setIsPostLiked(position: Int, flag: Boolean){
        isPostLiked[position] = flag
    }

    fun setPost(position: Int, post: Post){
        dataSet[position] = post
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
        private var communityPostListAdapterInterface: PostListAdapterInterface,
        private val id: Long,
        private val mediaAttachments: Array<FileMetaData>,
        private val parentHolder: PostListAdapter.ViewHolder
        ): RecyclerView.Adapter<PostMediaItemCollectionAdapter.ViewPagerHolder>() {
        private val viewPager = parentHolder.viewPager
        override fun getItemCount(): Int = mediaAttachments.size

        inner class ViewPagerHolder(parent: ViewGroup): RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.post_media_item, parent, false)
        ){
            val postMediaImage: ImageView = itemView.findViewById(R.id.image_post_media)
            val postMediaVideo: VideoView = itemView.findViewById(R.id.video_post_media)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerHolder{
            val holder = ViewPagerHolder(parent)
            setListenerOnView(holder)

            return holder
        }

        override fun onBindViewHolder(holder: ViewPagerHolder, position: Int) {
            communityPostListAdapterInterface.setPostMedia(holder, id, position, mediaAttachments[position].url, parentHolder.dummyLayout)
        }

        private fun setListenerOnView(holder: ViewPagerHolder) {
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