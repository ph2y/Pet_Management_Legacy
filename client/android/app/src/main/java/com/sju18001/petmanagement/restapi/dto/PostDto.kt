package com.sju18001.petmanagement.restapi.dto

import com.sju18001.petmanagement.restapi.dao.Post
import com.sju18001.petmanagement.restapi.global.DtoMetadata
import com.sju18001.petmanagement.restapi.global.Pageable

data class FetchPostReqDto (
    val pageIndex: Int,
    val petId: Long,
    val id: Long,
    val topPostId: Long
)

data class FetchPostResDto (
    val _metadata: DtoMetadata,
    val postList: List<Post>,
    val pageable: Pageable,
    val isLast: Boolean
)