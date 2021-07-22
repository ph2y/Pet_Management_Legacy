package com.sju18001.petmanagement.restapi

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ServerApi {
    @POST("api/account/login")
    fun signInRequest(@Body accountSignInRequestDto: AccountSignInRequestDto): Call<AccountSignInResponseDto>

    @POST("api/account/signup")
    fun signUpRequest(@Body accountSignUpRequestDto: AccountSignUpRequestDto): Call<AccountSignUpResponseDto>

    @POST("api/account/profilelookup")
    fun profileLookupRequest(@Body body: RequestBody): Call<AccountProfileLookupResponseDto>

    @POST("api/account/profileupdate")
    fun profileUpdateRequest(@Body accountProfileUpdateRequestDto: AccountProfileUpdateRequestDto): Call<AccountProfileUpdateResponseDto>

    @POST("api/account/findusername")
    fun findUsernameRequest(@Body accountFindUsernameRequestDto: AccountFindUsernameRequestDto): Call<AccountFindUsernameResponseDto>

    @POST("api/account/findpassword")
    fun findPasswordRequest(@Body accountFindPasswordRequestDto: AccountFindPasswordRequestDto): Call<AccountFindPasswordResponseDto>

    @POST("api/account/sendauthcode")
    fun sendAuthCodeRequest(@Body accountSendAuthCodeRequestDto: AccountSendAuthCodeRequestDto): Call<AccountSendAuthCodeResponseDto>
}