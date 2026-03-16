package org.joan.project.ui


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

@Composable
expect fun ProductoImagen(
    urlOrPath: String?,                    // ej: "C:\\imgs\\p1.jpg" o "file:///C:/imgs/p1.jpg"
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
)
