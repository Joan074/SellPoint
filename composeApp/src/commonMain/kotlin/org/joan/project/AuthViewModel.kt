package org.joan.project.viewmodel

import io.ktor.client.call.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.joan.project.db.entidades.EmpleadoResponse
import io.ktor.client.plugins.*
import org.joan.project.service.AuthService
import org.joan.project.db.entidades.ErrorResponse

class AuthViewModel(
    private val authService: AuthService
) {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _currentUser = MutableStateFlow<EmpleadoResponse?>(null)
    val currentUser: StateFlow<EmpleadoResponse?> = _currentUser

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val scope = CoroutineScope(Dispatchers.Default)

    fun login(usuario: String, contrasena: String) {
        _loginState.value = LoginState.Loading

        scope.launch {
            try {
                val response = authService.login(usuario, contrasena)
                _token.value = response.token
                _currentUser.value = response.empleado
                _loginState.value = LoginState.Success(response.empleado)
                // Aquí puedes persistir el token si lo deseas
            } catch (e: ClientRequestException) {
                val error = try {
                    e.response.body<ErrorResponse>()
                } catch (_: Exception) {
                    null
                }
                _loginState.value = LoginState.Error(
                    error?.mensaje ?: "Error desconocido durante el login"
                )
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(
                    e.message ?: "Error desconocido durante el login"
                )
            }
        }
    }

    fun logout() {
        scope.launch {
            try {
                _token.value?.let { authService.logout(it) }
            } catch (_: Exception) {
                // Silenciar errores de logout
            }

            _token.value = null
            _currentUser.value = null
            _loginState.value = LoginState.Idle
        }
    }

    suspend fun validateSession(): Boolean {
        return _token.value?.let { authService.validateToken(it) } ?: false
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val empleado: EmpleadoResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}

