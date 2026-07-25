package com.example.webstore_android_client.ui.mainMenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.utils.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class TopSearchBarViewModelUIState(
    val query: String = "",

    val isImageSearch: Boolean = false,
    val openSuggestions: Boolean = false,

    val suggestions:List<String> = emptyList()

)


class TopSearchBarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TopSearchBarViewModelUIState())
    val uiState: StateFlow<TopSearchBarViewModelUIState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    fun selectSuggestion(suggestion: String) {
        _uiState.update {
            it.copy(
                query = suggestion,
                openSuggestions = false
            )
        }
    }

    fun dismissSuggestions() {
        _uiState.update { it.copy(openSuggestions = false) }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }

        searchJob?.cancel()

        if (newQuery.length < 3) {
            _uiState.update { it.copy(suggestions = emptyList()) }
            return
        }

        searchJob = viewModelScope.launch {

            delay(300)

            when (val response = ApiProvider.productRepository.getSuggestions(newQuery)) {
                is ApiResult.Failure -> {
                    _uiState.update { it.copy(suggestions = emptyList()) }
                    println("Fetching suggestions failed: ${response.error.detail}")
                }
                is ApiResult.NetworkError -> {
                    _uiState.update { it.copy(suggestions = emptyList()) }
                    println("Fetching suggestions network error: ${response.exception.message}")
                }
                is ApiResult.Success -> {
                    _uiState.update { it.copy(suggestions = response.data, openSuggestions = response.data.isNotEmpty()) }

                }
            }
        }
    }

}