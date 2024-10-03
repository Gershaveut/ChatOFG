package com.gershaveut.chat_ofg.data

import com.benasher44.uuid.uuid4
import com.gershaveut.chat_ofg.Client
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val id: String = uuid4().toString(),
    var members: MutableMap<UserInfo, Boolean>,
    private var name: String? = null,
    var messages: MutableList<Message> = mutableListOf(),
    val createTime: Long = Clock.System.now().epochSeconds,
    var description: String? = null,
) {
    init {
        if (members.count() < 3) {
            members = members.keys.associateWith { true }.toMutableMap()
        }
    }

    constructor(creator: UserInfo, user: UserInfo) : this(members = mutableMapOf(creator to false, user to false))

    fun getName(user: User? = null): String {
        return name ?: if (user != null && members.count() < 3) {
            if (user.name == members.keys.first().name)
                members.keys.last().displayName
            else
                members.keys.first().displayName
        } else {
            members.entries.joinToString { it.key.displayName }
        }
    }

    fun setName(name: String?) {
        this.name = name
    }
}