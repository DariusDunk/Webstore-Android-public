package com.example.webstore_android_client.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.repositories.CategoryRepository
import com.example.webstore_android_client.api.utils.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryUiState(
    val isLoading: Boolean = true,
    val categories: List<String> = emptyList(),
    val error: String? = null
)

class CategoryViewModel(
    private val categoryRepository: CategoryRepository = ApiProvider.categoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        fetchCategories()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val response = categoryRepository.getNames()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            categories = response.data
                        )
                    }
                }
                is ApiResult.Failure -> {
                    val errorText = response.error.detail.ifBlank {
                        "Неуспешно зареждане на категории"
                    }

                    println("\n----------------------------------\n" +
                            "Failure text in fetchCategories: $errorText " +
                            " \n----------------------------------\n")

                    _uiState.update {
                        it.copy(isLoading = false, error = errorText)
                    }
                }
                is ApiResult.NetworkError -> {
                    println("\n----------------------------------\n" +
                            "Network error in fetchCategories: ${response.exception.message} " +
                            " \n----------------------------------\n")

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.exception.message ?: "Мрежова грешка"
                        )
                    }
                }
            }
        }
    }
}