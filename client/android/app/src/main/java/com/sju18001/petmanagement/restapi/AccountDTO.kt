package com.sju18001.petmanagement.restapi

data class AccountSignInRequestDTO (
    val username: String,
    val password: String
)

data class AccountSignInResponseDTO (
    val token: String
)