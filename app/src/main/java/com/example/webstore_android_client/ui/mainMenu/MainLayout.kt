package com.example.webstore_android_client.ui.mainMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.webstore_android_client.ui.productBrowsing.ImageSearchViewModel
import com.example.webstore_android_client.ui.theme.AppBackgroundDark
import com.example.webstore_android_client.ui.theme.AppBackgroundLight
import androidx.compose.ui.platform.LocalContext

@Composable
fun Menu(
    navController: NavHostController,
    viewModel:MenuViewModel = viewModel(),
    imageSearchViedModel: ImageSearchViewModel,
    showMenu: Boolean,
    content: @Composable (PaddingValues) -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val appBackground = if (isDark) AppBackgroundDark else AppBackgroundLight

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    val context = LocalContext.current

    if (showMenu) {
        Scaffold(
            modifier = Modifier.fillMaxSize().background(appBackground),
            containerColor = appBackground,
            topBar = {
                TopSearchBar(
                    navController = navController,
                    onProcessImage = { viewModel.processImage(context = context,
                        uri = it,
                        navController = navController,
                        imageSearchViewModel = imageSearchViedModel) },
                    imageSearchViewModel = imageSearchViedModel
                )
            },
            bottomBar = {
                BottomNavMenu(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        ) { innerPadding ->
            content(innerPadding)
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appBackground)
        ) {
            content(PaddingValues(0.dp))
        }
    }
}