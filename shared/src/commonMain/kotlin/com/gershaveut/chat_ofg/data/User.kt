package com.gershaveut.chat_ofg.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class User(
	var name: String,
	var displayName: String = name,
	var description: String? = null,
	var lastLoginTime: Long = Clock.System.now().epochSeconds,
	var createTime: Long = Clock.System.now().epochSeconds,
	var password: String,
	var chats: MutableList<String> = mutableListOf(),
) {
	fun toUserInfo() = UserInfo(this)

	fun setFromUserInfo(userInfo: UserInfo) {
		name = userInfo.name
		displayName = userInfo.displayName
		description = userInfo.description
		lastLoginTime = userInfo.lastLoginTime
		createTime = userInfo.createTime
	}
}