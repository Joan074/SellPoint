package org.joan.project

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.joan.project.db.Categorias
import org.joan.project.db.Proveedores
import org.joan.project.db.entidades.ProductoRequest
import org.joan.project.db.entidades.ProductoResponse
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductoTest {

    companion object {
        private var categoriaId = 0
        private var proveedorId = 0

        @BeforeClass
        @JvmStatic
        fun setup() {
            TestContainerHelper.initOnce()
            transaction {
                categoriaId = Categorias.selectAll()
                    .firstOrNull()?.get(Categorias.id)?.value
                    ?: Categorias.insert { it[nombre] = "TestCat" }[Categorias.id].value

                proveedorId = Proveedores.selectAll()
                    .firstOrNull()?.get(Proveedores.id)?.value
                    ?: Proveedores.insert {
                        it[nombre] = "TestProv"
                        it[contactoNombre] = ""
                        it[contactoEmail] = ""
                        it[contactoTelefono] = ""
                        it[direccion] = ""
                    }[Proveedores.id].value
            }
        }
    }

    @Test
    fun `GET producto con token valido devuelve lista`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = client.loginAdmin()
        val response = client.get("/producto") {
            header("Authorization", "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST producto crea nuevo producto y devuelve 201`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = client.loginAdmin()

        val nombre = "Pan Test ${System.currentTimeMillis()}"
        val response = client.post("/producto") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                ProductoRequest(
                    nombre = nombre,
                    precio = 1.20,
                    stock = 10,
                    categoriaId = categoriaId,
                    proveedorId = proveedorId
                )
            )
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val producto = response.body<ProductoResponse>()
        assertEquals(nombre, producto.nombre)
        assertEquals(1.20, producto.precio)
        assertEquals(10, producto.stock)
    }

    @Test
    fun `PUT producto actualiza nombre y precio`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = client.loginAdmin()

        val creado = client.post("/producto") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                ProductoRequest(
                    nombre = "Original ${System.currentTimeMillis()}",
                    precio = 1.0,
                    stock = 5,
                    categoriaId = categoriaId,
                    proveedorId = proveedorId
                )
            )
        }.body<ProductoResponse>()

        val updated = client.put("/producto/${creado.id}") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                ProductoRequest(
                    nombre = "Actualizado",
                    precio = 3.50,
                    stock = 20,
                    categoriaId = categoriaId,
                    proveedorId = proveedorId
                )
            )
        }
        assertEquals(HttpStatusCode.OK, updated.status)
        val updatedProducto = updated.body<ProductoResponse>()
        assertEquals("Actualizado", updatedProducto.nombre)
        assertEquals(3.50, updatedProducto.precio)
        assertEquals(20, updatedProducto.stock)
    }

    @Test
    fun `DELETE producto lo desactiva`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = client.loginAdmin()

        val creado = client.post("/producto") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                ProductoRequest(
                    nombre = "Para Borrar ${System.currentTimeMillis()}",
                    precio = 0.50,
                    stock = 1,
                    categoriaId = categoriaId,
                    proveedorId = proveedorId
                )
            )
        }.body<ProductoResponse>()

        val deleted = client.delete("/producto/${creado.id}") {
            header("Authorization", "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, deleted.status)

        // Verificar que ya no aparece en el listado
        val getDeleted = client.get("/producto/${creado.id}") {
            header("Authorization", "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, getDeleted.status)
    }

    @Test
    fun `PUT producto sin codigoBarras no devuelve 500`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = client.loginAdmin()

        val creado = client.post("/producto") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                ProductoRequest(
                    nombre = "Sin Codigo ${System.currentTimeMillis()}",
                    precio = 2.0,
                    stock = 5,
                    categoriaId = categoriaId,
                    proveedorId = proveedorId,
                    codigoBarras = null
                )
            )
        }.body<ProductoResponse>()

        val response = client.put("/producto/${creado.id}") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                ProductoRequest(
                    nombre = "Sin Codigo Editado",
                    precio = 2.50,
                    stock = 8,
                    categoriaId = categoriaId,
                    proveedorId = proveedorId,
                    codigoBarras = null
                )
            )
        }
        assertTrue(response.status != HttpStatusCode.InternalServerError,
            "PUT con codigoBarras=null no debe devolver 500")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
