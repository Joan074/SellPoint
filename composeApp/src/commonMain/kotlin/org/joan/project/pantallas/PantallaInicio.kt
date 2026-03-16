package org.joan.project.pantallas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun PantallaInicio(
    onSeleccion: (Pantalla) -> Unit,
    onCerrarSesion: () -> Unit,
    userName: String? = "Joan",

    // Acciones rápidas
    onCrearProducto: () -> Unit = {},
    onNuevaVenta: () -> Unit = { onSeleccion(Pantalla.Cobrar) },
    onSincronizar: () -> Unit = {},

    // KPIs (pásalos desde tus VM)
    totalProductos: Int = 0,
    valorInventario: Double = 0.0,
    bajoStock: Int = 0,
    ventasHoy: Double = 0.0,
    currency: String = "€"
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 20.dp)
        ) {
            InicioHeader(userName = userName, onCerrarSesion = onCerrarSesion)

            Spacer(Modifier.height(10.dp))

            // KPIs
            StatsRow(
                totalProductos = totalProductos,
                valorInventario = valorInventario,
                bajoStock = bajoStock,
                ventasHoy = ventasHoy,
                currency = currency
            )

            Spacer(Modifier.height(14.dp))

            QuickActionsRow(
                onCrearProducto = onCrearProducto,
                onNuevaVenta = onNuevaVenta,
                onSincronizar = onSincronizar
            )

            Spacer(Modifier.height(18.dp))

            Text("Menú Principal", style = MaterialTheme.typography.headlineSmall)

            Spacer(Modifier.height(12.dp))

            val items = remember {
                listOf(
                    MenuItem("Cobrar",      "Nueva venta",           Icons.Default.PointOfSale,   Pantalla.Cobrar,       Color(0xFF00796B)),
                    MenuItem("Productos",   "Gestión y catálogo",    Icons.Default.Inventory,     Pantalla.Listado,      Color(0xFF1565C0)),
                    MenuItem("Proveedores", "Compras y contactos",   Icons.Default.LocalShipping, Pantalla.Proveedores,  Color(0xFFE65100)),
                    MenuItem("Clientes",    "Fichas y fidelización", Icons.Default.Person,        Pantalla.Clientes,     Color(0xFF4527A0)),
                    MenuItem("Reportes",    "Ventas y stock",        Icons.Default.BarChart,      Pantalla.ReporteVentas,Color(0xFF2E7D32)),
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(220.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(items) { it ->
                    ActionCard(
                        title = it.title,
                        subtitle = it.subtitle,
                        icon = it.icon,
                        accentColor = it.accentColor,
                        onClick = { onSeleccion(it.dest) }
                    )
                }
            }

        }
    }
}

private data class MenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val dest: Pantalla,
    val accentColor: Color
)

@Composable
private fun InicioHeader(userName: String?, onCerrarSesion: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Saludo en lugar del título duplicado
        Column {
            Text(
                "Bienvenido",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(userName ?: "Usuario", style = MaterialTheme.typography.titleLarge)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { /* ajustes */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Ajustes")
            }
            Spacer(Modifier.width(6.dp))

            // Avatar con menú desplegable
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
                            text = (userName?.firstOrNull()?.uppercase() ?: "?"),
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Cerrar sesión") },
                        leadingIcon = { Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { menuExpanded = false; onCerrarSesion() }
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
    currency: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KpiCard(
            icon = Icons.Default.Inventory,
            label = "Productos",
            value = totalProductos.toString(),
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            icon = Icons.Default.AttachMoney,
            label = "Inventario",
            value = formatMoney(valorInventario, currency),
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            icon = Icons.Default.Warning,
            label = "Bajo stock",
            value = bajoStock.toString(),
            tone = KpiTone.Warning,
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            icon = Icons.Default.PointOfSale,
            label = "Ventas hoy",
            value = formatMoney(ventasHoy, currency),
            modifier = Modifier.weight(1f)
        )
    }
}

private enum class KpiTone { Normal, Warning }

@Composable
private fun KpiCard(
    icon: ImageVector,
    label: String,
    value: String,
    tone: KpiTone = KpiTone.Normal,
    modifier: Modifier = Modifier
) {
    val bg = if (tone == KpiTone.Warning)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.secondaryContainer

    val fg = if (tone == KpiTone.Warning)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onSecondaryContainer

    val iconTint = if (tone == KpiTone.Warning)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.tertiary

    ElevatedCard(
        modifier = modifier.height(82.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.elevatedCardElevation(3.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = bg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = .15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = fg.copy(alpha = .85f)
                )
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    color = fg
                )
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(
            onClick = onCrearProducto,
            label = { Text("Crear producto") },
            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
        )
        AssistChip(
            onClick = onNuevaVenta,
            label = { Text("Nueva venta") },
            leadingIcon = { Icon(Icons.Default.PointOfSale, contentDescription = null) }
        )
        AssistChip(
            onClick = onSincronizar,
            label = { Text("Sincronizar") },
            leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null) }
        )
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "press-scale")

    ElevatedCard(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .height(160.dp)
            .clickable(interactionSource = interaction, indication = LocalIndication.current) { onClick() },
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.elevatedCardElevation(10.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        // Barra de acento superior con el color de la sección
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(accentColor)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BoxScope.BackgroundDecor() {
    Box(
        Modifier
            .size(280.dp)
            .offset(x = (-60).dp, y = (-80).dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = .07f))
    )
    Box(
        Modifier
            .size(340.dp)
            .align(Alignment.BottomEnd)
            .offset(x = 60.dp, y = 60.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = .07f))
    )
}