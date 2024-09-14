package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.Group
import com.gershaveut.chat_ofg.data.Message
import com.gershaveut.chat_ofg.data.PrivateChat
import com.gershaveut.chat_ofg.data.User
import kotlinx.datetime.LocalDateTime

object Data {
    val users = mutableSetOf(
        User("DEV", "Developer", "ADMIN", LocalDateTime(2024, 7, 14, 12, 40), "DEV1"),
        User("Tester", lastLogin = LocalDateTime(2024, 7, 14, 12, 40), discription = "Testings...", password = "123456"),
        User("Designer", lastLogin = LocalDateTime(2024, 6, 15, 15, 30), discription = "Working!", password = "1234567"),
        User("User 1", lastLogin = LocalDateTime(2020, 3, 12, 15, 30), password = "12345678")
    )
    
    val groups = mutableSetOf(
        Group(
            mutableSetOf(
                users.find { it.name == "Tester" }!!,
                users.find { it.name == "Designer" }!!,
                users.find { it.name == "DEV" }!!
            ),
            "Discussion",
            LocalDateTime(2024, 6, 10, 4, 55),
            mutableListOf(
                Message(users.find { it.name == "Designer" }!!, "Check passed", LocalDateTime(2024, 6, 15, 15, 0)),
                Message(users.find { it.name == "Tester" }!!, "Check passed", LocalDateTime(2024, 6, 16, 15, 1)),
            ),
        ),
    )

    val privateChats = mutableSetOf(
        PrivateChat(
            users.find { it.name == "DEV" }!!, users.find { it.name == "Tester" }!!, LocalDateTime(2024, 6, 14, 12, 35), mutableListOf(
                Message(users.find { it.name == "Tester" }!!, "Everything works!", LocalDateTime(2024, 6, 14, 12, 40)),
            )
        ),
        PrivateChat(
            users.find { it.name == "DEV" }!!, users.find { it.name == "Designer" }!!, LocalDateTime(2024, 6, 13, 6, 0), mutableListOf(
                Message(users.find { it.name == "Designer" }!!, "Yes!", LocalDateTime(2025, 6, 13, 19, 30)),
            )
        )
    )
}