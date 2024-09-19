package com.gershaveut.chat_ofg.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
	var members: Map<String, Boolean>,
	var name: String? = null,
	var messages: MutableList<Message> = mutableListOf(),
	var createTime: Long = Clock.System.now().epochSeconds,
	var description: String? = null,
) {
	constructor(creator: User, user: User) : this(mapOf(creator.name to true,user.name to true))
}