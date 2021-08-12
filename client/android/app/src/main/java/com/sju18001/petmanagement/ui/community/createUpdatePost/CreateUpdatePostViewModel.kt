package com.sju18001.petmanagement.ui.community.createUpdatePost

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class CreateUpdatePostViewModel(private val handle: SavedStateHandle) : ViewModel() {
    // for photo/video files
    var photoVideoByteArrayList = handle.get<MutableList<ByteArray>>("photoVideoByteArrayList")?: mutableListOf()
        set(value) {
            handle.set("photoVideoByteArrayList", value)
            field = value
        }
    var photoVideoPathList = handle.get<MutableList<String>>("photoVideoPathList")?: mutableListOf()
        set(value) {
            handle.set("photoVideoPathList", value)
            field = value
        }
    var thumbnailList = handle.get<MutableList<Bitmap?>>("thumbnailList")?: mutableListOf()
        set(value) {
            handle.set("thumbnailList", value)
            field = value
        }

    // for post EditText
    var postEditText = handle.get<String>("postEditText")?: ""
        set(value) {
            handle.set("postEditText", value)
            field = value
        }
}