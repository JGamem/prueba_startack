package stsa.kotlin_htmx.database

import org.jetbrains.exposed.sql.Table

object Skins : Table("skins") {
    val id = varchar("id", 50)
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val image = text("image").nullable()
    val team = varchar("team", 100).nullable()

    override val primaryKey = PrimaryKey(id)
}

object Agents : Table("agents") {
    val id = varchar("id", 50)
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val image = text("image").nullable()
    val team = varchar("team", 100).nullable()

    override val primaryKey = PrimaryKey(id)
}

object Crates : Table("crates") {
    val id = varchar("id", 50)
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val image = text("image").nullable()

    override val primaryKey = PrimaryKey(id)
}

object Keys : Table("keys") {
    val id = varchar("id", 50)
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val image = text("image").nullable()

    override val primaryKey = PrimaryKey(id)
}

object CrateReferences : Table("crate_references") {
    val itemId = varchar("item_id", 50)
    val itemType = varchar("item_type", 50)
    val crateId = varchar("crate_id", 50).references(Crates.id)

    override val primaryKey = PrimaryKey(arrayOf(itemId, itemType, crateId))
}