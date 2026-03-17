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
    onAjustesClick: () -> Unit = {},
    productoVM: ProductoViewModel = koinInject(),
    ventaVM: VentaViewModel = koinInject(),
    authVM: AuthViewModel = koinInject()
) {
    val productos by productoVM.productos.collectAsState()
    val ventas    by ventaVM.ventas.collectAsState()
    val userName  = authVM.currentUser.collectAsState().value?.nombre

    val token = authVM.token.value
    val hoy   = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    LaunchedEffect(token) {
        if (token != null) {
            if (productos.isEmpty()) productoVM.cargarProductos(token)
            ventaVM.cargarVentasEntreFechas(token, hoy, hoy)
        }
    }

    // ── KPIs ──────────────────────────────────────────────────────────────────
    val totalProductos  = remember(productos) { productos.size }
    val valorInventario = remember(productos) { productos.sumOf { it.precio * it.stock } }
    val bajoStock       = remember(productos) { productos.count { it.stock < 10 } }
    val ventasHoy       = remember(ventas) {
        KpiUtils.ventasDeHoyIso(ventas, fechaIso = { it.fecha }, total = { it.total })
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────
    val ultimasVentasHoy = remember(ventas) {
        ventas.sortedByDescending { it.fecha }.take(5)
    }
    val productosBajoStock = remember(productos) {
        productos.filter { it.stock < 10 }.sortedBy { it.stock }
    }

    PantallaInicio(
        onSeleccion        = onSeleccion,
        onCerrarSesion     = onCerrarSesion,
        onAjustesClick     = onAjustesClick,
        userName           = userName,
        totalProductos     = totalProductos,
        valorInventario    = valorInventario,
        bajoStock          = bajoStock,
        ventasHoy          = ventasHoy,
        currency           = "€",
        ultimasVentasHoy   = ultimasVentasHoy,
        productosBajoStock = productosBajoStock
    )
}
