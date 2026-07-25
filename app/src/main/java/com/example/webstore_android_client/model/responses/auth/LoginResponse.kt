package com.example.webstore_android_client.model.responses.auth

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("username")
    val username:String,
    @SerializedName("customer_pfp")
    val customerPfp:String,
    @SerializedName("role")
    val role:String
)