package org.joan.project.db.repositories

import org.joan.project.db.DatabaseFactory.dbQuery
import org.joan.project.db.Empleados
import org.joan.project.db.entidades.EmpleadoLoginRequest
import org.joan.project.db.entidades.EmpleadoRequest
import org.joan.project.db.entidades.EmpleadoResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.mindrot.jbcrypt.BCrypt

class EmpleadoRepository {

    suspend fun autenticar(loginRequest: EmpleadoLoginRequest): EmpleadoResponse? = dbQuery {
        val row = Empleados.select { Empleados.usuario eq loginRequest.usuario }
            .singleOrNull() ?: return@dbQuery null

        val hashEnBD = row[Empleados.contraseña]
        val coincide = BCrypt.checkpw(loginRequest.contraseña, hashEnBD)

        if (coincide) {
            EmpleadoResponse(
                id = row[Empleados.id].value,
                nombre = row[Empleados.nombre],
                usuario = row[Empleados.usuario],
                rol = row[Empleados.rol]
            )
        } else {
            null
        }
    }

    suspend fun getById(id: Int): EmpleadoResponse? = dbQuery {
        Empleados.select { Empleados.id eq id }
            .map {
                EmpleadoResponse(
                    id = it[Empleados.id].value,
                    nombre = it[Empleados.nombre],
                    usuario = it[Empleados.usuario],
                    rol = it[Empleados.rol]
                )
            }.singleOrNull()
    }

    suspend fun getAll(): List<EmpleadoResponse> = dbQuery {
        Empleados.selectAll().map {
            EmpleadoResponse(
                id = it[Empleados.id].value,
                nombre = it[Empleados.nombre],
                usuario = it[Empleados.usuario],
                rol = it[Empleados.rol]
            )
        }
    }

    suspend fun crearEmpleado(request: EmpleadoRequest): EmpleadoResponse {
        if (usuarioExiste(request.usuario)) {
            throw IllegalArgumentException("El usuario '${request.usuario}' ya está en uso")
        }

        val hashedPassword = BCrypt.hashpw(request.contraseña, BCrypt.gensalt())
        val insert = Empleados.insert {
            it[nombre] = request.nombre
            it[usuario] = request.usuario
            it[contraseña] = hashedPassword
            it[rol] = request.rol
        }

        return EmpleadoResponse(
            id = insert[Empleados.id].value,
            nombre = insert[Empleados.nombre],
            usuario = insert[Empleados.usuario],
            rol = insert[Empleados.rol]
        )
    }


    suspend fun actualizarEmpleado(id: Int, request: EmpleadoRequest): Boolean = dbQuery {
        // Comprobar si el usuario ya está en uso por otro empleado
        val existe = Empleados.select {
            (Empleados.usuario eq request.usuario) and (Empleados.id neq id)
        }.count() > 0

        if (existe) {
            throw IllegalArgumentException("El usuario '${request.usuario}' ya está en uso")
        }

        val hashedPassword = BCrypt.hashpw(request.contraseña, BCrypt.gensalt())

        val actualizados = Empleados.update({ Empleados.id eq id }) {
            it[nombre] = request.nombre
            it[usuario] = request.usuario
            it[contraseña] = hashedPassword
            it[rol] = request.rol
        }

        actualizados > 0
    }



    suspend fun eliminarEmpleado(id: Int): Boolean = dbQuery {
        Empleados.deleteWhere { Empleados.id eq id } > 0
    }

    private suspend fun usuarioExiste(usuario: String, excluirId: Int? = null): Boolean = dbQuery {
        val query = Empleados.select { Empleados.usuario eq usuario }
        if (excluirId != null) {
            query.andWhere { Empleados.id neq excluirId }
        }
        query.any()
    }

}
