package com.sju18001.petmanagement.restapi.dao

import java.time.LocalDateTime

data class Post(
    val id: Long,
    val author: Account,
    val pet: Pet,
    val contents: String,
    val timestamp: String,
    val edited: Boolean,
    val serializedHashTags: String,
    val disclosure: String,
    val geoTagLat: Double,
    val geoTagLong: Double,
    val imageAttachments: String?,
    val videoAttachments: String?,
    val fileAttachments: String?
)