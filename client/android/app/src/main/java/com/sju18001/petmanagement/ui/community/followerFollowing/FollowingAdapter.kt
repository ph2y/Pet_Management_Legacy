package com.sju18001.petmanagement.ui.community.followerFollowing

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.DeleteFollowReqDto
import com.sju18001.petmanagement.restapi.dto.DeleteFollowResDto
import com.sju18001.petmanagement.restapi.dto.FetchAccountPhotoReqDto
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FollowingAdapter(val context: Context, val sessionManager: SessionManager,
                       val followerFollowingViewModel: FollowerFollowingViewModel):
    RecyclerView.Adapter<FollowingAdapter.HistoryListViewHolder>() {

    private var resultList = mutableListOf<FollowerFollowingListItem>()
    private var deleteFollowApiCall: Call<DeleteFollowResDto>? = null
    private var fetchAccountPhotoApiCall: Call<ResponseBody>? = null

    class HistoryListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val accountPhoto: CircleImageView = itemView.findViewById(R.id.account_photo)
        val accountNickname: TextView = itemView.findViewById(R.id.account_nickname)
        val followUnfollowButton: Button = itemView.findViewById(R.id.follow_unfollow_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingAdapter.HistoryListViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.follower_following_list_item, parent, false)
        return FollowingAdapter.HistoryListViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: FollowingAdapter.HistoryListViewHolder, position: Int) {
        // TODO: navigate to the account's pet profile

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

        holder.followUnfollowButton.setOnClickListener {
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
        // create DTO
        val deleteFollowReqDto = DeleteFollowReqDto(id)

        // API call
        deleteFollowApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .deleteFollowReq(deleteFollowReqDto)
        deleteFollowApiCall!!.enqueue(object: Callback<DeleteFollowResDto> {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onResponse(
                call: Call<DeleteFollowResDto>,
                response: Response<DeleteFollowResDto>
            ) {
                if(response.isSuccessful) {
                    // remove from list
                    resultList.removeAt(position)

                    // set following count
                    val followingText = context.getText(R.string.following_fragment_title).toString() +
                            ' ' + resultList.size.toString()
                    followerFollowingViewModel.setFollowingTitle(followingText)

                    // show animation
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, resultList.size)
                }
                else {
                    // set button to normal
                    holder.followUnfollowButton.isEnabled = true

                    // get error message
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                    // Toast + Log
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<DeleteFollowResDto>, t: Throwable) {
                // set button to normal
                holder.followUnfollowButton.isEnabled = true

                // log error message
                Log.d("error", t.message.toString())
            }
        })
    }

    private fun setAccountPhoto(id: Long, holder: FollowingAdapter.HistoryListViewHolder, position: Int) {
        // create DTO
        val fetchAccountPhotoReqDto = FetchAccountPhotoReqDto(id)

        // API call
        fetchAccountPhotoApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .fetchAccountPhotoReq(fetchAccountPhotoReqDto)
        fetchAccountPhotoApiCall!!.enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.isSuccessful) {
                    // convert photo to byte array + get bitmap
                    val photoByteArray = response.body()!!.byteStream().readBytes()
                    val photoBitmap = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)

                    // set account photo + save photo value
                    holder.accountPhoto.setImageBitmap(photoBitmap)

                    val currentItem = resultList[position]
                    currentItem.setValues(
                        currentItem.getHasPhoto(), photoBitmap, currentItem.getId(), currentItem.getNickname(), currentItem.getIsFollowing()
                    )
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

    override fun getItemCount() = resultList.size

    public fun setResult(result: MutableList<FollowerFollowingListItem>){
        this.resultList = result
        notifyDataSetChanged()
    }

    public fun onDestroy() {
        // stop api call when fragment is destroyed
        deleteFollowApiCall?.cancel()
        fetchAccountPhotoApiCall?.cancel()
    }
}