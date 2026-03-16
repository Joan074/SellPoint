package org.joan.project.ui


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

@Composable
actual fun ProductoImagen(
    urlOrPath: String?,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale
) {
    var state by remember(urlOrPath) { mutableStateOf<LoadState>(LoadState.Loading) }

    LaunchedEffect(urlOrPath) {
        if (urlOrPath.isNullOrBlank()) {
            state = LoadState.Error
            return@LaunchedEffect
        }
        state = try {
            val bytes = withContext(Dispatchers.IO) {
                when {
                    urlOrPath.startsWith("file://", ignoreCase = true) ->
                        URL(urlOrPath).openStream().use { it.readBytes() }
                    urlOrPath.startsWith("http", ignoreCase = true) ->
                        URL(urlOrPath).openStream().use { it.readBytes() } // opcional
                    else -> Files.readAllBytes(Paths.get(urlOrPath))       // ruta local normal
                }
            }
            val skiaImage = org.jetbrains.skia.Image.makeFromEncoded(bytes)
            LoadState.Success(skiaImage.toComposeImageBitmap())
        } catch (_: Throwable) {
            LoadState.Error
        }
    }

    when (val s = state) {
        is LoadState.Success -> Image(
            bitmap = s.bitmap,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
        LoadState.Loading -> Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) { Text("Cargando…", maxLines = 1, overflow = TextOverflow.Ellipsis) }
        LoadState.Error -> Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) { Text("Sin imagen", maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

private sealed class LoadState {
    data object Loading : LoadState()
    data object Error : LoadState()
    data class Success(val bitmap: ImageBitmap) : LoadState()
}
