package com.gershaveut.chat_ofg

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import chatofg.composeapp.generated.resources.Res
import chatofg.composeapp.generated.resources.auth
import chatofg.composeapp.generated.resources.lost
import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.Message
import com.gershaveut.chat_ofg.data.UserInfo
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random
import kotlin.random.nextUInt

val syncResponseFlow = MutableSharedFlow<String>()
val sharedFlow = syncResponseFlow.asSharedFlow()

val clientUser get() = Client.user!!

lateinit var onAction: (String) -> Unit

@Composable
fun App() {
	Napier.base(DebugAntilog())
	
	scope = rememberCoroutineScope()
	
	Client.onSync = {
		debug("Sync")
		
		scope.launch {
			syncResponseFlow.emit("")
		}
	}
	
	MaterialTheme {
		val openSettings = remember { mutableStateOf(false) }
		val user = remember { mutableStateOf(Client.user) }
		
		var connection by remember { mutableStateOf(true) }
		
		Column {
			if (DEBUG) {
				Row(
					Modifier.background(color = Color.Gray).fillMaxWidth().padding(5.dp),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					LazyRow {
						item {
							val modifier = Modifier.padding(5.dp)
							
							Button({
								sync()
							}, modifier) {
								Text("Sync")
							}
							
							Button({
								auth("User ${Random.nextUInt()}", "test") {
									debug("Create user")
									exit()
								}
							}, modifier) {
								Text("Create test user")
							}
						}
					}
					
					var lastAction by remember { mutableStateOf("") }
					
					Text(lastAction)
					
					onAction = {
						lastAction = it
					}
				}
			}
			
			Scaffold(bottomBar = {
				if (!connection && user.value != null) {
					ConnectLost()
				}
			}) {
				if (openSettings.value) {
					AppSettings(openSettings)
				} else {
					val chats = remember { mutableStateOf(Client.chats) }
					
					if (user.value == null) {
						Auth(stringResource(Res.string.auth), openSettings) {
							user.value = Client.user
							
							scope.launch {
								Client.handleConnection {
									if (it) {
										info("Connected")
									} else
										warning("Connection lost")
									
									connection = it
								}
							}
						}
					} else {
						Menu(user, openSettings, chats)
					}
				}
			}
		}
	}
}

@Composable
fun ConnectLost() {
	Row(
		Modifier.fillMaxWidth().height(35.dp)
			.background(MaterialTheme.colors.error)
			.padding(start = 5.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(stringResource(Res.string.lost), color = MaterialTheme.colors.onError)
	}
}

fun Chat.getNameClient() = this.getName(Client.user)

const val CLIENT_TAG = "Client"

fun warning(text: String) {
	Napier.w(text, tag = CLIENT_TAG)
}

fun debug(text: String) {
	if (DEBUG)
		Napier.d(text, tag = CLIENT_TAG)
}

fun info(text: String) {
	Napier.i(text, tag = CLIENT_TAG)
	onAction(text)
}

fun error(text: String) {
	Napier.e(text, tag = CLIENT_TAG)
	onAction(text)
}

fun exit() {
	Client.user = null
	
	Client.users.clear()
	Client.chats.clear()
}

lateinit var scope: CoroutineScope

fun tryScopeLaunch(block: suspend CoroutineScope.() -> Unit) {
	scope.launch {
		try {
			block()
		} catch (e: Exception) {
			error(e.toString())
		}
	}
}

fun sync() {
	tryScopeLaunch {
		Client.sync()
	}
}

fun auth(name: String, password: String, onAuth: () -> Unit) {
	info("Auth")
	
	tryScopeLaunch {
		Client.auth(name, password)
		
		onAuth()
	}
}

fun refreshChats(onRefresh: () -> Unit) {
	info("Refresh Chats")
	
	tryScopeLaunch {
		Client.chats = Client.getChats()
		
		onRefresh()
	}
}

fun refreshUsers(onRefresh: () -> Unit) {
	info("Refresh users")
	
	tryScopeLaunch {
		Client.users = Client.getUsers()
		
		onRefresh()
	}
}

fun getUser(name: String, onGet: (UserInfo) -> Unit) {
	info("Get user $name")
	
	tryScopeLaunch {
		val user = Client.getUser(name)
		
		debug("UserInfo: $user")
		
		onGet(user)
	}
}

fun updateUser() {
	info("Update user")
	
	tryScopeLaunch {
		Client.updateUser {
			error(it)
		}
	}
}

fun sendMessage(message: Message, chat: Chat, onCreated: ((Message) -> Unit)? = null) {
	info("Send message to ${chat.getNameClient()}")
	debug("Message: $message\n Chat: $chat")
	
	tryScopeLaunch {
		Client.sendMessage(message, chat, onCreated) {
			error(it)
		}
	}
}

fun createChat(chat: Chat, onCreated: ((Chat) -> Unit)? = null) {
	info("Create chat ${chat.getNameClient()}")
	debug("Chat: $chat")
	
	tryScopeLaunch {
		Client.createChat(chat, onCreated) {
			error(it)
		}
	}
}

fun updateChat(chat: Chat) {
	info("Update chat")
	debug("Chat: $chat")
	
	tryScopeLaunch {
		Client.updateChat(chat) {
			error(it)
		}
	}
}

fun inviteChat(userName: String, chat: Chat, onCreatedGroup: (() -> Unit)? = null) {
	info("Invite $userName to ${chat.getNameClient()}")
	debug("Chat: $chat")
	
	tryScopeLaunch {
		Client.inviteChat(userName, chat, onCreatedGroup) {
			error(it)
		}
	}
}

fun updatePassword(password: String) {
	info("Update password")
	
	tryScopeLaunch {
		Client.updatePassword(password) {
			error(it)
		}
	}
}

fun deleteChat(chat: Chat, onDeleted: ((Chat) -> Unit)? = null) {
	info("Delete chat ${chat.getNameClient()}")
	debug("Chat: $chat")
	
	tryScopeLaunch {
		Client.deleteChat(chat, onDeleted) {
			error(it)
		}
	}
}

fun leaveChat(chat: Chat, onDeleted: ((Chat) -> Unit)? = null) {
	info("Leave chat ${chat.getNameClient()}")
	debug("Chat: $chat")
	
	tryScopeLaunch {
		Client.leaveChat(chat, onDeleted) {
			error(it)
		}
	}
}

fun kickChat(userName: String, chat: Chat) {
	info("Kick $userName in ${chat.getNameClient()}")
	debug("Chat: $chat")
	
	tryScopeLaunch {
		Client.kickChat(userName, chat) {
			error(it)
		}
	}
}

fun adminChat(userName: String, chat: Chat) {
	info("Give admin $userName in ${chat.getNameClient()}")
	debug("Chat: $chat")
	
	tryScopeLaunch {
		Client.adminChat(userName, chat) {
			error(it)
		}
	}
}

fun readMessages(chat: Chat) {
	info("Read messages in ${chat.getNameClient()}")
	debug("Chat: $chat")
	
	tryScopeLaunch {
		Client.readMessages(chat) {
			error(it)
		}
	}
}

fun deletedMessage(message: Message, chat: Chat) {
	info("Delete message ${message.text}")
	debug("Message: $message")
	
	tryScopeLaunch {
		Client.deleteMessage(message, chat) {
			error(it)
		}
	}
}

fun editMessages(message: Message, chat: Chat) {
	info("Edit message ${message.text}")
	debug("Message: $message")
	
	tryScopeLaunch {
		Client.editMessage(message.text, message, chat) {
			error(it)
		}
	}
}

fun sync(onSync: () -> Unit) {
	tryScopeLaunch {
		sharedFlow.collect {
			try {
				onSync()
			} catch (_: Exception) {
			
			}
		}
	}
}