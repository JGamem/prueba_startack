package stsa.kotlin_htmx.link.pages

import io.ktor.server.html.*
import io.ktor.server.routing.RoutingContext
import kotlinx.html.*
import stsa.kotlin_htmx.pages.MainTemplate
import stsa.kotlin_htmx.pages.SelectionTemplate
import org.slf4j.LoggerFactory
import kotlin.collections.set


class LinkMainPage() {
    private val logger = LoggerFactory.getLogger(LinkMainPage::class.java)

    suspend fun renderMainPage(routingHandler: RoutingContext) {
        with(routingHandler) {
            call.respondHtmlTemplate(MainTemplate(template = SelectionTemplate(), "Select Main")) {
                mainSectionTemplate {
                    selectionPagesContent {
                        section {
                            div {
                                form {
                                    attributes["hx-post"] = "/"
                                    attributes["hx-swap"] = "outerHTML"

                                    div(classes = "htmx-indicator") {
                                        id = "formLoading"
                                    }

                                    div {
                                        input {
                                            type = InputType.text
                                            name = "lookupValue"
                                            attributes["aria-label"] = "Value"
                                            required = true
                                        }
                                        button {
                                            attributes["aria-label"] = "Search"
                                            +"Search"
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