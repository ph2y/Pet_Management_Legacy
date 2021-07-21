package com.sju18001.petmanagement.restapi.dto

data class PetProfileCreateRequestDto (
    val token: String,
    val name: String,
    val species: String,
    val breed: String?,
    val birth: String?,
    val gender: String?,
    val feed_interval: String?,
    val memo: String?,
    val photo_url: String?
)

data class PetProfileCreateResponseDto (
    val message: String
)

data class PetProfileFetchResponseDto (
    val id: Long,
    val name: String,
    val species: String,
    val breed: String?,
    val birth: String?,
    val gender: Boolean?,
    val feed_interval: String?,
    val memo: String?,
    val photo_url: String?
)