package com.gershaveut.chat_ofg

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chatofg.composeapp.generated.resources.*
import chatofg.composeapp.generated.resources.Res
import chatofg.composeapp.generated.resources.actions
import chatofg.composeapp.generated.resources.chat_settings
import chatofg.composeapp.generated.resources.info
import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.ChatType
import com.gershaveut.chat_ofg.data.UserInfo
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppSettings(openSettings: MutableState<Boolean>) {
	var password: String? = null
	
	Settings(openSettings, stringResource(Res.string.settings), {
		if (Client.user != null) {
			updateUser()
			
			if (password != null)
				updatePassword(password!!)
		}
	}) {
		
		LazyColumn {
			item {
				if (Client.user != null) {
					Category(stringResource(Res.string.account)) {
						Filed(stringResource(Res.string.display_name), clientUser.displayName, clientUser.name) {
							clientUser.displayName = it
						}
						
						FiledNullable(stringResource(Res.string.description), clientUser.description) {
							clientUser.description = it
						}
						
						FiledNullable(stringResource(Res.string.password)) {
							password = it
						}
					}
				}
				
				Category(stringResource(Res.string.application)) {
					Filed(stringResource(Res.string.server), Client.host, HOST_DEFAULT, stringResource(Res.string.server_host)) {
						Client.host = it
					}
				}
			}
		}
	}
}

@Composable
fun ChatSettings(openSettings: MutableState<Boolean>, chat: Chat, admin: Boolean) {
	Settings(openSettings, "${stringResource(Res.string.chat_settings)} " + chat.getNameClient(), {
		updateChat(chat)
	}) {
		LazyColumn {
			item {
				Category(stringResource(Res.string.info)) {
					Filed(
						stringResource(Res.string.name),
						chat.getNameClient(),
						chat.getNameClient(),
						preview = false,
						readOnly = !admin || chat.chatType == ChatType.PrivateChat
					) {
						chat.setName(it)
					}
					
					if (chat.chatType == ChatType.Group) {
						FiledNullable(stringResource(Res.string.description), chat.description, readOnly = !admin) {
							chat.description = it
						}
					}
				}
				
				var userInfo by remember { mutableStateOf<UserInfo?>(null) }
				
				if (userInfo != null)
					ChatDialog(stringResource(Res.string.user_info), {
						userInfo = null
					}) {
						ShowInfo(userInfo!!.name)
					}
				
				if (chat.chatType == ChatType.Group) {
					Category(stringResource(Res.string.members)) {
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
											Text(stringResource(Res.string.admin), color = BACKGROUND_VARIANT)
									}
									
									var expanded by remember { mutableStateOf(false) }
									
									Column {
										IconButton({
											expanded = true
										}) {
											Icon(Icons.Filled.MoreVert, stringResource(Res.string.actions))
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
												Text(stringResource(Res.string.show))
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
														Text(stringResource(Res.string.give_admin))
													}
												}
												
												TextButton(
													{
														expanded = false
														
														kickChat(member.key.name, chat)
													},
													modifier = Modifier.width(widthButton)
												) {
													Text(stringResource(Res.string.kick))
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
fun Settings(
	openSettings: MutableState<Boolean>,
	text: String,
	onClose: () -> Unit,
	content: @Composable SettingsScope.() -> Unit
) {
	Column {
		TopAppBar(
			title = {
				Text(text)
			},
			navigationIcon = {
				IconButton({
					openSettings.value = false
					
					if (InstanceSettingsScope.save) {
						onClose()
						InstanceSettingsScope.save = false
					}
				}) {
					Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
				}
			}
		)
		
		InstanceSettingsScope.content()
	}
}

object InstanceSettingsScope : SettingsScope()

open class SettingsScope {
	var save = false
	
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
					save = true
					
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
				Text(description, fontSize = 10.sp, color = BACKGROUND_VARIANT)
		}
	}
	
	@Composable
	fun SettingsRow(content: @Composable () -> Unit) {
		Row(modifier = Modifier.padding(5.dp)) {
			content()
		}
	}
}