package org.joan.project.viewmodel

import com.russhwolf.settings.Settings
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.joan.project.db.entidades.EmpleadoResponse
import org.joan.project.service.AuthService
import org.joan.project.db.entidades.ErrorResponse

private const val KEY_TOKEN    = "auth_token"
private const val KEY_USER_ID  = "auth_user_id"
private const val KEY_USER_NOM = "auth_user_nombre"
private const val KEY_USER_USR = "auth_user_usuario"
private const val KEY_USER_ROL = "auth_user_rol"

class AuthViewModel(
    private val authService: AuthService,
    private val settings: Settings
) {
    private val _loginState  = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _currentUser = MutableStateFlow<EmpleadoResponse?>(null)
    val currentUser: StateFlow<EmpleadoResponse?> = _currentUser

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        // Restore persisted session so validateSession() can run without login
        val savedToken = settings.getStringOrNull(KEY_TOKEN)
        if (savedToken != null) {
            _token.value = savedToken
            _currentUser.value = EmpleadoResponse(
                id      = settings.getIntOrNull(KEY_USER_ID)      ?: 0,
                nombre  = settings.getStringOrNull(KEY_USER_NOM)  ?: "",
                usuario = settings.getStringOrNull(KEY_USER_USR)  ?: "",
                rol     = settings.getStringOrNull(KEY_USER_ROL)  ?: ""
            )
        }
    }

    fun login(usuario: String, contrasena: String) {
        _loginState.value = LoginState.Loading

        // ── MODO OFFLINE ──────────────────────────────────────────────────────
        if (usuario == "demo" && contrasena == "demo") {
            val demo = EmpleadoResponse(id = 0, nombre = "Demo", usuario = "demo", rol = "ADMIN")
            _token.value = "token-demo-offline"
            _currentUser.value = demo
            _loginState.value = LoginState.Success(demo)
            // Demo session is intentionally NOT persisted
            return
        }
        // ─────────────────────────────────────────────────────────────────────

        scope.launch {
            try {
                val response = authService.login(usuario, contrasena)
                _token.value      = response.token
                _currentUser.value = response.empleado
                _loginState.value = LoginState.Success(response.empleado)
                persistSession(response.token, response.empleado)
            } catch (e: ClientRequestException) {
                val body = try { e.response.body<ErrorResponse>() } catch (_: Exception) { null }
                _loginState.value = LoginState.Error(body?.mensaje ?: "Error desconocido durante el login")
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Error desconocido durante el login")
            }
        }
    }

    fun logout() {
        val tokenToRevoke = _token.value
        clearPersistedSession()
        _token.value       = null
        _currentUser.value = null
        _loginState.value  = LoginState.Idle

        scope.launch {
            try { tokenToRevoke?.let { authService.logout(it) } } catch (_: Exception) {}
        }
    }

    suspend fun validateSession(): Boolean =
        _token.value?.let { authService.validateToken(it) } ?: false

    private fun persistSession(token: String, emp: EmpleadoResponse) {
        settings.putString(KEY_TOKEN,    token)
        settings.putInt   (KEY_USER_ID,  emp.id)
        settings.putString(KEY_USER_NOM, emp.nombre)
        settings.putString(KEY_USER_USR, emp.usuario)
        settings.putString(KEY_USER_ROL, emp.rol)
    }

    private fun clearPersistedSession() {
        settings.remove(KEY_TOKEN)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_USER_NOM)
        settings.remove(KEY_USER_USR)
        settings.remove(KEY_USER_ROL)
    }
}

sealed class LoginState {
    object Idle    : LoginState()
    object Loading : LoginState()
    data class Success(val empleado: EmpleadoResponse) : LoginState()
    data class Error(val message: String)              : LoginState()
}
