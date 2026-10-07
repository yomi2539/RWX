import java.util.Locale

plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    api(project(":mod-api"))
    implementation(libs.compose.runtime)
    // The Compose desktop host supplies Skiko and its platform native library.
    // Keep AWT classes out of Android's transitive runtime dependencies.
    compileOnly(libs.skiko.awt)
    compileOnly(libs.httpclient)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.commons.compress)
    api(libs.kotlinx.coroutines.core)
    api(libs.tomlkt)
    api(libs.koin.core)
    api(libs.kotlinx.serialization.json)
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    testImplementation(libs.skiko.awt)
    val skiaOs = System.getProperty("os.name").lowercase(Locale.ROOT).let {
        when {
            it.contains("mac") || it.contains("darwin") -> "macos"
            it.contains("win") -> "windows"
            else -> "linux"
        }
    }
    val skiaArch = System.getProperty("os.arch").lowercase(Locale.ROOT).let {
        if (it == "aarch64" || it == "arm64") "arm64" else "x64"
    }
    testRuntimeOnly("org.jetbrains.skiko:skiko-awt-runtime-$skiaOs-$skiaArch:${libs.versions.skikoVersion.get()}")
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.httpclient)
}
