package org.joan.project

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joan.project.db.DatabaseFactory
import org.joan.project.db.Empleados
import org.joan.project.db.entidades.EmpleadoLoginRequest
import org.joan.project.db.entidades.TokenResponse
import org.mindrot.jbcrypt.BCrypt
import org.testcontainers.containers.PostgreSQLContainer

object TestContainerHelper {

    val postgres: PostgreSQLContainer<Nothing> by lazy {
        PostgreSQLContainer<Nothing>("postgres:16").apply { start() }
    }

    @Volatile private var dbInitialized = false

    fun initOnce() {
        if (dbInitialized) return
        synchronized(this) {
            if (dbInitialized) return
            val p = postgres
            System.setProperty("DB_URL", p.jdbcUrl)
            System.setProperty("DB_USER", p.username)
            System.setProperty("DB_PASSWORD", p.password)
            DatabaseFactory.init()
            transaction {
                val existe = Empleados.select { Empleados.usuario eq "admin" }.any()
                if (!existe) {
                    Empleados.insert {
                        it[nombre] = "Administrador"
                        it[usuario] = "admin"
                        it[contraseña] = BCrypt.hashpw("admin", BCrypt.gensalt())
                        it[rol] = "ADMIN"
                    }
                }
            }
            dbInitialized = true
        }
    }
}

fun ApplicationTestBuilder.jsonClient(): HttpClient = createClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

suspend fun HttpClient.loginAdmin(): String {
    val response = post("/auth/login") {
        contentType(ContentType.Application.Json)
        setBody(EmpleadoLoginRequest(usuario = "admin", contraseña = "admin"))
    }
    return response.body<TokenResponse>().token
}
