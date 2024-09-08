package com.gershaveut.chat_ofg

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import com.gershaveut.chat_ofg.data.Group
import com.gershaveut.chat_ofg.data.Message
import com.gershaveut.chat_ofg.data.PrivateChat
import com.gershaveut.chat_ofg.data.User
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val clientDataTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

val clientUser = User(
	"DEV",
	lastLogin = clientDataTime
)

val users = listOf(
	User("Tester", lastLogin = LocalDateTime(2024, 7, 14, 12, 40), discription = "Testings..."),
	User("Designer", lastLogin = LocalDateTime(2024, 6, 15, 15, 30), discription = "Working!")
)

val chats = listOf(
	Group(
		arrayListOf(
			users[0],
			users[1]
		),
		"Discussion",
		LocalDateTime(2024, 6, 10, 4, 55),
		arrayListOf(
			Message(clientUser, "This is a group check", LocalDateTime(2024, 6, 15, 14, 40)),
			Message(users[1], "Check passed", LocalDateTime(2024, 6, 15, 15, 0)),
			Message(users[0], "Check passed", LocalDateTime(2024, 6, 16, 15, 1)),
		),
	),
	PrivateChat(
		users[0], LocalDateTime(2024, 6, 14, 12, 35), arrayListOf(
			Message(users[0], "Everything works!", LocalDateTime(2024, 6, 14, 12, 40)),
			Message(clientUser, "Great", LocalDateTime(2024, 7, 14, 13, 0)),
		)
	), PrivateChat(
		users[1], LocalDateTime(2024, 6, 13, 6, 0), arrayListOf(
			Message(
				clientUser,
				"Will everything be ready soon?",
				LocalDateTime(2024, 6, 13, 6, 15)
			),
			Message(users[1], "Yes!", LocalDateTime(2025, 6, 13, 19, 30)),
		)
	)
)

@Composable
fun App() {
	MaterialTheme {
		Menu()
	}
}