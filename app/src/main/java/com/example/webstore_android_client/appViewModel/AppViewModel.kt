package com.example.webstore_android_client.appViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.webstore_android_client.repositories.CartSummaryDataRepository
import com.example.webstore_android_client.repositories.CustomerDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val customerRepo: CustomerDataRepository,
    private val cartRepo: CartSummaryDataRepository
) : ViewModel() {

    val user = customerRepo.userState
    val cart = cartRepo.cart
    val isGuestFlow: StateFlow<Boolean> = customerRepo.userState
        .map { user -> user == null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    private val _loading =
        MutableStateFlow(true)

    val loading: StateFlow<Boolean> =
        _loading

    init {
        bootstrap()
    }


    private fun bootstrap() {

        viewModelScope.launch {

            _loading.value = true
            try {
                customerRepo.loadInitialUser()
            } catch (e: Exception) {
                print("User data fetch failed, probably guest..\n" + e.message)
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearUserData() {
        customerRepo.logout()
    }

}