package org.joan.project.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.joan.project.viewmodel.AuthViewModel
import org.joan.project.viewmodel.LoginState
import org.joan.project.viewmodel.NegocioViewModel
import org.koin.compose.koinInject
import java.io.File
import java.io.InputStream

@Composable
fun PantallaLogin(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = koinInject(),
    negocioViewModel: NegocioViewModel = koinInject()
) {
    // Estado UI
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var attempted by remember { mutableStateOf(false) }

    val loginState by authViewModel.loginState.collectAsState()
    val negocio by negocioViewModel.datos.collectAsState()
    val isLoading = loginState is LoginState.Loading
    val focusManager = LocalFocusManager.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Reaccionar a éxito / error
    LaunchedEffect(loginState) {
        when (val s = loginState) {
            is LoginState.Success -> onLoginSuccess()
            is LoginState.Error -> scope.launch { snackbar.show("Error: ${s.message}") }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbar) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = .08f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = .08f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            // Decoración sutil
            Box(
                Modifier
                    .size(240.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-40).dp, y = (-40).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = .08f))
            )
            Box(
                Modifier
                    .size(320.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (40).dp, y = (40).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = .08f))
            )

            // Tarjeta central
            Card(
                modifier = Modifier
                    .widthIn(min = 340.dp, max = 440.dp)
                    .align(Alignment.Center),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ),
                elevation = CardDefaults.cardElevation(16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    LogoRedondo(logoPath = negocio.logoPath)
                    Text(
                        negocio.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        "Iniciar Sesión",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Usuario
                    OutlinedTextField(
                        value = usuario,
                        onValueChange = { usuario = it },
                        label = { Text("Usuario") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        isError = attempted && usuario.isBlank(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                    if (attempted && usuario.isBlank()) {
                        Text(
                            "Introduce tu usuario",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Contraseña
                    OutlinedTextField(
                        value = contrasena,
                        onValueChange = { contrasena = it },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        enabled = !isLoading,
                        isError = attempted && contrasena.isBlank(),
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
                                attempted = true
                                if (usuario.isNotBlank() && contrasena.isNotBlank()) {
                                    authViewModel.login(usuario, contrasena)
                                }
                            }
                        )
                    )
                    if (attempted && contrasena.isBlank()) {
                        Text(
                            "Introduce tu contraseña",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Recordarme / Olvidé contraseña
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it }, enabled = !isLoading)
                            Spacer(Modifier.width(6.dp))
                            Text("Recordarme", style = MaterialTheme.typography.bodyMedium)
                        }
                        TextButton(onClick = { /* TODO: acción */ }, enabled = !isLoading) {
                            Text("¿Olvidaste la contraseña?")
                        }
                    }

                    // Botón de acción
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            attempted = true
                            if (usuario.isNotBlank() && contrasena.isNotBlank()) {
                                authViewModel.login(usuario, contrasena)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isLoading && usuario.isNotBlank() && contrasena.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Text(if (isLoading) "Entrando..." else "Iniciar sesión")
                    }

                    // Pie con info ligera
                    Text(
                        text = "SellPoint TPV • Acceso seguro",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/* ---------- Logo redondo con fondo suave ---------- */
@Composable
private fun LogoRedondo(logoPath: String? = null) {
    val logoPainter = remember(logoPath) {
        if (logoPath != null) {
            // Logo personalizado desde archivo
            runCatching {
                BitmapPainter(File(logoPath).inputStream().buffered().use { loadImageBitmap(it) })
            }.getOrNull()
        } else {
            // Fallback al recurso logo.png del classpath
            runCatching {
                BitmapPainter(useResource("logo.png") { input: InputStream -> loadImageBitmap(input) })
            }.getOrNull()
        }
    }

    Box(
        modifier = Modifier
            .size(84.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = .10f)),
        contentAlignment = Alignment.Center
    ) {
        if (logoPainter != null) {
            Image(painter = logoPainter, contentDescription = "Logo", modifier = Modifier.size(64.dp))
        } else {
            Icon(
                Icons.Default.Store,
                contentDescription = "Logo",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/* ---------- Helpers ---------- */
private suspend fun SnackbarHostState.show(msg: String) {
    currentSnackbarData?.dismiss()
    showSnackbar(message = msg, withDismissAction = true)
}
