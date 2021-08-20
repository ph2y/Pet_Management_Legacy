package com.sju18001.petmanagement.restapi.dao

data class Attachment(
    val name: String,
    val size: Int,
    val entity: String,
    val type: String,
    val url: String
)
