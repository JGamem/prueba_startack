package stsa.kotlin_htmx.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import stsa.kotlin_htmx.database.*
import stsa.kotlin_htmx.models.*

class CsKeyRepository : GameItemRepository<CsKey> {
    override suspend fun getAll(): List<CsKey> = DatabaseFactory.dbQuery {
        Keys.selectAll().map { row ->
            val id = row[Keys.id]
            val crates = getCratesForItem(id, "key")
            CsKey(
                id = id,
                name = row[Keys.name],
                description = row[Keys.description],
                image = row[Keys.image],
                crates = crates
            )
        }
    }

    override suspend fun getById(id: String): CsKey? = DatabaseFactory.dbQuery {
        Keys.select { Keys.id eq id }
            .map { row ->
                val crates = getCratesForItem(id, "key")
                CsKey(
                    id = row[Keys.id],
                    name = row[Keys.name],
                    description = row[Keys.description],
                    image = row[Keys.image],
                    crates = crates
                )
            }
            .singleOrNull()
    }

    override suspend fun search(query: SearchQuery): SearchResult<CsKey> = DatabaseFactory.dbQuery {
    var statement = Keys.selectAll()
    
    // Apply search term
    query.term?.let { term ->
        statement = statement.andWhere {
            Keys.name.lowerCase() like "%${term.lowercase()}%" or 
            (Keys.description.lowerCase() like "%${term.lowercase()}%")
        }
    }
    
    // Apply filters
    query.filter.forEach { (key, value) ->
        when (key) {
            "crates" -> {
                // Join with CrateReferences to filter by crate
                statement = Join(
                    Keys, CrateReferences,
                    onColumn = Keys.id, otherColumn = CrateReferences.itemId,
                    additionalConstraint = { CrateReferences.itemType eq "key" }
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
        val id = row[Keys.id]
        val crates = getCratesForItem(id, "key")
        CsKey(
            id = id,
            name = row[Keys.name],
            description = row[Keys.description],
            image = row[Keys.image],
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

    override suspend fun saveAll(items: List<CsKey>): Unit = DatabaseFactory.dbQuery {
        items.forEach { key ->
            Keys.insert {
                it[id] = key.id
                it[name] = key.name
                it[description] = key.description
                it[image] = key.image
            }
            
            // Save crate references
            key.crates?.forEach { crateId ->
                CrateReferences.insert {
                    it[itemId] = key.id
                    it[itemType] = "key"
                    it[CrateReferences.crateId] = crateId
                }
            }
        }
    }

    override suspend fun deleteAll(): Unit = DatabaseFactory.dbQuery {
        CrateReferences.deleteWhere { itemType eq "key" }
        Keys.deleteAll()
    }

    private fun getCratesForItem(itemId: String, itemType: String): List<String> {
        return CrateReferences
            .select { (CrateReferences.itemId eq itemId) and (CrateReferences.itemType eq itemType) }
            .map { it[CrateReferences.crateId] }
    }
}