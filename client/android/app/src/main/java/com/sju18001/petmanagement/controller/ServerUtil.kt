package com.sju18001.petmanagement.controller

import android.util.Log
import com.sju18001.petmanagement.restapi.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class ServerUtil {
    companion object{
        fun updateProfile(token: String, accountProfileUpdateRequestDto: AccountProfileUpdateRequestDto){
            val call = RetrofitBuilder.getServerApiWithToken(token).profileUpdateRequest(accountProfileUpdateRequestDto)
            call.enqueue(object: Callback<AccountProfileUpdateResponseDto> {
                override fun onResponse(
                    call: Call<AccountProfileUpdateResponseDto>,
                    response: Response<AccountProfileUpdateResponseDto>
                ) {
                }

                override fun onFailure(call: Call<AccountProfileUpdateResponseDto>, t: Throwable) {
                    Log.e("ServerUtil", t.message.toString())
                }
            })
        }

        fun sendAuthCode(accountSendAuthCodeRequestDto: AccountSendAuthCodeRequestDto, responseCallback: (Response<AccountSendAuthCodeResponseDto>) -> Unit, failureCallback: (Throwable) -> Unit){
            val call = RetrofitBuilder.getServerApi().sendAuthCodeRequest(accountSendAuthCodeRequestDto)
            call.enqueue(object: Callback<AccountSendAuthCodeResponseDto> {
                override fun onResponse(
                    call: Call<AccountSendAuthCodeResponseDto>,
                    response: Response<AccountSendAuthCodeResponseDto>
                ) {
                    responseCallback(response)
                }

                override fun onFailure(call: Call<AccountSendAuthCodeResponseDto>, t: Throwable) {
                    failureCallback(t)
                }
            })
        }

        fun findPassword(accountFindPasswordRequestDto: AccountFindPasswordRequestDto, responseCallback: (Response<AccountFindPasswordResponseDto>) -> Unit, failureCallback: (Throwable) -> Unit){
            val call = RetrofitBuilder.getServerApi().findPasswordRequest(accountFindPasswordRequestDto)
            call.enqueue(object: Callback<AccountFindPasswordResponseDto> {
                override fun onResponse(
                    call: Call<AccountFindPasswordResponseDto>,
                    response: Response<AccountFindPasswordResponseDto>
                ) {
                    responseCallback(response)
                }

                override fun onFailure(call: Call<AccountFindPasswordResponseDto>, t: Throwable) {
                    failureCallback(t)
                }
            })
        }
    }
}