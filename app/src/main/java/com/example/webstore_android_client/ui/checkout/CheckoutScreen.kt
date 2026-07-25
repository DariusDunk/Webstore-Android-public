package com.example.webstore_android_client.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.webstore_android_client.model.localDTOs.purchase.SelectedCheckoutItem
import com.example.webstore_android_client.ui.theme.AppBackgroundDark
import com.example.webstore_android_client.ui.theme.AppBackgroundLight
import com.example.webstore_android_client.ui.theme.CartPressedGreen
import com.example.webstore_android_client.ui.theme.DarkCustom
import com.example.webstore_android_client.ui.theme.QuantityButtonBgLight
import com.example.webstore_android_client.ui.theme.RowBgDark
import com.example.webstore_android_client.ui.theme.WhiteCustom
 
@Composable
fun CheckoutScreen(
    navController: NavHostController,
    viewModel: CheckoutViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ----------- Dialog State Holders --------------------------------------------------
    var errorEvent by remember { mutableStateOf<CheckoutEvent.ShowError?>(null) }
    var warningEvent by remember { mutableStateOf<CheckoutEvent.ShowWarning?>(null) }
    var saveRecipientEvent by remember { mutableStateOf<CheckoutEvent.AskToSaveRecipientData?>(null) }
    val previousEntry = navController.previousBackStackEntry

    val selectedItems =
        previousEntry
            ?.savedStateHandle
            ?.get<List<SelectedCheckoutItem>>("selectedItems")
            ?: emptyList()

    val isDirectPurchase =
        previousEntry
            ?.savedStateHandle
            ?.get<Boolean>("isDirectPurchase")
            ?: false

    LaunchedEffect(selectedItems, isDirectPurchase) {
        viewModel.initialize(selectedItems, isDirectPurchase)
    }

    // ------------- Collect one-time events ----------------------------------------------─
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CheckoutEvent.NavigateToCart -> navController.navigate("cart")
                is CheckoutEvent.ShowError -> errorEvent = event
                is CheckoutEvent.ShowWarning -> warningEvent = event
                is CheckoutEvent.AskToSaveRecipientData -> saveRecipientEvent = event
            }
        }
    }

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) AppBackgroundDark else AppBackgroundLight
    val textColor = if (isDark) Color.White else Color.Black

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.step != CheckoutStep.SUCCESS) {
                CheckoutProgressBar(currentStep = uiState.step)
                Spacer(modifier = Modifier.height(16.dp))
            }

            when (uiState.step) {
                CheckoutStep.INFO ->
                    InfoStepScreen(
                        state = uiState,
                        isGuest = viewModel.isGuestUser,
                        onFormDataChange = viewModel::onFormDataChange,
                        onContinue = viewModel::onContinueFromInfo
                    )

                CheckoutStep.SUMMARY ->
                    SummaryStepScreen(
                        state = uiState,
                        onBack = viewModel::onBackFromSummary,
                        onConfirm = viewModel::onConfirmOrder
                    )

                CheckoutStep.SUCCESS ->
                    SuccessStepScreen(
                        orderResult = uiState.orderResult,
                        onNavigateToHome = {navController.navigate("home")}
                    )
            }
        }
    }

    // -------------------- Native Compose Dialogs ------------------------------------------------

    errorEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { errorEvent = null },
            title = { Text(text = event.title, color = textColor, fontWeight = FontWeight.Bold) },
            text = { Text(text = event.message, color = textColor) },
            containerColor = if (isDark) RowBgDark else WhiteCustom,
            confirmButton = {
                TextButton(onClick = { errorEvent = null }) {
                    Text("ОК", color = if (isDark) CartPressedGreen else DarkCustom)
                }
            }
        )
    }

    warningEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { warningEvent = null },
            title = { Text(text = event.title, color = textColor, fontWeight = FontWeight.Bold) },
            text = { Text(text = event.message, color = textColor) },
            containerColor = if (isDark) RowBgDark else WhiteCustom,
            confirmButton = {
                TextButton(onClick = { warningEvent = null }) {
                    Text("Разбрах", color = if (isDark) CartPressedGreen else DarkCustom)
                }
            }
        )
    }

    saveRecipientEvent?.let { event ->
        AlertDialog(
            onDismissRequest = {
                event.onSkip()
                saveRecipientEvent = null
            },
            title = { Text(text = "Запазване на нови данни", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Забелязахме, че въведохте нови данни за доставка. Искате ли да ги запазим за бъдещи поръчки?",
                    color = textColor
                )
            },
            containerColor = if (isDark) RowBgDark else WhiteCustom,
            confirmButton = {
                TextButton(
                    onClick = {
                        event.onSave()
                        saveRecipientEvent = null
                    }
                ) {
                    Text("Да", color = if (isDark) CartPressedGreen else DarkCustom)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        event.onSkip()
                        saveRecipientEvent = null
                    }
                ) {
                    Text("Не", color = if (isDark) Color.LightGray else Color.Gray)
                }
            }
        )
    }
}

@Composable
private fun CheckoutProgressBar(currentStep: CheckoutStep) {
    val isDark = isSystemInDarkTheme()

    val barBg      = if (isDark) RowBgDark           else QuantityButtonBgLight
    val activeColor = if (isDark) Color(0xFF4ADE80)   else CartPressedGreen
    val inactiveColor = if (isDark) Color(0xFF6B7280) else Color(0xFF9CA3AF)
    val textColor  = if (isDark) WhiteCustom          else DarkCustom

    val step1Active  = currentStep >= CheckoutStep.INFO
    val step1Done    = currentStep >= CheckoutStep.SUMMARY
    val step2Active  = currentStep >= CheckoutStep.SUMMARY
    val connectorActive = step1Done

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(barBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            ProgressStep(
                label = "Информация",
                indicator = if (step1Done) "✓" else "●",
                isActive = step1Active,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                textColor = textColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(2.dp)
                    .background(if (connectorActive) activeColor else inactiveColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            ProgressStep(
                label = "Преглед",
                indicator = "●",
                isActive = step2Active,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                textColor = textColor
            )
        }
    }
}

@Composable
private fun ProgressStep(
    label: String,
    indicator: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    textColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = indicator,
            fontSize = 20.sp,
            color = if (isActive) activeColor else inactiveColor
        )
    }
}