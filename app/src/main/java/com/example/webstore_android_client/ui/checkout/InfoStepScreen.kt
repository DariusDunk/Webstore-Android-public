package com.example.webstore_android_client.ui.checkout

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.webstore_android_client.ui.theme.CartPressedGreen
import com.example.webstore_android_client.ui.theme.DarkCustom
import com.example.webstore_android_client.ui.theme.EmeraldGreen
import com.example.webstore_android_client.ui.theme.ErrorRed
import com.example.webstore_android_client.ui.theme.GreenLight
import com.example.webstore_android_client.ui.theme.MainBgDark
import com.example.webstore_android_client.ui.theme.MainBgLight
import com.example.webstore_android_client.ui.theme.PageBgLight
import com.example.webstore_android_client.ui.theme.CardBgDark
import com.example.webstore_android_client.ui.theme.WhiteCustom


@Composable
fun InfoStepScreen(
    state: CheckoutState,
    isGuest: Boolean,
    onFormDataChange: ((FormData) -> FormData) -> Unit,
    onContinue: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = if (isDark) EmeraldGreen else MainBgLight)
        }
        return
    }

    val form = state.formData

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ----------------- Delivery Info Card ------------------------------------------------
        CheckoutCard(isDark = isDark) {
            SectionHeader(text = "Данни за доставка", isDark = isDark)

            Spacer(modifier = Modifier.height(12.dp))

            CheckoutTextField(
                label = "Име и фамилия",
                value = form.contactName,
                onValueChange = { v -> onFormDataChange { it.copy(contactName = v) } },
                keyboardType = KeyboardType.Text,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            CheckoutTextField(
                label = "Телефон",
                value = form.contactNumber,
                onValueChange = { v -> onFormDataChange { it.copy(contactNumber = v) } },
                keyboardType = KeyboardType.Phone,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            CheckoutTextField(
                label = "Адрес",
                value = form.address,
                onValueChange = { v -> onFormDataChange { it.copy(address = v) } },
                keyboardType = KeyboardType.Text,
                singleLine = false,
                minLines = 3,
                isDark = isDark
            )

            if (isGuest) {
                Spacer(modifier = Modifier.height(12.dp))
                CheckoutTextField(
                    label = "Имейл",
                    value = form.email,
                    onValueChange = { v -> onFormDataChange { it.copy(email = v) } },
                    keyboardType = KeyboardType.Email,
                    isDark = isDark,
                    isError = state.emailError != null,
                    errorMessage = state.emailError
                )
            }
        }

        // -------------------- Payment Method Card ----------------------------------------------─
        CheckoutCard(isDark = isDark) {
            SectionHeader(text = "Начин на плащане", isDark = isDark)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = form.paymentMethod == PaymentMethod.CASH_ON_DELIVERY,
                    onClick = { onFormDataChange { it.copy(paymentMethod = PaymentMethod.CASH_ON_DELIVERY) } },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = if (isDark) EmeraldGreen else MainBgLight
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = PaymentMethod.CASH_ON_DELIVERY.displayName,
                    color = if (isDark) Color(0xFFE5E7EB) else DarkCustom,
                    fontSize = 14.sp
                )
            }
        }

        // ----------------- Mini Order Summary Card ------------------------------------------─
        CheckoutCard(isDark = isDark) {
            val textSecondary = if (isDark) Color(0xFFE5E7EB) else DarkCustom

            Text(
                text = "Обобщение",
                color = if (isDark) WhiteCustom else DarkCustom,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (state.cartSummary == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = if (isDark) EmeraldGreen else MainBgLight)
                }
            } else {
                val summary = state.cartSummary

                SummaryRow(
                    label = "Продукти (${summary.items.size}):",
                    value = "${"%.2f".format(summary.totals.subtotalStotinki / 100.0)} €",
                    labelColor = textSecondary,
                    valueColor = textSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Доставка:", color = textSecondary)
                    if (summary.totals.shippingStotinki == 0L) {
                        Text(
                            "БЕЗПЛАТНО!",
                            color = if (isDark) GreenLight else CartPressedGreen,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            "${"%.2f".format(summary.totals.shippingStotinki / 100.0)} €",
                            color = textSecondary
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
                )

                SummaryRow(
                    label = "Общо:",
                    value = "${"%.2f".format(summary.totals.totalStotinki / 100.0)} €",
                    labelColor = if (isDark) WhiteCustom else DarkCustom,
                    valueColor = if (isDark) WhiteCustom else DarkCustom,
                    labelWeight = FontWeight.Bold,
                    valueSize = 18.sp
                )
            }
        }

        // --------------------- Continue Button --------------------------------------------------─
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) MainBgDark else MainBgLight
            )
        ) {
            Text(
                text = "Продължи",
                color = WhiteCustom,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

 
@Composable
internal fun CheckoutCard(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) CardBgDark else PageBgLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
internal fun SectionHeader(text: String, isDark: Boolean) {
    Text(
        text = text,
        color = if (isDark) WhiteCustom else DarkCustom,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
    HorizontalDivider(
        modifier = Modifier.padding(top = 8.dp),
        color = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
    )
}

@Composable
internal fun CheckoutTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isDark: Boolean,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val labelColor  = if (isDark) Color(0xFFD1D5DB) else Color(0xFF374151)
    val focusedBorderColor   = if (isDark) EmeraldGreen else MainBgLight
    val unfocusedBorderColor = if (isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB)
    val containerColor       = if (isDark) Color(0xFF111827) else WhiteCustom
    val textColor            = if (isDark) WhiteCustom else DarkCustom

    Column {
        Text(label, color = labelColor, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            minLines = minLines,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor      = textColor,
                unfocusedTextColor    = textColor,
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
                errorContainerColor   = containerColor,
                focusedBorderColor    = focusedBorderColor,
                unfocusedBorderColor  = unfocusedBorderColor,
                errorBorderColor      = ErrorRed,
                cursorColor           = focusedBorderColor
            )
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = ErrorRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
internal fun SummaryRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    labelWeight: FontWeight = FontWeight.Normal,
    valueSize: androidx.compose.ui.unit.TextUnit = 16.sp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = labelColor, fontWeight = labelWeight)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = valueSize)
    }
}
