package com.sju18001.petmanagement.restapi.dao

data class Comment(
    val id: Long,
    val author: Account,
    val post: Post?,
    val postId: Long,
    val parentComment: Comment?,
    val parentCommentId: Comment?,
    val contents: String,
    val timestamp: String,
    val edited: Boolean
)
