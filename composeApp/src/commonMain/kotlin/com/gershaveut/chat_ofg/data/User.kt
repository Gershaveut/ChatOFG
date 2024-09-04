package com.gershaveut.chat_ofg.data

import kotlinx.datetime.LocalDateTime

data class User(var name: String, var displayName: String = name, var discription: String? = null, var lastLogin: LocalDateTime) {
}