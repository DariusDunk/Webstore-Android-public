package com.example.webstore_android_client.ui.productBasic

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.webstore_android_client.R

@Composable
fun ProductImage(
    productCode: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String = "Product image",
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current
    val fallbackPainter = painterResource(R.drawable.no_image_product)

//    println("----------------------------------Product image url: $PRODUCT_IMAGE_BASE_URL/$productCode/$imageUrl ----------------------------------")

    val model: Any? = if (!imageUrl.isNullOrBlank()) {
        ImageRequest.Builder(context)
            .data("$PRODUCT_IMAGE_BASE_URL/$productCode/$imageUrl")
            .crossfade(true)
            .build()
    } else null

    AsyncImage(
        model              = model,
        contentDescription = contentDescription,
        modifier           = modifier,
        contentScale       = contentScale,
        fallback           = fallbackPainter,
        error              = fallbackPainter,
        placeholder        = fallbackPainter
    )
}
