package io.github.rwx.mod.api

import androidx.compose.runtime.Composable

interface Graphics {
    fun registerTexture(id: TextureId, file: ResourcePath, options: TextureOptions = TextureOptions())
    fun registerProjectileRenderer(id: RendererId, renderer: ProjectileRenderer)
    fun registerPreFireRenderer(id: RendererId, renderer: PreFireRenderer)
    fun registerPostFireRenderer(id: RendererId, renderer: PostFireRenderer)
    fun registerEffectRenderer(id: RendererId, renderer: EffectRenderer)
    fun registerUnitRenderer(id: RendererId, renderer: UnitRenderer)
    fun registerTerrainOverlayRenderer(id: RendererId, renderer: TerrainOverlayRenderer)
    fun registerTerrainShader(definition: TerrainShaderDefinition)
    fun registerAnimation(definition: AnimationDefinition)
    fun registerShader(definition: ShaderDefinition)
    fun setUnitShader(unitId: String, shaderId: String?)
    fun addRenderPass(definition: RenderPassDefinition)
}

@JvmInline
value class RendererId(val value: String) {
    init {
        require(value.isNotBlank() && value == value.trim()) { "Renderer id must be non-blank and trimmed" }
    }
}

@JvmInline
value class RenderVariantId(val value: String) {
    init {
        require(value.isNotBlank() && value == value.trim()) { "Render variant id must be non-blank and trimmed" }
    }
}

@JvmInline
value class TextureId(val value: String) {
    init {
        require(value.isNotBlank() && value == value.trim()) { "Texture id must be non-blank and trimmed" }
    }
}

data class ProjectileRenderBinding(
    val rendererId: RendererId,
    val variantId: RenderVariantId,
)

data class PreFireRenderBinding(
    val rendererId: RendererId,
    val variantId: RenderVariantId,
)

data class PostFireRenderBinding(
    val rendererId: RendererId,
    val variantId: RenderVariantId,
)

data class EffectRenderBinding(
    val rendererId: RendererId,
    val variantId: RenderVariantId,
)

data class UnitRenderBinding(
    val rendererId: RendererId,
    val variantId: RenderVariantId,
    val replaceBody: Boolean = false,
)

fun interface ProjectileRenderer {
    fun render(context: ProjectileRenderContext, canvas: RenderCanvas)
}

fun interface PreFireRenderer {
    fun render(context: PreFireRenderContext, canvas: RenderCanvas)
}

fun interface PostFireRenderer {
    fun render(context: PostFireRenderContext, canvas: RenderCanvas)
}

fun interface EffectRenderer {
    fun render(context: EffectRenderContext, canvas: RenderCanvas)
}

fun interface UnitRenderer {
    fun render(context: UnitRenderContext, canvas: RenderCanvas)
}

data class ProjectileRenderContext(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val ageTicks: Float,
    val remainingTicks: Float,
    val drawSize: Float,
    val variantId: RenderVariantId,
)

data class PreFireRenderContext(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val ageTicks: Float,
    val remainingTicks: Float,
    val durationTicks: Float,
    val variantId: RenderVariantId,
)

/**
 * One frame of a turret's recovery (post-fire) window.
 *
 * Mirrors [PreFireRenderContext], but [endX]/[endY] is the point the shot was actually taken
 * against, held fixed for the whole window. The turret is free to re-aim during recovery and
 * its target may die outright; recoil still plays out along the barrel that fired.
 */
data class PostFireRenderContext(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val ageTicks: Float,
    val remainingTicks: Float,
    val durationTicks: Float,
    val variantId: RenderVariantId,
)

data class EffectRenderContext(
    val originX: Float,
    val originY: Float,
    val ageTicks: Float,
    val lifetimeTicks: Float,
    val rotationDegrees: Float,
    val alpha: Float,
    val scaleX: Float,
    val scaleY: Float,
    val shadowPass: Boolean,
    val variantId: RenderVariantId,
    val sourceSpeedFraction: Float? = null,
)

data class UnitRenderContext(
    val instanceId: UnitInstanceId,
    val definitionId: UnitId,
    val teamId: Int,
    val centerX: Float,
    val centerY: Float,
    val drawWidth: Float,
    val drawHeight: Float,
    val collisionRadius: Float,
    /** Logical world-space heading; texture orientation offsets belong to the renderer. */
    val rotationDegrees: Float,
    val destroyed: Boolean,
    val layer: UnitRenderLayer,
    val variantId: RenderVariantId,
)

enum class UnitRenderLayer {
    UNDER_UNIT,
    BODY,
    OVER_UNIT,
}

interface RenderCanvas {
    fun save()
    fun restore()
    fun rotate(degrees: Float, pivotX: Float, pivotY: Float)
    fun scale(scaleX: Float, scaleY: Float, pivotX: Float, pivotY: Float)

    fun drawTexture(
        textureId: TextureId,
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float,
        tint: RgbaColor = RgbaColor(255, 255, 255),
        opacity: Float = 1f,
        blendMode: RenderBlendMode = RenderBlendMode.ALPHA,
    )

    fun drawTextureRegion(
        textureId: TextureId,
        source: TextureRegion,
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float,
        tint: RgbaColor = RgbaColor(255, 255, 255),
        opacity: Float = 1f,
        blendMode: RenderBlendMode = RenderBlendMode.ALPHA,
    ) = drawTexture(textureId, centerX, centerY, width, height, tint, opacity, blendMode)

    fun drawCircle(
        centerX: Float,
        centerY: Float,
        radius: Float,
        color: RgbaColor = RgbaColor(255, 255, 255),
        opacity: Float = 1f,
        style: RenderShapeStyle = RenderShapeStyle.FILL,
        strokeWidth: Float = 1f,
        blendMode: RenderBlendMode = RenderBlendMode.ALPHA,
    )

    fun drawRect(
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float,
        color: RgbaColor = RgbaColor(255, 255, 255),
        opacity: Float = 1f,
        style: RenderShapeStyle = RenderShapeStyle.FILL,
        strokeWidth: Float = 1f,
        blendMode: RenderBlendMode = RenderBlendMode.ALPHA,
    )
}

enum class RenderShapeStyle {
    FILL,
    STROKE,
}

data class TextureRegion(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    init {
        require(left in 0f..1f && top in 0f..1f && right in 0f..1f && bottom in 0f..1f) {
            "Texture region coordinates must be normalized"
        }
        require(left < right && top < bottom) { "Texture region must have positive area" }
    }
}

enum class RenderBlendMode {
    ALPHA,
    ADDITIVE,
}

data class TextureOptions(
    val premultiplyAlpha: Boolean = false,
    val filter: TextureFilter = TextureFilter.NEAREST,
    val wrap: TextureWrap = TextureWrap.CLAMP
)

enum class TextureFilter {
    NEAREST,
    LINEAR
}

enum class TextureWrap {
    CLAMP,
    REPEAT
}

data class AnimationDefinition(
    val id: String,
    val frames: List<ResourcePath>,
    val frameDurationTicks: Int,
    val loop: Boolean = true
)

data class ShaderDefinition(
    val id: String,
    val vertex: ResourcePath? = null,
    val fragment: ResourcePath,
    val uniforms: List<ShaderUniformDefinition> = emptyList(),
    val targets: Set<ShaderTarget> = setOf(ShaderTarget.UNIT)
)

data class ShaderUniformDefinition(
    val name: String,
    val type: ShaderUniformType,
    val defaultValue: Any? = null
)

enum class ShaderUniformType {
    FLOAT,
    VEC2,
    VEC3,
    VEC4,
    INT,
    BOOL,
    MAT4,
    TEXTURE
}

enum class ShaderTarget {
    UNIT,
    PROJECTILE,
    EFFECT,
    TERRAIN,
    UI,
    POST_PROCESS
}

data class RenderPassDefinition(
    val id: String,
    val shaderId: String,
    val target: ShaderTarget,
    val order: Int = 0,
    val enabled: Boolean = true
)

interface Ui {
    fun addInGameMenuItem(menuId: Int? = null, text: LocalizedText, onClick: (id: Int) -> Unit)
    fun removeInGameMenuItem(menuId: Int)
    fun selectedUnits(): List<UnitRuntimeState>

    /**
     * Adds a Compose layer drawn over the running game. Layers are composed in ascending [order]
     * (then by id) inside a full-screen box; pointer and key input a layer does not consume is
     * forwarded to the game. [content] is composed on the UI thread, so read game state through
     * snapshots or Compose state rather than touching the engine directly.
     */
    fun registerHud(id: HudId, order: Int = 0, content: @Composable () -> Unit)
    fun unregisterHud(id: HudId)
    fun setNativeHudVisible(visible: Boolean)

    /**
     * Registers a full-screen window shown by [openWindow]. The host draws the [title] and a back
     * button; [content] fills the remaining responsive column. [ModWindowContext.refresh] rebuilds
     * the content from scratch, [ModWindowContext.close] returns to the game.
     */
    fun registerWindow(
        id: ModWindowId,
        title: LocalizedText,
        content: @Composable (context: ModWindowContext) -> Unit,
    )

    fun openWindow(id: ModWindowId)
    fun closeWindow()
    fun refreshWindow()
    fun showMessage(message: LocalizedText, durationTicks: Int = 180)
    fun requestWorldPosition(
        onSelected: (WorldPosition) -> Unit,
        onCancelled: () -> Unit = {},
    ): WorldPositionSelection
}

@JvmInline
value class HudId(val value: String) {
    init {
        require(value.matches(Regex("[a-z][a-z0-9_.-]*:[a-z0-9][a-z0-9_./-]*"))) {
            "Invalid namespace-qualified HUD id: $value"
        }
    }
}

interface WorldPositionSelection {
    val active: Boolean

    fun cancel()
}

@JvmInline
value class ModWindowId(val value: String) {
    init {
        require(value.matches(Regex("[a-z][a-z0-9_.-]*:[a-z0-9][a-z0-9_./-]*"))) {
            "Invalid namespace-qualified mod window id: $value"
        }
    }
}

interface ModWindowContext {
    val api: Api
    val locale: String
    fun refresh()
    fun close()
}

data class UiCommandButtonDefinition(
    val id: String,
    val text: LocalizedText,
    val icon: ResourcePath? = null
)

enum class UiAnchor {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    CENTER
}
