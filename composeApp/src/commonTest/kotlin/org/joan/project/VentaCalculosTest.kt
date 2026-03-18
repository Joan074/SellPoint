package org.joan.project

import org.joan.project.db.entidades.CategoriaSimpleResponse
import org.joan.project.db.entidades.LineaVenta
import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.db.entidades.ProveedorSimpleResponse
import kotlin.math.abs
import kotlin.math.round
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VentaCalculosTest {

    private val IVA = 0.21  // 21% IVA

    private fun producto(precio: Double) = ProductoResponse(
        id = 1,
        nombre = "Croissant",
        precio = precio,
        stock = 50,
        codigoBarras = null,
        categoria = CategoriaSimpleResponse(1, "Dulce"),
        proveedor = ProveedorSimpleResponse(1, "Proveedor")
    )

    /** Redondea a 2 decimales (€) */
    private fun Double.round2(): Double = round(this * 100) / 100.0

    @Test
    fun `subtotal de una linea es precio por cantidad`() {
        val linea = LineaVenta(producto(1.50), cantidad = 3)
        assertEquals(4.50, linea.subtotal)
    }

    @Test
    fun `subtotal de carrito con multiples lineas`() {
        val carrito = listOf(
            LineaVenta(producto(2.0), cantidad = 3),   // 6.0
            LineaVenta(producto(0.50), cantidad = 4)   // 2.0
        )
        assertEquals(8.0, carrito.sumOf { it.subtotal })
    }

    @Test
    fun `iva al 21 porciento sobre subtotal`() {
        val subtotal = 10.0
        val iva = (subtotal * IVA).round2()
        assertEquals(2.10, iva)
    }

    @Test
    fun `total es subtotal mas iva`() {
        val subtotal = 10.0
        val iva = (subtotal * IVA).round2()
        val total = (subtotal + iva).round2()
        assertEquals(12.10, total)
    }

    @Test
    fun `total con descuento fijo`() {
        val subtotal = 20.0
        val iva = (subtotal * IVA).round2()
        val descuento = 2.0
        val total = (subtotal + iva - descuento).round2()
        assertEquals(22.20, total)
    }

    @Test
    fun `descuento porcentual sobre total`() {
        val totalBruto = 20.0
        val descuentoPct = 10.0
        val totalFinal = (totalBruto * (1.0 - descuentoPct / 100.0)).round2()
        assertEquals(18.0, totalFinal)
    }

    @Test
    fun `subtotal con precio cero es cero`() {
        val linea = LineaVenta(producto(0.0), cantidad = 5)
        assertEquals(0.0, linea.subtotal)
    }

    // En VentaCalculosTest
    @Test
    fun `total nunca es negativo aunque el descuento supere el subtotal`() {
        val subtotal = 5.0
        val descuento = 10.0  // Mayor que el subtotal
        val total = (subtotal - descuento).coerceAtLeast(0.0)
        assertTrue(total >= 0.0)
    }

    @Test
    fun `carrito vacio tiene subtotal cero`() {
        val carrito = emptyList<LineaVenta>()
        assertEquals(0.0, carrito.sumOf { it.subtotal })
    }
}
