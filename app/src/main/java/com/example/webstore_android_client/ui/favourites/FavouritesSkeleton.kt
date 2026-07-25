package com.example.webstore_android_client.ui.favourites

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.webstore_android_client.ui.theme.AppBackgroundLight
import com.example.webstore_android_client.ui.theme.SkeletonCardDark
import com.example.webstore_android_client.ui.theme.SkeletonPulseDark
import com.example.webstore_android_client.ui.theme.WhiteCustom

 
@Composable
fun FavouritesSkeleton(isDark: Boolean) {
    val pulseBg = if (isDark) SkeletonPulseDark else AppBackgroundLight  
    val cardBg  = if (isDark) SkeletonCardDark  else WhiteCustom         

    val transition = rememberInfiniteTransition(label = "favSkeleton")
    val alpha by transition.animateFloat(
        initialValue  = 0.35f,
        targetValue   = 0.9f,
        animationSpec = infiniteRepeatable(
            animation  = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(5) {
            FavouriteItemSkeleton(pulseBg = pulseBg, cardBg = cardBg, alpha = alpha)
        }
    }
}


 
@Composable
private fun FavouriteItemSkeleton(pulseBg: Color, cardBg: Color, alpha: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(cardBg)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ---- Product image placeholder ------------------------------------------
        Box(
            modifier = Modifier
                .size(80.dp)
                .alpha(alpha)
                .clip(RoundedCornerShape(6.dp))
                .background(pulseBg)
        )

        Spacer(Modifier.width(16.dp))

        // ---- Text content area ------------------------------------------------─
        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(18.dp)
                    .alpha(alpha)
                    .clip(RoundedCornerShape(4.dp))
                    .background(pulseBg)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .alpha(alpha)
                    .clip(RoundedCornerShape(4.dp))
                    .background(pulseBg)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .alpha(alpha)
                            .clip(CircleShape)
                            .background(pulseBg)
                    )
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        // ---- Action column ----------------------------------------------------─
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(pulseBg)
            )
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(24.dp)
                    .alpha(alpha)
                    .clip(RoundedCornerShape(4.dp))
                    .background(pulseBg)
            )
        }
    }
}
