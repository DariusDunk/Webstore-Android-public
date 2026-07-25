package com.example.webstore_android_client.ui.productBrowsing

sealed class ProductQueryMode {
    data class Category(val categoryName: String) : ProductQueryMode()

    data class Manufacturer(val manufacturerName: String) : ProductQueryMode()

    data class Search(val query: String) : ProductQueryMode()

    object ImageSearch : ProductQueryMode()
}

enum class ProductSort(val value: String, val label: String) {
    RELEVANCE(   "relevance",    "Значение"     ),
    POPULARITY(  "popularity",   "Популярност"  ),
    PRICE_ASC(   "price_asc",    "Цена възх."   ),
    PRICE_DESC(  "price_desc",   "Цена низх."   ),
    NEWEST(      "newest",       "Най-нови"     ),
    REVIEW_COUNT("review_count", "Брой ревюта"  );

    companion object {

        fun defaultFor(mode: ProductQueryMode): ProductSort = when (mode) {
            is ProductQueryMode.Search -> RELEVANCE
            else                       -> POPULARITY
        }

//        fun fromValue(value: String?): ProductSort? = entries.find { it.value == value }

        fun availableFor(mode: ProductQueryMode): List<ProductSort> = buildList {
            when (mode) {
                is ProductQueryMode.Search -> add(RELEVANCE)
                else                       -> add(POPULARITY)
            }
            addAll(listOf(PRICE_ASC, PRICE_DESC, NEWEST, REVIEW_COUNT))
        }
    }
}

data class ProductFilters(
    val minPriceStotinki: Int? = null,
    val maxPriceStotinki: Int? = null,
    val manufacturers: List<String> = emptyList(),
    val minRating: Int? = null,
    val attributes: Map<String, List<String>> = emptyMap()
) {
    val isEmpty: Boolean
        get() = minPriceStotinki == null &&
                maxPriceStotinki == null &&
                manufacturers.isEmpty() &&
                minRating == null &&
                attributes.isEmpty()

//    companion object {
//        fun isValidPriceRange(min: Int?, max: Int?): Boolean {
//            if (min == null || max == null) return true
//            return min in 0..max
//        }
//
//        fun isValidRating(value: Int?): Boolean =
//            value == null || value in 0..5
//    }
}


data class ProductQuery(
    val mode: ProductQueryMode,
    val filters: ProductFilters = ProductFilters(),
    val sort: ProductSort = ProductSort.defaultFor(ProductQueryMode.Search("")),
) {

    val sortAvailable: Boolean get() = mode !is ProductQueryMode.ImageSearch

    val filtersAvailable: Boolean get() = mode is ProductQueryMode.Category

    val availableSorts: List<ProductSort> get() = ProductSort.availableFor(mode)


    val screenTitle: String get() = when (val m = mode) {
        is ProductQueryMode.Category     -> "Продукти от категория ${m.categoryName}"
        is ProductQueryMode.Manufacturer -> "Продукти от марка ${m.manufacturerName}"
        is ProductQueryMode.Search       -> "Резултати за: ${m.query}"
        ProductQueryMode.ImageSearch     -> "Резултати от търсене по изображение"
    }

    // ---------------------------------- Factory helpers ----------------------------------

    companion object {

        fun fromNavArgs(modeString: String?, details: String?): ProductQuery {
            val mode = when (modeString) {
                "category"     -> ProductQueryMode.Category(details ?: "")
                "manufacturer" -> ProductQueryMode.Manufacturer(details ?: "")
                "image_search" -> ProductQueryMode.ImageSearch
                else           -> ProductQueryMode.Search(details ?: "")
            }
            return ProductQuery(mode = mode, sort = ProductSort.defaultFor(mode))
        }
    }
}
