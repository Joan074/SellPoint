package org.joan.project.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.joan.project.db.entidades.EmpleadoLoginRequest
import org.joan.project.db.entidades.TokenResponse
import org.joan.project.util.ServerConfig

// AuthService.kt en commonMain
class AuthService(private val client: HttpClient, private val serverConfig: ServerConfig) {
    suspend fun login(usuario: String, contrasena: String): TokenResponse {
        return client.post("${serverConfig.url}/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(EmpleadoLoginRequest(usuario, contrasena))
        }.body()
    }

    suspend fun logout(token: String) {
        client.post("${serverConfig.url}/auth/logout") {
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun validateToken(token: String): Boolean {
        return try {
            client.get("${serverConfig.url}/empleados/yo") {
                header("Authorization", "Bearer $token")
            }.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }
}