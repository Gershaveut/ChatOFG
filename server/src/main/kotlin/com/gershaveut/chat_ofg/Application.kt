package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.Chat
import com.gershaveut.chat_ofg.data.User
import com.gershaveut.chat_ofg.data.UserInfo
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.logging.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.concurrent.timer

var users = mutableListOf<User>()
var chats = mutableListOf<Chat>()

val usersInfo: List<UserInfo>
	get() = users.map {
		UserInfo(
			it.name,
			it.displayName,
			it.description,
			it.createTime,
			it.lastLoginTime
		)
	}

val messageResponseFlow = MutableSharedFlow<String>()
val sharedFlow = messageResponseFlow.asSharedFlow()

val LOGGER = KtorSimpleLogger("com.gershaveut.Data")

const val FILE_USERS_NAME = "Users.txt"
const val FILE_CHATS_NAME = "Chats.txt"

suspend fun main() = coroutineScope {
	File(FILE_USERS_NAME).createNewFile()
	File(FILE_CHATS_NAME).createNewFile()
	
	loadData()
	
	val timer = timer(initialDelay = 100000L, period = 100000L) {
		saveData()
	}
	
	embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
		.start(wait = true)
	
	timer.cancel()
	
	saveData()
}

fun Application.module() {
	install(WebSockets)
	install(ContentNegotiation) {
		json()
	}
	install(Authentication) {
		basic("auth-basic") {
			realm = "User Access"
			validate { credentials ->
				users.find { it.name == credentials.name }?.let {
					if (it.password == credentials.password) {
						it.lastLoginTime = Clock.System.now().epochSeconds
						
						return@validate UserIdPrincipal(credentials.name)
					} else {
						return@validate null
					}
				}
				
				users.add(
					User(
						name = credentials.name,
						password = credentials.password
					)
				)
				return@validate UserIdPrincipal(credentials.name)
			}
		}
	}
	
	routing {
		authenticate("auth-basic") {
			webSocket("/echo") {
				launch {
					sharedFlow.collect { message ->
						if (call.principal<UserIdPrincipal>()!!.name == message) {
							send(message)
						}
					}
				}
				
				while (true) {
					for (frame in incoming) {
						frame as? Frame.Text ?: continue
						frame.readText()
					}
				}
			}
			
			auth()
			
			users()
			user()
			
			chats()
			chat()
		}
	}
}

private val json = Json { allowStructuredMapKeys = true }

fun saveData() {
	try {
		LOGGER.info("Save data")
		
		File(FILE_USERS_NAME).writeText(json.encodeToString(users))
		File(FILE_CHATS_NAME).writeText(json.encodeToString(chats))
	} catch (e: Exception) {
		LOGGER.error("Save data error")
		LOGGER.debug(e.toString())
	}
}

fun loadData() {
	try {
		LOGGER.info("Load data")
		
		users = json.decodeFromString(File(FILE_USERS_NAME).readText())
		chats = json.decodeFromString(File(FILE_CHATS_NAME).readText())
	} catch (e: Exception) {
		LOGGER.error("Load data error")
		LOGGER.debug(e.toString())
	}
}

fun PipelineContext<Unit, ApplicationCall>.userName() = call.principal<UserIdPrincipal>()!!.name
fun PipelineContext<Unit, ApplicationCall>.user() = users.find { it.name == call.principal<UserIdPrincipal>()!!.name }!!
fun PipelineContext<Unit, ApplicationCall>.userInfo() = user().toUserInfo()

fun PipelineContext<Unit, ApplicationCall>.findChat() =
	chats.find { chats -> chats.id == user().chats.find { it == call.parameters["chatId"].toString() }!! }!!

suspend fun PipelineContext<Unit, ApplicationCall>.chatAccessAdmin(onAccept: suspend () -> Unit) {
	if (findChat().members.mapKeys { it.key.name }[userName()]!!)
		onAccept()
	else
		accessDenied()
}

suspend fun PipelineContext<Unit, ApplicationCall>.chatAccess(onAccept: suspend () -> Unit) {
	if (user().chats.any { it == call.parameters["chatId"].toString() })
		onAccept()
	else
		accessDenied()
}

suspend fun PipelineContext<Unit, ApplicationCall>.accessDenied() {
	call.respondText("Access denied", status = HttpStatusCode.NotAcceptable)
}