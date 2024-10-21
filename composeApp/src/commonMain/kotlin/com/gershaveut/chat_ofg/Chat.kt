package com.gershaveut.chat_ofg

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chatofg.composeapp.generated.resources.*
import com.benasher44.uuid.uuid4
import com.gershaveut.chat_ofg.data.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class PinnedType(val actionString: StringResource, val icon: ImageVector) {
	Edit(Res.string.pinned_edit, Icons.Filled.Edit),
	Reply(Res.string.pinned_reply, Icons.AutoMirrored.Filled.ArrowBack),
	Forward(Res.string.pinned_forward, Icons.AutoMirrored.Filled.ArrowForward);
}

@Composable
fun OpenChat(
	chat: Chat,
	showInfo: MutableState<Boolean>,
	openChat: MutableState<Chat?>,
	onClose: (String?) -> Unit
) {
	val messagesState = rememberLazyListState()
	val messages = remember { mutableStateOf(chat.messages) }
	val scope = rememberCoroutineScope()
	
	fun close(reason: String? = null) {
		openChat.value = null
		
		onClose(reason)
	}
	
	fun scroll() {
		scope.launch {
			if (messages.value.isNotEmpty())
				messagesState.animateScrollToItem(messages.value.count() - 1)
		}
	}
	
	val showScroll = messagesState.firstVisibleItemIndex < messages.value.count() - 10
	
	sync {
		val updatedChat = Client.chats.find { it.id == chat.id }!!
		
		messages.value = updatedChat.messages
		openChat.value = updatedChat
		
		if (!showScroll)
			readMessages(chat) {
				scroll()
			}
	}
	
	Column {
		val openChatSettings = remember { mutableStateOf(false) }
		val forwardChat = remember { mutableStateOf(false) }
		
		val pinnedMessage = remember { mutableStateOf<Pair<Message, PinnedType>?>(null) }
		
		if (!openChatSettings.value) {
			if (forwardChat.value) {
				TopAppBar({
					Text(stringResource(Res.string.chat_selection))
				},
					navigationIcon = {
						IconButton({
							forwardChat.value = false
							pinnedMessage.value = null
						}) {
							Icon(
								Icons.AutoMirrored.Filled.ArrowBack,
								contentDescription = stringResource(Res.string.back)
							)
						}
					}
				)
				
				LazyColumn {
					items(Client.chats, { it.id }) { chat ->
						Row(
							modifier = Modifier.fillMaxWidth().padding(1.dp).clickable {
								readMessages(chat)
								
								forwardChat.value = false
								
								openChat.value = chat
								messages.value = chat.messages
							}
						) {
							ChatRow(chat)
						}
					}
				}
			} else {
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
							close()
						}) {
							Icon(
								Icons.AutoMirrored.Filled.ArrowBack,
								contentDescription = stringResource(Res.string.back)
							)
						}
					},
					actions = {
						var expanded by remember { mutableStateOf(false) }
						
						IconButton({
							expanded = true
						}) {
							Icon(Icons.Filled.MoreVert, contentDescription = stringResource(Res.string.actions))
						}
						
						var selectInvite by remember { mutableStateOf(false) }
						
						if (selectInvite) {
							SelectUsers(
								stringResource(Res.string.invite),
								users.apply { removeAll(openChat.value!!.members.keys) },
								{
									selectInvite = false
								}) { members ->
								selectInvite = false
								
								members.forEach {
									inviteChat(it.name, openChat.value!!) {
										if (chat.chatType == ChatType.PrivateChat)
											close()
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
								Text(stringResource(Res.string.show))
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
								Text(stringResource(Res.string.invite))
							}
							
							Divider()
							
							TextButton(
								{
									expanded = false
									
									openChatSettings.value = true
								},
								modifier = Modifier.width(widthButton)
							) {
								Text(stringResource(Res.string.chat_settings))
							}
							
							val deletedChatText = stringResource(Res.string.deleted_chat)
							val leavedChatText = stringResource(Res.string.leaved_chat)
							
							if (chat.userAccess(clientUser)) {
								TextButton(
									{
										expanded = false
										
										deleteChat(openChat.value!!) {
											close("$deletedChatText ${chat.getNameClient()}")
										}
									},
									modifier = Modifier.width(widthButton)
								) {
									Text(stringResource(Res.string.delete_chat))
								}
							}
							
							if (chat.chatType != ChatType.PrivateChat) {
								TextButton(
									{
										expanded = false
										
										leaveChat(openChat.value!!) {
											close("$leavedChatText ${chat.getNameClient()}")
										}
									},
									modifier = Modifier.width(widthButton)
								) {
									Text(stringResource(Res.string.leave_chat))
								}
							}
						}
					}
				)
				
				Scaffold(
					floatingActionButton = {
						if (showScroll)
							Column {
								Row(modifier = Modifier.background(
									MaterialTheme.colors.secondaryVariant,
									MaterialTheme.shapes.large
								)) {
									Text(chat.unreadMessagesCountClient().toString())
								}
								
								FloatingActionButton({
									scroll()
									
									readMessages(chat)
								}, modifier = Modifier.padding(bottom = 50.dp)) {
									Icon(
										Icons.Filled.KeyboardArrowDown,
										contentDescription = stringResource(Res.string.scroll)
									)
								}
							}
					}
				) {
					Column {
						LazyColumn(modifier = Modifier.weight(15f), state = messagesState) {
							itemsIndexed(
								messages.value,
								{ _, it -> it.id }) { index, message ->
								if (message.messageType == MessageType.System) {
									SystemMessage(message.text)
								} else {
									// Message Data
									val dataLast =
										messages.value.findLast { last -> index > messages.value.indexOfLast { last.id == it.id } && last.messageType == MessageType.Default && last.id != message.id }?.sendTime?.timeToLocalDateTime()?.date
									
									if ((dataLast == null && message.messageType != MessageType.System) || message.sendTime.timeToLocalDateTime().date != dataLast!!) {
										val data = message.sendTime.timeToLocalDateTime().date
										
										val dataText =
											if (data.year == getCurrentDataTime().year)
												"${data.dayOfMonth} ${data.month.name}"
											else
												data.customToString()
										
										SystemMessage(dataText)
									}
									
									Message(message, chat, messages, pinnedMessage, messagesState, forwardChat)
								}
							}
						}
						
						if (pinnedMessage.value != null) {
							PinnedMessage(pinnedMessage)
						}
						
						fun sendMessage(message: Message) {
							messages.value = messages.value.toMutableList().apply { add(message) }
							
							sendMessage(message, chat)
							
							scroll()
						}
						
						SendRow(pinnedMessage) { message ->
							when (pinnedMessage.value?.second) {
								PinnedType.Edit -> {
									val editMessage = pinnedMessage.value!!.first
									
									editMessage.apply {
										text = message.text
										modified = true
										messageStatus = MessageStatus.UnSend
									}
									
									messages.value[messages.value.indexOfFirst { it.id == editMessage.id }] =
										editMessage
									
									editMessages(editMessage, chat)
									
									pinnedMessage.value = null
								}
								
								PinnedType.Reply -> {
									val replyMessage = pinnedMessage.value!!.first
									
									message.apply {
										reply = replyMessage
									}
									
									pinnedMessage.value = null
									
									sendMessage(message)
								}
								
								PinnedType.Forward -> {
									val forwardMessage = pinnedMessage.value!!.first.apply {
										forwarded = true
									}
									
									pinnedMessage.value = null
									
									if (message.text.isEmpty()) {
										sendMessage(forwardMessage.apply {
											id = uuid4().toString()
											messageStatus = MessageStatus.UnSend
										})
									} else {
										sendMessage(message.apply {
											reply = forwardMessage
											forwarded = true
										})
									}
								}
								
								else -> {
									sendMessage(message)
								}
							}
						}
					}
				}
			}
		} else {
			ChatSettings(openChatSettings, chat, chat.userAccess(clientUser))
		}
	}
	
	scroll()
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
fun PinnedMessage(pinnedMessage: MutableState<Pair<Message, PinnedType>?>) {
	Row(
		modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Icon(
				pinnedMessage.value!!.second.icon,
				stringResource(Res.string.pinned_message),
				modifier = Modifier.padding(5.dp)
			)
			
			Column {
				Text(
					stringResource(pinnedMessage.value!!.second.actionString),
					color = MaterialTheme.colors.secondaryVariant
				)
				
				Text(pinnedMessage.value!!.first.text, color = BACKGROUND_VARIANT)
			}
		}
		
		IconButton({
			pinnedMessage.value = null
		}) {
			Icon(Icons.Filled.Close, stringResource(Res.string.close))
		}
	}
}

@Composable
fun SendRow(pinnedMessage: MutableState<Pair<Message, PinnedType>?>, onSend: (message: Message) -> Unit) {
	Row {
		var messageText by remember { mutableStateOf("") }
		
		TextField(
			messageText, { text ->
				messageText = text
			},
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth(),
			placeholder = { Text(stringResource(Res.string.print)) }
		)
		IconButton(
			{
				if (messageText.isNotEmpty() || (pinnedMessage.value != null && pinnedMessage.value!!.second == PinnedType.Forward)) {
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
fun Message(
	message: Message,
	chat: Chat,
	messages: MutableState<MutableList<Message>>,
	pinnedMessage: MutableState<Pair<Message, PinnedType>?>,
	messagesState: LazyListState,
	forwardChat: MutableState<Boolean>
) {
	var showInfo by remember { mutableStateOf(false) }
	
	if (showInfo)
		ChatDialog(stringResource(Res.string.user_info), {
			showInfo = false
		}) {
			if (message.forwarded && message.reply != null)
				ShowInfo(message.reply!!.creator.name)
			else
				ShowInfo(message.creator.name)
		}
	
	var expanded by remember { mutableStateOf(false) }
	
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = if (clientUser.name == message.creator.name && calculateWindowSizeClass().widthSizeClass == WindowWidthSizeClass.Compact) Alignment.End else Alignment.Start
	) {
		Column(
			modifier = Modifier.padding(5.dp).padding(top = 0.dp).background(
				color = if (clientUser.name == message.creator.name) MY_MESSAGE else OTHERS_MESSAGE,
				shape = MaterialTheme.shapes.medium
			).clickable {
				expanded = true
			},
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
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
					Text(stringResource(Res.string.show))
				}
				
				TextButton(
					{
						expanded = false
						
						forwardChat.value = true
						pinnedMessage.value = message to PinnedType.Forward
					},
					modifier = Modifier.width(widthButton)
				) {
					Text(stringResource(Res.string.forward))
				}
				
				TextButton(
					{
						expanded = false
						
						pinnedMessage.value = message to PinnedType.Reply
					},
					modifier = Modifier.width(widthButton)
				) {
					Text(stringResource(Res.string.reply))
				}
				
				if (chat.userAccess(clientUser) || message.creator.name == clientUser.name) {
					Divider()
					
					if (message.creator.name == clientUser.name && !(message.reply == null && message.forwarded)) {
						TextButton(
							{
								expanded = false
								
								pinnedMessage.value = message to PinnedType.Edit
							},
							modifier = Modifier.width(widthButton)
						) {
							Text(stringResource(Res.string.edit_message))
						}
					}
					
					TextButton(
						{
							expanded = false
							
							messages.value = messages.value.toMutableList().apply { remove(message) }
							
							deletedMessage(message, chat)
						},
						modifier = Modifier.width(widthButton)
					) {
						Text(stringResource(Res.string.delete_message))
					}
				}
			}
			
			Column {
				if (message.forwarded) {
					Text(
						"${stringResource(Res.string.forwarded)} ${if (message.reply != null) message.reply!!.creator.displayName else message.creator.displayName}",
						modifier = Modifier.padding(5.dp).clickable {
							showInfo = true
						})
				}
				
				if (message.reply != null) {
					val scope = rememberCoroutineScope()
					val reply = message.reply!!
					
					Column(
						modifier = Modifier.padding(5.dp).background(
							color = REPLY_MESSAGE,
							shape = MaterialTheme.shapes.medium
						).clickable {
							scope.launch {
								if (message.forwarded) {
									showInfo = true
								} else {
									val index = messages.value.indexOfFirst { it.id == reply.id }
									
									if (index != -1)
										messagesState.animateScrollToItem(index)
								}
							}
						},
						horizontalAlignment = Alignment.CenterHorizontally,
					) {
						Text(
							reply.creator.displayName,
							color = MaterialTheme.colors.secondaryVariant,
							modifier = Modifier
								.padding(7.dp).align(Alignment.Start)
						)
						
						Text(
							reply.text, modifier = Modifier
								.padding(7.dp).align(Alignment.Start)
						)
					}
				}
				
				Text(
					message.creator.displayName,
					color = MaterialTheme.colors.secondaryVariant,
					modifier = Modifier
						.padding(7.dp).align(Alignment.Start)
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
							stringResource(Res.string.edited),
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