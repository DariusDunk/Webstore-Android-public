package com.example.webstore_android_client.ui.productBrowsing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SheetState
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.webstore_android_client.ui.theme.MainBgDark
import com.example.webstore_android_client.ui.theme.MainBgLight
import com.example.webstore_android_client.ui.theme.MutedGrey
import com.example.webstore_android_client.ui.theme.PageBgDark
import com.example.webstore_android_client.ui.theme.PageBgLight
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    isVisible: Boolean,
    categoryFilters: CategoryFiltersData?,
    currentFilters: ProductFilters,
    onApply: (ProductFilters) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible || categoryFilters == null) return

    val isDark       = isSystemInDarkTheme()
    val sheetBg      = if (isDark) PageBgDark  else PageBgLight
    val submitBg     = if (isDark) MainBgDark   else MainBgLight
    val textColor    = if (isDark) Color.White else Color.Black
    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var draftFilters by remember(currentFilters) { mutableStateOf(currentFilters) }

    ModalBottomSheet(
        onDismissRequest  = onDismiss,
        sheetState        = sheetState,
        containerColor    = sheetBg,
        shape             = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // ---------------------------------- Header ----------------------------------
            Text(
                text       = "Филтри",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = textColor,
                modifier   = Modifier.padding(vertical = 16.dp)
            )

            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // ---------------------------------- Scrollable Content Container ----------------------------------
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                val minPriceAvailable = categoryFilters.priceLowestStotinki / 100f
                val maxPriceAvailable = categoryFilters.priceHighestStotinki / 100f

                if (minPriceAvailable < maxPriceAvailable) {
                    FilterSectionTitle("Ценови диапазон", textColor)

                    val currentMin = (draftFilters.minPriceStotinki?.div(100f)) ?: minPriceAvailable
                    val currentMax = (draftFilters.maxPriceStotinki?.div(100f)) ?: maxPriceAvailable

                    var sliderPosition by remember(currentMin, currentMax) {
                        mutableStateOf(currentMin..currentMax)
                    }

                    Text(
                        text = "От €${"%.2f".format(sliderPosition.start)} до €${"%.2f".format(sliderPosition.endInclusive)}",
                        color = MutedGrey,
                        fontSize = 14.sp
                    )

                    RangeSlider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        valueRange = minPriceAvailable..maxPriceAvailable,
                        onValueChangeFinished = {
                            draftFilters = draftFilters.copy(
                                minPriceStotinki = (sliderPosition.start * 100).roundToInt(),
                                maxPriceStotinki = (sliderPosition.endInclusive * 100).roundToInt()
                            )
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = submitBg,
                            activeTrackColor = submitBg
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MutedGrey.copy(alpha = 0.2f))
                }

                if (categoryFilters.manufacturers.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    FilterSectionTitle("Производител", textColor)

                    categoryFilters.manufacturers.forEach { manufacturer ->
                        FilterCheckboxRow(
                            text = manufacturer,
                            textColor = textColor,
                            checkedColor = submitBg,
                            isChecked = draftFilters.manufacturers.contains(manufacturer),
                            onCheckedChange = { checked ->
                                val updatedMfrs = if (checked) {
                                    draftFilters.manufacturers + manufacturer
                                } else {
                                    draftFilters.manufacturers - manufacturer
                                }
                                draftFilters = draftFilters.copy(manufacturers = updatedMfrs)
                            }
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MutedGrey.copy(alpha = 0.2f))
                }

                Spacer(Modifier.height(16.dp))
                FilterSectionTitle("Минимален Рейтинг", textColor)
                (categoryFilters.ratings).forEach { rating ->
                    FilterCheckboxRow(
                        text = "$rating",
                        textColor = textColor,
                        checkedColor = submitBg,
                        isChecked = draftFilters.minRating == rating,
                        onCheckedChange = { checked ->
                            val newRating = if (checked) rating else null
                            draftFilters = draftFilters.copy(minRating = newRating)
                        }
                    )
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MutedGrey.copy(alpha = 0.2f))

                if (categoryFilters.attributes.isNotEmpty()) {
                    categoryFilters.attributes.forEach { attr ->
                        Spacer(Modifier.height(16.dp))

                        val titleText = if (!attr.measurementUnit.isNullOrBlank()) {
                            "${attr.attributeName} (${attr.measurementUnit})"
                        } else {
                            attr.attributeName
                        }

                        FilterSectionTitle(titleText, textColor)

                        attr.options.forEach { option ->
                            val activeOptions = draftFilters.attributes[attr.attributeName] ?: emptyList()
                            FilterCheckboxRow(
                                text = option,
                                textColor = textColor,
                                checkedColor = submitBg,
                                isChecked = activeOptions.contains(option),
                                onCheckedChange = { checked ->
                                    val updatedOptions = if (checked) {
                                        activeOptions + option
                                    } else {
                                        activeOptions - option
                                    }

                                    val newMap = draftFilters.attributes.toMutableMap().apply {
                                        if (updatedOptions.isEmpty()) {
                                            remove(attr.attributeName)
                                        } else {
                                            put(attr.attributeName, updatedOptions)
                                        }
                                    }
                                    draftFilters = draftFilters.copy(attributes = newMap)
                                }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---------------------------------- Action buttons ----------------------------------
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = { onReset(); onDismiss() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Изчисти", color = textColor)
                }

                Button(
                    onClick  = {
                        onApply(draftFilters)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = submitBg,
                        contentColor   = Color.White
                    )
                ) {
                    Text("Приложи")
                }
            }
        }
    }
}

// ---------------------------------- UI Helper Components ----------------------------------

@Composable
private fun FilterSectionTitle(title: String, textColor: Color) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun FilterCheckboxRow(
    text: String,
    textColor: Color,
    checkedColor: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = checkedColor)
        )
        Text(
            text = text,
            color = textColor,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}