package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

val chats
    get() = mutableSetOf<Chat>().apply {
        addAll(Data.privateChats)
        addAll(Data.groups)
    }

val messageResponseFlow = MutableSharedFlow<String>()
val sharedFlow = messageResponseFlow.asSharedFlow()

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(WebSockets)
    install(ContentNegotiation) {
        json()
    }
    install(Authentication) {
        basic("auth-basic") {
            realm = "User Access"
            validate { credentials ->
                Data.users.find { it.name == credentials.name }?.let {
                    if (it.password == credentials.password) {
                        it.lastLogin = getCurrentDataTime()

                        return@validate UserIdPrincipal(credentials.name)
                    } else {
                        return@validate null
                    }
                }

                Data.users.add(User(credentials.name, password = credentials.password, lastLogin = getCurrentDataTime()))
                return@validate UserIdPrincipal(credentials.name)
            }
        }
    }

    routing {
        webSocket("/echo") {
            launch {
                sharedFlow.collect { message ->
                    send(message)
                }
            }

            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                frame.readText()
            }
        }

        authenticate("auth-basic") {
            auth()

            users()
            user()

            groups()
            group()

            privateChats()
            privateChat()

            message()
        }
    }
}

suspend fun sync(userName: String) {
    messageResponseFlow.emit(userName)
}

fun Route.auth() {
    get("/") {
        call.respond(Data.users.find { it.name == userName() }!!)
    }
}

fun Route.users() {
    get("/users") {
        call.respond(Data.users)
    }
}

fun Route.user() {
    get("/user/{name}") {
        call.respond(Data.users.find { it.name == call.parameters["name"].toString() }!!)
    }
}

fun Route.groups() {
    get("/groups") {
        call.respond(userGroups())
    }
}

fun Route.group() {
    get("/group/{name}") {
        val group = Data.groups.filter { it.isMember(userName()) }.find { it.getNameChat() == call.parameters["name"].toString() }!!

        if (group.isMember(userName())) {
            call.respond(group)
        } else {
            call.respondText("Group not found", status = HttpStatusCode.NotFound)
        }
    }
}

fun Route.privateChats() {
    get("/private-chats") {
        call.respond(userPrivateChats())
    }
}

fun Route.privateChat() {
    get("/private-chat/{name}") {
        val chat = userPrivateChats().find { it.getNameChat() == call.parameters["name"].toString() }!!

        if (chat.isMember(userName())) {
            call.respond(chat)
        } else {
            call.respondText("Chat not found", status = HttpStatusCode.NotFound)
        }
    }

    post("/private-chat") {
        val privateChat = call.receive<PrivateChat>()

        if (userPrivateChats().any { it.getNameChat() == privateChat.getNameChat() }) {
            call.respondText("A chat with this name has already been created", status = HttpStatusCode.Conflict)
        } else {
            Data.privateChats.add(privateChat)
            call.respondText("Created chat", status = HttpStatusCode.Created)

            sync(privateChat.user.name)
        }
    }
}

fun Route.message() {
    post("/chat") {
        val chat = chats.find { it.getNameChat() == call.parameters["chatName"].toString() }!!

        if (chat.isMember(userName())) {
            chat.getMessagesChat().add(call.receive<Message>().apply { messageStatus = MessageStatus.UnRead })
            call.respondText("Sent message", status = HttpStatusCode.Created)

            chat.getMembers().forEach {
                sync(it)
            }
        } else {
            call.respondText("Chat not found", status = HttpStatusCode.NotFound)
        }
    }

    post("/chat/read") {
        val chat = chats.find { it.getNameChat() == call.parameters["chatName"].toString() }!!

        if (chat.isMember(userName())) {
            chat.getMessagesChat().forEach {
                if (it.owner.name != userName()) {
                    it.messageStatus = MessageStatus.Read
                }
            }
            call.respondText("Messages read", status = HttpStatusCode.Accepted)

            chat.getMembers().forEach {
                sync(it)
            }
        }
        else {
            call.respondText("Chat not found", status = HttpStatusCode.NotFound)
        }
    }
}