package org.joan.project

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.*
import io.ktor.http.ContentDisposition.Companion.File
import io.ktor.server.config.*
import io.ktor.server.response.*
import org.joan.project.db.entidades.EmpleadoPrincipal
import org.joan.project.db.repositories.TokenRepository
import java.io.File

fun Application.configureSecurity(tokenRepo: TokenRepository) {
    val config = ConfigFactory.load().getConfig("ktor.jwt")

    val jwtSecret = config.getString("secret")
    val jwtIssuer = config.getString("issuer")
    val jwtAudience = config.getString("audience")
    val jwtRealm = config.getString("realm")

    println("✅ Config JWT manual:")
    println("🔒 jwt.secret: $jwtSecret")
    println("🌐 jwt.issuer: $jwtIssuer")
    println("🎯 jwt.audience: $jwtAudience")
    println("🛡️ jwt.realm: $jwtRealm")

    install(Authentication) {
        jwt("jwt-auth") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                val username = credential.payload.getClaim("username").asString()
                if (!username.isNullOrBlank()) JWTPrincipal(credential.payload) else null
            }



            challenge { _, _ ->
                println("Error: Token no válido o expirado") // Depuración
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}

