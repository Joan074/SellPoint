package org.joan.project.pantallas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.joan.project.db.entidades.LineaVenta
import org.joan.project.db.entidades.VentaRequest
import org.joan.project.viewmodel.AuthViewModel
import org.joan.project.viewmodel.ProductoViewModel
import org.joan.project.viewmodel.VentaViewModel
import org.joan.project.visual.generarTicketPDF
import coil3.compose.SubcomposeAsyncImage
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.joan.project.scanner.platformSupportsCameraScanner
import org.koin.compose.koinInject
import java.awt.Desktop
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCobrar(
    productoViewModel: ProductoViewModel = koinInject(),
    authViewModel: AuthViewModel = koinInject(),
    ventaViewModel: VentaViewModel = koinInject(),
    onVolverClick: () -> Unit
) {
    val productos by productoViewModel.productos.collectAsState()
    val ventas by ventaViewModel.ventas.collectAsState()
    val token = authViewModel.token.value
    val currentUser = authViewModel.currentUser.collectAsState().value

    // --- Sistema multi-ticket ---
    // Siempre hay al menos un ticket abierto
    val tickets = remember { mutableStateListOf(mutableStateListOf<LineaVenta>()) }
    var ticketActivo by remember { mutableStateOf(0) }
    val carrito = tickets[ticketActivo]

    var categoria by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }

    // descuentos & pagos
    var descuentoGlobalPct by remember { mutableStateOf(0.0) }   // % 0..100
    var descuentoGlobalEur by remember { mutableStateOf(0.0) }   // €
    var metodoPago by remember { mutableStateOf("EFECTIVO") }
    var efectivoEntregado by remember { mutableStateOf<Double?>(null) }

    // UI
    val snackbar = remember { SnackbarHostState() }
    var error by remember { mutableStateOf<String?>(null) }
    var resumenAbierto by remember { mutableStateOf(false) }
    var ultimaVenta by remember { mutableStateOf<VentaRequest?>(null) }
    var abrirDialogoDescuento by remember { mutableStateOf(false) }

    // carga inicial
    LaunchedEffect(Unit) {
        if (token != null) {
            if (productos.isEmpty()) productoViewModel.cargarProductos(token)
            val hoy = Clock.System.todayIn(TimeZone.currentSystemDefault())
            ventaViewModel.cargarVentasEntreFechas(token, hoy, hoy)
        }
    }

    // --- Derivados ---
    val categorias = remember(productos) { productos.map { it.categoria.nombre }.distinct().sorted() }


    val catCounts = remember(productos) {
        productos.groupBy { it.categoria.nombre }
            .mapValues { it.value.size }
    }

    val productosFiltrados = remember(productos, categoria, query) {
        productos.filter {
            (categoria == null || it.categoria.nombre == categoria) &&
                    (query.isBlank() || it.nombre.contains(query, true) || (it.codigoBarras ?: "").contains(query, true))
        }
    }

    fun subtotalCarrito() = carrito.sumOf { it.subtotal } // PVP (IVA incluido)
    fun descuentoImporte(base: Double): Double {
        val porPct = base * (descuentoGlobalPct.coerceIn(0.0, 100.0) / 100.0)
        return (porPct + descuentoGlobalEur).coerceAtMost(base).coerceAtLeast(0.0)
    }
    fun totalConDescuento(): Double {
        val base = subtotalCarrito()
        return (base - descuentoImporte(base)).coerceAtLeast(0.0)
    }
    fun totalAPagar() = totalConDescuento()
    fun cambio(): Double = (efectivoEntregado ?: 0.0) - totalAPagar()

    // --- UI ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Punto de Venta") },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onVolverClick,
                        modifier = Modifier.padding(start = 4.dp).size(48.dp)
                    ) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                }
            )
        },
        bottomBar = {
            BarraCobro(
                subtotal = subtotalCarrito(),
                descuentoPct = descuentoGlobalPct,
                descuentoEur = descuentoGlobalEur,
                descuentoCalculado = descuentoImporte(subtotalCarrito()),
                total = totalAPagar(),
                metodoPago = metodoPago,
                onMetodo = { metodoPago = it },
                efectivoEntregado = efectivoEntregado,
                onEfectivo = { efectivoEntregado = it },
                onAbrirDescuento = { abrirDialogoDescuento = true },
                onCobrar = {
                    val tk = token ?: return@BarraCobro
                    val emp = currentUser ?: return@BarraCobro
                    if (carrito.isEmpty()) return@BarraCobro

                    val req = VentaRequest(
                        clienteId = null,
                        empleadoId = emp.id,
                        metodoPago = metodoPago,
                        descuento = descuentoImporte(subtotalCarrito()), // importe total de descuento aplicado
                        items = carrito.map {
                            org.joan.project.db.entidades.ItemVentaRequest(
                                productoId = it.producto.id,
                                cantidad = it.cantidad,
                                precioEspecial = null,
                                descuento = 0.0,
                                promocionId = null
                            )
                        }
                    )
                    ventaViewModel.crearVenta(
                        tk, req,
                        onSuccess = {
                            token?.let { t ->
                                val hoy = Clock.System.todayIn(TimeZone.currentSystemDefault())
                                ventaViewModel.cargarVentasEntreFechas(t, hoy, hoy)
                            }
                            ultimaVenta = req
                            // Cierra el ticket activo; si hay más de uno lo elimina,
                            // si es el único lo vacía pero lo conserva
                            if (tickets.size > 1) {
                                tickets.removeAt(ticketActivo)
                                ticketActivo = (ticketActivo - 1).coerceAtLeast(0)
                            } else {
                                tickets[0].clear()
                            }
                            categoria = null
                            descuentoGlobalPct = 0.0
                            descuentoGlobalEur = 0.0
                            efectivoEntregado = null
                            resumenAbierto = true
                        },
                        onError = { error = it }
                    )
                },
                cobrarHabilitado = carrito.isNotEmpty()
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --------- COLUMNA IZQ: CARRITO (45%, altura completa) ---------
            Column(
                modifier = Modifier.weight(0.45f).fillMaxHeight()
            ) {
                // ---- Franja de pestañas de tickets ----
                val tealColor = MaterialTheme.colorScheme.tertiary
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tickets.forEachIndexed { idx, _ ->
                        FilterChip(
                            selected = idx == ticketActivo,
                            onClick = { ticketActivo = idx },
                            label = { Text("Ticket ${idx + 1}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = tealColor,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                            )
                        )
                    }
                    // Botón "+" para nuevo ticket (máx 5)
                    if (tickets.size < 5) {
                        FilledTonalIconButton(
                            onClick = {
                                tickets.add(mutableStateListOf())
                                ticketActivo = tickets.lastIndex
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Nuevo ticket", modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                // weight(1f) hace que la card ocupe todo el espacio vertical restante
                ElevatedCard(Modifier.fillMaxWidth().weight(1f)) {
                    if (carrito.isEmpty()) {
                        // Estado vacío: icono + texto centrados, ocupa toda la card
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                )
                                Text(
                                    "Carrito vacío",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Añade productos o escanea un código",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(carrito.size) { idx ->
                                val l = carrito[idx]
                                LineaCarrito(
                                    nombre = l.producto.nombre,
                                    precio = l.producto.precio,
                                    cantidad = l.cantidad,
                                    stock = l.producto.stock,
                                    onMas = {
                                        carrito[idx] = l.copy(
                                            cantidad = (l.cantidad + 1).coerceAtMost(l.producto.stock)
                                        )
                                    },
                                    onMenos = {
                                        if (l.cantidad > 1) carrito[idx] = l.copy(cantidad = l.cantidad - 1)
                                        else carrito.removeAt(idx)
                                    },
                                    onEliminar = { carrito.removeAt(idx) }
                                )
                            }
                        }
                    }
                }
            }

            // --------- COLUMNA DCHA: PRODUCTOS (55%, altura completa) ---------
            Column(Modifier.weight(0.55f).fillMaxHeight()) {
                // --- BUSCADOR (fijo arriba) ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Buscar o escanear código") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledTonalIconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Clear, null)
                    }
                    // Camera button — only visible on Android (ML Kit); desktop uses keyboard scanner
                    if (platformSupportsCameraScanner()) {
                        Spacer(Modifier.width(4.dp))
                        FilledTonalIconButton(onClick = { /* launch ML Kit scanner from androidMain */ }) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Escanear con cámara")
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                // --- VENTAS TOTALES DEL DÍA ---
                val totalHoy = ventas.sumOf { it.total }
                val numHoy = ventas.size
                Surface(
                    tonalElevation = 3.dp,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.TrendingUp, null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Ventas hoy",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                "$numHoy ${if (numHoy == 1) "venta" else "ventas"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                money(totalHoy),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                // --- PRODUCTOS (ocupa todo el espacio disponible) ---
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(productosFiltrados, key = { it.id }) { p ->
                        ProductoCard(
                            nombre = p.nombre,
                            precio = p.precio,
                            stock = p.stock,
                            imagenUrl = p.imagenUrl,
                            onClick = {
                                val idx = carrito.indexOfFirst { it.producto.id == p.id }
                                if (idx >= 0) {
                                    val l = carrito[idx]
                                    if (l.cantidad < p.stock) carrito[idx] = l.copy(cantidad = l.cantidad + 1)
                                } else if (p.stock > 0) {
                                    carrito += org.joan.project.db.entidades.LineaVenta(producto = p, cantidad = 1)
                                }
                            }
                        )
                    }
                }

                // --- CHIPS DE CATEGORÍA fijos en la parte inferior ---
                Spacer(Modifier.height(8.dp))
                Surface(
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = categoria == null,
                                onClick = { categoria = null },
                                label = {
                                    Text(
                                        "Todas (${productos.size})",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                modifier = Modifier.height(48.dp)
                            )
                        }
                        items(categorias, key = { it }) { cat ->
                            FilterChip(
                                selected = categoria == cat,
                                onClick = { categoria = cat },
                                label = {
                                    Text(
                                        "$cat (${catCounts[cat] ?: 0})",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                modifier = Modifier.height(48.dp)
                            )
                        }
                    }
                }
            }


        }

        // errores & snack
        error?.let {
            LaunchedEffect(it) { snackbar.showSnackbar(it); error = null }
        }

        // Diálogo de descuento
        if (abrirDialogoDescuento) {
            DescuentoDialog(
                pctInicial = descuentoGlobalPct,
                eurInicial = descuentoGlobalEur,
                subtotal = subtotalCarrito(),
                onCerrar = { abrirDialogoDescuento = false },
                onAplicar = { pct, eur ->
                    descuentoGlobalPct = pct
                    descuentoGlobalEur = eur
                    abrirDialogoDescuento = false
                },
                onQuitarTodo = {
                    descuentoGlobalPct = 0.0
                    descuentoGlobalEur = 0.0
                    abrirDialogoDescuento = false
                }
            )
        }

        // Resumen post-cobro + ticket
        if (resumenAbierto && ultimaVenta != null) {
            AnimatedVisibility(true, enter = fadeIn(), exit = fadeOut()) {
                AlertDialog(
                    onDismissRequest = { resumenAbierto = false },
                    title = { Text("Venta completada") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Método: ${ultimaVenta!!.metodoPago}")
                            Divider()
                            ultimaVenta!!.items.forEach { item ->
                                val prod = productos.find { it.id == item.productoId } ?: return@forEach
                                val pu = item.precioEspecial ?: prod.precio
                                Text("${prod.nombre}  ·  ${item.cantidad} x ${money(pu)}")
                            }
                            Divider()
                            val totalVenta = ultimaVenta!!.items.sumOf {
                                it.cantidad * (it.precioEspecial
                                    ?: (productos.find { p -> p.id == it.productoId }?.precio ?: 0.0))
                            }
                            Text(
                                "Total: ${money(totalVenta)}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    },
                    confirmButton = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                val archivo = File("ticket_${System.currentTimeMillis()}.pdf")
                                generarTicketPDF(ultimaVenta!!, productos, archivo)
                                Desktop.getDesktop().open(archivo)
                                resumenAbierto = false
                            }) { Text("Imprimir ticket") }
                            TextButton(onClick = { resumenAbierto = false }) { Text("Cerrar") }
                        }
                    }
                )
            }
        }
    }
}

/* ---------- Componentes ---------- */

@Composable
private fun LineaCarrito(
    nombre: String,
    precio: Double,
    cantidad: Int,
    stock: Int,
    onMas: () -> Unit,
    onMenos: () -> Unit,
    onEliminar: () -> Unit
) {
    ElevatedCard {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(nombre, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(money(precio))
                    Spacer(Modifier.width(10.dp))
                    AssistChip(onClick = {}, label = { Text("Stock $stock") })
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onMenos) { Icon(Icons.Default.Remove, null) }
                Text(cantidad.toString(), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onMas) { Icon(Icons.Default.Add, null) }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun ProductoCard(
    nombre: String,
    precio: Double,
    stock: Int,
    imagenUrl: String?,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(
            Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SubcomposeAsyncImage(
                model = when {
                    imagenUrl == null -> null
                    imagenUrl.startsWith("http") -> imagenUrl
                    else -> File(imagenUrl)
                },
                contentDescription = nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(75.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium),
                loading = {
                    Box(
                        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(modifier = Modifier.size(20.dp)) }
                },
                error = {
                    Box(
                        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.ImageNotSupported, null) }
                }
            )
            Spacer(Modifier.height(4.dp))
            Text(nombre, maxLines = 1, style = MaterialTheme.typography.bodySmall)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(money(precio), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                if (stock <= 3) {
                    AssistChip(
                        onClick = {},
                        label = { Text(if (stock == 0) "Sin stock" else "Quedan $stock", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BarraCobro(
    subtotal: Double,
    descuentoPct: Double,
    descuentoEur: Double,
    descuentoCalculado: Double,
    total: Double,
    metodoPago: String,
    onMetodo: (String) -> Unit,
    efectivoEntregado: Double?,
    onEfectivo: (Double?) -> Unit,
    onAbrirDescuento: () -> Unit,
    onCobrar: () -> Unit,
    cobrarHabilitado: Boolean
) {
    var efectivoText by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    LaunchedEffect(efectivoEntregado) {
        if (efectivoEntregado == null && efectivoText.isNotEmpty()) efectivoText = ""
    }

    val teal   = MaterialTheme.colorScheme.tertiary
    val onTeal = MaterialTheme.colorScheme.onTertiary

    Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── ZONA IZQUIERDA: descuento + efectivo entregado ──
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Subtotal
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(money(subtotal), style = MaterialTheme.typography.bodyMedium)
                }
                if (descuentoCalculado > 0.0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Descuento", style = MaterialTheme.typography.bodyMedium, color = teal)
                        Text("-${money(descuentoCalculado)}", style = MaterialTheme.typography.bodyMedium, color = teal)
                    }
                }

                // Botón descuento
                FilledTonalButton(onClick = onAbrirDescuento, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.LocalOffer, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    val resumen = when {
                        descuentoPct > 0 && descuentoEur > 0 -> "${"%.0f".format(descuentoPct)}% + ${money(descuentoEur)}"
                        descuentoPct > 0 -> "${"%.0f".format(descuentoPct)}%"
                        descuentoEur > 0 -> money(descuentoEur)
                        else -> "Aplicar descuento"
                    }
                    Text(resumen)
                }

                // Efectivo entregado (visible solo si método = EFECTIVO)
                AnimatedVisibility(metodoPago == "EFECTIVO") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = efectivoText,
                            onValueChange = { newText ->
                                val clean = newText.filter { it.isDigit() || it == '.' || it == ',' }
                                efectivoText = clean
                                onEfectivo(clean.replace(',', '.').toDoubleOrNull())
                            },
                            label = { Text("Efectivo entregado (€)") },
                            leadingIcon = { Icon(Icons.Default.Payments, null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(5, 10, 20, 50).forEach { b ->
                                AssistChip(
                                    onClick = {
                                        val nuevo = (efectivoEntregado ?: 0.0) + b
                                        onEfectivo(nuevo)
                                        efectivoText = "%.2f".format(nuevo)
                                    },
                                    label = { Text("+$b€") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        val diff = (efectivoEntregado ?: 0.0) - total
                        if (efectivoEntregado != null) {
                            val (textoCambio, colorCambio) =
                                if (diff >= 0) "Cambio: ${money(diff)}" to Color(0xFF2E7D32)
                                else "Faltan: ${money(-diff)}" to MaterialTheme.colorScheme.error
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    textoCambio,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colorCambio
                                )
                            }
                        }
                    }
                }
            }

            // Separador vertical
            Box(
                Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            // ── ZONA DERECHA: total + métodos de pago + finalizar ──
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // TOTAL prominente en teal
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = teal.copy(alpha = 0.10f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "TOTAL A PAGAR",
                            style = MaterialTheme.typography.labelLarge,
                            color = teal
                        )
                        Text(
                            money(total),
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = teal
                        )
                    }
                }

                // Métodos de pago — botones grandes con icono
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("EFECTIVO", Icons.Default.Payments,    "Efectivo"),
                        Triple("TARJETA",  Icons.Default.CreditCard,  "Tarjeta"),
                        Triple("BIZUM",    Icons.Default.PhoneAndroid, "Bizum"),
                    ).forEach { (nombre, icono, label) ->
                        val selected = metodoPago == nombre
                        ElevatedButton(
                            onClick = { onMetodo(nombre) },
                            modifier = Modifier.weight(1f).height(60.dp),
                            colors = if (selected)
                                ButtonDefaults.elevatedButtonColors(containerColor = teal, contentColor = onTeal)
                            else
                                ButtonDefaults.elevatedButtonColors()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(icono, null, Modifier.size(20.dp))
                                Text(label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // Botón finalizar — teal sólido
                Button(
                    onClick = onCobrar,
                    enabled = cobrarHabilitado,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = teal, contentColor = onTeal)
                ) {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Finalizar venta", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}


/* ---------- Diálogo de descuento ---------- */

@Composable
private fun DescuentoDialog(
    pctInicial: Double,
    eurInicial: Double,
    subtotal: Double,
    onCerrar: () -> Unit,
    onAplicar: (Double, Double) -> Unit,
    onQuitarTodo: () -> Unit
) {
    var pctText by remember { mutableStateOf(if (pctInicial == 0.0) "" else "%.0f".format(pctInicial)) }
    var eurText by remember { mutableStateOf(if (eurInicial == 0.0) "" else "%.2f".format(eurInicial)) }

    val pct = pctText.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.0
    val eur = eurText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val descuento = (subtotal * (pct / 100.0) + eur).coerceAtMost(subtotal)

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Descuento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = pctText,
                    onValueChange = { pctText = it },
                    label = { Text("Porcentaje (%)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                )
                OutlinedTextField(
                    value = eurText,
                    onValueChange = { eurText = it },
                    label = { Text("Importe (€)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Divider()
                Text("Aplicado: -${money(descuento)}")
                Text("Total provisional: ${money((subtotal - descuento).coerceAtLeast(0.0))}")
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onQuitarTodo) { Text("Quitar") }
                TextButton(onClick = { onAplicar(pct, eur) }) { Text("Aplicar") }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCerrar) { Text("Cancelar") }
        }
    )
}

/* ---------- Helpers de dinero ---------- */

private fun money(amount: Double): String =
    formatMoneyCommon(amount = amount, symbol = "€", thousandSep = '.', decimalSep = ',', decimals = 2)

private fun formatMoneyCommon(
    amount: Double,
    symbol: String = "€",
    thousandSep: Char = '.',
    decimalSep: Char = ',',
    decimals: Int = 2
): String {
    val neg = amount < 0
    val absVal = kotlin.math.abs(amount)

    // Escala como entero: 10^decimals (evita Double.pow)
    val scale = pow10L(decimals)

    // Redondeo a "decimals" sin usar formateadores de Java
    val scaled = kotlin.math.floor(absVal * scale + 0.5).toLong()

    val intPart = scaled / scale
    val fracPart = (scaled % scale).toInt()

    val intStr = intPart
        .toString()
        .reversed()
        .chunked(3)
        .joinToString(thousandSep.toString())
        .reversed()

    val fracStr = fracPart.toString().padStart(decimals, '0')
    val sign = if (neg) "-" else ""
    return "$sign$symbol $intStr$decimalSep$fracStr"
}

private fun pow10L(n: Int): Long {
    var s = 1L
    repeat(n) { s *= 10L }
    return s
}
