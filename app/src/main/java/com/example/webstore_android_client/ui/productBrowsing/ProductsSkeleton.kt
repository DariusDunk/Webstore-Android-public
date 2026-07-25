package com.example.webstore_android_client.ui.productBrowsing

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.webstore_android_client.ui.theme.AppBackgroundLight
import com.example.webstore_android_client.ui.theme.CardWhite
import com.example.webstore_android_client.ui.theme.PageBgDark
import com.example.webstore_android_client.ui.theme.PageBgLight
import com.example.webstore_android_client.ui.theme.RowBgDark
import com.example.webstore_android_client.ui.theme.RowBgLight
import com.example.webstore_android_client.ui.theme.SkeletonCardDark
import com.example.webstore_android_client.ui.theme.SkeletonPulseDark

 
@Composable
fun ProductsSkeleton() {
    val isDark     = isSystemInDarkTheme()
    val pageBg     = if (isDark) PageBgDark     else PageBgLight
    val rowBg      = if (isDark) RowBgDark      else RowBgLight
    val pulseBg    = if (isDark) SkeletonPulseDark else AppBackgroundLight
    val cardBg     = if (isDark) SkeletonCardDark  else CardWhite

    val transition = rememberInfiniteTransition(label = "productsSkeleton")
    val alpha by transition.animateFloat(
        initialValue  = 0.35f,
        targetValue   = 0.9f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )

    LazyVerticalGrid(
        columns              = GridCells.Fixed(2),
        modifier             = Modifier.fillMaxSize().background(pageBg),
        contentPadding       = androidx.compose.foundation.layout.PaddingValues(8.dp),
        verticalArrangement  = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ------------- Sort / filter bar skeleton   ---------------------------
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(rowBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(180.dp)
                        .alpha(alpha)
                        .clip(RoundedCornerShape(6.dp))
                        .background(pulseBg)
                )
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(80.dp)
                        .alpha(alpha)
                        .clip(RoundedCornerShape(6.dp))
                        .background(pulseBg)
                )
            }
        }

        // ------------------  skeleton product cards  --------------------
        items(8) {
            SkeletonProductCard(
                pulseBg = pulseBg,
                cardBg  = cardBg,
                alpha   = alpha
            )
        }
    }
}
 
@Composable
private fun SkeletonProductCard(
    pulseBg: Color,
    cardBg: Color,
    alpha: Float
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(cardBg)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .alpha(alpha)
                .clip(RoundedCornerShape(4.dp))
                .background(pulseBg)
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(14.dp)
                .alpha(alpha)
                .clip(RoundedCornerShape(4.dp))
                .background(pulseBg)
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(14.dp)
                .alpha(alpha)
                .clip(RoundedCornerShape(4.dp))
                .background(pulseBg)
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .alpha(alpha)
                .clip(RoundedCornerShape(4.dp))
                .background(pulseBg)
        )
    }
}
