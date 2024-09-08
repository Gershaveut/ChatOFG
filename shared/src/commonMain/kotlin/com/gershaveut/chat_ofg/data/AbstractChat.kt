package com.gershaveut.chat_ofg.data

import kotlinx.datetime.LocalDateTime

abstract class AbstractChat(
	var name: String,
	var sign: String,
	var createTime: LocalDateTime,
	var messages: ArrayList<Message>,
	var description: String? = null
)