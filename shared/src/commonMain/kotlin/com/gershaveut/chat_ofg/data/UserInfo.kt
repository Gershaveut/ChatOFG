package com.gershaveut.chat_ofg.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Suppress("EqualsOrHashCode")
@Serializable
data class UserInfo(
    var name: String,
    var displayName: String = name,
    var description: String? = null,
    var lastLoginTime: Long = Clock.System.now().epochSeconds,
    var createTime: Long = Clock.System.now().epochSeconds,
) {
    override fun equals(other: Any?): Boolean = other is UserInfo && name == other.name
}