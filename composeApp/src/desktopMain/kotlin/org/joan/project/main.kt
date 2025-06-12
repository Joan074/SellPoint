package org.joan.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.jetbrains.compose.resources.painterResource
import org.joan.project.pantallas.PantallaLogin
import org.joan.project.viewmodel.AuthViewModel
import org.koin.compose.koinInject
import org.koin.core.context.startKoin

// Pantallas posibles
sealed class Pantalla {
    object Login : Pantalla()
    object Dashboard : Pantalla()
}

fun main() = application {
    // Iniciar Koin
    startKoin {
        modules(appModule)
    }

    // Estado de navegación
    var pantallaActual by remember { mutableStateOf<Pantalla>(Pantalla.Login) }

    // Composición principal
    Window(
        onCloseRequest = ::exitApplication,
        title = "SellPoint TPV",
        state = rememberWindowState(width = 1200.dp, height = 800.dp),
        icon = painterResource("logo.png") // ruta a tu icono dentro de resources

    ) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Color(0xFF006A6A),
                secondary = Color(0xFF6A006A),
                tertiary = Color(0xFF6A6A00)
            ),
            typography = Typography()
        ) {
            val authViewModel: AuthViewModel = koinInject()
            val currentUser by authViewModel.currentUser.collectAsState()
            val coroutineScope = rememberCoroutineScope()
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
