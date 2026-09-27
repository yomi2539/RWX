package com.corrodinggames.rts.game.units.buildings.turrets;

import android.graphics.PointF;
import android.graphics.Rect;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.UnitSize;
import com.corrodinggames.rts.game.units.UnitTypeEnum;
import com.corrodinggames.rts.game.units.actions.AbstractUnitAction;
import com.corrodinggames.rts.game.units.actions.ActionDisplayType;
import com.corrodinggames.rts.game.units.actions.ActionId;
import com.corrodinggames.rts.game.units.actions.PopupQueueAction;
import com.corrodinggames.rts.game.units.buildings.FactoryWithQueue;
import com.corrodinggames.rts.game.units.buildings.Projectile;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.local.Locale;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.network.NetworkEngine;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;
import java.io.IOException;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.d.a.b */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/d/a/b.class */
public class TurretFactory extends FactoryWithQueue {

    /* JADX INFO: renamed from: j */
    boolean isUpgraded;

    /* JADX INFO: renamed from: k */
    int turretType;

    /* JADX INFO: renamed from: l */
    TurretImplementation turretImplementation;

    /* JADX INFO: renamed from: H */
    boolean isAnimating;

    /* JADX INFO: renamed from: I */
    float animationTargetAngle;

    /* JADX INFO: renamed from: J */
    float animationTimer;

    /* JADX INFO: renamed from: K */
    boolean animationDirection;

    /* JADX INFO: renamed from: dK */
    Rect drawRect;

    /* JADX INFO: renamed from: g */
    static Texture sharedBaseTexture = null;

    /* JADX INFO: renamed from: a */
    static Texture turretTopL1Texture = null;

    /* JADX INFO: renamed from: b */
    static Texture turretTopL2Texture = null;

    /* JADX INFO: renamed from: c */
    static Texture turretTopL3Texture = null;

    /* JADX INFO: renamed from: d */
    static Texture turretTopArtilleryTexture = null;

    /* JADX INFO: renamed from: e */
    static Texture turretTopFlameTexture = null;

    /* JADX INFO: renamed from: h */
    static Texture[] teamTextures = new Texture[10];

    /* JADX INFO: renamed from: i */
    static Texture deadTexture = null;

    /* JADX INFO: renamed from: t */
    static String GUN_TURRET_TYPE = "gun";

    /* JADX INFO: renamed from: u */
    static String GUN_TURRET_T2_TYPE = "gunT2";

    /* JADX INFO: renamed from: v */
    static String GUN_TURRET_T3_TYPE = "gunT3";

    /* JADX INFO: renamed from: w */
    static String ARTILLERY_TURRET_TYPE = "artillery";

    /* JADX INFO: renamed from: x */
    static String FLAMETHROWER_TURRET_TYPE = "flamethrower";

    /* JADX INFO: renamed from: C */
    static String AA_TURRET_T1_TYPE = "aa_t1";

    /* JADX INFO: renamed from: D */
    static String AA_TURRET_T2_TYPE = "aa_t2";

    /* JADX INFO: renamed from: E */
    static String AA_FLAK_TURRET_TYPE = "aa_flak";

    /* JADX INFO: renamed from: F */
    static Texture iconTexture = null;

    /* JADX INFO: renamed from: G */
    static Texture[] teamIconTextures = new Texture[10];

    /* JADX INFO: renamed from: dL */
    public static AbstractUnitAction upgradeToT2Action = new PopupQueueAction(101) { // from class: com.corrodinggames.rts.game.units.d.a.b.1
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "-Increases HP, attack damage, and range";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return Locale.get("gui.actions.upgradeToGunT2", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return 1000;
        }

        @Override // com.corrodinggames.rts.game.units.actions.PopupQueueAction
        public float K() {
            return 0.001f;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z) {
            TurretFactory turretFactory = (TurretFactory) baseUnit;
            if (turretFactory.getTurretTechLevel() != 1 || turretFactory.a(AbstractUnitAction.NONE_ACTION_ID, z) > 0) {
                return false;
            }
            return super.canAfford(baseUnit, z);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public boolean b(BaseUnit baseUnit) {
            if (((TurretFactory) baseUnit).getTurretTechLevel() != 1) {
                return false;
            }
            return true;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: L, reason: merged with bridge method [inline-methods] */
        public UnitTypeEnum getUnitType() {
            return null;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public ActionDisplayType getActionDisplayType() {
            return ActionDisplayType.upgrade;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public void onConfirmed(BaseUnit baseUnit) {
            TurretFactory turretFactory = (TurretFactory) baseUnit;
            turretFactory.setTurretTypeInternal(TurretFactory.GUN_TURRET_T2_TYPE);
            turretFactory.W();
        }
    };

    /* JADX INFO: renamed from: dM */
    public static AbstractUnitAction upgradeToT3Action = new PopupQueueAction(104) { // from class: com.corrodinggames.rts.game.units.d.a.b.2
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "-Extra attack damage, and range.\n-Large amount of HP\n-Self repair";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return Locale.get("gui.actions.upgradeToGunT3", new Object[0]);
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
            if (((TurretFactory) baseUnit).a(AbstractUnitAction.NONE_ACTION_ID, z) > 0) {
                return false;
            }
            return super.canAfford(baseUnit, z);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public boolean b(BaseUnit baseUnit) {
            if (!(((TurretFactory) baseUnit).turretImplementation instanceof GunTurretT2)) {
                return false;
            }
            return true;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: L, reason: merged with bridge method [inline-methods] */
        public UnitTypeEnum getUnitType() {
            return null;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public ActionDisplayType getActionDisplayType() {
            return ActionDisplayType.upgrade;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public void onConfirmed(BaseUnit baseUnit) {
            TurretFactory turretFactory = (TurretFactory) baseUnit;
            turretFactory.setTurretTypeInternal(TurretFactory.GUN_TURRET_T3_TYPE);
            turretFactory.W();
        }
    };

    /* JADX INFO: renamed from: dN */
    public static AbstractUnitAction upgradeToArtilleryAction = new PopupQueueAction(102) { // from class: com.corrodinggames.rts.game.units.d.a.b.3
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "-Large increase in range";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return Locale.get("gui.actions.upgradeToArtillery", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return 1600;
        }

        @Override // com.corrodinggames.rts.game.units.actions.PopupQueueAction
        public float K() {
            return 4.0E-4f;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z) {
            TurretFactory turretFactory = (TurretFactory) baseUnit;
            if (turretFactory.getTurretTechLevel() != 1 || turretFactory.a(AbstractUnitAction.NONE_ACTION_ID, z) > 0) {
                return false;
            }
            return super.canAfford(baseUnit, z);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public boolean b(BaseUnit baseUnit) {
            if (((TurretFactory) baseUnit).getTurretTechLevel() != 1) {
                return false;
            }
            return true;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: L, reason: merged with bridge method [inline-methods] */
        public UnitTypeEnum getUnitType() {
            return null;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public ActionDisplayType getActionDisplayType() {
            return ActionDisplayType.upgrade;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public void onConfirmed(BaseUnit baseUnit) {
            TurretFactory turretFactory = (TurretFactory) baseUnit;
            turretFactory.setTurretTypeInternal(TurretFactory.ARTILLERY_TURRET_TYPE);
            turretFactory.W();
        }
    };

    /* JADX INFO: renamed from: dO */
    public static AbstractUnitAction upgradeToFlamethrowerAction = new PopupQueueAction(103) { // from class: com.corrodinggames.rts.game.units.d.a.b.4
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "-Short range area affect\n-Adds self-repair";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return Locale.get("gui.actions.upgradeToFlamethrower", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return 700;
        }

        @Override // com.corrodinggames.rts.game.units.actions.PopupQueueAction
        public float K() {
            return 0.002f;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z) {
            TurretFactory turretFactory = (TurretFactory) baseUnit;
            if (turretFactory.getTurretTechLevel() != 1 || turretFactory.a(AbstractUnitAction.NONE_ACTION_ID, z) > 0) {
                return false;
            }
            return super.canAfford(baseUnit, z);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public boolean b(BaseUnit baseUnit) {
            if (((TurretFactory) baseUnit).getTurretTechLevel() != 1) {
                return false;
            }
            return true;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: L, reason: merged with bridge method [inline-methods] */
        public UnitTypeEnum getUnitType() {
            return null;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public ActionDisplayType getActionDisplayType() {
            return ActionDisplayType.upgrade;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public void onConfirmed(BaseUnit baseUnit) {
            TurretFactory turretFactory = (TurretFactory) baseUnit;
            turretFactory.setTurretTypeInternal(TurretFactory.FLAMETHROWER_TURRET_TYPE);
            turretFactory.W();
        }
    };

    /* JADX INFO: renamed from: dP */
    static ArrayList upgradeActions = new ArrayList();

    static {
        upgradeActions.add(upgradeToT2Action);
        upgradeActions.add(upgradeToT3Action);
        upgradeActions.add(upgradeToArtilleryAction);
        upgradeActions.add(upgradeToFlamethrowerAction);
    }

    /* JADX INFO: renamed from: M */
    public int getTurretTechLevel() {
        return this.turretImplementation.getTechLevel();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float H(int i) {
        return this.turretImplementation.getTurretOffset(i);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void a_(String str) {
        setTurretTypeInternal(str);
    }

    /* JADX INFO: renamed from: b */
    public void setTurretTypeInternal(String str) {
        if (!this.turretImplementation.isSameType(str)) {
            TurretImplementation turretImplementation = this.turretImplementation;
            this.turretImplementation = createTurretImplementation(str);
            this.turretImplementation.copyFrom(turretImplementation);
        }
    }

    /* JADX INFO: renamed from: c */
    public TurretImplementation createTurretImplementation(String str) {
        if (str.equals(GUN_TURRET_TYPE)) {
            return new GunTurret(this);
        }
        if (str.equals(GUN_TURRET_T2_TYPE)) {
            return new GunTurretT2(this);
        }
        if (str.equals(GUN_TURRET_T3_TYPE)) {
            return new GunTurretT3(this);
        }
        if (str.equals(ARTILLERY_TURRET_TYPE)) {
            return new ArtilleryTurret(this);
        }
        if (str.equals(FLAMETHROWER_TURRET_TYPE)) {
            return new FlamethrowerTurret(this);
        }
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeBoolean(this.isUpgraded);
        gameOutputStream.writeBoolean(this.turretType == 1);
        gameOutputStream.writeStringUTF(this.turretImplementation.c());
        gameOutputStream.writeInt(this.turretType);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject
    public void a(GameInputStream gameInputStream) throws IOException {
        boolean z = gameInputStream.readBoolean();
        if (z) {
            a(2);
        }
        if (gameInputStream.getProtocolVersion() >= 27) {
            this.turretType = gameInputStream.readBoolean() ? 1 : 0;
        }
        if (gameInputStream.getProtocolVersion() >= 35) {
            String utf = gameInputStream.readUTF();
            if (!this.turretImplementation.isSameType(utf)) {
                setTurretTypeInternal(utf);
            }
            this.turretType = gameInputStream.readInt();
        } else if (z && !(this instanceof AntiAirTurret)) {
            setTurretTypeInternal(GUN_TURRET_T2_TYPE);
        }
        super.a(gameInputStream);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.BaseUnit
    public Texture v() {
        if (this.team.teamId == -1) {
            return null;
        }
        return teamIconTextures[this.team.getTeamColorIndex()];
    }

    /* JADX INFO: renamed from: dB */
    public static void loadTextures() {
        GameEngine gameEngine = GameEngine.getInstance();
        sharedBaseTexture = gameEngine.renderGraphicsEngine.a(R.drawable.turret_base);
        deadTexture = gameEngine.renderGraphicsEngine.a(R.drawable.turret_base_dead);
        turretTopL1Texture = gameEngine.renderGraphicsEngine.a(R.drawable.turret_top);
        turretTopL2Texture = gameEngine.renderGraphicsEngine.a(R.drawable.turret_top_l2);
        turretTopL3Texture = gameEngine.renderGraphicsEngine.a(R.drawable.turret_top_l3);
        turretTopArtilleryTexture = gameEngine.renderGraphicsEngine.a(R.drawable.turret_top_artillery);
        turretTopFlameTexture = gameEngine.renderGraphicsEngine.a(R.drawable.turret_top_flame);
        teamTextures = PlayerTeam.getTeamColorTextures(sharedBaseTexture);
        iconTexture = gameEngine.renderGraphicsEngine.a(R.drawable.unit_icon_building_turrent);
        teamIconTextures = PlayerTeam.getTeamColorTextures(iconTexture);
    }

    public TurretFactory(boolean z) {
        super(z);
        this.turretImplementation = new GunTurret(this);
        this.isAnimating = true;
        this.drawRect = new Rect();
        T(35);
        U(42);
        this.radius = 16.0f;
        this.displayRadius = this.radius;
        this.maxHealth = 700.0f;
        this.currentHealth = this.maxHealth;
        super.baseTexture = sharedBaseTexture;
        this.movementLevels[0].targetX = Utility.getDeterministicRandomInt(this, -180, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_STB_INPUT);
        this.buildingTargetRect.a(0, 0, 1, 1);
        this.buildingVelocityRect.a(0, 0, 1, 1);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture d() {
        if (this.isDead) {
            return deadTexture;
        }
        if (this.team == null) {
            return teamTextures[teamTextures.length - 1];
        }
        return teamTextures[this.team.getTeamColorIndex()];
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture k() {
        return null;
    }

    @Override
    // com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit
    public Texture d(int i) {
        return this.turretImplementation.getTurretTopTexture(i);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.BaseBuilding
    /* JADX INFO: renamed from: L */
    public boolean onDeath() {
        super.baseTexture = deadTexture;
        S(0);
        this.isAlive = false;
        a(UnitSize.large);
        return true;
    }

    /* JADX INFO: renamed from: s */
    public void updateTurretRotation(float f) {
        if (this.movementLevels[0].a()) {
            if (this.isAnimating) {
                this.animationTargetAngle = this.movementLevels[0].targetX;
                this.isAnimating = false;
                this.animationTimer = Utility.getDeterministicRandomInt(this, 0, 120);
            }
            this.animationTimer += f;
            if (this.animationTimer > 450.0f) {
                this.animationTimer = Utility.getDeterministicRandomInt(this, 0, 30);
                this.animationDirection = !this.animationDirection;
            }
            if (this.animationTimer < 120.0f) {
                if (this.animationDirection) {
                    a(f * 0.3f, this.animationTargetAngle - 20.0f, 0);
                    return;
                } else {
                    a(f * 0.3f, this.animationTargetAngle + 20.0f, 0);
                    return;
                }
            }
            return;
        }
        this.isAnimating = true;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f) {
        super.update(f);
        if (isAlive()) {
            this.turretImplementation.update(f);
        }
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit
    public void a(BaseUnit baseUnit, int i) {
        this.turretImplementation.fireProjectile(baseUnit, i);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit
    public float m() {
        return this.turretImplementation.getAttackRange();
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit
    public float b(int i) {
        return this.turretImplementation.getAttackDelay(i);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit
    public float c(int i) {
        return this.turretImplementation.getTurretRestingRotationSpeed(i);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float w(int i) {
        return this.turretImplementation.getTurretTurnSpeed(i);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean b(int i, float f) {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public boolean c(float f) {
        if (!super.c(f)) {
            return false;
        }
        if (!this.isDead) {
            drawTurret();
            return true;
        }
        return true;
    }

    /* JADX INFO: renamed from: dC */
    void drawTurret() {
        GameEngine gameEngine = GameEngine.getInstance();
        Texture textureD = d(0);
        PointF pointFG = G(0);
        gameEngine.renderGraphicsEngine.a(textureD, pointFG.x - GameEngine.getInstance().viewpointXSnapped, pointFG.y - GameEngine.getInstance().viewpointYSnapped, this.movementLevels[0].targetX, getBuildingPaint());
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: K, reason: merged with bridge method [inline-methods] */
    public UnitTypeEnum r() {
        return UnitTypeEnum.turret;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.BaseUnit
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
    public float g(int i) {
        return this.turretImplementation.getTurretHeight(i);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void M(int i) {
        if (b(i) < 10.0f) {
            return;
        }
        super.M(i);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface
    public void a(Projectile projectile) {
        AbstractUnitAction abstractUnitActionA = validateActionId(projectile.j);
        if (abstractUnitActionA != null) {
            abstractUnitActionA.onConfirmed(this);
        } else {
            NetworkEngine.reportDesyncVerbose("specialAction=null on completeQueueItem(turret) for item.uIndex:" + projectile.j + " id:" + this.objectId, true);
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public ActionId cm() {
        if (getTurretTechLevel() == 1) {
            return upgradeToT2Action.getActionId();
        }
        if (this.turretImplementation instanceof GunTurretT2) {
            return upgradeToT3Action.getActionId();
        }
        return AbstractUnitAction.NONE_ACTION_ID;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: a */
    public void clearAndAddAction(ArrayList arrayList) {
        arrayList.clear();
        if (getTurretTechLevel() == 1) {
            arrayList.add(upgradeToArtilleryAction.getActionId());
            arrayList.add(upgradeToFlamethrowerAction.getActionId());
        }
    }

    @Override // com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit
    public void a(int i) {
        if (i == 1) {
            this.isUpgraded = false;
        } else if (i == 2 && !this.isUpgraded) {
            this.isUpgraded = true;
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public PointF E(int i) {
        return this.turretImplementation.getTurretType(i);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.BaseUnit
    public float bV() {
        if (this.movementLevels[0].rotation > 0.0f && this.turretImplementation.isSameType(ARTILLERY_TURRET_TYPE)) {
            return 1.0f - (this.movementLevels[0].rotation / b(0));
        }
        return super.bV();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public PointF G(int i) {
        tempPointF3.a(super.G(i));
        tempPointF3.b(0.0f, -5.0f);
        return tempPointF3;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: N */
    public ArrayList getAvailableActions() {
        return upgradeActions;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public void e(float f) {
        super.e(f);
        GameViewUtils.a(this, m());
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cZ */
    public float getTileOffsetX() {
        return GameEngine.getInstance().tileMap.tileWorldSizeX;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: da */
    public float getTileOffsetY() {
        return GameEngine.getInstance().tileMap.tileWorldSizeY;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: db */
    public float getSelectionRadius() {
        return super.getSelectionRadius() - 8.0f;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public int cL() {
        return this.turretImplementation.d();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float q(int i) {
        return this.turretImplementation.getAttackDamage(i);
    }
}
