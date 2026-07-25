package com.example.webstore_android_client.ui.homePage


import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.webstore_android_client.ui.theme.MutedGrey
import com.example.webstore_android_client.ui.theme.PageBgDark
import com.example.webstore_android_client.ui.theme.PageBgLight


@Composable
fun HomePage(
    navController: NavHostController,
    viewModel: HomePageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark  = isSystemInDarkTheme()
    val pageBg  = if (isDark) PageBgDark else PageBgLight

    //----------------------------------Loading----------------------------------
    if (uiState.isLoading) {
        HomePageSkeleton()
        return
    }

    // ----------------------------------Error state ----------------------------------
    if (uiState.error != null && uiState.rows.isEmpty()) {
        Box(
            modifier          = Modifier
                .fillMaxSize()
                .background(pageBg),
            contentAlignment  = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally)
            {
                Text(
                    text = "Няма намерени продукти.",
                    fontSize = 18.sp,
                    color = MutedGrey,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.fetchHomePage() }) {
                    Text(text = "Опитай отново")
                }
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        if (uiState.rows.isEmpty()) {
            // ---------------------------------- Empty state ----------------------------------
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally)
                {
                    Text(
                        text = "Няма намерени продукти.",
                        fontSize = 18.sp,
                        color = MutedGrey,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.fetchHomePage() }) {
                        Text(text = "Опитай отново")
                    }
                }
            }
        } else {
            //----------------------------------Content----------------------------------
            LazyColumn(
                modifier             = Modifier.fillMaxSize(),
                contentPadding       = PaddingValues(vertical = 32.dp),
                verticalArrangement  = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(
                    items = uiState.rows,
                    key   = { index, item -> "${item.type}-$index" }
                ) { _, rowItem ->
                    ProductRow(
                        title          = rowItem.title,
                        products       = rowItem.products,
                        onProductClick = { productCode ->
                            navController.navigate("details/$productCode")
                        }
                    )
                }
            }
        }
    }
}
