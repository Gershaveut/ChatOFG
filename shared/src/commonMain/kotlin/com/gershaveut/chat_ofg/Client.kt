package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.Message
import com.gershaveut.chat_ofg.data.MessageStatus
import com.gershaveut.chat_ofg.data.UserInfo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*

object Client {
	var host = HOST_DEFAULT
	
	var user: UserInfo? = null
	
	var users = mutableListOf<UserInfo>()
	var chats = mutableListOf<Chat>()
	
	var onSync: (() -> Unit)? = null
	
	private var authName: String? = null
	private var authPassword: String? = null
	
	private val domain get() = "http://$host:$SERVER_PORT"
	
	private val client = HttpClient(CIO) {
		install(WebSockets)
		install(ContentNegotiation) {
			json()
		}
		
		install(Auth) {
			basic {
				credentials {
					BasicAuthCredentials(username = authName!!, password = authPassword!!)
				}
				realm = "User Access"
			}
		}
	}
	
	suspend fun handleConnection(onConnection: (Boolean) -> Unit) {
		try {
			client.webSocket(
				method = HttpMethod.Get,
				host = host,
				port = SERVER_PORT,
				path = "/echo"
			) {
				onConnection(true)
				
				while (user != null) {
					val userName = incoming.receive() as? Frame.Text ?: continue
					
					if (user == null)
						continue
					
					if (userName.readText() == user!!.name)
						sync()
				}
			}
		} catch (_: Exception) {
			onConnection(false)
			handleConnection(onConnection)
		}
	}
	
	suspend fun auth(name: String, password: String) {
		authName = name
		authPassword = password
		
		user = client.get("$domain/").body()
	}
	
	suspend fun getUsers(): MutableList<UserInfo> = client.get("$domain/users").body()
	suspend fun getChats(): MutableList<Chat> = client.get("$domain/chats").body()
	suspend fun getUser(name: String): UserInfo = client.get("$domain/user/$name").body()
	
	suspend fun updateUser(onAccepted: (() -> Unit)? = null, onError: ((reason: String) -> Unit)? = null) {
		client.post("$domain/user/update") {
			contentType(ContentType.Application.Json)
			setBody(user!!)
		}.let {
			if (it.status == HttpStatusCode.Accepted)
				onAccepted?.invoke()
			else
				onError?.invoke(it.body())
		}
	}
	
	suspend fun updatePassword(
		password: String,
		onAccepted: (() -> Unit)? = null,
		onError: ((reason: String) -> Unit)? = null
	) {
		client.post("$domain/user/password") {
			contentType(ContentType.Application.Json)
			setBody(password)
		}.let {
			if (it.status == HttpStatusCode.Accepted)
				onAccepted?.invoke()
			else
				onError?.invoke(it.body())
		}
	}
	
	suspend fun createChat(
		chat: Chat,
		onCreated: ((Chat) -> Unit)? = null,
		onError: ((reason: String) -> Unit)? = null
	) {
		client.post("$domain/chat") {
			contentType(ContentType.Application.Json)
			setBody(chat)
		}.let {
			if (it.status == HttpStatusCode.Created)
				onCreated?.invoke(chat)
			else
				onError?.invoke(it.body())
		}
	}
	
	suspend fun updateChat(chat: Chat, onAccepted: (() -> Unit)? = null, onError: ((reason: String) -> Unit)? = null) {
		client.post("$domain/chat/update") {
			contentType(ContentType.Application.Json)
			setBody(chat)
			parameter("chatId", chat.id)
		}.let {
			if (it.status == HttpStatusCode.Accepted)
				onAccepted?.invoke()
			else
				onError?.invoke(it.body())
		}
	}
	
	suspend fun deleteChat(
		chat: Chat,
		onDeleted: ((Chat) -> Unit)? = null,
		onError: ((reason: String) -> Unit)? = null
	) {
		client.post("$domain/chat/delete") {
			parameter("chatId", chat.id)
		}.let {
			if (it.status == HttpStatusCode.Accepted)
				onDeleted?.invoke(chat)
			else
				onError?.invoke(it.body())
		}
	}
	
	suspend fun leaveChat(chat: Chat, onLeaved: ((Chat) -> Unit)? = null, onError: ((reason: String) -> Unit)? = null) {
		client.post("$domain/chat/leave") {
			parameter("chatId", chat.id)
		}.let {
			if (it.status == HttpStatusCode.Accepted)
				onLeaved?.invoke(chat)
			else
				onError?.invoke(it.body())
		}
	}
	
	suspend fun kickChat(
		userName: String,
		chat: Chat,
		onAccepted: (() -> Unit)? = null,
		onError: ((reason: String) -> Unit)? = null
	) {
		client.post("$domain/chat/kick") {
			contentType(ContentType.Application.Json)
			setBody(userName)
			parameter("chatId", chat.id)
		}.let {
			if (it.status == HttpStatusCode.Accepted)
				onAccepted?.invoke()
			else
				onError?.invoke(it.body())
		}
	}
	
	suspend fun adminChat(
		userName: String,
		chat: Chat,
		onAccepted: (() -> Unit)? = null,
		onError: ((reason: String) -> Unit)? = null
	) {
		client.post("$domain/chat/admin") {
			contentType(ContentType.Application.Json)
			setBody(userName)
			parameter("chatId", chat.id)
		}.let {
			if (it.status == HttpStatusCode.Accepted) {
				onAccepted?.invoke()
			} else {
				onError?.invoke(it.body())
			}
		}
	}
	
	suspend fun inviteChat(
		userName: String,
		chat: Chat,
		onCreatedGroup: (() -> Unit)? = null,
		onError: ((reason: String) -> Unit)? = null
	) {
		client.post("$domain/chat/invite") {
			contentType(ContentType.Application.Json)
			setBody(userName)
			parameter("chatId", chat.id)
		}.let {
			if (it.status == HttpStatusCode.Created || it.status == HttpStatusCode.Accepted) {
				onCreatedGroup?.invoke()
			} else {
				onError?.invoke(it.body())
			}
		}
	}
	
	suspend fun sendMessage(
		message: Message,
		chat: Chat,
		onCreated: ((Message) -> Unit)? = null,
		onError: ((reason: String) -> Unit)? = null
	) {
		client.post("$domain/chat/message") {
			contentType(ContentType.Application.Json)
			setBody(message)
			parameter("chatId", chat.id)
		}.let {
			if (it.status == HttpStatusCode.Created) {
				onCreated?.invoke(message)
			} else {
				onError?.invoke(it.body())
			}
		}
	}
	
	suspend fun deleteMessage(message: Message, chat: Chat, onAccepted: (() -> Unit)? = null, onError: ((reason: String) -> Unit)? = null) {
		client.post("$domain/chat/message/delete") {
			contentType(ContentType.Application.Json)
			setBody(message)
			parameter("chatId", chat.id)
		}.let {
			if (it.status == HttpStatusCode.Accepted)
				onAccepted?.invoke()
			else
				onError?.invoke(it.body())
		}
	}
	
	suspend fun editMessage(newText: String, message: Message, chat: Chat, onAccepted: (() -> Unit)? = null, onError: ((reason: String) -> Unit)? = null) {
		client.post("$domain/chat/message/edit") {
			contentType(ContentType.Application.Json)
			setBody(newText)
			parameter("chatId", chat.id)
			parameter("messageId", message.id)
		}.let {
			if (it.status == HttpStatusCode.Accepted)
				onAccepted?.invoke()
			else
				onError?.invoke(it.body())
		}
	}
	
	suspend fun readMessages(chat: Chat, onAccepted: (() -> Unit)? = null, onError: ((reason: String) -> Unit)? = null) {
		chat.messages.forEach {
			if (it.creator.name != user!!.name) {
				it.messageStatus = MessageStatus.Read
			}
		}
		
		client.post("$domain/chat/read") {
			parameter("chatId", chat.id)
		}.let {
			if (it.status == HttpStatusCode.Accepted)
				onAccepted?.invoke()
			else
				onError?.invoke(it.body())
		}
	}
	
	private suspend fun sync() {
		chats = getChats()
		
		onSync?.invoke()
	}
}