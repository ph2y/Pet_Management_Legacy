package com.sju18001.petmanagement.restapi.dao

data class Pet(
    val id: Long,
    val ownername: String,
    val name: String,
    val species: String,
    val breed: String,
    val birth: String?,
    val yearOnly: Boolean?,
    val gender: Boolean,
    val message: String?,
    val photoUrl: String?
)
