package com.ktorc.server.crud

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object RoomTable: IntIdTable(name = "rooms") {
    val name: Column<String> = text("name")
}

class Room(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Room>(RoomTable)
    var name by RoomTable.name
}

