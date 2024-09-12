package com.gershaveut.chat_ofg.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Suppress("EqualsOrHashCode")
@Serializable
data class User(
	var name: String,
	var displayName: String = name,
	var discription: String? = null,
	var lastLogin: LocalDateTime,
	var password: String
) {
	override fun equals(other: Any?): Boolean = other is User && name == other.name
}