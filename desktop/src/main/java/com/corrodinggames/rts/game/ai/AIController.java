package com.corrodinggames.rts.game.ai;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.ai.behaviors.AIBehavior;
import com.corrodinggames.rts.game.ai.behaviors.AIBehaviorType;
import com.corrodinggames.rts.game.units.*;
import com.corrodinggames.rts.game.units.actions.AbstractUnitAction;
import com.corrodinggames.rts.game.units.actions.ActionId;
import com.corrodinggames.rts.game.units.actions.ActionType;
import com.corrodinggames.rts.game.units.actions.PopupQueueAction;
import com.corrodinggames.rts.game.units.air.AmphibiousJet;
import com.corrodinggames.rts.game.units.buildings.*;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.game.units.custom.price.UnitPrice;
import com.corrodinggames.rts.game.units.sea.AttackSubmarine;
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
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.a.a */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/a/a.class */
public final class AIController extends PlayerTeam {

    /* JADX INFO: renamed from: as */
    public static boolean unitCountsUpdated;

    /* JADX INFO: renamed from: at */
    int PATHFINDING_OVERLOAD_THRESHOLD = 3000;

    /* JADX INFO: renamed from: au */
    int mapWidth;

    /* JADX INFO: renamed from: av */
    int mapHeight;

    /* JADX INFO: renamed from: aw */
    int mapSeed;

    /* JADX INFO: renamed from: ax */
    int baseCount;

    /* JADX INFO: renamed from: ay */
    int advancedBaseCount;

    /* JADX INFO: renamed from: az */
    int attackGroupCount;

    /* JADX INFO: renamed from: aA */
    int landAttackGroupCount;

    /* JADX INFO: renamed from: aB */
    int airAttackGroupCount;

    /* JADX INFO: renamed from: aC */
    int builderGroupCount;

    /* JADX INFO: renamed from: aD */
    int waterBuilderGroupCount;

    /* JADX INFO: renamed from: aE */
    int totalBuilderUnits;

    /* JADX INFO: renamed from: aF */
    int transporterGroupCount;

    /* JADX INFO: renamed from: aG */
    int activeTransporterGroupCount;

    /* JADX INFO: renamed from: aH */
    int constructingBaseCount;

    /* JADX INFO: renamed from: aI */
    public int nextStrategyId;

    /* JADX INFO: renamed from: aJ */
    int aiFlags;

    /* JADX INFO: renamed from: aK */
    boolean aiEnabled;

    /* JADX INFO: renamed from: aL */
    float attackTimerEasy;

    /* JADX INFO: renamed from: aM */
    float attackTimerNormal;

    /* JADX INFO: renamed from: aN */
    float attackTimerHard;

    /* JADX INFO: renamed from: aO */
    float attackTimerInsane;

    /* JADX INFO: renamed from: aP */
    float resourceMultiplierEasy;

    /* JADX INFO: renamed from: aQ */
    float resourceMultiplierNormal;

    /* JADX INFO: renamed from: aR */
    float resourceMultiplierHard;

    /* JADX INFO: renamed from: aS */
    float resourceMultiplierInsane;

    /* JADX INFO: renamed from: aT */
    float globalTimer;

    /* JADX INFO: renamed from: aU */
    float attackTimersUpdateCounter;

    /* JADX INFO: renamed from: aV */
    int pathfindingUpdateTimer;

    /* JADX INFO: renamed from: aW */
    float attackRangeMultiplier;

    /* JADX INFO: renamed from: aX */
    public boolean canBuild;

    /* JADX INFO: renamed from: aY */
    public boolean isAggressive;

    /* JADX INFO: renamed from: aZ */
    public boolean enableScouting;

    /* JADX INFO: renamed from: ba */
    int unitProductionTimer;

    /* JADX INFO: renamed from: bb */
    int unitCount;

    /* JADX INFO: renamed from: bc */
    int buildingCount;

    /* JADX INFO: renamed from: bd */
    boolean enableNaval;

    /* JADX INFO: renamed from: be */
    boolean enableAirForce;

    /* JADX INFO: renamed from: bf */
    boolean enableExperimentalUnits;

    /* JADX INFO: renamed from: bg */
    DamageZone avoidDamageZone;

    /* JADX INFO: renamed from: bh */
    boolean isReadyForAttack;

    /* JADX INFO: renamed from: bi */
    boolean hasSufficientUnitsForAttack;

    /* JADX INFO: renamed from: bj */
    boolean isWaitingForBetterAttackTiming;

    /* JADX INFO: renamed from: bk */
    boolean isAttackAllowed;

    /* JADX INFO: renamed from: bl */
    int attackCooldownTimer;

    /* JADX INFO: renamed from: bm */
    ConcurrentLinkedQueue<AIStrategyNode> strategyNodes;

    /* JADX INFO: renamed from: bn */
    ArrayList<AIStrategyNode> activeStrategies;

    /* JADX INFO: renamed from: bo */
    PointF debugPoint;

    /* JADX INFO: renamed from: bp */
    Paint debugPaint;

    /* JADX INFO: renamed from: bq */
    ArrayList debugMessages;

    /* JADX INFO: renamed from: br */
    UnitBuildStrategy builderUnitBuildStrategy;

    /* JADX INFO: renamed from: bs */
    UnitBuildStrategy landUnitBuildStrategy;

    /* JADX INFO: renamed from: bt */
    UnitBuildStrategy airUnitBuildStrategy;

    /* JADX INFO: renamed from: bu */
    UnitBuildStrategy seaUnitBuildStrategy;

    /* JADX INFO: renamed from: bv */
    UnitBuildStrategy experimentalUnitBuildStrategy;

    /* JADX INFO: renamed from: bw */
    UnitBuildStrategy antiNukeUnitBuildStrategy;

    /* JADX INFO: renamed from: bx */
    UnitBuildStrategy baseDefenseUnitBuildStrategy;

    /* JADX INFO: renamed from: by */
    UnitBuildStrategy scoutUnitBuildStrategy;

    /* JADX INFO: renamed from: bz */
    UnitBuildStrategy fabricatorUnitBuildStrategy;

    /* JADX INFO: renamed from: bA */
    UnitBuildStrategy extractorUnitBuildStrategy;

    /* JADX INFO: renamed from: bB */
    UnitBuildStrategy landFactoryUnitBuildStrategy;

    /* JADX INFO: renamed from: bC */
    UnitBuildStrategy airFactoryUnitBuildStrategy;

    /* JADX INFO: renamed from: bD */
    UnitBuildStrategy seaFactoryUnitBuildStrategy;

    /* JADX INFO: renamed from: bE */
    public BuildPreferenceCache buildPreferenceCache;

    /* JADX INFO: renamed from: bF */
    int difficultyLevel;

    /* JADX INFO: renamed from: bG */
    public float aiUnitManagementTimer;

    /* JADX INFO: renamed from: bH */
    ArrayList unitsToProcess;

    /* JADX INFO: renamed from: bK */
    private static ArrayList tempUnitList = new ArrayList();
    public static final UnitList bI = new UnitList();

    /* JADX INFO: renamed from: bJ */
    public final FastArrayList aiBehaviors;

    /* JADX INFO: renamed from: ac */
    public boolean isInsaneDifficulty() {
        return getAIDifficulty() == 3 || getAIDifficulty() > 300;
    }

    /* JADX INFO: renamed from: ad */
    public boolean isHardOrAboveDifficulty() {
        return getAIDifficulty() >= 2;
    }

    /* JADX INFO: renamed from: ae */
    public boolean isAggressiveFlagEnabled() {
        return (1 & this.aiFlags) == 1;
    }

    /* JADX INFO: renamed from: af */
    public boolean isAggressiveMode() {
        return isAggressiveFlagEnabled();
    }

    /* JADX INFO: renamed from: ag */
    public int getAIDifficulty() {
        return this.difficultyLevel;
    }

    /* JADX INFO: renamed from: ah */
    public boolean isPathfindingOverloaded() {
        if (GameEngine.getInstance().pathfindingEngine.airCostMap.i > 3000) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: ai */
    public boolean isAttackBlockedByConditions() {
        if (isPathfindingOverloaded() || !this.isReadyForAttack || !this.hasSufficientUnitsForAttack || !this.isWaitingForBetterAttackTiming || !this.isAttackAllowed) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: aj */
    public boolean shouldLaunchAttack() {
        if (this.isAttackAllowed && isAttackBlockedByConditions() && this.hasSufficientUnitsForAttack) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public boolean canUnitReachNode(float f, float f2, AIStrategyNode aIStrategyNode, UnitMovementType unitMovementType) {
        if (isPathPossibleBetweenPoints(f, f2, aIStrategyNode.posX, aIStrategyNode.posY, unitMovementType)) {
            return true;
        }
        float f3 = -180.0f;
        while (true) {
            float f4 = f3;
            if (f4 < 180.0f) {
                if (!isPathPossibleBetweenPoints(f, f2, aIStrategyNode.posX + (Utility.fastCos(f4) * aIStrategyNode.radius * 0.4f), aIStrategyNode.posY + (Utility.fastSin(f4) * aIStrategyNode.radius * 0.4f), unitMovementType)) {
                    f3 = f4 + 90.0f;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public boolean isPathPossibleBetweenPoints(float f, float f2, float f3, float f4, UnitMovementType unitMovementType) {
        if (unitMovementType == UnitMovementType.AIR || unitMovementType == UnitMovementType.NONE) {
            return true;
        }
        short sB = GameViewUtils.b(f, f2, unitMovementType);
        short sB2 = GameViewUtils.b(f3, f4, unitMovementType);
        if (sB == -3 || sB2 == -3) {
            String strName = "null";
            if (unitMovementType != null) {
                strName = unitMovementType.name();
            }
            setDebugKeyState("pathPossible: no isolatedGroups found! (" + strName + ")");
            GameEngine.printStackTrace();
        }
        if (sB != -1 && sB2 != -1 && sB != -2 && sB2 != -2 && sB == sB2) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public boolean isPathPossibleForUnit(BaseUnit baseUnit, float f, float f2) {
        return isPathPossibleBetweenPoints(baseUnit.posX, baseUnit.posY, f, f2, baseUnit.getMovementType());
    }

    /* JADX INFO: renamed from: b */
    public boolean canUnitReachPointWithOffsets(BaseUnit baseUnit, float f, float f2) {
        UnitMovementType unitMovementTypeH = baseUnit.getMovementType();
        return isPathPossibleBetweenPoints(baseUnit.posX, baseUnit.posY, f, f2, unitMovementTypeH) || isPathPossibleBetweenPoints(baseUnit.posX, baseUnit.posY, f + 60.0f, f2, unitMovementTypeH) || isPathPossibleBetweenPoints(baseUnit.posX, baseUnit.posY, f - 60.0f, f2, unitMovementTypeH) || isPathPossibleBetweenPoints(baseUnit.posX, baseUnit.posY, f, f2 + 60.0f, unitMovementTypeH) || isPathPossibleBetweenPoints(baseUnit.posX, baseUnit.posY, f, f2 - 60.0f, unitMovementTypeH);
    }

    /* JADX INFO: renamed from: a */
    public boolean canUnitReachUnit(BaseUnit baseUnit, BaseUnit baseUnit2) {
        return canUnitReachPointWithOffsets(baseUnit, baseUnit2.posX, baseUnit2.posY);
    }

    @Override // com.corrodinggames.rts.game.PlayerTeam, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        int i;
        gameOutputStream.writeBoolean(this.aiEnabled);
        gameOutputStream.writeFloat(this.attackTimerEasy);
        gameOutputStream.writeFloat(this.attackTimerNormal);
        gameOutputStream.writeFloat(this.attackTimerHard);
        gameOutputStream.writeFloat(this.attackTimerInsane);
        gameOutputStream.writeFloat(this.globalTimer);
        gameOutputStream.writeInt(this.pathfindingUpdateTimer);
        gameOutputStream.writeFloat(this.attackRangeMultiplier);
        gameOutputStream.writeBoolean(this.canBuild);
        gameOutputStream.writeInt(this.unitProductionTimer);
        gameOutputStream.writeInt(this.strategyNodes.size());
        for (AIStrategyNode aIStrategyNode : this.strategyNodes) {
            if (aIStrategyNode instanceof BaseZone) {
                i = 1;
            } else if (aIStrategyNode instanceof UnitGroup) {
                i = 2;
            } else if (aIStrategyNode instanceof TransporterGroup) {
                i = 3;
            } else if (aIStrategyNode instanceof PlainZone) {
                i = 4;
            } else if (aIStrategyNode instanceof RallyGroup) {
                i = 5;
            } else {
                throw new RuntimeException("zone not instance not supported:" + aIStrategyNode.getClass().getName());
            }
            gameOutputStream.writeByte(i);
            gameOutputStream.writeInt(aIStrategyNode.strategyId);
        }
        for (AIStrategyNode aIStrategyNode2 : this.strategyNodes) {
            gameOutputStream.writeInt(aIStrategyNode2.strategyId);
            aIStrategyNode2.a(gameOutputStream);
        }
        gameOutputStream.writeByte(9);
        gameOutputStream.writeInt(this.nextStrategyId);
        gameOutputStream.writeBoolean(this.enableNaval);
        gameOutputStream.writeBoolean(this.isReadyForAttack);
        gameOutputStream.writeBoolean(this.hasSufficientUnitsForAttack);
        gameOutputStream.writeBoolean(this.isWaitingForBetterAttackTiming);
        gameOutputStream.writeBoolean(this.isAttackAllowed);
        gameOutputStream.writeFloat(this.attackTimersUpdateCounter);
        gameOutputStream.writeInt(this.attackCooldownTimer);
        gameOutputStream.writeInt(this.mapWidth);
        gameOutputStream.writeInt(this.mapHeight);
        gameOutputStream.writeInt(this.mapSeed);
        gameOutputStream.writeBoolean(this.isAggressive);
        gameOutputStream.writeInt(this.aiFlags);
        gameOutputStream.writeMagicShort();
        gameOutputStream.writeInt(this.aiBehaviors.size);
        for (int i2 = 0; i2 < this.aiBehaviors.size; i2++) {
            AIBehavior aIBehavior = (AIBehavior) this.aiBehaviors.get(i2);
            gameOutputStream.writeEnumOrdinal(aIBehavior.getBehaviorType());
            aIBehavior.a(gameOutputStream);
        }
        gameOutputStream.writeMagicShort();
        super.a(gameOutputStream);
    }

    /* JADX INFO: renamed from: l */
    public AIStrategyNode createZoneByTypeId(int i) {
        if (i == 1) {
            return new BaseZone(this, -1.0f, -1.0f);
        }
        if (i == 2) {
            return new UnitGroup(this);
        }
        if (i == 3) {
            return new TransporterGroup(this);
        }
        if (i == 4) {
            return new PlainZone(this);
        }
        if (i == 5) {
            return new RallyGroup(this);
        }
        if (i == 0) {
            GameEngine.logColored("Found zone type 0, loading PlainZone instead");
            return new PlainZone(this);
        }
        throw new RuntimeException("Unknown zone type:" + i);
    }

    @Override // com.corrodinggames.rts.game.PlayerTeam
    /* JADX INFO: renamed from: c */
    public void readExtendedTeamState(GameInputStream gameInputStream) throws IOException {
        AIStrategyNode aIStrategyNodeFindNodeById;
        this.aiEnabled = gameInputStream.readBoolean();
        this.attackTimerEasy = gameInputStream.readFloat();
        this.attackTimerNormal = gameInputStream.readFloat();
        this.attackTimerHard = gameInputStream.readFloat();
        this.attackTimerInsane = gameInputStream.readFloat();
        this.globalTimer = gameInputStream.readFloat();
        this.pathfindingUpdateTimer = gameInputStream.readInt();
        this.attackRangeMultiplier = gameInputStream.readFloat();
        this.canBuild = gameInputStream.readBoolean();
        this.unitProductionTimer = gameInputStream.readInt();
        int i = gameInputStream.readInt();
        this.strategyNodes.clear();
        boolean z = false;
        if (gameInputStream.getProtocolVersion() >= 20) {
            z = true;
            for (int i2 = 0; i2 < i; i2++) {
                createZoneByTypeId((int) gameInputStream.readByte()).strategyId = gameInputStream.readInt();
            }
        }
        for (int i3 = 0; i3 < i; i3++) {
            if (!z) {
                aIStrategyNodeFindNodeById = createZoneByTypeId((int) gameInputStream.readByte());
            } else {
                aIStrategyNodeFindNodeById = findNodeById(gameInputStream.readInt());
            }
            aIStrategyNodeFindNodeById.readFromInputStream(gameInputStream);
        }
        byte b = gameInputStream.readByte();
        if (b >= 1) {
            this.nextStrategyId = gameInputStream.readInt();
        }
        this.activeStrategies.clear();
        this.activeStrategies.addAll(this.strategyNodes);
        if (b >= 2) {
            this.enableNaval = gameInputStream.readBoolean();
            this.isReadyForAttack = gameInputStream.readBoolean();
            this.hasSufficientUnitsForAttack = gameInputStream.readBoolean();
        }
        if (b >= 3) {
            this.isWaitingForBetterAttackTiming = gameInputStream.readBoolean();
            this.isAttackAllowed = gameInputStream.readBoolean();
        }
        if (b >= 4) {
            this.attackTimersUpdateCounter = gameInputStream.readFloat();
        }
        if (b >= 5) {
            this.attackCooldownTimer = gameInputStream.readInt();
        }
        if (b >= 6) {
            this.mapWidth = gameInputStream.readInt();
            this.mapHeight = gameInputStream.readInt();
            this.mapSeed = gameInputStream.readInt();
        }
        if (b >= 7) {
            this.isAggressive = gameInputStream.readBoolean();
        }
        if (b >= 8) {
            this.aiFlags = gameInputStream.readInt();
        }
        if (b >= 9) {
            gameInputStream.a("ai-c s");
            this.aiBehaviors.clear();
            int i4 = gameInputStream.readInt();
            for (int i5 = 0; i5 < i4; i5++) {
                AIBehavior a = ((AIBehaviorType) gameInputStream.readEnumOrdinalOrNull(AIBehaviorType.class)).createBehavior();
                a.readFromInputStream(gameInputStream);
                addAIBehavior(a);
            }
            gameInputStream.a("ai-c e");
        }
        super.readExtendedTeamState(gameInputStream);
        updateUnitCounts();
    }

    /* JADX INFO: renamed from: m */
    public AIStrategyNode findNodeById(int i) {
        for (AIStrategyNode aIStrategyNode : this.strategyNodes) {
            if (aIStrategyNode.strategyId == i) {
                return aIStrategyNode;
            }
        }
        return null;
    }

    /* JADX INFO: renamed from: a */
    public int filterUnitAndCommand(AIStrategyNode aIStrategyNode) {
        if (aIStrategyNode == null) {
            return -1;
        }
        return aIStrategyNode.strategyId;
    }

    /* JADX INFO: renamed from: ak */
    void updateUnitCounts() {
        this.attackGroupCount = 0;
        this.builderGroupCount = 0;
        this.waterBuilderGroupCount = 0;
        this.totalBuilderUnits = 0;
        this.landAttackGroupCount = 0;
        this.airAttackGroupCount = 0;
        this.transporterGroupCount = 0;
        this.activeTransporterGroupCount = 0;
        this.baseCount = 0;
        this.advancedBaseCount = 0;
        this.constructingBaseCount = 0;
        for (AIStrategyNode aIStrategyNode : this.activeStrategies) {
            if (aIStrategyNode instanceof BaseZone) {
                BaseZone baseZone = (BaseZone) aIStrategyNode;
                this.baseCount++;
                if (baseZone.getNumberOfExtractors() >= 2) {
                    this.advancedBaseCount++;
                }
                if (baseZone.hasResources) {
                    this.constructingBaseCount++;
                }
            }
            if (aIStrategyNode instanceof UnitGroup) {
                UnitGroup unitGroup = (UnitGroup) aIStrategyNode;
                if (!unitGroup.isActive) {
                    if (unitGroup.isReadyToAct) {
                        this.attackGroupCount++;
                        if (!unitGroup.isEngaging && !unitGroup.isFull()) {
                            if (unitGroup.isSeaGroup) {
                                this.airAttackGroupCount++;
                            } else {
                                this.landAttackGroupCount++;
                            }
                        }
                    } else {
                        this.builderGroupCount++;
                        if (unitGroup.isFull()) {
                            this.waterBuilderGroupCount++;
                        }
                        this.totalBuilderUnits += unitGroup.getUnitCount();
                    }
                }
            }
            if (aIStrategyNode instanceof TransporterGroup) {
                this.transporterGroupCount++;
                if (((AIUnitGroupBase) aIStrategyNode).getUnitCount() > 0) {
                    this.activeTransporterGroupCount++;
                }
            }
        }
    }

    public AIController(int i, boolean z) {
        super(i, z);
        this.PATHFINDING_OVERLOAD_THRESHOLD = 3000;
        this.aiFlags = 0;
        this.resourceMultiplierHard = 0.0f;
        this.resourceMultiplierInsane = 0.0f;
        this.enableNaval = true;
        this.enableAirForce = true;
        this.enableExperimentalUnits = false;
        this.strategyNodes = new ConcurrentLinkedQueue();
        this.activeStrategies = new ArrayList();
        this.debugPoint = new PointF();
        this.debugMessages = new ArrayList();
        this.builderUnitBuildStrategy = new UnitBuildStrategy(this,"attackingUnitsLand") { // from class: com.corrodinggames.rts.game.a.a.1
            @Override // com.corrodinggames.rts.game.ai.UnitBuildStrategy
            /* JADX INFO: renamed from: a */
            public boolean canBuildUnit(UnitType unitType) {
                return AIController.this.shouldIssueActionForUnitType(unitType) && matchesMovementType(unitType, UnitMovementType.LAND);
            }
        };
        this.landUnitBuildStrategy = new UnitBuildStrategy(this,"attackingUnitsHover") { // from class: com.corrodinggames.rts.game.a.a.6
            @Override // com.corrodinggames.rts.game.ai.UnitBuildStrategy
            /* JADX INFO: renamed from: a */
            public boolean canBuildUnit(UnitType unitType) {
                return AIController.this.shouldIssueActionForUnitType(unitType) && matchesMovementType(unitType, UnitMovementType.HOVER);
            }
        };
        this.airUnitBuildStrategy = new UnitBuildStrategy(this,"attackingUnitsAir") { // from class: com.corrodinggames.rts.game.a.a.7
            @Override // com.corrodinggames.rts.game.ai.UnitBuildStrategy
            /* JADX INFO: renamed from: a */
            public boolean canBuildUnit(UnitType unitType) {
                return AIController.this.shouldIssueActionForUnitType(unitType) && matchesMovementType(unitType, UnitMovementType.AIR);
            }
        };
        this.seaUnitBuildStrategy = new UnitBuildStrategy(this,"attackingUnitsWater") { // from class: com.corrodinggames.rts.game.a.a.8
            @Override // com.corrodinggames.rts.game.ai.UnitBuildStrategy
            /* JADX INFO: renamed from: a */
            public boolean canBuildUnit(UnitType unitType) {
                return AIController.this.shouldIssueActionForUnitType(unitType) && matchesMovementType(unitType, UnitMovementType.WATER);
            }
        };
        this.experimentalUnitBuildStrategy = new UnitBuildStrategy(this,"buildingUnits") { // from class: com.corrodinggames.rts.game.a.a.9
            @Override // com.corrodinggames.rts.game.ai.UnitBuildStrategy
            /* JADX INFO: renamed from: a */
            public boolean canBuildUnit(UnitType unitType) {
                if (BaseUnit.findAttackDamageSource(unitType).bI()) {
                    if ((unitType instanceof CustomUnitConfig) && ((CustomUnitConfig) unitType).disableUse) {
                        return false;
                    }
                    return true;
                }
                return false;
            }
        };
        this.antiNukeUnitBuildStrategy = new UnitBuildStrategy(this,"transportUnits") { // from class: com.corrodinggames.rts.game.a.a.10
            @Override // com.corrodinggames.rts.game.ai.UnitBuildStrategy
            /* JADX INFO: renamed from: a */
            public boolean canBuildUnit(UnitType unitType) {
                if (AIController.this.isNonCombatCustomUnit(BaseUnit.findAttackDamageSource(unitType))) {
                    if ((unitType instanceof CustomUnitConfig) && ((CustomUnitConfig) unitType).disableUse) {
                        return false;
                    }
                    if (unitType.o() == UnitMovementType.AIR || unitType.o() == UnitMovementType.HOVER || unitType.o() == UnitMovementType.OVER_CLIFF_WATER) {
                        return true;
                    }
                    return false;
                }
                return false;
            }
        };
        this.baseDefenseUnitBuildStrategy = new UnitBuildStrategy(this,"transportUnitsFlying") { // from class: com.corrodinggames.rts.game.a.a.11
            @Override // com.corrodinggames.rts.game.ai.UnitBuildStrategy
            /* JADX INFO: renamed from: a */
            public boolean canBuildUnit(UnitType unitType) {
                if (AIController.this.antiNukeUnitBuildStrategy.canBuildUnit(unitType) && unitType.o() == UnitMovementType.AIR) {
                    return true;
                }
                return false;
            }
        };
        this.scoutUnitBuildStrategy = new UnitBuildStrategy(this,"transportUnitsNonFlying") { // from class: com.corrodinggames.rts.game.a.a.12
            @Override // com.corrodinggames.rts.game.ai.UnitBuildStrategy
            /* JADX INFO: renamed from: a */
            public boolean canBuildUnit(UnitType unitType) {
                if (AIController.this.antiNukeUnitBuildStrategy.canBuildUnit(unitType) && unitType.o() != UnitMovementType.AIR) {
                    return true;
                }
                return false;
            }
        };
        this.fabricatorUnitBuildStrategy = new UnitBuildStrategy(this,"builderUnits") { // from class: com.corrodinggames.rts.game.a.a.13
            @Override // com.corrodinggames.rts.game.ai.UnitBuildStrategy
            /* JADX INFO: renamed from: a */
            public boolean canBuildUnit(UnitType unitType) {
                if (unitType.m()) {
                    if ((!(unitType instanceof CustomUnitConfig) || !((CustomUnitConfig) unitType).disableUse) && unitType.o() != UnitMovementType.WATER) {
                        return true;
                    }
                    return false;
                }
                return false;
            }
        };
        this.extractorUnitBuildStrategy = new UnitBuildStrategy(this,"harvesterUnits") { // from class: com.corrodinggames.rts.game.a.a.2
            @Override // com.corrodinggames.rts.game.ai.UnitBuildStrategy
            /* JADX INFO: renamed from: a */
            public boolean canBuildUnit(UnitType unitType) {
                BaseUnit.findAttackDamageSource(unitType);
                if (unitType.n()) {
                    if ((!(unitType instanceof CustomUnitConfig) || !((CustomUnitConfig) unitType).disableUse) && unitType.o() != UnitMovementType.WATER) {
                        return true;
                    }
                    return false;
                }
                return false;
            }
        };
        this.landFactoryUnitBuildStrategy = new UnitBuildStrategy(this,"extractorUnits") { // from class: com.corrodinggames.rts.game.a.a.3
            @Override // com.corrodinggames.rts.game.ai.UnitBuildStrategy
            /* JADX INFO: renamed from: a */
            public boolean canBuildUnit(UnitType unitType) {
                if (BaseUnit.findAttackDamageSource(unitType).bI() && unitType.p()) {
                    if ((unitType instanceof CustomUnitConfig) && ((CustomUnitConfig) unitType).disableUse) {
                        return false;
                    }
                    return true;
                }
                return false;
            }
        };
        this.airFactoryUnitBuildStrategy = new UnitBuildStrategy(this,"buildingFactories") { // from class: com.corrodinggames.rts.game.a.a.4
            @Override // com.corrodinggames.rts.game.ai.UnitBuildStrategy
            /* JADX INFO: renamed from: a */
            public boolean canBuildUnit(UnitType unitType) {
                UnitType unitTypeI;
                BaseUnit baseUnitFindAttackDamageSource = BaseUnit.findAttackDamageSource(unitType);
                if (baseUnitFindAttackDamageSource.bI()) {
                    if ((unitType instanceof CustomUnitConfig) && ((CustomUnitConfig) unitType).disableUse) {
                        return false;
                    }
                    boolean z2 = false;
                    for (AbstractUnitAction abstractUnitAction : baseUnitFindAttackDamageSource.getAvailableActions()) {
                        if (abstractUnitAction != null && (abstractUnitAction instanceof PopupQueueAction)) {
                            PopupQueueAction popupQueueAction = (PopupQueueAction) abstractUnitAction;
                            if (!popupQueueAction.isDisplayOnly() && (unitTypeI = popupQueueAction.getUnitType()) != null && !unitTypeI.isBuildingUnit()) {
                                z2 = true;
                            }
                        }
                    }
                    if (z2) {
                        return true;
                    }
                    return false;
                }
                return false;
            }
        };
        this.seaFactoryUnitBuildStrategy = new UnitBuildStrategy(this,"buildingFactoriesForBuilders") { // from class: com.corrodinggames.rts.game.a.a.5
            @Override // com.corrodinggames.rts.game.ai.UnitBuildStrategy
            /* JADX INFO: renamed from: a */
            public boolean canBuildUnit(UnitType unitType) {
                UnitType unitTypeI;
                BaseUnit baseUnitFindAttackDamageSource = BaseUnit.findAttackDamageSource(unitType);
                if (baseUnitFindAttackDamageSource.bI()) {
                    if ((unitType instanceof CustomUnitConfig) && ((CustomUnitConfig) unitType).disableUse) {
                        return false;
                    }
                    boolean z2 = false;
                    for (AbstractUnitAction abstractUnitAction : baseUnitFindAttackDamageSource.getAvailableActions()) {
                        if (abstractUnitAction != null && (abstractUnitAction instanceof PopupQueueAction)) {
                            PopupQueueAction popupQueueAction = (PopupQueueAction) abstractUnitAction;
                            if (!popupQueueAction.isDisplayOnly() && (unitTypeI = popupQueueAction.getUnitType()) != null && !unitTypeI.isBuildingUnit() && unitTypeI.m()) {
                                z2 = true;
                            }
                        }
                    }
                    if (z2) {
                        return true;
                    }
                    return false;
                }
                return false;
            }
        };
        this.buildPreferenceCache = new BuildPreferenceCache();
        this.aiUnitManagementTimer = 0.0f;
        this.unitsToProcess = new ArrayList();
        this.aiBehaviors = new FastArrayList();
        initializeController();
    }

    public AIController(int i) {
        this(i, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: a */
    public boolean shouldIssueActionForUnitType(UnitType unitType) {
        BaseUnit baseUnitFindAttackDamageSource = BaseUnit.findAttackDamageSource(unitType);
        if (!baseUnitFindAttackDamageSource.bI() && (baseUnitFindAttackDamageSource instanceof OrderableUnit) && !isNonCombatCustomUnit(baseUnitFindAttackDamageSource) && !baseUnitFindAttackDamageSource.canUnitAttack() && ((OrderableUnit) baseUnitFindAttackDamageSource).canAttack()) {
            if (unitType instanceof CustomUnitConfig) {
                CustomUnitConfig customUnitConfig = (CustomUnitConfig) unitType;
                if (customUnitConfig.disableUse || !customUnitConfig.useAsAttacker) {
                    return false;
                }
                return true;
            }
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: av */
    private void initializeController() {
        GameEngine gameEngine = GameEngine.getInstance();
        this.attackTimerEasy = 100 + (this.teamId * 9);
        this.attackTimerHard = SlickToAndroidKeycodes.AndroidCodes.KEYCODE_BUTTON_15 + (this.teamId * 19);
        this.resourceMultiplierEasy = 50 + (this.teamId * 2);
        this.attackRangeMultiplier = 4200 + (this.teamId * 5);
        this.globalTimer = 3500 + (this.teamId * 5);
        this.attackTimersUpdateCounter = 7500 + (this.teamId * 5);
        this.debugPaint = new Paint();
        this.debugPaint.b(Color.a(0, 255, 0));
        this.debugPaint.a(Paint.Style.STROKE);
        this.debugPaint.a(true);
        gameEngine.setScaledTextSize(this.debugPaint, 14.0f);
        initializeBuildStrategies();
    }

    /* JADX INFO: renamed from: al */
    public void initializeBuildStrategies() {
        Iterator it = this.debugMessages.iterator();
        while (it.hasNext()) {
            ((UnitBuildStrategy) it.next()).rebuildUnitMix();
        }
    }

    /* JADX INFO: renamed from: d */
    public void setDebugKeyState(String str) {
        GameEngine.log("ai_debug(" + this.teamId + ")", str);
    }

    /* JADX INFO: renamed from: am */
    public PointF getRandomTilePosition() {
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.tileMap.setCursorTileIndexFromTileIndex(Utility.getRandomIntInRange(0, gameEngine.tileMap.tileCountX), Utility.getRandomIntInRange(0, gameEngine.tileMap.tileCountY));
        this.debugPoint.a(gameEngine.tileMap.cursorTileX, gameEngine.tileMap.cursorTileY);
        return this.debugPoint;
    }

    /* JADX INFO: renamed from: an */
    public PointF getRandomUnitTilePosition() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine.tileMap.unitObjects.size() == 0) {
            return null;
        }
        Point point = (Point) gameEngine.tileMap.unitObjects.get(Utility.getRandomInt(gameEngine.tileMap.unitObjects.size()));
        gameEngine.tileMap.setCursorTileIndexFromTileIndex(point.worldX, point.worldY);
        this.debugPoint.a(gameEngine.tileMap.cursorTileX, gameEngine.tileMap.cursorTileY);
        return this.debugPoint;
    }

    /* JADX INFO: renamed from: a */
    public PointF getNearestTileToPosition(float f, float f2) {
        GameEngine gameEngine = GameEngine.getInstance();
        float f3 = -1.0f;
        PointF pointF = new PointF();
        for (int i = 0; i < gameEngine.tileMap.unitObjects.size(); i++) {
            Point point = (Point) gameEngine.tileMap.unitObjects.get(i);
            gameEngine.tileMap.setCursorTileIndexFromTileIndex(point.worldX, point.worldY);
            this.debugPoint.a(gameEngine.tileMap.cursorTileX, gameEngine.tileMap.cursorTileY);
            PointF pointF2 = this.debugPoint;
            float fDistanceSq = Utility.distanceSq(pointF2.x, pointF2.y, f, f2);
            if (fDistanceSq < f3 || f3 == -1.0f) {
                f3 = fDistanceSq;
                pointF.a(pointF2);
            }
        }
        if (f3 == -1.0f) {
            return null;
        }
        return pointF;
    }

    /* JADX INFO: renamed from: e */
    BaseZone findZoneForUnit(BaseUnit baseUnit) {
        for (AIStrategyNode aIStrategyNode : this.activeStrategies) {
            if (aIStrategyNode instanceof BaseZone) {
                BaseZone baseZone = (BaseZone) aIStrategyNode;
                if (baseZone.isUnitInside(baseUnit)) {
                    return baseZone;
                }
            }
        }
        return null;
    }

    /* JADX INFO: renamed from: b */
    BaseZone findZoneContainingPoint(float f, float f2) {
        for (AIStrategyNode aIStrategyNode : this.activeStrategies) {
            if (aIStrategyNode instanceof BaseZone) {
                BaseZone baseZone = (BaseZone) aIStrategyNode;
                if (baseZone.isPointInside(f, f2)) {
                    return baseZone;
                }
            }
        }
        return null;
    }

    /* JADX INFO: renamed from: f */
    BaseZone findNearestZoneForUnit(BaseUnit baseUnit) {
        return findNearestZone(baseUnit.posX, baseUnit.posY);
    }

    /* JADX INFO: renamed from: c */
    BaseZone findNearestZone(float f, float f2) {
        float f3 = -1.0f;
        BaseZone baseZone = null;
        for (AIStrategyNode aIStrategyNode : this.activeStrategies) {
            if (aIStrategyNode instanceof BaseZone) {
                BaseZone baseZone2 = (BaseZone) aIStrategyNode;
                float fD = baseZone2.getDistanceSqToPoint(f, f2);
                if (baseZone == null || fD < f3) {
                    f3 = fD;
                    baseZone = baseZone2;
                }
            }
        }
        return baseZone;
    }

    /* JADX INFO: renamed from: a */
    BaseZone checkUnitVariableCondition(UnitMovementType unitMovementType, float f, float f2, boolean z) {
        float f3 = -1.0f;
        BaseZone baseZone = null;
        for (AIStrategyNode aIStrategyNode : this.activeStrategies) {
            if (aIStrategyNode instanceof BaseZone) {
                BaseZone baseZone2 = (BaseZone) aIStrategyNode;
                float fD = baseZone2.getDistanceSqToPoint(f, f2);
                if (canUnitReachNode(f, f2, baseZone2, unitMovementType) && (!z || !baseZone2.isContested)) {
                    if (baseZone == null || fD < f3) {
                        f3 = fD;
                        baseZone = baseZone2;
                    }
                }
            }
        }
        return baseZone;
    }

    /* JADX INFO: renamed from: a */
    public static boolean isPathPossibleForUnit(BaseUnit baseUnit, float f, float f2, float f3) {
        if (Utility.distanceSq(baseUnit.posX, baseUnit.posY, f, f2) < f3 * f3) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    private boolean assignTaskToUnitType(PointF pointF) {
        if (findEnemyUnitInRange(this, pointF.x, pointF.y, 290.0f) != null) {
            return false;
        }
        BaseZone baseZoneFindNearestZone = findNearestZone(pointF.x, pointF.y);
        if (baseZoneFindNearestZone != null && baseZoneFindNearestZone.getDistanceSqToPoint(pointF.x, pointF.y) < 490000.0f) {
            return false;
        }
        PointF nearestTileToPosition = getNearestTileToPosition(pointF.x, pointF.y);
        if ((nearestTileToPosition != null && Utility.distanceSq(pointF.x, pointF.y, nearestTileToPosition.x, nearestTileToPosition.y) < 160000.0f) || GameViewUtils.d(pointF.x, pointF.y) || GameViewUtils.d(pointF.x + 60.0f, pointF.y) || GameViewUtils.d(pointF.x, pointF.y + 60.0f) || GameViewUtils.d(pointF.x - 60.0f, pointF.y) || GameViewUtils.d(pointF.x, pointF.y + 60.0f)) {
            return false;
        }
        return true;
    }

    /* JADX INFO: renamed from: b */
    private boolean isSafePointForOutpost(PointF pointF) {
        for (BaseUnit baseUnit : BaseUnit.bE) {
            if (baseUnit.team != this && (baseUnit instanceof CommandCenter)) {
                if (baseUnit.team.c(this) && isPathPossibleForUnit(baseUnit, pointF.x, pointF.y, 300.0f)) {
                    return false;
                }
                if (baseUnit.team.d(this) && isPathPossibleForUnit(baseUnit, pointF.x, pointF.y, 320.0f)) {
                    return false;
                }
            }
        }
        if (countEnemyUnitsInRange(this, pointF.x, pointF.y, 360.0f) >= 4 || countEnemyUnitsInRangeWithFilter((PlayerTeam) this, pointF.x, pointF.y, 360.0f, true) >= 2) {
            return false;
        }
        return true;
    }

    /* JADX INFO: renamed from: a */
    public int countUnitsForBuildStrategyWithFilter(UnitBuildStrategy unitBuildStrategy, UnitFilterMode unitFilterMode) {
        int iAssignTaskToUnitTypeOnCommand = 0;
        Iterator it = unitBuildStrategy.unitPriorities.iterator();
        while (it.hasNext()) {
            iAssignTaskToUnitTypeOnCommand += assignTaskToUnitTypeOnCommand(((UnitBuildPriority) it.next()).unitType, unitFilterMode);
        }
        return iAssignTaskToUnitTypeOnCommand;
    }

    /* JADX INFO: renamed from: a */
    public int assignTaskToUnitTypeOnCommand(UnitType unitType, UnitFilterMode unitFilterMode) {
        return applyAIBuilder(unitType, true, unitFilterMode);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: renamed from: a */
    public int applyAIBuilder(UnitType unitType, boolean z, UnitFilterMode unitFilterMode) {
        boolean zJ = unitType.isBuildingUnit();
        Integer numA = this.buildPreferenceCache.getCachedCount(zJ, unitType, z);
        if (numA != null) {
            return numA.intValue();
        }
        int iH = 0;
        if (zJ) {
            z = false;
        }
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i = 0; i < size; i++) {
            BaseUnit baseUnit = baseUnitArrA[i];
            if (baseUnit.team == this && (unitFilterMode == UnitFilterMode.include || !baseUnit.isActive)) {
                if (baseUnit.unitType == unitType) {
                    iH++;
                }
                if (z && (baseUnit instanceof FactoryQueueInterface)) {
                    iH += ((FactoryQueueInterface) baseUnit).h(unitType);
                }
            }
        }
        this.buildPreferenceCache.putCachedCount(zJ, unitType, z, Integer.valueOf(iH));
        return iH;
    }

    /* JADX INFO: renamed from: ao */
    public int countUnitsInGroups() {
        int size = 0;
        for (AIStrategyNode aIStrategyNode : this.activeStrategies) {
            if (aIStrategyNode instanceof UnitGroup) {
                size += ((UnitGroup) aIStrategyNode).unitsNeedingTransport.size();
            }
        }
        return size;
    }

    /* JADX INFO: renamed from: g */
    public boolean isNonCombatCustomUnit(BaseUnit baseUnit) {
        if (baseUnit instanceof OrderableUnit) {
            OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
            if (orderableUnit.canTransportUnits()) {
                UnitType unitTypeR = orderableUnit.r();
                if ((unitTypeR instanceof CustomUnitConfig) && !((CustomUnitConfig) unitTypeR).useAsTransport) {
                    return false;
                }
                return true;
            }
            return false;
        }
        return false;
    }

    /* JADX INFO: renamed from: h */
    public boolean isCombatCustomUnit(BaseUnit baseUnit) {
        if (baseUnit instanceof OrderableUnit) {
            OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
            if (!orderableUnit.bI() && orderableUnit.canAttack() && !isNonCombatCustomUnit(orderableUnit) && !orderableUnit.canUnitAttack()) {
                UnitType unitTypeR = orderableUnit.r();
                if ((unitTypeR instanceof CustomUnitConfig) && !((CustomUnitConfig) unitTypeR).useAsAttacker) {
                    return false;
                }
                return true;
            }
            return false;
        }
        return false;
    }

    /* JADX INFO: renamed from: b */
    public boolean canUnitEngageTarget(BaseUnit baseUnit, BaseUnit baseUnit2) {
        if (this.isTeamObserver) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (orderableUnit.hasNoCurrentWaypoint() && PathfindingUtils.a(orderableUnit, baseUnit2)) {
                    return true;
                }
                return false;
            }
            return false;
        }
        if (isCombatCustomUnit(baseUnit) && (baseUnit instanceof OrderableUnit) && PathfindingUtils.a((OrderableUnit) baseUnit, baseUnit2)) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: i */
    public void renderDebugOverlay(float f) {
        if (!unitCountsUpdated || !GameEngine.getInstance().isDebugTempMode || this.enableScouting || this.canBuild) {
            return;
        }
        GameEngine gameEngine = GameEngine.getInstance();
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i = 0; i < size; i++) {
            BaseUnit baseUnit = baseUnitArrA[i];
            if (baseUnit.team == this && gameEngine.bufferedVisibleWorldRect.b((int) (baseUnit.posX - 200.0f), (int) (baseUnit.posY - 200.0f), (int) (baseUnit.posX + 200.0f), (int) (baseUnit.posY + 200.0f))) {
                if (baseUnit instanceof OrderableUnit) {
                }
                String str = VariableScope.nullOrMissingString;
                float f2 = (baseUnit.posY - gameEngine.viewpointYSnapped) - 60.0f;
                this.debugPaint.b(Color.a(0, 255, 0));
                if (baseUnit instanceof CommandCenter) {
                    f2 -= 80.0f;
                    str = ((((((str + "Base ( Team:" + this.teamId + " )") + "\nuseTransportsOnThisMap: " + isAttackBlockedByConditions()) + "\nuseHoverTransportsOnThisMap: " + shouldLaunchAttack()) + "\nattackingCount: " + this.unitProductionTimer) + "\ndefendingCount: " + this.unitCount) + "\nnumOfUnitsNeedingTransport: " + countUnitsInGroups()) + "\ntransport: " + this.activeTransporterGroupCount;
                    if (isAggressiveFlagEnabled()) {
                        str = str + "\nTurtling: true";
                    }
                    this.debugPaint.b(Color.a(255, 255, 255));
                }
                if (str.length() != 0) {
                    for (String str2 : str.split("\n")) {
                        float f3 = baseUnit.posX - gameEngine.viewpointXSnapped;
                        float f4 = f2;
                        float fM = (-this.debugPaint.l()) + this.debugPaint.m();
                        gameEngine.renderGraphicsEngine.k();
                        if (gameEngine.zoom > 1.0f) {
                            gameEngine.restoreZoomTransform();
                            f3 *= gameEngine.zoom;
                            f4 *= gameEngine.zoom;
                            fM /= gameEngine.zoom;
                        }
                        gameEngine.renderGraphicsEngine.a(str2, f3, f4, this.debugPaint);
                        gameEngine.renderGraphicsEngine.l();
                        f2 += fM;
                    }
                }
            }
        }
        for (AIStrategyNode aIStrategyNode : this.strategyNodes) {
            if (gameEngine.bufferedVisibleWorldRect.b((int) (aIStrategyNode.posX - aIStrategyNode.radius), (int) (aIStrategyNode.posY - aIStrategyNode.radius), (int) (aIStrategyNode.posX + aIStrategyNode.radius), (int) (aIStrategyNode.posY + aIStrategyNode.radius))) {
                this.debugPaint.b(getTeamColorArgb());
                gameEngine.renderGraphicsEngine.a(aIStrategyNode.posX - gameEngine.viewpointXSnapped, aIStrategyNode.posY - gameEngine.viewpointYSnapped, aIStrategyNode.radius + 2.0f, this.debugPaint);
                int iA = Color.a(0, 255, 0);
                String str3 = VariableScope.nullOrMissingString + "\n" + aIStrategyNode.getClass().getSimpleName() + " ( Team:" + this.teamId + " )";
                float f5 = aIStrategyNode.posY - gameEngine.viewpointYSnapped;
                if (aIStrategyNode instanceof BaseZone) {
                    f5 -= 50.0f;
                    BaseZone baseZone = (BaseZone) aIStrategyNode;
                    String str4 = (((str3 + "\nState: " + baseZone.stage.name() + "(id:" + baseZone.strategyId + ")") + "\nunsafe: " + baseZone.hasEnemyUnits() + " (" + baseZone.isUnderAttack + ")") + "\nunsafeBaseTimer: " + baseZone.lastTimeReclaimed) + "\nallowedUnits: " + baseZone.reclaimScore;
                    if (baseZone.lastBuiltUnitType != null) {
                        str4 = str4 + "\nlastAttemptedBuilding: " + baseZone.lastBuiltUnitType.getUnitTypeDescriptionShort();
                    }
                    if (baseZone.lastBuiltCustomUnit != null) {
                        str4 = str4 + "\nlastAttemptedBuilding-cannotAffordPrice: " + baseZone.lastBuiltCustomUnit.a(false, true, 4, true);
                    }
                    if (baseZone.lastBuiltCustomUnit2 != null) {
                        str4 = str4 + "\nlastAttemptedBuilding-cannotAffordBy: " + baseZone.lastBuiltCustomUnit2.a(false, true, 4, true);
                    }
                    String str5 = ((((str4 + "\nlastAttemptedBuildingCount: " + baseZone.numberOfLandUnits) + "\nlastAttemptedBuildingFailed: " + baseZone.numberOfAirUnits) + "\nlastUnitAttempt: " + baseZone.debugText + " (" + baseZone.numberOfWaterUnits + " - " + baseZone.numberOfBuildings + ")") + "\nbuildBuildingDelay: " + baseZone.defensiveScore) + "\ncredits: " + Utility.formatNumberWithTwoDecimals(this.credits) + " (x" + Utility.padString(getSpectatorEnergyFactor()) + ")";
                    if (baseZone.stage == BaseZoneStage.Pre) {
                        str5 = str5 + "\nclaimedBaseTimer: " + baseZone.extractorScore;
                    }
                    if (baseZone.waterUnitsScore > 100.0f) {
                        str5 = str5 + "\nabandonedTimer: " + baseZone.waterUnitsScore;
                    }
                    if (baseZone.updateTimer > 0.0f) {
                        str5 = str5 + "\nrequestedBuildersDelay: " + baseZone.updateTimer + " (" + baseZone.lastAttackedTimer + ")";
                    }
                    str3 = (str5 + "\nBuilders: " + baseZone.numberOfFactories) + "\nIdle Builders: " + baseZone.numberOfCombatUnits;
                }
                if (aIStrategyNode instanceof UnitGroup) {
                    UnitGroup unitGroup = (UnitGroup) aIStrategyNode;
                    if (unitGroup.requiresTarget) {
                        str3 = str3 + "\nVIP Mode";
                    }
                    String str6 = (((str3 + "\n" + (unitGroup.isDefensive() ? "Defensive Type" : "Attack Type")) + "\nUnits: " + unitGroup.units.size() + " / " + unitGroup.maxUnits) + "\nStagingForAttack: " + unitGroup.isDefending) + "\nAttackDelay: " + unitGroup.attackDelay;
                    if (unitGroup.defendDuration != 0.0f) {
                        str6 = str6 + "\nStagingTimer: " + unitGroup.defendDuration;
                    }
                    String str7 = str6 + "\nStagingTargetFound: " + unitGroup.isInCombat;
                    if (unitGroup.attackingDuration != 0.0f) {
                        str7 = str7 + "\nattackingFor: " + unitGroup.attackingDuration;
                    }
                    str3 = str7 + "\ncommonMovement: " + unitGroup.getCommonMovementType().name();
                    if (unitGroup.isSeaGroup) {
                        str3 = str3 + " (seaGroup)";
                    }
                    if (unitGroup.unitsNeedingTransport.size() > 0) {
                        str3 = str3 + "\nunitsNeedingTransport:" + unitGroup.unitsNeedingTransport.size();
                    }
                    if (unitGroup.groupName != null) {
                        str3 = str3 + "\nlast action:" + unitGroup.groupName;
                    }
                    if (!unitGroup.isEngaging && !unitGroup.isDefending) {
                        str3 = str3 + "\nnext move:" + ((int) secondsToMinutes(unitGroup.nextMoveTimer)) + "s";
                    }
                }
                if (aIStrategyNode instanceof TransporterGroup) {
                    TransporterGroup transporterGroup = (TransporterGroup) aIStrategyNode;
                    str3 = ((str3 + "\nUnitsWanted: " + transporterGroup.capacity) + "\nunits: " + transporterGroup.units.size()) + "\nreadyToMoveOut: " + transporterGroup.isWaitingForUnits;
                    if (transporterGroup.unitGroup != null) {
                        str3 = str3 + "\nCurrentlyHelping: " + transporterGroup.unitGroup.strategyId;
                    }
                }
                if (aIStrategyNode instanceof RallyGroup) {
                    str3 = str3 + "\nneedsTransportGroup: " + ((RallyGroup) aIStrategyNode).lifetime;
                }
                this.debugPaint.b(getTeamColorArgb());
                for (String str8 : str3.split("\n")) {
                    if (!str8.trim().equals(VariableScope.nullOrMissingString)) {
                        float f6 = aIStrategyNode.posX - gameEngine.viewpointXSnapped;
                        float f7 = f5;
                        float fM2 = (-this.debugPaint.l()) + this.debugPaint.m();
                        gameEngine.renderGraphicsEngine.k();
                        if (gameEngine.zoom > 1.0f) {
                            gameEngine.restoreZoomTransform();
                            f6 *= gameEngine.zoom;
                            f7 *= gameEngine.zoom;
                            fM2 /= gameEngine.zoom;
                        }
                        gameEngine.renderGraphicsEngine.a(str8, f6, f7, this.debugPaint);
                        gameEngine.renderGraphicsEngine.l();
                        f5 += fM2;
                        this.debugPaint.b(iA);
                    }
                }
            }
        }
    }

    /* JADX INFO: renamed from: e */
    public BaseUnit getAnySelectableUnitForTeam(PlayerTeam playerTeam) {
        for (BaseUnit baseUnit : BaseUnit.bE) {
            if (baseUnit.team == playerTeam && ((baseUnit instanceof CommandCenter) || baseUnit.isTargetable)) {
                return baseUnit;
            }
        }
        for (BaseUnit baseUnit2 : BaseUnit.bE) {
            if (baseUnit2.team == playerTeam && baseUnit2.isBuilder) {
                return baseUnit2;
            }
        }
        return null;
    }

    @Override // com.corrodinggames.rts.game.PlayerTeam
    /* JADX INFO: renamed from: a */
    public void updateTeam(float f) {
        BaseUnit anySelectableUnitForTeam;
        super.updateTeam(f);
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.canBuild || this.enableScouting) {
            return;
        }
        if (!gameEngine.networkEngine.networkGameActive || (gameEngine.networkEngine.isServer && !gameEngine.replayEngine.j())) {
            if (this.aiUnitManagementTimer > 0.0f) {
                this.aiUnitManagementTimer -= f;
                return;
            }
            this.difficultyLevel = getTeamColorId();
            if (this.enableAirForce && gameEngine.gameTimeMillis > 3000) {
                this.enableAirForce = false;
                BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
                int i = 0;
                int size = BaseUnit.bE.size();
                while (true) {
                    if (i >= size) {
                        break;
                    }
                    BaseUnit baseUnit = baseUnitArrA[i];
                    if (!(baseUnit instanceof DamageZone)) {
                        i++;
                    } else {
                        setDebugKeyState("firstRunDelayed: Found damagingBorder");
                        this.avoidDamageZone = (DamageZone) baseUnit;
                        break;
                    }
                }
            }
            if (this.enableNaval) {
                this.enableNaval = false;
                this.isReadyForAttack = true;
                this.hasSufficientUnitsForAttack = true;
                this.isWaitingForBetterAttackTiming = true;
                this.isAttackAllowed = true;
                BaseUnit anySelectableUnitForTeam2 = getAnySelectableUnitForTeam(this);
                if (anySelectableUnitForTeam2 == null) {
                    setDebugKeyState("firstRun: no command center found");
                }
                if (anySelectableUnitForTeam2 != null) {
                    for (int i2 = 0; i2 < PlayerTeam.TEAM_NEUTRAL; i2++) {
                        PlayerTeam playerTeamK = PlayerTeam.k(i2);
                        if (playerTeamK != null && playerTeamK != this && (anySelectableUnitForTeam = getAnySelectableUnitForTeam(playerTeamK)) != null) {
                            if (!isPathPossibleBetweenPoints(anySelectableUnitForTeam2.posX, anySelectableUnitForTeam2.posY, anySelectableUnitForTeam.posX, anySelectableUnitForTeam.posY, UnitMovementType.LAND)) {
                                this.isReadyForAttack = false;
                            }
                            if (!isPathPossibleBetweenPoints(anySelectableUnitForTeam2.posX, anySelectableUnitForTeam2.posY, anySelectableUnitForTeam.posX, anySelectableUnitForTeam.posY, UnitMovementType.HOVER)) {
                                this.hasSufficientUnitsForAttack = false;
                            }
                        }
                    }
                    Iterator it = gameEngine.tileMap.unitObjects.iterator();
                    while (it.hasNext()) {
                        PointF worldPoint = gameEngine.tileMap.tileToWorldPoint((Point) it.next());
                        if (!isPathPossibleBetweenPoints(anySelectableUnitForTeam2.posX, anySelectableUnitForTeam2.posY, worldPoint.x, worldPoint.y + gameEngine.tileMap.tileWorldSizeY, UnitMovementType.LAND)) {
                            this.isWaitingForBetterAttackTiming = false;
                        }
                        if (!isPathPossibleBetweenPoints(anySelectableUnitForTeam2.posX, anySelectableUnitForTeam2.posY, worldPoint.x, worldPoint.y + gameEngine.tileMap.tileWorldSizeY, UnitMovementType.HOVER)) {
                            this.isAttackAllowed = false;
                        }
                    }
                }
            }
            this.resourceMultiplierEasy += f;
            this.resourceMultiplierNormal += f;
            if (this.resourceMultiplierEasy > 25.0f) {
                this.resourceMultiplierEasy -= 25.0f;
                if (this.resourceMultiplierEasy > 25.0f) {
                    this.resourceMultiplierEasy = 25.0f;
                }
                if (this.resourceMultiplierEasy < -1.0f) {
                    this.resourceMultiplierEasy = -1.0f;
                }
                for (AIStrategyNode aIStrategyNode : this.strategyNodes) {
                    if (aIStrategyNode instanceof BaseZone) {
                        ((BaseZone) aIStrategyNode).resourceScore += this.resourceMultiplierNormal;
                    }
                }
                for (int i3 = 0; i3 < 2; i3++) {
                    BaseZone baseZone = null;
                    for (AIStrategyNode aIStrategyNode2 : this.strategyNodes) {
                        if (aIStrategyNode2 instanceof BaseZone) {
                            BaseZone baseZone2 = (BaseZone) aIStrategyNode2;
                            if (baseZone == null || baseZone.resourceScore < baseZone2.resourceScore) {
                                baseZone = baseZone2;
                            }
                        }
                    }
                    if (baseZone == null || baseZone.resourceScore < 50.0f) {
                        break;
                    }
                    BaseZone baseZone3 = baseZone;
                    baseZone3.updateZone(baseZone3.resourceScore);
                    baseZone3.updateZoneStrategy(baseZone3.resourceScore);
                    baseZone3.resourceScore = 0.0f;
                }
                this.resourceMultiplierNormal = 0.0f;
            }
            this.attackTimerEasy += f;
            this.attackTimerNormal += f;
            if (this.attackTimerEasy > 80.0f) {
                updateAILogic(this.attackTimerNormal);
                this.attackTimerEasy -= 80.0f;
                if (this.attackTimerEasy > 80.0f) {
                    this.attackTimerEasy = 80.0f;
                }
                if (this.attackTimerEasy < -1.0f) {
                    this.attackTimerEasy = -1.0f;
                }
                this.attackTimerNormal = 0.0f;
            }
            this.attackTimerHard += f;
            this.attackTimerInsane += f;
            if (this.attackTimerHard > 250.0f) {
                updateCoreLogic(this.attackTimerInsane);
                this.attackTimerHard -= 250.0f;
                if (this.attackTimerHard > 250.0f) {
                    this.attackTimerHard = 250.0f;
                }
                if (this.attackTimerHard < -1.0f) {
                    this.attackTimerHard = -1.0f;
                }
                this.attackTimerInsane = 0.0f;
            }
        }
    }

    /* JADX INFO: renamed from: j */
    public float secondsToMilliseconds(float f) {
        return (f / 60.0f) * 1000.0f;
    }

    /* JADX INFO: renamed from: k */
    public float secondsToMinutes(float f) {
        return f / 60.0f;
    }

    /* JADX INFO: renamed from: a */
    public void issueCommand(OrderableUnit orderableUnit, ActionId actionId) {
        Command commandNewCommandForTeam = GameEngine.getInstance().commandController.newCommandForTeam(this);
        commandNewCommandForTeam.addUnitToCommand(orderableUnit);
        commandNewCommandForTeam.setActionId(actionId);
    }

    /* JADX INFO: renamed from: l */
    public void updateSpecialUnits(float f) {
        for (BaseUnit baseUnit : BaseUnit.bE) {
            if (baseUnit.team == this && (baseUnit instanceof OrderableUnit) && isEligibleUnitForRandomSelection(baseUnit)) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (orderableUnit instanceof AttackSubmarine) {
                    boolean z = false;
                    BaseUnit commandOrAttackTarget = orderableUnit.getCommandOrAttackTarget();
                    if (commandOrAttackTarget != null && orderableUnit.isWithinEngagementRange(commandOrAttackTarget)) {
                        z = !commandOrAttackTarget.isTouchingWater();
                    }
                    boolean z2 = !orderableUnit.Q();
                    if (z && z != z2) {
                        issueCommand(orderableUnit, AttackSubmarine.j.getActionId());
                    }
                    if (!z && z != z2) {
                        issueCommand(orderableUnit, AttackSubmarine.k.getActionId());
                    }
                }
                if (orderableUnit instanceof AmphibiousJet) {
                    boolean z3 = true;
                    BaseUnit commandOrAttackTarget2 = orderableUnit.getCommandOrAttackTarget();
                    if (commandOrAttackTarget2 != null && orderableUnit.isWithinEngagementRange(commandOrAttackTarget2)) {
                        z3 = !commandOrAttackTarget2.Q();
                    }
                    boolean z4 = !orderableUnit.Q();
                    if (z3 && z3 != z4) {
                        issueCommand(orderableUnit, AmphibiousJet.y.getActionId());
                    }
                    if (!z3 && z3 != z4) {
                        issueCommand(orderableUnit, AmphibiousJet.z.getActionId());
                    }
                }
                if (orderableUnit.be() == UnitBehaviorType.bomber && orderableUnit.hasNoCurrentWaypoint() && orderableUnit.getCommandOrAttackTarget() != null) {
                    Command commandNewCommandForTeam = GameEngine.getInstance().commandController.newCommandForTeam(this);
                    commandNewCommandForTeam.addUnitToCommand(orderableUnit);
                    commandNewCommandForTeam.setAttackTarget(orderableUnit.getCommandOrAttackTarget());
                }
            }
        }
    }

    /* JADX INFO: renamed from: c */
    public AttackMode getAttackModeForUnit(OrderableUnit orderableUnit) {
        if (orderableUnit.aS()) {
            boolean z = true;
            if (orderableUnit.canUnitAttack()) {
                z = false;
            }
            if (isNonCombatCustomUnit(orderableUnit)) {
                z = false;
            }
            if (z) {
                if (this.isAggressive) {
                    return AttackMode.aggressive;
                }
                return AttackMode.outOfRange;
            }
        }
        return AttackMode.onlyInRange;
    }

    /* JADX INFO: renamed from: ap */
    public ArrayList getReusableList() {
        tempUnitList.clear();
        return tempUnitList;
    }

    /* JADX INFO: renamed from: d */
    public void onUnitPreUpdate(OrderableUnit orderableUnit) {
        Iterator it = this.aiBehaviors.iterator();
        while (it.hasNext()) {
            ((AIBehavior) it.next()).onUnitAdded(this, orderableUnit);
        }
    }

    /* JADX INFO: renamed from: e */
    public void onUnitPostUpdate(OrderableUnit orderableUnit) {
        Iterator it = this.aiBehaviors.iterator();
        while (it.hasNext()) {
            ((AIBehavior) it.next()).onUnitRemoved(this, orderableUnit);
        }
    }

    /* JADX INFO: renamed from: m */
    public void updateCoreLogic(float f) {
        boolean z;
        BaseUnit randomIdleUnit;
        GameEngine gameEngine = GameEngine.getInstance();
        this.buildPreferenceCache.clearBuildingCaches();
        Iterator it = this.aiBehaviors.iterator();
        while (it.hasNext()) {
            ((AIBehavior) it.next()).updateCore(secondsToMilliseconds(f), this);
        }
        int i = 0;
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i2 = 0; i2 < size; i2++) {
            BaseUnit baseUnit = baseUnitArrA[i2];
            if (baseUnit.team == this && !baseUnit.u()) {
                i++;
                if (baseUnit instanceof OrderableUnit) {
                    OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                    if (!orderableUnit.isVisible) {
                        orderableUnit.isVisible = true;
                        onUnitPreUpdate(orderableUnit);
                    }
                    if (baseUnit.transportContainer == null) {
                        BaseZone baseZone = orderableUnit.aC;
                        orderableUnit.aC = findNearestZoneForUnit(orderableUnit);
                        if (orderableUnit.aC != null && baseZone != orderableUnit.aC) {
                            if (orderableUnit.bI()) {
                                orderableUnit.aD = isPathPossibleBetweenPoints(baseUnit.posX, baseUnit.posY, orderableUnit.aC.posX, orderableUnit.aC.posY, UnitMovementType.LAND);
                                if (!orderableUnit.aD && orderableUnit.r().p()) {
                                    orderableUnit.aD = isPathPossibleBetweenPoints(baseUnit.posX, baseUnit.posY + 15.0f, orderableUnit.aC.posX, orderableUnit.aC.posY, UnitMovementType.LAND);
                                }
                            } else {
                                orderableUnit.aD = isPathPossibleBetweenPoints(baseUnit.posX, baseUnit.posY, orderableUnit.aC.posX, orderableUnit.aC.posY, UnitMovementType.LAND);
                            }
                        }
                    }
                }
            }
        }
        updateSpecialUnits(f);
        for (BaseUnit baseUnit2 : BaseUnit.bE) {
            if (baseUnit2.team == this && (baseUnit2 instanceof OrderableUnit)) {
                OrderableUnit orderableUnit2 = (OrderableUnit) baseUnit2;
                AttackMode attackModeForUnit = getAttackModeForUnit(orderableUnit2);
                if (orderableUnit2.attackMode != attackModeForUnit && isEligibleUnitForRandomSelection(orderableUnit2)) {
                    Command commandNewCommandForTeam = gameEngine.commandController.newCommandForTeam(this);
                    commandNewCommandForTeam.addUnitToCommand(orderableUnit2);
                    commandNewCommandForTeam.setAttackMode(attackModeForUnit);
                }
                if (orderableUnit2.canUnitAttack() && orderableUnit2.isExperimental() && orderableUnit2.aB == null && isEligibleUnitForRandomSelection(orderableUnit2)) {
                    UnitGroup.createHuntGroup(this, orderableUnit2);
                }
            }
        }
        if (i == 0 && !this.isTeamObserver) {
            this.enableScouting = true;
        }
        this.attackTimersUpdateCounter = Utility.moveTowardsZero(this.attackTimersUpdateCounter, f);
        this.globalTimer = Utility.moveTowardsZero(this.globalTimer, f);
        if (isInsaneDifficulty()) {
            this.globalTimer = Utility.moveTowardsZero(this.globalTimer, 4.0f * f);
        }
        if (this.globalTimer == 0.0f) {
            int i3 = 0;
            for (AIStrategyNode aIStrategyNode : this.activeStrategies) {
                if ((aIStrategyNode instanceof BaseZone) && ((BaseZone) aIStrategyNode).stage == BaseZoneStage.Pre) {
                    i3++;
                }
            }
            boolean z2 = false;
            if (i3 > 2) {
                z2 = true;
            }
            if (z2) {
                this.globalTimer = 300.0f;
            } else {
                PointF randomUnitTilePosition = getRandomUnitTilePosition();
                if (randomUnitTilePosition != null) {
                    randomUnitTilePosition.y += gameEngine.tileMap.tileWorldSizeY;
                    if (findZoneContainingPoint(randomUnitTilePosition.x, randomUnitTilePosition.y) == null && isSafePointForOutpost(randomUnitTilePosition)) {
                        this.globalTimer = 2000.0f;
                        BaseZone baseZone2 = new BaseZone(this, randomUnitTilePosition.x, randomUnitTilePosition.y);
                        baseZone2.radius = 360.0f;
                        baseZone2.stage = BaseZoneStage.Pre;
                        baseZone2.zoneType = BaseZoneType.ResourceOutpost;
                        this.mapSeed++;
                    }
                }
            }
        }
        if (this.attackTimersUpdateCounter == 0.0f) {
            this.attackTimersUpdateCounter = 100.0f;
            int i4 = 0;
            for (AIStrategyNode aIStrategyNode2 : this.activeStrategies) {
                if ((aIStrategyNode2 instanceof BaseZone) && ((BaseZone) aIStrategyNode2).zoneType == BaseZoneType.ForwardOutpost) {
                    i4++;
                }
            }
            if (i4 < 3 && (randomIdleUnit = getRandomIdleUnit()) != null) {
                PointF pointF = new PointF();
                pointF.x = randomIdleUnit.posX;
                pointF.y = randomIdleUnit.posY;
                if (pointF != null && findZoneContainingPoint(pointF.x, pointF.y) == null && assignTaskToUnitType(pointF)) {
                    this.attackTimersUpdateCounter = 5000.0f;
                    BaseZone baseZone3 = new BaseZone(this, pointF.x, pointF.y);
                    baseZone3.radius = 310.0f;
                    baseZone3.stage = BaseZoneStage.Pre;
                    baseZone3.zoneType = BaseZoneType.ForwardOutpost;
                    this.mapSeed++;
                }
            }
        }
        this.buildingCount = 0;
        this.unitProductionTimer = 0;
        this.unitCount = 0;
        BaseUnit[] baseUnitArrA2 = BaseUnit.bE.a();
        int size2 = BaseUnit.bE.size();
        for (int i5 = 0; i5 < size2; i5++) {
            BaseUnit baseUnit3 = baseUnitArrA2[i5];
            if (baseUnit3.team == this && (baseUnit3 instanceof OrderableUnit)) {
                OrderableUnit orderableUnit3 = (OrderableUnit) baseUnit3;
                if (!baseUnit3.bI()) {
                    if (orderableUnit3.aB != null && orderableUnit3.aB.isDefensive()) {
                        this.unitCount++;
                    } else if (isCombatCustomUnit(orderableUnit3) && !orderableUnit3.isActive) {
                        if (orderableUnit3.getMovementType() == UnitMovementType.WATER) {
                            this.buildingCount++;
                        } else {
                            this.unitProductionTimer++;
                        }
                    }
                }
            }
        }
        this.resourceMultiplierHard = Utility.moveTowardsZero(this.resourceMultiplierHard, f);
        this.resourceMultiplierInsane += f;
        if (this.resourceMultiplierHard == 0.0f) {
            int i6 = 0;
            int i7 = 0;
            int i8 = 0;
            int i9 = 0;
            for (BaseUnit baseUnit4 : BaseUnit.bE) {
                if (baseUnit4.team == this && baseUnit4.isAlive()) {
                    if (((baseUnit4 instanceof LandFactory) || (baseUnit4 instanceof AirFactory) || (baseUnit4 instanceof SeaFactory)) && (baseUnit4 instanceof AirFactory)) {
                        i7++;
                        if (((AirFactory) baseUnit4).getUpgradeLevel() > 1) {
                            i6++;
                        }
                    }
                    if (baseUnit4.r().p()) {
                        i8++;
                        if (AbstractUnitAction.isActionIdSpecified(baseUnit4.cm())) {
                            i9++;
                        }
                    }
                }
            }
            if (hasCredits(4100.0d) || this.resourceMultiplierInsane > 2400.0f || this.constructingBaseCount == 0) {
                for (BaseUnit baseUnit5 : BaseUnit.bE) {
                    if (baseUnit5.team == this && (baseUnit5 instanceof OrderableUnit)) {
                        OrderableUnit orderableUnit4 = (OrderableUnit) baseUnit5;
                        if (orderableUnit4.hasAiHighPriorityAction()) {
                            ArrayList<AbstractUnitAction> arrayListN = orderableUnit4.getAvailableActions();
                            ArrayList reusableList = getReusableList();
                            for (AbstractUnitAction abstractUnitAction : arrayListN) {
                                if (abstractUnitAction.isAiHighPriority(orderableUnit4)) {
                                    reusableList.add(abstractUnitAction);
                                }
                            }
                            if (reusableList.size() > 0) {
                                issueUnitAction(orderableUnit4, (AbstractUnitAction) AIUnitActionUtils.getRandomElement(reusableList));
                            }
                        }
                    }
                }
                boolean z3 = false;
                if (hasCredits(30000.0d)) {
                    z3 = true;
                }
                for (BaseUnit baseUnit6 : BaseUnit.bE) {
                    if (baseUnit6.team == this && (baseUnit6 instanceof OrderableUnit)) {
                        OrderableUnit orderableUnit5 = (OrderableUnit) baseUnit6;
                        ActionId actionIdCm = orderableUnit5.cm();
                        if (AbstractUnitAction.isActionIdSpecified(actionIdCm)) {
                            float fCn = orderableUnit5.getAiUpgradePriority();
                            if (fCn < 0.0f) {
                                fCn = 6.0f;
                                z = false;
                            } else {
                                z = true;
                            }
                            if (fCn == 0.0f) {
                                continue;
                            } else {
                                int randomInt = Utility.getRandomInt(100);
                                float f2 = 100.0f - fCn;
                                if (z3) {
                                    f2 -= 4.0f;
                                }
                                if (!z) {
                                    if (baseUnit6.r().p() && i9 > 0) {
                                        f2 = 50.0f;
                                    }
                                    if (i7 > 0 && i6 == 0) {
                                        f2 = 99.0f;
                                        if (baseUnit6 instanceof AirFactory) {
                                            f2 = 40.0f;
                                        }
                                    }
                                }
                                if (f2 < 10.0f) {
                                    f2 = 10.0f;
                                }
                                if (((float) randomInt) > f2) {
                                    if (orderableUnit5.getUnitAIPathfindTarget()) {
                                    }
                                    if (Utility.getRandomInt(100) > 50) {
                                        orderableUnit5.clearAndAddAction(this.unitsToProcess);
                                        if (this.unitsToProcess.size() != 0) {
                                            actionIdCm = (ActionId) this.unitsToProcess.get(new Random().nextInt(this.unitsToProcess.size()));
                                        }
                                    }
                                    boolean z4 = false;
                                    AbstractUnitAction abstractUnitActionA = orderableUnit5.validateActionId(actionIdCm);
                                    if (abstractUnitActionA != null) {
                                        if (abstractUnitActionA.isAiDisabled(orderableUnit5)) {
                                            z4 = true;
                                        }
                                        if (abstractUnitActionA.getActionType() == ActionType.targetGround) {
                                            z4 = true;
                                        }
                                        if (!abstractUnitActionA.b(orderableUnit5)) {
                                            z4 = true;
                                        }
                                        if (!abstractUnitActionA.canAfford((BaseUnit) orderableUnit5, false)) {
                                            z4 = true;
                                        }
                                    } else {
                                        z4 = true;
                                    }
                                    if (!z4) {
                                        issueCommand(orderableUnit5, actionIdCm);
                                        processAIVariable(orderableUnit5, abstractUnitActionA.getPrice(), true);
                                        this.resourceMultiplierHard = 900.0f;
                                        this.resourceMultiplierInsane = 0.0f;
                                        if (!z3) {
                                            break;
                                        }
                                        if (hasCredits(40000.0d)) {
                                            if (Utility.getRandomInt(100) > 95) {
                                                break;
                                            }
                                        } else if (Utility.getRandomInt(100) > 80) {
                                            break;
                                        }
                                    } else {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
        }
        for (AIStrategyNode aIStrategyNode3 : this.strategyNodes) {
            if (aIStrategyNode3 instanceof AIUnitGroupBase) {
                ((AIUnitGroupBase) aIStrategyNode3).updateCore(f);
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public boolean issueUnitAction(OrderableUnit orderableUnit, AbstractUnitAction abstractUnitAction) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (abstractUnitAction.b(orderableUnit) && abstractUnitAction.canAfford((BaseUnit) orderableUnit, false)) {
            Command commandNewCommandForTeam = gameEngine.commandController.newCommandForTeam(this);
            commandNewCommandForTeam.addUnitToCommand(orderableUnit);
            commandNewCommandForTeam.setActionId(abstractUnitAction.getQueueId());
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public boolean pathCheck(OrderableUnit orderableUnit, AbstractUnitAction abstractUnitAction, PointF pointF, BaseUnit baseUnit) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (abstractUnitAction.b(orderableUnit) && abstractUnitAction.canAfford((BaseUnit) orderableUnit, false)) {
            Command commandNewCommandForTeam = gameEngine.commandController.newCommandForTeam(this);
            commandNewCommandForTeam.addUnitToCommand(orderableUnit);
            commandNewCommandForTeam.setActionTarget(abstractUnitAction.getQueueId(), pointF, baseUnit);
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: aq */
    public void checkZoneIntegrity() {
        for (AIStrategyNode aIStrategyNode : this.strategyNodes) {
            if (aIStrategyNode instanceof BaseZone) {
                ((BaseZone) aIStrategyNode).fixOverlaps();
            }
        }
        for (AIStrategyNode aIStrategyNode2 : this.strategyNodes) {
            for (AIStrategyNode aIStrategyNode3 : this.strategyNodes) {
                if (aIStrategyNode2 != aIStrategyNode3 && aIStrategyNode2.strategyId == aIStrategyNode3.strategyId) {
                    GameEngine.logErrorColored("Id overlap on:" + aIStrategyNode2.strategyId);
                    GameEngine.logErrorColored("zone x:" + aIStrategyNode2.posX);
                    GameEngine.logErrorColored("zone y:" + aIStrategyNode2.posY);
                    GameEngine.logErrorColored("zone radius:" + aIStrategyNode2.radius);
                    GameEngine.logErrorColored("zone type:" + aIStrategyNode2.getClass().getName());
                }
            }
        }
        int i = 0;
        Iterator it = this.strategyNodes.iterator();
        while (it.hasNext()) {
            if (((AIStrategyNode) it.next()) instanceof BaseZone) {
                i++;
            }
        }
        int i2 = 0;
        for (AIStrategyNode aIStrategyNode4 : this.strategyNodes) {
            if (aIStrategyNode4 instanceof BaseZone) {
                for (AIStrategyNode aIStrategyNode5 : this.strategyNodes) {
                    if ((aIStrategyNode5 instanceof BaseZone) && aIStrategyNode4 != aIStrategyNode5 && Utility.distanceSq(aIStrategyNode4.posX, aIStrategyNode4.posY, aIStrategyNode5.posX, aIStrategyNode5.posY) < 400.0f) {
                        i2++;
                    }
                }
            }
        }
        if (i2 > 0) {
            setDebugKeyState("baseOverlapCount:" + i2);
        }
    }

    @Override // com.corrodinggames.rts.game.PlayerTeam
    /* JADX INFO: renamed from: a */
    public void onUnitBuilt(OrderableUnit orderableUnit) {
        if (orderableUnit.team == this) {
            this.buildPreferenceCache.invalidateBuiltUnit(orderableUnit);
        }
    }

    /* JADX INFO: renamed from: n */
    public void updateAILogic(float f) {
        int i;
        UnitCommand currentWaypoint;
        BaseZone baseZoneFindNearestZoneForUnit;
        GameEngine gameEngine = GameEngine.getInstance();
        this.buildPreferenceCache.clearUnitCountCaches();
        Iterator it = this.aiBehaviors.iterator();
        while (it.hasNext()) {
            ((AIBehavior) it.next()).updateAI(secondsToMilliseconds(f), this);
        }
        for (AIStrategyNode aIStrategyNode : this.strategyNodes) {
            if (aIStrategyNode instanceof AIUnitGroupBase) {
                ((AIUnitGroupBase) aIStrategyNode).updateAI(f);
            }
        }
        if (this.avoidDamageZone != null) {
            Iterator it2 = this.strategyNodes.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                AIStrategyNode aIStrategyNode2 = (AIStrategyNode) it2.next();
                if (this.avoidDamageZone.a(aIStrategyNode2.posX, aIStrategyNode2.posY)) {
                    if (aIStrategyNode2 instanceof BaseZone) {
                        aIStrategyNode2.destroy();
                        break;
                    } else if (aIStrategyNode2 instanceof UnitGroup) {
                        PointF pointFA = this.avoidDamageZone.a(aIStrategyNode2.posX, aIStrategyNode2.posY, aIStrategyNode2.radius + 20.0f);
                        aIStrategyNode2.posX = pointFA.x;
                        aIStrategyNode2.posY = pointFA.y;
                    }
                }
            }
        }
        this.attackRangeMultiplier = Utility.moveTowardsZero(this.attackRangeMultiplier, f);
        int i2 = 0;
        Iterator it3 = this.activeStrategies.iterator();
        while (it3.hasNext()) {
            if (((AIStrategyNode) it3.next()) instanceof BaseZone) {
                i2++;
            }
        }
        if (i2 < 1) {
            Iterator it4 = BaseUnit.bE.iterator();
            while (true) {
                if (!it4.hasNext()) {
                    break;
                }
                BaseUnit baseUnit = (BaseUnit) it4.next();
                if (baseUnit.team == this && (baseUnit instanceof CommandCenter)) {
                    BaseZone baseZone = new BaseZone(this, baseUnit.posX, baseUnit.posY);
                    baseZone.radius = 420.0f;
                    baseZone.stage = BaseZoneStage.Active;
                    baseZone.zoneType = BaseZoneType.Main;
                    i2++;
                    break;
                }
            }
            if (i2 < 1) {
                Iterator it5 = BaseUnit.bE.iterator();
                while (true) {
                    if (!it5.hasNext()) {
                        break;
                    }
                    BaseUnit baseUnit2 = (BaseUnit) it5.next();
                    if (baseUnit2.team == this && this.fabricatorUnitBuildStrategy.hasUnitPriority(baseUnit2.r())) {
                        BaseZone baseZone2 = new BaseZone(this, baseUnit2.posX, baseUnit2.posY);
                        baseZone2.radius = 420.0f;
                        baseZone2.stage = BaseZoneStage.Active;
                        baseZone2.zoneType = BaseZoneType.Main;
                        i2++;
                        break;
                    }
                }
            }
            if (i2 < 1) {
                Iterator it6 = BaseUnit.bE.iterator();
                while (true) {
                    if (!it6.hasNext()) {
                        break;
                    }
                    BaseUnit baseUnit3 = (BaseUnit) it6.next();
                    if (baseUnit3.team == this && (baseUnit3 instanceof OrderableUnit)) {
                        OrderableUnit orderableUnit = (OrderableUnit) baseUnit3;
                        boolean z = false;
                        Iterator it7 = this.fabricatorUnitBuildStrategy.unitPriorities.iterator();
                        while (true) {
                            if (it7.hasNext()) {
                                if (orderableUnit.canUseActionForUnitType(((UnitBuildPriority) it7.next()).unitType, true)) {
                                    z = true;
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                        if (z) {
                            BaseZone baseZone3 = new BaseZone(this, baseUnit3.posX, baseUnit3.posY);
                            baseZone3.radius = 420.0f;
                            baseZone3.stage = BaseZoneStage.Active;
                            baseZone3.zoneType = BaseZoneType.Main;
                            i2++;
                            break;
                        }
                    }
                }
            }
            if (i2 < 1) {
                Iterator it8 = BaseUnit.bE.iterator();
                while (true) {
                    if (!it8.hasNext()) {
                        break;
                    }
                    BaseUnit baseUnit4 = (BaseUnit) it8.next();
                    if (baseUnit4.team == this && (baseUnit4 instanceof OrderableUnit) && ((OrderableUnit) baseUnit4).hasHighPriorityAction()) {
                        BaseZone baseZone4 = new BaseZone(this, baseUnit4.posX, baseUnit4.posY);
                        baseZone4.radius = 420.0f;
                        baseZone4.stage = BaseZoneStage.Active;
                        baseZone4.zoneType = BaseZoneType.Main;
                        int i3 = i2 + 1;
                        break;
                    }
                }
            }
            if (!this.enableExperimentalUnits) {
                this.enableExperimentalUnits = true;
                if (countUnitsForBuildStrategyWithFilter(this.landFactoryUnitBuildStrategy, UnitFilterMode.include) >= 1) {
                    for (int i4 = 0; i4 < gameEngine.tileMap.unitObjects.size(); i4++) {
                        Point point = (Point) gameEngine.tileMap.unitObjects.get(i4);
                        gameEngine.tileMap.setCursorTileIndexFromTileIndex(point.worldX, point.worldY);
                        this.debugPoint.a(gameEngine.tileMap.cursorTileX, gameEngine.tileMap.cursorTileY);
                        PointF pointF = this.debugPoint;
                        pointF.y += gameEngine.tileMap.tileWorldSizeY;
                        if (findZoneContainingPoint(pointF.x, pointF.y) == null && addAITask(this.landFactoryUnitBuildStrategy, pointF.x, pointF.y, 200) >= 1 && isSafePointForOutpost(pointF)) {
                            BaseZone baseZone5 = new BaseZone(this, pointF.x, pointF.y);
                            baseZone5.radius = 360.0f;
                            baseZone5.stage = BaseZoneStage.Pre;
                            baseZone5.zoneType = BaseZoneType.ResourceOutpost;
                        }
                    }
                }
            }
        }
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i5 = 0; i5 < size; i5++) {
            BaseUnit baseUnit5 = baseUnitArrA[i5];
            if (baseUnit5.team == this && baseUnit5.transportContainer == null && (baseUnit5 instanceof OrderableUnit) && baseUnit5.canUnitAttack() && isEligibleUnitForRandomSelection(baseUnit5)) {
                OrderableUnit orderableUnit2 = (OrderableUnit) baseUnit5;
                if (findZoneForUnit((BaseUnit) orderableUnit2) != null) {
                    if (orderableUnit2.hasNoCurrentWaypoint()) {
                    }
                } else if (orderableUnit2.hasNoCurrentWaypoint() && (baseZoneFindNearestZoneForUnit = findNearestZoneForUnit(orderableUnit2)) != null) {
                    PointF pointFW = baseZoneFindNearestZoneForUnit.getRandomPointInside();
                    Command commandNewCommandForTeam = gameEngine.commandController.newCommandForTeam(this);
                    commandNewCommandForTeam.addUnitToCommand(orderableUnit2);
                    commandNewCommandForTeam.setMoveTarget(pointFW.x, pointFW.y);
                }
            }
        }
        int size2 = BaseUnit.bE.size();
        for (int i6 = 0; i6 < size2; i6++) {
            BaseUnit baseUnit6 = baseUnitArrA[i6];
            if (baseUnit6.team == this && (baseUnit6 instanceof OrderableUnit)) {
                OrderableUnit orderableUnit3 = (OrderableUnit) baseUnit6;
                if (orderableUnit3.wayPointTimer > 2400.0f && isEligibleUnitForRandomSelection(orderableUnit3)) {
                    if (!orderableUnit3.aN || orderableUnit3.wayPointTimer >= 24000.0f) {
                        Command commandNewCommandForTeam2 = gameEngine.commandController.newCommandForTeam(this);
                        commandNewCommandForTeam2.addUnitToCommand(orderableUnit3);
                        commandNewCommandForTeam2.setClearExistingOrders();
                        if (!orderableUnit3.canUnitAttack()) {
                        }
                    }
                } else if (!orderableUnit3.canUnitAttack() && isEligibleUnitForRandomSelection(orderableUnit3) && (currentWaypoint = orderableUnit3.getCurrentWaypoint()) != null && currentWaypoint.getCommandType() == UnitCommandType.build && orderableUnit3.wayPointTimer > 700.0f) {
                    Command commandNewCommandForTeam3 = gameEngine.commandController.newCommandForTeam(this);
                    commandNewCommandForTeam3.addUnitToCommand(orderableUnit3);
                    commandNewCommandForTeam3.setClearExistingOrders();
                }
            }
        }
        if (!this.isTeamObserver) {
            updateUnitCounts();
            int i7 = 1;
            boolean z2 = true;
            if (isAggressiveMode()) {
                i7 = 1 + 1;
                z2 = false;
            }
            if (this.advancedBaseCount > 6) {
                i7 = 2;
            }
            if (this.advancedBaseCount > 11) {
                i7 = 3;
            }
            if (this.builderGroupCount < i7) {
                UnitGroup unitGroup = new UnitGroup(this, false);
                unitGroup.maxUnits = 8;
                if (isInsaneDifficulty()) {
                    unitGroup.maxUnits = 10;
                }
                unitGroup.selectNewPosition();
                this.mapHeight++;
            }
            if ((this.waterBuilderGroupCount >= i7 || this.totalBuilderUnits > 6) && this.landAttackGroupCount < 1 && z2) {
                UnitGroup unitGroup2 = new UnitGroup(this, true);
                if (this.mapWidth < 2) {
                    unitGroup2.maxUnits = 3;
                } else if (this.mapWidth < 5) {
                    unitGroup2.maxUnits = 5;
                } else {
                    unitGroup2.maxUnits = 7;
                    if (isInsaneDifficulty()) {
                        if (this.mapWidth < 25) {
                            unitGroup2.maxUnits = 14;
                        } else {
                            unitGroup2.maxUnits = 18;
                        }
                    }
                }
                unitGroup2.selectNewPosition();
                this.mapWidth++;
            }
            if (isPathfindingOverloaded() && this.airAttackGroupCount < 1 && z2) {
                UnitGroup unitGroup3 = new UnitGroup(this, true);
                unitGroup3.isSeaGroup = true;
                unitGroup3.maxUnits = 5;
                if (isInsaneDifficulty()) {
                    unitGroup3.maxUnits = 10;
                }
                unitGroup3.selectNewPosition();
            }
            if (isAttackBlockedByConditions() && this.transporterGroupCount < 3) {
                TransporterGroup transporterGroup = new TransporterGroup(this);
                transporterGroup.capacity = 1;
                transporterGroup.selectNewPosition();
            }
        }
        if (this.isTeamObserver) {
            if (this.attackRangeMultiplier > 30.0f) {
                this.attackRangeMultiplier = 30.0f;
            }
            if (this.attackRangeMultiplier == 0.0f) {
                this.pathfindingUpdateTimer++;
                if (this.pathfindingUpdateTimer == 1) {
                    this.attackRangeMultiplier = 1000.0f;
                    return;
                }
                if (this.pathfindingUpdateTimer == 2) {
                    this.attackRangeMultiplier = 3000.0f;
                    BaseUnit randomEnemyUnit = getRandomEnemyUnit();
                    if (randomEnemyUnit != null) {
                        if (this.isTeamObserver) {
                            i = 0;
                        } else {
                            i = 2;
                            if (this.unitProductionTimer < 4) {
                                i = 5;
                            }
                        }
                        Command commandNewCommandForTeam4 = gameEngine.commandController.newCommandForTeam(this);
                        int size3 = BaseUnit.bE.size();
                        for (int i8 = 0; i8 < size3; i8++) {
                            BaseUnit baseUnit7 = baseUnitArrA[i8];
                            if (baseUnit7.team == this && (baseUnit7 instanceof OrderableUnit)) {
                                OrderableUnit orderableUnit4 = (OrderableUnit) baseUnit7;
                                if (!orderableUnit4.isActive && canUnitEngageTarget(orderableUnit4, randomEnemyUnit)) {
                                    if (i <= 0) {
                                        commandNewCommandForTeam4.addUnitToCommand(orderableUnit4);
                                    } else {
                                        i--;
                                    }
                                }
                            }
                        }
                        commandNewCommandForTeam4.setAttackMoveTarget(randomEnemyUnit.posX, randomEnemyUnit.posY);
                        return;
                    }
                    return;
                }
                this.pathfindingUpdateTimer = 0;
            }
        }
    }

    /* JADX INFO: renamed from: i */
    public boolean isEligibleUnitForRandomSelection(BaseUnit baseUnit) {
        if (baseUnit.u() || baseUnit.t() || baseUnit.canNotBeGivenOrdersByPlayer() || baseUnit.isAIUnit) {
            return false;
        }
        return true;
    }

    /* JADX INFO: renamed from: ar */
    public BaseUnit getRandomIdleUnit() {
        BaseUnit baseUnit = null;
        int i = 0;
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i2 = 0; i2 < size; i2++) {
            BaseUnit baseUnit2 = baseUnitArrA[i2];
            if (!baseUnit2.isDead && baseUnit2.transportContainer == null && this == baseUnit2.team && isCombatCustomUnit(baseUnit2)) {
                i++;
            }
        }
        int iRandom = (int) (Math.random() * ((double) i));
        int i3 = 0;
        Iterator it = BaseUnit.bE.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            BaseUnit baseUnit3 = (BaseUnit) it.next();
            if (!baseUnit3.isDead && baseUnit3.transportContainer == null && this == baseUnit3.team && isCombatCustomUnit(baseUnit3)) {
                if (i3 == iRandom) {
                    baseUnit = baseUnit3;
                    break;
                }
                i3++;
            }
        }
        return baseUnit;
    }

    /* JADX INFO: renamed from: as */
    public BaseUnit getRandomEnemyUnit() {
        BaseUnit baseUnit = null;
        int i = 0;
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i2 = 0; i2 < size; i2++) {
            BaseUnit baseUnit2 = baseUnitArrA[i2];
            if (!baseUnit2.isDead && baseUnit2.transportContainer == null && !baseUnit2.u() && c(baseUnit2.team) && isUnitAllowedForSelection(baseUnit2)) {
                i++;
            }
        }
        int iRandom = (int) (Math.random() * ((double) i));
        int i3 = 0;
        Iterator it = BaseUnit.bE.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            BaseUnit baseUnit3 = (BaseUnit) it.next();
            if (!baseUnit3.isDead && baseUnit3.transportContainer == null && !baseUnit3.u() && c(baseUnit3.team) && isUnitAllowedForSelection(baseUnit3)) {
                if (i3 == iRandom) {
                    baseUnit = baseUnit3;
                    break;
                }
                i3++;
            }
        }
        return baseUnit;
    }

    /* JADX INFO: renamed from: at */
    public PointF getRandomEligibleUnitPosition() {
        BaseUnit baseUnit = null;
        int i = 0;
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i2 = 0; i2 < size; i2++) {
            BaseUnit baseUnit2 = baseUnitArrA[i2];
            if (!baseUnit2.isDead && baseUnit2.transportContainer == null && !baseUnit2.u() && c(baseUnit2.team) && isUnitAllowedForSelection(baseUnit2)) {
                i++;
            }
        }
        int iRandom = (int) (Math.random() * ((double) i));
        int i3 = 0;
        Iterator it = BaseUnit.bE.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            BaseUnit baseUnit3 = (BaseUnit) it.next();
            if (!baseUnit3.isDead && baseUnit3.transportContainer == null && !baseUnit3.u() && c(baseUnit3.team) && isUnitAllowedForSelection(baseUnit3)) {
                if (i3 == iRandom) {
                    baseUnit = baseUnit3;
                    break;
                }
                i3++;
            }
        }
        if (baseUnit != null) {
            return new PointF(baseUnit.posX, baseUnit.posY);
        }
        return null;
    }

    /* JADX INFO: renamed from: a */
    public static BaseUnit findEnemyUnitInRange(PlayerTeam playerTeam, float f, float f2, float f3) {
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i = 0; i < size; i++) {
            BaseUnit baseUnit = baseUnitArrA[i];
            if (baseUnit.posX + f3 > f && baseUnit.posX - f3 < f && baseUnit.posY + f3 > f2 && baseUnit.posY - f3 < f2 && baseUnit.team != playerTeam && isPathPossibleForUnit(baseUnit, f, f2, f3) && baseUnit.team.c(playerTeam)) {
                return baseUnit;
            }
        }
        return null;
    }

    /* JADX INFO: renamed from: a */
    public static int countEnemyUnitsInRangeWithFilter(PlayerTeam playerTeam, float f, float f2, float f3, boolean z) {
        int i = 0;
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i2 = 0; i2 < size; i2++) {
            BaseUnit baseUnit = baseUnitArrA[i2];
            if (baseUnit.posX + f3 > f && baseUnit.posX - f3 < f && baseUnit.posY + f3 > f2 && baseUnit.posY - f3 < f2 && baseUnit.team != playerTeam && isPathPossibleForUnit(baseUnit, f, f2, f3) && baseUnit.team.d(playerTeam) && (!z || baseUnit.bI())) {
                i++;
            }
        }
        return i;
    }

    /* JADX INFO: renamed from: b */
    public static int countEnemyUnitsInRange(PlayerTeam playerTeam, float f, float f2, float f3) {
        int i = 0;
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i2 = 0; i2 < size; i2++) {
            BaseUnit baseUnit = baseUnitArrA[i2];
            if (baseUnit.posX + f3 > f && baseUnit.posX - f3 < f && baseUnit.posY + f3 > f2 && baseUnit.posY - f3 < f2 && baseUnit.team != playerTeam && isPathPossibleForUnit(baseUnit, f, f2, f3) && baseUnit.team.c(playerTeam)) {
                i++;
            }
        }
        return i;
    }

    /* JADX INFO: renamed from: a */
    public int addAITask(UnitBuildStrategy unitBuildStrategy, float f, float f2, int i) {
        int iShouldIssueActionForUnitType = 0;
        Iterator it = unitBuildStrategy.unitPriorities.iterator();
        while (it.hasNext()) {
            iShouldIssueActionForUnitType += countUnitsOfTypeInRange(((UnitBuildPriority) it.next()).unitType, f, f2, i);
        }
        return iShouldIssueActionForUnitType;
    }

    /* JADX INFO: renamed from: a */
    public int countUnitsOfTypeInRange(UnitType unitType, float f, float f2, int i) {
        int i2 = 0;
        GameEngine gameEngine = GameEngine.getInstance();
        bI.clear();
        gameEngine.unitSpatialIndex.a(this, f, f2, i, bI);
        BaseUnit[] baseUnitArrA = bI.a();
        int size = bI.size();
        for (int i3 = 0; i3 < size; i3++) {
            BaseUnit baseUnit = baseUnitArrA[i3];
            if (baseUnit.team == this && baseUnit.unitType == unitType && isPathPossibleForUnit(baseUnit, f, f2, i)) {
                i2++;
            }
        }
        return i2;
    }

    /* JADX INFO: renamed from: au */
    public int countAllUnits() {
        int i = 0;
        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
        int size = BaseUnit.bE.size();
        for (int i2 = 0; i2 < size; i2++) {
            BaseUnit baseUnit = baseUnitArrA[i2];
            i++;
        }
        return i;
    }

    @Override // com.corrodinggames.rts.game.PlayerTeam
    /* JADX INFO: renamed from: T */
    public void onTeamActivated() {
        if (this.enableScouting && countAllUnits() != 0) {
            GameEngine.log("waking up AI");
            this.enableScouting = false;
        }
    }

    @Override // com.corrodinggames.rts.game.PlayerTeam
    /* JADX INFO: renamed from: d */
    public void onUnitRemoved(BaseUnit baseUnit) {
        if (!(baseUnit instanceof OrderableUnit)) {
            return;
        }
        OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
        orderableUnit.isVisible = false;
        if (orderableUnit.aC != null) {
            orderableUnit.aC.removeUnit(orderableUnit);
            orderableUnit.aC = null;
        }
        if (orderableUnit.aB != null) {
            orderableUnit.aB.removeUnit(orderableUnit);
            orderableUnit.aB = null;
        }
        onUnitPostUpdate(orderableUnit);
    }

    /* JADX INFO: renamed from: a */
    public void processAIVariable(OrderableUnit orderableUnit, UnitPrice unitPrice, boolean z) {
        if (orderableUnit.aC != null) {
            orderableUnit.aC.recordBuiltFactoryUnit(orderableUnit, unitPrice, z);
        }
    }

    /* JADX INFO: renamed from: j */
    public boolean isUnitAllowedForSelection(BaseUnit baseUnit) {
        if (!baseUnit.isVisibleToEnemies() && c(baseUnit.team)) {
            return false;
        }
        return true;
    }

    /* JADX INFO: renamed from: a */
    public boolean isPathPossibleBetweenPoints(UnitPrice unitPrice, BaseUnit baseUnit) {
        return canUnitReachPoint(unitPrice, baseUnit, false);
    }

    /* JADX INFO: renamed from: a */
    public boolean canUnitReachPoint(UnitPrice unitPrice, BaseUnit baseUnit, boolean z) {
        return unitPrice.b(baseUnit);
    }

    /* JADX INFO: renamed from: a */
    public void addAIBehavior(AIBehavior aIBehavior) {
        if (!this.aiBehaviors.contains(aIBehavior)) {
            this.aiBehaviors.add(aIBehavior);
        } else {
            c("Skipping add of component: " + aIBehavior.getBehaviorType().name());
        }
    }
}
