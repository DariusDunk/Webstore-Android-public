package com.example.webstore_android_client.model.responses.error

data class ErrorResponse(
    val type: String = "",
    val title: String = "",
    val status: Int = 0,
    val detail: String = ""
)
