plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
    kotlin("plugin.serialization") version "2.0.20"
}

group = "com.gershaveut.chat_ofg"
version = "1.0.0"
application {
    mainClass.set("com.gershaveut.chat_ofg.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.ktor.server.auth)
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
}