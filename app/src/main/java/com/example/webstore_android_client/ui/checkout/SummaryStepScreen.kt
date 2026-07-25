package com.example.webstore_android_client.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.webstore_android_client.ui.productBasic.ProductImage
import com.example.webstore_android_client.ui.theme.CancelGray
import com.example.webstore_android_client.ui.theme.CartPressedGreen
import com.example.webstore_android_client.ui.theme.DarkCustom
import com.example.webstore_android_client.ui.theme.GreenLight
import com.example.webstore_android_client.ui.theme.ImageBgGrey
import com.example.webstore_android_client.ui.theme.OrangeDark
import com.example.webstore_android_client.ui.theme.OrangeLight
import com.example.webstore_android_client.ui.theme.WhiteCustom
 
@Composable
fun SummaryStepScreen(
    state: CheckoutState,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    if (state.isMutating) {
        PendingPurchaseOverlay(message = "Финализиране на покупката...")
    }

    val form    = state.formData
    val summary = state.cartSummary ?: return    

    val textPrimary   = if (isDark) WhiteCustom          else DarkCustom
    val textSecondary = if (isDark) Color(0xFFD1D5DB)    else Color(0xFF6B7280)  
    val dividerColor  = if (isDark) Color(0xFF374151)    else Color(0xFFD1D5DB)  
 
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {

        // -------------- Header ------------------------------------------------------------
        item {
            CheckoutCard(isDark = isDark) {
                Text(
                    text = "Преглед на поръчката",
                    color = textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        // ---------------- Delivery details --------------------------------------------------
        item {
            CheckoutCard(isDark = isDark) {
                Text(
                    text = "Данни за доставка:",
                    color = textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(form.contactName, color = textSecondary, fontSize = 14.sp)
                Text(form.contactNumber, color = textSecondary, fontSize = 14.sp)
                Text(form.address, color = textSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Плащане: ${form.paymentMethod.displayName}",
                    color = textSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // ----------------- Product list ------------------------------------------------------
        item {
            Text(
                text = "Продукти",
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        items(items = summary.items, key = { it.compactProductResponse.productCode }) { item ->
            val product = item.compactProductResponse
            val lineTotal = (product.salePriceStotinki * item.quantity) / 100.0

            CheckoutCard(isDark = isDark) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    ProductImage(productCode = product.productCode,
                        imageUrl = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isDark) Color(0xFF111827) else ImageBgGrey
                            ))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            color = textPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "x${item.quantity}",
                            color = textSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = "${"%.2f".format(lineTotal)} €",
                        color = textPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // ------------------- Totals ------------------------------------------------------------
        item {
            CheckoutCard(isDark = isDark) {
                SummaryRow(
                    label = "Междинна сума:",
                    value = "${"%.2f".format(summary.totals.subtotalStotinki / 100.0)} €",
                    labelColor = textPrimary,
                    valueColor = textPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Доставка:", color = textPrimary)
                    if (summary.totals.shippingStotinki == 0L) {
                        Text(
                            "БЕЗПЛАТНО!",
                            color = if (isDark) GreenLight else CartPressedGreen,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            "${"%.2f".format(summary.totals.shippingStotinki / 100.0)} €",
                            color = textPrimary
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = dividerColor
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ОБЩО:",
                        color = textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "${"%.2f".format(summary.totals.totalStotinki / 100.0)} €",
                        color = if (isDark) OrangeDark else OrangeLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }

        // ---------------------- Navigation buttons ------------------------------------------------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBack,
                    enabled = !state.isMutating,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor      = textPrimary,
                        disabledContentColor = textPrimary.copy(alpha = 0.4f)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(true).copy(
                        brush = SolidColor(
                            if (isDark) Color(0xFF4B5563) else CancelGray
                        )
                    )
                ) {
                    Text(
                        text = "Назад",
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }

                Button(
                    onClick = onConfirm,
                    enabled = !state.isMutating,
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = CartPressedGreen,
                        disabledContainerColor = CartPressedGreen.copy(alpha = 0.5f),
                        contentColor           = WhiteCustom,
                        disabledContentColor   = WhiteCustom.copy(alpha = 0.7f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 1.dp
                    )
                ) {
                    Text(
                        text = "Потвърди поръчка",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
