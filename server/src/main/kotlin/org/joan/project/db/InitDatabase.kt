package org.joan.project.db

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

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
