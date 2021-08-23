package com.sju18001.petmanagement.restapi.dao

data class Like(
    val id: Long,
    val likedAccount: Account,
    val likedAccountId: Long,
    val likedPost: Post?,
    val likedPostId: Long?,
    val likedComment: Comment?,
    val likedCommentId: Long?
)