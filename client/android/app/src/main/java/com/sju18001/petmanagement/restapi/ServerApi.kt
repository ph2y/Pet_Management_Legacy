package com.sju18001.petmanagement.restapi

import com.sju18001.petmanagement.restapi.dto.*
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ServerApi {

    // Create Account, Login API
    @POST("api/account/login")
    fun loginReq(@Body loginReqDto: LoginReqDto): Call<LoginResDto>

    @POST("api/account/create")
    fun createAccountReq(@Body createAccountReqDto: CreateAccountReqDto): Call<CreateAccountResDto>

    @POST("api/account/profilelookup")
    fun profileLookupRequest(@Body body: RequestBody): Call<AccountProfileLookupResponseDto>

    @POST("api/account/profileupdate")
    fun profileUpdateRequest(@Body accountProfileUpdateRequestDto: AccountProfileUpdateRequestDto): Call<AccountProfileUpdateResponseDto>

    @POST("api/account/authcode/send")
    fun sendAuthCodeReq(@Body accountSendAuthCodeRequestDto: SendAuthCodeReqDto): Call<SendAuthCodeResDto>

    @POST("api/account/authcode/verify")
    fun verifyAuthCodeRequest(@Body accountVerifyAuthCodeRequestDto: VerifyAuthCodeReqDto): Call<VerifyAuthCodeResDto>

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
    fun petProfileUpdateRequest(@Body petProfileUpdateRequestDto: PetProfileUpdateRequestDto): Call<PetProfileUpdateResponseDto>

    @POST("api/pet/profile/delete")
    fun petProfileDeleteRequest(@Body petProfileDeleteRequestDto: PetProfileDeleteRequestDto): Call<PetProfileDeleteResponseDto>

    // PetFeedSchedule API
    @POST("api/pet/feed/create")
    fun petFeedScheduleCreateRequest(@Body petFeedScheduleCreateRequestDto: PetFeedScheduleCreateRequestDto): Call<PetFeedScheduleCreateResponseDto>

    @POST("api/pet/feed/delete")
    fun petFeedScheduleDeleteRequest(@Body petFeedScheduleDeleteRequestDto: PetFeedScheduleDeleteRequestDto): Call<PetFeedScheduleDeleteResponseDto>

    @POST("api/pet/feed/fetch")
    fun petFeedScheduleFetchRequest(@Body body: RequestBody): Call<List<PetFeedScheduleFetchResponseDto>>

    @POST("api/pet/feed/update")
    fun petFeedScheduleUpdateRequest(@Body petFeedScheduleUpdateRequestDto: PetFeedScheduleUpdateRequestDto): Call<PetFeedScheduleUpdateResponseDto>
}