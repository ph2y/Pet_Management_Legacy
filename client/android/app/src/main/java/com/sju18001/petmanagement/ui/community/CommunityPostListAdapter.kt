package com.sju18001.petmanagement.ui.community

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.restapi.dao.Post
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.FetchAccountPhotoReqDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface CommunityPostListAdapterInterface{
    fun startCommunityCommentActivity(postId: Long)
    fun startCreateUpdatePostActivity(postId: Long)
    fun setAccountPhoto(id: Long, holder: CommunityPostListAdapter.ViewHolder)
    fun setAccountDefaultPhoto(holder: CommunityPostListAdapter.ViewHolder)
}

private const val MAX_LINE = 5

class CommunityPostListAdapter(private var dataSet: ArrayList<Post>) : RecyclerView.Adapter<CommunityPostListAdapter.ViewHolder>() {
    lateinit var communityPostListAdapterInterface: CommunityPostListAdapterInterface

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val petPhotoImage: ImageView = view.findViewById(R.id.pet_photo)
        val nicknameTextView: TextView = view.findViewById(R.id.nickname)
        val petNameTextView: TextView = view.findViewById(R.id.pet_name)
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
}