package org.joan.project.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val dbUrl = System.getenv("DB_URL")
            ?: error("Falta la variable de entorno DB_URL")
        val dbUser = System.getenv("DB_USER")
            ?: error("Falta la variable de entorno DB_USER")
        val dbPassword = System.getenv("DB_PASSWORD")
            ?: error("Falta la variable de entorno DB_PASSWORD")

        val config = HikariConfig().apply {
            jdbcUrl = dbUrl
            driverClassName = "org.postgresql.Driver"
            username = dbUser
            password = dbPassword
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            poolName = "SellPointPool"
        }

        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Clientes,
                Empleados,
                Categorias,
                Proveedores,
                Productos,
                Ventas,
                DetalleVenta,
                Tokens
            )
        }
    }


    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}