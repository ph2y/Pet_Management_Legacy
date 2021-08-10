package com.sju18001.petmanagement.ui.community.comment

import android.app.Activity
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.LeadingMarginSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.restapi.dao.Comment
import com.sju18001.petmanagement.restapi.dao.PetSchedule
import com.sju18001.petmanagement.restapi.dao.Post
import com.sju18001.petmanagement.ui.community.CommunityPostListAdapter

interface CommunityCommentListAdapterInterface{
    fun getActivity(): Activity
}

class CommunityCommentListAdapter(private var dataSet: ArrayList<Comment>) : RecyclerView.Adapter<CommunityCommentListAdapter.ViewHolder>()  {
    lateinit var communityCommentListAdapterInterface: CommunityCommentListAdapterInterface

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val communityCommentLayout: ConstraintLayout = view.findViewById(R.id.layout_community_comment)
        val nicknameTextView: TextView = view.findViewById(R.id.text_nickname)
        val contentTextView: TextView = view.findViewById(R.id.text_content)
        val timeTextView: TextView = view.findViewById(R.id.text_time)
        val replyTextView: TextView = view.findViewById(R.id.text_reply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.community_comment_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 데이터 동기화
        updateDataSetToViewHolder(holder, dataSet[position])
        
        // 댓글 내용에 indent 추가
        setSpanToContent(holder.nicknameTextView, holder.contentTextView)
        
        // 리스너 추가
        setListenerOnViews(holder)
    }

    override fun getItemCount(): Int = dataSet.size

    private fun updateDataSetToViewHolder(holder: CommunityCommentListAdapter.ViewHolder, data: Comment){
        holder.nicknameTextView.text = data.nickname
        holder.contentTextView.text = data.content
        holder.timeTextView.text = data.time
    }

    private fun setSpanToContent(nicknameTextView: TextView, contentTextView: TextView){
        contentTextView.post{
            // content의 첫줄에 닉네임만큼의 indent를 주기 위함
            val spannable = SpannableString(contentTextView.text)
            val span = LeadingMarginSpan.Standard(nicknameTextView.width + 10, 0)
            spannable.setSpan(span, 0, spannable.count(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            contentTextView.text = spannable
        }
    }

    private fun setListenerOnViews(holder: ViewHolder){
        // 편의 기능: 키보드 내리기
        holder.communityCommentLayout.setOnClickListener {
            Util.hideKeyboard(communityCommentListAdapterInterface.getActivity())
        }

        holder.nicknameTextView.setOnClickListener {
            // TODO: 프로필로 이동
        }

        holder.replyTextView.setOnClickListener {
            // TODO: 답글 달기
        }
    }

    fun addItems(items: List<Comment>){
        for(item in items){
            dataSet.add(item)
        }
    }
}