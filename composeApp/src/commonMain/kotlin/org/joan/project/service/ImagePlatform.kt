package org.joan.project.service

import androidx.compose.ui.graphics.ImageBitmap

expect suspend fun seleccionarImagenBytes(): ByteArray?
expect fun imagenBitmapDeBytes(bytes: ByteArray): ImageBitmap?
expect fun comprimirImagenJpeg(bytes: ByteArray): ByteArray
expect fun seleccionarRutaImagenLogo(): String?
expect fun cargarImagenLocalBitmap(path: String): ImageBitmap?
expect fun cargarLogoBitmapDesdeRecursos(): ImageBitmap?
