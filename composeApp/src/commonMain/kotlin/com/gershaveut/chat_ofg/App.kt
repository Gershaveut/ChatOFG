package com.gershaveut.chat_ofg

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.Message
import com.gershaveut.chat_ofg.data.UserInfo
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
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
	Napier.base(DebugAntilog())
	
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
			AppSettings(openSettings)
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
							Modifier.fillMaxWidth().height(35.dp)
								.background(MaterialTheme.colors.error)
								.padding(start = 5.dp),
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

fun Chat.getNameClient() = this.getName(Client.user)

fun error(text: String) {
	Napier.e(text, tag = "Client")
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
		Client.updateUser {
			error(it)
		}
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun sendMessage(message: Message, chat: Chat, onCreated: ((Message) -> Unit)? = null) {
	scope.launch {
		Client.sendMessage(message, chat, onCreated) {
			error(it)
		}
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun createChat(chat: Chat, onCreated: ((Chat) -> Unit)? = null) {
	scope.launch {
		Client.createChat(chat, onCreated) {
			error(it)
		}
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun updateChat(chat: Chat) {
	scope.launch {
		Client.updateChat(chat) {
			error(it)
		}
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun inviteChat(userName: String, chat: Chat, onCreatedGroup: (() -> Unit)? = null) {
	scope.launch {
		Client.inviteChat(userName, chat, onCreatedGroup) {
			error(it)
		}
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun updatePassword(password: String) {
	scope.launch {
		Client.updatePassword(password) {
			error(it)
		}
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun deleteChat(chat: Chat, onDeleted: ((Chat) -> Unit)? = null) {
	scope.launch {
		Client.deleteChat(chat, onDeleted) {
			error(it)
		}
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun leaveChat(chat: Chat, onDeleted: ((Chat) -> Unit)? = null) {
	scope.launch {
		Client.leaveChat(chat, onDeleted) {
			error(it)
		}
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun kickChat(userName: String, chat: Chat) {
	scope.launch {
		Client.kickChat(userName, chat) {
			error(it)
		}
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun adminChat(userName: String, chat: Chat) {
	scope.launch {
		Client.adminChat(userName, chat) {
			error(it)
		}
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun readMessages(chat: Chat) {
	scope.launch {
		Client.readMessages(chat) {
			error(it)
		}
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun deletedMessages(message: Message, chat: Chat) {
	scope.launch {
		Client.deleteMessage(message, chat) {
			error(it)
		}
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun editMessages(newText: String, message: Message, chat: Chat) {
	scope.launch {
		Client.editMessage(newText, message, chat) {
			error(it)
		}
	}
}

@OptIn(DelicateCoroutinesApi::class)
fun sync(onSync: () -> Unit) {
	scope.launch {
		sharedFlow.collect {
			try {
				onSync()
			} catch (_: Exception) {
			
			}
		}
	}
}