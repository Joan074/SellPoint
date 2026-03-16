package org.joan.project.service

import org.joan.project.repository.EmpleadoRepository

class EmpleadoService(private val repo: EmpleadoRepository) {
    suspend fun login(usuario: String, contrasena: String): Boolean {
        return repo.login(usuario, contrasena)
    }
}