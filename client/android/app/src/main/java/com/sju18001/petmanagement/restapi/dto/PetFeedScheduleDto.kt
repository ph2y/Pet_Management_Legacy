package com.sju18001.petmanagement.restapi.dto

data class PetFeedScheduleCreateRequestDto(
    val pet_id_list: String,
    val feed_time: String,
    val memo: String?
)

data class PetFeedScheduleCreateResponseDto(
    val message: String
)

data class PetFeedScheduleDeleteRequestDto(
    val id: Long
)

data class PetFeedScheduleDeleteResponseDto(
    val message: String
)

data class PetFeedScheduleFetchResponseDto(
    val id: Long,
    val pet_id_list: String,
    val feed_time: String,
    val memo: String?,
    val is_turned_on: Boolean
)

data class PetFeedScheduleUpdateRequestDto(
    val id: Long,
    val pet_id_list: String,
    val feed_time: String,
    val memo: String?,
    val is_turned_on: Boolean
)

data class PetFeedScheduleUpdateResponseDto(
    val message: String
)