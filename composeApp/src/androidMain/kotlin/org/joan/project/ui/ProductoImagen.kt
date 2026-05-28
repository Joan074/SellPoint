package org.joan.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
actual fun ProductoImagen(
    urlOrPath: String?,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
) {
    AsyncImage(
        model = urlOrPath,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    )
}
