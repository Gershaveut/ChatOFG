package com.gershaveut.chat_ofg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    Surface( modifier = Modifier.sizeIn(500.dp, 750.dp) ) {
        App()
    }
}

@Preview
@Composable
fun AppAndroidPreviewChat() {
    Surface( modifier = Modifier.sizeIn(500.dp, 750.dp) ) {
        //Chat(clientChats[2])
    }
}

@Preview
@Composable
fun AppAndroidPreviewChatInfo() {
    Surface( modifier = Modifier.sizeIn(250.dp, 500.dp) ) {
        //clientChats[2].ShowInfo()
    }
}