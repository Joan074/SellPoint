package org.joan.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.res.painterResource as desktopPainterResource
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import org.jetbrains.compose.resources.painterResource
import org.joan.project.pantallas.PantallaLogin
import org.joan.project.viewmodel.AuthViewModel
import org.joan.project.visual.SellPointTheme
import org.koin.compose.koinInject
import org.koin.core.context.startKoin

// Pantallas posibles
sealed class Pantalla {
    object Login : Pantalla()
    object Dashboard : Pantalla()
}

fun main() = try {
    // Initialise Coil3 with OkHttp network support for HTTP image URLs
    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory()) }
            .build()
    }

    application {
        // Iniciar Koin
        startKoin {
            modules(desktopModule, appModule)
        }

        // Estado de navegación
        var pantallaActual by remember { mutableStateOf<Pantalla>(Pantalla.Login) }

        // Composición principal
        Window(
            onCloseRequest = ::exitApplication,
            title = "SellPoint TPV",
            state = rememberWindowState(width = 1200.dp, height = 800.dp),
            icon = desktopPainterResource("logo.png")

        ) {
            SellPointTheme {
                val authViewModel: AuthViewModel = koinInject()
                val currentUser by authViewModel.currentUser.collectAsState()
                var checkingSession by remember { mutableStateOf(true) }

                // Validar sesión activa al iniciar
                LaunchedEffect(Unit) {
                    checkingSession = true
                    val isValid = authViewModel.validateSession()
                    checkingSession = false
                    pantallaActual = if (isValid && currentUser != null) Pantalla.Dashboard else Pantalla.Login
                }

                // Indicador de carga
                if (checkingSession) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    when (pantallaActual) {
                        is Pantalla.Login -> PantallaLogin(
                            onLoginSuccess = {
                                pantallaActual = Pantalla.Dashboard
                            }
                        )

                        is Pantalla.Dashboard -> currentUser?.let {
                            AppPrincipal(
                                currentUser = it,
                                onLogout = {
                                    authViewModel.logout()
                                    pantallaActual = Pantalla.Login
                                }
                            )
                        }
                    }
                }
            }
        }
    }
} catch (e: Exception) {
    e.printStackTrace()
    println("Error en la aplicación: ${e.message}")
    Thread.sleep(10000) // Pausa para que puedas leer el error en consola
}
