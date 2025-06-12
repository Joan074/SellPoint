package org.joan.project

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.joan.project.service.AuthService
import org.joan.project.service.ProductoService
import org.joan.project.viewmodel.AuthViewModel
import org.joan.project.viewmodel.ProductoViewModel
import org.koin.dsl.module
import org.joan.project.service.VentaService
import org.joan.project.viewmodel.VentaViewModel


// AppModule.kt en commonMain
val appModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }

            install(Logging) {
                level = LogLevel.HEADERS
                logger = object : Logger {
                    override fun log(message: String) {
                        println("HTTP Client: $message")
                    }
                }
            }

            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }

            expectSuccess = true
        }
    }

    // Servicios y ViewModels
    single { AuthService(get()) }
    single { AuthViewModel(get()) }

    single { ProductoService(get()) }
    single { ProductoViewModel(get()) }

    single { VentaService(get()) }
    single { VentaViewModel(get()) }

}

