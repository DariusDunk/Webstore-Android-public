package com.example.webstore_android_client.ui.userProfile.purchaseHistory


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.api.ApiProvider.purchaseRepository
import com.example.webstore_android_client.api.utils.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class PurchaseDetailUiState(
    val detail: PurchaseDetail? = null,
    val isLoading: Boolean = true,
    val showCancelDialog: Boolean = false,
    val showRefundDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val downloadedInvoiceFile: File? = null,
    val errorMessage: String? = null
)

class PurchaseDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseDetailUiState())
    val uiState: StateFlow<PurchaseDetailUiState> = _uiState.asStateFlow()

    private var detailJob: Job? = null

    fun loadDetail(
        purchaseCode: String,
        purchaseDate: Instant?,
        status: String,
        deliveryAddress: String,
        totalCostCents: Int,
        shippingFeeCents: Int
    ) {
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {


                   when (val response = purchaseRepository.getDetailedPurchaseData(purchaseCode)) {
                       is ApiResult.Failure -> {

                           val errorResponse = response.error
                           val errorText = errorResponse.detail?:"Имаше проблем с зареждането на детайлите за поръчката."
                           _uiState.update {
                               it.copy(
                                   isLoading = false,
                                   snackbarMessage = errorText,
                               )
                           }
                       }
                       is ApiResult.NetworkError -> {
                           _uiState.update {
                               it.copy(
                                   isLoading = false,
                                   snackbarMessage = "Имаше проблем с зареждането на детайлите за поръчката.",
                               )
                           }

                           println("\n----------------------------------\n" +
                                   "Purchase detail fetch error: ${response.exception}" +
                                   " \n----------------------------------\n")

                       }
                       is ApiResult.Success -> {
                           val data = response.data
                           _uiState.update {
                               it.copy(
                                   isLoading = false,
                                   detail    = PurchaseDetail(
                                       purchaseCode      = purchaseCode,
                                       purchaseDate      = LocalDateTime.ofInstant(purchaseDate, ZoneId.systemDefault()),
                                       status            = status,
                                       products          = data.products.map { product -> product.toOrderProduct() },
                                       deliveryAddress   = deliveryAddress,
                                       totalCostCents    = totalCostCents,
                                       shippingFeeCents  = shippingFeeCents,
                                       productsTotalCents = data.productsTotalCents,
                                       recipientName     = data.recipientName,
                                       recipientPhone    = data.recipientPhone,
                                       paymentMethod     = data.paymentMethod,
                                       deliveryDate      = data.deliveryDate,
                                   ),
                               )
                           }
                       }
                   }


            } catch (e: Exception) {
                println("\n----------------------------------\n" +
                        "Purchase detail fetch exception: ${e.stackTrace}" +
                        " \n----------------------------------\n")
                _uiState.update {
                    it.copy(
                        isLoading       = false,
                        snackbarMessage = "Имаше проблем с зареждането на детайлите за поръчката.",
                    )
                }
            }
        }
    }


    fun onCancelClicked() {
        _uiState.update { it.copy(showCancelDialog = true) }
    }

    fun onCancelDialogDismiss() {
        _uiState.update { it.copy(showCancelDialog = false) }
    }

    fun onConfirmCancel() {
        val purchaseCode = _uiState.value.detail?.purchaseCode ?: return
        _uiState.update { it.copy(showCancelDialog = false) }
        viewModelScope.launch {
            try {

                when (val response = purchaseRepository.cancelOrder(purchaseCode)) {
                    is ApiResult.Failure -> {
                        val errorResponse = response.error
                        val errorText = errorResponse.detail?:"Имаше проблем със заявката за отказване на поръчката."
                        _uiState.update {
                            it.copy(snackbarMessage = errorText)
                        }
                    }
                    is ApiResult.NetworkError ->{
                        _uiState.update {
                            it.copy(snackbarMessage = "Имаше проблем със заявката за отказване на поръчката.")
                        }

                        println("\n----------------------------------\n" +
                                "Error for order cancel request: ${response.exception}" +
                                " \n----------------------------------\n")
                    }
                    is ApiResult.Success ->
                    {
                        _uiState.update {
                            it.copy(snackbarMessage = "Поръчката е успешно отказана, състоянието ще се обнови скоро!")
                        }
                        // TODO: refresh or notify the list screen.

                    }
                }

            } catch (e: Exception) {
                println("\n----------------------------------\n" +
                        "Exception for order cancel request: ${e.stackTrace}" +
                        " \n----------------------------------\n")
                _uiState.update {
                    it.copy(snackbarMessage = "Имаше проблем със заявката за отказване на поръчката.")
                }
            }
        }
    }

    fun onRefundClicked() {
        _uiState.update { it.copy(showRefundDialog = true) }
    }

    fun onRefundDialogDismiss() {
        _uiState.update { it.copy(showRefundDialog = false) }
    }

    fun onConfirmRefund() {
        val purchaseCode = _uiState.value.detail?.purchaseCode ?: return
        _uiState.update { it.copy(showRefundDialog = false) }
        viewModelScope.launch {
            try {

                when (val response = purchaseRepository.requestRefund(purchaseCode = purchaseCode)) {
                    is ApiResult.Failure ->
                    {
                        val errorResponse = response.error
                        val errorText = errorResponse.detail?:"Имаше проблем със заявката за връщане на поръчката."
                        _uiState.update {
                            it.copy(snackbarMessage = errorText)
                        }
                    }
                    is ApiResult.NetworkError ->{
                        _uiState.update {
                            it.copy(snackbarMessage = "Имаше проблем със заявката за връщане на поръчката, опитайте отново.")
                        }

                        println("\n----------------------------------\n" +
                                "Error in refund request: ${response.exception}" +
                                " \n----------------------------------\n")
                    }
                    is ApiResult.Success ->
                    {
                        _uiState.update {
                            it.copy(snackbarMessage = "Успешно заявяване на връщане, състоянието ще се обнови скоро!")
                        }
                    }
                }


            } catch (e: Exception) {
                println("\n----------------------------------\n" +
                        "Exception in refund request: ${e.stackTrace}" +
                        " \n----------------------------------\n")
                _uiState.update {
                    it.copy(snackbarMessage = "Имаше проблем със заявката за връщане на поръчката, опитайте отново.")
                }
            }
        }
    }

    fun onDownloadInvoice() {
        val purchaseCode = _uiState.value.detail?.purchaseCode ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val file = purchaseRepository.getInvoicePDF(purchaseCode)

            if (file != null) {
                _uiState.update {
                    it.copy(isLoading = false, downloadedInvoiceFile = file)
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Грешка при изтегляне на файла.")
                }
            }
        }
    }



    fun clearDownloadedInvoice() {
        _uiState.update { it.copy(downloadedInvoiceFile = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    fun onSnackbarMessageConsumed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        detailJob?.cancel()
    }


}
