package com.example.webstore_android_client.ui.userProfile

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.webstore_android_client.ui.theme.CardWhite
import com.example.webstore_android_client.ui.theme.DarkCustom
import com.example.webstore_android_client.ui.theme.EmeraldGreen
import com.example.webstore_android_client.ui.theme.MainBgDark
import com.example.webstore_android_client.ui.theme.MainBgLight
import com.example.webstore_android_client.ui.theme.MutedGrey
import com.example.webstore_android_client.ui.theme.QuantityButtonBgDark
import com.example.webstore_android_client.ui.theme.RowBgDark
import com.example.webstore_android_client.ui.theme.TextFieldBgDark
import com.example.webstore_android_client.ui.theme.TextFieldBgLight
import com.example.webstore_android_client.ui.theme.UnfocusedBorderColor
import com.example.webstore_android_client.ui.theme.WhiteCustom
 
private val cardBg @Composable get() =
    if (isSystemInDarkTheme()) RowBgDark else CardWhite

private val headingText @Composable get() =
    if (isSystemInDarkTheme()) WhiteCustom else DarkCustom

private val primaryGreen @Composable get() =
    if (isSystemInDarkTheme()) MainBgDark else MainBgLight

private val fieldBg @Composable get() =
    if (isSystemInDarkTheme()) TextFieldBgDark else TextFieldBgLight

private val borderUnfocused @Composable get() =
    if (isSystemInDarkTheme()) QuantityButtonBgDark else UnfocusedBorderColor

 
@Composable
fun UpdateUserInfoScreen(
    viewModel: UpdateUserInfoViewModel = viewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val previousEntry = navController.previousBackStackEntry

    val firstName =
        previousEntry
            ?.savedStateHandle
            ?.get<String>("firstName")

    val lastName =
        previousEntry
            ?.savedStateHandle
            ?.get<String>("lastName")

    val phone =
        previousEntry
            ?.savedStateHandle
            ?.get<String>("phone")

    LaunchedEffect(Unit) {
        viewModel.initUserData(firstName ?:"",lastName?:"",phone?:"")
    }


    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarMessageConsumed()
        }
    }

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        UpdateUserInfoCard(
            uiState = uiState,
            onFirstNameChange = viewModel::onFirstNameChange,
            onLastNameChange = viewModel::onLastNameChange,
            onPhoneChange = viewModel::onPhoneChange,
            onSaveClicked = viewModel::onSaveClicked,
            onRequestPasswordChange = viewModel::onRequestPasswordChange,
        )

        if (uiState.showConfirmDialog) {
            ConfirmSaveDialog(
                onConfirm = viewModel::onConfirmSave,
                onDismiss = viewModel::onConfirmDialogDismiss,
            )
        }

        SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = EmeraldGreen,
                contentColor = WhiteCustom,
            )
        }
    }
}

 
@Composable
private fun UpdateUserInfoCard(
    uiState: UpdateUserInfoUiState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSaveClicked: () -> Unit,
    onRequestPasswordChange: () -> Unit,
) {
    val green = primaryGreen

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            // --------- Section 1: profile fields ----------------------------------
            SectionHeading(text = "Промяна на потребителски данни")

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                ProfileTextField(
                    label = "Име",
                    value = uiState.firstName,
                    onValueChange = onFirstNameChange,
                    keyboardType = KeyboardType.Text,
                )
                ProfileTextField(
                    label = "Фамилия",
                    value = uiState.lastName,
                    onValueChange = onLastNameChange,
                    keyboardType = KeyboardType.Text,
                )
                ProfileTextField(
                    label = "Телефон",
                    value = uiState.phone,
                    onValueChange = onPhoneChange,
                    keyboardType = KeyboardType.Phone,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(color = green)
            } else {
                PrimaryButton(
                    text = "Запази промените",
                    onClick = onSaveClicked,
                    containerColor = green,
                )
            }

            // ------------------ Section 2: password ----------------------------------------
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeading(text = "Смяна на парола")

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = "Изпрати имейл за смяна на паролата",
                onClick = onRequestPasswordChange,
                containerColor = green,
            )
        }
    }
}

@Composable
private fun ConfirmSaveDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val green = primaryGreen

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Потвърждение за промяна", fontWeight = FontWeight.SemiBold) },
        text = { Text("Сигурни ли сте че искате да потвърдите промените?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Да", color = green, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Не", color = MutedGrey)
            }
        },
    )
}


@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = headingText,
    )
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val green = primaryGreen

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MutedGrey,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = fieldBg,
                unfocusedContainerColor = fieldBg,
                focusedBorderColor = green,
                unfocusedBorderColor = borderUnfocused,
                focusedTextColor = headingText,
                unfocusedTextColor = headingText,
                cursorColor = green,
            ),
        )
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = WhiteCustom,
        ),
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
