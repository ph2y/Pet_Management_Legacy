package com.sju18001.petmanagement.restapi.dto

data class LoginReqDto (
    val username: String,
    val password: String
)

data class LoginResDto (
    val _metadata: DtoMetadata,
    val token: String?
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

data class FetchAccountResDto(
    val _metadata: DtoMetadata,
    val id: Long,
    val username: String,
    val email: String,
    val phone: String,
    val marketing: Boolean?,
    val nickname: String?,
    val photoUrl: String?,
    val userMessage: String?
)

data class UpdateAccountReqDto(
    val email: String,
    val phone: String,
    val nickname: String?,
    val marketing: Boolean?,
    val userMessage: String?
)

data class UpdateAccountResDto(
    val _metadata: DtoMetadata
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
