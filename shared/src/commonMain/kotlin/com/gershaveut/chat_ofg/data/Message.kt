package com.gershaveut.chat_ofg.data

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val creator: User,
    var text: String,
    val sendTime: Long = Clock.System.now().epochSeconds,
    var messageStatus: MessageStatus = MessageStatus.UnRead,
    var modified: Boolean = false
)