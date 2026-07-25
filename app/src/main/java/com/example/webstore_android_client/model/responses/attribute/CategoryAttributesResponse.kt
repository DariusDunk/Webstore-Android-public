package com.example.webstore_android_client.model.responses.attribute

data class CategoryAttributesResponse(
    val attributeName: String,
    val options: List<String> = emptyList(),
    val measurementUnit: String?
)
