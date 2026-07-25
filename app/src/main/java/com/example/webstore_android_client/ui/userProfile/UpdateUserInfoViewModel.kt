package com.example.webstore_android_client.ui.userProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.model.localDTOs.customer.UserData
import com.example.webstore_android_client.model.requests.customer.CustomerDataUpdateRequest
import com.example.webstore_android_client.repositories.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UpdateUserInfoUiState(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val snackbarMessage: String? = null,
)

class UpdateUserInfoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUserInfoUiState())
    val uiState: StateFlow<UpdateUserInfoUiState> = _uiState.asStateFlow()

    fun initUserData(firstName: String, lastName: String, phone: String) {
        _uiState.update { it.copy(isLoading = true) }

        if (firstName.isBlank()
            ||lastName.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    snackbarMessage = "Невалидни данни за потребителя.",
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                firstName = firstName,
                lastName = lastName,
                phone = phone,
            )
        }
        _uiState.update { it.copy(isLoading = false) }
    }

    fun onFirstNameChange(value: String) {
        _uiState.update { it.copy(firstName = value) }
    }

    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value) }
    }

    fun onPhoneChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        if (digitsOnly.length <= 10) {
            _uiState.update { it.copy(phone = digitsOnly) }
        }
    }

    fun onSaveClicked() {
        _uiState.update { it.copy(showConfirmDialog = true) }
    }

    fun onConfirmDialogDismiss() {
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    fun onConfirmSave() {
        _uiState.update { it.copy(showConfirmDialog = false) }
        updateUser()
    }

    private fun updateUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {

               when (val response = ApiProvider.customerRepository.updateUserData(
                    CustomerDataUpdateRequest(
                        uiState.value.firstName,
                        uiState.value.lastName,
                        uiState.value.phone
                    )
                )) {
                   is ApiResult.Failure -> {
                       _uiState.update {
                           it.copy(
                               isLoading = false,
                               snackbarMessage = "Неуспешно обновяване",
                           )
                       }
                   }
                   is ApiResult.NetworkError -> {

                       _uiState.update {
                           it.copy(
                               isLoading = false,
                               snackbarMessage = "Имаше проблем с обновяването на профила.",
                           )
                       }

                       println("\n----------------------------------\n" +
                               "Error updating user data: ${response.exception}" +
                               " \n----------------------------------\n")
                   }
                   is ApiResult.Success -> {

                       val currentUserRole = RepositoryProvider.customerDataRepository.userState.value?.role
                       val currentPfp = RepositoryProvider.customerDataRepository.userState.value?.customerPfp

                       _uiState.update {
                           it.copy(
                               isLoading = false,
                               snackbarMessage = "Успешно обновяване на профила!",
                           )
                       }

                       RepositoryProvider.customerDataRepository.setUser(UserData("${uiState.value.firstName} ${uiState.value.lastName}",
                           currentPfp?:"",
                           currentUserRole?:"customer"))
                   }
               }


            } catch (e: Exception) {
                println("\n----------------------------------\n" +
                        "Error updating user data: ${e.stackTrace}" +
                        " \n----------------------------------\n")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        snackbarMessage = "Имаше проблем с обновяването на профила.",
                    )
                }
            }
        }
    }

    fun onRequestPasswordChange() {
        viewModelScope.launch {
            try {

                when (val response = ApiProvider.customerRepository.requestPasswordUpdate()) {
                    is ApiResult.Failure -> {
                        val errorResponse = response.error
                        _uiState.update {
                            it.copy(snackbarMessage = errorResponse.detail?:"Имаше проблем с обработването на заявката за смяна на паролата.")
                        }
                    }
                    is ApiResult.NetworkError -> {

                        _uiState.update {
                            it.copy(snackbarMessage = "Имаше проблем с обработването на заявката за смяна на паролата.")
                        }

                        println("\n----------------------------------\n" +
                                " Error requesting password change: ${response.exception}" +
                                " \n----------------------------------\n")

                    }
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(snackbarMessage = "Имейл за смяна на паролата беше изпратен успешно")
                        }
                    }
                }


            } catch (e: Exception) {
                _uiState.update {
                    it.copy(snackbarMessage = "Имаше проблем с обработването на заявката за смяна на паролата.")
                }

                println("\n----------------------------------\n" +
                        " Error requesting password change: ${e.message}" +
                        " \n------")
                _uiState.update {
                    it.copy(snackbarMessage = "Имаше проблем с обработването на заявката за смяна на паролата.")
                }
            }
        }
    }

    fun onSnackbarMessageConsumed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
