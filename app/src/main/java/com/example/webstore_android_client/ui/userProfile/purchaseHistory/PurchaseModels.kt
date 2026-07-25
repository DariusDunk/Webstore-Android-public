package com.example.webstore_android_client.ui.userProfile.purchaseHistory


import java.time.Instant
import java.time.LocalDateTime

//data class ProductSummary(
//    val productCode: String,
//    val productName: String,
//    val imageUrl: String,
//)

data class OrderProduct(
    val productCode: String,
    val productName: String,
    val imageUrl: String,
    val rating: Int,
    val reviewCount: Int,
    val singlePriceCents: Int,
    val quantity: Int,
) {
    val lineTotalCents: Int get() = singlePriceCents * quantity
}

//data class CompactPurchaseResponse(
//    val purchaseCode: String,
//    val purchaseDate: String?,
//    val status: String,
//    val products: List<ProductSummary>,
//    val deliveryAddress: String?,
//    val totalCostCents: Int,
//    val shippingFeeCents: Int,
//)

data class PurchaseDetail(
    val purchaseCode: String,
    val purchaseDate: LocalDateTime?,
    val status: String,
    val products: List<OrderProduct>,
    val deliveryAddress: String?,
    val totalCostCents: Int,
    val shippingFeeCents: Int,
    val productsTotalCents: Int,
    val recipientName: String?,
    val recipientPhone: String?,
    val paymentMethod: String?,
    val deliveryDate: Instant?,
)

fun formatEurCents(cents: Int): String = "%.2f €".format(cents / 100.0)

//
//fun formatTimestamp(timestamp: String?): String {
//    if (timestamp.isNullOrBlank()) return ""
//    return try {
//        val inputFmt  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
//        val outputFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        val date = inputFmt.parse(timestamp) ?: return timestamp
//        outputFmt.format(date)
//    } catch (_: Exception) {
//        timestamp
//    }
//}
