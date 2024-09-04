package com.gershaveut.chat_ofg

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform