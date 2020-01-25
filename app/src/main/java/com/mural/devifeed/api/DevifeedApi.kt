package com.mural.devifeed.api

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface DevifeedApi {

    companion object {
        private const val BASE_URL = "https://www.reddit.com/"

        fun create(): DevifeedApi {
            HttpUrl.parse(BASE_URL).let {
                val client = OkHttpClient.Builder()
                    .build()
                return Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(it)
                    .client(client)
                    .build()
                    .create(DevifeedApi::class.java)
            }
        }
    }

    @GET("/r/all/top.json")
    fun getTop(@Query("limit") limit: Int): Call<ListingParser>

    @GET("/r/all/top.json")
    fun getTopAfter(@Query("after") after: String, @Query("limit") limit: Int): Call<ListingParser>

}