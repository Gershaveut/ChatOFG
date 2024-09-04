package com.gershaveut.chat_ofg

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.Message
import com.gershaveut.chat_ofg.data.User
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Duration.Companion.ZERO

val clientUser = User("Client User", lastLogin = ZERO)

val users = listOf(User("User 1", lastLogin = ZERO))
val chats = listOf(Chat(users[0], listOf(Message(users[0], "Test", ZERO))))

@Composable
@Preview
fun App() {
    MaterialTheme {
        val screenSize = remember { mutableStateOf(Pair(-1, -1)) }

        Layout(
            content = {
                ChatScreen(screenSize)
            },
            measurePolicy = { measurables, constraints ->
                val width = constraints.maxWidth
                val height = constraints.maxHeight

                screenSize.value = Pair(width, height)

                val placeables = measurables.map { measurable ->
                    measurable.measure(constraints)
                }

                layout(width, height) {
                    var yPosition = 0

                    placeables.forEach { placeable ->
                        placeable.placeRelative(x = 0, y = yPosition)
                        yPosition += placeable.height
                    }
                }
            }
        )
    }
}

@Composable
@Preview
fun Menu() {
    Surface(modifier = Modifier.width(750.dp).height(500.dp)) {
        LazyColumn {
            items(chats) {
                ChatRow(it)
            }
        }
    }
}

@Composable
fun ChatRow(chat: Chat) {
    val owner = chat.owner

    Row(
        modifier = Modifier.fillMaxWidth().padding(1.dp).clickable {
            TODO("Open chat")
        }
    ) {
        // Image box
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.background(
                color = Color.LightGray,
                shape = CircleShape
            ).size(45.dp)
        ) {
            Text(owner.name.toCharArray()[0].toString().uppercase())
        }
        
        // Info
        Column {
            // Row Name and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(owner.name, textAlign = TextAlign.Start)
                
                Text(
                    chat.messages.last().sendTime.toString(),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
            
            // Row Last Text and New Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    chat.messages.last().text,
                    textAlign = TextAlign.Start,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                val unread = chat.messages.count { !it.read }

                if (unread > 0) {
                    Text(
                        unread.toString(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.background(
                            color = Color.Cyan,
                            shape = CircleShape
                        ).size(25.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ChatScreen(screenSize: MutableState<Pair<Int, Int>>, chat: Chat) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            // Message
            LazyColumn( modifier = Modifier.weight(15f) ) {
                items(chat.messages) { messages ->
                    @Composable
                    fun messageContent(message: Message) {
                        Column( horizontalAlignment = Alignment.CenterHorizontally ) {
                            if (message.owner != null) {
                                Text(message.owner!!, color = Color(41, 150, 201), fontSize = 15.sp, modifier = Modifier
                                    .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                                    .align(Alignment.Start))
                            }
                            
                            if (message.id != null) {
                                //Image(ImageBitmap.imageResource(message.id!!), null)
                            }
                            
                            Text(message.text, modifier = Modifier
                                .padding(
                                    top = if (message.owner == null) 10.dp else 0.dp,
                                    start = 10.dp,
                                    bottom = 10.dp,
                                    end = 10.dp
                                )
                                .align(Alignment.Start))
                        }
                    }
                    
                    val chatBoxModifier = Modifier.sizeIn(maxWidth = 350.dp).padding(top = 5.dp, start = 5.dp, end = 5.dp)
                    
                    if (chatMessage.isRemote) {
                        Box(
                            modifier = chatBoxModifier
                                .background(
                                    color = Color(238, 238, 238),
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            messageContent(chatMessage)
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (screenSize.value.first > 600) Arrangement.Start else Arrangement.End) {
                            Box(
                                modifier = chatBoxModifier
                                    .background(
                                        color = Color(199, 225, 252),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                messageContent(chatMessage)
                            }
                        }
                    }
                }
            }

            // Chat box
            Row {
                val message = remember{mutableStateOf("")}
                
                TextField(
                    message.value, { text ->
                        message.value = text
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    placeholder = { Text("stringResource(R.string.co_message)") }
                )
                IconButton( {
                
                },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Send, null)
                }
            }
        }
    }
}