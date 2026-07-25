package com.example.webstore_android_client

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.example.webstore_android_client.api.config.ImageLoaderClient

class WebStoreApplication: Application(), SingletonImageLoader.Factory {

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoaderClient.createClient(this)
    }

}
