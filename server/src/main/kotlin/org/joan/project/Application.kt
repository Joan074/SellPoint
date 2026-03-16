package org.joan.project

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joan.project.db.initDatabase
import org.joan.project.db.repositories.*
import org.slf4j.event.Level

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    println("Valor de jwt.realm: " + environment.config.propertyOrNull("jwt.realm")?.getString())

    val tokenRepo = TokenRepository()
    configureSecurity(tokenRepo) // Configurar autenticación
    configureSerialization()

    // Inicialización de la base de datos
    initDatabase(this)

    val empleadoRepo = EmpleadoRepository()
    val productoRepo = ProductoRepository()
    val clienteRepo = ClienteRepository()
    val categoriaRepo = CategoriaRepository()
    val proveedorRepo = ProveedorRepository()
    val reporteRepo = ReporteRepository()
    val ventaRepo = VentaRepository(productoRepo, clienteRepo, empleadoRepo)

    configureRouting(
        empleadoRepo,
        ventaRepo,
        productoRepo,
        categoriaRepo,
        proveedorRepo,
        reporteRepo,
        tokenRepo
    )

    // Limpieza periódica de tokens expirados
    environment.monitor.subscribe(ApplicationStarted) {
        launch {
            while (true) {
                try {
                    tokenRepo.limpiarTokensExpirados()
                    println("🧹 Tokens expirados eliminados")
                } catch (e: Exception) {
                    println("⚠️ Error al limpiar tokens expirados: \${e.message}")
                }
                delay(60 * 60 * 1000L) // cada 1 hora
            }
        }
    }

    // Configuración del servidor
    install(CallLogging) {
        level = Level.INFO
    }

    routing {
        get("/") {
            call.respondText("SellPoint API v1.0")
        }

        get("/health") {
            call.respond(
                mapOf(
                    "status" to "OK",
                    "database" to "Connected",
                    "version" to "1.0.0"
                )
            )
        }
    }
}
