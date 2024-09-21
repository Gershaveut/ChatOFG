package com.gershaveut.chat_ofg.data

import com.gershaveut.chat_ofg.Client
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
	var members: Map<String, Boolean>,
	private var name: String? = null,
	var messages: MutableList<Message> = mutableListOf(),
	var createTime: Long = Clock.System.now().epochSeconds,
	var description: String? = null,
) {
	constructor(creator: User, user: User) : this(mapOf(creator.name to true,user.name to true))

	fun getName(user: User) : String {
		return name ?: if (user.name == members.keys.first())
			members.keys.last()
		else
			members.keys.first()
	}

	fun getName() : String {
		return if (Client.user != null) getName(Client.user!!) else members.keys.first()
	}
}