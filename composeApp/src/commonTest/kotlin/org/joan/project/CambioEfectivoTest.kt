package org.joan.project

import kotlin.math.round
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CambioEfectivoTest {

    /** Calcula el cambio que debe devolverse al cliente */
    private fun calcularCambio(efectivoEntregado: Double, totalAPagar: Double): Double =
        (efectivoEntregado - totalAPagar).coerceAtLeast(0.0)

    private fun Double.round2(): Double = round(this * 100) / 100.0

    @Test
    fun `cambio correcto cuando el cliente entrega mas del total`() {
        assertEquals(1.50, calcularCambio(10.0, 8.50))
    }

    @Test
    fun `cambio es cero cuando el cliente paga exacto`() {
        assertEquals(0.0, calcularCambio(5.0, 5.0))
    }

    @Test
    fun `cambio nunca es negativo aunque no alcance`() {
        val cambio = calcularCambio(5.0, 10.0)
        assertTrue(cambio >= 0.0)
        assertEquals(0.0, cambio)
    }

    @Test
    fun `cambio con billete de 20 euros`() {
        val cambio = calcularCambio(20.0, 12.50).round2()
        assertEquals(7.50, cambio)
    }

    @Test
    fun `cambio con billete de 50 euros`() {
        val cambio = calcularCambio(50.0, 37.50).round2()
        assertEquals(12.50, cambio)
    }

    @Test
    fun `cambio pagando justo con monedas exactas`() {
        val total = 3.50
        assertEquals(0.0, calcularCambio(3.50, total))
    }

    @Test
    fun `cambio con pago fraccionado en efectivo`() {
        // Cliente entrega 2€ para un total de 1.50€
        val cambio = calcularCambio(2.0, 1.50)
        assertEquals(0.50, cambio)
    }
}
