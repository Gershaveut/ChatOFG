package com.gershaveut.chat_ofg

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Auth(type: String, onConfirm: () -> Unit) {
    Column( verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally ) {
        var name by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Text(type)

        TextField(
            name, { text ->
                name = text
            },
            modifier = Modifier.size(100.dp, 25.dp),
            placeholder = { Text("Name") }
        )

        TextField(
            password, { text ->
                password = text
            },
            modifier = Modifier.size(100.dp, 25.dp),
            placeholder = { Text("Password") }
        )

        Button(onConfirm) {
            Text("Confirm")
        }
    }
}