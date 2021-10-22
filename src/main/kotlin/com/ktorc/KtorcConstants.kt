package com.ktorc

object KtorcConstants {

    const val DEFAULT_ROOM = "Global"

    // Secret-Related Constants
    const val SECRET_VERSION = "1"
    const val PROJECT = "thompson-gcp"

    // Database Constants
    val JDBC_URL_TEMPLATE = """
        jdbc:postgresql:///%s?cloudSqlInstance=%s&socketFactory=
        com.google.cloud.sql.postgres.SocketFactory&user=%s&password=%s
    """.trimIndent() // <DB_NAME> <CLOUD_SQL_CONNECTION_NAME> <DB_USER> <DB_PASS>


    // TEMPLATES
    const val STD_RESPONSE_FORMAT = "%s: %s" // {user Id} {message}
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