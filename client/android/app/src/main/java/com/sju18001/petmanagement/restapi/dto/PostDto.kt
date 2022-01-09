package com.sju18001.petmanagement.restapi.dto

import com.sju18001.petmanagement.restapi.dao.Post
import com.sju18001.petmanagement.restapi.global.DtoMetadata
import com.sju18001.petmanagement.restapi.global.FileMetaData
import com.sju18001.petmanagement.restapi.global.Pageable
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
    val _metadata: DtoMetadata,
    val id: Long
)

data class UpdatePostReqDto(
    val id: Long,
    val petId: Long,
    val contents: String?,
    val hashTags: List<String>,
    val disclosure: String,
    val geoTagLat: BigDecimal?,
    val geoTagLong: BigDecimal?
)

data class UpdatePostResDto(
    val _metadata: DtoMetadata
)

data class FetchPostReqDto (
    val pageIndex: Int?,
    val topPostId: Long?,
    val petId: Long?,
    val id: Long?
)

data class FetchPostResDto (
    val _metadata: DtoMetadata,
    val postList: List<Post>?,
    val pageable: Pageable?,
    val isLast: Boolean?
)

data class DeletePostReqDto (
    val id: Long
)

data class DeletePostResDto (
    val _metadata: DtoMetadata
)

data class FetchPostImageReqDto (
    val id: Long,
    val index: Int
)

data class FetchPostVideoReqDto (
    val id: Long,
    val index: Int
)

data class UpdatePostMediaResDto (
    val _metadata: DtoMetadata,
    val fileMetadataList: List<FileMetaData>
)

data class DeletePostMediaReqDto (
    val id: Long
)

data class DeletePostMediaResDto (
    val _metadata: DtoMetadata
)

data class UpdatePostFileResDto (
    val _metadata: DtoMetadata,
    val fileMetadataList: List<FileMetaData>
)

data class DeletePostFileReqDto (
    val id: Long,
    val fileType: String
)

data class DeletePostFileResDto (
    val _metadata: DtoMetadata
)

data class FetchPostFileReqDto (
    val id: Long,
    val index: Int
)