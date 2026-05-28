package org.joan.project.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.koin.core.context.GlobalContext
import java.io.ByteArrayOutputStream

actual suspend fun seleccionarImagenBytes(): ByteArray? {
    val uri = ImagePickerRegistry.launch("image/*").await() ?: return null
    val ctx = GlobalContext.get().get<android.app.Application>()
    return ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
}

actual fun imagenBitmapDeBytes(bytes: ByteArray): ImageBitmap? =
    runCatching {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }.getOrNull()

private const val MAX_DIM = 800

actual fun comprimirImagenJpeg(bytes: ByteArray): ByteArray {
    val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
    val (newW, newH) = calcularDimensiones(original.width, original.height)
    val scaled = Bitmap.createScaledBitmap(original, newW, newH, true)
    val out = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
    return out.toByteArray()
}

private fun calcularDimensiones(w: Int, h: Int): Pair<Int, Int> {
    if (w <= MAX_DIM && h <= MAX_DIM) return w to h
    val ratio = minOf(MAX_DIM.toDouble() / w, MAX_DIM.toDouble() / h)
    return (w * ratio).toInt() to (h * ratio).toInt()
}

// Logo selection requires async activity result — not supported as a sync call on Android.
actual fun seleccionarRutaImagenLogo(): String? = null

actual fun cargarImagenLocalBitmap(path: String): ImageBitmap? =
    runCatching {
        if (path.startsWith("content://")) {
            val ctx = GlobalContext.get().get<android.app.Application>()
            ctx.contentResolver.openInputStream(Uri.parse(path))?.use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        } else {
            BitmapFactory.decodeFile(path)?.asImageBitmap()
        }
    }.getOrNull()

// No logo asset bundled in the Android target.
actual fun cargarLogoBitmapDesdeRecursos(): ImageBitmap? = null
