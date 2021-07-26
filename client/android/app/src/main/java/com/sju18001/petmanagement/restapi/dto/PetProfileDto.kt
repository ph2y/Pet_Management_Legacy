package com.sju18001.petmanagement.restapi.dto

import java.time.LocalDate

// Create Dto
data class PetProfileCreateRequestDto (
    val name: String,
    val species: String,
    val breed: String,
    val birth: String,
    val gender: Boolean?,
    val message: String?,
    val photo_url: String?
    )

data class PetProfileCreateResponseDto (
    val message: String
    )

// Fetch Dto
data class PetProfileFetchResponseDto (
    val id: Long,
    val name: String,
    val species: String,
    val breed: String,
    val birth: String,
    val gender: Boolean?,
    val message: String?,
    val photo_url: String?
    )

// Update Dto
data class PetProfileUpdateRequestDto (
    val id: Long,
    val name: String,
    val species: String,
    val breed: String,
    val birth: String,
    val gender: Boolean?,
    val message: String?,
    val photo_url: String?
    )

data class PetProfileUpdateResponseDto (
    val message: String
    )

// Delete Dto
data class PetProfileDeleteRequestDto (
    val id: Long
    )

data class PetProfileDeleteResponseDto (
    val message: String
    )