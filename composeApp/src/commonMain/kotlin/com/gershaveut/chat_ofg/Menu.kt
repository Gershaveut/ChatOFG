package com.gershaveut.chat_ofg

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.MessageStatus
import com.gershaveut.chat_ofg.data.PrivateChat
import com.gershaveut.chat_ofg.data.User
import kotlinx.coroutines.*
import kotlinx.datetime.LocalDateTime

@Composable
fun Menu() {
	val openChat: MutableState<Chat?> = remember { mutableStateOf(null) }

	Column {
		if (openChat.value == null) {
			var createChat by remember { mutableStateOf(false) }

			// Menu
			TopAppBar(
				title = { Text("ChatOFG") },
				navigationIcon = {
					IconButton({

					}) { Icon(Icons.Filled.Menu, contentDescription = "Menu") }
				},
				actions = {
					IconButton( {
						refreshChats()
					} ) {
						Icon(Icons.Filled.Refresh, "Refresh")
					}
				}
			)

			Scaffold(floatingActionButton = {
				FloatingActionButton({
					createChat = true
				}) {
					Icon(Icons.Filled.Add, contentDescription = "Create Chat")
				}
			},
				floatingActionButtonPosition = FabPosition.End
			) {
				LazyColumn {
					items(Client.chats, { it.getNameChat() }) { chat ->
						Row(
							modifier = Modifier.fillMaxWidth().padding(1.dp).clickable {
								chat.getMessagesChat().map {
									if (it.owner != Client.user && it.messageStatus == MessageStatus.UnRead) it.messageStatus =
										MessageStatus.Read
								}

								openChat.value = chat
							}
						) {
							ChatRow(chat)
						}
					}
				}
			}

			if (createChat) {
				ChatDialog("Create Chat", {
					createChat = false
				}) {
					LazyColumn {
						items(Client.users, { it.name }) { user ->
							UserRow(user)
						}
					}
				}
			}

		} else {
			// Chat
			val showInfo = remember { mutableStateOf(false) }

			TopAppBar(
				title = {
					Text(
						openChat.value!!.getNameChat(),
						modifier = Modifier.clickable { showInfo.value = true })
				},
				navigationIcon = {
					IconButton({
						openChat.value = null
					}) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
				},
			)

			Chat(openChat.value!!)

			if (showInfo.value) {
				ChatDialog( "Info" , {
					showInfo.value = false
				} ) {
					val chat = openChat.value!!

					ShowInfo(chat.getNameChat(), chat.getSignChat(), chat.getDescriptionChat(), chat.getCreateTimeChat())
				}
			}
		}
	}
}

@Composable
fun ChatDialog(name: String, onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
	Dialog(onDismissRequest = onDismissRequest) {
		Surface(
			modifier = Modifier.size(250.dp, 500.dp)
		) {
			Column {
				TopAppBar(
					title = {
						Text(name)
					},
					actions = {
						IconButton({
							onDismissRequest()
						}) {
							Icon(
								Icons.AutoMirrored.Filled.ExitToApp,
								contentDescription = "Close"
							)
						}
					}
				)

				content()
			}
		}
	}
}

@Composable
fun ShowInfo(name: String, sign: String, description: String?, createTime: LocalDateTime) {
	Column(modifier = Modifier.padding(top = 5.dp, start = 5.dp)) {
		Row(modifier = Modifier.padding(bottom = 10.dp)) {
			UserAvatar(name, 60.dp)

			Column {
				Text(
					name,
					textAlign = TextAlign.Start,
					modifier = Modifier.padding(start = 5.dp)
				)

				Text(
					sign,
					textAlign = TextAlign.Start,
					fontSize = 12.sp,
					color = Colors.BACKGROUND_VARIANT,
					modifier = Modifier.padding(5.dp)
				)
			}
		}

		InfoRow(Icons.Outlined.Info, "Description", description ?: "No Description")

		InfoRow(Icons.Outlined.Info, "Creation Time", cdtToString(createTime))
	}
}

@Composable
private fun InfoRow(icon: ImageVector, contentDescription: String, text: String) {
	Row {
		Column {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Icon(
					icon,
					contentDescription = contentDescription
				)

				Text(contentDescription, modifier = Modifier.padding(start = 5.dp))
			}

			Text(
				text,
				fontSize = 12.sp,
				color = Colors.BACKGROUND_VARIANT,
				modifier = Modifier.padding(5.dp)
			)
		}
	}
}

@Composable
fun UserAvatar(name: String, size: Dp = 45.dp) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier.background(
			color = Colors.BACKGROUND_SECONDARY,
			shape = MaterialTheme.shapes.small
		).size(size)
	) {
		Text(name.toCharArray()[0].toString().uppercase())
	}
}

@Composable
fun ChatRow(chat: Chat) {
	Row ( modifier = Modifier.padding(5.dp) ) {

		// Image box
		UserAvatar(chat.getNameChat())

		val lastMessage = chat.getMessagesChat().last()

		// Info
		Column {
			// Row Name and time
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				Text(chat.getNameChat(), textAlign = TextAlign.Start)

				Row {
					if (lastMessage.owner == Client.user)
						MessageStatusIcon(lastMessage.messageStatus)

					Text(
						cdtToString(lastMessage.sendTime),
						fontSize = 10.sp,
						color = Colors.BACKGROUND_VARIANT,
						modifier = Modifier.padding(start = 5.dp)
					)
				}
			}

			// Row Last Text and New Message
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				Text(
					lastMessage.text,
					textAlign = TextAlign.Start,
					fontSize = 12.sp,
					color = Colors.BACKGROUND_VARIANT
				)

				val unread =
					chat.getMessagesChat().count { it.owner != Client.user && it.messageStatus == MessageStatus.UnRead }

				if (unread > 0) {
					Text(
						unread.toString(),
						textAlign = TextAlign.Center,
						modifier = Modifier.background(
							color = MaterialTheme.colors.secondary,
							shape = MaterialTheme.shapes.small
						).size(25.dp)
					)
				}
			}
		}
	}
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun UserRow(user: User) {
	Row( verticalAlignment = Alignment.CenterVertically , modifier = Modifier.padding(5.dp).fillMaxWidth().clickable {
		refreshUsers()

		scope.launch {
			Client.createPrivateChat(PrivateChat(user, Client.dataTime))
			refreshChats()
		}
	} ) {
		UserAvatar(user.name)

		Text(user.name, Modifier.padding(start = 5.dp))
	}
}