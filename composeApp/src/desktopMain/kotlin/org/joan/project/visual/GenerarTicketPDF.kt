package org.joan.project.visual

import com.lowagie.text.*
import com.lowagie.text.Image as PdfImage
import com.lowagie.text.pdf.PdfWriter
import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.db.entidades.VentaRequest
import org.joan.project.db.entidades.VentaResponse
import org.joan.project.viewmodel.DatosNegocio
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// ── Carpeta temporal para tickets ─────────────────────────────────────────────

actual fun carpetaTickets(): File {
    val tmp = System.getenv("TEMP") ?: System.getProperty("java.io.tmpdir") ?: "."
    return File(tmp, "sellpoint/tickets").also { it.mkdirs() }
}

actual fun nuevoArchivoTicket(): File =
    File(carpetaTickets(), "ticket_${System.currentTimeMillis()}.pdf")

actual fun abrirArchivoConViewer(archivo: File) {
    Desktop.getDesktop().open(archivo)
}

// ── Layout del ticket ─────────────────────────────────────────────────────────

private const val W      = 40
private const val W_DESC = 16
private const val W_CANT =  4
private const val W_PREC = 10
private const val W_IMPO = 10

private const val W_LABEL = 25
private const val W_NUM   = 13

private val SEP_D = "=".repeat(W)
private val SEP_S = "-".repeat(W)

// ─────────────────────────────────────────────────────────────────────────────

actual fun generarTicketPDF(
    venta: VentaRequest,
    productos: List<ProductoResponse>,
    archivo: File,
    nombreEmpleado: String,
    negocio: DatosNegocio,
    nombreCliente: String?
) {
    val pageHeight = (360f + venta.items.size * 14f).coerceAtLeast(500f)

    val doc = Document(
        Rectangle(226.77f, pageHeight),
        8f, 8f, 10f, 10f
    )
    PdfWriter.getInstance(doc, FileOutputStream(archivo))
    doc.open()

    val fNeg   = Font(Font.HELVETICA, 12f, Font.BOLD)
    val fSub   = Font(Font.HELVETICA,  7.5f)
    val fM     = Font(Font.COURIER,    8f)
    val fMB    = Font(Font.COURIER,    8f, Font.BOLD)
    val fMSm   = Font(Font.COURIER,    7f)

    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val fecha     = "%02d/%02d/%04d".format(now.dayOfMonth, now.monthNumber, now.year)
    val hora      = "%02d:%02d".format(now.hour, now.minute)
    val ticketNum = "T-%04d%02d%02d-%04d".format(
        now.year, now.monthNumber, now.dayOfMonth,
        (System.currentTimeMillis() % 10000).toInt()
    )

    fun p(txt: String, f: Font = fM, align: Int = Element.ALIGN_LEFT) =
        Paragraph(txt, f).apply { alignment = align; spacingBefore = 0f; spacingAfter = 0f }

    fun sp()   = p(" ", fMSm)
    fun sepD() = p(SEP_D, fM)
    fun sepS() = p(SEP_S, fM)

    fun num(d: Double)    = "%.2f".format(d).replace(".", ",")
    fun numNeg(d: Double) = "-${num(d)}"
    fun totalLine(label: String, d: Double, neg: Boolean = false, f: Font = fM) {
        val n = if (neg) numNeg(d) else num(d)
        doc.add(p("${label.padEnd(W_LABEL)}${n.padStart(W_NUM)} \u20AC", f))
    }

    doc.add(sp())

    negocio.logoPath?.let { path ->
        runCatching {
            val img = PdfImage.getInstance(path)
            img.scaleToFit(60f, 60f)
            img.alignment = PdfImage.ALIGN_CENTER
            doc.add(img)
            doc.add(sp())
        }
    }

    doc.add(p(negocio.nombre,    fNeg, Element.ALIGN_CENTER))
    doc.add(p(negocio.direccion, fSub, Element.ALIGN_CENTER))
    doc.add(p("Tel: ${negocio.telefono}", fSub, Element.ALIGN_CENTER))
    doc.add(p(negocio.cif,       fSub, Element.ALIGN_CENTER))
    doc.add(sp())
    doc.add(sepD())
    doc.add(sp())

    fun info(lbl: String, v: String) = p("${lbl.padEnd(9)}: $v", fM)
    doc.add(info("Ticket",  ticketNum))
    doc.add(info("Fecha",   "$fecha  $hora h"))
    doc.add(info("Pago",    metodoStr(venta.metodoPago)))
    if (nombreEmpleado.isNotBlank()) doc.add(info("Cajero", nombreEmpleado))
    doc.add(info("Cliente", nombreCliente ?: "Cliente basico"))
    doc.add(sp())
    doc.add(sepS())
    doc.add(sp())

    doc.add(
        p(
            "DESCRIPCION".padEnd(W_DESC) +
            "CANT".padStart(W_CANT) +
            "PRECIO".padStart(W_PREC) +
            "IMPORTE".padStart(W_IMPO),
            fMB
        )
    )
    doc.add(sepS())

    var subtotalBruto = 0.0
    venta.items.forEach { item ->
        val prod  = productos.find { it.id == item.productoId } ?: return@forEach
        val pu    = item.precioEspecial ?: prod.precio
        val total = pu * item.cantidad
        subtotalBruto += total

        doc.add(
            p(
                prod.nombre.take(W_DESC).padEnd(W_DESC) +
                item.cantidad.toString().padStart(W_CANT) +
                num(pu).padStart(W_PREC) +
                num(total).padStart(W_IMPO),
                fM
            )
        )
    }

    doc.add(sepS())
    doc.add(sp())

    val descuento     = venta.descuento.coerceAtLeast(0.0)
    val totalConDesc  = (subtotalBruto - descuento).coerceAtLeast(0.0)
    val baseImponible = totalConDesc / 1.21
    val cuotaIva      = totalConDesc - baseImponible

    if (descuento > 0.0) {
        totalLine("Subtotal", subtotalBruto)
        totalLine("Descuento", descuento, neg = true)
    }
    totalLine("Base imponible", baseImponible)
    totalLine("IVA 21%", cuotaIva)
    doc.add(sp())
    doc.add(sepD())

    doc.add(p("${"TOTAL".padEnd(W_LABEL)}${num(totalConDesc).padStart(W_NUM)} \u20AC", fMB))

    doc.add(sepD())
    doc.add(sp())

    doc.add(p("*** Gracias por su compra ***", fMB,  Element.ALIGN_CENTER))
    doc.add(sp())
    doc.add(p("Conserve este ticket para",     fMSm, Element.ALIGN_CENTER))
    doc.add(p("cambios y devoluciones",         fMSm, Element.ALIGN_CENTER))
    doc.add(sp())

    doc.close()
}

private fun metodoStr(m: String) = when (m.uppercase()) {
    "EFECTIVO" -> "Efectivo"
    "TARJETA"  -> "Tarjeta bancaria"
    "BIZUM"    -> "Bizum"
    else       -> m
}

actual fun generarTicketPDF(
    venta: VentaResponse,
    archivo: File,
    negocio: DatosNegocio
) {
    val pageHeight = (360f + venta.items.size * 14f).coerceAtLeast(500f)
    val doc = Document(
        Rectangle(226.77f, pageHeight),
        8f, 8f, 10f, 10f
    )
    PdfWriter.getInstance(doc, FileOutputStream(archivo))
    doc.open()

    val fNeg  = Font(Font.HELVETICA, 12f, Font.BOLD)
    val fSub  = Font(Font.HELVETICA,  7.5f)
    val fM    = Font(Font.COURIER,    8f)
    val fMB   = Font(Font.COURIER,    8f, Font.BOLD)
    val fMSm  = Font(Font.COURIER,    7f)

    val (fecha, hora) = try {
        val ldt = LocalDateTime.parse(venta.fecha.take(19))
        "%02d/%02d/%04d".format(ldt.dayOfMonth, ldt.monthValue, ldt.year) to
        "%02d:%02d".format(ldt.hour, ldt.minute)
    } catch (e: Exception) {
        (venta.fecha.take(10)) to ""
    }
    val ticketNum = venta.numeroTicket ?: "T-${venta.id}"

    fun p(txt: String, f: Font = fM, align: Int = Element.ALIGN_LEFT) =
        Paragraph(txt, f).apply { alignment = align; spacingBefore = 0f; spacingAfter = 0f }
    fun sp()   = p(" ", fMSm)
    fun sepD() = p(SEP_D, fM)
    fun sepS() = p(SEP_S, fM)
    fun num(d: Double) = "%.2f".format(d).replace(".", ",")
    fun totalLine(label: String, d: Double, neg: Boolean = false, f: Font = fM) {
        val n = if (neg) "-${num(d)}" else num(d)
        doc.add(p("${label.padEnd(W_LABEL)}${n.padStart(W_NUM)} \u20AC", f))
    }

    doc.add(sp())
    negocio.logoPath?.let { path ->
        runCatching {
            val img = PdfImage.getInstance(path)
            img.scaleToFit(60f, 60f)
            img.alignment = PdfImage.ALIGN_CENTER
            doc.add(img)
            doc.add(sp())
        }
    }
    doc.add(p(negocio.nombre,    fNeg, Element.ALIGN_CENTER))
    doc.add(p(negocio.direccion, fSub, Element.ALIGN_CENTER))
    doc.add(p("Tel: ${negocio.telefono}", fSub, Element.ALIGN_CENTER))
    doc.add(p(negocio.cif,       fSub, Element.ALIGN_CENTER))
    doc.add(sp())
    doc.add(sepD())
    doc.add(sp())

    fun info(lbl: String, v: String) = p("${lbl.padEnd(9)}: $v", fM)
    doc.add(info("Ticket",  ticketNum))
    doc.add(info("Fecha",   "$fecha  $hora h"))
    doc.add(info("Pago",    metodoStr(venta.metodoPago)))
    doc.add(info("Cajero",  venta.empleado.nombre))
    doc.add(info("Cliente", venta.cliente?.nombre ?: "Cliente basico"))
    doc.add(sp())
    doc.add(sepS())
    doc.add(sp())

    doc.add(
        p(
            "DESCRIPCION".padEnd(W_DESC) +
            "CANT".padStart(W_CANT) +
            "PRECIO".padStart(W_PREC) +
            "IMPORTE".padStart(W_IMPO),
            fMB
        )
    )
    doc.add(sepS())

    venta.items.forEach { item ->
        doc.add(
            p(
                item.nombre.take(W_DESC).padEnd(W_DESC) +
                item.cantidad.toString().padStart(W_CANT) +
                num(item.precioUnitario).padStart(W_PREC) +
                num(item.subtotal).padStart(W_IMPO),
                fM
            )
        )
    }

    doc.add(sepS())
    doc.add(sp())

    val descuento = (venta.subtotal + venta.iva - venta.total).coerceAtLeast(0.0)
    if (descuento > 0.01) {
        totalLine("Subtotal s/ descuento", venta.subtotal + descuento)
        totalLine("Descuento", descuento, neg = true)
    }
    totalLine("Base imponible", venta.subtotal)
    totalLine("IVA", venta.iva)
    doc.add(sp())
    doc.add(sepD())
    doc.add(p("${"TOTAL".padEnd(W_LABEL)}${num(venta.total).padStart(W_NUM)} \u20AC", fMB))
    doc.add(sepD())
    doc.add(sp())

    doc.add(p("*** Gracias por su compra ***", fMB,  Element.ALIGN_CENTER))
    doc.add(sp())
    doc.add(p("Conserve este ticket para",     fMSm, Element.ALIGN_CENTER))
    doc.add(p("cambios y devoluciones",         fMSm, Element.ALIGN_CENTER))
    doc.add(sp())

    doc.close()
}
