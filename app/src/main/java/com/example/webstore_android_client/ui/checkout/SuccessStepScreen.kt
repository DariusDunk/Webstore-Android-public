package com.example.webstore_android_client.ui.checkout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.webstore_android_client.ui.theme.CartPressedGreen
import com.example.webstore_android_client.ui.theme.DarkCustom
import com.example.webstore_android_client.ui.theme.EmeraldGreen
import com.example.webstore_android_client.ui.theme.MainBgLight
import com.example.webstore_android_client.ui.theme.PageBgLight
import com.example.webstore_android_client.ui.theme.RowBgDark
import com.example.webstore_android_client.ui.theme.WhiteCustom
 
@Composable
fun SuccessStepScreen(
    orderResult: OrderResult?,
    onNavigateToHome: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val cardBg         = if (isDark) RowBgDark             else PageBgLight
    val innerPanelBg   = if (isDark) Color(0xFF111827)     else Color(0xFFF3F4F6)   
    val textPrimary    = if (isDark) WhiteCustom            else DarkCustom
    val textSecondary  = if (isDark) Color(0xFFD1D5DB)     else Color(0xFF6B7280)   
    val dividerColor   = if (isDark) Color(0xFF374151)     else Color(0xFFD1D5DB)
    val orderCodeColor = if (isDark) EmeraldGreen           else MainBgLight

    // ---- Entrance scale animation  --------------------
    val scaleAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessMedium
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ------------ Checkmark circle ------------------------------------------------─
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scaleAnim.value)
                .clip(CircleShape)
                .background(
                    if (isDark) Color(0xFF14532D).copy(alpha = 0.5f)
                    else        Color(0xFFDCFCE7)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                fontSize = 42.sp,
                color = if (isDark) EmeraldGreen else CartPressedGreen
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --------------- Title ------------------------------------------------------------─
        Text(
            text = "Успешна поръчка!",
            color = textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ------------------- Subtitle messages ------------------------------------------------─
        Text(
            text = "Вашата поръчка беше приета успешно.",
            color = textSecondary,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Имейл за потвърждение беше изпратен на предоставения адрес.",
            color = textSecondary,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // --------------------- Order summary panel ----------------------------------------------─
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(innerPanelBg)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Номер на поръчка:",
                color = textSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = orderResult?.purchaseCode ?: "ГРЕШКА-В-КОДА",
                color = orderCodeColor,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = dividerColor
            )

            Text(
                text = "Общо платено:",
                color = textSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = orderResult?.totalCost
                    ?.let { "${"%.2f".format(it / 100.0)} €" }
                    ?: "0.00 €",
                color = textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // --------------- Back to home button ----------------------------------------------─
        OutlinedButton(
            onClick = onNavigateToHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = textPrimary
            ),
            border = ButtonDefaults.outlinedButtonBorder(true).copy(
                brush = androidx.compose.ui.graphics.SolidColor(dividerColor)
            )
        ) {
            Text(
                text = "Към начална страница",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        }

        // TODO: Замени с реалния път до потребителските поръчки
        // Spacer(modifier = Modifier.height(12.dp))
        // Button(
        //     onClick = onNavigateToOrders,
        //     modifier = Modifier.fillMaxWidth().height(52.dp),
        //     shape = RoundedCornerShape(8.dp),
        //     colors = ButtonDefaults.buttonColors(
        //         containerColor = if (isDark) MainBgDark else MainBgLight
        //     )
        // ) {
        //     Text("Моите поръчки", color = WhiteCustom, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        // }
    }
}
