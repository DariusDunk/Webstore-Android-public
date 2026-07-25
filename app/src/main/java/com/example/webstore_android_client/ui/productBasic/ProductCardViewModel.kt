package com.example.webstore_android_client.ui.productBasic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.model.localDTOs.cart.CartSummaryData
import com.example.webstore_android_client.model.requests.cart.CartItemRequest
import com.example.webstore_android_client.repositories.RepositoryProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException


data class ProductCardUiState(
    val isCartLoading: Boolean = false
)
class ProductCardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProductCardUiState())
    val uiState: StateFlow<ProductCardUiState> = _uiState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private val _errorEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    fun addToCart(productCode: String) {
        if (_uiState.value.isCartLoading) return

        viewModelScope.launch {
            _uiState.value = ProductCardUiState(isCartLoading = true)
            try {

                val requestBody =   CartItemRequest(
                        productCode = productCode,
                        doIncrement = true,
                    )

                when(val response = ApiProvider.cartRepository.addOrRemoveFromCart(requestBody))
                {
                    is ApiResult.Success->
                    {
                        RepositoryProvider.cartSummaryDataRepository.update(
                            CartSummaryData(
                                response.data.cartSummary.cartTotalCoins,
                                response.data.cartSummary.cartQuantity
                            )
                        )

                        _toastEvent.emit(response.data.message)
                    }

                    is ApiResult.Failure->
                    {


                        val errorText = if (!response.error.detail.isBlank()) response.error.detail else "Неуспешно добавяне в количката"

                        println("\n----------------------------------\n" +
                                "Failure text in addToCart for ProductCardViewModel: $errorText " +
                                " \n----------------------------------\n")

                        val errorBody = response.error.toString()

                        println(
                            "\n----------------------------------\n" +
                                    "RAW ERROR BODY: $errorBody" +
                                    "\n----------------------------------\n"
                        )


                        _errorEvent.emit(errorText)
                    }

                    is ApiResult.NetworkError->
                    {
                        println("\n----------------------------------\n" +
                                "Netwok error in addToCart for ProductCardViewModel: ${response.exception.message} " +
                                " \n----------------------------------\n")
                        _errorEvent.emit(response.exception.message ?: "Мрежова грешка")
                    }
                }

            } catch (e: HttpException) {
                when (e.code()) {

                    401 -> Unit
                    else -> _errorEvent.emit("Неуспешна заявка")
                }
            } catch (e: Exception) {
                println("Cart request exception: " + e.message)
                _errorEvent.emit("Имаше проблем със заявката за количката")
            } finally {
                _uiState.value = ProductCardUiState(isCartLoading = false)
            }
        }
    }
}
