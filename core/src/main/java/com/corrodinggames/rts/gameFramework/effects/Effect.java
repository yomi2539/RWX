package com.corrodinggames.rts.gameFramework.effects;

import com.corrodinggames.rts.game.units.custom.EffectTemplate;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.GameObject;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.graphics.GamePaint;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine;
import com.corrodinggames.rts.gameFramework.graphics.ShaderProgram;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;
import io.github.rwx.geometry.PointF;
import io.github.rwx.geometry.Rect;
import io.github.rwx.geometry.RectF;
import io.github.rwx.mod.registry.RenderRegistry;
import io.github.rwx.render.canvas.*;

import java.io.IOException;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.d.e */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/d/e.class */
public final class Effect {
    /* JADX INFO: renamed from: ay */
    private final EffectManager effectManager;
    /* JADX INFO: renamed from: b */
    public GameObject parentObject;
    /* JADX INFO: renamed from: c */
    public boolean ignoreParentZ;
    /* JADX INFO: renamed from: d */
    public boolean isLight;
    /* JADX INFO: renamed from: g */
    public int effectKind;
    /* JADX INFO: renamed from: o */
    public boolean isActive;
    /* JADX INFO: renamed from: p */
    public boolean isUiEffect;
    /* JADX INFO: renamed from: r */
    public boolean fadeIn;
    /* JADX INFO: renamed from: s */
    public boolean fadeOut;
    /* JADX INFO: renamed from: t */
    public float fadeDuration;
    /* JADX INFO: renamed from: u */
    public boolean useGravity;
    /* JADX INFO: renamed from: v */
    public boolean useBounce;
    /* JADX INFO: renamed from: x */
    public int startColor;
    /* JADX INFO: renamed from: y */
    public int endColor;
    /* JADX INFO: renamed from: A */
    public short emitRecursionDepth;
    /* JADX INFO: renamed from: E */
    public float alpha;
    /* JADX INFO: renamed from: F */
    public float scaleTo;
    /* JADX INFO: renamed from: G */
    public float scaleFrom;
    public float scaleXFrom;
    public float scaleXTo;
    public float scaleYFrom;
    public float scaleYTo;
    public float imageAnchorY;
    /* JADX INFO: renamed from: H */
    public boolean scaleWithZoom;
    /* JADX INFO: renamed from: I */
    public float posX;
    /* JADX INFO: renamed from: J */
    public float posY;
    /* JADX INFO: renamed from: K */
    public float posZ;
    /* JADX INFO: renamed from: L */
    public boolean isLaser;
    /* JADX INFO: renamed from: M */
    public float laserTargetX;
    /* JADX INFO: renamed from: N */
    public float laserTargetY;
    /* JADX INFO: renamed from: O */
    public float laserTargetZ;
    /* JADX INFO: renamed from: P */
    public float velocityX;
    /* JADX INFO: renamed from: Q */
    public float velocityY;
    /* JADX INFO: renamed from: R */
    public float velocityZ;
    /* JADX INFO: renamed from: S */
    public float bobAmplitude;
    /* JADX INFO: renamed from: T */
    public float bobPeriod;
    /* JADX INFO: renamed from: U */
    public float delayedStartTimer;
    /* JADX INFO: renamed from: V */
    public float lifeTimer;
    /* JADX INFO: renamed from: W */
    public float lifeMax;
    /* JADX INFO: renamed from: Y */
    public float rotation;
    /* JADX INFO: renamed from: Z */
    public float rotationSpeed;
    /* JADX INFO: renamed from: aa */
    public String text;
    /* JADX INFO: renamed from: ab */
    public Paint textPaint;
    /* JADX INFO: renamed from: ac */
    public float textXOffset;
    /* JADX INFO: renamed from: ad */
    public float textYOffset;
    /* JADX INFO: renamed from: ae */
    public boolean animateFrames;
    /* JADX INFO: renamed from: af */
    public int animateFrameStart;
    /* JADX INFO: renamed from: ag */
    public int animateFrameEnd;
    /* JADX INFO: renamed from: ah */
    public boolean animateFramePingPong;
    /* JADX INFO: renamed from: ai */
    public boolean animateFrameLooping;
    /* JADX INFO: renamed from: aj */
    public float animateFrameSpeed;
    /* JADX INFO: renamed from: ak */
    public float currentFrame;
    /* JADX INFO: renamed from: al */
    public boolean animateFrameReversing;
    /* JADX INFO: renamed from: am */
    public boolean unusedFlag1;
    /* JADX INFO: renamed from: ap */
    public int frameIndex;
    /* JADX INFO: renamed from: aq */
    public int stripIndex;
    /* JADX INFO: renamed from: au */
    public float cachedAlpha;
    /* JADX INFO: renamed from: av */
    public int cachedColor;
    /* JADX INFO: renamed from: aw */
    public boolean hasColorFilterApplied;
    /* JADX INFO: renamed from: h */
    public static int KIND_LIGHT = 1;
    /* JADX INFO: renamed from: i */
    public static int KIND_MUZZLE_FLASH = 2;
    /* JADX INFO: renamed from: j */
    public static int KIND_FLAME = 3;
    /* JADX INFO: renamed from: k */
    public static int KIND_FIRE = 4;
    /* JADX INFO: renamed from: l */
    public static int KIND_SMOKE = 5;
    /* JADX INFO: renamed from: m */
    public static int KIND_BLOOD = 6;
    /* JADX INFO: renamed from: n */
    public static int KIND_ALTERNATE_FIRE = 7;
    /* JADX INFO: renamed from: C */
    public static MultiplyAddColorFilter cachedLightingColorFilter = null;
    /* JADX INFO: renamed from: D */
    public static int cachedLightingColorFilterColor = 0;
    /* JADX INFO: renamed from: ax */
    public static GamePaint[] alphaTextures = new GamePaint[128];
    /* JADX INFO: renamed from: a */
    public EffectTemplate template = EffectTemplate.defaultEffectTemplate;
    /* JADX INFO: renamed from: e */
    public boolean showInFog = true;
    /* JADX INFO: renamed from: f */
    public boolean visibilityChecked = false;
    /* JADX INFO: renamed from: q */
    public EffectQuality priority = EffectQuality.verylow;
    /* JADX INFO: renamed from: w */
    public float physicsGravity = 1.0f;
    /* JADX INFO: renamed from: z */
    public float endColorTransitionTime = -1.0f;
    /* JADX INFO: renamed from: B */
    public MultiplyAddColorFilter lightingColorFilter = null;
    /* JADX INFO: renamed from: X */
    public float trailTimer = 0.0f;
    /* JADX INFO: renamed from: an */
    public boolean isCentered = false;
    /* JADX INFO: renamed from: ao */
    public float verticalAnchorOffset = 0.0f;
    /* JADX INFO: renamed from: ar */
    public short drawLayer = 2;
    /* JADX INFO: renamed from: as */
    public boolean shadow = false;
    /* JADX INFO: renamed from: at */
    public GamePaint colorPaint = getFreshTexture();

    protected Effect(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    static {
        for (int i2 = 0; i2 < alphaTextures.length; i2++) {
            alphaTextures[i2] = getFreshTexture();
            alphaTextures[i2].c((int) ((i2 / (float) (alphaTextures.length - 1)) * 255.0f));
        }
    }

    /* JADX INFO: renamed from: a */
    public static GamePaint getFreshTexture() {
        return GameViewUtils.b();
    }

    /* JADX INFO: renamed from: a */
    public GamePaint getTexture(float f) {
        int length = (int) (f * (alphaTextures.length - 1));
        if (length < 0) {
            length = 0;
        }
        if (length > alphaTextures.length - 1) {
            length = alphaTextures.length - 1;
        }
        return alphaTextures[length];
    }

    /* JADX INFO: renamed from: b */
    public void reset() {
        if (this.isActive) {
            this.isActive = false;
            this.effectManager.activeEffectsCount--;
            EffectManager.useStrictCounting = true;
            if (this.template.alsoEmitEffectsOnDeath != null && this.emitRecursionDepth < 20) {
                float f = this.posX;
                float f2 = this.posY;
                float f3 = this.posZ;
                if (this.parentObject != null) {
                    f += this.parentObject.posX;
                    f2 += this.parentObject.posY;
                    f3 += this.parentObject.posZ;
                }
                this.template.alsoEmitEffectsOnDeath.a(f, f2, f3, this.rotation, this.parentObject, 0, this.emitRecursionDepth);
            }
        }
    }

    /* JADX INFO: renamed from: c */
    public void free() {
        this.template = EffectTemplate.defaultEffectTemplate;
        this.priority = EffectQuality.verylow;
        this.parentObject = null;
        this.ignoreParentZ = false;
        this.isLight = false;
        this.showInFog = true;
        this.visibilityChecked = false;
        this.effectKind = 0;
        this.isUiEffect = false;
        this.posX = 0.0f;
        this.posY = 0.0f;
        this.isLaser = false;
        this.laserTargetX = 0.0f;
        this.laserTargetY = 0.0f;
        this.laserTargetZ = 0.0f;
        this.posZ = 0.0f;
        this.drawLayer = (short) 2;
        this.isCentered = false;
        this.verticalAnchorOffset = 0.0f;
        this.animateFrames = false;
        this.currentFrame = 0.0f;
        this.animateFrameSpeed = 0.0f;
        this.animateFrameEnd = 0;
        this.animateFramePingPong = false;
        this.animateFrameLooping = false;
        this.animateFrameReversing = false;
        this.unusedFlag1 = false;
        this.frameIndex = 0;
        this.stripIndex = 0;
        this.delayedStartTimer = 0.0f;
        this.lifeTimer = 15.0f;
        this.lifeMax = this.lifeTimer;
        this.trailTimer = 0.0f;
        this.fadeIn = false;
        this.fadeOut = false;
        this.fadeDuration = 0.0f;
        this.scaleTo = 1.0f;
        this.scaleFrom = 1.0f;
        this.scaleXFrom = Float.NaN;
        this.scaleXTo = Float.NaN;
        this.scaleYFrom = Float.NaN;
        this.scaleYTo = Float.NaN;
        this.imageAnchorY = 0.5f;
        this.scaleWithZoom = false;
        this.useGravity = false;
        this.useBounce = false;
        this.physicsGravity = 1.0f;
        this.alpha = 1.0f;
        this.rotation = 0.0f;
        this.rotationSpeed = 0.0f;
        this.velocityX = 0.0f;
        this.velocityY = 0.0f;
        this.velocityZ = 0.0f;
        this.bobAmplitude = 0.0f;
        this.bobPeriod = 0.0f;
        this.text = null;
        this.textPaint = null;
        this.textXOffset = 0.0f;
        this.textYOffset = 0.0f;
        this.emitRecursionDepth = (short) 0;
        this.startColor = -1;
        this.lightingColorFilter = null;
        this.endColor = -1;
        this.endColorTransitionTime = -1.0f;
        this.colorPaint.a((ColorFilter) null);
        this.colorPaint.a((io.github.rwx.render.frame.GameCanvasBlendMode) null);
        this.hasColorFilterApplied = false;
        this.colorPaint.a((DisplacementEffect) null);
        this.colorPaint.a((ShaderProgram) null);
        this.shadow = false;
    }

    /* JADX INFO: renamed from: a */
    public void recycle(Effect effect) {
        this.template = effect.template;
        this.priority = effect.priority;
        this.effectKind = effect.effectKind;
        this.parentObject = effect.parentObject;
        this.ignoreParentZ = effect.ignoreParentZ;
        this.isLight = effect.isLight;
        this.showInFog = effect.showInFog;
        this.isUiEffect = effect.isUiEffect;
        this.posX = effect.posX;
        this.posY = effect.posY;
        this.isLaser = effect.isLaser;
        this.laserTargetX = effect.laserTargetX;
        this.laserTargetY = effect.laserTargetY;
        this.laserTargetZ = effect.laserTargetZ;
        this.posZ = effect.posZ;
        this.drawLayer = effect.drawLayer;
        this.isCentered = effect.isCentered;
        this.verticalAnchorOffset = effect.verticalAnchorOffset;
        this.animateFrames = effect.animateFrames;
        this.currentFrame = effect.currentFrame;
        this.animateFrameSpeed = effect.animateFrameSpeed;
        this.animateFrameEnd = effect.animateFrameEnd;
        this.animateFramePingPong = effect.animateFramePingPong;
        this.animateFrameLooping = effect.animateFrameLooping;
        this.animateFrameReversing = effect.animateFramePingPong;
        this.unusedFlag1 = effect.unusedFlag1;
        this.frameIndex = effect.frameIndex;
        this.stripIndex = effect.stripIndex;
        this.delayedStartTimer = effect.delayedStartTimer;
        this.lifeTimer = effect.lifeTimer;
        this.lifeMax = effect.lifeMax;
        this.trailTimer = effect.trailTimer;
        this.fadeIn = effect.fadeIn;
        this.fadeOut = effect.fadeOut;
        this.fadeDuration = effect.fadeDuration;
        this.scaleTo = effect.scaleTo;
        this.scaleFrom = effect.scaleFrom;
        this.scaleXFrom = effect.scaleXFrom;
        this.scaleXTo = effect.scaleXTo;
        this.scaleYFrom = effect.scaleYFrom;
        this.scaleYTo = effect.scaleYTo;
        this.imageAnchorY = effect.imageAnchorY;
        this.scaleWithZoom = effect.scaleWithZoom;
        this.useGravity = effect.useGravity;
        this.useBounce = effect.useBounce;
        this.physicsGravity = effect.physicsGravity;
        this.alpha = effect.alpha;
        this.rotation = effect.rotation;
        this.rotationSpeed = effect.rotationSpeed;
        this.velocityX = effect.velocityX;
        this.velocityY = effect.velocityY;
        this.velocityZ = effect.velocityZ;
        this.bobAmplitude = effect.bobAmplitude;
        this.bobPeriod = effect.bobPeriod;
        this.text = effect.text;
        this.textPaint = effect.textPaint;
        this.textXOffset = effect.textXOffset;
        this.textYOffset = effect.textYOffset;
        this.emitRecursionDepth = effect.emitRecursionDepth;
        this.startColor = effect.startColor;
        this.endColor = effect.endColor;
        this.endColorTransitionTime = effect.endColorTransitionTime;
        this.lightingColorFilter = effect.lightingColorFilter;
        this.shadow = effect.shadow;
        this.colorPaint.a(effect.colorPaint.getBlendMode());
    }

    /* JADX INFO: renamed from: b */
    public void draw(float f) {
        this.delayedStartTimer = Utility.moveTowardsZero(this.delayedStartTimer, f);
        if (this.delayedStartTimer > 0.0f) {
            return;
        }
        this.lifeTimer -= f;
        if (this.parentObject != null && this.parentObject.isDestroyed && !this.template.liveAfterAttachedDies) {
            this.lifeTimer = -1.0f;
        }
        if (this.lifeTimer < 0.0f) {
            reset();
            return;
        }
        if (this.animateFrames) {
            if (this.animateFrameReversing) {
                this.currentFrame -= this.animateFrameSpeed * f;
            } else {
                this.currentFrame += this.animateFrameSpeed * f;
            }
            if (this.animateFramePingPong) {
                if (this.animateFrameReversing) {
                    if (this.currentFrame < this.animateFrameStart) {
                        if (!this.animateFrameLooping) {
                            reset();
                            return;
                        } else {
                            this.animateFrameReversing = false;
                            this.currentFrame = this.animateFrameStart;
                        }
                    }
                } else if (this.currentFrame >= this.animateFrameEnd + 1) {
                    this.animateFrameReversing = true;
                    this.currentFrame = this.animateFrameEnd;
                }
            } else if (this.currentFrame >= this.animateFrameEnd + 1) {
                if (!this.animateFrameLooping) {
                    reset();
                    return;
                }
                this.currentFrame = this.animateFrameStart;
            }
            this.frameIndex = (int) this.currentFrame;
        }
        if (this.useGravity) {
            this.velocityZ -= (this.velocityZ * 0.002f) * f;
            this.velocityX -= f * 0.0015f;
        }
        if (this.useBounce) {
            if (this.posZ > 0.0f) {
                this.velocityZ -= (0.1f * this.physicsGravity) * f;
            } else {
                if (this.velocityZ < 0.0f) {
                    this.velocityZ = -this.velocityZ;
                    this.velocityZ *= 0.5f;
                    this.velocityZ = Utility.moveTowardsZero(this.velocityZ, 1.3f);
                }
                if (this.posZ < 0.0f) {
                    this.posZ = 0.0f;
                }
                if (this.velocityZ < 0.2d) {
                    this.drawLayer = (short) 1;
                }
                this.velocityX = Utility.moveTowardsZero(this.velocityX, 0.15f * f);
                this.velocityY = Utility.moveTowardsZero(this.velocityY, 0.15f * f);
                this.rotationSpeed = Utility.moveTowardsZero(this.rotationSpeed, 1.0f * f);
            }
        }
        this.posX += this.velocityX * f;
        this.posY += this.velocityY * f;
        this.posZ += this.velocityZ * f;
        this.rotation += this.rotationSpeed * f;
        if (this.template.trailEffect != null) {
            this.trailTimer += f;
            if (this.trailTimer > this.template.trailEffectRate) {
                this.trailTimer = 0.0f;
                if (this.emitRecursionDepth < 20) {
                    float f2 = this.posX;
                    float f3 = this.posY;
                    float f4 = this.posZ;
                    if (this.parentObject != null) {
                        f2 += this.parentObject.posX;
                        f3 += this.parentObject.posY;
                        f4 += this.parentObject.posZ;
                    }
                    this.template.trailEffect.a(f2, f3, f4, this.rotation, this.parentObject, 0, this.emitRecursionDepth);
                }
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public static void computeSpriteRect(int i2, SpriteSheet spriteSheet, Rect rect) {
        int i3 = 0;
        if (i2 >= spriteSheet.framesPerRow) {
            i3 = 0 + (i2 / spriteSheet.framesPerRow);
            i2 %= spriteSheet.framesPerRow;
        }
        int i4 = spriteSheet.offsetX + (i2 * spriteSheet.stepX);
        int i5 = spriteSheet.offsetY + (i3 * spriteSheet.stepY);
        rect.a = i4;
        rect.b = i5;
        rect.c = i4 + spriteSheet.frameWidth;
        rect.d = i5 + spriteSheet.frameHeight;
    }

    /* JADX INFO: renamed from: a */
    public boolean update(GameEngine gameEngine, boolean z) {
        SpriteSheet spriteSheet;
        PointF pointFCreatePointWithOffset;
        float fClampTo255;
        GamePaint texture;
        Rect rect = EffectManager.rect;
        RectF rectF = EffectManager.rectF;
        if (this.delayedStartTimer > 0.0f) {
            return false;
        }
        if (z && this.posZ < 1.0f) {
            return false;
        }
        if (RenderRegistry.drawEffect(this, gameEngine, z)) {
            return true;
        }
        if (this.template.imageStrip != null) {
            spriteSheet = this.template.imageStrip;
        } else {
            spriteSheet = EffectManager.effectTemplates[this.stripIndex];
        }
        if (!spriteSheet.singleFrame) {
            computeSpriteRect(this.frameIndex, spriteSheet, rect);
        } else {
            rect.a(0, 0, spriteSheet.texture.m(), spriteSheet.texture.l());
        }
        if (!z) {
            pointFCreatePointWithOffset = Utility.createPointWithOffset(this.posX, this.posY, this.posZ);
        } else {
            pointFCreatePointWithOffset = Utility.createPointWithOffset(this.posX, this.posY, 0.0f);
        }
        boolean z2 = this.drawLayer == 4;
        float fFromHexString = 1.0f;
        if (this.scaleFrom != 1.0f || this.scaleTo != 1.0f || this.scaleWithZoom) {
            fFromHexString = Utility.lerp(this.scaleFrom, this.scaleTo, 1.0f - (this.lifeTimer / this.lifeMax));
            boolean z3 = this.drawLayer != 4;
            if (this.scaleWithZoom && z3) {
                fFromHexString = fFromHexString * (1.0f / gameEngine.zoom) * gameEngine.screenScale;
            }
        }
        float scaleProgress = 1.0f - (this.lifeTimer / this.lifeMax);
        float scaleX = Float.isNaN(this.scaleXFrom)
                ? fFromHexString
                : Utility.lerp(this.scaleXFrom, this.scaleXTo, scaleProgress);
        float scaleY = Float.isNaN(this.scaleYFrom)
                ? fFromHexString
                : Utility.lerp(this.scaleYFrom, this.scaleYTo, scaleProgress);
        rectF.a(pointFCreatePointWithOffset.x, pointFCreatePointWithOffset.y, pointFCreatePointWithOffset.x + rect.b(), pointFCreatePointWithOffset.y + rect.c());
        if (this.isCentered) {
            rectF.a((-rectF.b()) / 2.0f, (-rectF.c()) / 2.0f);
        }
        if (this.verticalAnchorOffset != 0.0f) {
            rectF.a(0.0f, rectF.c() * this.verticalAnchorOffset * fFromHexString);
        }
        if (this.imageAnchorY != 0.5f) {
            rectF.a(0.0f, rectF.c() * (0.5f - this.imageAnchorY));
        }
        if (this.parentObject != null) {
            if (!z && !this.ignoreParentZ) {
                rectF.a(this.parentObject.posX, this.parentObject.posY - this.parentObject.posZ);
            } else {
                rectF.a(this.parentObject.posX, this.parentObject.posY);
            }
        }
        if ((!z2 || this.isLaser) && !Utility.rectanglesOverlap(gameEngine.bufferedVisibleWorldRect, rectF)) {
            return false;
        }
        if (!this.showInFog && !z2 && !this.visibilityChecked) {
            if (!gameEngine.tileMap.isWorldPointVisibleForTeam(rectF.d(), rectF.e(), gameEngine.playerTeam)) {
                return false;
            }
            this.visibilityChecked = true;
        }
        if (!z2) {
            rectF.a(-gameEngine.viewpointXSnapped, -gameEngine.viewpointYSnapped);
        }
        if (this.bobAmplitude != 0.0f) {
            rectF.a(0.0f, Utility.fastSin(((this.lifeMax - this.lifeTimer) / this.bobPeriod) * 360.0f) * this.bobAmplitude);
        }
        float f = this.lifeMax - this.lifeTimer;
        float fA = 1.0f;
        float f2 = 1.0f;
        float f3 = 1.0f;
        float f4 = 1.0f;
        boolean z4 = this.colorPaint.getBlendMode() != null;
        if (this.startColor != -1) {
            fA = ArgbColor.a(this.startColor) * 0.003921569f;
            int iB = ArgbColor.b(this.startColor);
            int iC = ArgbColor.c(this.startColor);
            int iD = ArgbColor.d(this.startColor);
            if (iB != 255 || iC != 255 || iD != 255) {
                z4 = true;
                f2 = iB * 0.003921569f;
                f3 = iC * 0.003921569f;
                f4 = iD * 0.003921569f;
            }
        }
        if (this.endColorTransitionTime >= 0.0f) {
            float fA2 = ArgbColor.a(this.endColor) * 0.003921569f;
            float fB = ArgbColor.b(this.endColor) * 0.003921569f;
            float fC = ArgbColor.c(this.endColor) * 0.003921569f;
            float fD = ArgbColor.d(this.endColor) * 0.003921569f;
            if (this.endColorTransitionTime <= f) {
                fA = fA2;
                z4 = true;
                f2 = fB;
                f3 = fC;
                f4 = fD;
            } else {
                float f5 = f / this.endColorTransitionTime;
                float f6 = 1.0f - f5;
                fA = (fA * f6) + (fA2 * f5);
                z4 = true;
                f2 = (f2 * f6) + (fB * f5);
                f3 = (f3 * f6) + (fC * f5);
                f4 = (f4 * f6) + (fD * f5);
            }
        }
        if (this.fadeIn && f >= this.fadeDuration) {
            fClampTo255 = fA * (this.lifeTimer / (this.lifeMax - this.fadeDuration)) * this.alpha;
        } else if (this.fadeOut && f < this.fadeDuration) {
            fClampTo255 = fA * (f / this.fadeDuration) * this.alpha;
        } else {
            fClampTo255 = fA * this.alpha;
        }
        if (fClampTo255 > 1.0f) {
            fClampTo255 = 1.0f;
        }
        if (fClampTo255 < 0.0f) {
            fClampTo255 = 0.0f;
        }
        boolean z5 = false;
        GraphicsEngine graphicsEngine = gameEngine.renderGraphicsEngine;
        if (this.rotation != 0.0f) {
            if (0 == 0) {
                z5 = true;
                graphicsEngine.k();
            }
            graphicsEngine.a(this.rotation + 90.0f, rectF.d(), rectF.e());
        }
        if (scaleX != 1.0f || scaleY != 1.0f) {
            if (!z5) {
                z5 = true;
                graphicsEngine.k();
            }
            graphicsEngine.a(scaleX, scaleY, rectF.d(), rectF.e());
        }
        if (z) {
            fClampTo255 = Utility.clampTo255(fClampTo255 / 3.0f, 0.0f, 1.0f);
            f2 = 0.0f;
            f3 = 0.0f;
            f4 = 0.0f;
            z4 = true;
        }
        if (z4 && graphicsEngine.backendCapabilities().getRequiresImageTintColorFilter() && !z && this.lightingColorFilter == null) {
            int iLongToIntArray = Utility.packArgb(255, (int) (f2 * 255.0f), (int) (f3 * 255.0f), (int) (f4 * 255.0f));
            if (cachedLightingColorFilter != null && cachedLightingColorFilterColor == iLongToIntArray) {
                this.lightingColorFilter = cachedLightingColorFilter;
            } else {
                cachedLightingColorFilter = new MultiplyAddColorFilter(iLongToIntArray, 0);
                cachedLightingColorFilterColor = iLongToIntArray;
                this.lightingColorFilter = cachedLightingColorFilter;
            }
        }
        MultiplyAddColorFilter multiplyAddColorFilter = this.lightingColorFilter;
        if (multiplyAddColorFilter != null) {
            if (!this.hasColorFilterApplied) {
                this.colorPaint.a(multiplyAddColorFilter);
                this.hasColorFilterApplied = true;
            }
            z4 = true;
        } else if (this.hasColorFilterApplied) {
            this.colorPaint.a((ColorFilter) null);
            this.hasColorFilterApplied = false;
        }
        if (this.drawLayer == 3) {
            if (EffectManager.displacementEffect == null) {
                GameEngine.log("Loading displacement effect");
                EffectManager.displacementEffect = new DisplacementEffect();
            }
            if (EffectManager.shader == null) {
                try {
                    EffectManager.shader = new ShaderProgram("assets/shaders/post_displacement.frag");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (this.effectManager.texture != null) {
                DisplacementEffect koolDisplacementEffect = EffectManager.displacementEffect;
                koolDisplacementEffect.configure(this.effectManager.texture, 0.12f * gameEngine.zoom);
                this.colorPaint.a(koolDisplacementEffect);
                ShaderProgram shaderProgram = EffectManager.shader;
                shaderProgram.a("screenBase", this.effectManager.texture);
                shaderProgram.b("screenBaseSize", this.effectManager.texture);
                shaderProgram.a("u_resolution", gameEngine.screenWidth, gameEngine.screenHeight);
                shaderProgram.a("u_offsetBy", 0.12f * gameEngine.zoom);
                shaderProgram.a("u_uiScaling", 1.0f);
                this.colorPaint.a(shaderProgram);
                z4 = true;
            }
        }
        if (!z4) {
            texture = getTexture(fClampTo255);
        } else {
            texture = this.colorPaint;
            int iLongToIntArray2 = Utility.packArgb(255, (int) (f2 * 255.0f), (int) (f3 * 255.0f), (int) (f4 * 255.0f));
            float f7 = this.cachedAlpha - fClampTo255;
            if (f7 < -0.01f || f7 > 0.01f || this.cachedColor != iLongToIntArray2) {
                this.cachedAlpha = fClampTo255;
                this.cachedColor = iLongToIntArray2;
                this.colorPaint.b(Utility.packArgb((int) (fClampTo255 * 255.0f), (int) (f2 * 255.0f), (int) (f3 * 255.0f), (int) (f4 * 255.0f)));
            }
        }
        if (this.text != null) {
            Paint paint = texture;
            if (this.textPaint != null) {
                paint = this.textPaint;
            }
            graphicsEngine.a(this.text, rectF.d() + this.textXOffset, rectF.e() + this.textYOffset, paint);
        }
        if (this.isLaser) {
            PointF pointFCreatePointWithOffset2 = Utility.createPointWithOffset(this.laserTargetX, this.laserTargetY, this.laserTargetZ);
            graphicsEngine.a(rectF.a, rectF.b, pointFCreatePointWithOffset2.x - gameEngine.viewpointXSnapped, pointFCreatePointWithOffset2.y - gameEngine.viewpointYSnapped, this.effectManager.linePaint);
        } else if (z) {
            if (spriteSheet.shadowTexture != null) {
                graphicsEngine.a(spriteSheet.shadowTexture, rect, rectF, texture);
            }
        } else {
            graphicsEngine.a(spriteSheet.texture, rect, rectF, texture);
        }
        if (z5) {
            graphicsEngine.l();
            return true;
        }
        return true;
    }
}
