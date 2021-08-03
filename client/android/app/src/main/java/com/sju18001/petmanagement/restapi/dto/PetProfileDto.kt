package com.sju18001.petmanagement.restapi.dto

import com.sju18001.petmanagement.restapi.dao.DtoMetadata
import com.sju18001.petmanagement.restapi.dao.Pet

// Create Dto
data class PetProfileCreateRequestDto (
    val name: String,
    val species: String?,
    val breed: String?,
    val birth: String?,
    val year_only: Boolean?,
    val gender: Boolean?,
    val message: String?,
    val photo_url: String?
    )

data class PetProfileCreateResponseDto (
    val message: String
    )

// Fetch Dto
data class FetchPetResDto (
    val _metadata: DtoMetadata,
    val petList: List<Pet>
    )

// Update Dto
data class PetProfileUpdateRequestDto (
    val id: Long,
    val name: String,
    val species: String?,
    val breed: String?,
    val birth: String?,
    val year_only: Boolean?,
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