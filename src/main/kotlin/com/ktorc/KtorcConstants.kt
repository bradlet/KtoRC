package com.ktorc

object KtorcConstants {

    const val DEFAULT_ROOM = "Global"

    // TEMPLATES
    const val STD_RESPONSE_FORMAT = "%s SAID: %s" // {user Id} {message}
    const val WELCOME_MSG_FORMAT = "Welcome %s to the %s chat!" // {user Id} {room id}

    object Headers {
        const val USER_IDENTIFIER = "user_id"
    }

    object Paths {
        const val DEFAULT_URI = "/"
    }

    const val COMMAND_PREFIX = "cm&"
    enum class COMMAND {
        CREATE_ROOM,
        JOIN_ROOM,
        LEAVE_ROOM,
        LIST_ROOMS,
        HERE;
        //DELETE_ROOM,

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