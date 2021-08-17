package com.sju18001.petmanagement.ui.community.followerFollowing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class FollowerFollowingViewModel(private val handle: SavedStateHandle) : ViewModel() {
    // for updating title
    private val followerTitle: MutableLiveData<String> = MutableLiveData()
    private val followingTitle: MutableLiveData<String> = MutableLiveData()

    public fun setFollowerTitle(input: String) {
        followerTitle.value = input
    }
    public fun getFollowerTitle(): LiveData<String> {
        return followerTitle
    }
    public fun setFollowingTitle(input: String) {
        followingTitle.value = input
    }
    public fun getFollowingTitle(): LiveData<String> {
        return followingTitle
    }
}