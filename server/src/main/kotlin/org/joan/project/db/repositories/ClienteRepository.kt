package org.joan.project.db.repositories

import org.joan.project.db.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.joan.project.db.Clientes
import org.joan.project.db.entidades.ClienteRequest
import org.joan.project.db.entidades.ClienteResponse

class ClienteRepository {
    suspend fun getAll(): List<ClienteResponse> = dbQuery {
        Clientes.selectAll().map { toClienteResponse(it) }
    }

    suspend fun getById(id: Int): ClienteResponse? = dbQuery {
        Clientes.select { Clientes.id eq id }
            .map { toClienteResponse(it) }
            .singleOrNull()
    }

    suspend fun create(clienteRequest: ClienteRequest): ClienteResponse = dbQuery {
        val insertStatement = Clientes.insert {
            it[nombre] = clienteRequest.nombre
            it[telefono] = clienteRequest.telefono
        }

        // Obtener el cliente recién creado para construir la respuesta
        Clientes.select { Clientes.id eq insertStatement[Clientes.id] }
            .map { toClienteResponse(it) }
            .single()
    }

    suspend fun update(id: Int, clienteRequest: ClienteRequest): Boolean = dbQuery {
        Clientes.update({ Clientes.id eq id }) {
            it[nombre] = clienteRequest.nombre
            it[telefono] = clienteRequest.telefono
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        Clientes.deleteWhere { Clientes.id eq id } > 0
    }

    private fun toClienteResponse(row: ResultRow): ClienteResponse {
        return ClienteResponse(
            id = row[Clientes.id].value,
            nombre = row[Clientes.nombre],
            telefono = row[Clientes.telefono]
        )
    }
}