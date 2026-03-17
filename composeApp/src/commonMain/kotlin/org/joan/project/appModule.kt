package org.joan.project

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.joan.project.service.*
import org.joan.project.viewmodel.*
import org.joan.project.viewmodel.NegocioViewModel
import org.koin.dsl.module
import org.koin.core.scope.get


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
    single { AuthViewModel(get(), get()) }  // get() = AuthService, get() = Settings

    single { ProductoService(get()) }
    single { ProductoViewModel(get()) }
    single { SupabaseStorageService(get()) }

    single { VentaService(get()) }
    single { VentaViewModel(get()) }

    single { ProveedorService(get()) }
    single { ProveedorViewModel(get()) }

    single { CategoriaService(get()) }
    single { CategoriaViewModel(get()) }

    single { NegocioViewModel(get()) }
    single { ClienteViewModel() }



}

