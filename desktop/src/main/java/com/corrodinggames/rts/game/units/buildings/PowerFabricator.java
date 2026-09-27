package com.corrodinggames.rts.game.units.buildings;

import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.PlayerTeam;
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
import com.corrodinggames.rts.gameFramework.effects.EffectQuality;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.local.Locale;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.d.h */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/d/h.class */
public class PowerFabricator extends FactoryWithQueue {

    /* JADX INFO: renamed from: h */
    float powerGenerationTimer;

    /* JADX INFO: renamed from: i */
    float animationTimer;

    /* JADX INFO: renamed from: j */
    int animationFrame;

    /* JADX INFO: renamed from: a */
    static Texture level1Texture = null;

    /* JADX INFO: renamed from: b */
    static Texture level2Texture = null;

    /* JADX INFO: renamed from: c */
    static Texture level3Texture = null;

    /* JADX INFO: renamed from: d */
    static Texture[] level1TeamTextures = new Texture[10];

    /* JADX INFO: renamed from: e */
    static Texture[] level2TeamTextures = new Texture[10];

    /* JADX INFO: renamed from: f */
    static Texture[] level3TeamTextures = new Texture[10];

    /* JADX INFO: renamed from: g */
    static Texture deadTexture = null;

    /* JADX INFO: renamed from: k */
    static AbstractUnitAction upgradeToLevel2Action = new PopupQueueAction(102) { // from class: com.corrodinggames.rts.game.units.d.h.1
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return Locale.get("units.fabricator.upgrade.description", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return Locale.get("units.fabricator.upgrade.name", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return UnitTypeEnum.fabricator.getUpgradeCost(2);
        }

        @Override // com.corrodinggames.rts.game.units.actions.PopupQueueAction
        public float K() {
            return 3.0E-4f;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z) {
            PowerFabricator powerFabricator = (PowerFabricator) baseUnit;
            if (powerFabricator.buildingAnimationState != 1 || powerFabricator.a(getActionId(), z) > 0) {
                return false;
            }
            return super.canAfford(baseUnit, z);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public boolean b(BaseUnit baseUnit) {
            if (((PowerFabricator) baseUnit).buildingAnimationState != 1) {
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
    };

    /* JADX INFO: renamed from: l */
    static AbstractUnitAction upgradeToLevel3Action = new PopupQueueAction(103) { // from class: com.corrodinggames.rts.game.units.d.h.2
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return Locale.get("units.fabricator.upgrade.descriptionT3", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return Locale.get("units.fabricator.upgrade.nameT3", new Object[0]);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return UnitTypeEnum.fabricator.getUpgradeCost(3);
        }

        @Override // com.corrodinggames.rts.game.units.actions.PopupQueueAction
        public float K() {
            return 2.0E-4f;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z) {
            PowerFabricator powerFabricator = (PowerFabricator) baseUnit;
            if (powerFabricator.buildingAnimationState != 2 || powerFabricator.a(getActionId(), z) > 0) {
                return false;
            }
            return super.canAfford(baseUnit, z);
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public boolean b(BaseUnit baseUnit) {
            if (((PowerFabricator) baseUnit).buildingAnimationState != 2) {
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
    };

    /* JADX INFO: renamed from: t */
    static ArrayList availableActions = new ArrayList();

    static {
        availableActions.add(upgradeToLevel2Action);
        availableActions.add(upgradeToLevel3Action);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeFloat(this.powerGenerationTimer);
        gameOutputStream.writeBoolean(this.buildingAnimationState == 2);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject
    public void a(GameInputStream gameInputStream) throws IOException {
        this.powerGenerationTimer = gameInputStream.readFloat();
        boolean z = gameInputStream.readBoolean();
        if (gameInputStream.getProtocolVersion() < 51 && z) {
            a(2);
        }
        super.a(gameInputStream);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.BaseBuilding
    /* JADX INFO: renamed from: R */
    public void setBuildingAnimationState(int i) {
        a(i);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: b, reason: merged with bridge method [inline-methods] */
    public UnitTypeEnum r() {
        return UnitTypeEnum.fabricator;
    }

    /* JADX INFO: renamed from: K */
    public static void loadTextures() {
        GameEngine gameEngine = GameEngine.getInstance();
        level1Texture = gameEngine.renderGraphicsEngine.a(R.drawable.power);
        level2Texture = gameEngine.renderGraphicsEngine.a(R.drawable.power_t2);
        level3Texture = gameEngine.renderGraphicsEngine.a(R.drawable.power_t3);
        level1TeamTextures = PlayerTeam.getTeamColorTextures(level1Texture);
        level2TeamTextures = PlayerTeam.getTeamColorTextures(level2Texture);
        level3TeamTextures = PlayerTeam.getTeamColorTextures(level3Texture);
        deadTexture = gameEngine.renderGraphicsEngine.a(R.drawable.power_dead);
    }

    public PowerFabricator(boolean z) {
        super(z);
        this.animationTimer = 0.0f;
        this.animationFrame = 0;
        this.baseTexture = level1Texture;
        a(this.baseTexture, 3);
        this.radius = 25.0f;
        this.displayRadius = this.radius;
        this.maxHealth = 800.0f;
        this.currentHealth = this.maxHealth;
        this.buildingTargetRect.a(-1, -1, 1, 1);
        this.buildingVelocityRect.a(this.buildingTargetRect);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.BaseBuilding
    /* JADX INFO: renamed from: L */
    public boolean onDeath() {
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.effectManager.createExplosion(this.posX, this.posY, this.posZ);
        this.baseTexture = deadTexture;
        S(0);
        this.isAlive = false;
        gameEngine.soundEngine.playSound(SoundEngine.buildingExplodeSound, 0.8f, this.posX, this.posY);
        gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
        Effect effectCreateSmallExplosion = gameEngine.effectManager.createSmallExplosion(this.posX, this.posY, this.posZ, -1127220);
        if (effectCreateSmallExplosion != null) {
            effectCreateSmallExplosion.scaleFrom = 0.15f;
            effectCreateSmallExplosion.scaleTo = 1.0f;
            effectCreateSmallExplosion.drawLayer = (short) 2;
            effectCreateSmallExplosion.lifeTimer = 35.0f;
            effectCreateSmallExplosion.lifeMax = effectCreateSmallExplosion.lifeTimer;
            effectCreateSmallExplosion.delayedStartTimer = 0.0f;
            effectCreateSmallExplosion.startColor = -14492382;
        }
        bo();
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture k() {
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture d() {
        if (this.isDead) {
            return deadTexture;
        }
        if (this.team == null) {
            return level1TeamTextures[level1TeamTextures.length - 1];
        }
        if (this.buildingAnimationState == 1) {
            return level1TeamTextures[this.team.getTeamColorIndex()];
        }
        if (this.buildingAnimationState == 2) {
            return level2TeamTextures[this.team.getTeamColorIndex()];
        }
        if (this.buildingAnimationState == 3) {
            return level3TeamTextures[this.team.getTeamColorIndex()];
        }
        GameEngine.log("Unknown tech level:" + this.buildingAnimationState);
        return level1TeamTextures[this.team.getTeamColorIndex()];
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryWithQueue, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f) {
        super.update(f);
        if (!isAlive() || this.isDead) {
            return;
        }
        this.animationTimer = Utility.moveTowardsZero(this.animationTimer, f);
        if (this.animationTimer == 0.0f) {
            this.animationTimer = 17.0f;
            this.animationFrame++;
            if (this.animationFrame > 5) {
                this.animationFrame = 0;
            }
            if (this.animationFrame <= 2) {
                this.currentAnimationFrame = this.animationFrame;
            } else {
                this.currentAnimationFrame = 5 - this.animationFrame;
            }
        }
        this.powerGenerationTimer += f;
        if (this.powerGenerationTimer > PlayerTeam.resourceIncomeUpdateInterval - 0.1f) {
            this.powerGenerationTimer -= PlayerTeam.resourceIncomeUpdateInterval;
            this.team.b(getCreditIncomeRate() * (PlayerTeam.resourceIncomeUpdateInterval / PlayerTeam.resourceIncomeRatePeriod));
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public float getCreditIncomeRate() {
        if (this.buildingAnimationState == 1) {
            return 2.0f;
        }
        if (this.buildingAnimationState == 2) {
            return 7.0f;
        }
        return 14.0f;
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
        if (this.buildingAnimationState == 1) {
            return upgradeToLevel2Action.getActionId();
        }
        if (this.buildingAnimationState == 2) {
            return upgradeToLevel3Action.getActionId();
        }
        return AbstractUnitAction.NONE_ACTION_ID;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: V */
    public int getUpgradeLevel() {
        return this.buildingAnimationState;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.BaseBuilding, com.corrodinggames.rts.game.units.OrderableUnit
    public void a(int i) {
        PlayerTeam.b((BaseUnit) this);
        if (this.buildingAnimationState > i) {
            this.buildingAnimationState = 1;
            this.maxHealth = 800.0f;
            if (this.currentHealth > this.maxHealth) {
                this.currentHealth = this.maxHealth;
            }
        }
        if (this.buildingAnimationState < 2 && i >= 2) {
            this.maxHealth += 500.0f;
            this.currentHealth += 500.0f;
        }
        if (this.buildingAnimationState < 3 && i >= 3) {
            this.maxHealth += 1300.0f;
            this.currentHealth += 1300.0f;
        }
        this.buildingAnimationState = i;
        PlayerTeam.c(this);
        S();
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: N */
    public ArrayList getAvailableActions() {
        return availableActions;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: db */
    public float getSelectionRadius() {
        return super.getSelectionRadius() - 8.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public int bp() {
        return 12;
    }
}
