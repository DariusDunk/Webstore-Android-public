package com.example.webstore_android_client.ui.userProfile.purchaseHistory


import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.webstore_android_client.tools.formatDate
import com.example.webstore_android_client.ui.productBasic.ProductImage
import com.example.webstore_android_client.ui.theme.CardWhite
import com.example.webstore_android_client.ui.theme.DarkCustom
import com.example.webstore_android_client.ui.theme.ErrorRed
import com.example.webstore_android_client.ui.theme.GreenLight
import com.example.webstore_android_client.ui.theme.MainBgDark
import com.example.webstore_android_client.ui.theme.MainBgLight
import com.example.webstore_android_client.ui.theme.MutedGrey
import com.example.webstore_android_client.ui.theme.OutOfStockRed
import com.example.webstore_android_client.ui.theme.QuantityButtonBgDark
import com.example.webstore_android_client.ui.theme.RowBgDark
import com.example.webstore_android_client.ui.theme.SkeletonCardDark
import com.example.webstore_android_client.ui.theme.SkeletonPulseDark
import com.example.webstore_android_client.ui.theme.StarEmptyLight
import com.example.webstore_android_client.ui.theme.TextFieldBgDark
import com.example.webstore_android_client.ui.theme.TextFieldBgLight
import com.example.webstore_android_client.ui.theme.WhiteCustom
import java.time.Instant


private val cardBg        @Composable get() = if (isSystemInDarkTheme()) RowBgDark          else CardWhite
private val headingText   @Composable get() = if (isSystemInDarkTheme()) WhiteCustom         else DarkCustom
private val bodyText      @Composable get() = if (isSystemInDarkTheme()) WhiteCustom.copy(alpha = 0.85f) else DarkCustom.copy(alpha = 0.85f)
private val dividerColor  @Composable get() = if (isSystemInDarkTheme()) QuantityButtonBgDark else StarEmptyLight
private val rowBg         @Composable get() = if (isSystemInDarkTheme()) TextFieldBgDark     else TextFieldBgLight
private val primaryGreen  @Composable get() = if (isSystemInDarkTheme()) MainBgDark           else MainBgLight
private val greenLink     @Composable get() = if (isSystemInDarkTheme()) GreenLight           else MainBgLight

@Composable
fun PurchaseDetailScreen(
    purchaseCode: String,
//    onBack: () -> Unit,
    viewModel: PurchaseDetailViewModel = viewModel(),
    navController:NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val previousEntry = navController.previousBackStackEntry

    val purchaseDate =
        previousEntry
            ?.savedStateHandle
            ?.get<Instant>("purchaseDate")

    val status =
        previousEntry
            ?.savedStateHandle
            ?.get<String>("purchaseStatus")
            ?: ""

    val deliveryAddress =
        previousEntry
            ?.savedStateHandle
            ?.get<String>("deliveryAddress")
            ?: ""

    val totalCostCents =
        previousEntry
            ?.savedStateHandle
            ?.get<Int>("totalCostCents")
            ?: -1

    val shippingFeeCents =
        previousEntry
            ?.savedStateHandle
            ?.get<Int>("shippingFeeCents")
            ?: -1

    LaunchedEffect(purchaseCode) {
        viewModel.loadDetail(purchaseCode,
            purchaseDate,
            status,
            deliveryAddress,
            totalCostCents,
            shippingFeeCents)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onSnackbarMessageConsumed()
        }
    }

    val context = LocalContext.current

    LaunchedEffect(uiState.downloadedInvoiceFile) {
        uiState.downloadedInvoiceFile?.let { file ->
            try {
                val authority = "${context.packageName}.fileprovider"
                val uri = FileProvider.getUriForFile(context, authority, file)

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val chooser = Intent.createChooser(intent, "Преглед на фактура с:")
                context.startActivity(chooser)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Няма инсталирано приложение за преглед на PDF.", Toast.LENGTH_LONG).show()
            } finally {
                viewModel.clearDownloadedInvoice()
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMsg ->
            snackbarHostState.showSnackbar(message = errorMsg)
            viewModel.clearErrorMessage()
        }
    }

    if (uiState.showCancelDialog) {
        ConfirmActionDialog(
            title     = "Заявка за отказване",
            body      = "Сигурни ли сте че искате да откажете тази поръчка?",
            onConfirm = viewModel::onConfirmCancel,
            onDismiss = viewModel::onCancelDialogDismiss,
        )
    }
    if (uiState.showRefundDialog) {
        ConfirmActionDialog(
            title     = "Заявка за връщане",
            body      = "Сигурни ли сте че искате да върнете тази поръчка?",
            onConfirm = viewModel::onConfirmRefund,
            onDismiss = viewModel::onRefundDialogDismiss,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        when {
            uiState.isLoading -> DetailLoadingState()
            uiState.detail != null -> DetailContent(
                detail              = uiState.detail!!,
                onBack              = {navController.navigate("purchase_history")},
                onCancelClicked     = viewModel::onCancelClicked,
                onRefundClicked     = viewModel::onRefundClicked,
                onDownloadInvoice   = viewModel::onDownloadInvoice,
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter),
        ) { data ->
            Snackbar(snackbarData = data)
        }
    }
}

@Composable
private fun DetailLoadingState() {
    val dark   = isSystemInDarkTheme()
    val shimBg = if (dark) SkeletonCardDark else TextFieldBgLight

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(shimBg, RoundedCornerShape(8.dp)),
        ) {
            CircularProgressIndicator(color = if (dark) SkeletonPulseDark else MutedGrey)
        }
    }
}

@Composable
private fun DetailContent(
    detail: PurchaseDetail,
    onBack: () -> Unit,
    onCancelClicked: () -> Unit,
    onRefundClicked: () -> Unit,
    onDownloadInvoice: () -> Unit,
) {
    val status = PurchaseStatus.fromKey(detail.status)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

            DetailHeader(
                detail          = detail,
                status          = status,
                onBack          = onBack,
                onCancelClicked = onCancelClicked,
                onRefundClicked = onRefundClicked,
            )

            HorizontalDivider(color = dividerColor)

            // ----------- Purchase data rows ----------------------
            InfoRow(label = "Дата на поръчка:", value = formatDate(detail.purchaseDate, "dd.MM.yyyy"))
            HorizontalDivider(color = dividerColor)

            InfoRow(label = "Адрес за доставка:", value = detail.deliveryAddress)
            HorizontalDivider(color = dividerColor)

            InfoRow(label = "Име на получател:", value = detail.recipientName)
            HorizontalDivider(color = dividerColor)

            InfoRow(label = "Телефон на получател:", value = detail.recipientPhone)
            HorizontalDivider(color = dividerColor)

            if (detail.deliveryDate!=null && status == PurchaseStatus.DELIVERED) {
                InfoRow(label = "Дата на доставка:", value = formatDate(detail.deliveryDate, "dd.MM.yyyy"))
                HorizontalDivider(color = dividerColor)
            }

            // ------------- Product list -----------------------------------------------
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    text     = "ПРОДУКТИ",
                    fontSize = 11.sp,
                    color    = MutedGrey,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 320.dp),
                ) {
                    detail.products.forEach { product ->
                        OrderProductRow(product = product)
                    }
                }
            }

            HorizontalDivider(color = dividerColor)

            // ---------- Totals + invoice link --------------------------------------
            TotalsSection(
                detail            = detail,
                onDownloadInvoice = onDownloadInvoice,
            )
        }
    }
}

@Composable
private fun DetailHeader(
    detail: PurchaseDetail,
    status: PurchaseStatus,
    onBack: () -> Unit,
    onCancelClicked: () -> Unit,
    onRefundClicked: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) {
                Text(
                    text  = "← Назад",
                    color = MutedGrey,
                    fontSize = 13.sp,
                )
            }
            Text(
                text  = "|",
                color = dividerColor,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Column {
                Text(text = "Поръчка", fontSize = 11.sp, color = MutedGrey)
                Text(
                    text       = detail.purchaseCode,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color      = headingText,
                )
            }
        }

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusChip(status)
            if (status.showDotsMenu) {
                DotsDropdownMenu(
                    status          = status,
                    onCancelClicked = onCancelClicked,
                    onRefundClicked = onRefundClicked,
                )
            }
        }
    }
}

@Composable
private fun DotsDropdownMenu(
    status: PurchaseStatus,
    onCancelClicked: () -> Unit,
    onRefundClicked: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val dark     = isSystemInDarkTheme()
    val menuBg   = if (dark) SkeletonCardDark else CardWhite

    Box {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector        = Icons.Default.MoreVert,
                contentDescription = "Още опции",
                tint               = MutedGrey,
            )
        }
        DropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false },
            containerColor   = menuBg,
        ) {
            if (status.canCancel) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text  = "Откажи поръчка",
                            color = if (dark) OutOfStockRed else ErrorRed,
                            fontSize = 13.sp,
                        )
                    },
                    onClick = {
                        expanded = false
                        onCancelClicked()
                    },
                )
            }
            if (status.canRequestRefund) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text  = "Заяви възстановяване",
                            color = if (dark) OutOfStockRed else ErrorRed,
                            fontSize = 13.sp,
                        )
                    },
                    onClick = {
                        expanded = false
                        onRefundClicked()
                    },
                )
            }
        }
    }
}

@Composable
private fun OrderProductRow(product: OrderProduct) {
    val dark      = isSystemInDarkTheme()
    val rowBorder = if (dark) QuantityButtonBgDark else StarEmptyLight

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(rowBg)
            .border(1.dp, rowBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        ProductImage(
            imageUrl    = product.imageUrl,
            contentDescription = product.productName,
            productCode = product.productCode,
            contentScale        = ContentScale.Fit,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = product.productName,
                fontWeight = FontWeight.Medium,
                fontSize   = 14.sp,
                color      = headingText,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            StarRating(rating = product.rating, reviewCount = product.reviewCount)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text     = "${formatEurCents(product.singlePriceCents)} × ${product.quantity}",
                fontSize = 12.sp,
                color    = MutedGrey,
            )
            Text(
                text       = formatEurCents(product.lineTotalCents),
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = headingText,
            )
        }
    }
}

@Composable
private fun TotalsSection(
    detail: PurchaseDetail,
    onDownloadInvoice: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Bottom,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {

        Button(
            onClick = onDownloadInvoice,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
        ) {
            Text(
                text = "Фактура",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.End) {
            TotalRow(
                label = "Продукти",
                value = formatEurCents(detail.productsTotalCents),
                bold  = false,
            )
            TotalRow(
                label = "Доставка",
                value = if (detail.shippingFeeCents == 0) "Безплатна"
                        else formatEurCents(detail.shippingFeeCents),
                bold  = false,
            )
            HorizontalDivider(color = dividerColor, modifier = Modifier.padding(vertical = 2.dp).width(200.dp))
            TotalRow(
                label = "Общо",
                value = formatEurCents(detail.totalCostCents),
                bold  = true,
            )
        }
    }
}

@Composable
private fun TotalRow(label: String, value: String, bold: Boolean) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.width(200.dp),
    ) {
        Text(
            text       = label,
            fontSize   = 13.sp,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (bold) headingText else MutedGrey,
        )
        Text(
            text       = value,
            fontSize   = 13.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color      = headingText,
        )
    }
}

@Composable
private fun ConfirmActionDialog(
    title: String,
    body: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val green = primaryGreen
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Text(text = body)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Да", color = green, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Не", color = MutedGrey)
            }
        },
    )
}
