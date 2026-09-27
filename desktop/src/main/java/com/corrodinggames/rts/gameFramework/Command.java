package com.corrodinggames.rts.gameFramework;

import android.graphics.PointF;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.units.*;
import com.corrodinggames.rts.game.units.actions.AbstractUnitAction;
import com.corrodinggames.rts.game.units.actions.ActionId;
import com.corrodinggames.rts.game.units.actions.PingMapAction;
import com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.network.NetworkEngine;
import com.corrodinggames.rts.gameFramework.ui.LagHidingManager;
import com.corrodinggames.rts.gameFramework.utility.FastArrayList;

import java.io.IOException;
import java.util.AbstractList;
import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.e */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/e.class */
public class Command {

    /* JADX INFO: renamed from: a */
    public boolean isReplayCommand;

    /* JADX INFO: renamed from: b */
    public String debugStackTrace;

    /* JADX INFO: renamed from: c */
    public int scheduledTick;

    /* JADX INFO: renamed from: d */
    public int createdTick;

    /* JADX INFO: renamed from: i */
    public PlayerTeam team;

    /* JADX INFO: renamed from: j */
    public UnitCommand unitCommand;

    /* JADX INFO: renamed from: l */
    public PointF targetPoint;

    /* JADX INFO: renamed from: m */
    public BaseUnit targetUnit;

    /* JADX INFO: renamed from: n */
    public AttackMode attackMode;

    /* JADX INFO: renamed from: z */
    private PointF rallyPoint;

    /* JADX INFO: renamed from: p */
    public PlayerTeam sourceTeam;

    /* JADX INFO: renamed from: q */
    public short allowedTeamMask;

    /* JADX INFO: renamed from: r */
    public boolean isSystemAction;

    /* JADX INFO: renamed from: s */
    public float gameSpeedChange;

    /* JADX INFO: renamed from: t */
    public float systemFloat;

    /* JADX INFO: renamed from: u */
    public int systemActionType;

    /* JADX INFO: renamed from: y */
    final /* synthetic */ CommandController commandController;

    /* JADX INFO: renamed from: e */
    public boolean isQueued = false;

    /* JADX INFO: renamed from: f */
    public boolean isInstantCommand = false;

    /* JADX INFO: renamed from: g */
    public boolean stopCurrentAction = false;

    /* JADX INFO: renamed from: h */
    public boolean isHighPriority = false;

    /* JADX INFO: renamed from: k */
    public ActionId actionId = AbstractUnitAction.NONE_ACTION_ID;

    /* JADX INFO: renamed from: o */
    public boolean clearExistingOrders = false;

    /* JADX INFO: renamed from: A */
    private FastArrayList pendingUnitIds = new FastArrayList();

    /* JADX INFO: renamed from: v */
    FastArrayList<OrderableUnit> selectedUnits = new FastArrayList();

    /* JADX INFO: renamed from: w */
    FastArrayList commandTargets = new FastArrayList();

    /* JADX INFO: renamed from: x */
    public boolean hasProcessedTargets = false;

    public Command(CommandController commandController) {
        this.commandController = commandController;
    }

    /* JADX INFO: renamed from: a */
    public boolean hasValidCommandTargetPaths() {
        Iterator it = this.commandTargets.iterator();
        while (it.hasNext()) {
            if (((CommandTarget) it.next()).path.a() == null) {
                return false;
            }
        }
        return true;
    }

    /* JADX INFO: renamed from: b */
    public void precomputeCommandTargets() {
        GameEngine gameEngine = GameEngine.getInstance();
        this.hasProcessedTargets = true;
        FormationGroup formationGroupC = gameEngine.formationEngine.c();
        Iterator it = this.selectedUnits.iterator();
        while (it.hasNext()) {
            formationGroupC.units.add((OrderableUnit) it.next());
        }
        if (this.unitCommand != null) {
            float targetX = this.unitCommand.getTargetX();
            float targetY = this.unitCommand.getTargetY();
            if (this.unitCommand.getCommandType() != UnitCommandType.move && this.unitCommand.getCommandType() != UnitCommandType.attackMove && this.unitCommand.getCommandType() != UnitCommandType.attack) {
                return;
            }
            for (OrderableUnit orderableUnit : formationGroupC.a(targetX, targetY, this.unitCommand.isQueued)) {
                if (!orderableUnit.isCurrentTileBlocked() && orderableUnit.I() && (!this.isQueued || orderableUnit.getCurrentWaypoint() == null)) {
                    int pathingTargetRadiusTiles = 0;
                    if (this.unitCommand.getCommandType() == UnitCommandType.attack) {
                        pathingTargetRadiusTiles = orderableUnit.getPathingTargetRadiusTiles(this.unitCommand.getTargetUnit());
                    }
                    CommandTarget commandTarget = new CommandTarget();
                    commandTarget.unitId = orderableUnit.objectId;
                    commandTarget.startX = orderableUnit.posX;
                    commandTarget.startY = orderableUnit.posY;
                    commandTarget.targetX = targetX;
                    commandTarget.targetY = targetY;
                    commandTarget.createdTick = gameEngine.currentTick;
                    commandTarget.movementType = orderableUnit.getMovementType();
                    commandTarget.path = orderableUnit.createPathToTarget(targetX, targetY, pathingTargetRadiusTiles, true, false, false);
                    commandTarget.path.remainingTimeMs = 120.0f;
                    commandTarget.path.allowedDelayMs = commandTarget.path.remainingTimeMs;
                    commandTarget.path.isTimedOut = true;
                    this.commandTargets.add(commandTarget);
                }
            }
        }
    }

    /* JADX INFO: renamed from: c */
    public PlayerTeam getTeam() {
        return this.team;
    }

    /* JADX INFO: renamed from: d */
    public int getAffectedUnitCount() {
        return this.pendingUnitIds.size() + this.selectedUnits.size();
    }

    /* JADX INFO: renamed from: e */
    public boolean isSystemCommand() {
        if (!AbstractUnitAction.isActionIdSpecified(this.actionId) && getAffectedUnitCount() == 0) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: f */
    public synchronized Command cloneCommand() {
        try {
            GameOutputStream gameOutputStream = new GameOutputStream();
            serializeCommand(gameOutputStream);
            GameInputStream gameInputStream = new GameInputStream(gameOutputStream.toByteArray());
            Command command = new Command(this.commandController);
            command.scheduledTick = this.scheduledTick;
            command.deserializeCommand(gameInputStream);
            return command;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* JADX INFO: renamed from: g */
    public void prepareForNetworkTransfer() {
        if (this.unitCommand != null) {
            Iterator it = this.selectedUnits.iterator();
            while (it.hasNext()) {
                this.pendingUnitIds.add(Long.valueOf(((OrderableUnit) it.next()).objectId));
            }
            this.selectedUnits.clear();
            this.unitCommand.updateTargetUnitIdFromUnit();
        }
    }

    /* JADX INFO: renamed from: a */
    public synchronized void serializeCommand(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.startBlock("c");
        gameOutputStream.writeByte(this.team.teamId);
        gameOutputStream.writeBoolean(this.unitCommand != null);
        if (this.unitCommand != null) {
            this.unitCommand.serialize(gameOutputStream);
        }
        gameOutputStream.writeBoolean(this.isQueued);
        gameOutputStream.writeBoolean(this.stopCurrentAction);
        gameOutputStream.writeInt(-1);
        gameOutputStream.writeEnumOrdinal(this.attackMode);
        gameOutputStream.writeBoolean(this.rallyPoint != null);
        if (this.rallyPoint != null) {
            gameOutputStream.writeFloat(this.rallyPoint.x);
            gameOutputStream.writeFloat(this.rallyPoint.y);
        }
        gameOutputStream.writeBoolean(this.clearExistingOrders);
        gameOutputStream.writeInt(this.selectedUnits.size() + this.pendingUnitIds.size());
        Iterator it = this.selectedUnits.iterator();
        while (it.hasNext()) {
            gameOutputStream.writeLong(((OrderableUnit) it.next()).objectId);
        }
        Iterator it2 = this.pendingUnitIds.iterator();
        while (it2.hasNext()) {
            gameOutputStream.writeLong(((Long) it2.next()).longValue());
        }
        gameOutputStream.writeBoolean(this.sourceTeam != null);
        if (this.sourceTeam != null) {
            gameOutputStream.writeTeamIdByte(this.sourceTeam);
        }
        gameOutputStream.writeBoolean(this.targetPoint != null);
        if (this.targetPoint != null) {
            gameOutputStream.writeFloat(this.targetPoint.x);
            gameOutputStream.writeFloat(this.targetPoint.y);
        }
        gameOutputStream.writeUnitIdOrNullBaseUnit(this.targetUnit);
        gameOutputStream.writeStringUTF(this.actionId.getId());
        gameOutputStream.writeBoolean(this.isInstantCommand);
        gameOutputStream.writeShort(this.allowedTeamMask);
        gameOutputStream.writeBoolean(this.isSystemAction);
        if (this.isSystemAction) {
            gameOutputStream.writeByte(0);
            gameOutputStream.writeFloat(this.gameSpeedChange);
            gameOutputStream.writeFloat(this.systemFloat);
            gameOutputStream.writeInt(this.systemActionType);
        }
        gameOutputStream.writeInt(this.commandTargets.size());
        for (int i = 0; i < this.commandTargets.size(); i++) {
            ((CommandTarget) this.commandTargets.get(i)).serialize(gameOutputStream);
        }
        gameOutputStream.writeBoolean(this.isHighPriority);
        gameOutputStream.endBlock("c");
    }

    /* JADX INFO: renamed from: a */
    public void deserializeCommand(GameInputStream gameInputStream) throws IOException {
        gameInputStream.startBlockNamed("c");
        this.team = PlayerTeam.k(gameInputStream.readByte());
        if (this.team == null) {
            throw new IOException("team==null");
        }
        if (gameInputStream.readBoolean()) {
            this.unitCommand = new UnitCommand();
            this.unitCommand.deserialize(gameInputStream);
        }
        this.isQueued = gameInputStream.readBoolean();
        this.stopCurrentAction = gameInputStream.readBoolean();
        this.actionId = ActionId.intern(String.valueOf(gameInputStream.readInt()));
        this.attackMode = (AttackMode) gameInputStream.readEnumOrdinalOrNull(AttackMode.class);
        if (gameInputStream.readBoolean()) {
            this.rallyPoint = new PointF();
            this.rallyPoint.x = gameInputStream.readFloat();
            this.rallyPoint.y = gameInputStream.readFloat();
        }
        this.clearExistingOrders = gameInputStream.readBoolean();
        int i = gameInputStream.readInt();
        for (int i2 = 0; i2 < i; i2++) {
            this.pendingUnitIds.add(gameInputStream.readUnitId());
        }
        if (gameInputStream.getProtocolVersion() >= 16) {
            this.sourceTeam = null;
            if (gameInputStream.readBoolean()) {
                this.sourceTeam = gameInputStream.readOptionalPlayerTeam();
            }
        }
        if (gameInputStream.getProtocolVersion() >= 29) {
            if (gameInputStream.readBoolean()) {
                this.targetPoint = new PointF();
                this.targetPoint.x = gameInputStream.readFloat();
                this.targetPoint.y = gameInputStream.readFloat();
            }
            this.targetUnit = gameInputStream.readBaseUnit();
        }
        if (gameInputStream.getProtocolVersion() >= 33) {
            this.actionId = ActionId.intern(gameInputStream.readUTF());
        }
        if (gameInputStream.getProtocolVersion() >= 37) {
            this.isInstantCommand = gameInputStream.readBoolean();
        }
        if (gameInputStream.getProtocolVersion() >= 52) {
            this.allowedTeamMask = gameInputStream.readShortValue();
        }
        if (gameInputStream.getProtocolVersion() >= 53) {
            this.isSystemAction = gameInputStream.readBoolean();
            if (this.isSystemAction) {
                gameInputStream.readByte();
                this.gameSpeedChange = gameInputStream.readFloat();
                this.systemFloat = gameInputStream.readFloat();
                this.systemActionType = gameInputStream.readInt();
            }
            int i3 = gameInputStream.readInt();
            this.commandTargets.clear();
            for (int i4 = 0; i4 < i3; i4++) {
                CommandTarget commandTarget = new CommandTarget();
                commandTarget.deserialize(gameInputStream);
                this.commandTargets.add(commandTarget);
            }
        }
        if (gameInputStream.getProtocolVersion() >= 80) {
            this.isHighPriority = gameInputStream.readBoolean();
        }
        gameInputStream.d("c");
    }

    /* JADX INFO: renamed from: a */
    public void addUnitsToCommand(AbstractList abstractList) {
        Iterator it = abstractList.iterator();
        while (it.hasNext()) {
            addUnitToCommand((OrderableUnit) it.next());
        }
    }

    /* JADX INFO: renamed from: a */
    public void addUnitToCommand(OrderableUnit orderableUnit) {
        if (orderableUnit == null) {
            throw new RuntimeException("unit cannot be null");
        }
        if (orderableUnit.team != this.team) {
        }
        if (this.team.isTeamSpectator) {
            if (orderableUnit.team != this.team && GameEngine.getInstance().playerTeam != this.team) {
                GameEngine.log("CommandController", "Warning AI: " + this.team.teamId + " gave an order to unit with team:" + orderableUnit.team.teamId + " type:" + orderableUnit.r().getUnitTypeDescriptionShort());
                GameEngine.logWarningAndStack(VariableScope.nullOrMissingString);
            }
            if (orderableUnit.canNotBeGivenOrdersByPlayer()) {
                GameEngine.log("CommandController", "Warning AI: " + this.team.teamId + " gave an order to unit with canNotBeGivenOrdersByPlayer: " + orderableUnit.r().getUnitTypeDescriptionShort());
            }
        }
        this.selectedUnits.add(orderableUnit);
    }

    /* JADX INFO: renamed from: h */
    public void setClearExistingOrders() {
        this.clearExistingOrders = true;
    }

    /* JADX INFO: renamed from: a */
    public void setMoveTarget(float f, float f2) {
        this.unitCommand = new UnitCommand();
        this.unitCommand.setMoveTarget(f, f2);
    }

    /* JADX INFO: renamed from: b */
    public void setAttackMoveTarget(float f, float f2) {
        this.unitCommand = new UnitCommand();
        this.unitCommand.setAttackMoveTarget(f, f2);
    }

    /* JADX INFO: renamed from: a */
    public void setAttackTarget(BaseUnit baseUnit) {
        this.unitCommand = new UnitCommand();
        this.unitCommand.setAttackTarget(baseUnit);
    }

    /* JADX INFO: renamed from: a */
    public void setAttackMoveTarget(float f, float f2, boolean z) {
        this.unitCommand = new UnitCommand();
        this.unitCommand.setAttackMoveTarget(f, f2);
        this.unitCommand.isQueued = z;
    }

    /* JADX INFO: renamed from: a */
    public void setAttackTarget(BaseUnit baseUnit, boolean z) {
        this.unitCommand = new UnitCommand();
        this.unitCommand.setAttackTarget(baseUnit);
        this.unitCommand.isQueued = z;
    }

    /* JADX INFO: renamed from: a */
    public void setBuildTarget(float f, float f2, UnitType unitType, int i) {
        this.unitCommand = new UnitCommand();
        this.unitCommand.setBuildCommand(f, f2, unitType, i);
    }

    /* JADX INFO: renamed from: b */
    public void setRepairTarget(BaseUnit baseUnit) {
        this.unitCommand = new UnitCommand();
        this.unitCommand.setRepairCommand(baseUnit);
    }

    /* JADX INFO: renamed from: c */
    public void setGuardTarget(BaseUnit baseUnit) {
        this.unitCommand = new UnitCommand();
        this.unitCommand.setGuardCommand(baseUnit);
    }

    /* JADX INFO: renamed from: c */
    public void setPatrolTarget(float f, float f2) {
        this.unitCommand = new UnitCommand();
        this.unitCommand.setPatrolPoint(f, f2);
    }

    /* JADX INFO: renamed from: d */
    public void setReclaimTarget(BaseUnit baseUnit) {
        this.unitCommand = new UnitCommand();
        this.unitCommand.setReclaimTargetUnit(baseUnit);
    }

    /* JADX INFO: renamed from: e */
    public void setLoadIntoTarget(BaseUnit baseUnit) {
        this.unitCommand = new UnitCommand();
        this.unitCommand.setLoadIntoTargetUnit(baseUnit);
    }

    /* JADX INFO: renamed from: f */
    public void setLoadUpTarget(BaseUnit baseUnit) {
        this.unitCommand = new UnitCommand();
        this.unitCommand.setLoadUpTargetUnit(baseUnit);
    }

    /* JADX INFO: renamed from: a */
    public void setActionId(ActionId actionId) {
        this.actionId = actionId;
    }

    /* JADX INFO: renamed from: a */
    public void setActionTarget(ActionId actionId, PointF pointF, BaseUnit baseUnit) {
        this.actionId = actionId;
        this.targetPoint = pointF;
        this.targetUnit = baseUnit;
    }

    /* JADX INFO: renamed from: a */
    public void setAttackMode(AttackMode attackMode) {
        this.attackMode = attackMode;
    }

    /* JADX INFO: renamed from: a */
    public void setRallyPoint(PointF pointF) {
        this.rallyPoint = pointF;
    }

    /* JADX INFO: renamed from: i */
    public synchronized void resolvePendingUnitIds() {
        Iterator it = this.pendingUnitIds.iterator();
        while (it.hasNext()) {
            OrderableUnit orderableUnitB = GameObject.b(((Long) it.next()).longValue(), true);
            if (orderableUnitB != null) {
                this.selectedUnits.add(orderableUnitB);
            }
        }
        this.pendingUnitIds.clear();
        Iterator it2 = this.selectedUnits.iterator();
        while (it2.hasNext()) {
            if (((OrderableUnit) it2.next()).isDead) {
                it2.remove();
            }
        }
    }

    /* JADX INFO: renamed from: j */
    public void applyCommandToUnits() {
        if (AbstractUnitAction.isActionIdSpecified(this.actionId)) {
            for (OrderableUnit orderableUnit : this.selectedUnits) {
                orderableUnit.stopMoving(orderableUnit.validateActionId(this.actionId), this.stopCurrentAction);
            }
        }
    }

    /* JADX INFO: renamed from: k */
    public void executeCommand() {
        UnitCommand lastWaypoint;
        String str;
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine.replayEngine.j() && !this.isReplayCommand) {
            return;
        }
        resolvePendingUnitIds();
        if (this.isSystemAction) {
            if (this.gameSpeedChange != 0.0f) {
                GameEngine.log("issueCommand: changeStepRate:" + this.gameSpeedChange);
                gameEngine.networkEngine.applyChangedSetup(this.gameSpeedChange, "command");
                return;
            }
            if (this.systemActionType != 0) {
                GameEngine.log("system action:" + this.systemActionType);
                if (this.systemActionType == 1) {
                    GameEngine.log("new DebugDesyncDetector");
                    new UnitSpawner(false).setUnitTeam(PlayerTeam.TEAM_ALL);
                    return;
                }
                if (this.systemActionType == 2) {
                    GameEngine.log("new DebugDesyncDetector (stress test)");
                    UnitSpawner unitSpawner = new UnitSpawner(false);
                    unitSpawner.setUnitTeam(PlayerTeam.TEAM_ALL);
                    unitSpawner.stressTestMode = true;
                    return;
                }
                if (this.systemActionType == 100) {
                    GameEngine.log("team surrender");
                    if (this.team == null) {
                        GameEngine.log("team not found");
                        return;
                    }
                    if (gameEngine.networkEngine.isServer) {
                        gameEngine.networkEngine.j("'" + this.team.teamName + "' has surrendered");
                    }
                    this.team.isTeamVictory = true;
                    for (Object o : BaseUnit.bE) {
                        BaseUnit baseUnit=(BaseUnit) o;
                        if (baseUnit.team == this.team && (baseUnit instanceof OrderableUnit)) {
                            ((OrderableUnit) baseUnit).c(false);
                        }
                    }
                    return;
                }
                if (this.systemActionType == 200) {
                    GameEngine.log("queue quick resync");
                    gameEngine.networkEngine.quickResyncRequested = true;
                    return;
                }
                if (this.systemActionType == 5) {
                    GameEngine.log("system command spawn");
                    if (this.unitCommand == null || this.unitCommand.getCommandType() != UnitCommandType.build || this.unitCommand.getBuildUnitType() == null) {
                        GameEngine.log("system command spawn - failed");
                        return;
                    }
                    int buildQueueSize = this.unitCommand.getBuildQueueSize();
                    UnitType buildUnitType = this.unitCommand.getBuildUnitType();
                    boolean z = false;
                    if (this.team != null && this.team == gameEngine.playerTeam && gameEngine.playerTeam.getUnitCount(false, false) == 0) {
                        z = true;
                    }
                    BaseUnit baseUnitA = buildUnitType.a();
                    baseUnitA.posX = this.unitCommand.getTargetX();
                    baseUnitA.posY = this.unitCommand.getTargetY();
                    if (this.team != null) {
                        baseUnitA.f(this.team);
                    } else {
                        baseUnitA.f(PlayerTeam.TEAM_ALL);
                    }
                    baseUnitA.setCommandTargetUnit(null);
                    if (buildQueueSize != 1 && (baseUnitA instanceof OrderableUnit)) {
                        ((OrderableUnit) baseUnitA).a(buildQueueSize);
                    }
                    baseUnitA.onUnitSpawned();
                    if (baseUnitA instanceof OrderableUnit) {
                        OrderableUnit orderableUnit = (OrderableUnit) baseUnitA;
                        orderableUnit.br();
                        if (baseUnitA.bI()) {
                            gameEngine.pathfindingEngine.a(orderableUnit);
                        }
                    }
                    PlayerTeam.c(baseUnitA);
                    if (gameEngine.playerTeam == baseUnitA.team && baseUnitA.team != PlayerTeam.TEAM_ALL && !baseUnitA.u() && z) {
                        gameEngine.centerViewpoint(baseUnitA.posX, baseUnitA.posY);
                        gameEngine.gameUI.selectUnit(baseUnitA);
                        return;
                    }
                    return;
                }
                GameEngine.log("issueCommand: unknown system action:" + this.systemActionType);
                return;
            }
            GameEngine.log("issueCommand: Null System action");
            return;
        }
        if (this.sourceTeam != null) {
            this.sourceTeam.teamLastConnectionTime = System.currentTimeMillis();
            this.sourceTeam.teamPingCount = gameEngine.gameTimeMillis;
        }
        if (this.sourceTeam != null) {
            String str2 = null;
            OrderableUnit orderableUnit2 = null;
            Iterator it = this.selectedUnits.iterator();
            while (it.hasNext()) {
                OrderableUnit orderableUnit3 = (OrderableUnit) it.next();
                if (orderableUnit3.team != this.sourceTeam && !canIssueOrdersToTeam(this.sourceTeam, orderableUnit3.team)) {
                    if (str2 == null) {
                        str = VariableScope.nullOrMissingString;
                    } else {
                        str = str2 + ", ";
                    }
                    if (orderableUnit2 == null) {
                        orderableUnit2 = orderableUnit3;
                    }
                    str2 = str + orderableUnit3.objectId;
                    it.remove();
                } else if (orderableUnit3.canNotBeGivenOrdersByPlayer()) {
                    CommandController.logWithRateLimit("Warning unit: " + orderableUnit3.objectId + " has canNotBeGivenOrdersByPlayer set");
                    it.remove();
                }
            }
            if (str2 != null) {
                NetworkEngine.reportDesyncVerbose("Player(" + this.sourceTeam.teamId + ") " + this.sourceTeam.teamName + " cannot control units: " + str2, true);
                if (orderableUnit2 != null) {
                    String str3 = VariableScope.nullOrMissingString;
                    if (orderableUnit2.team != null) {
                        str3 = str3 + " targetUnitTeamId: " + orderableUnit2.team.teamId + " targetUnitTeamName: " + orderableUnit2.team.teamName;
                    }
                    CommandController.logWithRateLimit(str3);
                }
            }
        }
        if (this.clearExistingOrders) {
            for (OrderableUnit orderableUnit4 : this.selectedUnits) {
                orderableUnit4.clearAllWaypoints();
                orderableUnit4.attackTarget = null;
            }
        }
        if (this.unitCommand != null) {
            this.unitCommand.resolveTargetUnitFromId();
            FormationGroup formationGroupB = gameEngine.formationEngine.b();
            formationGroupB.commandTargets = this.commandTargets;
            int i = 0;
            while (i <= 1) {
                boolean z2 = i == 1;
                for (OrderableUnit orderableUnit5 : this.selectedUnits) {
                    if (orderableUnit5.isTransportAttached == z2) {
                        if (this.isInstantCommand) {
                            orderableUnit5.removeNonBuildRepairWaypoints();
                        } else if (!this.isQueued) {
                            orderableUnit5.clearAllWaypoints();
                        } else if (this.isHighPriority && this.unitCommand != null && (lastWaypoint = orderableUnit5.getLastWaypoint()) != null && this.unitCommand.isApproximatelySameTarget(lastWaypoint) && (lastWaypoint.getCommandType() == UnitCommandType.attackMove || lastWaypoint.getCommandType() == UnitCommandType.move)) {
                            if (this.unitCommand.getCommandType() == UnitCommandType.attackMove || this.unitCommand.getCommandType() == UnitCommandType.move) {
                                orderableUnit5.removeLastWaypoint();
                            }
                        }
                    }
                }
                i++;
            }
            for (OrderableUnit orderableUnit6 : this.selectedUnits) {
                if (!orderableUnit6.isValidNewWaypoint(this.unitCommand, CommandController.logRateLimitCounter < 5)) {
                    String str4 = VariableScope.nullOrMissingString;
                    if (this.sourceTeam != null) {
                        str4 = "Player(" + this.sourceTeam.teamId + ") " + this.sourceTeam.teamName + ": ";
                    }
                    CommandController.logWithRateLimit(str4 + "isValidNewWaypoint==false on: " + orderableUnit6.getUnitShortName());
                } else {
                    UnitCommand unitCommandAppendWaypointCopy = orderableUnit6.appendWaypointCopy(this.unitCommand);
                    formationGroupB.a(orderableUnit6, unitCommandAppendWaypointCopy);
                    orderableUnit6.a(unitCommandAppendWaypointCopy);
                }
            }
            formationGroupB.b();
            return;
        }
        if (AbstractUnitAction.isActionIdSpecified(this.actionId)) {
            for (OrderableUnit orderableUnit7 : this.selectedUnits) {
                AbstractUnitAction abstractUnitActionA = orderableUnit7.validateActionId(this.actionId);
                if (abstractUnitActionA == null) {
                    CommandController.logWithRateLimit("Could not find specialAction:" + this.actionId.getId() + " on " + orderableUnit7.r().getUnitTypeDescriptionShort());
                } else if (!abstractUnitActionA.b(orderableUnit7)) {
                    CommandController.logWithRateLimit("!isAvailable specialAction:" + this.actionId.getId() + " on " + orderableUnit7.r().getUnitTypeDescriptionShort() + " (action being skipped)");
                    if (CommandController.DEBUG_TRACE_ENABLED) {
                        CommandController.logWithRateLimit("Command source:" + this.debugStackTrace);
                    }
                } else {
                    orderableUnit7.a(abstractUnitActionA);
                    LagHidingManager.onUnitActionStarted(orderableUnit7, abstractUnitActionA);
                    orderableUnit7.a(abstractUnitActionA, this.stopCurrentAction, this.targetPoint, this.targetUnit);
                }
            }
            PingMapAction pingMapActionA = PingMapAction.a(this.actionId);
            if (pingMapActionA != null) {
                if (gameEngine.playerTeam != null && this.team != null) {
                    if (this.team.d(gameEngine.playerTeam)) {
                        gameEngine.gameUI.sendMapPing(this.targetPoint.x, this.targetPoint.y, this.team, pingMapActionA);
                    }
                } else {
                    CommandController.logWithRateLimit("PingMapAction failed: game.playerTeam==null or this.team==null");
                }
            }
        }
        if (this.attackMode != null) {
            Iterator it2 = this.selectedUnits.iterator();
            while (it2.hasNext()) {
                ((OrderableUnit) it2.next()).attackMode = this.attackMode;
            }
        }
        if (this.rallyPoint != null) {
            for (Object obj : this.selectedUnits) {
                if (obj instanceof FactoryQueueInterface) {
                    ((FactoryQueueInterface) obj).a(this.rallyPoint);
                }
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public boolean canIssueOrdersToTeam(PlayerTeam playerTeam, PlayerTeam playerTeam2) {
        if (playerTeam != null && playerTeam2 != null && playerTeam2.d(playerTeam) && (this.allowedTeamMask & (1 << playerTeam2.teamId)) != 0) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: l */
    public boolean prepareAndValidateCommand() {
        UnitType buildUnitType;
        BaseUnit baseUnitFindTurretPosition;
        this.allowedTeamMask = (short) 0;
        for (int i = 0; i < PlayerTeam.TEAM_NEUTRAL; i++) {
            PlayerTeam playerTeamK = PlayerTeam.k(i);
            if (playerTeamK != null && playerTeamK.isSharedControlEnabled()) {
                this.allowedTeamMask = (short) (this.allowedTeamMask | (1 << i));
            }
        }
        if (GameEngine.getInstance().getVersionCode(true) < 127 && this.unitCommand != null && this.unitCommand.getCommandType() == UnitCommandType.build && (buildUnitType = this.unitCommand.getBuildUnitType()) != null && (baseUnitFindTurretPosition = BaseUnit.findTurretPosition(buildUnitType)) != null && !(baseUnitFindTurretPosition instanceof OrderableUnit)) {
            GameEngine.log("Rejecting non OrderableUnit build order: " + buildUnitType.getUnitTypeDescriptionShort());
            return false;
        }
        if (this.unitCommand != null && this.unitCommand.isForceMove) {
            GameEngine.log("Rejecting waypoint with addedByAction true");
            return false;
        }
        return true;
    }
}
