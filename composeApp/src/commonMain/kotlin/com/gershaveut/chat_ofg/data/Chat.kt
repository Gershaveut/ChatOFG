package com.gershaveut.chat_ofg.data

import com.gershaveut.chat_ofg.cdtToString
import kotlinx.datetime.LocalDateTime

class Chat(val user: User, createTime: LocalDateTime, messages: ArrayList<Message>) : AbstractChat(user.displayName, cdtToString(user.lastLogin), createTime, messages, user.discription)