package com.sju18001.petmanagement.ui.community.createUpdatePost

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.math.BigDecimal

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
    var geoTagLat = handle.get<BigDecimal>("geoTagLat")
        set(value) {
            handle.set("geoTagLat", value)
            field = value
        }
    var geoTagLong = handle.get<BigDecimal>("geoTagLong")
        set(value) {
            handle.set("geoTagLong", value)
            field = value
        }

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

    // for disclosure
    var disclosure = handle.get<String>("disclosure")
        set(value) {
            handle.set("disclosure", value)
            field = value
        }

    // for post EditText
    var postEditText = handle.get<String>("postEditText")?: ""
        set(value) {
            handle.set("postEditText", value)
            field = value
        }
}