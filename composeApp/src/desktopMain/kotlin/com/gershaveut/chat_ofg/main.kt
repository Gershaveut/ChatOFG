package com.gershaveut.chat_ofg

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ChatOFG",
    ) {
        App()
    }
}