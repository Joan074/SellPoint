package org.joan.project.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.joan.project.db.entidades.*
import org.joan.project.viewmodel.CategoriaViewModel
import org.joan.project.viewmodel.ProductoViewModel
import org.joan.project.viewmodel.ProveedorViewModel
import org.koin.compose.koinInject
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Files
import org.jetbrains.skia.Image as SkiaImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearProducto(
    productoViewModel: ProductoViewModel = koinInject(),
    categoriaViewModel: CategoriaViewModel = koinInject(),
    proveedorViewModel: ProveedorViewModel = koinInject(),
    token: String,
    onProductoCreado: () -> Unit,
    onVolverClick: () -> Unit
) {
    // --------- estado formulario ----------
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var codigoBarras by remember { mutableStateOf("") }

    // Imagen local
    var imagenPath by remember { mutableStateOf<String?>(null) }
    var imagenBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // Catálogos (via VM)
    val categorias by categoriaViewModel.categorias.collectAsState()
    val catLoading by categoriaViewModel.cargando.collectAsState()
    val catError by categoriaViewModel.error.collectAsState()

    val proveedores by proveedorViewModel.proveedores.collectAsState()
    val provLoading by proveedorViewModel.cargando.collectAsState()
    val provError by proveedorViewModel.error.collectAsState()

    // selección
    var categoriaSel by remember { mutableStateOf<CategoriaResponse?>(null) }
    var proveedorSel by remember { mutableStateOf<ProveedorResponse?>(null) }

    // UI state
    var showNuevaCategoria by remember { mutableStateOf(false) }
    var showNuevoProveedor by remember { mutableStateOf(false) }
    var creandoProveedor by remember { mutableStateOf(false) }
    var creandoProducto by remember { mutableStateOf(false) }

    var showError by remember { mutableStateOf(false) }
    var errorGlobal by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // --------- cargar catálogos ----------
    LaunchedEffect(token) {
        categoriaViewModel.cargarCategorias(token)
        proveedorViewModel.cargarProveedores(token)
    }

    val isFormValid = remember(nombre, precio, stock, categoriaSel, proveedorSel) {
        nombre.isNotBlank() &&
                precio.toDoubleOrNull() != null &&
                stock.toIntOrNull() != null &&
                categoriaSel != null &&
                proveedorSel != null
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Top bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolverClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    text = "Nuevo producto",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Card formulario
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
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre*") },
                        isError = showError && nombre.isBlank(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = precio,
                            onValueChange = { precio = it },
                            label = { Text("Precio*") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = showError && precio.toDoubleOrNull() == null,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("Stock*") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = showError && stock.toIntOrNull() == null,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = codigoBarras,
                        onValueChange = { codigoBarras = it },
                        label = { Text("Código de barras (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // --------- Selector Categoría ----------
                    SelectorBuscable(
                        label = "Categoría*",
                        opciones = categorias,
                        textoOpcion = { it.nombre },
                        seleccionado = categoriaSel,
                        onSeleccion = { categoriaSel = it },
                        onClear = { categoriaSel = null },           // permite limpiar
                        botonCrear = {
                            TextButton(onClick = { showNuevaCategoria = true }) {
                                Icon(Icons.Outlined.Add, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Nueva categoría")
                            }
                        },
                        cargando = catLoading
                    )
                    if (catError != null) {
                        Text(catError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    // --------- Selector Proveedor ----------
                    SelectorBuscable(
                        label = "Proveedor*",
                        opciones = proveedores,
                        textoOpcion = { it.nombre },
                        seleccionado = proveedorSel,
                        onSeleccion = { proveedorSel = it },
                        onClear = { proveedorSel = null },          // permite limpiar
                        botonCrear = {
                            TextButton(onClick = { showNuevoProveedor = true }) {
                                Icon(Icons.Outlined.Add, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Nuevo proveedor")
                            }
                        },
                        cargando = provLoading
                    )
                    if (provError != null) {
                        Text(provError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    // --------- Imagen local ----------
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
                    }) { Text("Seleccionar imagen") }

                    if (imagenBitmap != null) {
                        Image(
                            bitmap = imagenBitmap!!,
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                        Text(imagenPath ?: "", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("No se ha seleccionado ninguna imagen", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (errorGlobal != null) {
                Spacer(Modifier.height(12.dp))
                Text(errorGlobal!!, color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Botón crear
        Button(
            onClick = {
                showError = true
                errorGlobal = null
                if (!isFormValid) return@Button

                val req = ProductoRequest(
                    nombre = nombre.trim(),
                    precio = precio.toDoubleOrNull() ?: 0.0,
                    stock = stock.toIntOrNull() ?: 0,
                    codigoBarras = codigoBarras.ifBlank { null },
                    categoriaId = categoriaSel!!.id,
                    proveedorId = proveedorSel!!.id,
                    imagenUrl = imagenPath
                )

                creandoProducto = true
                scope.launch {
                    productoViewModel.crearProducto(
                        request = req,
                        token = token,
                        onSuccess = {
                            creandoProducto = false
                            onProductoCreado()
                        },
                        onError = { err ->
                            creandoProducto = false
                            errorGlobal = err
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = isFormValid && !creandoProducto
        ) {
            if (creandoProducto) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text("Crear producto", style = MaterialTheme.typography.titleMedium)
        }
    }

    // ------- Diálogo Nueva Categoría -------
    if (showNuevaCategoria) {
        DialogNuevaCategoria(
            onDismiss = { showNuevaCategoria = false },
            onCrear = { nombreCat ->
                scope.launch {
                    try {
                        val creada = categoriaViewModel.crearCategoria(token, CategoriaRequest(nombreCat.trim()))
                        categoriaSel = creada
                        showNuevaCategoria = false
                    } catch (e: Exception) {
                        errorGlobal = "No se pudo crear la categoría: ${e.message}"
                    }
                }
            }
        )
    }

    // ------- Diálogo Nuevo Proveedor -------
    if (showNuevoProveedor) {
        DialogNuevoProveedor(
            creando = creandoProveedor,
            onDismiss = { showNuevoProveedor = false },
            onCrear = { nombreProv, contacto, email, tel, dir ->
                creandoProveedor = true
                scope.launch {
                    try {
                        val creado = proveedorViewModel.crearProveedor(
                            token = token,
                            request = ProveedorRequest(
                                nombre = nombreProv.trim(),
                                contactoNombre = (contacto ?: "").trim(),
                                contactoEmail = (email ?: "").trim(),
                                contactoTelefono = (tel ?: "").trim(),
                                direccion = (dir ?: "").trim()
                            )
                        )
                        proveedorSel = creado
                        showNuevoProveedor = false
                    } catch (e: Exception) {
                        errorGlobal = "No se pudo crear el proveedor: ${e.message}"
                    } finally {
                        creandoProveedor = false
                    }
                }
            }
        )
    }
}

/* ======================= Auxiliares ======================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SelectorBuscable(
    label: String,
    opciones: List<T>,
    textoOpcion: (T) -> String,
    seleccionado: T?,
    onSeleccion: (T) -> Unit,
    onClear: (() -> Unit)? = null,            // permite limpiar selección
    botonCrear: (@Composable () -> Unit)? = null,
    cargando: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf(seleccionado?.let(textoOpcion) ?: "") }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        if (it.isBlank() && seleccionado != null) {
                            onClear?.invoke()
                        }
                        if (!expanded) expanded = true
                    },
                    label = { Text(label) },
                    trailingIcon = {
                        Row {
                            if (cargando) {
                                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            } else {
                                if (seleccionado != null || query.isNotBlank()) {
                                    IconButton(onClick = {
                                        onClear?.invoke()
                                        query = ""
                                    }) {
                                        Icon(Icons.Outlined.Close, contentDescription = "Limpiar selección")
                                    }
                                }
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                            }
                        }
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    singleLine = true
                )

                val filtradas = remember(opciones, query) {
                    if (query.isBlank()) opciones
                    else opciones.filter { textoOpcion(it).contains(query, ignoreCase = true) }
                }

                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("— Ninguno —") },
                        onClick = {
                            onClear?.invoke()
                            query = ""
                            expanded = false
                        }
                    )
                    Divider()

                    filtradas.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(textoOpcion(item)) },
                            onClick = {
                                onSeleccion(item)
                                query = textoOpcion(item)
                                expanded = false
                            }
                        )
                    }

                    if (filtradas.isEmpty()) {
                        DropdownMenuItem(text = { Text("No hay resultados") }, enabled = false, onClick = {})
                    }
                }
            }

            if (botonCrear != null) {
                Spacer(Modifier.width(8.dp))
                botonCrear()
            }
        }

        if (seleccionado == null) {
            Spacer(Modifier.height(4.dp))
            Text("Selecciona una opción", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DialogNuevaCategoria(
    onDismiss: () -> Unit,
    onCrear: (String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva categoría") },
        text = {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la categoría") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { if (nombre.isNotBlank()) onCrear(nombre) }, enabled = nombre.isNotBlank()) {
                Text("Crear")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun DialogNuevoProveedor(
    creando: Boolean,
    onDismiss: () -> Unit,
    onCrear: (nombre: String, contacto: String?, email: String?, tel: String?, dir: String?) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var contacto by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var tel by remember { mutableStateOf("") }
    var dir by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo proveedor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre*") }, singleLine = true)
                OutlinedTextField(contacto, { contacto = it }, label = { Text("Persona de contacto") }, singleLine = true)
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true)
                OutlinedTextField(tel, { tel = it }, label = { Text("Teléfono") }, singleLine = true)
                OutlinedTextField(dir, { dir = it }, label = { Text("Dirección") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = { onCrear(nombre, contacto.ifBlank { null }, email.ifBlank { null }, tel.ifBlank { null }, dir.ifBlank { null }) },
                enabled = nombre.isNotBlank() && !creando
            ) {
                if (creando) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text("Crear")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !creando) { Text("Cancelar") }
        }
    )
}
