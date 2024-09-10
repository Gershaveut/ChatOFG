package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.PrivateChat
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        users()
        user()

        groups()
        group()

        privateChats()
        privateChat()
    }
}

fun Routing.users() {
    get("/users") {
        call.respond(Data.users)
    }
}

fun Routing.user() {
    get("/user/{name}") {
        call.respond(Data.users.find { it.name == call.parameters["name"].toString() }!!)
    }
}

fun Routing.groups() {
    get("/groups") {
        call.respond(Data.groups)
    }
}

fun Routing.group() {
    get("/group/{name}") {
        call.respond(Data.groups.find { it.name == call.parameters["name"].toString() }!!)
    }
}

fun Routing.privateChats() {
    get("/private-chats") {
        call.respond(Data.privateChats)
    }
}

fun Routing.privateChat() {
    get("/private-chat/{name}") {
        call.respond(Data.privateChats.find { it.user.name == call.parameters["name"].toString() }!!)
    }

    post("/private-chat") {
        Data.privateChats.add(call.receive<PrivateChat>())
        call.respondText("Created chat", status = HttpStatusCode.Created)
    }
}