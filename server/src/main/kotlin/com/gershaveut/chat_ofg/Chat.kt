package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.chats() {
	get("/chats") {
		call.respond(chats.filter { chat -> user().chats.any { it == chat.id } })
	}
}

fun Route.chat() {
	val path = "/chat"
	
	get("$path/{id}") {
		val chat = findChat()
		
		call.respond(chat)
	}
	
	post(path) {
		val chat = call.receive<Chat>()
		
		if (chat.members.size > 1) {
			val newChat = Chat(members = chat.members, messages = mutableListOf(Message(userInfo(), "Chat Created", messageType = MessageType.System)))
			
			chats.add(newChat)
			
			chat.forEachMembers { user ->
				users.find { it.name == user.name }?.chats?.add(newChat.id)
			}
			
			chat.syncChat()
			
			call.respondText("Created chat", status = HttpStatusCode.Created)
		} else {
			call.respondText("There are less than two participants in the chat", status = HttpStatusCode.NotAcceptable)
		}
	}
	
	post("$path/message") {
		val chat = findChat()
		
		chatAccess {
			chat.messages.add(call.receive<Message>().apply {
				messageStatus = MessageStatus.UnRead
				text = text.removeMax(300)
			})
			call.respondText("Sent message", status = HttpStatusCode.Created)
			
			chat.syncChat()
		}
	}
	
	post("$path/message/delete") {
		val chat = findChat()
		val message = call.receive<Message>()
		val user = userInfo()
		
		chatAccess {
			if (chat.userAccess(user) || user.name == message.creator.name) {
				chat.messages.remove(message)
				
				chat.syncChat { it.name != userName() }
				
				call.respondText("Message deleted", status = HttpStatusCode.Accepted)
			} else {
				accessDenied()
			}
		}
	}
	
	post("$path/message/edit") {
		val chat = findChat()
		val id = call.parameters["messageId"].toString()
		val user = userInfo()
		
		chatAccess {
			chat.messages.find { it.id == id }!!.apply {
				if (chat.userAccess(user) || user.name == creator.name) {
					text = call.receive()
					modified = true
					
					chat.syncChat()
					
					call.respondText("Message edited", status = HttpStatusCode.Accepted)
				} else {
					accessDenied()
				}
			}
		}
	}
	
	post("$path/read") {
		val chat = findChat()
		
		chatAccess {
			chat.messages.forEach {
				if (it.creator.name != userName()) {
					it.messageStatus = MessageStatus.Read
				}
			}
			call.respondText("Messages read", status = HttpStatusCode.Accepted)
			
			chat.syncChat { it.name != userName() }
		}
	}
	
	post("$path/delete") {
		val chat = findChat()
		
		chatAccessAdmin {
			chat.forEachMembers { user ->
				users.find { it.name == user.name }?.chats?.remove(chat.id)
			}
			
			chat.syncChat()
			
			call.respondText("Chat deleted", status = HttpStatusCode.Accepted)
		}
	}
	
	post("$path/leave") {
		val chat = findChat()
		val leaveUser = user()
		
		leaveUser.chats.remove(chat.id)
		
		sync(leaveUser.name)
		
		chat.members.remove(chat.members.keys.find { it.name == leaveUser.name }!!)
		chat.messages.add(Message(leaveUser.toUserInfo(), "${leaveUser.name} leaved", messageType = MessageType.System))
		
		chat.syncChat()
		
		call.respondText("User leaved", status = HttpStatusCode.Accepted)
	}
	
	post("$path/update") {
		val updateChat = call.receive<Chat>()
		val chat = findChat()
		
		if (chat.chatType == ChatType.Group) {
			chatAccessAdmin {
				val newName = updateChat.getName().removeMax()
				val newDescription = updateChat.description?.removeMax()
				
				if (chat.getName() != newName) {
					chat.setName(newName)
					chat.messages.add(
						Message(
							userInfo(),
							"Update name to $newName by ${userName()}",
							messageType = MessageType.System
						)
					)
				}
				
				newDescription?.let {
					if (chat.description != newDescription) {
						chat.description = newDescription
						chat.messages.add(
							Message(
								userInfo(),
								"Update description to $newDescription by ${userName()}",
								messageType = MessageType.System
							)
						)
					}
				}
				
				chat.syncChat()
				
				call.respondText("Chat updated", status = HttpStatusCode.Accepted)
			}
		} else {
			accessDenied()
		}
	}
	
	post("$path/invite") {
		val chat = findChat()
		val name = call.receive<String>()
		val invitedUser = users.find { it.name == name }!!
		
		chatAccess {
			if (!invitedUser.chats.any { it == chat.id }) {
				if (chat.chatType == ChatType.Group) {
					invitedUser.chats.add(chat.id)
					
					sync(name)
					
					chat.members[invitedUser.toUserInfo()] = false
					chat.messages.add(Message(userInfo(), "${userName()} Invited", messageType = MessageType.System))
					
					chat.syncChat()
					
					call.respondText("User $name invited", status = HttpStatusCode.Accepted)
				} else {
					val newChat = Chat(members = chat.members.apply { put(invitedUser.toUserInfo(), false) })
					
					invitedUser.chats.add(newChat.id)
					
					chat.forEachMembers { user ->
						users.find { it.name == user.name }?.chats?.add(chat.id)
					}
					
					chat.syncChat()
					
					call.respondText("Group created", status = HttpStatusCode.Created)
				}
			} else {
				call.respondText("User $name already exists", status = HttpStatusCode.NotAcceptable)
			}
		}
	}
	
	post("$path/kick") {
		val chat = findChat()
		val name = call.receive<String>()
		
		chatAccessAdmin {
			users.find { it.name == name }!!.chats.remove(chat.id)
			
			sync(name)
			
			chat.members.remove(chat.members.keys.find { it.name == name }!!)
			chat.messages.add(Message(userInfo(), "$name kicked", messageType = MessageType.System))
			
			chat.syncChat()
			
			call.respondText("User $name kicked", status = HttpStatusCode.Accepted)
		}
	}
	
	post("$path/admin") {
		val chat = findChat()
		val name = call.receive<String>()
		
		chatAccessAdmin {
			chat.members[chat.members.keys.find { it.name == name }!!] = true
			
			chat.messages.add(Message(userInfo(), "${userName()} give admin $name", messageType = MessageType.System))
			
			chat.syncChat()
			
			call.respondText("Given admin $name", status = HttpStatusCode.Accepted)
		}
	}
}

fun Chat.forEachMembers(action: (UserInfo) -> Unit) {
	this.members.keys.forEach {
		action(it)
	}
}

suspend fun Chat.syncChat(predicate: (UserInfo) -> Boolean = { true }) {
	this.members.keys.forEach {
		if (predicate(it))
			sync(it.name)
	}
}