package org.joan.project.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joan.project.service.EmpleadoService

/*class EmpleadoViewModel(private val service: EmpleadoService) : ViewModel() {

    suspend fun login(usuario: String, contrasena: String): Boolean {
        return withContext(Dispatchers.IO) {
            service.login(usuario, contrasena)
        }
    }
}*/