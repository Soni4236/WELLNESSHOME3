package com.simats.wellnesshome.api

// Request Body for Signup
data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
)

// Request Body for Login
data class LoginRequest(
    val email: String,
    val password: String
)

// Generic Response (adjust based on actual backend)
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val userId: String? = null
)
