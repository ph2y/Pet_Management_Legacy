package com.sju18001.petmanagement.ui.community.comment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class CommentViewModel(private val handle: SavedStateHandle): ViewModel() {
    // Reply
    var idForReply = handle.get<Long>("idForReply")?: null
        set(value){
            handle.set("idForReply", value)
            field = value
        }

    var nicknameForReply = handle.get<String>("nicknameForReply")?: ""
        set(value){
            handle.set("nicknameForReply", value)
            field = value
        }
}