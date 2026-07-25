package com.example.webstore_android_client.ui.productBrowsing

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.webstore_android_client.model.responses.page.PageResponse
import com.example.webstore_android_client.model.responses.product.CompactProductResponse

data class ImageSearchSession(
    val imageUri: Uri,
    val categories: List<String>,
    val manufacturers: List<String>,
    val productSnapshot: PageResponse<CompactProductResponse>? = null
)

class ImageSearchViewModel : ViewModel() {
    private val _session = MutableStateFlow<ImageSearchSession?>(null)
    val session = _session.asStateFlow()

    private val _isInferring = MutableStateFlow(false)
    val isInferring = _isInferring.asStateFlow()

    fun setSession(newSession: ImageSearchSession) {
        _session.update { newSession }
    }

    fun clearSession() {
        _session.update { null }
    }

    fun setInferring(status: Boolean) {
        _isInferring.update { status }
    }
}