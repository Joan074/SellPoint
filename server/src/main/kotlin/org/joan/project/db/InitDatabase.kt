package org.joan.project.db

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.math.BigDecimal

private val CATEGORIAS_SEED = listOf(
    "Salado", "Pan", "Dulce", "Frío", "Bolsas", "Varios",
    "Quino y Ana", "La Abuela", "Ilipast", "Cafes", "Lozano"
)

private val PRODUCTOS_PAN_SEED = listOf(
    "Gallega", "Bocadillo Normal", "Campesina", "Bocadillo Cereal",
    "Barra Maíz", "Bocadillo Integral", "Bocadillo Centeno", "Rústica",
    "Integral", "Barra Cereales", "Candeal", "Paris", "Bocata Espelta Centeno"
)

fun initDatabase(app: Application) {
    try {
        DatabaseFactory.init()

        // Mensaje de éxito en el log
        app.log.info("""
            
            🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉
            🎉                                           🎉
            🎉   🚀 ¡BASE DE DATOS INICIALIZADA! 🚀      🎉
            🎉                                           🎉
            🎉   ✅ Conexión exitosa                     🎉
            🎉   ✅ Configuración correcta               🎉
            🎉   ✅ Todo listo para usar                 🎉
            🎉                                           🎉
            🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉
            
        """.trimIndent())

        // Crear usuario admin por defecto si no existe
        transaction {
            val existe = Empleados.select { Empleados.usuario eq "admin" }.any()
            if (!existe) {
                val hashed = BCrypt.hashpw("admin", BCrypt.gensalt())
                Empleados.insert {
                    it[nombre] = "Administrador"
                    it[usuario] = "admin"
                    it[contraseña] = hashed
                    it[rol] = "ADMIN"
                }
                app.log.info("👤 Usuario por defecto 'admin' creado con contraseña 'admin'")
            } else {
                app.log.info("ℹ️ Usuario 'admin' ya existe")
            }
        }

        seedDatosIniciales(app)

    } catch (e: Exception) {
        app.log.error("""
            
            ❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌
            ❌                                                    ❌
            ❌   💥 ERROR EN LA BASE DE DATOS 💥                 ❌
            ❌                                                    ❌
            ❌   🚫 ${e.message?.take(50)?.padEnd(50)}  ❌
            ❌                                                    ❌
            ❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌❌
            
        """.trimIndent())
        throw e
    }
}

private fun seedDatosIniciales(app: Application) {
    transaction {
        // ── Categorías ──────────────────────────────────────────────────
        var nuevasCategorias = 0
        for (nombre in CATEGORIAS_SEED) {
            val existe = Categorias.select { Categorias.nombre eq nombre }.any()
            if (!existe) {
                Categorias.insert { it[Categorias.nombre] = nombre }
                nuevasCategorias++
            }
        }
        if (nuevasCategorias > 0)
            app.log.info("📂 $nuevasCategorias categorías creadas")

        // ── Proveedor por defecto ────────────────────────────────────────
        val proveedorId = Proveedores.selectAll()
            .firstOrNull()?.get(Proveedores.id)?.value
            ?: Proveedores.insert {
                it[nombre] = "Sin proveedor"
                it[contactoNombre] = ""
                it[contactoEmail] = ""
                it[contactoTelefono] = ""
                it[direccion] = ""
            }[Proveedores.id].value

        // ── Productos de Pan ─────────────────────────────────────────────
        val panId = Categorias.select { Categorias.nombre eq "Pan" }
            .firstOrNull()?.get(Categorias.id)?.value

        if (panId != null) {
            var nuevosProductos = 0
            for (nombreProducto in PRODUCTOS_PAN_SEED) {
                val existe = Productos.select {
                    (Productos.nombre eq nombreProducto) and
                    (Productos.categoriaId eq panId)
                }.any()
                if (!existe) {
                    Productos.insert {
                        it[nombre] = nombreProducto
                        it[precio] = BigDecimal.ZERO
                        it[stock] = 0
                        it[categoriaId] = panId
                        it[Productos.proveedorId] = proveedorId
                        it[activo] = true
                    }
                    nuevosProductos++
                }
            }
            if (nuevosProductos > 0)
                app.log.info("🍞 $nuevosProductos productos de Pan creados")
        }
    }
}
