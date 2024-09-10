package com.gershaveut.chat_ofg

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.native.concurrent.ThreadLocal

@Composable
fun App() {
	MaterialTheme {
		getChats()

		Menu()
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun getChats() {
	chats.clear()

	GlobalScope.launch {
		chats.addAll(getGroups())
		chats.addAll(getPrivateChats())
	}
}