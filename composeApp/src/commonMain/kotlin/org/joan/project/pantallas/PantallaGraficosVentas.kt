package org.joan.project.pantallas

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.joan.project.db.entidades.VentaResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun PantallaGraficosVentas(
    ventas: List<VentaResponse>,
    onVolverClick: () -> Unit
) {
    val ventasPorFecha = ventas
        .groupBy { it.fecha.substring(0, 10) }
        .mapValues { it.value.sumOf { venta -> venta.total } }
        .toSortedMap()

    val ventasPorMetodo = ventas
        .groupBy { it.metodoPago }
        .mapValues { it.value.sumOf { venta -> venta.total } }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
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

@Composable
fun GraficoBarras(data: Map<String, Double>) {
    val max = data.values.maxOrNull() ?: 1.0
    val color = MaterialTheme.colorScheme.primary
    val etiquetas = data.keys.toList()
    val valores = data.values.toList()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 24.dp)
    ) {
        Text("📊 Ventas por día", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)  // Altura incrementada para el número y la fecha
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.Bottom
        ) {
            valores.forEachIndexed { index, valor ->
                val heightRatio = (valor / max).toFloat().coerceIn(0f, 1f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(45.dp)
                        .padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = "%.0f".format(valor),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp * heightRatio)  // Más espacio para la barra
                            .background(color, shape = MaterialTheme.shapes.small)
                    )
                    Spacer(modifier = Modifier.height(12.dp)) // espacio para separación
                    Text(
                        text = etiquetas[index].takeLast(5),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
            }
        }
    }
}



@Composable
fun GraficoVentasPorHora(ventas: List<VentaResponse>) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    val ventasPorHora: Map<Int, Double> = (0..23).associateWith { 0.0 }.toMutableMap().also { map ->
        for (venta in ventas) {
            val dateTime = LocalDateTime.parse(venta.fecha)
            val hora = dateTime.hour
            map[hora] = map[hora]?.plus(venta.total) ?: venta.total
        }
    }

    val max = ventasPorHora.values.maxOrNull() ?: 1.0
    val color = MaterialTheme.colorScheme.secondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Text("📈 Ventas por hora (0-23)", style = MaterialTheme.typography.titleMedium)

        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(220.dp)
                .padding(top = 12.dp)
        ) {
            val barWidth = size.width / 23f
            val points = ventasPorHora.toSortedMap()
            val puntoList = points.values.map { it.toFloat() }
            val maxHeight = size.height

            for (i in 0 until puntoList.size - 1) {
                val x1 = i * barWidth
                val y1 = maxHeight - (puntoList[i] / max) * maxHeight
                val x2 = (i + 1) * barWidth
                val y2 = maxHeight - (puntoList[i + 1] / max) * maxHeight
                drawLine(color, Offset(x1, y1.toFloat()), Offset(x2, y2.toFloat()), strokeWidth = 4f)
            }

            puntoList.forEachIndexed { i, value ->
                val x = i * barWidth
                val y = maxHeight - (value / max) * maxHeight
                drawCircle(color, radius = 6f, center = Offset(x, y.toFloat()))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            (0..23).forEach { hora ->
                Text(
                    text = hora.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(20.dp)
                )
            }
        }
    }
}

@Composable
fun GraficoCircular(data: Map<String, Double>) {
    val total = data.values.sum()
    if (total == 0.0) {
        Text("No hay datos para mostrar el gráfico circular", style = MaterialTheme.typography.bodyMedium)
        return
    }
    val porcentajes = data.mapValues { (it.value / total).toFloat() }

    val colores = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.outline
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Text("🍰 Por método de pago", style = MaterialTheme.typography.titleMedium)

        val segmentos = data.entries.mapIndexed { index, (metodo, _) ->
            metodo to colores.getOrElse(index) { MaterialTheme.colorScheme.outline }
        }.toMap()

        Canvas(
            modifier = Modifier
                .size(180.dp)
                .padding(top = 12.dp)
        ) {
            var inicio = -90f
            data.entries.forEach { (metodo, _) ->
                val sweep = porcentajes[metodo]!! * 360f
                drawArc(
                    color = segmentos[metodo]!!,
                    startAngle = inicio,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset.Zero,
                    size = size
                )
                inicio += sweep
            }
        }

        Column(Modifier.padding(top = 12.dp)) {
            data.entries.forEachIndexed { index, (metodo, valor) ->
                Text(
                    "• $metodo: %.2f €".format(valor),
                    fontWeight = FontWeight.Medium,
                    color = colores.getOrElse(index) { MaterialTheme.colorScheme.outline }
                )
            }
        }
    }
}
