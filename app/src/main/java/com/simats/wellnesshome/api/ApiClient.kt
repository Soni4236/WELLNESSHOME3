package com.simats.wellnesshome.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // REPLACE THIS WITH YOUR ACTUAL BACKEND URL
    private const val BASE_URL = "http://172.20.10.3/wellness/" 

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}
