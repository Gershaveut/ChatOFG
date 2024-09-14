package com.gershaveut.chat_ofg.data

import com.gershaveut.chat_ofg.cdtToString
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class PrivateChat(var creater: User, var user: User, var createTime: LocalDateTime, var messages: MutableList<Message> = mutableListOf()) : Chat {
	override fun getNameChat(): String = user.displayName
	override fun getNameChat(user: User): String {
		return if (creater == user) {
			user.displayName
		} else {
			creater.displayName
		}
	}
	override fun getSignChat(): String = "Last login: " + cdtToString(user.lastLogin)
	override fun getSignChat(user: User): String {
		return "Last login: " + if (creater == user) {
			cdtToString(user.lastLogin)
		} else {
			cdtToString(creater.lastLogin)
		}
	}
	override fun getCreateTimeChat(): LocalDateTime = createTime
	override fun getMessagesChat(): MutableList<Message> = messages
	override fun getDescriptionChat(): String? = user.discription
	override fun getDescriptionChat(user: User): String? {
		return if (creater == user) {
			user.discription
		} else {
			creater.discription
		}
	}
	override fun getMembers(): List<String> = listOf(creater.name, user.name)
}