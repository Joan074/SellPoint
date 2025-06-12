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

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.insert-koin:koin-core:3.5.3")
                implementation("io.insert-koin:koin-compose:1.0.4")
                implementation("io.ktor:ktor-client-core:2.3.10")
                implementation("io.ktor:ktor-client-cio:2.3.10")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.10")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.10")
                implementation("io.ktor:ktor-client-logging:2.3.10")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation("org.jetbrains.compose.material:material-icons-extended:1.5.0")

                implementation(compose.foundation)
                implementation(compose.material3) // si usas Material3
                implementation(compose.runtime)


                implementation(project(":shared"))
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.cio)
                implementation("org.jetbrains.skiko:skiko:0.8.15")
                implementation("org.slf4j:slf4j-api:2.0.9")
                implementation("ch.qos.logback:logback-classic:1.4.11")
                implementation("io.github.qdsfdhvh:image-loader:1.4.0")
                implementation("com.github.librepdf:openpdf:1.3.30")
                implementation("com.itextpdf:itextpdf:5.5.13.3")


            }
        }
    }
}


compose.desktop {
    application {
        mainClass = "org.joan.project.main.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "SellPointJoan"
            packageVersion = "1.0.0"
        }
    }
}


