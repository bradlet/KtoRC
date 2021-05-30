package com.ktorc

object KtorcConstants {

    const val DEFAULT_ROOM = "Global"
    const val STD_RESPONSE_FORMAT = "%s SAID: %s" // {user Id} {message}

    object Headers {
        const val USER_IDENTIFIER = "user_id"
    }

    object Params {
        const val ROOM_IDENTIFIER = "room_id"
    }

    object Paths {
        const val DEFAULT_URI = "/"
        const val ROOM_URI = "/room/%s" // {room Id}
    }

    const val COMMAND_PREFIX = "cm&"
    enum class COMMAND {
        CREATE_ROOM,
        JOIN_ROOM,
        DELETE_ROOM,
        LIST_ROOMS;

        companion object{
            fun nullableValueOf(value: String): COMMAND? {
                return if (values().map { it.name }.contains(value))
                    valueOf(value)
                else
                    null
            }
        }
    }

}