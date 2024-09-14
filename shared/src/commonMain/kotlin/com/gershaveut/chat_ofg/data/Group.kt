package com.gershaveut.chat_ofg.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    var users: MutableSet<User>,
    var name: String,
    var createTime: LocalDateTime,
    var messages: MutableList<Message>,
    var description: String? = null
) : Chat {
    override fun getNameChat(): String = name
    override fun getSignChat(): String = "Users: " + users.count()
    override fun getCreateTimeChat(): LocalDateTime = createTime
    override fun getMessagesChat(): MutableList<Message> = messages
    override fun getDescriptionChat(): String? = description
    override fun getMembers(): List<String> = users.map { it.name }
}