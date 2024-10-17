package com.gershaveut.chat_ofg

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.Message
import com.gershaveut.chat_ofg.data.UserInfo

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		setContent {
			App()
		}
	}
}

val userTest = UserInfo("Test")
val user = UserInfo("DEV")
val chats = mutableListOf(
	Chat(
		members = mutableMapOf(user to true, userTest to true),
		messages = mutableListOf(
			Message(userTest, "test"),
			Message(user, "test")
		)
	)
)

@Composable
fun Preview(content: @Composable () -> Unit) {
	Client.user = user
	Client.chats = chats
	
	MaterialTheme {
		Surface(modifier = Modifier.sizeIn(500.dp, 750.dp)) {
			content()
		}
	}
}

/*
@Preview
@Composable
fun AppAndroidPreview() {
	Preview {
		App()
	}
}

@SuppressLint("UnrememberedMutableState", "MutableCollectionMutableState")
@Preview
@Composable
fun AppAndroidPreviewMenu() {
	Preview {
		Menu(mutableStateOf(user), mutableStateOf(false), mutableStateOf(chats))
	}
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
fun AppAndroidPreviewChatInfo() {
	Preview {
		OpenChat(
			chats[0],
			mutableStateOf(false),
			mutableStateOf(null),
		)
	}
}
 */