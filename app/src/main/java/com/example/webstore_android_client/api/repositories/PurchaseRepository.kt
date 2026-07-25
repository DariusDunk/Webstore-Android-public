package com.example.webstore_android_client.api.repositories

import com.example.webstore_android_client.api.services.PurchaseService
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.api.utils.safeApiCall
import com.example.webstore_android_client.model.requests.purchase.PurchaseCompleteRequest
import com.example.webstore_android_client.model.responses.page.PageResponse
import com.example.webstore_android_client.model.responses.purchase.CompactPurchaseResponse
import com.example.webstore_android_client.model.responses.purchase.PurchaseDetailResponse
import com.example.webstore_android_client.model.responses.purchase.SuccessfulPurchaseResponse
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PurchaseRepository(
    private val api: PurchaseService,
    private val context: Context
) {

    suspend fun completePurchase(request: PurchaseCompleteRequest): ApiResult<SuccessfulPurchaseResponse>
    {
        return safeApiCall { api.completePurchase(request) }
    }

    suspend fun getPurchaseHistoryPage(page: Int): ApiResult<PageResponse<CompactPurchaseResponse>> {
        return safeApiCall { api.getPurchaseHistoryPage(page) }
    }

    suspend fun getDetailedPurchaseData(purchaseCode: String): ApiResult<PurchaseDetailResponse>
    {
        return safeApiCall { api.getDetailedPurchaseData(purchaseCode) }
    }

    suspend fun cancelOrder(purchaseCode: String): ApiResult<Unit> {
        return safeApiCall { api.cancelOrder(purchaseCode = purchaseCode) }
    }

    suspend fun requestRefund(purchaseCode: String): ApiResult<Unit>
    {
        return safeApiCall { api.requestRefund(purchaseCode = purchaseCode) }
    }

    suspend fun getInvoicePDF(purchaseCode: String): File? = withContext(Dispatchers.IO) {
        try {
            val responseBody = api.downloadInvoice(purchaseCode)

            val invoiceDir = File(context.cacheDir, "invoices")
            if (!invoiceDir.exists()) {
                invoiceDir.mkdirs()
            }

            val pdfFile = File(invoiceDir, "invoice-$purchaseCode.pdf")

            responseBody.byteStream().use { inputStream ->
                FileOutputStream(pdfFile).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


}