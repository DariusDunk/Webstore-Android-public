package com.example.webstore_android_client.api.services

import com.example.webstore_android_client.model.requests.auth.LoginRequest
import com.example.webstore_android_client.model.requests.auth.RegisterRequest
import com.example.webstore_android_client.model.responses.auth.CombinedLoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthService {

    @POST("auth/login")
    suspend fun loginUser(@Body user: LoginRequest): Response<CombinedLoginResponse>

    @POST("auth/logout")
    suspend fun logoutUser(): Response<Unit>

    @POST("auth/forgotten-password/{email}")
    suspend fun forgotPassword(@Path("email") email: String): Response<Unit>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>


}
