package com.sju18001.petmanagement.ui.community.followerFollowing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class SearchViewModel(private val handle: SavedStateHandle) : ViewModel() {
    // for follower id list
    var followerIdList = handle.get<MutableList<Long>>("followerIdList")
        set(value) {
            handle.set("followerIdList", value)
            field = value
        }

    // for search
    var searchEditText = handle.get<String>("searchEditText")?: ""
        set(value){
            handle.set("searchEditText", value)
            field = value
        }

    // for account info
    var accountPhotoUrl = handle.get<String>("accountPhotoUrl")
        set(value){
            handle.set("accountPhotoUrl", value)
            field = value
        }
    var accountPhotoByteArray = handle.get<ByteArray>("accountPhotoByteArray")
        set(value){
            handle.set("accountPhotoByteArray", value)
            field = value
        }

    // for id
    var accountId = handle.get<Long>("accountId")
        set(value){
            handle.set("accountId", value)
            field = value
        }

    // for nickname
    var accountNickname = handle.get<String>("accountNickname")
        set(value){
            handle.set("accountNickname", value)
            field = value
        }

    // for API
    var apiIsLoading = handle.get<Boolean>("apiIsLoading")?: false
        set(value){
            handle.set("apiIsLoading", value)
            field = value
        }
}