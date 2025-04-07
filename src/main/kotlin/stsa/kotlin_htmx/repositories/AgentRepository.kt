package stsa.kotlin_htmx.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import stsa.kotlin_htmx.database.*
import stsa.kotlin_htmx.models.*

class AgentRepository : GameItemRepository<Agent> {
    override suspend fun getAll(): List<Agent> = DatabaseFactory.dbQuery {
        Agents.selectAll().map { row ->
            Agent(
                id = row[Agents.id],
                name = row[Agents.name],
                description = row[Agents.description],
                image = row[Agents.image],
                team = row[Agents.team]
            )
        }
    }

    override suspend fun getById(id: String): Agent? = DatabaseFactory.dbQuery {
        Agents.select { Agents.id eq id }
            .map { row ->
                Agent(
                    id = row[Agents.id],
                    name = row[Agents.name],
                    description = row[Agents.description],
                    image = row[Agents.image],
                    team = row[Agents.team]
                )
            }
            .singleOrNull()
    }

    override suspend fun search(query: SearchQuery): SearchResult<Agent> = DatabaseFactory.dbQuery {
        var statement = Agents.selectAll()
        
        // Apply search term
        query.term?.let { term ->
            statement = statement.andWhere {
                Agents.name.lowerCase() like "%${term.lowercase()}%" or 
                (Agents.description.lowerCase() like "%${term.lowercase()}%")
            }
        }
        
        // Apply filters
        query.filter.forEach { (key, value) ->
            when (key) {
                "team" -> statement = statement.andWhere { Agents.team.lowerCase() like "%${value.lowercase()}%" }
            }
        }
        
        val total = statement.count().toInt()
        
        // Convertir query.pageSize y el offset a Long
        val pageSizeLong = query.pageSize.toLong()
        val offsetLong = ((query.page - 1) * query.pageSize).toLong()
        statement = statement.limit(pageSizeLong, offsetLong)
        
        val items = statement.map { row ->
            Agent(
                id = row[Agents.id],
                name = row[Agents.name],
                description = row[Agents.description],
                image = row[Agents.image],
                team = row[Agents.team]
            )
        }
        
        SearchResult(
            items = items,
            total = total,
            page = query.page,
            pageSize = query.pageSize
        )
    }

    override suspend fun saveAll(items: List<Agent>): Unit = DatabaseFactory.dbQuery {
        items.forEach { agent ->
            Agents.insert {
                it[id] = agent.id
                it[name] = agent.name
                it[description] = agent.description
                it[image] = agent.image
                it[team] = agent.team
            }
        }
    }

    override suspend fun deleteAll(): Unit = DatabaseFactory.dbQuery {
        Agents.deleteAll()
    }
}