package com.gershaveut.chat_ofg

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun App() {
	MaterialTheme {
		loadChats()
		loadUsers()

		Menu()
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun loadChats() {
	chats.clear()

	GlobalScope.launch {
		chats.addAll(getGroups())
		chats.addAll(getPrivateChats())
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun loadUsers() {
	GlobalScope.launch {
		users = getUsers()
	}
}