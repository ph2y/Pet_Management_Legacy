package com.sju18001.petmanagement.ui.community.comment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class CommunityCommentViewModel(private val handle: SavedStateHandle): ViewModel() {
    // Reply
    var nicknameForReply = handle.get<String>("nicknameForReply")?: ""
        set(value){
            handle.set("nicknameForReply", value)
            field = value
        }
}