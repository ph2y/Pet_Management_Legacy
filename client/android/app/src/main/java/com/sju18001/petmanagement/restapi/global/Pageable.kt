package com.sju18001.petmanagement.restapi.global

import java.util.*

data class Pageable (
    val pageNumber: Int,
    val pageSize: Int,
    val offset: Long,
    val sort: Sort,
    val unpaged: Boolean
)