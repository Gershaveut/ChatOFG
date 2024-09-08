package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.Group
import com.gershaveut.chat_ofg.data.Message
import com.gershaveut.chat_ofg.data.PrivateChat
import com.gershaveut.chat_ofg.data.User
import kotlinx.datetime.LocalDateTime

object Data {
    val users = mutableListOf(
        User("Tester", lastLogin = LocalDateTime(2024, 7, 14, 12, 40), discription = "Testings..."),
        User("Designer", lastLogin = LocalDateTime(2024, 6, 15, 15, 30), discription = "Working!")
    )

    val chats = mutableListOf(
        Group(
            mutableListOf(
                users[0],
                users[1]
            ),
            "Discussion",
            LocalDateTime(2024, 6, 10, 4, 55),
            mutableListOf(
                Message(users[1], "Check passed", LocalDateTime(2024, 6, 15, 15, 0)),
                Message(users[0], "Check passed", LocalDateTime(2024, 6, 16, 15, 1)),
            ),
        ),
        PrivateChat(
            users[0], LocalDateTime(2024, 6, 14, 12, 35), mutableListOf(
                Message(users[0], "Everything works!", LocalDateTime(2024, 6, 14, 12, 40)),
            )
        ),
        PrivateChat(
            users[1], LocalDateTime(2024, 6, 13, 6, 0), mutableListOf(
                Message(users[1], "Yes!", LocalDateTime(2025, 6, 13, 19, 30)),
            )
        )
    )
}