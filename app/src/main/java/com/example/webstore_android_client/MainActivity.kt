package com.example.webstore_android_client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.config.RetrofitClient
import com.example.webstore_android_client.appViewModel.AppViewModel
import com.example.webstore_android_client.navigation.NavigationGraph
import com.example.webstore_android_client.repositories.RepositoryProvider
import com.example.webstore_android_client.ui.theme.WebstoreAndroidClientTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.create(applicationContext)
        val appViewModel = AppViewModel(
            RepositoryProvider.customerDataRepository,
            RepositoryProvider.cartSummaryDataRepository
        )

        ApiProvider.initialize(applicationContext)

        enableEdgeToEdge()
        setContent {
            WebstoreAndroidClientTheme {
            MenuStart(appViewModel)
            }
        }


    }

}

@Composable
fun MenuStart(
    appViewModel: AppViewModel
) {

    val navigationController = rememberNavController()


    NavigationGraph(navigationController, appViewModel)
}
