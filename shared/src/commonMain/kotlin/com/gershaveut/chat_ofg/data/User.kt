package com.gershaveut.chat_ofg.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class User(
	val name: String,
	var displayName: String = name,
	var description: String? = null,
	var lastLoginTime: Long = Clock.System.now().epochSeconds,
	val createTime: Long = Clock.System.now().epochSeconds,
	var password: String,
	var chats: MutableList<String> = mutableListOf(),
) {
	fun toUserInfo() = UserInfo(this)
}