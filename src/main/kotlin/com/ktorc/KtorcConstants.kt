package com.ktorc

object KtorcConstants {

    const val STD_RESPONSE_FORMAT = "%s SAID: %s" // {user id} {message}

    object Headers {
        const val USER_IDENTIFIER = "user_id"
    }

    object Params {
        const val ROOM_IDENTIFIER = "room_id"
    }

    const val COMMAND_PREFIX = "cm&"
    enum class COMMAND {
        CREATE_ROOM,
        CHANGE_ROOM,
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