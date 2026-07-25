package com.example.webstore_android_client.ui.checkout

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.webstore_android_client.ui.theme.*
 
@Composable
fun PendingPurchaseOverlay(
    message: String = "Обработка на поръчката..."
) {
    val isDark = isSystemInDarkTheme()

    val scrimColor = if (isDark)
        Color(0xFF000000).copy(alpha = 0.60f)
    else
        Color(0xFF4B5563).copy(alpha = 0.50f)

    val cardBg     = if (isDark) RowBgDark else PageBgLight          
    val textColor  = if (isDark) Color(0xFFE5E7EB) else Color(0xFF374151)

    val spinnerColor = if (isDark) EmeraldGreen else MainBgLight
    val pulseColor   = Color(0xFF22C55E).copy(alpha = 0.20f)         

    // ----------- Animations ------------------------------------------------------------

    val pingAnim = rememberInfiniteTransition(label = "ping")
    val pingScale by pingAnim.animateFloat(
        initialValue = 1f, targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "pingScale"
    )
    val pingAlpha by pingAnim.animateFloat(
        initialValue = 0.6f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "pingAlpha"
    )

    val spinAnim = rememberInfiniteTransition(label = "spin")
    val rotation by spinAnim.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing)
        ),
        label = "rotation"
    )

    val textAnim = rememberInfiniteTransition(label = "textPulse")
    val textAlpha by textAnim.animateFloat(
        initialValue = 1f, targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textAlpha"
    )


    Dialog(
        onDismissRequest = {   },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor),
            contentAlignment = Alignment.Center
        ) {
            // -------------------- Central card ------------------------------------------------─
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(cardBg, RoundedCornerShape(16.dp))
                    .then(
                        Modifier.background(
                            Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .fillMaxWidth()
                        .background(cardBg, RoundedCornerShape(16.dp))
                        .padding(1.dp)
                        .background(cardBg, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ------------------- Spinner + pulse ring ------------------------------
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .scale(pingScale)
                                    .background(
                                        pulseColor.copy(alpha = pingAlpha),
                                        CircleShape
                                    )
                            )

                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = spinnerColor,
                                strokeWidth = 3.5.dp,
                                trackColor = spinnerColor.copy(alpha = 0.15f)
                            )
                        }

                        // --------------------- Status message ------------------------------------
                        Text(
                            text = message,
                            color = textColor.copy(alpha = textAlpha),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
