package org.joan.project.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.joan.project.viewmodel.DatosNegocio
import org.joan.project.viewmodel.NegocioViewModel
import org.koin.compose.koinInject
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun PantallaAjustesNegocio(
    negocioViewModel: NegocioViewModel = koinInject(),
    onVolverClick: () -> Unit
) {
    val datos by negocioViewModel.datos.collectAsState()

    var nombre    by remember(datos) { mutableStateOf(datos.nombre) }
    var direccion by remember(datos) { mutableStateOf(datos.direccion) }
    var telefono  by remember(datos) { mutableStateOf(datos.telefono) }
    var cif       by remember(datos) { mutableStateOf(datos.cif) }
    var logoPath  by remember(datos) { mutableStateOf(datos.logoPath) }
    var guardado  by remember { mutableStateOf(false) }

    val logoPainter = remember(logoPath) {
        logoPath?.let { path ->
            runCatching {
                BitmapPainter(File(path).inputStream().buffered().use { loadImageBitmap(it) })
            }.getOrNull()
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isSmall = maxWidth < 700.dp
        val formWidth = if (isSmall) 1f else 0.65f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isSmall) 16.dp else 32.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = if (isSmall) Alignment.Start else Alignment.CenterHorizontally
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolverClick) {
                    Icon(Icons.Outlined.ArrowBack, "Volver")
                }
                Text(
                    "Datos del negocio",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth(formWidth)) {

                // ── Logo ──────────────────────────────────────────────────────
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Logo del negocio", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Preview cuadrado
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (logoPainter != null) {
                                    Image(
                                        painter = logoPainter,
                                        contentDescription = "Logo",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.size(80.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Outlined.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilledTonalButton(onClick = {
                                    val chooser = JFileChooser()
                                    chooser.dialogTitle = "Seleccionar logo"
                                    chooser.fileFilter = FileNameExtensionFilter(
                                        "Imágenes (PNG, JPG)", "png", "jpg", "jpeg"
                                    )
                                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                        logoPath = chooser.selectedFile.absolutePath
                                        guardado = false
                                    }
                                }) {
                                    Icon(Icons.Outlined.Upload, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Seleccionar imagen")
                                }
                                if (logoPath != null) {
                                    OutlinedButton(onClick = {
                                        logoPath = null
                                        guardado = false
                                    }) {
                                        Icon(Icons.Outlined.Delete, null, Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Quitar logo")
                                    }
                                }
                            }
                        }

                        if (logoPath != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                logoPath!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Datos del negocio ─────────────────────────────────────────
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Información fiscal y de contacto", style = MaterialTheme.typography.titleSmall)

                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it; guardado = false },
                            label = { Text("Nombre del negocio") },
                            leadingIcon = { Icon(Icons.Outlined.Store, null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = direccion,
                            onValueChange = { direccion = it; guardado = false },
                            label = { Text("Dirección") },
                            leadingIcon = { Icon(Icons.Outlined.LocationOn, null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it; guardado = false },
                            label = { Text("Teléfono") },
                            leadingIcon = { Icon(Icons.Outlined.Phone, null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = cif,
                            onValueChange = { cif = it; guardado = false },
                            label = { Text("CIF / NIF") },
                            leadingIcon = { Icon(Icons.Outlined.Badge, null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Vista previa ticket ───────────────────────────────────────
                TicketPreviewCard(DatosNegocio(nombre, direccion, telefono, cif, logoPath))

                Spacer(Modifier.height(24.dp))

                // ── Guardar ───────────────────────────────────────────────────
                Button(
                    onClick = {
                        negocioViewModel.guardar(nombre, direccion, telefono, cif, logoPath)
                        guardado = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Outlined.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar cambios", style = MaterialTheme.typography.titleMedium)
                }

                if (guardado) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Cambios guardados correctamente",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TicketPreviewCard(datos: DatosNegocio) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Vista previa del encabezado del ticket",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Text(datos.nombre,   style = MaterialTheme.typography.titleMedium)
            Text(datos.direccion, style = MaterialTheme.typography.bodySmall)
            Text("Tel: ${datos.telefono}", style = MaterialTheme.typography.bodySmall)
            Text(datos.cif,      style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
        }
    }
}
