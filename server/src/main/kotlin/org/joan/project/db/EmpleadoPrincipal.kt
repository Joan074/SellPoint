package org.joan.project.db

import io.ktor.server.auth.Principal

data class EmpleadoPrincipal(
    val id: Int,
    val usuario: String,
    val rol: String,
    val token: String
) : Principal