package com.example.webstore_android_client.ui.cart

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.webstore_android_client.model.localDTOs.purchase.SelectedCheckoutItem
import com.example.webstore_android_client.model.responses.cart.CartEntryResponse
import com.example.webstore_android_client.ui.productBasic.ProductImage
import com.example.webstore_android_client.ui.theme.AppBackgroundDark
import com.example.webstore_android_client.ui.theme.AppBackgroundLight
import com.example.webstore_android_client.ui.theme.CardBgDark
import com.example.webstore_android_client.ui.theme.CardWhite
import com.example.webstore_android_client.ui.theme.EmeraldGreen
import com.example.webstore_android_client.ui.theme.ErrorRed
import com.example.webstore_android_client.ui.theme.ImageBgGrey
import com.example.webstore_android_client.ui.theme.MainBgDark
import com.example.webstore_android_client.ui.theme.MainBgLight
import com.example.webstore_android_client.ui.theme.MutedGrey
import com.example.webstore_android_client.ui.theme.OutOfStockRed
import com.example.webstore_android_client.ui.theme.QuantityButtonBgDark
import com.example.webstore_android_client.ui.theme.QuantityButtonBgLight
import com.example.webstore_android_client.ui.theme.StarAndSaleOrange

@Composable
fun CartScreen(
    navController: NavHostController,
    viewModel: CartViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    
    val appBg = if (isDark) AppBackgroundDark else AppBackgroundLight
    val cardBg = if (isDark) CardBgDark else CardWhite
    val context: Context = LocalContext.current


    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearToast()
        }
    }

    if (uiState.showDeleteDialog) {
        val count = uiState.selectedCodes.size
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Премахване на продукти") },
            text = { 
                Text(
                    "Сигурни ли сте, че искате да премахнете ${if (count > 1) "тези $count продукти" else "този продукт"}?"
                ) 
            },
            confirmButton = {
                TextButton(onClick = { viewModel.executeBatchDelete() }) {
                    Text("Да", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Отказ", color = MutedGrey)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appBg)
            .padding(horizontal = 16.dp)
    ) {
        // --- Header Actions ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(cardBg)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = uiState.isAllSelected,
                    onCheckedChange = { viewModel.toggleSelectAll(it) },
                    enabled = uiState.selectableEntries.isNotEmpty() && uiState.mutatingCodes.isEmpty(),
                    colors = CheckboxDefaults.colors(checkedColor = EmeraldGreen)
                )
                Text(
                    text = "${uiState.selectedCodes.size} избрани от ${uiState.entries.size}",
                    color = MutedGrey,
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = { viewModel.promptBatchDelete() },
                enabled = uiState.selectedCodes.isNotEmpty() && uiState.mutatingCodes.isEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed,
                    disabledContainerColor = ErrorRed.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Премахни", color = Color.White)
            }
        }

        // --- Cart Items & Summary ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(uiState.entries, key = { it.product.productCode }) { entry ->
                CartItemRow(
                    entry = entry,
                    isSelected = uiState.selectedCodes.contains(entry.product.productCode),
                    isMutating = uiState.mutatingCodes.contains(entry.product.productCode),
                    onToggleSelect = { viewModel.toggleSelection(entry.product.productCode) },
                    onQuantityChange = { inc -> viewModel.handleQuantityChange(entry.product.productCode, inc) },
                    onDelete = { viewModel.handleDeleteItem(entry.product.productCode) },
                    onProductClick = { navController.navigate("details/${entry.product.productCode}") }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                CartSummaryCard(uiState = uiState, onProceed = {

                    val selectedEntries = uiState
                        .selectedEntries
                        .map { entry -> SelectedCheckoutItem(entry.product.productCode, entry.quantity) }
                        .toList()

                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selectedItems", selectedEntries)


                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("isDirectPurchase", false)

                    navController.navigate("checkout")
                })
            }
        }
    }
}

@Composable
fun CartItemRow(
    entry: CartEntryResponse,
    isSelected: Boolean,
    isMutating: Boolean,
    onToggleSelect: () -> Unit,
    onQuantityChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onProductClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) CardBgDark else CardWhite
    val textColor = if (isDark) Color.White else Color.Black
    val quantityBg = if (isDark) QuantityButtonBgDark else QuantityButtonBgLight
    
    val product = entry.product
    val alpha = if (product.isInStock) 1f else 0.5f
    val modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(cardBg)
        .let {
            if (entry.isOverStock) it.border(2.dp, OutOfStockRed, RoundedCornerShape(8.dp)) else it
        }
        .clickable(enabled = product.isInStock && !isMutating, onClick = onProductClick)
        .alpha(if (isMutating) 0.5f else alpha)
        .padding(12.dp)

    Box {
        Column(modifier = modifier) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    enabled = product.isInStock && !isMutating,
                    colors = CheckboxDefaults.colors(checkedColor = EmeraldGreen)
                )

                ProductImage(productCode = product.productCode,
                    imageUrl = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ImageBgGrey)
                        .padding(4.dp)
                )

                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = product.name,
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(text = "★ ${product.rating}", color = StarAndSaleOrange, fontSize = 14.sp)
                        Text(text = " (${product.reviewCount})", color = MutedGrey, fontSize = 12.sp)
                    }
                }
            }

            if (!product.isInStock) {
                Text("Няма наличност", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            } else if (entry.isOverStock) {
                Text("⚠️ Заявеното количество надвишава наличността (${entry.stockQuantity} бр.)", color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    if (entry.isDiscounted) {
                        Text(
                            text = "€${"%.2f".format(product.originalPriceStotinki / 100.0)}",
                            color = MutedGrey,
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "€${"%.2f".format(product.salePriceStotinki / 100.0)}",
                                color = ErrorRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(" × ${entry.quantity}", color = textColor, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp))
                        }
                        val saved = (product.originalPriceStotinki - product.salePriceStotinki) / 100.0
                        Text("спестени: €${"%.2f".format(saved)}", color = EmeraldGreen, fontSize = 12.sp)
                    } else {
                        Text(
                            text = "€${"%.2f".format(product.originalPriceStotinki / 100.0)} × ${entry.quantity}",
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(quantityBg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onQuantityChange(false) },
                            enabled = product.isInStock && !isMutating,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(32.dp)
                        ) { Text("-", color = textColor) }
                        
                        Text(text = "${entry.quantity}", color = textColor, modifier = Modifier.padding(horizontal = 8.dp))
                        
                        TextButton(
                            onClick = { onQuantityChange(true) },
                            enabled = product.isInStock && !isMutating,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(32.dp)
                        ) { Text("+", color = textColor) }
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    
                    TextButton(onClick = onDelete, enabled = !isMutating) {
                        Text("🗑️", fontSize = 18.sp)
                    }
                }
            }
        }
        
        if (isMutating) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EmeraldGreen)
            }
        }
    }
}

@Composable
fun CartSummaryCard(uiState: CartUiState, onProceed: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) CardBgDark else CardWhite
    val textColor = if (isDark) Color.White else Color.Black
    val buttonBg = if (isDark) MainBgDark else MainBgLight

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(cardBg)
            .padding(16.dp)
    ) {
        Text("Обща сметка:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
        
        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isDiscountMode) {
            Text("Цена за продукти: €${"%.2f".format(uiState.totalSaleCostCents / 100.0)}", color = ErrorRed, fontWeight = FontWeight.Medium)
            Text("€${"%.2f".format(uiState.totalOriginalCostCents / 100.0)}", color = MutedGrey, textDecoration = TextDecoration.LineThrough)
            Text("спестихте: €${"%.2f".format((uiState.totalOriginalCostCents - uiState.totalSaleCostCents) / 100.0)}", color = EmeraldGreen, fontSize = 14.sp)
        } else {
            Text("Цена за продукти: €${"%.2f".format(uiState.totalSaleCostCents / 100.0)}", color = textColor)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val deliveryText = if (uiState.selectedCodes.isEmpty()) "0.00 €" else if (uiState.deliveryCostCents > 0) "€${"%.2f".format(uiState.deliveryCostCents / 100.0)}" else "БЕЗПЛАТНО"
        Text("Цена за доставка: $deliveryText", color = textColor)

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MutedGrey.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isDiscountMode) {
            Text("Общо: €${"%.2f".format(uiState.grandSaleTotalCents / 100.0)}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
            Text("€${"%.2f".format(uiState.grandOriginalTotalCents / 100.0)}", color = MutedGrey, textDecoration = TextDecoration.LineThrough)
            Text("спестихте: €${"%.2f".format((uiState.grandOriginalTotalCents - uiState.grandSaleTotalCents) / 100.0)}", color = EmeraldGreen, fontWeight = FontWeight.Medium)
        } else {
            Text("Общо: €${"%.2f".format(uiState.grandSaleTotalCents / 100.0)}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onProceed,
            enabled = uiState.selectedCodes.isNotEmpty() && !uiState.hasOverStockSelected && uiState.mutatingCodes.isEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = buttonBg),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Продължи", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
        }
    }

}
