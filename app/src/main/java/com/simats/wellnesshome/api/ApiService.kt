package com.simats.wellnesshome.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    
    @POST("auth/register.php") // Endpoint: /auth/register.php
    fun signup(@Body request: SignupRequest): Call<AuthResponse>

    @POST("auth/login.php") // Endpoint: /auth/login.php
    fun login(@Body request: LoginRequest): Call<AuthResponse>
}
