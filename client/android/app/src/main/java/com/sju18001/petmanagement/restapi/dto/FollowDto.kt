package com.sju18001.petmanagement.restapi.dto

import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.global.DtoMetadata

data class FetchFollowerResDto (
    val _metadata: DtoMetadata,
    val followerList: List<Account>
)