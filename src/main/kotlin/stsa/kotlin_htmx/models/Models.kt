package stsa.kotlin_htmx.models

interface GameItem {
    val id: String
    val name: String
    val description: String?
    val image: String?
}

data class Skin(
    override val id: String,
    override val name: String,
    override val description: String? = null,
    override val image: String? = null,
    val crates: List<String>? = null,
    val team: String? = null
) : GameItem

data class Agent(
    override val id: String,
    override val name: String,
    override val description: String? = null,
    override val image: String? = null,
    val team: String? = null
) : GameItem

data class Crate(
    override val id: String,
    override val name: String,
    override val description: String? = null,
    override val image: String? = null
) : GameItem

data class CsKey(
    override val id: String,
    override val name: String,
    override val description: String? = null,
    override val image: String? = null,
    val crates: List<String>? = null
) : GameItem

data class SearchQuery(
    val term: String? = null,
    val filter: Map<String, String> = emptyMap(),
    val page: Int = 1,
    val pageSize: Int = 20
)

data class SearchResult<T : GameItem>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)