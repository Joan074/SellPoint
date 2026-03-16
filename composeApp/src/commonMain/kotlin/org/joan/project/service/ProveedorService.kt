package org.joan.project.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.joan.project.db.entidades.ProveedorNotaRequest
import org.joan.project.db.entidades.ProveedorRequest
import org.joan.project.db.entidades.ProveedorResponse
import org.joan.project.util.BASE_URL

class ProveedorService(private val client: HttpClient) {

    suspend fun getAllProveedores(token: String): List<ProveedorResponse> {
        return client.get("$BASE_URL/proveedores") {  // <- plural
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun crearProveedor(token: String, request: ProveedorRequest): ProveedorResponse {
        return client.post("$BASE_URL/proveedores") { // <- plural
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // ProveedorService
    suspend fun actualizarProveedor(token: String, id: Int, request: ProveedorRequest): ProveedorResponse {
        return client.put("$BASE_URL/proveedores/$id") {  // <- plural
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun eliminarProveedor(token: String, id: Int) {
        client.delete("$BASE_URL/proveedores/$id") {       // <- plural
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }


    suspend fun actualizarNota(token: String, id: Int, nota: String?): ProveedorResponse {
        return client.put("$BASE_URL/proveedores/$id/nota") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(ProveedorNotaRequest(nota))
        }.body()
    }

    suspend fun obtenerNota(token: String, id: Int): String? {
        val resp: Map<String, String?> = client.get("$BASE_URL/proveedores/$id/nota") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return resp["nota"]
    }

    suspend fun borrarNota(token: String, id: Int): ProveedorResponse {
        return client.delete("$BASE_URL/proveedores/$id/nota") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }



}

