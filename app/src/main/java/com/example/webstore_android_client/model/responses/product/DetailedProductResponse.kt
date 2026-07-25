package com.example.webstore_android_client.model.responses.product

import com.example.webstore_android_client.model.responses.attribute.AttributeOptionResponse
import com.google.gson.annotations.SerializedName

data class DetailedProductResponse(
    @SerializedName("name") val name: String,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("originalPriceStotinki") val originalPriceStotinki: Int,
    @SerializedName("salePriceStotinki") val salePriceStotinki: Int,
    @SerializedName("productCode") val productCode: String,
    @SerializedName("manufacturer") val manufacturer: String,
    @SerializedName("attributes") val attributes: List<AttributeOptionResponse>?,
    @SerializedName("productDescription") val productDescription: String,
    @SerializedName("rating") val rating: Short,
    @SerializedName("deliveryCost") val deliveryCost: Short?,
    @SerializedName("model") val model: String,
    @SerializedName("productImages") val productImageURLs: List<String>?,
    @SerializedName("inFavourites") val inFavourites: Boolean?,
    @SerializedName("inCart") val inCart: Boolean,
    @SerializedName("reviewed") val reviewed: Boolean,
    @SerializedName("isInStock") val isInStock: Boolean
)
