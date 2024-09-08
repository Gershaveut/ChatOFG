package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.AbstractChat
import com.gershaveut.chat_ofg.data.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val clientDataTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

val clientUser = User(
    "DEV",
    lastLogin = clientDataTime
)

var users: MutableList<User> = mutableListOf()
var chats: MutableList<AbstractChat> = mutableListOf()

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun getUsers() {
    users = client.get("http://localhost:8080/users/").body()
}