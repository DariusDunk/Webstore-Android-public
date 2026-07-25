package com.example.webstore_android_client.model.requests.auth

data class RegisterRequest(
    val name: String,
    val familyName: String,
    val email: String,
    val password: String
)
