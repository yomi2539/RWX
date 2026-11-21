package io.github.rwx.mod.api

import androidx.compose.runtime.Composable

interface Graphics {
    fun registerTexture(id: String, file: String, options: TextureOptions = TextureOptions())
    fun registerProjectileRenderer(id: String, renderer: ProjectileRenderer)
    fun registerPreFireRenderer(id: String, renderer: PreFireRenderer)
    fun registerPostFireRenderer(id: String, renderer: PostFireRenderer)
    fun registerEffectRenderer(id: String, renderer: EffectRenderer)
    fun registerUnitRenderer(id: String, renderer: UnitRenderer)
    fun registerTerrainOverlayRenderer(id: String, renderer: TerrainOverlayRenderer)
    fun registerTerrainShader(definition: TerrainShaderDefinition)
    fun registerAnimation(definition: AnimationDefinition)
    fun registerShader(definition: ShaderDefinition)
    fun setUnitShader(unitId: String, shaderId: String?)
    fun addRenderPass(definition: RenderPassDefinition)
}

data class ProjectileRenderBinding(
    val rendererId: String,
    val variantId: String,
)

data class PreFireRenderBinding(
    val rendererId: String,
    val variantId: String,
)

data class PostFireRenderBinding(
    val rendererId: String,
    val variantId: String,
)

data class EffectRenderBinding(
    val rendererId: String,
    val variantId: String,
)

data class UnitRenderBinding(
    val rendererId: String,
    val variantId: String,
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
    val variantId: String,
)

data class PreFireRenderContext(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val ageTicks: Float,
    val remainingTicks: Float,
    val durationTicks: Float,
    val variantId: String,
)

data class PostFireRenderContext(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val ageTicks: Float,
    val remainingTicks: Float,
    val durationTicks: Float,
    val variantId: String,
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
    val variantId: String,
    val sourceSpeedFraction: Float? = null,
)

data class UnitRenderContext(
    val instanceId: Long,
    val definitionId: String,
    val teamId: Int,
    val centerX: Float,
    val centerY: Float,
    val drawWidth: Float,
    val drawHeight: Float,
    val collisionRadius: Float,
    val rotationDegrees: Float,
    val destroyed: Boolean,
    val layer: UnitRenderLayer,
    val variantId: String,
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
        textureId: String,
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float,
        tint: RgbaColor = RgbaColor(255, 255, 255),
        opacity: Float = 1f,
        blendMode: RenderBlendMode = RenderBlendMode.ALPHA,
    )

    fun drawTextureRegion(
        textureId: String,
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
    val frames: List<String>,
    val frameDurationTicks: Int,
    val loop: Boolean = true
)

data class ShaderDefinition(
    val id: String,
    val vertex: String? = null,
    val fragment: String,
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
    fun registerHud(id: String, order: Int = 0, content: @Composable () -> Unit)
    fun unregisterHud(id: String)
    fun setNativeHudVisible(visible: Boolean)

    /**
     * Registers a full-screen window shown by [openWindow]. The host draws the [title] and a back
     * button; [content] fills the remaining responsive column. [ModWindowContext.refresh] rebuilds
     * the content from scratch, [ModWindowContext.close] returns to the game.
     */
    fun registerWindow(
        id: String,
        title: LocalizedText,
        content: @Composable (context: ModWindowContext) -> Unit,
    )

    fun openWindow(id: String)
    fun closeWindow()
    fun refreshWindow()
    fun showMessage(message: LocalizedText, durationTicks: Int = 180)
    fun requestWorldPosition(
        onSelected: (WorldPosition) -> Unit,
        onCancelled: () -> Unit = {},
    ): WorldPositionSelection
}

interface WorldPositionSelection {
    val active: Boolean

    fun cancel()
}

interface ModWindowContext {
    val api: Api
    val locale: String
    fun refresh()
    fun close()
}
