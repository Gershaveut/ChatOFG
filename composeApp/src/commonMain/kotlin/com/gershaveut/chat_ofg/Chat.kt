package com.gershaveut.chat_ofg

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gershaveut.chat_ofg.data.AbstractChat
import com.gershaveut.chat_ofg.data.Message
import com.gershaveut.chat_ofg.data.MessageStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun Chat(chat: AbstractChat) {
	Column {
		// Message
		LazyColumn(modifier = Modifier.weight(15f)) {
			var previousMessage: Message? = null
			
			items(chat.messages) { message ->
				val chatBoxModifier =
					Modifier.sizeIn(maxWidth = 350.dp).padding(top = 5.dp, start = 5.dp, end = 5.dp)
				
				// Message Data
				if (previousMessage == null || message.sendTime.date != previousMessage!!.sendTime.date) {
					val data = message.sendTime.date
					
					val dataText =
						if (data.year == clientDataTime.year)
							"${data.dayOfMonth} ${data.month.name}"
						else
							cdToString(data)
					
					Row(
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier.fillMaxWidth()
					) {
						Box(
							modifier = Modifier.padding(5.dp)
								.background(
									color = Colors.BACKGROUND_SECONDARY,
									shape = RoundedCornerShape(15.dp)
								)
						) {
							Text(dataText, modifier = Modifier.padding(10.dp, 5.dp))
						}
					}
				}
				
				previousMessage = message
				
				if (message.owner != clientUser) {
					Box(
						modifier = chatBoxModifier
							.background(
								color = Colors.OTHERS_MESSAGE,
								shape = RoundedCornerShape(10.dp)
							)
					) {
						Message(message)
					}
				} else {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = if (calculateWindowSizeClass().widthSizeClass == WindowWidthSizeClass.Compact) Arrangement.End else Arrangement.Start
					) {
						Box(
							modifier = chatBoxModifier
								.background(
									color = Colors.MY_MESSAGE,
									shape = RoundedCornerShape(10.dp)
								)
						) {
							Message(message)
						}
					}
				}
			}
		}
		
		SendRow(chat)
	}
}

@Composable
fun SendRow(chat: AbstractChat) {
	Row {
		val message = remember { mutableStateOf("") }
		
		TextField(
			message.value, { text ->
				message.value = text
			},
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth(),
			placeholder = { Text("Print here...") }
		)
		IconButton(
			{
				chat.messages.add(
					Message(
						clientUser,
						message.value,
						Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
						MessageStatus.UnSend
					)
				)
			},
			modifier = Modifier.size(50.dp)
		) {
			Icon(Icons.AutoMirrored.Outlined.Send, null)
		}
	}
}

@Composable
fun Message(message: Message) {
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		Text(
			message.owner.displayName,
			color = MaterialTheme.colors.secondaryVariant,
			fontSize = 15.sp,
			modifier = Modifier
				.padding(top = 10.dp, start = 10.dp, end = 10.dp)
				.align(Alignment.Start)
		)
		
		/*
		if (message.id != null) {
			Image(ImageBitmap.imageResource(message.id!!), null)
		}
		*/
		
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
			Text(
				message.sendTime.time.toString(),
				color = Colors.BACKGROUND_VARIANT,
				fontSize = 10.sp
			)
			
			if (message.owner == clientUser)
				Row ( modifier = Modifier.padding(horizontal = 5.dp) ) {
					MessageStatusIcon(message.messageStatus)
				}
		}
	}
}

@Composable
fun MessageStatusIcon(messageStatus: MessageStatus) {
	when (messageStatus) {
		MessageStatus.UnSend -> Text("?", color = Colors.BACKGROUND_VARIANT, fontSize = 10.sp)
		MessageStatus.UnRead -> Text("!", color = Colors.BACKGROUND_VARIANT, fontSize = 10.sp)
		MessageStatus.Read -> Text("!!", color = MaterialTheme.colors.primaryVariant, fontSize = 10.sp)
	}
}