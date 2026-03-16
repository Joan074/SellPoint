package org.joan.project.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.joan.project.db.entidades.ProveedorRequest
import org.joan.project.db.entidades.ProveedorResponse
import org.joan.project.service.ProveedorService

class ProveedorViewModel(
    private val proveedorService: ProveedorService
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _proveedores = MutableStateFlow<List<ProveedorResponse>>(emptyList())
    val proveedores: StateFlow<List<ProveedorResponse>> = _proveedores

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Notas en memoria por proveedor
    private val _notas = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val notas: StateFlow<Map<Int, List<String>>> = _notas

    fun cargarProveedores(token: String) {
        scope.launch {
            _cargando.value = true
            _error.value = null
            try {
                _proveedores.value = proveedorService
                    .getAllProveedores(token)
                    .sortedBy { it.nombre.lowercase() }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _cargando.value = false
            }
        }
    }

    suspend fun crearProveedor(token: String, request: ProveedorRequest): ProveedorResponse {
        val creado = proveedorService.crearProveedor(token, request)
        _proveedores.value = (_proveedores.value + creado).sortedBy { it.nombre.lowercase() }
        return creado
    }



    fun actualizarNota(token: String, id: Int, nota: String?, onError: (String)->Unit = {}, onSuccess: ()->Unit = {}) {
        scope.launch {
            try {
                val actualizado = proveedorService.actualizarNota(token, id, nota)
                _proveedores.value = _proveedores.value.map { if (it.id == id) actualizado else it }
                onSuccess()
            } catch (e: Exception) { onError(e.message ?: "Error al actualizar nota") }
        }
    }

    fun borrarNota(token: String, id: Int, onError: (String)->Unit = {}, onSuccess: ()->Unit = {}) {
        scope.launch {
            try {
                val actualizado = proveedorService.borrarNota(token, id)
                _proveedores.value = _proveedores.value.map { if (it.id == id) actualizado else it }
                onSuccess()
            } catch (e: Exception) { onError(e.message ?: "Error al borrar nota") }
        }
    }


    fun actualizarProveedor(token: String, id: Int, request: ProveedorRequest) {
        scope.launch {
            try {
                val actualizado = proveedorService.actualizarProveedor(token, id, request)
                _proveedores.value = _proveedores.value.map { if (it.id == id) actualizado else it }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }


    fun eliminarProveedor(token: String, id: Int) {
        scope.launch {
            try {
                proveedorService.eliminarProveedor(token, id)
                _proveedores.value = _proveedores.value.filterNot { it.id == id }
                // opcional: limpiar sus notas
                _notas.value = _notas.value.toMutableMap().apply { remove(id) }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
