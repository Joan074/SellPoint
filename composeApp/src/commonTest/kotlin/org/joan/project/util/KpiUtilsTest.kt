package org.joan.project.util

import org.joan.project.db.entidades.CategoriaSimpleResponse
import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.db.entidades.ProveedorSimpleResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class KpiUtilsTest {

    private val cat = CategoriaSimpleResponse(1, "Panadería")
    private val prov = ProveedorSimpleResponse(1, "Proveedor")

    private fun producto(precio: Double, stock: Int, id: Int = 1) = ProductoResponse(
        id = id,
        nombre = "Producto $id",
        precio = precio,
        stock = stock,
        codigoBarras = null,
        categoria = cat,
        proveedor = prov
    )

    @Test
    fun `valorInventario suma precio por stock de cada producto`() {
        val productos = listOf(
            producto(precio = 10.0, stock = 5),  // 50.0
            producto(precio = 2.50, stock = 4)   // 10.0
        )
        assertEquals(60.0, KpiUtils.valorInventario(productos))
    }

    @Test
    fun `valorInventario de lista vacia es cero`() {
        assertEquals(0.0, KpiUtils.valorInventario(emptyList()))
    }

    @Test
    fun `valorInventario con stock cero aporta cero`() {
        val productos = listOf(
            producto(precio = 5.0, stock = 0),
            producto(precio = 3.0, stock = 2)
        )
        assertEquals(6.0, KpiUtils.valorInventario(productos))
    }

    @Test
    fun `totalProductos cuenta todos los elementos`() {
        val productos = listOf(
            producto(1.0, 1),
            producto(2.0, 2),
            producto(3.0, 3)
        )
        assertEquals(3, KpiUtils.totalProductos(productos))
    }

    @Test
    fun `totalProductos de lista vacia es cero`() {
        assertEquals(0, KpiUtils.totalProductos(emptyList()))
    }

    @Test
    fun `bajoStock con umbral por defecto cuenta stock menor que 10`() {
        val productos = listOf(
            producto(1.0, 9),   // bajo stock
            producto(2.0, 10),  // justo en el umbral → NO bajo
            producto(3.0, 3)    // bajo stock
        )
        assertEquals(2, KpiUtils.bajoStock(productos))
    }

    @Test
    fun `bajoStock con umbral personalizado`() {
        val productos = listOf(
            producto(1.0, 5),
            producto(2.0, 20),
            producto(3.0, 1)
        )
        assertEquals(2, KpiUtils.bajoStock(productos, umbral = 10))
    }

    @Test
    fun `bajoStock con todos sobre el umbral devuelve cero`() {
        val productos = listOf(
            producto(1.0, 50),
            producto(2.0, 100)
        )
        assertEquals(0, KpiUtils.bajoStock(productos, umbral = 10))
    }
}
