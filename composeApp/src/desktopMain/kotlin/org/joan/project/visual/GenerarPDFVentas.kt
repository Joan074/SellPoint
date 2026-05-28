package org.joan.project.visual

import com.lowagie.text.Document
import com.lowagie.text.Font
import com.lowagie.text.Paragraph
import com.lowagie.text.Phrase
import com.lowagie.text.Chunk
import com.lowagie.text.Element
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import com.lowagie.text.pdf.PdfPageEventHelper
import com.lowagie.text.pdf.ColumnText
import org.joan.project.db.entidades.VentaResponse
import java.awt.Color
import java.io.FileOutputStream
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

actual fun seleccionarRutaPdf(): String? {
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

actual fun generarPdfVentasProfesional(
    rutaArchivo: String,
    ventas: List<VentaResponse>,
    total: Double,
    promedio: Double
) {
    val document = Document(com.lowagie.text.PageSize.A4, 36f, 36f, 54f, 54f)
    val writer = PdfWriter.getInstance(document, FileOutputStream(rutaArchivo))

    val headerFooterFont = Font(Font.HELVETICA, 9f, Font.ITALIC, Color.GRAY)
    val _dt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val fechaGeneracion = "%04d-%02d-%02d %02d:%02d:%02d".format(
        _dt.year, _dt.monthNumber, _dt.dayOfMonth, _dt.hour, _dt.minute, _dt.second
    )

    writer.pageEvent = object : PdfPageEventHelper() {
        override fun onEndPage(writer: PdfWriter?, document: Document?) {
            val cb = writer?.directContent
            val pagina = "Página ${writer?.pageNumber}"
            val rect = document?.pageSize

            ColumnText.showTextAligned(
                cb, Element.ALIGN_CENTER,
                Phrase(pagina, headerFooterFont),
                rect!!.width / 2, rect.bottom + 30, 0f
            )
            ColumnText.showTextAligned(
                cb, Element.ALIGN_RIGHT,
                Phrase("Generado: $fechaGeneracion", headerFooterFont),
                rect.right - 36, rect.bottom + 30, 0f
            )
        }
    }

    document.open()

    val tituloFont    = Font(Font.HELVETICA, 22f, Font.BOLD, Color(33, 150, 243))
    val textoNormal   = Font(Font.HELVETICA, 11f, Font.NORMAL, Color.BLACK)
    val negrita       = Font(Font.HELVETICA, 11f, Font.BOLD, Color.BLACK)
    val grisClaro     = Color(240, 240, 240)

    document.add(Paragraph("Reporte de Ventas\n\n", tituloFont).apply { alignment = Element.ALIGN_CENTER })
    document.add(Paragraph("Fecha de generación: $fechaGeneracion\n\n", textoNormal).apply { alignment = Element.ALIGN_CENTER })
    document.add(Paragraph().apply {
        add(Chunk("Total acumulado: ", negrita)); add(Chunk("%.2f €\n".format(total), textoNormal))
        add(Chunk("Cantidad de ventas: ", negrita)); add(Chunk("${ventas.size}\n", textoNormal))
        add(Chunk("Promedio por venta: ", negrita)); add(Chunk("%.2f €\n\n".format(promedio), textoNormal))
    })

    val tabla = PdfPTable(4).apply {
        widthPercentage = 100f
        setWidths(floatArrayOf(2f, 4f, 3f, 2f))
        headerRows = 1
    }

    val headerFont    = Font(Font.HELVETICA, 12f, Font.BOLD, Color.WHITE)
    val headerBgColor = Color(33, 150, 243)

    listOf("Fecha", "Empleado", "Método de pago", "Total (€)").forEach { texto ->
        tabla.addCell(PdfPCell(Phrase(texto, headerFont)).apply {
            backgroundColor = headerBgColor
            horizontalAlignment = Element.ALIGN_CENTER
            verticalAlignment = Element.ALIGN_MIDDLE
            setPadding(8f); borderWidth = 0.5f; borderColor = Color.GRAY
        })
    }

    ventas.forEachIndexed { index, venta ->
        val bgColor = if (index % 2 == 0) grisClaro else Color.WHITE
        fun cell(texto: String, align: Int = Element.ALIGN_LEFT) =
            PdfPCell(Phrase(texto, textoNormal)).apply {
                backgroundColor = bgColor
                horizontalAlignment = align; verticalAlignment = Element.ALIGN_MIDDLE
                setPadding(6f); borderWidth = 0.25f; borderColor = Color.LIGHT_GRAY
            }
        tabla.addCell(cell(venta.fecha.substring(0, 10), Element.ALIGN_CENTER))
        tabla.addCell(cell(venta.empleado.nombre))
        tabla.addCell(cell(venta.metodoPago, Element.ALIGN_CENTER))
        tabla.addCell(cell("%.2f".format(venta.total), Element.ALIGN_RIGHT))
    }

    document.add(tabla)
    document.add(Paragraph("\n\nReporte generado automáticamente por SellPointTPV", textoNormal).apply {
        alignment = Element.ALIGN_CENTER; spacingBefore = 30f
    })
    document.close()
}
