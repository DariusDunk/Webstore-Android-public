package com.example.webstore_android_client.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.repositories.CustomerRepository
import com.example.webstore_android_client.api.repositories.ProductRepository
import com.example.webstore_android_client.api.repositories.PurchaseRepository
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.model.localDTOs.cart.CartSummaryData
import com.example.webstore_android_client.model.localDTOs.purchase.SelectedCheckoutItem
import com.example.webstore_android_client.model.requests.cart.CartQuantityRequest
import com.example.webstore_android_client.model.requests.purchase.PurchaseCompleteRequest
import com.example.webstore_android_client.model.requests.purchase.PurchaseProduct
import com.example.webstore_android_client.model.requests.purchase.RecipientDataRequest
import com.example.webstore_android_client.repositories.CartSummaryDataRepository
import com.example.webstore_android_client.repositories.CustomerDataRepository
import com.example.webstore_android_client.repositories.RepositoryProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
 
class CheckoutViewModel(
) : ViewModel() {

    private val productRepository: ProductRepository = ApiProvider.productRepository
    private val purchaseRepository: PurchaseRepository = ApiProvider.purchaseRepository
    private val customerRepository: CustomerRepository = ApiProvider.customerRepository
    private val customerDataRepository: CustomerDataRepository = RepositoryProvider.customerDataRepository
    private val cartSummaryRepository: CartSummaryDataRepository = RepositoryProvider.cartSummaryDataRepository
    // ----------- Navigation arguments --------------------------------------------------


    val isGuestUser: Boolean = customerDataRepository.userState.value == null

    // ------------ State & Events --------------------------------------------------------
    private val _uiState = MutableStateFlow(CheckoutState())
    val uiState: StateFlow<CheckoutState> = _uiState.asStateFlow()

    private val _events = Channel<CheckoutEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var fetchedRecipientData: RecipientTemplate? = null

    // ------------------ Initialisation --------------------------------------------------------
    fun initialize(selectedItems: List<SelectedCheckoutItem>, isDirectPurchase: Boolean) {

        if (selectedItems.isEmpty()) {
            viewModelScope.launch { _events.send(CheckoutEvent.NavigateToCart) }
        } else {

            _uiState.update { it.copy(selectedCheckoutItems =  selectedItems,
                isDirectPurchase = isDirectPurchase) }

            fetchProductDetails()
            if (!isGuestUser) fetchRecipientTemplate()
        }
    }

    // ------------------ Data loading ----------------------------------------------------------

    private fun fetchProductDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {

                val requestBody = uiState.value.selectedCheckoutItems.map { item ->
                    CartQuantityRequest(
                        productCode = item.productCode,
                        quantity = item.quantity.toShort()
                    )
                }

                when (val response = productRepository.getByCodesWithStockValidation(requestBody)) {
                    is ApiResult.Failure -> {

                        val errorResponse = response.error

                        _events.send(
                            CheckoutEvent.ShowError(
                                title = errorResponse.title?:"Неуспешно извличане",
                                message = errorResponse.detail?:"Имаше проблем с извличането на продуктите"
                            )
                        )
                        _events.send(CheckoutEvent.NavigateToCart)

                    }
                    is ApiResult.NetworkError -> {
                        _events.send(
                            CheckoutEvent.ShowError(
                                title = "Неуспешно извличане",
                                message = "Имаше проблем с извличането на продуктите"
                            )
                        )
                        println("\n----------------------------------\n" +
                                "Initial product fetch in checkout viewModel failed: ${response.exception}" +
                                " \n----------------------------------\n")
                        _events.send(CheckoutEvent.NavigateToCart)
                    }
                    is ApiResult.Success -> {
                        val responseBody = response.data

                        println("\n----------------------------------\n" +
                                "Fetched products data: $responseBody" +
                                " \n----------------------------------\n")

                        val cartItems = responseBody.map { (product, quantity) -> CartItem(product, quantity) }

                        _uiState.update { it.copy(cartSummary = buildCartSummary(cartItems)) }
                    }
                }
                _uiState.update { it.copy(isLoading = false) }


            } catch (e: Exception) {
                println("\n----------------------------------\n" +
                        "Error fetching product info: ${e.message}" +
                        " \n----------------------------------\n")
                _uiState.update { it.copy(isLoading = false) }
                _events.send(
                    CheckoutEvent.ShowError(
                        title = "Неуспешно извличане",
                        message = "Имаше проблем с извличането на продуктите"
                    )
                )
                _events.send(CheckoutEvent.NavigateToCart)
            }
        }
    }

    private fun fetchRecipientTemplate() {
        viewModelScope.launch {
            try {
                 when (val response = customerRepository.getRecipientTemplate()) {
                     is ApiResult.Failure -> {
                         println("\n----------------------------------\n" +
                                 "Error fetching recipient template: ${response.error}" +
                                 " \n----------------------------------\n")
                     }
                     is ApiResult.NetworkError -> {
                        println("\n----------------------------------\n" +
                                "Error fetching recipient template: ${response.exception}" +
                                " \n----------------------------------\n")
                     }
                     is ApiResult.Success -> {
                         val responseBody = response.data
                         fetchedRecipientData = RecipientTemplate(responseBody.contactName, responseBody.contactNumber, responseBody.address)
                         _uiState.update { s ->
                             s.copy(formData = s.formData.copy(
                                 contactName   = s.formData.contactName.ifBlank   { responseBody.contactName },
                                 contactNumber = s.formData.contactNumber.ifBlank { responseBody.contactNumber },
                                 address       = s.formData.address.ifBlank       { responseBody.address }
                             ))
                         }
                     }
                 }

            } catch (_: Exception) {
            }
        }
    }

    // ------------------------ Form updates  ----------------------------

    fun onFormDataChange(updater: (FormData) -> FormData) {
        _uiState.update { it.copy(formData = updater(it.formData), emailError = null) }
    }

    // ----------------------- Step transitions ------------------------------------------------------

    fun onContinueFromInfo() {
        val form = _uiState.value.formData

        if (form.contactName.isBlank() || form.contactNumber.isBlank() || form.address.isBlank()) {
            viewModelScope.launch {
                _events.send(
                    CheckoutEvent.ShowWarning(
                        "Празни полета",
                        "Моля попълнете всички полета!"
                    )
                )
            }
            return
        }
        if (isGuestUser && !form.email.contains('@')) {
            _uiState.update { it.copy(emailError = "Моля въведете валиден имейл адрес.") }
            return
        }
        _uiState.update { it.copy(emailError = null) }

        val changed = if (fetchedRecipientData == null) {
            true
        } else {
            form.contactName != fetchedRecipientData!!.contactName ||
                    form.contactNumber != fetchedRecipientData!!.contactNumber ||
                    form.address != fetchedRecipientData!!.address
        }

        if (!isGuestUser && changed) {
            viewModelScope.launch {
                _events.send(
                    CheckoutEvent.AskToSaveRecipientData(
                    onSave = {
                        viewModelScope.launch {
                           when(val response = customerRepository.setRecipientTemplate(
                                RecipientDataRequest(
                                    form.contactName,
                                    form.contactNumber,
                                    form.address
                                )
                            )) {
                               is ApiResult.Failure -> { val errorResponse = response.error

                                   _events.send(
                                       CheckoutEvent.ShowError(
                                           errorResponse.title?:"Неуспешно запазване",
                                           errorResponse.detail?:"Имаше проблем със запазването на образеца"
                                       )
                                   )}
                               is ApiResult.NetworkError -> {    _events.send(
                                   CheckoutEvent.ShowError(
                                       "Неуспешно запазване",
                                       "Имаше проблем със запазването на образеца"
                                   )
                               )

                                   println("\n----------------------------------\n" +
                                           "Recipient template set exception: ${response.exception}" +
                                           " \n----------------------------------\n")}
                               is ApiResult.Success-> {
                                   fetchedRecipientData = RecipientTemplate(form.contactName, form.contactNumber, form.address)
                               }
                           }



                            _uiState.update { it.copy(step = CheckoutStep.SUMMARY) }
                        }
                    },
                    onSkip = {
                        _uiState.update { it.copy(step = CheckoutStep.SUMMARY) }
                    }
                ))
            }
        } else {
            _uiState.update { it.copy(step = CheckoutStep.SUMMARY) }
        }
    }

    fun onBackFromSummary() {
        _uiState.update { it.copy(step = CheckoutStep.INFO) }
    }

    // --------------------- Order submission  --------------------

    fun onConfirmOrder() {
        if (_uiState.value.isMutating) return
        _uiState.update { it.copy(isMutating = true) }

        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val form = currentState.formData
                val items = currentState.cartSummary?.items ?: run {
                    _uiState.update { it.copy(isMutating = false) }
                    return@launch
                }

                 val requestBody = PurchaseCompleteRequest(
                     products = items.map {
                         PurchaseProduct(
                             it.compactProductResponse.productCode,
                             it.quantity
                         )
                     },
                     isDirectPurchase = uiState.value.isDirectPurchase,
                     recipientDataRequest = RecipientDataRequest(form.contactName, form.contactNumber, form.address),
                     paymentMethod = form.paymentMethod.name,
                     email = form.email
                 )
                 when (val response = purchaseRepository.completePurchase(requestBody)) {
                     is ApiResult.Failure -> {
                         _uiState.update { it.copy(isMutating = false) }
                         val errorBody = response.error

                         val msg = errorBody.detail?:"Не успяхме да обработим поръчката Ви. Моля, опитайте отново."
                         _events.send(CheckoutEvent.ShowError("Възникна грешка", msg))
                     }
                     is ApiResult.NetworkError -> {
                         _uiState.update { it.copy(isMutating = false) }
                         val msg = "Не успяхме да обработим поръчката Ви. Моля, опитайте отново."
                         _events.send(CheckoutEvent.ShowError("Възникна грешка", msg))

                         println("\n----------------------------------\n" +
                                 "Confirm order exception: ${response.exception}" +
                                 " \n----------------------------------\n")
                     }
                     is ApiResult.Success -> {

                         val responseBody = response.data
                         val updatedSummary= responseBody.cartSummary

                         cartSummaryRepository.update(CartSummaryData(updatedSummary.cartTotalCoins, updatedSummary.cartQuantity))

                          val result = OrderResult(responseBody.purchaseCode, responseBody.totalCost.toLong())

                          _uiState.update { it.copy(orderResult = result, step = CheckoutStep.SUCCESS, isMutating = false) }

                     }
                 }

            } catch (e: Exception) {
                _uiState.update { it.copy(isMutating = false) }
                val msg = "Не успяхме да обработим поръчката Ви. Моля, опитайте отново."
                _events.send(CheckoutEvent.ShowError("Възникна грешка", msg))
            }
        }
    }

    // ----------------------- Helpers --------------------------------------------------------------─

    private fun buildCartSummary(items: List<CartItem>): CartSummary {
        val totals = calculateTotals(items)
        return CartSummary(items = items, totals = totals)
    }

    private fun calculateTotals(items: List<CartItem>): CartTotals {
        val freeShippingThreshold = 10_000L
        val subtotal = items.sumOf { it.compactProductResponse.salePriceStotinki }
        val shipping = if (subtotal > freeShippingThreshold) 0L else 1_000L
        return CartTotals(
            subtotalStotinki = subtotal.toLong(),
            shippingStotinki = shipping,
            totalStotinki = subtotal + shipping
        )
    }
}

// --------------------- Helper model  ---------------------
private data class RecipientTemplate(
    val contactName: String,
    val contactNumber: String,
    val address: String
)
