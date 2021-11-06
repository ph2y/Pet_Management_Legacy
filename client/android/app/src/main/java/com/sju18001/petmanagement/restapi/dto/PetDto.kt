package com.sju18001.petmanagement.restapi.dto

import com.sju18001.petmanagement.restapi.global.DtoMetadata
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
    val _metadata: DtoMetadata,
    val id: Long
    )

// Fetch Dto
data class FetchPetReqDto(
    val id: Long?,
    val accountUsername: String?
    )

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
data class DeletePetReqDto (
    val id: Long
    )

data class DeletePetResDto (
    val _metadata: DtoMetadata
    )

// Fetch Pet Photo
data class FetchPetPhotoReqDto (
    val id: Long
    )

// Update Photo Dto
data class UpdatePetPhotoResDto (
    val _metadata: DtoMetadata,
    val fileUrl: String?
    )

data class DeletePetPhotoReqDto (
    val id: Long
        )

data class DeletePetPhotoResDto (
    val _metadata: DtoMetadata
        )