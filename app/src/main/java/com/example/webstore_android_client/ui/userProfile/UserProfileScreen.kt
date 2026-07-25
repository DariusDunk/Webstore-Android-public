package com.example.webstore_android_client.ui.userProfile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.webstore_android_client.R
import com.example.webstore_android_client.model.responses.customer.CustomerProfileResponse
import com.example.webstore_android_client.tools.formatDate
import com.example.webstore_android_client.ui.theme.ErrorRed
import com.example.webstore_android_client.ui.theme.GreenDark
import com.example.webstore_android_client.ui.theme.GreenLight
import com.example.webstore_android_client.ui.theme.MainBgDark
import com.example.webstore_android_client.ui.theme.MainBgLight
import com.example.webstore_android_client.ui.theme.MutedGrey
import com.example.webstore_android_client.ui.theme.PageBgDark
import com.example.webstore_android_client.ui.theme.PageBgLight
import com.example.webstore_android_client.ui.theme.RowBgDark
import com.example.webstore_android_client.ui.theme.RowBgLight

@Composable
fun UserProfileScreen(
    navController: NavHostController,
    viewModel: UserProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()

    val pageBg = if (isDark) PageBgDark else PageBgLight
    val mainBgColor = if (isDark) MainBgDark else MainBgLight

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = mainBgColor)
        }
        return
    }

//    if (uiState.profileData != null) {
//        println("---------------------------------- PROFILE DATA ----------------------------------\n" +
//                "${uiState.profileData}")
//    }
//
//    if (uiState.profileData == null) {
//        println("---------------------------------- PROFILE DATA IS NULL ----------------------------------")
//
//    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeader(profile = uiState.profileData, bgColor = mainBgColor)

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            ProfileStatsRow(profile = uiState.profileData)
            Spacer(modifier = Modifier.height(16.dp))
            PersonalInfoCard(profile = uiState.profileData)
        }

        Spacer(modifier = Modifier.height(24.dp))

        NavigationMenuCard(
            onNavigateToHistory = {

                navController.navigate("purchase_history")

            },
            onNavigateToUpdate = {
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("firstName", uiState.profileData?.firstName)

                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("lastName", uiState.profileData?.familyName)

                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("phone", uiState.profileData?.phoneNumber)

                navController.navigate("profile/update")
            },
            onLogout = { viewModel.logout() }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileHeader(profile: CustomerProfileResponse?, bgColor: Color) {
    val defaultPfpPainter = painterResource(R.drawable.default_pfp)
//    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
    val memberSince = profile?.registerDate?.let { formatDate(it, "yyyy-MM-dd") } ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
//            model = profile?.userPfp,
            model = "",
            contentDescription = "Profile Picture",
            contentScale = ContentScale.Crop,
            placeholder = defaultPfpPainter,
            error = defaultPfpPainter,
            fallback = defaultPfpPainter,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${profile?.firstName ?: ""} ${profile?.familyName ?: ""}".trim(),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )

        if (memberSince.isNotEmpty()) {
            Text(
                text = "Член от $memberSince",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ProfileStatsRow(profile: CustomerProfileResponse?) {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) RowBgDark else RowBgLight
    val valueColor = if (isDark) GreenDark else GreenLight

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "Поръчки",
            value = profile?.purchasesCount ?: 0,
            bgColor = cardBg,
            valColor = valueColor,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Ревюта",
            value = profile?.reviewsCount ?: 0,
            bgColor = cardBg,
            valColor = valueColor,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Любими",
            value = profile?.favouritesCount ?: 0,
            bgColor = cardBg,
            valColor = valueColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: Int,
    bgColor: Color,
    valColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = valColor
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MutedGrey,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun PersonalInfoCard(profile: CustomerProfileResponse?) {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) RowBgDark else RowBgLight
    val textColor = if (isDark) MutedGrey else Color.Black

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardBg, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "ЛИЧНИ ДАННИ",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MutedGrey,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        InfoRow(label = "Имейл", value = profile?.email ?: "", textColor = textColor)
        HorizontalDivider(
            color = MutedGrey.copy(alpha = 0.2f),
            modifier = Modifier.padding(vertical = 12.dp)
        )
        InfoRow(label = "Телефон", value = profile?.phoneNumber ?: "", textColor = textColor)
    }
}

@Composable
private fun InfoRow(label: String, value: String, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MutedGrey, fontSize = 14.sp)
        Text(text = value, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun NavigationMenuCard(
    onNavigateToHistory: () -> Unit,
    onNavigateToUpdate: () -> Unit,
    onLogout: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) RowBgDark else RowBgLight
    val textColor = if (isDark) MutedGrey else Color.Black

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(cardBg, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        MenuButton(
            text = "История на покупките",
            textColor = textColor,
            onClick = onNavigateToHistory
        )
        HorizontalDivider(color = MutedGrey.copy(alpha = 0.2f))

        MenuButton(
            text = "Промяна на потребителски данни",
            textColor = textColor,
            onClick = onNavigateToUpdate
        )

//        Divider(color = MutedGrey.copy(alpha = 0.2f))
        HorizontalDivider(color = MutedGrey.copy(alpha = 0.2f))
        MenuButton(text = "Изход", textColor = ErrorRed, onClick = onLogout)
    }
}

@Composable
private fun MenuButton(text: String, textColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}