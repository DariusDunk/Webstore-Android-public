package com.example.webstore_android_client.ui.homePage


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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun HomePageSkeleton() {
    val isDark = isSystemInDarkTheme()
    val pageBg = if (isDark) PageBgDark else PageBgLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(3) {
                SkeletonRow(isDark = isDark)
            }
        }
    }
}

// ---------------------------------- Skeleton row ----------------------------------

@Composable
private fun SkeletonRow(isDark: Boolean) {
    val rowBg      = if (isDark) RowBgDark       else RowBgLight
    val pulseBg    = if (isDark) SkeletonPulseDark else AppBackgroundLight
    val cardBg     = if (isDark) SkeletonCardDark  else CardWhite

    val transition = rememberInfiniteTransition(label = "skeletonRow")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue  = 0.85f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(rowBg)
            .padding(16.dp)
    ) {
        // ----------------------------------Title placeholder  ----------------------------------
        Box(
            modifier = Modifier
                .height(22.dp)
                .width(200.dp)
                .alpha(alpha)
                .clip(RoundedCornerShape(4.dp))
                .background(pulseBg)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(2) {
                SkeletonCard(
                    pulseBg = pulseBg,
                    cardBg  = cardBg,
                    alpha   = alpha,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ---------------------------------- Skeleton card ----------------------------------

@Composable
private fun SkeletonCard(
    pulseBg: Color,
    cardBg: Color,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(cardBg)
            .padding(8.dp)
    ) {
        // ----------------------------------Image area  ----------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .alpha(alpha)
                .clip(RoundedCornerShape(4.dp))
                .background(pulseBg)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ----------------------------------Product name placeholder   ----------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(14.dp)
                .alpha(alpha)
                .clip(RoundedCornerShape(4.dp))
                .background(pulseBg)
        )

        Spacer(modifier = Modifier.height(6.dp))

        //----------------------------------Price placeholder  ----------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(14.dp)
                .alpha(alpha)
                .clip(RoundedCornerShape(4.dp))
                .background(pulseBg)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // ----------------------------------Bottom action bar placeholder ----------------------------------
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
