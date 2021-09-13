package com.sju18001.petmanagement.restapi.dto

import com.sju18001.petmanagement.restapi.global.DtoMetadata

data class CreateLikeReqDto(
    val postId: Long?,
    val commentId: Long?
)

data class CreateLikeResDto(
    val _metadata: DtoMetadata
)

data class DeleteLikeReqDto(
    val postId: Long?,
    val commentId: Long?
)

data class DeleteLikeResDto(
    val _metadata: DtoMetadata
)

data class FetchLikeReqDto(
    val postId: Long?,
    val commentId: Long?
)

data class FetchLikeResDto(
    val _metadata: DtoMetadata,
    val likedCount: Long?,
    val likedAccountIdList: List<Long>?
)