package org.joan.project.pantallas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.db.entidades.VentaResponse

@Composable
fun PantallaInicio(
    onSeleccion: (Pantalla) -> Unit,
    onCerrarSesion: () -> Unit,
    userName: String? = null,
    onAjustesClick: () -> Unit = {},

    // Acciones rápidas
    onCrearProducto: () -> Unit = {},
    onNuevaVenta: () -> Unit = { onSeleccion(Pantalla.Cobrar) },
    onSincronizar: () -> Unit = {},

    // KPIs
    totalProductos: Int = 0,
    valorInventario: Double = 0.0,
    bajoStock: Int = 0,
    ventasHoy: Double = 0.0,
    currency: String = "€",

    // Dashboard
    ultimasVentasHoy: List<VentaResponse> = emptyList(),
    productosBajoStock: List<ProductoResponse> = emptyList()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = .05f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = .05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        BackgroundDecor()

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val isSmall  = maxWidth < 1024.dp
            val numCols  = (maxWidth / 220.dp).toInt().coerceIn(1, 4)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 20.dp)
            ) {
                InicioHeader(
                    userName       = userName,
                    onCerrarSesion = onCerrarSesion,
                    onAjustesClick = onAjustesClick
                )

                Spacer(Modifier.height(10.dp))

                StatsRow(
                    totalProductos  = totalProductos,
                    valorInventario = valorInventario,
                    bajoStock       = bajoStock,
                    ventasHoy       = ventasHoy,
                    currency        = currency,
                    isSmall         = isSmall
                )

                Spacer(Modifier.height(14.dp))

                QuickActionsRow(
                    onCrearProducto = onCrearProducto,
                    onNuevaVenta    = onNuevaVenta,
                    onSincronizar   = onSincronizar
                )

                Spacer(Modifier.height(18.dp))

                Text("Menú Principal", style = MaterialTheme.typography.headlineSmall)

                Spacer(Modifier.height(12.dp))

                // ── Menú (grid manual para poder usar verticalScroll) ──────────
                val menuItems = remember {
                    listOf(
                        MenuItem("Cobrar",      "Nueva venta",           Icons.Default.PointOfSale,   Pantalla.Cobrar,        Color(0xFF00796B)),
                        MenuItem("Productos",   "Gestión y catálogo",    Icons.Default.Inventory,     Pantalla.Listado,       Color(0xFF1565C0)),
                        MenuItem("Proveedores", "Compras y contactos",   Icons.Default.LocalShipping, Pantalla.Proveedores,   Color(0xFFE65100)),
                        MenuItem("Clientes",    "Fichas y fidelización", Icons.Default.Person,        Pantalla.Clientes,      Color(0xFF4527A0)),
                        MenuItem("Reportes",    "Ventas y stock",        Icons.Default.BarChart,      Pantalla.ReporteVentas, Color(0xFF2E7D32)),
                    )
                }

                val filas = menuItems.chunked(numCols)
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    filas.forEach { fila ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            fila.forEach { item ->
                                ActionCard(
                                    title       = item.title,
                                    subtitle    = item.subtitle,
                                    icon        = item.icon,
                                    accentColor = item.accentColor,
                                    onClick     = { onSeleccion(item.dest) },
                                    modifier    = Modifier.weight(1f)
                                )
                            }
                            // Rellena huecos en la última fila
                            repeat(numCols - fila.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ── Dashboard: últimas ventas + bajo stock ─────────────────────
                Text("Resumen del día", style = MaterialTheme.typography.headlineSmall)

                Spacer(Modifier.height(12.dp))

                if (isSmall) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        UltimasVentasCard(
                            ventas    = ultimasVentasHoy,
                            onClick   = { onSeleccion(Pantalla.ReporteVentas) },
                            modifier  = Modifier.fillMaxWidth()
                        )
                        BajoStockCard(
                            productos = productosBajoStock,
                            onClick   = { onSeleccion(Pantalla.Listado) },
                            modifier  = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        UltimasVentasCard(
                            ventas   = ultimasVentasHoy,
                            onClick  = { onSeleccion(Pantalla.ReporteVentas) },
                            modifier = Modifier.weight(1f)
                        )
                        BajoStockCard(
                            productos = productosBajoStock,
                            onClick   = { onSeleccion(Pantalla.Listado) },
                            modifier  = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ── Últimas ventas ─────────────────────────────────────────────────────────────

@Composable
private fun UltimasVentasCard(
    ventas: List<VentaResponse>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DashboardCard(
        titulo   = "Últimas ventas",
        icono    = Icons.Default.PointOfSale,
        accion   = "Ver reportes",
        onClick  = onClick,
        modifier = modifier
    ) {
        if (ventas.isEmpty()) {
            EmptyState("Sin ventas registradas hoy")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ventas.forEach { venta ->
                    VentaRow(venta)
                }
            }
        }
    }
}

@Composable
private fun VentaRow(venta: VentaResponse) {
    val hora = remember(venta.fecha) {
        venta.fecha.substringAfter("T", "").take(5).ifBlank { "--:--" }
    }
    val resumen = remember(venta.items) {
        when {
            venta.items.isEmpty() -> venta.numeroTicket ?: "Venta #${venta.id}"
            else -> venta.items.take(2).joinToString(", ") {
                if (it.cantidad > 1) "${it.nombre} ×${it.cantidad}" else it.nombre
            } + if (venta.items.size > 2) " +${venta.items.size - 2}" else ""
        }
    }
    val metodoPagoColor = when (venta.metodoPago.uppercase()) {
        "TARJETA" -> Color(0xFF1565C0)
        "BIZUM"   -> Color(0xFF00796B)
        else      -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Hora
        Text(
            hora,
            style     = MaterialTheme.typography.labelMedium,
            fontWeight= FontWeight.Medium,
            color     = MaterialTheme.colorScheme.primary,
            modifier  = Modifier.width(40.dp)
        )
        Spacer(Modifier.width(8.dp))
        // Descripción
        Text(
            resumen,
            style    = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        // Método de pago (chip pequeño)
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = metodoPagoColor.copy(alpha = 0.12f)
        ) {
            Text(
                venta.metodoPago.take(3).uppercase(),
                style   = MaterialTheme.typography.labelSmall,
                color   = metodoPagoColor,
                modifier= Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        // Total
        Text(
            "${"%.2f".format(venta.total)} €",
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── Bajo stock ────────────────────────────────────────────────────────────────

@Composable
private fun BajoStockCard(
    productos: List<ProductoResponse>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DashboardCard(
        titulo   = "Bajo stock",
        icono    = Icons.Default.Warning,
        accion   = "Ver productos",
        onClick  = onClick,
        modifier = modifier
    ) {
        if (productos.isEmpty()) {
            EmptyState("Todos los productos tienen stock suficiente")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                productos.forEach { prod ->
                    StockRow(prod)
                }
            }
        }
    }
}

@Composable
private fun StockRow(producto: ProductoResponse) {
    val critico = producto.stock < 5
    val stockColor = if (critico)
        MaterialTheme.colorScheme.error
    else
        Color(0xFFE65100)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Icono estado
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(stockColor.copy(alpha = .12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (critico) Icons.Default.Error else Icons.Default.Warning,
                contentDescription = null,
                tint     = stockColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        // Nombre
        Text(
            producto.nombre,
            style    = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        // Badge de stock
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = stockColor.copy(alpha = if (critico) 0.18f else 0.10f)
        ) {
            Text(
                "${producto.stock} ud.",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color      = stockColor,
                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

// ── Contenedor genérico de tarjeta de dashboard ────────────────────────────────

@Composable
private fun DashboardCard(
    titulo: String,
    icono: ImageVector,
    accion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        onClick   = onClick,
        shape     = RoundedCornerShape(18.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        modifier  = modifier
    ) {
        Column(Modifier.padding(16.dp)) {
            // Encabezado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    icono,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    titulo,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.weight(1f)
                )
                Text(
                    accion,
                    style  = MaterialTheme.typography.labelSmall,
                    color  = MaterialTheme.colorScheme.primary
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            content()
        }
    }
}

@Composable
private fun EmptyState(mensaje: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            mensaje,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Componentes existentes (sin cambios salvo modifier en ActionCard)
// ─────────────────────────────────────────────────────────────────────────────

private data class MenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val dest: Pantalla,
    val accentColor: Color
)

@Composable
private fun InicioHeader(userName: String?, onCerrarSesion: () -> Unit, onAjustesClick: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Bienvenido",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(userName ?: "Usuario", style = MaterialTheme.typography.titleLarge)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onAjustesClick) {
                Icon(Icons.Default.Settings, contentDescription = "Ajustes del negocio")
            }
            Spacer(Modifier.width(6.dp))

            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { menuExpanded = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = .15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = (userName?.firstOrNull()?.uppercase() ?: "?"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(userName ?: "Usuario", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded          = menuExpanded,
                    onDismissRequest  = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text        = { Text("Cerrar sesión") },
                        leadingIcon = { Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error) },
                        onClick     = { menuExpanded = false; onCerrarSesion() }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(
    totalProductos: Int,
    valorInventario: Double,
    bajoStock: Int,
    ventasHoy: Double,
    currency: String,
    isSmall: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KpiCard(icon = Icons.Default.Inventory,   label = "Productos",    value = totalProductos.toString(),           isSmall = isSmall, modifier = Modifier.weight(1f))
        KpiCard(icon = Icons.Default.AttachMoney, label = "Inventario",   value = formatMoney(valorInventario, currency), isSmall = isSmall, modifier = Modifier.weight(1f))
        KpiCard(icon = Icons.Default.Warning,     label = "Bajo stock",   value = bajoStock.toString(),                tone = KpiTone.Warning, isSmall = isSmall, modifier = Modifier.weight(1f))
        KpiCard(icon = Icons.Default.PointOfSale, label = "Ventas hoy",   value = formatMoney(ventasHoy, currency),    isSmall = isSmall, modifier = Modifier.weight(1f))
    }
}

private enum class KpiTone { Normal, Warning }

@Composable
private fun KpiCard(
    icon: ImageVector,
    label: String,
    value: String,
    tone: KpiTone = KpiTone.Normal,
    isSmall: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bg       = if (tone == KpiTone.Warning) MaterialTheme.colorScheme.errorContainer     else MaterialTheme.colorScheme.secondaryContainer
    val fg       = if (tone == KpiTone.Warning) MaterialTheme.colorScheme.onErrorContainer   else MaterialTheme.colorScheme.onSecondaryContainer
    val iconTint = if (tone == KpiTone.Warning) MaterialTheme.colorScheme.onErrorContainer   else MaterialTheme.colorScheme.tertiary

    ElevatedCard(
        modifier  = modifier.height(if (isSmall) 64.dp else 82.dp),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.elevatedCardElevation(3.dp),
        colors    = CardDefaults.elevatedCardColors(containerColor = bg)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(iconTint.copy(alpha = .15f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = iconTint) }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = fg.copy(alpha = .85f))
                Text(value, style = MaterialTheme.typography.titleMedium, color = fg)
            }
        }
    }
}

private fun formatMoney(amount: Double, currency: String): String =
    "$currency ${"%.2f".format(amount)}"

@Composable
private fun QuickActionsRow(
    onCrearProducto: () -> Unit,
    onNuevaVenta: () -> Unit,
    onSincronizar: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        AssistChip(onClick = onCrearProducto, label = { Text("Crear producto") }, leadingIcon = { Icon(Icons.Default.Add, null) })
        AssistChip(onClick = onNuevaVenta,    label = { Text("Nueva venta") },    leadingIcon = { Icon(Icons.Default.PointOfSale, null) })
        AssistChip(onClick = onSincronizar,   label = { Text("Sincronizar") },    leadingIcon = { Icon(Icons.Default.Sync, null) })
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "press-scale")

    ElevatedCard(
        modifier  = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .height(160.dp)
            .clickable(interactionSource = interaction, indication = LocalIndication.current) { onClick() },
        shape     = RoundedCornerShape(22.dp),
        elevation = CardDefaults.elevatedCardElevation(10.dp),
        colors    = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(5.dp).background(accentColor))
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(14.dp)).background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = accentColor) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BoxScope.BackgroundDecor() {
    Box(Modifier.size(280.dp).offset(x = (-60).dp, y = (-80).dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = .07f)))
    Box(Modifier.size(340.dp).align(Alignment.BottomEnd).offset(x = 60.dp, y = 60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary.copy(alpha = .07f)))
}
