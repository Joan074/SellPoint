package org.joan.project.service

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.util.UUID

class SupabaseStorageService(private val client: HttpClient) {

    companion object {
        const val PROJECT_URL = "https://ycntxneimlyubswsntqy.supabase.co"
        const val ANON_KEY = "sb_publishable_g5hXQDj0z7KCxpMkbrdBtQ_EZAJ0oSP"
        const val BUCKET = "productos"
    }

    suspend fun subirImagen(bytes: ByteArray): String {
        val nombreArchivo = "${UUID.randomUUID()}.jpg"
        val compressed = comprimirImagenJpeg(bytes)

        client.post("$PROJECT_URL/storage/v1/object/$BUCKET/$nombreArchivo") {
            header(HttpHeaders.Authorization, "Bearer $ANON_KEY")
            header("apikey", ANON_KEY)
            header("x-upsert", "true")
            contentType(ContentType.Image.JPEG)
            setBody(compressed)
        }

        return "$PROJECT_URL/storage/v1/object/public/$BUCKET/$nombreArchivo"
    }
}
