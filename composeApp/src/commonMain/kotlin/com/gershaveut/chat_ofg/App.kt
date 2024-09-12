package com.gershaveut.chat_ofg

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.Message
import com.gershaveut.chat_ofg.data.PrivateChat
import com.gershaveut.chat_ofg.data.User
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        var user by remember { mutableStateOf(Client.user) }

        if (user == null) {
            Auth("Auth") { name, password ->
                Client.authName = name
                Client.authPassword = password

                auth {
                    user = Client.user
                }
            }
        } else {
            Menu()
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
        val tempChats = mutableSetOf<Chat>()

        tempChats.addAll(Client.getGroups())
        tempChats.addAll(Client.getPrivateChats())

        Client.chats = tempChats
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun refreshUsers(onRefresh: () -> Unit) {
    scope.launch {
        Client.users = Client.getUsers()
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
        Client.createPrivateChat(PrivateChat(Client.user!!, user, Client.getDataTime()), onCreated)
    }
}