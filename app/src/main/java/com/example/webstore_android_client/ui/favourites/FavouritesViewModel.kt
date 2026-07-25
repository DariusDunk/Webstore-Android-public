package com.example.webstore_android_client.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.model.localDTOs.cart.CartSummaryData
import com.example.webstore_android_client.model.requests.cart.CartItemRequest
import com.example.webstore_android_client.model.requests.product.FavouriteBatchDeleteWithRefetchRequest
import com.example.webstore_android_client.model.requests.product.FavouriteDeleteWithRefetchRequest
import com.example.webstore_android_client.model.responses.page.PageResponse
import com.example.webstore_android_client.model.responses.product.CompactProductResponse
import com.example.webstore_android_client.repositories.RepositoryProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ---------------- UI State ---------------------------------------------------------------

data class FavouritesUiState(
    // ------------- Product list   ---------------------------------------
    val products: List<CompactProductResponse> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = -1,
    val totalElements: Long = 0L,

    // ------- Multi-select   -------------------------------
    val selectedProductCodes: Set<String> = emptySet(),

    // ------- Per-item mutation overlay  -------------------

    val mutatingProductCodes: Set<String> = emptySet(),

    // ------- Dialogs ---------------------------------------------------------------
    val showDeleteConfirmDialog: Boolean = false,

    // ------- Error ----------------------------------------------------------------------
    val error: String? = null
) {
    val isMutating: Boolean get() = mutatingProductCodes.isNotEmpty()

    val allSelected: Boolean
        get() = products.isNotEmpty() &&
                products.none { it.productCode !in selectedProductCodes && it.productCode !in mutatingProductCodes }

    val selectedCount: Int get() = selectedProductCodes.size
}

// -------------- One-shot events ---------------------------------------------------------─

sealed class FavouritesEvent {
    data class ShowToast(val message: String) : FavouritesEvent()
    data class ShowError(val title: String, val detail: String) : FavouritesEvent()
}
class FavouritesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FavouritesUiState())
    val uiState: StateFlow<FavouritesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FavouritesEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<FavouritesEvent> = _events.asSharedFlow()

    private var fetchJob: Job? = null

    init { refresh() }

    fun refresh() {
        fetchJob?.cancel()
        _uiState.update {
            it.copy(
                isLoading          = true,
                products           = emptyList(),
                currentPage        = -1,
                hasMore            = true,
                selectedProductCodes = emptySet(),
                error              = null
            )
        }
        fetchJob = viewModelScope.launch { doFetch(page = 0, append = false) }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || state.isLoading || !state.hasMore) return
        val nextPage = state.currentPage + 1
        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch { doFetch(page = nextPage, append = true) }
    }

    private suspend fun doFetch(page: Int, append: Boolean) {

        val response: ApiResult<PageResponse<CompactProductResponse>> = ApiProvider.customerRepository.getFavouritesPaged(page)

        when (response) {
            is ApiResult.Success -> {
                val page = response.data
                _uiState.update { current ->
                    current.copy(
                        isLoading     = false,
                        isLoadingMore = false,
                        products      = if (append) current.products + page.content else page.content,
                        currentPage   = page.pageNumber,
                        hasMore       = !page.last,
                        totalElements = page.totalElements,
                        error         = null
                    )
                }
            }
            is ApiResult.Failure -> _uiState.update {
                it.copy(
                    isLoading     = false,
                    isLoadingMore = false,
                    error         = response.error.detail ?: "Грешка при зареждане на любими"
                )
            }
            is ApiResult.NetworkError -> _uiState.update {
                it.copy(
                    isLoading     = false,
                    isLoadingMore = false,
                    error         = response.exception.message ?: "Грешка в мрежата"
                )
            }
        }
    }

    fun toggleSelect(productCode: String) {
        _uiState.update { current ->
            val updated = current.selectedProductCodes.toMutableSet()
            if (productCode in updated) updated.remove(productCode) else updated.add(productCode)
            current.copy(selectedProductCodes = updated)
        }
    }

    fun selectAll() {
        _uiState.update { current ->
            val selectable = current.products
                .filter { it.productCode !in current.mutatingProductCodes }
                .map { it.productCode }
                .toSet()
            current.copy(selectedProductCodes = selectable)
        }
    }

    fun deselectAll() {
        _uiState.update { it.copy(selectedProductCodes = emptySet()) }
    }

    fun addToCart(product: CompactProductResponse) {
        if (product.productCode in _uiState.value.mutatingProductCodes) return

        viewModelScope.launch {
            markMutating(product.productCode)
            try {

                   val result = ApiProvider.cartRepository.addOrRemoveFromCart(
                       CartItemRequest(
                           productCode = product.productCode,
                          doIncrement = true
                       )
                   )
                   when (result) {
                       is ApiResult.Success -> {
                           RepositoryProvider.cartSummaryDataRepository.update(
                               CartSummaryData(
                                   result.data.cartSummary.cartTotalCoins,
                                   result.data.cartSummary.cartQuantity
                               )
                           )
                           _events.emit(FavouritesEvent.ShowToast(result.data.message))
                       }
                       is ApiResult.Failure ->
                           _events.emit(FavouritesEvent.ShowError("Проблем с добавянето", result.error.detail ?: ""))
                       is ApiResult.NetworkError ->
                           _events.emit(FavouritesEvent.ShowError("Проблем с добавянето", result.exception.message ?: ""))
                   }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _events.emit(FavouritesEvent.ShowError("Проблем с добавянето", e.message ?: ""))
            } finally {
                unmarkMutating(product.productCode)
            }
        }
    }

    fun deleteItem(product: CompactProductResponse) {
        if (product.productCode in _uiState.value.mutatingProductCodes) return

        viewModelScope.launch {
            markMutating(product.productCode)
            try {
                val requestBody =
                    FavouriteDeleteWithRefetchRequest(uiState.value.currentPage,product.productCode)
                   val result = ApiProvider.customerRepository.removeFromFavWithRefetch(requestBody)
                   when (result) {
                       is ApiResult.Success -> {
                           _uiState.update { current ->
                               current.copy(
                                   products      = current.products.filter { it.productCode != product.productCode }, //todo moje da e po-dobre optimistic update da se pravi tuk
//                                   products      = newProducts,
                                   totalElements = (current.totalElements - 1).coerceAtLeast(0),
//                                   totalElements = newProducts.size.toLong(),
                                   selectedProductCodes = current.selectedProductCodes - product.productCode
                               )
                           }
                           _events.emit(FavouritesEvent.ShowToast("Успешно премахнат"))
                       }
                       is ApiResult.Failure ->
                           _events.emit(FavouritesEvent.ShowError("Проблем с премахването", result.error.detail ?: ""))
                       is ApiResult.NetworkError ->
                           _events.emit(FavouritesEvent.ShowError("Проблем с премахването", result.exception.message ?: ""))
                   }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _events.emit(FavouritesEvent.ShowError("Проблем с премахването", e.message ?: ""))
            } finally {
                unmarkMutating(product.productCode)
            }
        }
    }


    fun addSelectedToCart() {
        val codes = _uiState.value.selectedProductCodes.toList()
        if (codes.isEmpty()) return

        RepositoryProvider.customerDataRepository.userState.value != null

        viewModelScope.launch {
            markMutating(codes.toSet())
            try {

                   val result = ApiProvider.cartRepository.addBatchToCart(
                       uiState.value.selectedProductCodes.toList()
                   )
                   when (result) {
                       is ApiResult.Success -> {
                           val data = result.data

                           val cartSummary = data.cartSummaryResponse
                           val messageResponse = data.messageResponse
                           RepositoryProvider.cartSummaryDataRepository.update(
                               CartSummaryData(cartSummary.cartTotalCoins, cartSummary.cartQuantity)
                           )
                           if (messageResponse.type == "success") {
                               _events.emit(FavouritesEvent.ShowToast(messageResponse.detail ?: ""))
                           } else if (messageResponse.type == "partial_success") {
                               _events.emit(FavouritesEvent.ShowError(messageResponse.title ?: "Частичен успех", "Недостатъчна наличност "))// todo tova da e s dialog
                           }
                           deselectAll()
                       }
                       is ApiResult.Failure ->
                           _events.emit(FavouritesEvent.ShowError("Неуспешно добавяне", result.error.detail ?: ""))
                       is ApiResult.NetworkError -> {

                          println("\n----------------------------------\n" +
                                   "Batch add to cart exception: ${result.exception.message}\n" +
                                   " \n----------------------------------\n")

                           _events.emit(
                               FavouritesEvent.ShowError(
                                   "Неуспешно добавяне",
                                   result.exception.message ?: ""
                               )
                           )
                       }
                   }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _events.emit(FavouritesEvent.ShowError("Неуспешно добавяне", e.message ?: ""))
            } finally {
                unmarkMutating(codes.toSet())
            }
        }
    }

    fun requestDeleteSelected() {
        if (_uiState.value.selectedProductCodes.isEmpty()) return
        _uiState.update { it.copy(showDeleteConfirmDialog = true) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false) }
    }

    fun executeDeleteSelected() {
        val codes = _uiState.value.selectedProductCodes.toList()
        val currentPage = uiState.value.currentPage
        _uiState.update { it.copy(showDeleteConfirmDialog = false) }
        if (codes.isEmpty()) return

        viewModelScope.launch {
            markMutating(codes.toSet())
            try {
                   val response = ApiProvider.customerRepository.removeBatchedFavouritesWithRefetch(
                       FavouriteBatchDeleteWithRefetchRequest(productCodes = codes, currentPage = currentPage)
                   )
                   when (response) {
                       is ApiResult.Success -> {

                           _uiState.update { current ->
                               val removed = codes.toSet()
                               current.copy(
                                   products      = current.products.filter { it.productCode !in removed },
//                                   products      = newProducts,
                                   totalElements = (current.totalElements - codes.size).coerceAtLeast(0),
//                                   totalElements = totalElements.toLong(),
                                   selectedProductCodes = emptySet()
                               )
                           }
                           _events.emit(FavouritesEvent.ShowToast("Продуктите са успешно премахнати"))
                       }
                       is ApiResult.Failure ->
                           _events.emit(FavouritesEvent.ShowError("Неуспешно премахване", response.error.detail ?: ""))
                       is ApiResult.NetworkError ->
                           _events.emit(FavouritesEvent.ShowError("Неуспешно премахване", response.exception.message ?: ""))
                   }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _events.emit(FavouritesEvent.ShowError("Неуспешно премахване", e.message ?: ""))
            } finally {
                unmarkMutating(codes.toSet())
            }
        }
    }

    private fun markMutating(code: String) {
        _uiState.update { it.copy(mutatingProductCodes = it.mutatingProductCodes + code) }
    }

    private fun markMutating(codes: Set<String>) {
        _uiState.update { it.copy(mutatingProductCodes = it.mutatingProductCodes + codes) }
    }

    private fun unmarkMutating(code: String) {
        _uiState.update { it.copy(mutatingProductCodes = it.mutatingProductCodes - code) }
    }

    private fun unmarkMutating(codes: Set<String>) {
        _uiState.update { it.copy(mutatingProductCodes = it.mutatingProductCodes - codes) }
    }
}