package io.github.rwx.mod.api

import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.firstOrNull
import kotlin.collections.plus
import kotlin.let
import kotlin.require
import kotlin.text.isNotBlank
import kotlin.text.isNotEmpty
import kotlin.text.lowercase
import kotlin.text.orEmpty
import kotlin.text.replace
import kotlin.text.substringBefore
import kotlin.text.trim
import kotlin.to

data class Cost(val resources: Map<String, Double>) {
    companion object {
        val ZERO = Cost(emptyMap())
        const val CREDITS_ID: String = "credits"
    }
}

data class RgbaColor(
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Int = 255,
) {
    init {
        require(red in 0..255 && green in 0..255 && blue in 0..255 && alpha in 0..255) {
            "RGBA color channels must be between 0 and 255"
        }
    }
}

data class LocalizedText(
    val key: String? = null,
    val literal: String? = null,
    val translations: Map<String, String> = emptyMap(),
) {
    init {
        require(key == null || key.isNotBlank()) { "Localized text key cannot be blank" }
        require(literal == null || literal.isNotEmpty()) { "Localized text literal cannot be empty" }
    }

    fun translated(locale: String, value: String): LocalizedText = copy(
        translations = translations + (normalizeLocale(locale) to value),
    )

    fun resolve(locale: String? = null): String {
        val normalized = locale?.let(::normalizeLocale)
        val exact = normalized?.let { translations[it] }
        val language = normalized?.substringBefore('-')?.let { languageTag ->
            translations.entries.firstOrNull { normalizeLocale(it.key).substringBefore('-') == languageTag }?.value
        }
        return exact ?: language ?: literal ?: key.orEmpty()
    }

    companion object {
        fun key(key: String) = LocalizedText(key = key)
        fun literal(value: String) = LocalizedText(literal = value)

        private fun normalizeLocale(locale: String): String = locale.trim().lowercase().replace('_', '-')
    }
}
