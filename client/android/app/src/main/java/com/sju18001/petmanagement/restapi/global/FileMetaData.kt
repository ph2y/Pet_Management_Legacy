package com.sju18001.petmanagement.restapi.global

data class FileMetaData (
    val name: String,
    val size: Long,
    val entity: String,
    val type: String,
    val url: String
)