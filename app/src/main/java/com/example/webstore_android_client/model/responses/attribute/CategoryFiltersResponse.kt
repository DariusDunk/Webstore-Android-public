package com.example.webstore_android_client.model.responses.attribute

import com.google.gson.annotations.SerializedName

data class CategoryFiltersResponse(

    @SerializedName("category_attributes")
    val categoryAttributesResponses: List<CategoryAttributesResponse>,

    @SerializedName("manufacturers")
    val manufacturerNames: Set<String>,

    @SerializedName("ratings")
    val ratings: Set<Int>,

    @SerializedName("price_lowest")
    val priceLowest: Int,

    @SerializedName("price_highest")
    val priceHighest: Int
)
