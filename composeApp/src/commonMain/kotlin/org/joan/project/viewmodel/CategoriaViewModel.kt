package org.joan.project.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.joan.project.db.entidades.CategoriaRequest
import org.joan.project.db.entidades.CategoriaResponse
import org.joan.project.service.CategoriaService

class CategoriaViewModel(
    private val categoriaService: CategoriaService
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _categorias = MutableStateFlow<List<CategoriaResponse>>(emptyList())
    val categorias: StateFlow<List<CategoriaResponse>> = _categorias

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun cargarCategorias(token: String) {
        if (token == DEMO_TOKEN) {
            _categorias.value = CATEGORIAS_DEMO.sortedBy { it.nombre.lowercase() }
            _cargando.value = false
            return
        }
        scope.launch {
            _cargando.value = true
            _error.value = null
            try {
                _categorias.value = categoriaService
                    .getAllCategorias(token)
                    .sortedBy { it.nombre.lowercase() }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _cargando.value = false
            }
        }
    }

    suspend fun crearCategoria(token: String, request: CategoriaRequest): CategoriaResponse {
        val creada = categoriaService.crearCategoria(token, request)
        _categorias.value = (_categorias.value + creada).sortedBy { it.nombre.lowercase() }
        return creada
    }
}
