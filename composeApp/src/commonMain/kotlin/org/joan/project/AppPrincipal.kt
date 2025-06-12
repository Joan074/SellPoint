// AppPrincipal.kt
package org.joan.project

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.joan.project.db.entidades.EmpleadoResponse
import org.joan.project.pantallas.*
import org.joan.project.viewmodel.AuthViewModel
import org.joan.project.viewmodel.ProductoViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPrincipal(
    currentUser: EmpleadoResponse,
    onLogout: () -> Unit
) {
    val authViewModel: AuthViewModel = koinInject()
    val productoViewModel: ProductoViewModel = koinInject()

    var pantalla by remember { mutableStateOf<Pantalla>(Pantalla.Inicio) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SellPoint TPV") },
                actions = {
                    Text(currentUser.nombre, modifier = Modifier.padding(end = 16.dp))
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            Crossfade(targetState = pantalla, label = "PantallaTransition") { actual ->
                when (actual) {
                    Pantalla.Inicio -> PantallaInicio { pantalla = it }

                    Pantalla.Listado -> PantallaProductos(
                        authViewModel = authViewModel,
                        productoViewModel = productoViewModel,
                        onCrearProductoClick = { pantalla = Pantalla.Crear },
                        onVolverClick = { pantalla = Pantalla.Inicio },
                        onEditarProductoClick = {
                            pantalla = Pantalla.Editar(it)
                        }
                    )

                    Pantalla.Crear -> PantallaCrearProducto(
                        token = authViewModel.token.value ?: "",
                        viewModel = productoViewModel,
                        onProductoCreado = {
                            productoViewModel.cargarProductos(authViewModel.token.value ?: "")
                            pantalla = Pantalla.Listado
                        },
                        onVolverClick = { pantalla = Pantalla.Listado }
                    )

                    is Pantalla.Editar -> PantallaEditarProducto(
                        producto = actual.producto,
                        token = authViewModel.token.value ?: "",
                        viewModel = productoViewModel,
                        onProductoActualizado = {
                            productoViewModel.cargarProductos(authViewModel.token.value ?: "")
                            pantalla = Pantalla.Listado
                        },
                        onVolverClick = { pantalla = Pantalla.Listado }
                    )

                    Pantalla.Cobrar -> PantallaCobrar(
                        authViewModel = authViewModel,
                        productoViewModel = productoViewModel,
                        onVolverClick = { pantalla = Pantalla.Inicio }
                    )

                    Pantalla.Proveedores -> Text("Pantalla de Proveedores")
                    Pantalla.Clientes -> Text("Pantalla de Clientes")

                    Pantalla.ReporteVentas -> PantallaReporteVentas(
                        onVolverClick = { pantalla = Pantalla.Inicio },
                        onGraficosClick = { ventas -> pantalla = Pantalla.Graficos(ventas) }
                    )


                    is Pantalla.Graficos -> PantallaGraficosVentas(
                        ventas = actual.ventas,
                        onVolverClick = { pantalla = Pantalla.ReporteVentas }
                    )

                }
            }
        }
    }
}