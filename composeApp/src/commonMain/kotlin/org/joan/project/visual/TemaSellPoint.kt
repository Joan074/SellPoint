package org.joan.project.visual

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import sellpointjoan.composeapp.generated.resources.Res
import sellpointjoan.composeapp.generated.resources.Inter_Bold
import sellpointjoan.composeapp.generated.resources.Inter_Medium
import sellpointjoan.composeapp.generated.resources.Inter_Regular
import sellpointjoan.composeapp.generated.resources.Inter_SemiBold

// ---------------------------------------------------------------------------
// Paleta profesional para TPV — charcoal oscuro + teal como acento
// ---------------------------------------------------------------------------
private val ProfessionalColorScheme = lightColorScheme(

    // Acciones principales — charcoal muy oscuro
    primary            = Color(0xFF1C2B3A),
    onPrimary          = Color(0xFFFFFFFF),
    primaryContainer   = Color(0xFFDDE3EA),
    onPrimaryContainer = Color(0xFF0A1929),

    // Soporte — azul pizarra medio
    secondary            = Color(0xFF455A64),
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFFCFD8DC),
    onSecondaryContainer = Color(0xFF1C313A),

    // Acento puntual — teal sobrio
    tertiary            = Color(0xFF00796B),
    onTertiary          = Color(0xFFFFFFFF),
    tertiaryContainer   = Color(0xFFB2DFDB),
    onTertiaryContainer = Color(0xFF00251E),

    // Fondos y superficies
    background       = Color(0xFFF4F6F8),
    onBackground     = Color(0xFF1A1C1E),
    surface          = Color(0xFFFFFFFF),
    onSurface        = Color(0xFF1A1C1E),
    surfaceVariant   = Color(0xFFE8ECF0),
    onSurfaceVariant = Color(0xFF42474E),

    // Bordes y divisores
    outline = Color(0xFF8E9399),

    // Errores
    error   = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
)

private val AppShapes = Shapes(
    small  = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large  = RoundedCornerShape(16.dp),
)

// ---------------------------------------------------------------------------
// Tema principal
// ---------------------------------------------------------------------------
@Composable
fun SellPointTheme(content: @Composable () -> Unit) {
    val inter = FontFamily(
        Font(Res.font.Inter_Regular,  FontWeight.Normal),
        Font(Res.font.Inter_Medium,   FontWeight.Medium),
        Font(Res.font.Inter_SemiBold, FontWeight.SemiBold),
        Font(Res.font.Inter_Bold,     FontWeight.Bold),
    )

    val typography = Typography(
        displayLarge   = TextStyle(fontFamily = inter, fontWeight = FontWeight.Bold,     fontSize = 57.sp, lineHeight = 64.sp),
        headlineLarge  = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
        headlineMedium = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
        titleLarge     = TextStyle(fontFamily = inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
        titleMedium    = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 24.sp),
        titleSmall     = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge      = TextStyle(fontFamily = inter, fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium     = TextStyle(fontFamily = inter, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall      = TextStyle(fontFamily = inter, fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge     = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
        labelMedium    = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp),
        labelSmall     = TextStyle(fontFamily = inter, fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 16.sp),
    )

    MaterialTheme(
        colorScheme = ProfessionalColorScheme,
        typography  = typography,
        shapes      = AppShapes,
        content     = content
    )
}
