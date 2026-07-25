package com.example.webstore_android_client.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.model.localDTOs.cart.CartSummaryData
import com.example.webstore_android_client.model.requests.cart.CartItemRequest
import com.example.webstore_android_client.model.responses.cart.CartEntryResponse
import com.example.webstore_android_client.repositories.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val isLoading: Boolean = true,
    val entries: List<CartEntryResponse> = emptyList(),
    val selectedCodes: Set<String> = emptySet(),
    val mutatingCodes: Set<String> = emptySet(),
    val showDeleteDialog: Boolean = false,
    val toastMessage: String? = null
) {
    val selectableEntries = entries.filter { it.product.isInStock }
    val isAllSelected = selectableEntries.isNotEmpty() && selectedCodes.size == selectableEntries.size
    val selectedEntries = entries.filter { selectedCodes.contains(it.product.productCode) }
    val totalSaleCostCents = selectedEntries.sumOf { it.currentPriceStotinki * it.quantity }
    val totalOriginalCostCents = selectedEntries.sumOf { it.product.originalPriceStotinki * it.quantity }
    val isDiscountMode = selectedEntries.any { it.isDiscounted }
    val hasOverStockSelected = selectedEntries.any { it.isOverStock }
    
    val deliveryCostCents = if (selectedEntries.isEmpty()) 0 else if (totalSaleCostCents > 10000) 0 else 1000
    val grandSaleTotalCents = totalSaleCostCents + deliveryCostCents
    val grandOriginalTotalCents = totalOriginalCostCents + deliveryCostCents

}

class CartViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchCart()
    }

    private fun fetchCart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when(val response = ApiProvider.cartRepository.getCart()) {
                is ApiResult.Failure -> {
                    val errorText = response.error.detail.ifEmpty { "Неуспешно извличане на количката" }

                    _uiState.update { it.copy(toastMessage = errorText) }

                }
                is ApiResult.NetworkError -> {
                   println("\n----------------------------------\n" +
                            "Cart fetch request failed: ${response.exception.message}\n" +
                            " \n----------------------------------\n")
                    _uiState.update { it.copy(toastMessage = "Неуспешно извличане на количката") }
                }
                is ApiResult.Success -> {
                    val responseData= response.data
                    val entries = responseData.products
                    val cartState = responseData.cartSummaryResponse
                    RepositoryProvider.cartSummaryDataRepository.update(CartSummaryData(cartState.cartTotalCoins, cartState.cartQuantity))
                    _uiState.update { it.copy(entries = entries) }
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun toggleSelection(productCode: String) {
        _uiState.update { state ->
            val newSelection = if (state.selectedCodes.contains(productCode)) {
                state.selectedCodes - productCode
            } else {
                state.selectedCodes + productCode
            }
            state.copy(selectedCodes = newSelection)
        }
    }

    fun toggleSelectAll(selectAll: Boolean) {
        _uiState.update { state ->
            if (selectAll) {
                val allSelectable = state.selectableEntries
                    .filter { !state.mutatingCodes.contains(it.product.productCode) }
                    .map { it.product.productCode }
                    .toSet()
                state.copy(selectedCodes = allSelectable)
            } else {
                state.copy(selectedCodes = emptySet())
            }
        }
    }

    fun handleQuantityChange(productCode: String, increment: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(mutatingCodes = it.mutatingCodes + productCode) }
            val response = ApiProvider.cartRepository.addOrRemoveFromCart(CartItemRequest(productCode, increment))
            when (response) {
                is ApiResult.Failure -> {
                    val errorText = response.error.detail.ifEmpty { "Неуспешна промяна на количество" }
                    _uiState.update { it.copy(toastMessage = errorText) }

                }
                is ApiResult.NetworkError -> {
                    println("\n----------------------------------\n" +
                            "Cart quantity change request failed: ${response.exception.message}\n" +
                            " \n----------------------------------\n")

                    _uiState.update { it.copy(toastMessage = "Неуспешна промяна на количество") }

                }
                is ApiResult.Success -> {

                    val responseData = response.data
                    val message = responseData.message
                    val cartSummary = responseData.cartSummary

                    RepositoryProvider.cartSummaryDataRepository.update(
                        CartSummaryData(cartSummary.cartTotalCoins, cartSummary.cartQuantity)
                    )
                    _uiState.update { it.copy(toastMessage = message) }

                    val isSingleQuantity = uiState.value.entries
                        .find { it.product.productCode == productCode }
                        ?.quantity == 1

                    if (!increment && isSingleQuantity) {
                        _uiState.update { it.copy(selectedCodes = it.selectedCodes - productCode) }
                    }

                }
            }

            _uiState.update { it.copy(mutatingCodes = it.mutatingCodes - productCode) }

            if (response is ApiResult.Success) {
                fetchCart()
            }
        }
    }

    fun handleDeleteItem(productCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(mutatingCodes = it.mutatingCodes + productCode) }

            when (val response = ApiProvider.cartRepository.removeFromCart(productCode)) {
                is ApiResult.Failure -> {
                    val errorText = response.error.detail.ifEmpty { "Неуспешно премахване на продукт" }
                    _uiState.update { it.copy(toastMessage = errorText) }
                }
                is ApiResult.NetworkError ->
                {
                    println("\n----------------------------------\n" +
                            "Cart delete request failed: ${response.exception.message}\n" +
                            " \n----------------------------------\n")

                    _uiState.update { it.copy(toastMessage = "Неуспешно премахване на продукт") }

                }
                is ApiResult.Success-> {
                    val responseData = response.data
                    val newProducts = responseData.products
                    val cartSummary = responseData.cartSummaryResponse

                    RepositoryProvider.cartSummaryDataRepository.update(
                        CartSummaryData(cartSummary.cartTotalCoins, cartSummary.cartQuantity)
                    )
                    _uiState.update { it.copy(entries = newProducts) }

                    if (newProducts.isEmpty())
                    {
                        _uiState.update { it.copy(selectedCodes = emptySet()) }
                    }
                    else
                    {
                        _uiState.update { it.copy(selectedCodes = it.selectedCodes - productCode) }
                    }
                }
            }

            _uiState.update { it.copy(mutatingCodes = it.mutatingCodes - productCode) }
        }
    }

    // --- Batched Deletion Dialog Logic ---
    fun promptBatchDelete() {
        if (_uiState.value.selectedCodes.isNotEmpty()) {
            _uiState.update { it.copy(showDeleteDialog = true) }
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun executeBatchDelete() {
        val selected = _uiState.value.selectedCodes.toList()
        dismissDeleteDialog()
        
        viewModelScope.launch {
            _uiState.update { it.copy(mutatingCodes = it.mutatingCodes + selected) }

            when (val response = ApiProvider.cartRepository.removeFromCart(selected)) {
                is ApiResult.Failure -> {
                    val errorText = response.error.detail.ifEmpty { "Неуспешно премахване на продукти" }
                    _uiState.update { it.copy(toastMessage = errorText) }
                }
                is ApiResult.NetworkError -> {
                    println("\n----------------------------------\n" +
                            "Cart batch delete request failed: ${response.exception.message}\n" +
                            " \n----------------------------------\n")
                    _uiState.update { it.copy(toastMessage = "Неуспешно премахване на продукти") }
                }
                is ApiResult.Success  ->
                {
                    val responseData = response.data
                    val newProducts = responseData.products
                    val cartSummary = responseData.cartSummaryResponse

                    RepositoryProvider.cartSummaryDataRepository.update(
                        CartSummaryData(cartSummary.cartTotalCoins, cartSummary.cartQuantity)
                    )

                    _uiState.update { it.copy(entries = newProducts) }

                    if (newProducts.isEmpty())

                    {
                        _uiState.update { it.copy(selectedCodes = emptySet()) }
                    }
                    else
                    {
                        _uiState.update { it.copy(selectedCodes = it.selectedCodes - selected.toSet()) }
                    }

                }
            }

            _uiState.update { it.copy(mutatingCodes = it.mutatingCodes - selected.toSet()) }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}