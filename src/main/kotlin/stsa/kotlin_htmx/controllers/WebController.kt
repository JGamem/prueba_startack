package stsa.kotlin_htmx.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.slf4j.LoggerFactory
import stsa.kotlin_htmx.auth.UserSession
import stsa.kotlin_htmx.models.SearchQuery
import stsa.kotlin_htmx.services.*

class WebController(
    private val skinService: SkinService,
    private val agentService: AgentService,
    private val crateService: CrateService,
    private val csKeyService: CsKeyService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun registerRoutes(application: Application) {
        application.routing {
            // Home page
            get("/") {
                call.respondHtml {
                    head {
                        title { +"Startrack - Counter-Strike Items" }
                        meta {
                            charset = "UTF-8"
                        }
                        meta {
                            name = "viewport"
                            content = "width=device-width, initial-scale=1.0"
                        }
                        link {
                            rel = "stylesheet"
                            href = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
                        }
                        script {
                            src = "https://unpkg.com/htmx.org@2.0.3"
                        }
                    }
                    body {
                        div("container mt-5") {
                            h1 { +"Startrack - Counter-Strike Items" }
                            
                            div("row mt-4") {
                                div("col-md-3") {
                                    div("card") {
                                        div("card-body") {
                                            h5("card-title") { +"Skins" }
                                            p("card-text") { +"Browse weapon skins" }
                                            a("/skins", classes = "btn btn-primary") { +"Ver Skins" }
                                        }
                                    }
                                }
                                
                                div("col-md-3") {
                                    div("card") {
                                        div("card-body") {
                                            h5("card-title") { +"Agents" }
                                            p("card-text") { +"Browse player agents" }
                                            a("/agents", classes = "btn btn-primary") { +"Ver Agents" }
                                        }
                                    }
                                }
                                
                                div("col-md-3") {
                                    div("card") {
                                        div("card-body") {
                                            h5("card-title") { +"Crates" }
                                            p("card-text") { +"Browse loot crates" }
                                            a("/crates", classes = "btn btn-primary") { +"Ver Crates" }
                                        }
                                    }
                                }
                                
                                div("col-md-3") {
                                    div("card") {
                                        div("card-body") {
                                            h5("card-title") { +"Keys" }
                                            p("card-text") { +"Browse crate keys (requires login)" }
                                            a("/keys", classes = "btn btn-primary") { +"Ver Keys" }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Skins page
            get("/skins") {
                renderItemsPage(call, "Skins", "/api/skins")
            }
            
            // Agents page
            get("/agents") {
                renderItemsPage(call, "Agents", "/api/agents")
            }
            
            // Crates page
            get("/crates") {
                renderItemsPage(call, "Crates", "/api/crates")
            }
            
            // Keys page (protected)
            get("/keys") {
                val userSession = call.sessions.get<UserSession>()
                if (userSession == null) {
                    call.respondRedirect("/login?redirect=/keys")
                } else {
                    renderItemsPage(call, "Keys", "/api/keys")
                }
            }
            
            // Login page
            get("/login") {
                val redirect = call.request.queryParameters["redirect"] ?: "/"
                call.respondHtml {
                    head {
                        title { +"Login - Startrack" }
                        meta {
                            charset = "UTF-8"
                        }
                        meta {
                            name = "viewport"
                            content = "width=device-width, initial-scale=1.0"
                        }
                        link {
                            rel = "stylesheet"
                            href = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
                        }
                        script {
                            src = "https://unpkg.com/htmx.org@2.0.3"
                        }
                    }
                    body {
                        div("container mt-5") {
                            div("row justify-content-center") {
                                div("col-md-6") {
                                    div("card") {
                                        div("card-header") {
                                            h3 { +"Login" }
                                        }
                                        div("card-body") {
                                            form {
                                                attributes["hx-post"] = "/auth/login"
                                                attributes["hx-swap"] = "none"
                                                attributes["hx-on::after-request"] = "if(event.detail.successful) { window.location.href = '$redirect'; }"
                                                
                                                div("mb-3") {
                                                    label("form-label") {
                                                        htmlFor = "username"
                                                        +"Username"
                                                    }
                                                    input(type = InputType.text, classes = "form-control") {
                                                        id = "username"
                                                        name = "username"
                                                        required = true
                                                    }
                                                }
                                                
                                                div("mb-3") {
                                                    label("form-label") {
                                                        htmlFor = "password"
                                                        +"Password"
                                                    }
                                                    input(type = InputType.password, classes = "form-control") {
                                                        id = "password"
                                                        name = "password"
                                                        required = true
                                                    }
                                                }
                                                
                                                button(type = ButtonType.submit, classes = "btn btn-primary") {
                                                    +"Login"
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private suspend fun renderItemsPage(call: ApplicationCall, title: String, apiEndpoint: String) {
        call.respondHtml {
            head {
                title { +"$title - Startrack" }
                meta {
                    charset = "UTF-8"
                }
                meta {
                    name = "viewport"
                    content = "width=device-width, initial-scale=1.0"
                }
                link {
                    rel = "stylesheet"
                    href = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
                }
                script {
                    src = "https://unpkg.com/htmx.org@2.0.3"
                }
                style {
                    +"""
                    .item-card {
                        margin-bottom: 20px;
                        transition: transform 0.3s;
                    }
                    .item-card:hover {
                        transform: translateY(-5px);
                    }
                    .item-image {
                        height: 200px;
                        background-color: #f8f9fa;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .item-image img {
                        max-height: 100%;
                        max-width: 100%;
                        object-fit: contain;
                    }
                    .htmx-indicator {
                        display: none;
                    }
                    .htmx-request .htmx-indicator {
                        display: block;
                    }
                    """
                }
            }
            body {
                div("container mt-4") {
                    h1 { +title }
                    
                    div("row mb-4") {
                        div("col-md-12") {
                            div("card") {
                                div("card-body") {
                                    form {
                                        attributes["hx-post"] = "$apiEndpoint/search"
                                        attributes["hx-target"] = "#results"
                                        attributes["hx-trigger"] = "submit, input[name='term'] changed delay:500ms"
                                        
                                        div("row") {
                                            div("col-md-4") {
                                                div("mb-3") {
                                                    label("form-label") {
                                                        htmlFor = "term"
                                                        +"Search"
                                                    }
                                                    input(type = InputType.text, classes = "form-control") {
                                                        id = "term"
                                                        name = "term"
                                                        placeholder = "Search by name or description"
                                                    }
                                                }
                                            }
                                            
                                            if (title == "Skins" || title == "Agents") {
                                                div("col-md-4") {
                                                    div("mb-3") {
                                                        label("form-label") {
                                                            htmlFor = "team"
                                                            +"Team"
                                                        }
                                                        input(type = InputType.text, classes = "form-control") {
                                                            id = "team"
                                                            name = "filter[team]"
                                                            placeholder = "Filter by team"
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            if (title == "Skins" || title == "Keys") {
                                                div("col-md-4") {
                                                    div("mb-3") {
                                                        label("form-label") {
                                                            htmlFor = "crates"
                                                            +"Crate"
                                                        }
                                                        input(type = InputType.text, classes = "form-control") {
                                                            id = "crates"
                                                            name = "filter[crates]"
                                                            placeholder = "Filter by crate"
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            div("col-md-4 d-flex align-items-end") {
                                                button(type = ButtonType.submit, classes = "btn btn-primary me-2") {
                                                    +"Search"
                                                }
                                                
                                                a("$apiEndpoint/export", classes = "btn btn-secondary") {
                                                    +"Export to XML"
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    div("text-center my-4 htmx-indicator") {
                        id = "loading"
                        +"Loading items..."
                    }
                    
                    div("row") {
                        id = "results"
                        attributes["hx-get"] = apiEndpoint
                        attributes["hx-trigger"] = "load"
                        attributes["hx-indicator"] = "#loading"
                    }
                }
            }
        }
    }
}