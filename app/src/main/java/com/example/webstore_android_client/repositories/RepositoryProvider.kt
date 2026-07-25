package com.example.webstore_android_client.repositories

import com.example.webstore_android_client.api.ApiProvider

object RepositoryProvider {

    val cartSummaryDataRepository =
        CartSummaryDataRepository()

    val customerDataRepository =
        CustomerDataRepository(
            cartSummaryDataRepository
        )
}