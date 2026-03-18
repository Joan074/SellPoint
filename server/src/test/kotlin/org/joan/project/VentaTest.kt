package org.joan.project

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.joan.project.db.Categorias
import org.joan.project.db.Empleados
import org.joan.project.db.Productos
import org.joan.project.db.Proveedores
import org.joan.project.db.entidades.ItemVentaRequest
import org.joan.project.db.entidades.VentaRequest
import org.joan.project.db.entidades.VentaResponse
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VentaTest {

    companion object {
        private var empleadoId = 0
        private var productoId = 0

        @BeforeClass
        @JvmStatic
        fun setup() {
            TestContainerHelper.initOnce()
            transaction {
                empleadoId = Empleados.select { Empleados.usuario eq "admin" }
                    .first()[Empleados.id].value

                val categoriaId = Categorias.selectAll()
                    .firstOrNull()?.get(Categorias.id)?.value
                    ?: Categorias.insert { it[nombre] = "VentaTestCat" }[Categorias.id].value

                val proveedorId = Proveedores.selectAll()
                    .firstOrNull()?.get(Proveedores.id)?.value
                    ?: Proveedores.insert {
                        it[nombre] = "VentaTestProv"
                        it[contactoNombre] = ""
                        it[contactoEmail] = ""
                        it[contactoTelefono] = ""
                        it[direccion] = ""
                    }[Proveedores.id].value

                productoId = Productos.insert {
                    it[nombre] = "Producto Para Venta Test"
                    it[precio] = 5.0.toBigDecimal()
                    it[stock] = 999
                    it[this.categoriaId] = categoriaId
                    it[this.proveedorId] = proveedorId
                    it[activo] = true
                }[Productos.id].value
            }
        }
    }

    @Test
    fun `POST ventas crea venta correctamente`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = client.loginAdmin()

        val response = client.post("/ventas") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                VentaRequest(
                    empleadoId = empleadoId,
                    items = listOf(ItemVentaRequest(productoId = productoId, cantidad = 2)),
                    metodoPago = "EFECTIVO"
                )
            )
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val venta = response.body<VentaResponse>()
        assertEquals("COMPLETADA", venta.estado)
        assertEquals("EFECTIVO", venta.metodoPago)
        assertEquals(1, venta.items.size)
        assertEquals(2, venta.items[0].cantidad)
        assertTrue(venta.subtotal > 0.0)
        assertTrue(venta.total > 0.0)
        assertTrue(venta.numeroTicket != null)
    }

    @Test
    fun `subtotal y total de venta se calculan con iva`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = client.loginAdmin()

        val response = client.post("/ventas") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                VentaRequest(
                    empleadoId = empleadoId,
                    items = listOf(ItemVentaRequest(productoId = productoId, cantidad = 1)),
                    metodoPago = "TARJETA"
                )
            )
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val venta = response.body<VentaResponse>()
        // subtotal = 5.0, iva = 5.0 * 0.16 = 0.80, total = 5.80
        assertEquals(5.0, venta.subtotal)
        assertTrue(venta.iva > 0.0)
        assertEquals(venta.subtotal + venta.iva, venta.total, absoluteTolerance = 0.01)
    }

    @Test
    fun `GET ventas devuelve lista`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = client.loginAdmin()

        val response = client.get("/ventas") {
            header("Authorization", "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET ventas sin token devuelve 401`() = testApplication {
        application { module() }
        val client = jsonClient()

        val response = client.get("/ventas")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
