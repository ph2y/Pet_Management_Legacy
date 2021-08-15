package com.sju18001.petmanagement.ui.community.followerFollowing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class SearchViewModel(private val handle: SavedStateHandle) : ViewModel() {
    // for search
    var searchEditText = handle.get<String>("searchEditText")?: ""
        set(value){
            handle.set("searchEditText", value)
            field = value
        }

    // for account info
}