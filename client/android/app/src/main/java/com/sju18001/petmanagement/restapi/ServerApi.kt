package com.sju18001.petmanagement.restapi

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ServerApi {
    @POST("api/account/login")
    fun signInRequest(@Body accountSignInRequestDto: AccountSignInRequestDto): Call<AccountSignInResponseDto>

    @POST("api/account/signup")
    fun signUpRequest(@Body accountSignUpRequestDto: AccountSignUpRequestDto): Call<AccountSignUpResponseDto>
}