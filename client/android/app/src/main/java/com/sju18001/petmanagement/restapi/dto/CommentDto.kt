package com.sju18001.petmanagement.restapi.dto

import com.sju18001.petmanagement.restapi.dao.Comment
import com.sju18001.petmanagement.restapi.global.DtoMetadata
import com.sju18001.petmanagement.restapi.global.Pageable

data class CreateCommentReqDto(
    val postId: Long,
    val parentCommentId: Long?,
    val contents: String
)

data class CreateCommentResDto(
    val _metadata: DtoMetadata,
    val id: Long
)

data class DeleteCommentReqDto(
    val id: Long
)

data class DeleteCommentResDto(
    val _metadata: DtoMetadata
)

data class FetchCommentReqDto(
    val pageIndex: Int?,
    val topCommentId: Long?,
    val postId: Long?,
    val parentCommentId: Long?,
    val id: Long?
)

data class FetchCommentResDto(
    val _metadata: DtoMetadata,
    val commentList: List<Comment>?,
    val pageable: Pageable?,
    val isLast: Boolean?
)

data class UpdateCommentReqDto(
    val id: Long,
    val contents: String
)

data class UpdateCommentResDto(
    val _metadata: DtoMetadata
)