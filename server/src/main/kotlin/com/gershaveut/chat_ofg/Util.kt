package com.gershaveut.chat_ofg

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.util.pipeline.*
import kotlinx.datetime.*

fun PipelineContext<Unit, ApplicationCall>.userName() = call.principal<UserIdPrincipal>()!!.name
fun PipelineContext<Unit, ApplicationCall>.user() = users.find { it.name == call.principal<UserIdPrincipal>()!!.name }!!
fun PipelineContext<Unit, ApplicationCall>.findChat() = user().chats.find { it.name == call.parameters["chatName"].toString() }!!
