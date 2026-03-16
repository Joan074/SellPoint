package org.joan.project.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.joan.project.db.entidades.CategoriaRequest
import org.joan.project.db.entidades.CategoriaResponse
import org.joan.project.util.BASE_URL

class CategoriaService(private val client: HttpClient) {

    suspend fun getAllCategorias(token: String): List<CategoriaResponse> {
        return client.get("$BASE_URL/categorias") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun crearCategoria(token: String, request: CategoriaRequest): CategoriaResponse {
        return client.post("$BASE_URL/categorias") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
