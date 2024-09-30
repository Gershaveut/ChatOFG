package com.gershaveut.chat_ofg

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gershaveut.chat_ofg.data.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

val syncResponseFlow = MutableSharedFlow<String>()
val sharedFlow = syncResponseFlow.asSharedFlow()

val clientUser get() = Client.user!!

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun App() {
    Client.onSync = {
        scope.launch {
            syncResponseFlow.emit("")
        }
    }

    MaterialTheme {
        val openSettings = remember { mutableStateOf(false) }
        val user = remember { mutableStateOf(Client.user) }

        var connection by remember { mutableStateOf(true) }

        if (openSettings.value) {
            Settings(openSettings)
        } else {
            if (user.value == null) {
                Auth("Auth", openSettings) {
                    user.value = Client.user
                }
            } else {
                scope.launch {
                    Client.handleConnection {
                        connection = it
                    }
                }

                Scaffold(bottomBar = {
                    if (!connection) {
                        Row(
                            Modifier.fillMaxWidth().height(35.dp).background(MaterialTheme.colors.error).padding(start = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Connection lost", color = MaterialTheme.colors.onError)
                        }
                    }
                }) {
                    Menu(user, openSettings)
                }
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
val scope = GlobalScope

@OptIn(DelicateCoroutinesApi::class)
fun auth(name: String, password: String, onAuth: () -> Unit) {
    scope.launch {
        Client.auth(name, password)

        onAuth()
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun refreshChats(onRefresh: () -> Unit) {
    scope.launch {
        Client.chats = Client.getChats()

        onRefresh()
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun refreshUsers(onRefresh: () -> Unit) {
    scope.launch {
        Client.users = Client.getUsers()

        onRefresh()
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun getUser(name: String, onGet: (UserInfo) -> Unit) {
    scope.launch {
        onGet(Client.getUser(name))
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun updateUser() {
    scope.launch {
        Client.updateUser()
    }
}


@OptIn(DelicateCoroutinesApi::class)
fun sendMessage(message: Message, chat: Chat, onCreated: ((Message) -> Unit)? = null) {
    scope.launch {
        Client.sendMessage(message, chat, onCreated)
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun createChat(user: UserInfo, onCreated: ((Chat) -> Unit)? = null) {
    scope.launch {
        Client.createChat(Chat(clientUser, user), onCreated)
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun readMessages(chat: Chat) {
    scope.launch {
        Client.readMessages(chat)
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun sync(onSync: () -> Unit) {
    scope.launch {
        sharedFlow.collect {
            onSync()
        }
    }
}