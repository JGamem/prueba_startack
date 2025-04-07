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
            // Skins endpoints
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
                
                get("/export") {
                    val term = call.request.queryParameters["term"]
                    val filters = call.request.queryParameters.entries()
                        .filter { it.key != "term" && !it.key.startsWith("_") }
                        .associate { it.key to it.value.first() }
                    
                    val query = SearchQuery(term = term, filter = filters)
                    val results = skinService.search(query)
                    
                    val xml = xmlConverter.toXml(results.items)
                    call.respondText(xml, ContentType.Application.Xml)
                }
            }
            
            // Agents endpoints
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
                
                get("/export") {
                    val term = call.request.queryParameters["term"]
                    val filters = call.request.queryParameters.entries()
                        .filter { it.key != "term" && !it.key.startsWith("_") }
                        .associate { it.key to it.value.first() }
                    
                    val query = SearchQuery(term = term, filter = filters)
                    val results = agentService.search(query)
                    
                    val xml = xmlConverter.toXml(results.items)
                    call.respondText(xml, ContentType.Application.Xml)
                }
            }
            
            // Crates endpoints
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
                
                get("/export") {
                    val term = call.request.queryParameters["term"]
                    val filters = call.request.queryParameters.entries()
                        .filter { it.key != "term" && !it.key.startsWith("_") }
                        .associate { it.key to it.value.first() }
                    
                    val query = SearchQuery(term = term, filter = filters)
                    val results = crateService.search(query)
                    
                    val xml = xmlConverter.toXml(results.items)
                    call.respondText(xml, ContentType.Application.Xml)
                }
            }
            
            // Keys endpoints (protected with authentication)
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
                    
                    get("/export") {
                        val term = call.request.queryParameters["term"]
                        val filters = call.request.queryParameters.entries()
                            .filter { it.key != "term" && !it.key.startsWith("_") }
                            .associate { it.key to it.value.first() }
                        
                        val query = SearchQuery(term = term, filter = filters)
                        val results = keyService.search(query)
                        
                        val xml = xmlConverter.toXml(results.items)
                        call.respondText(xml, ContentType.Application.Xml)
                    }
                }
            }
            
            // Authentication endpoints
            route("/auth") {
                post("/login") {
                    val authFormResult = application.plugin(Authentication).providers
                        .firstOrNull { it.name == "auth-form" }
                    
                    // This is a simple workaround for the suspend function issue
                    val username = call.request.queryParameters["username"] 
                        ?: call.request.post()["username"]
                    val password = call.request.queryParameters["password"] 
                        ?: call.request.post()["password"]
                    
                    if (username != null && password != null) {
                        val authService = application.attributes.get(AttributeKey("auth-service"))
                                as? AuthenticationService ?: AuthenticationService()
                        
                        if (authService.validateCredentials(username, password)) {
                            val principal = UserSession(username)
                            call.sessions.set(principal)
                            call.respond(HttpStatusCode.OK, mapOf("message" to "Login successful"))
                        } else {
                            call.respond(HttpStatusCode.Unauthorized)
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Missing credentials"))
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
            
            // XML Export endpoint
            get("/xml") {
                call.respondText(
                    """<?xml version="1.0" encoding="UTF-8"?><startrack></startrack>""",
                    ContentType.Application.Xml
                )
            }
        }
    }
}