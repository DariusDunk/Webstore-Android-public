package com.example.webstore_android_client.ui.userProfile.purchaseHistory


import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.webstore_android_client.ui.theme.DarkCustom
import com.example.webstore_android_client.ui.theme.GoldStar
import com.example.webstore_android_client.ui.theme.MutedGrey
import com.example.webstore_android_client.ui.theme.StarEmptyDark
import com.example.webstore_android_client.ui.theme.StarEmptyLight
import com.example.webstore_android_client.ui.theme.WhiteCustom

@Composable
fun StarRating(rating: Int, reviewCount: Int) {
    val dark       = isSystemInDarkTheme()
    val emptyColor = if (dark) StarEmptyDark else StarEmptyLight

    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { i ->
            Text(
                text  = "★",
                fontSize = 16.sp,
                color = if (i < rating) GoldStar else emptyColor,
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text     = "$reviewCount ревюта",
            fontSize = 12.sp,
            color    = MutedGrey,
        )
    }
}

@Composable
fun StatusChip(status: PurchaseStatus) {
    val (bg, fg) = status.chipColors()

    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(fg),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text       = status.label,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Medium,
                color      = fg,
            )
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
) {
    if (value.isNullOrBlank()) return
    val dark      = isSystemInDarkTheme()
    val valueText = if (dark) WhiteCustom.copy(alpha = 0.85f) else DarkCustom.copy(alpha = 0.85f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text     = label,
            fontSize = 12.sp,
            color    = MutedGrey,
            modifier = Modifier.width(160.dp),
        )
        Text(
            text     = value,
            fontSize = 13.sp,
            color    = valueText,
        )
    }
}
