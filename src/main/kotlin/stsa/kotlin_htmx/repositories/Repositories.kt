package stsa.kotlin_htmx.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import stsa.kotlin_htmx.database.*
import stsa.kotlin_htmx.models.*

interface GameItemRepository<T : GameItem> {
    suspend fun getAll(): List<T>
    suspend fun getById(id: String): T?
    suspend fun search(query: SearchQuery): SearchResult<T>
    suspend fun saveAll(items: List<T>)
    suspend fun deleteAll()
}

class SkinRepository : GameItemRepository<Skin> {
    suspend fun getByCrateId(crateId: String): List<Skin> = DatabaseFactory.dbQuery {
        (Skins innerJoin CrateReferences)
            .select { (CrateReferences.crateId eq crateId) and (CrateReferences.itemType eq "skin") }
            .map { 
                Skin(
                    id = it[Skins.id],
                    name = it[Skins.name],
                    description = it[Skins.description],
                    image = it[Skins.image],
                    team = it[Skins.team]
                ) 
            }
    }

    override suspend fun getAll(): List<Skin> = DatabaseFactory.dbQuery {
        Skins.selectAll().map { row ->
            val id = row[Skins.id]
            val crates = getCratesForItem(id, "skin")
            Skin(
                id = id,
                name = row[Skins.name],
                description = row[Skins.description],
                image = row[Skins.image],
                team = row[Skins.team],
                crates = crates
            )
        }
    }

    override suspend fun getById(id: String): Skin? = DatabaseFactory.dbQuery {
        Skins.select { Skins.id eq id }
            .map { row ->
                val crates = getCratesForItem(id, "skin")
                Skin(
                    id = row[Skins.id],
                    name = row[Skins.name],
                    description = row[Skins.description],
                    image = row[Skins.image],
                    team = row[Skins.team],
                    crates = crates
                )
            }
            .singleOrNull()
    }

    override suspend fun search(query: SearchQuery): SearchResult<Skin> = DatabaseFactory.dbQuery {
    var statement = Skins.selectAll()
    
    // Apply search term
    query.term?.let { term ->
        statement = statement.andWhere {
            Skins.name.lowerCase() like "%${term.lowercase()}%" or 
            (Skins.description.lowerCase() like "%${term.lowercase()}%")
        }
    }
    
    // Apply filters
    query.filter.forEach { (key, value) ->
        when (key) {
            "team" -> statement = statement.andWhere { Skins.team.lowerCase() like "%${value.lowercase()}%" }
            "crates" -> {
                // Join with CrateReferences to filter by crate
                statement = Join(
                    Skins, CrateReferences,
                    onColumn = Skins.id, otherColumn = CrateReferences.itemId,
                    additionalConstraint = { CrateReferences.itemType eq "skin" }
                ).selectAll().andWhere {
                    val crateId = Crates.select { Crates.name.lowerCase() eq value.lowercase() }
                        .map { it[Crates.id] }
                        .firstOrNull()
                    crateId?.let { CrateReferences.crateId eq it } ?: Op.FALSE
                }
            }
        }
    }
    
    // Obtenemos todos los resultados sin paginación
    val allItems = statement.map { row ->
        val id = row[Skins.id]
        val crates = getCratesForItem(id, "skin")
        Skin(
            id = id,
            name = row[Skins.name],
            description = row[Skins.description],
            image = row[Skins.image],
            team = row[Skins.team],
            crates = crates
        )
    }.toList()
    
    // Aplicamos paginación manualmente
    val total = allItems.size
    val startIndex = (query.page - 1) * query.pageSize
    val endIndex = minOf(startIndex + query.pageSize, total)
    val paginatedItems = if (startIndex < total) allItems.subList(startIndex, endIndex) else emptyList()
    
    SearchResult(
        items = paginatedItems,
        total = total,
        page = query.page,
        pageSize = query.pageSize
    )
}
    override suspend fun saveAll(items: List<Skin>): Unit = DatabaseFactory.dbQuery {
        items.forEach { skin ->
            Skins.insert {
                it[id] = skin.id
                it[name] = skin.name
                it[description] = skin.description
                it[image] = skin.image
                it[team] = skin.team
            }
            
            // Save crate references
            skin.crates?.forEach { crateId ->
                CrateReferences.insert {
                    it[itemId] = skin.id
                    it[itemType] = "skin"
                    it[CrateReferences.crateId] = crateId
                }
            }
        }
    }

    override suspend fun deleteAll(): Unit = DatabaseFactory.dbQuery {
        CrateReferences.deleteWhere { itemType eq "skin" }
        Skins.deleteAll()
    }

    private fun getCratesForItem(itemId: String, itemType: String): List<String> {
        return CrateReferences
            .select { (CrateReferences.itemId eq itemId) and (CrateReferences.itemType eq itemType) }
            .map { it[CrateReferences.crateId] }
    }
}