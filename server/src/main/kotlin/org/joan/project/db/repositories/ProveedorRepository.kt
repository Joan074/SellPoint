package org.joan.project.db.repositories

import org.joan.project.db.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.joan.project.db.Proveedores
import org.joan.project.db.entidades.ProveedorRequest
import org.joan.project.db.entidades.ProveedorResponse

class ProveedorRepository {

    suspend fun getAll(): List<ProveedorResponse> = dbQuery {
        Proveedores.select { Proveedores.activo eq true }.map { row ->
            ProveedorResponse(
                id = row[Proveedores.id].value,
                nombre = row[Proveedores.nombre],
                contactoNombre = row[Proveedores.contactoNombre],
                contactoEmail = row[Proveedores.contactoEmail],
                contactoTelefono = row[Proveedores.contactoTelefono],
                direccion = row[Proveedores.direccion]
            )
        }
    }

    suspend fun getById(id: Int): ProveedorResponse? = dbQuery {
        Proveedores.select { (Proveedores.id eq id) and (Proveedores.activo eq true) }
            .map { row ->
                ProveedorResponse(
                    id = row[Proveedores.id].value,
                    nombre = row[Proveedores.nombre],
                    contactoNombre = row[Proveedores.contactoNombre],
                    contactoEmail = row[Proveedores.contactoEmail],
                    contactoTelefono = row[Proveedores.contactoTelefono],
                    direccion = row[Proveedores.direccion]
                )
            }.singleOrNull()
    }

    suspend fun crearProveedor(request: ProveedorRequest): ProveedorResponse = dbQuery {
        val insert = Proveedores.insert {
            it[nombre] = request.nombre
            it[contactoNombre] = request.contactoNombre
            it[contactoEmail] = request.contactoEmail
            it[contactoTelefono] = request.contactoTelefono
            it[direccion] = request.direccion
            it[activo] = true
        }

        ProveedorResponse(
            id = insert[Proveedores.id].value,
            nombre = insert[Proveedores.nombre],
            contactoNombre = insert[Proveedores.contactoNombre],
            contactoEmail = insert[Proveedores.contactoEmail],
            contactoTelefono = insert[Proveedores.contactoTelefono],
            direccion = insert[Proveedores.direccion]
        )
    }

    suspend fun actualizarProveedor(id: Int, request: ProveedorRequest): Boolean = dbQuery {
        val filas = Proveedores.update({ Proveedores.id eq id }) {
            it[nombre] = request.nombre
            it[contactoNombre] = request.contactoNombre
            it[contactoEmail] = request.contactoEmail
            it[contactoTelefono] = request.contactoTelefono
            it[direccion] = request.direccion
        }
        filas > 0
    }

    suspend fun desactivarProveedor(id: Int): Boolean = dbQuery {
        Proveedores.update({ Proveedores.id eq id }) {
            it[activo] = false
        } > 0
    }

    suspend fun eliminarProveedor(id: Int): Boolean = desactivarProveedor(id)
}
