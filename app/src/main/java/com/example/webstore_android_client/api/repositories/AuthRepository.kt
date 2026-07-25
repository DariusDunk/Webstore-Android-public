package com.example.webstore_android_client.api.repositories

import com.example.webstore_android_client.api.services.AuthService
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.api.utils.safeApiCall
import com.example.webstore_android_client.model.requests.auth.LoginRequest
import com.example.webstore_android_client.model.requests.auth.RegisterRequest
import com.example.webstore_android_client.model.responses.auth.CombinedLoginResponse

class AuthRepository(private val authService: AuthService) {

    suspend fun logoutUser(): ApiResult<Unit> {
        return safeApiCall { authService.logoutUser() }
    }

    suspend fun loginUser( user: LoginRequest): ApiResult<CombinedLoginResponse>
    {
        return safeApiCall { authService.loginUser(user) }
    }

    suspend fun forgotPassword(email: String): ApiResult<Unit> {
        return safeApiCall { authService.forgotPassword(email) }
    }

    suspend fun register(request: RegisterRequest): ApiResult<Unit>
    {
        return safeApiCall { authService.register(request) }
    }
}