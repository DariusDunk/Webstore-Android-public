package com.example.webstore_android_client.api.config

import com.example.webstore_android_client.events.SessionEvents
import com.example.webstore_android_client.repositories.RepositoryProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response



class SessionInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {
    companion object {
        const val SESSION_HEADER = "x-session-id"
        const val AUTH_INTENT_HEADER = "x-auth-intent"
    }
    override fun intercept(
        chain: Interceptor.Chain
    ): Response {

        val sessionId = runBlocking {
            sessionManager.getSessionId().first()
        }
        val url = chain.request().url.encodedPath
        val isAuthPath = url.contains("/auth")
        val isCart = url.contains("/cart")

        val requestBuilder =
            chain.request().newBuilder()

        if (!sessionId.isNullOrBlank()) {
            requestBuilder.addHeader(
                SESSION_HEADER,
                sessionId
            )
        }

        if (isCart) {
            val isAuth = RepositoryProvider.customerDataRepository.userState.value != null
            requestBuilder.addHeader(AUTH_INTENT_HEADER,
                isAuth.toString()
            )
        }

        val response =
            chain.proceed(requestBuilder.build())

        val newSessionId =
            response.header(SESSION_HEADER)


        runBlocking {
            if (newSessionId!=null)
            {
                if (newSessionId.isNotBlank()) {
                    sessionManager.saveSessionId(
                        newSessionId
                    )
                } else {
                    println("----------------------------------Response session is blank, deleting local session----------------------------------")
                    sessionManager.deleteSessionId()
                }
            }
        }

        if (response.code == 401 && !isAuthPath) {

            println("----------------------------------Session expired, deleting local session----------------------------------")

            SessionEvents.sessionExpired()
        }

        return response
    }
}