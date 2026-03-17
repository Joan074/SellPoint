package org.joan.project.visual

import com.lowagie.text.*
import com.lowagie.text.Image as PdfImage
import com.lowagie.text.pdf.PdfWriter
import org.joan.project.db.entidades.ProductoResponse
import org.joan.project.db.entidades.VentaRequest
import org.joan.project.viewmodel.DatosNegocio
import java.io.File
import java.io.FileOutputStream
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// ── Layout del ticket ─────────────────────────────────────────────────────
//
//   Courier 8pt: ancho carácter = 4,8 pt
//   Página 80 mm = 226,77 pt; márgenes 8 pt × 2 → útil ≈ 210,77 pt
//   Máximo seguro: floor(210,77 / 4,8) = 43 caracteres.
//   Usamos 40 para dejar margen extra.
//
//   Columnas productos (total = 40):
//     DESC  16  — descripción, alineada a la izquierda
//     CANT   4  — cantidad,    alineada a la derecha
//     PREC  10  — precio unit, alineado  a la derecha (solo dígitos + coma)
//     IMPO  10  — importe,     alineado  a la derecha (solo dígitos + coma)
//
//   Líneas de totales (total = 40):
//     LABEL 25  — etiqueta, alineada a la izquierda
//     NUM   13  — importe,  alineado  a la derecha  ← solo dígitos + coma
//     SPC    2  — " €"  fijo al final                ← FUERA del padding
//
//   CLAVE: los columnas numéricas usan SOLO caracteres ASCII (dígitos, coma,
//   guión) para garantizar anchura fija en Courier. El símbolo "€" se añade
//   SIEMPRE en una posición fija fuera del bloque paddeado.
//
private const val W      = 40        // anchura de línea en caracteres
private const val W_DESC = 16
private const val W_CANT =  4
private const val W_PREC = 10
private const val W_IMPO = 10        // W_DESC+W_CANT+W_PREC+W_IMPO = 40

private const val W_LABEL = 25
private const val W_NUM   = 13       // W_LABEL + W_NUM + 2(" €") = 40

private val SEP_D = "=".repeat(W)
private val SEP_S = "-".repeat(W)

// ─────────────────────────────────────────────────────────────────────────

fun generarTicketPDF(
    venta: VentaRequest,
    productos: List<ProductoResponse>,
    archivo: File,
    nombreEmpleado: String = "",
    negocio: DatosNegocio = DatosNegocio(),
    nombreCliente: String? = null
) {
    val pageHeight = (360f + venta.items.size * 14f).coerceAtLeast(500f)

    val doc = Document(
        Rectangle(226.77f, pageHeight),
        8f, 8f, 10f, 10f     // left, right, top, bottom
    )
    PdfWriter.getInstance(doc, FileOutputStream(archivo))
    doc.open()

    // ── Fuentes ──────────────────────────────────────────────────────────
    val fNeg   = Font(Font.HELVETICA, 12f, Font.BOLD)
    val fSub   = Font(Font.HELVETICA,  7.5f)
    val fM     = Font(Font.COURIER,    8f)
    val fMB    = Font(Font.COURIER,    8f, Font.BOLD)
    val fMSm   = Font(Font.COURIER,    7f)

    // ── Fecha y número de ticket ─────────────────────────────────────────
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val fecha     = "%02d/%02d/%04d".format(now.dayOfMonth, now.monthNumber, now.year)
    val hora      = "%02d:%02d".format(now.hour, now.minute)
    val ticketNum = "T-%04d%02d%02d-%04d".format(
        now.year, now.monthNumber, now.dayOfMonth,
        (System.currentTimeMillis() % 10000).toInt()
    )

    // ── Helpers ──────────────────────────────────────────────────────────
    fun p(txt: String, f: Font = fM, align: Int = Element.ALIGN_LEFT) =
        Paragraph(txt, f).apply { alignment = align; spacingBefore = 0f; spacingAfter = 0f }

    fun sp()   = p(" ", fMSm)
    fun sepD() = p(SEP_D, fM)
    fun sepS() = p(SEP_S, fM)

    // Número decimal con coma — solo ASCII, seguro para padding en Courier
    fun num(d: Double)             = "%.2f".format(d).replace(".", ",")
    fun numNeg(d: Double)          = "-${num(d)}"
    // Línea de total: label(25) + número(13) + " €" = 40
    fun totalLine(label: String, d: Double, neg: Boolean = false, f: Font = fM) {
        val n = if (neg) numNeg(d) else num(d)
        doc.add(p("${label.padEnd(W_LABEL)}${n.padStart(W_NUM)} \u20AC", f))
    }

    // ── CABECERA ─────────────────────────────────────────────────────────
    doc.add(sp())

    // Logo (si está configurado)
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

    // ── INFO DEL TICKET ───────────────────────────────────────────────────
    fun info(lbl: String, v: String) = p("${lbl.padEnd(9)}: $v", fM)
    doc.add(info("Ticket",  ticketNum))
    doc.add(info("Fecha",   "$fecha  $hora h"))
    doc.add(info("Pago",    metodoStr(venta.metodoPago)))
    if (nombreEmpleado.isNotBlank()) doc.add(info("Cajero", nombreEmpleado))
    doc.add(info("Cliente", nombreCliente ?: "Cliente basico"))
    doc.add(sp())
    doc.add(sepS())
    doc.add(sp())

    // ── CABECERA TABLA ────────────────────────────────────────────────────
    //   Todas las cadenas son ASCII puro → anchura garantizada en Courier.
    //   DESC(16) + CANT(4) + PRECIO(10) + IMPORTE(10) = 40
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

    // ── LÍNEAS DE PRODUCTOS ───────────────────────────────────────────────
    //   Columnas numéricas: solo dígitos y coma decimal.  SIN "€".
    //   Ejemplo (qty=2, pu=1.20, tot=2.40):
    //     "Coca-Cola 33cl     2      1,20      2,40"
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

    // ── TOTALES ───────────────────────────────────────────────────────────
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

    // TOTAL — mismo esquema label(25)+num(13)+" €"=40, en negrita
    doc.add(p("${"TOTAL".padEnd(W_LABEL)}${num(totalConDesc).padStart(W_NUM)} \u20AC", fMB))

    doc.add(sepD())
    doc.add(sp())

    // ── PIE ───────────────────────────────────────────────────────────────
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
