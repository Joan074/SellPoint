package org.joan.project.db.entidades

import kotlinx.serialization.Serializable

// ClienteResponse.kt
@Serializable
data class ClienteResponse(
    val id: Int,
    val nombre: String,
    val telefono: String?
)

@Serializable
data class ProductoResponse(
    val id: Int,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val codigoBarras: String?,
    val categoria: CategoriaSimpleResponse,
    val proveedor: ProveedorSimpleResponse
)

// VentaResponse.kt (Versión mejorada)
@Serializable
data class VentaResponse(
    val id: Int,
    val cliente: ClienteSimpleResponse?, // Hacerlo nullable
    val empleado: EmpleadoSimpleResponse,
    val fecha: String,
    val subtotal: Double,
    val iva: Double,
    val total: Double,
    val estado: String,
    val metodoPago: String,
    val items: List<ItemVentaResponse>,
    val numeroTicket: String? = null
)

@Serializable
data class ItemVentaResponse(
    val productoId: Int,
    val codigoBarras: String,
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val descuento: Double,
    val subtotal: Double,
    val promocionId: Int? = null
)


// TicketResponse.kt (Versión mejorada)
@Serializable
data class TicketResponse(
    val numeroTicket: String,
    val fecha: String,
    val cliente: String?,
    val items: List<ItemTicketResponse>,
    val subtotal: Double,
    val iva: Double,
    val descuento: Double,
    val total: Double,
    val metodoPago: String,
    val codigoQR: String? = null // Para devoluciones
)

@Serializable
data class ItemTicketResponse(
    val codigo: String,
    val descripcion: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val importe: Double
)

@Serializable
data class ProveedorResponse(
    val id: Int,
    val nombre: String,
    val contactoNombre: String,
    val contactoEmail: String,
    val contactoTelefono: String,
    val direccion: String,
    val productosSuministrados: Int = 0, // Puede calcularse
    val activo: Boolean = true
)

@Serializable
data class CategoriaResponse(
    val id: Int,
    val nombre: String,
    val cantidadProductos: Long = 0 // Puede calcularse
)

// Modelos simples para relaciones
@Serializable
data class ClienteSimpleResponse(
    val id: Int,
    val nombre: String
)

@Serializable
data class EmpleadoSimpleResponse(
    val id: Int,
    val nombre: String
)

@Serializable
data class CategoriaSimpleResponse(
    val id: Int,
    val nombre: String
)

@Serializable
data class ProveedorSimpleResponse(
    val id: Int,
    val nombre: String
)

@Serializable
data class ProductoSimpleResponse(
    val id: Int,
    val nombre: String,
    val precio: Double
)

// Reportes y estadísticas
@Serializable
data class ReporteDiarioResponse(
    val fecha: String,
    val totalVentas: Double,
    val cantidadVentas: Int,
    val ventas: List<VentaSimpleResponse>,
    val productosMasVendidos: List<ProductoVendidoResponse>,
    val metodoPagos: Map<String, Double> // {"EFECTIVO": 1000.0, "TARJETA": 500.0}
)

@Serializable
data class VentaSimpleResponse(
    val id: Int,
    val total: Double,
    val fecha: String,
    val metodoPago: String
)

@Serializable
data class ProductoVendidoResponse(
    val productoId: Int,
    val nombre: String,
    val cantidadVendida: Int,
    val totalVendido: Double
)

@Serializable
data class VentaDetalladaResponse(
    val id: Int,
    val cliente: ClienteSimpleResponse,
    val empleado: EmpleadoSimpleResponse,
    val fecha: String,
    val subtotal: Double,
    val iva: Double,
    val descuento: Double,
    val total: Double,
    val metodoPago: String,
    val estado: String, // "COMPLETADA", "ANULADA", "PENDIENTE"
    val items: List<ItemVentaResponse>,
    val numeroTicket: String? = null
)