package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.Message
import com.gershaveut.chat_ofg.data.PrivateChat
import com.gershaveut.chat_ofg.data.User
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
import io.ktor.util.pipeline.*

val chats
    get() = mutableSetOf<Chat>().apply {
        addAll(Data.privateChats)
        addAll(Data.groups)
    }

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
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
        }
    }
}

fun Route.message() {
    post("/chat") {
        val chat = chats.find { it.getNameChat() == call.parameters["chatName"].toString() }!!

        if (chat.isMember(userName())) {
            chat.getMessagesChat().add(call.receive<Message>())
            call.respondText("Sent message", status = HttpStatusCode.Created)
        } else {
            call.respondText("Chat not found", status = HttpStatusCode.NotFound)
        }
    }
}