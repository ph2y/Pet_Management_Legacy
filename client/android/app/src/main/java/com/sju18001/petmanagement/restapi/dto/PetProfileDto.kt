package com.sju18001.petmanagement.restapi.dto

import com.sju18001.petmanagement.restapi.dao.DtoMetadata
import com.sju18001.petmanagement.restapi.dao.Pet

// Create Dto
data class CreatePetReqDto (
    val name: String,
    val species: String?,
    val breed: String?,
    val birth: String?,
    val yearOnly: Boolean,
    val gender: Boolean,
    val message: String
    )

data class CreatePetResDto (
    val _metadata: DtoMetadata
    )

// Fetch Dto
data class FetchPetResDto (
    val _metadata: DtoMetadata,
    val petList: List<Pet>
    )

// Update Dto
data class UpdatePetReqDto (
    val id: Long,
    val name: String,
    val species: String?,
    val breed: String?,
    val birth: String?,
    val yearOnly: Boolean?,
    val gender: Boolean?,
    val message: String?
    )

data class UpdatePetResDto (
    val _metadata: DtoMetadata
    )

// Delete Dto
data class PetProfileDeleteRequestDto (
    val id: Long
    )

data class PetProfileDeleteResponseDto (
    val message: String
    )