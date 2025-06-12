package org.joan.project.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.joan.project.db.entidades.VentaRequest
import org.joan.project.db.entidades.VentaResponse
import org.joan.project.service.VentaService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class VentaViewModel(
    private val ventaService: VentaService
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _ventas = MutableStateFlow<List<VentaResponse>>(emptyList())
    val ventas: StateFlow<List<VentaResponse>> = _ventas

    /**
     * Cargar ventas entre dos fechas y actualizar el estado interno
     */
    fun cargarVentasEntreFechas(
        token: String,
        desde: LocalDate,
        hasta: LocalDate,
        onError: (String) -> Unit = {},
        onSuccess: () -> Unit = {}
    ) {
        scope.launch {
            try {
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                val desdeStr = desde.atStartOfDay().format(formatter)
                val hastaStr = hasta.atTime(23, 59, 59).format(formatter)

                val resultado = ventaService.getVentasEntreFechas(token, desdeStr, hastaStr)
                _ventas.value = resultado
                onSuccess()
            } catch (e: Exception) {
                println("Error al cargar ventas: ${e.message}")
                onError(e.message ?: "Error al cargar ventas")
            }
        }
    }


    /**
     * Crear una nueva venta
     */
    fun crearVenta(
        token: String,
        ventaRequest: VentaRequest,
        onSuccess: (VentaResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                val nuevaVenta = ventaService.crearVenta(token, ventaRequest)
                _ventas.value = _ventas.value + listOf(nuevaVenta)
                onSuccess(nuevaVenta)
            } catch (e: Exception) {
                onError(e.message ?: "Error al crear la venta")
            }
        }
    }

    /**
     * Anular una venta
     */
    fun anularVenta(
        token: String,
        ventaId: Int,
        onSuccess: (VentaResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                val anulada = ventaService.anularVenta(token, ventaId)
                _ventas.value = _ventas.value.map { if (it.id == ventaId) anulada else it }
                onSuccess(anulada)
            } catch (e: Exception) {
                onError(e.message ?: "Error al anular la venta")
            }
        }
    }

    /**
     * Limpiar ventas del estado
     */
    fun limpiarVentas() {
        _ventas.value = emptyList()
    }
}
