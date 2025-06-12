package org.joan.project.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.joan.project.viewmodel.AuthViewModel
import org.joan.project.viewmodel.LoginState
import org.koin.compose.koinInject
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.useResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import java.io.InputStream

@Composable
fun PantallaLogin(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = koinInject()
) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val loginState by authViewModel.loginState.collectAsState()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 300.dp, max = 400.dp)
                .align(Alignment.Center)
                .padding(24.dp),
            elevation = CardDefaults.cardElevation(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                MostrarLogo()

                Text(
                    text = "Iniciar Sesión",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = usuario,
                    onValueChange = { usuario = it },
                    label = { Text("Usuario") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            authViewModel.login(usuario, contrasena)
                        }
                    )
                )

                when (val state = loginState) {
                    is LoginState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is LoginState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { authViewModel.login(usuario, contrasena) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                    is LoginState.Success -> {
                        LaunchedEffect(Unit) {
                            onLoginSuccess()
                        }
                    }
                    else -> {
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                authViewModel.login(usuario, contrasena)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = usuario.isNotBlank() && contrasena.isNotBlank()
                        ) {
                            Text("Iniciar sesión")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MostrarLogo() {
    // Carga segura del recurso 'logo.png' desde resources
    val logo: Painter? = runCatching {
        BitmapPainter(useResource("logo.png") { inputStream: InputStream ->
            loadImageBitmap(inputStream)
        })
    }.getOrNull()

    if (logo != null) {
        Image(
            painter = logo,
            contentDescription = "Logo de la app",
            modifier = Modifier.size(90.dp) // tamaño adecuado para que no ocupe todo el espacio
        )
    } else {
        Text("No se pudo cargar la imagen")
    }
}
