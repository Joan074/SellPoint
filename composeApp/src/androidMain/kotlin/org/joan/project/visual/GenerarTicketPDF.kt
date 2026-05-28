package org.joan.project.visual

import java.io.File
import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.db.entidades.VentaRequest
import org.joan.project.db.entidades.VentaResponse
import org.joan.project.viewmodel.DatosNegocio

actual fun carpetaTickets(): File =
    File(System.getProperty("java.io.tmpdir") ?: ".", "sellpoint/tickets").also { it.mkdirs() }

actual fun nuevoArchivoTicket(): File =
    File(carpetaTickets(), "ticket_${System.currentTimeMillis()}.pdf")

actual fun generarTicketPDF(
    venta: VentaRequest,
    productos: List<ProductoResponse>,
    archivo: File,
    nombreEmpleado: String,
    negocio: DatosNegocio,
    nombreCliente: String?
) { /* PDF generation not supported on Android in this version */ }

actual fun generarTicketPDF(venta: VentaResponse, archivo: File, negocio: DatosNegocio) {
    /* PDF generation not supported on Android in this version */
}

actual fun abrirArchivoConViewer(archivo: File) {
    /* File viewer not supported on Android in this version */
}
