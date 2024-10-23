package com.gershaveut.chat_ofg

import androidx.annotation.IntRange
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chatofg.composeapp.generated.resources.*
import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.ChatType
import com.gershaveut.chat_ofg.data.MessageStatus
import com.gershaveut.chat_ofg.data.UserInfo
import com.russhwolf.settings.set
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.enums.EnumEntries

@Composable
fun AppSettings(openSettings: MutableState<Boolean>) {
	var password: String? = null
	
	val chatSettings = remember { mutableStateOf(false) }
	
	Settings(openSettings, stringResource(Res.string.settings), {
		if (Client.user != null) {
			settings[KEY_HOST] = Client.host
			
			updateUser()
			
			if (password != null)
				updatePassword(password!!)
		}
		
		settings[KEY_MESSAGE_TEXT_SIZE] = messageTextSize
		settings[KEY_MESSAGE_CORNERS] = messageCorners
	}, chatSettings to stringResource(Res.string.chat_settings)) {
		LazyColumn {
			item {
				if (!chatSettings.value) {
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
						Filed(
							stringResource(Res.string.server),
							Client.host,
							HOST_DEFAULT,
							stringResource(Res.string.server_host)
						) {
							Client.host = it
						}
						
						Button(stringResource(Res.string.chat_settings)) {
							chatSettings.value = true
						}
					}
					
					if (DEBUG) {
						Category("DEBUG") {
							FiledNullable("Filed Nullable", null) {
							
							}
							
							Filed("Filed", null, "null") {
							
							}
							
							Dropdown(MessageStatus.entries, "Dropdown", MessageStatus.UnSend) {
							
							}
						}
					}
				} else {
					// Chat Settings
					Category(stringResource(Res.string.message_settings)) {
						var messageTextSizePreview by remember { mutableStateOf(messageTextSize) }
						var messageCornersPreview by remember { mutableStateOf(messageTextSize) }
						
						SliderRange(stringResource(Res.string.message_text_size), messageTextSize, 10f..20f, 7) {
							messageTextSize = it
							messageTextSizePreview = it
						}
						
						SliderRange(stringResource(Res.string.message_corners), messageCorners, 0f..17f, 0) {
							messageCorners = it
							messageCornersPreview = it
						}
						
						Divider()
						
						Column {
							val settingUser = UserInfo("User")
							
							val chat = Chat(clientUser, settingUser).apply {
								messages = mutableListOf(
									com.gershaveut.chat_ofg.data.Message(
										settingUser,
										stringResource(Res.string.setting_user_message)
									),
									com.gershaveut.chat_ofg.data.Message(
										clientUser,
										stringResource(Res.string.setting_client_message)
									)
								)
							}
							
							chat.messages.forEachIndexed { index, message ->
								MessageRow(message, messageTextSizePreview, messageCornersPreview, chat.messages, index, true)
							}
						}
						
						Divider()
					}
				}
			}
		}
	}
}

@Composable
fun ChatSettings(openSettings: MutableState<Boolean>, chat: Chat, admin: Boolean) {
	val snackbarHostState = remember { SnackbarHostState() }
	
	val scope = rememberCoroutineScope()
	
	fun snackbar(text: String) {
		scope.launch {
			snackbarHostState.showSnackbar(text)
		}
	}
	
	Scaffold(snackbarHost = {
		SnackbarHost(snackbarHostState) {
			Snackbar(snackbarData = it)
		}
	}) {
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
													
													val giveAdminText = stringResource(Res.string.given_admin)
													val kickedMemberText = stringResource(Res.string.kicked_member)
													
													if (!member.value) {
														TextButton(
															{
																expanded = false
																
																adminChat(member.key.name, chat)
																
																snackbar("$giveAdminText ${member.key.displayName}")
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
															
															snackbar("$kickedMemberText ${member.key.displayName}")
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
}

@Composable
fun Settings(
	openSettings: MutableState<Boolean>,
	text: String,
	onClose: () -> Unit,
	vararg subSettings: Pair<MutableState<Boolean>, String>?,
	content: @Composable SettingsScope.() -> Unit
) {
	Column {
		TopAppBar(
			title = {
				val titleText = if (subSettings.isNotEmpty()) {
					subSettings.find { it?.first?.value == true }?.second ?: text
				} else {
					text
				}
				
				Text(titleText)
			},
			navigationIcon = {
				IconButton({
					if (subSettings.any { it?.first?.value == true }) {
						subSettings.forEach {
							it!!.first.value = false
						}
					} else {
						openSettings.value = false
						
						if (InstanceSettingsScope.save) {
							onClose()
							InstanceSettingsScope.save = false
						}
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
	fun Button(
		name: String,
		description: String? = null,
		onClick: () -> Unit,
	) {
		SettingsRow(Modifier.fillMaxWidth().clickable {
			onClick()
		}, Arrangement.SpaceBetween) {
			SettingInfo(name, description)
			
			Icon(Icons.Filled.PlayArrow, stringResource(Res.string.go))
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
	fun <T : Enum<T>> Dropdown(
		enumEntries: EnumEntries<T>,
		name: String,
		value: Enum<T>,
		description: String? = null,
		readOnly: Boolean = false,
		onValueChanged: (value: Enum<T>) -> Unit,
	) {
		var expanded by remember { mutableStateOf(false) }
		var closeValue by remember { mutableStateOf(value) }
		
		SettingsRow {
			SettingInfo(name, description)
			
			Row(
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.size(150.dp, 50.dp)
			) {
				Text(closeValue.name)
				
				if (!readOnly) {
					Column {
						IconButton({
							expanded = true
						}) {
							Icon(Icons.Filled.ArrowDropDown, stringResource(Res.string.expand))
						}
						
						DropdownMenu(expanded, {
							expanded = false
						}) {
							enumEntries.forEach {
								DropdownMenuItem({
									save = true
									
									closeValue = it
									expanded = false
									
									onValueChanged(it)
								}) {
									Text(it.name)
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Composable
	fun SliderRange(
		name: String,
		value: Float,
		valueRange: ClosedFloatingPointRange<Float>,
		@IntRange steps: Int,
		description: String? = null,
		readOnly: Boolean = false,
		onValueChanged: (value: Float) -> Unit,
	) {
		var sliderValue by remember { mutableStateOf(value) }
		
		SettingsRow {
			SettingInfo(name, description)
			
			Slider(
				sliderValue,
				{ sliderValue = it },
				valueRange = valueRange,
				steps = steps,
				enabled = !readOnly,
				modifier = Modifier.fillMaxWidth(),
				onValueChangeFinished = {
					save = true
					
					onValueChanged(sliderValue)
				})
		}
	}
	
	@Composable
	fun SwitchCheck(
		name: String,
		value: Boolean,
		description: String? = null,
		readOnly: Boolean = false,
		onValueChanged: (value: Boolean) -> Unit,
	) {
		var checked by remember { mutableStateOf(value) }
		
		SettingsRow {
			SettingInfo(name, description)
			
			Switch(checked, {
				onValueChanged(it)
				
				checked = it
			}, enabled = !readOnly)
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
	fun SettingsRow(
		modifier: Modifier = Modifier,
		horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
		content: @Composable () -> Unit
	) {
		Row(
			modifier = modifier.padding(5.dp),
			horizontalArrangement = horizontalArrangement,
			verticalAlignment = Alignment.CenterVertically
		) {
			content()
		}
	}
}