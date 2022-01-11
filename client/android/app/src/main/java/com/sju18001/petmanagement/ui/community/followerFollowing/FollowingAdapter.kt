package com.sju18001.petmanagement.ui.community.followerFollowing

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.dto.DeleteFollowReqDto
import com.sju18001.petmanagement.restapi.dto.FetchAccountPhotoReqDto
import com.sju18001.petmanagement.ui.community.CommunityUtil
import com.sju18001.petmanagement.ui.community.post.PostListAdapter
import de.hdodenhof.circleimageview.CircleImageView

class FollowingAdapter(val context: Context, val followerFollowingViewModel: FollowerFollowingViewModel):
    RecyclerView.Adapter<FollowingAdapter.HistoryListViewHolder>() {

    private var resultList = mutableListOf<FollowerFollowingListItem>()

    private var isViewDestroyed = false

    class HistoryListViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val mainLayout: View = view.findViewById(R.id.main_layout)
        val accountPhoto: CircleImageView = view.findViewById(R.id.account_photo)
        val accountNickname: TextView = view.findViewById(R.id.account_nickname)
        val followUnfollowButton: Button = view.findViewById(R.id.follow_unfollow_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingAdapter.HistoryListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.follower_following_list_item, parent, false)

        val holder = FollowingAdapter.HistoryListViewHolder(view)
        setListenerOnView(holder)

        return holder
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: FollowingAdapter.HistoryListViewHolder, position: Int) {
        // set account photo
        if(resultList[position].getHasPhoto()) {
            if(resultList[position].getPhoto() == null) {
                setAccountPhoto(resultList[position].getId(), holder, position)
            }
            else {
                holder.accountPhoto.setImageBitmap(resultList[position].getPhoto())
            }
        }
        else {
            holder.accountPhoto.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_account_circle_24))
        }

        // set account nickname
        val nicknameText = resultList[position].getNickname() + '님'
        holder.accountNickname.text = nicknameText

        // for follow/unfollow button
        holder.followUnfollowButton.setBackgroundColor(context.getColor(R.color.border_line))
        holder.followUnfollowButton.setTextColor(context.resources.getColor(R.color.black))
        holder.followUnfollowButton.text = context.getText(R.string.unfollow_button)
    }

    private fun setListenerOnView(holder: FollowingAdapter.HistoryListViewHolder) {
        // start pet profile
        holder.mainLayout.setOnClickListener {
            val position = holder.absoluteAdapterPosition

            CommunityUtil.fetchRepresentativePetAndStartPetProfile(context, Account(
                resultList[position].getId(), resultList[position].getUsername(), "", "", null,
                null, resultList[position].getNickname(), if (resultList[position].getHasPhoto()) "true" else null,
                "", resultList[position].getRepresentativePetId()), isViewDestroyed)
        }

        holder.followUnfollowButton.setOnClickListener {
            val position = holder.absoluteAdapterPosition

            // show confirm dialog
            val builder = AlertDialog.Builder(context)
            val messageText = resultList[position].getNickname() + context.getString(R.string.unfollow_confirm_dialog_message)
            builder.setMessage(messageText)
                .setPositiveButton(
                    R.string.confirm
                ) { _, _ ->
                    // set button to loading
                    holder.followUnfollowButton.isEnabled = false

                    // API call
                    deleteFollow(resultList[position].getId(), holder, position)
                }
                .setNegativeButton(
                    R.string.cancel
                ) { dialog, _ ->
                    dialog.cancel()
                }
                .create().show()
        }
    }

    private fun deleteFollow(id: Long, holder: FollowingAdapter.HistoryListViewHolder, position: Int) {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(context)!!)
            .deleteFollowReq(DeleteFollowReqDto(id))

        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, context, {
            holder.followUnfollowButton.isEnabled = true

            // remove from list
            resultList.removeAt(position)

            // set following count
            val followingText = context.getText(R.string.following_fragment_title).toString() +
                    ' ' + resultList.size.toString()
            followerFollowingViewModel.setFollowingTitle(followingText)

            // show animation
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, resultList.size)
        }, {
            holder.followUnfollowButton.isEnabled = true
        }, {
            holder.followUnfollowButton.isEnabled = true
        })
    }

    private fun setAccountPhoto(id: Long, holder: FollowingAdapter.HistoryListViewHolder, position: Int) {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(context)!!)
            .fetchAccountPhotoReq(FetchAccountPhotoReqDto(id))

        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, context, { response ->
            // convert photo to byte array + get bitmap
            val photoByteArray = response.body()!!.byteStream().readBytes()
            val photoBitmap = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)

            // set account photo + save photo value
            holder.accountPhoto.setImageBitmap(photoBitmap)

            val currentItem = resultList[position]
            currentItem.setValues(
                currentItem.getHasPhoto(), photoBitmap, currentItem.getId(), currentItem.getUsername(),
                currentItem.getNickname(), currentItem.getIsFollowing(), currentItem.getRepresentativePetId()
            )
        }, {}, {})
    }

    override fun getItemCount() = resultList.size

    public fun setResult(result: MutableList<FollowerFollowingListItem>){
        this.resultList = result
        notifyDataSetChanged()
    }

    public fun onDestroy() {
        isViewDestroyed = true
    }
}