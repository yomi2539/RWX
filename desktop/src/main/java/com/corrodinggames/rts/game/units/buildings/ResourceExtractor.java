package com.corrodinggames.rts.game.units.buildings;

import android.graphics.Rect;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.map.MapTile;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.UnitTypeEnum;
import com.corrodinggames.rts.game.units.actions.AbstractUnitAction;
import com.corrodinggames.rts.game.units.actions.ActionDisplayType;
import com.corrodinggames.rts.game.units.actions.ActionId;
import com.corrodinggames.rts.game.units.actions.PopupQueueAction;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.audio.SoundEngine;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.effects.EffectEmitter;
import com.corrodinggames.rts.gameFramework.effects.EffectQuality;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.local.Locale;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.d.g */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/d/g.class */
public class ResourceExtractor extends FactoryWithQueue {

    /* JADX INFO: renamed from: a */
    float resourceGenerationTimer;

    /* JADX INFO: renamed from: b */
    int extractorLevel;

    /* JADX INFO: renamed from: c */
    float animationTimer;

    /* JADX INFO: renamed from: d */
    int animationFrame;
    Rect j;
    Rect k;

    /* JADX INFO: renamed from: w */
    static ArrayList level2Actions;

    /* JADX INFO: renamed from: x */
    static ArrayList level3Actions;

    /* JADX INFO: renamed from: e */
    static Texture[] level1Textures = new Texture[10];

    /* JADX INFO: renamed from: f */
    static Texture[] level2Textures = new Texture[10];

    /* JADX INFO: renamed from: g */
    static Texture[] level3Textures = new Texture[10];

    /* JADX INFO: renamed from: h */
    static Texture backgroundTexture = null;

    /* JADX INFO: renamed from: i */
    static Texture deadTexture = null;
    public static int l = 0;

    /* JADX INFO: renamed from: t */
    static AbstractUnitAction upgradeToLevel2Action = new PopupQueueAction(102) { // from class: com.corrodinggames.rts.game.units.d.g.1
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return Locale.get("units.extractor.upgrade.description", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return Locale.get("gui.actions.upgradeT2", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return UnitTypeEnum.extractor.getUpgradeCost(2);
        }

        @Override // com.corrodinggames.rts.game.units.actions.PopupQueueAction
        public float K() {
            return 6.0E-4f;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z) {
            ResourceExtractor resourceExtractor = (ResourceExtractor) baseUnit;
            if (resourceExtractor.extractorLevel != 1 || resourceExtractor.a(getActionId(), z) > 0) {
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
            return ActionDisplayType.upgrade;
        }
    };

    /* JADX INFO: renamed from: u */
    static AbstractUnitAction upgradeToLevel3Action = new PopupQueueAction(103) { // from class: com.corrodinggames.rts.game.units.d.g.2
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return Locale.get("units.extractor.upgrade.descriptionT3", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return Locale.get("gui.actions.upgradeT3", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return UnitTypeEnum.extractor.getUpgradeCost(3);
        }

        @Override // com.corrodinggames.rts.game.units.actions.PopupQueueAction
        public float K() {
            return 3.0E-4f;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z) {
            ResourceExtractor resourceExtractor = (ResourceExtractor) baseUnit;
            if (resourceExtractor.extractorLevel != 2 || resourceExtractor.a(getActionId(), z) > 0) {
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
            return ActionDisplayType.upgrade;
        }
    };

    /* JADX INFO: renamed from: v */
    static ArrayList level1Actions = new ArrayList();

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeFloat(this.resourceGenerationTimer);
        gameOutputStream.writeBoolean(this.extractorLevel > 1);
        gameOutputStream.writeInt(this.extractorLevel);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject
    public void a(GameInputStream gameInputStream) throws IOException {
        this.resourceGenerationTimer = gameInputStream.readFloat();
        int i = 1;
        if (gameInputStream.readBoolean()) {
            i = 2;
        }
        if (gameInputStream.getProtocolVersion() >= 31) {
            i = gameInputStream.readInt();
        }
        if (i != 1) {
            a(i);
        }
        super.a(gameInputStream);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: b, reason: merged with bridge method [inline-methods] */
    public UnitTypeEnum r() {
        return UnitTypeEnum.extractor;
    }

    static {
        level1Actions.add(upgradeToLevel2Action);
        level2Actions = new ArrayList();
        level2Actions.add(upgradeToLevel3Action);
        level3Actions = new ArrayList();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: c */
    public boolean canPlaceAtCurrentPosition(PlayerTeam playerTeam) {
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.tileMap.setCursorTileIndexFromWorldPoint(this.posX, this.posY);
        MapTile pathingOverrideTileAt = gameEngine.tileMap.getPathingOverrideTileAt(gameEngine.tileMap.cursorTileX, gameEngine.tileMap.cursorTileY);
        if (pathingOverrideTileAt == null || !pathingOverrideTileAt.isResourcePool) {
            return false;
        }
        return super.canPlaceAtCurrentPosition(playerTeam);
    }

    /* JADX INFO: renamed from: K */
    public static void loadTextures() {
        GameEngine gameEngine = GameEngine.getInstance();
        Texture textureA = gameEngine.renderGraphicsEngine.a(R.drawable.extractor);
        Texture textureA2 = gameEngine.renderGraphicsEngine.a(R.drawable.extractor_t2);
        Texture textureA3 = gameEngine.renderGraphicsEngine.a(R.drawable.extractor_t3);
        deadTexture = gameEngine.renderGraphicsEngine.a(R.drawable.extractor_dead);
        level1Textures = PlayerTeam.getTeamColorTextures(textureA);
        level2Textures = PlayerTeam.getTeamColorTextures(textureA2);
        level3Textures = PlayerTeam.getTeamColorTextures(textureA3);
        textureA.n();
        textureA2.n();
        textureA3.n();
        backgroundTexture = gameEngine.renderGraphicsEngine.a(R.drawable.extractor_back);
    }

    public ResourceExtractor(boolean z) {
        super(z);
        this.extractorLevel = 1;
        this.animationTimer = 0.0f;
        this.animationFrame = 0;
        this.j = new Rect();
        this.k = new Rect();
        this.baseTexture = level1Textures[9];
        T(37);
        U(56);
        this.radius = 18.0f;
        this.displayRadius = this.radius;
        this.maxHealth = 800.0f;
        this.currentHealth = this.maxHealth;
        this.buildingTargetRect.a(0, -1, 0, 0);
        this.buildingVelocityRect.a(this.buildingTargetRect);
        S();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public int bp() {
        return 16;
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

    @Override // com.corrodinggames.rts.game.units.buildings.BaseBuilding
    /* JADX INFO: renamed from: ds */
    public boolean isBuildingActive() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture d() {
        if (this.isDead) {
            return deadTexture;
        }
        if (this.team == null) {
            return level1Textures[level1Textures.length - 1];
        }
        if (this.extractorLevel == 3) {
            return level3Textures[this.team.getTeamColorIndex()];
        }
        if (this.extractorLevel == 2) {
            return level2Textures[this.team.getTeamColorIndex()];
        }
        return level1Textures[this.team.getTeamColorIndex()];
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture k() {
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.BaseBuilding
    /* JADX INFO: renamed from: L */
    public boolean onDeath() {
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.effectManager.createExplosion(this.posX, this.posY, this.posZ);
        this.baseTexture = deadTexture;
        this.overlayTexture = null;
        S(0);
        this.isAlive = false;
        gameEngine.soundEngine.playSound(SoundEngine.buildingExplodeSound, 0.8f, this.posX, this.posY);
        EffectEmitter.createDefaultFireEmitter(this.posX, this.posY).startColorOverride = -6684775;
        EffectEmitter effectEmitterB = EffectEmitter.createAlternateFireEmitter(this.posX, this.posY);
        effectEmitterB.duration = 500.0f;
        effectEmitterB.startColorOverride = -6684775;
        gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
        Effect effectCreateSmallExplosion = gameEngine.effectManager.createSmallExplosion(this.posX, this.posY, this.posZ, -1127220);
        if (effectCreateSmallExplosion != null) {
            effectCreateSmallExplosion.scaleFrom = 0.15f;
            effectCreateSmallExplosion.scaleTo = 1.0f;
            effectCreateSmallExplosion.drawLayer = (short) 2;
            effectCreateSmallExplosion.lifeTimer = 35.0f;
            effectCreateSmallExplosion.lifeMax = effectCreateSmallExplosion.lifeTimer;
            effectCreateSmallExplosion.delayedStartTimer = 0.0f;
            effectCreateSmallExplosion.startColor = -13378253;
        }
        bo();
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f) {
        super.update(f);
        if (!isAlive() || this.isDead) {
            return;
        }
        this.animationTimer = Utility.moveTowardsZero(this.animationTimer, f * this.extractorLevel);
        if (this.animationTimer == 0.0f) {
            this.animationTimer = 17.0f;
            this.animationFrame++;
            if (this.animationFrame > 7) {
                this.animationFrame = 0;
            }
            if (this.animationFrame <= 3) {
                this.currentAnimationFrame = this.animationFrame;
            } else {
                this.currentAnimationFrame = 7 - this.animationFrame;
            }
        }
        this.resourceGenerationTimer += f;
        if (this.resourceGenerationTimer > PlayerTeam.resourceIncomeUpdateInterval - 0.1f) {
            this.resourceGenerationTimer -= PlayerTeam.resourceIncomeUpdateInterval;
            this.team.b(getCreditIncomeRate() * (PlayerTeam.resourceIncomeUpdateInterval / PlayerTeam.resourceIncomeRatePeriod));
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public float getCreditIncomeRate() {
        if (this.extractorLevel == 3) {
            return 18.0f;
        }
        if (this.extractorLevel == 2) {
            return 12.0f;
        }
        return 8.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public boolean c(float f) {
        return super.c(f);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: l */
    public boolean canAttack() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit
    public void a(BaseUnit baseUnit, int i) {
        throw new RuntimeException("Unit cannot shoot");
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit
    public float m() {
        return 0.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit
    public float b(int i) {
        return 0.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit
    public float c(int i) {
        return 0.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface
    public void a(Projectile projectile) {
        if (projectile.j.equals(upgradeToLevel2Action.getActionId())) {
            a(2);
            W();
        }
        if (projectile.j.equals(upgradeToLevel3Action.getActionId())) {
            a(3);
            W();
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public ActionId cm() {
        if (this.extractorLevel == 1) {
            return upgradeToLevel2Action.getActionId();
        }
        if (this.extractorLevel == 2) {
            return upgradeToLevel3Action.getActionId();
        }
        return AbstractUnitAction.NONE_ACTION_ID;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: V */
    public int getUpgradeLevel() {
        return this.extractorLevel;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit
    public void a(int i) {
        PlayerTeam.b((BaseUnit) this);
        if (this.extractorLevel > i) {
            this.extractorLevel = 1;
            this.maxHealth = 800.0f;
            if (this.currentHealth > this.maxHealth) {
                this.currentHealth = this.maxHealth;
            }
        }
        if (this.extractorLevel < 2 && i >= 2) {
            this.maxHealth += 200.0f;
            this.currentHealth += 200.0f;
        }
        if (this.extractorLevel < 3 && i >= 3) {
            this.maxHealth += 1000.0f;
            this.currentHealth += 1000.0f;
        }
        this.extractorLevel = i;
        PlayerTeam.c(this);
        S();
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: N */
    public ArrayList getAvailableActions() {
        if (this.extractorLevel == 1) {
            return level1Actions;
        }
        if (this.extractorLevel == 2) {
            return level2Actions;
        }
        return level3Actions;
    }
}
