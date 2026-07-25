package com.example.webstore_android_client.ui.productBasic

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.webstore_android_client.model.responses.product.CompactProductResponse
import com.example.webstore_android_client.ui.theme.CardWhite
import com.example.webstore_android_client.ui.theme.CartPressedGreen
import com.example.webstore_android_client.ui.theme.ImageBgGrey
import com.example.webstore_android_client.ui.theme.MutedGrey
import com.example.webstore_android_client.ui.theme.OutOfStockRed
import com.example.webstore_android_client.ui.theme.StarAndSaleOrange
import kotlin.math.ceil
import kotlin.math.floor

private fun Modifier.grayscaleAndFade(alpha: Float = 0.75f): Modifier =
    this
        .graphicsLayer { }
        .drawWithContent {
            val paint = Paint().apply {
                this.alpha = alpha
                colorFilter = ColorFilter.colorMatrix(
                    ColorMatrix().apply { setToSaturation(0f) }
                )
            }
            drawIntoCanvas { canvas ->
                canvas.saveLayer(Rect(Offset.Zero, size), paint)
                this@drawWithContent.drawContent()
                canvas.restore()
            }
        }

@Composable
private fun StarRating(
    rating: Float,
    reviewCount: Int,
    modifier: Modifier = Modifier
) {
    val fullStars  = floor(rating).toInt()
    val hasHalf    = rating % 1f != 0f
    val totalShown = minOf(ceil(rating.toDouble()).toInt(), 5)
    val emptyStars = 5 - totalShown

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(horizontal = 10.dp)
    ) {
        repeat(fullStars) {
            Text("★", color = StarAndSaleOrange, fontSize = 17.sp, lineHeight = 20.sp)
        }
        if (hasHalf) {
            Text("☆", color = StarAndSaleOrange, fontSize = 17.sp, lineHeight = 20.sp)
        }
        repeat(emptyStars) {
            Text("☆", color = MutedGrey, fontSize = 17.sp, lineHeight = 20.sp)
        }
        Text(
            text     = "(${reviewCount})",
            color    = MutedGrey,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun PriceDisplay(
    originalPriceStotinki: Int,
    salePriceStotinki: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
        if (originalPriceStotinki != salePriceStotinki) {
            Text(
                text           = "€${"%.2f".format(originalPriceStotinki / 100.0)}",
                color          = MutedGrey,
                fontWeight     = FontWeight.Bold,
                fontSize       = 14.sp,
                textDecoration = TextDecoration.LineThrough
            )
            Text(
                text       = "€${"%.2f".format(salePriceStotinki / 100.0)}",
                color      = StarAndSaleOrange,
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp
            )
        } else {
            Text(
                text       = "€${"%.2f".format(salePriceStotinki / 100.0)}",
                color      = MutedGrey,
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp
            )
        }
    }
}

@Composable
private fun CartButton(
    isLoading: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor = when {
        isDisabled -> Color.White.copy(alpha = 0.6f)
        isPressed  -> CartPressedGreen
        else       -> Color.White
    }
    val borderColor = when {
        isDisabled -> Color.LightGray
        else       -> Color(0xFFD1D5DB)
    }
    val iconTint = if (isPressed && !isDisabled) Color.White else Color(0xFF374151)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                enabled           = !isLoading && !isDisabled,
                onClick           = onClick
            )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color       = CartPressedGreen
            )
        } else {
            Text(
                text     = "🛒",
                fontSize = 18.sp,
                color    = iconTint
            )
        }
    }
}

@Composable
fun ProductCard(
    compactProductResponse: CompactProductResponse,
    onProductClick: (productCode: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductCardViewModel = viewModel(key = compactProductResponse.productCode)
) {
    val uiState  by viewModel.uiState.collectAsState()
    val context  = LocalContext.current
    val isOutOfStock = !compactProductResponse.isInStock

    val ratingValue = if (compactProductResponse.rating != 0) compactProductResponse.rating / 10f else 0f

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = modifier) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isOutOfStock) Modifier.grayscaleAndFade(0.75f) else Modifier
                ),
            shape  = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(
                containerColor         = CardWhite,
                disabledContainerColor = CardWhite
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            onClick   = { onProductClick(compactProductResponse.productCode) },
        ) {
            Box {
                Column(modifier = Modifier.fillMaxWidth()) {

                    ProductImage(
                        productCode = compactProductResponse.productCode,
                        imageUrl = compactProductResponse.imageUrl,
                        contentDescription = compactProductResponse.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .background(ImageBgGrey)
                    )

                    Text(
                        text     = compactProductResponse.name,
                        color    = MutedGrey,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )

                    StarRating(rating = ratingValue, reviewCount = compactProductResponse.reviewCount)

                    PriceDisplay(
                        originalPriceStotinki = compactProductResponse.originalPriceStotinki,
                        salePriceStotinki     = compactProductResponse.salePriceStotinki
                    )

                    Spacer(modifier = Modifier.height(52.dp))
                }

                if (isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.10f))
                    )
                    Text(
                        text       = "Няма наличност",
                        color      = Color.White,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(OutOfStockRed)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        CartButton(
            isLoading  = uiState.isCartLoading,
            isDisabled = isOutOfStock,
            onClick    = { viewModel.addToCart(compactProductResponse.productCode) },
            modifier   = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        )
    }
}
