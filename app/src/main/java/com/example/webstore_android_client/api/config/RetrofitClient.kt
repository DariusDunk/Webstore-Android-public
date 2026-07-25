package com.example.webstore_android_client.api.config

import android.content.Context
import com.example.webstore_android_client.deserializers.LocalDateDeserializer
import com.example.webstore_android_client.deserializers.LocalDateTimeDeserializer
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.deserializers.InstantDeserializer
import com.google.gson.GsonBuilder
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory


object RetrofitClient {
    private const val BASE_URL = "https://bff.domain/android/"

    private lateinit var retrofit: Retrofit


    fun create(context: Context) {

        val sslContext: SSLContext? = SSLContext.getInstance("TLS")

        sslContext?.init(
            null,
            trustAllCerts,//dev mode solution
            SecureRandom()
        )

        val sslSocketFactory: SSLSocketFactory =
            (sslContext?.socketFactory ?: SSLSocketFactory.getDefault()) as SSLSocketFactory

        val sessionManager = SessionManager(context.applicationContext)
        val authInterceptor = SessionInterceptor(sessionManager)


        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val gson = GsonBuilder()
            .registerTypeAdapter(
                LocalDateTime::class.java,
                LocalDateTimeDeserializer()
            )
            .registerTypeAdapter(LocalDate::class.java, LocalDateDeserializer())
            .registerTypeAdapter(Instant::class.java, InstantDeserializer())
            .create()


        val client = OkHttpClient.Builder()
            .sslSocketFactory(
                sslSocketFactory,
                trustAllCerts[0]
            )
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .dns { hostname ->
                if (hostname.contains( "custom.domain", ignoreCase = true ) || hostname == "bff.domain") {
                    listOf(InetAddress.getByName("10.0.2.2"))
                } else {
                    Dns.SYSTEM.lookup(hostname)
                }
            }
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()

        initApiProvider()

    }

    private fun initApiProvider() {
        ApiProvider.init(retrofit)
    }
}

