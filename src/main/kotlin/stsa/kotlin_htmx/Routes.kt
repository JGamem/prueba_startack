package stsa.kotlin_htmx

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*
import stsa.kotlin_htmx.pages.*
import stsa.kotlin_htmx.link.pages.LinkMainPage
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("stsa.kotlin_htmx.Routes")

fun Application.configurePageRoutes() {
    routing {
        get {
            call.respondHtmlTemplate(MainTemplate(template = EmptyTemplate(), "Front page")) {
                mainSectionTemplate {
                    emptyContentWrapper {
                        section {
                            p {
                                +"Startrack Demo"
                            }
                        }
                    }
                }
            }
        }

        route("/link") {
            val linkMainPage = LinkMainPage()
            get {
                linkMainPage.renderMainPage(this)
            }
        }
    }
}
