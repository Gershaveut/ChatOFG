package com.gershaveut.chat_ofg

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import com.gershaveut.chat_ofg.data.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

val syncResponseFlow = MutableSharedFlow<String>()
val sharedFlow = syncResponseFlow.asSharedFlow()

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

        if (openSettings.value) {
            Settings(openSettings)
        } else {
            if (user.value == null) {
                Auth("Auth", openSettings)
            } else {
                scope.launch {
                    Client.handleConnection()
                }

                Menu(user, openSettings)
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
val scope = GlobalScope

@OptIn(DelicateCoroutinesApi::class)
fun auth(name: String, password: String) {
    scope.launch {
        Client.auth(name, password)
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
fun sendMessage(message: Message, chat: Chat, onCreated: ((Message) -> Unit)? = null) {
    scope.launch {
        Client.sendMessage(message, chat, onCreated)
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun createChat(user: User, onCreated: ((Chat) -> Unit)? = null) {
    scope.launch {
        Client.createPrivateChat(PrivateChat(Client.user!!, user, getCurrentDataTime()), onCreated)
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun readMessages(chat: Chat) {
    chat.getMessagesChat().forEach {
        if (it.owner.name != Client.user!!.name)
            it.messageStatus = MessageStatus.Read
    }

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