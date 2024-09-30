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

    var user: User? = null

    var users = mutableListOf<UserInfo>()
    var chats = mutableListOf<Chat>()

    var onSync: (() -> Unit)? = null

    private var authName: String? = null
    private var authPassword: String? = null

    private val domain get() = "http://$host:$SERVER_PORT"

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

    suspend fun handleConnection() {
        client.webSocket(method = HttpMethod.Get, host = host, port = SERVER_PORT, path = "/echo") {
            while (user != null) {
                val userName = incoming.receive() as? Frame.Text ?: continue

                if (user == null)
                    continue

                if (userName.readText() == user!!.name)
                    sync()
            }
        }
    }

    suspend fun auth(name: String, password: String) {
        authName = name
        authPassword = password

        user = client.get("$domain/").body()
    }

    suspend fun getUsers(): MutableList<UserInfo> = client.get("$domain/users").body()
    suspend fun getChats(): MutableList<Chat> = client.get("$domain/chats").body()

    suspend fun getUser(name: String): UserInfo = client.get("$domain/user/$name").body()

    suspend fun createChat(chat: Chat, onCreated: ((Chat) -> Unit)? = null) {
        if (client.post("$domain/chat") {
                contentType(ContentType.Application.Json)
                setBody(chat)
            }.status == HttpStatusCode.Created) {
            onCreated?.let { it(chat) }
        }
    }

    suspend fun sendMessage(message: Message, chat: Chat, onCreated: ((Message) -> Unit)? = null) {
        chat.messages.add(message)

        if (client.post("$domain/chat/message") {
                contentType(ContentType.Application.Json)
                setBody(message)
                parameter("chatName", chat.id)
            }.status == HttpStatusCode.Created) {
            onCreated?.let { it(message) }
        }
    }

    suspend fun readMessages(chat: Chat) {
        chat.messages.forEach {
            if (it.creator != user!!.name) {
                it.messageStatus = MessageStatus.Read
            }
        }

        client.post("$domain/chat/read") {
            parameter("chatName", chat.id)
        }
    }

    suspend fun sync() {
        chats = getChats()
        users = getUsers()

        onSync?.invoke()
    }
}