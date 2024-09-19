package com.gershaveut.chat_ofg

import kotlinx.datetime.*

fun LocalDateTime.customToString() = this.date.customToString() + ' ' + this.time
fun LocalDate.customToString() = this.toString().replace('-', '.')

fun Long.timeToLocalDateTime(time: Long) : LocalDateTime = Instant.fromEpochSeconds(time).toLocalDateTime(TimeZone.currentSystemDefault())