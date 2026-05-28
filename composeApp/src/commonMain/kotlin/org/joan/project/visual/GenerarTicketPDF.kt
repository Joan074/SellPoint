package org.joan.project.visual

import java.io.File
import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.db.entidades.VentaRequest
import org.joan.project.db.entidades.VentaResponse
import org.joan.project.viewmodel.DatosNegocio

expect fun carpetaTickets(): File
expect fun nuevoArchivoTicket(): File

expect fun generarTicketPDF(
    venta: VentaRequest,
    productos: List<ProductoResponse>,
    archivo: File,
    nombreEmpleado: String,
    negocio: DatosNegocio,
    nombreCliente: String?
)

expect fun generarTicketPDF(venta: VentaResponse, archivo: File, negocio: DatosNegocio)

expect fun abrirArchivoConViewer(archivo: File)
