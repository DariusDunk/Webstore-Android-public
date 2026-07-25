package com.example.webstore_android_client.ui.productBrowsing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.model.requests.product.CatmanSearchRequest
import com.example.webstore_android_client.model.requests.product.ProductFilterRequest
import com.example.webstore_android_client.model.responses.attribute.CategoryAttributesResponse
import com.example.webstore_android_client.model.responses.page.PageResponse
import com.example.webstore_android_client.model.responses.product.CompactProductResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryFiltersData(
    val priceLowestStotinki: Int = 0,
    val priceHighestStotinki: Int = 0,
    val manufacturers: List<String> = emptyList(),
    val attributes: List<CategoryAttributesResponse> = emptyList(),
    val ratings: List<Int> = emptyList()
)


data class ProductsUiState(
    // ---------------------------------- Product list ----------------------------------
    val products: List<CompactProductResponse> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = -1,
    val totalElements: Long = 0L,
    val totalPages: Int = 0,

    // ---------------------------------- Active query ----------------------------------
    val query: ProductQuery? = null,

    // ---------------------------------- Filters  ----------------------------------
    val categoryFilters: CategoryFiltersData? = null,
    val isFiltersSheetOpen: Boolean = false,

    val error: String? = null
)


class ProductsViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    init {
        val initialQuery = ProductQuery.fromNavArgs(
            modeString = savedStateHandle["mode"],
            details = savedStateHandle["details"]
        )

        if (initialQuery.mode !is ProductQueryMode.ImageSearch) {
            load(initialQuery)
        }
    }

    fun load(query: ProductQuery) {
        fetchJob?.cancel()
        _uiState.update {
            it.copy(
                isLoading = true,
                products = emptyList(),
                currentPage = -1,
                hasMore = true,
                query = query,
                error = null,
                categoryFilters = if (query.mode == it.query?.mode) it.categoryFilters else null
            )
        }
        fetchJob = viewModelScope.launch {
            executeFetch(query = query, page = 0, append = false)
        }
    }

    fun loadMore() {
        val state = _uiState.value
//        println("LOAD MORE CALLED page=${state.currentPage}")

        if (state.isLoadingMore || state.isLoading || !state.hasMore || state.query == null) return
        val nextPage = state.currentPage + 1
        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            executeFetch(query = state.query, page = nextPage, append = true, imageSession = activeImageSession)
        }
    }

    fun changeSort(sort: ProductSort) {
        val q = _uiState.value.query ?: return
        if (q.sort == sort) return
        load(q.copy(sort = sort))
    }

    fun applyFilters(filters: ProductFilters) {
        val q = _uiState.value.query ?: return
        load(q.copy(filters = filters))
        _uiState.update { it.copy(isFiltersSheetOpen = false) }
    }

    fun resetFilters() {
        val q = _uiState.value.query ?: return
        load(q.copy(filters = ProductFilters()))
    }

    fun openFiltersSheet() {
        _uiState.update { it.copy(isFiltersSheetOpen = true) }
    }

    fun closeFiltersSheet() {
        _uiState.update { it.copy(isFiltersSheetOpen = false) }
    }

    fun retry() = _uiState.value.query?.let { load(it) }


    private var activeImageSession: ImageSearchSession? = null

    fun load(query: ProductQuery, imageSession: ImageSearchSession? = null) {
        fetchJob?.cancel()

        activeImageSession = imageSession

        _uiState.update {
            it.copy(
                isLoading = true,
                products = emptyList(),
                currentPage = -1,
                hasMore = true,
                query = query,
                error = null,
                categoryFilters = if (query.mode == it.query?.mode) it.categoryFilters else null
            )
        }
        fetchJob = viewModelScope.launch {
            executeFetch(query = query, page = 0, append = false, imageSession = activeImageSession)
        }
    }

    private suspend fun executeFetch(
        query: ProductQuery,
        page: Int,
        append: Boolean,
        imageSession: ImageSearchSession? = null
    ) {

        when (val result = doFetch(query, page, imageSession)) {
            is ApiResult.Success -> {
                val pageData = result.data

                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        products = if (append) current.products + (pageData.content?:emptyList())
                        else pageData.content,
                        currentPage = pageData.pageNumber,
                        hasMore = !pageData.last,
                        totalElements = pageData.totalElements,
                        totalPages = pageData.totalPages,
                        error = null
                    )
                }

                if (!append && query.mode is ProductQueryMode.Category) {
                    fetchCategoryFilters(query.mode.categoryName)
                }
            }

            is ApiResult.Failure -> _uiState.update {

                val errorMsg = result.error.detail.takeIf { result.error.detail.isNotEmpty() }
                    ?: "Грешка при извличане на продукти"


                it.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = errorMsg
                )
            }

            is ApiResult.NetworkError -> _uiState.update {
                it.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = result.exception.message ?: "Грешка в мрежата"
                )
            }
        }
    }
    private fun validatePriceRange(min: Int, max: Int) {
        if (min > max) {
            _uiState.update()
            {
                it.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = "Невалиден ценови диапазон"
                )
            }
            throw IllegalArgumentException("Invalid price range")
        }
    }

    private suspend fun doFetch(
        query: ProductQuery,
        page: Int,
        imageSession: ImageSearchSession? = null
    ): ApiResult<PageResponse<CompactProductResponse>> {
        return try {
            when (val mode = query.mode) {

                // ---------------------------------- Category ----------------------------------
                is ProductQueryMode.Category -> if (query.filters.isEmpty) {

                    return ApiProvider.productRepository.getByCategoryPaged(
                        mode.categoryName,
                        page,
                        query.sort.value
                    )
                } else {

                         val minPrice        = query.filters.minPriceStotinki
                         val maxPrice        = query.filters.maxPriceStotinki
                         val manufacturers   = query.filters.manufacturers
                         val minRating       = query.filters.minRating
                         val attributes      = query.filters.attributes

                    if (minPrice!=null && maxPrice!=null)
                    {
                        validatePriceRange(minPrice, maxPrice)
                    }

                    val requestBody = ProductFilterRequest(minPrice,
                        maxPrice,
                        manufacturers,
                        minRating,
                        attributes)

                    return ApiProvider.productRepository.getByFilters(
                        mode.categoryName,
                        page,
                        requestBody,
                        query.sort.value
                        )

                }

                // ---------------------------------- Manufacturer ----------------------------------
                is ProductQueryMode.Manufacturer -> {
                    return ApiProvider.productRepository.getByManufacturerPaged(
                        mode.manufacturerName,
                        page,
                        query.sort.value
                    )
                }

                // ---------------------------------- Search ----------------------------------
                is ProductQueryMode.Search -> {
                    return ApiProvider.productRepository.getByKeyword(
                        mode.query,
                        page,
                        query.sort.value
                    )
                }

                // ---------------------------------- Lightweight Image categories + manufacturers search ----------------------------------
                ProductQueryMode.ImageSearch -> {

                    if (imageSession == null) {
                        return ApiResult.Failure(com.example.webstore_android_client.model.responses.error.ErrorResponse(detail = "Няма активно изображение за търсене."))
                    }

                    if (page == 0 && imageSession.productSnapshot != null) {
                        return ApiResult.Success(imageSession.productSnapshot)
                    }

                    if (imageSession.categories.isEmpty() && imageSession.manufacturers.isEmpty()) {
                        return ApiResult.Success(PageResponse(emptyList(), 0, 0, 0, 0, true))
                    }

                    val requestBody = CatmanSearchRequest(imageSession.categories, imageSession.manufacturers, page)

                    val response = ApiProvider.productRepository.getByCategoriesAndManufacturers(requestBody)

                    return response

                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.NetworkError(e)
        }
    }

    private suspend fun fetchCategoryFilters(categoryName: String) {

        val response = ApiProvider.attributeRepository.getFilters(categoryName)

        if (response is ApiResult.Success) {

            val responseData = response.data

            _uiState.update {
                it.copy(
                    categoryFilters = CategoryFiltersData(
                        responseData.priceLowest,
                        responseData.priceHighest,
                        responseData.manufacturerNames.toList(),
                        responseData.categoryAttributesResponses,
                        responseData.ratings.toList().sortedDescending()
                    )
                )
            }
        } else {
            _uiState.update { it.copy(categoryFilters = null) }
        }
    }
}
