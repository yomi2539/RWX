package com.corrodinggames.rts.gameFramework.effects;

import android.content.Context;
import android.graphics.*;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.map.MapTile;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.GameObject;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.graphics.ShaderProgram;
import com.corrodinggames.rts.gameFramework.graphics.Texture;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.d.c */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/d/c.class */
public final class EffectManager {

    /* JADX INFO: renamed from: h */
    public static boolean useStrictCounting;

    /* JADX INFO: renamed from: i */
    public int maxParticlesVeryLow;

    /* JADX INFO: renamed from: j */
    public int maxParticlesLow;

    /* JADX INFO: renamed from: k */
    public static ShaderProgram shader;

    /* JADX INFO: renamed from: l */
    public Texture texture;

    /* JADX INFO: renamed from: p */
    public static final Rect unusedRect = new Rect();

    /* JADX INFO: renamed from: s */
    public static SpriteSheet[] effectTemplates;

    /* JADX INFO: renamed from: f */
    public static Effect[] effects = new Effect[0];

    /* JADX INFO: renamed from: g */
    public static int nextFreeEffect = 0;

    /* JADX INFO: renamed from: n */
    public static final RectF rectF = new RectF();

    /* JADX INFO: renamed from: o */
    public static final Rect rect = new Rect();
    /* JADX INFO: renamed from: r */
    public static final Paint unusedPaint = new Paint();

    /* JADX INFO: renamed from: q */
    public static final Paint paint = new Paint();
    /* JADX INFO: renamed from: m */
    public Texture shockwaveTexture;

    /* JADX INFO: renamed from: a */
    public int activeEffectsCount = 0;

    /* JADX INFO: renamed from: b */
    public int maxEffectsVeryLow = 80;

    /* JADX INFO: renamed from: c */
    public int maxEffectsLow = 100;

    /* JADX INFO: renamed from: d */
    public int maxEffectsHigh = 110;

    /* JADX INFO: renamed from: e */
    public int maxEffectsVeryHigh = 120;

    /* JADX INFO: renamed from: y */
    private boolean[] activeEffectLayerFlags = new boolean[5];

    /* JADX INFO: renamed from: t */
    EffectQuality overrideEffectQuality = null;

    /* JADX INFO: renamed from: u */
    boolean onlyOnScreen = false;

    /* JADX INFO: renamed from: v */
    boolean forceHighQuality = false;

    /* JADX INFO: renamed from: w */
    public final Paint linePaint = new Paint();

    /* JADX INFO: renamed from: x */
    float lastUpdate = 0.0f;

    /* JADX INFO: renamed from: a */
    public static void attachEffectToGameObject(Effect effect, GameObject gameObject) {
        if (effect == null) {
            return;
        }
        effect.parentObject = gameObject;
        effect.posX -= gameObject.posX;
        effect.posY -= gameObject.posY;
        effect.posZ -= gameObject.posZ;
    }

    /* JADX INFO: renamed from: a */
    public Effect getNewEffect(EffectQuality effectQuality) {
        int i = 0;
        int fps = GameEngine.getInstance().getFps();
        if (fps < 13) {
            i = -this.maxParticlesLow;
        } else if (fps < 28) {
            i = -this.maxParticlesVeryLow;
        }
        int i2 = this.activeEffectsCount;
        if (effectQuality == EffectQuality.verylow && i2 > this.maxEffectsVeryLow + i) {
            return null;
        }
        if (effectQuality == EffectQuality.low && i2 > this.maxEffectsLow + i) {
            return null;
        }
        if (effectQuality == EffectQuality.high && i2 > this.maxEffectsHigh + i) {
            return null;
        }
        if (effectQuality == EffectQuality.veryhigh && i2 > this.maxEffectsVeryHigh + i) {
            return null;
        }
        Effect effectFindFreeEffect = findFreeEffect(true, (EffectQuality) null);
        if (effectFindFreeEffect == null && (effectQuality == EffectQuality.critical || effectQuality == EffectQuality.veryhigh)) {
            effectFindFreeEffect = findFreeEffect(false, EffectQuality.high);
        }
        if (effectFindFreeEffect != null) {
            if (!effectFindFreeEffect.isActive) {
                effectFindFreeEffect.isActive = true;
                this.activeEffectsCount++;
            }
            return effectFindFreeEffect;
        }
        return null;
    }

    /* JADX INFO: renamed from: a */
    public void createExplosion(float f, float f2, float f3) {
        createExplosionWithVelocity(f, f2, f3, 0.0f, 20.0f);
    }

    /* JADX INFO: renamed from: a */
    public void createExplosionWithVelocity(float f, float f2, float f3, float f4, float f5) {
        for (int i = 0; i < 7; i++) {
            Effect effectCreateSmallExplosion = createSmallExplosion(f + Utility.randomFloatInRange(-20.0f, 20.0f), f2 + Utility.randomFloatInRange(-20.0f, 20.0f), f3);
            if (effectCreateSmallExplosion != null) {
                effectCreateSmallExplosion.delayedStartTimer = f4 + Utility.randomFloatInRange(0.0f, f5);
                effectCreateSmallExplosion.animateFrameSpeed = Utility.randomFloatInRange(0.3f, 0.6f);
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public float random(float f, float f2) {
        return Utility.randomFloatInRange(f, f2);
    }

    /* JADX INFO: renamed from: a */
    private Effect findFreeEffect(boolean z, EffectQuality effectQuality) {
        Effect[] effectArr = effects;
        int length = effectArr.length;
        if (z && effectQuality == null) {
            for (int i = 0; i < length; i++) {
                Effect effect = effectArr[i];
                if (!effect.isActive) {
                    if (nextFreeEffect == i) {
                        nextFreeEffect++;
                    }
                    return effect;
                }
            }
            return null;
        }
        for (Effect effect2 : effectArr) {
            if ((!z || !effect2.isActive) && (effectQuality == null || effect2.priority.isLowerThan(effectQuality))) {
                return effect2;
            }
        }
        return null;
    }

    /* JADX INFO: renamed from: b */
    public Effect createSmallExplosion(float f, float f2, float f3) {
        setOnlyOnScreen();
        Effect effectCreateEffectInternal = createEffectInternal(f, f2, f3, EffectType.custom, false, EffectQuality.high);
        if (effectCreateEffectInternal != null) {
            effectCreateEffectInternal.stripIndex = 1;
            effectCreateEffectInternal.animateFrames = true;
            effectCreateEffectInternal.currentFrame = 0.0f;
            effectCreateEffectInternal.animateFrameSpeed = 0.5f;
            effectCreateEffectInternal.animateFrameEnd = 12;
            effectCreateEffectInternal.frameIndex = 0;
            effectCreateEffectInternal.lifeTimer = 35.0f;
            effectCreateEffectInternal.lifeMax = effectCreateEffectInternal.lifeTimer - 10.0f;
            effectCreateEffectInternal.fadeIn = true;
            effectCreateEffectInternal.alpha = 0.7f;
            effectCreateEffectInternal.rotation = random(-180.0f, 180.0f);
            float fRandom = random(0.8f, 1.0f);
            effectCreateEffectInternal.scaleFrom = fRandom;
            effectCreateEffectInternal.scaleTo = fRandom;
        }
        return effectCreateEffectInternal;
    }

    /* JADX INFO: renamed from: c */
    public Effect createLargeExplosion(float f, float f2, float f3) {
        setOnlyOnScreen();
        Effect effectCreateEffectInternal = createEffectInternal(f, f2, f3, EffectType.custom, false, EffectQuality.high);
        if (effectCreateEffectInternal != null) {
            effectCreateEffectInternal.stripIndex = 13;
            effectCreateEffectInternal.animateFrames = true;
            effectCreateEffectInternal.currentFrame = 3.0f;
            effectCreateEffectInternal.animateFrameSpeed = 0.5f;
            effectCreateEffectInternal.animateFrameEnd = 7;
            effectCreateEffectInternal.frameIndex = 0;
            effectCreateEffectInternal.lifeTimer = 35.0f;
            effectCreateEffectInternal.lifeMax = effectCreateEffectInternal.lifeTimer - 10.0f;
            effectCreateEffectInternal.fadeIn = true;
            effectCreateEffectInternal.alpha = 1.0f;
            effectCreateEffectInternal.scaleFrom = 0.5f;
            effectCreateEffectInternal.scaleTo = 0.5f;
        }
        return effectCreateEffectInternal;
    }

    /* JADX INFO: renamed from: a */
    public Effect createFlameEffect(float f, float f2, float f3, float f4) {
        return createFlameEffect(f, f2, f3, f4, 0);
    }

    /* JADX INFO: renamed from: a */
    public Effect createFlameEffect(float f, float f2, float f3, float f4, int i) {
        return createFlameEffectInternal(f, f2, f3, f4, i, 0);
    }

    /* JADX INFO: renamed from: b */
    public Effect createFlameEffect2(float f, float f2, float f3, float f4, int i) {
        return createFlameEffectInternal(f, f2, f3, f4, i, 1);
    }

    /* JADX INFO: renamed from: a */
    public Effect createLaserEffect(float f, float f2, float f3, float f4, float f5, float f6) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (!gameEngine.tileMap.isWorldPointVisibleForTeam(f, f2, gameEngine.playerTeam) && !gameEngine.tileMap.isWorldPointVisibleForTeam(f4, f5, gameEngine.playerTeam)) {
            return null;
        }
        Effect effectCreateEffectInternal = createEffectInternal(f, f2, f3, EffectType.custom, true, EffectQuality.high);
        if (effectCreateEffectInternal != null) {
            effectCreateEffectInternal.isCentered = false;
            effectCreateEffectInternal.lifeTimer = 5.0f;
            effectCreateEffectInternal.lifeMax = effectCreateEffectInternal.lifeTimer;
            effectCreateEffectInternal.fadeIn = true;
            effectCreateEffectInternal.alpha = 1.0f;
            effectCreateEffectInternal.isLaser = true;
            effectCreateEffectInternal.laserTargetX = f4;
            effectCreateEffectInternal.laserTargetY = f5;
            effectCreateEffectInternal.laserTargetZ = f6;
        }
        return effectCreateEffectInternal;
    }

    /* JADX INFO: renamed from: a */
    public Effect createFlameEffectInternal(float f, float f2, float f3, float f4, int i, int i2) {
        setOnlyOnScreen();
        Effect effectCreateEffectInternal = createEffectInternal(f, f2, f3, EffectType.custom, false, EffectQuality.high);
        if (effectCreateEffectInternal != null) {
            effectCreateEffectInternal.effectKind = Effect.KIND_FLAME;
            effectCreateEffectInternal.animateFrames = true;
            if (i2 == 1) {
                effectCreateEffectInternal.stripIndex = 3;
                effectCreateEffectInternal.currentFrame = 1.0f;
                effectCreateEffectInternal.animateFrameSpeed = 0.4f;
                effectCreateEffectInternal.animateFrameEnd = 4;
            } else {
                effectCreateEffectInternal.stripIndex = 3;
                effectCreateEffectInternal.currentFrame = 0.0f;
                effectCreateEffectInternal.animateFrameSpeed = 0.5f;
                effectCreateEffectInternal.animateFrameEnd = 3;
            }
            effectCreateEffectInternal.rotation = f4;
            effectCreateEffectInternal.frameIndex = 0;
            effectCreateEffectInternal.lifeTimer = 20.0f;
            effectCreateEffectInternal.lifeMax = effectCreateEffectInternal.lifeTimer;
            effectCreateEffectInternal.fadeIn = false;
            if (i != 0) {
                effectCreateEffectInternal.lightingColorFilter = new LightingColorFilter(i, 0);
            }
        }
        return effectCreateEffectInternal;
    }

    /* JADX INFO: renamed from: c */
    public Effect createMuzzleFlash(float f, float f2, float f3, float f4, int i) {
        Effect effectCreateEffectInternal = createEffectInternal(f, f2, f3, EffectType.custom, false, EffectQuality.verylow);
        if (effectCreateEffectInternal != null) {
            effectCreateEffectInternal.stripIndex = 4;
            effectCreateEffectInternal.effectKind = Effect.KIND_MUZZLE_FLASH;
            effectCreateEffectInternal.frameIndex = Utility.getRandomIntInRange(0, 2);
            effectCreateEffectInternal.rotation = f4;
            effectCreateEffectInternal.isCentered = true;
            effectCreateEffectInternal.velocityX = Utility.fastCos(f4) * 0.15f;
            effectCreateEffectInternal.velocityY = Utility.fastSin(f4) * 0.15f;
            effectCreateEffectInternal.lifeTimer = 30.0f;
            effectCreateEffectInternal.lifeMax = effectCreateEffectInternal.lifeTimer;
            effectCreateEffectInternal.fadeIn = true;
            effectCreateEffectInternal.drawLayer = (short) 1;
            effectCreateEffectInternal.scaleFrom = 0.8f;
            effectCreateEffectInternal.scaleTo = 2.3f;
            if (i != 0) {
                effectCreateEffectInternal.lightingColorFilter = new LightingColorFilter(i, 0);
            }
        }
        return effectCreateEffectInternal;
    }

    /* JADX INFO: renamed from: a */
    public Effect createLightEffect(GameObject gameObject, int i) {
        return createLightEffect(gameObject, i, 0.5f);
    }

    /* JADX INFO: renamed from: a */
    public Effect createLightEffect(GameObject gameObject, int i, float f) {
        setForceHighQuality();
        Effect effectCreateLightEffectInternal = createLightEffectInternal(gameObject.posX, gameObject.posY, gameObject.posZ, i);
        if (effectCreateLightEffectInternal != null) {
            effectCreateLightEffectInternal.posX = 0.0f;
            effectCreateLightEffectInternal.posY = 0.0f;
            effectCreateLightEffectInternal.posZ = 0.0f;
            effectCreateLightEffectInternal.lifeTimer = 400.0f;
            effectCreateLightEffectInternal.lifeMax = effectCreateLightEffectInternal.lifeTimer;
            effectCreateLightEffectInternal.alpha = 0.3f;
            effectCreateLightEffectInternal.scaleFrom = f;
            effectCreateLightEffectInternal.parentObject = gameObject;
        }
        return effectCreateLightEffectInternal;
    }

    /* JADX INFO: renamed from: a */
    public Effect createLightEffect(float f, float f2, float f3, int i) {
        if (this.overrideEffectQuality == null && !this.forceHighQuality) {
            setOnlyOnScreen();
        }
        return createLightEffectInternal(f, f2, f3, i);
    }

    /* JADX INFO: renamed from: b */
    public Effect createLightEffectInternal(float f, float f2, float f3, int i) {
        Effect effectCreateEffectInternal = createEffectInternal(f, f2, f3, EffectType.custom, true, EffectQuality.low);
        if (effectCreateEffectInternal != null) {
            effectCreateEffectInternal.showInFog = false;
            effectCreateEffectInternal.effectKind = Effect.KIND_LIGHT;
            effectCreateEffectInternal.stripIndex = 2;
            effectCreateEffectInternal.lifeTimer = 10.0f;
            effectCreateEffectInternal.lifeMax = effectCreateEffectInternal.lifeTimer;
            effectCreateEffectInternal.fadeIn = true;
            effectCreateEffectInternal.alpha = 0.5f;
            effectCreateEffectInternal.drawLayer = (short) 2;
            effectCreateEffectInternal.isLight = true;
            if (i != 0) {
                effectCreateEffectInternal.startColor = i;
                effectCreateEffectInternal.lightingColorFilter = new LightingColorFilter(i, 0);
            }
        }
        return effectCreateEffectInternal;
    }

    /* JADX INFO: renamed from: b */
    public Effect createSmokeEffect(float f, float f2, float f3, float f4) {
        setOnlyOnScreen();
        Effect effectCreateEffectInternal = createEffectInternal(f, f2, f3, EffectType.custom, false, EffectQuality.low);
        if (effectCreateEffectInternal != null) {
            effectCreateEffectInternal.effectKind = Effect.KIND_SMOKE;
            effectCreateEffectInternal.stripIndex = 0;
            effectCreateEffectInternal.frameIndex = 13;
            effectCreateEffectInternal.drawLayer = (short) 1;
            effectCreateEffectInternal.fadeIn = true;
            effectCreateEffectInternal.alpha = 0.8f;
            effectCreateEffectInternal.lifeMax = 80.0f;
            effectCreateEffectInternal.lifeTimer = effectCreateEffectInternal.lifeMax;
            effectCreateEffectInternal.rotation = Utility.randomFloatInRange(-180.0f, 180.0f);
            effectCreateEffectInternal.scaleFrom = Utility.randomFloatInRange(0.6f, 0.8f);
            effectCreateEffectInternal.scaleTo = 1.5f;
            effectCreateEffectInternal.velocityX = (Utility.fastCos(f4) * 0.13f * Utility.randomFloatInRange(1.0f, 1.5f)) + Utility.randomFloatInRange(-0.01f, 0.01f);
            effectCreateEffectInternal.velocityY = (Utility.fastSin(f4) * 0.13f * Utility.randomFloatInRange(1.0f, 1.5f)) + Utility.randomFloatInRange(-0.01f, 0.01f);
        }
        return effectCreateEffectInternal;
    }

    /* JADX INFO: renamed from: a */
    public Effect createRedLaserEffect(float f, float f2, float f3, int i, float f4, float f5) {
        Effect effectCreateEffectInternal = createEffectInternal(f, f2, f3, EffectType.custom, false, EffectQuality.high);
        if (effectCreateEffectInternal != null) {
            effectCreateEffectInternal.effectKind = Effect.KIND_SMOKE;
            effectCreateEffectInternal.stripIndex = 6;
            effectCreateEffectInternal.lifeTimer = 120.0f;
            effectCreateEffectInternal.lifeMax = effectCreateEffectInternal.lifeTimer;
            effectCreateEffectInternal.fadeIn = true;
            effectCreateEffectInternal.scaleFrom = 0.2f;
            effectCreateEffectInternal.scaleTo = 0.9f;
            effectCreateEffectInternal.drawLayer = (short) 1;
            effectCreateEffectInternal.alpha = 0.5f;
            effectCreateEffectInternal.velocityX = f4;
            effectCreateEffectInternal.velocityY = f5;
            if (i != 0) {
                i = Color.a(255, 0, 0, 200);
            }
            if (i != 0) {
                effectCreateEffectInternal.lightingColorFilter = new LightingColorFilter(i, 0);
            }
        }
        return effectCreateEffectInternal;
    }

    /* JADX INFO: renamed from: a */
    public void createDirectedExplosion(float f, float f2, float f3, int i, float f4, float f5, float f6) {
        createRedLaserEffect(f, f2, 0.0f, 0, 0.0f, 0.0f);
        for (int i2 = -180; i2 < 180; i2 += 45) {
            float f7 = f6 + i2;
            Effect effectCreateSmokeEffect = createSmokeEffect(f + (Utility.fastCos(f7) * (-5.0f)), f2 + (Utility.fastSin(f7) * (-5.0f)), 0.0f, f7);
            if (effectCreateSmokeEffect != null) {
                effectCreateSmokeEffect.drawLayer = (short) 2;
                effectCreateSmokeEffect.fadeOut = true;
                effectCreateSmokeEffect.fadeDuration = 7.0f;
            }
        }
    }

    /* JADX INFO: renamed from: c */
    public Effect createSmallExplosion(float f, float f2, float f3, int i) {
        Effect effectCreateSmallExplosionInternal = createSmallExplosionInternal(f, f2, f3, i);
        if (effectCreateSmallExplosionInternal != null) {
            effectCreateSmallExplosionInternal.stripIndex = 11;
        }
        return effectCreateSmallExplosionInternal;
    }

    /* JADX INFO: renamed from: d */
    public Effect createSmallExplosionInternal(float f, float f2, float f3, int i) {
        setOnlyOnScreen();
        Effect effectCreateEffectInternal = createEffectInternal(f, f2, f3, EffectType.custom, false, EffectQuality.high);
        if (effectCreateEffectInternal != null) {
            effectCreateEffectInternal.stripIndex = 6;
            effectCreateEffectInternal.lifeTimer = 30.0f;
            effectCreateEffectInternal.lifeMax = effectCreateEffectInternal.lifeTimer;
            effectCreateEffectInternal.fadeIn = true;
            effectCreateEffectInternal.scaleFrom = 0.2f;
            effectCreateEffectInternal.scaleTo = 1.3f;
            effectCreateEffectInternal.drawLayer = (short) 1;
            if (i != 0) {
                effectCreateEffectInternal.lightingColorFilter = new LightingColorFilter(i, 0);
            }
        }
        return effectCreateEffectInternal;
    }

    /* JADX INFO: renamed from: d */
    public Effect createBloodEffect(float f, float f2, float f3) {
        Effect effectCreateBloodEffectInternal = createBloodEffectInternal(f, f2, f3, 0.3f, 0.7f);
        if (effectCreateBloodEffectInternal != null) {
            effectCreateBloodEffectInternal.stripIndex = 14;
            effectCreateBloodEffectInternal.frameIndex = Utility.getRandomIntInRange(0, 5);
            effectCreateBloodEffectInternal.physicsGravity = 0.5f;
        }
        return effectCreateBloodEffectInternal;
    }

    /* JADX INFO: renamed from: e */
    public Effect createBloodEffect2(float f, float f2, float f3) {
        Effect effectCreateBloodEffectInternal = createBloodEffectInternal(f, f2, f3, 1.0f, 1.0f);
        if (effectCreateBloodEffectInternal != null) {
        }
        return effectCreateBloodEffectInternal;
    }

    /* JADX INFO: renamed from: b */
    public Effect createBloodEffectInternal(float f, float f2, float f3, float f4, float f5) {
        setForceHighQuality();
        Effect effectCreateEffectInternal = createEffectInternal(f, f2, f3, EffectType.custom, false, EffectQuality.high);
        if (effectCreateEffectInternal != null) {
            effectCreateEffectInternal.effectKind = Effect.KIND_BLOOD;
            effectCreateEffectInternal.stripIndex = 12;
            effectCreateEffectInternal.frameIndex = Utility.getRandomIntInRange(0, 7);
            effectCreateEffectInternal.lifeTimer = Utility.randomFloatInRange(400.0f, 800.0f);
            effectCreateEffectInternal.lifeMax = effectCreateEffectInternal.lifeTimer - 150.0f;
            effectCreateEffectInternal.fadeIn = true;
            float fRandomFloatInRange = Utility.randomFloatInRange(0.6f, 1.0f);
            effectCreateEffectInternal.scaleFrom = fRandomFloatInRange;
            effectCreateEffectInternal.scaleTo = fRandomFloatInRange;
            effectCreateEffectInternal.drawLayer = (short) 2;
            effectCreateEffectInternal.useBounce = true;
            effectCreateEffectInternal.shadow = true;
            float fRandomFloatInRange2 = Utility.randomFloatInRange(-180.0f, 180.0f);
            float fRandomFloatInRange3 = Utility.randomFloatInRange(0.4f, 1.2f) * f4;
            effectCreateEffectInternal.velocityX = Utility.fastCos(fRandomFloatInRange2) * fRandomFloatInRange3;
            effectCreateEffectInternal.velocityY = Utility.fastSin(fRandomFloatInRange2) * fRandomFloatInRange3;
            effectCreateEffectInternal.velocityZ = Utility.randomFloatInRange(0.6f, 2.7f) * f5;
            effectCreateEffectInternal.rotation = Utility.randomFloatInRange(-180.0f, 180.0f);
            effectCreateEffectInternal.posZ += 1.0f;
        }
        return effectCreateEffectInternal;
    }

    /* JADX INFO: renamed from: f */
    public Effect createShockwaveEffect(float f, float f2, float f3) {
        Effect effectCreateEffectInternal = createEffectInternal(f, f2, f3, EffectType.custom, false, EffectQuality.low);
        if (effectCreateEffectInternal != null) {
            effectCreateEffectInternal.stripIndex = 8;
            effectCreateEffectInternal.lifeTimer = 480.0f;
            effectCreateEffectInternal.lifeMax = effectCreateEffectInternal.lifeTimer;
            effectCreateEffectInternal.fadeIn = false;
            effectCreateEffectInternal.drawLayer = (short) 1;
            effectCreateEffectInternal.animateFrames = true;
            effectCreateEffectInternal.currentFrame = 0.0f;
            effectCreateEffectInternal.scaleFrom = 0.5f;
            effectCreateEffectInternal.scaleFrom = 1.0f;
            int randomIntInRange = Utility.getRandomIntInRange(0, 100);
            if (randomIntInRange > 80) {
                effectCreateEffectInternal.animateFrameSpeed = Utility.randomFloatInRange(0.1f, 0.15f);
                effectCreateEffectInternal.animateFrameEnd = 15;
            } else if (randomIntInRange > 60) {
                effectCreateEffectInternal.animateFrameSpeed = Utility.randomFloatInRange(0.06f, 0.16f);
                effectCreateEffectInternal.animateFramePingPong = true;
                effectCreateEffectInternal.animateFrameEnd = 6;
                effectCreateEffectInternal.fadeIn = true;
            } else {
                effectCreateEffectInternal.animateFrameSpeed = Utility.randomFloatInRange(0.06f, 0.16f);
                effectCreateEffectInternal.animateFramePingPong = true;
                effectCreateEffectInternal.animateFrameEnd = 3;
                effectCreateEffectInternal.fadeIn = true;
            }
        }
        return effectCreateEffectInternal;
    }

    /* JADX INFO: renamed from: b */
    public void setOverrideEffectQuality(EffectQuality effectQuality) {
        this.overrideEffectQuality = effectQuality;
    }

    /* JADX INFO: renamed from: a */
    public void setOnlyOnScreen() {
        this.onlyOnScreen = true;
    }

    /* JADX INFO: renamed from: b */
    public void setForceHighQuality() {
        this.forceHighQuality = true;
    }

    /* JADX INFO: renamed from: a */
    public Effect createEffect(float f, float f2, float f3, EffectType effectType, boolean z, EffectQuality effectQuality) {
        Effect effectCreateEffectInternal = createEffectInternal(f, f2, f3, effectType, z, effectQuality);
        if (effectCreateEffectInternal != null) {
            effectCreateEffectInternal.isUiEffect = true;
        }
        return effectCreateEffectInternal;
    }

    /* JADX INFO: renamed from: b */
    public Effect createEffectInternal(float f, float f2, float f3, EffectType effectType, boolean z, EffectQuality effectQuality) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.overrideEffectQuality != null) {
            effectQuality = this.overrideEffectQuality;
            this.overrideEffectQuality = null;
        }
        boolean z2 = this.forceHighQuality;
        this.forceHighQuality = false;
        if (this.onlyOnScreen) {
            this.onlyOnScreen = false;
            if (!gameEngine.extendedVisibleWorldRect.b(f, f2)) {
                return null;
            }
        }
        if (!z && gameEngine.tileMap != null && !gameEngine.tileMap.isWorldPointVisibleForTeam(f, f2, gameEngine.playerTeam)) {
            return null;
        }
        if (gameEngine.bufferedVisibleWorldRectF.b(f, f2)) {
            if (effectQuality == EffectQuality.verylow) {
                effectQuality = EffectQuality.low;
            } else if (effectQuality == EffectQuality.low) {
                effectQuality = EffectQuality.high;
            } else if (effectQuality == EffectQuality.high) {
                effectQuality = EffectQuality.veryhigh;
            }
        } else if (z2 || gameEngine.extendedVisibleWorldRect.b(f, f2)) {
        }
        Effect newEffect = getNewEffect(effectQuality);
        if (newEffect == null) {
            return null;
        }
        newEffect.free();
        newEffect.priority = effectQuality;
        newEffect.stripIndex = 0;
        newEffect.isCentered = true;
        newEffect.posX = f;
        newEffect.posY = f2;
        newEffect.posZ = f3;
        newEffect.alpha = 1.0f;
        if (effectType == EffectType.hitGround || effectType == EffectType.playerLand || effectType == EffectType.playerJump) {
            newEffect.frameIndex = 7;
            newEffect.lifeTimer = 12.0f;
            newEffect.fadeIn = true;
            newEffect.velocityY = -0.3f;
            newEffect.alpha = 0.7f;
            if (effectType == EffectType.playerJump) {
                newEffect.frameIndex = 3;
                newEffect.velocityY = -0.7f;
                newEffect.lifeTimer = 24.0f;
                newEffect.alpha = 0.7f;
            }
            if (effectType == EffectType.playerLand) {
                newEffect.frameIndex = 4;
                newEffect.lifeTimer = 15.0f;
                newEffect.alpha = 0.4f;
            }
        }
        if (effectType == EffectType.teleport) {
            newEffect.frameIndex = 1;
            newEffect.lifeTimer = 25.0f;
            newEffect.fadeIn = true;
        }
        if (effectType == EffectType.gemCollect) {
            newEffect.frameIndex = 5;
            newEffect.lifeTimer = 42.0f;
            newEffect.fadeIn = true;
            newEffect.velocityY = 0.1f;
            newEffect.alpha = 2.0f;
        }
        if (effectType == EffectType.keyDoorOpen) {
            newEffect.frameIndex = 6;
            newEffect.lifeTimer = 39.0f;
            newEffect.fadeIn = true;
            newEffect.velocityY = 0.1f;
            newEffect.alpha = 2.0f;
        }
        if (effectType == EffectType.blood) {
            newEffect.frameIndex = 14;
            newEffect.lifeTimer = 39.0f;
            newEffect.fadeIn = true;
            newEffect.velocityY = 0.1f;
            newEffect.alpha = 0.7f;
        }
        newEffect.lifeMax = newEffect.lifeTimer;
        return newEffect;
    }

    /* JADX INFO: renamed from: a */
    public void loadContent(Context context) {
        int i;
        GameEngine gameEngine = GameEngine.getInstance();
        this.linePaint.a(130, 200, 0, 0);
        this.linePaint.a(true);
        this.linePaint.a(2.0f);
        this.linePaint.a(Paint.Cap.ROUND);
        if (GameEngine.isPCOrIOSVersion) {
            this.linePaint.a(3.0f);
        }
        effectTemplates = new SpriteSheet[20];
        SpriteSheet spriteSheet = new SpriteSheet();
        spriteSheet.frameWidth = 25;
        spriteSheet.frameHeight = 25;
        spriteSheet.offsetX = 1;
        spriteSheet.offsetY = 1;
        spriteSheet.stepX = 26;
        spriteSheet.stepY = 26;
        spriteSheet.texture = gameEngine.renderGraphicsEngine.a(R.drawable.effects, true);
        spriteSheet.name = "effects";
        spriteSheet.createOutline();
        effectTemplates[0] = spriteSheet;
        SpriteSheet spriteSheet2 = new SpriteSheet();
        spriteSheet2.frameWidth = 39;
        spriteSheet2.frameHeight = 40;
        spriteSheet2.offsetX = 1;
        spriteSheet2.offsetY = 1;
        spriteSheet2.stepX = 40;
        spriteSheet2.stepY = 41;
        spriteSheet2.texture = gameEngine.renderGraphicsEngine.a(R.drawable.explode_big, true);
        spriteSheet2.name = "explode_big";
        effectTemplates[1] = spriteSheet2;
        SpriteSheet spriteSheet3 = new SpriteSheet();
        spriteSheet3.singleFrame = true;
        spriteSheet3.texture = gameEngine.renderGraphicsEngine.a(R.drawable.light_50, true);
        spriteSheet3.name = "light_50";
        effectTemplates[2] = spriteSheet3;
        SpriteSheet spriteSheet4 = new SpriteSheet();
        spriteSheet4.frameWidth = 20;
        spriteSheet4.frameHeight = 25;
        spriteSheet4.offsetX = 0;
        spriteSheet4.offsetY = 0;
        spriteSheet4.stepX = 20;
        spriteSheet4.stepY = 25;
        spriteSheet4.texture = gameEngine.renderGraphicsEngine.a(R.drawable.flame, true);
        spriteSheet4.name = "flame";
        effectTemplates[3] = spriteSheet4;
        SpriteSheet spriteSheet5 = new SpriteSheet();
        spriteSheet5.frameWidth = 20;
        spriteSheet5.frameHeight = 25;
        spriteSheet5.offsetX = 0;
        spriteSheet5.offsetY = 0;
        spriteSheet5.stepX = spriteSheet5.frameWidth;
        spriteSheet5.stepY = spriteSheet5.frameHeight;
        spriteSheet5.texture = gameEngine.renderGraphicsEngine.a(R.drawable.dust, true);
        spriteSheet5.name = "dust";
        effectTemplates[4] = spriteSheet5;
        SpriteSheet spriteSheet6 = new SpriteSheet();
        spriteSheet6.frameWidth = 50;
        spriteSheet6.frameHeight = 40;
        spriteSheet6.offsetX = 0;
        spriteSheet6.offsetY = 0;
        spriteSheet6.stepX = spriteSheet6.frameWidth;
        spriteSheet6.stepY = spriteSheet6.frameHeight;
        spriteSheet6.texture = gameEngine.renderGraphicsEngine.a(R.drawable.smoke_black, true);
        spriteSheet6.name = "smoke_black";
        spriteSheet6.createOutline();
        effectTemplates[5] = spriteSheet6;
        SpriteSheet spriteSheet7 = new SpriteSheet();
        spriteSheet7.frameWidth = 50;
        spriteSheet7.frameHeight = 50;
        spriteSheet7.offsetX = 0;
        spriteSheet7.offsetY = 0;
        spriteSheet7.stepX = spriteSheet7.frameWidth;
        spriteSheet7.stepY = spriteSheet7.frameHeight;
        spriteSheet7.texture = gameEngine.renderGraphicsEngine.a(R.drawable.shockwave, true);
        spriteSheet7.name = "shockwave";
        effectTemplates[6] = spriteSheet7;
        SpriteSheet spriteSheet8 = new SpriteSheet();
        spriteSheet8.frameWidth = 20;
        spriteSheet8.frameHeight = 20;
        spriteSheet8.offsetX = 0;
        spriteSheet8.offsetY = 0;
        spriteSheet8.stepX = spriteSheet8.frameWidth;
        spriteSheet8.stepY = spriteSheet8.frameHeight;
        spriteSheet8.texture = gameEngine.renderGraphicsEngine.a(R.drawable.fire, true);
        spriteSheet8.name = "fire";
        effectTemplates[7] = spriteSheet8;
        SpriteSheet spriteSheet9 = new SpriteSheet();
        spriteSheet9.frameWidth = 20;
        spriteSheet9.frameHeight = 30;
        spriteSheet9.stepX = spriteSheet9.frameWidth + 2;
        spriteSheet9.stepY = spriteSheet9.frameHeight;
        spriteSheet9.texture = gameEngine.renderGraphicsEngine.a(R.drawable.lava_bubble, true);
        spriteSheet9.name = "lava_bubble";
        effectTemplates[8] = spriteSheet9;
        SpriteSheet spriteSheet10 = new SpriteSheet();
        spriteSheet10.frameWidth = 28;
        spriteSheet10.frameHeight = 28;
        spriteSheet10.offsetX = 0;
        spriteSheet10.offsetY = 0;
        spriteSheet10.stepX = spriteSheet10.frameWidth + 1;
        spriteSheet10.stepY = spriteSheet10.frameHeight + 1;
        spriteSheet10.texture = gameEngine.renderGraphicsEngine.a(R.drawable.effects2, true);
        spriteSheet10.name = "effects2";
        effectTemplates[9] = spriteSheet10;
        SpriteSheet spriteSheet11 = new SpriteSheet();
        spriteSheet11.frameWidth = 20;
        spriteSheet11.frameHeight = 25;
        spriteSheet11.offsetX = 0;
        spriteSheet11.offsetY = 0;
        spriteSheet11.stepX = 20;
        spriteSheet11.stepY = 25;
        spriteSheet11.texture = gameEngine.renderGraphicsEngine.a(R.drawable.plasma_shot, true);
        spriteSheet11.name = "plasma_shot";
        effectTemplates[10] = spriteSheet11;
        SpriteSheet spriteSheet12 = new SpriteSheet();
        spriteSheet12.frameWidth = 104;
        spriteSheet12.frameHeight = 104;
        spriteSheet12.offsetX = 0;
        spriteSheet12.offsetY = 0;
        spriteSheet12.stepX = spriteSheet12.frameWidth;
        spriteSheet12.stepY = spriteSheet12.frameHeight;
        spriteSheet12.texture = gameEngine.renderGraphicsEngine.a(R.drawable.shockwave_large, true);
        spriteSheet12.name = "shockwave_large";
        effectTemplates[11] = spriteSheet12;
        SpriteSheet spriteSheet13 = new SpriteSheet();
        spriteSheet13.frameWidth = 20;
        spriteSheet13.frameHeight = 20;
        spriteSheet13.offsetX = 0;
        spriteSheet13.offsetY = 0;
        spriteSheet13.stepX = spriteSheet13.frameWidth;
        spriteSheet13.stepY = spriteSheet13.frameHeight;
        spriteSheet13.texture = gameEngine.renderGraphicsEngine.a(R.drawable.explode_bits, true);
        spriteSheet13.name = "explode_bits";
        spriteSheet13.createOutline();
        effectTemplates[12] = spriteSheet13;
        SpriteSheet spriteSheet14 = new SpriteSheet();
        spriteSheet14.frameWidth = 39;
        spriteSheet14.frameHeight = 40;
        spriteSheet14.offsetX = 1;
        spriteSheet14.offsetY = 1;
        spriteSheet14.stepX = 40;
        spriteSheet14.stepY = 41;
        spriteSheet14.texture = gameEngine.renderGraphicsEngine.a(R.drawable.explode_big2, true);
        spriteSheet14.name = "explode_big2";
        effectTemplates[13] = spriteSheet14;
        SpriteSheet spriteSheet15 = new SpriteSheet();
        spriteSheet15.frameWidth = 20;
        spriteSheet15.frameHeight = 20;
        spriteSheet15.offsetX = 0;
        spriteSheet15.offsetY = 0;
        spriteSheet15.stepX = spriteSheet15.frameWidth;
        spriteSheet15.stepY = spriteSheet15.frameHeight;
        spriteSheet15.texture = gameEngine.renderGraphicsEngine.a(R.drawable.explode_bits_bug, true);
        spriteSheet15.name = "explode_bits_bug";
        spriteSheet15.createOutline();
        effectTemplates[14] = spriteSheet15;
        SpriteSheet spriteSheet16 = new SpriteSheet();
        spriteSheet16.frameWidth = 20;
        spriteSheet16.frameHeight = 20;
        spriteSheet16.offsetX = 0;
        spriteSheet16.offsetY = 0;
        spriteSheet16.stepX = spriteSheet16.frameWidth;
        spriteSheet16.stepY = spriteSheet16.frameHeight;
        spriteSheet16.texture = gameEngine.renderGraphicsEngine.a(R.drawable.projectiles, true);
        spriteSheet16.name = "projectiles";
        spriteSheet16.createOutline();
        effectTemplates[15] = spriteSheet16;
        SpriteSheet spriteSheet17 = new SpriteSheet();
        spriteSheet17.frameWidth = 20;
        spriteSheet17.frameHeight = 20;
        spriteSheet17.offsetX = 0;
        spriteSheet17.offsetY = 0;
        spriteSheet17.stepX = spriteSheet17.frameWidth;
        spriteSheet17.stepY = spriteSheet17.frameHeight;
        spriteSheet17.texture = gameEngine.renderGraphicsEngine.a(R.drawable.projectiles2, true);
        spriteSheet17.name = "projectiles2";
        spriteSheet17.createOutline();
        effectTemplates[16] = spriteSheet17;
        SpriteSheet spriteSheet18 = new SpriteSheet();
        spriteSheet18.frameWidth = 30;
        spriteSheet18.frameHeight = 30;
        spriteSheet18.offsetX = 0;
        spriteSheet18.offsetY = 0;
        spriteSheet18.stepX = spriteSheet18.frameWidth + 1;
        spriteSheet18.stepY = spriteSheet18.frameHeight + 1;
        spriteSheet18.texture = gameEngine.renderGraphicsEngine.a(R.drawable.effects3, true);
        spriteSheet18.name = "effects3";
        effectTemplates[17] = spriteSheet18;
        SpriteSheet spriteSheet19 = new SpriteSheet();
        spriteSheet19.frameWidth = 50;
        spriteSheet19.frameHeight = 40;
        spriteSheet19.offsetX = 0;
        spriteSheet19.offsetY = 0;
        spriteSheet19.stepX = spriteSheet19.frameWidth;
        spriteSheet19.stepY = spriteSheet19.frameHeight;
        spriteSheet19.texture = gameEngine.renderGraphicsEngine.a(R.drawable.smoke_white, true);
        spriteSheet19.name = "smoke_white";
        spriteSheet19.createOutline();
        effectTemplates[18] = spriteSheet19;
        SpriteSheet spriteSheet20 = new SpriteSheet();
        spriteSheet20.frameWidth = 56;
        spriteSheet20.frameHeight = 56;
        spriteSheet20.offsetX = 0;
        spriteSheet20.offsetY = 0;
        spriteSheet20.stepX = spriteSheet20.frameWidth;
        spriteSheet20.stepY = spriteSheet20.frameHeight;
        spriteSheet20.texture = gameEngine.renderGraphicsEngine.a(R.drawable.shockwave2, true);
        spriteSheet20.name = "shockwave2";
        spriteSheet20.createOutline();
        effectTemplates[19] = spriteSheet20;
        if (GameEngine.isPC()) {
            i = 500;
            this.maxParticlesVeryLow = 90;
            this.maxParticlesLow = 210;
        } else {
            i = 350;
            this.maxParticlesVeryLow = 90;
            this.maxParticlesLow = 170;
        }
        effects = new Effect[i];
        this.maxEffectsVeryLow = i - 60;
        this.maxEffectsLow = i - 30;
        this.maxEffectsHigh = i - 20;
        this.maxEffectsVeryHigh = i - 10;
        for (int i2 = 0; i2 < effects.length; i2++) {
            effects[i2] = new Effect(this);
        }
    }

    /* JADX INFO: renamed from: a */
    public int findEffectTemplateIndex(String str) {
        for (int i = 0; i < effectTemplates.length; i++) {
            if (effectTemplates[i] != null) {
                if (effectTemplates[i].name != null && effectTemplates[i].name.equalsIgnoreCase(str)) {
                    return i;
                }
                if ((VariableScope.nullOrMissingString + i).equals(str)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /* JADX INFO: renamed from: a */
    public void update(float f) {
        GameEngine gameEngine = GameEngine.getInstance();
        Effect[] effectArr = effects;
        for (int i = 0; i < nextFreeEffect; i++) {
            Effect effect = effectArr[i];
            if (effect.isActive && !effect.isUiEffect) {
                effect.draw(f);
            }
        }
        if (useStrictCounting) {
            while (nextFreeEffect > 0 && !effectArr[nextFreeEffect - 1].isActive) {
                nextFreeEffect--;
            }
        }
        this.lastUpdate += f;
        if (this.lastUpdate > 10.0f) {
            this.lastUpdate = 0.0f;
            gameEngine.tileMap.setCursorTileIndexFromWorldPoint(gameEngine.viewpointXInt + Utility.getRandomIntInRange(0, (int) gameEngine.visibleWorldWidth), gameEngine.viewpointYInt + Utility.getRandomIntInRange(0, (int) gameEngine.visibleWorldHeight));
            int i2 = gameEngine.tileMap.cursorTileX;
            int i3 = gameEngine.tileMap.cursorTileY;
            MapTile tileAt = gameEngine.tileMap.getTileAt(i2, i3);
            if (tileAt != null && tileAt.isLava && !tileAt.isCliff) {
                gameEngine.tileMap.setCursorTileIndexFromTileIndex(i2, i3);
                createShockwaveEffect(gameEngine.tileMap.cursorTileX + 10, (gameEngine.tileMap.cursorTileY - 10) + 10, 0.0f);
            }
        }
    }

    /* JADX INFO: renamed from: b */
    public int getEffectCount(float f) {
        GameEngine gameEngine = GameEngine.getInstance();
        int i = 0;
        for (int i2 = 0; i2 < this.activeEffectLayerFlags.length; i2++) {
            this.activeEffectLayerFlags[i2] = false;
        }
        for (int i3 = 0; i3 < nextFreeEffect; i3++) {
            Effect effect = effects[i3];
            if (effect.isActive) {
                if (!this.activeEffectLayerFlags[effect.drawLayer]) {
                    this.activeEffectLayerFlags[effect.drawLayer] = true;
                }
                if (effect.isUiEffect) {
                    effect.draw(f);
                }
                if (effect.shadow && effect.update(gameEngine, true)) {
                    i++;
                }
            }
        }
        return i;
    }

    /* JADX INFO: renamed from: a */
    public int drawEffect(float f, int i) {
        if (!this.activeEffectLayerFlags[i]) {
            return 0;
        }
        GameEngine gameEngine = GameEngine.getInstance();
        int i2 = 0;
        Effect[] effectArr = effects;
        for (int i3 = 0; i3 < nextFreeEffect; i3++) {
            Effect effect = effectArr[i3];
            if (effect.isActive && effect.drawLayer == i && effect.update(gameEngine, false)) {
                i2++;
            }
        }
        return i2;
    }

    /* JADX INFO: renamed from: a */
    public void setBitmapQuality(boolean z) {
        if (z) {
            return;
        }
        for (int i = 0; i < effects.length; i++) {
            Effect effect = effects[i];
            if (effect.isActive) {
                effect.isActive = false;
                this.activeEffectsCount--;
            }
        }
        if (this.activeEffectsCount != 0) {
            GameEngine.logErrorColored("EffectEngine::removeAll: effectListActiveSize == " + this.activeEffectsCount);
        }
        nextFreeEffect = 0;
    }
}
