package com.example.webstore_android_client.ui.userProfile.purchaseHistory

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.webstore_android_client.ui.theme.EmeraldGreen
import com.example.webstore_android_client.ui.theme.ErrorRed
import com.example.webstore_android_client.ui.theme.GoldStar
import com.example.webstore_android_client.ui.theme.GreenDark
import com.example.webstore_android_client.ui.theme.GreenLight
import com.example.webstore_android_client.ui.theme.LinkBlueDark
import com.example.webstore_android_client.ui.theme.LinkBlueLight
import com.example.webstore_android_client.ui.theme.OrangeDark
import com.example.webstore_android_client.ui.theme.OrangeLight
import com.example.webstore_android_client.ui.theme.OutOfStockRed
import com.example.webstore_android_client.ui.theme.StarAndSaleOrange


enum class PurchaseStatus(
    val key: String,
    val label: String,
) {
    PENDING("PENDING", "Изчакваща"),
    PROCESSING("PROCESSING", "Обработва се"),
    SHIPPED("SHIPPED", "Изпратена"),
    DELIVERED("DELIVERED", "Доставена"),
    CANCELLED("CANCELLED", "Отказана"),
    REFUND_REQUESTED("REFUND_REQUESTED", "Заявено връщане"),
    REFUNDED("REFUNDED", "Върната");

    // ---------------- Action availability (mirrors decideDotOptions / showDots in JSX) ------

    val canCancel: Boolean         get() = this == PROCESSING
    val canRequestRefund: Boolean  get() = this == DELIVERED
    val showDotsMenu: Boolean      get() = canCancel || canRequestRefund

    // ------------------ Chip colours ----------------------------------------------------------


    @Composable
    fun chipColors(): Pair<Color, Color> {
        val dark = isSystemInDarkTheme()
        return when (this) {

            PENDING ->
                if (dark) GoldStar.copy(alpha = 0.25f) to GoldStar
                else      StarAndSaleOrange.copy(alpha = 0.15f) to StarAndSaleOrange

            PROCESSING, SHIPPED ->
                if (dark) LinkBlueDark.copy(alpha = 0.30f) to LinkBlueLight
                else      LinkBlueLight.copy(alpha = 0.15f) to LinkBlueDark

            DELIVERED ->
                if (dark) EmeraldGreen.copy(alpha = 0.25f) to GreenLight
                else      EmeraldGreen.copy(alpha = 0.15f) to GreenDark

            CANCELLED ->
                if (dark) OutOfStockRed.copy(alpha = 0.25f) to ErrorRed
                else      ErrorRed.copy(alpha = 0.12f)      to OutOfStockRed

            REFUND_REQUESTED, REFUNDED ->
                if (dark) OrangeDark.copy(alpha = 0.35f) to OrangeLight
                else      OrangeLight.copy(alpha = 0.15f) to OrangeDark
        }
    }

    companion object {
        fun fromKey(key: String): PurchaseStatus =
            entries.find { it.key == key } ?: PENDING
    }
}
