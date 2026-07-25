package com.example.webstore_android_client.ui.dialogue

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun LoginPromptDialogue(message: String, navController: NavController, onClick: () -> Unit) {

        AlertDialog(
            title = {
                Text("Липсващ профил")
            },
            text = {
                Text(message.ifEmpty { "Моля влезте в профила си" })
            },
            confirmButton = {
                Button(
                    onClick = {

                        onClick()

                        navController.navigate("login") {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                ) {
                    Text("Влизане")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        onClick()
                    })
                {
                    Text("Отказ")
                }
            },
            onDismissRequest = {
                onClick()
            }
        )
}