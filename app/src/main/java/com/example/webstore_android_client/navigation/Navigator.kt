package com.example.webstore_android_client.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.webstore_android_client.appViewModel.AppViewModel
import com.example.webstore_android_client.events.SessionEvents
import com.example.webstore_android_client.events.eventTypes.SessionEvent
import com.example.webstore_android_client.ui.auth.LoginScreen
import com.example.webstore_android_client.ui.auth.RegisterScreen
import com.example.webstore_android_client.ui.cart.CartScreen
import com.example.webstore_android_client.ui.categories.CategoryScreen
import com.example.webstore_android_client.ui.checkout.CheckoutScreen
import com.example.webstore_android_client.ui.detailedProduct.ProductDetailsScreen
import com.example.webstore_android_client.ui.favourites.FavouritesScreen
import com.example.webstore_android_client.ui.homePage.HomePage
import com.example.webstore_android_client.ui.mainMenu.Menu
import com.example.webstore_android_client.ui.productBrowsing.ImageSearchViewModel
import com.example.webstore_android_client.ui.productBrowsing.ProductsScreen
import com.example.webstore_android_client.ui.userProfile.UpdateUserInfoScreen
import com.example.webstore_android_client.ui.userProfile.UserProfileScreen
import com.example.webstore_android_client.ui.userProfile.purchaseHistory.PurchaseDetailScreen
import com.example.webstore_android_client.ui.userProfile.purchaseHistory.PurchaseHistoryScreen
import kotlinx.coroutines.delay


data class NavigationPaths(
    val login: String = "login",
    val categories: String = "categories",
    val register: String = "register",
    val details: String = "details",
    val checkout: String = "checkout",
    val home: String = "home",
    val cart: String = "cart",
    val favorites: String = "favorites",
    val profile: String = "profile",
    val products: String = "products"
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationGraph(
    navController: NavHostController,
    appViewModel: AppViewModel,
    imageSearchViedModel: ImageSearchViewModel = viewModel(),
) {

    var showSessionExpiredDialog by remember { mutableStateOf(false) }
//    val customerData by appViewModel.user.collectAsState()
    val isGuest by appViewModel.isGuestFlow.collectAsState(initial = true)
    val navigationPaths = NavigationPaths()


    LaunchedEffect(Unit) {
        SessionEvents.events.collect { event ->
            when (event) {

                SessionEvent.Expired -> {

                    showSessionExpiredDialog = true
                }

                SessionEvent.Logout -> {

                    navController.navigate("home") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }

                    delay(1000)

                    appViewModel.clearUserData()
                }
            }
        }
    }


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val noMenuRoutes = listOf(navigationPaths.login, navigationPaths.register, navigationPaths.checkout)
    val showMenu = currentRoute == null || currentRoute !in noMenuRoutes

    Menu(
        navController = navController,
        showMenu = showMenu,
        imageSearchViedModel = imageSearchViedModel
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") { LoginScreen(navController) }
            composable("categories") { CategoryScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("details/{productCode}") { backStackEntry ->
                ProductDetailsScreen(
                    productCode = backStackEntry.arguments?.getString("productCode") ?: "",
                    navController = navController
                )
            }
            composable("checkout") {  CheckoutScreen(navController = navController)  }

            composable("home") { HomePage(navController) }
            composable("cart") { CartScreen(navController) }
            composable("favorites") { FavouritesScreen(navController) }
            composable("profile") {

                if (isGuest)
                    LoginScreen(navController)
                else {
                    UserProfileScreen(navController)
                }

            }

            composable("profile/update")
            {
                if (isGuest)
                    LoginScreen(navController)
                else {
                    UpdateUserInfoScreen(navController = navController)
                }
            }

            composable("purchase_history")
            {
                if (isGuest)
                    LoginScreen(navController)
                else {
                    PurchaseHistoryScreen(navController = navController)
                }
            }

            composable("detailed_purchase/{purchaseCode}")
            {
                if (isGuest)
                    LoginScreen(navController)
                else {
                    PurchaseDetailScreen(navController = navController,
                        purchaseCode = it.arguments?.getString("purchaseCode") ?: "")
                }
            }


            composable(
                route = "products/{mode}/{details}",
                arguments = listOf(
                    navArgument("mode") { type = NavType.StringType },
                    navArgument("details") { type = NavType.StringType }
                )
            ) { ProductsScreen(navController, imageSearchViewModel =imageSearchViedModel) }

            composable(
                route = "products/{mode}",
                arguments = listOf(navArgument("mode") { type = NavType.StringType })
            ) { ProductsScreen(navController, imageSearchViewModel = imageSearchViedModel) }

        }
    }


    if (showSessionExpiredDialog) {
        AlertDialog(
            title = {
                Text("Невалидна сесия")
            },
            text = {
                Text("Сесията ви е изтекла или прекратена")
            },
            confirmButton = {
                Button(
                    onClick = {

                        showSessionExpiredDialog = false

                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                        appViewModel.clearUserData()

                    }
                ) {
                    Text("OK")
                }
            },
            onDismissRequest = {}
        )
    }


}
