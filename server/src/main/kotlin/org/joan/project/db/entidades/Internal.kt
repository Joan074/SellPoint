package org.joan.project.db.entidades

import io.ktor.server.auth.*
import kotlinx.serialization.Serializable

// TokenResponse.kt
@Serializable
data class TokenResponse(
    val token: String,
    val expiracion: String,
    val empleado: EmpleadoResponse // Añadido para incluir datos básicos del empleado
)

// ErrorResponse.kt
@Serializable
data class ErrorResponse(
    val codigo: Int,
    val mensaje: String,
    val detalles: String? = null
)

// EmpleadoResponse.kt (Movido aquí por coherencia)
@Serializable
data class EmpleadoResponse(
    val id: Int,
    val nombre: String,
    val usuario: String,
    val rol: String
)

data class EmpleadoPrincipal(
    val id: Int,
    val usuario: String,
    val rol: String,
    val token: String
) : Principal
