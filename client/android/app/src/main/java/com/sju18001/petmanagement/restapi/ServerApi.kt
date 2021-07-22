package com.sju18001.petmanagement.restapi

import com.sju18001.petmanagement.restapi.dto.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ServerApi {

    // Sign in, Sign up API
    @POST("api/account/login")
    fun signInRequest(@Body accountSignInRequestDto: AccountSignInRequestDto): Call<AccountSignInResponseDto>

    @POST("api/account/signup")
    fun signUpRequest(@Body accountSignUpRequestDto: AccountSignUpRequestDto): Call<AccountSignUpResponseDto>

    // Pet Profile CRUD API
    @POST("api/pet/profile/create")
    fun petProfileCreateRequest(@Header("Authorization") token: String, @Body petProfileCreateRequestDto: PetProfileCreateRequestDto): Call<PetProfileCreateResponseDto>

    @POST("api/pet/profile/fetch")
    fun petProfileFetchRequest(@Header("Authorization") token: String): Call<List<PetProfileFetchResponseDto>>
}