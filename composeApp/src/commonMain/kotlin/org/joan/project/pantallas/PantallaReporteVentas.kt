package org.joan.project.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.joan.project.db.entidades.VentaResponse
import org.joan.project.viewmodel.AuthViewModel
import org.joan.project.viewmodel.VentaViewModel
import org.joan.project.visual.generarPdfVentasProfesional
import org.koin.compose.koinInject
import java.time.LocalDate
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun PantallaReporteVentas(
    ventaViewModel: VentaViewModel = koinInject(),
    authViewModel: AuthViewModel = koinInject(),
    onVolverClick: () -> Unit,
    onGraficosClick: (ventas: List<VentaResponse>) -> Unit
) {
    var fechaInicioTexto by remember { mutableStateOf(LocalDate.now().minusDays(7).toString()) }
    var fechaFinTexto by remember { mutableStateOf(LocalDate.now().toString()) }

    var fechaInicio by remember { mutableStateOf(LocalDate.now().minusDays(7)) }
    var fechaFin by remember { mutableStateOf(LocalDate.now()) }

    val ventas by ventaViewModel.ventas.collectAsState()

    var cargando by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var rangoInvalido by remember { mutableStateOf(false) }
    var formatoInvalido by remember { mutableStateOf(false) }

    val total = ventas.sumOf { it.total }
    val promedio = if (ventas.isNotEmpty()) total / ventas.size else 0.0

    fun validarRango(): Boolean = !fechaInicio.isAfter(fechaFin)

    fun validarFechas(): Boolean {
        return try {
            val ini = LocalDate.parse(fechaInicioTexto)
            val fin = LocalDate.parse(fechaFinTexto)
            fechaInicio = ini
            fechaFin = fin
            formatoInvalido = false
            true
        } catch (e: Exception) {
            formatoInvalido = true
            false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onVolverClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Text(
                "Reporte de Ventas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                // Exportar PDF al hacer click
                val ruta = seleccionarRutaPdf()
                if (ruta != null) {
                    generarPdfVentasProfesional(ruta, ventas, total, promedio)
                }
            }) {
                Icon(Icons.Default.Download, contentDescription = "Exportar")
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = fechaInicioTexto,
                isError = formatoInvalido || rangoInvalido,
                onValueChange = { fechaInicioTexto = it },
                label = { Text("Desde (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = fechaFinTexto,
                isError = formatoInvalido || rangoInvalido,
                onValueChange = { fechaFinTexto = it },
                label = { Text("Hasta (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    errorMsg = null
                    if (validarFechas()) {
                        rangoInvalido = !validarRango()
                        if (!rangoInvalido) {
                            cargando = true
                            val token = authViewModel.token.value ?: return@Button
                            ventaViewModel.cargarVentasEntreFechas(
                                token = token,
                                desde = fechaInicio,
                                hasta = fechaFin,
                                onError = {
                                    errorMsg = it
                                    cargando = false
                                },
                                onSuccess = {
                                    cargando = false
                                }
                            )
                        }
                    }
                },
                enabled = !cargando
            ) {
                Text("Consultar")
            }
        }

        if (formatoInvalido) {
            Text(
                "❌ Formato de fecha inválido. Usa YYYY-MM-DD.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else if (rangoInvalido) {
            Text(
                "❌ El rango de fechas es inválido. 'Desde' debe ser igual o anterior a 'Hasta'.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        when {
            cargando -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMsg != null -> {
                Text(
                    "❌ $errorMsg",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
            ventas.isEmpty() -> {
                Spacer(Modifier.height(32.dp))
                Text(
                    "No se encontraron ventas en este período.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ventas) { venta ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Fecha: ${venta.fecha.substring(0, 10)}", fontWeight = FontWeight.Medium)
                                Text("Empleado: ${venta.empleado.nombre}")
                                Text("Método de pago: ${venta.metodoPago}")
                                Text("Total: %.2f €".format(venta.total), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Divider()
                Text("Resumen del período", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Total acumulado: %.2f €".format(total))
                Text("Cantidad de ventas: ${ventas.size}")
                Text("Promedio por venta: %.2f €".format(promedio))

                Spacer(Modifier.height(24.dp))
                Text("Visualización de datos", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { onGraficosClick(ventas) },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Ver gráficos")
                }
            }
        }
    }
}

// Función para mostrar diálogo de selección de ruta para guardar PDF
fun seleccionarRutaPdf(): String? {
    val chooser = JFileChooser()
    chooser.dialogTitle = "Guardar reporte como"
    chooser.fileFilter = FileNameExtensionFilter("Archivos PDF", "pdf")
    val result = chooser.showSaveDialog(null)
    return if (result == JFileChooser.APPROVE_OPTION) {
        var path = chooser.selectedFile.absolutePath
        if (!path.endsWith(".pdf", ignoreCase = true)) path += ".pdf"
        path
    } else null
}
