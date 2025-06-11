package org.joan.project.visual

import com.lowagie.text.*
import com.lowagie.text.pdf.PdfWriter
import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.db.entidades.VentaRequest
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

fun generarTicketPDF(venta: VentaRequest, productos: List<ProductoResponse>, archivo: File) {
    val pageSize = Rectangle(220f, 400f) // 80mm ancho aprox
    val documento = Document(pageSize, 10f, 10f, 10f, 10f)
    PdfWriter.getInstance(documento, FileOutputStream(archivo))
    documento.open()

    val fuenteMono = Font(Font.COURIER, 9f)

    fun addLinea(texto: String) {
        documento.add(Paragraph(texto, fuenteMono))
    }

    val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date())

    // Cabecera
    addLinea("TIENDA SELLPOINT")
    addLinea("Fecha: $fechaActual")
    addLinea("Pago: ${venta.metodoPago}")
    addLinea("=".repeat(37))

    // Productos
    venta.items.forEach { item ->
        val producto = productos.find { it.id == item.productoId }
        if (producto != null) {
            val precioUnitario = item.precioEspecial ?: producto.precio
            val subtotal = precioUnitario * item.cantidad
            val nombre = producto.nombre.take(16).padEnd(16)
            val linea = "$nombre ${"%2d".format(item.cantidad)} x ${"%5.2f".format(precioUnitario)} = ${"%6.2f".format(subtotal)}"
            addLinea(linea)
        }
    }

    addLinea("=".repeat(37))

    val total = venta.items.sumOf {
        it.cantidad * (it.precioEspecial
            ?: productos.find { p -> p.id == it.productoId }?.precio ?: 0.0)
    }

    addLinea("TOTAL:".padEnd(25) + "                                  "+"%6.2f €".format(total))
    addLinea("=".repeat(37))
    addLinea("Gracias por su compra")

    documento.close()
}
