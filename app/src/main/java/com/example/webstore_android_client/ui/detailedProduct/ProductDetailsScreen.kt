package com.example.webstore_android_client.ui.detailedProduct

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.webstore_android_client.R
import com.example.webstore_android_client.model.localDTOs.purchase.SelectedCheckoutItem
import com.example.webstore_android_client.model.responses.product.DetailedProductResponse
import com.example.webstore_android_client.model.responses.review.ReviewResponse
import com.example.webstore_android_client.repositories.RepositoryProvider
import com.example.webstore_android_client.tools.formatDate
import com.example.webstore_android_client.ui.dialogue.LoginPromptDialogue
import com.example.webstore_android_client.ui.productBasic.ProductImage
import com.example.webstore_android_client.ui.reviewPostDialogue.ReviewPostDialog
import com.example.webstore_android_client.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProductDetailsScreen(
    productCode: String,
    viewModel: ProductDetailsViewModel = viewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val pageBg = if (isDark) PageBgDark else PageBgLight
    val context: Context = LocalContext.current
    var showLightbox by remember { mutableStateOf(false) }

    LaunchedEffect(productCode) {
        viewModel.loadProductData(productCode)
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeToast()
        }
    }

    if (uiState.showGuestPrompt)
    {
        LoginPromptDialogue(uiState.guestPromptText, navController, onClick = {viewModel.dismissGuestPrompt()})
    }

    if (uiState.isProductLoading || uiState.productData == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBg), contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = if (isDark) MainBgDark else MainBgLight)
        }
        return
    }

    val product = uiState.productData!!
    val images = product.productImageURLs ?: listOf()
    val pagerState = rememberPagerState(pageCount = { images.size.coerceAtLeast(1) })

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(onClick = { navController.navigate("home") }) {
                    Text(
                        text = "Начало",
                        fontSize = 12.sp,
                        color = MutedGrey,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Text(
                    text = product.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) RowBgDark else RowBgLight)
                        .clickable { showLightbox = true },
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        ProductImage(
                            productCode = productCode,
                            imageUrl = images.getOrNull(page),
                            modifier = Modifier.fillMaxSize(),
                            contentDescription = "Main Product Image"
                        )
                    }
                }

                if (images.size > 1) {
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        images.forEachIndexed { index, _ ->
                            val color = if (pagerState.currentPage == index) {
                                if (isDark) MainBgDark else MainBgLight
                            } else {
                                MutedGrey.copy(alpha = 0.5f)
                            }
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                val textColor = if (isDark) WhiteCustom else Color.Black
                Text("Модел: ${product.model}", color = textColor, fontSize = 14.sp)
                Text("Производител: ${product.manufacturer}", color = textColor, fontSize = 14.sp)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Производител:",
                        color = textColor,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    AssistChip(
                        onClick = {
                            navController.navigate(
                                "products/manufacturer/${Uri.encode(product.manufacturer)}"
                            )
                        },
                        label = {
                            Text(product.manufacturer)
                        }
                    )
                }

                product.attributes?.forEach { attr ->
                    Text(
                        "${attr.attributeName}: ${attr.option} ${attr.measurementUnit ?: ""}",
                        color = textColor,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    product.productDescription,
                    color = textColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        item {
            PriceAndActionsCard(
                product,
                uiState.quantity,
                viewModel,
                isDark,
                uiState.isAddingToCart,
                uiState.isMutatingFavourites,
                navController,
                uiState.isInFavourites,
                uiState.quantity

            )
        }

        item {
            HorizontalDivider(
                color = MutedGrey.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 16.dp)
            )
            ReviewsHeader(uiState, isDark, productCode, viewModel)

            ReviewsFilterSection(uiState, productCode, viewModel, isDark)
            HorizontalDivider(
                color = MutedGrey.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        itemsIndexed(uiState.reviews) { index, review ->
            ReviewItem(review, isDark, viewModel)

            if (index == uiState.reviews.lastIndex && !uiState.isLastPage && !uiState.isReviewsLoading) {
                LaunchedEffect(index) { viewModel.loadReviews(productCode) }
            }
        }

        if (uiState.isReviewsLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = if (isDark) MainBgDark else MainBgLight)
                }
            }
        }
    }

    if (showLightbox) {
        ProductImageLightbox(
            images = images,
            initialIndex = pagerState.currentPage,
            onDismiss = { showLightbox = false },
            productCode = productCode
        )
    }

    ReviewPostDialog(
        isOpen = uiState.isReviewDialogOpen,
        onClose = { viewModel.closeReviewDialog() },
        productCode = productCode,
        productName = product.name,
        productImage = product.productImageURLs?.firstOrNull(),
        handlePostSuccess = { msg -> viewModel.onReviewPostSuccess(msg, productCode) },
        mode = uiState.reviewDialogMode,
        existingReview = uiState.existingReviewToEdit
    )

    if (uiState.showExistingReviewPrompt ) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissExistingReviewPrompt() },
            title = { Text("Съществуващо ревю") },
            text = { Text("Вече имаш ревю за този продукт! Можеш да го промениш или изтриеш.") },
            confirmButton = {
                TextButton(onClick = {
                    uiState.existingReviewToEdit?.let {
                        viewModel.openUpdateDialog(
                            it
                        )
                    }
                }) {
                    Text("Промяна", color = if (isDark) GreenLight else MainBgLight)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.requestDeleteConfirm() }) {
                    Text("Изтриване", color = ErrorRed)
                }
            }
        )
    }

    if (uiState.showDeleteConfirmPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirmPrompt() },
            title = { Text("Изтриване на ревю") },
            text = { Text("Изтриването на ревюто е окончателно и няма да можете да публикувате ново ревю. Сигурни ли сте че искате да изтриете това ревю?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteReview(productCode) }) {
                    Text("Да", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteConfirmPrompt() }) {
                    Text("Отказ", color = MutedGrey)
                }
            }
        )
    }
}


@Composable
private fun PriceAndActionsCard(
    product: DetailedProductResponse,
    qty: Short, viewModel: ProductDetailsViewModel,
    isDark: Boolean,
    isAddingToCart: Boolean = false,
    isAddingToFavourites: Boolean = false,
    navController: NavController,
    inFavourites: Boolean = false,
    quantity: Short
) {
    val cardBg = if (isDark) RowBgDark else RowBgLight
    val textColor = if (isDark) WhiteCustom else Color.Black
    val mainBg = if (isDark) MainBgDark else MainBgLight
    val favButtonColor = if (inFavourites) Color.Red else CartPressedGreen


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(cardBg, RoundedCornerShape(12.dp))
            .border(1.dp, MutedGrey.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                if (product.salePriceStotinki < product.originalPriceStotinki) {
                    Text(
                        text = "€ ${(product.salePriceStotinki / 100.0).format(2)}",
                        color = ErrorRed,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "€ ${(product.originalPriceStotinki / 100.0).format(2)}",
                        color = MutedGrey,
                        fontSize = 14.sp,
                        textDecoration = TextDecoration.LineThrough
                    )
                } else {
                    Text(
                        text = "€ ${(product.originalPriceStotinki / 100.0).format(2)}",
                        color = textColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Цена за доставка:", color = MutedGrey, fontSize = 12.sp)
                if (product.deliveryCost == null || product.deliveryCost == 0.toShort()) {
                    Text(
                        text = "безплатна",
                        color = textColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "€ ${(product.deliveryCost / 100.0).format(2)}",
                        color = textColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (product.isInStock) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .border(1.dp, MutedGrey, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "-", fontSize = 20.sp, color = textColor, modifier = Modifier
                            .clickable { viewModel.updateQuantity(-1) }
                            .padding(horizontal = 8.dp))
                    Text(
                        qty.toString(),
                        fontSize = 16.sp,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(
                        "+", fontSize = 20.sp, color = textColor, modifier = Modifier
                            .clickable { viewModel.updateQuantity(1) }
                            .padding(horizontal = 8.dp))
                }

                Button(
                    enabled = !isAddingToCart,
                    onClick = { viewModel.addQuantityToCart(product.productCode, qty) },
                    colors = ButtonDefaults.buttonColors(containerColor = mainBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text("🛒", color = Color.White)
                }
                Button(
                    enabled = !isAddingToFavourites,
                    onClick = {


                        if (RepositoryProvider.customerDataRepository.userState.value != null)
                            viewModel.addToOrRemoveFromFavourites(product.productCode)
                        else
                            viewModel.displayGuestPrompt("За добавяне в любими се изисква профил!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = favButtonColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text("♡")
                }
            }

            Button(
                onClick = {
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selectedItems", listOf(SelectedCheckoutItem(product.productCode, quantity.toInt())))

                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("isDirectPurchase", true)

                    navController.navigate("checkout") },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(40.dp)
            ) {
                Text("⚡ Купи сега", color = Color.White)
            }
        } else {
            Text(
                "Изчерпан",
                color = ErrorRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ReviewsHeader(
    uiState: ProductDetailsUiState,
    isDark: Boolean,
    productCode: String,
    viewModel: ProductDetailsViewModel
) {
    val mainBgColor = if (isDark) MainBgDark else MainBgLight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Text(
                uiState.averageRating.format(1),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color.Black
            )
            Text("★".repeat(uiState.averageRating.toInt()), color = GoldStar, fontSize = 24.sp)
            Text("${uiState.totalReviews} ревюта", color = MutedGrey, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.onAddReviewClick(productCode) },
                colors = ButtonDefaults.buttonColors(containerColor = mainBgColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                if (uiState.isCheckingReview) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )
                }
                Text("Добави ревю", color = Color.White)
            }
        }

        Column(modifier = Modifier.width(150.dp)) {
            (5 downTo 1).forEach { star ->
                val count =
                    uiState.ratingOverview.find { (it.rating / 10) == star }?.count ?: 0
                val progress =
                    if (uiState.totalReviews > 0) count.toFloat() / uiState.totalReviews else 0f
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        "$star★",
                        fontSize = 12.sp,
                        color = MutedGrey,
                        modifier = Modifier.width(24.dp)
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isDark) GreenDark else GreenLight,
                        trackColor = if (isDark) RowBgDark else RowBgLight
                    )
                    Text(
                        count.toString(),
                        fontSize = 12.sp,
                        color = MutedGrey,
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewsFilterSection(
    uiState: ProductDetailsUiState,
    productCode: String,
    viewModel: ProductDetailsViewModel,
    isDark: Boolean
) {
    val cardBg = if (isDark) RowBgDark else RowBgLight
    val textColor = if (isDark) WhiteCustom else Color.Black

    var sortMenuExpanded by remember { mutableStateOf(false) }
    var ratingMenuExpanded by remember { mutableStateOf(false) }
    var userMenuExpanded by remember { mutableStateOf(false) }
    var sortFilterText by remember { mutableStateOf("най-нови") }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Box {
                FilterChip(
                    selected = false,
                    onClick = { sortMenuExpanded = true },
                    label = {
                        Text(
                            "Сортирай: $sortFilterText",
                            color = textColor
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = textColor
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(containerColor = cardBg)
                )
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }) {
                    DropdownMenuItem(text = { Text("най-нови") }, onClick = {
                        sortFilterText = "най-нови"
                        viewModel.updateFilters(productCode, sort = "NEWEST")
                        sortMenuExpanded = false
                    })
                    DropdownMenuItem(text = { Text("най-стари") }, onClick = {
                        sortFilterText = "най-стари"
                        viewModel.updateFilters(productCode, sort = "OLDEST")
                        sortMenuExpanded = false
                    })
                }
            }
        }

        item {
            Box {
                val ratingLabel =
                    if (uiState.filterRating == 0.toShort()) "всички" else "${uiState.filterRating} звезди"
                FilterChip(
                    selected = uiState.filterRating != 0.toShort(),
                    onClick = { ratingMenuExpanded = true },
                    label = {
                        Text(
                            stringResource(R.string.RATING_LABEL, ratingLabel),
                            color = textColor
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = textColor
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(containerColor = cardBg)
                )
                DropdownMenu(
                    expanded = ratingMenuExpanded,
                    onDismissRequest = { ratingMenuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("всички") },
                        onClick = {
                            viewModel.updateFilters(
                                productCode,
                                rating = 0
                            ); ratingMenuExpanded = false
                        })
                    (5 downTo 1).forEach { stars ->
                        DropdownMenuItem(
                            text = { Text("$stars звезди") },
                            onClick = {
                                viewModel.updateFilters(
                                    productCode,
                                    rating = stars.toShort()
                                ); ratingMenuExpanded = false
                            })
                    }
                }
            }
        }

        item {
            Box {
                val userLabel =
                    if (uiState.verifiedOnly) "само потвърдени потребители" else "всички"
                FilterChip(
                    selected = uiState.verifiedOnly,
                    onClick = { userMenuExpanded = true },
                    label = { Text("Потребители: $userLabel", color = textColor) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = textColor
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(containerColor = cardBg)
                )
                DropdownMenu(
                    expanded = userMenuExpanded,
                    onDismissRequest = { userMenuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("всички") },
                        onClick = {
                            viewModel.updateFilters(
                                productCode,
                                userOnly = false
                            ); userMenuExpanded = false
                        })
                    DropdownMenuItem(
                        text = { Text("само потвърдени потребители") },
                        onClick = {
                            viewModel.updateFilters(
                                productCode,
                                userOnly = true
                            ); userMenuExpanded = false
                        })
                }
            }
        }
    }
}

@Composable
private fun ReviewItem(
    review: ReviewResponse,
    isDark: Boolean,
    viewModel: ProductDetailsViewModel
) {
    val textColor = if (isDark) WhiteCustom else Color.Black
    val defaultPfp = painterResource(R.drawable.default_pfp)

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = review.customerDetailsForReview.customerPfp,
                    contentDescription = null,
                    placeholder = defaultPfp,
                    error = defaultPfp,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            review.customerDetailsForReview.name,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (review.customerDetailsForReview.isVerified) {
                            Text(
                                text = "✓ Потвърден купувач",
                                color = EmeraldGreen,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .background(
                                        EmeraldGreen.copy(alpha = 0.1f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        formatDate(review.postTimestamp, "yyyy-MM-dd"),
                        color = MutedGrey,
                        fontSize = 12.sp
                    )

                }
            }

            if (review.customerDetailsForReview.currentUser && !review.isDeleted) {
                var menuExpanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Още опции",
                            tint = MutedGrey
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(if (isDark) RowBgDark else RowBgLight)
                    ) {
                        if (!review.customerDetailsForReview.isExpired) {
                            DropdownMenuItem(
                                text = { Text("Промяна", color = textColor) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.openUpdateDialog(review)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Изтриване", color = ErrorRed) },
                            onClick = {
                                menuExpanded = false
                                viewModel.requestDeleteConfirm(review)
                            }
                        )
                    }
                }
            }
        }

        Text(
            "★".repeat((review.rating / 10)),
            color = GoldStar,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            review.reviewText,
            color = textColor,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ProductImageLightbox(
    images: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    productCode: String
) {
    val pagerState =
        rememberPagerState(initialPage = initialIndex, pageCount = { images.size.coerceAtLeast(1) })

    val thumbnailListState =
        rememberLazyListState(initialFirstVisibleItemIndex = maxOf(0, initialIndex - 2))
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        thumbnailListState.animateScrollToItem(maxOf(0, pagerState.currentPage - 2))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    ProductImage(
                        productCode = productCode,
                        imageUrl = images.getOrNull(page),
                        contentDescription = "Zoomed Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                if (images.size > 1) {
                    LazyRow(
                        state = thumbnailListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(images) { index, imgUrl ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.DarkGray,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                            ) {
                                ProductImage(
                                    productCode = productCode,
                                    imageUrl = imgUrl,
                                    modifier = Modifier.fillMaxSize(),
                                    contentDescription = "Thumbnail",
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "×",
                color = Color.White,
                fontSize = 32.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .clickable { onDismiss() }
            )

        }
    }
}

fun Double.format(digits: Int): String = java.lang.String.format("%.${digits}f", this)
fun Float.format(digits: Int): String = java.lang.String.format("%.${digits}f", this)