package com.gershaveut.chat_ofg

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
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
import com.gershaveut.chat_ofg.data.*
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
                }
            )

            var users by remember { mutableStateOf(Client.users) }

            Scaffold(
                floatingActionButton = {
                    FloatingActionButton({
                        refreshUsers {
                            users = Client.users
                        }

                        createChat = true
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Create Chat")
                    }
                },
                floatingActionButtonPosition = FabPosition.End
            ) {
                var chats by remember { mutableStateOf(Client.chats) }

                refreshChats {
                    chats = Client.chats
                }

                LazyColumn {
                    items(chats, { it.getNameChat() }) { chat ->
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
                        items(users.filter { it != Client.user }, { it.name }) { user ->
                            UserRow(user, openChat)
                        }
                    }
                }
            }

        } else {
            // Chat
            val showInfo = remember { mutableStateOf(false) }

            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                            .clickable { showInfo.value = true }) {

                        Text(
                            openChat.value!!.getNameChat(Client.user!!)
                        )
                    }
                },
                navigationIcon = {
                    IconButton({
                        openChat.value = null
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )

            Chat(openChat.value!!)

            if (showInfo.value) {
                ChatDialog("Info", {
                    showInfo.value = false
                }) {
                    val chat = openChat.value!!

                    ShowInfo(
                        chat.getNameChat(Client.user!!),
                        chat.getSignChat(Client.user!!),
                        chat.getDescriptionChat(Client.user!!),
                        chat.getCreateTimeChat()
                    )
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

        InfoRow(Icons.Outlined.Info, "Creation Time", createTime.customToString())
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
    Row(modifier = Modifier.padding(5.dp)) {
        val chatName = chat.getNameChat()

        // Image box
        UserAvatar(chatName)

        var lastMessage = Message(Client.user!!, "Chat created", getCurrentDataTime())

        try {
            lastMessage = chat.getMessagesChat().last()
        } catch (_: Exception) {

        }

        // Info
        Column(modifier = Modifier.padding(start = 5.dp)) {
            // Row Name and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(chatName, textAlign = TextAlign.Start)

                Row {
                    if (lastMessage.owner == Client.user)
                        MessageStatusIcon(lastMessage.messageStatus)

                    Text(
                        lastMessage.sendTime.customToString(),
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

@Composable
fun UserRow(user: User, openChat: MutableState<Chat?>) {
    Row(modifier = Modifier.fillMaxWidth().clickable {
        createChat(user) { chat ->
            openChat.value = chat
        }
    }) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(5.dp)
        ) {
            UserAvatar(user.name)

            Text(user.name, Modifier.padding(start = 5.dp))
        }
    }
}