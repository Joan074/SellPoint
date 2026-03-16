package org.joan.project.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.joan.project.db.entidades.ProveedorRequest
import org.joan.project.db.entidades.ProveedorResponse
import org.joan.project.viewmodel.AuthViewModel
import org.joan.project.viewmodel.ProveedorViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaProveedores(
    authViewModel: AuthViewModel = koinInject(),
    proveedorViewModel: ProveedorViewModel = koinInject(),
    onVolverClick: () -> Unit
) {
    val token = authViewModel.token.value

    val proveedores by proveedorViewModel.proveedores.collectAsState()
    val cargando by proveedorViewModel.cargando.collectAsState()
    val errorVM by proveedorViewModel.error.collectAsState()

    // Estado UI
    var query by remember { mutableStateOf("") }
    var sortAsc by remember { mutableStateOf(true) }

    // Form (crear/editar)
    var showForm by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<ProveedorResponse?>(null) }

    // Nota (editar una única nota del proveedor)
    var notaTarget by remember { mutableStateOf<ProveedorResponse?>(null) }

    val scope = rememberCoroutineScope()

    // Carga inicial
    LaunchedEffect(token) {
        token?.let { proveedorViewModel.cargarProveedores(it) }
    }

    val listaFiltradaOrdenada by remember(query, sortAsc, proveedores) {
        derivedStateOf {
            proveedores
                .filter { p ->
                    query.isBlank() ||
                            p.nombre.contains(query, true) ||
                            p.contactoNombre.contains(query, true) ||
                            p.contactoEmail.contains(query, true)
                }
                .sortedBy { it.nombre.lowercase() }
                .let { if (sortAsc) it else it.reversed() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onVolverClick) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver")
            }
            Text(
                "Proveedores",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
            FilledTonalButton(
                enabled = token != null,
                onClick = { editTarget = null; showForm = true } // crear
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Agregar proveedor")
            }
        }

        Spacer(Modifier.height(12.dp))

        // Búsqueda + ordenar
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Outlined.Cancel, null)
                        }
                    }
                },
                placeholder = { Text("Buscar por nombre, contacto o email…") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(10.dp))

            FilledTonalIconButton(onClick = { sortAsc = !sortAsc }) {
                Icon(
                    if (sortAsc) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
                    contentDescription = "Orden"
                )
            }
        }

        if (errorVM != null) {
            Spacer(Modifier.height(8.dp))
            Text(errorVM!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(12.dp))

        // Lista
        if (cargando && proveedores.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(listaFiltradaOrdenada, key = { it.id }) { p ->
                    ProveedorCard(
                        proveedor = p,
                        onEditar = { editTarget = p; showForm = true },
                        onEditarNota = { notaTarget = p },
                        onEliminar = {
                            val tk = token ?: return@ProveedorCard
                            scope.launch { proveedorViewModel.eliminarProveedor(tk, p.id) }
                        }
                    )
                }
            }
        }
    }

    // --- Diálogo Crear/Editar ---
    if (showForm) {
        val editing = editTarget != null
        ProveedorFormDialog(
            abierto = true,
            titulo = if (editing) "Editar proveedor" else "Nuevo proveedor",
            initial = editTarget,
            onDismiss = { showForm = false },
            onConfirm = { req ->
                val tk = token ?: return@ProveedorFormDialog
                scope.launch {
                    if (editing) {
                        proveedorViewModel.actualizarProveedor(tk, editTarget!!.id, req)
                    } else {
                        proveedorViewModel.crearProveedor(tk, req)
                    }
                    showForm = false
                }
            }
        )
    }

    // --- Diálogo de Nota (única) ---
    notaTarget?.let { prov ->
        NotaProveedorDialog(
            proveedor = prov,
            onDismiss = { notaTarget = null },
            onGuardar = { nuevaNota ->
                val tk = token ?: return@NotaProveedorDialog
                scope.launch {
                    // Construimos request con los mismos datos + nota actualizada
                    proveedorViewModel.actualizarProveedor(
                        tk,
                        prov.id,
                        ProveedorRequest(
                            nombre = prov.nombre,
                            contactoNombre = prov.contactoNombre,
                            contactoEmail = prov.contactoEmail,
                            contactoTelefono = prov.contactoTelefono,
                            direccion = prov.direccion,
                            nota = nuevaNota
                        )
                    )
                    notaTarget = null
                }
            }
        )
    }
}

/* ---------- Card de proveedor ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProveedorCard(
    proveedor: ProveedorResponse,
    onEditar: () -> Unit,
    onEditarNota: () -> Unit,
    onEliminar: () -> Unit
) {
    ElevatedCard(
        onClick = onEditar,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar inicial
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        proveedor.nombre.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        proveedor.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val contacto = listOf(
                        proveedor.contactoNombre,
                        proveedor.contactoEmail,
                        proveedor.contactoTelefono
                    ).filter { it.isNotBlank() }.joinToString(" · ")
                    if (contacto.isNotBlank()) {
                        Text(
                            contacto,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Menú
                var menu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Outlined.MoreVert, null) }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = { menu = false; onEditar() },
                            leadingIcon = { Icon(Icons.Outlined.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Editar nota") },
                            onClick = { menu = false; onEditarNota() },
                            leadingIcon = { Icon(Icons.Outlined.StickyNote2, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            onClick = { menu = false; onEliminar() },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Delete,
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            if (proveedor.direccion.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text("Dirección: ${proveedor.direccion}", style = MaterialTheme.typography.bodySmall)
            }

            // Preview de nota (primera línea)
            proveedor.nota?.let { n ->
                if (n.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Nota: " + n.lineSequence().firstOrNull().orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = onEditarNota,
                    label = { Text("Nota") },
                    leadingIcon = { Icon(Icons.Outlined.StickyNote2, null) }
                )
                if (!proveedor.activo) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Inactivo") },
                        leadingIcon = { Icon(Icons.Outlined.DoNotDisturb, null) }
                    )
                }
            }
        }
    }
}

/* ---------- Diálogo de formulario (crear/editar) ---------- */

@Composable
private fun ProveedorFormDialog(
    abierto: Boolean,
    titulo: String,
    initial: ProveedorResponse?,
    onDismiss: () -> Unit,
    onConfirm: (ProveedorRequest) -> Unit
) {
    if (!abierto) return

    var nombre by remember(initial) { mutableStateOf(initial?.nombre ?: "") }
    var contactoNombre by remember(initial) { mutableStateOf(initial?.contactoNombre ?: "") }
    var contactoEmail by remember(initial) { mutableStateOf(initial?.contactoEmail ?: "") }
    var contactoTelefono by remember(initial) { mutableStateOf(initial?.contactoTelefono ?: "") }
    var direccion by remember(initial) { mutableStateOf(initial?.direccion ?: "") }
    var nota by remember(initial) { mutableStateOf(initial?.nota ?: "") } // NUEVO
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre*") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = contactoNombre,
                    onValueChange = { contactoNombre = it },
                    label = { Text("Persona de contacto*") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = contactoEmail,
                    onValueChange = { contactoEmail = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                OutlinedTextField(
                    value = contactoTelefono,
                    onValueChange = { contactoTelefono = it },
                    label = { Text("Teléfono") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Nota (opcional)") },
                    singleLine = false,
                    minLines = 3
                )
                if (error != null) {
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (nombre.isBlank() || contactoNombre.isBlank()) {
                    error = "Nombre y contacto son obligatorios."
                    return@Button
                }
                onConfirm(
                    ProveedorRequest(
                        nombre = nombre.trim(),
                        contactoNombre = contactoNombre.trim(),
                        contactoEmail = contactoEmail.trim(),
                        contactoTelefono = contactoTelefono.trim(),
                        direccion = direccion.trim(),
                        nota = nota.ifBlank { null } // NUEVO
                    )
                )
            }) { Text("Guardar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/* ---------- Diálogo de edición de la nota ---------- */

@Composable
private fun NotaProveedorDialog(
    proveedor: ProveedorResponse,
    onDismiss: () -> Unit,
    onGuardar: (String?) -> Unit
) {
    var texto by remember(proveedor) { mutableStateOf(proveedor.nota.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nota de ${proveedor.nombre}") },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Nota") },
                singleLine = false,
                minLines = 4
            )
        },
        confirmButton = {
            Button(onClick = { onGuardar(texto.ifBlank { null }) }) { Text("Guardar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
