package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.*

object Client {
    fun getDataTime(): LocalDateTime {
        val current = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val currentTime = current.time

        return LocalDateTime(current.date, LocalTime(currentTime.hour, currentTime.minute))
    }

    var user: User? = null

    var authName: String? = null
    var authPassword: String? = null

    var users = mutableSetOf<User>()
    var chats = mutableSetOf<Chat>()

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }

        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(username = authName!!, password = authPassword!!)
                }
                realm = "User Access"
            }
        }
    }

    suspend fun auth() : User = client.get("$DOMAIN/").body()

    suspend fun getUsers(): MutableSet<User> = client.get("$DOMAIN/users").body()

    suspend fun getGroups(): MutableSet<Group> = client.get("$DOMAIN/groups").body()

    suspend fun getPrivateChats(): MutableSet<PrivateChat> = client.get("$DOMAIN/private-chats").body()

    suspend fun createPrivateChat(privateChat: PrivateChat, onCreated: ((Chat) -> Unit)? = null) {
        if (client.post("$DOMAIN/private-chat") {
                contentType(ContentType.Application.Json)
                setBody(privateChat)
            }.status == HttpStatusCode.Created) {
            onCreated?.let { it(privateChat) }
            chats.add(privateChat)
        }
    }

    suspend fun sendMessage(message: Message, chat: Chat, onCreated: ((Message) -> Unit)? = null) {
        if (client.post("$DOMAIN/chat") {
                contentType(ContentType.Application.Json)
                setBody(message)
                parameter("chatName", chat.getNameChat())
            }.status == HttpStatusCode.Created) {
            onCreated?.let { it(message) }
            chat.getMessagesChat().add(message)
        }
    }
}