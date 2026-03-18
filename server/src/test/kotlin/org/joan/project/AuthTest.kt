package org.joan.project

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.joan.project.db.entidades.EmpleadoLoginRequest
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertEquals

class AuthTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() = TestContainerHelper.initOnce()
    }

    @Test
    fun `login con credenciales correctas devuelve 200`() = testApplication {
        application { module() }
        val client = jsonClient()
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(EmpleadoLoginRequest(usuario = "admin", contraseña = "admin"))
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `login con contrasena incorrecta devuelve 401`() = testApplication {
        application { module() }
        val client = jsonClient()
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(EmpleadoLoginRequest(usuario = "admin", contraseña = "wrongpassword"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `login con usuario inexistente devuelve 401`() = testApplication {
        application { module() }
        val client = jsonClient()
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(EmpleadoLoginRequest(usuario = "noexiste", contraseña = "cualquiera"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `acceso a ruta protegida sin token devuelve 401`() = testApplication {
        application { module() }
        val client = jsonClient()
        val response = client.get("/producto")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `health check devuelve 200`() = testApplication {
        application { module() }
        val client = jsonClient()
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
