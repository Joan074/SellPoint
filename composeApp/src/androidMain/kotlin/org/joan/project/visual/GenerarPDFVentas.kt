package org.joan.project.visual

import org.joan.project.db.entidades.VentaResponse

actual fun seleccionarRutaPdf(): String? = null

actual fun generarPdfVentasProfesional(
    rutaArchivo: String,
    ventas: List<VentaResponse>,
    total: Double,
    promedio: Double
) { /* PDF export not supported on Android in this version */ }
