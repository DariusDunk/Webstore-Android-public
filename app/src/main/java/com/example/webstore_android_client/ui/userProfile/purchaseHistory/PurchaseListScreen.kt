package com.example.webstore_android_client.ui.userProfile.purchaseHistory

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.distinctUntilChanged
import com.example.webstore_android_client.model.responses.purchase.CompactPurchaseResponse
import com.example.webstore_android_client.tools.formatDate
import com.example.webstore_android_client.ui.productBasic.ProductImage
import com.example.webstore_android_client.ui.theme.CardWhite
import com.example.webstore_android_client.ui.theme.DarkCustom
import com.example.webstore_android_client.ui.theme.MainBgDark
import com.example.webstore_android_client.ui.theme.MainBgLight
import com.example.webstore_android_client.ui.theme.MutedGrey
import com.example.webstore_android_client.ui.theme.QuantityButtonBgDark
import com.example.webstore_android_client.ui.theme.RowBgDark
import com.example.webstore_android_client.ui.theme.StarEmptyLight
import com.example.webstore_android_client.ui.theme.WhiteCustom

private val cardBg        @Composable get() = if (isSystemInDarkTheme()) RowBgDark     else CardWhite
private val headingText   @Composable get() = if (isSystemInDarkTheme()) WhiteCustom   else DarkCustom
private val borderNormal  @Composable get() = if (isSystemInDarkTheme()) QuantityButtonBgDark else StarEmptyLight
private val primaryGreen  @Composable get() = if (isSystemInDarkTheme()) MainBgDark    else MainBgLight


@Composable
fun PurchaseHistoryScreen(
    navController: NavHostController,
    viewModel: PurchaseListViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            if (totalItems > 0 && lastVisible >= totalItems - 2) {
                totalItems
            } else null
        }
            .distinctUntilChanged()
            .collect { trigger ->
                if (trigger != null) {
                    viewModel.loadMore()
                }
            }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onErrorConsumed()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(
            text       = "История на покупките",
            fontSize   = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color      = headingText,
            modifier   = Modifier.padding(bottom = 16.dp),
        )

        when {
            uiState.isLoading && uiState.compactPurchaseResponses.isEmpty() -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                ) {
                    CircularProgressIndicator(color = primaryGreen)
                }
            }

            !uiState.isLoading && uiState.compactPurchaseResponses.isEmpty() -> {
                Text(
                    text     = "Все още няма направени поръчки.",
                    color    = MutedGrey,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                )
            }

            else -> {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.compactPurchaseResponses,
                        key   = { it.purchaseCode },
                    ) { purchase ->
                        PurchaseCard(
                            compactPurchaseResponse = purchase,
                            onClick  = {
                                navController.currentBackStackEntry?.savedStateHandle?.apply {
                                    set("purchaseDate", purchase.purchaseDate)
                                    set("purchaseStatus", purchase.status)
                                    set("deliveryAddress", purchase.deliveryAddress)
                                    set("totalCostCents", purchase.totalCostCents)
                                    set("shippingFeeCents", purchase.shippingFeeCents)
                                }
                                navController.navigate("detailed_purchase/${purchase.purchaseCode}")
                            },
                        )
                    }

                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = primaryGreen, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(snackbarData = data)
        }
    }
}


@Composable
private fun PurchaseCard(
    compactPurchaseResponse: CompactPurchaseResponse,
    onClick: () -> Unit,
) {
    val status          = PurchaseStatus.fromKey(compactPurchaseResponse.status)
    val previewProducts = compactPurchaseResponse.products.take(3)
    val overflow        = compactPurchaseResponse.products.size - 3
    val dark            = isSystemInDarkTheme()
    val dividerColor    = if (dark) QuantityButtonBgDark else StarEmptyLight

    Surface(
        shape  = RoundedCornerShape(12.dp),
        color  = cardBg,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderNormal, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text(
                        text     = "Номер на поръчка",
                        fontSize = 11.sp,
                        color    = MutedGrey,
                    )
                    Text(
                        text       = compactPurchaseResponse.purchaseCode,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color      = headingText,
                    )
                }
                Row(
                    verticalAlignment  = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text     = formatDate(compactPurchaseResponse.purchaseDate, "dd.MM.yyyy"),
                        fontSize = 11.sp,
                        color    = MutedGrey,
                    )
                    StatusChip(status)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.fillMaxWidth(),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    previewProducts.forEach { product ->
                        ProductImage(
                            imageUrl    = product.imageUrl,
                            contentDescription = product.productName,
                            modifier        = Modifier.size(48.dp),
                            productCode = product.productCode
                        )
                    }
                    if (overflow > 0) {
                        OverflowThumb(count = overflow)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text     = "Адрес за доставка",
                        fontSize = 11.sp,
                        color    = MutedGrey,
                    )
                    Text(
                        text     = compactPurchaseResponse.deliveryAddress ?: "",
                        fontSize = 12.sp,
                        color    = headingText.copy(alpha = 0.85f),
                        maxLines = 1,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = dividerColor,
                        shape = RoundedCornerShape(0.dp),
                    )
                    .padding(top = 12.dp),
            ) {
                Column {
                    Text(text = "Обща сума", fontSize = 11.sp, color = MutedGrey)
                    Text(
                        text       = formatEurCents(compactPurchaseResponse.totalCostCents),
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = headingText,
                    )
                }
                Text(
                    text     = "Виж детайли →",
                    fontSize = 11.sp,
                    color    = MutedGrey,
                )
            }
        }
    }
}

@Composable
private fun OverflowThumb(count: Int) {
    val dark    = isSystemInDarkTheme()
    val bgColor = if (dark) QuantityButtonBgDark else StarEmptyLight
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = Modifier.size(48.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text       = "+$count",
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MutedGrey,
            )
        }
    }
}