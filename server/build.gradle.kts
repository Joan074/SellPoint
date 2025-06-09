plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass.set("org.joan.project.ApplicationKt")

    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true", "-Dconfig.file=application.conf")

}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_19)
        }
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

dependencies {
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
    implementation(libs.bcrypt)

    // Base de datos PostgreSQL + Exposed
    implementation(libs.postgresql)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.hikari.cp)

    // MongoDB (opcional)
    implementation(libs.kmongo.coroutine)
    implementation(libs.mongodb.driver.kotlin.coroutine)

    // Utilidades
    implementation(libs.logback)
    implementation(libs.kotlinx.coroutines.core)

    // Testing
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.junit)
    testImplementation(libs.ktor.server.tests)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.kotlinx.json)

    // BCrypt para hashing de contraseñas
    implementation("org.mindrot:jbcrypt:0.4")

    // Java Time para fechas (si no lo tienes)
    // implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

}

sourceSets {
    main {
        resources.srcDir("src/main/resources")
    }
}
