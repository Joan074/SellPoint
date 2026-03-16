package org.joan.project.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.joan.project.db.entidades.CategoriaSimpleResponse
import org.joan.project.db.entidades.ProductoRequest
import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.db.entidades.ProveedorSimpleResponse
import org.joan.project.service.ProductoService

private const val DEMO_TOKEN = "token-demo-offline"

private val PROVEEDOR_DEMO = ProveedorSimpleResponse(id = 0, nombre = "Demo")

private val PRODUCTOS_DEMO = listOf(
    ProductoResponse(id = 1,  nombre = "Pan Rústico",       precio = 1.50, stock = 20, codigoBarras = null, categoria = CategoriaSimpleResponse(1, "Panadería"),    proveedor = PROVEEDOR_DEMO),
    ProductoResponse(id = 2,  nombre = "Coca Cola 33cl",    precio = 1.20, stock = 15, codigoBarras = null, categoria = CategoriaSimpleResponse(2, "Bebidas"),      proveedor = PROVEEDOR_DEMO),
    ProductoResponse(id = 3,  nombre = "Cerveza Estrella",  precio = 1.50, stock = 30, codigoBarras = null, categoria = CategoriaSimpleResponse(2, "Bebidas"),      proveedor = PROVEEDOR_DEMO),
    ProductoResponse(id = 4,  nombre = "Jamón Serrano",     precio = 8.50, stock = 10, codigoBarras = null, categoria = CategoriaSimpleResponse(3, "Charcutería"), proveedor = PROVEEDOR_DEMO),
    ProductoResponse(id = 5,  nombre = "Queso Manchego",    precio = 6.00, stock =  8, codigoBarras = null, categoria = CategoriaSimpleResponse(3, "Charcutería"), proveedor = PROVEEDOR_DEMO),
    ProductoResponse(id = 6,  nombre = "Agua Mineral 1.5L", precio = 0.80, stock = 25, codigoBarras = null, categoria = CategoriaSimpleResponse(2, "Bebidas"),      proveedor = PROVEEDOR_DEMO),
    ProductoResponse(id = 7,  nombre = "Patatas Fritas",    precio = 1.20, stock = 18, codigoBarras = null, categoria = CategoriaSimpleResponse(4, "Snacks"),       proveedor = PROVEEDOR_DEMO),
    ProductoResponse(id = 8,  nombre = "Chocolate Milka",   precio = 1.80, stock = 12, codigoBarras = null, categoria = CategoriaSimpleResponse(4, "Snacks"),       proveedor = PROVEEDOR_DEMO),
)

class ProductoViewModel(private val productoService: ProductoService) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _productos = MutableStateFlow<List<ProductoResponse>>(emptyList())
    val productos: StateFlow<List<ProductoResponse>> = _productos

    fun cargarProductos(token: String) {
        if (token == DEMO_TOKEN) {
            _productos.value = PRODUCTOS_DEMO
            return
        }
        scope.launch {
            try {
                val resultado = productoService.getAllProductos(token)
                _productos.value = resultado
            } catch (e: Exception) {
                println("Error al cargar productos: ${e.message}")
            }
        }
    }

    fun crearProducto(request: ProductoRequest, token: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        scope.launch {
            try {
                val creado = productoService.crearProducto(token, request)
                _productos.value = _productos.value + creado
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al crear el producto")
            }
        }
    }

    fun actualizarProducto(
        id: Int,
        request: ProductoRequest,
        token: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                val actualizado = productoService.actualizarProducto(id, token, request)
                // Reemplaza el producto en la lista
                _productos.value = _productos.value.map {
                    if (it.id == id) actualizado else it
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al actualizar el producto")
            }
        }
    }
    fun eliminarProducto(
        id: Int,
        token: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                productoService.eliminarProducto(id, token)
                _productos.value = _productos.value.filterNot { it.id == id }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al eliminar el producto")
            }
        }
    }







}
