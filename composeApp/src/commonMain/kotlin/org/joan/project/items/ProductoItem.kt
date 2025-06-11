package org.joan.project.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import org.joan.project.db.entidades.ProductoResponse
import java.io.File
import javax.imageio.ImageIO
import androidx.compose.ui.graphics.toComposeImageBitmap

@Composable
fun ProductoItem(
    producto: ProductoResponse,
    modifier: Modifier = Modifier,
    onClick: () -> Unit // ← Añadido
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick) // ← Soporte de clic
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val imageBitmap = try {
                producto.imagenUrl?.let { path ->
                    val file = File(path)
                    if (file.exists()) ImageIO.read(file)?.toComposeImageBitmap() else null
                }
            } catch (_: Exception) {
                null
            }

            if (imageBitmap != null) {
                Image(
                    painter = BitmapPainter(imageBitmap),
                    contentDescription = "Imagen del producto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sin imagen", style = MaterialTheme.typography.bodySmall)
                }
            }

            Divider()

            Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
            Text("Precio: ${producto.precio} €", style = MaterialTheme.typography.bodyMedium)
            Text("Stock: ${producto.stock}", style = MaterialTheme.typography.bodyMedium)
            Text("Categoría: ${producto.categoria.nombre}", style = MaterialTheme.typography.bodySmall)
            Text("Proveedor: ${producto.proveedor.nombre}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
