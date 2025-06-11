package org.joan.project.pantallas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PantallaInicio(
    onSeleccion: (Pantalla) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Menú Principal",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                TarjetaMenu("Productos", Icons.Default.Inventory, Pantalla.Listado, onSeleccion)
            }
            item {
                TarjetaMenu("Cobrar", Icons.Default.PointOfSale, Pantalla.Cobrar, onSeleccion)
            }
            item {
                TarjetaMenu("Proveedores", Icons.Default.LocalShipping, Pantalla.Proveedores, onSeleccion)
            }
            item {
                TarjetaMenu("Clientes", Icons.Default.Person, Pantalla.Clientes, onSeleccion)
            }
            item {
                TarjetaMenu("Reportes", Icons.Default.BarChart, Pantalla.Inicio, onSeleccion) // Aún no implementada
            }
        }
    }
}

@Composable
fun TarjetaMenu(
    titulo: String,
    icono: ImageVector,
    destino: Pantalla,
    onClick: (Pantalla) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, label = "cardScale")

    ElevatedCard(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current
            ) { onClick(destino) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(titulo, fontSize = 16.sp)
        }
    }
}

