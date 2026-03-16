package org.joan.project.pantallas

import androidx.compose.runtime.*
import org.koin.compose.koinInject
import org.joan.project.viewmodel.ProductoViewModel
import org.joan.project.viewmodel.VentaViewModel
import org.joan.project.viewmodel.AuthViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.joan.project.util.KpiUtils

@Composable
fun PantallaInicioRoute(
    onSeleccion: (Pantalla) -> Unit,
    onCerrarSesion: () -> Unit,
    productoVM: ProductoViewModel = koinInject(),
    ventaVM: VentaViewModel = koinInject(),
    authVM: AuthViewModel = koinInject()
) {
    val productos by productoVM.productos.collectAsState()
    val ventas by ventaVM.ventas.collectAsState()

    val token = authVM.token.value
    val hoy = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    // Cargar datos al entrar en Inicio
    LaunchedEffect(token) {
        if (token != null) {
            if (productos.isEmpty()) {
                productoVM.cargarProductos(token)     // ← carga productos aquí
            }
            ventaVM.cargarVentasEntreFechas(token, hoy, hoy) // ← y ventas del día
        }
    }

    val totalProductos = remember(productos) { productos.size }
    val valorInventario = remember(productos) { productos.sumOf { it.precio * it.stock } }
    val bajoStock = remember(productos) { productos.count { it.stock < 10 } }
    val ventasHoy = remember(ventas) {
        KpiUtils.ventasDeHoyIso(ventas, fechaIso = { it.fecha }, total = { it.total })
    }

    PantallaInicio(
        onSeleccion = onSeleccion,
        onCerrarSesion = onCerrarSesion,
        totalProductos = totalProductos,
        valorInventario = valorInventario,
        bajoStock = bajoStock,
        ventasHoy = ventasHoy,
        currency = "€"
    )
}
