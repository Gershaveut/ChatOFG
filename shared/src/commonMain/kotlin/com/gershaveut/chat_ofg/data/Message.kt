package com.gershaveut.chat_ofg.data

import kotlinx.datetime.LocalDateTime

class Message(
    val owner: User,
    var text: String,
    val sendTime: LocalDateTime,
    var messageStatus: MessageStatus = MessageStatus.UnRead,
    var modified: Boolean = false
)