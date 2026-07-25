package com.example.webstore_android_client.ui.userProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.repositories.CustomerRepository
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.events.SessionEvents
import com.example.webstore_android_client.model.responses.customer.CustomerProfileResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val isLoading: Boolean = true,
    val profileData: CustomerProfileResponse? = null,
    val error: String? = null,
)

class UserProfileViewModel(

) : ViewModel() {

    private val customerRepository: CustomerRepository = ApiProvider.customerRepository

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                when (val response = customerRepository.getProfileData()) {
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(profileData = response.data, isLoading = false) }
                    }

                    is ApiResult.Failure -> {

                        val errorText =
                            response.error.detail.ifBlank { "Неуспешно зареждане на профилните данни" }

                        _uiState.update { it.copy(isLoading = false, error = errorText) }
                    }

                    is ApiResult.NetworkError -> {

                        println(
                            "\n----------------------------------\n" +
                                    "Failed to load profile data:  $response.exception.message" +
                                    " \n----------------------------------\n"
                        )

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Неуспешно зареждане на профилните данни"
                            )
                        }
                    }
                }

//                println("SUCCESSFUL FETCH: $response")
//                _uiState.update { it.copy(profileData = response, isLoading = false) }

//                println("----------------------------------UI State for user data: \n${_uiState.value.profileData}----------------------------------")

                // _uiState.update { it.copy(profileData = response, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load profile"
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {

                val response = ApiProvider.authRepository.logoutUser()
                SessionEvents.logout()

                if (response is ApiResult.Failure
                    || response is ApiResult.NetworkError
                ) {
                    print("Logout API failed, but clearing local session anyway: $response")
                }

            } catch (e: Exception) {
                print("Logout API failed, but clearing local session anyway: ${e.message}")
            } finally {
                SessionEvents.logout()
            }
        }
    }
}