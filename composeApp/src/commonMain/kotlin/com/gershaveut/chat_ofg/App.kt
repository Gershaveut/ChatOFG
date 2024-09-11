package com.gershaveut.chat_ofg

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.Message
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun App() {
	refreshChats()
	refreshUsers()

	MaterialTheme {
		Menu()
	}
}

@OptIn(DelicateCoroutinesApi::class)
val scope = GlobalScope

@OptIn(DelicateCoroutinesApi::class)
fun refreshChats() {
	scope.launch {
		val tempChats: MutableList<Chat> = mutableListOf()

		tempChats.addAll(Client.getGroups())
		tempChats.addAll(Client.getPrivateChats())

		Client.chats = tempChats
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun refreshUsers() {
	scope.launch {
		Client.users = Client.getUsers()
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun sendMessage(message: Message, chat: Chat, onCreated: () -> Unit) {
	scope.launch {
		Client.sendMessage(message, chat, onCreated)
	}
}