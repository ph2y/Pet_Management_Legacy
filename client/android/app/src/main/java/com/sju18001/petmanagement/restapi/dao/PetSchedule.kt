package com.sju18001.petmanagement.restapi.dao

data class PetSchedule (
    val id: Long,
    val username: String,
    val petList: List<Pet>,
    val time: String?,
    val memo: String?,
    val enable: Boolean,
    val petIdList: String
)