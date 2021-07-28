package com.sju18001.petmanagement.restapi

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 싱글톤 패턴을 사용하므로, RetrofitBuilder.* 형태로 호출합니다.
class RetrofitBuilder {
    companion object{
        fun getServerApiWithToken(token: String): ServerApi{
            // 인터셉터 초기화
            val networkInterceptor = Interceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                val response = chain.proceed(newRequest)

                response.newBuilder().build()
            }

            // OkHttpClient 빌드
            val client = OkHttpClient.Builder()
                .addNetworkInterceptor(networkInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ServerApi::class.java)
        }

        fun getServerApi(): ServerApi{
            val retrofit = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .client(OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ServerApi::class.java)
        }
    }
}