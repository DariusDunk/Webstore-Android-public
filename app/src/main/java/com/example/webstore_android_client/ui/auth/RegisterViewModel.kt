package com.example.webstore_android_client.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.model.requests.auth.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class RegisterUiState(
    val name: String = "",
    val familyName: String = "",
    val email: String = "",
    val password: String = "",
    val repeatedPassword: String = "",
    val nameError: String = "",
    val familyNameError: String = "",
    val emailError: String = "",
    val passwordError: String = "",
    val repeatedPasswordError: String = "",
    val isLoading: Boolean = false,
    var showSuccessDialog: Boolean = false,
)

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()


    fun onNameChange(value: String) {

        val isValidInput = value.all { ch -> ch.isLetter() || ch.isWhitespace() }

        _uiState.update { currentState ->
            if (isValidInput) {
                val emptyError = if (value.trim().isEmpty()) "Моля въведете име" else ""

                currentState.copy(
                    name = value,
                    nameError = emptyError
                )
            } else {
                currentState.copy(
                    nameError = "Името може да съдържа само букви"
                )
            }
        }
    }

    fun onFamilyNameChange(value: String) {

        val isValidInput = value.all { ch -> ch.isLetter() || ch.isWhitespace() }

        _uiState.update { currentState ->
            if (isValidInput) {
                val error = if (value.trim().isEmpty()) "Моля въведете фамилия" else ""
               currentState.copy(
                   familyName = value,
                   familyNameError = error
               )
            } else {
                currentState.copy(
                    familyNameError = "Фамилията може да съдържа само букви"
                )
            }
        }
    }

    fun onEmailChange(value: String) {
        val error = if (value.trim().isNotEmpty() && !value.contains('@'))
            "Моля въведете валиден иmейл" else ""
        _uiState.update { it.copy(email = value, emailError = error) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { current ->
            val pwError = when {
                value.isEmpty()       -> ""
                value.length < 12     -> "Паролата трябва да е поне 12 символа"
                else                  -> ""
            }
            val repError = when {
                current.repeatedPassword.isEmpty()          -> current.repeatedPasswordError
                current.repeatedPassword != value           -> "Двете пароли не съвпадат"
                else                                        -> ""
            }
            current.copy(password = value, passwordError = pwError, repeatedPasswordError = repError)
        }
    }

    fun onRepeatedPasswordChange(value: String) {
        _uiState.update { current ->
            val error = if (value.isNotEmpty() && value != current.password)
                "Двете пароли не съвпадат" else ""
            current.copy(repeatedPassword = value, repeatedPasswordError = error)
        }
    }

    private fun validate(): Boolean {
        val current = _uiState.value
        var updated = current
        var isValid = true

        if (current.name.trim().isEmpty()) {
            updated = updated.copy(nameError = "Непопълнено поле"); isValid = false
        }
        if (current.familyName.trim().isEmpty()) {
            updated = updated.copy(familyNameError = "Непопълнено поле"); isValid = false
        }
        if (current.email.trim().isEmpty()) {
            updated = updated.copy(emailError = "Непопълнено поле"); isValid = false
        }
        when {
            current.password.trim().isEmpty() -> {
                updated = updated.copy(passwordError = "Непопълнено поле"); isValid = false
            }
            current.password.trim() == current.email.trim() -> {
                updated = updated.copy(passwordError = "Паролата трябва да е различна от иmейла")
                isValid = false
            }
        }
        if (current.password != current.repeatedPassword) {
            updated = updated.copy(repeatedPasswordError = "Двете пароли не съвпадат"); isValid = false
        }

        _uiState.value = updated
        return isValid
    }


    fun register() {
        if (!validate()) return

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {

                val requestBody = RegisterRequest(
                    state.name,
                    state.familyName,
                    state.email,
                    state.password
                )

                when(val response = ApiProvider.authRepository.register(requestBody)) {
                    is ApiResult.Failure -> {
                        val errorText = response.error.detail.ifEmpty { "Неуспешна регистрация" }
                        _uiState.update { it.copy(isLoading = false, emailError = errorText) }
                    }
                    is ApiResult.NetworkError -> {

                        println("\n----------------------------------\n" +
                                "Registration exception: ${response.exception.message}" +
                                " \n----------------------------------\n")

                        _uiState.update { it.copy(isLoading = false, emailError = "Грешка при регистрация") }
                    }
                    is ApiResult.Success<*> -> {
                        _uiState.update { it.copy(isLoading = false, showSuccessDialog = true) }
                    }
                }


            } catch (e: HttpException) {
                val msg = when (e.code()) {
                    409  -> "Имейлът вече е регистриран"
                    else -> "Неуспешна регистрация"
                }
                _uiState.update { it.copy(isLoading = false, emailError = msg) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, emailError = "Грешка при регистрация")
                }
            }
        }
    }

}
