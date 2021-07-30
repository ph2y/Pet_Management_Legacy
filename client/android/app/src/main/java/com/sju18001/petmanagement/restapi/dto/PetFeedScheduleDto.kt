package com.sju18001.petmanagement.restapi.dto
import java.time.LocalTime

data class PetFeedScheduleCreateRequestDto(
    val pet_id: Long,
    val feed_time: LocalTime,
    val memo: String
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
    val pet_id: Long,
    val feed_time: String,
    val memo: String,
    val is_turned_on: Boolean
)

data class PetFeedScheduleUpdateRequestDto(
    val id: Long,
    val pet_id: Long,
    val feed_time: LocalTime,
    val memo: String,
    val is_turned_on: Boolean
)

data class PetFeedScheduleUpdateResponseDto(
    val message: String
)