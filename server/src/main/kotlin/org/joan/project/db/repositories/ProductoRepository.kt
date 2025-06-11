package org.joan.project.db.repositories

import org.joan.project.db.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.joan.project.db.Categorias
import org.joan.project.db.Productos
import org.joan.project.db.Proveedores
import org.joan.project.db.entidades.*

class ProductoRepository {
    suspend fun getAll(): List<ProductoResponse> = dbQuery {
        (Productos innerJoin Categorias innerJoin Proveedores)
            .selectAll()
            .map { toProductoResponse(it) }
    }

    suspend fun getById(id: Int): ProductoResponse? = dbQuery {
        (Productos.innerJoin(Categorias).innerJoin(Proveedores))
            .select { Productos.id eq id }
            .map { toProductoResponse(it) }
            .singleOrNull()
    }

    suspend fun create(productoRequest: ProductoRequest): ProductoResponse = dbQuery {
        val insert = Productos.insert {
            it[nombre] = productoRequest.nombre
            it[precio] = productoRequest.precio.toBigDecimal()
            it[stock] = productoRequest.stock
            it[categoriaId] = productoRequest.categoriaId
            it[proveedorId] = productoRequest.proveedorId
            productoRequest.codigoBarras?.let { cb -> it[codigoBarras] = cb }
            productoRequest.imagenUrl?.let { url -> it[Productos.imagenUrl] = url } // ← AÑADIDO
        }


        // Necesitamos hacer join para obtener nombres de categoría/proveedor
        (Productos.innerJoin(Categorias).innerJoin(Proveedores))
            .select { Productos.id eq insert[Productos.id] }
            .map { toProductoResponse(it) }
            .single()
    }

    suspend fun update(id: Int, productoRequest: ProductoRequest): Boolean = dbQuery {
        // Verificamos si ya existe otro producto con el mismo código de barras
        val otroProductoConMismoCodigo = Productos.select {
            (Productos.codigoBarras eq productoRequest.codigoBarras) and
                    (Productos.id neq id)
        }.count() > 0

        if (otroProductoConMismoCodigo) {
            throw IllegalArgumentException("Ese código de barras ya está en uso por otro producto.")
        }

        // Actualización del producto
        val updatedRows = Productos.update({ Productos.id eq id }) {
            it[nombre] = productoRequest.nombre
            it[precio] = productoRequest.precio.toBigDecimal()
            it[stock] = productoRequest.stock
            it[categoriaId] = productoRequest.categoriaId
            it[proveedorId] = productoRequest.proveedorId
            productoRequest.codigoBarras?.let { cb -> it[codigoBarras] = cb }
            productoRequest.imagenUrl?.let { url -> it[imagenUrl] = url } // ← AÑADIDO
        }


        updatedRows > 0
    }


    suspend fun sumarStock(id: Int, cantidad: Int): Boolean = dbQuery {
        Productos.update({ Productos.id eq id }) {
            with(SqlExpressionBuilder) {
                it.update(stock, stock + cantidad)
            }
        } > 0
    }


    suspend fun restarStock(id: Int, cantidad: Int): Boolean = dbQuery {
        Productos.update({ Productos.id eq id }) {
            with(SqlExpressionBuilder) {
                it.update(stock, stock - cantidad)
            }
        } > 0
    }




    suspend fun actualizarStock(id: Int, nuevoStock: Int): Boolean = dbQuery {
        Productos.update({ Productos.id eq id }) {
            it[stock] = nuevoStock
        } > 0
    }


    suspend fun buscarProductosPorNombre(term: String): List<ProductoResponse> = dbQuery {
        (Productos.innerJoin(Categorias).innerJoin(Proveedores))
            .select { Productos.nombre like "%${term}%" }
            .map { toProductoResponse(it) }
    }

    suspend fun actualizarPrecio(id: Int, nuevoPrecio: Double): Boolean = dbQuery {
        Productos.update({ Productos.id eq id }) {
            it[precio] = nuevoPrecio.toBigDecimal()
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        Productos.deleteWhere { Productos.id eq id } > 0
    }

    private fun toProductoResponse(row: ResultRow): ProductoResponse {
        return ProductoResponse(
            id = row[Productos.id].value,
            nombre = row[Productos.nombre],
            precio = row[Productos.precio].toDouble(),
            stock = row[Productos.stock],
            codigoBarras = row[Productos.codigoBarras],
            imagenUrl = row[Productos.imagenUrl], // ← AÑADIDO
            categoria = CategoriaSimpleResponse(
                row[Categorias.id].value,
                row[Categorias.nombre]
            ),
            proveedor = ProveedorSimpleResponse(
                row[Proveedores.id].value,
                row[Proveedores.nombre]
            )
        )
    }



}