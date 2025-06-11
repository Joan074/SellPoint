package org.joan.project.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class EmpleadoRepository(private val client: HttpClient) {

    var token: String? = null

    suspend fun login(usuario: String, contrasena: String): Boolean {
        val response: HttpResponse = client.post("http://localhost:8080/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("username" to usuario, "password" to contrasena))
        }

        return if (response.status == HttpStatusCode.OK) {
            val body = response.body<String>()
            val json = Json.parseToJsonElement(body).jsonObject
            token = json["token"]?.jsonPrimitive?.content
            token != null
        } else {
            false
        }
    }
}
