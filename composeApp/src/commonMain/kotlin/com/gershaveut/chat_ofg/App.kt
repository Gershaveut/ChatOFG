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
                Auth("Auth", openSettings) { name, password ->
                    Client.authName = name
                    Client.authPassword = password

                    auth {
                        user.value = Client.user
                    }
                }
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
fun auth(onAuth: () -> Unit) {
    scope.launch {
        Client.user = Client.auth()
        onAuth()
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun refreshChats(onRefresh: () -> Unit) {
    scope.launch {
        val tempChats = mutableListOf<Chat>()

        tempChats.addAll(Client.getGroups())
        tempChats.addAll(Client.getPrivateChats())

        Client.chats = tempChats

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