package com.corrodinggames.rts.game.units.buildings;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.UnitSize;
import com.corrodinggames.rts.game.units.UnitTypeEnum;
import com.corrodinggames.rts.game.units.actions.AbstractUnitAction;
import com.corrodinggames.rts.game.units.actions.ActionDisplayType;
import com.corrodinggames.rts.game.units.actions.ActionId;
import com.corrodinggames.rts.game.units.actions.ActionType;
import com.corrodinggames.rts.game.units.actions.PopupQueueAction;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.audio.SoundEngine;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.effects.EffectQuality;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.local.Locale;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.network.NetworkEngine;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;
import java.io.IOException;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.d.q */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/d/q.class */
public class NukeLauncher extends FactoryWithQueue {

    /* JADX INFO: renamed from: c */
    int nukeCount;

    /* JADX INFO: renamed from: f */
    PointF tempPoint;

    /* JADX INFO: renamed from: g */
    Rect drawRect;

    /* JADX INFO: renamed from: a */
    static Texture[] teamTextures = new Texture[10];

    /* JADX INFO: renamed from: b */
    static Texture deadTexture = null;

    /* JADX INFO: renamed from: d */
    static Texture iconTexture = null;

    /* JADX INFO: renamed from: e */
    static Texture[] teamIconTextures = new Texture[10];

    /* JADX INFO: renamed from: h */
    static AbstractUnitAction launchNukeAction = new AbstractUnitAction(142) { // from class: com.corrodinggames.rts.game.units.d.q.1
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return Locale.get("gui.actions.launchNuke", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return Locale.get("gui.actions.launchNuke", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return 0;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public int getActiveCount(BaseUnit baseUnit, boolean z) {
            return ((NukeLauncher) baseUnit).nukeCount;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: K, reason: merged with bridge method [inline-methods] */
        public UnitTypeEnum getUnitType() {
            return null;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public ActionType getActionType() {
            return ActionType.targetGround;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public ActionDisplayType getActionDisplayType() {
            return ActionDisplayType.action;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z) {
            return ((NukeLauncher) baseUnit).nukeCount > 0;
        }
    };

    /* JADX INFO: renamed from: i */
    static AbstractUnitAction buildNukeAction = new PopupQueueAction(143) { // from class: com.corrodinggames.rts.game.units.d.q.2
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return Locale.get("gui.actions.buildNuke.description", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return Locale.get("gui.actions.buildNuke", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return 11000;
        }

        @Override // com.corrodinggames.rts.game.units.actions.PopupQueueAction
        public float K() {
            return 3.0E-4f;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z) {
            NukeLauncher nukeLauncher = (NukeLauncher) baseUnit;
            if (nukeLauncher.nukeCount + nukeLauncher.a(getActionId(), z) >= 4.0f) {
                return false;
            }
            return super.canAfford(baseUnit, z);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: L, reason: merged with bridge method [inline-methods] */
        public UnitTypeEnum getUnitType() {
            return null;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public ActionDisplayType getActionDisplayType() {
            return ActionDisplayType.queueUnit;
        }
    };

    /* JADX INFO: renamed from: j */
    static ArrayList availableActions = new ArrayList();

    static {
        availableActions.add(launchNukeAction);
        availableActions.add(buildNukeAction);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeInt(this.nukeCount);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject
    public void a(GameInputStream gameInputStream) throws IOException {
        this.nukeCount = gameInputStream.readInt();
        super.a(gameInputStream);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.BaseUnit
    public Texture v() {
        if (this.team.teamId == -1) {
            return null;
        }
        return teamIconTextures[this.team.getTeamColorIndex()];
    }

    public static void b() {
        GameEngine gameEngine = GameEngine.getInstance();
        deadTexture = gameEngine.renderGraphicsEngine.a(R.drawable.nuke_launcher_dead);
        Texture textureA = gameEngine.renderGraphicsEngine.a(R.drawable.nuke_launcher);
        teamTextures = PlayerTeam.getTeamColorTextures(textureA);
        textureA.n();
        iconTexture = gameEngine.renderGraphicsEngine.a(R.drawable.unit_icon_building);
        teamIconTextures = PlayerTeam.getTeamColorTextures(iconTexture);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public int bp() {
        return 20;
    }

    public NukeLauncher(boolean z) {
        super(z);
        this.tempPoint = new PointF();
        this.drawRect = new Rect();
        this.baseTexture = teamTextures[teamTextures.length - 1];
        b(this.baseTexture);
        this.radius = 40.0f;
        this.displayRadius = this.radius;
        this.maxHealth = 1500.0f;
        this.currentHealth = this.maxHealth;
        this.buildingTargetRect.a(-2, -1, 2, 1);
        this.buildingVelocityRect.a(-2, -1, 2, 2);
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

    @Override
    // com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit
    public void a(int i) {
    }

    public static Projectile a(BaseUnit baseUnit, float f, float f2, float f3, float f4) {
        Projectile projectileA = Projectile.a(baseUnit, f, f2);
        projectileA.S(10);
        projectileA.textureFrame = (short) 0;
        projectileA.frameIndex = (short) 1;
        projectileA.textureType = (short) 1;
        projectileA.renderScale = 1.0f;
        projectileA.fliesToPosition = true;
        projectileA.hasFixedTarget = true;
        projectileA.targetX = f3;
        projectileA.targetY = f4;
        projectileA.lifeTimer = 99999.0f;
        projectileA.speed = 0.1f;
        projectileA.targetSpeed = 2.7f;
        projectileA.color = Color.a(255, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_PAIRING, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_PAIRING, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_PAIRING);
        projectileA.damage = 300.0f;
        projectileA.isBallistic = true;
        projectileA.hasTrail = true;
        projectileA.isSmallExplosion = true;
        projectileA.C = true;
        projectileA.isNuke = true;
        projectileA.minHeight = 80.0f;
        projectileA.maxHeight = 100.0f;
        projectileA.verticalVelocity = 1.1f;
        projectileA.splashDamage = 5400.0f;
        projectileA.explosionRadius = 250.0f;
        projectileA.excludesAir = true;
        projectileA.matchesTargetAltitude = false;
        projectileA.trackHitUnits = true;
        projectileA.explosionAnimTimer = 75.0f;
        projectileA.explosionAnimDuration = projectileA.explosionAnimTimer;
        projectileA.revealsFog = true;
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
        Effect effectCreateLightEffect = gameEngine.effectManager.createLightEffect(projectileA, -1118720);
        if (effectCreateLightEffect != null) {
            effectCreateLightEffect.lifeTimer = 1300.0f;
            effectCreateLightEffect.lifeMax = effectCreateLightEffect.lifeTimer;
            effectCreateLightEffect.alpha = 0.2f;
            effectCreateLightEffect.scaleFrom = 1.0f;
        }
        return projectileA;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f) {
        super.update(f);
        if (!isAlive() || this.isDead) {
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public PointF E(int i) {
        tempPointF2.a(this.posX, this.posY - 3.0f);
        return tempPointF2;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit
    public void a(BaseUnit baseUnit, int i) {
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit
    public float c(int i) {
        return 4.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean b(int i, float f) {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public boolean c(float f) {
        return super.c(f);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: K, reason: merged with bridge method [inline-methods] */
    public UnitTypeEnum r() {
        return UnitTypeEnum.NukeLaucher;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: l */
    public boolean canAttack() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float g(int i) {
        return 1.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.BaseUnit
    public float bV() {
        return super.bV();
    }

    @Override // com.corrodinggames.rts.game.units.buildings.BaseBuilding
    /* JADX INFO: renamed from: L */
    public boolean onDeath() {
        GameEngine gameEngine = GameEngine.getInstance();
        this.baseTexture = deadTexture;
        S(0);
        this.isAlive = false;
        a(UnitSize.verylargeBuilding);
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
        gameEngine.effectManager.createExplosionWithVelocity(this.posX, this.posY, this.posZ, 40.0f, 120.0f);
        return true;
    }

    public void a(float f, float f2) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.nukeCount <= 0) {
            return;
        }
        if (Utility.distanceSq(this.posX, this.posY, f, f2) < 202500.0f) {
            if (this.team == gameEngine.playerTeam) {
                gameEngine.gameUI.showMediumPriorityMessage("Nuke target too close");
                return;
            }
            return;
        }
        this.nukeCount--;
        PointF pointFE = E(0);
        a(this, pointFE.x, pointFE.y, f, f2).initialDelay = 5.0f;
        Effect effectCreateLightEffect = gameEngine.effectManager.createLightEffect(pointFE.x, pointFE.y, this.posZ, -1127220);
        if (effectCreateLightEffect != null) {
            effectCreateLightEffect.delayedStartTimer = 5.0f;
            effectCreateLightEffect.scaleFrom = 2.1f;
            effectCreateLightEffect.scaleTo = 2.1f;
            effectCreateLightEffect.drawLayer = (short) 2;
            effectCreateLightEffect.fadeOut = true;
            effectCreateLightEffect.fadeDuration = 70.0f;
            effectCreateLightEffect.lifeTimer = 370.0f;
            effectCreateLightEffect.lifeMax = effectCreateLightEffect.lifeTimer;
            effectCreateLightEffect.alpha = 1.0f;
        }
        Effect effectCreateSmallExplosionInternal = gameEngine.effectManager.createSmallExplosionInternal(pointFE.x, pointFE.y, 0.0f, -1);
        if (effectCreateSmallExplosionInternal != null) {
            effectCreateSmallExplosionInternal.scaleFrom = 1.0f;
            effectCreateSmallExplosionInternal.scaleTo = 3.1f;
            effectCreateSmallExplosionInternal.drawLayer = (short) 2;
            effectCreateSmallExplosionInternal.lifeTimer = 170.0f;
            effectCreateSmallExplosionInternal.lifeMax = effectCreateSmallExplosionInternal.lifeTimer;
            effectCreateSmallExplosionInternal.delayedStartTimer = 5.0f + 20.0f;
        }
        gameEngine.soundEngine.playSoundAt(SoundEngine.nukeLaunchSound, 0.27f, 0.8f, pointFE.x, pointFE.y);
    }

    /* JADX INFO: renamed from: M */
    public void initializeTextures() {
        this.nukeCount++;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface
    public void a(com.corrodinggames.rts.game.units.buildings.Projectile projectile) {
        if (projectile.j.equals(buildNukeAction.getActionId())) {
            initializeTextures();
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public ActionId cm() {
        return AbstractUnitAction.NONE_ACTION_ID;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void a(AbstractUnitAction abstractUnitAction, boolean z, PointF pointF, BaseUnit baseUnit) {
        if (z) {
            return;
        }
        if (abstractUnitAction == launchNukeAction) {
            if (pointF == null) {
                NetworkEngine.reportDesync("action:" + launchNukeAction.getActionId() + " needs point but it is missing");
                return;
            } else {
                a(pointF.x, pointF.y);
                return;
            }
        }
        super.a(abstractUnitAction, z, pointF, baseUnit);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: N */
    public ArrayList getAvailableActions() {
        return availableActions;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public void e(float f) {
        super.e(f);
        GameViewUtils.b((BaseUnit) this, 450.0f, false);
    }
}
