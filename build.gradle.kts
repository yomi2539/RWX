import io.github.rwx.build.AppVersionSupport
import io.github.rwx.build.I18nGenerationExtension
import io.github.rwx.build.I18nGenerationSupport

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

group = property("group") as String
version = property("version") as String

allprojects {
    group = rootProject.group
    version = rootProject.version
}

val generateI18nSources = I18nGenerationSupport.register(rootProject)
val generateAppVersionSource = AppVersionSupport.register(rootProject)

configure<I18nGenerationExtension> {
    inputFormat = "properties"
}

project(":core").tasks.matching { it.name.startsWith("compile") }.configureEach {
    dependsOn(generateI18nSources, generateAppVersionSource)
}

subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (project.path != ":android") {
            options.release = 25
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        systemProperty("user.language", "en")
        systemProperty("user.country", "US")
    }
}
