package com.example.webstore_android_client.model.responses.attribute

import com.google.gson.annotations.SerializedName

data class AttributeOptionResponse(
    @SerializedName("attributeName") val attributeName: String,
    @SerializedName("option") val option: String,
    @SerializedName("measurementUnit") val measurementUnit: String?
)