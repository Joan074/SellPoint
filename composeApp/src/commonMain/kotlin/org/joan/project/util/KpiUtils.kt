package org.joan.project.util

import kotlinx.datetime.*
import org.joan.project.db.entidades.ProductoResponse

object KpiUtils {
    fun totalProductos(productos: List<ProductoResponse>) = productos.size

    fun valorInventario(productos: List<ProductoResponse>): Double =
        productos.sumOf { it.precio * it.stock }

    fun bajoStock(productos: List<ProductoResponse>, umbral: Int = 10): Int =
        productos.count { it.stock < umbral }

    /** Ventas de HOY cuando tienes fecha en epoch millis */
    inline fun <T> ventasDeHoyEpoch(
        ventas: List<T>,
        crossinline fechaEpochMillis: (T) -> Long,
        crossinline total: (T) -> Double,
        timezone: TimeZone = TimeZone.currentSystemDefault()
    ): Double {
        val hoy = Clock.System.todayIn(timezone)
        return ventas.asSequence()
            .filter {
                Instant.fromEpochMilliseconds(fechaEpochMillis(it))
                    .toLocalDateTime(timezone).date == hoy
            }
            .sumOf(total)
    }

    /** Ventas de HOY cuando tienes fecha como ISO-8601 ("2025-08-09" o "2025-08-09T14:32:00") */
    inline fun <T> ventasDeHoyIso(
        ventas: List<T>,
        crossinline fechaIso: (T) -> String,
        crossinline total: (T) -> Double
    ): Double {
        val hoy = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return ventas.asSequence()
            .filter {
                val s = fechaIso(it)
                runCatching { LocalDate.parse(s.take(10)) }.getOrNull() == hoy
            }
            .sumOf(total)
    }
}
