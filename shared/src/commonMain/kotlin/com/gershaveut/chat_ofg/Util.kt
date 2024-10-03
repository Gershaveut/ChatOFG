package com.gershaveut.chat_ofg

import kotlinx.datetime.*

fun LocalDateTime.customToString() = this.date.customToString() + ' ' + this.time
fun LocalDate.customToString() = this.toString().replace('-', '.')

fun Long.timeToLocalDateTime(): LocalDateTime =
    Instant.fromEpochSeconds(this).toLocalDateTime(TimeZone.currentSystemDefault())

fun getCurrentDataTime(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun String.removeMax(maxLength: Int) : String {
    val length = this.length

    if (length > maxLength)
        return this.removeRange(maxLength, length)

    return this
}

fun String.removeMax() : String = removeMax(20)