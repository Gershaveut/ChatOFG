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
import chatofg.composeapp.generated.resources.*
import com.gershaveut.chat_ofg.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun Menu(user: MutableState<UserInfo?>, openSettings: MutableState<Boolean>, chats: MutableState<MutableList<Chat>>) {
	val drawerState = rememberDrawerState(DrawerValue.Closed)
	val showInfo = remember { mutableStateOf(false) }
	
	val scope = rememberCoroutineScope()
	
	ModalDrawer(
		{
			NavigationMenu(scope, drawerState, showInfo, user, openSettings)
		},
		drawerState = drawerState
	) {
		val openChat: MutableState<Chat?> = remember { mutableStateOf(null) }
		var users by remember { mutableStateOf(Client.users) }
		
		Column {
			val snackbarHostState = remember { SnackbarHostState() }
			var createChat by remember { mutableStateOf(false) }
			
			if (openChat.value == null) {
				TopAppBar(
					title = { Text(stringResource(Res.string.app_name)) },
					navigationIcon = {
						IconButton({
							scope.launch {
								drawerState.open()
							}
						}) { Icon(Icons.Filled.Menu, contentDescription = stringResource(Res.string.menu)) }
					}
				)
			}
			
			Scaffold(
				floatingActionButton = {
					if (openChat.value == null) {
						FloatingActionButton({
							refreshUsers {
								users = Client.users
							}
							
							createChat = true
						}) {
							Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.create_chat))
						}
					}
				},
				floatingActionButtonPosition = FabPosition.End,
				snackbarHost = {
					SnackbarHost(snackbarHostState) {
						Snackbar(snackbarData = it)
					}
				}) {
				if (openChat.value == null) {
					sync {
						chats.value = Client.chats
						
						// Close chat if deleted TODO: Repair
						if (openChat.value != null) {
							if (!Client.chats.any { it.id == openChat.value!!.id })
								openChat.value = null
						}
					}
					
					LazyColumn {
						items(chats.value, { it.id }) { chat ->
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
					
					if (createChat) {
						SelectUsers(stringResource(Res.string.create_chat), users, {
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
					OpenChat(openChat.value!!, showInfo, openChat, snackbarHostState)
				}
				
				if (showInfo.value) {
					if (openChat.value != null) {
						ChatDialog(stringResource(Res.string.chat_info), {
							showInfo.value = false
						}) {
							ShowInfo(openChat.value!!)
						}
					} else {
						ChatDialog(stringResource(Res.string.account), {
							showInfo.value = false
						}) {
							ShowInfo(clientUser.name)
						}
					}
				}
			}
		}
	}
}

@Composable
fun NavigationMenu(
	scope: CoroutineScope,
	drawerState: DrawerState,
	showInfo: MutableState<Boolean>,
	user: MutableState<UserInfo?>,
	openSettings: MutableState<Boolean>
) {
	Column(modifier = Modifier.padding(5.dp), verticalArrangement = Arrangement.SpaceBetween) {
		Column {
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
				MenuButton(stringResource(Res.string.exit), Icons.AutoMirrored.Filled.ArrowBack) {
					user.value = null
					
					exit()
					
					info("Exit")
				}
				MenuButton(stringResource(Res.string.settings), Icons.Filled.Settings) {
					openSettings.value = true
				}
			}
		}
		
		Text("${stringResource(Res.string.version)}: $VERSION")
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
					Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.confirm))
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
								contentDescription = stringResource(Res.string.close)
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
					color = BACKGROUND_VARIANT,
					modifier = Modifier.padding(5.dp)
				)
			}
		}
		
		InfoRow(
			Icons.Outlined.Info,
			stringResource(Res.string.description),
			description ?: stringResource(Res.string.no_description)
		)
		
		InfoRow(
			Icons.Outlined.Info,
			stringResource(Res.string.creation_time),
			createTime.timeToLocalDateTime().customToString()
		)
	}
}

@Composable
fun ShowInfo(userName: String) {
	var userInfo: UserInfo? by remember { mutableStateOf(null) }
	
	if (userInfo == null) {
		getUser(userName) {
			userInfo = it
		}
	}
	
	ShowInfo(
		userInfo?.displayName ?: "...",
		if (userInfo == null) "..." else "${stringResource(Res.string.last_login)}: ${
			userInfo!!.lastLoginTime.timeToLocalDateTime().customToString()
		} \n@${userInfo!!.name}",
		userInfo?.description,
		userInfo?.createTime ?: 0
	)
}

@Composable
fun ShowInfo(chat: Chat) {
	var sign by remember { mutableStateOf("") }
	var description: String? by remember { mutableStateOf(null) }
	
	if (chat.chatType == ChatType.Group) {
		sign = "${stringResource(Res.string.members)}: " + chat.members.size
		description = chat.description
		
		ShowInfo(chat.getNameClient(), sign, description, chat.createTime)
	} else {
		ShowInfo(chat.members.keys.find { it.name == chat.getNameClient() }!!.name)
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
				color = BACKGROUND_VARIANT,
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
			color = BACKGROUND_SECONDARY,
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
		
		var lastMessage = Message(clientUser, stringResource(Res.string.chat_created), chat.createTime)
		
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
						color = BACKGROUND_VARIANT,
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
					color = BACKGROUND_VARIANT
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
		}.background(if (selected) SELECTED else MaterialTheme.colors.background)
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