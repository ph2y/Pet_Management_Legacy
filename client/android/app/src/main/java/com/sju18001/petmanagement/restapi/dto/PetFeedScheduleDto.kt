package com.sju18001.petmanagement.restapi.dto

import com.sju18001.petmanagement.restapi.dao.DtoMetadata
import com.sju18001.petmanagement.restapi.dao.PetSchedule

data class CreatePetScheduleReqDto(
    val petIdList: String?,
    val time: String,
    val memo: String?
)

data class CreatePetScheduleResDto(
    val _metadata: DtoMetadata
)

data class DeletePetScheduleReqDto(
    val id: Long
)

data class DeletePetScheduleResDto(
    val _metadata: DtoMetadata
)

data class FetchPetScheduleResDto(
    val _metadata: DtoMetadata,
    val petScheduleList: List<PetSchedule>
)

data class UpdatePetScheduleReqDto(
    val id: Long,
    val petIdList: String?,
    val time: String,
    val memo: String?,
    val enable: Boolean
)

data class UpdatePetScheduleResDto(
    val _metadata: DtoMetadata
)