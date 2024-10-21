package com.gershaveut.chat_ofg.data

import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
	val id: String = uuid4().toString(),
	var members: MutableMap<UserInfo, Boolean>,
	private var name: String? = null,
	var messages: MutableList<Message> = mutableListOf(),
	val createTime: Long = Clock.System.now().epochSeconds,
	var description: String? = null,
	val chatType: ChatType = if (members.count() > 2) ChatType.Group else ChatType.PrivateChat
) {
	init {
		if (chatType == ChatType.PrivateChat) {
			members = members.keys.associateWith { true }.toMutableMap()
		}
	}
	
	constructor(creator: UserInfo, user: UserInfo) : this(
		members = mutableMapOf(
			creator to true,
			user to true
		)
	)
	
	fun getName(user: UserInfo? = null): String {
		return name ?: if (user != null && chatType == ChatType.PrivateChat) {
			if (user.name == members.keys.first().name)
				members.keys.last().displayName
			else
				members.keys.first().displayName
		} else {
			members.entries.joinToString { it.key.displayName }
		}
	}
	
	fun setName(name: String?) {
		this.name = name
	}
	
	fun userAccess(userInfo: UserInfo) : Boolean {
		members.forEach {
			if (it.key.name == userInfo.name)
				return it.value
		}
		
		return false
	}
	
	fun unreadMessagesCount(user: UserInfo) = messages.count { it.creator.name != user.name && it.messageStatus == MessageStatus.UnRead }
}