package com.gershaveut.chat_ofg

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
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
        chats()
    }
}

fun Routing.users() {
    get("/users") {
        call.respond(Data.users)
    }
}

fun Routing.chats() {
    get("/chats") {
        call.respond(Data.chats)
    }
}