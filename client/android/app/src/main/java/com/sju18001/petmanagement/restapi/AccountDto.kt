package com.sju18001.petmanagement.restapi

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

data class AccountProfileLookupResponseDto(
    val message: String,
    val username: String,
    val email: String,
    val name: String?,
    val phone: String,
    val photo: String?
)

data class AccountProfileUpdateRequestDto(
    val email: String,
    val name: String,
    val phone: String,
    val photo: String?
)

data class AccountProfileUpdateResponseDto(
    val message: String
)