package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
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
val usersInfo: List<UserInfo>
    get() = users.map {
        UserInfo(
            it.name,
            it.displayName,
            it.description,
            it.createTime,
            it.lastLoginTime
        )
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
                        name = credentials.name,
                        password = credentials.password
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
        call.respond(usersInfo)
    }
}

fun Route.user() {
    val path = "/user"

    get("$path/{name}") {
        call.respond(usersInfo.find { it.name == call.parameters["name"].toString() }!!)
    }

    post("$path/update") {
        val user = call.receive<User>()

        if (user.name == userName()) {
            users[users.indexOf(users.find { it.name == user().name })] = user

            call.respondText("User updated", status = HttpStatusCode.Accepted)
        } else {
            call.respondText("Wrong user name", status = HttpStatusCode.Conflict)
        }
    }
}

fun Route.chats() {
    get("/chats") {
        call.respond(user().chats)
    }
}

fun Route.chat() {
    val path = "/chat"

    get("$path/{id}") {
        val chat = findChat()

        call.respond(chat)
    }

    post(path) {
        val chat = call.receive<Chat>()

        if (chat.members.size > 1) {
            if (chat.members.size > 2)
                chat.setName(chat.getName().removeMax())
            else
                chat.setName(null)

            chat.members.keys.forEach { user ->
                users.find { it.name == user.name }?.let {
                    it.chats.add(chat)
                    sync(it.name)
                }
            }

            call.respondText("Created chat", status = HttpStatusCode.Created)
        } else {
            call.respondText("There are less than two participants in the chat", status = HttpStatusCode.NotAcceptable)
        }
    }

    post("$path/message") {
        val chat = findChat()

        chat.messages.add(call.receive<Message>().apply {
            messageStatus = MessageStatus.UnRead
            text = text.removeMax(300)
        })
        call.respondText("Sent message", status = HttpStatusCode.Created)

        chat.members.keys.forEach {
            sync(it.name)
        }
    }

    post("$path/read") {
        val chat = findChat()

        chat.messages.forEach {
            if (it.creator != userName()) {
                it.messageStatus = MessageStatus.Read
            }
        }
        call.respondText("Messages read", status = HttpStatusCode.Accepted)

        chat.members.keys.forEach {
            if (it.name != userName())
                sync(it.name)
        }
    }

    post("$path/delete") {
        val chat = findChat()

        chatAccess {
            chat.members.keys.forEach { user ->
                users.find { it.name == user.name }?.let {
                    it.chats.remove(chat)
                    sync(it.name)
                }
            }

            call.respondText("Chat deleted", status = HttpStatusCode.Accepted)
        }
    }

    post("$path/update") {
        val updateChat = call.receive<Chat>()
        val chat = findChat()

        if (chat.members.size > 2) {
            updateChat.setName(updateChat.getName().removeMax())
            updateChat.description?.let { updateChat.description = updateChat.description!!.removeMax(300) }

            chatAccess {
                chat.members.keys.forEach { userInfo ->
                    users.find { it.name == userInfo.name }!!.let { user ->
                        user.chats[user.chats.indexOf(user.chats.find { it.id == chat.id })] = updateChat
                    }
                }
            }
        } else {
            accessDenied()
        }
    }
}

fun PipelineContext<Unit, ApplicationCall>.userName() = call.principal<UserIdPrincipal>()!!.name
fun PipelineContext<Unit, ApplicationCall>.user() = users.find { it.name == call.principal<UserIdPrincipal>()!!.name }!!
fun PipelineContext<Unit, ApplicationCall>.findChat() = user().chats.find { it.id == call.parameters["id"].toString() }!!

suspend fun PipelineContext<Unit, ApplicationCall>.chatAccess(onAccept: suspend () -> Unit) {
    if (findChat().members.mapKeys { it.key.name }[userName()]!!)
        onAccept()
    else
        accessDenied()
}

suspend fun PipelineContext<Unit, ApplicationCall>.accessDenied() {
    call.respondText("Access denied", status = HttpStatusCode.NotAcceptable)
}