package com.corrodinggames.rts.game.units.custom;

import android.graphics.Color;
import android.graphics.LightingColorFilter;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.GameObject;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.effects.*;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;
import com.corrodinggames.rts.gameFramework.utility.IniFile;

import java.util.ArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.custom.ay */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/custom/ay.class */
public class EffectTemplate {
    public static final EffectTemplate defaultEffectTemplate = new EffectTemplate("default");
    public String name;
    private BuiltInEffectType builtInEffect;
    public SpriteSheet imageStrip;
    public boolean createWhenOffscreen;
    public boolean createWhenZoomedOut;
    public boolean createWhenOverLiquid;
    public boolean createWhenOverLand;
    public float spawnChance;
    CustomUnitSpawnList ifSpawnFailsEmitEffects;
    public float life;
    public float lifeRandom;
    public boolean showInFog;
    public float xOffsetRelative;
    public float yOffsetRelative;
    public float hOffset;
    public boolean alwayStartDirAtZero;
    public float pivotOffset;
    public float pivotOffsetRandom;
    public float dirOffset;
    public float xOffsetRelativeRandom;
    public float yOffsetRelativeRandom;
    public float hOffsetRandom;
    public float dirOffsetRandom;
    public float xOffsetAbsolute;
    public float yOffsetAbsolute;
    public float xOffsetAbsoluteRandom;
    public float yOffsetAbsoluteRandom;
    public float xSpeedRelative;
    public float ySpeedRelative;
    public float hSpeed;
    public float dirSpeed;
    public float xSpeedRelativeRandom;
    public float ySpeedRelativeRandom;
    public float hSpeedRandom;
    public float dirSpeedRandom;
    public float xSpeedAbsolute;
    public float ySpeedAbsolute;
    public float xSpeedAbsoluteRandom;
    public float ySpeedAbsoluteRandom;
    public EffectQuality priority;
    public float scaleTo;
    public float scaleFrom;
    public float alpha;
    public int color;
    public LightingColorFilter cachedLightingColorFilter;
    public float teamColorRatio;
    public boolean shadow;
    public short drawLayer;
    public float fadeInTime;
    public boolean fadeOut;
    public float delayedStartTimer;
    public float delayedStartTimerRandom;
    public int frameIndex;
    public int frameIndexRandom;
    public int stripIndex;
    public boolean attachedToUnit;
    public boolean liveAfterAttachedDies;
    public boolean atmospheric;
    public boolean physics;
    public float physicsGravity;
    public int animateFrameStart;
    public int animateFrameEnd;
    public int animateFrameStartRandomAdd;
    public boolean animateFramePingPong;
    public boolean animateFrameLooping;
    public float animateFrameSpeed;
    public float animateFrameSpeedRandom;
    public CustomUnitSpawnList alsoEmitEffects;
    public CustomUnitSpawnList alsoEmitEffectsOnDeath;
    public CustomUnitSpawnList trailEffect;
    public float trailEffectRate;
    public SoundList alsoPlaySound;
    public static ArrayList fields;

    public EffectTemplate(BuiltInEffectType builtInEffectType) {
        this.builtInEffect = null;
        this.spawnChance = 1.0f;
        this.life = 200.0f;
        this.priority = EffectQuality.high;
        this.scaleTo = 1.0f;
        this.scaleFrom = 1.0f;
        this.alpha = 1.0f;
        this.color = -1;
        this.teamColorRatio = 0.0f;
        this.drawLayer = (short) 2;
        this.physicsGravity = 1.0f;
        this.builtInEffect = builtInEffectType;
    }

    EffectTemplate(String str) {
        this.builtInEffect = null;
        this.spawnChance = 1.0f;
        this.life = 200.0f;
        this.priority = EffectQuality.high;
        this.scaleTo = 1.0f;
        this.scaleFrom = 1.0f;
        this.alpha = 1.0f;
        this.color = -1;
        this.teamColorRatio = 0.0f;
        this.drawLayer = (short) 2;
        this.physicsGravity = 1.0f;
        this.name = str;
    }

    public Effect a(float f, float f2, float f3, float f4, GameObject gameObject, int i, short s) {
        BaseUnit baseUnit;
        Effect effectCreateSmallExplosion;
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.spawnChance < 1.0f && Utility.randomFloatInRange(0.0f, 1.0f) > this.spawnChance) {
            if (i < 5 && this.ifSpawnFailsEmitEffects != null) {
                this.ifSpawnFailsEmitEffects.a(f, f2, f3, f4, gameObject, i + 1, s);
                return null;
            }
            return null;
        }
        if (this.builtInEffect != null) {
            if (this.builtInEffect == BuiltInEffectType.small) {
                effectCreateSmallExplosion = gameEngine.effectManager.createFlameEffect(f, f2, f3, f4);
            } else if (this.builtInEffect == BuiltInEffectType.medium) {
                effectCreateSmallExplosion = gameEngine.effectManager.createFlameEffect2(f, f2, f3, f4, 0);
                if (effectCreateSmallExplosion != null) {
                    effectCreateSmallExplosion.scaleFrom = 0.75f;
                    effectCreateSmallExplosion.scaleTo = 0.75f;
                }
            } else if (this.builtInEffect == BuiltInEffectType.large) {
                effectCreateSmallExplosion = gameEngine.effectManager.createFlameEffect2(f, f2, f3, f4, 0);
            } else if (this.builtInEffect == BuiltInEffectType.smoke) {
                effectCreateSmallExplosion = gameEngine.effectManager.createMuzzleFlash(f, f2, f3, f4, 0);
            } else if (this.builtInEffect == BuiltInEffectType.shockwave) {
                effectCreateSmallExplosion = gameEngine.effectManager.createSmallExplosionInternal(f, f2, f3, 0);
            } else if (this.builtInEffect == BuiltInEffectType.largeExplosion) {
                gameEngine.effectManager.createExplosion(f, f2, f3);
                effectCreateSmallExplosion = null;
            } else if (this.builtInEffect == BuiltInEffectType.smallExplosion) {
                effectCreateSmallExplosion = gameEngine.effectManager.createSmallExplosion(f, f2, f3);
            } else if (this.builtInEffect == BuiltInEffectType.resourcePoolSmoke) {
                EffectEmitter.createDefaultFireEmitter(f, f2).startColorOverride = -6684775;
                EffectEmitter effectEmitterB = EffectEmitter.createAlternateFireEmitter(f, f2);
                effectEmitterB.duration = 500.0f;
                effectEmitterB.startColorOverride = -6684775;
                gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                Effect effectCreateSmallExplosion2 = gameEngine.effectManager.createSmallExplosion(f, f2, f3, -1127220);
                if (effectCreateSmallExplosion2 != null) {
                    effectCreateSmallExplosion2.scaleFrom = 0.15f;
                    effectCreateSmallExplosion2.scaleTo = 1.0f;
                    effectCreateSmallExplosion2.drawLayer = (short) 2;
                    effectCreateSmallExplosion2.lifeTimer = 35.0f;
                    effectCreateSmallExplosion2.lifeMax = effectCreateSmallExplosion2.lifeTimer;
                    effectCreateSmallExplosion2.delayedStartTimer = 0.0f;
                    effectCreateSmallExplosion2.startColor = -13378253;
                }
                effectCreateSmallExplosion = null;
            } else {
                if (this.builtInEffect == BuiltInEffectType.noneExplosion) {
                    return null;
                }
                throw new RuntimeException("Unhandled built-in type:" + this.builtInEffect);
            }
            if (effectCreateSmallExplosion == null) {
                return null;
            }
            effectCreateSmallExplosion.drawLayer = (short) 2;
            if (gameObject != null) {
                EffectManager.attachEffectToGameObject(effectCreateSmallExplosion, gameObject);
            }
            return effectCreateSmallExplosion;
        }
        if (!this.createWhenZoomedOut && !gameEngine.shouldDrawHighDetailEffects) {
            return null;
        }
        if (!this.createWhenOverLiquid && GameViewUtils.d(f, f2)) {
            return null;
        }
        if (!this.createWhenOverLand && !GameViewUtils.d(f, f2)) {
            return null;
        }
        if (this.createWhenOffscreen) {
            gameEngine.effectManager.setForceHighQuality();
        } else {
            gameEngine.effectManager.setOnlyOnScreen();
        }
        boolean z = this.showInFog;
        boolean z2 = false;
        if (!z && this.attachedToUnit) {
            z2 = true;
            z = true;
        }
        Effect effectCreateEffectInternal = gameEngine.effectManager.createEffectInternal(f, f2, f3, EffectType.custom, z, this.priority);
        if (effectCreateEffectInternal == null) {
            return null;
        }
        effectCreateEffectInternal.template = this;
        effectCreateEffectInternal.emitRecursionDepth = (short) (s + 1);
        if (z2 && !this.showInFog) {
            effectCreateEffectInternal.showInFog = false;
        }
        effectCreateEffectInternal.lifeTimer = this.life;
        effectCreateEffectInternal.lifeTimer += a(this.lifeRandom);
        effectCreateEffectInternal.lifeMax = effectCreateEffectInternal.lifeTimer;
        effectCreateEffectInternal.stripIndex = this.stripIndex;
        if (this.imageStrip != null) {
        }
        effectCreateEffectInternal.frameIndex = this.frameIndex;
        if (this.frameIndexRandom != 0) {
            effectCreateEffectInternal.frameIndex += Utility.getRandomIntInRange(-this.frameIndexRandom, this.frameIndexRandom);
            if (effectCreateEffectInternal.frameIndex < 0) {
                effectCreateEffectInternal.frameIndex = 0;
            }
        }
        float fA = f4 + this.pivotOffset + a(this.pivotOffsetRandom);
        if (this.alwayStartDirAtZero) {
            effectCreateEffectInternal.rotation = 0.0f;
        } else {
            effectCreateEffectInternal.rotation = fA;
        }
        effectCreateEffectInternal.rotation += this.dirOffset;
        effectCreateEffectInternal.rotation += a(this.dirOffsetRandom);
        if (this.xOffsetAbsoluteRandom != 0.0f || this.yOffsetAbsoluteRandom != 0.0f || this.xOffsetAbsolute != 0.0f || this.yOffsetAbsolute != 0.0f) {
            float fA2 = this.xOffsetAbsolute + a(this.xOffsetAbsoluteRandom);
            float fA3 = this.yOffsetAbsolute + a(this.yOffsetAbsoluteRandom);
            effectCreateEffectInternal.posX += fA2;
            effectCreateEffectInternal.posY += fA3;
        }
        if (this.xOffsetRelativeRandom != 0.0f || this.yOffsetRelativeRandom != 0.0f || this.xOffsetRelative != 0.0f || this.yOffsetRelative != 0.0f) {
            float fFastCos = Utility.fastCos(fA);
            float fFastSin = Utility.fastSin(fA);
            float fA4 = this.xOffsetRelative + a(this.xOffsetRelativeRandom);
            float fA5 = this.yOffsetRelative + a(this.yOffsetRelativeRandom);
            effectCreateEffectInternal.posX += (fFastCos * fA5) - (fFastSin * fA4);
            effectCreateEffectInternal.posY += (fFastSin * fA5) + (fFastCos * fA4);
        }
        effectCreateEffectInternal.posZ += this.hOffset + a(-this.hOffsetRandom, this.hOffsetRandom);
        effectCreateEffectInternal.isCentered = true;
        effectCreateEffectInternal.fadeIn = true;
        effectCreateEffectInternal.drawLayer = this.drawLayer;
        effectCreateEffectInternal.scaleFrom = this.scaleFrom;
        effectCreateEffectInternal.scaleTo = this.scaleTo;
        effectCreateEffectInternal.alpha = this.alpha;
        effectCreateEffectInternal.startColor = this.color;
        effectCreateEffectInternal.lightingColorFilter = this.cachedLightingColorFilter;
        if (this.teamColorRatio != 0.0f && gameObject != null) {
            PlayerTeam playerTeam = null;
            if (gameObject instanceof BaseUnit) {
                playerTeam = ((BaseUnit) gameObject).team;
            }
            if ((gameObject instanceof Projectile) && (baseUnit = ((Projectile) gameObject).sourceUnit) != null) {
                playerTeam = baseUnit.team;
            }
            if (playerTeam != null) {
                float f5 = 1.0f - this.teamColorRatio;
                int iA = Color.a(effectCreateEffectInternal.startColor);
                int iB = (int) (Color.b(effectCreateEffectInternal.startColor) * f5);
                int iC = (int) (Color.c(effectCreateEffectInternal.startColor) * f5);
                int iD = (int) (Color.d(effectCreateEffectInternal.startColor) * f5);
                int teamColorArgb = playerTeam.getTeamColorArgb();
                effectCreateEffectInternal.startColor = Color.a(iA, Utility.distance((int) (iB + (Color.b(teamColorArgb) * this.teamColorRatio)), 0, 255), Utility.distance((int) (iC + (Color.c(teamColorArgb) * this.teamColorRatio)), 0, 255), Utility.distance((int) (iD + (Color.d(teamColorArgb) * this.teamColorRatio)), 0, 255));
                if (GameEngine.isAndroidPlatform()) {
                    effectCreateEffectInternal.lightingColorFilter = new LightingColorFilter(effectCreateEffectInternal.startColor, 0);
                }
            }
        }
        if (this.fadeInTime != 0.0f) {
            effectCreateEffectInternal.fadeOut = true;
            effectCreateEffectInternal.fadeDuration = this.fadeInTime;
        }
        effectCreateEffectInternal.shadow = this.shadow;
        effectCreateEffectInternal.fadeIn = this.fadeOut;
        effectCreateEffectInternal.delayedStartTimer = this.delayedStartTimer;
        effectCreateEffectInternal.delayedStartTimer += a(-this.delayedStartTimerRandom, this.delayedStartTimerRandom);
        effectCreateEffectInternal.useGravity = this.atmospheric;
        effectCreateEffectInternal.useBounce = this.physics;
        effectCreateEffectInternal.physicsGravity = this.physicsGravity;
        effectCreateEffectInternal.priority = this.priority;
        effectCreateEffectInternal.velocityX = this.xSpeedAbsolute + a(this.xSpeedAbsoluteRandom);
        effectCreateEffectInternal.velocityY = this.ySpeedAbsolute + a(this.ySpeedAbsoluteRandom);
        if (this.xSpeedRelative != 0.0f || this.ySpeedRelative != 0.0f || this.xSpeedRelativeRandom != 0.0f || this.ySpeedRelativeRandom != 0.0f) {
            float fFastCos2 = Utility.fastCos(fA);
            float fFastSin2 = Utility.fastSin(fA);
            float fA6 = this.xSpeedRelative + a(this.xSpeedRelativeRandom);
            float fA7 = this.ySpeedRelative + a(this.ySpeedRelativeRandom);
            effectCreateEffectInternal.velocityX += (fFastCos2 * fA7) - (fFastSin2 * fA6);
            effectCreateEffectInternal.velocityY += (fFastSin2 * fA7) + (fFastCos2 * fA6);
        }
        effectCreateEffectInternal.velocityZ = this.hSpeed + a(this.hSpeedRandom);
        effectCreateEffectInternal.rotationSpeed = this.dirSpeed + a(this.dirSpeedRandom);
        if (this.animateFrameStart != this.animateFrameEnd) {
            effectCreateEffectInternal.animateFrames = true;
        }
        effectCreateEffectInternal.animateFrameStart = this.animateFrameStart;
        if (this.animateFrameStartRandomAdd != 0) {
            effectCreateEffectInternal.animateFrameStart += Utility.getRandomIntInRange(0, this.animateFrameStartRandomAdd);
        }
        effectCreateEffectInternal.animateFrameEnd = this.animateFrameEnd;
        effectCreateEffectInternal.currentFrame = this.animateFrameStart;
        effectCreateEffectInternal.animateFramePingPong = this.animateFramePingPong;
        effectCreateEffectInternal.animateFrameLooping = this.animateFrameLooping;
        effectCreateEffectInternal.animateFrameSpeed = this.animateFrameSpeed;
        effectCreateEffectInternal.animateFrameSpeed += a(this.animateFrameSpeedRandom);
        if (gameObject != null && this.attachedToUnit) {
            EffectManager.attachEffectToGameObject(effectCreateEffectInternal, gameObject);
        }
        if (this.alsoPlaySound != null) {
            this.alsoPlaySound.a(f, f2, 1.0f);
        }
        if (i < 5 && this.alsoEmitEffects != null) {
            this.alsoEmitEffects.a(f, f2, f3, fA, gameObject, i + 1, (short) 0);
        }
        return effectCreateEffectInternal;
    }

    public final float a(float f) {
        if (f == 0.0f) {
            return 0.0f;
        }
        return Utility.randomFloatInRange(-f, f);
    }

    public final float a(float f, float f2) {
        if (f == f2) {
            return f;
        }
        return Utility.randomFloatInRange(f, f2);
    }

    public void a(CustomUnitConfig customUnitConfig, IniFile iniFile, String str) throws ConfigParseException {
        GameEngine gameEngine = GameEngine.getInstance();
        this.createWhenOffscreen = iniFile.getBoolean(str, "createWhenOffscreen", (Boolean) false).booleanValue();
        this.createWhenZoomedOut = iniFile.getBoolean(str, "createWhenZoomedOut", (Boolean) true).booleanValue();
        this.createWhenOverLiquid = iniFile.getBoolean(str, "createWhenOverLiquid", (Boolean) true).booleanValue();
        this.createWhenOverLand = iniFile.getBoolean(str, "createWhenOverLand", (Boolean) true).booleanValue();
        if (!this.createWhenOverLiquid && !this.createWhenOverLand) {
            throw new RuntimeException(str + " effect cannot have both createWhenOverLiquid and createWhenOverLand set to false, it would never be created");
        }
        this.spawnChance = iniFile.getFloat(str, "spawnChance", Float.valueOf(1.0f)).floatValue();
        this.life = iniFile.getFloat(str, "life", Float.valueOf(200.0f)).floatValue();
        this.lifeRandom = iniFile.getFloat(str, "lifeRandom", Float.valueOf(0.0f)).floatValue();
        this.showInFog = iniFile.getBoolean(str, "showInFog", (Boolean) false).booleanValue();
        this.xOffsetRelative = iniFile.getFloat(str, "xOffsetRelative", Float.valueOf(0.0f)).floatValue();
        this.yOffsetRelative = iniFile.getFloat(str, "yOffsetRelative", Float.valueOf(0.0f)).floatValue();
        this.hOffset = iniFile.getFloat(str, "hOffset", Float.valueOf(0.0f)).floatValue();
        this.alwayStartDirAtZero = iniFile.getBooleanFromTwoKeys(str, "alwaysStartDirAtZero", "alwayStartDirAtZero", (Boolean) false).booleanValue();
        this.pivotOffset = iniFile.getFloat(str, "pivotOffset", Float.valueOf(0.0f)).floatValue();
        this.pivotOffsetRandom = iniFile.getFloat(str, "pivotOffsetRandom", Float.valueOf(0.0f)).floatValue();
        this.dirOffset = iniFile.getFloat(str, "dirOffset", Float.valueOf(0.0f)).floatValue();
        this.xOffsetRelativeRandom = iniFile.getFloat(str, "xOffsetRelativeRandom", Float.valueOf(0.0f)).floatValue();
        this.yOffsetRelativeRandom = iniFile.getFloat(str, "yOffsetRelativeRandom", Float.valueOf(0.0f)).floatValue();
        this.hOffsetRandom = iniFile.getFloat(str, "hOffsetRandom", Float.valueOf(0.0f)).floatValue();
        this.dirOffsetRandom = iniFile.getFloat(str, "dirOffsetRandom", Float.valueOf(0.0f)).floatValue();
        this.xOffsetAbsolute = iniFile.getFloat(str, "xOffsetAbsolute", Float.valueOf(0.0f)).floatValue();
        this.yOffsetAbsolute = iniFile.getFloat(str, "yOffsetAbsolute", Float.valueOf(0.0f)).floatValue();
        this.xOffsetAbsoluteRandom = iniFile.getFloat(str, "xOffsetAbsoluteRandom", Float.valueOf(0.0f)).floatValue();
        this.yOffsetAbsoluteRandom = iniFile.getFloat(str, "yOffsetAbsoluteRandom", Float.valueOf(0.0f)).floatValue();
        this.xSpeedRelative = iniFile.getFloat(str, "xSpeedRelative", Float.valueOf(0.0f)).floatValue();
        this.ySpeedRelative = iniFile.getFloat(str, "ySpeedRelative", Float.valueOf(0.0f)).floatValue();
        this.hSpeed = iniFile.getFloat(str, "hSpeed", Float.valueOf(0.0f)).floatValue();
        this.dirSpeed = iniFile.getFloat(str, "dirSpeed", Float.valueOf(0.0f)).floatValue();
        this.xSpeedRelativeRandom = iniFile.getFloat(str, "xSpeedRelativeRandom", Float.valueOf(0.0f)).floatValue();
        this.ySpeedRelativeRandom = iniFile.getFloat(str, "ySpeedRelativeRandom", Float.valueOf(0.0f)).floatValue();
        this.hSpeedRandom = iniFile.getFloat(str, "hSpeedRandom", Float.valueOf(0.0f)).floatValue();
        this.dirSpeedRandom = iniFile.getFloat(str, "dirSpeedRandom", Float.valueOf(0.0f)).floatValue();
        this.xSpeedAbsolute = iniFile.getFloat(str, "xSpeedAbsolute", Float.valueOf(0.0f)).floatValue();
        this.ySpeedAbsolute = iniFile.getFloat(str, "ySpeedAbsolute", Float.valueOf(0.0f)).floatValue();
        this.xSpeedAbsoluteRandom = iniFile.getFloat(str, "xSpeedAbsoluteRandom", Float.valueOf(0.0f)).floatValue();
        this.ySpeedAbsoluteRandom = iniFile.getFloat(str, "ySpeedAbsoluteRandom", Float.valueOf(0.0f)).floatValue();
        this.scaleTo = iniFile.getFloat(str, "scaleTo", Float.valueOf(this.scaleTo)).floatValue();
        this.scaleFrom = iniFile.getFloat(str, "scaleFrom", Float.valueOf(this.scaleFrom)).floatValue();
        this.alpha = iniFile.getFloat(str, "alpha", Float.valueOf(this.alpha)).floatValue();
        this.color = iniFile.getColorAsInt(str, "color", Integer.valueOf(this.color)).intValue();
        if (GameEngine.isAndroidPlatform() && this.color != 0 && this.color != -1) {
            this.cachedLightingColorFilter = new LightingColorFilter(this.color, 0);
        }
        this.teamColorRatio = iniFile.getFloat(str, "teamColorRatio", Float.valueOf(this.teamColorRatio)).floatValue();
        if (this.teamColorRatio < 0.0f || this.teamColorRatio > 1.0f) {
            throw new RuntimeException(str + " teamColorRatio should be between 0-1 got:" + this.teamColorRatio);
        }
        this.shadow = iniFile.getBoolean(str, "shadow", (Boolean) false).booleanValue();
        this.drawLayer = (short) 2;
        if (iniFile.getBoolean(str, "drawUnderUnits", (Boolean) false).booleanValue()) {
            this.drawLayer = (short) 1;
        }
        String string = iniFile.getString(str, "drawType", (String) null);
        if (string != null && !string.equals("normal")) {
            if (string.equals("displacement")) {
                this.drawLayer = (short) 3;
            } else {
                throw new ConfigParseException("Unknown drawType: " + string);
            }
        }
        this.fadeInTime = iniFile.getFloat(str, "fadeInTime", Float.valueOf(0.0f)).floatValue();
        this.fadeOut = iniFile.getBoolean(str, "fadeOut", (Boolean) true).booleanValue();
        this.delayedStartTimer = iniFile.getTime(str, "delayedStartTimer", Float.valueOf(0.0f)).floatValue();
        this.delayedStartTimerRandom = iniFile.getFloat(str, "delayedStartTimerRandom", Float.valueOf(0.0f)).floatValue();
        this.frameIndex = iniFile.getInt(str, "frameIndex", (Integer) 0).intValue();
        this.frameIndexRandom = iniFile.getInt(str, "frameIndexRandom", (Integer) 0).intValue();
        String string2 = iniFile.getString(str, "stripIndex", "0");
        this.stripIndex = gameEngine.effectManager.findEffectTemplateIndex(string2);
        if (this.stripIndex == -1) {
            throw new RuntimeException("Failed to find stripIndex with name:" + string2);
        }
        this.attachedToUnit = iniFile.getBoolean(str, "attachedToUnit", (Boolean) true).booleanValue();
        this.liveAfterAttachedDies = iniFile.getBoolean(str, "liveAfterAttachedDies", (Boolean) true).booleanValue();
        this.atmospheric = iniFile.getBoolean(str, "atmospheric", (Boolean) false).booleanValue();
        this.physics = iniFile.getBoolean(str, "physics", (Boolean) false).booleanValue();
        this.physicsGravity = iniFile.getFloat(str, "physicsGravity", Float.valueOf(1.0f)).floatValue();
        String string3 = iniFile.getString(str, "priority", (String) null);
        if (string3 != null) {
            try {
                this.priority = EffectQuality.valueOf(string3);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Unknown priority:" + string3);
            }
        }
        int iIntValue = iniFile.getInt(str, "total_frames", (Integer) 1).intValue();
        if (iIntValue < 1) {
            throw new ConfigParseException("TOTAL_FRAMES cannot be: " + iIntValue + " (must be 1 or more)");
        }
        Texture textureA = customUnitConfig.a(iniFile, str, "image");
        if (textureA != null) {
            this.imageStrip = new SpriteSheet();
            this.imageStrip.texture = textureA;
            this.imageStrip.frameWidth = this.imageStrip.texture.m() / iIntValue;
            this.imageStrip.frameHeight = this.imageStrip.texture.l();
            this.imageStrip.frameWidth = iniFile.getInt(str, "frame_width", Integer.valueOf(this.imageStrip.frameWidth)).intValue();
            this.imageStrip.frameHeight = iniFile.getInt(str, "frame_height", Integer.valueOf(this.imageStrip.frameHeight)).intValue();
            if (iIntValue == 1 && this.imageStrip.frameWidth >= this.imageStrip.texture.m()) {
                this.imageStrip.singleFrame = true;
            } else if (this.imageStrip.frameHeight < this.imageStrip.texture.l()) {
                this.imageStrip.framesPerRow = this.imageStrip.texture.m() / this.imageStrip.frameWidth;
                if (this.imageStrip.framesPerRow < 1) {
                    this.imageStrip.framesPerRow = 1;
                }
            }
            this.imageStrip.offsetX = 0;
            this.imageStrip.offsetY = 0;
            this.imageStrip.stepX = this.imageStrip.frameWidth;
            this.imageStrip.stepY = this.imageStrip.frameHeight;
            String string4 = iniFile.getString(str, "imageShadow", (String) null);
            if (string4 != null) {
                this.imageStrip.shadowTexture = CustomUnitConfigParser.cacheTexture(customUnitConfig.resourceLoadPath, string4, customUnitConfig.imageSmoothing, customUnitConfig, str, "imageShadow");
                this.shadow = true;
            }
            if (this.shadow && this.imageStrip.shadowTexture == null) {
                throw new ConfigParseException("imageShadow is required if image and shadow:true is used");
            }
        }
        this.animateFrameStart = iniFile.getInt(str, "animateFrameStart", (Integer) 0).intValue();
        this.animateFrameStartRandomAdd = iniFile.getInt(str, "animateFrameStartRandomAdd", (Integer) 0).intValue();
        this.animateFrameEnd = iniFile.getInt(str, "animateFrameEnd", (Integer) 0).intValue();
        this.animateFramePingPong = iniFile.getBoolean(str, "animateFramePingPong", (Boolean) false).booleanValue();
        this.animateFrameLooping = iniFile.getBoolean(str, "animateFrameLooping", (Boolean) false).booleanValue();
        this.animateFrameSpeed = iniFile.getTime(str, "animateFrameSpeed", Float.valueOf(0.5f)).floatValue();
        this.animateFrameSpeedRandom = iniFile.getTime(str, "animateFrameSpeedRandom", Float.valueOf(0.0f)).floatValue();
        if (textureA != null && ((this.imageStrip.frameWidth >= this.imageStrip.texture.m() || iIntValue != 1) && this.animateFrameEnd > iIntValue)) {
            throw new ConfigParseException("animateFrameEnd:" + this.animateFrameEnd + " cannot be larger than TOTAL_FRAMES: " + iIntValue + " (when using custom image)");
        }
        this.alsoEmitEffects = customUnitConfig.createSpawnList(iniFile.getString(str, "alsoEmitEffects", (String) null));
        this.alsoEmitEffectsOnDeath = customUnitConfig.createSpawnList(iniFile.getString(str, "alsoEmitEffectsOnDeath", (String) null));
        this.trailEffect = customUnitConfig.createSpawnList(iniFile.getString(str, "trailEffect", (String) null));
        this.trailEffectRate = iniFile.getTime(str, "trailEffectRate", Float.valueOf(6.0f)).floatValue();
        this.ifSpawnFailsEmitEffects = customUnitConfig.createSpawnList(iniFile.getString(str, "ifSpawnFailsEmitEffects", (String) null));
        this.alsoPlaySound = SoundList.a(customUnitConfig, iniFile.getString(str, "alsoPlaySound", (String) null), (SoundList) null);
    }
}
