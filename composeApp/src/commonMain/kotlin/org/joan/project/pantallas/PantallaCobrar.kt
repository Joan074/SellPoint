package org.joan.project.pantallas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import org.joan.project.db.entidades.LineaVenta
import org.joan.project.db.entidades.VentaRequest
import org.joan.project.viewmodel.AuthViewModel
import org.joan.project.viewmodel.ProductoViewModel
import org.joan.project.viewmodel.VentaViewModel
import org.joan.project.visual.generarTicketPDF
import org.koin.compose.koinInject
import java.awt.Desktop
import java.io.File
import javax.imageio.ImageIO

@Composable
fun PantallaCobrar(
    productoViewModel: ProductoViewModel = koinInject(),
    authViewModel: AuthViewModel = koinInject(),
    ventaViewModel: VentaViewModel = koinInject(),
    onVolverClick: () -> Unit
) {
    val productos by productoViewModel.productos.collectAsState()
    val token = authViewModel.token.value
    val currentUser = authViewModel.currentUser.collectAsState().value

    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    val carrito = remember { mutableStateListOf<LineaVenta>() }
    val imagenes = remember { mutableStateMapOf<Int, ImageBitmap?>() }

    val categorias = productos.map { it.categoria.nombre }.distinct().sorted()
    val productosFiltrados = productos.filter {
        categoriaSeleccionada == null || it.categoria.nombre == categoriaSeleccionada
    }

    val total by derivedStateOf { carrito.sumOf { it.subtotal } }

    var metodoPago by remember { mutableStateOf("EFECTIVO") }
    var errorVenta by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    var mostrarSnackbar by remember { mutableStateOf(false) }
    var mostrarResumenVenta by remember { mutableStateOf(false) }
    var ultimaVenta by remember { mutableStateOf<VentaRequest?>(null) }

    LaunchedEffect(Unit) {
        if (token != null && productos.isEmpty()) {
            productoViewModel.cargarProductos(token)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            IconButton(onClick = onVolverClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Text(
                "Punto de Venta",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Divider()

        Row(Modifier.weight(1f)) {
            // Panel izquierdo: carrito y pago
            Column(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f, fill = true)) {
                    Text("Carrito:", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    carrito.forEach { linea ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(linea.producto.nombre)
                                Text("${linea.cantidad} x %.2f€".format(linea.producto.precio))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    val index = carrito.indexOf(linea)
                                    if (linea.cantidad > 1) {
                                        carrito[index] = linea.copy(cantidad = linea.cantidad - 1)
                                    } else {
                                        carrito.removeAt(index)
                                    }
                                }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Restar")
                                }
                                IconButton(onClick = {
                                    val index = carrito.indexOf(linea)
                                    carrito[index] = linea.copy(cantidad = linea.cantidad + 1)
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = "Sumar")
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Divider()
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total:", style = MaterialTheme.typography.titleMedium)
                        Text("%.2f €".format(total), style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Método de pago:", style = MaterialTheme.typography.bodyMedium)
                        listOf("EFECTIVO", "TARJETA", "BIZUM").forEach { metodo ->
                            FilterChip(
                                selected = metodoPago == metodo,
                                onClick = { metodoPago = metodo },
                                label = { Text(metodo) }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { carrito.clear() },
                            modifier = Modifier.weight(1f),
                            enabled = carrito.isNotEmpty()
                        ) {
                            Text("Vaciar carrito")
                        }

                        Button(
                            onClick = {
                                if (token != null && currentUser != null && carrito.isNotEmpty()) {
                                    val ventaRequest = VentaRequest(
                                        clienteId = null,
                                        empleadoId = currentUser.id,
                                        metodoPago = metodoPago,
                                        descuento = 0.0,
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
                                        token = token,
                                        ventaRequest = ventaRequest,
                                        onSuccess = {
                                            carrito.clear()
                                            categoriaSeleccionada = null
                                            ultimaVenta = ventaRequest
                                            mostrarResumenVenta = true
                                            mostrarSnackbar = true
                                        },
                                        onError = {
                                            errorVenta = it
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = carrito.isNotEmpty()
                        ) {
                            Text("Finalizar venta")
                        }
                    }
                }
            }

            // Panel derecho: productos y filtros
            Column(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight()
                    .padding(12.dp)
            ) {
                Text("Categorías:", style = MaterialTheme.typography.titleMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = categoriaSeleccionada == null,
                        onClick = { categoriaSeleccionada = null },
                        label = { Text("Todas") }
                    )
                    categorias.forEach { cat ->
                        FilterChip(
                            selected = categoriaSeleccionada == cat,
                            onClick = { categoriaSeleccionada = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(140.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(productosFiltrados) { producto ->
                        val imageBitmap = remember(producto.id) {
                            imagenes.getOrPut(producto.id) {
                                try {
                                    producto.imagenUrl?.let { path ->
                                        val file = File(path)
                                        if (file.exists()) ImageIO.read(file)?.toComposeImageBitmap()
                                        else null
                                    }
                                } catch (_: Exception) {
                                    null
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clickable {
                                    val index = carrito.indexOfFirst { it.producto.id == producto.id }
                                    if (index >= 0) {
                                        val anterior = carrito[index]
                                        carrito[index] = anterior.copy(cantidad = anterior.cantidad + 1)
                                    } else {
                                        carrito.add(LineaVenta(producto, 1))
                                    }
                                },
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (imageBitmap != null) {
                                    Image(
                                        painter = BitmapPainter(imageBitmap),
                                        contentDescription = producto.nombre,
                                        modifier = Modifier
                                            .height(80.dp)
                                            .fillMaxWidth()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .height(80.dp)
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Sin imagen", style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                Spacer(Modifier.height(6.dp))
                                Text(producto.nombre, style = MaterialTheme.typography.bodyMedium)
                                Text("${producto.precio} €", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        if (errorVenta != null) {
            LaunchedEffect(errorVenta) {
                snackbarHostState.showSnackbar("Error: $errorVenta")
                errorVenta = null
            }
        }

        if (mostrarSnackbar) {
            LaunchedEffect(Unit) {
                snackbarHostState.showSnackbar("Venta registrada correctamente")
                mostrarSnackbar = false
            }
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            SnackbarHost(snackbarHostState)
        }

        if (mostrarResumenVenta && ultimaVenta != null) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AlertDialog(
                    onDismissRequest = { mostrarResumenVenta = false },
                    confirmButton = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                val archivo = File("ticket_${System.currentTimeMillis()}.pdf")
                                generarTicketPDF(
                                    venta = ultimaVenta!!,
                                    productos = productos,
                                    archivo = archivo
                                )
                                Desktop.getDesktop().open(archivo)
                                mostrarResumenVenta = false
                            }) {
                                Text("Imprimir ticket")
                            }
                            TextButton(onClick = { mostrarResumenVenta = false }) {
                                Text("No Imprimir")
                            }

                        }
                    },
                    title = { Text(" Venta completada", style = MaterialTheme.typography.titleLarge) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Método de pago: ${ultimaVenta!!.metodoPago}", style = MaterialTheme.typography.bodyMedium)
                            Divider(Modifier.padding(vertical = 4.dp))

                            ultimaVenta!!.items.forEach { item ->
                                val producto = productos.find { it.id == item.productoId }
                                if (producto != null) {
                                    val precioUnitario = item.precioEspecial ?: producto.precio
                                    val subtotal = precioUnitario * item.cantidad
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(producto.nombre, style = MaterialTheme.typography.bodyMedium)
                                            Text("${item.cantidad} x %.2f €".format(precioUnitario), style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text("= %.2f €".format(subtotal), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }

                            Divider(Modifier.padding(vertical = 4.dp))

                            val totalVenta = ultimaVenta!!.items.sumOf {
                                it.cantidad * (it.precioEspecial
                                    ?: productos.find { p -> p.id == it.productoId }?.precio ?: 0.0)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total:", style = MaterialTheme.typography.titleMedium)
                                Text("%.2f €".format(totalVenta), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                )
            }
        }
    }
}