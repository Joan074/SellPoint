package org.joan.project.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.joan.project.db.entidades.*
import org.joan.project.service.VentaService

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
        if (token == DEMO_TOKEN) {
            val desdeStr = LocalDateTime(desde, LocalTime(0, 0, 0)).toString()
            val hastaStr = LocalDateTime(hasta, LocalTime(23, 59, 59)).toString()
            _ventas.value = VENTAS_DEMO.filter { it.fecha >= desdeStr && it.fecha <= hastaStr }
            onSuccess()
            return
        }
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
            val items = ventaRequest.items.mapNotNull { req ->
                val prod = PRODUCTOS_DEMO.find { it.id == req.productoId } ?: return@mapNotNull null
                val precio = req.precioEspecial ?: prod.precio
                ItemVentaResponse(
                    productoId     = prod.id,
                    codigoBarras   = prod.codigoBarras ?: "",
                    nombre         = prod.nombre,
                    cantidad       = req.cantidad,
                    precioUnitario = precio,
                    descuento      = 0.0,
                    subtotal       = precio * req.cantidad
                )
            }
            val subtotal = items.sumOf { it.subtotal }
            val iva      = subtotal * 0.21
            val now      = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val dateTag  = now.date.toString().replace("-", "")
            val demo = VentaResponse(
                id           = (System.currentTimeMillis() % 100000).toInt(),
                cliente      = null,
                empleado     = EmpleadoSimpleResponse(0, "Demo"),
                fecha        = now.toString(),
                subtotal     = subtotal,
                iva          = iva,
                total        = subtotal + iva,
                estado       = "COMPLETADA",
                metodoPago   = ventaRequest.metodoPago,
                items        = items,
                numeroTicket = "T-$dateTag-${(System.currentTimeMillis() % 9000 + 1000).toInt()}"
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
