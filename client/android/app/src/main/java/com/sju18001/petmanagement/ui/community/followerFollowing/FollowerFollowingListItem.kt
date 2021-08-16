package com.sju18001.petmanagement.ui.community.followerFollowing

import android.graphics.Bitmap
import java.time.LocalDate

class FollowerFollowingListItem {

    // item elements
    private var mPhoto: Bitmap? = null
    private var mNickname: String? = null

    // set values for the item
    public fun setValues(photo: Bitmap?, nickname: String) {
        mPhoto = photo
        mNickname = nickname
    }

    // get values from the item
    public fun getPhoto(): Bitmap? {
        return mPhoto
    }
    public fun getNickname(): String {
        return mNickname!!
    }
}