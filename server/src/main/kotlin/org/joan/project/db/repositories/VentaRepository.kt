package org.joan.project.db.repositories

import org.joan.project.db.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.joan.project.db.*
import org.joan.project.db.entidades.*
import java.time.LocalDateTime
import java.math.BigDecimal

class VentaRepository(
    private val productoRepository: ProductoRepository,
    private val clienteRepository: ClienteRepository,
    private val empleadoRepository: EmpleadoRepository
) {


    suspend fun crearVenta(ventaRequest: VentaRequest): VentaResponse = dbQuery {
        // Validaciones
        val empleado = empleadoRepository.getById(ventaRequest.empleadoId)
            ?: throw IllegalArgumentException("Empleado no encontrado")
        val cliente = ventaRequest.clienteId?.let { clienteRepository.getById(it) }

        // Cálculos
        val (subtotal, iva) = calcularTotales(ventaRequest.items)
        val total = subtotal + iva - ventaRequest.descuento

        // Fecha única
        val fechaAhora = LocalDateTime.now()

        // Insertar venta
        val ventaId = Ventas.insertAndGetId {
            it[this.clienteId] = ventaRequest.clienteId
            it[this.empleadoId] = ventaRequest.empleadoId
            it[this.fecha] = fechaAhora
            it[this.subtotal] = BigDecimal.valueOf(subtotal)
            it[this.iva] = BigDecimal.valueOf(iva)
            it[this.total] = BigDecimal.valueOf(total)
            it[this.estado] = "COMPLETADA"
            it[this.metodoPago] = ventaRequest.metodoPago
            it[this.descuento] = BigDecimal.valueOf(ventaRequest.descuento)
        }.value

        // Procesar items
        val items = ventaRequest.items.map { item ->
            procesarItemVenta(ventaId, item)
        }

        // Generar y guardar número de ticket
        val numeroTicket = "T-${ventaId}-${fechaAhora.year}"
        Ventas.update({ Ventas.id eq ventaId }) {
            it[Ventas.numeroTicket] = numeroTicket
        }


        // Convertir cliente y empleado a sus versiones "Simple"
        val clienteSimple = cliente?.let {
            ClienteSimpleResponse(id = it.id, nombre = it.nombre)
        }

        val empleadoSimple = EmpleadoSimpleResponse(
            id = empleado.id,
            nombre = empleado.nombre
        )

        // Construir respuesta
        VentaResponse(
            id = ventaId,
            cliente = clienteSimple,
            empleado = empleadoSimple,
            fecha = fechaAhora.toString(),
            subtotal = subtotal,
            iva = iva,
            total = total,
            estado = "COMPLETADA",
            metodoPago = ventaRequest.metodoPago,
            items = items,
            numeroTicket = numeroTicket
        )
    }

    private suspend fun calcularTotales(items: List<ItemVentaRequest>): Pair<Double, Double> {
        var subtotal = 0.0
        var totalIva = 0.0

        items.forEach { item ->
            val producto = productoRepository.getById(item.productoId)
                ?: throw IllegalArgumentException("Producto ${item.productoId} no encontrado")

            val precio = item.precioEspecial ?: producto.precio
            val subtotalItem = precio * item.cantidad
            val ivaItem = subtotalItem * 0.16

            subtotal += subtotalItem
            totalIva += ivaItem
        }

        return Pair(subtotal, totalIva)
    }

    private suspend fun procesarItemVenta(ventaId: Int, item: ItemVentaRequest): ItemVentaResponse {
        val producto = productoRepository.getById(item.productoId)
            ?: throw IllegalArgumentException("Producto ${item.productoId} no encontrado")

        val precio = item.precioEspecial ?: producto.precio
        val subtotal = precio * item.cantidad
        val iva = subtotal * 0.16

        // Registrar detalle con descuento = 0 y sin promoción por ahora
        DetalleVenta.insert {
            it[DetalleVenta.ventaId] = ventaId
            it[productoId] = item.productoId
            it[cantidad] = item.cantidad
            it[precioUnitario] = precio.toBigDecimal()
            it[DetalleVenta.subtotal] = subtotal.toBigDecimal()
            it[DetalleVenta.iva] = iva.toBigDecimal()
            it[descuento] = item.descuento.toBigDecimal()
            if (item.promocionId != null) {
                it[promocionId] = item.promocionId
            }

            // Si en el futuro se usa promoción, añadir aquí: it[promocionId] = ...
        }

        // Actualizar stock
        productoRepository.restarStock(item.productoId, item.cantidad)

        return ItemVentaResponse(
            productoId = item.productoId,
            codigoBarras = producto.codigoBarras ?: "",
            nombre = producto.nombre,
            cantidad = item.cantidad,
            precioUnitario = precio,
            descuento = item.descuento,
            subtotal = subtotal,
            promocionId = item.promocionId
        )

    }

    suspend fun obtenerVentasPorFecha(
        desde: LocalDateTime,
        hasta: LocalDateTime,
        estado: String? = null
    ): List<VentaResponse> = dbQuery {
        val query = if (estado != null) {
            Ventas.select {
                Ventas.fecha.between(desde, hasta) and (Ventas.estado eq estado.uppercase())
            }
        } else {
            Ventas.select {
                Ventas.fecha.between(desde, hasta)
            }
        }

        query.map { ventaRow ->
            construirVentaResponse(ventaRow)
        }
    }


    suspend fun obtenerVentaPorId(id: Int): VentaResponse? = dbQuery {
        Ventas.select { Ventas.id eq id }
            .singleOrNull()
            ?.let { construirVentaResponse(it) }
    }

    private suspend fun construirVentaResponse(ventaRow: ResultRow): VentaResponse {
        val clienteId = ventaRow[Ventas.clienteId]
        val cliente = clienteId?.let { clienteRepository.getById(it) }
        val empleado = empleadoRepository.getById(ventaRow[Ventas.empleadoId])
            ?: throw IllegalStateException("Empleado no encontrado")

        val items = DetalleVenta
            .innerJoin(Productos)
            .select { DetalleVenta.ventaId eq ventaRow[Ventas.id] }
            .map { detalleRow ->
                ItemVentaResponse(
                    productoId = detalleRow[DetalleVenta.productoId].value,
                    codigoBarras = detalleRow[Productos.codigoBarras] ?: "",  // ← Aquí el cambio
                    nombre = detalleRow[Productos.nombre],
                    cantidad = detalleRow[DetalleVenta.cantidad],
                    precioUnitario = detalleRow[DetalleVenta.precioUnitario].toDouble(),
                    descuento = detalleRow[DetalleVenta.descuento].toDouble(),
                    subtotal = detalleRow[DetalleVenta.subtotal].toDouble(),
                    promocionId = detalleRow.getOrNull(DetalleVenta.promocionId)?.value // opcional si lo usas
                )
            }

        val clienteSimple = cliente?.let {
            ClienteSimpleResponse(id = it.id, nombre = it.nombre)
        }


        val empleadoSimple = EmpleadoSimpleResponse(
            id = empleado.id,
            nombre = empleado.nombre
        )

        return VentaResponse(
            id = ventaRow[Ventas.id].value,
            cliente = clienteSimple,
            empleado = empleadoSimple,
            fecha = ventaRow[Ventas.fecha].toString(),
            subtotal = ventaRow[Ventas.subtotal].toDouble(),
            iva = ventaRow[Ventas.iva].toDouble(),
            total = ventaRow[Ventas.total].toDouble(),
            estado = ventaRow[Ventas.estado],
            metodoPago = ventaRow[Ventas.metodoPago],
            items = items,
            numeroTicket = ventaRow.getOrNull(Ventas.numeroTicket)
        )
    }


    suspend fun anularVenta(id: Int): VentaResponse? = dbQuery {
        val ventaRow = Ventas.select { Ventas.id eq id }.singleOrNull() ?: return@dbQuery null

        // Si ya está anulada, no tocar nada
        if (ventaRow[Ventas.estado] == "ANULADA") {
            return@dbQuery construirVentaResponse(ventaRow) // ← devuelve igual, pero sin duplicar stock
        }

        // Si no lo está, anular y reponer stock
        Ventas.update({ Ventas.id eq id }) {
            it[estado] = "ANULADA"
        }

        val detalles = DetalleVenta.select { DetalleVenta.ventaId eq id }
        for (detalle in detalles) {
            val productoId = detalle[DetalleVenta.productoId].value
            val cantidad = detalle[DetalleVenta.cantidad]
            productoRepository.sumarStock(productoId, cantidad)
        }

        val ventaActualizada = Ventas.select { Ventas.id eq id }.single()
        construirVentaResponse(ventaActualizada)

    }

}
