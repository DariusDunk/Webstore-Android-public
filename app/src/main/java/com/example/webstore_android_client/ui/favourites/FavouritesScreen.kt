package com.example.webstore_android_client.ui.favourites

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.webstore_android_client.model.responses.product.CompactProductResponse
import com.example.webstore_android_client.ui.productBasic.ProductImage
import com.example.webstore_android_client.ui.theme.AppBackgroundDark
import com.example.webstore_android_client.ui.theme.AppBackgroundLight
import com.example.webstore_android_client.ui.theme.EmeraldGreen
import com.example.webstore_android_client.ui.theme.GoldStar
import com.example.webstore_android_client.ui.theme.MainBgDark
import com.example.webstore_android_client.ui.theme.MainBgLight
import com.example.webstore_android_client.ui.theme.MutedGrey
import com.example.webstore_android_client.ui.theme.OrangeLight
import com.example.webstore_android_client.ui.theme.OutOfStockRed
import com.example.webstore_android_client.ui.theme.StarAndSaleOrange
import com.example.webstore_android_client.ui.theme.StarEmptyDark
import com.example.webstore_android_client.ui.theme.StarEmptyLight
import com.example.webstore_android_client.ui.theme.TextFieldBgDark
import com.example.webstore_android_client.ui.theme.WhiteCustom


private fun Modifier.grayscaleAndFade(alpha: Float = 0.75f): Modifier =
    this
        .graphicsLayer { }
        .drawWithContent {
            val paint = Paint().apply {
                this.alpha = alpha
                colorFilter = ColorFilter.colorMatrix(
                    ColorMatrix().apply { setToSaturation(0f) }
                )
            }
            drawIntoCanvas { canvas ->
                canvas.saveLayer(Rect(Offset.Zero, size), paint)
                this@drawWithContent.drawContent()
                canvas.restore()
            }
        }
 
@Composable
fun FavouritesScreen(
    navController: NavHostController,
    viewModel: FavouritesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark  = isSystemInDarkTheme()
    val context = LocalContext.current
    val pageBg  = if (isDark) AppBackgroundDark else AppBackgroundLight

    // ----- One-shot event collector ----------------------------------------------
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FavouritesEvent.ShowToast ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is FavouritesEvent.ShowError ->
                    Toast.makeText(context, "${event.title}: ${event.detail}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ----- Infinite scroll trigger ------------------------------------------------
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val info        = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
            val total = info.totalItemsCount
            total > 0 && lastVisible >= total - 2
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMore()
    }

    // ----------- Delete confirmation dialog --------------------------------------------
    if (uiState.showDeleteConfirmDialog) {
        val count = uiState.selectedCount
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = { Text("Премахване на продукти") },
            text  = {
                Text(
                    if (count > 1) "Сигурни ли сте, че искате да премахнете тези $count продукта?"
                    else           "Сигурни ли сте, че искате да премахнете този продукт?"
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::executeDeleteSelected,
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = OutOfStockRed,
                        contentColor   = Color.White
                    )
                ) { Text("Да, изтрий") }
            },
            dismissButton = {
                OutlinedButton(onClick = viewModel::dismissDeleteDialog) { Text("Отказ") }
            }
        )
    }

    // ------------ Root layout ------------------------------------------------------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        // --------------- Action bar   ------------------
        ActionBar(
            uiState       = uiState,
            isDark        = isDark,
            onSelectAll   = { if (uiState.allSelected) viewModel.deselectAll() else viewModel.selectAll() },
            onAddToCart   = viewModel::addSelectedToCart,
            onDeleteSelected = viewModel::requestDeleteSelected
        )

        // ---------------- Content area ------------------------------------------------------
        when {
            uiState.isLoading && uiState.products.isEmpty() -> {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    FavouritesSkeleton(isDark = isDark)
                }
            }

            !uiState.isLoading && uiState.totalElements == 0L -> {
                EmptyFavourites(isDark = isDark)
            }

            uiState.error != null && uiState.products.isEmpty() -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            uiState.error!!,
                            color     = MutedGrey,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = viewModel::refresh,
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) MainBgDark else MainBgLight,
                                contentColor   = Color.White
                            )
                        ) { Text("Опитай отново") }
                    }
                }
            }

            else -> {
                LazyColumn(
                    state           = listState,
                    contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier        = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.products,
                        key   = { it.productCode }
                    ) { product ->
                        FavouriteItemCard(
                            product       = product,
                            isSelected    = product.productCode in uiState.selectedProductCodes,
                            isMutating    = product.productCode in uiState.mutatingProductCodes,
                            anyMutating   = uiState.isMutating,
                            isDark        = isDark,
                            onToggleSelect = { viewModel.toggleSelect(product.productCode) },
                            onAddToCart    = { viewModel.addToCart(product) },
                            onDelete       = { viewModel.deleteItem(product) },
                            onProductClick = { navController.navigate("details/${product.productCode}") }
                        )
                    }

                    // ----- Infinite scroll footer --------------------------------
                    when {
                        uiState.isLoadingMore -> item(key = "loading_more") {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier         = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(28.dp),
                                    strokeWidth = 2.dp,
                                    color       = if (isDark) MainBgDark else MainBgLight
                                )
                            }
                        }

                        !uiState.hasMore && uiState.products.isNotEmpty() ->
                            item(key = "end_marker") {
                                Text(
                                    text      = "Всички ${uiState.totalElements} любими са заредени",
                                    color     = MutedGrey,
                                    fontSize  = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier  = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                )
                            }
                    }

                    if (uiState.error != null && uiState.products.isNotEmpty()) {
                        item(key = "inline_error") {
                            Row(
                                modifier              = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(uiState.error!!, color = MutedGrey, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                TextButton(onClick = viewModel::refresh) { Text("Опитай пак") }
                            }
                        }
                    }

                    item(key = "bottom_spacer") { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ActionBar(
    uiState: FavouritesUiState,
    isDark: Boolean,
    onSelectAll: () -> Unit,
    onAddToCart: () -> Unit,
    onDeleteSelected: () -> Unit
) {
    val cardBg       = if (isDark) TextFieldBgDark else WhiteCustom
    val labelColor   = if (isDark) Color.White     else MutedGrey
    val addCartBg    = if (isDark) OrangeLight     else MainBgLight

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .background(cardBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Checkbox(
            checked         = uiState.allSelected,
            onCheckedChange = { onSelectAll() },
            enabled         = uiState.products.isNotEmpty() && !uiState.isMutating,
            colors          = CheckboxDefaults.colors(
                checkedColor   = EmeraldGreen,
                uncheckedColor = MutedGrey
            )
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text     = "${uiState.selectedCount} избрани от общо ${uiState.products.size} продукта",
            color    = labelColor,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        Button(
            onClick  = onAddToCart,
            enabled  = uiState.selectedCount > 0 && !uiState.isMutating,
            shape    = RoundedCornerShape(6.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = addCartBg,
                contentColor           = Color.White,
                disabledContainerColor = addCartBg.copy(alpha = 0.5f),
                disabledContentColor   = Color.White.copy(alpha = 0.5f)
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text("Добави в количка", fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.width(8.dp))

        Button(
            onClick  = onDeleteSelected,
            enabled  = uiState.selectedCount > 0 && !uiState.isMutating,
            shape    = RoundedCornerShape(6.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = OutOfStockRed,
                contentColor           = Color.White,
                disabledContainerColor = OutOfStockRed.copy(alpha = 0.5f),
                disabledContentColor   = Color.White.copy(alpha = 0.5f)
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text("Изтрий", fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun FavouriteItemCard(
    product: CompactProductResponse,
    isSelected: Boolean,
    isMutating: Boolean,
    anyMutating: Boolean,
    isDark: Boolean,
    onToggleSelect: () -> Unit,
    onAddToCart: () -> Unit,
    onDelete: () -> Unit,
    onProductClick: () -> Unit
) {
    val cardBg       = if (isDark) TextFieldBgDark else WhiteCustom
    val textColor    = if (isDark) Color.White     else Color.Black
    val emptyStarColor = if (isDark) StarEmptyDark else StarEmptyLight
    val isOutOfStock = !product.isInStock

    val filledStars = (product.rating / 10).coerceIn(0, 5)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        // -------------- Main Layout --------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
                .then(if (isOutOfStock) Modifier.grayscaleAndFade(0.75f) else Modifier)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onProductClick
                )
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // ------------Checkbox---------------------
            Checkbox(
                checked         = isSelected,
                onCheckedChange = { onToggleSelect() },
                enabled         = !isMutating,
                colors          = CheckboxDefaults.colors(
                    checkedColor   = EmeraldGreen,
                    uncheckedColor = MutedGrey,
                    disabledCheckedColor   = MutedGrey,
                    disabledUncheckedColor = MutedGrey
                )
            )

            Spacer(Modifier.width(8.dp))

            //-------------- Product image-----------------
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                ProductImage(
                    productCode        = product.productCode,
                    imageUrl           = product.imageUrl,
                    contentDescription = product.name,
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier.size(72.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // -------------- Right Content Column --------------------
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text       = product.name,
                    color      = textColor,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { i ->
                        Text(
                            text     = "★",
                            color    = if (i < filledStars) GoldStar else emptyStarColor,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text     = "(${product.reviewCount})",
                        color    = MutedGrey,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        val hasSale = product.salePriceStotinki > 0 &&
                                product.salePriceStotinki < product.originalPriceStotinki
                        if (hasSale) {
                            Text(
                                text       = "${"%.2f".format(product.salePriceStotinki / 100.0)} €",
                                color      = StarAndSaleOrange,
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text           = "${"%.2f".format(product.originalPriceStotinki / 100.0)} €",
                                color          = MutedGrey,
                                fontSize       = 12.sp,
                                textDecoration = TextDecoration.LineThrough
                            )
                        } else {
                            Text(
                                text       = "${"%.2f".format(product.originalPriceStotinki / 100.0)} €",
                                color      = textColor,
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !isMutating, onClick = onDelete),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text     = "🗑️",
                                fontSize = 18.sp,
                                color    = if (isMutating) MutedGrey else textColor
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier         = Modifier
                                .size(40.dp)
                                .border(
                                    width  = 1.dp,
                                    color  = if (!isOutOfStock && !isMutating) EmeraldGreen else Color.LightGray,
                                    shape  = RoundedCornerShape(10.dp)
                                )
                                .background(
                                    color = if (!isOutOfStock && !isMutating) EmeraldGreen.copy(alpha = 0.1f) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .then(
                                    if (!isOutOfStock && !isMutating)
                                        Modifier.clickable(onClick = onAddToCart)
                                    else Modifier
                                )
                        ) {
                            Text(
                                text     = "🛒",
                                fontSize = 20.sp,
                                color    = if (isOutOfStock || isMutating) Color.Gray else textColor
                            )
                        }
                    }
                }
            }
        }

        // -------- Out-of-stock badge ------------------------------------------------
        if (isOutOfStock) {
            Text(
                text       = "Няма наличност",
                color      = Color.White,
                fontSize   = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(OutOfStockRed)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }

        // ----------------- Mutation overlay --------------------------------------------------
        if (isMutating) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.30f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color       = Color.White,
                    modifier    = Modifier.size(32.dp),
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

@Composable
private fun EmptyFavourites(isDark: Boolean) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = "Няма любими продукти",
            color     = if (isDark) Color(0xFFD1D5DB) else MutedGrey,
            fontSize  = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}
