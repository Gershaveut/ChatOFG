package com.gershaveut.chat_ofg.data

import kotlin.time.Duration

data class Message(val owner: User, var text: String, val sendTime: Duration, var read: Boolean = false, var modified: Boolean = false) {
}