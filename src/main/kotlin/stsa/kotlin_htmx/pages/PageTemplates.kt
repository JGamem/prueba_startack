package stsa.kotlin_htmx.pages

import io.ktor.server.html.*
import kotlinx.html.*
import stsa.kotlin_htmx.pages.HtmlElements.rawCss

/**
 * See https://ktor.io/docs/server-html-dsl.html#templates for more information
 */
class MainTemplate<T : Template<FlowContent>>(private val template: T, val pageTitle: String) : Template<HTML> {

    val mainSectionTemplate = TemplatePlaceholder<T>()
    val headerContent = Placeholder<FlowContent>()

    override fun HTML.apply() {
        lang = "en"
        attributes["data-theme"] = "light"

        head {
            title { +"HTMX and KTor <3 - $pageTitle" }
            meta { charset = "UTF-8" }
            meta {
                name = "viewport"
                content = "width=device-width, initial-scale=1"
            }
            meta {
                name = "description"
                content = "Hello"
            }
            link {
                rel = "icon"
                href = "/static/favicon.ico"
                type = "image/x-icon"
                sizes = "any"
            }
            link {
                rel = "stylesheet"
                href = "https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css"
            }
            script(src = "https://www.googletagmanager.com/gtag/js?id=G-30QSF4X9PW") {}
            script {
                unsafe {
                    raw(
                        """
                          window.dataLayer = window.dataLayer || [];
                          function gtag(){dataLayer.push(arguments);}
                          gtag('js', new Date());

                          gtag('config', 'G-30QSF4X9PW');
                        """.trimIndent()
                    )
                }
            }
            script(src = "https://unpkg.com/htmx.org@2.0.3") {}
            script(src = "https://unpkg.com/htmx-ext-json-enc@2.0.1/json-enc.js") {}
            script(src = "https://unpkg.com/htmx-ext-preload@2.0.1/preload.js") {}
            script(src = "https://unpkg.com/htmx-ext-sse@2.2.2/sse.js") {}

            style {
                    rawCss(
                        """
                            /* General styling */
                            body {
                                font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
                                line-height: 1.6;
                                color: #333;
                                background-color: #f8f9fa;
                            }
                            
                            /* Item grid */
                            .items-grid {
                                display: grid;
                                grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
                                gap: 20px;
                                margin-top: 20px;
                            }
                            
                            .item-card {
                                background-color: white;
                                border-radius: 8px;
                                box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
                                overflow: hidden;
                                transition: transform 0.3s ease;
                            }
                            
                            .item-card:hover {
                                transform: translateY(-5px);
                                box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
                            }
                            
                            .item-image {
                                height: 200px;
                                background-color: #f0f0f0;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                            }
                            
                            .item-image img {
                                max-width: 100%;
                                max-height: 100%;
                                object-fit: contain;
                            }
                            
                            .no-image {
                                color: #999;
                                font-style: italic;
                            }
                            
                            .item-details {
                                padding: 15px;
                            }
                            
                            .item-details h3 {
                                margin-top: 0;
                                margin-bottom: 10px;
                                color: #156ae8;
                            }
                            
                            /* Search form */
                            #searchForm {
                                background-color: white;
                                padding: 20px;
                                border-radius: 8px;
                                box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
                                margin-bottom: 20px;
                            }
                            
                            #searchForm label {
                                font-weight: 500;
                                margin-right: 10px;
                            }
                            
                            #searchForm input {
                                padding: 8px 12px;
                                border: 1px solid #ddd;
                                border-radius: 4px;
                                margin-right: 10px;
                                margin-bottom: 10px;
                            }
                            
                            .export-btn {
                                display: inline-block;
                                background-color: #4CAF50;
                                color: white;
                                padding: 8px 16px;
                                border-radius: 4px;
                                text-decoration: none;
                                margin-left: 10px;
                            }
                            
                            .export-btn:hover {
                                background-color: #45a049;
                            }
                            
                            /* Pagination */
                            .pagination {
                                display: flex;
                                justify-content: center;
                                margin-top: 30px;
                            }
                            
                            .page-link {
                                display: inline-block;
                                padding: 8px 16px;
                                margin: 0 5px;
                                border-radius: 4px;
                                background-color: #f0f0f0;
                                color: #333;
                                text-decoration: none;
                            }
                            
                            .page-link.active {
                                background-color: #156ae8;
                                color: white;
                            }
                            
                            .page-link:hover:not(.active) {
                                background-color: #ddd;
                            }
                            
                            /* Loading indicator */
                            .htmx-indicator {
                                display: none;
                                padding: 20px;
                                text-align: center;
                                font-style: italic;
                                color: #666;
                            }
                            
                            .htmx-request .htmx-indicator {
                                display: block;
                            }
                        """.trimIndent()
                    )
                }
        body {            // This is inherited so means we use JSON as a default for all communication
            attributes["hx-ext"] = "json-enc"

            div {
                style = "max-width: 90vw; margin: auto;"

                // Logo
                header {
                    h1 { +"Startrack Demos" }

                    nav {
                        ul {
                            li { a(href = "/") { +"Home" } }
                            li { span("separator") { +"🚀" } }
                            li { a(href = "/link") { +"Category" } }
                            li { span("separator") { +"🚀" } }
                            li { a(href = "/link") { +"Category" } }
                            li { span("separator") { +"🚀" } }
                            li { a(href = "/link") { +"Category" } }
                            li { span("separator") { +"🚀" } }
                            li { a(href = "/link") { +"Category" } }
                        }
                    }

                    div {
                        insert(headerContent)
                    }
                }

                // Main content
                main {
                    id = "mainContent"
                    insert(template, mainSectionTemplate)
                }

                footer {
                    +""
                }

                script {
                    unsafe {
                        raw(
                            """
                            document.body.addEventListener('htmx:afterSettle', function(evt) {
                                // The updated element is directly available in evt.detail.elt
                                const updatedElement = evt.detail.elt;
                                updatedElement.classList.add('htmx-modified');
                            });
                        """.trimIndent()
                        )
                    }
                }
            }
        }
    }
}

// The two below is mainly to cater for two different sub-templates
class SelectionTemplate : Template<FlowContent> {
    val selectionPagesContent = Placeholder<FlowContent>()

    override fun FlowContent.apply() {
        style {
            rawCss(
                """
                    #choices {
                        display: grid; /* Enables grid layout */
                        grid-template-columns: repeat(auto-fit, minmax(15em, 1fr)); /* Adjust the number of columns based on the width of the container */
                        /* Key line for responsiveness: */
                        gap: 20px; /* Adjust the spacing between items */
            
                        a {
                            display: block;
                        }
                    }                    
                """.trimIndent()
            )
        }
        insert(selectionPagesContent)
    }
}

/**
 * This is an empty template to allow us to enforce specifying something
 *
 * There is probably a better way to do this
 */
class EmptyTemplate : Template<FlowContent> {
    val emptyContentWrapper = Placeholder<FlowContent>()

    override fun FlowContent.apply() {
        insert(emptyContentWrapper)
    }
}