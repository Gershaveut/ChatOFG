package com.gershaveut.chat_ofg

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Settings(openSettings: MutableState<Boolean>) {
    Column {
        TopAppBar(
            title = {
                Text("Settings")
            },
            navigationIcon = {
                IconButton({
                    openSettings.value = false
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column {
            Filed("Server", Client.host, "Server host", HOST_DEFAULT) { text ->
                Client.host = text
            }
        }
    }
}

@Composable
fun Filed(name: String, value: String, description: String? = null, defaultValue: String? = null, onValueChanged: (text: String) -> Unit,) {
    var textFiled by remember { mutableStateOf(if (value != defaultValue) value else "") }

    SettingsRow {
        SettingInfo(name, description)

        TextField(
            textFiled, { text ->
                textFiled = text

                if (text.isNotEmpty())
                    onValueChanged(text)
                else if (defaultValue != null)
                    onValueChanged(defaultValue)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { if (defaultValue != null) Text(defaultValue) }
        )
    }
}

@Composable
fun SettingInfo(name: String, description: String? = null) {
    Column ( verticalArrangement = Arrangement.Center, modifier = Modifier.padding(5.dp) ) {
        Text(name)

        if (description != null)
            Text(description, fontSize = 10.sp, color = Colors.BACKGROUND_VARIANT)
    }
}

@Composable
fun SettingsRow(content: @Composable () -> Unit) {
    Row( modifier = Modifier.padding(5.dp) ) {
        content()
    }
}