package com.example.webstore_android_client.api.utils

import com.example.webstore_android_client.model.responses.error.ErrorResponse
import com.google.gson.Gson
import retrofit2.Response

suspend fun <T> safeApiCall(
    call: suspend () -> Response<T>
): ApiResult<T> {

    return try {

        val response = call()

        if(response.isSuccessful) {

            val body = response.body()

            if (body != null) {
                ApiResult.Success(body)
            } else {
                @Suppress("UNCHECKED_CAST")
                ApiResult.Success(Unit as T)
            }

        } else {

            ApiResult.Failure(
                parseError(response)
            )
        }

    } catch(e: Exception) {

        ApiResult.NetworkError(e)
    }
}

fun parseError(response: Response<*>): ErrorResponse {

    val errorBody = response.errorBody()?.string()

    return try {

        if (errorBody.isNullOrEmpty()) {
            return ErrorResponse(
                status = response.code(),
                title = "Empty error body",
                detail = "No error response from server"
            )
        }

        Gson().fromJson(errorBody, ErrorResponse::class.java)

    } catch (e: Exception) {

        println(
            "\n----------------------------------\n" +
                    "Error parsing failed: ${e.message}" +
                    "\n----------------------------------\n"
        )

        ErrorResponse(
            status = response.code(),
            title = "",
            detail = ""
        )
    }
}