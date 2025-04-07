package stsa.kotlin_htmx.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import stsa.kotlin_htmx.auth.AuthenticationService
import stsa.kotlin_htmx.auth.UserSession
import stsa.kotlin_htmx.models.SearchQuery
import stsa.kotlin_htmx.services.*
import stsa.kotlin_htmx.util.XmlConverter

class ApiController(
    private val skinService: SkinService,
    private val agentService: AgentService,
    private val crateService: CrateService,
    private val keyService: CsKeyService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val xmlConverter = XmlConverter()
    
    fun registerRoutes(application: Application) {
        application.routing {
            // Rutas sin autenticación
            route("/api/skins") {
                get {
                    val skins = skinService.getAll()
                    call.respond(skins)
                }
                
                get("/{id}") {
                    val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val skin = skinService.getById(id) ?: return@get call.respond(HttpStatusCode.NotFound)
                    call.respond(skin)
                }
                
                post("/search") {
                    val query = call.receive<SearchQuery>()
                    val results = skinService.search(query)
                    call.respond(results)
                }
            }
            
            // Rutas para agentes
            route("/api/agents") {
                get {
                    val agents = agentService.getAll()
                    call.respond(agents)
                }
                
                get("/{id}") {
                    val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val agent = agentService.getById(id) ?: return@get call.respond(HttpStatusCode.NotFound)
                    call.respond(agent)
                }
                
                post("/search") {
                    val query = call.receive<SearchQuery>()
                    val results = agentService.search(query)
                    call.respond(results)
                }
            }
            
            // Rutas para crates
            route("/api/crates") {
                get {
                    val crates = crateService.getAll()
                    call.respond(crates)
                }
                
                get("/{id}") {
                    val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val crate = crateService.getById(id) ?: return@get call.respond(HttpStatusCode.NotFound)
                    call.respond(crate)
                }
                
                post("/search") {
                    val query = call.receive<SearchQuery>()
                    val results = crateService.search(query)
                    call.respond(results)
                }
            }
            
            // Rutas protegidas para keys
            route("/api/keys") {
                authenticate("auth-session") {
                    get {
                        val keys = keyService.getAll()
                        call.respond(keys)
                    }
                    
                    get("/{id}") {
                        val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                        val key = keyService.getById(id) ?: return@get call.respond(HttpStatusCode.NotFound)
                        call.respond(key)
                    }
                    
                    post("/search") {
                        val query = call.receive<SearchQuery>()
                        val results = keyService.search(query)
                        call.respond(results)
                    }
                }
            }
            
            // Rutas de autenticación
            route("/auth") {
                post("/login") {
                    try {
                        val formParameters = call.receiveParameters()
                        val username = formParameters["username"]
                        val password = formParameters["password"]
                        
                        if (username != null && password != null) {
                            val authService = AuthenticationService()
                            
                            if (authService.validateCredentials(username, password)) {
                                val principal = UserSession(username)
                                call.sessions.set(principal)
                                call.respond(HttpStatusCode.OK, mapOf("message" to "Login successful"))
                            } else {
                                call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid credentials"))
                            }
                        } else {
                            call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Missing credentials"))
                        }
                    } catch (e: Exception) {
                        application.log.error("Login error", e)
                        call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "An error occurred during login"))
                    }
                }
                
                get("/logout") {
                    call.sessions.clear<UserSession>()
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Logged out successfully"))
                }
                
                get("/status") {
                    val userSession = call.sessions.get<UserSession>()
                    if (userSession != null) {
                        call.respond(mapOf("authenticated" to true, "username" to userSession.username))
                    } else {
                        call.respond(mapOf("authenticated" to false))
                    }
                }
            }
        }
    }
}