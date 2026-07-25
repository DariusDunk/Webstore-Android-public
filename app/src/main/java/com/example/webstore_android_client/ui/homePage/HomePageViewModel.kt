package com.example.webstore_android_client.ui.homePage


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.model.responses.product.HomePageRowResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
data class HomePageUiState(
    val rows: List<HomePageRowResponse> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomePageViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomePageUiState())
    val uiState: StateFlow<HomePageUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    init {
        fetchHomePage()
    }

    fun fetchHomePage() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
//                val rows = ApiProvider.productService.getHomePage()
                when(val response = ApiProvider.productRepository.getHomePage()) {
                    is ApiResult.Success->
                    _uiState.update { it.copy(isLoading = false, rows = response.data) }

                    is ApiResult.Failure ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = response.error.detail
                            )
                        }
                    is ApiResult.NetworkError ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = response.exception.message ?: "Неуспешна заявка"
                            )
                        }
                }
            } catch (e: CancellationException) {

                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Грешка при зареждане"
                    )
                }
            }
        }
    }
}
