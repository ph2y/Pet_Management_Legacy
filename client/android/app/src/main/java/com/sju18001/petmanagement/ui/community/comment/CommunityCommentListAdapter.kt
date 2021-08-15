package com.sju18001.petmanagement.ui.community.comment

import android.app.Activity
import android.content.Context
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.restapi.dao.Comment
import com.sju18001.petmanagement.restapi.dao.PetSchedule
import com.sju18001.petmanagement.restapi.dao.Post
import com.sju18001.petmanagement.ui.community.CommunityPostListAdapter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC

interface CommunityCommentListAdapterInterface{
    fun getActivity(): Activity
}

class CommunityCommentListAdapter(private var dataSet: ArrayList<Comment>) : RecyclerView.Adapter<CommunityCommentListAdapter.ViewHolder>()  {
    lateinit var communityCommentListAdapterInterface: CommunityCommentListAdapterInterface

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val communityCommentLayout: ConstraintLayout = view.findViewById(R.id.layout_community_comment)
        val nicknameTextView: TextView = view.findViewById(R.id.text_nickname)
        val contentsTextView: TextView = view.findViewById(R.id.text_contents)
        val timestampTextView: TextView = view.findViewById(R.id.text_timestamp)
        val replyTextView: TextView = view.findViewById(R.id.text_reply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.community_comment_item, parent, false)

        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 데이터 동기화
        updateDataSetToViewHolder(holder, dataSet[position])
        
        // 댓글 내용에 indent 추가
        setSpanToContent(holder.nicknameTextView, holder.contentsTextView)
        
        // 리스너 추가
        setListenerOnViews(holder)
    }

    override fun getItemCount(): Int = dataSet.size

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDataSetToViewHolder(holder: CommunityCommentListAdapter.ViewHolder, data: Comment){
        holder.nicknameTextView.text = data.author.nickname
        holder.contentsTextView.text = data.contents
        holder.timestampTextView.text = getTimestampForDisplay(data.timestamp)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTimestampForDisplay(timestamp: String): String{
        var timestampForDisplay: String = timestamp

        val secondDiff: Long = Util.getSecondDifferenceInLocalDateTime(LocalDateTime.parse(timestamp))
        val minuteDiff: Long = secondDiff / 60
        val hourDiff: Long = minuteDiff / 60
        val dateDiff: Long = hourDiff / 24
        val monthDiff: Long = dateDiff / 30
        val yearDiff: Long = monthDiff / 12

        timestampForDisplay = when {
            yearDiff > 0 -> "${yearDiff}년"
            monthDiff > 0 -> "${monthDiff}달"
            dateDiff > 0 -> "${dateDiff}일"
            hourDiff > 0 -> "${hourDiff}시간"
            minuteDiff > 0 -> "${minuteDiff}분"
            else -> "${secondDiff}초"
        }

        return timestampForDisplay
    }

    private fun setSpanToContent(nicknameTextView: TextView, contentTextView: TextView){
        contentTextView.post{
            // contents의 첫줄에 닉네임만큼의 indent를 주기 위함
            val spannable = SpannableString(contentTextView.text)
            val span = LeadingMarginSpan.Standard(nicknameTextView.width + 10, 0)
            spannable.setSpan(span, 0, spannable.count(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            contentTextView.text = spannable
        }
    }

    private fun setListenerOnViews(holder: ViewHolder){
        holder.nicknameTextView.setOnClickListener {
            // TODO: 프로필로 이동
        }

        holder.replyTextView.setOnClickListener {
            // TODO: 답글 달기
        }
    }

    fun addItem(item: Comment){
        dataSet.add(item)
    }

    fun resetDataSet(){
        dataSet = arrayListOf()
    }
}