package com.sju18001.petmanagement.ui.community.followerFollowing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class FollowerFollowingViewModel(private val handle: SavedStateHandle) : ViewModel() {
    // for API
    var apiIsCanceled = handle.get<Boolean>("apiIsCanceled")?: false
        set(value){
            handle.set("apiIsCanceled", value)
            field = value
        }
}