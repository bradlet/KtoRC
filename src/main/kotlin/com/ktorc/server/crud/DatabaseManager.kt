package com.ktorc.server.crud

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class DatabaseManager(
    private val database: Database
) {

    suspend fun listRooms(): List<Room> = newSuspendedTransaction(db = database) {
        Room.all().toList()
    }

    suspend fun createRoom(roomName: String) = newSuspendedTransaction(db = database) {
        Room.new { name = roomName }
    }

    // Returns false if no room with given name is found to be deleted.
    // Returns true if room found and deleted
    suspend fun deleteRoom(roomName: String): Boolean = newSuspendedTransaction(db = database) {
        val room = Room.find { RoomTable.name eq roomName }.firstOrNull()
        if (room == null)
            false
        else {
            room.delete()
            true
        }
    }

    companion object {
    }
}


object UserTable: IntIdTable(name = "users") {
    val name: Column<String> = text("name")
    val email: Column<String> = text("email")
}

object RoomMembersTable: Table(name = "room_members") {
    val roomId: Column<Int> = integer("room_id")
    val userId: Column<Int> = integer("user_id")
}
