package org.joan.project.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.joan.project.db.entidades.ClienteResponse

private const val DEMO_TOKEN = "token-demo-offline"

private val CLIENTES_DEMO = listOf(
    ClienteResponse(id = 1, nombre = "María García",    telefono = "612 345 678"),
    ClienteResponse(id = 2, nombre = "Juan Martínez",   telefono = "623 456 789"),
    ClienteResponse(id = 3, nombre = "Ana López",       telefono = null),
)

class ClienteViewModel {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _clientes = MutableStateFlow<List<ClienteResponse>>(emptyList())
    val clientes: StateFlow<List<ClienteResponse>> = _clientes

    fun cargarClientes(token: String) {
        if (token == DEMO_TOKEN) {
            _clientes.value = CLIENTES_DEMO
            return
        }
        // TODO: implementar llamada real al servicio de clientes
    }
}
