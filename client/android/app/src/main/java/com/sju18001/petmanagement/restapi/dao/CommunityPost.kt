package com.sju18001.petmanagement.restapi.dao

data class CommunityPost(
    // TODO: 서버에서 dto가 완성되면 동기화
    val nickname: String,
    val petPhotoUrl: String,
    val petName: String,
    val photoUrlList: List<String>,
    val content: String,
    val like: Int
)