package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.Group
import com.gershaveut.chat_ofg.data.Message
import com.gershaveut.chat_ofg.data.PrivateChat
import com.gershaveut.chat_ofg.data.User
import kotlinx.datetime.LocalDateTime

object Data {
    val users = mutableSetOf<User>()
    val groups = mutableSetOf<Group>()
    val privateChats = mutableSetOf<PrivateChat>()
}