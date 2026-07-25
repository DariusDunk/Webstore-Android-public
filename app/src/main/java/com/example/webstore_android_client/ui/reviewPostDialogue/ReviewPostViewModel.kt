package com.example.webstore_android_client.ui.reviewPostDialogue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.repositories.ProductRepository
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.model.requests.review.ReviewPostRequest
import com.example.webstore_android_client.model.responses.review.ReviewResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewPostUiState(
    val mode: String = "create",
    val fetchedRating: Int = 0,
    val fetchedReviewText: String = "",
    val rating: Int = 0,
    val reviewText: String = "",
    val isSubmitting: Boolean = false,
    val isLoadingExistingReview: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val successMessage: String? = null
) {
    val isSubmitDisabled: Boolean
        get() = isSubmitting || isLoadingExistingReview || rating == 0 || reviewText.length > 500 ||
                (mode == "update" && rating == fetchedRating && reviewText == fetchedReviewText)
}

class ReviewPostViewModel(
    private val repository: ProductRepository = ApiProvider.productRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewPostUiState())
    val uiState: StateFlow<ReviewPostUiState> = _uiState.asStateFlow()

    fun initDialog(mode: String, existingReview: ReviewResponse?) {
        if (mode == "update" && existingReview != null) {
            val r = (existingReview.rating / 10)
            _uiState.update {
                it.copy(
                    mode = "update",
                    fetchedRating = r,
                    fetchedReviewText = existingReview.reviewText,
                    rating = r,
                    reviewText = existingReview.reviewText,
                    error = null,
                    isSuccess = false,
                    isSubmitting = false
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    mode = "create",
                    fetchedRating = 0,
                    fetchedReviewText = "",
                    rating = 0,
                    reviewText = "",
                    error = null,
                    isSuccess = false,
                    isSubmitting = false
                )
            }
        }
    }

    fun updateRating(r: Int) {
        _uiState.update { it.copy(rating = r) }
    }

    fun updateReviewText(text: String) {
        if (text.length <= 500) {
            _uiState.update { it.copy(reviewText = text) }
        }
    }

    fun submitReview(productCode: String) {
        val state = _uiState.value
        if (state.isSubmitDisabled) return

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            val request = ReviewPostRequest(
                rating = state.rating.toShort() ,
                reviewText = state.reviewText.trim(),
                productCode = productCode
            )

            val response = if (state.mode == "update") {
                repository.updateReview(request)
            } else {
                repository.postReview(request)
            }

            when (response) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isSuccess = true,
                            successMessage = if (state.mode == "update") "Ревюто е обновено успешно!" else "Ревюто е добавено успешно!"
                        )
                    }
                }
                is ApiResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = if(response.error.detail=="")
                                "Възникна проблем при обработката на ревюто"
                            else
                                response.error.detail
                        )
                    }
                }
                is ApiResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = response.exception.message ?: "Грешка в мрежата"
                        )
                    }
                }
            }
        }
    }

    fun consumeEvents() {
        _uiState.update { it.copy(isSuccess = false, successMessage = null, error = null) }
    }
}
