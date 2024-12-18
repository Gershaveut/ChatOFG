package com.gershaveut.chat_ofg

import com.gershaveut.chat_ofg.data.User
import com.gershaveut.chat_ofg.data.UserInfo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.auth() {
	get("/") {
		call.respond(userInfo())
	}
}

fun Route.users() {
	get("/users") {
		call.respond(usersInfo)
	}
}

fun Route.user() {
	val path = "/user"
	
	get("$path/{name}") {
		call.respond(usersInfo.find { it.name == call.parameters["name"].toString() }!!)
	}
	
	post("$path/update") {
		val userInfo = call.receive<UserInfo>()
		
		if (userInfo.name == userName()) {
			users[users.indexOf(user())].setFromUserInfo(userInfo)
			
			call.respondText("User updated", status = HttpStatusCode.Accepted)
		} else {
			call.respondText("Wrong user name", status = HttpStatusCode.Conflict)
		}
	}
	
	post("$path/password") {
		user().password = call.receive<String>()
		
		call.respondText("Password updated", status = HttpStatusCode.Accepted)
	}
}