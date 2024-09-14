package com.gershaveut.chat_ofg

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.util.pipeline.*

fun PipelineContext<Unit, ApplicationCall>.userName() = call.principal<UserIdPrincipal>()!!.name