package com.sju18001.petmanagement.restapi

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 싱글톤 패턴을 사용하므로, RetrofitBuilder.* 형태로 호출합니다.
class RetrofitBuilder {
    companion object{
        const val BASE_URL = "http://220.85.251.6:9000"
        // const val BASE_URL = "http://10.0.2.2:8080"

        fun getServerApiWithToken(token: String): ServerApi{
            // 인터셉터 초기화
            val networkInterceptor = Interceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                val response = chain.proceed(newRequest)

                response.newBuilder().build()
            }

            // For Logging
            val hlt = HttpLoggingInterceptor()
            hlt.level = HttpLoggingInterceptor.Level.BODY

            // OkHttpClient 빌드
            val client = OkHttpClient.Builder()
                .addNetworkInterceptor(hlt)
                .addNetworkInterceptor(networkInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("$BASE_URL/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ServerApi::class.java)
        }

        fun getServerApiWithTokenWithRangeRequest(token: String, range: Long): ServerApi{
            // 인터셉터 초기화
            val networkInterceptor = Interceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Range","bytes=")
                    .build()
                val response = chain.proceed(newRequest)

                response.newBuilder().build()
            }

            // For Logging
            val hlt = HttpLoggingInterceptor()
            hlt.level = HttpLoggingInterceptor.Level.BODY

            // OkHttpClient 빌드
            val client = OkHttpClient.Builder()
                .addNetworkInterceptor(hlt)
                .addNetworkInterceptor(networkInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("$BASE_URL/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ServerApi::class.java)
        }

        fun getServerApi(): ServerApi{
            // For Logging
            val hlt = HttpLoggingInterceptor()
            hlt.level = HttpLoggingInterceptor.Level.BODY

            // OkHttpClient 빌드
            val client = OkHttpClient.Builder()
                .addNetworkInterceptor(hlt)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("$BASE_URL/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ServerApi::class.java)
        }
    }
}