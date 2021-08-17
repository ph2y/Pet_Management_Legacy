package com.sju18001.petmanagement.restapi.dto

import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.global.DtoMetadata

data class FetchFollowerResDto (
    val _metadata: DtoMetadata,
    val followerList: List<Account>
)

data class FetchFollowingResDto (
    val _metadata: DtoMetadata,
    val followingList: List<Account>
)

data class CreateFollowReqDto (
    val id: Long
)

data class CreateFollowResDto (
    val _metadata: DtoMetadata
)

data class DeleteFollowReqDto (
    val id: Long
)

data class DeleteFollowResDto (
    val _metadata: DtoMetadata
)