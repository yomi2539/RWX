package com.corrodinggames.rts.game.ai;

import android.graphics.PointF;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.PathfindingUtils;
import com.corrodinggames.rts.game.units.UnitMovementType;
import com.corrodinggames.rts.game.units.sea.WaterUnit;
import com.corrodinggames.rts.gameFramework.Command;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.utility.FastArrayList;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;
import java.io.IOException;
import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.a.g */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/a/g.class */
public class UnitGroup extends AIUnitGroupBase {
    /* JADX INFO: renamed from: a */
    boolean isActive;
    /* JADX INFO: renamed from: b */
    String groupName;
    /* JADX INFO: renamed from: c */
    boolean requiresTarget;
    /* JADX INFO: renamed from: C */
    public int lastDamageTimeMillis;
    /* JADX INFO: renamed from: e */
    boolean retreatWhenDamaged;
    /* JADX INFO: renamed from: f */
    boolean isRetreating;
    /* JADX INFO: renamed from: g */
    OrderableUnit targetUnit;
    /* JADX INFO: renamed from: h */
    boolean isReadyToAct;
    /* JADX INFO: renamed from: D */
    public BaseUnit lastAttacker;
    /* JADX INFO: renamed from: d */
    boolean unusedFlag1;
    /* JADX INFO: renamed from: k */
    BaseZone zone;
    /* JADX INFO: renamed from: i */
    int unusedInt1;
    /* JADX INFO: renamed from: j */
    int unusedInt2;
    /* JADX INFO: renamed from: l */
    float attackDelay;
    /* JADX INFO: renamed from: m */
    float unusedFloat1;
    /* JADX INFO: renamed from: n */
    float nextMoveTimer;
    /* JADX INFO: renamed from: q */
    boolean isDefending;
    /* JADX INFO: renamed from: r */
    boolean isInCombat;
    /* JADX INFO: renamed from: o */
    float attackingDuration;
    /* JADX INFO: renamed from: t */
    float healTimer;
    /* JADX INFO: renamed from: u */
    float defendDuration;
    /* JADX INFO: renamed from: v */
    boolean isEngaging;
    /* JADX INFO: renamed from: w */
    BaseUnit attackTarget;
    /* JADX INFO: renamed from: x */
    float totalUpdateTime;
    /* JADX INFO: renamed from: p */
    float enemyScanTimer;
    /* JADX INFO: renamed from: s */
    boolean unusedFlag2;
    /* JADX INFO: renamed from: A */
    int maxUnits;
    /* JADX INFO: renamed from: y */
    float recruitTimer;
    /* JADX INFO: renamed from: z */
    float commandTimer;
    /* JADX INFO: renamed from: B */
    boolean isSeaGroup;
    /* JADX INFO: renamed from: E */
    UnitMovementType commonMovementType;

    public UnitGroup(AIController aIController) {
        super(aIController);
        this.isReadyToAct = true;
        this.attackDelay = 1000.0f;
        this.unusedFloat1 = 100.0f;
        this.nextMoveTimer = 4000.0f;
        this.attackingDuration = 0.0f;
        this.enemyScanTimer = 1000.0f;
        this.isDefending = false;
        this.isInCombat = false;
        this.unusedFlag2 = false;
        this.healTimer = 0.0f;
        this.defendDuration = 0.0f;
        this.lastDamageTimeMillis = -9999;
        this.lastAttacker = null;
        this.commonMovementType = UnitMovementType.NONE;
    }

    public UnitGroup(AIController aIController, boolean z) {
        this(aIController);
        this.isReadyToAct = z;
    }

    /* JADX INFO: renamed from: a */
    public static UnitGroup createHuntGroup(AIController aIController, OrderableUnit orderableUnit) {
        UnitGroup unitGroup = new UnitGroup(aIController, false);
        unitGroup.isActive = true;
        unitGroup.requiresTarget = true;
        unitGroup.unusedFlag1 = true;
        unitGroup.retreatWhenDamaged = true;
        unitGroup.targetUnit = orderableUnit;
        unitGroup.addUnit(orderableUnit);
        unitGroup.maxUnits = 0;
        unitGroup.selectNewPosition();
        return unitGroup;
    }

    @Override // com.corrodinggames.rts.game.ai.AIUnitGroupBase
    /* JADX INFO: renamed from: a */
    public boolean isActive() {
        return this.isActive;
    }

    @Override // com.corrodinggames.rts.game.ai.AIUnitGroupBase
    /* JADX INFO: renamed from: b */
    public boolean isDefensive() {
        if (!this.isReadyToAct) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.ai.AIStrategyNode, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeBoolean(this.isReadyToAct);
        gameOutputStream.writeInt(this.unusedInt1);
        gameOutputStream.writeInt(this.unusedInt2);
        gameOutputStream.writeInt(this.units.size());
        Iterator it = this.units.iterator();
        while (it.hasNext()) {
            gameOutputStream.writeOrderableUnit((OrderableUnit) it.next());
        }
        gameOutputStream.writeByte(7);
        gameOutputStream.writeBoolean(false);
        gameOutputStream.writeBoolean(this.unusedFlag2);
        gameOutputStream.writeFloat(this.attackingDuration);
        gameOutputStream.writeInt(this.unitsNeedingTransport.size());
        Iterator it2 = this.unitsNeedingTransport.iterator();
        while (it2.hasNext()) {
            gameOutputStream.writeOrderableUnit((OrderableUnit) it2.next());
        }
        gameOutputStream.writeBoolean(this.isSeaGroup);
        gameOutputStream.writeBoolean(this.isActive);
        gameOutputStream.writeBoolean(this.requiresTarget);
        gameOutputStream.writeBoolean(this.unusedFlag1);
        gameOutputStream.writeBoolean(this.retreatWhenDamaged);
        gameOutputStream.writeBoolean(this.isRetreating);
        gameOutputStream.writeOrderableUnit(this.targetUnit);
        gameOutputStream.writeInt(this.maxUnits);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.game.ai.AIStrategyNode
    /* JADX INFO: renamed from: a */
    public void readFromInputStream(GameInputStream gameInputStream) throws IOException {
        this.isReadyToAct = gameInputStream.readBoolean();
        this.unusedInt1 = gameInputStream.readInt();
        this.unusedInt2 = gameInputStream.readInt();
        clearUnits();
        int i = gameInputStream.readInt();
        for (int i2 = 0; i2 < i; i2++) {
            OrderableUnit unitEntity = gameInputStream.readOrderableUnit();
            if (unitEntity != null) {
                addUnit(unitEntity);
            }
        }
        byte b = gameInputStream.readByte();
        if (b >= 1) {
            gameInputStream.readBoolean();
        }
        if (b >= 2) {
            this.unusedFlag2 = gameInputStream.readBoolean();
        }
        if (b >= 3) {
            this.attackingDuration = gameInputStream.readFloat();
        }
        if (b >= 4) {
            this.unitsNeedingTransport.clear();
            int i3 = gameInputStream.readInt();
            for (int i4 = 0; i4 < i3; i4++) {
                OrderableUnit unitEntity2 = gameInputStream.readOrderableUnit();
                if (unitEntity2 != null) {
                    this.unitsNeedingTransport.add(unitEntity2);
                }
            }
        }
        if (b >= 5) {
            this.isSeaGroup = gameInputStream.readBoolean();
        }
        if (b >= 6) {
            this.isActive = gameInputStream.readBoolean();
            this.requiresTarget = gameInputStream.readBoolean();
            this.unusedFlag1 = gameInputStream.readBoolean();
            this.retreatWhenDamaged = gameInputStream.readBoolean();
            this.isRetreating = gameInputStream.readBoolean();
            this.targetUnit = gameInputStream.readOrderableUnit();
        }
        if (b >= 7) {
            this.maxUnits = gameInputStream.readInt();
        }
        if (!this.isSeaGroup) {
            Iterator it = this.units.iterator();
            while (it.hasNext()) {
                OrderableUnit orderableUnit = (OrderableUnit) it.next();
                if (orderableUnit instanceof WaterUnit) {
                    if (orderableUnit != null && orderableUnit.aB == this) {
                        orderableUnit.aB = null;
                    }
                    if (orderableUnit != null) {
                        this.unitsNeedingTransport.remove(orderableUnit);
                    }
                    it.remove();
                }
            }
        }
        super.readFromInputStream(gameInputStream);
    }

    @Override // com.corrodinggames.rts.game.ai.AIUnitGroupBase
    /* JADX INFO: renamed from: a */
    protected void addUnit(OrderableUnit orderableUnit) {
        super.addUnit(orderableUnit);
        this.commonMovementType = calculateCommonMovementType();
    }

    /* JADX INFO: renamed from: c */
    public void recruitNearbyUnits() {
        for (BaseUnit baseUnit : BaseUnit.bE) {
            if (!baseUnit.isDead && baseUnit.team == this.aiController && this.maxUnits > this.units.size() && (baseUnit instanceof OrderableUnit)) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (!orderableUnit.isActive && !orderableUnit.isAIUnit && orderableUnit.aB == null && this.aiController.isCombatCustomUnit(orderableUnit) && this.aiController.isEligibleUnitForRandomSelection(orderableUnit)) {
                    if (this.isSeaGroup) {
                        if (baseUnit.getMovementType() != UnitMovementType.LAND) {
                            if (!this.aiController.isPathPossibleForUnit(orderableUnit, this.posX, this.posY) || (!isDefensive() && Utility.getRandomIntInRange(0, 100) <= 2)) {
                                addUnit(orderableUnit);
                            }
                        }
                    } else if (baseUnit.getMovementType() != UnitMovementType.WATER) {
                        if (!this.aiController.isPathPossibleForUnit(orderableUnit, this.posX, this.posY)) {
                        }
                        addUnit(orderableUnit);
                    }
                }
            }
        }
    }

    /* JADX INFO: renamed from: d */
    public boolean isFull() {
        if (this.maxUnits <= this.units.size()) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public BaseUnit getAttackerWithin(float f) {
        if (GameEngine.getInstance().gameTimeMillis - (f * 1000.0f) < this.lastDamageTimeMillis) {
            return this.lastAttacker;
        }
        return null;
    }

    /* JADX INFO: renamed from: e */
    public BaseUnit getRecentAttacker() {
        BaseUnit baseUnitA = getAttackerWithin(6.0f);
        if (baseUnitA != null) {
            return baseUnitA;
        }
        return null;
    }

    /* JADX INFO: renamed from: f */
    public BaseUnit getGroupCommandTarget() {
        Iterator it = this.units.iterator();
        while (it.hasNext()) {
            BaseUnit commandOrAttackTarget = ((OrderableUnit) it.next()).getCommandOrAttackTarget();
            if (commandOrAttackTarget != null) {
                return commandOrAttackTarget;
            }
        }
        return null;
    }

    /* JADX INFO: renamed from: a */
    public void issueCommandToUnits(Command command, boolean z, BaseUnit baseUnit) {
        for (OrderableUnit orderableUnit : this.units) {
            if (!z || orderableUnit.hasNoCurrentWaypoint()) {
                if (baseUnit == null || this.aiController.canUnitReachUnit(orderableUnit, baseUnit)) {
                    command.addUnitToCommand(orderableUnit);
                }
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public void setGroupName(String str) {
        this.groupName = str;
    }

    /* JADX INFO: renamed from: a */
    public PointF getRallyPointAwayFrom(BaseUnit baseUnit) {
        PointF pointF = new PointF();
        pointF.x = this.posX;
        pointF.y = this.posY;
        float fRandom = (float) (Math.random() * 360.0d);
        float fRandomFloatInRange = Utility.randomFloatInRange(50.0f, 100.0f);
        pointF.x += Utility.fastCos(fRandom) * fRandomFloatInRange;
        pointF.y += Utility.fastSin(fRandom) * fRandomFloatInRange;
        if (baseUnit != null) {
            float angleBetweenPoints = Utility.getAngleBetweenPoints(pointF.x, pointF.y, baseUnit.posX, baseUnit.posY);
            float fRandomFloatInRange2 = Utility.randomFloatInRange(100.0f, 200.0f);
            pointF.x += Utility.fastCos(angleBetweenPoints) * (-fRandomFloatInRange2);
            pointF.y += Utility.fastSin(angleBetweenPoints) * (-fRandomFloatInRange2);
        }
        return pointF;
    }

    @Override // com.corrodinggames.rts.game.ai.AIUnitGroupBase
    /* JADX INFO: renamed from: b */
    public void updateCore(float f) {
        BaseUnit baseUnitE;
        super.updateCore(f);
        removeDeadUnits();
        this.commonMovementType = calculateCommonMovementType();
        if (!this.isRetreating && (baseUnitE = getRecentAttacker()) != null && getGroupCommandTarget() == null) {
            if (canAnyUnitAttackTarget(baseUnitE, false)) {
                setGroupName("fighting attacker");
                Command commandNewCommandForTeam = GameEngine.getInstance().commandController.newCommandForTeam(this.aiController);
                issueCommandToUnits(commandNewCommandForTeam, true, baseUnitE);
                commandNewCommandForTeam.setAttackMoveTarget(baseUnitE.posX, baseUnitE.posY, false);
                return;
            }
            setGroupName("flight from attacker");
            PointF pointFA = getRallyPointAwayFrom(baseUnitE);
            this.posX = pointFA.x;
            this.posY = pointFA.y;
            if (this.commandTimer > 200.0f) {
                this.commandTimer = 200.0f;
            }
        }
    }

    @Override // com.corrodinggames.rts.game.ai.AIUnitGroupBase
    /* JADX INFO: renamed from: c */
    public void updateAI(float f) {
        GameEngine gameEngine = GameEngine.getInstance();
        this.totalUpdateTime += f;
        for (OrderableUnit orderableUnit : this.units) {
            if (orderableUnit != null && this.lastDamageTimeMillis < orderableUnit.bs) {
                this.lastDamageTimeMillis = orderableUnit.bs;
                this.lastAttacker = orderableUnit.unitTarget1;
            }
        }
        removeDeadUnits();
        if (isFull()) {
            this.attackDelay = Utility.moveTowardsZero(this.attackDelay, f);
        } else if (this.isEngaging) {
        }
        this.recruitTimer = Utility.moveTowardsZero(this.recruitTimer, f);
        this.commandTimer = Utility.moveTowardsZero(this.commandTimer, f);
        this.enemyScanTimer = Utility.moveTowardsZero(this.enemyScanTimer, f);
        if (!this.isEngaging && !this.isInCombat && !isFull() && this.recruitTimer == 0.0f) {
            this.recruitTimer = 200 + Utility.getRandomInt(200);
            recruitNearbyUnits();
        }
        if (!this.isEngaging || this.isDefending) {
            if (!this.isDefending) {
                this.nextMoveTimer = Utility.moveTowardsZero(this.nextMoveTimer, f);
                if (this.nextMoveTimer == 0.0f) {
                    if (this.zone == null) {
                        this.zone = findNearestZone();
                    }
                    if (this.zone != null) {
                        PointF pointFW = this.zone.getRandomPointInside();
                        if (!isTileFreeForGroup(pointFW.x, pointFW.y)) {
                            this.nextMoveTimer = 100.0f;
                            setGroupName("random move: bad target");
                        } else {
                            this.nextMoveTimer = 4000.0f;
                            this.posX = pointFW.x;
                            this.posY = pointFW.y;
                            setGroupName("random move");
                        }
                    } else {
                        setGroupName("random move: no linked base");
                    }
                }
            }
            if (this.commandTimer == 0.0f) {
                this.commandTimer = 800.0f;
                Command commandNewCommandForTeam = gameEngine.commandController.newCommandForTeam(this.aiController);
                for (OrderableUnit orderableUnit2 : this.units) {
                    boolean z = true;
                    if (getDistanceSqToUnit(orderableUnit2) < 28900.0f) {
                        z = false;
                    }
                    if (!this.isRetreating && orderableUnit2.canUnitAttack() && !orderableUnit2.hasNoCurrentWaypoint()) {
                        z = false;
                    }
                    if (z) {
                        commandNewCommandForTeam.addUnitToCommand(orderableUnit2);
                    }
                }
                if (this.isRetreating) {
                    commandNewCommandForTeam.setMoveTarget(this.posX, this.posY);
                } else {
                    commandNewCommandForTeam.setAttackMoveTarget(this.posX, this.posY);
                }
            }
        }
        if (this.isReadyToAct) {
            updateAttackAI(f);
        } else {
            updateDefensiveAI(f);
        }
        if (this.maxUnits == 0 && this.units.size() == 0) {
            destroy();
        }
        if (this.requiresTarget) {
            if (this.targetUnit == null || this.targetUnit.isDead) {
                destroy();
            }
        }
    }

    /* JADX INFO: renamed from: g */
    BaseZone findNearestZone() {
        float f = -1.0f;
        BaseZone baseZone = null;
        for (AIStrategyNode aIStrategyNode : this.aiController.activeStrategies) {
            if (aIStrategyNode instanceof BaseZone) {
                BaseZone baseZone2 = (BaseZone) aIStrategyNode;
                if (canAllUnitsReach(baseZone2.posX, baseZone2.posY)) {
                    float fD = baseZone2.getDistanceSqToPoint(this.posX, this.posY);
                    if (baseZone == null || fD < f) {
                        f = fD;
                        baseZone = baseZone2;
                    }
                }
            }
        }
        return baseZone;
    }

    /* JADX INFO: renamed from: d */
    public void updateDefensiveAI(float f) {
        if (this.zone == null || this.zone.isDestroyed) {
            selectNewPosition();
        }
        if (this.requiresTarget && this.targetUnit != null) {
            if (this.retreatWhenDamaged && !this.isRetreating) {
                if (this.targetUnit.currentHealth / this.targetUnit.maxHealth < 0.5d) {
                    this.isRetreating = true;
                    if (this.commandTimer > 100.0f) {
                        this.commandTimer = 100.0f;
                    }
                }
                if (this.attackTarget == null) {
                    selectNewPosition();
                }
            } else {
                if (this.targetUnit.currentHealth / this.targetUnit.maxHealth > 0.6d) {
                    this.isRetreating = false;
                }
                boolean z = false;
                if (this.zone != null && !this.zone.isContested) {
                    z = true;
                }
                if (!z) {
                    BaseZone baseZoneCheckUnitVariableCondition = this.aiController.checkUnitVariableCondition(this.targetUnit.getMovementType(), this.targetUnit.posX, this.targetUnit.posY, true);
                    if (baseZoneCheckUnitVariableCondition != null) {
                        this.zone = baseZoneCheckUnitVariableCondition;
                    }
                    if (this.zone != null) {
                        PointF pointFW = this.zone.getRandomPointInside();
                        this.posX = pointFW.x;
                        this.posY = pointFW.y;
                        if (this.commandTimer > 100.0f) {
                            this.commandTimer = 100.0f;
                        }
                        setGroupName("moving to new base");
                    }
                }
            }
        }
        if (this.zone != null) {
            for (int i = 0; i < 2; i++) {
                if (this.enemyScanTimer == 0.0f) {
                    BaseUnit closestEnemyUnit = this.zone.getClosestEnemyUnit();
                    if (closestEnemyUnit == null) {
                        break;
                    }
                    if (canAnyUnitAttackTarget(closestEnemyUnit, false)) {
                        this.attackTarget = closestEnemyUnit;
                        this.enemyScanTimer = 500.0f;
                        this.nextMoveTimer = 2000.0f;
                        if (!this.isRetreating) {
                            this.posX = closestEnemyUnit.posX;
                            this.posY = closestEnemyUnit.posY;
                        }
                        if (this.commandTimer > 100.0f) {
                            this.commandTimer = 100.0f;
                        }
                        setGroupName("defending base");
                    }
                }
            }
            if (this.enemyScanTimer == 0.0f) {
                this.isRetreating = false;
                this.attackTarget = null;
            }
        }
    }

    /* JADX INFO: renamed from: e */
    public void updateAttackAI(float f) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.isEngaging) {
            if (this.attackTarget == null || !this.attackTarget.isAlive() || this.attackTarget.isDead || !this.isInCombat) {
                this.attackTarget = this.aiController.getRandomEnemyUnit();
                if (this.attackTarget != null && !canAnyUnitAttackTarget(this.attackTarget, true)) {
                    this.attackTarget = null;
                }
            }
            if (this.attackTarget != null) {
                if (this.isDefending) {
                    this.defendDuration += f;
                    if (!this.isInCombat) {
                        this.healTimer = Utility.moveTowardsZero(this.healTimer, f);
                        if (this.healTimer == 0.0f) {
                            this.healTimer = 20.0f;
                            repositionNearAttackTarget();
                        }
                    } else {
                        boolean z = false;
                        Iterator it = this.units.iterator();
                        while (it.hasNext()) {
                            if (getDistanceSqToUnit((OrderableUnit) it.next()) > 28900.0f) {
                                z = true;
                            }
                        }
                        if (!z) {
                            this.isDefending = false;
                        }
                        Iterator it2 = this.units.iterator();
                        while (it2.hasNext()) {
                            if (((OrderableUnit) it2.next()).bs > gameEngine.gameTimeMillis - 1000) {
                                this.isDefending = false;
                                setGroupName("Not staging due to damage");
                            }
                        }
                    }
                    if (this.defendDuration > 17000.0f) {
                        this.isDefending = false;
                        setGroupName("attacking target");
                    }
                } else {
                    this.attackingDuration += f;
                    if (this.commandTimer == 0.0f) {
                        this.commandTimer = 800.0f;
                        boolean z2 = false;
                        FastArrayList fastArrayList = new FastArrayList();
                        for (OrderableUnit orderableUnit : this.units) {
                            boolean z3 = true;
                            if (this.attackTarget != null) {
                                if (!this.aiController.canUnitReachUnit(orderableUnit, this.attackTarget)) {
                                    z3 = false;
                                }
                                if (z3 && !PathfindingUtils.a(orderableUnit, this.attackTarget)) {
                                    z3 = false;
                                }
                            }
                            if (z3) {
                                z2 = true;
                                fastArrayList.add(orderableUnit);
                            }
                        }
                        if (!z2) {
                            this.isDefending = false;
                            setGroupName("cannot reach main target");
                        } else {
                            Command commandNewCommandForTeam = gameEngine.commandController.newCommandForTeam(this.aiController);
                            commandNewCommandForTeam.addUnitsToCommand(fastArrayList);
                            if (this.attackTarget != null && Utility.getRandomIntInRange(0, 100) < 80) {
                                commandNewCommandForTeam.setAttackMoveTarget(this.attackTarget.posX, this.attackTarget.posY, true);
                            } else {
                                commandNewCommandForTeam.setAttackTarget(this.attackTarget, true);
                            }
                            setGroupName("attacking main target");
                        }
                    }
                }
            }
        } else if (this.attackDelay == 0.0f) {
            this.isEngaging = true;
            this.isDefending = true;
        }
        if (this.isEngaging) {
            if (this.units.size() == 0) {
                destroy();
            }
            if (this.attackingDuration > 1000.0f && this.units.size() < 3) {
                destroy();
            }
            if (this.attackingDuration > 11000.0f) {
                destroy();
            }
        }
    }

    /* JADX INFO: renamed from: h */
    public void repositionNearAttackTarget() {
        float f = this.attackTarget.posX;
        float f2 = this.attackTarget.posY;
        float angleBetweenPoints = Utility.getAngleBetweenPoints(f, f2, this.posX, this.posY);
        float fDistance = Utility.distance(f, f2, this.posX, this.posY);
        if (Utility.getRandomIntInRange(0, 100) < 80) {
            angleBetweenPoints += Utility.getRandomIntInRange(-110, 110);
        }
        int i = (int) (((double) fDistance) * 0.6d);
        if (i < 720) {
            i = 720;
        }
        float randomIntInRange = Utility.getRandomIntInRange(50, i);
        if (Utility.getRandomIntInRange(0, 100) < 80 && randomIntInRange < 450.0f) {
            randomIntInRange = Utility.getRandomIntInRange(450, i);
        }
        float fFastCos = f + (Utility.fastCos(angleBetweenPoints) * randomIntInRange);
        float fFastSin = f2 + (Utility.fastSin(angleBetweenPoints) * randomIntInRange);
        boolean z = true;
        if (!isTileFreeForGroup(fFastCos, fFastSin)) {
            z = false;
        }
        boolean z2 = false;
        boolean z3 = false;
        for (OrderableUnit orderableUnit : this.units) {
            if (orderableUnit.getMovementType() == UnitMovementType.LAND) {
                z2 = true;
            }
            if (orderableUnit.getMovementType() == UnitMovementType.WATER) {
                z3 = true;
            }
        }
        if (z2) {
            if (this.aiController.activeTransporterGroupCount == 0 && !canAllUnitsReach(fFastCos, fFastSin)) {
                z = false;
            }
            if (!this.aiController.isPathPossibleBetweenPoints(fFastCos, fFastSin, this.attackTarget.posX, this.attackTarget.posY, UnitMovementType.LAND) && Utility.getRandomIntInRange(0, 100) < 98) {
                z = false;
            }
        }
        if (z3) {
            if (!canAllUnitsReach(fFastCos, fFastSin)) {
                z = false;
            }
            if (!this.aiController.isPathPossibleBetweenPoints(fFastCos, fFastSin, this.attackTarget.posX, this.attackTarget.posY, UnitMovementType.WATER)) {
                z = false;
            }
        }
        if (z) {
            this.posX = fFastCos;
            this.posY = fFastSin;
            this.commandTimer = 0.0f;
            this.isInCombat = true;
            this.unitsNeedingTransport.clear();
            for (OrderableUnit orderableUnit2 : this.units) {
                if (orderableUnit2.getMovementType() != UnitMovementType.WATER && !this.aiController.isPathPossibleForUnit(orderableUnit2, this.posX, this.posY)) {
                    this.unitsNeedingTransport.add(orderableUnit2);
                }
            }
        }
    }

    /* JADX INFO: renamed from: i */
    public UnitMovementType getCommonMovementType() {
        return this.commonMovementType;
    }

    /* JADX INFO: renamed from: j */
    public UnitMovementType calculateCommonMovementType() {
        if (this.units.size() == 0) {
            if (this.isSeaGroup) {
                return UnitMovementType.WATER;
            }
            return UnitMovementType.LAND;
        }
        boolean z = true;
        Iterator it = this.units.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            if (((OrderableUnit) it.next()).getMovementType() != UnitMovementType.AIR) {
                z = false;
                break;
            }
        }
        if (z) {
            return UnitMovementType.AIR;
        }
        if (this.isSeaGroup) {
            boolean z2 = true;
            Iterator it2 = this.units.iterator();
            while (it2.hasNext()) {
                if (((OrderableUnit) it2.next()).getMovementType() == UnitMovementType.WATER) {
                    z2 = false;
                }
            }
            if (z2) {
                return UnitMovementType.HOVER;
            }
            return UnitMovementType.WATER;
        }
        boolean z3 = true;
        Iterator it3 = this.units.iterator();
        while (it3.hasNext()) {
            UnitMovementType unitMovementTypeH = ((OrderableUnit) it3.next()).getMovementType();
            if (unitMovementTypeH == UnitMovementType.LAND || unitMovementTypeH == UnitMovementType.OVER_CLIFF) {
                z3 = false;
            }
        }
        if (z3) {
            return UnitMovementType.HOVER;
        }
        return UnitMovementType.LAND;
    }

    /* JADX INFO: renamed from: a */
    public boolean isTileFreeForGroup(float f, float f2) {
        return !GameViewUtils.a(f, f2, getCommonMovementType());
    }

    /* JADX INFO: renamed from: b */
    public boolean canAllUnitsReach(float f, float f2) {
        Iterator it = this.units.iterator();
        while (it.hasNext()) {
            if (!this.aiController.isPathPossibleForUnit((OrderableUnit) it.next(), f, f2)) {
                return false;
            }
        }
        return true;
    }

    /* JADX INFO: renamed from: a */
    public boolean canAnyUnitAttackTarget(BaseUnit baseUnit, boolean z) {
        for (OrderableUnit orderableUnit : this.units) {
            if (z || this.aiController.isPathPossibleForUnit(orderableUnit, baseUnit.posX, baseUnit.posY)) {
                if (PathfindingUtils.a(orderableUnit, baseUnit)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: k */
    public void selectNewPosition() {
        PointF randomTilePosition = null;
        if (this.requiresTarget && this.targetUnit != null) {
            this.posX = this.targetUnit.posX;
            this.posY = this.targetUnit.posY;
            this.zone = this.aiController.findNearestZone(this.targetUnit.posX, this.targetUnit.posY);
            return;
        }
        if (1 != 0) {
            int i = 0;
            while (i < 7) {
                boolean z = i > 3;
                if (randomTilePosition == null) {
                    for (AIStrategyNode aIStrategyNode : this.aiController.activeStrategies) {
                        if (aIStrategyNode instanceof BaseZone) {
                            BaseZone baseZone = (BaseZone) aIStrategyNode;
                            if (baseZone.stage == BaseZoneStage.Active && (baseZone.getNumberOfExtractors() > 2 || z)) {
                                if (randomTilePosition == null || Utility.getRandomInt(this.aiController.advancedBaseCount + 2) == 0) {
                                    for (int i2 = 0; i2 < 10; i2++) {
                                        if (randomTilePosition == null) {
                                            PointF pointFW = baseZone.getRandomPointInside();
                                            if (isTileFreeForGroup(pointFW.x, pointFW.y)) {
                                                randomTilePosition = pointFW;
                                            }
                                        }
                                    }
                                    this.zone = baseZone;
                                }
                            }
                        }
                    }
                }
                i++;
            }
        }
        if (randomTilePosition == null) {
            randomTilePosition = this.aiController.getRandomTilePosition();
            this.zone = null;
        }
        this.posX = randomTilePosition.x;
        this.posY = randomTilePosition.y;
    }
}
