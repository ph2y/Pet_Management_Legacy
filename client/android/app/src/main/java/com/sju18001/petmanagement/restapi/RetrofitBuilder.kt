package com.sju18001.petmanagement.restapi

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 싱글톤 패턴을 사용하므로, RetrofitBuilder.* 형태로 호출합니다.
object RetrofitBuilder {
    var serverApi: ServerApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        serverApi = retrofit.create(ServerApi::class.java)
    }
}