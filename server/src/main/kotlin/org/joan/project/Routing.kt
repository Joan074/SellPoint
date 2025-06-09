package org.joan.project

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.joan.project.db.entidades.*
import org.joan.project.db.repositories.*
import java.time.LocalDate
import java.util.*
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

fun Application.configureRouting(
    empleadoRepo: EmpleadoRepository,
    ventaRepo: VentaRepository,
    productoRepo: ProductoRepository,
    categoriaRepo: CategoriaRepository,
    proveedorRepo: ProveedorRepository,
    reporteRepo: ReporteRepository,
    tokenRepo: TokenRepository
    ) {
    routing {
        // Ruta base y health check
        get("/") {
            call.respondText("SellPoint API v1.0")
        }

        get("/health") {
            call.respond(mapOf(
                "status" to "OK",
                "version" to "1.0.0"
            ))
        }

        // Autenticación
        post("/auth/login") {
            val loginRequest = call.receive<EmpleadoLoginRequest>()
            println("🟢 Intentando autenticar usuario: ${loginRequest.usuario}")

            val empleado = empleadoRepo.autenticar(loginRequest)
            if (empleado == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(401, "Usuario o contraseña incorrectos")
                )
                return@post
            }

            val jwtConfig = ConfigFactory.load().getConfig("ktor.jwt")
            val jwtSecret = jwtConfig.getString("secret")
            val jwtIssuer = jwtConfig.getString("issuer")
            val jwtAudience = jwtConfig.getString("audience")
            val expiracionMs = 1000 * 60 * 60 * 24 * 14 // 14 días

            val expiracion = System.currentTimeMillis() + expiracionMs

            val token = JWT.create()
                .withAudience(jwtAudience)
                .withIssuer(jwtIssuer)
                .withClaim("username", empleado.usuario)
                .withClaim("rol", empleado.rol)
                .withExpiresAt(Date(expiracion))
                .sign(Algorithm.HMAC256(jwtSecret))

            // ✅ Guardar el token en la base de datos
            tokenRepo.guardarToken(
                empleadoId = empleado.id,
                token = token,
                expiracion = Date(expiracion).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
            )

            call.respond(
                TokenResponse(
                    token = token,
                    expiracion = Date(expiracion).toString(),
                    empleado = empleado
                )
            )
        }


        post("/auth/logout") {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")?.trim()

            if (token == null || !tokenRepo.validarToken(token)) {
                call.respond(HttpStatusCode.Unauthorized, "Token inválido o ya expirado")
                return@post
            }

            tokenRepo.eliminarToken(token)
            call.respond(HttpStatusCode.OK, mapOf("mensaje" to "Sesión cerrada"))
        }



        // Rutas protegidas con JWT
        authenticate("jwt-auth") {
            route("/ventas") {

                get {
                    val desdeStr = call.request.queryParameters["desde"]
                    val hastaStr = call.request.queryParameters["hasta"]
                    val estado = call.request.queryParameters["estado"] // Puede ser COMPLETADA, PENDIENTE, ANULADA, etc.

                    val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

                    val desde = try {
                        desdeStr?.let { LocalDateTime.parse(it, formatter) }
                    } catch (e: Exception) {
                        null
                    } ?: LocalDateTime.now().minusMonths(1)

                    val hasta = try {
                        hastaStr?.let { LocalDateTime.parse(it, formatter) }
                    } catch (e: Exception) {
                        null
                    } ?: LocalDateTime.now()

                    println("⏳ Buscando ventas desde $desde hasta $hasta con estado = ${estado ?: "TODOS"}")

                    val ventas = ventaRepo.obtenerVentasPorFecha(desde, hasta, estado)

                    call.respond(ventas)
                }


                get("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, "ID inválido"))
                        return@get
                    }

                    val venta = ventaRepo.obtenerVentaPorId(id)
                    if (venta == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(404, "Venta no encontrada"))
                    } else {
                        call.respond(venta)
                    }
                }

                post {
                    val request = call.receive<VentaRequest>()
                    try {
                        val nuevaVenta = ventaRepo.crearVenta(request)
                        call.respond(HttpStatusCode.Created, nuevaVenta)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, e.message ?: "Error de validación"))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse(500, "Error inesperado", e.message))
                    }
                }

                get("/cliente/{clienteId}") {
                    val clienteId = call.parameters["clienteId"]?.toIntOrNull()
                    if (clienteId == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, "ID de cliente inválido"))
                        return@get
                    }

                    val ventasCliente = ventaRepo.obtenerVentasPorFecha(
                        LocalDateTime.now().minusYears(1),
                        LocalDateTime.now()
                    ).filter { it.cliente?.id == clienteId }

                    call.respond(ventasCliente)
                }

                put("/{id}/anular") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, "ID inválido"))
                        return@put
                    }

                    val anulada = ventaRepo.anularVenta(id)
                    if (anulada == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(404, "Venta no encontrada o ya anulada"))
                    } else {
                        call.respond(anulada)
                    }
                }

                get("/hoy") {
                    val ahora = LocalDateTime.now()
                    val inicioDelDia = ahora.toLocalDate().atStartOfDay()
                    val finDelDia = inicioDelDia.plusDays(1).minusNanos(1)

                    val ventasHoy = ventaRepo.obtenerVentasPorFecha(
                        desde = inicioDelDia,
                        hasta = finDelDia
                    )

                    call.respond(ventasHoy)
                }
            }
        }


        authenticate("jwt-auth") {
            route("/producto") {

                get {
                    val productos = productoRepo.getAll()
                    call.respond(productos)
                }

                get("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, "ID inválido"))
                        return@get
                    }

                    val producto = productoRepo.getById(id)
                    if (producto == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(404, "Producto no encontrado"))
                    } else {
                        call.respond(producto)
                    }
                }

                get("/buscar") {
                    val term = call.request.queryParameters["term"] ?: ""
                    val resultados = productoRepo.buscarProductosPorNombre(term)
                    call.respond(resultados)
                }

                post {
                    val request = call.receive<ProductoRequest>()
                    val creado = productoRepo.create(request)
                    call.respond(HttpStatusCode.Created, creado)
                }

                put("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    val productoRequest = call.receive<ProductoRequest>()

                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, "ID inválido")
                        return@put
                    }

                    try {
                        val result = productoRepo.update(id, productoRequest)
                        if (result) {
                            call.respond(HttpStatusCode.OK, "Producto actualizado")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Producto no encontrado")
                        }
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Error de validación")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error inesperado")
                    }
                }

                put("/{id}/precio") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    val body = call.receive<PrecioUpdateRequest>()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, "ID inválido"))
                        return@put
                    }

                    val actualizado = productoRepo.actualizarPrecio(id, body.precio)
                    if (actualizado) {
                        call.respond(HttpStatusCode.OK, mapOf("mensaje" to "Precio actualizado"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(404, "Producto no encontrado"))
                    }
                }

                delete("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, "ID inválido"))
                        return@delete
                    }

                    val eliminado = productoRepo.delete(id)
                    if (eliminado) {
                        call.respond(HttpStatusCode.OK, mapOf("mensaje" to "Producto eliminado"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(404, "Producto no encontrado"))
                    }
                }

                put("/{id}/stock") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, "ID inválido"))
                        return@put
                    }

                    val body = call.receive<Map<String, Int>>()
                    val nuevoStock = body["stock"]
                    if (nuevoStock == null || nuevoStock < 0) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, "Stock inválido"))
                        return@put
                    }

                    val actualizado = productoRepo.actualizarStock(id, nuevoStock)
                    if (actualizado) {
                        call.respond(HttpStatusCode.OK, mapOf("mensaje" to "Stock actualizado"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(404, "Producto no encontrado"))
                    }
                }

            }
        }

        authenticate("jwt-auth") {
            route("/empleados") {

                get {
                    val empleados = empleadoRepo.getAll()
                    call.respond(empleados)
                }

                get("/yo") {
                    val empleado = call.principal<EmpleadoPrincipal>()  // Esto lo da el JWT
                    if (empleado == null) {
                        call.respond(HttpStatusCode.Unauthorized)
                        return@get
                    }

                    val empleadoData = empleadoRepo.getById(empleado.id)
                    if (empleadoData != null) {
                        call.respond(empleadoData)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Empleado no encontrado"))
                    }
                }


                get("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))
                        return@get
                    }
                    val empleado = empleadoRepo.getById(id)
                    if (empleado != null) {
                        call.respond(empleado)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Empleado no encontrado"))
                    }
                }

                post {
                    try {
                        val request = call.receive<EmpleadoRequest>()
                        val nuevo = empleadoRepo.crearEmpleado(request)
                        call.respond(HttpStatusCode.Created, nuevo)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                    }
                }


                put("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))
                        return@put
                    }

                    try {
                        val request = call.receive<EmpleadoRequest>()
                        val actualizado = empleadoRepo.actualizarEmpleado(id, request)
                        if (actualizado) {
                            call.respond(mapOf("mensaje" to "Empleado actualizado"))
                        } else {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Empleado no encontrado"))
                        }
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
                    }
                }


                delete("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))
                        return@delete
                    }
                    val eliminado = empleadoRepo.eliminarEmpleado(id)
                    if (eliminado) {
                        call.respond(mapOf("mensaje" to "Empleado eliminado"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Empleado no encontrado"))
                    }
                }
            }

        }

        authenticate("jwt-auth") {

            route("/categorias") {

                get {
                    call.respond(categoriaRepo.getAll())
                }

                get("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))
                        return@get
                    }
                    val categoria = categoriaRepo.getById(id)
                    if (categoria != null) {
                        call.respond(categoria)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Categoría no encontrada"))
                    }
                }

                post {
                    val request = call.receive<CategoriaRequest>()
                    val nueva = categoriaRepo.crearCategoria(request.nombre)
                    call.respond(HttpStatusCode.Created, nueva)
                }

                put("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    val request = call.receive<CategoriaRequest>()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))
                        return@put
                    }

                    val actualizado = categoriaRepo.actualizarCategoria(id, request.nombre)
                    if (actualizado) {
                        call.respond(mapOf("mensaje" to "Categoría actualizada"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Categoría no encontrada"))
                    }
                }

                delete("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))
                        return@delete
                    }

                    val eliminado = categoriaRepo.eliminarCategoria(id)
                    if (eliminado) {
                        call.respond(mapOf("mensaje" to "Categoría eliminada"))
                    } else {
                        call.respond(
                            HttpStatusCode.Conflict,
                            mapOf("error" to "No se puede eliminar la categoría porque tiene productos asignados")
                        )
                    }
                }
            }
    }

        authenticate("jwt-auth") {
            route("/proveedores") {

                get {
                    val proveedores = proveedorRepo.getAll()
                    call.respond(proveedores)
                }

                get("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, "ID inválido")
                        return@get
                    }

                    val proveedor = proveedorRepo.getById(id)
                    if (proveedor == null) {
                        call.respond(HttpStatusCode.NotFound, "Proveedor no encontrado")
                    } else {
                        call.respond(proveedor)
                    }
                }

                post {
                    val request = call.receive<ProveedorRequest>()
                    val creado = proveedorRepo.crearProveedor(request)
                    call.respond(HttpStatusCode.Created, creado)
                }

                put("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, "ID inválido")
                        return@put
                    }

                    val request = call.receive<ProveedorRequest>()
                    val actualizado = proveedorRepo.actualizarProveedor(id, request)
                    if (actualizado) {
                        call.respond(HttpStatusCode.OK, "Proveedor actualizado")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Proveedor no encontrado")
                    }
                }

                delete("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, "ID inválido")
                        return@delete
                    }

                    val eliminado = proveedorRepo.eliminarProveedor(id)
                    if (eliminado) {
                        call.respond(HttpStatusCode.OK, "Proveedor desactivado correctamente")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Proveedor no encontrado o ya estaba inactivo")
                    }
                }

            }
        }

        authenticate("jwt-auth") {
            route("/reporte") {

                get("/hoy") {
                    val reporte = reporteRepo.generarReporteHoy()
                    call.respond(reporte)
                }

                get("/diario") {
                    val fechaTexto = call.request.queryParameters["fecha"]
                    if (fechaTexto == null) {
                        call.respond(HttpStatusCode.BadRequest, "Debe proporcionar una fecha con el parámetro 'fecha'")
                        return@get
                    }

                    try {
                        val fecha = LocalDate.parse(fechaTexto)
                        val reporte = reporteRepo.generarReporteDiario(fecha)
                        call.respond(reporte)
                    } catch (e: DateTimeParseException) {
                        call.respond(HttpStatusCode.BadRequest, "Formato de fecha inválido (use YYYY-MM-DD)")
                    }
                }

                get("/rango") {
                    val inicioTexto = call.request.queryParameters["inicio"]
                    val finTexto = call.request.queryParameters["fin"]

                    if (inicioTexto == null || finTexto == null) {
                        call.respond(HttpStatusCode.BadRequest, "Debe proporcionar los parámetros 'inicio' y 'fin'")
                        return@get
                    }

                    try {
                        val inicio = LocalDate.parse(inicioTexto)
                        val fin = LocalDate.parse(finTexto)
                        val reporte = reporteRepo.generarReportePorRango(inicio, fin)
                        call.respond(reporte)
                    } catch (e: DateTimeParseException) {
                        call.respond(HttpStatusCode.BadRequest, "Formato de fechas inválido (use YYYY-MM-DD)")
                    }
                }

                get("/metodos-pago") {
                    val resumen = reporteRepo.obtenerTotalesPorMetodoPago()
                    call.respond(resumen)
                }

            }
        }

    }
}

