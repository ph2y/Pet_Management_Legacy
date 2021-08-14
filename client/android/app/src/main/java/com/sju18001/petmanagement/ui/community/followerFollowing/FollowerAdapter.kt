package com.sju18001.petmanagement.ui.community.followerFollowing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import de.hdodenhof.circleimageview.CircleImageView

class FollowerAdapter : RecyclerView.Adapter<FollowerAdapter.HistoryListViewHolder>() {

    private var resultList = mutableListOf<FollowerFollowingListItem>()

    class HistoryListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val accountPhoto: CircleImageView = itemView.findViewById(R.id.account_photo)
        val accountNickname: TextView = itemView.findViewById(R.id.account_nickname)
        val followUnfollowButton: Button = itemView.findViewById(R.id.follow_unfollow_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerAdapter.HistoryListViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.follower_following_list_item, parent, false)
        return FollowerAdapter.HistoryListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FollowerAdapter.HistoryListViewHolder, position: Int) {
        // set account photo
        if(resultList[position].getPhoto() != null) {
            holder.accountPhoto.setImageBitmap(resultList[position].getPhoto())
        }

        // set account nickname
        holder.accountNickname.text = resultList[position].getNickname()

        // for follow button
        // TODO
    }

    override fun getItemCount() = resultList.size

    public fun setResult(result: MutableList<FollowerFollowingListItem>){
        this.resultList = result
        notifyDataSetChanged()
    }
}