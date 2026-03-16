@file:Suppress("OPT_IN_USAGE")
@file:OptIn(io.github.koalaplot.core.util.ExperimentalKoalaPlotApi::class)
package org.joan.project.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.bar.BarScope
import io.github.koalaplot.core.bar.DefaultVerticalBar
import io.github.koalaplot.core.bar.DefaultVerticalBarPlotEntry
import io.github.koalaplot.core.bar.DefaultVerticalBarPosition
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.pie.DefaultSlice
import io.github.koalaplot.core.pie.PieChart
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.LinearAxisModel
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraph
import kotlinx.datetime.LocalDateTime
import org.joan.project.db.entidades.VentaResponse

/** Simple Point implementation for line chart data. */
private data class Pt<X, Y>(override val x: X, override val y: Y) : Point<X, Y>

@Composable
fun PantallaGraficosVentas(
    ventas: List<VentaResponse>,
    onVolverClick: () -> Unit
) {
    val ventasPorFecha = ventas
        .groupBy { it.fecha.take(10) }
        .mapValues { it.value.sumOf { v -> v.total } }
        .toSortedMap()

    val ventasPorMetodo = ventas
        .groupBy { it.metodoPago }
        .mapValues { it.value.sumOf { v -> v.total } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onVolverClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Text(
                "Visualización de Ventas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(Modifier.height(32.dp))
        GraficoCircular(ventasPorMetodo)
        Spacer(Modifier.height(32.dp))
        GraficoBarras(ventasPorFecha)
        Spacer(Modifier.height(32.dp))
        GraficoVentasPorHora(ventas)
    }
}

// ─── Barras: ventas por día ────────────────────────────────────────────────
@Composable
fun GraficoBarras(data: Map<String, Double>) {
    if (data.isEmpty()) {
        Text("Sin datos de ventas por día", style = MaterialTheme.typography.bodyMedium)
        return
    }
    val maxVal = (data.values.maxOrNull() ?: 1.0).toFloat().coerceAtLeast(1f)
    val categories = data.keys.toList()
    val values = data.values.map { it.toFloat() }
    val barColor = MaterialTheme.colorScheme.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
    ) {
        Text("Ventas por día", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        val xLabels: (String) -> String = { cat -> cat.takeLast(5) }
        val yLabels: (Float) -> String = { v -> "%.0f€".format(v) }
        XYGraph(
            xAxisModel = CategoryAxisModel(categories),
            yAxisModel = LinearAxisModel(0f..(maxVal * 1.15f)),
            modifier = Modifier.fillMaxWidth().height(280.dp),
            xAxisLabels = xLabels,
            yAxisLabels = yLabels
        ) {
            VerticalBarPlot(
                data = categories.mapIndexed { i, cat ->
                    DefaultVerticalBarPlotEntry(
                        x = cat,
                        y = DefaultVerticalBarPosition(yMin = 0f, yMax = values[i])
                    )
                },
                bar = @Composable { _: Int ->
                    DefaultVerticalBar(color = barColor)
                }
            )
        }
    }
}

// ─── Línea: ventas por hora ────────────────────────────────────────────────
@Composable
fun GraficoVentasPorHora(ventas: List<VentaResponse>) {
    val ventasPorHora: MutableMap<Int, Double> = (0..23).associateWith { 0.0 }.toMutableMap()
    for (venta in ventas) {
        try {
            val hora = LocalDateTime.parse(venta.fecha.take(19)).hour
            ventasPorHora[hora] = (ventasPorHora[hora] ?: 0.0) + venta.total
        } catch (_: Exception) {}
    }
    val maxVal = (ventasPorHora.values.maxOrNull() ?: 1.0).toFloat().coerceAtLeast(1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
    ) {
        Text("Ventas por hora (0–23 h)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        val xLabels: (Float) -> String = { h -> h.toInt().toString() }
        val yLabels: (Float) -> String = { v -> "%.0f€".format(v) }
        XYGraph(
            xAxisModel = LinearAxisModel(0f..23f),
            yAxisModel = LinearAxisModel(0f..(maxVal * 1.15f)),
            modifier = Modifier.fillMaxWidth().height(260.dp),
            xAxisLabels = xLabels,
            yAxisLabels = yLabels
        ) {
            LinePlot(
                data = (0..23).map { h ->
                    Pt(h.toFloat(), (ventasPorHora[h] ?: 0.0).toFloat())
                }
            )
        }
    }
}

// ─── Circular: métodos de pago ─────────────────────────────────────────────
@Composable
fun GraficoCircular(data: Map<String, Double>) {
    val total = data.values.sum()
    if (total == 0.0) {
        Text("No hay datos para el gráfico de métodos de pago", style = MaterialTheme.typography.bodyMedium)
        return
    }
    val colores = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.outline
    )
    val labels = data.keys.toList()
    val values = data.values.map { it.toFloat() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
    ) {
        Text("Por método de pago", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        PieChart(
            values = values,
            modifier = Modifier.size(240.dp),
            slice = { index ->
                DefaultSlice(color = colores[index % colores.size])
            },
            label = { index ->
                Text(labels[index], style = MaterialTheme.typography.labelSmall)
            }
        )
        Spacer(Modifier.height(12.dp))
        Column(Modifier.padding(horizontal = 16.dp)) {
            data.entries.forEachIndexed { i, (metodo, valor) ->
                val pct = (valor / total * 100).toInt()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(12.dp),
                        color = colores[i % colores.size],
                        shape = MaterialTheme.shapes.small
                    ) {}
                    Text(
                        "$metodo: ${"%.2f".format(valor)} € ($pct%)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
