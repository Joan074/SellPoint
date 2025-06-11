package org.joan.project.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.joan.project.db.entidades.VentaRequest

class VentaService(private val client: HttpClient) {

    suspend fun crearVenta(token: String, request: VentaRequest): HttpResponse {
        return client.post("http://localhost:8080/ventas") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(request)
        }
    }
}