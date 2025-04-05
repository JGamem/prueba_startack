package stsa.kotlin_htmx.pages

import kotlinx.html.*
import org.intellij.lang.annotations.Language

object HtmlElements {

    fun STYLE.rawCss(@Language("CSS") css: String) {
        unsafe {
            raw(css)
        }
    }

}