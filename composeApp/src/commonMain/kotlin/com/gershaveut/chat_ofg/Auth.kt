package com.gershaveut.chat_ofg

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import chatofg.composeapp.generated.resources.*
import chatofg.composeapp.generated.resources.Res
import chatofg.composeapp.generated.resources.confirm
import chatofg.composeapp.generated.resources.name
import chatofg.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.stringResource

@Composable
fun Auth(type: String, openSettings: MutableState<Boolean>, onAuth: () -> Unit) {
	Scaffold(topBar = {
		IconButton({
			openSettings.value = true
		}) {
			Icon(Icons.Filled.Settings, contentDescription = stringResource(Res.string.settings))
		}
	}) {
		Column(
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.fillMaxSize()
		) {
			var name by remember { mutableStateOf("") }
			var password by remember { mutableStateOf("") }
			
			Text(type)
			
			val fieldModifier = Modifier.size(200.dp, 75.dp).padding(5.dp)
			
			TextField(
				name, { text ->
					name = text
				},
				modifier = fieldModifier,
				placeholder = { Text(stringResource(Res.string.name)) }
			)
			
			TextField(
				password, { text ->
					password = text
				},
				modifier = fieldModifier,
				placeholder = { Text(stringResource(Res.string.password)) },
				visualTransformation = PasswordVisualTransformation(),
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
			)
			
			Button({
				if (name.isNotEmpty() && password.isNotEmpty())
					auth(name, password, onAuth)
			}) {
				Text(stringResource(Res.string.confirm))
			}
		}
	}
}