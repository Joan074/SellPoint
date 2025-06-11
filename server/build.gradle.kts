plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass.set("org.joan.project.ApplicationKt")
    applicationDefaultJvmArgs = listOf(
        "-Dio.ktor.development=true",
        "-Dconfig.file=application.conf"
    )
}

kotlin {
    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

sourceSets {
    main {
        resources.srcDir("src/main/resources")
    }
}

dependencies {
    implementation(project(":shared"))

    // Ktor Server Core
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.resources)

    // Autenticación
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation("org.mindrot:jbcrypt:0.4")

    // Base de datos PostgreSQL + Exposed
    implementation(libs.postgresql)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.hikari.cp)

    // Utilidades
    implementation(libs.logback)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Testing
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.junit)
    testImplementation(libs.ktor.server.tests)
}
