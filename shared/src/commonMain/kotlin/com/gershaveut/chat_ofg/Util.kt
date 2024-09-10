package com.gershaveut.chat_ofg

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

fun cdtToString(dataTime: LocalDateTime): String = (cdToString(dataTime.date) + ' ' + dataTime.time)
fun cdToString(dataTime: LocalDate): String = dataTime.toString().replace('-', '.')