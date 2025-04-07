package stsa.kotlin_htmx.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.days
import kotlinx.serialization.Serializable

@Serializable
class UserSession(val username: String) : Principal

// Define the key globally so it can be accessed from other parts of the app
val AUTH_SERVICE_KEY = AttributeKey<AuthenticationService>("auth-service")

class AuthenticationService {
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    // In a real app, this would be stored in a database with hashed passwords
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
    attributes.put(AUTH_SERVICE_KEY, authService)
    
    install(Sessions) {
    cookie<UserSession>("user_session") {
        cookie.path = "/"
        cookie.maxAgeInSeconds = 1.days.inWholeSeconds
        cookie.extensions["SameSite"] = "lax"
        cookie.httpOnly = true
        
        serializer = object : SessionSerializer<UserSession> {
            override fun serialize(session: UserSession): String {
                return session.username
            }
            
            override fun deserialize(text: String): UserSession {
                return UserSession(text)
            }
        }
        
        val encryptKey = hex("00112233445566778899aabbccddeeff")
        val signKey = hex("a1a2a3a4a5a6a7a8")
        transform(SessionTransportTransformerEncrypt(encryptKey, signKey))
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