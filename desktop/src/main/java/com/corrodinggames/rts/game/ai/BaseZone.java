package com.corrodinggames.rts.game.ai;

import android.graphics.PointF;
import com.corrodinggames.rts.game.map.MapTile;
import com.corrodinggames.rts.game.units.*;
import com.corrodinggames.rts.game.units.actions.AbstractUnitAction;
import com.corrodinggames.rts.game.units.buildings.BaseBuilding;
import com.corrodinggames.rts.game.units.buildings.CommandCenter;
import com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface;
import com.corrodinggames.rts.game.units.custom.AnimationSet;
import com.corrodinggames.rts.game.units.custom.AnimationTag;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig;
import com.corrodinggames.rts.game.units.custom.price.UnitPrice;
import com.corrodinggames.rts.game.units.management.UnitListIterator;
import com.corrodinggames.rts.gameFramework.Command;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.utility.FastArrayList;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;
import com.corrodinggames.rts.gameFramework.utility.UnitList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.a.i */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/a/i.class */
public class BaseZone extends AIStrategyNode {

    /* JADX INFO: renamed from: a */
    float resourceScore;

    /* JADX INFO: renamed from: b */
    BaseZoneStage stage;

    /* JADX INFO: renamed from: c */
    BaseZoneType zoneType;

    /* JADX INFO: renamed from: d */
    float reclaimScore;

    /* JADX INFO: renamed from: e */
    float defensiveScore;

    /* JADX INFO: renamed from: f */
    float lastFoundBuildingPriority;

    /* JADX INFO: renamed from: g */
    float updateTimer;

    /* JADX INFO: renamed from: h */
    int lastAttackedTimer;

    /* JADX INFO: renamed from: i */
    float landUnitsScore;

    /* JADX INFO: renamed from: j */
    float airUnitsScore;

    /* JADX INFO: renamed from: k */
    float waterUnitsScore;

    /* JADX INFO: renamed from: l */
    float extractorScore;

    /* JADX INFO: renamed from: m */
    float lastTimeAttackedByEnemy;

    /* JADX INFO: renamed from: n */
    boolean hasResources;

    /* JADX INFO: renamed from: o */
    boolean hasLandAccess;

    /* JADX INFO: renamed from: p */
    FastArrayList unitTypesInZone;

    /* JADX INFO: renamed from: q */
    UnitList unitsInZone;

    /* JADX INFO: renamed from: r */
    UnitList buildingsInZone;

    /* JADX INFO: renamed from: s */
    boolean isUnderAttack;

    /* JADX INFO: renamed from: t */
    boolean isContested;

    /* JADX INFO: renamed from: u */
    float lastTimeScouted;

    /* JADX INFO: renamed from: v */
    float lastTimeReclaimed;

    /* JADX INFO: renamed from: w */
    PointF tempPoint1;

    /* JADX INFO: renamed from: x */
    PointF tempPoint2;

    /* JADX INFO: renamed from: y */
    int lastTimeBuilt;

    /* JADX INFO: renamed from: z */
    UnitType lastBuiltUnitType;

    /* JADX INFO: renamed from: A */
    UnitPrice lastBuiltCustomUnit;

    /* JADX INFO: renamed from: B */
    UnitPrice lastBuiltCustomUnit2;

    /* JADX INFO: renamed from: C */
    int numberOfLandUnits;

    /* JADX INFO: renamed from: D */
    int numberOfAirUnits;

    /* JADX INFO: renamed from: E */
    String debugText;

    /* JADX INFO: renamed from: F */
    int numberOfWaterUnits;

    /* JADX INFO: renamed from: G */
    int numberOfBuildings;

    /* JADX INFO: renamed from: H */
    boolean isInitialized;

    /* JADX INFO: renamed from: I */
    int numberOfExtractors;

    /* JADX INFO: renamed from: J */
    int numberOfFactories;

    /* JADX INFO: renamed from: K */
    int numberOfCombatUnits;

    /* JADX INFO: renamed from: L */
    int numberOfIdleCombatUnits;

    /* JADX INFO: renamed from: M */
    boolean isPrimary;

    /* JADX INFO: renamed from: N */
    ArrayList rallyPoints;

    /* JADX INFO: renamed from: O */
    UnitType lastBuiltFactoryType;

    /* JADX INFO: renamed from: P */
    UnitPrice lastBuiltFactoryCustomUnit;

    @Override // com.corrodinggames.rts.game.ai.AIStrategyNode, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeEnumOrdinal(this.stage);
        gameOutputStream.writeEnumOrdinal(this.zoneType);
        gameOutputStream.writeFloat(this.reclaimScore);
        gameOutputStream.writeFloat(this.defensiveScore);
        gameOutputStream.writeFloat(this.lastFoundBuildingPriority);
        gameOutputStream.writeFloat(this.updateTimer);
        gameOutputStream.writeFloat(this.landUnitsScore);
        gameOutputStream.writeFloat(this.airUnitsScore);
        gameOutputStream.writeFloat(this.waterUnitsScore);
        gameOutputStream.writeFloat(this.extractorScore);
        gameOutputStream.writeByte(4);
        gameOutputStream.writeFloat(this.lastTimeReclaimed);
        gameOutputStream.writeFloat(this.lastTimeAttackedByEnemy);
        gameOutputStream.writeBoolean(this.hasResources);
        gameOutputStream.writeBoolean(this.hasLandAccess);
        gameOutputStream.writeInt(this.lastAttackedTimer);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.game.ai.AIStrategyNode
    /* JADX INFO: renamed from: a */
    public void readFromInputStream(GameInputStream gameInputStream) throws IOException {
        this.stage = (BaseZoneStage) gameInputStream.readEnumOrdinalOrNull(BaseZoneStage.class);
        this.zoneType = (BaseZoneType) gameInputStream.readEnumOrdinalOrNull(BaseZoneType.class);
        this.reclaimScore = gameInputStream.readFloat();
        this.defensiveScore = gameInputStream.readFloat();
        this.lastFoundBuildingPriority = gameInputStream.readFloat();
        this.updateTimer = gameInputStream.readFloat();
        this.landUnitsScore = gameInputStream.readFloat();
        this.airUnitsScore = gameInputStream.readFloat();
        this.waterUnitsScore = gameInputStream.readFloat();
        this.extractorScore = gameInputStream.readFloat();
        byte b = gameInputStream.readByte();
        if (b >= 1) {
            this.lastTimeReclaimed = gameInputStream.readFloat();
        }
        if (b >= 2) {
            this.lastTimeAttackedByEnemy = gameInputStream.readFloat();
        }
        if (b >= 3) {
            this.hasResources = gameInputStream.readBoolean();
            this.hasLandAccess = gameInputStream.readBoolean();
        }
        if (b >= 4) {
            this.lastAttackedTimer = gameInputStream.readInt();
        }
        super.readFromInputStream(gameInputStream);
    }

    public BaseZone(AIController aIController, float f, float f2) {
        super(aIController, f, f2);
        this.reclaimScore = -1.0f;
        this.updateTimer = 100.0f;
        this.landUnitsScore = 50.0f;
        this.airUnitsScore = 50.0f;
        this.unitTypesInZone = new FastArrayList();
        this.unitsInZone = new UnitList();
        this.buildingsInZone = new UnitList();
        this.lastTimeReclaimed = 0.0f;
        this.tempPoint1 = new PointF();
        this.tempPoint2 = new PointF();
        this.isInitialized = false;
        this.rallyPoints = new ArrayList();
    }

    /* JADX INFO: renamed from: a */
    public PointF findClosestResource() {
        MapTile pathingOverrideTileAt;
        GameEngine gameEngine = GameEngine.getInstance();
        PointF pointF = null;
        int i = (int) (this.radius * gameEngine.tileMap.tileScaleX);
        gameEngine.tileMap.setCursorTileIndexFromWorldPoint(this.posX, this.posY);
        int i2 = gameEngine.tileMap.cursorTileX;
        int i3 = gameEngine.tileMap.cursorTileY;
        for (int i4 = i2 - i; i4 <= i2 + i; i4++) {
            for (int i5 = i3 - i; i5 <= i3 + i; i5++) {
                if (gameEngine.tileMap.isInBounds(i4, i5) && (pathingOverrideTileAt = gameEngine.tileMap.getPathingOverrideTileAt(i4, i5)) != null && pathingOverrideTileAt.isResourcePool) {
                    BaseUnit baseUnitB = BaseBuilding.b(i4, i5);
                    boolean z = false;
                    if (baseUnitB == null) {
                        z = true;
                    }
                    if (baseUnitB != null && (baseUnitB instanceof OrderableUnit) && !((OrderableUnit) baseUnitB).r().p()) {
                        z = true;
                    }
                    if (z) {
                        gameEngine.tileMap.setCursorTileIndexFromTileIndex(i4, i5);
                        if (pointF == null || Utility.getRandomIntInRange(0, 100) < 50) {
                            this.tempPoint1.a(gameEngine.tileMap.cursorTileX + gameEngine.tileMap.halfTileWorldSizeX, gameEngine.tileMap.cursorTileY + gameEngine.tileMap.halfTileWorldSizeY);
                            pointF = this.tempPoint1;
                        }
                    }
                }
            }
        }
        return pointF;
    }

    /* JADX INFO: renamed from: a */
    public void removeUnit(OrderableUnit orderableUnit) {
        this.unitsInZone.remove(orderableUnit);
    }

    /* JADX INFO: renamed from: b */
    public void updateUnitsInZone() {
        this.unitTypesInZone.clear();
        this.unitsInZone.clear();
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i = 0; i < size; i++) {
            BaseUnit baseUnit = baseUnitArrA[i];
            if (baseUnit.team == this.aiController && !baseUnit.isDead && !baseUnit.u() && countUnitsForStrategy(baseUnit)) {
                this.unitsInZone.add(baseUnit);
                UnitType unitType = baseUnit.unitType;
                if (!this.unitTypesInZone.contains(unitType)) {
                    this.unitTypesInZone.add(unitType);
                }
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public boolean hasBuilderFor(UnitType unitType) {
        if (findFactoryForUnitType(unitType, false, true) != null) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: b */
    public boolean canBuild(UnitType unitType) {
        if ((GameEngine.getInstance().isDemo && !unitType.isAvailableInDemo()) || unitType.w()) {
            return false;
        }
        Object[] objArrA = this.unitTypesInZone.a();
        int size = this.unitTypesInZone.size();
        for (int i = 0; i < size; i++) {
            UnitType unitType2 = (UnitType) objArrA[i];
            BaseUnit[] baseUnitArrA = this.unitsInZone.a();
            int i2 = 0;
            int size2 = this.unitsInZone.size();
            while (true) {
                if (i2 < size2) {
                    BaseUnit baseUnit = baseUnitArrA[i2];
                    if (baseUnit.r() != unitType2 || !(baseUnit instanceof OrderableUnit)) {
                        i2++;
                    } else if (((OrderableUnit) baseUnit).canUseActionForUnitType(unitType, true)) {
                        return true;
                    } else {
                        i2++;
                    }
                } else {
                    break;
                }
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: c */
    public UnitType getBestBuildingToBuild() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.aiController.isAggressive) {
            return null;
        }
        int iCountEnemyUnitsInRange = countUnitsForBuildStrategy(this.aiController.airFactoryUnitBuildStrategy);
        UnitType unitType = null;
        float f = -1.0f;
        for (UnitType unitType2 : UnitTypeEnum.ae) {
            if (unitType2.isBuildingUnit() && canBuild(unitType2)) {
                int iAssignTaskToUnitTypeOnCommand = this.aiController.assignTaskToUnitTypeOnCommand(unitType2, UnitFilterMode.include);
                int iCountUnitsOfType = countUnitsOfType(unitType2);
                boolean z = false;
                if (unitType2 instanceof CustomUnitConfig) {
                    z = true;
                    CustomUnitConfig customUnitConfig = (CustomUnitConfig) unitType2;
                    if (customUnitConfig.relatedUnits.size() != 0) {
                        for (UnitType unitType3 : customUnitConfig.relatedUnits) {
                            iAssignTaskToUnitTypeOnCommand += this.aiController.assignTaskToUnitTypeOnCommand(unitType3, UnitFilterMode.include);
                            iCountUnitsOfType += countUnitsOfType(unitType3);
                        }
                    }
                }
                float f2 = -2.0f;
                if (unitType2.p() && !z) {
                    int i = iCountUnitsOfType;
                    if (findClosestResource() != null && Utility.getRandomIntInRange(0, 100) < 90) {
                        if (i == 0) {
                            if (this.aiController.credits < 5000.0d) {
                                f2 = 0.98f;
                            } else {
                                f2 = 0.58f;
                            }
                        }
                        if (i == 1) {
                            f2 = 0.55f;
                        }
                        if (i == 2) {
                            f2 = 0.4f;
                        }
                        if (i >= 3) {
                            f2 = 0.25f / i;
                        }
                        if (iAssignTaskToUnitTypeOnCommand >= 3) {
                            f2 = (float) (((double) f2) * 0.6d);
                        }
                    }
                }
                if (unitType2 == UnitTypeEnum.landFactory && (iAssignTaskToUnitTypeOnCommand < 5 || iCountEnemyUnitsInRange == 0)) {
                    if (iAssignTaskToUnitTypeOnCommand == 0) {
                        f2 = 0.8f;
                    } else if (iCountUnitsOfType < 2) {
                        f2 = 0.46f / (iAssignTaskToUnitTypeOnCommand + (iCountUnitsOfType * 2));
                    }
                }
                if (unitType2 == UnitTypeEnum.seaFactory && this.aiController.isPathfindingOverloaded() && (iAssignTaskToUnitTypeOnCommand < 5 || iCountEnemyUnitsInRange == 0)) {
                    if (iAssignTaskToUnitTypeOnCommand == 0) {
                        f2 = 0.3f;
                    } else if (iCountUnitsOfType < 1) {
                        f2 = 0.1f / (iAssignTaskToUnitTypeOnCommand + (iCountUnitsOfType * 2));
                    }
                }
                if (unitType2 == UnitTypeEnum.airFactory && (iAssignTaskToUnitTypeOnCommand < 5 || iCountEnemyUnitsInRange == 0)) {
                    if (iAssignTaskToUnitTypeOnCommand == 0) {
                        f2 = 0.48f;
                    } else if (iCountUnitsOfType < 2) {
                        f2 = 0.29f / (iAssignTaskToUnitTypeOnCommand + iCountUnitsOfType);
                    }
                }
                if (unitType2 == UnitTypeEnum.turret) {
                    if (iCountUnitsOfType == 0) {
                        f2 = 0.47f;
                    } else if (iCountUnitsOfType < 3) {
                        f2 = 0.35f / iCountUnitsOfType;
                    } else if (iCountUnitsOfType < 4) {
                        f2 = 0.025f / iCountUnitsOfType;
                    }
                }
                if (unitType2 == UnitTypeEnum.laserDefence && iCountUnitsOfType == 0) {
                    f2 = 0.018f;
                }
                if (unitType2 == UnitTypeEnum.repairbay && iCountUnitsOfType == 0) {
                    f2 = 0.02f;
                }
                if (unitType2 == UnitTypeEnum.antiAirTurret) {
                    if (iCountUnitsOfType == 0) {
                        f2 = 0.42f;
                    } else if (this.aiController.isInsaneDifficulty()) {
                        if (iCountUnitsOfType < 4) {
                            f2 = 0.3f / iCountUnitsOfType;
                        }
                    } else if (iCountUnitsOfType < 3) {
                        f2 = 0.3f / iCountUnitsOfType;
                    } else if (iCountUnitsOfType < 4) {
                        f2 = 0.02f / iCountUnitsOfType;
                    }
                }
                if (unitType2 == UnitTypeEnum.fabricator && this.aiController.credits > 2000.0d && iCountUnitsOfType < 5) {
                    if (iAssignTaskToUnitTypeOnCommand == 0) {
                        f2 = 0.11f;
                    } else {
                        f2 = 0.07f / ((0.2f * iAssignTaskToUnitTypeOnCommand) + iCountUnitsOfType);
                    }
                }
                if (unitType2 == UnitTypeEnum.AntiNukeLaucher && ((!gameEngine.isInGameOrLobby() || !gameEngine.networkEngine.roomSettings.noNukes) && this.aiController.credits > 2200.0d && iAssignTaskToUnitTypeOnCommand < 4)) {
                    if (iAssignTaskToUnitTypeOnCommand == 0) {
                        f2 = 0.06f;
                    } else if (iCountUnitsOfType < 1) {
                        f2 = 0.05f / (iAssignTaskToUnitTypeOnCommand + (iCountUnitsOfType * 2));
                    }
                }
                if (z) {
                    CustomUnitConfig customUnitConfig2 = (CustomUnitConfig) unitType2;
                    if (!customUnitConfig2.disableUse && ((iAssignTaskToUnitTypeOnCommand < customUnitConfig2.maxGlobal || customUnitConfig2.maxGlobal == -1) && (iCountUnitsOfType < customUnitConfig2.maxEachBase || customUnitConfig2.maxEachBase == -1))) {
                        f2 = customUnitConfig2.buildPriority;
                        if (iCountUnitsOfType < customUnitConfig2.recommendedInEachBaseNum) {
                            f2 = customUnitConfig2.recommendedInEachBasePriorityIfUnmet;
                        }
                        if (iCountUnitsOfType == 0) {
                            f2 += customUnitConfig2.nonInBaseExtraPriority;
                        }
                        if (iAssignTaskToUnitTypeOnCommand == 0) {
                            f2 += customUnitConfig2.nonGlobalExtraPriority;
                        }
                        if (unitType2.p() && findClosestResource() == null) {
                            f2 = -2.0f;
                        }
                    }
                }
                if (this.aiController.isHardOrAboveDifficulty() && unitType2 == UnitTypeEnum.experimentalLandFactory && this.aiController.credits > 15000.0d) {
                    if (iAssignTaskToUnitTypeOnCommand == 0) {
                        f2 = 0.04f;
                    }
                    if (this.aiController.credits > 55000.0d && iAssignTaskToUnitTypeOnCommand == 1) {
                        f2 = 0.03f;
                    }
                }
                if (f2 >= 0.0f && (f2 > f || Utility.randomFloatInRange(0.0f, 1.0f) < 0.01d)) {
                    f = f2;
                    unitType = unitType2;
                }
            }
        }
        this.lastFoundBuildingPriority = f;
        return unitType;
    }

    /* JADX INFO: renamed from: a */
    public int countUnitsForBuildStrategy(UnitBuildStrategy unitBuildStrategy) {
        int iCountUnitsOfType = 0;
        Iterator it = unitBuildStrategy.unitPriorities.iterator();
        while (it.hasNext()) {
            iCountUnitsOfType += countUnitsOfType(((UnitBuildPriority) it.next()).unitType);
        }
        return iCountUnitsOfType;
    }

    /* JADX INFO: renamed from: c */
    public int countUnitsOfType(UnitType unitType) {
        int i = 0;
        UnitList unitList = this.unitsInZone;
        BaseUnit[] baseUnitArrA = unitList.a();
        int size = unitList.size();
        for (int i2 = 0; i2 < size; i2++) {
            BaseUnit baseUnit = baseUnitArrA[i2];
            if (baseUnit.team == this.aiController && baseUnit.unitType == unitType && countUnitsForStrategy(baseUnit)) {
                i++;
            }
        }
        return i;
    }

    /* JADX INFO: renamed from: d */
    public int countIdleCombatUnits() {
        int i = 0;
        UnitList unitList = this.unitsInZone;
        BaseUnit[] baseUnitArrA = unitList.a();
        int size = unitList.size();
        for (int i2 = 0; i2 < size; i2++) {
            BaseUnit baseUnit = baseUnitArrA[i2];
            if (baseUnit.team == this.aiController && (baseUnit instanceof OrderableUnit)) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (belongsToZoneUnit(orderableUnit, false) && !orderableUnit.isActive && orderableUnit.aB == null && this.aiController.isCombatCustomUnit(orderableUnit) && this.aiController.isEligibleUnitForRandomSelection(orderableUnit)) {
                    i++;
                }
            }
        }
        return i;
    }

    /* JADX INFO: renamed from: e */
    public int getNumberOfCombatUnits() {
        return this.numberOfCombatUnits;
    }

    /* JADX INFO: renamed from: f */
    public boolean hasEnemyUnits() {
        return countEnemyUnits() != 0;
    }

    /* JADX INFO: renamed from: g */
    public BaseUnit getClosestEnemyUnit() {
        float f = this.radius + 120.0f;
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i = 0; i < size; i++) {
            BaseUnit baseUnit = baseUnitArrA[i];
            if (baseUnit.posX + f > this.posX && baseUnit.posX - f < this.posX && baseUnit.posY + f > this.posY && baseUnit.posY - f < this.posY && baseUnit.team != this.aiController && isUnitInRange(baseUnit, 120.0f) && baseUnit.team.c(this.aiController) && this.aiController.isUnitAllowedForSelection(baseUnit)) {
                return baseUnit;
            }
        }
        return null;
    }

    /* JADX INFO: renamed from: h */
    public int countEnemyUnits() {
        return countEnemyUnitsInRange(60.0f);
    }

    /* JADX INFO: renamed from: a */
    public int countEnemyUnitsInRange(float f) {
        int i = 0;
        float f2 = this.radius + f;
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i2 = 0; i2 < size; i2++) {
            BaseUnit baseUnit = baseUnitArrA[i2];
            if (baseUnit.posX + f2 > this.posX && baseUnit.posX - f2 < this.posX && baseUnit.posY + f2 > this.posY && baseUnit.posY - f2 < this.posY && baseUnit.team != this.aiController && isUnitInRange(baseUnit, f) && baseUnit.team.c(this.aiController) && baseUnit.canAttack() && this.aiController.isUnitAllowedForSelection(baseUnit)) {
                i++;
            }
        }
        return i;
    }

    /* JADX INFO: renamed from: i */
    public void commandUnitsToAttackClosestEnemy() {
        GameEngine gameEngine = GameEngine.getInstance();
        BaseUnit closestEnemyUnit = getClosestEnemyUnit();
        if (closestEnemyUnit != null) {
            Command commandNewCommandForTeam = gameEngine.commandController.newCommandForTeam(this.aiController);
            BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
            int size = BaseUnit.bE.size();
            for (int i = 0; i < size; i++) {
                BaseUnit baseUnit = baseUnitArrA[i];
                if (baseUnit instanceof OrderableUnit) {
                    OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                    if (baseUnit.team == this.aiController && this.aiController.canUnitEngageTarget(baseUnit, closestEnemyUnit) && this.aiController.isEligibleUnitForRandomSelection(orderableUnit) && orderableUnit.hasNoCurrentWaypoint()) {
                        if (!baseUnit.isActive) {
                            if (AIController.isPathPossibleForUnit(baseUnit, this.posX, this.posY, 800.0f)) {
                                commandNewCommandForTeam.addUnitToCommand(orderableUnit);
                            }
                        } else if (AIController.isPathPossibleForUnit(baseUnit, this.posX, this.posY, 540.0f)) {
                            commandNewCommandForTeam.addUnitToCommand(orderableUnit);
                        }
                    }
                }
            }
            commandNewCommandForTeam.setAttackTarget(closestEnemyUnit);
        }
    }

    /* JADX INFO: renamed from: a */
    public boolean countUnitsForStrategy(BaseUnit baseUnit) {
        return belongsToZone(baseUnit, false);
    }

    /* JADX INFO: renamed from: a */
    public boolean belongsToZone(BaseUnit baseUnit, boolean z) {
        if ((baseUnit instanceof OrderableUnit) && ((OrderableUnit) baseUnit).aC == this) {
            if (!z || isUnitInside(baseUnit)) {
                return true;
            }
            return false;
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public boolean belongsToZoneUnit(OrderableUnit orderableUnit, boolean z) {
        if (orderableUnit.aC == this) {
            if (!z || isUnitInside(orderableUnit)) {
                return true;
            }
            return false;
        }
        return false;
    }

    /* JADX INFO: renamed from: j */
    public int countEnemyUnitsInsideZone() {
        int i = 0;
        for (Object zoneUnit : getUnitsInZoneIterator()) {
            BaseUnit baseUnit=(BaseUnit) zoneUnit;
            if (this.aiController != baseUnit.team && baseUnit.team.c(this.aiController) && (baseUnit instanceof OrderableUnit) && isUnitInside(baseUnit)) {
                i++;
            }
        }
        return i;
    }

    /* JADX INFO: renamed from: k */
    public UnitListIterator getUnitsInZoneIterator() {
        return GameEngine.getInstance().unitSpatialIndex.b(this.posX, this.posY, this.radius);
    }

    /* JADX INFO: renamed from: x */
    private OrderableUnit findIdleCombatUnit() {
        return findBuilderForUnitType((UnitType) null, (PointF) null, true);
    }

    /* JADX INFO: renamed from: y */
    private OrderableUnit findAvailableBuilder() {
        return findAvailableBuilderForUnitType(null);
    }

    /* JADX INFO: renamed from: f */
    private OrderableUnit findAvailableBuilderForUnitType(UnitType unitType) {
        return findBuilderForUnitType(unitType, (PointF) null, false);
    }

    /* JADX INFO: renamed from: a */
    private OrderableUnit findBuilderForUnitType(UnitType unitType, PointF pointF, boolean z) {
        if (this.numberOfCombatUnits == 0) {
            return null;
        }
        this.lastTimeBuilt = 0;
        float f = Float.MAX_VALUE;
        OrderableUnit orderableUnit = null;
        GameEngine gameEngine = GameEngine.getInstance();
        if (unitType != null && ((gameEngine.isDemo && !unitType.isAvailableInDemo()) || unitType.w())) {
            return null;
        }
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i = 0; i < size; i++) {
            BaseUnit baseUnit = baseUnitArrA[i];
            if (baseUnit.team == this.aiController && countUnitsForStrategy(baseUnit) && baseUnit.transportContainer == null && baseUnit.canUnitAttack() && (baseUnit instanceof OrderableUnit) && this.aiController.isEligibleUnitForRandomSelection(baseUnit)) {
                OrderableUnit orderableUnit2 = (OrderableUnit) baseUnit;
                if (AIUnitActionUtils.isIdleOrReclaiming(orderableUnit2) && (!z || orderableUnit2.I())) {
                    this.lastTimeBuilt++;
                    if (unitType == null || orderableUnit2.canUseActionForUnitType(unitType, true)) {
                        boolean z2 = false;
                        float fDistanceSq = -1.0f;
                        if (pointF != null) {
                            fDistanceSq = Utility.distanceSq(pointF.x, pointF.y, baseUnit.posX, baseUnit.posY);
                        }
                        if (orderableUnit == null) {
                            z2 = true;
                        } else {
                            if (pointF != null && fDistanceSq < f) {
                                z2 = true;
                            }
                            if (Utility.randomFloatInRange(0.0f, 1.0f) < 0.2d) {
                                z2 = true;
                            }
                        }
                        if (z2) {
                            orderableUnit = (OrderableUnit) baseUnit;
                            if (pointF != null) {
                                f = fDistanceSq;
                            }
                        }
                    }
                }
            }
        }
        return orderableUnit;
    }


    /* JADX INFO: renamed from: a */
    private OrderableUnit findHarvesterForBuildTarget(BaseUnit am, PointF pointF, boolean boolean3) {
        if (this.numberOfIdleCombatUnits == 0) {
            return null;
        } else {
            float var4 = Float.MAX_VALUE;
            OrderableUnit var5 = null;
            GameEngine var6 = GameEngine.getInstance();
            BaseUnit[] var7 = this.unitsInZone.a();
            int var8 = 0;

            for (int var9 = this.unitsInZone.size(); var8 < var9; var8++) {
                BaseUnit var10 = var7[var8];
                if (var10.team == this.aiController && this.countUnitsForStrategy(var10) && var10.transportContainer == null) {
                    UnitType var11 = var10.r();
                    if (var11.n() && var10 instanceof OrderableUnit && this.aiController.isEligibleUnitForRandomSelection(var10)) {
                        OrderableUnit var12 = (OrderableUnit)var10;
                        boolean var13 = AIUnitActionUtils.isIdle(var12);
                        if (var13 && (!boolean3 || var12.I()) && (am == null || var12.h(am, true))) {
                            boolean var14 = false;
                            if (var11 instanceof CustomUnitConfig) {
                                CustomUnitConfig var15 = (CustomUnitConfig)var11;
                                if (var15.onlyUseAsHarvester_ifBaseHasUnitTagged != null && !this.hasUnitWithAnimationTag(var15.onlyUseAsHarvester_ifBaseHasUnitTagged)) {
                                    continue;
                                }
                            }

                            float var16 = -1.0F;
                            if (pointF != null) {
                                var16 = Utility.distanceSq(pointF.x, pointF.y, var10.posX, var10.posY);
                            }

                            if (var5 == null) {
                                var14 = true;
                            } else {
                                if (pointF != null && var16 < var4) {
                                    var14 = true;
                                }

                                if (Utility.randomFloatInRange(0.0F, 1.0F) < 0.2) {
                                    var14 = true;
                                }
                            }

                            if (var14) {
                                var5 = (OrderableUnit)var10;
                                if (pointF != null) {
                                    var4 = var16;
                                }
                            }
                        }
                    }
                }
            }

            return var5;
        }
    }

    /* JADX INFO: renamed from: g */
    private boolean tryBuildUnit(UnitType unitType) {
        PointF pointFE;
        OrderableUnit orderableUnitA;
        this.lastBuiltUnitType = unitType;
        this.lastBuiltCustomUnit = null;
        this.lastBuiltCustomUnit2 = null;
        GameEngine gameEngine = GameEngine.getInstance();
        if (unitType.p()) {
            pointFE = findClosestResource();
        } else {
            pointFE = findBuildLocation(unitType);
        }
        if (pointFE == null || (orderableUnitA = findBuilderForUnitType(unitType, pointFE, false)) == null) {
            return false;
        }
        if (unitType == UnitTypeEnum.seaFactory) {
            int iC = GameViewUtils.c(pointFE.x, pointFE.y, UnitMovementType.WATER);
            this.aiController.getClass();
            if (iC < 3000) {
                return false;
            }
        }
        int queueSize = 1;
        AbstractUnitAction abstractUnitActionFindActionForUnitType = orderableUnitA.findActionForUnitType(unitType, true);
        if (abstractUnitActionFindActionForUnitType != null) {
            queueSize = abstractUnitActionFindActionForUnitType.getQueueSize();
        } else {
            GameEngine.logColored("buildBuilding: could not find getBuildUnitAction for builder this shouldn't happen:" + unitType.getUnitTypeDescriptionShort());
        }
        if (!abstractUnitActionFindActionForUnitType.b(orderableUnitA) || !abstractUnitActionFindActionForUnitType.canAfford((BaseUnit) orderableUnitA, false)) {
            if (!this.aiController.isPathPossibleBetweenPoints(abstractUnitActionFindActionForUnitType.getPrice(), orderableUnitA)) {
                this.lastBuiltCustomUnit = abstractUnitActionFindActionForUnitType.getPrice();
                this.lastBuiltCustomUnit2 = this.lastBuiltCustomUnit.i(orderableUnitA);
                return true;
            }
            return true;
        }
        if (abstractUnitActionFindActionForUnitType.usesActionTarget()) {
            Command commandNewCommandForTeam = gameEngine.commandController.newCommandForTeam(this.aiController);
            commandNewCommandForTeam.addUnitToCommand(orderableUnitA);
            commandNewCommandForTeam.setActionTarget(abstractUnitActionFindActionForUnitType.getActionId(), pointFE, (BaseUnit) null);
            return true;
        }
        Command commandNewCommandForTeam2 = gameEngine.commandController.newCommandForTeam(this.aiController);
        commandNewCommandForTeam2.addUnitToCommand(orderableUnitA);
        commandNewCommandForTeam2.setBuildTarget(pointFE.x, pointFE.y, unitType, queueSize);
        return true;
    }

    /* JADX INFO: renamed from: z */
    private boolean hasUnitWithHighPriorityAction() {
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i = 0; i < size; i++) {
            BaseUnit baseUnit = baseUnitArrA[i];
            if (baseUnit.team == this.aiController && countUnitsForStrategy(baseUnit) && baseUnit.isAlive() && !baseUnit.u() && (baseUnit instanceof OrderableUnit) && ((OrderableUnit) baseUnit).hasHighPriorityAction()) {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public boolean hasUnitWithAnimationTag(AnimationSet animationSet) {
        AnimationSet unitCombatAnimation;
        BaseUnit[] baseUnitArrA = this.unitsInZone.a();
        int size = this.unitsInZone.size();
        for (int i = 0; i < size; i++) {
            BaseUnit baseUnit = baseUnitArrA[i];
            if (baseUnit.team == this.aiController && baseUnit.isAlive() && (unitCombatAnimation = baseUnit.getTags()) != null && AnimationTag.a(animationSet, unitCombatAnimation)) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference fix 'apply assigned field type' failed
    java.lang.UnsupportedOperationException: ArgType.getObject(), call class: class jadx.core.dex.instructions.args.ArgType$UnknownArg
    	at jadx.core.dex.instructions.args.ArgType.getObject(ArgType.java:593)
    	at jadx.core.dex.attributes.nodes.ClassTypeVarsAttr.getTypeVarsMapFor(ClassTypeVarsAttr.java:35)
    	at jadx.core.dex.nodes.utils.TypeUtils.replaceClassGenerics(TypeUtils.java:177)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.insertExplicitUseCast(FixTypesVisitor.java:397)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryFieldTypeWithNewCasts(FixTypesVisitor.java:359)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.applyFieldType(FixTypesVisitor.java:309)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:94)
     */
    /* JADX INFO: renamed from: a */
    private OrderableUnit findFactoryForUnitType(UnitType unitType, boolean z, boolean z2) {
        BaseUnit[] baseUnitArrA = this.unitsInZone.a();
        int size = this.unitsInZone.size();
        for (int i = 0; i < size; i++) {
            BaseUnit fireUnit = baseUnitArrA[i];
            if (fireUnit.team == this.aiController && fireUnit.isAlive() && this.aiController.isEligibleUnitForRandomSelection(fireUnit) && (fireUnit instanceof FactoryQueueInterface) && (fireUnit instanceof OrderableUnit)) {
                OrderableUnit orderableUnit = (OrderableUnit) fireUnit;
                FactoryQueueInterface factoryQueueInterface = (FactoryQueueInterface) fireUnit;
                AbstractUnitAction unitAction = fireUnit.getUnitAction(unitType);
                if (unitAction != null && ((factoryQueueInterface.dy() || !z) && !unitAction.isAiDisabled(fireUnit) && unitAction.b(orderableUnit) && unitAction.canAfford((BaseUnit) orderableUnit, false) && ((!(fireUnit instanceof CommandCenter) || unitType.m() || getNumberOfExtractors() <= 2 || this.isUnderAttack || !z) && (!z2 || orderableUnit.aD)))) {
                    return orderableUnit;
                }
            }
        }
        return null;
    }

    /* JADX INFO: renamed from: a */
    private boolean tryBuildFromStrategy(UnitBuildStrategy unitBuildStrategy, boolean z) {
        Iterator it = unitBuildStrategy.getShuffledUnits().iterator();
        while (it.hasNext()) {
            if (buildUnit(((UnitBuildPriority) it.next()).unitType, z)) {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    private boolean buildUnit(UnitType unitType, boolean z) {
        return buildUnit(unitType, z, 1);
    }

    /* JADX INFO: renamed from: a */
    private boolean buildUnit(UnitType unitType, boolean z, int i) {
        if (i < 1) {
            GameEngine.log("AI", "buildUnit: quantity cannot be < 1");
            return false;
        }
        OrderableUnit orderableUnitA = findFactoryForUnitType(unitType, true, z);
        if (orderableUnitA == null) {
        }
        if (orderableUnitA == null) {
            return false;
        }
        GameEngine gameEngine = GameEngine.getInstance();
        AbstractUnitAction abstractUnitActionE = orderableUnitA.getUnitAction(unitType);
        if (abstractUnitActionE == null) {
            GameEngine.log("AI", "buildUnit: action is null!");
            return false;
        }
        if (!abstractUnitActionE.b(orderableUnitA)) {
            GameEngine.log("AI", "buildUnit: isAvailable==false");
            return false;
        }
        if (!abstractUnitActionE.canAfford((BaseUnit) orderableUnitA, false)) {
            GameEngine.log("AI", "buildUnit: isActive==false");
            return false;
        }
        if (abstractUnitActionE.isNotAvailable(orderableUnitA)) {
            return false;
        }
        if (abstractUnitActionE.isQueuable() && gameEngine.isDemo) {
            return false;
        }
        for (int i2 = 0; i2 < i; i2++) {
            Command commandNewCommandForTeam = gameEngine.commandController.newCommandForTeam(this.aiController);
            commandNewCommandForTeam.addUnitToCommand(orderableUnitA);
            commandNewCommandForTeam.setActionId(abstractUnitActionE.getQueueId());
        }
        return true;
    }

    BaseZone findNearestSupportZone() {
        BaseZone baseZone;
        float f = -1.0f;
        BaseZone baseZone2 = null;
        for (AIStrategyNode aIStrategyNode : this.aiController.activeStrategies) {
            if ((aIStrategyNode instanceof BaseZone) && (baseZone = (BaseZone) aIStrategyNode) != this && baseZone.getNumberOfCombatUnits() > 1) {
                float fA = baseZone.getDistanceSqToZone(this);
                if (baseZone2 == null || fA < f) {
                    f = fA;
                    baseZone2 = baseZone;
                }
            }
        }
        return baseZone2;
    }

    /* JADX INFO: renamed from: m */
    public void requestReinforcement() {
        OrderableUnit orderableUnitX;
        GameEngine gameEngine = GameEngine.getInstance();
        BaseZone baseZoneL = findNearestSupportZone();
        if (baseZoneL != null && baseZoneL.getNumberOfCombatUnits() > 1 && (orderableUnitX = baseZoneL.findIdleCombatUnit()) != null) {
            PointF pointFW = getRandomPointInside();
            if (GameViewUtils.a(orderableUnitX, pointFW.x, pointFW.y)) {
                boolean zIsPathPossibleForUnit = this.aiController.isPathPossibleForUnit(orderableUnitX, pointFW.x, pointFW.y);
                if (zIsPathPossibleForUnit || this.aiController.activeTransporterGroupCount != 0) {
                    Command commandNewCommandForTeam = gameEngine.commandController.newCommandForTeam(this.aiController);
                    commandNewCommandForTeam.addUnitToCommand(orderableUnitX);
                    commandNewCommandForTeam.setMoveTarget(pointFW.x, pointFW.y);
                    this.lastAttackedTimer++;
                    this.updateTimer = Utility.getRandomIntInRange(1800, 2500);
                    if (this.lastAttackedTimer >= 2) {
                        this.updateTimer += 11000.0f;
                    }
                    baseZoneL.numberOfCombatUnits--;
                    if (!zIsPathPossibleForUnit) {
                        boolean z = true;
                        if (orderableUnitX.aB != null) {
                            if (!orderableUnitX.aB.isActive()) {
                                orderableUnitX.aB.removeUnit(orderableUnitX);
                            } else {
                                z = false;
                                if (!orderableUnitX.aB.unitsNeedingTransport.contains(orderableUnitX)) {
                                    orderableUnitX.aB.unitsNeedingTransport.add(orderableUnitX);
                                }
                            }
                        }
                        if (z) {
                            RallyGroup rallyGroup = new RallyGroup(this.aiController);
                            rallyGroup.addUnitNeedingTransport(orderableUnitX);
                            rallyGroup.posX = pointFW.x;
                            rallyGroup.posY = pointFW.y;
                        }
                        this.updateTimer = Utility.getRandomIntInRange(12000, 14000);
                    }
                }
            }
        }
    }

    /* JADX INFO: renamed from: A */
    private BaseUnit findRepairTarget() {
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i = 0; i < size; i++) {
            BaseUnit baseUnit = baseUnitArrA[i];
            if (baseUnit.team == this.aiController && belongsToZone(baseUnit, true) && baseUnit.bI() && (baseUnit.currentHealth < baseUnit.maxHealth - 1.0f || baseUnit.buildProgress < 1.0f)) {
                return baseUnit;
            }
        }
        return null;
    }

    /* JADX INFO: renamed from: n */
    public void buildRecommendedHarvesters() {
        GameEngine.getInstance();
        if (this.buildingsInZone.size() == 0 || this.isUnderAttack) {
            return;
        }
        for (int i = 0; i < 8; i++) {
            UnitType randomUnitType = this.aiController.extractorUnitBuildStrategy.getRandomUnitType();
            if (randomUnitType != null && hasBuilderFor(randomUnitType) && tryBuildRecommendedHarvester(randomUnitType)) {
                return;
            }
        }
    }


    public boolean tryBuildRecommendedHarvester(UnitType as) {
        if (!(as instanceof CustomUnitConfig)) {
            return false;
        } else {
            CustomUnitConfig var2 = (CustomUnitConfig)as;
            if (var2.whenUsingAsHarvester_recommendedInEachBase == -1 && var2.whenUsingAsHarvester_recommendedGlobal == -1) {
                return false;
            } else {
                int var3 = 0;
                int var4 = 0;
                boolean var5 = var2.whenUsingAsHarvester_includeOtherHarvesterCounts;
                BaseUnit[] var6 = BaseUnit.bE.a();
                int var7 = 0;

                for (int var8 = BaseUnit.bE.size(); var7 < var8; var7++) {
                    BaseUnit var9 = var6[var7];
                    if (var9.team == this.aiController && var9.transportContainer == null && var9 instanceof OrderableUnit && this.aiController.isEligibleUnitForRandomSelection(var9)) {
                        OrderableUnit var10 = (OrderableUnit)var9;
                        UnitType var11 = var9.r();
                        if (var5 ? var11.n() : var11 == var2 || var2.relatedUnits.contains(var11)) {
                            var4++;
                            if (this.countUnitsForStrategy(var9)) {
                                var3++;
                            }
                        }
                    }
                }

                if (var2.whenUsingAsHarvester_recommendedInEachBase != -1 && var3 >= var2.whenUsingAsHarvester_recommendedInEachBase) {
                    return false;
                } else {
                    return var2.whenUsingAsHarvester_recommendedGlobal != -1 && var4 >= var2.whenUsingAsHarvester_recommendedGlobal ? false : this.buildUnit(var2, true);
                }
            }
        }
    }
    /* JADX INFO: renamed from: o */
    public void sendHarvesterToReclaim() {
        BaseUnit baseUnitR;
        if (this.buildingsInZone.size() != 0 && (baseUnitR = getRandomBuilding()) != null) {
            this.tempPoint2.a(baseUnitR.posX, baseUnitR.posY);
            OrderableUnit orderableUnitA = findHarvesterForBuildTarget(baseUnitR, this.tempPoint2, true);
            if (orderableUnitA != null) {
                issueReclaimCommand(orderableUnitA, baseUnitR);
            }
        }
    }

    /* JADX INFO: renamed from: q */
    public void retargetHarvestersToBetterBuilding() {
        OrderableUnit orderableUnit;
        UnitCommand currentWaypoint;
        BaseUnit targetUnit;
        if (this.buildingsInZone.size() != 0 && this.lastBuiltCustomUnit2 != null) {
            BaseUnit[] baseUnitArrA = this.unitsInZone.a();
            int size = this.unitsInZone.size();
            for (int i = 0; i < size; i++) {
                BaseUnit baseUnit = baseUnitArrA[i];
                if (baseUnit.team == this.aiController && countUnitsForStrategy(baseUnit) && baseUnit.transportContainer == null && baseUnit.r().n() && (baseUnit instanceof OrderableUnit) && this.aiController.isEligibleUnitForRandomSelection(baseUnit) && Utility.randomFloatInRange(0.0f, 1.0f) <= 0.3d && (currentWaypoint = (orderableUnit = (OrderableUnit) baseUnit).getCurrentWaypoint()) != null && currentWaypoint.getCommandType() == UnitCommandType.reclaim && (targetUnit = currentWaypoint.getTargetUnit()) != null && targetUnit.getResourceRate() > 0.0f && !this.lastBuiltCustomUnit2.c(targetUnit.getBuildPrice())) {
                    issueReclaimCommand(orderableUnit, getRandomBuilding());
                    return;
                }
            }
        }
    }

    /* JADX INFO: renamed from: r */
    public BaseUnit getRandomBuilding() {
        BaseUnit baseUnit = null;
        for (int i = 0; i < 20; i++) {
            baseUnit = this.buildingsInZone.get(Utility.getRandomIntInRange(0, this.buildingsInZone.size() - 1));
            if (baseUnit == null || this.lastBuiltCustomUnit2 == null || this.lastBuiltCustomUnit2.c(baseUnit.getBuildPrice())) {
                break;
            }
        }
        return baseUnit;
    }

    /* JADX INFO: renamed from: a */
    public void issueReclaimCommand(OrderableUnit orderableUnit, BaseUnit baseUnit) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (orderableUnit.g(baseUnit, true)) {
            Command commandNewCommandForTeam = gameEngine.commandController.newCommandForTeam(this.aiController);
            commandNewCommandForTeam.addUnitToCommand(orderableUnit);
            commandNewCommandForTeam.setReclaimTarget(baseUnit);
        }
    }

    /* JADX INFO: renamed from: s */
    public void repairDamagedBuildings() {
        GameEngine gameEngine = GameEngine.getInstance();
        BaseUnit baseUnitM112A = findRepairTarget();
        if (baseUnitM112A != null) {
            this.tempPoint2.a(baseUnitM112A.posX, baseUnitM112A.posY);
            OrderableUnit orderableUnitA = findBuilderForUnitType((UnitType) null, this.tempPoint2, true);
            if (orderableUnitA != null && orderableUnitA.canRepairTarget(baseUnitM112A) && baseUnitM112A.e(orderableUnitA) < 2) {
                Command commandNewCommandForTeam = gameEngine.commandController.newCommandForTeam(this.aiController);
                commandNewCommandForTeam.addUnitToCommand(orderableUnitA);
                commandNewCommandForTeam.setRepairTarget(baseUnitM112A);
            }
        }
    }

    /* JADX INFO: renamed from: b */
    public void updateZone(float f) {
        refreshUnitCounts(f);
        int i = this.numberOfFactories;
        int i2 = this.numberOfExtractors;
        updateUnitsInZone();
        this.hasResources = hasUnitWithHighPriorityAction();
        if (this.hasResources) {
            this.hasLandAccess = true;
        }
        if (i >= 1) {
            repairDamagedBuildings();
        }
        if (this.isPrimary && this.numberOfExtractors > 0) {
            buildRecommendedHarvesters();
            retargetHarvestersToBetterBuilding();
            sendHarvesterToReclaim();
        }
        if (i < 2 && this.landUnitsScore == 0.0f) {
            this.landUnitsScore = 300.0f;
            int iShouldWriteForUnitType = this.aiController.countUnitsForBuildStrategyWithFilter(this.aiController.fabricatorUnitBuildStrategy, UnitFilterMode.include);
            if (!this.isUnderAttack || iShouldWriteForUnitType <= 2) {
                boolean z = Utility.getRandomIntInRange(0, 100) < 5;
                if (!z && tryBuildFromStrategy(this.aiController.fabricatorUnitBuildStrategy, true)) {
                    this.isInitialized = false;
                    this.landUnitsScore = 900.0f;
                } else {
                    if (!z) {
                        this.isInitialized = true;
                    }
                    if (!this.isUnderAttack && this.lastTimeReclaimed == 0.0f && i < 1 && this.updateTimer == 0.0f) {
                        requestReinforcement();
                    }
                }
            }
        }
        int iJ = countEnemyUnitsInsideZone();
        if (i == 0 && i2 == 0) {
            this.waterUnitsScore += f;
            if (iJ > 2) {
                this.waterUnitsScore += 2.0f * f;
            }
            if (iJ > 5) {
                this.waterUnitsScore += 4.0f * f;
            }
        } else {
            this.waterUnitsScore = Utility.moveTowardsZero(this.waterUnitsScore, f);
        }
        if (this.waterUnitsScore > 11000.0f) {
            destroy();
        }
        if (this.stage == BaseZoneStage.Pre && ((i != 0 && i2 != 0) || (i2 > 5 && iJ == 0))) {
            this.extractorScore += f;
            if (this.extractorScore > 2000.0f) {
                this.stage = BaseZoneStage.Active;
            }
        }
        fixOverlaps();
    }

    /* JADX INFO: renamed from: t */
    public void fixOverlaps() {
        if (this.stage == null) {
            GameEngine.logErrorColored("fixOverlaps: this.state==null");
            GameEngine.logErrorColored("id:" + this.strategyId);
            GameEngine.logErrorColored("x:" + this.posX);
            GameEngine.logErrorColored("y:" + this.posY);
            GameEngine.logErrorColored("radius:" + this.radius);
            if (this.aiController != null) {
                GameEngine.logErrorColored("team:" + this.aiController.teamId);
                return;
            }
            return;
        }
        for (AIStrategyNode aIStrategyNode : this.aiController.strategyNodes) {
            if ((aIStrategyNode instanceof BaseZone) && aIStrategyNode != this) {
                BaseZone baseZone = (BaseZone) aIStrategyNode;
                if (Utility.distanceSq(this.posX, this.posY, baseZone.posX, baseZone.posY) < 400.0f) {
                    if (baseZone.stage == null) {
                        GameEngine.logErrorColored("fixOverlaps: targetBase.state==null");
                    } else if (baseZone.stage.getOrdinal() < this.stage.getOrdinal()) {
                        baseZone.destroy();
                    } else {
                        destroy();
                    }
                }
            }
        }
    }

    /* JADX INFO: renamed from: u */
    public int getNumberOfExtractors() {
        return this.numberOfExtractors;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference fix 'apply assigned field type' failed
    java.lang.UnsupportedOperationException: ArgType.getObject(), call class: class jadx.core.dex.instructions.args.ArgType$UnknownArg
    	at jadx.core.dex.instructions.args.ArgType.getObject(ArgType.java:593)
    	at jadx.core.dex.attributes.nodes.ClassTypeVarsAttr.getTypeVarsMapFor(ClassTypeVarsAttr.java:35)
    	at jadx.core.dex.nodes.utils.TypeUtils.replaceClassGenerics(TypeUtils.java:177)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.insertExplicitUseCast(FixTypesVisitor.java:397)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryFieldTypeWithNewCasts(FixTypesVisitor.java:359)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.applyFieldType(FixTypesVisitor.java:309)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:94)
     */
    /* JADX INFO: renamed from: c */
    public void refreshUnitCounts(float f) {
        this.numberOfExtractors = 0;
        this.numberOfFactories = 0;
        this.numberOfIdleCombatUnits = 0;
        this.numberOfCombatUnits = 0;
        this.isPrimary = false;
        this.buildingsInZone.clear();
        for (Object o : getUnitsInZoneIterator()) {
            BaseUnit baseUnit=(BaseUnit) o;
            if (baseUnit.getResourceRate() > 0.0f && isUnitInside(baseUnit)) {
                this.isPrimary = true;
                this.buildingsInZone.add(baseUnit);
            }
        }
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i = 0; i < size; i++) {
            BaseUnit fireUnit = baseUnitArrA[i];
            if (fireUnit.team == this.aiController && (fireUnit instanceof OrderableUnit)) {
                OrderableUnit orderableUnit = (OrderableUnit) fireUnit;
                if (belongsToZoneUnit(orderableUnit, false) && fireUnit.isAlive() && this.aiController.isEligibleUnitForRandomSelection(fireUnit) && !fireUnit.u()) {
                    UnitType unitTypeR = fireUnit.r();
                    if (unitTypeR.isBuildingUnit()) {
                        this.numberOfExtractors++;
                    }
                    if (unitTypeR.m()) {
                        this.numberOfFactories++;
                        if (AIUnitActionUtils.isIdleOrReclaiming(orderableUnit)) {
                            this.numberOfCombatUnits++;
                        }
                    }
                    if (unitTypeR.n()) {
                        this.numberOfIdleCombatUnits++;
                    }
                    if (fireUnit instanceof FactoryQueueInterface) {
                        this.numberOfFactories += ((FactoryQueueInterface) fireUnit).h(UnitTypeEnum.builder);
                    }
                }
            }
        }
    }

    /* JADX INFO: renamed from: d */
    public void updateZoneStrategy(float f) {
        UnitType bestBuildingToBuild;
        this.isUnderAttack = hasEnemyUnits();
        this.isContested = this.isUnderAttack;
        if (this.isUnderAttack) {
            this.lastTimeReclaimed += f;
            this.lastTimeScouted = 100.0f;
        } else {
            this.lastTimeReclaimed = 0.0f;
        }
        if (this.lastTimeReclaimed > 6000.0f) {
            this.isUnderAttack = false;
        }
        this.lastTimeAttackedByEnemy = Utility.moveTowardsZero(this.lastTimeAttackedByEnemy, f);
        this.defensiveScore = Utility.moveTowardsZero(this.defensiveScore, f);
        this.updateTimer = Utility.moveTowardsZero(this.updateTimer, f);
        this.landUnitsScore = Utility.moveTowardsZero(this.landUnitsScore, f);
        this.airUnitsScore = Utility.moveTowardsZero(this.airUnitsScore, f);
        if (this.isUnderAttack && this.airUnitsScore == 0.0f) {
            this.airUnitsScore = 100 + (this.strategyId % 15);
            if (!this.aiController.isAggressive) {
                commandUnitsToAttackClosestEnemy();
            }
        }
        if (this.defensiveScore <= 0.0f) {
            this.defensiveScore = 270 + (this.strategyId % 15);
            if (this.aiController.isInsaneDifficulty()) {
                this.defensiveScore = SlickToAndroidKeycodes.AndroidCodes.KEYCODE_BUTTON_3 + (this.strategyId % 15);
            }
            if (this.lastFoundBuildingPriority < 0.2d) {
                this.defensiveScore += 180.0f;
            }
            if (this.lastFoundBuildingPriority < 0.08d) {
                this.defensiveScore += 180.0f;
            }
            if ((findAvailableBuilder() != null) && (bestBuildingToBuild = getBestBuildingToBuild()) != null && ((this.lastFoundBuildingPriority > 0.8d || this.aiController.hasCredits(1300.0d)) && ((this.lastFoundBuildingPriority > 0.4d || this.aiController.hasCredits(1700.0d)) && ((this.lastFoundBuildingPriority > 0.2d || this.aiController.hasCredits(2100.0d)) && ((this.lastFoundBuildingPriority > 0.1d || this.aiController.hasCredits(2800.0d)) && ((this.lastFoundBuildingPriority > 0.05d || this.aiController.hasCredits(3100.0d)) && (this.lastFoundBuildingPriority > 0.01d || this.aiController.hasCredits(4800.0d)))))))) {
                this.numberOfLandUnits++;
                if (!tryBuildUnit(bestBuildingToBuild)) {
                    this.defensiveScore -= 120.0f;
                    this.numberOfAirUnits++;
                }
            }
        }
        float fU = getNumberOfExtractors() / 3.0f;
        if (fU < 1.0f) {
            fU = 1.0f;
        }
        if (this.isUnderAttack) {
            this.reclaimScore = (float) (((double) this.reclaimScore) + (((double) f) * 0.015d));
        }
        if (this.lastFoundBuildingPriority < 0.6d) {
            if (this.aiController.unitCount < 2) {
                this.reclaimScore = (float) (((double) this.reclaimScore) + (((double) f) * 7.0E-4d * ((double) fU)));
            } else if (this.aiController.hasCredits(1200.0d)) {
                this.reclaimScore = (float) (((double) this.reclaimScore) + (((double) f) * 1.0E-4d * ((double) fU)));
            }
            if (this.aiController.hasCredits(1600.0d)) {
                this.reclaimScore = (float) (((double) this.reclaimScore) + (((double) f) * 0.001d));
            }
            if (this.aiController.hasCredits(2200.0d)) {
                this.reclaimScore = (float) (((double) this.reclaimScore) + (((double) f) * 0.001d));
            }
            if (this.aiController.hasCredits(2600.0d)) {
                this.reclaimScore = (float) (((double) this.reclaimScore) + (((double) f) * 0.001d));
            }
            if (this.aiController.hasCredits(8000.0d)) {
                this.reclaimScore = (float) (((double) this.reclaimScore) + (((double) f) * 0.005d));
            }
            if (this.aiController.hasCredits(9000.0d)) {
                this.reclaimScore = (float) (((double) this.reclaimScore) + (((double) f) * 0.01d));
            }
            if (this.aiController.hasCredits(10100.0d)) {
                this.reclaimScore = (float) (((double) this.reclaimScore) + (((double) f) * 0.01d));
            }
            if (this.aiController.hasCredits(30000.0d)) {
                this.reclaimScore = (float) (((double) this.reclaimScore) + (((double) f) * 0.05d));
            }
        }
        if (this.aiController.hasCredits(5000.0d)) {
            this.reclaimScore = (float) (((double) this.reclaimScore) + (((double) f) * 0.001d));
        }
        if (!this.aiController.hasCredits(800.0d) && !this.isUnderAttack && this.reclaimScore > 1.2f) {
            this.reclaimScore = 1.2f;
        }
        if (this.reclaimScore > 3.5f) {
            this.reclaimScore = 3.5f;
        }
        for (int i = 0; i < 12; i++) {
            tryBuildCombatUnits();
            if (this.reclaimScore < 3.0f) {
                return;
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public void addRandomUnitTypesForBuild(ArrayList arrayList, UnitBuildStrategy unitBuildStrategy, UnitMovementType unitMovementType, int i) {
        this.rallyPoints.clear();
        for (int i2 = 0; i2 < i; i2++) {
            UnitType randomUnitTypeByMovement = unitBuildStrategy.getRandomUnitTypeByMovement(unitMovementType);
            if (randomUnitTypeByMovement != null && !this.rallyPoints.contains(randomUnitTypeByMovement)) {
                this.rallyPoints.add(randomUnitTypeByMovement);
            }
        }
        arrayList.addAll(this.rallyPoints);
    }

    /* JADX INFO: renamed from: v */
    public void tryBuildCombatUnits() {
        UnitMovementType unitMovementType;
        BaseUnit closestEnemyUnit;
        int iCountIdleCombatUnits = countIdleCombatUnits();
        int i = 12;
        int i2 = 50;
        if (this.aiController.isInsaneDifficulty()) {
            i2 = 65;
            i = 16;
        }
        boolean zA = this.aiController.hasCredits(25000.0d);
        ArrayList arrayList = new ArrayList();
        boolean zIsAttackBlockedByConditions = this.aiController.isAttackBlockedByConditions();
        boolean zShouldLaunchAttack = this.aiController.shouldLaunchAttack();
        float f = 0.4f;
        float f2 = 0.3f;
        if (!this.aiController.isReadyForAttack) {
            f = 0.1f;
            f2 = 0.5f;
        }
        if (!this.aiController.hasSufficientUnitsForAttack) {
            f = 0.2f;
            f2 = 0.1f;
        }
        float fRandomFloatInRange = Utility.randomFloatInRange(0.0f, 1.0f);
        if (fRandomFloatInRange < f) {
            unitMovementType = UnitMovementType.LAND;
        } else if (fRandomFloatInRange < f + f2) {
            unitMovementType = UnitMovementType.HOVER;
        } else {
            unitMovementType = UnitMovementType.AIR;
        }
        if ((this.aiController.hasCredits(1300.0d) && this.reclaimScore >= 1.0f) || (this.aiController.hasCredits(300.0d) && this.reclaimScore >= 3.0f)) {
            if (this.aiController.isPathfindingOverloaded() && this.aiController.buildingCount < i && Utility.getRandomInt(100) < 35) {
                addRandomUnitTypesForBuild(arrayList, this.aiController.seaUnitBuildStrategy, null, 2);
                if (zA) {
                }
            }
            if (iCountIdleCombatUnits < 3 && this.aiController.unitProductionTimer < i2) {
                if (unitMovementType == UnitMovementType.LAND) {
                    addRandomUnitTypesForBuild(arrayList, this.aiController.builderUnitBuildStrategy, null, 4);
                    if (zA) {
                    }
                } else if (unitMovementType == UnitMovementType.HOVER) {
                    addRandomUnitTypesForBuild(arrayList, this.aiController.landUnitBuildStrategy, null, 4);
                    if (zA) {
                    }
                } else {
                    addRandomUnitTypesForBuild(arrayList, this.aiController.airUnitBuildStrategy, null, 4);
                    if (zA) {
                    }
                }
            }
            if (this.reclaimScore >= 1.0f && zIsAttackBlockedByConditions && this.lastTimeAttackedByEnemy == 0.0f) {
                int iShouldWriteForUnitType = this.aiController.countUnitsForBuildStrategyWithFilter(this.aiController.baseDefenseUnitBuildStrategy, UnitFilterMode.include);
                int iShouldWriteForUnitType2 = iShouldWriteForUnitType + this.aiController.countUnitsForBuildStrategyWithFilter(this.aiController.scoutUnitBuildStrategy, UnitFilterMode.include);
                int iCountUnitsInGroups = this.aiController.countUnitsInGroups();
                if ((this.aiController.hasCredits(1700.0d) || iCountUnitsInGroups > 10 || (this.aiController.attackCooldownTimer == 0 && iCountUnitsInGroups >= 1 && iShouldWriteForUnitType == 0)) && (iShouldWriteForUnitType2 < 3 || (iCountUnitsInGroups > 20 && iShouldWriteForUnitType2 < 5))) {
                    if (zShouldLaunchAttack && iShouldWriteForUnitType2 < 2) {
                        addRandomUnitTypesForBuild(arrayList, this.aiController.antiNukeUnitBuildStrategy, null, 2);
                    } else {
                        addRandomUnitTypesForBuild(arrayList, this.aiController.antiNukeUnitBuildStrategy, UnitMovementType.AIR, 2);
                    }
                }
            }
        }
        if (arrayList.size() == 0) {
            this.numberOfWaterUnits++;
        }
        while (arrayList.size() != 0) {
            UnitType unitType = (UnitType) arrayList.remove(arrayList.size() - 1);
            BaseUnit baseUnitFindAttackDamageSource = BaseUnit.findAttackDamageSource(unitType);
            boolean z = true;
            if (this.isUnderAttack && Utility.getRandomInt(100) < 10 && (closestEnemyUnit = getClosestEnemyUnit()) != null && !this.aiController.canUnitEngageTarget(baseUnitFindAttackDamageSource, closestEnemyUnit)) {
                this.numberOfWaterUnits++;
                z = false;
            }
            if (z) {
                if (buildUnit(unitType, false)) {
                    this.numberOfWaterUnits++;
                    this.aiController.buildPreferenceCache.invalidateUnitType(unitType);
                    this.reclaimScore -= 1.0f;
                    if (this.aiController.isNonCombatCustomUnit(baseUnitFindAttackDamageSource)) {
                        this.lastTimeAttackedByEnemy = 1000.0f;
                        this.aiController.attackCooldownTimer++;
                        return;
                    }
                    return;
                }
                this.numberOfBuildings++;
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public void recordBuiltFactoryUnit(OrderableUnit orderableUnit, UnitPrice unitPrice, boolean z) {
        this.lastBuiltFactoryType = orderableUnit.r();
        if (z) {
            this.lastBuiltFactoryCustomUnit = null;
            this.lastBuiltCustomUnit2 = null;
        } else {
            this.lastBuiltFactoryCustomUnit = unitPrice;
            this.lastBuiltCustomUnit2 = unitPrice.i(orderableUnit);
        }
    }
}
