package com.gershaveut.chat_ofg.data

import kotlinx.datetime.LocalDateTime

class Message(val owner: User, var text: String, val sendTime: LocalDateTime, var read: Boolean = false, var modified: Boolean = false)