package com.sju18001.petmanagement.restapi

import com.sju18001.petmanagement.restapi.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ServerApi {

    // Create Account, Login API
    @POST("api/account/login")
    fun loginReq(@Body loginReqDto: LoginReqDto): Call<LoginResDto>

    @POST("api/account/create")
    fun createAccountReq(@Body createAccountReqDto: CreateAccountReqDto): Call<CreateAccountResDto>

    @POST("api/account/fetch")
    fun fetchAccountReq(@Body body: RequestBody): Call<FetchAccountResDto>

    @POST("api/account/update")
    fun updateAccountReq(@Body updateAccountReqDto: UpdateAccountReqDto): Call<UpdateAccountResDto>

    @POST("api/account/authcode/send")
    fun sendAuthCodeReq(@Body sendAuthCodeReqDto: SendAuthCodeReqDto): Call<SendAuthCodeResDto>

    @POST("api/account/authcode/verify")
    fun verifyAuthCodeReq(@Body verifyAuthCodeReqDto: VerifyAuthCodeReqDto): Call<VerifyAuthCodeResDto>

    @POST("api/account/recoverUsername")
    fun recoverUsernameReq(@Body recoverUsernameReqDto: RecoverUsernameReqDto): Call<RecoverUsernameResDto>

    @POST("api/account/recoverPassword")
    fun recoverPasswordReq(@Body recoverPasswordReqDto: RecoverPasswordReqDto): Call<RecoverPasswordResDto>

    // Pet Profile CRUD API
    @POST("api/pet/create")
    fun createPetReq(@Body createPetReqDto: CreatePetReqDto): Call<CreatePetResDto>

    @POST("api/pet/fetch")
    fun fetchPetReq(@Body fetchPetReqDto: FetchPetReqDto): Call<FetchPetResDto>

    @POST("api/pet/update")
    fun updatePetReq(@Body updatePetReqDto: UpdatePetReqDto): Call<UpdatePetResDto>

    @POST("api/pet/delete")
    fun deletePetReq(@Body deletePetReqDto: DeletePetReqDto): Call<DeletePetResDto>

    // Pet Photo API
    @POST("api/pet/photo/fetch")
    fun fetchPetPhotoReq(@Body fetchPetPhotoReqDto: FetchPetPhotoReqDto): Call<ResponseBody>

    @Multipart
    @POST("api/pet/photo/update")
    fun updatePetPhotoReq(@Part("id") id: Long, @Part file: MultipartBody.Part): Call<UpdatePetPhotoResDto>

    // PetSchedule API
    @POST("api/pet/schedule/create")
    fun createPetScheduleReq(@Body createPetScheduleReqDto: CreatePetScheduleReqDto): Call<CreatePetScheduleResDto>

    @POST("api/pet/schedule/delete")
    fun deletePetScheduleReq(@Body deletePetScheduleReqDto: DeletePetScheduleReqDto): Call<DeletePetScheduleResDto>

    @POST("api/pet/schedule/fetch")
    fun fetchPetScheduleReq(@Body body: RequestBody): Call<FetchPetScheduleResDto>

    @POST("api/pet/schedule/update")
    fun updatePetScheduleReq(@Body updatePetScheduleReqDto: UpdatePetScheduleReqDto): Call<UpdatePetScheduleResDto>

    // Post API
    @POST("api/post/create")
    fun createPostReq(@Body createPostReqDto: CreatePostReqDto): Call<CreatePostResDto>

    @POST("api/post/fetch")
    fun fetchPostReq(@Body fetchPostReqDto: FetchPostReqDto): Call<FetchPostResDto>
}