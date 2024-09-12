package com.gershaveut.chat_ofg.data

import com.gershaveut.chat_ofg.cdtToString
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class PrivateChat(var creater: User, var user: User, var createTime: LocalDateTime, var messages: MutableList<Message> = mutableListOf()) : Chat {
	override fun getNameChat(): String = user.displayName
	override fun getSignChat(): String = "Last login: " + cdtToString(user.lastLogin)
	override fun getCreateTimeChat(): LocalDateTime = createTime
	override fun getMessagesChat(): MutableList<Message> = messages
	override fun getDescriptionChat(): String? = user.discription
}