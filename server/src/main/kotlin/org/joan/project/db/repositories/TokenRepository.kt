package org.joan.project.db.repositories

import org.joan.project.db.Tokens
import org.joan.project.db.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import java.time.LocalDateTime

class TokenRepository {

    suspend fun guardarToken(empleadoId: Int, token: String, expiracion: LocalDateTime) = dbQuery {
        Tokens.insert {
            it[Tokens.empleadoId] = empleadoId
            it[Tokens.token] = token
            it[Tokens.creadoEn] = LocalDateTime.now()
            it[Tokens.expiracion] = expiracion
        }
    }

    suspend fun validarToken(token: String): Boolean = dbQuery {
        Tokens.select {
            (Tokens.token eq token) and
                    (Tokens.expiracion greaterEq LocalDateTime.now())
        }.count() > 0
    }

    suspend fun eliminarToken(token: String) = dbQuery {
        Tokens.deleteWhere { Tokens.token eq token }
    }

    suspend fun limpiarTokensExpirados() = dbQuery {
        Tokens.deleteWhere {
            Tokens.expiracion less LocalDateTime.now()
        }
    }
}
