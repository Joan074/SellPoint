package org.joan.project.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.joan.project.db.entidades.ProductoRequest
import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.util.BASE_URL


class ProductoService(private val client: HttpClient) {
    suspend fun getAllProductos(token: String): List<ProductoResponse> {
        return client.get("http://localhost:8080/producto") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun crearProducto(token: String, request: ProductoRequest): ProductoResponse {
        return client.post("${BASE_URL}/producto") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun actualizarProducto(id: Int, token: String, request: ProductoRequest): ProductoResponse {
        return client.put("$BASE_URL/producto/$id") {
            header("Authorization", "Bearer $token")
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(request)
        }.body()
    }


}
