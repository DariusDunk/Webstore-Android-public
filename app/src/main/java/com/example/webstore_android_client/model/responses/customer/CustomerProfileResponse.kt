package com.example.webstore_android_client.model.responses.customer

import com.google.gson.annotations.SerializedName
import java.time.Instant

data class CustomerProfileResponse(
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val familyName: String?,
    @SerializedName("email") val email: String,
    @SerializedName("register_date") val registerDate: Instant?, // Or String depending on your Gson config
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("delivery_address") val deliveryAddress: String?,
    @SerializedName("favourites_count") val favouritesCount: Int,
    @SerializedName("user_pfp") val userPfp: String?,
    @SerializedName("reviews_count") val reviewsCount: Int,
    @SerializedName("purchases_count") val purchasesCount: Int
)
