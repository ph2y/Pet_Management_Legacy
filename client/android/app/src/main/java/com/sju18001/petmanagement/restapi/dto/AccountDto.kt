package com.sju18001.petmanagement.restapi.dto

data class AccountSignInRequestDto (
    val username: String,
    val password: String
)

data class AccountSignInResponseDto (
    val token: String
)

data class CreateAccountReqDto (
    val username: String,
    val password: String,
    val email: String,
    val phone: String,
    val nickname: String?,
    val marketing: Boolean,
    val userMessage: String?
)

data class CreateAccountResDto (
    val _metadata: DtoMetadata
)

data class SendAuthCodeReqDto(
    val email: String
)

data class SendAuthCodeResDto(
    val _metadata: DtoMetadata
)

data class VerifyAuthCodeReqDto(
    val email: String,
    val code: String
)

data class VerifyAuthCodeResDto(
    val _metadata: DtoMetadata
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
