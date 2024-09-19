package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.*

object Client {
    var host = HOST_DEFAULT
    private val domain get() = "http://$host:$SERVER_PORT"

    var user: User? = null

    var authName: String? = null
    var authPassword: String? = null

    var users = mutableListOf<User>()
    var chats = mutableListOf<Chat>()

    var onSync: (() -> Unit)? = null

    private val client = HttpClient(CIO) {
        install(WebSockets)
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

    suspend fun auth() : User = client.get("$domain/").body()

    suspend fun getUsers(): MutableList<User> = client.get("$domain/users").body()

    suspend fun getGroups(): MutableList<Group> = client.get("$domain/groups").body()

    suspend fun getPrivateChats(): MutableList<PrivateChat> = client.get("$domain/private-chats").body()

    suspend fun createPrivateChat(privateChat: PrivateChat, onCreated: ((Chat) -> Unit)? = null) {
        if (client.post("$domain/private-chat") {
                contentType(ContentType.Application.Json)
                setBody(privateChat)
            }.status == HttpStatusCode.Created) {
            onCreated?.let { it(privateChat) }
            chats.add(privateChat)
        }
    }

    suspend fun sendMessage(message: Message, chat: Chat, onCreated: ((Message) -> Unit)? = null) {
        if (client.post("$domain/chat") {
                contentType(ContentType.Application.Json)
                setBody(message)
                parameter("chatName", chat.getNameChat())
            }.status == HttpStatusCode.Created) {
            onCreated?.let { it(message) }
        }
    }

    suspend fun readMessages(chat: Chat) {
        client.post("$domain/chat/read") {
            parameter("chatName", chat.getNameChat())
        }
    }

    suspend fun handleConnection() {
        client.webSocket(method = HttpMethod.Get, host = host, port = SERVER_PORT, path = "/echo") {
            while(user != null) {
                val userName = incoming.receive() as? Frame.Text ?: continue

                if (user == null)
                    continue

                if (userName.readText() == user!!.name)
                    sync()
            }
        }
    }

    suspend fun sync() {
        val tempChats = mutableListOf<Chat>()

        tempChats.addAll(getGroups())
        tempChats.addAll(getPrivateChats())

        chats = tempChats

        users = getUsers()

        onSync?.invoke()
    }
}