package com.corrodinggames.rts.game.units.land;

import android.graphics.Color;
import android.graphics.PointF;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.UnitSize;
import com.corrodinggames.rts.game.units.UnitTypeEnum;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.audio.SoundEngine;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.e.a */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/e/a.class */
public class Artillery extends LandUnit {
    static Texture a = null;
    static Texture b = null;
    static Texture c = null;
    static Texture[] d = new Texture[10];

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: b, reason: merged with bridge method [inline-methods] */
    public UnitTypeEnum r() {
        return UnitTypeEnum.artillery;
    }

    public static void f() {
        GameEngine gameEngine = GameEngine.getInstance();
        a = gameEngine.renderGraphicsEngine.a(R.drawable.artillery2);
        b = gameEngine.renderGraphicsEngine.a(R.drawable.artillery1_dead);
        d = PlayerTeam.getTeamColorTextures(a);
        c = getUnitTextureSize(a);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture d() {
        if (this.isDead) {
            return b;
        }
        return d[this.team.getTeamColorIndex()];
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture k() {
        return c;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: F */
    public boolean canDrawShadow() {
        return GameEngine.getInstance().settingsEngine.renderExtraShadows && !this.isDead;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: G */
    public float getShadowOffsetX() {
        return 3.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: H */
    public float getShadowOffsetY() {
        return 3.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture d(int i) {
        return null;
    }

    public Artillery(boolean z) {
        super(z);
        T(28);
        U(50);
        this.radius = 18.0f;
        this.displayRadius = this.radius;
        this.maxHealth = 140.0f;
        this.currentHealth = this.maxHealth;
        this.baseTexture = a;
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

    @Override // com.corrodinggames.rts.game.units.land.LandUnit, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f) {
        super.update(f);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void a(BaseUnit baseUnit, int i) {
        PointF pointFE = E(i);
        Projectile projectileA = Projectile.a(this, pointFE.x, pointFE.y);
        PointF pointFK = getShadowOffsetForLevel(i);
        projectileA.trackOffsetX = pointFK.x;
        projectileA.trackOffsetY = pointFK.y;
        projectileA.lifeTimer = 150.0f;
        projectileA.speed = 4.0f;
        projectileA.isSmallExplosion = true;
        projectileA.color = Color.a(255, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_BUTTON_3, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_BUTTON_3, 80);
        projectileA.textureType = (short) 2;
        projectileA.textureFrame = (short) 1;
        projectileA.renderScale = 0.9f;
        PointF pointFA = baseUnit.a(pointFE.x, pointFE.y, projectileA.speed, projectileA.lifeTimer, m());
        projectileA.fliesToPosition = true;
        projectileA.hasFixedTarget = true;
        projectileA.targetX = pointFA.x;
        projectileA.targetY = pointFA.y;
        projectileA.splashDamage = 80.0f;
        projectileA.explosionRadius = 45.0f;
        projectileA.damageEnemiesOnly = true;
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.soundEngine.playSound(SoundEngine.cannonFiringSound, 0.3f, pointFE.x, pointFE.y);
        gameEngine.effectManager.createFlameEffect(pointFE.x, pointFE.y, this.posZ, this.movementLevels[i].targetX);
        Effect effectCreateLightEffect = gameEngine.effectManager.createLightEffect(pointFE.x, pointFE.y, this.posZ, -1118482);
        if (effectCreateLightEffect != null) {
            effectCreateLightEffect.lifeTimer = 15.0f;
            effectCreateLightEffect.lifeMax = effectCreateLightEffect.lifeTimer;
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: bW */
    public float getSecondaryBarProgress() {
        if (this.movementLevels[0].rotation > 0.0f) {
            return 1.0f - (this.movementLevels[0].rotation / b(0));
        }
        return super.getSecondaryBarProgress();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float m() {
        return 290.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float b(int i) {
        return 240.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: z */
    public float getMoveSpeed() {
        return 0.9f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: A */
    public float getMaxTurnSpeed() {
        return 1.7f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float B() {
        return 0.05f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float c(int i) {
        return 99.0f;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: l */
    public boolean canAttack() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: af */
    public boolean canAttackFlyingUnits() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean E() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float g(int i) {
        return 20.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: C */
    public float getMoveAccelerationSpeed() {
        return 0.05f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float D() {
        return 0.12f;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public void e(float f) {
        super.e(f);
        GameViewUtils.a(this, m());
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: bN */
    public float getPushMass() {
        return 14000.0f;
    }
}
