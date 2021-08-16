package com.sju18001.petmanagement.ui.community.followerFollowing

import android.graphics.Bitmap
import java.time.LocalDate

class FollowerFollowingListItem {

    // item elements
    private var mHasPhoto: Boolean? = null
    private var mPhoto: Bitmap? = null
    private var mId: Long? = null
    private var mNickname: String? = null
    private var mIsFollowing: Boolean? = null

    // set values for the item
    public fun setValues(hasPhoto: Boolean, photo: Bitmap?, id: Long, nickname: String, isFollowing: Boolean) {
        mHasPhoto = hasPhoto
        mPhoto = photo
        mId = id
        mNickname = nickname
        mIsFollowing = isFollowing
    }

    // get values from the item
    public fun getHasPhoto(): Boolean {
        return mHasPhoto!!
    }
    public fun getPhoto(): Bitmap? {
        return mPhoto
    }
    public fun getId(): Long {
        return mId!!
    }
    public fun getNickname(): String {
        return mNickname!!
    }
    public fun getIsFollowing(): Boolean {
        return mIsFollowing!!
    }
}