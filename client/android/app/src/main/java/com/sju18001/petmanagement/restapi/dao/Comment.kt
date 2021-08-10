package com.sju18001.petmanagement.restapi.dao

data class Comment(
    val photoUrl: String?,
    val nickname: String,
    val content: String,
    val time: String
)
