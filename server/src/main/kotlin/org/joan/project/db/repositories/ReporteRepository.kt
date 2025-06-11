package org.joan.project.db.repositories

import org.joan.project.db.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.joan.project.db.DetalleVenta
import org.joan.project.db.Productos
import org.joan.project.db.Ventas
import org.joan.project.db.entidades.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReporteRepository {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    suspend fun generarReporteDiario(fecha: LocalDate): ReporteDiarioResponse = generarReportePorRango(
        fechaInicio = fecha,
        fechaFin = fecha
    )

    suspend fun generarReportePorRango(fechaInicio: LocalDate, fechaFin: LocalDate): ReporteDiarioResponse = dbQuery {
        val inicioDia = fechaInicio.atStartOfDay()
        val finDia = fechaFin.plusDays(1).atStartOfDay()

        val ventas = Ventas
            .select { Ventas.fecha.between(inicioDia, finDia) }
            .map { row ->
                VentaSimpleResponse(
                    id = row[Ventas.id].value,
                    total = row[Ventas.total].toDouble(),
                    fecha = row[Ventas.fecha].toString(),
                    metodoPago = row[Ventas.metodoPago]
                )
            }

        val productosMasVendidos = productosMasVendidos(5, fechaInicio, fechaFin)

        val metodoPagos = ventas
            .groupBy { it.metodoPago }
            .mapValues { (_, grupo) -> grupo.sumOf { it.total } }

        ReporteDiarioResponse(
            fecha = fechaInicio.toString(),
            totalVentas = ventas.sumOf { it.total },
            cantidadVentas = ventas.size,
            ventas = ventas,
            productosMasVendidos = productosMasVendidos,
            metodoPagos = metodoPagos
        )
    }

    suspend fun productosMasVendidos(
        limite: Int = 5,
        fechaInicio: LocalDate? = null,
        fechaFin: LocalDate? = null
    ): List<ProductoVendidoResponse> = dbQuery {
        val baseQuery = DetalleVenta
            .innerJoin(Productos, { DetalleVenta.productoId }, { Productos.id })
            .innerJoin(Ventas, { DetalleVenta.ventaId }, { Ventas.id })
            .slice(
                Productos.id,
                Productos.nombre,
                DetalleVenta.cantidad.sum(),
                DetalleVenta.subtotal.sum()
            )

        val filteredQuery = if (fechaInicio != null && fechaFin != null) {
            baseQuery.select {
                Ventas.fecha.between(
                    fechaInicio.atStartOfDay(),
                    fechaFin.plusDays(1).atStartOfDay()
                )
            }
        } else {
            baseQuery.selectAll()
        }

        filteredQuery
            .groupBy(Productos.id, Productos.nombre)
            .orderBy(DetalleVenta.cantidad.sum(), SortOrder.DESC)
            .limit(limite)
            .map { row ->
                ProductoVendidoResponse(
                    productoId = row[Productos.id].value,
                    nombre = row[Productos.nombre],
                    cantidadVendida = row[DetalleVenta.cantidad.sum()]?.toInt() ?: 0,
                    totalVendido = row[DetalleVenta.subtotal.sum()]?.toDouble() ?: 0.0
                )
            }
    }

    suspend fun generarReporteHoy(): ReporteDiarioResponse {
        val hoy = LocalDate.now()
        return generarReporteDiario(hoy)
    }

    suspend fun obtenerTotalesPorMetodoPago(): Map<String, Double> = dbQuery {
        Ventas
            .selectAll()
            .groupBy { it[Ventas.metodoPago] }
            .mapValues { (_, rows) -> rows.sumOf { it[Ventas.total].toDouble() } }
    }

}
