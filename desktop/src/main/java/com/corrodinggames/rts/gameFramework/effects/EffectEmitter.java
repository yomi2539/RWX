package com.corrodinggames.rts.gameFramework.effects;

import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.GameObject;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import java.io.IOException;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.d.f */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/d/f.class */
public class EffectEmitter extends GameObject {
    /* JADX INFO: renamed from: v */
    static Effect defaultFireEffect;

    /* JADX INFO: renamed from: w */
    static Effect alternateFireEffect;

    /* JADX INFO: renamed from: x */
    private final EffectManager effectManager;

    /* JADX INFO: renamed from: a */
    public float duration;

    /* JADX INFO: renamed from: t */
    public float startDelay;

    /* JADX INFO: renamed from: b */
    public boolean isEmitting = true;

    /* JADX INFO: renamed from: j */
    public int startColorOverride = 0;

    /* JADX INFO: renamed from: k */
    public int endColorOverride = 0;

    /* JADX INFO: renamed from: l */
    public int endColorTransitionTime = -1;
    /* JADX INFO: renamed from: u */
    public boolean emitWhenOffscreen = false;
    /* JADX INFO: renamed from: c */
    float emitTimer;
    /* JADX INFO: renamed from: d */
    float frameTimer;
    /* JADX INFO: renamed from: e */
    float frameIndex;
    /* JADX INFO: renamed from: f */
    float emitInterval;
    /* JADX INFO: renamed from: g */
    float frameInterval;
    /* JADX INFO: renamed from: h */
    float maxFrames;
    /* JADX INFO: renamed from: i */
    Effect effectTemplate;
    /* JADX INFO: renamed from: m */
    float velocityRandomRangeX;
    /* JADX INFO: renamed from: n */
    float velocityRandomRangeY;
    /* JADX INFO: renamed from: o */
    float velocityRandomRangeZ;
    /* JADX INFO: renamed from: p */
    float positionRandomRangeX;
    /* JADX INFO: renamed from: q */
    float positionRandomRangeY;
    /* JADX INFO: renamed from: r */
    float rotationRandomRange;
    /* JADX INFO: renamed from: s */
    EffectQuality effectQuality;

    public EffectEmitter(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    /* JADX INFO: renamed from: b */
    public static void initFireEffects() {
        EffectManager effectManager = GameEngine.getInstance().effectManager;
        Effect effect = new Effect(effectManager);
        setupFireEffect(effect, false);
        effect.stripIndex = 18;
        effect.fadeDuration = 15.0f;
        defaultFireEffect = effect;
        Effect effect2 = new Effect(effectManager);
        setupAlternateFireEffect(effect2, false);
        alternateFireEffect = effect2;
    }

    /* JADX INFO: renamed from: a */
    public static void setupFireEffect(Effect effect, boolean z) {
        effect.free();
        effect.stripIndex = 5;
        if (z) {
            effect.frameIndex = Utility.getRandomIntInRange(0, 1);
        } else {
            effect.frameIndex = 0;
        }
        effect.rotation = 0.0f;
        effect.isCentered = true;
        effect.velocityX = 0.1f;
        effect.velocityZ = 0.5f;
        effect.useGravity = true;
        effect.lifeTimer = 300.0f;
        effect.lifeMax = effect.lifeTimer;
        effect.fadeIn = true;
        effect.fadeOut = true;
        effect.fadeDuration = 40.0f;
        effect.shadow = false;
        effect.drawLayer = (short) 2;
        effect.scaleFrom = 0.4f;
        effect.scaleTo = 1.5f;
        effect.effectKind = Effect.KIND_FIRE;
    }

    /* JADX INFO: renamed from: b */
    public static void setupAlternateFireEffect(Effect effect, boolean z) {
        effect.free();
        effect.stripIndex = 7;
        if (z) {
            effect.frameIndex = Utility.getRandomIntInRange(0, 3);
        } else {
            effect.frameIndex = 0;
        }
        effect.rotation = 0.0f;
        effect.isCentered = true;
        effect.velocityX = 0.0f;
        effect.velocityZ = 0.2f;
        effect.useGravity = true;
        effect.lifeTimer = 50.0f;
        effect.lifeMax = effect.lifeTimer;
        effect.fadeIn = true;
        effect.fadeOut = true;
        effect.fadeDuration = 10.0f;
        effect.shadow = false;
        effect.drawLayer = (short) 2;
        effect.effectKind = Effect.KIND_ALTERNATE_FIRE;
    }

    /* JADX INFO: renamed from: a */
    public static EffectEmitter createDefaultFireEmitter(float f, float f2) {
        EffectEmitter effectEmitterA = createEmitter(f, f2, defaultFireEffect);
        effectEmitterA.duration = 280.0f;
        effectEmitterA.emitInterval = 10.0f;
        effectEmitterA.emitTimer = 10.0f;
        effectEmitterA.velocityRandomRangeX = 0.03f;
        effectEmitterA.velocityRandomRangeY = 0.03f;
        effectEmitterA.positionRandomRangeX = 6.0f;
        effectEmitterA.positionRandomRangeY = 6.0f;
        effectEmitterA.effectQuality = EffectQuality.verylow;
        effectEmitterA.rotationRandomRange = 180.0f;
        effectEmitterA.startColorOverride = -16777216;
        return effectEmitterA;
    }

    /* JADX INFO: renamed from: b */
    public static EffectEmitter createAlternateFireEmitter(float f, float f2) {
        EffectEmitter effectEmitterA = createEmitter(f, f2, alternateFireEffect);
        effectEmitterA.duration = 330.0f;
        effectEmitterA.emitInterval = 10.0f;
        effectEmitterA.emitTimer = 10.0f;
        effectEmitterA.velocityRandomRangeX = 0.1f;
        effectEmitterA.velocityRandomRangeY = 0.03f;
        effectEmitterA.positionRandomRangeX = 4.0f;
        effectEmitterA.positionRandomRangeY = 4.0f;
        effectEmitterA.effectQuality = EffectQuality.verylow;
        return effectEmitterA;
    }

    /* JADX INFO: renamed from: a */
    public static EffectEmitter createEmitter(float f, float f2, Effect effect) {
        EffectManager effectManager = GameEngine.getInstance().effectManager;
        EffectEmitter effectEmitter = new EffectEmitter(effectManager);
        effectEmitter.posX = f;
        effectEmitter.posY = f2;
        effectEmitter.duration = 100.0f;
        effectEmitter.emitInterval = 10.0f;
        effectEmitter.effectTemplate = effect;
        if (effect == null) {
            effectEmitter.effectTemplate = new Effect(effectManager);
            GameEngine.logColored("Error: Emitter create srcEffect==null");
        }
        return effectEmitter;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeFloat(this.posX);
        gameOutputStream.writeFloat(this.posY);
        gameOutputStream.writeFloat(this.duration);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public void a(GameInputStream gameInputStream) throws IOException {
        this.posX = gameInputStream.readFloat();
        this.posY = gameInputStream.readFloat();
        this.duration = gameInputStream.readFloat();
        this.isEmitting = false;
        super.a(gameInputStream);
    }

    /* JADX INFO: renamed from: c */
    public boolean isVisible() {
        return GameEngine.getInstance().extendedVisibleWorldRect.b(this.posX, this.posY);
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f) {
        Effect effectCreateEffectInternal;
        this.startDelay = Utility.moveTowardsZero(this.startDelay, f);
        if (this.startDelay > 0.0f) {
            return;
        }
        if (this.isEmitting) {
            this.emitTimer += f;
            if (this.emitTimer > this.emitInterval) {
                this.frameTimer += f;
                if (this.frameTimer > this.frameInterval) {
                    this.frameTimer = 0.0f;
                    this.frameIndex += 1.0f;
                    if (this.frameIndex > this.maxFrames) {
                        this.emitTimer = 0.0f;
                        this.frameIndex = 0.0f;
                    }
                    if ((this.emitWhenOffscreen || isVisible()) && (effectCreateEffectInternal = this.effectManager.createEffectInternal(this.posX, this.posY, 0.0f, EffectType.custom, false, this.effectQuality)) != null) {
                        effectCreateEffectInternal.recycle(this.effectTemplate);
                        effectCreateEffectInternal.velocityX += Utility.randomFloatInRange(-this.velocityRandomRangeX, this.velocityRandomRangeX);
                        effectCreateEffectInternal.velocityY += Utility.randomFloatInRange(-this.velocityRandomRangeY, this.velocityRandomRangeY);
                        effectCreateEffectInternal.velocityZ += Utility.randomFloatInRange(-this.velocityRandomRangeZ, this.velocityRandomRangeZ);
                        effectCreateEffectInternal.rotation = Utility.randomFloatInRange(-this.rotationRandomRange, this.rotationRandomRange);
                        effectCreateEffectInternal.posX = this.posX;
                        effectCreateEffectInternal.posY = this.posY;
                        effectCreateEffectInternal.posX += Utility.randomFloatInRange(-this.positionRandomRangeX, this.positionRandomRangeX);
                        effectCreateEffectInternal.posY += Utility.randomFloatInRange(-this.positionRandomRangeY, this.positionRandomRangeY);
                        if (this.startColorOverride != 0) {
                            effectCreateEffectInternal.startColor = this.startColorOverride;
                        }
                        if (this.endColorTransitionTime >= 0) {
                            effectCreateEffectInternal.endColor = this.endColorOverride;
                            effectCreateEffectInternal.endColorTransitionTime = this.endColorTransitionTime;
                        }
                    }
                }
            }
        }
        this.duration -= f;
        if (this.duration < 0.0f) {
            remove();
        }
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public boolean a(GameEngine gameEngine) {
        return false;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public boolean c(float f) {
        return false;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public void e(float f) {
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public void a(float f, boolean z) {
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public void d(float f) {
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public boolean f(float f) {
        return false;
    }
}
