package com.example.webstore_android_client.api

import android.content.Context
import com.example.webstore_android_client.api.repositories.AttributeRepository
import com.example.webstore_android_client.api.repositories.AuthRepository
import com.example.webstore_android_client.api.repositories.CartRepository
import com.example.webstore_android_client.api.repositories.CategoryRepository
import com.example.webstore_android_client.api.repositories.CustomerRepository
import com.example.webstore_android_client.api.repositories.ProductRepository
import com.example.webstore_android_client.api.repositories.PurchaseRepository
import com.example.webstore_android_client.api.services.AttributeService
import com.example.webstore_android_client.api.services.AuthService
import com.example.webstore_android_client.api.services.CartService
import com.example.webstore_android_client.api.services.CategoryService
import com.example.webstore_android_client.api.services.CustomerService
import com.example.webstore_android_client.api.services.ProductService
import com.example.webstore_android_client.api.services.PurchaseService
import retrofit2.Retrofit

object ApiProvider {
    private lateinit var retrofit: Retrofit

    //---------------------------------- SERVICES ----------------------------------
    private val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    private val customerService: CustomerService by lazy {
        retrofit.create(CustomerService::class.java)
    }

    private val productService: ProductService by lazy {
        retrofit.create(ProductService::class.java)
    }

    private val cartService: CartService by lazy {
        retrofit.create(CartService::class.java)
    }

    private val categoryService: CategoryService by lazy {
        retrofit.create(CategoryService::class.java)
    }

    private val attributeService: AttributeService by lazy {
        retrofit.create(AttributeService::class.java)
    }

    private val purchaseService: PurchaseService by lazy {
        retrofit.create(PurchaseService::class.java)
    }
//---------------------------------- REPOSITORIES ----------------------------------


    private lateinit var applicationContext: Context

    val purchaseRepository: PurchaseRepository by lazy {
        PurchaseRepository(purchaseService, applicationContext)
    }

    fun initialize(context: Context) {
        this.applicationContext = context.applicationContext
    }
    val attributeRepository: AttributeRepository by lazy {
        AttributeRepository(attributeService)
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(categoryService)
    }

    val productRepository: ProductRepository by lazy {
        ProductRepository(productService)
    }

    val customerRepository: CustomerRepository by lazy {
        CustomerRepository(customerService)
    }

    val cartRepository: CartRepository by lazy {
        CartRepository(cartService)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authService)
    }

    fun init(retrofit: Retrofit) {
        ApiProvider.retrofit = retrofit
    }
}
