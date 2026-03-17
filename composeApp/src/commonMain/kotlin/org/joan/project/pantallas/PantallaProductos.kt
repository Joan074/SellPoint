package org.joan.project.pantallas

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.viewmodel.AuthViewModel
import org.joan.project.viewmodel.ProductoViewModel
import org.koin.compose.koinInject
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.input.key.*
import androidx.compose.ui.window.PopupProperties

// si tu ProductoImagen está en otro paquete, ajusta este import:
import org.joan.project.ui.ProductoImagen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PantallaProductos(
    authViewModel: AuthViewModel = koinInject(),
    productoViewModel: ProductoViewModel = koinInject(),
    onCrearProductoClick: () -> Unit,
    onVolverClick: () -> Unit,
    onEditarProductoClick: (ProductoResponse) -> Unit
) {
    val productos by productoViewModel.productos.collectAsState()
    val token = authViewModel.token.value

    var query by rememberSaveable { mutableStateOf("") }
    var categoriaSel by rememberSaveable { mutableStateOf<String?>(null) }
    var proveedorSel by rememberSaveable { mutableStateOf<String?>(null) }
    var sortBy by rememberSaveable { mutableStateOf(SortOption.NOMBRE) }
    var sortAsc by rememberSaveable { mutableStateOf(true) }
    var soloConStock by rememberSaveable { mutableStateOf(false) }
    var soloBajoStock by rememberSaveable { mutableStateOf(false) }

    var productoAEliminar by remember { mutableStateOf<ProductoResponse?>(null) }

    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val searchFocus = remember { FocusRequester() }

    LaunchedEffect(token) {
        if (token != null && productos.isEmpty()) {
            productoViewModel.cargarProductos(token)
        }
    }

    val categorias = remember(productos) { productos.map { it.categoria.nombre }.distinct().sorted() }
    val proveedores = remember(productos) { productos.map { it.proveedor.nombre }.distinct().sorted() }

    val productosFiltradosOrdenados by remember(query, categoriaSel, proveedorSel, sortBy, sortAsc, soloConStock, soloBajoStock, productos) {
        derivedStateOf {
            productos
                .asSequence()
                .filter { p ->
                    (query.isBlank() || p.nombre.contains(query, true)) &&
                            (categoriaSel == null || p.categoria.nombre == categoriaSel) &&
                            (proveedorSel == null || p.proveedor.nombre == proveedorSel) &&
                            (!soloConStock || p.stock > 0) &&
                            (!soloBajoStock || p.stock in 1..9)
                }
                .sortedWith(
                    when (sortBy) {
                        SortOption.NOMBRE -> compareBy<ProductoResponse> { it.nombre.lowercase() }
                        SortOption.PRECIO -> compareBy { it.precio }
                        SortOption.STOCK  -> compareBy { it.stock }
                    }.let { if (sortAsc) it else it.reversed() }
                )
                .toList()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onVolverClick) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver")
                    }
                },
                title = { Text("Gestión de Productos") },
                actions = {
                    IconButton(onClick = { if (token != null) productoViewModel.cargarProductos(token) }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Recargar")
                    }
                    FilledTonalIconButton(onClick = onCrearProductoClick) {
                        Icon(Icons.Outlined.Add, contentDescription = "Nuevo")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbar) }
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
        val isSmall = maxWidth < 1024.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isSmall) 12.dp else 24.dp, vertical = 12.dp)
        ) {

            // Resumen inventario
            InventorySummary(productosFiltradosOrdenados)
            Spacer(Modifier.height(8.dp))

            // Buscador + ordenación
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Outlined.Cancel, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    },
                    placeholder = { Text("Buscar por nombre…") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(searchFocus)
                        .onPreviewKeyEvent { ev ->
                            if (ev.type == KeyEventType.KeyDown) {
                                when (ev.key) {
                                    Key.Enter -> { if (query.isNotEmpty()) query = ""; true }
                                    Key.Escape -> { /* quitar foco */ false }
                                    else -> false
                                }
                            } else false
                        }
                )

                Spacer(Modifier.width(12.dp))

                SortDropdown(
                    sortBy = sortBy,
                    asc = sortAsc,
                    onChange = { s, a -> sortBy = s; sortAsc = a }
                )
            }

            Spacer(Modifier.height(10.dp))

            // Filtros rápidos de stock
            QuickStockFilters(
                conStock = soloConStock,
                bajoStock = soloBajoStock,
                onConStock = { soloConStock = it },
                onBajoStock = { soloBajoStock = it }
            )

            Spacer(Modifier.height(10.dp))

            // Filtros por categoría/proveedor
            FilterRow(
                categorias = categorias,
                proveedores = proveedores,
                categoriaSel = categoriaSel,
                proveedorSel = proveedorSel,
                onCategoria = { categoriaSel = it },
                onProveedor = { proveedorSel = it },
                onClearAll = {
                    categoriaSel = null; proveedorSel = null; query = ""; soloConStock = false; soloBajoStock = false
                }
            )

            Spacer(Modifier.height(16.dp))

            if (productos.isEmpty()) {
                EmptyState(onReload = { if (token != null) productoViewModel.cargarProductos(token) })
                return@Column
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(if (isSmall) 180.dp else 260.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(productosFiltradosOrdenados, key = { it.id }) { producto ->
                    ProductoCard(
                        producto = producto,
                        onEdit = { onEditarProductoClick(producto) },
                        onDelete = { productoAEliminar = producto },
                        modifier = Modifier
                            .animateItemPlacement()
                            .fillMaxWidth()
                    )
                }

                // Footer con totales visibles
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ListFooter(visible = productosFiltradosOrdenados, total = productos.size)
                }
            }
        }
        } // BoxWithConstraints
    }

    // Diálogo eliminar
    if (productoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            icon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
            title = { Text("Eliminar producto") },
            text = { Text("¿Seguro que quieres eliminar \"${productoAEliminar!!.nombre}\"?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        val id = productoAEliminar!!.id
                        val nombre = productoAEliminar!!.nombre
                        productoAEliminar = null
                        val tk = token ?: return@Button
                        productoViewModel.eliminarProducto(
                            id, tk,
                            onSuccess = {
                                scope.launch { snackbar.showSnackbar("Producto \"$nombre\" eliminado") }
                            },
                            onError = { err ->
                                scope.launch { snackbar.showSnackbar(err ?: "Error eliminando") }
                            }
                        )
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { productoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

// ---------- componentes UI ----------

private enum class SortOption { NOMBRE, PRECIO, STOCK }

@Composable
private fun SortDropdown(
    sortBy: SortOption,
    asc: Boolean,
    onChange: (SortOption, Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        FilledTonalButton(onClick = { expanded = true }) {
            Icon(Icons.Outlined.Sort, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(
                when (sortBy) {
                    SortOption.NOMBRE -> "Nombre"
                    SortOption.PRECIO -> "Precio"
                    SortOption.STOCK  -> "Stock"
                }
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                if (asc) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
                contentDescription = if (asc) "Ascendente" else "Descendente"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, 8.dp),
            properties = PopupProperties(focusable = true)
        ) {
            SortOption.values().forEach { option ->
                val isCurrent = option == sortBy
                DropdownMenuItem(
                    text = {
                        Text(
                            when (option) {
                                SortOption.NOMBRE -> "Nombre"
                                SortOption.PRECIO -> "Precio"
                                SortOption.STOCK  -> "Stock"
                            }
                        )
                    },
                    onClick = {
                        val newAsc = if (isCurrent) !asc else true
                        onChange(option, newAsc)
                        expanded = false
                    },
                    trailingIcon = {
                        if (isCurrent) {
                            Icon(
                                if (asc) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterRow(
    categorias: List<String>,
    proveedores: List<String>,
    categoriaSel: String?,
    proveedorSel: String?,
    onCategoria: (String?) -> Unit,
    onProveedor: (String?) -> Unit,
    onClearAll: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Outlined.FilterList, contentDescription = null, tint = LocalContentColor.current.copy(.7f))

        DropdownFilterChip(
            label = "Categoría",
            selected = categoriaSel,
            options = categorias,
            onSelected = onCategoria
        )
        DropdownFilterChip(
            label = "Proveedor",
            selected = proveedorSel,
            options = proveedores,
            onSelected = onProveedor
        )

        Spacer(Modifier.weight(1f))

        TextButton(
            onClick = onClearAll,
            enabled = categoriaSel != null || proveedorSel != null
        ) { Text("Limpiar filtros") }
    }
}

@Composable
private fun DropdownFilterChip(
    label: String,
    selected: String?,
    options: List<String>,
    onSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(selected ?: label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, 8.dp),
            properties = PopupProperties(focusable = true)
        ) {
            DropdownMenuItem(text = { Text("Todas") }, onClick = { onSelected(null); expanded = false })
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = { onSelected(opt); expanded = false })
            }
        }
    }
}

@Composable
private fun QuickStockFilters(
    conStock: Boolean,
    bajoStock: Boolean,
    onConStock: (Boolean) -> Unit,
    onBajoStock: (Boolean) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = conStock,
            onClick = { onConStock(!conStock) },
            label = { Text("Con stock") },
            leadingIcon = if (conStock) ({ Icon(Icons.Outlined.CheckCircle, null) }) else null
        )
        FilterChip(
            selected = bajoStock,
            onClick = { onBajoStock(!bajoStock) },
            label = { Text("Bajo stock < 10") },
            leadingIcon = if (bajoStock) ({ Icon(Icons.Outlined.Warning, null) }) else null
        )
    }
}

@Composable
private fun ProductoCard(
    producto: ProductoResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier) {
        Column(Modifier.padding(14.dp)) {

            // Cabecera: nombre + menú contextual
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                var menuExpanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "Opciones")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        offset = DpOffset(0.dp, 8.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = { onEdit(); menuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            onClick = { onDelete(); menuExpanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Imagen del producto (usa tu loader local)
            ProductoImagen(
                urlOrPath = producto.imagenUrl,
                contentDescription = producto.nombre,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f) // o height(120.dp) si prefieres fijo
                    .clip(MaterialTheme.shapes.medium)
            )

            Spacer(Modifier.height(10.dp))

            // Precio + stock
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "€ %.2f".format(producto.precio),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.width(10.dp))
                StockPill(stock = producto.stock)
            }

            Spacer(Modifier.height(6.dp))

            Text(
                "Cat. ${producto.categoria.nombre} • Prov. ${producto.proveedor.nombre}",
                style = MaterialTheme.typography.bodySmall,
                color = LocalContentColor.current.copy(.7f)
            )
        }
    }
}

@Composable
private fun StockPill(stock: Int) {
    val ok = stock > 10
    val bg = if (ok) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
    val fg = if (ok) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text("Stock: $stock", color = fg, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun InventorySummary(lista: List<ProductoResponse>) {
    val total = lista.size
    val valor = lista.sumOf { it.precio * it.stock }
    AssistChip(
        onClick = {},
        label = { Text("$total productos • Valor inventario: € %.2f".format(valor)) }
    )
}

@Composable
private fun ListFooter(visible: List<ProductoResponse>, total: Int) {
    val valorVisible = visible.sumOf { it.precio * it.stock }
    Text(
        "Mostrando ${visible.size} de $total • Valor visible: € %.2f".format(valorVisible),
        style = MaterialTheme.typography.bodySmall,
        color = LocalContentColor.current.copy(.7f),
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
private fun EmptyState(onReload: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No hay productos para mostrar", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Añade nuevos productos o recarga la lista.",
                style = MaterialTheme.typography.bodyMedium,
                color = LocalContentColor.current.copy(.7f)
            )
            Spacer(Modifier.height(12.dp))
            FilledTonalButton(onClick = onReload) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Recargar")
            }
        }
    }
}
