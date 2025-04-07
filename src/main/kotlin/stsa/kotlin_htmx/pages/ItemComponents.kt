package stsa.kotlin_htmx.pages

import io.ktor.server.html.*
import kotlinx.html.*
import stsa.kotlin_htmx.models.*

class ItemListTemplate<T : GameItem> : Template<FlowContent> {
    var items: List<T> = emptyList()
    var total: Int = 0
    var page: Int = 1
    var pageSize: Int = 20
    
    override fun FlowContent.apply() {
        div(classes = "item-list") {
            if (items.isEmpty()) {
                p { +"No items found" }
            } else {
                // Items grid
                div(classes = "items-grid") {
                    items.forEach { item ->
                        div(classes = "item-card") {
                            div(classes = "item-image") {
                                item.image?.let {
                                    img(src = it, alt = item.name)
                                } ?: run {
                                    div(classes = "no-image") { +"No image" }
                                }
                            }
                            
                            div(classes = "item-details") {
                                h3 { +item.name }
                                item.description?.let {
                                    p { +it }
                                }
                                
                                // Display team if available
                                when (item) {
                                    is Skin -> item.team?.let {
                                        p { +"Team: $it" }
                                    }
                                    is Agent -> item.team?.let {
                                        p { +"Team: $it" }
                                    }
                                    else -> {}
                                }
                                
                                // Display crates if available
                                when (item) {
                                    is Skin -> item.crates?.let { crates ->
                                        if (crates.isNotEmpty()) {
                                            p { +"Crates: ${crates.joinToString(", ")}" }
                                        }
                                    }
                                    is Key -> item.crates?.let { crates ->
                                        if (crates.isNotEmpty()) {
                                            p { +"Crates: ${crates.joinToString(", ")}" }
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
                
                // Pagination
                if (total > pageSize) {
                    div(classes = "pagination") {
                        val totalPages = (total + pageSize - 1) / pageSize
                        
                        if (page > 1) {
                            a(href = "#", classes = "page-link") {
                                attributes["hx-get"] = "?page=${page - 1}"
                                attributes["hx-target"] = "#results"
                                +"Previous"
                            }
                        }
                        
                        for (i in 1..minOf(5, totalPages)) {
                            a(href = "#", classes = if (i == page) "page-link active" else "page-link") {
                                attributes["hx-get"] = "?page=$i"
                                attributes["hx-target"] = "#results"
                                +"$i"
                            }
                        }
                        
                        if (page < totalPages) {
                            a(href = "#", classes = "page-link") {
                                attributes["hx-get"] = "?page=${page + 1}"
                                attributes["hx-target"] = "#results"
                                +"Next"
                            }
                        }
                    }
                }
            }
        }
    }
}