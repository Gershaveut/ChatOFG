package com.gershaveut.chat_ofg.data

import com.benasher44.uuid.uuid4
import com.gershaveut.chat_ofg.Client
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val id: String = uuid4().toString(),
    var members: Map<UserInfo, Boolean>,
    private var name: String? = null,
    var messages: MutableList<Message> = mutableListOf(),
    val createTime: Long = Clock.System.now().epochSeconds,
    var description: String? = null,
) {
    constructor(creator: User, user: UserInfo) : this(
        uuid4().toString(),
        mapOf(creator.toUserInfo() to true, user to true)
    )

    fun getName(user: User): String {
        return name ?: if (user.name == members.keys.first().name)
            members.keys.last().displayName
        else
            members.keys.first().displayName
    }

    fun getName(): String {
        return name ?: if (Client.user != null) getName(Client.user!!) else members.keys.first().displayName
    }
}