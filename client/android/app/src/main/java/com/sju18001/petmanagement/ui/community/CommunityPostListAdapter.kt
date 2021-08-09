package com.sju18001.petmanagement.ui.community

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.restapi.dao.Post
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.sju18001.petmanagement.R

interface CommunityPostListAdapterInterface{
    fun startCommunityCommentActivity()
}

private const val MAX_LINE = 5

class CommunityPostListAdapter(private var dataSet: ArrayList<Post>) : RecyclerView.Adapter<CommunityPostListAdapter.ViewHolder>() {
    lateinit var communityPostListAdapterInterface: CommunityPostListAdapterInterface

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val nicknameTextView: TextView = view.findViewById(R.id.nickname)
        val petNameTextView: TextView = view.findViewById(R.id.pet_name)
        val contentTextView: TextView = view.findViewById(R.id.content)
        val viewMoreTextView: TextView = view.findViewById(R.id.view_more)
        val likeButton: ImageButton = view.findViewById(R.id.like_button)
        val commentButton: ImageButton = view.findViewById(R.id.comment_button)
        val likeCountTextView: TextView = view.findViewById(R.id.like_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.community_post_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        updateDataSetToViewHolder(holder, dataSet[position])
        setViewMore(holder.contentTextView, holder.viewMoreTextView)

        // 댓글 버튼
        holder.commentButton.setOnClickListener {
            communityPostListAdapterInterface.startCommunityCommentActivity()
        }
    }

    override fun getItemCount(): Int = dataSet.size

    private fun updateDataSetToViewHolder(holder: ViewHolder, data: Post){
        holder.nicknameTextView.text = data.nickname
        holder.petNameTextView.text = data.petName
        holder.contentTextView.text = data.content
        holder.likeCountTextView.text = data.like.toString()
    }

    private fun setViewMore(contentTextView: TextView, viewMoreTextView: TextView){
        // TextView 초기화
        contentTextView.maxLines = MAX_LINE
        viewMoreTextView.visibility = View.GONE

        // getEllipsisCount()을 통한 더보기 표시 및 구현
        contentTextView.post{
            val lineCount = contentTextView.layout.lineCount
            if (lineCount > 0) {
                if (contentTextView.layout.getEllipsisCount(lineCount - 1) > 0) {
                    // 더보기 표시
                    viewMoreTextView.visibility = View.VISIBLE

                    // 더보기 클릭 이벤트
                    viewMoreTextView.setOnClickListener {
                        contentTextView.maxLines = Int.MAX_VALUE
                        viewMoreTextView.visibility = View.GONE
                    }
                }
            }
        }

    }

    fun addItems(items: List<Post>){
        for(item in items){
            dataSet.add(item)
        }
    }
}