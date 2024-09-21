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
import androidx.compose.foundation.lazy.*
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun Chat(chat: Chat) {
    Column {
        // Message
        val messagesState = rememberLazyListState()

        val messages = remember { mutableStateListOf<Message>() }

        val scope = rememberCoroutineScope()

        fun scroll() {
            scope.launch {
                if (messages.isNotEmpty())
                    messagesState.animateScrollToItem(messages.count() - 1)
            }
        }

        messages.addAll(chat.messages)

        sync {
            messages.clear()
            messages.addAll(Client.chats.find { it.getName() == chat.getName() }!!.messages)

            if (chat.messages.any { it.creator != Client.user!!.name && it.messageStatus == MessageStatus.UnRead }) {
                readMessages(chat)
                scroll()
            }
        }

        LazyColumn(modifier = Modifier.weight(15f), state = messagesState) {
            itemsIndexed(messages) { index, message ->
                val chatBoxModifier =
                    Modifier.sizeIn(maxWidth = 350.dp).padding(top = 5.dp, start = 5.dp, end = 5.dp)

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
                            modifier = Modifier.padding(5.dp)
                                .background(
                                    color = Colors.BACKGROUND_SECONDARY,
                                    shape = MaterialTheme.shapes.medium
                                )
                        ) {
                            Text(dataText, modifier = Modifier.padding(10.dp, 5.dp))
                        }
                    }
                }

                if (message.creator != Client.user!!.name) {
                    Box(
                        modifier = chatBoxModifier
                            .background(
                                color = Colors.OTHERS_MESSAGE,
                                shape = MaterialTheme.shapes.medium
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
                                    shape = MaterialTheme.shapes.medium
                                )
                        ) {
                            Message(message)
                        }
                    }
                }
            }
        }

        SendRow(chat) {
            messages.add(it)

            scroll()
        }
    }
}

@Composable
fun SendRow(chat: Chat, onSend: (message: Message) -> Unit) {
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
                    val message = Message(Client.user!!.name, messageText, Clock.System.now().epochSeconds, MessageStatus.UnSend)
                    onSend(message)

                    sendMessage(message, chat)

                    messageText = ""
                }
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
            message.creator,
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
                message.sendTime.timeToLocalDateTime().time.toString(),
                color = Colors.BACKGROUND_VARIANT,
                fontSize = 10.sp
            )

            if (message.creator == Client.user!!.name)
                Row(modifier = Modifier.padding(horizontal = 5.dp)) {
                    MessageStatusIcon(message.messageStatus)
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