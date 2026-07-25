package com.example.webstore_android_client.ui.userProfile.purchaseHistory


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.model.responses.purchase.CompactPurchaseResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PurchaseListUiState(
    val compactPurchaseResponses: List<CompactPurchaseResponse> = emptyList(),
    val currentPage: Int = -1,
    val totalPages: Int = 1,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class PurchaseListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseListUiState())
    val uiState: StateFlow<PurchaseListUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    init {
//        fetchPurchases(page = 0)
        loadMore()
    }


    fun loadMore() {
        val state = _uiState.value

        if (state.isLoadingMore || !state.hasMore || (state.isLoading && state.currentPage != -1)) return

        val nextPage = state.currentPage + 1

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            if (nextPage == 0) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
            }

            try {
                when (val response = ApiProvider.purchaseRepository.getPurchaseHistoryPage(nextPage)) {
                    is ApiResult.Failure -> {
                        val errorText = response.error.detail ?: "Имаше проблем с извличането на покупките"
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                errorMessage = errorText,
                            )
                        }
                    }
                    is ApiResult.NetworkError -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                errorMessage = "Имаше проблем с извличането на покупките",
                            )
                        }
                        println("\n----------------------------------\n" +
                                "Error fetching purchase history page: ${response.exception}\n" +
                                " \n----------------------------------\n")
                    }
                    is ApiResult.Success -> {
                        val responseBody = response.data
                        _uiState.update { current ->
                            val newContent = if (nextPage == 0) {
                                responseBody.content
                            } else {
                                current.compactPurchaseResponses + responseBody.content
                            }

                            current.copy(
                                compactPurchaseResponses = newContent,
                                currentPage = nextPage,
                                totalPages = responseBody.totalPages,
                                hasMore = nextPage < responseBody.totalPages - 1,
                                isLoading = false,
                                isLoadingMore = false
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                println("\n----------------------------------\n" +
                        "Exception in fetching purchase history page: ${e.stackTrace}\n" +
                        " \n----------------------------------\n")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = "Имаше проблем с извличането на покупките",
                    )
                }
            }
        }
    }

    fun onErrorConsumed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
