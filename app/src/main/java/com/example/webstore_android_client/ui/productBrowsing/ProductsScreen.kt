package com.example.webstore_android_client.ui.productBrowsing

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.webstore_android_client.ui.productBasic.ProductCard
import com.example.webstore_android_client.ui.theme.DarkCustom
import com.example.webstore_android_client.ui.theme.EmeraldGreen
import com.example.webstore_android_client.ui.theme.MainBgDark
import com.example.webstore_android_client.ui.theme.MainBgLight
import com.example.webstore_android_client.ui.theme.MutedGrey
import com.example.webstore_android_client.ui.theme.PageBgDark
import com.example.webstore_android_client.ui.theme.PageBgLight
import com.example.webstore_android_client.ui.theme.RowBgDark
import com.example.webstore_android_client.ui.theme.RowBgLight
import com.example.webstore_android_client.ui.theme.UnfocusedBorderColor
import com.example.webstore_android_client.ui.theme.WhiteCustom
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    navController: NavHostController,
    viewModel: ProductsViewModel = viewModel(),
    imageSearchViewModel: ImageSearchViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark  = isSystemInDarkTheme()
    val pageBg  = if (isDark) PageBgDark else PageBgLight
    val imageSession by imageSearchViewModel.session.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val modeString = navBackStackEntry?.arguments?.getString("mode")
    val isImageSearch = modeString == "image_search"
    val gridState = rememberLazyGridState()



    LaunchedEffect(isImageSearch, imageSession) {

        val query = ProductQuery.fromNavArgs(modeString, navBackStackEntry?.arguments?.getString("details"))

        if (isImageSearch) {
            if (imageSession != null) {
                viewModel.load(query, imageSession)
            } else {
                navController.navigate("home") {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        }
    }



    LaunchedEffect(gridState) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = layoutInfo.totalItemsCount

            if (total > 0 && lastVisible >= total - 4) {
                total
            } else {
                null
            }
        }
            .distinctUntilChanged()
            .collect { triggerTotal ->
                if (triggerTotal != null) {
                    viewModel.loadMore()
                }
            }
    }

    // ---------------------------------- Skeleton on initial load ----------------------------------
    if (uiState.isLoading && uiState.products.isEmpty()) {
        ProductsSkeleton()
        return
    }

    // ---------------------------------- Full-screen error ----------------------------------
    if (uiState.error != null && uiState.products.isEmpty()) {
        ProductsErrorScreen(
            message = uiState.error!!,
            onRetry = viewModel::retry
        )
        return
    }

    val query = uiState.query ?: return

    // ---------------------------------- Root layout ----------------------------------
    Box(modifier = Modifier
        .fillMaxSize()
        .background(pageBg)) {


        LazyVerticalGrid(
            columns               = GridCells.Fixed(2),
            state                 = gridState,
            contentPadding        = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier              = Modifier.fillMaxSize()
        ) {

            // ---------------------------------- Sort + Filter controls bar ----------------------------------
            if (query.sortAvailable || query.filtersAvailable) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SortFilterBar(
                        query         = query,
                        onSortChange  = viewModel::changeSort,
                        onFiltersOpen = viewModel::openFiltersSheet,
                        isDark        = isDark
                    )
                }
            }

            // ---------------------------------- Section title  ----------------------------------
            if (query.mode !is ProductQueryMode.Search) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text       = query.screenTitle,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (isDark) Color.White else Color.Black,
                        modifier   = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // ---------------------------------- Product grid ----------------------------------
            if (uiState.products.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyProductsMessage(isDark = isDark)
                }
            } else {
                items(
                    items = uiState.products,
                    key   = { it.productCode }
                ) { product ->
                    ProductCard(
                        compactProductResponse = product,
                        onProductClick         = { code ->
                            navController.navigate("details/$code")
                        }
                    )
                }
            }

            // ---------------------------------- Infinite scroll footer ----------------------------------

            when {
                uiState.isLoadingMore -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier         = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            CircularProgressIndicator(
                                color        = if (isDark) MainBgDark else MainBgLight,
                                modifier     = Modifier.size(28.dp),
                                strokeWidth  = 2.dp
                            )
                        }
                    }
                }

                !uiState.hasMore && uiState.products.isNotEmpty() -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text      = "Всички ${uiState.totalElements} продукта са заредени",
                            color     = MutedGrey,
                            fontSize  = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }

            if (uiState.error != null && uiState.products.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text     = uiState.error!!,
                            color    = MutedGrey,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = viewModel::retry,
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) MainBgDark else MainBgLight,
                                contentColor   = Color.White
                            )
                        ) {
                            Text("Опитай пак", fontSize = 13.sp)
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(16.dp)) }
        }

        // ---------------------------------- Filter bottom sheet  ----------------------------------
        FilterBottomSheet(
            isVisible       = uiState.isFiltersSheetOpen,
            categoryFilters = uiState.categoryFilters,
            currentFilters  = uiState.query?.filters ?: ProductFilters(),
            onApply         = viewModel::applyFilters,
            onReset         = viewModel::resetFilters,
            onDismiss       = viewModel::closeFiltersSheet
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortFilterBar(
    query: ProductQuery,
    onSortChange: (ProductSort) -> Unit,
    onFiltersOpen: () -> Unit,
    isDark: Boolean
) {
    val rowBg = if (isDark) RowBgDark else RowBgLight

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .background(rowBg, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // ---------------------------------- Sort dropdown ----------------------------------
        if (query.sortAvailable) {
            SortDropdown(
                selectedSort  = query.sort,
                availableSorts = query.availableSorts,
                onSortChange  = onSortChange,
                isDark        = isDark,
                modifier      = Modifier.weight(1f)
            )
        }

        // ---------------------------------- Filter button ----------------------------------
        if (query.filtersAvailable) {
            Button(
                onClick  = onFiltersOpen,
                modifier = Modifier.padding(start = 8.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) MainBgDark else MainBgLight,
                    contentColor   = Color.White
                ),
                shape    = RoundedCornerShape(6.dp)
            ) {
                Text("Филтри", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortDropdown(
    selectedSort: ProductSort,
    availableSorts: List<ProductSort>,
    onSortChange: (ProductSort) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val labelColor = if (isDark) Color.White else Color.Black
    val rowBg      = if (isDark) RowBgDark  else RowBgLight

    ExposedDropdownMenuBox(
        expanded        = expanded,
        onExpandedChange = { expanded = it },
        modifier        = modifier
    ) {
        val fillMaxWidth = Modifier
            .fillMaxWidth()
        OutlinedTextField(
            value         = selectedSort.label,
            onValueChange = {},
            readOnly      = true,
            label         = { Text("Сортиране по", fontSize = 12.sp, color = labelColor) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine    = true,
            colors        = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = rowBg,
                unfocusedContainerColor = rowBg,
                focusedBorderColor      = EmeraldGreen,
                unfocusedBorderColor    = UnfocusedBorderColor,
                focusedTextColor        = labelColor,
                unfocusedTextColor      = labelColor,
                focusedLabelColor       = labelColor,
                unfocusedLabelColor     = labelColor,
                focusedTrailingIconColor   = labelColor,
                unfocusedTrailingIconColor = labelColor
            ),
            shape    = RoundedCornerShape(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
        )

        ExposedDropdownMenu(
            expanded        = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableSorts.forEach { sort ->
                DropdownMenuItem(
                    text    = {
                        Text(
                            text   = sort.label,
                            color  = if (sort == selectedSort) EmeraldGreen else labelColor,
                            fontWeight = if (sort == selectedSort) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = { onSortChange(sort); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun EmptyProductsMessage(isDark: Boolean) {

    val textColour = if (isDark) WhiteCustom else  DarkCustom

    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = "Няма намерени продукти.",
            fontSize  = 18.sp,
            color     = textColour,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProductsErrorScreen(message: String, onRetry: () -> Unit) {
    val isDark  = isSystemInDarkTheme()
    val pageBg  = if (isDark) PageBgDark else PageBgLight
    val btnBg   = if (isDark) MainBgDark  else MainBgLight

    Column(
        modifier              = Modifier
            .fillMaxSize()
            .background(pageBg)
            .padding(32.dp),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        Text(
            text      = message,
            color     = MutedGrey,
            fontSize  = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors  = ButtonDefaults.buttonColors(
                containerColor = btnBg,
                contentColor   = Color.White
            )
        ) {
            Text("Опитай отново", fontSize = 15.sp)
        }
    }
}
