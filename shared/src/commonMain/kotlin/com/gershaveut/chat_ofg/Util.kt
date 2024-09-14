package com.gershaveut.chat_ofg

import kotlinx.datetime.*

fun LocalDateTime.customToString() = this.date.customToString() + ' ' + this.time
fun LocalDate.customToString() = this.toString().replace('-', '.')

fun getCurrentDataTime(): LocalDateTime {
    val current = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    val currentTime = current.time

    return LocalDateTime(current.date, LocalTime(currentTime.hour, currentTime.minute))
}