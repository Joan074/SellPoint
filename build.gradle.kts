plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.plugin.compose) apply false
}

tasks.register("esperarServidor") {
    group = "application"
    description = "Espera 3 s para que el servidor Ktor esté listo antes de arrancar el cliente"
    doLast {
        Thread.sleep(3_000)
    }
}
