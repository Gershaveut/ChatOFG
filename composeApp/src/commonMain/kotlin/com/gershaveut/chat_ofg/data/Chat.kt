package com.gershaveut.chat_ofg.data

import kotlinx.datetime.LocalDateTime

class Chat(val user: User, createTime: LocalDateTime, messages: ArrayList<Message>) : AbstractChat(user.displayName, user.lastLogin.toString(), createTime, messages, user.discription)