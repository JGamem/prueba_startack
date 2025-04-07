package stsa.kotlin_htmx.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.days

class UserSession(val username: String) : Principal

class AuthenticationService {
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    // En una app real, esto estaría almacenado en una base de datos con contraseñas hash
    private val users = mapOf(
        "admin" to "password123"
    )
    
    fun validateCredentials(username: String, password: String): Boolean {
        val storedPassword = users[username]
        return storedPassword != null && storedPassword == password
    }
}

fun Application.configureAuthentication() {
    val authService = AuthenticationService()
    
    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 1.days.inWholeSeconds
            cookie.extensions["SameSite"] = "lax"
            cookie.httpOnly = true
            transform(SessionTransportTransformerEncrypt(hex("00112233445566778899aabbccddeeff"), hex("a1a2a3a4a5a6a7a8")))
        }
    }
    
    install(Authentication) {
        session<UserSession>("auth-session") {
            validate { session ->
                session
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
        
        form("auth-form") {
            userParamName = "username"
            passwordParamName = "password"
            validate { credentials ->
                if (authService.validateCredentials(credentials.name, credentials.password)) {
                    UserSession(credentials.name)
                } else {
                    null
                }
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}