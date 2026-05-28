package org.joan.project.service

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image as SkiaImage
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.CompletableFuture
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual suspend fun seleccionarImagenBytes(): ByteArray? {
    val future = CompletableFuture<File?>()
    javax.swing.SwingUtilities.invokeLater {
        val chooser = JFileChooser()
        chooser.dialogTitle = "Seleccionar imagen del producto"
        chooser.fileFilter = FileNameExtensionFilter(
            "Imágenes (JPG, PNG, WEBP)", "jpg", "jpeg", "png", "webp"
        )
        val result = chooser.showOpenDialog(null)
        future.complete(if (result == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null)
    }
    val archivo = withContext(Dispatchers.IO) { future.get() } ?: return null
    return withContext(Dispatchers.IO) { archivo.readBytes() }
}

actual fun imagenBitmapDeBytes(bytes: ByteArray): ImageBitmap? =
    runCatching { SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap() }.getOrNull()

private const val MAX_DIM = 800
private const val JPEG_QUALITY = 0.8f

actual fun comprimirImagenJpeg(bytes: ByteArray): ByteArray {
    val original = ImageIO.read(bytes.inputStream()) ?: return bytes
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

actual fun cargarImagenLocalBitmap(path: String): ImageBitmap? =
    runCatching {
        java.io.File(path).inputStream().buffered().use {
            androidx.compose.ui.res.loadImageBitmap(it)
        }
    }.getOrNull()

actual fun cargarLogoBitmapDesdeRecursos(): ImageBitmap? =
    runCatching {
        androidx.compose.ui.res.useResource("logo.png") { input ->
            androidx.compose.ui.res.loadImageBitmap(input)
        }
    }.getOrNull()

actual fun seleccionarRutaImagenLogo(): String? {
    val chooser = JFileChooser()
    chooser.dialogTitle = "Seleccionar logo"
    chooser.fileFilter = FileNameExtensionFilter("Imágenes (PNG, JPG)", "png", "jpg", "jpeg")
    return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile.absolutePath
    } else null
}
