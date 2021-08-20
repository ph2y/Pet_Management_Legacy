package com.sju18001.petmanagement.restapi

import androidx.annotation.Nullable
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

    @POST("api/account/fetch")
    fun fetchAccountByNicknameReq(@Body fetchAccountReqDto: FetchAccountReqDto): Call<FetchAccountResDto>

    @POST("api/account/update")
    fun updateAccountReq(@Body updateAccountReqDto: UpdateAccountReqDto): Call<UpdateAccountResDto>

    @POST("api/account/delete")
    fun deleteAccountReq(@Body body: RequestBody): Call<DeleteAccountResDto>

    // Account Photo API
    @POST("api/account/photo/fetch")
    fun fetchAccountPhotoReq(@Body fetchAccountPhotoReqDto: FetchAccountPhotoReqDto): Call<ResponseBody>

    @Multipart
    @POST("api/account/photo/update")
    fun updateAccountPhotoReq(@Part file: MultipartBody.Part): Call<UpdateAccountPhotoResDto>

    // Account Password Change API
    @POST("api/account/password/update")
    fun updateAccountPasswordReq(@Body updateAccountPasswordReqDto: UpdateAccountPasswordReqDto): Call<UpdateAccountPasswordResDto>

    // Account recover API
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

    @POST("api/post/update")
    fun updatePostReq(@Body updatePostReqDto: UpdatePostReqDto): Call<UpdatePostResDto>

    @POST("api/post/fetch")
    fun fetchPostReq(@Body fetchPostReqDto: FetchPostReqDto): Call<FetchPostResDto>

    @POST("api/post/media/fetch")
    fun fetchPostMediaReq(@Body fetchPostMediaReqDto: FetchPostMediaReqDto): Call<ResponseBody>

    @Multipart
    @POST("api/post/media/update")
    fun updatePostMediaReq(@Part("id") id: Long, @Part fileList: List<MultipartBody.Part>): Call<UpdatePostMediaResDto>

    @POST("api/post/media/fetch")
    fun fetchPostMediaReq(@Body fetchPostMediaReqDto: FetchPostMediaReqDto): Call<ResponseBody>

    // Follow API
    @POST("api/community/follower/fetch")
    fun fetchFollowerReq(@Body body: RequestBody): Call<FetchFollowerResDto>

    @POST("api/community/following/fetch")
    fun fetchFollowingReq(@Body body: RequestBody): Call<FetchFollowingResDto>

    @POST("api/community/follow/create")
    fun createFollowReq(@Body createFollowReqDto: CreateFollowReqDto): Call<CreateFollowResDto>

    @POST("api/community/follow/delete")
    fun deleteFollowReq(@Body deleteFollowReqDto: DeleteFollowReqDto): Call<DeleteFollowResDto>

    // Comment API
    @POST("api/comment/create")
    fun createCommentReq(@Body createCommentReqDto: CreateCommentReqDto): Call<CreateCommentResDto>

    @POST("api/comment/fetch")
    fun fetchCommentReq(@Body fetchCommentReqDto: FetchCommentReqDto): Call<FetchCommentResDto>

    @POST("api/comment/update")
    fun updateCommentReq(@Body updateCommentReqDto: UpdateCommentReqDto): Call<UpdateCommentResDto>

    @POST("api/comment/delete")
    fun deleteCommentReq(@Body deleteCommentReqDto: DeleteCommentReqDto): Call<DeleteCommentResDto>
}