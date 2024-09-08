package com.gershaveut.chat_ofg.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
open class AbstractChat(
	var name: String,
	var sign: String,
	var createTime: LocalDateTime,
	var messages: MutableList<Message>,
	var description: String? = null
)