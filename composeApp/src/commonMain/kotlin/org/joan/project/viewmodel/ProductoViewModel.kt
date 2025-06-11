package org.joan.project.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.joan.project.db.entidades.ProductoRequest
import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.service.ProductoService

class ProductoViewModel(private val productoService: ProductoService) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _productos = MutableStateFlow<List<ProductoResponse>>(emptyList())
    val productos: StateFlow<List<ProductoResponse>> = _productos

    fun cargarProductos(token: String) {
        scope.launch {
            try {
                val resultado = productoService.getAllProductos(token)
                println("Recibidos ${resultado.size} productos:")
                resultado.forEach {
                    println("- ${it.nombre} (${it.precio} €)")
                }
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
