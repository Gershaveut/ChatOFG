package com.gershaveut.chat_ofg

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.ChatType
import com.gershaveut.chat_ofg.data.UserInfo

@Composable
fun AppSettings(openSettings: MutableState<Boolean>) {
	Column {
		var password: String? = null
		
		SettingsBar(openSettings, "Settings") {
			if (Client.user != null) {
				updateUser()
				
				if (password != null)
					updatePassword(password!!)
			}
		}
		
		LazyColumn {
			item {
				if (Client.user != null) {
					Category("Account") {
						Filed("Display name", clientUser.displayName, clientUser.name) {
							clientUser.displayName = it
						}
						
						FiledNullable("Description", clientUser.description) {
							clientUser.description = it
						}
						
						FiledNullable("Password") {
							password = it
						}
					}
				}
				
				Category("Application") {
					Filed("Server", Client.host, "Server host", HOST_DEFAULT) {
						Client.host = it
					}
				}
			}
		}
	}
}

@Composable
fun ChatSettings(openSettings: MutableState<Boolean>, chat: Chat, admin: Boolean) {
	Column {
		SettingsBar(openSettings, "Settings chat " + chat.getNameClient()) {
			updateChat(chat)
		}
		
		LazyColumn {
			item {
				Category("Info") {
					Filed(
						"Name",
						chat.getNameClient(),
						chat.getNameClient(),
						preview = false,
						readOnly = !admin || chat.chatType == ChatType.PrivateChat
					) {
						chat.setName(it)
					}
					
					if (chat.chatType == ChatType.Group) {
						FiledNullable("Description", chat.description, readOnly = !admin) {
							chat.description = it
						}
					}
				}
				
				var userInfo by remember { mutableStateOf<UserInfo?>(null) }
				
				if (userInfo != null)
					ChatDialog("User Info", {
						userInfo = null
					}) {
						ShowInfo(userInfo!!)
					}
				
				if (chat.chatType == ChatType.Group) {
					Category("Members") {
						Column {
							var members by remember { mutableStateOf(chat.members.entries) }
							
							sync {
								members = Client.chats.find { it.id == chat.id }?.members!!.entries
							}
							
							members.forEach { member ->
								Row(
									modifier = Modifier.fillMaxWidth(),
									horizontalArrangement = Arrangement.SpaceBetween,
									verticalAlignment = Alignment.CenterVertically
								)
								{
									Row(verticalAlignment = Alignment.CenterVertically) {
										
										UserRow(member.key)
										if (member.value)
											Text("Admin", color = Colors.BACKGROUND_VARIANT)
									}
									
									var expanded by remember { mutableStateOf(false) }
									
									Column {
										IconButton({
											expanded = true
										}) {
											Icon(Icons.Filled.MoreVert, "Actions")
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
													
													userInfo = member.key
												},
												modifier = Modifier.width(widthButton)
											) {
												Text("Show Info")
											}
											
											if (admin) {
												Divider()
												
												if (!member.value) {
													TextButton(
														{
															expanded = false
															
															adminChat(member.key.name, chat)
														},
														modifier = Modifier.width(widthButton)
													) {
														Text("Give admin")
													}
												}
												
												TextButton(
													{
														expanded = false
														
														kickChat(member.key.name, chat)
													},
													modifier = Modifier.width(widthButton)
												) {
													Text("Kick")
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

@Composable
fun SettingsBar(openSettings: MutableState<Boolean>, text: String, onClose: () -> Unit) {
	TopAppBar(
		title = {
			Text(text)
		},
		navigationIcon = {
			IconButton({
				openSettings.value = false
				onClose()
			}) {
				Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
			}
		}
	)
}

@Composable
fun Category(name: String, content: @Composable () -> Unit) {
	Column {
		Row(
			Modifier.height(50.dp).fillMaxWidth()
				.padding(start = 10.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(name, fontSize = 18.sp)
		}
		
		Divider()
		
		content()
	}
}

@Composable
fun FiledNullable(
	name: String,
	value: String? = null,
	defaultValue: String? = null,
	description: String? = null,
	readOnly: Boolean = false,
	preview: Boolean = true,
	onValueChanged: (text: String?) -> Unit,
) {
	var textFiled by remember { mutableStateOf(if (value != defaultValue) value else if (!preview) value else "") }
	
	SettingsRow {
		SettingInfo(name, description)
		
		TextField(
			textFiled ?: "", { text ->
				textFiled = text
				
				if (text.isNotEmpty())
					onValueChanged(text)
				else if (defaultValue != null)
					onValueChanged(defaultValue)
				else
					onValueChanged(null)
			},
			modifier = Modifier.fillMaxWidth(),
			placeholder = { if (defaultValue != null && preview) Text(defaultValue) },
			readOnly = readOnly
		)
	}
}

@Composable
fun Filed(
	name: String,
	value: String?,
	defaultValue: String,
	description: String? = null,
	readOnly: Boolean = false,
	preview: Boolean = true,
	onValueChanged: (text: String) -> Unit,
) {
	FiledNullable(name, value, defaultValue, description, readOnly, preview) {
		onValueChanged(it!!)
	}
}

@Composable
fun SettingInfo(name: String, description: String? = null) {
	Column(
		verticalArrangement = Arrangement.Center,
		modifier = Modifier.width(200.dp).padding(10.dp).padding(end = 50.dp)
	) {
		Text(name)
		
		if (description != null)
			Text(description, fontSize = 10.sp, color = Colors.BACKGROUND_VARIANT)
	}
}

@Composable
fun SettingsRow(content: @Composable () -> Unit) {
	Row(modifier = Modifier.padding(5.dp)) {
		content()
	}
}