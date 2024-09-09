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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.MessageStatus
import com.gershaveut.chat_ofg.data.PrivateChat
import com.gershaveut.chat_ofg.data.User
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun Menu() {
	val openChat: MutableState<Chat?> = remember { mutableStateOf(null) }
	
	Column {
		if (openChat.value == null) {
			// Menu
			TopAppBar(
				title = { Text("ChatOFG") },
				navigationIcon = {
					IconButton({
					
					}) { Icon(Icons.Filled.Menu, contentDescription = "Menu") }
				},
			)

			val chats = ArrayList<Chat>()

			chats.addAll(privateChats)
			chats.addAll(groups)

			LazyColumn {
				items(chats) { chat ->
					Row(
						modifier = Modifier.fillMaxWidth().padding(1.dp).clickable {
							chat.getMessagesChat().map {
								if (it.owner != clientUser && it.messageStatus == MessageStatus.UnRead) it.messageStatus =
									MessageStatus.Read
							}
							
							openChat.value = chat
						}
					) {
						ChatRow(chat)
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
				Dialog(onDismissRequest = {
					showInfo.value = false
				}) {
					Surface(
						modifier = Modifier.size(250.dp, 500.dp)
					) {
						Column {
							TopAppBar(
								title = {
									Text("Info")
								},
								actions = {
									IconButton({
										showInfo.value = false
									}) {
										Icon(
											Icons.AutoMirrored.Filled.ExitToApp,
											contentDescription = "Close"
										)
									}
								}
							)

							val chat = openChat.value!!

							ShowInfo(chat.getNameChat(), chat.getSignChat(), chat.getDescriptionChat(), chat.getCreateTimeChat())
						}
					}
				}
			}
		}
	}
}

@Composable
fun ShowInfo(name: String, sign: String, description: String?, createTime: LocalDateTime) {
	Column(modifier = Modifier.padding(top = 5.dp, start = 5.dp)) {
		Row(modifier = Modifier.padding(bottom = 10.dp)) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier.background(
					color = Colors.BACKGROUND_SECONDARY,
					shape = CircleShape
				).size(60.dp)
			) {
				Text(name.toCharArray()[0].toString().uppercase())
			}

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
fun ChatRow(chat: Chat) {
	// Image box
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier.background(
			color = Colors.BACKGROUND_SECONDARY,
			shape = CircleShape
		).size(45.dp)
	) {
		Text(chat.getNameChat().toCharArray()[0].toString().uppercase())
	}
	
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
				if (lastMessage.owner == clientUser)
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
				chat.getMessagesChat().count { it.owner != clientUser && it.messageStatus == MessageStatus.UnRead }
			
			if (unread > 0) {
				Text(
					unread.toString(),
					textAlign = TextAlign.Center,
					modifier = Modifier.background(
						color = MaterialTheme.colors.secondary,
						shape = CircleShape
					).size(25.dp)
				)
			}
		}
	}
}