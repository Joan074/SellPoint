import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.plugin.compose) // <- aquí sí lo encuentra desde TOML
}



kotlin {
    jvm("desktop")

    // Recomendado: fija JDK 17 para evitar sorpresas
    jvmToolchain(17)

    sourceSets {
        val commonMain by getting {
            dependencies {
                // DI / Ktor / KotlinX (usa tus versiones si prefieres, pero evita duplicados)
                implementation("io.insert-koin:koin-core:3.5.3")
                implementation("io.insert-koin:koin-compose:1.0.4")

                implementation("io.ktor:ktor-client-core:2.3.10")
                implementation("io.ktor:ktor-client-cio:2.3.10")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.10")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.10")
                implementation("io.ktor:ktor-client-logging:2.3.10")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

                // Compose: usa SIEMPRE los aliases para no mezclar versiones
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended) // <-- reemplaza la 1.5.0 hardcodeada

                // Image loading with Coil3
                implementation("io.coil-kt.coil3:coil-compose:3.0.4")

                // Charts
                implementation("io.github.koalaplot:koalaplot-core:0.6.3")

                // Date/time multiplatform
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

                // Persistent settings
                implementation("com.russhwolf:multiplatform-settings:1.1.1")

                implementation(project(":shared"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.cio)

                implementation("org.jetbrains.skiko:skiko:0.8.15")
                implementation("org.slf4j:slf4j-api:2.0.9")
                implementation("ch.qos.logback:logback-classic:1.4.11")

                // ❌ No necesitas image-loader aquí si ya está en commonMain
                // implementation("io.github.qdsfdhvh:image-loader:1.4.0")

                implementation("com.github.librepdf:openpdf:1.3.30")
                implementation("com.itextpdf:itextpdf:5.5.13.3")

                // Coil3 OkHttp network fetcher (JVM desktop)
                implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")
            }
        }
    }
}


compose.desktop {
    application {
        mainClass = "org.joan.project.MainKt"
        nativeDistributions {
            targetFormats(
                TargetFormat.Exe,  // <-- Añadido para Windows ejecutable
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb
            )
            packageName = "SellPointJoan"
            packageVersion = "1.0.0"
            // Opcional: icono para Windows
            windows {
                iconFile.set(project.file("src/desktopMain/resources/logo.ico"))
            }
        }
    }
}



