package com.sju18001.petmanagement.restapi.dto

// Create Dto
data class PetFeedScheduleCreateRequestDto (
    val pet_id: Long,
    val feed_time: String,
    val memo: String?,
)

data class PetFeedScheduleCreateResponseDto (
    val message: String
)

// Fetch Dto
data class PetFeedScheduleFetchResponseDto (
    val id: Long,
    val pet_id: Long,
    val feed_time: String,
    val memo: String?,
    val name: Boolean,
)

// Update Dto
data class PetFeedScheduleUpdateRequestDto (
    val id: Long,
    val pet_id: Long,
    val feed_time: String,
    val memo: String?,
    val name: Boolean,
)

data class PetFeedScheduleUpdateResponseDto (
    val message: String
)

// Delete Dto
data class PetFeedScheduleDeleteRequestDto (
    val id: Long
)

data class PetFeedScheduleDeleteResponseDto (
    val message: String
)
