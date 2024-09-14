package com.gershaveut.chat_ofg.data

import kotlinx.datetime.LocalDateTime

interface Chat {
	fun getNameChat(): String
	fun getNameChat(clientUser: User): String = getNameChat()
	fun getSignChat(): String
	fun getSignChat(clientUser: User): String = getSignChat()
	fun getCreateTimeChat(): LocalDateTime
	fun getMessagesChat(): MutableList<Message>
	fun getDescriptionChat(): String?
	fun getDescriptionChat(clientUser: User): String? = getDescriptionChat()
	fun getMembers() : List<String>

	fun isMember(name: String) : Boolean = getMembers().any { it == name }
}