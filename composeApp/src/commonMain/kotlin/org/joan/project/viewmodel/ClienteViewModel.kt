package org.joan.project.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.joan.project.db.entidades.ClienteResponse

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
