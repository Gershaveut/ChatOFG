package com.gershaveut.chat_ofg

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gershaveut.chat_ofg.data.Chat

@Composable
fun AppSettings(openSettings: MutableState<Boolean>) {
    Column {
        var password: String? = null

        SettingsBar(openSettings, "Settings") {
            if (Client.user != null) {
                if (password != null)
                    Client.user!!.password = password!!

                updateUser()
            }
        }

        LazyColumn {
            item {
                if (Client.user != null) {
                    Category("User") {
                        Filed("Display name", Client.user!!.displayName, Client.user!!.name) {
                            Client.user!!.displayName = it
                        }

                        FiledNullable("Description", Client.user!!.description) {
                            Client.user!!.description = it
                        }

                        FiledNullable("Password") {
                            password = it
                        }
                    }
                }

                Category("Application") {
                    Filed("Server", Client.host, "Server host", HOST_DEFAULT) {
                        Client.host = it
                    }
                }
            }
        }
    }
}

@Composable
fun ChatSettings(openSettings: MutableState<Boolean>, chat: Chat) {
    Column {
        SettingsBar(openSettings, "Settings " + chat.getNameClient()) {
            updateChat(chat)
        }

        LazyColumn {
            item {
                Category("Info") {
                    val readOnly = chat.members.size < 3

                    Filed("Name", chat.getNameClient(), chat.getNameClient(), readOnly = readOnly) {
                        chat.setName(it)
                    }

                    FiledNullable("Description", chat.description, readOnly = readOnly) {
                        chat.description = it
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsBar(openSettings: MutableState<Boolean>, text: String, onClose: () -> Unit) {
    TopAppBar(
        title = {
            Text(text)
        },
        navigationIcon = {
            IconButton({
                openSettings.value = false
                onClose()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    )
}

@Composable
fun Category(name: String, content: @Composable () -> Unit) {
    Column {
        Row(
            Modifier.background(MaterialTheme.colors.secondary).height(50.dp).fillMaxWidth().padding(start = 10.dp),
            Arrangement.Center,
            Alignment.CenterVertically
        ) {
            Text(name, fontSize = 18.sp, color = MaterialTheme.colors.onSecondary)
        }

        content()
    }
}

@Composable
fun FiledNullable(
    name: String,
    value: String? = null,
    defaultValue: String? = null,
    description: String? = null,
    readOnly: Boolean = false,
    onValueChanged: (text: String?) -> Unit,
) {
    var textFiled by remember { mutableStateOf(if (value != defaultValue) value else "") }

    SettingsRow {
        SettingInfo(name, description)

        TextField(
            textFiled ?: "", { text ->
                textFiled = text

                if (text.isNotEmpty())
                    onValueChanged(text)
                else if (defaultValue != null)
                    onValueChanged(defaultValue)
                else
                    onValueChanged(null)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { if (defaultValue != null) Text(defaultValue) },
            readOnly = readOnly
        )
    }
}

@Composable
fun Filed(
    name: String,
    value: String?,
    defaultValue: String,
    description: String? = null,
    readOnly: Boolean = false,
    onValueChanged: (text: String) -> Unit,
) {
    FiledNullable(name, value, description, defaultValue, readOnly) {
        onValueChanged(it!!)
    }
}

@Composable
fun SettingInfo(name: String, description: String? = null) {
    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.width(200.dp).padding(10.dp).padding(end = 50.dp)) {
        Text(name)

        if (description != null)
            Text(description, fontSize = 10.sp, color = Colors.BACKGROUND_VARIANT)
    }
}

@Composable
fun SettingsRow(content: @Composable () -> Unit) {
    Row(modifier = Modifier.padding(5.dp)) {
        content()
    }
}