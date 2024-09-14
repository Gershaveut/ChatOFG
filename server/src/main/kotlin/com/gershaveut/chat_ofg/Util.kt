package com.gershaveut.chat_ofg

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.util.pipeline.*
import kotlinx.datetime.*

fun PipelineContext<Unit, ApplicationCall>.userName() = call.principal<UserIdPrincipal>()!!.name
fun PipelineContext<Unit, ApplicationCall>.userGroups() = Data.groups.filter { it.isMember(userName()) }
fun PipelineContext<Unit, ApplicationCall>.userPrivateChats() = Data.privateChats.filter { it.isMember(userName()) }

fun getCurrentDataTime(): LocalDateTime {
    val current = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    val currentTime = current.time

    return LocalDateTime(current.date, LocalTime(currentTime.hour, currentTime.minute))
}

