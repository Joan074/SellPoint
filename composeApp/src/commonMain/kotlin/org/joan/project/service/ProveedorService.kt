package org.joan.project.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.joan.project.db.entidades.ProveedorNotaRequest
import org.joan.project.db.entidades.ProveedorRequest
import org.joan.project.db.entidades.ProveedorResponse
import org.joan.project.util.ServerConfig

class ProveedorService(private val client: HttpClient, private val serverConfig: ServerConfig) {

    suspend fun getAllProveedores(token: String): List<ProveedorResponse> {
        return client.get("${serverConfig.url}/proveedores") {  // <- plural
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun crearProveedor(token: String, request: ProveedorRequest): ProveedorResponse {
        return client.post("${serverConfig.url}/proveedores") { // <- plural
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // ProveedorService
    suspend fun actualizarProveedor(token: String, id: Int, request: ProveedorRequest): ProveedorResponse {
        return client.put("${serverConfig.url}/proveedores/$id") {  // <- plural
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun eliminarProveedor(token: String, id: Int) {
        client.delete("${serverConfig.url}/proveedores/$id") {       // <- plural
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }


    suspend fun actualizarNota(token: String, id: Int, nota: String?): ProveedorResponse {
        return client.put("${serverConfig.url}/proveedores/$id/nota") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(ProveedorNotaRequest(nota))
        }.body()
    }

    suspend fun obtenerNota(token: String, id: Int): String? {
        val resp: Map<String, String?> = client.get("${serverConfig.url}/proveedores/$id/nota") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return resp["nota"]
    }

    suspend fun borrarNota(token: String, id: Int): ProveedorResponse {
        return client.delete("${serverConfig.url}/proveedores/$id/nota") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }



}

