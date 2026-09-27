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
import com.corrodinggames.rts.game.units.actions.*;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.audio.SoundEngine;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.local.Locale;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;

import java.io.IOException;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.d.c */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/d/c.class */
public class AntiNukeLauncher extends FactoryWithQueue {

    /* JADX INFO: renamed from: c */
    boolean isActive;

    /* JADX INFO: renamed from: d */
    int antiNukeCount;

    /* JADX INFO: renamed from: e */
    float scanTimer;

    /* JADX INFO: renamed from: h */
    PointF tempPoint;

    /* JADX INFO: renamed from: i */
    Rect drawRect;

    /* JADX INFO: renamed from: a */
    static Texture[] teamTextures = new Texture[10];

    /* JADX INFO: renamed from: b */
    static Texture deadTexture = null;

    /* JADX INFO: renamed from: f */
    static Texture iconTexture = null;

    /* JADX INFO: renamed from: g */
    static Texture[] iconTeamTextures = new Texture[10];

    /* JADX INFO: renamed from: j */
    static AbstractUnitAction antiNukeCountAction = new AbstractUnitAction(145) { // from class: com.corrodinggames.rts.game.units.d.c.1
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return VariableScope.nullOrMissingString;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return Locale.get("gui.actions.antiNukeCount", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return 0;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z) {
            if (getActiveCount(baseUnit, false) == 0) {
                return false;
            }
            return true;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: K, reason: merged with bridge method [inline-methods] */
        public UnitTypeEnum getUnitType() {
            return null;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public ActionType getActionType() {
            return ActionType.none;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public ActionDisplayType getActionDisplayType() {
            return ActionDisplayType.none;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public int getActiveCount(BaseUnit baseUnit, boolean z) {
            return ((AntiNukeLauncher) baseUnit).antiNukeCount;
        }
    };

    /* JADX INFO: renamed from: k */
    static AbstractUnitAction buildAntiNukeAction = new PopupQueueAction(144) { // from class: com.corrodinggames.rts.game.units.d.c.2
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return Locale.get("gui.actions.buildAntiNuke.description", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return Locale.get("gui.actions.buildAntiNuke", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return 4000;
        }

        @Override // com.corrodinggames.rts.game.units.actions.PopupQueueAction
        public float K() {
            return 7.0E-4f;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z) {
            AntiNukeLauncher antiNukeLauncher = (AntiNukeLauncher) baseUnit;
            if (antiNukeLauncher.antiNukeCount + antiNukeLauncher.a(getActionId(), z) >= 12.0f) {
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

    /* JADX INFO: renamed from: l */
    static ArrayList availableActions = new ArrayList();

    static {
        availableActions.add(antiNukeCountAction);
        availableActions.add(buildAntiNukeAction);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeBoolean(this.isActive);
        gameOutputStream.writeInt(this.antiNukeCount);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject
    public void a(GameInputStream gameInputStream) throws IOException {
        this.isActive = gameInputStream.readBoolean();
        if (gameInputStream.getProtocolVersion() >= 30) {
            this.antiNukeCount = gameInputStream.readInt();
        }
        super.a(gameInputStream);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.BaseUnit
    public Texture v() {
        if (this.team.teamId == -1) {
            return null;
        }
        return iconTeamTextures[this.team.getTeamColorIndex()];
    }

    /* JADX INFO: renamed from: b */
    public static void initializeTextures() {
        GameEngine gameEngine = GameEngine.getInstance();
        deadTexture = gameEngine.renderGraphicsEngine.a(R.drawable.antinuke_launcher_dead);
        Texture textureA = gameEngine.renderGraphicsEngine.a(R.drawable.antinuke_launcher);
        teamTextures = PlayerTeam.getTeamColorTextures(textureA);
        textureA.n();
        iconTexture = gameEngine.renderGraphicsEngine.a(R.drawable.unit_icon_building_turrent);
        iconTeamTextures = PlayerTeam.getTeamColorTextures(iconTexture);
    }

    public AntiNukeLauncher(boolean z) {
        super(z);
        this.tempPoint = new PointF();
        this.drawRect = new Rect();
        this.baseTexture = teamTextures[teamTextures.length - 1];
        b(this.baseTexture);
        this.radius = 24.0f;
        this.displayRadius = this.radius;
        this.maxHealth = 2800.0f;
        this.currentHealth = this.maxHealth;
        this.buildingTargetRect.a(-1, -1, 1, 1);
        this.buildingVelocityRect.a(-1, -1, 1, 1);
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

    @Override // com.corrodinggames.rts.game.units.buildings.BaseBuilding
    /* JADX INFO: renamed from: L */
    public boolean onDeath() {
        GameEngine.getInstance();
        this.baseTexture = deadTexture;
        S(0);
        this.isAlive = false;
        a(UnitSize.verylargeBuilding);
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f) {
        super.update(f);
        if (isAlive() && !this.isDead && this.antiNukeCount > 0) {
            Projectile projectile = null;
            this.scanTimer = Utility.moveTowardsZero(this.scanTimer, f);
            if (this.scanTimer == 0.0f) {
                this.scanTimer = 15.0f;
                for (Projectile projectile2 : Projectile.a) {
                    if (projectile2.isNuke && projectile2.posZ > 50.0f && Utility.distanceSq(this.posX, this.posY, projectile2.posX, projectile2.posY) < 2200.0f * 2200.0f && Utility.distanceSq(this.posX, this.posY, projectile2.targetX, projectile2.targetY) < 1000000.0f && (projectile2.sourceUnit == null || (!projectile2.sourceUnit.team.d(this.team) && projectile2.sourceUnit.team != this.team))) {
                        if (!isProjectileAlreadyTargeted(projectile2)) {
                            projectile = projectile2;
                        }
                    }
                }
            }
            if (projectile != null) {
                launchAntiNuke(projectile);
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public boolean isProjectileAlreadyTargeted(Projectile projectile) {
        Object[] objArrA = Projectile.a.a();
        int i = Projectile.a.size;
        for (int i2 = 0; i2 < i; i2++) {
            Projectile projectile2 = (Projectile) objArrA[i2];
            if (projectile2 != projectile && projectile2.parentProjectile == projectile) {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: b */
    public void launchAntiNuke(Projectile projectile) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.antiNukeCount <= 0) {
            return;
        }
        this.antiNukeCount--;
        PointF pointFE = E(0);
        Projectile projectileA = Projectile.a(this, pointFE.x, pointFE.y);
        projectileA.S(10);
        projectileA.textureFrame = (short) 10;
        projectileA.textureType = (short) 0;
        projectileA.renderScale = 1.0f;
        projectileA.fliesToPosition = true;
        projectileA.parentProjectile = projectile;
        projectileA.lifeTimer = 99999.0f;
        projectileA.speed = 0.2f;
        projectileA.targetSpeed = 6.5f;
        projectileA.color = Color.a(255, 80, 60, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_STB_INPUT);
        projectileA.damage = 600.0f;
        projectileA.isBallistic = true;
        projectileA.hasTrail = true;
        projectileA.isSmallExplosion = true;
        projectileA.C = true;
        projectileA.minHeight = 80.0f;
        projectileA.maxHeight = 100.0f;
        projectileA.verticalVelocity = 2.0f;
        gameEngine.effectManager.createLightEffect(pointFE.x, pointFE.y, this.posZ, -1127220);
        Effect effectCreateSmallExplosionInternal = gameEngine.effectManager.createSmallExplosionInternal(pointFE.x, pointFE.y, 0.0f, -1);
        if (effectCreateSmallExplosionInternal != null) {
            effectCreateSmallExplosionInternal.scaleFrom = 0.5f;
            effectCreateSmallExplosionInternal.scaleTo = 2.1f;
            effectCreateSmallExplosionInternal.drawLayer = (short) 2;
            effectCreateSmallExplosionInternal.lifeTimer = 90.0f;
            effectCreateSmallExplosionInternal.lifeMax = effectCreateSmallExplosionInternal.lifeTimer;
            effectCreateSmallExplosionInternal.delayedStartTimer = 0.0f;
        }
        gameEngine.soundEngine.playSoundAt(SoundEngine.nukeLaunchSound, 0.15f, 1.5f, pointFE.x, pointFE.y);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public PointF E(int i) {
        tempPointF2.a(this.posX, this.posY - 9.0f);
        return tempPointF2;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit
    public void a(BaseUnit baseUnit, int i) {
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit
    public float m() {
        return 1000.0f;
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
        return UnitTypeEnum.AntiNukeLaucher;
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

    /* JADX INFO: renamed from: M */
    public void addAntiNuke() {
        this.antiNukeCount++;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface
    public void a(com.corrodinggames.rts.game.units.buildings.Projectile projectile) {
        if (projectile.j.equals(buildAntiNukeAction.getActionId())) {
            addAntiNuke();
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public ActionId cm() {
        if (this.antiNukeCount < 4) {
            return buildAntiNukeAction.getActionId();
        }
        return AbstractUnitAction.NONE_ACTION_ID;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: ck */
    public boolean isUpgradeable() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: N */
    public ArrayList getAvailableActions() {
        return availableActions;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public void e(float f) {
        super.e(f);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void O() {
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void cb() {
        GameViewUtils.a((BaseUnit) this, 990.0f, false, true);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public boolean a(GameEngine gameEngine) {
        if (this.isSelected) {
            return true;
        }
        return super.a(gameEngine);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: a */
    public float applyDamage(BaseUnit baseUnit, float f, Projectile projectile) {
        if (f > 2600.0f) {
            f = 2600.0f;
        }
        return super.applyDamage(baseUnit, f, projectile);
    }
}
