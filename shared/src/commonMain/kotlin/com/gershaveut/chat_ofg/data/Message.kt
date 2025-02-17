package com.gershaveut.chat_ofg.data

import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Message(
	var creator: UserInfo,
	var text: String,
	val sendTime: Long = Clock.System.now().epochSeconds,
	var messageStatus: MessageStatus = MessageStatus.UnRead,
	var modified: Boolean = false,
	val messageType: MessageType = MessageType.Default,
	var id: String = uuid4().toString(),
	var reply: Message? = null,
	var forwarded: Boolean = false
)