package com.sju18001.petmanagement.ui.community

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class CommunityViewModel(private val handle: SavedStateHandle): ViewModel() {
    var lastScrolledIndex = handle.get<Int>("lastScrolledIndex")?: -1
        set(value){
            handle.set("lastScrolledIndex", value)
            field = value
        }
}