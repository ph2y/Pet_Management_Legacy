package com.sju18001.petmanagement.ui.community.followerFollowing

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
import com.sju18001.petmanagement.restapi.dto.*
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FollowerAdapter(val context: Context) :
    RecyclerView.Adapter<FollowerAdapter.HistoryListViewHolder>() {

    private var resultList = mutableListOf<FollowerFollowingListItem>()

    private var isViewDestroyed = false

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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: FollowerAdapter.HistoryListViewHolder, position: Int) {
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
        if(resultList[position].getIsFollowing()) {
            holder.followUnfollowButton.setBackgroundColor(context.getColor(R.color.border_line))
            holder.followUnfollowButton.setTextColor(context.resources.getColor(R.color.black))
            holder.followUnfollowButton.text = context.getText(R.string.unfollow_button)
        }
        else {
            holder.followUnfollowButton.setBackgroundColor(context.getColor(R.color.carrot))
            holder.followUnfollowButton.setTextColor(context.resources.getColor(R.color.white))
            holder.followUnfollowButton.text = context.getText(R.string.follow_button)
        }
        holder.followUnfollowButton.setOnClickListener {
            // set button to loading
            holder.followUnfollowButton.isEnabled = false

            // API call
            if(resultList[position].getIsFollowing()) {
                deleteFollow(resultList[position].getId(), holder, position)
            }
            else {
                createFollow(resultList[position].getId(), holder, position)
            }
        }
    }

    private fun createFollow(id: Long, holder: HistoryListViewHolder, position: Int) {
        // create DTO
        val createFollowReqDto = CreateFollowReqDto(id)

        // API call
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(context)!!)
            .createFollowReq(createFollowReqDto)
        call.enqueue(object: Callback<CreateFollowResDto> {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onResponse(
                call: Call<CreateFollowResDto>,
                response: Response<CreateFollowResDto>
            ) {
                if(isViewDestroyed) return

                if(response.isSuccessful) {
                    // update isFollowing and button state
                    val currentItem = resultList[position]
                    currentItem.setValues(
                        currentItem.getHasPhoto(), currentItem.getPhoto(), currentItem.getId(), currentItem.getNickname(), true
                    )
                    notifyItemChanged(position)

                    holder.followUnfollowButton.isEnabled = true
                }
                else {
                    holder.followUnfollowButton.isEnabled = true

                    Util.showToastAndLogForFailedResponse(context, response.errorBody())
                }
            }

            override fun onFailure(call: Call<CreateFollowResDto>, t: Throwable) {
                Util.showToastAndLog(context, t.message.toString())
            }
        })
    }

    private fun deleteFollow(id: Long, holder: HistoryListViewHolder, position: Int) {
        // create DTO
        val deleteFollowReqDto = DeleteFollowReqDto(id)

        // API call
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(context)!!)
            .deleteFollowReq(deleteFollowReqDto)
        call.enqueue(object: Callback<DeleteFollowResDto> {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onResponse(
                call: Call<DeleteFollowResDto>,
                response: Response<DeleteFollowResDto>
            ) {
                if(response.isSuccessful) {
                    // update isFollowing and button state
                    val currentItem = resultList[position]
                    currentItem.setValues(
                        currentItem.getHasPhoto(), currentItem.getPhoto(), currentItem.getId(), currentItem.getNickname(), false
                    )
                    notifyItemChanged(position)

                    holder.followUnfollowButton.isEnabled = true
                }
                else {
                    holder.followUnfollowButton.isEnabled = true

                    Util.showToastAndLogForFailedResponse(context, response.errorBody())
                }
            }

            override fun onFailure(call: Call<DeleteFollowResDto>, t: Throwable) {
                holder.followUnfollowButton.isEnabled = true

                Util.showToastAndLog(context, t.message.toString())
            }
        })
    }

    private fun setAccountPhoto(id: Long, holder: HistoryListViewHolder, position: Int) {
        // create DTO
        val fetchAccountPhotoReqDto = FetchAccountPhotoReqDto(id)

        // API call
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(context)!!)
            .fetchAccountPhotoReq(fetchAccountPhotoReqDto)
        call.enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(isViewDestroyed) return

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
                    Util.showToastAndLogForFailedResponse(context, response.errorBody())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Util.showToastAndLog(context, t.message.toString())
            }
        })
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