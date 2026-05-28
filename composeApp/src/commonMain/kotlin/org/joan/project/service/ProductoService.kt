package org.joan.project.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.joan.project.db.entidades.ProductoRequest
import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.util.ServerConfig


class ProductoService(private val client: HttpClient, private val serverConfig: ServerConfig) {
    suspend fun getAllProductos(token: String): List<ProductoResponse> {
        println("Llamando a ${serverConfig.url}/producto")
        try {
            val response = client.get("${serverConfig.url}/producto") {
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
        return client.post("${serverConfig.url}/producto") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun actualizarProducto(id: Int, token: String, request: ProductoRequest): ProductoResponse {
        return client.put("${serverConfig.url}/producto/$id") {
            header("Authorization", "Bearer $token")
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun eliminarProducto(id: Int, token: String) {
        client.delete("${serverConfig.url}/producto/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }


}
