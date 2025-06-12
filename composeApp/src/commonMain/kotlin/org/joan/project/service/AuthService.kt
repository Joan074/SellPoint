package org.joan.project.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.joan.project.db.entidades.EmpleadoLoginRequest
import org.joan.project.db.entidades.TokenResponse
import org.joan.project.util.BASE_URL

// AuthService.kt en commonMain
class AuthService(private val client: HttpClient) {
    suspend fun login(usuario: String, contrasena: String): TokenResponse {
        return client.post("${BASE_URL}/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(EmpleadoLoginRequest(usuario, contrasena))
        }.body()
    }

    suspend fun logout(token: String) {
        client.post("${BASE_URL}/auth/logout") {
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun validateToken(token: String): Boolean {
        return try {
            client.get("${BASE_URL}/empleados/yo") {
                header("Authorization", "Bearer $token")
            }.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }
}