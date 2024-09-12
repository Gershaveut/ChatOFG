package com.gershaveut.chat_ofg

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Auth(type: String, onConfirm: (name: String, password: String) -> Unit) {
    Column( verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize() ) {
        var name by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Text(type)

        val fieldModifier = Modifier.size(200.dp, 75.dp).padding(5.dp)

        TextField(
            name, { text ->
                name = text
            },
            modifier = fieldModifier,
            placeholder = { Text("Name") }
        )

        TextField(
            password, { text ->
                password = text
            },
            modifier = fieldModifier,
            placeholder = { Text("Password") }
        )

        Button({
            if (name.isNotEmpty() && password.isNotEmpty())
                onConfirm(name, password)
            }) {
            Text("Confirm")
        }
    }
}