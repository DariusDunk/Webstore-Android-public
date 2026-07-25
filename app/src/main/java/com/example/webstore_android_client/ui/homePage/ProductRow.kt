package com.example.webstore_android_client.ui.homePage

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import com.example.webstore_android_client.model.responses.product.CompactProductResponse
import com.example.webstore_android_client.ui.productBasic.ProductCard
import com.example.webstore_android_client.ui.theme.RowBgDark
import com.example.webstore_android_client.ui.theme.RowBgLight

@Composable
fun ProductRow(
    title: String,
    products: List<CompactProductResponse>,
    onProductClick: (productCode: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) return

    val isDark = isSystemInDarkTheme()
    val rowBg      = if (isDark) RowBgDark  else RowBgLight
    val titleColor = if (isDark) Color.White else Color.Black

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val visibleCardCount = when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 4
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 3
        else -> 2
    }

    val density = LocalDensity.current
    val containerWidthPx = LocalWindowInfo.current.containerSize.width
    val screenWidthDp = with(density) { containerWidthPx.toDp() }

    val contentPadding = 16.dp
    val cardGap        = 8.dp

    val totalGapSpace = cardGap * (visibleCardCount - 1)

    val availableWidth = screenWidthDp - (contentPadding * 2) - totalGapSpace
    val cardWidth      = availableWidth / visibleCardCount

    val listState   = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(listState)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(rowBg)
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        // ----------------------------------Row title ----------------------------------
        Text(
            text       = title,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            color      = titleColor,
            modifier   = Modifier
                .padding(horizontal = contentPadding)
                .padding(bottom = 12.dp)
        )

        // ---------------------------------- Horizontal product scroll ----------------------------------
        LazyRow(
            state                  = listState,
            flingBehavior          = snapBehavior,
            contentPadding         = PaddingValues(horizontal = contentPadding, vertical = 8.dp),
            horizontalArrangement  = Arrangement.spacedBy(cardGap)
        ) {
            items(
                items = products,
                key   = { it.productCode }
            ) { product ->
                ProductCard(
                    onProductClick = onProductClick,
                    modifier = Modifier.width(cardWidth),
                    compactProductResponse = product,
                )
            }
        }
    }
}