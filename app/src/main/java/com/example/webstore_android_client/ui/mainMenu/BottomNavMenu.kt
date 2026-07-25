package com.example.webstore_android_client.ui.mainMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.webstore_android_client.R
import com.example.webstore_android_client.repositories.RepositoryProvider
import com.example.webstore_android_client.ui.theme.MainBgDark
import com.example.webstore_android_client.ui.theme.MainBgLight
import com.example.webstore_android_client.ui.theme.OrangeDark
import com.example.webstore_android_client.ui.theme.OrangeLight

@Composable
fun BottomNavMenu(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val mainBgColor = if (isDark) MainBgDark else MainBgLight
    val orangeColor = if (isDark) OrangeDark else OrangeLight
    val unselectedColor = Color.White.copy(alpha = 0.7f)
    val cartRepository = RepositoryProvider.cartSummaryDataRepository
//    val customerRepository = RepositoryProvider.customerRepository
//    val customer: UserData? = customerRepository.userState.collectAsState().value
    val defaultPfpPainter = painterResource(id = R.drawable.default_pfp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(mainBgColor)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BottomNavItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            isSelected = currentRoute == "home",
            selectedColor = orangeColor,
            unselectedColor = unselectedColor,
            onClick = { onNavigate("home") }
        )

        BottomNavItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Categories") },
            isSelected = currentRoute == "categories",
            selectedColor = orangeColor,
            unselectedColor = unselectedColor,
            onClick = { onNavigate("categories") }
        )

        BottomNavItem(
            icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorites") },
            isSelected = currentRoute == "favorites",
            selectedColor = orangeColor,
            unselectedColor = unselectedColor,
            onClick = { onNavigate("favorites") }
        )

        BadgedBox(
            badge = {
                val cartCount = cartRepository.cart.collectAsState().value.cartQuantity
                if (cartCount > 0) {
                    Badge(containerColor = Color.Red, contentColor = Color.White) {
                        Text(if (cartCount > 999) "999+" else cartCount.toString())
                    }
                }
            }
        ) {
            BottomNavItem(
                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                isSelected = currentRoute == "cart",
                selectedColor = orangeColor,
                unselectedColor = unselectedColor,
                onClick = { onNavigate("cart") }
            )
        }

        IconButton(onClick = { onNavigate("profile") }) {
            AsyncImage(
                model = "",
                contentDescription = "Profile",
                contentScale = ContentScale.Crop,
                placeholder = defaultPfpPainter,
                error = defaultPfpPainter,
                fallback = defaultPfpPainter,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (currentRoute == "profile") orangeColor else Color.Transparent,
                        shape = CircleShape
                    ),
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: @Composable () -> Unit,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        CompositionLocalProvider(
            LocalContentColor provides if (isSelected) selectedColor else unselectedColor
        ) {
            icon()
        }
    }
}