package com.sju18001.petmanagement.ui.community.followerFollowing

import android.graphics.Bitmap

class FollowerFollowingListItem {

    // item elements
    private var mHasPhoto: Boolean? = null
    private var mPhoto: Bitmap? = null
    private var mId: Long? = null
    private var mUsername: String? = null
    private var mNickname: String? = null
    private var mIsFollowing: Boolean? = null
    private var mRepresentativePetId: Long? = null

    // set values for the item
    public fun setValues(hasPhoto: Boolean, photo: Bitmap?, id: Long, username: String,
                         nickname: String, isFollowing: Boolean, representativePetId: Long?) {
        mHasPhoto = hasPhoto
        mPhoto = photo
        mId = id
        mUsername = username
        mNickname = nickname
        mIsFollowing = isFollowing
        mRepresentativePetId = representativePetId
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
    public fun getUsername(): String {
        return mUsername!!
    }
    public fun getNickname(): String {
        return mNickname!!
    }
    public fun getIsFollowing(): Boolean {
        return mIsFollowing!!
    }
    public fun getRepresentativePetId(): Long? {
        return mRepresentativePetId
    }
}