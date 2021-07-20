package com.sju18001.petmanagement.restapi.dto

data class AccountSignInRequestDto (
    val username: String,
    val password: String
)

data class AccountSignInResponseDto (
    val token: String
)

data class AccountSignUpRequestDto (
    val username: String,
    val password: String,
    val email: String,
    val name: String,
    val phone: String,
    val photo: String?,
    val marketing: Boolean?
)

data class AccountSignUpResponseDto (
    val message: String
)