package com.example.webstore_android_client.model.requests.auth

data class LoginRequest(
    val email: String,
    val password: String,
    val rememberMe: Boolean = false
)
