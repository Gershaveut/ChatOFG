package com.gershaveut.chat_ofg.data

import com.gershaveut.chat_ofg.cdtToString
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

class PrivateChat(val user: User, createTime: LocalDateTime, messages: MutableList<Message>) :
	AbstractChat(
		user.displayName,
		cdtToString(user.lastLogin),
		createTime,
		messages,
		user.discription
	)