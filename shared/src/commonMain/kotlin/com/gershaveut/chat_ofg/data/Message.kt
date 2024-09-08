package com.gershaveut.chat_ofg.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val owner: User,
    var text: String,
    val sendTime: LocalDateTime,
    var messageStatus: MessageStatus = MessageStatus.UnRead,
    var modified: Boolean = false
)