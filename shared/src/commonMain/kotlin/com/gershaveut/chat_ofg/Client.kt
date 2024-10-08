package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.Message
import com.gershaveut.chat_ofg.data.MessageStatus
import com.gershaveut.chat_ofg.data.UserInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText

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
	
	suspend fun updateUser() {
		client.post("$domain/user/update") {
			contentType(ContentType.Application.Json)
			setBody(user!!)
		}
	}
	
	suspend fun updatePassword(password: String) {
		client.post("$domain/user/password") {
			contentType(ContentType.Application.Json)
			setBody(password)
		}
	}
	
	suspend fun createChat(chat: Chat, onCreated: ((Chat) -> Unit)? = null) {
		if (client.post("$domain/chat") {
				contentType(ContentType.Application.Json)
				setBody(chat)
			}.status == HttpStatusCode.Created) {
			onCreated?.invoke(chat)
		}
	}
	
	suspend fun updateChat(chat: Chat) {
		client.post("$domain/chat/update") {
			contentType(ContentType.Application.Json)
			setBody(chat)
			parameter("chatId", chat.id)
		}
	}
	
	suspend fun deleteChat(chat: Chat, onDeleted: ((Chat) -> Unit)? = null) {
		if (client.post("$domain/chat/delete") {
				parameter("chatId", chat.id)
			}.status == HttpStatusCode.Accepted) {
			onDeleted?.invoke(chat)
		}
	}
	
	suspend fun leaveChat(chat: Chat, onLeaved: ((Chat) -> Unit)? = null) {
		if (client.post("$domain/chat/leave") {
				parameter("chatId", chat.id)
			}.status == HttpStatusCode.Accepted) {
			onLeaved?.invoke(chat)
		}
	}
	
	suspend fun kickChat(userName: String, chat: Chat) {
		client.post("$domain/chat/kick") {
			contentType(ContentType.Application.Json)
			setBody(userName)
			parameter("chatId", chat.id)
		}
	}
	
	suspend fun adminChat(userName: String, chat: Chat) {
		client.post("$domain/chat/admin") {
			contentType(ContentType.Application.Json)
			setBody(userName)
			parameter("chatId", chat.id)
		}
	}
	
	suspend fun inviteChat(userName: String, chat: Chat, onCreatedGroup: (() -> Unit)? = null) {
		if (client.post("$domain/chat/invite") {
			contentType(ContentType.Application.Json)
			setBody(userName)
			parameter("chatId", chat.id)
		}.status == HttpStatusCode.Created)
			onCreatedGroup?.invoke()
	}
	
	suspend fun sendMessage(message: Message, chat: Chat, onCreated: ((Message) -> Unit)? = null) {
		chat.messages.add(message)
		
		if (client.post("$domain/chat/message") {
				contentType(ContentType.Application.Json)
				setBody(message)
				parameter("chatId", chat.id)
			}.status == HttpStatusCode.Created) {
			onCreated?.let { it(message) }
		}
	}
	
	suspend fun deleteMessage(message: Message, chat: Chat) {
		client.post("$domain/chat/message/delete") {
			contentType(ContentType.Application.Json)
			setBody(message)
			parameter("chatId", chat.id)
		}
	}
	
	suspend fun editMessage(newText: String, message: Message, chat: Chat) {
		client.post("$domain/chat/message/edit") {
			contentType(ContentType.Application.Json)
			setBody(newText)
			parameter("chatId", chat.id)
			parameter("messageId", message.id)
		}
	}
	
	suspend fun readMessages(chat: Chat) {
		chat.messages.forEach {
			if (it.creator.name != user!!.name) {
				it.messageStatus = MessageStatus.Read
			}
		}
		
		client.post("$domain/chat/read") {
			parameter("chatId", chat.id)
		}
	}
	
	suspend fun sync() {
		chats = getChats()
		users = getUsers()
		
		onSync?.invoke()
	}
}