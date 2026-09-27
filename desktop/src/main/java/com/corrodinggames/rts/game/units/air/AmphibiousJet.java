package com.corrodinggames.rts.game.units.air;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.UnitMovementType;
import com.corrodinggames.rts.game.units.UnitTypeEnum;
import com.corrodinggames.rts.game.units.actions.AbstractUnitAction;
import com.corrodinggames.rts.game.units.actions.NoneAction;
import com.corrodinggames.rts.game.units.land.MammothTank;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.audio.SoundEngine;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.graphics.GamePaint;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;
import java.io.IOException;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.b.c */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/b/c.class */
public class AmphibiousJet extends AirUnit {
    public static final AbstractUnitAction y = new NoneAction(151) { // from class: com.corrodinggames.rts.game.units.b.c.1
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "-Surface unit.";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return "Fly";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z2) {
            return !((AmphibiousJet) baseUnit).hasWaterEffect;
        }
    };
    public static final AbstractUnitAction z = new NoneAction(152) { // from class: com.corrodinggames.rts.game.units.b.c.2
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "-Dive unit underwater.";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return "Dive";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z2) {
            return ((AmphibiousJet) baseUnit).hasWaterEffect && ((OrderableUnit) baseUnit).isOverWater();
        }
    };
    protected Paint waterPaint;
    float t;
    float u;
    /* JADX INFO: renamed from: q */
    float bobPhase;
    /* JADX INFO: renamed from: r */
    boolean hasWaterEffect;
    Rect x;
    static Texture a = null;
    static Texture b = null;
    static Texture c = null;
    static Texture d = null;
    static Texture[] e = new Texture[10];
    static Texture[] f = new Texture[10];
    static Texture[] g = new Texture[10];
    static Texture o = null;
    static Texture p = null;
    /* JADX INFO: renamed from: s */
    boolean isSubmerged;
    /* JADX INFO: renamed from: w */
    PointF tempPoint;
    static ArrayList A = new ArrayList();

    static {
        A.add(y);
        A.add(z);
    }

    public AmphibiousJet(boolean z2) {
        super(z2);
        this.hasWaterEffect = true;
        this.isSubmerged = true;
        this.t = 0.0f;
        this.u = 0.0f;
        this.waterPaint = new GamePaint();
        this.tempPoint = new PointF();
        this.x = new Rect();
        b(b);
        this.radius = 12.0f;
        this.displayRadius = this.radius + 1.0f;
        this.maxHealth = 530.0f;
        this.currentHealth = this.maxHealth;
        this.baseTexture = b;
        this.shadowTexture = c;
        this.posZ = 0.0f;
        S(5);
    }

    @Override // com.corrodinggames.rts.game.units.air.AirUnit, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeBoolean(this.hasWaterEffect);
        gameOutputStream.writeFloat(this.t);
        gameOutputStream.writeFloat(this.u);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.game.units.MovableUnit, com.corrodinggames.rts.game.units.BaseUnit
    public boolean Q() {
        return this.posZ < -1.0f;
    }

    @Override // com.corrodinggames.rts.game.units.air.AirUnit, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject
    public void a(GameInputStream gameInputStream) throws IOException {
        this.hasWaterEffect = gameInputStream.readBoolean();
        this.isSubmerged = !Q();
        if (gameInputStream.getProtocolVersion() >= 21) {
            this.t = gameInputStream.readFloat();
        }
        if (gameInputStream.getProtocolVersion() >= 22) {
            this.u = gameInputStream.readFloat();
        }
        M();
        super.a(gameInputStream);
    }

    @Override // com.corrodinggames.rts.game.units.air.AirUnit, com.corrodinggames.rts.game.units.BaseUnit
    public UnitMovementType getMovementType() {
        if (this.isUnitParalyzed) {
            return UnitMovementType.AIR;
        }
        if (b()) {
            return UnitMovementType.WATER;
        }
        return UnitMovementType.AIR;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: f, reason: merged with bridge method [inline-methods] */
    public UnitTypeEnum r() {
        return UnitTypeEnum.amphibiousJet;
    }

    public static void L() {
        GameEngine gameEngine = GameEngine.getInstance();
        b = gameEngine.renderGraphicsEngine.a(R.drawable.amphibious_jet);
        c = gameEngine.renderGraphicsEngine.a(R.drawable.amphibious_jet_shadow);
        a = gameEngine.renderGraphicsEngine.a(R.drawable.amphibious_jet_dead);
        e = PlayerTeam.getTeamColorTextures(b);
        Texture textureA = gameEngine.renderGraphicsEngine.a(R.drawable.amphibious_jet_p1);
        Texture textureA2 = gameEngine.renderGraphicsEngine.a(R.drawable.amphibious_jet_p2);
        f = PlayerTeam.getTeamColorTextures(textureA);
        g = PlayerTeam.getTeamColorTextures(textureA2);
        o = getUnitTextureSize(textureA);
        p = getUnitTextureSize(textureA2);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: aQ */
    public boolean drawShadow() {
        if (super.drawShadow()) {
            f(true);
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public boolean c(float f2) {
        if (!super.c(f2)) {
            return false;
        }
        if (this.isDead) {
            return true;
        }
        f(false);
        if (!this.isDead) {
            for (int i = 0; i < getTechLevel(); i++) {
                if (i != ds()) {
                    float fE = this.movementLevels[i].speed / e(i);
                    if (fE != 0.0f) {
                        GameEngine gameEngine = GameEngine.getInstance();
                        PointF pointFE = E(i);
                        gameEngine.renderGraphicsEngine.i();
                        gameEngine.renderGraphicsEngine.b(pointFE.x - gameEngine.viewpointXSnapped, (pointFE.y - gameEngine.viewpointYSnapped) - this.posZ);
                        gameEngine.renderGraphicsEngine.a(fE * 0.7f, fE * 0.7f);
                        gameEngine.renderGraphicsEngine.a(MammothTank.e, 0.0f, 0.0f, (Paint) null);
                        gameEngine.renderGraphicsEngine.j();
                    }
                }
            }
            return true;
        }
        return true;
    }

    public boolean b() {
        if (!this.hasWaterEffect || this.posZ < 0.0f) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: bl */
    public int getTechLevel() {
        return 3;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public PointF G(int i) {
        if (i == ds()) {
            return super.G(i);
        }
        float unitArmorRating = getRenderRotation(false) - 90.0f;
        PointF pointFA = a(i, false);
        float f2 = pointFA.x;
        float f3 = pointFA.y;
        tempPointF3.a(f2 + (Utility.fastCos(unitArmorRating) * 5.0f), f3 + (Utility.fastSin(unitArmorRating) * 5.0f));
        return tempPointF3;
    }

    public void f(boolean z2) {
        Paint paintAN;
        Texture texture;
        float f2;
        GameEngine gameEngine = GameEngine.getInstance();
        if (!z2) {
            paintAN = getRenderPaint();
        } else {
            this.waterPaint.a(50, 255, 255, 255);
            paintAN = this.waterPaint;
        }
        for (int i = 0; i <= 1; i++) {
            PointF pointFA = a(i, z2);
            float f3 = pointFA.x - gameEngine.viewpointXSnapped;
            float f4 = pointFA.y - gameEngine.viewpointYSnapped;
            float unitArmorRating = getRenderRotation(false) - 90.0f;
            if (!z2) {
                f4 -= this.posZ;
            }
            if (i == 0) {
                if (z2) {
                    texture = p;
                } else {
                    texture = g[this.team.getTeamColorIndex()];
                }
                f2 = unitArmorRating + 0.0f;
            } else {
                if (z2) {
                    texture = o;
                } else {
                    texture = f[this.team.getTeamColorIndex()];
                }
                f2 = unitArmorRating - 0.0f;
            }
            gameEngine.renderGraphicsEngine.a(texture, f3, f4, f2, paintAN);
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture d() {
        if (this.isDead) {
            return a;
        }
        return e[this.team.getTeamColorIndex()];
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture k() {
        return c;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture d(int i) {
        return d;
    }

    public PointF a(int i, boolean z2) {
        float unitArmorRating = getRenderRotation(false) - 90.0f;
        if (i == ds()) {
            throw new RuntimeException("index==2 is for base");
        }
        float f2 = this.posX;
        float f3 = this.posY;
        float fClampTo255 = Utility.clampTo255(this.u * 4.0f, 0.0f, 1.0f);
        float fClampTo2552 = Utility.clampTo255((this.u * 2.0f) - 1.0f, 0.0f, 1.0f);
        float fFastCos = f2 + (Utility.fastCos(unitArmorRating) * (7.0f - (5.0f * fClampTo255)));
        float fFastSin = f3 + (Utility.fastSin(unitArmorRating) * (7.0f - (5.0f * fClampTo255)));
        float f4 = (-90) + (SlickToAndroidKeycodes.AndroidCodes.KEYCODE_STB_INPUT * i);
        this.tempPoint.a(fFastCos + (Utility.fastCos(unitArmorRating + f4) * (12.0f - (5.0f * fClampTo2552))), fFastSin + (Utility.fastSin(unitArmorRating + f4) * (12.0f - (5.0f * fClampTo2552))));
        return this.tempPoint;
    }

    @Override // com.corrodinggames.rts.game.units.air.AirUnit, com.corrodinggames.rts.game.units.BaseUnit
    public boolean e() {
        GameEngine.getInstance().effectManager.createSmallExplosion(this.posX, this.posY, this.posZ);
        this.baseTexture = a;
        S(0);
        this.isAlive = false;
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.MovableUnit, com.corrodinggames.rts.game.units.BaseUnit
    public boolean i() {
        return !b();
    }

    public void M() {
        if (!this.isSubmerged) {
            S(1);
        } else {
            S(5);
        }
    }

    @Override // com.corrodinggames.rts.game.units.air.AirUnit, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f2) {
        float fFastSin;
        super.update(f2);
        if (!isAlive() || this.isDead) {
            return;
        }
        GameEngine gameEngine = GameEngine.getInstance();
        this.bobPhase += 2.0f * f2;
        if (this.bobPhase > 360.0f) {
            this.bobPhase -= 360.0f;
        }
        if (this.hasWaterEffect) {
            fFastSin = 20.0f + (Utility.fastSin(this.bobPhase) * 1.5f);
        } else {
            fFastSin = -8.0f;
        }
        if (this.hasWaterEffect && !Q()) {
            this.u = Utility.distanceSq(this.u, 0.0f, 0.018f * f2);
        } else {
            this.u = Utility.distanceSq(this.u, 1.0f, 0.018f * f2);
        }
        if (Utility.abs(this.posZ - fFastSin) > 3.0f) {
            float f3 = 0.6f;
            if (Q()) {
                f3 = 0.6f / 6.0f;
            }
            this.t = Utility.clamp(this.t, f3);
            this.t = Utility.distanceSq(this.t, f3, 0.006f * f2);
        } else {
            this.t = Utility.distanceSq(this.t, 0.07f, 0.006f * f2);
        }
        this.posZ = Utility.distanceSq(this.posZ, fFastSin, this.t * f2);
        boolean z2 = false;
        if (this.isSubmerged && Q()) {
            if (!isOverWater()) {
                this.hasWaterEffect = true;
            } else {
                this.isSubmerged = false;
                M();
                z2 = true;
            }
        }
        if (!this.isSubmerged && !Q()) {
            this.isSubmerged = true;
            M();
            z2 = true;
        }
        if (z2) {
            gameEngine.effectManager.createRedLaserEffect(this.posX, this.posY, 0.0f, 0, 0.0f, 0.0f);
            for (int i = -180; i < 180; i += 45) {
                float f4 = this.rotationSpeed + i;
                Effect effectCreateSmokeEffect = gameEngine.effectManager.createSmokeEffect((float) (((double) this.posX) + (Math.cos(Math.toRadians(f4)) * (-5.0d))), (float) (((double) this.posY) + (Math.sin(Math.toRadians(f4)) * (-5.0d))), 0.0f, f4);
                if (effectCreateSmokeEffect != null) {
                    effectCreateSmokeEffect.drawLayer = (short) 2;
                    effectCreateSmokeEffect.fadeOut = true;
                    effectCreateSmokeEffect.fadeDuration = 7.0f;
                }
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float q(int i) {
        if (i == ds()) {
            return 0.0f;
        }
        return 45.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void a(BaseUnit baseUnit, int i) {
        if (i == ds()) {
            return;
        }
        PointF pointFE = E(i);
        Projectile projectileA = Projectile.a(this, pointFE.x, pointFE.y, this.posZ, i);
        projectileA.color = Color.a(255, 247, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_EISU, 129);
        projectileA.damage = q(i);
        projectileA.targetUnit = baseUnit;
        projectileA.lifeTimer = 10.0f;
        projectileA.speed = 4.0f;
        projectileA.renderScale = 2.0f;
        projectileA.isSmallExplosion = false;
        projectileA.isInstantHit = true;
        projectileA.renderJitter = true;
        projectileA.damageToBuildings = 0.5f;
        projectileA.targetDamageMultiplier = 1.0f;
        projectileA.splashDamageMultiplier = 0.1f;
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.effectManager.createLightEffect(pointFE.x, pointFE.y, this.posZ, -1118482);
        gameEngine.soundEngine.playSound(SoundEngine.lightingBurstSound, 0.2f, this.posX, this.posY);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float m() {
        if (b()) {
            return 100.0f;
        }
        return 170.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float b(int i) {
        return 110.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float e(int i) {
        return 25 + (i * 10);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float f(int i) {
        return 0.2f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: z */
    public float getMoveSpeed() {
        if (!Q()) {
            return 1.4f;
        }
        return 0.4f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: A */
    public float getMaxTurnSpeed() {
        if (!Q()) {
            return 3.8f;
        }
        return 1.5f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float B() {
        return 0.3f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float c(int i) {
        return 4.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float w(int i) {
        return 0.35f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float y(int i) {
        return 0.38f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean E() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: C */
    public float getMoveAccelerationSpeed() {
        return 0.03f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float D() {
        return 0.1f;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: l */
    public boolean canAttack() {
        return true;
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

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: i */
    public void addRotation(float f2) {
        if (!hasAttackTarget()) {
            super.addRotation(f2);
            return;
        }
        this.rotationSpeed += f2;
        if (this.rotationSpeed > 180.0f) {
            this.rotationSpeed -= 360.0f;
        }
        if (this.rotationSpeed < -180.0f) {
            this.rotationSpeed += 360.0f;
        }
    }

    public int ds() {
        return 2;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: d */
    public float getRenderRotation(boolean z2) {
        return this.movementLevels[ds()].targetX + 90.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean ah() {
        if (!Q()) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: ae */
    public boolean canAttackSubmergedUnits() {
        if (!Q()) {
            return false;
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: af */
    public boolean canAttackFlyingUnits() {
        if (!Q()) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: ag */
    public boolean canAttackSurfaceUnits() {
        if (!Q()) {
            return true;
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: a */
    public void performUnitAction(AbstractUnitAction abstractUnitAction, boolean z2) {
        if (abstractUnitAction == y) {
            this.hasWaterEffect = true;
        }
        if (abstractUnitAction == z) {
            this.hasWaterEffect = false;
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: N */
    public ArrayList getAvailableActions() {
        return A;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public void e(float f2) {
        super.e(f2);
        GameViewUtils.a(this, m());
    }
}
