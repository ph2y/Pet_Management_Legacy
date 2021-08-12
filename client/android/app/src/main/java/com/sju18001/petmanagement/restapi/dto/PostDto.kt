package com.sju18001.petmanagement.restapi.dto

import com.sju18001.petmanagement.restapi.global.DtoMetadata
import java.math.BigDecimal

data class CreatePostReqDto(
    val petId: Long,
    val contents: String?,
    val hashTags: List<String>,
    val disclosure: String,
    val geoTagLat: BigDecimal?,
    val geoTagLong: BigDecimal?
)

data class CreatePostResDto(
    val _metadata: DtoMetadata
)