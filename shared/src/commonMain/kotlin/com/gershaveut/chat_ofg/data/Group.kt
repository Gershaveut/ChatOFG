package com.gershaveut.chat_ofg.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

class Group(
    val users: MutableList<User>,
    name: String,
    createTime: LocalDateTime,
    messages: MutableList<Message>,
    description: String? = null
) : AbstractChat(name, "Users: " + users.count(), createTime, messages, description)