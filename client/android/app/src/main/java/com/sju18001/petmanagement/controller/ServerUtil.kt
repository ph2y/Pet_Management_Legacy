package com.sju18001.petmanagement.controller

import android.util.Log
import com.sju18001.petmanagement.restapi.AccountProfileUpdateRequestDto
import com.sju18001.petmanagement.restapi.AccountProfileUpdateResponseDto
import com.sju18001.petmanagement.restapi.RetrofitBuilder
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
    }
}