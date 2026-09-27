package com.corrodinggames.rts.game.units.land;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.UnitSize;
import com.corrodinggames.rts.game.units.UnitTypeEnum;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.audio.SoundEngine;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.effects.EffectManager;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.e.e */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/e/e.class */
public class HeavyHoverTank extends HoverLandUnit {
    float a;
    static Texture b = null;
    static Texture c = null;
    static Texture d = null;
    static Texture[] e = new Texture[10];
    Rect f;

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: b, reason: merged with bridge method [inline-methods] */
    public UnitTypeEnum r() {
        return UnitTypeEnum.heavyHoverTank;
    }

    public static void f() {
        GameEngine gameEngine = GameEngine.getInstance();
        c = gameEngine.renderGraphicsEngine.a(R.drawable.heavy_hover_tank);
        b = gameEngine.renderGraphicsEngine.a(R.drawable.heavy_hover_tank_dead);
        d = gameEngine.renderGraphicsEngine.a(R.drawable.heavy_hover_tank_shadow);
        e = PlayerTeam.getTeamColorTextures(c);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture d() {
        if (this.isDead) {
            return b;
        }
        return e[this.team.getTeamColorIndex()];
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture k() {
        return d;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture d(int i) {
        return null;
    }

    public HeavyHoverTank(boolean z) {
        super(z);
        this.a = 0.0f;
        this.f = new Rect();
        T(24);
        U(36);
        this.radius = 11.0f;
        this.displayRadius = this.radius + 2.0f;
        this.maxHealth = 450.0f;
        this.currentHealth = this.maxHealth;
        this.baseTexture = c;
        this.shadowTexture = d;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean e() {
        GameEngine.getInstance();
        this.baseTexture = b;
        S(0);
        this.isAlive = false;
        a(UnitSize.normal);
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.land.HoverLandUnit, com.corrodinggames.rts.game.units.land.LandUnit, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f) {
        super.update(f);
        if (this.isDead || !isAlive()) {
            return;
        }
        this.a += 3.0f * f;
        if (this.a > 360.0f) {
            this.a -= 360.0f;
        }
        this.posZ = Utility.distanceSq(this.posZ, 4.0f + (Utility.fastSin(this.a) * 1.5f), 0.1f * f);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float q(int i) {
        return 40.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void a(BaseUnit baseUnit, int i) {
        PointF pointFE = E(i);
        Projectile projectileA = Projectile.a(this, pointFE.x, pointFE.y, this.posZ, i);
        PointF pointFK = getShadowOffsetForLevel(i);
        projectileA.trackOffsetX = pointFK.x;
        projectileA.trackOffsetY = pointFK.y;
        projectileA.color = Color.a(255, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_DATA_SERVICE, 0, 50);
        projectileA.damage = q(i);
        projectileA.targetUnit = baseUnit;
        projectileA.lifeTimer = 95.0f;
        projectileA.speed = 1.0f;
        projectileA.targetSpeed = 7.0f;
        projectileA.acceleration = 0.2f;
        projectileA.textureFrame = (short) 7;
        projectileA.renderScale = 1.0f;
        GameEngine gameEngine = GameEngine.getInstance();
        Effect effectCreateLightEffect = gameEngine.effectManager.createLightEffect(pointFE.x, pointFE.y, this.posZ, -56798);
        if (effectCreateLightEffect != null) {
            effectCreateLightEffect.alpha = 0.7f;
            effectCreateLightEffect.lifeTimer = 30.0f;
            effectCreateLightEffect.lifeMax = effectCreateLightEffect.lifeTimer;
            EffectManager.attachEffectToGameObject(effectCreateLightEffect, this);
        }
        gameEngine.effectManager.createLightEffect(projectileA, -1179648);
        gameEngine.soundEngine.playSound(SoundEngine.plasmaFire2Sound, 0.3f, pointFE.x, pointFE.y);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean E() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float m() {
        return 160.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float b(int i) {
        return 75.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: z */
    public float getMoveSpeed() {
        return 0.7f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: A */
    public float getMaxTurnSpeed() {
        return 20.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: i */
    public void addRotation(float f) {
        this.rotationSpeed += f;
        if (this.rotationSpeed > 180.0f) {
            this.rotationSpeed -= 360.0f;
        }
        if (this.rotationSpeed < -180.0f) {
            this.rotationSpeed += 360.0f;
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: C */
    public float getMoveAccelerationSpeed() {
        return 0.06f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float D() {
        return 0.09f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float c(int i) {
        return 2.4f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: bi */
    public boolean isSlidingMovement() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: bj */
    public boolean isIgnoreMoveOrders() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: d */
    public float getRenderRotation(boolean z) {
        return this.movementLevels[0].targetX + 90.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public boolean c(float f) {
        return super.c(f);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: l */
    public boolean canAttack() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: af */
    public boolean canAttackFlyingUnits() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float g(int i) {
        return 16.0f;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public void e(float f) {
        super.e(f);
        GameViewUtils.a(this, m());
    }
}
