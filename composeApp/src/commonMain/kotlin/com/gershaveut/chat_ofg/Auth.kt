package com.gershaveut.chat_ofg

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun Auth(type: String, openSettings: MutableState<Boolean>, onConfirm: () -> Unit) {
    Scaffold(topBar = {
        IconButton({
            openSettings.value = true
        } ) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings")
        }
    }) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
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
                placeholder = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Button({
                if (name.isNotEmpty() && password.isNotEmpty())
                    onConfirm(name, password)
            }) {
                Text("Confirm")
            }
        }
    }
}