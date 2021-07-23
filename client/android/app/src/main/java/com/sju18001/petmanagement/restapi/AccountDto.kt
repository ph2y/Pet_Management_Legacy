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
    val nickname: String?,
    val phone: String,
    val photo: String?,
    val marketing: Boolean,
    val userMessage: String?
)

data class AccountSignUpResponseDto (
    val message: String
)

data class AccountSendAuthCodeRequestDto(
    val email: String
)

data class AccountSendAuthCodeResponseDto(
    val message: String
)

data class AccountVerifyAuthCodeRequestDto(
    val email: String,
    val code: String
)

data class AccountVerifyAuthCodeResponseDto(
    val message: String
)

data class AccountProfileLookupResponseDto(
    val message: String,
    val username: String,
    val email: String,
    val nickname: String?,
    val phone: String,
    val photo: String?,
    val marketing: Boolean?,
    val userMessage: String?
)

data class AccountProfileUpdateRequestDto(
    val username: String,
    val email: String,
    val nickname: String?,
    val phone: String,
    val photo: String?,
    val marketing: Boolean?,
    val userMessage: String?
)

data class AccountProfileUpdateResponseDto(
    val message: String
)