package com.example.webstore_android_client.repositories

import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.repositories.CustomerRepository
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.model.localDTOs.cart.CartSummaryData
import com.example.webstore_android_client.model.localDTOs.customer.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CustomerDataRepository(
    private val cartSummaryDataRepository: CartSummaryDataRepository
) {

    private val _userState =
        MutableStateFlow<UserData?>(null)

    val userState: StateFlow<UserData?> =
        _userState

    fun setUser(user: UserData?) {
        _userState.value = user
    }

    fun loginSuccess(
        userData: UserData,
        cartSummary: CartSummaryData
    ) {
        setUser(userData)
        cartSummaryDataRepository.update(cartSummary)
    }

    fun logout() {

        setUser(null)

        cartSummaryDataRepository.update(
            CartSummaryData(
                0,
                0
            )
        )
    }

    suspend fun loadInitialUser() {
        val customerRepository: CustomerRepository = ApiProvider.customerRepository
        try {

            var response = customerRepository.getUserData()

            if (response is ApiResult.NetworkError) {

                println(
                    "\n----------------------------------\n" +
                            "Fetching userData failed, retrying" +
                            " \n----------------------------------\n"
                )

                response = customerRepository.getUserData()
            }

            when (response) {

                is ApiResult.Success -> {
                    val responseData = response.data
                    val userData =
                        UserData(responseData.username, responseData.customerPfp, responseData.role)
                    setUser(userData)
                    val cartSummary = responseData.cartSummary
                    val cartSummaryData =
                        CartSummaryData(cartSummary.cartTotalCoins, cartSummary.cartQuantity)
                    cartSummaryDataRepository.update(cartSummaryData)
                }

                is ApiResult.Failure -> {

                }

                is ApiResult.NetworkError -> {
                    println(
                        "\n----------------------------------\n" +
                                "Fetching userData retry failed" +
                                " \n----------------------------------\n"
                    )
                }
            }


        } catch (e: Exception) {
            print("User data fetch failed, probably guest..\n" + e.message)
        }


    }
}