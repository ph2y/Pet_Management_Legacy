package com.sju18001.petmanagement.restapi

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {
    var accountApi: AccountAPI

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        accountApi = retrofit.create(AccountAPI::class.java)
    }
}