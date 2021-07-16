package com.sju18001.petmanagement.restapi

data class AccountSignInRequestDto (
    val username: String,
    val password: String
)

data class AccountSignInResponseDto (
    val token: String
)