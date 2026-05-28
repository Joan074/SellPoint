import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.plugin.compose)
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }

    jvm("desktop")
    jvmToolchain(17)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.insert-koin:koin-core:3.5.3")
                implementation("io.insert-koin:koin-compose:1.0.4")

                implementation("io.ktor:ktor-client-core:2.3.10")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.10")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.10")
                implementation("io.ktor:ktor-client-logging:2.3.10")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)

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

                // Ktor CIO engine (desktop only)
                implementation(libs.ktor.client.cio)

                implementation("org.jetbrains.skiko:skiko:0.8.15")
                implementation("org.slf4j:slf4j-api:2.0.9")
                implementation("ch.qos.logback:logback-classic:1.4.11")

                implementation("com.github.librepdf:openpdf:1.3.30")
                implementation("com.itextpdf:itextpdf:5.5.13.3")

                // Coil3 OkHttp network fetcher (JVM desktop)
                implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.9.3")
                implementation("androidx.core:core-ktx:1.13.1")

                // Ktor OkHttp engine (Android)
                implementation("io.ktor:ktor-client-okhttp:2.3.10")

                // Coil3 OkHttp network fetcher (Android)
                implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")

                // Koin Android
                implementation("io.insert-koin:koin-android:3.5.3")
            }
        }
    }
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
    }
}

android {
    namespace = "org.joan.project"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.joan.project"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
}

compose.desktop {
    application {
        mainClass = "org.joan.project.MainKt"
        nativeDistributions {
            targetFormats(
                TargetFormat.Exe,
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb
            )
            packageName = "SellPointJoan"
            packageVersion = "1.0.0"
            windows {
                iconFile.set(project.file("src/desktopMain/resources/logo.ico"))
            }
        }
    }
}
