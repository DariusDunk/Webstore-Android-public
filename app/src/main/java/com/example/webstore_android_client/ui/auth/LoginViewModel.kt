package com.example.webstore_android_client.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.model.localDTOs.cart.CartSummaryData
import com.example.webstore_android_client.model.localDTOs.customer.UserData
import com.example.webstore_android_client.model.requests.auth.LoginRequest
import com.example.webstore_android_client.repositories.RepositoryProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String = "",
    val passwordError: String = "",
    val isLoading: Boolean = false,
    val rememberMe: Boolean = false,
    val isForgotPassword: Boolean = false,
    val navigateToMain: Boolean = false,
    val toastMessage: String = ""
)

sealed interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
}

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    private var pendingUserData: UserData? = null
    private var pendingCartSummary: CartSummaryData? = null
    private val _toastEvent = MutableSharedFlow<UiEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val toastEvent: SharedFlow<UiEvent> = _toastEvent.asSharedFlow()


    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = "") }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = "") }
    }

    fun onRememberMeChange(value: Boolean) {
        _uiState.update { it.copy(rememberMe = value) }
    }

    fun setForgotPassword(value: Boolean) {
        _uiState.update {
            it.copy(
                isForgotPassword = value,
                emailError = "",
                passwordError = ""
            )
        }
    }


    fun login() {
        val state = _uiState.value

        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Моля, въведете имейл") }
            return
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Моля, въведете парола") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {

                val requestBody = LoginRequest(
                    email = state.email,
                    password = state.password,
                    rememberMe = state.rememberMe
                )

                when(val response = ApiProvider.authRepository.loginUser(requestBody))
                {
                    is ApiResult.Success ->
                    {
                        val responseData = response.data
                        pendingUserData = UserData(
                            responseData.loginResponse.username,
                            responseData.loginResponse.customerPfp,
                            responseData.loginResponse.role
                        )
                        pendingCartSummary = CartSummaryData(
                            responseData.cartSummaryResponse.cartTotalCoins,
                            responseData.cartSummaryResponse.cartQuantity
                        )

                        _uiState.update { it.copy(isLoading = false, navigateToMain = true) }
                    }
                    is ApiResult.Failure ->
                    {
                        val errorText = response.error.detail.ifEmpty { "Грешка при влизане" }

                        _uiState.update { it.copy(isLoading = false, emailError = errorText) }
                    }

                    is ApiResult.NetworkError ->
                    {
                        println("General exception for login: " + response.exception.message)

                        _uiState.update { it.copy(isLoading = false, emailError = "Грешка при влизане") }

                    }
               }


            } catch (e: HttpException) {
                val msg = if (e.code() == 401) "Грешен имейл или парола"
                else "Грешка при влизане"
                _uiState.update { it.copy(isLoading = false, emailError = msg) }
            } catch (e: Exception) {
                println("General exception for login: " + e.message)
                _uiState.update { it.copy(isLoading = false, emailError = "Грешка при влизане") }
            }
        }
    }


    fun submitForgotPassword() {
        val state = _uiState.value
        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Моля, въведете имейл") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {

                when (val response = ApiProvider.authRepository.forgotPassword(state.email)) {
                    is ApiResult.Success -> {
                        _toastEvent.emit(UiEvent.ShowToast("Линк за възстановяване е изпратен на имейла ви"))
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isForgotPassword = false,
                                emailError = ""
                            )
                        }
                    }
                    is ApiResult.Failure -> _uiState.update {
                        val errorText = response.error.detail.ifEmpty { "Грешка при изпращане. Опитайте отново." }

                        it.copy(
                            isLoading = false,
                            emailError = errorText
                        )
                    }
                    is ApiResult.NetworkError -> {
                        println("Forgotten password error: ${response.exception.message}")

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                emailError = "Мрежова грешка"
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                println("Forgotten password error: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        emailError = "Грешка при изпращане. Опитайте отново."
                    )
                }
            }
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun onNavigatedToMain() {
        _uiState.update { it.copy(navigateToMain = false) }

        if (pendingUserData == null || pendingCartSummary == null) {
            return
        }


        println("Pending user data: $pendingUserData")
        println("Pending cart summary: $pendingCartSummary")
        GlobalScope.launch(Dispatchers.Main)
        {
            delay(500)
            RepositoryProvider.customerDataRepository.loginSuccess(
                userData = pendingUserData!!,
                cartSummary = pendingCartSummary!!
            )

        }
    }
}
