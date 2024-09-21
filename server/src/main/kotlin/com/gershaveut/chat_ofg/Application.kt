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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

val users = mutableListOf<User>()

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
                users.find { it.name == credentials.name }?.let {
                    if (it.password == credentials.password) {
                        it.lastLoginTime = Clock.System.now().epochSeconds

                        return@validate UserIdPrincipal(credentials.name)
                    } else {
                        return@validate null
                    }
                }

                users.add(
                    User(
                        credentials.name,
                        credentials.password
                    )
                )
                return@validate UserIdPrincipal(credentials.name)
            }
        }
    }

    routing {
        authenticate("auth-basic") {
            webSocket("/echo") {
                launch {
                    sharedFlow.collect { message ->
                        if (call.principal<UserIdPrincipal>()!!.name == message)
                            send(message)
                    }
                }

                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    frame.readText()
                }
            }

            auth()

            users()
            user()

            chats()
            chat()
        }
    }
}

suspend fun sync(user: User) {
    messageResponseFlow.emit(user.name)
}

suspend fun sync(userName: String) {
    messageResponseFlow.emit(userName)
}

fun Route.auth() {
    get("/") {
        call.respond(user())
    }
}

fun Route.users() {
    get("/users") {
        call.respond(users)
    }
}

fun Route.user() {
    get("/user/{name}") {
        call.respond(users.find { it.name == call.parameters["name"].toString() }!!)
    }
}

fun Route.chats() {
    get("/chats") {
        call.respond(user().chats)
    }
}

fun Route.chat() {
    get("/chat/{chatName}") {
        val chat = findChat()

        call.respond(chat)
    }

    post("/chat") {
        val chat = call.receive<Chat>()

        if (user().chats.any { it.getName() == chat.getName() }) {
            call.respondText("A chat with this name has already been created", status = HttpStatusCode.Conflict)
        } else {
            chat.members.forEach { name ->
                users.find { it.name == name.key }?.let {
                    it.chats.add(chat)
                    sync(it)
                }
            }

            call.respondText("Created chat", status = HttpStatusCode.Created)
        }
    }

    post("/chat/message") {
        val chat = findChat()

        chat.messages.add(call.receive<Message>().apply { messageStatus = MessageStatus.UnRead })
        call.respondText("Sent message", status = HttpStatusCode.Created)

        chat.members.forEach {
            sync(it.key)
        }
    }

    post("/chat/read") {
        val chat = findChat()

        chat.messages.forEach {
            if (it.creator.name != userName()) {
                it.messageStatus = MessageStatus.Read
            }
        }
        call.respondText("Messages read", status = HttpStatusCode.Accepted)

        chat.members.forEach {
            if (it.key != userName())
                sync(it.key)
        }
    }
}