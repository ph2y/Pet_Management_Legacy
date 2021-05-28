package com.sju18001.petmanagement.restapi

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoApi {
    @GET("v2/local/search/keyword.json")
    fun getSearchKeyword(
        @Header("Authorization") key: String,
        @Query("query") query: String,
        @Query("x") x: String,
        @Query("y") y: String,
        @Query("radius") radius: Int
    ): Call<Documents>
}