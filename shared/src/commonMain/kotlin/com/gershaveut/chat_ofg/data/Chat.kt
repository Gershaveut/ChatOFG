package com.gershaveut.chat_ofg.data

import kotlinx.datetime.LocalDateTime

interface Chat {
	fun getNameChat(): String
	fun getSignChat(): String
	fun getCreateTimeChat(): LocalDateTime
	fun getMessagesChat(): MutableList<Message>
	fun getDescriptionChat(): String?
}