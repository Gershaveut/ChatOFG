package com.gershaveut.chat_ofg

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gershaveut.chat_ofg.data.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun OpenChat(chat: Chat, showInfo: MutableState<Boolean>, openChat: MutableState<Chat?>) {
	Column {
		val messagesState = rememberLazyListState()
		var messages by remember { mutableStateOf(chat.messages) } // TODO: Update bug on add or edit
		val scope = rememberCoroutineScope()
		
		fun scroll() {
			scope.launch {
				if (messages.isNotEmpty())
					messagesState.animateScrollToItem(messages.count() - 1)
			}
		}
		
		sync {
			/* Code duplicate list bug
			messages.clear()
			messages.addAll(Client.chats.find { it.id == chat.id }!!.messages)
			 */
			
			messages = Client.chats.find { it.id == chat.id }!!.messages
			
			if (messages.any { it.creator.name != clientUser.name && it.messageStatus == MessageStatus.UnRead }) {
				readMessages(chat)
				scroll()
			}
		}
		
		val pinnedMessage = remember { mutableStateOf<Message?>(null) }
		
		val openChatSettings = remember { mutableStateOf(false) }
		
		if (!openChatSettings.value) {
			var users by remember { mutableStateOf(Client.users) }
			
			TopAppBar(
				title = {
					Row(verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.fillMaxSize()
							.clickable { showInfo.value = true }) {
						Text(
							chat.getNameClient()
						)
					}
				},
				navigationIcon = {
					IconButton({
						openChat.value = null
					}) {
						Icon(
							Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = "Back"
						)
					}
				},
				actions = {
					var expanded by remember { mutableStateOf(false) }
					
					IconButton({
						expanded = true
					}) {
						Icon(Icons.Filled.MoreVert, contentDescription = "Actions")
					}
					
					var selectInvite by remember { mutableStateOf(false) }
					
					if (selectInvite) {
						SelectUsers(
							"Invite user",
							users.apply { removeAll(openChat.value!!.members.keys) },
							{
								selectInvite = false
							}) { members ->
							selectInvite = false
							
							members.forEach {
								inviteChat(it.name, openChat.value!!) {
									if (chat.chatType == ChatType.PrivateChat)
										openChat.value = null
								}
							}
						}
					}
					
					DropdownMenu(
						modifier = Modifier.padding(horizontal = 5.dp),
						expanded = expanded,
						onDismissRequest = { expanded = false }
					) {
						val widthButton = 150.dp
						
						TextButton(
							{
								expanded = false
								
								showInfo.value = true
							},
							modifier = Modifier.width(widthButton)
						) {
							Text("Show Info")
						}
						
						sync {
							openChat.value = Client.chats.find { it.id == openChat.value!!.id }
						}
						
						TextButton(
							{
								expanded = false
								
								refreshUsers {
									users = Client.users
								}
								
								selectInvite = true
							},
							modifier = Modifier.width(widthButton)
						) {
							Text("Invite User")
						}
						
						Divider()
						
						TextButton(
							{
								expanded = false
								
								openChatSettings.value = true
							},
							modifier = Modifier.width(widthButton)
						) {
							Text("Chat settings")
						}
						
						if (chat.userAccess(clientUser)) {
							TextButton(
								{
									expanded = false
									
									deleteChat(openChat.value!!) {
										openChat.value = null
									}
								},
								modifier = Modifier.width(widthButton)
							) {
								Text("Delete chat")
							}
						}
						
						if (chat.chatType != ChatType.PrivateChat) {
							TextButton(
								{
									expanded = false
									
									leaveChat(openChat.value!!) {
										openChat.value = null
									}
								},
								modifier = Modifier.width(widthButton)
							) {
								Text("Leave chat")
							}
						}
					}
				}
			)
			
			LazyColumn(modifier = Modifier.weight(15f), state = messagesState) {
				itemsIndexed(messages, { _, it -> it.id } ) { index, message ->
					if (message.messageType == MessageType.System) {
						SystemMessage(message.text)
					} else {
						// Message Data
						val dataLast =
							messages.findLast { last -> index > messages.indexOfLast { last.id == it.id } && last.messageType == MessageType.Default && last.id != message.id }?.sendTime?.timeToLocalDateTime()?.date
						
						if ((dataLast == null && message.messageType != MessageType.System) || message.sendTime.timeToLocalDateTime().date != dataLast!!) {
							val data = message.sendTime.timeToLocalDateTime().date
							
							val dataText =
								if (data.year == getCurrentDataTime().year)
									"${data.dayOfMonth} ${data.month.name}"
								else
									data.customToString()
							
							SystemMessage(dataText)
						}
						
						Message(message, chat, messages, pinnedMessage)
					}
				}
				
				// TODO: Scroll on open
			}
			
			Column {
				if (pinnedMessage.value != null) {
					PinnedMessage(pinnedMessage)
				}
				
				SendRow { message ->
					if (pinnedMessage.value == null) {
						messages.add(message)
						
						sendMessage(message, chat)
						
						scroll()
					} else {
						pinnedMessage.value!!.apply {
							text = message.text
							modified = true
							messageStatus = MessageStatus.UnSend
						}
						
						messages[messages.indexOfFirst { it.id == pinnedMessage.value!!.id }] = pinnedMessage.value!!
						
						editMessages(pinnedMessage.value!!, chat)
						
						pinnedMessage.value = null
					}
				}
			}
		} else {
			ChatSettings(openChatSettings, chat, chat.userAccess(clientUser))
		}
	}
}

@Composable
fun SystemMessage(text: String) {
	Row(
		horizontalArrangement = Arrangement.Center,
		modifier = Modifier.fillMaxWidth()
	) {
		Box(
			modifier = Modifier.padding(5.dp).padding(top = 0.dp)
				.background(
					color = BACKGROUND_SECONDARY,
					shape = MaterialTheme.shapes.medium
				)
		) {
			Text(text, modifier = Modifier.padding(10.dp, 5.dp))
		}
	}
}

@Composable
fun PinnedMessage(pinnedMessage: MutableState<Message?>) {
	Row(
		modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 5.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(pinnedMessage.value!!.text)
		
		IconButton({
			pinnedMessage.value = null
		}) {
			Icon(Icons.Filled.Close, "Close")
		}
	}
}

@Composable
fun SendRow(onSend: (message: Message) -> Unit) {
	Row {
		var messageText by remember { mutableStateOf("") }
		
		TextField(
			messageText, { text ->
				messageText = text
			},
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth(),
			placeholder = { Text("Print here...") }
		)
		IconButton(
			{
				if (messageText.isNotEmpty()) {
					val message =
						Message(
							clientUser,
							messageText,
							Clock.System.now().epochSeconds,
							MessageStatus.UnSend
						)
					onSend(message)
					
					messageText = ""
				}
			},
			modifier = Modifier.size(50.dp)
		) {
			Icon(Icons.AutoMirrored.Outlined.Send, null, modifier = Modifier.padding(10.dp))
		}
	}
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun Message(message: Message, chat: Chat, messages: MutableList<Message>, pinnedMessage: MutableState<Message?>) {
	var showInfo by remember { mutableStateOf(false) }
	
	if (showInfo)
		ChatDialog("User Info", {
			showInfo = false
		}) {
			ShowInfo(message.creator.name)
		}
	
	var expanded by remember { mutableStateOf(false) }
	
	Column(horizontalAlignment = if (clientUser.name == message.creator.name && calculateWindowSizeClass().widthSizeClass == WindowWidthSizeClass.Compact) Alignment.End else Alignment.Start) {
		DropdownMenu(
			modifier = Modifier.padding(horizontal = 5.dp),
			expanded = expanded,
			onDismissRequest = { expanded = false }
		) {
			val widthButton = 150.dp
			
			TextButton(
				{
					expanded = false
					
					showInfo = true
				},
				modifier = Modifier.width(widthButton)
			) {
				Text("Show Info")
			}
			
			if (chat.userAccess(clientUser) || message.creator.name == clientUser.name) {
				Divider()
				
				if (message.creator.name == clientUser.name) {
					TextButton(
						{
							expanded = false
							
							pinnedMessage.value = message
						},
						modifier = Modifier.width(widthButton)
					) {
						Text("Edit Message")
					}
				}
				
				TextButton(
					{
						expanded = false
						
						messages.remove(message)
						
						deletedMessages(message, chat)
					},
					modifier = Modifier.width(widthButton)
				) {
					Text("Delete Message")
				}
			}
		}
		
		Row(modifier = Modifier.fillMaxWidth()) {
			Column(
				modifier = Modifier.sizeIn(maxWidth = 350.dp).padding(5.dp).padding(top = 0.dp).background(
					color = if (clientUser.name == message.creator.name) MY_MESSAGE else OTHERS_MESSAGE,
					shape = MaterialTheme.shapes.medium
				).clickable {
					expanded = true
				},
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Text(
					message.creator.displayName,
					color = MaterialTheme.colors.secondaryVariant,
					fontSize = 15.sp,
					modifier = Modifier
						.padding(top = 10.dp, start = 10.dp, end = 10.dp)
						.align(Alignment.Start)
				)
				
				Text(
					message.text, modifier = Modifier
						.padding(
							top = 10.dp,
							start = 10.dp,
							bottom = 5.dp,
							end = 10.dp
						).align(Alignment.Start)
				)
				
				Row(
					Modifier.align(Alignment.End).padding(
						bottom = 10.dp,
						end = 10.dp
					)
				) {
					if (message.modified)
						Text(
							"edited",
							color = BACKGROUND_VARIANT,
							fontSize = 10.sp
						)
					
					Text(
						message.sendTime.timeToLocalDateTime().time.toString(),
						color = BACKGROUND_VARIANT,
						fontSize = 10.sp
					)
					
					if (message.creator.name == clientUser.name)
						Row(modifier = Modifier.padding(horizontal = 5.dp)) {
							MessageStatusIcon(message.messageStatus)
						}
				}
			}
		}
	}
}

@Composable
fun MessageStatusIcon(messageStatus: MessageStatus) {
	val size = 10.sp
	
	when (messageStatus) {
		MessageStatus.UnSend -> Text("?", color = BACKGROUND_VARIANT, fontSize = size)
		MessageStatus.UnRead -> Text("!", color = BACKGROUND_VARIANT, fontSize = size)
		MessageStatus.Read -> Text("!!", color = MaterialTheme.colors.primaryVariant, fontSize = size)
	}
}