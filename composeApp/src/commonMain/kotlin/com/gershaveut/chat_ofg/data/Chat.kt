package com.gershaveut.chat_ofg.data

data class Chat(val owner: User, var messages: List<Message>) {
}