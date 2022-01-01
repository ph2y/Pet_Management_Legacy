package com.sju18001.petmanagement.ui.community.post.createUpdatePost

import android.graphics.Bitmap

data class PetListItem (
    val petId: Long,
    val petPhotoUrl: String?,
    var petPhoto: Bitmap?,
    val petName: String,
    val isRepresentativePet: Boolean,
    var isSelected: Boolean
)