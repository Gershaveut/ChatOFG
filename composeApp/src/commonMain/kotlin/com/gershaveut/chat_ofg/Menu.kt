package com.gershaveut.chat_ofg

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gershaveut.chat_ofg.data.*
import kotlinx.coroutines.launch

@Composable
fun Menu(user: MutableState<UserInfo?>, openSettings: MutableState<Boolean>) {
	val drawerState = rememberDrawerState(DrawerValue.Closed)
	val showInfo = remember { mutableStateOf(false) }
	
	val scope = rememberCoroutineScope()
	
	ModalDrawer(
		{
			Column(modifier = Modifier.padding(5.dp)) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.fillMaxWidth().clickable {
						scope.launch {
							drawerState.close()
						}
						
						showInfo.value = true
					}) {
					UserAvatar(clientUser.name, 60.dp)
					
					Text(clientUser.name, modifier = Modifier.padding(start = 5.dp))
				}
				
				Column {
					MenuButton("Exit", Icons.AutoMirrored.Filled.ArrowBack) {
						user.value = null
						Client.user = null
						
						Client.users.clear()
						Client.chats.clear()
					}
					MenuButton("Settings", Icons.Filled.Settings) {
						openSettings.value = true
					}
				}
			}
		},
		drawerState = drawerState
	) {
		val openChat: MutableState<Chat?> = remember { mutableStateOf(null) }
		var users by remember { mutableStateOf(Client.users) }
		
		Column {
			if (openChat.value == null) {
				var createChat by remember { mutableStateOf(false) }
				
				// Menu
				TopAppBar(
					title = { Text("ChatOFG") },
					navigationIcon = {
						IconButton({
							scope.launch {
								drawerState.open()
							}
						}) { Icon(Icons.Filled.Menu, contentDescription = "Menu") }
					}
				)
				
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
					
					sync {
						chats = Client.chats
						
						if (!chats.any { it.id == openChat.value?.id })
							openChat.value = null
					}
					
					refreshChats {
						chats = Client.chats
					}
					
					LazyColumn {
						items(chats, { it.id }) { chat ->
							Row(
								modifier = Modifier.fillMaxWidth().padding(1.dp).clickable {
									if (chat.messages.any { it.creator.name != clientUser.name && it.messageStatus == MessageStatus.UnRead })
										readMessages(chat)
									
									openChat.value = chat
								}
							) {
								ChatRow(chat)
							}
						}
					}
				}
				
				if (createChat) {
					SelectUsers("Create Chat", users, {
						createChat = false
					}) { members ->
						createChat = false
						
						if (members.size > 0) {
							createChat(
								Chat(
									members = members.associateWith { false }
										.toMutableMap()
										.apply { put(clientUser, true) })
							) { chat ->
								openChat.value = chat
							}
						}
					}
				}
			} else {
				val openChatSettings = remember { mutableStateOf(false) }
				
				if (!openChatSettings.value) {
					// Chat
					TopAppBar(
						title = {
							Row(verticalAlignment = Alignment.CenterVertically,
								modifier = Modifier.fillMaxSize()
									.clickable { showInfo.value = true }) {
								Text(
									openChat.value!!.getNameClient()
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
							
							DropdownMenu(
								modifier = Modifier.padding(horizontal = 5.dp),
								expanded = expanded,
								onDismissRequest = { expanded = false }
							) {
								val widthButton = 150.dp
								
								var selectInvite by remember { mutableStateOf(false) }
								
								TextButton(
									{
										showInfo.value = true
									},
									modifier = Modifier.width(widthButton)
								) {
									Text("Show Info")
								}
								
								sync {
									openChat.value = Client.chats.find { it.id == openChat.value!!.id }
								}
								
								if (selectInvite) {
									SelectUsers("Invite user", users.apply { removeAll(openChat.value!!.members.keys) }, {
										selectInvite = false
									}) { members ->
										selectInvite = false
										
										members.forEach {
											inviteChat(it.name, openChat.value!!) {
												openChat.value = null
											}
										}
									}
								}
								
								TextButton(
									{
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
										openChatSettings.value = true
									},
									modifier = Modifier.width(widthButton)
								) {
									Text("Chat settings")
								}
								
								if (openChat.value!!.userAccess(clientUser)) {
									TextButton(
										{
											deleteChat(openChat.value!!) {
												openChat.value = null
											}
										},
										modifier = Modifier.width(widthButton)
									) {
										Text("Delete chat")
									}
								} else {
									TextButton(
										{
											//TODO: Leave chat
										},
										modifier = Modifier.width(widthButton)
									) {
										Text("Leave chat")
									}
								}
							}
						}
					)
					
					OpenChat(openChat.value!!)
				} else {
					ChatSettings(openChatSettings, openChat.value!!, openChat.value!!.userAccess(clientUser))
				}
			}
			
			if (showInfo.value) {
				if (openChat.value != null) {
					ChatDialog("Chat Info", {
						showInfo.value = false
					}) {
						ShowInfo(openChat.value!!)
					}
				} else {
					ChatDialog("Account", {
						showInfo.value = false
					}) {
						ShowInfo(clientUser)
					}
				}
			}
		}
	}
}

@Composable
fun SelectUsers(
	name: String,
	users: MutableList<UserInfo>,
	onDismissRequest: () -> Unit,
	onConfirm: (members: MutableList<UserInfo>) -> Unit
) {
	ChatDialog(name, onDismissRequest) {
		val members = remember { mutableStateOf(mutableListOf<UserInfo>()) }
		
		Scaffold(
			floatingActionButton = {
				FloatingActionButton({
					onConfirm(members.value)
				}) {
					Icon(Icons.Filled.Add, contentDescription = "Confirm")
				}
			}
		) {
			LazyColumn {
				items(
					users.filter { it.name != clientUser.name },
					{ it.name }) { user ->
					UserRow(user, members)
				}
			}
		}
	}
}

@Composable
private fun MenuButton(name: String, icon: ImageVector, onClick: () -> Unit) {
	Button(
		{
			onClick()
		},
		modifier = Modifier.fillMaxWidth()
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Icon(icon, contentDescription = name)
			Text(name, modifier = Modifier.padding(start = 5.dp))
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
fun ShowInfo(name: String, sign: String, description: String?, createTime: Long) {
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
		
		InfoRow(
			Icons.Outlined.Info,
			"Creation Time",
			createTime.timeToLocalDateTime().customToString()
		)
	}
}

@Composable
fun ShowInfo(userInfo: UserInfo) {
	var sign by remember { mutableStateOf("") }
	var description: String? by remember { mutableStateOf(null) }
	
	if (sign == "") {
		sign = "...".also {
			getUser(userInfo.name) {
				sign = it.lastLoginTime.timeToLocalDateTime().customToString()
			}
		}
		
		description = "...".also {
			getUser(userInfo.name) {
				description = it.description
			}
		}
	}
	
	ShowInfo(userInfo.name, sign, description, userInfo.createTime)
}

@Composable
fun ShowInfo(chat: Chat) {
	var sign by remember { mutableStateOf("") }
	var description: String? by remember { mutableStateOf(null) }
	
	if (chat.chatType == ChatType.Group) {
		sign = "Members: " + chat.members.size
		description = chat.description
		
		ShowInfo(chat.getNameClient(), sign, description, chat.createTime)
	} else {
		ShowInfo(chat.members.keys.find { it.name == chat.getNameClient() }!!)
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
		val chatName = chat.getNameClient()
		
		// Image box
		UserAvatar(chatName)
		
		var lastMessage = Message(clientUser, "Chat created", chat.createTime)
		
		try {
			lastMessage = chat.messages.last()
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
					if (lastMessage.creator.name == clientUser.name)
						MessageStatusIcon(lastMessage.messageStatus)
					
					Text(
						lastMessage.sendTime.timeToLocalDateTime().customToString(),
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
					chat.messages.count { it.creator.name != clientUser.name && it.messageStatus == MessageStatus.UnRead }
				
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
fun UserRow(user: UserInfo, members: MutableState<MutableList<UserInfo>>) {
	var selected by remember { mutableStateOf(false) }
	
	Row(
		modifier = Modifier.fillMaxWidth().toggleable(selected) {
			selected = it
			
			if (selected)
				members.value.add(user)
			else
				members.value.remove(user)
		}.background(if (selected) Colors.SELECTED else MaterialTheme.colors.background)
	) {
		UserRow(user)
	}
}

@Composable
fun UserRow(user: UserInfo) {
	Row {
		Row(
			verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(5.dp)
		) {
			UserAvatar(user.displayName)
			
			Text(user.displayName, Modifier.padding(start = 5.dp))
		}
	}
}