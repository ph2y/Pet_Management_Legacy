package com.sju18001.petmanagement.restapi

import com.sju18001.petmanagement.restapi.dto.*
import okhttp3.RequestBody
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

    @POST("api/account/profilelookup")
    fun profileLookupRequest(@Body body: RequestBody): Call<AccountProfileLookupResponseDto>

    @POST("api/account/profileupdate")
    fun profileUpdateRequest(@Body accountProfileUpdateRequestDto: AccountProfileUpdateRequestDto): Call<AccountProfileUpdateResponseDto>

    @POST("api/account/sendauthcode")
    fun sendAuthCodeRequest(@Body accountSendAuthCodeRequestDto: AccountSendAuthCodeRequestDto): Call<AccountSendAuthCodeResponseDto>

    @POST("api/account/verifyauthcode")
    fun verifyAuthCodeRequest(@Body accountVerifyAuthCodeRequestDto: AccountVerifyAuthCodeRequestDto): Call<AccountVerifyAuthCodeResponseDto>

    @POST("api/account/findusername")
    fun findUsernameRequest(@Body accountFindUsernameRequestDto: AccountFindUsernameRequestDto): Call<AccountFindUsernameResponseDto>

    @POST("api/account/findpassword")
    fun findPasswordRequest(@Body accountFindPasswordRequestDto: AccountFindPasswordRequestDto): Call<AccountFindPasswordResponseDto>

    // Pet Profile CRUD API
    @POST("api/pet/profile/create")
    fun petProfileCreateRequest(@Body petProfileCreateRequestDto: PetProfileCreateRequestDto): Call<PetProfileCreateResponseDto>

    @POST("api/pet/profile/fetch")
    fun petProfileFetchRequest(): Call<List<PetProfileFetchResponseDto>>

    @POST("api/pet/profile/update")
    fun petProfileUpdateRequest(@Header("Authorization") token: String, @Body petProfileUpdateRequestDto: PetProfileUpdateRequestDto): Call<PetProfileUpdateResponseDto>

    @POST("api/pet/profile/delete")
    fun petProfileDeleteRequest(@Header("Authorization") token: String, @Body petProfileDeleteRequestDto: PetProfileDeleteRequestDto): Call<PetProfileDeleteResponseDto>

    // PetFeedSchedule API
    @POST("api/pet/feed/create")
    fun petFeedScheduleCreateRequest(@Body petFeedScheduleCreateRequestDto: PetFeedScheduleCreateRequestDto): Call<PetFeedScheduleCreateResponseDto>

    @POST("api/pet/feed/delete")
    fun petFeedScheduleDeleteRequest(@Body petFeedScheduleDeleteRequestDto: PetFeedScheduleDeleteRequestDto): Call<PetFeedScheduleDeleteResponseDto>

    @POST("api/pet/feed/delete")
    fun petFeedScheduleFetchRequest(): Call<PetFeedScheduleFetchResponseDto>

    @POST("api/pet/feed/update")
    fun petFeedScheduleUpdateRequest(@Body petFeedScheduleUpdateRequestDto: PetFeedScheduleUpdateRequestDto): Call<PetFeedScheduleUpdateResponseDto>
}