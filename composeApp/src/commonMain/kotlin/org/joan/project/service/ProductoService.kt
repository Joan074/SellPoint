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
        println("Llamando a $BASE_URL/producto")
        try {
            val response = client.get("$BASE_URL/producto") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            println("Status: ${response.status}")
            val productos: List<ProductoResponse> = response.body()
            println("Recibidos ${productos.size} productos")
            return productos
        } catch (e: Exception) {
            println("Error getAllProductos: ${e.message}")
            throw e
        }
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

    suspend fun eliminarProducto(id: Int, token: String) {
        client.delete("${BASE_URL}/producto/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }


}
