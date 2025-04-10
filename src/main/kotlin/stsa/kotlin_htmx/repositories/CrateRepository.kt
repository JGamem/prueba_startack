package stsa.kotlin_htmx.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import stsa.kotlin_htmx.database.*
import stsa.kotlin_htmx.models.*

class CrateRepository : GameItemRepository<Crate> {
    override suspend fun getAll(): List<Crate> = DatabaseFactory.dbQuery {
        Crates.selectAll().map { row ->
            Crate(
                id = row[Crates.id],
                name = row[Crates.name],
                description = row[Crates.description],
                image = row[Crates.image]
            )
        }
    }

    override suspend fun getById(id: String): Crate? = DatabaseFactory.dbQuery {
        Crates.select { Crates.id eq id }
            .map { row ->
                Crate(
                    id = row[Crates.id],
                    name = row[Crates.name],
                    description = row[Crates.description],
                    image = row[Crates.image]
                )
            }
            .singleOrNull()
    }

    override suspend fun search(query: SearchQuery): SearchResult<Crate> = DatabaseFactory.dbQuery {
    var statement = Crates.selectAll()
    
    // Apply search term
    query.term?.let { term ->
        statement = statement.andWhere {
            Crates.name.lowerCase() like "%${term.lowercase()}%" or 
            (Crates.description.lowerCase() like "%${term.lowercase()}%")
        }
    }
    
    // Obtenemos todos los resultados sin paginación
    val allItems = statement.map { row ->
        Crate(
            id = row[Crates.id],
            name = row[Crates.name],
            description = row[Crates.description],
            image = row[Crates.image]
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

    override suspend fun saveAll(items: List<Crate>): Unit = DatabaseFactory.dbQuery {
        items.forEach { crate ->
            Crates.insert {
                it[id] = crate.id
                it[name] = crate.name
                it[description] = crate.description
                it[image] = crate.image
            }
        }
    }

    override suspend fun deleteAll(): Unit = DatabaseFactory.dbQuery {
        Crates.deleteAll()
    }
}