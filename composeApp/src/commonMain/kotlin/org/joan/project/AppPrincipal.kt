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
import org.joan.project.viewmodel.CategoriaViewModel
import org.joan.project.viewmodel.ClienteViewModel
import org.joan.project.viewmodel.NegocioViewModel
import org.joan.project.viewmodel.ProductoViewModel
import org.joan.project.viewmodel.ProveedorViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPrincipal(
    currentUser: EmpleadoResponse,
    onLogout: () -> Unit
) {
    val authViewModel: AuthViewModel = koinInject()
    val productoViewModel: ProductoViewModel = koinInject()
    val proveedorViewModel: ProveedorViewModel = koinInject()
    val categoriaViewModel: CategoriaViewModel = koinInject()
    val negocioViewModel: NegocioViewModel = koinInject()
    val clienteViewModel: ClienteViewModel = koinInject()

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
                    Pantalla.Login -> PantallaLogin(
                        onLoginSuccess = { pantalla = Pantalla.Inicio }
                    )

                    Pantalla.Inicio -> PantallaInicioRoute(
                        onSeleccion = { pantalla = it },
                        onCerrarSesion = {
                            authViewModel.logout()
                            pantalla = Pantalla.Login
                        },
                        onAjustesClick = { pantalla = Pantalla.AjustesNegocio }
                    )


                    Pantalla.Listado -> PantallaProductos(
                        authViewModel = authViewModel,
                        productoViewModel = productoViewModel,
                        onCrearProductoClick = { pantalla = Pantalla.Crear },
                        onVolverClick = { pantalla = Pantalla.Inicio },
                        onEditarProductoClick = { pantalla = Pantalla.Editar(it) }
                    )
                    Pantalla.Crear -> PantallaCrearProducto(
                        token = authViewModel.token.value ?: "",
                        productoViewModel = productoViewModel,
                        categoriaViewModel = categoriaViewModel,
                        proveedorViewModel = proveedorViewModel,
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
                    Pantalla.Proveedores -> PantallaProveedores(
                        proveedorViewModel = proveedorViewModel,
                        onVolverClick = { pantalla = Pantalla.Inicio }
                    )


                    Pantalla.AjustesNegocio -> PantallaAjustesNegocio(
                        negocioViewModel = negocioViewModel,
                        onVolverClick = { pantalla = Pantalla.Inicio }
                    )

                    Pantalla.Clientes -> PantallaClientes(
                        authViewModel   = authViewModel,
                        clienteViewModel = clienteViewModel,
                        onVolverClick   = { pantalla = Pantalla.Inicio }
                    )
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
