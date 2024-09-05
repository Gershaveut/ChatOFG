package com.gershaveut.chat_ofg.data

import kotlinx.datetime.LocalDateTime

class Group(val users: ArrayList<User>, name: String, createTime: LocalDateTime, messages: ArrayList<Message>, description: String? = null) : AbstractChat(name, "Users: " + users.count(), createTime, messages, description)