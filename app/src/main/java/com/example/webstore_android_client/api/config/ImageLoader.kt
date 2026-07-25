package com.example.webstore_android_client.api.config

import android.content.Context
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import okhttp3.Dns
import okhttp3.OkHttpClient
import java.net.InetAddress
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory

object ImageLoaderClient
{

    fun createClient(
        context: Context
    ): ImageLoader {
        val sslContext: SSLContext? = SSLContext.getInstance("TLS")

        sslContext?.init(
            null,
            trustAllCerts,
            SecureRandom()
        )

        val sslSocketFactory: SSLSocketFactory =
            (sslContext?.socketFactory ?: SSLSocketFactory.getDefault()) as SSLSocketFactory

        val client = OkHttpClient.Builder()
            .sslSocketFactory(
                sslSocketFactory,
                trustAllCerts[0]
            )
            .hostnameVerifier { _, _ -> true }
            .dns { hostname ->
            if (hostname == "minio.domain" || hostname == "bff.domain") {
                listOf(InetAddress.getByName("10.0.2.2"))
            } else {
                Dns.SYSTEM.lookup(hostname)
            }
        }.build()

      return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { client }))
            }
            .crossfade(true)
            .build()
    }
}