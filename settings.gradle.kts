rootProject.name = "SellPointJoan"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/maven") }
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") } // <- añadido
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/maven") }
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") } // <- añadido

    }
}

include(":server", ":shared", ":composeApp")
