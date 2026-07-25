package com.example.webstore_android_client.ui.auth

import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.webstore_android_client.ui.theme.AppBackgroundDark
import com.example.webstore_android_client.ui.theme.AppBackgroundLight
import com.example.webstore_android_client.ui.theme.CancelGray
import com.example.webstore_android_client.ui.theme.LinkBlueDark
import com.example.webstore_android_client.ui.theme.LinkBlueLight
import com.example.webstore_android_client.ui.theme.MainBgDark
import com.example.webstore_android_client.ui.theme.MainBgLight

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val appBackground = if (isDark) AppBackgroundDark else AppBackgroundLight
    val greenBackground = if (isDark) MainBgDark else MainBgLight
    val linkBlue = if (isDark) LinkBlueLight else LinkBlueDark

    LaunchedEffect(uiState.navigateToMain) {
        if (uiState.navigateToMain) {
            navController.navigate("home") {
                popUpTo(navController.graph.id) { inclusive = true }
            }
            viewModel.onNavigatedToMain()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(navController.context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
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
            shape = RoundedCornerShape(16.dp),
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
                    text = if (uiState.isForgotPassword) "Възстановяване на парола"
                    else "Влизане",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                if (uiState.isForgotPassword) {
                    ForgotPasswordForm(
                        uiState = uiState,
                        viewModel = viewModel,
                        linkBlue = linkBlue
                    )
                } else {
                    LoginForm(
                        uiState = uiState,
                        viewModel = viewModel,
                        navController = navController,
                        linkBlue = linkBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginForm(
    uiState: LoginUiState,
    viewModel: LoginViewModel,
    navController: NavHostController,
    linkBlue: Color
) {


    AuthTextField(
        value = uiState.email,
        onValueChange = viewModel::onEmailChange,
        label = "Имейл:",
        placeholder = "Въведете имейл",
        errorMessage = uiState.emailError,
        keyboardType = KeyboardType.Email,
        enabled = !uiState.isLoading
    )

    Spacer(modifier = Modifier.height(16.dp))

    AuthTextField(
        value = uiState.password,
        onValueChange = viewModel::onPasswordChange,
        label = "Парола:",
        placeholder = "Въведете парола",
        errorMessage = uiState.passwordError,
        isPassword = true,
        enabled = !uiState.isLoading
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = uiState.rememberMe,
                onCheckedChange = viewModel::onRememberMeChange,
                enabled = !uiState.isLoading,
                colors = CheckboxDefaults.colors(
                    checkedColor = linkBlue,
                    uncheckedColor = Color.Black,
                    checkmarkColor = Color.White
                )
            )
            Text(
                text = "Запомни ме",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        TextButton(
            onClick = { viewModel.setForgotPassword(true) },
            enabled = !uiState.isLoading,
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            Text(
                text = "Забравена парола?",
                color = linkBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = viewModel::login,
        enabled = !uiState.isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color.White.copy(alpha = 0.7f),
            disabledContentColor = Color.Black.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        if (uiState.isLoading) {
            FlashingDots(color = Color.Black)
        } else {
            Text(text = "Влизане", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Нямаш акаунт? ", color = Color.Black)
        TextButton(
            onClick = { navController.navigate("register") },
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "Регистрирай се сега",
                color = linkBlue,
                fontWeight = FontWeight.Medium
            )
        }
    }

    TextButton(
        onClick = { navController.navigate("home") },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Начална страница", color = linkBlue)
    }
}

@Composable
private fun ForgotPasswordForm(
    uiState: LoginUiState,
    viewModel: LoginViewModel,
    linkBlue: Color
) {
    Text(
        text = "Въведете имейл адреса си и ние ще ви изпратим " +
                "линк за създаване на нова парола.",
        color = Color.Black,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    )

    AuthTextField(
        value = uiState.email,
        onValueChange = viewModel::onEmailChange,
        label = "Имейл:",
        placeholder = "Въведете имейл",
        errorMessage = uiState.emailError,
        keyboardType = KeyboardType.Email,
        enabled = !uiState.isLoading
    )

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { viewModel.setForgotPassword(false) },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CancelGray,
                contentColor = Color.Black,
                disabledContainerColor = CancelGray.copy(alpha = 0.7f)
            )
        ) {
            Text(text = "Отказ", fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = viewModel::submitForgotPassword,
            enabled = !uiState.isLoading,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = linkBlue,
                contentColor = Color.White,
                disabledContainerColor = linkBlue.copy(alpha = 0.7f)
            )
        ) {
            if (uiState.isLoading) {
                FlashingDots(color = Color.White)
            } else {
                Text(text = "Изпрати", fontWeight = FontWeight.Bold)
            }
        }
    }
}
