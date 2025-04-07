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
        
        val total = statement.count().toInt()
        
        statement = statement.limit(query.pageSize.toLong(), ((query.page - 1) * query.pageSize).toLong())
        
        val items = statement.map { row ->
            Crate(
                id = row[Crates.id],
                name = row[Crates.name],
                description = row[Crates.description],
                image = row[Crates.image]
            )
        }
        
        SearchResult(
            items = items,
            total = total,
            page = query.page,
            pageSize = query.pageSize
        )
    }

    override suspend fun saveAll(items: List<Crate>) = DatabaseFactory.dbQuery {
        items.forEach { crate ->
            Crates.insert {
                it[id] = crate.id
                it[name] = crate.name
                it[description] = crate.description
                it[image] = crate.image
            }
        }
    }

    override suspend fun deleteAll() = DatabaseFactory.dbQuery {
        Crates.deleteAll()
    }
}