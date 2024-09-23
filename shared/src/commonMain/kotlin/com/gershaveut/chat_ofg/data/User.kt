package com.gershaveut.chat_ofg.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Suppress("EqualsOrHashCode")
@Serializable
data class User(
    var name: String,
    var password: String,
    var chats: MutableList<Chat> = mutableListOf(),
    var displayName: String = name,
    var description: String? = null,
    var lastLoginTime: Long = Clock.System.now().epochSeconds,
    var createTime: Long = Clock.System.now().epochSeconds,
) {
    override fun equals(other: Any?): Boolean = other is User && name == other.name
}