package com.sju18001.petmanagement.ui.community.post.generalFiles

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class GeneralFilesViewModel(private val handle: SavedStateHandle) : ViewModel() {
    var downloadedFilePath = handle.get<String>("downloadedFilePath")
        set(value) {
            handle.set("downloadedFilePath", value)
            field = value
        }
    var userSelectedUri = handle.get<Uri>("userSelectedUri")
        set(value) {
            handle.set("userSelectedUri", value)
            field = value
        }
}