package com.gershaveut.chat_ofg.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
	val name: String,
	var displayName: String = name,
	var description: String? = null,
	var lastLoginTime: Long = Clock.System.now().epochSeconds,
	val createTime: Long = Clock.System.now().epochSeconds,
) {
	constructor(user: User) : this(
		user.name,
		user.displayName,
		user.description,
		user.lastLoginTime,
		user.createTime
	)
}