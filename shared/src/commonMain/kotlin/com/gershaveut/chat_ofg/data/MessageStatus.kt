package com.gershaveut.chat_ofg.data

import kotlinx.serialization.Serializable

@Serializable
enum class MessageStatus {
	UnSend,
	UnRead,
	Read
}