package com.example.webstore_android_client.ui.mainMenu

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.webstore_android_client.ui.productBrowsing.ImageSearchViewModel
import com.example.webstore_android_client.ui.theme.*
import java.io.File
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat

@Composable
fun TopSearchBar(
    viewModel: TopSearchBarViewModel = viewModel(),
    imageSearchViewModel: ImageSearchViewModel,
    navController: NavHostController,
    onProcessImage: (Uri) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val mainBgColor = if (isDark) MainBgDark else MainBgLight
    val barBgColor = if (isDark) DarkCustom else WhiteCustom
    val textColor = if (isDark) Color.White else Color.Black
    val orangeColor = if (isDark) OrangeDark else OrangeLight

    val uiState by viewModel.uiState.collectAsState()
    val imageSession by imageSearchViewModel.session.collectAsState()
    val isInferring by imageSearchViewModel.isInferring.collectAsState()
    val context = LocalContext.current
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onProcessImage(uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            onProcessImage(tempCameraUri!!)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val uri = context.createTempImageUri()
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(
                context,
                "Нужен е достъп до камерата за тази функция.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Surface(modifier = Modifier.fillMaxWidth(), color = mainBgColor) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(50.dp)
                .clip(CircleShape)
                .background(barBgColor),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (imageSession != null) {
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    AsyncImage(
                        model = imageSession!!.imageUri,
                        contentDescription = "Image Search Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    IconButton(
                        onClick = { imageSearchViewModel.clearSession() },
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .background(Color.Red, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).padding(start = if (imageSession != null) 12.dp else 20.dp, end = 8.dp)) {
                if (uiState.query.isEmpty() && imageSession == null) {
                    Text("Търсене на продукти...", color = Color.Gray)
                } else if (imageSession != null) {
                    Text("Търсене по изображение...", color = Color.Gray)
                }

                BasicTextField(
                    value = if (imageSession != null) "" else uiState.query,
                    onValueChange = viewModel::onQueryChange,
                    enabled = imageSession == null && !isInferring,
                    textStyle = LocalTextStyle.current.copy(color = textColor),
                    cursorBrush = SolidColor(orangeColor),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            if (!isInferring) {
                IconButton(onClick = { showImageSourceDialog = true }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Търсене по изображение", tint = Color.Gray)
                }
            }

            Box(modifier = Modifier.fillMaxHeight().width(60.dp).background(orangeColor), contentAlignment = Alignment.Center) {
                IconButton(
                    enabled = !isInferring,
                    onClick = {
                        if (imageSession != null) navController.navigate("products/image_search")
                        else navController.navigate("products/search/${Uri.encode(uiState.query)}")
                    }
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Търсене", tint = Color.White)
                }
            }
        }

        DropdownMenu(
            expanded = uiState.openSuggestions,
            onDismissRequest = { viewModel.dismissSuggestions() },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(barBgColor)
        ) {
            uiState.suggestions.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        viewModel.selectSuggestion(item)

                        navController.navigate("products/search/${Uri.encode(item.trim())}")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text(text = "Изберете източник", color = textColor) },
            text = { Text(text = "Искате ли да направите снимка или да изберете от галерията?", color = textColor) },
            containerColor = barBgColor,
            confirmButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false

                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            val uri = context.createTempImageUri()
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                ) {
                    Text("Камера", color = orangeColor)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Text("Галерия", color = orangeColor)
                }
            }
        )
    }
}


fun Context.createTempImageUri(): Uri {
    val imageDir = File(cacheDir, "camera_images")
    if (!imageDir.exists()) {
        imageDir.mkdirs()
    }

    val tempFile = File.createTempFile("search_image_", ".jpg", imageDir).apply {
        createNewFile()
        deleteOnExit()
    }

    return FileProvider.getUriForFile(
        this,
        "${packageName}.fileprovider",
        tempFile
    )

}