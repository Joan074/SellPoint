plugins {
    // Aplica los plugins que necesites en el root project
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktor) apply false

    // Solo incluye los plugins multiplataforma si realmente los necesitas
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
}