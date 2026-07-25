package com.example.webstore_android_client.api.utils

import com.example.webstore_android_client.model.responses.error.ErrorResponse

sealed interface ApiResult<out T> {

    data class Success<T>(
        val data: T
    ) : ApiResult<T>

    data class Failure(
        val error: ErrorResponse
    ) : ApiResult<Nothing>

    data class NetworkError(
        val exception: Throwable
    ) : ApiResult<Nothing>
}