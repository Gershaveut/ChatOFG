package com.gershaveut.chat_ofg.data

import kotlinx.datetime.LocalDateTime

interface Chat {
	fun getNameChat(): String
	fun getNameChat(user: User): String = getNameChat()
	fun getSignChat(): String
	fun getSignChat(user: User): String = getSignChat()
	fun getCreateTimeChat(): LocalDateTime
	fun getMessagesChat(): MutableList<Message>
	fun getDescriptionChat(): String?
	fun getDescriptionChat(user: User): String? = getDescriptionChat()
	fun getMembers() : List<String>

	fun isMember(name: String) : Boolean = getMembers().any { it == name }
}