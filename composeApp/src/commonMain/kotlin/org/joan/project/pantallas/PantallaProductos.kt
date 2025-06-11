package org.joan.project.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.items.ProductoItem
import org.joan.project.viewmodel.AuthViewModel
import org.joan.project.viewmodel.ProductoViewModel
import org.koin.compose.koinInject

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

    var filtroNombre by remember { mutableStateOf("") }
    var filtroCategoria by remember { mutableStateOf<String?>(null) }
    var filtroProveedor by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (token != null) {
            productoViewModel.cargarProductos(token)
        }
    }

    val productosFiltrados = productos.filter { producto ->
        (filtroNombre.isBlank() || producto.nombre.contains(filtroNombre, ignoreCase = true)) &&
                (filtroCategoria == null || producto.categoria.nombre == filtroCategoria) &&
                (filtroProveedor == null || producto.proveedor.nombre == filtroProveedor)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolverClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Text(
                "Gestión de Productos",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onCrearProductoClick) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo")
                Spacer(Modifier.width(8.dp))
                Text("Nuevo")
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = filtroNombre,
                onValueChange = { filtroNombre = it },
                label = { Text("Buscar por nombre") },
                modifier = Modifier.weight(1f)
            )

            DropdownMenuFiltro(
                label = "Categoría",
                opciones = productos.map { it.categoria.nombre }.distinct(),
                seleccion = filtroCategoria,
                onSeleccion = { filtroCategoria = it }
            )

            DropdownMenuFiltro(
                label = "Proveedor",
                opciones = productos.map { it.proveedor.nombre }.distinct(),
                seleccion = filtroProveedor,
                onSeleccion = { filtroProveedor = it }
            )
        }

        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(productosFiltrados) { producto ->
                ProductoItem(
                    producto = producto,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEditarProductoClick(producto) }
                )
            }
        }
    }
}

@Composable
fun DropdownMenuFiltro(
    label: String,
    opciones: List<String>,
    seleccion: String?,
    onSeleccion: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedButton(onClick = { expanded = true }) {
            Text(seleccion ?: label)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todas") },
                onClick = {
                    onSeleccion(null)
                    expanded = false
                }
            )
            opciones.distinct().sorted().forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}
