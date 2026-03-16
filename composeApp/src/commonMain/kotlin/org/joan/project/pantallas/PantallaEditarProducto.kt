package org.joan.project.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.db.entidades.ProductoRequest
import org.joan.project.viewmodel.ProductoViewModel
import org.koin.compose.koinInject
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Files
import org.jetbrains.skia.Image as SkiaImage

@Composable
fun PantallaEditarProducto(
    producto: ProductoResponse,
    viewModel: ProductoViewModel = koinInject(),
    token: String,
    onVolverClick: () -> Unit,
    onProductoActualizado: () -> Unit
) {
    var nombre by remember { mutableStateOf(producto.nombre) }
    var precio by remember { mutableStateOf(producto.precio.toString()) }
    var stock by remember { mutableStateOf(producto.stock.toString()) }
    var codigoBarras by remember { mutableStateOf(producto.codigoBarras ?: "") }
    var categoriaId by remember { mutableStateOf(producto.categoria.id.toString()) }
    var proveedorId by remember { mutableStateOf(producto.proveedor.id.toString()) }

    var imagenPath by remember { mutableStateOf(producto.imagenUrl) }
    var imagenBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    var showError by remember { mutableStateOf(false) }
    var errorGlobal by remember { mutableStateOf<String?>(null) }

    val isFormValid = nombre.isNotBlank() &&
            precio.toDoubleOrNull() != null &&
            stock.toIntOrNull() != null &&
            categoriaId.toIntOrNull() != null &&
            proveedorId.toIntOrNull() != null

    val scrollState = rememberScrollState()

    // Cargar imagen desde la ruta original
    LaunchedEffect(producto.imagenUrl) {
        producto.imagenUrl?.let { path ->
            val file = File(path)
            if (file.exists()) {
                val bytes = Files.readAllBytes(file.toPath())
                val skiaImage = SkiaImage.makeFromEncoded(bytes)
                imagenBitmap = skiaImage.toComposeImageBitmap()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolverClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    text = "Editar Producto",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = producto.id.toString(),
                        onValueChange = {},
                        label = { Text("ID (no editable)") },
                        enabled = false
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre*") },
                        isError = showError && nombre.isBlank(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = precio,
                        onValueChange = { precio = it },
                        label = { Text("Precio*") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        isError = showError && precio.toDoubleOrNull() == null,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Stock*") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        isError = showError && stock.toIntOrNull() == null,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = codigoBarras,
                        onValueChange = { codigoBarras = it },
                        label = { Text("Código de Barras (opcional)") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = categoriaId,
                        onValueChange = { categoriaId = it },
                        label = { Text("ID Categoría*") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        isError = showError && categoriaId.toIntOrNull() == null,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = proveedorId,
                        onValueChange = { proveedorId = it },
                        label = { Text("ID Proveedor*") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        isError = showError && proveedorId.toIntOrNull() == null,
                        singleLine = true
                    )

                    Button(onClick = {
                        val dialog = FileDialog(null as Frame?, "Seleccionar imagen", FileDialog.LOAD)
                        dialog.isVisible = true
                        if (dialog.file != null) {
                            val selectedFile = File(dialog.directory, dialog.file)
                            imagenPath = selectedFile.absolutePath

                            val bytes = Files.readAllBytes(selectedFile.toPath())
                            val skiaImage = SkiaImage.makeFromEncoded(bytes)
                            imagenBitmap = skiaImage.toComposeImageBitmap()
                        }
                    }) {
                        Text("Cambiar Imagen")
                    }

                    if (imagenBitmap != null) {
                        Image(
                            bitmap = imagenBitmap!!,
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    } else {
                        Text("Imagen actual o no seleccionada", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (errorGlobal != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(errorGlobal!!, color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                showError = true
                if (isFormValid) {
                    val req = ProductoRequest(
                        nombre = nombre,
                        precio = precio.toDouble(),
                        stock = stock.toInt(),
                        codigoBarras = if (codigoBarras.isBlank()) null else codigoBarras,
                        categoriaId = categoriaId.toInt(),
                        proveedorId = proveedorId.toInt(),
                        imagenUrl = imagenPath
                    )
                    viewModel.actualizarProducto(producto.id, req, token, {
                        onProductoActualizado()
                    }, {
                        errorGlobal = it
                    })
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = isFormValid
        ) {
            Text("Guardar cambios", style = MaterialTheme.typography.titleMedium)
        }
    }
}
