package org.joan.project.db.entidades

import kotlinx.serialization.Serializable

// ClienteRequest.kt
@Serializable
data class ClienteRequest(
    val nombre: String,
    val telefono: String?
)

// EmpleadoLoginRequest.kt
@Serializable
data class EmpleadoLoginRequest(
    val usuario: String,
    val contraseña: String
)

// EmpleadoCreateRequest.kt (Nuevo)
@Serializable
data class EmpleadoCreateRequest(
    val nombre: String,
    val usuario: String,
    val contraseña: String,
    val rol: String
)

@Serializable
data class EmpleadoRequest(
    val nombre: String,
    val usuario: String,
    val contraseña: String? = null,  // Puede ser null en actualización si no se quiere cambiar
    val rol: String                  // Ej: "ADMIN" o "CAJERO"
)

@Serializable
data class ProductoRequest(
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val categoriaId: Int,
    val proveedorId: Int,
    val codigoBarras: String? = null
)


@Serializable
data class VentaRequest(
    val clienteId: Int? = null, // Hacerlo opcional
    val empleadoId: Int, // Obligatorio
    val items: List<ItemVentaRequest>,
    val metodoPago: String,
    val descuento: Double = 0.0
)

@Serializable
data class ItemVentaRequest(
    val productoId: Int,
    val cantidad: Int,
    val precioEspecial: Double? = null,
    val descuento: Double = 0.0,
    val promocionId: Int? = null
)


// CategoriaRequest.kt (Nuevo)
@Serializable
data class CategoriaRequest(
    val nombre: String
)

// ProveedorRequest.kt (Nuevo)
@Serializable
data class ProveedorRequest(
    val nombre: String,
    val contactoNombre: String,
    val contactoEmail: String,
    val contactoTelefono: String,
    val direccion: String
)

@Serializable
data class PrecioUpdateRequest(
    val precio: Double
)
