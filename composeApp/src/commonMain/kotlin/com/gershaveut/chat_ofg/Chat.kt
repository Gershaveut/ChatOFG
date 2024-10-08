package com.gershaveut.chat_ofg

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.Message
import com.gershaveut.chat_ofg.data.MessageStatus
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun OpenChat(chat: Chat) {
	Column {
		// Message
		val messagesState = rememberLazyListState()
		val messages = remember { chat.messages.toMutableStateList() }
		val scope = rememberCoroutineScope()
		
		fun scroll() {
			scope.launch {
				if (messages.isNotEmpty())
					messagesState.animateScrollToItem(messages.count() - 1)
			}
		}
		
		sync {
			messages.clear()
			messages.addAll(Client.chats.find { it.id == chat.id }!!.messages)
			
			if (messages.any { it.creator.name != clientUser.name && it.messageStatus == MessageStatus.UnRead }) {
				readMessages(chat)
				scroll()
			}
		}
		
		val pinnedMessage = remember { mutableStateOf<Message?>(null) }
		
		LazyColumn(modifier = Modifier.weight(15f), state = messagesState) {
			itemsIndexed(messages) { index, message ->
				// Message Data
				if (index <= 0 || message.sendTime.timeToLocalDateTime().date != messages[index - 1].sendTime.timeToLocalDateTime().date) {
					val data = message.sendTime.timeToLocalDateTime().date
					
					val dataText =
						if (data.year == getCurrentDataTime().year)
							"${data.dayOfMonth} ${data.month.name}"
						else
							data.customToString()
					
					Row(
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier.fillMaxWidth()
					) {
						Box(
							modifier = Modifier.padding(5.dp).padding(top = 0.dp)
								.background(
									color = Colors.BACKGROUND_SECONDARY,
									shape = MaterialTheme.shapes.medium
								)
						) {
							Text(dataText, modifier = Modifier.padding(10.dp, 5.dp))
						}
					}
				}
				
				Message(message, chat, messages, pinnedMessage)
			}
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
					messages.find { it.id == pinnedMessage.value!!.id }!!.text = message.text
					
					editMessages(message.text, pinnedMessage.value!!, chat)
					
					pinnedMessage.value = null
				}
			}
		}
	}
}

@Composable
fun PinnedMessage(pinnedMessage: MutableState<Message?>) {
	Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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
	var expanded by remember { mutableStateOf(false) }
	
	Column {
		DropdownMenu(
			modifier = Modifier.padding(horizontal = 5.dp),
			expanded = expanded,
			onDismissRequest = { expanded = false }
		) {
			val widthButton = 150.dp
			
			var showInfo by remember { mutableStateOf(false) }
			
			if (showInfo)
				ChatDialog("User Info", {
					showInfo = false
				}) {
					ShowInfo(message.creator.name)
				}
			
			TextButton(
				{
					expanded = false
					
					showInfo = true
				},
				modifier = Modifier.width(widthButton)
			) {
				Text("Show Info")
			}
			
			if (chat.userAccess(clientUser) && message.creator.name == clientUser.name) {
				Divider()
				
				TextButton(
					{
						expanded = false
						
						pinnedMessage.value = message
					},
					modifier = Modifier.width(widthButton)
				) {
					Text("Edit Message")
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
		
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = if (clientUser.name == message.creator.name && calculateWindowSizeClass().widthSizeClass == WindowWidthSizeClass.Compact) Arrangement.End else Arrangement.Start
		) {
			Column(
				modifier = Modifier.sizeIn(maxWidth = 350.dp).padding(5.dp).padding(top = 0.dp).background(
					color = if (clientUser.name == message.creator.name) Colors.MY_MESSAGE else Colors.OTHERS_MESSAGE,
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
							color = Colors.BACKGROUND_VARIANT,
							fontSize = 10.sp
						)
					
					Text(
						message.sendTime.timeToLocalDateTime().time.toString(),
						color = Colors.BACKGROUND_VARIANT,
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
		MessageStatus.UnSend -> Text("?", color = Colors.BACKGROUND_VARIANT, fontSize = size)
		MessageStatus.UnRead -> Text("!", color = Colors.BACKGROUND_VARIANT, fontSize = size)
		MessageStatus.Read -> Text("!!", color = MaterialTheme.colors.primaryVariant, fontSize = size)
	}
}