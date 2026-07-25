package com.example.webstore_android_client.ui.mainMenu
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.webstore_android_client.api.ApiProvider
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.tools.prepareImagePart
import com.example.webstore_android_client.ui.productBrowsing.ImageSearchSession
import com.example.webstore_android_client.ui.productBrowsing.ImageSearchViewModel
import kotlinx.coroutines.launch

class MenuViewModel: ViewModel() {


    fun processImage(
        context: Context,
        uri: Uri,
        imageSearchViewModel: ImageSearchViewModel,
        navController: NavHostController
    ) {
        viewModelScope.launch {
            if (imageSearchViewModel.isInferring.value) return@launch

            imageSearchViewModel.setInferring(true)

            val imagePart = prepareImagePart(context, uri)
            if (imagePart == null) {
                Toast.makeText(context, "Грешка при зареждане на изображението", Toast.LENGTH_SHORT).show()
                imageSearchViewModel.setInferring(false)
                return@launch
            }

            try {
                val response = ApiProvider.productRepository.inferProducts(imagePart)

                if (response is ApiResult.Success) {
                    val data = response.data


                    if (data.productPage.content.isEmpty()) {
                        Toast.makeText(context, "Не бяха открити продукти с това изображение", Toast.LENGTH_LONG).show()
                        imageSearchViewModel.clearSession()
                    } else {
                        imageSearchViewModel.setSession(
                            ImageSearchSession(
                                imageUri = uri,
                                categories = data.categoryNames,
                                manufacturers = data.manufacturerNames,
                                productSnapshot = data.productPage
                            )
                        )

                        navController.navigate("products/image_search")
                    }
                } else {
                    Toast.makeText(context, "Възникна грешка със заявката", Toast.LENGTH_LONG).show()
                    imageSearchViewModel.clearSession()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Мрежова грешка", Toast.LENGTH_SHORT).show()
                imageSearchViewModel.clearSession()
            } finally {
                imageSearchViewModel.setInferring(false)
            }
        }
    }
}