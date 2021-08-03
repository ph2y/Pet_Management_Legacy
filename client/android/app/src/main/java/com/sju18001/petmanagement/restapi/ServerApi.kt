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
    fun loginReq(@Body loginReqDto: LoginReqDto): Call<LoginResDto>

    @POST("api/account/signup")
    fun signUpRequest(@Body accountSignUpRequestDto: AccountSignUpRequestDto): Call<AccountSignUpResponseDto>

    @POST("api/account/fetch")
    fun fetchAccountReq(@Body body: RequestBody): Call<FetchAccountResDto>

    @POST("api/account/update")
    fun updateAccountReq(@Body updateAccountReqDto: UpdateAccountReqDto): Call<UpdateAccountResDto>

    @POST("api/account/authcode/send")
    fun sendAuthCodeReq(@Body sendAuthCodeReqDto: SendAuthCodeReqDto): Call<SendAuthCodeResDto>

    @POST("api/account/verifyauthcode")
    fun verifyAuthCodeRequest(@Body accountVerifyAuthCodeRequestDto: AccountVerifyAuthCodeRequestDto): Call<AccountVerifyAuthCodeResponseDto>

    @POST("api/account/recoverUsername")
    fun recoverUsernameReq(@Body recoverUsernameReqDto: RecoverUsernameReqDto): Call<RecoverUsernameResDto>

    @POST("api/account/recoverPassword")
    fun recoverPasswordReq(@Body recoverPasswordReqDto: RecoverPasswordReqDto): Call<RecoverPasswordResDto>

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