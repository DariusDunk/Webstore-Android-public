package com.example.webstore_android_client.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.webstore_android_client.ui.theme.AppBackgroundDark
import com.example.webstore_android_client.ui.theme.AppBackgroundLight
import com.example.webstore_android_client.ui.theme.LinkBlueDark
import com.example.webstore_android_client.ui.theme.LinkBlueLight
import com.example.webstore_android_client.ui.theme.MainBgDark
import com.example.webstore_android_client.ui.theme.MainBgLight


@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val appBackground = if (isDark) AppBackgroundDark else AppBackgroundLight
    val greenBackground = if (isDark) MainBgDark else MainBgLight

    if (uiState.showSuccessDialog) {
        AlertDialog(
            title = {
                Text("Успешнаш регистрация")
            },
            text = {
                Text("Регистрацията ви е успешна!")
            },
            confirmButton = {
                Button(
                    onClick = {

                        uiState.showSuccessDialog = false

                        navController.navigate("login") {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            onDismissRequest = {
                uiState.showSuccessDialog = false

                navController.navigate("login") {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appBackground)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape  = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = greenBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text       = "Регистрация",
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.Black,
                    modifier   = Modifier.padding(bottom = 24.dp)
                )

                RegisterForm(uiState = uiState,
                    viewModel = viewModel,
                    navController = navController,
                    isDark = isDark)
            }
        }
    }
}


@Composable
private fun RegisterForm(
    uiState: RegisterUiState,
    viewModel: RegisterViewModel,
    navController: NavHostController,
    isDark: Boolean
) {
    val linkBlue = if (isDark) LinkBlueLight else  LinkBlueDark

    AuthTextField(
        value        = uiState.name,
        onValueChange = viewModel::onNameChange,
        label        = "Иmе:",
        placeholder  = "Въведете иmе",
        errorMessage = uiState.nameError,
        enabled      = !uiState.isLoading
    )

    Spacer(modifier = Modifier.height(16.dp))

    AuthTextField(
        value        = uiState.familyName,
        onValueChange = viewModel::onFamilyNameChange,
        label        = "Фамилия:",
        placeholder  = "Въведете фамилно иmе",
        errorMessage = uiState.familyNameError,
        enabled      = !uiState.isLoading
    )

    Spacer(modifier = Modifier.height(16.dp))

    AuthTextField(
        value        = uiState.email,
        onValueChange = viewModel::onEmailChange,
        label        = "Иmейл:",
        placeholder  = "example@domain.com",
        errorMessage = uiState.emailError,
        keyboardType = KeyboardType.Email,
        enabled      = !uiState.isLoading
    )

    Spacer(modifier = Modifier.height(16.dp))

    AuthTextField(
        value        = uiState.password,
        onValueChange = viewModel::onPasswordChange,
        label        = "Парола:",
        placeholder  = "мин. 12 символа",
        errorMessage = uiState.passwordError,
        isPassword   = true,
        enabled      = !uiState.isLoading
    )

    Spacer(modifier = Modifier.height(16.dp))

    AuthTextField(
        value        = uiState.repeatedPassword,
        onValueChange = viewModel::onRepeatedPasswordChange,
        label        = "Потвърдете паролата:",
        placeholder  = "Потвърдете паролата",
        errorMessage = uiState.repeatedPasswordError,
        isPassword   = true,
        enabled      = !uiState.isLoading
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick  = viewModel::register,
        enabled  = !uiState.isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape  = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor         = Color.White,
            contentColor           = Color.Black,
            disabledContainerColor = Color.White.copy(alpha = 0.7f),
            disabledContentColor   = Color.Black.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        if (uiState.isLoading) {
            FlashingDots(color = Color.Black)
        } else {
            Text(
                text       = "Регистрирай се",
                fontWeight = FontWeight.Bold,
                fontSize   = 16.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(text = "Вече имаш акаунт? ", color = Color.Black)
        TextButton(
            onClick        = { navController.navigate("login") },
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text       = "Влез тук",
                color      = linkBlue,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
