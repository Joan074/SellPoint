package org.joan.project.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.joan.project.db.entidades.EmpleadoSimpleResponse
import org.joan.project.db.entidades.VentaRequest
import org.joan.project.db.entidades.VentaResponse
import org.joan.project.service.VentaService

private const val DEMO_TOKEN = "token-demo-offline"

class VentaViewModel(
    private val ventaService: VentaService
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _ventas = MutableStateFlow<List<VentaResponse>>(emptyList())
    val ventas: StateFlow<List<VentaResponse>> = _ventas

    fun cargarVentasEntreFechas(
        token: String,
        desde: LocalDate,
        hasta: LocalDate,
        onError: (String) -> Unit = {},
        onSuccess: () -> Unit = {}
    ) {
        if (token == DEMO_TOKEN) { _ventas.value = emptyList(); onSuccess(); return }
        scope.launch {
            try {
                val desdeStr = LocalDateTime(desde, LocalTime(0, 0, 0)).toString()
                val hastaStr = LocalDateTime(hasta, LocalTime(23, 59, 59)).toString()
                _ventas.value = ventaService.getVentasEntreFechas(token, desdeStr, hastaStr)
                onSuccess()
            } catch (e: Exception) {
                println("Error al cargar ventas: ${e.message}")
                onError(e.message ?: "Error al cargar ventas")
            }
        }
    }

    fun crearVenta(
        token: String,
        ventaRequest: VentaRequest,
        onSuccess: (VentaResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        if (token == DEMO_TOKEN) {
            val demo = VentaResponse(
                id = 0,
                cliente = null,
                empleado = EmpleadoSimpleResponse(id = 0, nombre = "Demo"),
                fecha = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString(),
                subtotal = 0.0, iva = 0.0, total = 0.0,
                estado = "COMPLETADA",
                metodoPago = ventaRequest.metodoPago,
                items = emptyList(),
                numeroTicket = "DEMO-${System.currentTimeMillis()}"
            )
            onSuccess(demo)
            return
        }
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

    fun limpiarVentas() { _ventas.value = emptyList() }
}
