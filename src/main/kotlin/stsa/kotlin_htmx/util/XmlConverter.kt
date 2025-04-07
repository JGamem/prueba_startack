package stsa.kotlin_htmx.util

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import stsa.kotlin_htmx.models.GameItem

@JacksonXmlRootElement(localName = "items")
data class ItemsWrapper<T : GameItem>(val items: List<T>)

class XmlConverter {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val xmlMapper = XmlMapper().registerKotlinModule()
    
    fun <T : GameItem> toXml(items: List<T>): String {
        return try {
            val wrapper = ItemsWrapper(items)
            val xml = xmlMapper.writeValueAsString(wrapper)
            """<?xml version="1.0" encoding="UTF-8"?>$xml"""
        } catch (e: Exception) {
            logger.error("Error converting to XML: ${e.message}", e)
            """<?xml version="1.0" encoding="UTF-8"?><items/>"""
        }
    }
}