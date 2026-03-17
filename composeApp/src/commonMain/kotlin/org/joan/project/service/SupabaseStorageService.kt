package org.joan.project.service

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

class SupabaseStorageService(private val client: HttpClient) {

    companion object {
        const val PROJECT_URL = "https://ycntxneimlyubswsntqy.supabase.co"
        const val ANON_KEY = "sb_publishable_g5hXQDj0z7KCxpMkbrdBtQ_EZAJ0oSP"
        const val BUCKET = "productos"
        private const val MAX_DIM = 800
        private const val JPEG_QUALITY = 0.8f
    }

    suspend fun subirImagen(archivo: File): String {
        val nombreArchivo = "${UUID.randomUUID()}.jpg"
        val bytes = comprimirImagen(archivo)

        client.post("$PROJECT_URL/storage/v1/object/$BUCKET/$nombreArchivo") {
            header(HttpHeaders.Authorization, "Bearer $ANON_KEY")
            header("apikey", ANON_KEY)
            header("x-upsert", "true")
            contentType(ContentType.Image.JPEG)
            setBody(bytes)
        }

        return "$PROJECT_URL/storage/v1/object/public/$BUCKET/$nombreArchivo"
    }

    private fun comprimirImagen(archivo: File): ByteArray {
        val original = ImageIO.read(archivo)

        val (newWidth, newHeight) = calcularDimensiones(original.width, original.height)

        val resized = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
        val g2d = resized.createGraphics()
        g2d.color = java.awt.Color.WHITE
        g2d.fillRect(0, 0, newWidth, newHeight)
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null)
        g2d.dispose()

        val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
        val param = writer.defaultWriteParam.apply {
            compressionMode = ImageWriteParam.MODE_EXPLICIT
            compressionQuality = JPEG_QUALITY
        }

        val out = ByteArrayOutputStream()
        writer.output = ImageIO.createImageOutputStream(out)
        writer.write(null, IIOImage(resized, null, null), param)
        writer.dispose()

        return out.toByteArray()
    }

    private fun calcularDimensiones(width: Int, height: Int): Pair<Int, Int> {
        if (width <= MAX_DIM && height <= MAX_DIM) return width to height
        val ratio = minOf(MAX_DIM.toDouble() / width, MAX_DIM.toDouble() / height)
        return (width * ratio).toInt() to (height * ratio).toInt()
    }
}
