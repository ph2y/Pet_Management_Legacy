package com.sju18001.petmanagement.restapi

import com.sju18001.petmanagement.restapi.dto.AccountSignInRequestDto
import com.sju18001.petmanagement.restapi.dto.AccountSignInResponseDto
import com.sju18001.petmanagement.restapi.dto.AccountSignUpRequestDto
import com.sju18001.petmanagement.restapi.dto.AccountSignUpResponseDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ServerApi {

    // Sign in, Sign up API
    @POST("api/account/login")
    fun signInRequest(@Body accountSignInRequestDto: AccountSignInRequestDto): Call<AccountSignInResponseDto>

    @POST("api/account/signup")
    fun signUpRequest(@Body accountSignUpRequestDto: AccountSignUpRequestDto): Call<AccountSignUpResponseDto>
}