package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.Group
import com.gershaveut.chat_ofg.data.PrivateChat
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

var chats: MutableList<Chat> = mutableListOf()

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun getUsers() : MutableList<User> = client.get("$DOMAIN/users").body()

suspend fun getGroups() : MutableList<Group> = client.get("$DOMAIN/groups").body()

suspend fun getPrivateChats() : MutableList<PrivateChat> = client.get("$DOMAIN/private-chats").body()