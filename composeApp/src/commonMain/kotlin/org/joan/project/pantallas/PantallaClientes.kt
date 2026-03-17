package org.joan.project.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.joan.project.db.entidades.ClienteResponse
import org.joan.project.viewmodel.AuthViewModel
import org.joan.project.viewmodel.ClienteViewModel
import org.koin.compose.koinInject

@Composable
fun PantallaClientes(
    authViewModel: AuthViewModel = koinInject(),
    clienteViewModel: ClienteViewModel = koinInject(),
    onVolverClick: () -> Unit
) {
    val token    = authViewModel.token.value
    val clientes by clienteViewModel.clientes.collectAsState()

    var query   by remember { mutableStateOf("") }
    var sortAsc by remember { mutableStateOf(true) }

    LaunchedEffect(token) {
        token?.let { clienteViewModel.cargarClientes(it) }
    }

    val listaFiltrada by remember(query, sortAsc, clientes) {
        derivedStateOf {
            clientes
                .filter { c ->
                    query.isBlank() ||
                    c.nombre.contains(query, ignoreCase = true) ||
                    c.telefono?.contains(query, ignoreCase = true) == true
                }
                .sortedBy { it.nombre.lowercase() }
                .let { if (sortAsc) it else it.reversed() }
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isSmall = maxWidth < 1024.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isSmall) 10.dp else 18.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onVolverClick) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    "Clientes",
                    style    = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                )
                // Badge con total
                if (clientes.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            "${clientes.size}",
                            style    = MaterialTheme.typography.labelMedium,
                            color    = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Búsqueda + orden ─────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value         = query,
                    onValueChange = { query = it },
                    leadingIcon   = { Icon(Icons.Outlined.Search, null) },
                    trailingIcon  = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Outlined.Cancel, null)
                            }
                        }
                    },
                    placeholder = { Text("Buscar por nombre o teléfono…") },
                    singleLine  = true,
                    modifier    = Modifier.weight(1f)
                )
                Spacer(Modifier.width(10.dp))
                FilledTonalIconButton(onClick = { sortAsc = !sortAsc }) {
                    Icon(
                        if (sortAsc) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
                        contentDescription = "Orden"
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Lista ─────────────────────────────────────────────────────────
            when {
                clientes.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                listaFiltrada.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint     = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Sin resultados para \"$query\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier            = Modifier.weight(1f)
                    ) {
                        items(listaFiltrada, key = { it.id }) { cliente ->
                            ClienteCard(cliente)
                        }
                    }
                }
            }
        }
    }
}

// ── Tarjeta de cliente ────────────────────────────────────────────────────────

@Composable
private fun ClienteCard(cliente: ClienteResponse) {
    // Color de avatar basado en la inicial (paleta fija de 6 colores)
    val avatarColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        androidx.compose.ui.graphics.Color(0xFF00796B),
        androidx.compose.ui.graphics.Color(0xFF1565C0),
        androidx.compose.ui.graphics.Color(0xFF4527A0)
    )
    val color = remember(cliente.nombre) {
        avatarColors[cliente.nombre.first().uppercaseChar().code % avatarColors.size]
    }

    ElevatedCard(
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.elevatedCardElevation(3.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con inicial
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    cliente.nombre.firstOrNull()?.uppercase() ?: "?",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = color
                )
            }

            Spacer(Modifier.width(12.dp))

            // Nombre + teléfono
            Column(Modifier.weight(1f)) {
                Text(
                    cliente.nombre,
                    style     = MaterialTheme.typography.titleSmall,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                val telefono = cliente.telefono
                if (telefono != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            telefono,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        "Sin teléfono",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // ID badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    "#${cliente.id}",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
