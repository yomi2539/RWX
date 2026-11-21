package io.github.rwx.mod.registry

import com.corrodinggames.rts.gameFramework.GameEngine
import io.github.rwx.mod.impl.ApiImpl
import kotlin.math.sqrt

object AudioRegistry : OwnedRegistry {
    private class RegisteredSound(
        val backendId: String,
        val path: String,
        val minimumVolume: Float,
        var loaded: Boolean = false,
    )

    /** Keyed per owner, so two mods may each register the same sound id. */
    private val sounds = RegistrationTable<String, RegisteredSound>(
        label = "Sound",
        describeKey = { it.substringAfter(KEY_SEPARATOR) },
    )

    fun registerSound(owner: ApiImpl, id: String, file: String, properties: Map<String, Any?>) {
        val backendId = "${owner.manifest.id}:$id"
        val minimumVolume = (properties["minimumVolume"] as? Number)?.toFloat() ?: 0f
        require(minimumVolume in 0f..1f) { "Sound minimumVolume must be between 0 and 1: $id" }
        sounds.register(owner, key(owner, id), RegisteredSound(backendId, file, minimumVolume))
    }

    fun materialize(owner: ApiImpl) {
        sounds.ownedBy(owner).forEach { sound -> materialize(owner, sound) }
    }

    fun playSound(owner: ApiImpl, id: String, x: Float?, y: Float?, volume: Float) {
        val sound = sounds.required(key(owner, id))
        materialize(owner, sound)
        val engine = GameEngine.getInstance()
        var resolvedVolume = volume.coerceAtLeast(0f)
        var pan = 0f
        if (engine != null && x != null && y != null) {
            val centerX = engine.viewpointXSnapped + engine.halfVisibleWorldWidth
            val centerY = engine.viewpointYSnapped + engine.halfVisibleWorldHeight
            val dx = x - centerX
            val dy = y - centerY
            val hearingRadius = engine.halfVisibleWorldWidth.coerceAtLeast(1f)
            val distance = sqrt(dx * dx + dy * dy)
            val distanceVolume = (1f - ((distance - hearingRadius) / hearingRadius)).coerceIn(0f, 1f)
            resolvedVolume *= distanceVolume.coerceAtLeast(sound.minimumVolume)
            pan = (dx / hearingRadius).coerceIn(-1f, 1f)
        }
        if (engine?.settingsEngine != null) {
            resolvedVolume *= engine.settingsEngine.masterVolume * engine.settingsEngine.gameVolume
            if (!engine.settingsEngine.enableSounds) resolvedVolume = 0f
        }
        if (resolvedVolume >= 0.01f) {
            owner.platformBridge?.audio?.playSound(sound.backendId, resolvedVolume.coerceAtMost(1f), pan)
        }
    }

    override fun unregister(owner: ApiImpl) {
        sounds.removeOwned(owner).values.forEach { sound ->
            if (sound.loaded) owner.platformBridge?.audio?.unregisterSound(sound.backendId)
        }
    }

    private fun materialize(owner: ApiImpl, sound: RegisteredSound) {
        if (sound.loaded) return
        val audio = owner.platformBridge?.audio ?: return
        val data = owner.openPackagedResource(sound.path).use { it.readBytes() }
        audio.registerSound(sound.backendId, data, sound.path)
        sound.loaded = true
    }

    private fun key(owner: ApiImpl, id: String): String = "${owner.manifest.id}$KEY_SEPARATOR$id"

    /** Separates mod id from sound id; not legal in either, so keys cannot collide. */
    private const val KEY_SEPARATOR = "\u0000"
}
