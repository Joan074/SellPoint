package org.joan.project.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joan.project.db.entidades.ItemVentaRequest
import org.joan.project.db.entidades.VentaRequest
import org.joan.project.service.VentaService

class VentaViewModel(
    private val ventaService: VentaService
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun crearVenta(
        token: String,
        ventaRequest: VentaRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                ventaService.crearVenta(token, ventaRequest)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al registrar la venta")
            }
        }
    }
}
