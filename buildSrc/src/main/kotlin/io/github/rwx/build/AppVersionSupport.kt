package io.github.rwx.build

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

object AppVersionSupport {
    const val DEFAULT_OUTPUT_FILE = "core/src/main/kotlin/io/github/rwx/AppVersion.kt"

    fun versionCode(version: String): Int {
        val parts = version.split('.', '-', '+').map { it.toIntOrNull() ?: 0 }
        return parts.getOrElse(0) { 0 } * 10_000 +
                parts.getOrElse(1) { 0 } * 100 +
                parts.getOrElse(2) { 0 }
    }

    fun register(project: Project): TaskProvider<Task> {
        val outputFile = project.layout.projectDirectory.file(DEFAULT_OUTPUT_FILE)
        return project.tasks.register("generateAppVersionSource") {
            description = "Generate the AppVersion Kotlin source from the Gradle project version."
            val versionProvider = project.providers.provider { project.version.toString() }
            inputs.property("version", versionProvider)
            outputs.file(outputFile)

            doLast {
                val version = versionProvider.get()
                val target = outputFile.asFile
                val content = buildString {
                    appendLine("// Auto-generated from gradle.properties - Do not modify")
                    appendLine("package io.github.rwx")
                    appendLine()
                    appendLine("object AppVersion {")
                    appendLine("    const val NAME: String = \"$version\"")
                    appendLine("    const val CODE: Int = ${versionCode(version)}")
                    appendLine("}")
                }
                if (!target.exists() || target.readText() != content) {
                    target.parentFile.mkdirs()
                    target.writeText(content)
                }
            }
        }
    }
}
