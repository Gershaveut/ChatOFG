package com.gershaveut.chat_ofg

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.PrivateChat
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

		tempChats.addAll(getGroups())
		tempChats.addAll(getPrivateChats())

		chats = tempChats
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun refreshUsers() {
	GlobalScope.launch {
		users = getUsers()
	}
}