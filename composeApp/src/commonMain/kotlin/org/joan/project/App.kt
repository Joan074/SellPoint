package org.joan.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.joan.project.pantallas.PantallaLogin
import org.joan.project.viewmodel.AuthViewModel
import org.joan.project.visual.SellPointTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    SellPointTheme {
        val authViewModel: AuthViewModel = koinInject()
        val currentUser by authViewModel.currentUser.collectAsState()
        var checkingSession by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            authViewModel.validateSession()
            checkingSession = false
        }

        when {
            checkingSession -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            currentUser != null -> {
                AppPrincipal(
                    currentUser = currentUser!!,
                    onLogout = { authViewModel.logout() }
                )
            }
            else -> {
                PantallaLogin(onLoginSuccess = {})
            }
        }
    }
}
