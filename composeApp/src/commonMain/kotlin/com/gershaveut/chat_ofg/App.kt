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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gershaveut.chat_ofg.data.AbstractChat
import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.Group
import com.gershaveut.chat_ofg.data.Message
import com.gershaveut.chat_ofg.data.User
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview

val clientUser = User(
    "DEV",
    lastLogin = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
)

val users = listOf(
    User("Tester", lastLogin = LocalDateTime(2024, 7, 14, 12, 40), discription = "Testings..."),
    User("Designer", lastLogin = LocalDateTime(2024, 6, 15, 15, 30), discription = "Working!")
)

val chats = listOf(
    Group(
        arrayListOf(
            users[0],
            users[1]
        ),
        "Discussion",
        LocalDateTime(2024, 6, 10, 4, 55),
        arrayListOf(
            Message(clientUser, "This is a group check", LocalDateTime(2024, 6, 15, 14, 40)),
            Message(users[1], "Check passed", LocalDateTime(2024, 6, 15, 15, 0)),
            Message(users[0], "Check passed", LocalDateTime(2024, 6, 15, 15, 1)),
        ),
    ),
    Chat(
        users[0], LocalDateTime(2024, 6, 14, 12, 35), arrayListOf(
            Message(users[0], "Everything works!", LocalDateTime(2024, 6, 14, 12, 40)),
            Message(clientUser, "Great", LocalDateTime(2024, 6, 14, 13, 0)),
        )
    ), Chat(
        users[1], LocalDateTime(2024, 6, 13, 6, 0), arrayListOf(
            Message(clientUser, "Will everything be ready soon?", LocalDateTime(2024, 6, 13, 6, 15)),
            Message(users[1], "Yes!", LocalDateTime(2024, 6, 13, 19, 30)),
        )
    )
)

@Composable
@Preview
fun App() {
    MaterialTheme {
                Menu()
    }
}

@Composable
@Preview
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
                            chat.messages.map { it.read = true }

                            openChat.value = chat
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
                            Text(chat.name.toCharArray()[0].toString().uppercase())
                        }

                        // Info
                        Column {
                            // Row Name and time
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(chat.name, textAlign = TextAlign.Start)

                                Text(
                                    chat.messages.last().sendTime.toString(),
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }

                            // Row Last Text and New Message
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

            ChatScreen(openChat.value!!)

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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
fun ChatScreen(chat: AbstractChat) {
    Column {
        // Message
        LazyColumn(modifier = Modifier.weight(15f)) {
            items(chat.messages) { message ->
                @Composable
                fun messageContent(message: Message) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            message.owner.displayName,
                            color = Color(41, 150, 201),
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
                                )
                                .align(Alignment.Start)
                        )

                        Text(
                            message.sendTime.time.toString(),
                            color = Color.Gray,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .padding(
                                    start = 10.dp,
                                    bottom = 10.dp,
                                    end = 10.dp
                                )
                                .align(Alignment.End)
                        )
                    }
                }

                val chatBoxModifier =
                    Modifier.sizeIn(maxWidth = 350.dp).padding(top = 5.dp, start = 5.dp, end = 5.dp)

                if (message.owner != clientUser) {
                    Box(
                        modifier = chatBoxModifier
                            .background(
                                color = Color(238, 238, 238),
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        messageContent(message)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (calculateWindowSizeClass().widthSizeClass == WindowWidthSizeClass.Compact) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = chatBoxModifier
                                .background(
                                    color = Color(199, 225, 252),
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            messageContent(message)
                        }
                    }
                }
            }
        }

        // Chat box
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
                    chat.messages.add(Message(clientUser, message.value, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), true))
                },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.Send, null)
            }
        }
    }
}