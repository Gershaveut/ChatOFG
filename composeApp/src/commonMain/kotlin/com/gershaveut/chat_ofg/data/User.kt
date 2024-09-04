package com.gershaveut.chat_ofg.data

import kotlin.time.Duration

data class User(var name: String, var displayName: String = name, var discription: String? = null, var lastLogin: Duration) {
}