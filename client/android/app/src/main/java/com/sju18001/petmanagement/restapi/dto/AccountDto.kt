package com.sju18001.petmanagement.restapi.dto

data class LoginReqDto (
    val username: String,
    val password: String
)

data class LoginResDto (
    val _metadata: DtoMetadata,
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

data class AccountFindUsernameRequestDto(
    val email: String
)

data class AccountFindUsernameResponseDto(
    val username: String?,
    val message: String
)

data class AccountFindPasswordRequestDto(
    val username: String,
    val code: String
)

data class AccountFindPasswordResponseDto(
    val message: String
)
