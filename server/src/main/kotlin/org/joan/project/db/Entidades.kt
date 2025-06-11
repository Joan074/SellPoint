package org.joan.project.db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal
import org.jetbrains.exposed.sql.Table

object Clientes : IntIdTable() {
    val nombre = varchar("nombre", 255)
    val telefono = varchar("telefono", 20).nullable() // Para notificaciones
}

object Empleados : IntIdTable() {
    val nombre = varchar("nombre", 255)
    val usuario = varchar("usuario", 255).uniqueIndex()
    val contraseña = varchar("contraseña", 255)
    val rol = varchar("rol", 255)
}

object Categorias : IntIdTable() {
    val nombre = varchar("nombre", 255).uniqueIndex()
}

object Proveedores : IntIdTable() {
    val nombre = varchar("nombre", 255)
    val contactoNombre = varchar("contacto_nombre", 255)
    val contactoEmail = varchar("contacto_email", 255)
    val contactoTelefono = varchar("contacto_telefono", 255)
    val direccion = text("direccion")
    val activo = bool("activo").default(true)

}

object Productos : IntIdTable() {
    val nombre = varchar("nombre", 255)
    val precio = decimal("precio", 10, 2)
    val stock = integer("stock")
    val categoriaId = reference("categoria_id", Categorias)
    val proveedorId = reference("proveedor_id", Proveedores)
    val codigoBarras = varchar("codigo_barras", 50).nullable().uniqueIndex()
    val imagenUrl = varchar("imagen_url", 500).nullable() // 👈 Añadido
    val activo = bool("activo").default(true)
    val fechaCreacion = timestamp("fecha_creacion").defaultExpression(CurrentTimestamp())
}


object Ventas : IntIdTable("ventas") {
    val clienteId = integer("cliente_id").references(Clientes.id).nullable()
    val empleadoId = integer("empleado_id").references(Empleados.id)
    val fecha = datetime("fecha")
    val subtotal = decimal("subtotal", 12, 2).default(BigDecimal.ZERO)
    val iva = decimal("iva", 12, 2).default(BigDecimal.ZERO)
    val total = decimal("total", 12, 2).default(BigDecimal.ZERO)
    val estado = varchar("estado", 20).default("PENDIENTE")
    val metodoPago = varchar("metodo_pago", 20).default("EFECTIVO")
    val descuento = decimal("descuento", 12, 2).default(BigDecimal.ZERO)
    val numeroTicket = varchar("numero_ticket", 50).nullable()
}

object DetalleVenta : Table() {
    val ventaId = reference("venta_id", Ventas)
    val productoId = reference("producto_id", Productos)
    val cantidad = integer("cantidad")
    val precioUnitario = decimal("precio_unitario", 10, 2)
    val subtotal = decimal("subtotal", 10, 2)
    val iva = decimal("iva", 10, 2) // ← AÑADIDO AQUÍ
    val descuento = decimal("descuento", 10, 2).default(0.0.toBigDecimal())
    val promocionId = reference("promocion_id", Promociones).nullable()

    override val primaryKey = PrimaryKey(ventaId, productoId)
}


object FormasPago : IntIdTable() {
    val nombre = varchar("nombre", 50).uniqueIndex()
    val descripcion = text("descripcion").nullable()
    val activa = bool("activa").default(true)
}

object Pagos : IntIdTable() {
    val ventaId = reference("venta_id", Ventas)
    val formaPagoId = reference("forma_pago_id", FormasPago)
    val monto = decimal("monto", 10, 2)
    val referencia = varchar("referencia", 100).nullable() // Nº transacción, etc.
}

object Promociones : IntIdTable() {
    val nombre = varchar("nombre", 100)
    val tipo = varchar("tipo", 20) // DESCUENTO, 2x1, etc.
    val valor = decimal("valor", 10, 2)
    val fechaInicio = timestamp("fecha_inicio")
    val fechaFin = timestamp("fecha_fin")
    val activa = bool("activa").default(true)
}

object ProductoPromocion : Table() {
    val productoId = reference("producto_id", Productos)
    val promocionId = reference("promocion_id", Promociones)
    override val primaryKey = PrimaryKey(productoId, promocionId)
}

object Tokens : Table() {
    val empleadoId = integer("empleado_id").references(Empleados.id)
    val token = varchar("token", 255).uniqueIndex()
    val creadoEn = datetime("creado_en") // Usa datetime si vas a trabajar con LocalDateTime
    val expiracion = datetime("expiracion")
}