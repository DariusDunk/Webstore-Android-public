package com.example.webstore_android_client.ui.detailedProduct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.repositories.CartRepository
import com.example.webstore_android_client.api.repositories.ProductRepository
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.model.localDTOs.cart.CartSummaryData
import com.example.webstore_android_client.model.requests.cart.CartQuantityRequest
import com.example.webstore_android_client.model.requests.review.ReviewDeleteRequest
import com.example.webstore_android_client.model.requests.review.ReviewsOfProductRequest
import com.example.webstore_android_client.model.responses.cart.CartOperationResponse
import com.example.webstore_android_client.model.responses.product.DetailedProductResponse
import com.example.webstore_android_client.model.responses.product.RatingOverviewResponse
import com.example.webstore_android_client.model.responses.review.CustomerDetailsForReview
import com.example.webstore_android_client.model.responses.review.ReviewResponse
import com.example.webstore_android_client.repositories.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

data class ProductDetailsUiState(
    val isProductLoading: Boolean = true,
    val isReviewsLoading: Boolean = false,
    val productData: DetailedProductResponse? = null,
    val ratingOverview: List<RatingOverviewResponse> = emptyList(),
    val averageRating: Float = 0f,
    val isInFavourites: Boolean = false,

    // Reviews Pagination State
    val reviews: List<ReviewResponse> = emptyList(),
    val currentPage: Int = 0,
    val isLastPage: Boolean = false,
    val totalReviews: Long = 0,

    // Filters
    val sortOrder: String = "NEWEST",
    val filterRating: Short = 0,
    val verifiedOnly: Boolean = false,

    // User Actions
    val quantity: Short = 1.toShort(),
    val error: String? = null,
    val toastMessage: String? = null,
    val isAddingToCart: Boolean = false,
    val isMutatingFavourites: Boolean = false,


    // Review Dialog States
    val isCheckingReview: Boolean = false,
    val isReviewDialogOpen: Boolean = false,
    val reviewDialogMode: String = "create",
    val existingReviewToEdit: ReviewResponse? = null,

    // Prompt States
    val showGuestPrompt: Boolean = false,
    val guestPromptText: String = "",
    val showExistingReviewPrompt: Boolean = false,
    val showDeleteConfirmPrompt: Boolean = false
)

class ProductDetailsViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()
    private val customerDataRepo = RepositoryProvider.customerDataRepository
    private val api: ProductRepository = ApiProvider.productRepository
    private val cartApi: CartRepository = ApiProvider.cartRepository

    fun displayGuestPrompt(text: String) {
        _uiState.update { it.copy(showGuestPrompt = true, guestPromptText = text) }
    }

    fun loadProductData(productCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProductLoading = true) }
            try {
                when (val productResponse = api.getProductDetail(productCode)) {
                    is ApiResult.Success -> {
                        val productData = productResponse.data.productDetails
                        val reviewOverview = productResponse.data.ratingOverview?.toList() ?: emptyList()
                        val isInFavourites = productData.inFavourites ?: false

                        _uiState.update {
                            it.copy(
                                productData = productData,
                                ratingOverview = reviewOverview,
                                averageRating = calculateAverage(reviewOverview),
                                isProductLoading = false,
                                isInFavourites = isInFavourites
                            )
                        }

                        loadReviews(productCode, reset = true)
                    }
                    is ApiResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                isProductLoading = false,
                                error = productResponse.error.detail
                            )
                        }
                    }
                    is ApiResult.NetworkError -> {
                        _uiState.update {
                            it.copy(
                                isProductLoading = false,
                                error = productResponse.exception.message ?: "Грешка в мрежата"
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                println(
                    "\n----------------------------------\n" +
                            "Failed to load detailed product: ${e.message} \n" +
                            "----------------------------------\n"
                )
                _uiState.update {
                    it.copy(
                        isProductLoading = false,
                        error = "Failed to load product"
                    )
                }
            }
        }
    }

    private fun calculateAverage(overview: List<RatingOverviewResponse>?): Float {
        var totalScore = 0L
        var totalCount = 0L

        if (overview != null && overview.isNotEmpty()) {
            overview.forEach {
                val normalized = it.rating / 10
                if (normalized in 1..5) {
                    totalScore += (normalized * it.count)
                    totalCount += it.count
                }
            }
        }
        return if (totalCount > 0) (totalScore.toFloat() / totalCount) else 0f
    }

    fun onAddReviewClick(productCode: String) {
        val isGuest = customerDataRepo.userState.value == null
        if (isGuest) {
            _uiState.update { it.copy(showGuestPrompt = true,
                guestPromptText ="За оставянето на ревюта се изисква профил. Можете да се регистрирате или да влезете в профила си" ) }
            return
        }

        if (_uiState.value.isCheckingReview) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingReview = true) }

            when (val result = api.getSpecificReview(productCode)) {
                is ApiResult.Success -> {
                    if (result.data.exists) {
                        val dummyReview = ReviewResponse(
                            reviewId = 0,
                            reviewText = result.data.reviewText,
                            rating = result.data.rating,
                            postTimestamp = Instant.now(),
                            customerDetailsForReview = CustomerDetailsForReview(
                                "",
                                null,
                                false,
                                currentUser = true,
                                isExpired = false
                            ),
                            isDeleted = false
                        )
                        _uiState.update {
                            it.copy(
                                isCheckingReview = false,
                                showExistingReviewPrompt = true,
                                existingReviewToEdit = dummyReview
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isCheckingReview = false,
                                isReviewDialogOpen = true,
                                reviewDialogMode = "create",
                                existingReviewToEdit = null
                            )
                        }
                    }
                }
                is ApiResult.Failure -> {
                    val errorMsg = result.error.detail.takeIf { it.isNotEmpty() } ?: result.error.detail
                    _uiState.update { it.copy(isCheckingReview = false, toastMessage = errorMsg) }
                }
                is ApiResult.NetworkError -> {
                    _uiState.update { it.copy(isCheckingReview = false, toastMessage = result.exception.message ?: "Мрежова грешка") }
                }
            }
        }
    }

    fun openUpdateDialog(review: ReviewResponse) {
        _uiState.update {
            it.copy(
                showExistingReviewPrompt = false,
                isReviewDialogOpen = true,
                reviewDialogMode = "update",
                existingReviewToEdit = review
            )
        }
    }

    fun requestDeleteConfirm(review: ReviewResponse? = null) {
        _uiState.update {
            it.copy(
                showExistingReviewPrompt = false,
                showDeleteConfirmPrompt = true,
                existingReviewToEdit = review ?: it.existingReviewToEdit
            )
        }
    }

    fun confirmDeleteReview(productCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteConfirmPrompt = false) }

            when (val result = api.deleteReview(ReviewDeleteRequest(productCode))) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(toastMessage = "Изтриването успешно!") }
                    loadReviews(productCode, reset = true)
                    loadProductData(productCode)
                }
                is ApiResult.Failure -> {
                    val errorMsg = result.error.detail.takeIf { it.isNotEmpty() } ?: result.error.detail
                    _uiState.update { it.copy(toastMessage = errorMsg) }
                }
                is ApiResult.NetworkError -> {
                    _uiState.update { it.copy(toastMessage = result.exception.message ?: "Мрежова грешка") }
                }
            }
        }
    }

    fun onReviewPostSuccess(message: String, productCode: String) {
        _uiState.update { it.copy(isReviewDialogOpen = false, toastMessage = message) }
        loadProductData(productCode)
    }

    fun closeReviewDialog() { _uiState.update { it.copy(isReviewDialogOpen = false) } }
    fun dismissGuestPrompt() { _uiState.update { it.copy(showGuestPrompt = false, guestPromptText = "") } }
    fun dismissExistingReviewPrompt() { _uiState.update { it.copy(showExistingReviewPrompt = false) } }
    fun dismissDeleteConfirmPrompt() { _uiState.update { it.copy(showDeleteConfirmPrompt = false) } }
    fun consumeToast() { _uiState.update { it.copy(toastMessage = null) } }

    fun loadReviews(productCode: String, reset: Boolean = false) {
        if (_uiState.value.isReviewsLoading || (_uiState.value.isLastPage && !reset)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isReviewsLoading = true) }
            val pageToLoad = if (reset) 0 else _uiState.value.currentPage + 1
            val ratingForRequest =
                if (uiState.value.filterRating == 0.toShort()) null else uiState.value.filterRating

            try {

                val bodyForRequest = ReviewsOfProductRequest(
                    productCode,
                    pageToLoad,
                    uiState.value.sortOrder,
                    uiState.value.verifiedOnly,
                    if (ratingForRequest != null) (ratingForRequest * 10).toShort() else null
                )
                when(val response = api.getPagedReviews(bodyForRequest))
                {
                    is ApiResult.Success->
                    {
                        _uiState.update { currentState ->
                            val newReviews =
                                if (reset) response.data.content else currentState.reviews + response.data.content
                            currentState.copy(
                                reviews = newReviews,
                                currentPage = response.data.pageNumber,
                                isLastPage = response.data.last,
                                totalReviews = response.data.totalElements,
                                isReviewsLoading = false
                            )
                        }
                    }
                    is ApiResult.Failure->
                    {
                        _uiState.update {
                            it.copy(
                                isReviewsLoading = false,
                                error = response.error.detail
                            )
                        }

                    }
                    is ApiResult.NetworkError->
                    {
                        _uiState.update {
                            it.copy(
                                isReviewsLoading = false,
                                error = response.exception.message ?: "Грешка в мрежата"
                            )
                        }

                    }
                }



            } catch (e: Exception) {

                println(
                    "\n----------------------------------\n" +
                            "Error loading reviews: ${e.message} " +
                            " \n----------------------------------\n"
                )
                _uiState.update { it.copy(isReviewsLoading = false) }
            }
        }
    }

    fun updateFilters(
        productCode: String,
        sort: String? = null,
        rating: Short? = null,
        userOnly: Boolean? = null
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                sortOrder = sort ?: currentState.sortOrder,
                filterRating = rating ?: currentState.filterRating,
                verifiedOnly = userOnly ?: currentState.verifiedOnly
            )
        }
        loadReviews(productCode, reset = true)
    }

    fun updateQuantity(delta: Short) {
        _uiState.update {
            val newQty = (it.quantity + delta).coerceAtLeast(1).toShort()
            it.copy(quantity = newQty)
        }
    }

    fun addToFavourites(productCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isMutatingFavourites = true) }

            when (val response = ApiProvider.customerRepository.addToFavourites(productCode)) {
                is ApiResult.Failure ->  {
                    val errorText = response.error.detail.ifEmpty { "Неуспешно добавяне" }
                    _uiState.update { it.copy(toastMessage = errorText) }
                }
                is ApiResult.NetworkError -> {
                    println("\n----------------------------------\n" +
                            "Request and error parsing for add to favourites request from detailed product failed: ${response.exception.message}" +
                            " \n---")
                    _uiState.update { it.copy(toastMessage = "Неуспешно добавяне" ) }

                }
                is ApiResult.Success -> {
                    _uiState.update { it.copy(toastMessage = "Успешно добавен в любими!", isInFavourites = true) }
                }
            }
            _uiState.update { it.copy(isMutatingFavourites = false) }
        }
    }

    fun removeFromFavourites(productCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isMutatingFavourites = true) }

            when (val response = ApiProvider.customerRepository.removeFromFavouritesInDetProd(productCode)) {
                is ApiResult.Failure -> {
                    val errorText = response.error.detail.ifEmpty { "Неуспешно премахване" }
                    _uiState.update { it.copy(toastMessage = errorText) }
                }
                is ApiResult.NetworkError -> {
                    println("\n----------------------------------\n" +
                            "Request and error parsing for remove from favourites request from detailed product failed: ${response.exception.message}" +
                            " \n---")
                    _uiState.update { it.copy(toastMessage = "Неуспешно премахване") }

                }
                is ApiResult.Success<*> -> {
                    _uiState.update { it.copy(toastMessage = "Успешно премахнат от любими!", isInFavourites = false) }
                }
            }
            _uiState.update { it.copy(isMutatingFavourites = false) }
        }
    }

    fun addToOrRemoveFromFavourites(productCode: String) {
        if (uiState.value.isInFavourites) {
            removeFromFavourites(productCode)
        } else {
            addToFavourites(productCode)
        }
    }

    fun addQuantityToCart(productCode: String, quantity: Short)
    {
        viewModelScope.launch {

            _uiState.update { it.copy(isAddingToCart = true) }

            val response = cartApi.addQuantityToCart(CartQuantityRequest(productCode, quantity))

            when (response)
            {

                is ApiResult.Success ->
                {
                    val responseData: CartOperationResponse = response.data
                    _uiState.update { it.copy(toastMessage = responseData.message) }

                    RepositoryProvider
                        .cartSummaryDataRepository
                        .update(CartSummaryData(
                            responseData.cartSummary.cartTotalCoins,
                            responseData.cartSummary.cartQuantity
                        ))
                }

                is ApiResult.Failure ->
                {
                    val errorText = response.error.detail.ifEmpty { "Неуспешно добавяне на количеството в количката" }


                    _uiState.update { it.copy(toastMessage = errorText) }
                }

                is ApiResult.NetworkError ->
                {
                    println("\n----------------------------------\n" +
                            "Request and error parsing for add quantity to cart from detailed product failed: ${response.exception.message}" +
                            " \n----------------------------------\n")
                }
            }
            _uiState.update { it.copy(isAddingToCart = false) }
        }
    }

}