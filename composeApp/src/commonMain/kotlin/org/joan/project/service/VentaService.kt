package org.joan.project.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.joan.project.db.entidades.VentaRequest
import org.joan.project.db.entidades.VentaResponse
import org.joan.project.util.BASE_URL

class VentaService(private val client: HttpClient) {

    suspend fun getVentasEntreFechas(token: String, desde: String, hasta: String): List<VentaResponse> {
        val response: HttpResponse = client.get("$BASE_URL/ventas") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("desde", desde)
            parameter("hasta", hasta)
        }

        if (!response.status.isSuccess()) {
            throw RuntimeException("Error al obtener ventas: ${response.status}")
        }

        return response.body()
    }

    suspend fun crearVenta(token: String, request: VentaRequest): VentaResponse {
        val response: HttpResponse = client.post("${BASE_URL}/ventas") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (!response.status.isSuccess()) {
            throw RuntimeException("Error al crear venta: ${response.status}")
        }

        return response.body()
    }

    suspend fun anularVenta(token: String, ventaId: Int): VentaResponse {
        val response: HttpResponse = client.put("${BASE_URL}/ventas/$ventaId/anular") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        if (!response.status.isSuccess()) {
            throw RuntimeException("Error al anular venta: ${response.status}")
        }

        return response.body()
    }
}
