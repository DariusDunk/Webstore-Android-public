package com.example.webstore_android_client.ui.reviewPostDialogue

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.webstore_android_client.R
import com.example.webstore_android_client.model.responses.review.ReviewResponse
import com.example.webstore_android_client.ui.productBasic.ProductImage
import com.example.webstore_android_client.ui.theme.*

@Composable
fun ReviewPostDialog(
    isOpen: Boolean,
    onClose: () -> Unit,
    productCode: String,
    productName: String,
    productImage: String?,
    handlePostSuccess: (String) -> Unit,
    mode: String = "create",
    existingReview: ReviewResponse? = null,
    viewModel: ReviewPostViewModel = viewModel()
) {
    if (!isOpen) return

    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    LaunchedEffect(isOpen, mode, existingReview) {
        viewModel.initDialog(mode, existingReview)
    }

    LaunchedEffect(uiState.isSuccess, uiState.error) {
        if (uiState.isSuccess && uiState.successMessage != null) {
            handlePostSuccess(uiState.successMessage!!)
            viewModel.consumeEvents()
        }
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
            viewModel.consumeEvents()
        }
    }

    val dialogBg = if (isDark) RowBgDark else CardWhite
    val textColor = if (isDark) WhiteCustom else Color.Black
    val mainBgColor = if (isDark) MainBgDark else MainBgLight

    Dialog(
        onDismissRequest = {
            if (!uiState.isSubmitting && !uiState.isLoadingExistingReview) {
                onClose()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(dialogBg)
                .padding(24.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (mode == "update") "Редакция на ревю за:" else "Добавяне на ревю за:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    if (productImage != null) {
                        ProductImage(
                            productCode = productCode,
                            imageUrl = productImage,
                            contentDescription = productName,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) AppBackgroundDark else ImageBgGrey)
                                .border(
                                    1.dp,
                                    MutedGrey.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = productName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = "Как оценяваш продукта?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    for (i in 1..5) {
                        val isFilled = i <= uiState.rating
                        Text(
                            text = if (isFilled) "★" else "☆",
                            fontSize = 40.sp,
                            color = if (isFilled) GoldStar else (if (isDark) StarEmptyDark else StarEmptyLight),
                            modifier = Modifier
                                .clickable(enabled = !uiState.isSubmitting) {
                                    viewModel.updateRating(
                                        i
                                    )
                                }
                                .padding(horizontal = 4.dp)
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Коментар (незадължително):",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = uiState.reviewText,
                        onValueChange = { viewModel.updateReviewText(it) },
                        placeholder = {
                            Text(
                                text = "Какво е впечатлението ти от продукта?\nБи ли го препоръчал и защо?",
                                color = MutedGrey,
                                fontSize = 14.sp
                            )
                        },
                        enabled = !uiState.isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 240.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isDark) TextFieldBgDark else TextFieldBgLight,
                            unfocusedContainerColor = if (isDark) TextFieldBgDark else TextFieldBgLight,
                            focusedBorderColor = LinkBlueLight,
                            unfocusedBorderColor = MutedGrey.copy(alpha = 0.5f),
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(
                        text = "${uiState.reviewText.length}/500",
                        fontSize = 12.sp,
                        color = if (uiState.reviewText.length >= 500) ErrorRed else MutedGrey,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp, bottom = 24.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.submitReview(productCode) },
                        enabled = !uiState.isSubmitDisabled,
                        colors = ButtonDefaults.buttonColors(containerColor = mainBgColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .padding(end = 16.dp)
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp)
                            )
                        }
                        Text(
                            text = if (mode == "update") stringResource(R.string.UPDATE_REVIEW_BUTTON) else stringResource(
                                R.string.POST_REVIEW_BUTTON
                            ),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                    }

                    TextButton(
                        onClick = onClose,
                        enabled = !uiState.isSubmitting
                    ) {
                        Text(
                            text = "Отказ",
                            fontSize = 16.sp,
                            color = textColor
                        )
                    }
                }
            }
            Text(
                text = "×",
                fontSize = 28.sp,
                color = MutedGrey,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable(enabled = !uiState.isSubmitting) { onClose() }
                    .padding(8.dp)
            )
        }
    }
}
