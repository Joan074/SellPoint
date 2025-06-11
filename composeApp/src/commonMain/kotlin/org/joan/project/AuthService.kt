package org.joan.project

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.joan.project.db.entidades.EmpleadoLoginRequest
import org.joan.project.db.entidades.TokenResponse

// AuthService.kt en commonMain
class AuthService(private val client: HttpClient) {
    suspend fun login(usuario: String, contrasena: String): TokenResponse {
        return client.post("http://localhost:8080/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(EmpleadoLoginRequest(usuario, contrasena))
        }.body()
    }

    suspend fun logout(token: String) {
        client.post("http://localhost:8080/auth/logout") {
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun validateToken(token: String): Boolean {
        return try {
            client.get("http://localhost:8080/empleados/yo") {
                header("Authorization", "Bearer $token")
            }.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }
}