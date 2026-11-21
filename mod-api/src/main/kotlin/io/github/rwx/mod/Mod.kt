package io.github.rwx.mod

interface Mod {
    val manifest: ModManifest
    val type: ModType

    fun init()

    fun dispose() {}
}

enum class ModType {
    JVM,
    LEGACY
}

//JVM mod和Legacy mod 共同的语义
interface ModManifest {
    val id: String
    val name: String
    val minGameVersionName: String
    var description: String
}

data class JvmModManifest(
    override val id: String,
    override val name: String,
    override val minGameVersionName: String = "",
    override var description: String,
    val author: String = "",
    val version: String = "",
    val dependencies: List<String> = emptyList(),
    val priority: Int = 0,
    val thumbnail: String = ""
) : ModManifest

open class LegacyModManifest : ModManifest {
    override var id: String = ""
    override var name: String = ""
    override var minGameVersionName: String = "1.15"
    override var description: String = ""
    open val errorsAndWarnings: String? = null
    open var path: String = ""
}
