package org.joan.project.db.repositories

import org.joan.project.db.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.joan.project.db.Categorias
import org.joan.project.db.Productos
import org.joan.project.db.entidades.CategoriaResponse

class CategoriaRepository {
    suspend fun getAll(): List<CategoriaResponse> = dbQuery {
        Categorias.selectAll().map { row ->
            CategoriaResponse(
                id = row[Categorias.id].value,
                nombre = row[Categorias.nombre],
                cantidadProductos = Productos.select { Productos.categoriaId eq row[Categorias.id] }.count()
            )
        }
    }

    suspend fun getById(id: Int): CategoriaResponse? = dbQuery {
        Categorias.select { Categorias.id eq id }
            .map { row ->
                CategoriaResponse(
                    id = row[Categorias.id].value,
                    nombre = row[Categorias.nombre],
                    cantidadProductos = Productos.select { Productos.categoriaId eq row[Categorias.id] }.count()
                )
            }.singleOrNull()
    }

    suspend fun crearCategoria(nombre: String): CategoriaResponse = dbQuery {
        val insert = Categorias.insert {
            it[Categorias.nombre] = nombre
        }
        CategoriaResponse(
            id = insert[Categorias.id].value,
            nombre = insert[Categorias.nombre]
        )
    }

    suspend fun actualizarCategoria(id: Int, nuevoNombre: String): Boolean = dbQuery {
        Categorias.update({ Categorias.id eq id }) {
            it[nombre] = nuevoNombre
        } > 0
    }


    suspend fun eliminarCategoria(id: Int): Boolean = dbQuery {
        val productosConCategoria = Productos.select { Productos.categoriaId eq id }.count()
        if (productosConCategoria > 0) return@dbQuery false

        Categorias.deleteWhere { Categorias.id eq id } > 0
    }


}