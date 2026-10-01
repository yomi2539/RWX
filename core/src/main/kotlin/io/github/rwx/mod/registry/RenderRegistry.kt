package io.github.rwx.mod.registry

import com.corrodinggames.rts.game.Projectile
import com.corrodinggames.rts.game.ProjectileTemplate
import com.corrodinggames.rts.game.units.OrderableUnit
import com.corrodinggames.rts.game.units.custom.CustomProjectileTemplate
import com.corrodinggames.rts.game.units.custom.CustomUnit
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig
import com.corrodinggames.rts.gameFramework.GameEngine
import com.corrodinggames.rts.gameFramework.effects.Effect
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
import com.corrodinggames.rts.gameFramework.graphics.Texture
import io.github.rwx.geometry.Rect
import io.github.rwx.geometry.RectF
import io.github.rwx.mod.api.*
import io.github.rwx.mod.impl.ApiImpl
import io.github.rwx.render.frame.GameCanvasBlendMode
import io.github.rwx.render.canvas.Paint
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

object RenderRegistry : OwnedRegistry {
    private class RegisteredTexture(
        val path: ResourcePath,
        val options: TextureOptions,
        var texture: Texture? = null,
    )

    private data class BoundUnitRenderer(
        val definitionId: UnitId,
        val binding: UnitRenderBinding,
    )

    /**
     * One monitor for every table: [activateUnitBindings] resolves a renderer and records
     * the binding it implies as a single step, so these cannot be locked independently.
     */
    private val lock = Any()
    private val textures = RegistrationTable<String, RegisteredTexture>("Texture", lock)
    private val projectileRenderers =
        RegistrationTable<String, ProjectileRenderer>("Projectile renderer", lock)
    private val preFireRenderers = RegistrationTable<String, PreFireRenderer>("Pre-fire renderer", lock)
    private val postFireRenderers =
        RegistrationTable<String, PostFireRenderer>("Post-fire renderer", lock)
    private val effectRenderers = RegistrationTable<String, EffectRenderer>("Effect renderer", lock)
    private val unitRenderers = RegistrationTable<String, UnitRenderer>("Unit renderer", lock)
    private val unitBindings = RegistrationTable<CustomUnitConfig, BoundUnitRenderer>(
        label = "Unit render binding",
        lock = lock,
        entries = IdentityHashMap(),
    )
    private val failures = ModFailureLog("Mod renderer")

    fun registerTexture(owner: ApiImpl, id: TextureId, path: ResourcePath, options: TextureOptions) {
        textures.register(owner, id.value, RegisteredTexture(path, options))
    }

    fun registerProjectileRenderer(owner: ApiImpl, id: RendererId, renderer: ProjectileRenderer) {
        projectileRenderers.register(owner, id.value, renderer)
    }

    fun registerPreFireRenderer(owner: ApiImpl, id: RendererId, renderer: PreFireRenderer) {
        preFireRenderers.register(owner, id.value, renderer)
    }

    fun registerPostFireRenderer(owner: ApiImpl, id: RendererId, renderer: PostFireRenderer) {
        postFireRenderers.register(owner, id.value, renderer)
    }

    fun registerEffectRenderer(owner: ApiImpl, id: RendererId, renderer: EffectRenderer) {
        effectRenderers.register(owner, id.value, renderer)
    }

    fun registerUnitRenderer(owner: ApiImpl, id: RendererId, renderer: UnitRenderer) {
        unitRenderers.register(owner, id.value, renderer)
    }

    fun activateUnitBindings(
        owner: ApiImpl,
        nativeUnits: Map<UnitId, CustomUnitConfig>,
        definitions: List<UnitDefinition>,
    ): Int = synchronized(lock) {
        unitBindings.removeOwned(owner)
        var count = 0
        definitions.forEach { definition ->
            val binding = definition.extension.renderBinding ?: return@forEach
            val renderer = checkNotNull(unitRenderers.owned(binding.rendererId.value)) {
                "Unit renderer is not registered: ${binding.rendererId.value}"
            }
            check(renderer.owner === owner) {
                "Unit renderer ${binding.rendererId.value} belongs to another mod"
            }
            val nativeUnit = checkNotNull(nativeUnits[definition.id]) {
                "Native unit is not active: ${definition.id.value}"
            }
            unitBindings.put(owner, nativeUnit, BoundUnitRenderer(definition.id, binding))
            count++
        }
        count
    }

    override fun unregister(owner: ApiImpl) {
        synchronized(lock) {
            textures.removeOwned(owner)
            projectileRenderers.removeOwned(owner)
            preFireRenderers.removeOwned(owner)
            postFireRenderers.removeOwned(owner)
            effectRenderers.removeOwned(owner)
            unitRenderers.removeOwned(owner)
            unitBindings.removeOwned(owner)
        }
    }

    @JvmStatic
    fun drawUnit(unit: CustomUnit, gameEngine: GameEngine, layer: UnitRenderLayer): Boolean {
        val (bound, renderer) = synchronized(lock) {
            val bound = unitBindings[unit.unitConfig] ?: return false
            if (layer == UnitRenderLayer.BODY && !bound.binding.replaceBody) return false
            bound to (unitRenderers[bound.binding.rendererId.value] ?: return false)
        }
        val unitOffset = unit.getRenderOffset()
        val centerX = unit.posX + unitOffset.x - gameEngine.viewpointXSnapped
        val centerY = unit.posY + unitOffset.y - unit.posZ - gameEngine.viewpointYSnapped
        val bounds = unit.getUnitBounds()
        val imageScale = unit.renderScale
        val width = (abs(bounds.c - bounds.a).takeIf { it > 0f } ?: (unit.radius * 2f)) * imageScale
        val height = (abs(bounds.d - bounds.b).takeIf { it > 0f } ?: (unit.radius * 2f)) * imageScale
        val canvas = EngineRenderCanvas(gameEngine.renderGraphicsEngine, unit.renderPaint.f() / 255f)
        var rendered = false
        canvas.save()
        try {
            failures.runSafelyOncePerId("unit:${bound.binding.rendererId.value}") {
                renderer.render(
                    UnitRenderContext(
                        instanceId = UnitInstanceId(unit.objectId),
                        definitionId = bound.definitionId,
                        teamId = unit.team?.teamId ?: -1,
                        centerX = centerX,
                        centerY = centerY,
                        drawWidth = width,
                        drawHeight = height,
                        collisionRadius = unit.radius,
                        rotationDegrees = unit.rotationSpeed,
                        destroyed = unit.isDestroyed,
                        layer = layer,
                        variantId = bound.binding.variantId,
                    ),
                    canvas,
                )
                rendered = true
            }
        } finally {
            canvas.finish()
        }
        return rendered
    }

    @JvmStatic
    fun drawPreFire(unit: CustomUnit, gameEngine: GameEngine) {
        var canvas: EngineRenderCanvas? = null
        try {
            unit.unitConfig.turrets.forEachIndexed { turretIndex, turret ->
                val rendererId = turret?.preFireRendererId ?: return@forEachIndexed
                val variantId = turret.preFireRendererVariant ?: return@forEachIndexed
                val renderer = preFireRenderers[rendererId] ?: return@forEachIndexed
                val movement = unit.movementLevels[turretIndex]
                val target = movement.targetUnit ?: return@forEachIndexed
                val phaseStart = (turret.warmup - turret.preFireDuration).coerceAtLeast(0f)
                val age = movement.speed - phaseStart
                if (unit.isDestroyed || turret.preFireDuration <= 0f || age <= 0f) return@forEachIndexed

                val muzzle = unit.D(turretIndex)
                val activeCanvas = canvas ?: EngineRenderCanvas(gameEngine.renderGraphicsEngine).also { canvas = it }
                failures.runSafelyOncePerId("pre-fire:$rendererId") {
                    renderer.render(
                        PreFireRenderContext(
                            startX = muzzle.x - gameEngine.viewpointXSnapped,
                            startY = muzzle.y - unit.posZ - muzzle.z - gameEngine.viewpointYSnapped,
                            endX = target.posX - gameEngine.viewpointXSnapped,
                            endY = target.posY - target.posZ - gameEngine.viewpointYSnapped,
                            ageTicks = age.coerceIn(0f, turret.preFireDuration),
                            remainingTicks = (turret.preFireDuration - age).coerceAtLeast(0f),
                            durationTicks = turret.preFireDuration,
                            variantId = RenderVariantId(variantId),
                        ),
                        activeCanvas,
                    )
                }
            }
        } finally {
            canvas?.finish()
        }
    }

    /**
     * Draws each turret's recovery (post-fire) window.
     *
     * The window and its age come from the same reload counter
     * [TurretFireCycleObserverRegistry] reads, and the aim point is taken from that registry
     * rather than recomputed, so a mod's recoil visual lands exactly where its observer was
     * told the shot went. Recomputing it here would drift the moment the turret re-aims.
     */
    @JvmStatic
    fun drawPostFire(unit: CustomUnit, gameEngine: GameEngine) {
        var canvas: EngineRenderCanvas? = null
        try {
            unit.unitConfig.turrets.forEachIndexed { turretIndex, turret ->
                val rendererId = turret?.postFireRendererId ?: return@forEachIndexed
                val variantId = turret.postFireRendererVariant ?: return@forEachIndexed
                val renderer = postFireRenderers[rendererId] ?: return@forEachIndexed
                val movement = unit.movementLevels[turretIndex]
                val age = unit.b(turretIndex) - movement.rotation
                if (unit.isDestroyed ||
                    turret.postFireDuration <= 0f ||
                    movement.rotation <= 0f ||
                    age < 0f ||
                    age >= turret.postFireDuration
                ) {
                    return@forEachIndexed
                }

                val muzzle = unit.D(turretIndex)
                val aim = TurretFireCycleObserverRegistry.aimPointOf(unit.objectId, turretIndex)
                val activeCanvas = canvas ?: EngineRenderCanvas(gameEngine.renderGraphicsEngine).also { canvas = it }
                failures.runSafelyOncePerId("post-fire:$rendererId") {
                    renderer.render(
                        PostFireRenderContext(
                            startX = muzzle.x - gameEngine.viewpointXSnapped,
                            startY = muzzle.y - unit.posZ - muzzle.z - gameEngine.viewpointYSnapped,
                            // No recorded aim point means the turret fired without ever
                            // holding a target; collapse the vector onto the muzzle rather
                            // than inventing a direction.
                            endX = (aim?.x ?: muzzle.x) - gameEngine.viewpointXSnapped,
                            endY = (aim?.let { it.y - it.z } ?: muzzle.y) - gameEngine.viewpointYSnapped,
                            ageTicks = age.coerceIn(0f, turret.postFireDuration),
                            remainingTicks = (turret.postFireDuration - age).coerceAtLeast(0f),
                            durationTicks = turret.postFireDuration,
                            variantId = RenderVariantId(variantId),
                        ),
                        activeCanvas,
                    )
                }
            }
        } finally {
            canvas?.finish()
        }
    }

    @JvmStatic
    fun drawProjectile(
        projectile: Projectile,
        projectileTemplate: ProjectileTemplate,
        gameEngine: GameEngine,
        targetX: Float,
        targetY: Float,
        targetHeight: Float,
    ): Boolean {
        val custom = projectileTemplate as? CustomProjectileTemplate ?: return false
        val rendererId = custom.renderExtensionId ?: return false
        val variantId = custom.renderExtensionVariant ?: return false
        val renderer = projectileRenderers[rendererId] ?: return false
        val startX = projectile.posX - gameEngine.viewpointXSnapped
        val startY = projectile.posY - gameEngine.viewpointYSnapped - projectile.posZ
        val endX = targetX - gameEngine.viewpointXSnapped + projectile.trackOffsetX
        val endY = targetY - targetHeight - gameEngine.viewpointYSnapped + projectile.trackOffsetY
        val canvas = EngineRenderCanvas(gameEngine.renderGraphicsEngine)
        try {
            failures.runSafelyOncePerId("projectile:$rendererId") {
                renderer.render(
                    ProjectileRenderContext(
                        startX = startX,
                        startY = startY,
                        endX = endX,
                        endY = endY,
                        ageTicks = projectile.age.coerceAtLeast(0f),
                        remainingTicks = projectile.lifeTimer.coerceAtLeast(0f),
                        drawSize = projectileTemplate.drawSize,
                        variantId = RenderVariantId(variantId),
                    ),
                    canvas,
                )
            }
        } finally {
            canvas.finish()
        }
        return true
    }

    @JvmStatic
    fun drawEffect(effect: Effect, gameEngine: GameEngine, shadowPass: Boolean): Boolean {
        val rendererId = effect.template.renderExtensionId ?: return false
        val variantId = effect.template.renderExtensionVariant ?: return false
        val renderer = effectRenderers[rendererId] ?: return false

        var worldX = effect.posX
        var worldY = effect.posY
        var height = effect.posZ
        effect.parentObject?.let { source ->
            worldX += source.posX
            worldY += source.posY
            height += source.posZ
        }
        if (!shadowPass && !effect.showInFog && !effect.visibilityChecked) {
            if (!gameEngine.tileMap.isWorldPointVisibleForTeam(worldX, worldY, gameEngine.playerTeam)) return true
            effect.visibilityChecked = true
        }
        val age = (effect.lifeMax - effect.lifeTimer).coerceIn(0f, effect.lifeMax.coerceAtLeast(0f))
        val progress = if (effect.lifeMax <= 0f) 1f else age / effect.lifeMax
        val scaleX = if (effect.scaleXFrom.isNaN()) {
            effect.scaleFrom + (effect.scaleTo - effect.scaleFrom) * progress
        } else {
            effect.scaleXFrom + (effect.scaleXTo - effect.scaleXFrom) * progress
        }
        val scaleY = if (effect.scaleYFrom.isNaN()) {
            effect.scaleFrom + (effect.scaleTo - effect.scaleFrom) * progress
        } else {
            effect.scaleYFrom + (effect.scaleYTo - effect.scaleYFrom) * progress
        }
        val canvas = EngineRenderCanvas(gameEngine.renderGraphicsEngine)
        val sourceSpeedFraction = (effect.parentObject as? OrderableUnit)?.let { source ->
            planarSpeedFraction(source.velocityX, source.velocityY, source.getMoveSpeed())
        }
        try {
            failures.runSafelyOncePerId("effect:$rendererId") {
                renderer.render(
                    EffectRenderContext(
                        originX = worldX - gameEngine.viewpointXSnapped,
                        originY = worldY - (if (shadowPass) 0f else height) - gameEngine.viewpointYSnapped,
                        ageTicks = age,
                        lifetimeTicks = effect.lifeMax,
                        rotationDegrees = effect.rotation,
                        alpha = effect.alpha,
                        scaleX = scaleX,
                        scaleY = scaleY,
                        shadowPass = shadowPass,
                        variantId = RenderVariantId(variantId),
                        sourceSpeedFraction = sourceSpeedFraction,
                    ),
                    canvas,
                )
            }
        } finally {
            canvas.finish()
        }
        return true
    }

    internal fun resolveTexture(id: TextureId): Pair<Texture, TextureOptions> {
        val registration = textures.owned(id.value)
            ?: error("Texture is not registered: ${id.value}")
        val entry = registration.value
        val texture = entry.texture ?: synchronized(entry) {
            entry.texture ?: registration.owner.loadRenderTexture(entry.path, entry.options)
                .also { entry.texture = it }
        }
        return texture to entry.options
    }

    internal class EngineRenderCanvas(private val graphics: GraphicsEngine, private val globalOpacity: Float = 1f) :
        RenderCanvas {
        private val source = Rect()
        private val destination = RectF()
        private val paint = Paint()
        private var saveDepth = 0

        override fun save() {
            graphics.k()
            saveDepth++
        }

        override fun restore() {
            check(saveDepth > 0) { "RenderCanvas.restore() called without save()" }
            graphics.l()
            saveDepth--
        }

        override fun rotate(degrees: Float, pivotX: Float, pivotY: Float) {
            graphics.a(degrees, pivotX, pivotY)
        }

        override fun scale(scaleX: Float, scaleY: Float, pivotX: Float, pivotY: Float) {
            require(scaleX.isFinite() && scaleY.isFinite()) { "Canvas scale must be finite" }
            graphics.a(scaleX, scaleY, pivotX, pivotY)
        }

        override fun drawTexture(
            textureId: TextureId,
            centerX: Float,
            centerY: Float,
            width: Float,
            height: Float,
            tint: RgbaColor,
            opacity: Float,
            blendMode: RenderBlendMode,
        ) {
            require(width > 0f && height > 0f) { "Texture dimensions must be positive: $textureId" }
            val (texture, options) = resolveTexture(textureId)
            paint.a(options.filter == TextureFilter.LINEAR)
            paint.a(
                (tint.alpha * opacity.coerceIn(0f, 1f) * globalOpacity).roundToInt(),
                tint.red,
                tint.green,
                tint.blue,
            )
            paint.a(
                when (blendMode) {
                    RenderBlendMode.ALPHA -> GameCanvasBlendMode.SourceOver
                    RenderBlendMode.ADDITIVE -> GameCanvasBlendMode.Add
                }
            )
            source.a(0, 0, texture.width(), texture.height())
            destination.a(
                centerX - width * 0.5f,
                centerY - height * 0.5f,
                centerX + width * 0.5f,
                centerY + height * 0.5f,
            )
            graphics.a(texture, source, destination, paint)
        }

        override fun drawTextureRegion(
            textureId: TextureId,
            source: TextureRegion,
            centerX: Float,
            centerY: Float,
            width: Float,
            height: Float,
            tint: RgbaColor,
            opacity: Float,
            blendMode: RenderBlendMode,
        ) {
            require(width > 0f && height > 0f) { "Texture dimensions must be positive: $textureId" }
            val (texture, options) = resolveTexture(textureId)
            paint.a(options.filter == TextureFilter.LINEAR)
            paint.a(
                (tint.alpha * opacity.coerceIn(0f, 1f) * globalOpacity).roundToInt(),
                tint.red,
                tint.green,
                tint.blue,
            )
            paint.a(
                when (blendMode) {
                    RenderBlendMode.ALPHA -> GameCanvasBlendMode.SourceOver
                    RenderBlendMode.ADDITIVE -> GameCanvasBlendMode.Add
                }
            )
            val left = (source.left * texture.width()).roundToInt().coerceIn(0, texture.width() - 1)
            val top = (source.top * texture.height()).roundToInt().coerceIn(0, texture.height() - 1)
            val right = (source.right * texture.width()).roundToInt().coerceIn(left + 1, texture.width())
            val bottom = (source.bottom * texture.height()).roundToInt().coerceIn(top + 1, texture.height())
            this.source.a(left, top, right, bottom)
            destination.a(
                centerX - width * 0.5f,
                centerY - height * 0.5f,
                centerX + width * 0.5f,
                centerY + height * 0.5f,
            )
            graphics.a(texture, this.source, destination, paint)
        }

        override fun drawCircle(
            centerX: Float,
            centerY: Float,
            radius: Float,
            color: RgbaColor,
            opacity: Float,
            style: RenderShapeStyle,
            strokeWidth: Float,
            blendMode: RenderBlendMode,
        ) {
            require(radius.isFinite() && radius > 0f) { "Circle radius must be positive and finite: $radius" }
            applyShapePaint(color, opacity, style, strokeWidth, blendMode)
            graphics.a(centerX, centerY, radius, paint)
        }

        override fun drawRect(
            centerX: Float,
            centerY: Float,
            width: Float,
            height: Float,
            color: RgbaColor,
            opacity: Float,
            style: RenderShapeStyle,
            strokeWidth: Float,
            blendMode: RenderBlendMode,
        ) {
            require(width.isFinite() && width > 0f) { "Rect width must be positive and finite: $width" }
            require(height.isFinite() && height > 0f) { "Rect height must be positive and finite: $height" }
            applyShapePaint(color, opacity, style, strokeWidth, blendMode)
            destination.a(
                centerX - width * 0.5f,
                centerY - height * 0.5f,
                centerX + width * 0.5f,
                centerY + height * 0.5f,
            )
            graphics.a(destination, paint)
        }

        private fun applyShapePaint(
            color: RgbaColor,
            opacity: Float,
            style: RenderShapeStyle,
            strokeWidth: Float,
            blendMode: RenderBlendMode,
        ) {
            require(strokeWidth.isFinite() && strokeWidth > 0f) { "Stroke width must be positive and finite: $strokeWidth" }
            paint.a(
                (color.alpha * opacity.coerceIn(0f, 1f) * globalOpacity).roundToInt(),
                color.red,
                color.green,
                color.blue,
            )
            paint.a(
                when (style) {
                    RenderShapeStyle.FILL -> Paint.Style.FILL
                    RenderShapeStyle.STROKE -> Paint.Style.STROKE
                }
            )
            paint.a(strokeWidth)
            paint.a(
                when (blendMode) {
                    RenderBlendMode.ALPHA -> GameCanvasBlendMode.SourceOver
                    RenderBlendMode.ADDITIVE -> GameCanvasBlendMode.Add
                }
            )
        }

        fun finish() {
            while (saveDepth > 0) {
                graphics.l()
                saveDepth--
            }
        }
    }
}

internal fun planarSpeedFraction(velocityX: Float, velocityY: Float, maximumSpeed: Float): Float =
    if (maximumSpeed > 0f) sqrt(velocityX * velocityX + velocityY * velocityY) / maximumSpeed else 0f
