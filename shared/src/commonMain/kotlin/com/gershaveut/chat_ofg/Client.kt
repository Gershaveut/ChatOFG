package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object Client {
    val dataTime get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    val user = User(
        "DEV",
        lastLogin = dataTime
    )

    var users: MutableList<User> = mutableListOf()
    var chats: MutableList<Chat> = mutableListOf()

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun getUsers(): MutableList<User> = client.get("$DOMAIN/users").body()

    suspend fun getGroups(): MutableList<Group> = client.get("$DOMAIN/groups").body()

    suspend fun getPrivateChats(): MutableList<PrivateChat> = client.get("$DOMAIN/private-chats").body()

    suspend fun createPrivateChat(privateChat: PrivateChat) {
        client.post("$DOMAIN/private-chat") {
            contentType(ContentType.Application.Json)
            setBody(privateChat)
        }
    }

    suspend fun sendMessage(message: Message, chat: Chat, onCreated: () -> Unit) {
        if (client.post("$DOMAIN/chat/${chat.getNameChat()}") {
            contentType(ContentType.Application.Json)
            setBody(message)
        }.status == HttpStatusCode.Created) {
            onCreated()
        }
    }
}