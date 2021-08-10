package com.sju18001.petmanagement.restapi.dto

import com.sju18001.petmanagement.restapi.global.DtoMetadata

data class LoginReqDto (
    val username: String,
    val password: String
)

data class LoginResDto (
    val _metadata: DtoMetadata,
    val token: String?
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

data class RecoverUsernameReqDto(
    val email: String
)

data class RecoverUsernameResDto(
    val _metadata: DtoMetadata,
    val username: String?
)

data class RecoverPasswordReqDto(
    val username: String,
    val code: String
)

data class RecoverPasswordResDto(
    val _metadata: DtoMetadata
)

data class DeleteAccountResDto(
    val _metadata: DtoMetadata
)