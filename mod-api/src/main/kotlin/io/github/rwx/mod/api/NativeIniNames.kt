package io.github.rwx.mod.api


internal val INI_SECTION_NAME_PATTERN = Regex("""[\p{L}\p{N}_.-]+""")

internal val INI_MUTATOR_NAME_PATTERN = Regex("""[\p{L}\p{N}.-]+""")

internal fun requireIniSectionName(name: String): String {
    require(name.matches(INI_SECTION_NAME_PATTERN)) { "Invalid native INI section name: $name" }
    return name
}

internal fun requireIniMutatorName(name: String) {
    require(name.matches(INI_MUTATOR_NAME_PATTERN)) {
        "Invalid projectile mutator name '$name': native mutator names cannot contain underscores"
    }
}
