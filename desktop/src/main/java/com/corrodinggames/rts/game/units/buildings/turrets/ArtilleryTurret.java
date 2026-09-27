package com.corrodinggames.rts.game.units.buildings.turrets;

import android.graphics.Color;
import android.graphics.PointF;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.UnitTypeEnum;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.audio.SoundEngine;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.d.a.d */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/d/a/d.class */
class ArtilleryTurret extends TurretImplementation {

    /* JADX INFO: renamed from: b */
    final /* synthetic */ TurretFactory turretFactory;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    ArtilleryTurret(TurretFactory turretFactory) {
        super(turretFactory);
        this.turretFactory = turretFactory;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.turrets.TurretImplementation
    public String c() {
        return TurretFactory.ARTILLERY_TURRET_TYPE;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.turrets.TurretImplementation
    public int d() {
        return UnitTypeEnum.turret.c() + TurretFactory.upgradeToArtilleryAction.getCostAmount();
    }

    @Override // com.corrodinggames.rts.game.units.buildings.turrets.TurretImplementation
    /* JADX INFO: renamed from: d */
    public Texture getTurretTopTexture(int i) {
        return TurretFactory.turretTopArtilleryTexture;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.turrets.TurretImplementation
    /* JADX INFO: renamed from: a */
    float getAttackRange() {
        return 350.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.turrets.TurretImplementation
    /* JADX INFO: renamed from: a */
    public float getAttackDelay(int i) {
        return 220.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.turrets.TurretImplementation
    /* JADX INFO: renamed from: b */
    public float getAttackDamage(int i) {
        return 100.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.turrets.TurretImplementation
    /* JADX INFO: renamed from: a */
    public void fireProjectile(BaseUnit baseUnit, int i) {
        PointF pointFC = getTurretType(i);
        Projectile projectileA = Projectile.a(this.turretFactory, pointFC.x, pointFC.y);
        PointF pointFK = this.turretFactory.getShadowOffsetForLevel(i);
        projectileA.trackOffsetX = pointFK.x;
        projectileA.trackOffsetY = pointFK.y;
        projectileA.lifeTimer = 150.0f;
        projectileA.speed = 4.0f;
        projectileA.isSmallExplosion = true;
        projectileA.color = Color.a(255, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_BUTTON_3, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_BUTTON_3, 80);
        projectileA.textureType = (short) 2;
        projectileA.textureFrame = (short) 0;
        projectileA.renderScale = 0.9f;
        PointF pointFA = baseUnit.a(pointFC.x, pointFC.y, projectileA.speed, projectileA.lifeTimer, getAttackRange());
        projectileA.fliesToPosition = true;
        projectileA.hasFixedTarget = true;
        projectileA.targetX = pointFA.x;
        projectileA.targetY = pointFA.y;
        projectileA.splashDamage = getAttackDamage(i);
        projectileA.explosionRadius = 55.0f;
        projectileA.damageEnemiesOnly = true;
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.soundEngine.playSound(SoundEngine.cannonFiringSound, 0.3f, pointFC.x, pointFC.y);
        gameEngine.effectManager.createFlameEffect(pointFC.x, pointFC.y, this.turretFactory.posZ, this.turretFactory.movementLevels[i].targetX);
        gameEngine.effectManager.createLightEffect(projectileA, -1118482);
        Effect effectCreateLightEffect = gameEngine.effectManager.createLightEffect(pointFC.x, pointFC.y, this.turretFactory.posZ, -1118482);
        if (effectCreateLightEffect != null) {
            effectCreateLightEffect.lifeTimer = 15.0f;
            effectCreateLightEffect.lifeMax = effectCreateLightEffect.lifeTimer;
        }
    }

    @Override // com.corrodinggames.rts.game.units.buildings.turrets.TurretImplementation
    /* JADX INFO: renamed from: b */
    public int getTechLevel() {
        return 2;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.turrets.TurretImplementation
    /* JADX INFO: renamed from: a */
    public void copyFrom(TurretImplementation turretImplementation) {
        this.turretFactory.maxHealth += 300.0f;
        this.turretFactory.currentHealth += 300.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.turrets.TurretImplementation
    /* JADX INFO: renamed from: e */
    public float getTurretRestingRotationSpeed(int i) {
        return 2.5f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.turrets.TurretImplementation
    /* JADX INFO: renamed from: f */
    public float getTurretTurnSpeed(int i) {
        return 0.2f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.turrets.TurretImplementation
    /* JADX INFO: renamed from: h */
    public float getTurretOffset(int i) {
        return -2.0f;
    }
}
