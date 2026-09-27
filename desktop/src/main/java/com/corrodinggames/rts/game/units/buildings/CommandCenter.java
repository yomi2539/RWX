package com.corrodinggames.rts.game.units.buildings;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.UnitSize;
import com.corrodinggames.rts.game.units.UnitTypeEnum;
import com.corrodinggames.rts.game.units.actions.QueueUnitAction;
import com.corrodinggames.rts.game.units.actions.SetRallyAction;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.audio.SoundEngine;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.effects.EffectEmitter;
import com.corrodinggames.rts.gameFramework.effects.EffectQuality;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;
import java.io.IOException;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.d.e */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/d/e.class */
public class CommandCenter extends FactoryWithQueue {

    /* JADX INFO: renamed from: a */
    static Texture sharedBaseTexture = null;

    /* JADX INFO: renamed from: b */
    static Texture[] teamTextures = new Texture[10];

    /* JADX INFO: renamed from: c */
    static Texture deadTexture = null;

    /* JADX INFO: renamed from: d */
    static Texture backgroundTexture = null;

    /* JADX INFO: renamed from: e */
    float resourceGenerationTimer;

    /* JADX INFO: renamed from: f */
    public float animationTimer1;

    /* JADX INFO: renamed from: g */
    public float animationTimer2;

    /* JADX INFO: renamed from: h */
    public int animationCounter;

    /* JADX INFO: renamed from: i */
    public float animationTimer3;

    /* JADX INFO: renamed from: j */
    public float animationTimer4;

    /* JADX INFO: renamed from: k */
    float frameTimer;

    /* JADX INFO: renamed from: l */
    int frameIndex;

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeFloat(this.resourceGenerationTimer);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject
    public void a(GameInputStream gameInputStream) throws IOException {
        this.resourceGenerationTimer = gameInputStream.readFloat();
        super.a(gameInputStream);
    }

    /* JADX INFO: renamed from: b */
    public static void loadTextures() {
        GameEngine gameEngine = GameEngine.getInstance();
        sharedBaseTexture = gameEngine.renderGraphicsEngine.a(R.drawable.base);
        deadTexture = gameEngine.renderGraphicsEngine.a(R.drawable.base_dead);
        backgroundTexture = gameEngine.renderGraphicsEngine.a(R.drawable.base_back);
        teamTextures = PlayerTeam.getTeamColorTextures(sharedBaseTexture);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: K, reason: merged with bridge method [inline-methods] */
    public UnitTypeEnum r() {
        return UnitTypeEnum.commandCenter;
    }

    public CommandCenter(boolean z) {
        super(z);
        this.frameTimer = 20.0f;
        this.frameIndex = 0;
        super.baseTexture = sharedBaseTexture;
        this.overlayTexture = backgroundTexture;
        T(53);
        U(68);
        this.radius = 30.0f;
        this.displayRadius = this.radius;
        this.maxHealth = 4000.0f;
        this.currentHealth = this.maxHealth;
        S(3);
        this.buildingTargetRect.a(-1, -1, 1, 1);
        this.buildingVelocityRect.a(-1, -1, 1, 2);
    }

    @Override
    // com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit
    public void a(int i) {
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void S() {
        super.S();
        if (this.isDead) {
            this.overlayTexture = null;
        } else {
            this.overlayTexture = backgroundTexture;
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture d() {
        if (this.isDead) {
            return deadTexture;
        }
        return teamTextures[this.team.getTeamColorIndex()];
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture k() {
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.BaseBuilding
    /* JADX INFO: renamed from: L */
    public boolean onDeath() {
        GameEngine gameEngine = GameEngine.getInstance();
        super.baseTexture = deadTexture;
        this.overlayTexture = null;
        S(0);
        this.isAlive = false;
        a(UnitSize.large);
        float f = this.posX;
        float f2 = this.posY;
        gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
        Effect effectCreateLightEffect = gameEngine.effectManager.createLightEffect(f, f2, this.posZ, Color.a(255, 255, 255, 255));
        if (effectCreateLightEffect != null) {
            effectCreateLightEffect.scaleFrom = 8.0f;
            effectCreateLightEffect.scaleTo = 5.0f;
            effectCreateLightEffect.alpha = 0.9f;
            effectCreateLightEffect.lifeTimer = 20.0f;
            effectCreateLightEffect.lifeMax = effectCreateLightEffect.lifeTimer;
            effectCreateLightEffect.fadeIn = true;
        }
        gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
        Effect effectCreateSmallExplosion = gameEngine.effectManager.createSmallExplosion(f, f2, 0.0f, -1127220);
        if (effectCreateSmallExplosion != null) {
            effectCreateSmallExplosion.scaleFrom = 0.2f;
            effectCreateSmallExplosion.scaleTo = 2.0f;
            effectCreateSmallExplosion.drawLayer = (short) 2;
            effectCreateSmallExplosion.lifeTimer = 45.0f;
            effectCreateSmallExplosion.lifeMax = effectCreateSmallExplosion.lifeTimer;
            effectCreateSmallExplosion.delayedStartTimer = 0.0f;
        }
        gameEngine.effectManager.createExplosionWithVelocity(this.posX, this.posY, this.posZ, 40.0f, 70.0f);
        EffectEmitter.createDefaultFireEmitter(this.posX, this.posY);
        EffectEmitter.createAlternateFireEmitter(this.posX, this.posY).duration = 800.0f;
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cF */
    public RectF getUnitBounds() {
        RectF rectFCF = super.getUnitBounds();
        rectFCF.a(6.0f, 0.0f);
        return rectFCF;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f) {
        GameEngine.getInstance();
        super.update(f);
        if (!isAlive() || this.isDead) {
            return;
        }
        this.frameTimer = Utility.moveTowardsZero(this.frameTimer, f);
        if (this.frameTimer == 0.0f) {
            this.frameTimer = 5.0f;
            this.frameIndex++;
            if (this.frameIndex > 6) {
                this.frameIndex = 0;
                this.frameTimer = 70.0f;
            }
            if (this.frameIndex <= 3) {
                this.currentAnimationFrame = this.frameIndex;
            } else {
                this.currentAnimationFrame = 6 - this.frameIndex;
            }
        }
        this.animationTimer1 += f;
        this.animationCounter++;
        this.animationTimer3 += 10.0f;
        if (this.animationTimer4 > f) {
            this.animationTimer4 = f;
        }
        this.animationTimer2 += f;
        this.resourceGenerationTimer += f;
        if (this.resourceGenerationTimer > PlayerTeam.resourceIncomeUpdateInterval - 0.1f) {
            this.resourceGenerationTimer -= PlayerTeam.resourceIncomeUpdateInterval;
            this.team.b(getCreditIncomeRate() * (PlayerTeam.resourceIncomeUpdateInterval / PlayerTeam.resourceIncomeRatePeriod));
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public float getCreditIncomeRate() {
        return 18.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float q(int i) {
        return 70.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit
    public void a(BaseUnit baseUnit, int i) {
        Projectile projectileA = Projectile.a(this, this.posX, this.posY);
        PointF pointFK = getShadowOffsetForLevel(i);
        projectileA.trackOffsetX = pointFK.x;
        projectileA.trackOffsetY = pointFK.y;
        projectileA.color = Color.a(255, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_DATA_SERVICE, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_DATA_SERVICE, 50);
        projectileA.damage = q(i);
        projectileA.targetUnit = baseUnit;
        projectileA.lifeTimer = 180.0f;
        projectileA.speed = 2.0f;
        projectileA.targetSpeed = 5.0f;
        projectileA.isBallistic = true;
        projectileA.hasTrail = true;
        projectileA.isSmallExplosion = true;
        projectileA.autoRetarget = true;
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.effectManager.createLightEffect(projectileA, -1118720);
        gameEngine.soundEngine.playSound(SoundEngine.missileFireSound, 0.8f, this.posX, this.posY);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit
    public float m() {
        return 280.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit
    public float b(int i) {
        return 70.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit
    public float c(int i) {
        return 999.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean b(int i, float f) {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: l */
    public boolean canAttack() {
        return true;
    }

    /* JADX INFO: renamed from: a */
    public static void addAvailableActions(ArrayList arrayList, int i) {
        arrayList.add(new SetRallyAction());
        arrayList.add(new QueueUnitAction(UnitTypeEnum.builder, 1.0f));
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: N */
    public ArrayList getAvailableActions() {
        return r().a(getUpgradeLevel());
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: a */
    public float applyDamage(BaseUnit baseUnit, float f, Projectile projectile) {
        if (f > 2500.0f) {
            f = 2500.0f;
        }
        return super.applyDamage(baseUnit, f, projectile);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean bJ() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public void e(float f) {
        super.e(f);
        GameViewUtils.a(this, m());
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public int s() {
        return 20;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public int bp() {
        return 35;
    }
}
