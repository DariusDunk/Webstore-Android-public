package com.example.webstore_android_client.ui.checkout

import com.example.webstore_android_client.model.localDTOs.purchase.SelectedCheckoutItem
import com.example.webstore_android_client.model.responses.product.CompactProductResponse

enum class CheckoutStep { INFO, SUMMARY, SUCCESS }

enum class PaymentMethod(val displayName: String) {
    CASH_ON_DELIVERY("Наложен платеж (Плащане при доставка)")
}

data class FormData(
    val contactName: String = "",
    val contactNumber: String = "",
    val address: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val email: String = ""
)


data class CartItem(
    val compactProductResponse: CompactProductResponse,
    val quantity: Int
)

data class CartTotals(
    val subtotalStotinki: Long = 0L,
    val shippingStotinki: Long = 0L,
    val totalStotinki: Long = 0L
)

data class CartSummary(
    val items: List<CartItem> = emptyList(),
    val totals: CartTotals = CartTotals()
)

data class OrderResult(
    val purchaseCode: String?,
    val totalCost: Long?
)

data class CheckoutState(
    val step: CheckoutStep = CheckoutStep.INFO,
    val formData: FormData = FormData(),
    val cartSummary: CartSummary? = null,
    val orderResult: OrderResult? = null,
    val isLoading: Boolean = false,
    val isMutating: Boolean = false,
    val emailError: String? = null,
    val selectedCheckoutItems: List<SelectedCheckoutItem> = emptyList(),
    val isDirectPurchase: Boolean = false
)


sealed class CheckoutEvent {
    object NavigateToCart : CheckoutEvent()

    data class ShowError(val title: String, val message: String) : CheckoutEvent()
    data class ShowWarning(val title: String, val message: String) : CheckoutEvent()

    data class AskToSaveRecipientData(
        val onSave: () -> Unit,
        val onSkip: () -> Unit
    ) : CheckoutEvent()
}
