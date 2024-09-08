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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gershaveut.chat_ofg.data.AbstractChat
import com.gershaveut.chat_ofg.data.MessageStatus

@Composable
fun Menu() {
	val openChat: MutableState<AbstractChat?> = remember { mutableStateOf(null) }
	
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
			
			LazyColumn {
				items(chats) { chat ->
					Row(
						modifier = Modifier.fillMaxWidth().padding(1.dp).clickable {
							chat.messages.map {
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
						openChat.value!!.name,
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
							
							openChat.value!!.ShowInfo()
						}
					}
				}
			}
		}
	}
}

@Composable
fun ChatRow(chat: AbstractChat) {
	// Image box
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier.background(
			color = Colors.BACKGROUND_SECONDARY,
			shape = CircleShape
		).size(45.dp)
	) {
		Text(chat.name.toCharArray()[0].toString().uppercase())
	}
	
	val lastMessage = chat.messages.last()
	
	// Info
	Column {
		// Row Name and time
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(chat.name, textAlign = TextAlign.Start)
			
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
				chat.messages.count { it.messageStatus == MessageStatus.UnRead }
			
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