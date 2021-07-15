package com.sju18001.petmanagement.restapi

data class AccountLoginRequestDTO (
    val username: String,
    val password: String
)

data class AccountLoginResponseDTO (
    val token: String
)