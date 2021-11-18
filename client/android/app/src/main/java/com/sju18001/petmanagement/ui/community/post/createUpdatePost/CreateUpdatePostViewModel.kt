package com.sju18001.petmanagement.ui.community.post.createUpdatePost

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class CreateUpdatePostViewModel(private val handle: SavedStateHandle) : ViewModel() {
    // for pet
    var petId = handle.get<Long>("petId")
        set(value) {
            handle.set("petId", value)
            field = value
        }

    // for location
    var isUsingLocation = handle.get<Boolean>("isUsingLocation")?: true
        set(value) {
            handle.set("isUsingLocation", value)
            field = value
        }

    // for photo files
    var photoPathList = handle.get<MutableList<String>>("photoPathList")?: mutableListOf()
        set(value) {
            handle.set("photoPathList", value)
            field = value
        }
    var photoThumbnailList = handle.get<MutableList<Bitmap?>>("photoThumbnailList")?: mutableListOf()
        set(value) {
            handle.set("photoThumbnailList", value)
            field = value
        }

    // for disclosure
    var disclosure = handle.get<String>("disclosure")?: "PUBLIC"
        set(value) {
            handle.set("disclosure", value)
            field = value
        }

    // for hashtag
    var hashtagEditText = handle.get<String>("hashtagEditText")?: ""
        set(value) {
            handle.set("hashtagEditText", value)
            field = value
        }
    var hashtagList = handle.get<MutableList<String>>("hashtagList")?: mutableListOf()
        set(value) {
            handle.set("hashtagList", value)
            field = value
        }

    // for post EditText
    var postEditText = handle.get<String>("postEditText")?: ""
        set(value) {
            handle.set("postEditText", value)
            field = value
        }

    // for API
    var apiIsLoading = handle.get<Boolean>("apiIsLoading")?: false
        set(value) {
            handle.set("apiIsLoading", value)
        }

    // for update
    var fetchedPostDataForUpdate = handle.get<Boolean>("fetchedPostDataForUpdate")?: false
        set(value) {
            handle.set("fetchedPostDataForUpdate", value)
            field = value
        }
    var postId = handle.get<Long>("postId")
        set(value) {
            handle.set("postId", value)
            field = value
        }
}