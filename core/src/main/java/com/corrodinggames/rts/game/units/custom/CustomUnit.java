package com.corrodinggames.rts.game.units.custom;

import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.game.ScorchMark;
import com.corrodinggames.rts.game.units.*;
import com.corrodinggames.rts.game.units.actions.AbstractUnitAction;
import com.corrodinggames.rts.game.units.actions.ActionId;
import com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface;
import com.corrodinggames.rts.game.units.buildings.FactoryQueueManager;
import com.corrodinggames.rts.game.units.buildings.FactoryWithQueue;
import com.corrodinggames.rts.game.units.buildings.NukeLauncher;
import com.corrodinggames.rts.game.units.custom.condition.StoredResources;
import com.corrodinggames.rts.game.units.custom.hooks.*;
import com.corrodinggames.rts.game.units.custom.logic.ActionType;
import com.corrodinggames.rts.game.units.custom.logic.CustomAction;
import com.corrodinggames.rts.game.units.custom.logic.CustomActionDef;
import com.corrodinggames.rts.game.units.custom.logic.LogicAction;
import com.corrodinggames.rts.game.units.custom.logicBooleans.LogicBoolean;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.game.units.custom.price.UnitPrice;
import com.corrodinggames.rts.game.units.land.BuilderUnit;
import com.corrodinggames.rts.game.units.land.HovercraftUnit;
import com.corrodinggames.rts.gameFramework.FormationGroup;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.GameObject;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.audio.SoundEngine;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.effects.EffectEmitter;
import com.corrodinggames.rts.gameFramework.effects.EffectQuality;
import com.corrodinggames.rts.gameFramework.effects.EffectType;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.network.NetworkEngine;
import com.corrodinggames.rts.gameFramework.utility.FastArrayList;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;
import com.corrodinggames.rts.gameFramework.utility.Vector3D;
import io.github.rwx.geometry.PointF;
import io.github.rwx.geometry.Rect;
import io.github.rwx.geometry.RectF;
import io.github.rwx.mod.UnitEventRuntime;
import io.github.rwx.mod.api.UnitRenderLayer;
import io.github.rwx.mod.registry.DamageRegistry;
import io.github.rwx.mod.registry.RenderRegistry;
import io.github.rwx.mod.registry.TurretFireCycleObserverRegistry;
import io.github.rwx.render.canvas.ArgbColor;
import io.github.rwx.render.canvas.Paint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.custom.j */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/custom/j.class */
public class CustomUnit extends MovableUnit implements TransportUnitInterface, UnitPathPoints, FactoryQueueInterface {

    /* JADX INFO: renamed from: a */
    public int animationFrameIndex;

    /* JADX INFO: renamed from: b */
    public final CustomUnitAnimationController animationController;

    /* JADX INFO: renamed from: c */
    float currentFrameTime;

    /* JADX INFO: renamed from: d */
    float frameAnimationSpeed;

    /* JADX INFO: renamed from: e */
    float frameAnimationTimer;

    /* JADX INFO: renamed from: f */
    public float frameAnimationDelay;

    /* JADX INFO: renamed from: g */
    boolean frameAnimationLooping;

    /* JADX INFO: renamed from: h */
    public boolean frameAnimationPlaying;

    /* JADX INFO: renamed from: i */
    public boolean frameAnimationReverse;

    /* JADX INFO: renamed from: j */
    float moveSpeedMultiplier;

    /* JADX INFO: renamed from: k */
    boolean hasProcessedDeathGroundCollision;

    /* JADX INFO: renamed from: l */
    boolean isDeathFallComplete;

    /* JADX INFO: renamed from: m */
    float transportUnloadTimer;

    /* JADX INFO: renamed from: n */
    boolean isUnloading;
    float o;
    public boolean p;
    float q;
    boolean r;
    float s;
    float t;
    public float u;
    public boolean v;
    float w;

    /* JADX INFO: renamed from: x */
    public CustomUnitConfig unitConfig;
    public UnitStats y;

    /* JADX INFO: renamed from: z */
    public CustomUnitConfig factoryUnitConfig;

    /* JADX INFO: renamed from: A */
    public CustomUnitDataField[] attachmentPoints;

    /* JADX INFO: renamed from: B */
    public final FastArrayList<BaseUnit> transportedUnits;
    public FastArrayList<BaseUnit> C;
    public float D;
    PointF[] E;
    PointF[] F;
    Projectile[] G;
    Paint J;

    /* JADX INFO: renamed from: dL */
    final FactoryQueueManager factoryQueueManager;
    public static PointF dM;
    public static BaseUnit dN;
    public static int dO;
    public float dP;
    public float dQ;
    public float dR;
    public float dS;

    /* JADX INFO: renamed from: dT */
    public LegInstance[] legInstances;
    static boolean dY;
    static final HashMap dZ;
    static int ea;
    static String eb;
    static final PointF ec;

    /* JADX INFO: renamed from: ed */
    AnimationSet currentActionHandler;
    protected static final Vector3D ee;
    protected static final PointF ef;
    FastArrayList eg;
    static final PointF H = new PointF();
    static final PointF I = new PointF();
    protected static final PointF K = new PointF();
    protected static final Vector3D dK = new Vector3D();
    static ArrayList dU = new ArrayList();
    public static FastArrayList dV = new FastArrayList();
    public static FastArrayList dW = new FastArrayList();
    public static FastArrayList dX = new FastArrayList();

    /* JADX INFO: renamed from: K */
    public void initTransportedUnitPoints() {
        this.E = new PointF[6];
        this.F = new PointF[this.E.length];
        for (int i = 0; i < this.E.length; i++) {
            this.E[i] = new PointF();
            this.F[i] = new PointF();
        }
    }

    @Override // com.corrodinggames.rts.game.units.UnitPathPoints
    public PointF[] b() {
        if (this.E == null) {
            initTransportedUnitPoints();
        }
        return this.E;
    }

    @Override // com.corrodinggames.rts.game.units.UnitPathPoints
    public PointF[] e_() {
        if (this.E == null) {
            initTransportedUnitPoints();
        }
        return this.F;
    }

    @Override
    // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeInt(11);
        gameOutputStream.writeFloat(this.frameAnimationTimer);
        gameOutputStream.writeFloat(this.transportUnloadTimer);
        gameOutputStream.writeBoolean(this.isUnloading);
        gameOutputStream.writeInt(this.transportedUnits.size());
        for (BaseUnit transportedUnit : this.transportedUnits) {
            gameOutputStream.writeUnitIdOrNullBaseUnit(transportedUnit);
        }
        gameOutputStream.writeBoolean(this.r);
        gameOutputStream.writeFloat(this.o);
        gameOutputStream.writeFloat(this.frameAnimationDelay);
        gameOutputStream.writeFloat(this.s);
        gameOutputStream.writeBoolean(this.v);
        byte length = 0;
        if (this.legInstances != null) {
            length = (byte) this.legInstances.length;
        }
        gameOutputStream.writeByte(length);
        if (this.legInstances != null) {
            for (int i = 0; i < length; i++) {
                LegInstance legInstance = this.legInstances[i];
                gameOutputStream.writeFloat(legInstance.b);
                gameOutputStream.writeFloat(legInstance.c);
                gameOutputStream.writeFloat(legInstance.d);
                gameOutputStream.writeFloat(legInstance.i);
                gameOutputStream.writeBoolean(legInstance.k);
                gameOutputStream.writeBoolean(legInstance.j);
                gameOutputStream.writeBoolean(legInstance.m);
                gameOutputStream.writeBoolean(legInstance.n);
            }
        }
        this.factoryQueueManager.a(gameOutputStream);
        gameOutputStream.writeUnitTypeId(this.factoryUnitConfig);
        gameOutputStream.writeBoolean(this.frameAnimationReverse);
        gameOutputStream.writeBoolean(this.frameAnimationPlaying);
        boolean z = this.currentActionHandler != this.unitConfig.tags;
        gameOutputStream.writeBoolean(z);
        if (z) {
            AnimationTag.a(this.currentActionHandler, gameOutputStream);
        }
        UnitStats.a(this.y, this, gameOutputStream);
        gameOutputStream.writeFloat(this.q);
        super.a(gameOutputStream);
    }

    public static void a(float f, int i) {
        int i2;
        if (dW.size == 0) {
            return;
        }
        int i3 = 0;
        while (true) {
            if (i3 >= 105 || (i2 = dW.size) == 0) {
                break;
            }
            Object[] objArrA = dW.a();
            for (int i4 = i2 - 1; i4 >= 0; i4--) {
                CustomUnitEventData customUnitEventData = (CustomUnitEventData) objArrA[i4];
                CustomEventBinding customEventBinding = customUnitEventData.eventInfo;
                CustomUnit customUnit = customUnitEventData.customUnit;
                if (customEventBinding.c == customUnit.unitConfig) {
                    ec.x = customUnit.posX;
                    ec.y = customUnit.posY;
                    PointF pointF = ec;
                    LogicBoolean.setContextEventSource(customUnitEventData);
                    customUnit.a(customEventBinding.b, pointF, (BaseUnit) null, 0, 0);
                    LogicBoolean.clearContext();
                }
            }
            if (i3 < 105) {
                if (i2 == dW.size) {
                    break;
                }
                Object[] objArrA2 = dW.a();
                int i5 = 0;
                for (int i6 = i2; i6 < dW.size; i6++) {
                    if (i3 < ((CustomUnitEventData) objArrA2[i6]).eventInfo.e) {
                        i5++;
                    }
                }
                if (i5 <= 0) {
                    break;
                }
                dV.clear();
                for (int i7 = 0; i7 < dW.size; i7++) {
                    CustomUnitEventData customUnitEventData2 = (CustomUnitEventData) objArrA2[i7];
                    boolean z = true;
                    if (i7 < i2) {
                        z = false;
                    } else if (i3 >= customUnitEventData2.eventInfo.e) {
                        z = false;
                    }
                    if (!z) {
                        customUnitEventData2.a();
                        dX.add(customUnitEventData2);
                    } else {
                        dV.add(customUnitEventData2);
                    }
                }
                dW.clear();
                FastArrayList fastArrayList = dV;
                dV = dW;
                dW = fastArrayList;
                if (i5 != dW.size) {
                    GameEngine.log("processAllQueuedEvents: " + i5 + "!=" + dW.size);
                }
                i3++;
            } else {
                GameEngine.log("processAllQueuedEvents: recursion limit reached");
                break;
            }
        }
        Object[] objArrA3 = dW.a();
        for (int i8 = dW.size - 1; i8 >= 0; i8--) {
            CustomUnitEventData customUnitEventData3 = (CustomUnitEventData) objArrA3[i8];
            customUnitEventData3.a();
            dX.add(customUnitEventData3);
        }
        dW.clear();
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cr */
    public boolean canTransportUnits() {
        return this.unitConfig.maxTransportingUnits > 0;
    }

    @Override
    // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.TransportUnitInterface
    /* JADX INFO: renamed from: bB */
    public int getTransportedUnitCount() {
        return this.transportedUnits.size();
    }

    @Override
    // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.TransportUnitInterface
    /* JADX INFO: renamed from: bA */
    public boolean isTransportUnloadingActive() {
        return this.isUnloading;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean d(BaseUnit baseUnit, boolean z) {
        if (this.unitConfig.maxTransportingUnits == 0 || this.isUnloading || this.buildProgress < 1.0f || !hasTransportSpaceFor(baseUnit) || baseUnit == this) {
            return false;
        }
        if (this.team != baseUnit.team && !z && (!this.unitConfig.whileNeutralTransportAnyTeam || this.team != PlayerTeam.TEAM_ALL)) {
            return false;
        }
        if (this.unitConfig.transportUnitsRequireTag != null && !this.unitConfig.transportUnitsRequireTag.a() && !AnimationTag.a(this.unitConfig.transportUnitsRequireTag, baseUnit.getTags())) {
            return false;
        }
        if (this.unitConfig.transportUnitsRequireMovementType.size() > 0) {
            boolean z2 = false;
            Iterator it = this.unitConfig.transportUnitsRequireMovementType.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                if (((UnitMovementType) it.next()) == baseUnit.getMovementType()) {
                    z2 = true;
                    break;
                }
            }
            if (!z2) {
                return false;
            }
        }
        return GameViewUtils.a(baseUnit, this.unitConfig.transportUnitsBlockAirAndWaterUnits, this.unitConfig.transportUnitsBlockOtherTransports);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean e(BaseUnit baseUnit, boolean z) {
        if (!d(baseUnit, z)) {
            return false;
        }
        loadTransportedUnit(baseUnit);
        return true;
    }

    /* JADX INFO: renamed from: C */
    public void loadTransportedUnit(BaseUnit baseUnit) {
        baseUnit.transportContainer = this;
        this.transportedUnits.add(baseUnit);
        if (this.unitConfig.whileNeutralConvertToTransportedTeam && this.team == PlayerTeam.TEAM_ALL) {
            changeTeam(baseUnit.team);
        }
        a(UnitEventType.transportingNewUnit, baseUnit);
        baseUnit.a(UnitEventType.enteredTransport, this);
        GameEngine.getInstance().gameUI.deselectUnit(baseUnit);
    }

    /* JADX INFO: renamed from: D */
    public void unloadTransportedUnit(BaseUnit baseUnit) {
        a(UnitEventType.transportUnloadedOrRemovedUnit, baseUnit);
        baseUnit.a(UnitEventType.leftTransport, this);
        if (this.unitConfig.convertToNeutralIfNotTransporting && this.transportedUnits.size() == 0) {
            changeTeam(PlayerTeam.TEAM_ALL);
        }
    }

    @Override // com.corrodinggames.rts.game.units.TransportUnitInterface
    public void e(BaseUnit baseUnit) {
        if (baseUnit.transportContainer == this) {
            this.transportedUnits.remove(baseUnit);
            baseUnit.transportContainer = null;
            unloadTransportedUnit(baseUnit);
            return;
        }
        GameEngine.logWarningAndStack("Unit is not being transported");
    }

    /* JADX INFO: renamed from: L */
    public void startUnloading() {
        if (this.unitConfig.maxTransportingUnits == 0) {
            return;
        }
        this.isUnloading = true;
        this.transportUnloadTimer = 30.0f;
    }

    /* JADX INFO: renamed from: M */
    public void finishUnloading() {
        if (this.unitConfig.maxTransportingUnits == 0) {
            return;
        }
        this.isUnloading = false;
    }

    public boolean g(boolean z) {
        if (this.transportedUnits.size() == 0) {
            return false;
        }
        boolean unloadOnEvenSide = this.transportedUnits.size() % 2 == 0;
        BaseUnit transportedUnit = (BaseUnit) this.transportedUnits.remove(this.transportedUnits.size() - 1);
        return b(transportedUnit, z, unloadOnEvenSide);
    }

    /* JADX INFO: renamed from: a */
    public boolean canAttackTargetUnit(BaseUnit baseUnit, boolean z, boolean z2) {
        this.transportedUnits.remove(baseUnit);
        return b(baseUnit, z, z2);
    }

    @Override
    // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject
    public void a(GameInputStream gameInputStream) throws IOException {
        UnitType unitTypeQ;
        byte b;
        LegInstance legInstance;
        if (gameInputStream.getProtocolVersion() >= 32) {
            int i = gameInputStream.readInt();
            this.frameAnimationTimer = gameInputStream.readFloat();
            this.transportUnloadTimer = gameInputStream.readFloat();
            this.isUnloading = gameInputStream.readBoolean();
            this.transportedUnits.clear();
            int i2 = gameInputStream.readInt();
            for (int i3 = 0; i3 < i2; i3++) {
                BaseUnit baseUnit = gameInputStream.readBaseUnit();
                if (baseUnit != null) {
                    this.transportedUnits.add(baseUnit);
                }
            }
            if (i >= 1) {
                this.r = gameInputStream.readBoolean();
            }
            if (i >= 2) {
                this.o = gameInputStream.readFloat();
            }
            if (i >= 3) {
                this.frameAnimationDelay = gameInputStream.readFloat();
                this.s = gameInputStream.readFloat();
            }
            if (i >= 4) {
                this.v = gameInputStream.readBoolean();
            }
            if (i >= 5 && (b = gameInputStream.readByte()) != 0) {
                du();
                if (this.legInstances == null) {
                    GameEngine.log("readIn: legs==null but leg data found in save, this might be due to missing or changed mods");
                }
                for (int i4 = 0; i4 < b; i4++) {
                    if (this.legInstances == null) {
                        legInstance = new LegInstance();
                    } else if (i4 >= this.legInstances.length) {
                        GameEngine.log("readIn: legs " + i4 + ">=" + this.legInstances.length);
                        legInstance = new LegInstance();
                    } else {
                        legInstance = this.legInstances[i4];
                    }
                    LegInstance legInstance2 = legInstance;
                    legInstance2.b = gameInputStream.readFloat();
                    legInstance2.c = gameInputStream.readFloat();
                    legInstance2.d = gameInputStream.readFloat();
                    legInstance2.i = gameInputStream.readFloat();
                    legInstance2.k = gameInputStream.readBoolean();
                    legInstance2.o = true;
                    legInstance2.j = gameInputStream.readBoolean();
                    legInstance2.m = gameInputStream.readBoolean();
                    legInstance2.n = gameInputStream.readBoolean();
                }
            }
            if (i >= 6) {
                this.factoryQueueManager.a(gameInputStream);
            }
            if (i >= 7 && (unitTypeQ = gameInputStream.q()) != null) {
                if (unitTypeQ instanceof CustomUnitConfig) {
                    this.factoryUnitConfig = (CustomUnitConfig) unitTypeQ;
                } else {
                    GameEngine.logColored("Got non CustomUnitMetadata object of:" + unitTypeQ.getUnitTypeDescriptionShort() + " loading real_meta");
                    this.factoryUnitConfig = CustomUnitConfig.instance;
                }
            }
            if (i >= 8) {
                this.frameAnimationReverse = gameInputStream.readBoolean();
                this.frameAnimationPlaying = gameInputStream.readBoolean();
            }
            if (i >= 9 && gameInputStream.readBoolean()) {
                a(AnimationTag.a(gameInputStream), true);
            }
            if (i >= 10) {
                UnitStats.a(this, gameInputStream, i);
            }
            if (i >= 11) {
                this.q = gameInputStream.readFloat();
            }
        }
        super.a(gameInputStream);
        if (this.legInstances != null) {
            this.dP = this.posX;
            this.dQ = this.posY;
            this.dR = this.posZ;
            this.dS = this.rotationSpeed;
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: bY */
    public int getTransportedUnitsWeight() {
        if (this.unitConfig.maxTransportingUnits == 0 || !this.unitConfig.showTransportBar) {
            return -1;
        }
        return dI();
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: bZ */
    public int getMaxTransportWeight() {
        if (this.unitConfig.maxTransportingUnits == 0 || !this.unitConfig.showTransportBar) {
            return -1;
        }
        return this.unitConfig.maxTransportingUnits;
    }

    public void ds() {
        if (this.transportedUnits.size > 0) {
            h(this.unitConfig.transportUnitsKillOnDeath.read(this));
        }
    }

    public void h(boolean z) {
        for (BaseUnit baseUnit : this.transportedUnits) {
            baseUnit.transportContainer = null;
            baseUnit.posX = this.posX + (Utility.fastCos(this.rotationSpeed) * (-9.0f));
            baseUnit.posY = this.posY + (Utility.fastSin(this.rotationSpeed) * (-9.0f));
            if (z) {
                baseUnit.markForDeath();
            }
        }
        this.transportedUnits.clear();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit
    public void bu() {
        if (!this.isDead) {
            a(UnitEventType.destroyed);
        }
        Object[] objArrA = this.unitConfig.onCreateListeners.a();
        for (int i = this.unitConfig.onCreateListeners.size - 1; i >= 0; i--) {
            ((CustomUnitRenderHook) objArrA[i]).b(this);
        }
        super.bu();
    }

    @Override
    // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.SizedObject, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void remove() {
        ds();
        Object[] objArrA = this.unitConfig.onCreateListeners.a();
        for (int i = this.unitConfig.onCreateListeners.size - 1; i >= 0; i--) {
            ((CustomUnitRenderHook) objArrA[i]).c(this);
        }
        PlayerTeam.a((BaseUnit) this);
        this.factoryQueueManager.a(true);
        super.remove();
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: dt, reason: merged with bridge method [inline-methods] */
    public CustomUnitConfig r() {
        return this.unitConfig;
    }

    @Override // com.corrodinggames.rts.game.units.MovableUnit, com.corrodinggames.rts.game.units.OrderableUnit
    public boolean I() {
        if (this.unitConfig.isBuildingUnit || this.frameAnimationLooping || this.parentEntity != null) {
            return false;
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: aR */
    public boolean canExecuteMovementCommands() {
        if (this.unitConfig.ignoreMoveOrders) {
            return false;
        }
        AttachmentSlotDefinition attachmentSlotDefinitionDn = dn();
        if (attachmentSlotDefinitionDn != null && !attachmentSlotDefinitionDn.O) {
            return false;
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean aS() {
        if (this.unitConfig.isBuildingUnit) {
            return false;
        }
        AttachmentSlotDefinition attachmentSlotDefinitionDn = dn();
        if (attachmentSlotDefinitionDn != null && !attachmentSlotDefinitionDn.H) {
            return false;
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public UnitMovementType getMovementType() {
        return this.unitConfig.effectiveMovementType;
    }

    @Override // com.corrodinggames.rts.game.units.MovableUnit, com.corrodinggames.rts.game.units.BaseUnit
    public boolean i() {
        return this.unitConfig.effectiveMovementType == UnitMovementType.AIR && this.posZ >= 4.0f;
    }

    @Override // com.corrodinggames.rts.game.units.MovableUnit, com.corrodinggames.rts.game.units.BaseUnit
    public boolean Q() {
        return this.posZ <= -1.0f;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cv */
    public boolean isWaterUnit() {
        if (getMovementType() == UnitMovementType.WATER) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: ct */
    public boolean isAirborne() {
        if (getMovementType() == UnitMovementType.AIR) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public Texture v() {
        if (this.team.teamId == -1 || this.unitConfig.teamColoredIconTextures == null) {
            return null;
        }
        return this.unitConfig.teamColoredIconTextures[this.team.getTeamColorIndex()];
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture d() {
        if (this.isDead && this.unitConfig.image_wreak != null) {
            return this.unitConfig.image_wreak;
        }
        return this.unitConfig.teamColoredBaseTextures[this.team.getTeamColorIndex()];
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture k() {
        return this.unitConfig.shadowTexture;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: F */
    public boolean canDrawShadow() {
        return GameEngine.getInstance().settingsEngine.renderExtraShadows && (!this.isDead || this.posZ >= 1.0f) && this.posZ >= -1.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture d(int i) {
        TurretConfig turretConfig = this.unitConfig.turrets[i];
        if (turretConfig.invisibilityCondition != null && turretConfig.invisibilityCondition.read(this)) {
            return null;
        }
        if (turretConfig.teamColoredImages != null) {
            return turretConfig.teamColoredImages[this.team.getTeamColorIndex()];
        }
        if (turretConfig.image != null) {
            return turretConfig.image;
        }
        if (this.unitConfig.teamColoredTurretTextures != null) {
            return this.unitConfig.teamColoredTurretTextures[this.team.getTeamColorIndex()];
        }
        return this.unitConfig.image_turret;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float h(int i) {
        return this.unitConfig.turrets[i].imageDrawOffsetX;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float i(int i) {
        return this.unitConfig.turrets[i].imageDrawOffsetY;
    }

    private boolean b(BaseUnit baseUnit, boolean z, boolean z2) {
        boolean z3;
        float fFloatValue = 180.0f;
        if (this.unitConfig.exit_dirOffset != null) {
            fFloatValue = this.unitConfig.exit_dirOffset.floatValue();
        }
        float fFloatValue2 = 70.0f;
        if (this.unitConfig.exit_moveAwayAmount != null) {
            fFloatValue2 = this.unitConfig.exit_moveAwayAmount.floatValue();
        }
        float f = this.unitConfig.exit_x;
        float f2 = this.unitConfig.exit_y;
        float fFastCos = (this.posX + (Utility.fastCos(this.rotationSpeed + fFloatValue) * f2)) - (Utility.fastSin(this.rotationSpeed + fFloatValue) * f);
        float fFastSin = this.posY + (Utility.fastSin(this.rotationSpeed + fFloatValue) * f2) + (Utility.fastCos(this.rotationSpeed + fFloatValue) * f);
        float fFastCos2 = fFastCos + (Utility.fastCos(this.rotationSpeed + 90.0f) * (z2 ? -7.0f : 7.0f));
        float fFastSin2 = fFastSin + (Utility.fastSin(this.rotationSpeed + 90.0f) * (z2 ? -7.0f : 7.0f));
        if (!z && !bI()) {
            if (!GameViewUtils.a(baseUnit, fFastCos2, fFastSin2)) {
                fFastCos2 += 10.0f;
            }
            if (!GameViewUtils.a(baseUnit, fFastCos2, fFastSin2)) {
                fFastCos2 -= 20.0f;
            }
            if (!GameViewUtils.a(baseUnit, fFastCos2, fFastSin2)) {
                fFastCos2 -= 10.0f;
                fFastSin2 += 10.0f;
            }
            if (!GameViewUtils.a(baseUnit, fFastCos2, fFastSin2)) {
                fFastSin2 -= 20.0f;
            }
        }
        if (!z && !GameViewUtils.a(baseUnit, fFastCos2, fFastSin2) && !bI()) {
            z3 = false;
        } else {
            z3 = true;
            boolean z4 = false;
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (orderableUnit.parentEntity == this) {
                    AttachmentSlotDefinition attachmentSlotDefinitionDn = orderableUnit.dn();
                    if (attachmentSlotDefinitionDn == null) {
                        GameEngine.log("Unload, attachment data is null");
                    }
                    if (attachmentSlotDefinitionDn != null) {
                        z4 = attachmentSlotDefinitionDn.E;
                    }
                }
            }
            GameViewUtils.a(baseUnit, this);
            float f3 = this.rotationSpeed + fFloatValue;
            if (!z4) {
                baseUnit.posX = fFastCos2;
                baseUnit.posY = fFastSin2;
                baseUnit.rotationSpeed = f3;
                baseUnit.velocityY = 0.0f;
                baseUnit.velocityX = 0.0f;
                baseUnit.worldX = 0.0f;
                baseUnit.worldY = 0.0f;
                baseUnit.worldX += 0.1f;
            }
            baseUnit.attackTargetUnit = this;
            baseUnit.bS = 45.0f;
            if (z4) {
                baseUnit.bS = 85.0f;
            }
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit2 = (OrderableUnit) baseUnit;
                if (!z4) {
                    orderableUnit2.j(baseUnit.rotationSpeed);
                }
                if (!this.unitConfig.transportUnitsKeepWaypoints.read(this)) {
                    orderableUnit2.clearAllWaypoints();
                    if (fFloatValue2 != 0.0f) {
                        orderableUnit2.appendMoveWaypoint(baseUnit.posX + (Utility.fastCos(f3 + (z2 ? -7.0f : 7.0f)) * fFloatValue2), baseUnit.posY + (Utility.fastSin(f3 + (z2 ? -7.0f : 7.0f)) * fFloatValue2));
                    }
                    orderableUnit2.waypointSyncGroupId = 0;
                } else {
                    orderableUnit2.clearPathData();
                }
            }
            if (!z4) {
                if (!this.unitConfig.exitHeightIgnoreParent) {
                    baseUnit.posZ = this.posZ;
                }
                baseUnit.posZ += this.unitConfig.exit_heightOffset;
            }
            if (baseUnit instanceof CustomUnit) {
                ((CustomUnit) baseUnit).dF();
            }
        }
        if (!z3) {
            this.transportedUnits.add(baseUnit);
        } else {
            unloadTransportedUnit(baseUnit);
        }
        return z3;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void bq() {
        if (!this.unitConfig.hideScorchMark) {
            super.bq();
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean e() {
        PlayerTeam playerTeam;
        GameEngine gameEngine = GameEngine.getInstance();
        if (bI()) {
            gameEngine.pathfindingEngine.a(this);
        }
        if (this.buildProgress < 1.0f) {
            if (this.unitConfig.effectOnDeathIfUnbuilt != null && this.unitConfig.effectOnDeathIfUnbuilt.b()) {
                this.unitConfig.effectOnDeathIfUnbuilt.a(this.posX, this.posY, this.posZ, this.rotationSpeed, null);
                return false;
            }
            if (this.unitConfig.effectOnDeath != null && this.unitConfig.effectOnDeath.b()) {
                this.unitConfig.effectOnDeath.a(this.posX, this.posY, this.posZ, this.rotationSpeed, null);
                return false;
            }
            a(UnitSize.verysmall);
            return false;
        }
        if (!this.unitConfig.slowDeathFall) {
            S(0);
        }
        if (this.unitConfig.effectOnDeath != null && this.unitConfig.effectOnDeath.b()) {
            this.unitConfig.effectOnDeath.a(this.posX, this.posY, this.posZ, this.rotationSpeed, null);
        }
        if (this.unitConfig.fireTurretAtSelfOnDeathIndex != -1) {
            a((BaseUnit) null, this.posX, this.posY, this.unitConfig.fireTurretAtSelfOnDeathIndex, (CustomProjectileTemplate) null, 0);
        }
        if (this.unitConfig.unitsSpawnedOnDeath != null) {
            if (this.unitConfig.unitsSpawnedOnDeath_setToTeamOfLastAttacker && this.unitTarget1 != null && this.unitTarget1.team != null) {
                playerTeam = this.unitTarget1.team;
            } else {
                playerTeam = this.team;
            }
            if (!playerTeam.isTeamVictory) {
                this.unitConfig.unitsSpawnedOnDeath.a(this.posX, this.posY, this.posZ, this.rotationSpeed, playerTeam, this.isSelected, this);
            }
        }
        this.isAlive = false;
        if (!this.unitConfig.slowDeathFall) {
            ds();
        }
        if (this.unitConfig.nukeOnDeath) {
            boolean z = false;
            if (this.unitConfig.nukeOnDeathDisableWhenNoNuke && gameEngine.isInGameOrLobby() && gameEngine.networkEngine.roomSettings.noNukes) {
                z = true;
            }
            if (!z) {
                Projectile projectileA = NukeLauncher.a(this, this.posX, this.posY, this.posX, this.posY);
                projectileA.isBallistic = false;
                projectileA.explosionRadius = this.unitConfig.nukeOnDeathRange;
                projectileA.splashDamage = this.unitConfig.nukeOnDeathDamage;
            }
        }
        if (this.unitConfig.fireOnDeath != 0) {
            FireUnit fireUnit = new FireUnit(false);
            fireUnit.posX = this.posX;
            fireUnit.posY = this.posY;
            fireUnit.setUnitTeam(PlayerTeam.TEAM_UNKNOWN);
            PlayerTeam.c(fireUnit);
        }
        if (this.unitConfig.soundOnDeath != null) {
            this.unitConfig.soundOnDeath.a(this.posX, this.posY, 1.0f);
        }
        if (this.unitConfig.isBio) {
            if (this.unitConfig.soundOnDeath == null) {
                gameEngine.soundEngine.playSound(SoundEngine.bugDieSound, 0.8f, this.posX, this.posY);
            }
            if (this.unitConfig.explodeOnDeath) {
                if (!i() && !this.unitConfig.hideScorchMark) {
                    ScorchMark.a(this, 1);
                }
                if (this.unitConfig.explodeTypeOnDeath != null) {
                    a(this.unitConfig.explodeTypeOnDeath, true);
                } else if (this.unitConfig.dieOnAttack) {
                    a(UnitSize.large);
                }
            }
            if (this.unitConfig.isBug) {
                for (int i = 0; i < bp(); i++) {
                    gameEngine.effectManager.createBloodEffect(this.posX, this.posY, this.posZ);
                }
            }
        } else {
            if (this.legInstances != null) {
                dv();
                for (int i2 = 0; i2 < this.legInstances.length; i2++) {
                    LegInstance legInstance = this.legInstances[i2];
                    LegConfig legConfig = this.unitConfig.legConfig[i2];
                    float f = this.posX + legInstance.b;
                    float f2 = this.posY + legInstance.c;
                    if (legConfig.J && !legConfig.p && (legConfig.q == null || !legConfig.q.read(this))) {
                        if (!GameViewUtils.d(f, f2) && !this.unitConfig.hideScorchMark) {
                            ScorchMark.a(f, f2);
                        }
                        gameEngine.effectManager.createSmallExplosion(f, f2, 0.0f);
                    }
                }
            }
            if (!i()) {
                if (this.unitConfig.explodeOnDeath) {
                    if (this.unitConfig.explodeTypeOnDeath != null) {
                        a(this.unitConfig.explodeTypeOnDeath, true);
                    } else {
                        a(UnitSize.small);
                    }
                }
            } else {
                if (this.unitConfig.explodeOnDeath) {
                    if (this.unitConfig.explodeTypeOnDeath != null) {
                        a(this.unitConfig.explodeTypeOnDeath, false);
                    } else {
                        gameEngine.effectManager.createSmallExplosion(this.posX, this.posY, this.posZ);
                    }
                }
                for (int i3 = 0; i3 < bp(); i3++) {
                    gameEngine.effectManager.createBloodEffect2(this.posX, this.posY, this.posZ);
                }
            }
        }
        if (this.unitConfig.slowDeathFall) {
            return true;
        }
        if (this.unitConfig.image_wreak != null) {
            this.baseTexture = this.unitConfig.image_wreak;
            this.animationFrameIndex = 0;
            this.ew = true;
            return true;
        }
        return false;
    }

    public void a(CustomUnitConfig customUnitConfig, boolean z, boolean z2) {
        a(customUnitConfig, z, z2, (CustomUnitDataField[]) null);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void U() {
        super.U();
        for (int i = 0; i < this.unitConfig.turrets.length; i++) {
            UnitMovementData unitMovementData = this.movementLevels[i];
            GameEngine.log("Dir was:" + unitMovementData.targetX + " for name:" + this.unitConfig.turrets[i].name);
            GameEngine.log("lockDelay:" + unitMovementData.velocityY + " shootCooldown:" + unitMovementData.rotation);
            GameEngine.log("updateAndShouldResetTurret:" + b(i, 0.0f));
            float fC = C(i);
            GameEngine.log("idleDir:" + fC);
            GameEngine.log("diffDir:" + Utility.rotateTowardsAngle(unitMovementData.targetX, fC, 360.0f));
        }
    }

    public void a(CustomUnitConfig customUnitConfig, boolean z, boolean z2, CustomUnitDataField[] customUnitDataFieldArr) {
        GameEngine gameEngine = GameEngine.getInstance();
        CustomUnitConfig customUnitConfig2 = this.unitConfig;
        UnitStats unitStats = this.y;
        this.unitConfig = customUnitConfig;
        this.unitType = this.unitConfig;
        this.y = customUnitConfig.unitStats;
        if (z2) {
            UnitStats.a(this, unitStats, customUnitConfig2);
        } else if (customUnitDataFieldArr != null) {
            UnitStats.a(this, unitStats, customUnitDataFieldArr);
        }
        bS();
        if (!z2) {
            j(true);
        }
        if (!z) {
            if (customUnitConfig.turrets.length > 1) {
                boolean z3 = true;
                if (customUnitConfig.turrets.length != customUnitConfig2.turrets.length) {
                    z3 = false;
                } else {
                    int i = 0;
                    while (true) {
                        if (i >= customUnitConfig.turrets.length) {
                            break;
                        }
                        if (customUnitConfig.turrets[i].name.equalsIgnoreCase(customUnitConfig2.turrets[i].name)) {
                            i++;
                        } else {
                            z3 = false;
                            break;
                        }
                    }
                }
                if (!z3) {
                    UnitMovementData[] unitMovementDataArr = new UnitMovementData[customUnitConfig.turrets.length];
                    for (int i2 = 0; i2 < customUnitConfig.turrets.length; i2++) {
                        TurretConfig turretConfig = customUnitConfig.turrets[i2];
                        TurretConfig turretConfigFindProjectileConfigByName = customUnitConfig2.findProjectileConfigByName(turretConfig.name);
                        if (turretConfigFindProjectileConfigByName != null) {
                            unitMovementDataArr[turretConfig.turretIndex] = this.movementLevels[turretConfigFindProjectileConfigByName.turretIndex];
                            this.movementLevels[turretConfigFindProjectileConfigByName.turretIndex] = null;
                        }
                    }
                    for (int i3 = 0; i3 < customUnitConfig.turrets.length; i3++) {
                        if (unitMovementDataArr[i3] == null) {
                            int i4 = 0;
                            while (true) {
                                if (i4 >= customUnitConfig.turrets.length) {
                                    break;
                                }
                                if (this.movementLevels[i4] == null) {
                                    i4++;
                                } else {
                                    unitMovementDataArr[i3] = this.movementLevels[i4];
                                    this.movementLevels[i4] = null;
                                    break;
                                }
                            }
                            if (unitMovementDataArr[i3] == null) {
                                unitMovementDataArr[i3] = new UnitMovementData();
                            }
                            unitMovementDataArr[i3].a(this.rotationSpeed);
                        }
                    }
                    this.movementLevels = unitMovementDataArr;
                }
            }
        } else {
            for (int i5 = 0; i5 < customUnitConfig.turrets.length; i5++) {
                this.movementLevels[i5].a(this.rotationSpeed + B(i5));
            }
        }
        if (this.unitConfig.imageFloatingPointSize) {
            V(this.unitConfig.frameWidth);
            W(this.unitConfig.frameHeight);
        } else {
            T(this.unitConfig.frameWidth);
            U(this.unitConfig.frameHeight);
        }
        this.ew = false;
        this.radius = this.unitConfig.radius;
        this.displayRadius = this.unitConfig.displayRadius;
        if (this.isDead) {
            this.animationFrameIndex = 0;
        } else {
            this.animationFrameIndex = this.unitConfig.default_frame;
        }
        this.frameAnimationLooping = false;
        if (z) {
            this.posZ += this.unitConfig.startingHeightOffset;
        }
        float f = this.maxHealth;
        this.maxHealth = this.y.maxHp;
        if (z || f == 0.0f) {
            o(this.maxHealth);
        } else {
            o((this.currentHealth / f) * this.maxHealth);
        }
        float f2 = this.unitEnergyMax;
        this.unitEnergyMax = this.y.maxShield;
        if (this.unitConfig.startShieldAtZero) {
            if (this.shield > this.unitEnergyMax) {
                this.shield = this.unitEnergyMax;
            }
        } else if (z || f2 == 0.0f) {
            this.shield = this.unitEnergyMax;
        } else {
            this.shield = (this.shield / f2) * this.unitEnergyMax;
        }
        if (this.unitConfig.startEnergyAtZero) {
            if (this.currentEnergy > this.y.maxEnergy) {
                this.currentEnergy = this.y.maxEnergy;
            }
        } else if (z) {
            this.currentEnergy = this.y.maxEnergy * this.unitConfig.energyStartingPercentage;
        } else if (unitStats.maxEnergy == 0.0f) {
            this.currentEnergy = this.y.maxEnergy;
        } else {
            this.currentEnergy = (this.currentEnergy / unitStats.maxEnergy) * this.y.maxEnergy;
        }
        if (this.team == null) {
            this.baseTexture = this.unitConfig.baseTexture;
        } else {
            S();
        }
        if (this.unitConfig.isBuildingUnit && z) {
            this.rotationSpeed = -90.0f;
        }
        f_();
        if (!z && ((this.price != null || this.additionalCost != null) && (!UnitPrice.b(this.unitConfig.price, customUnitConfig2.price) || !UnitPrice.b(this.unitConfig.streamingCost, customUnitConfig2.streamingCost)))) {
            this.price = null;
            this.additionalCost = null;
        }
        if (!z) {
            boolean z4 = this.unitConfig.isBuildingUnit() != customUnitConfig2.isBuildingUnit();
            if (this.unitConfig.isBuildingUnit()) {
                this.velocityY = 0.0f;
                this.velocityX = 0.0f;
                if (customUnitConfig2.isBuildingUnit() && !this.unitConfig.footprint.equals(customUnitConfig2.footprint)) {
                    z4 = true;
                }
            }
            if (z4) {
                gameEngine.pathfindingEngine.a(this);
            }
        }
        this.moveSpeedMultiplier = 1.0f;
        if (this.unitConfig.drawLayer != -2) {
        }
        if (!this.isDead) {
            dF();
        }
        if (this.unitConfig.isMelee) {
        }
        du();
        getTrackingManager().a(this.unitConfig);
        if (!z) {
            int techLevel = getTechLevel();
            for (int i6 = 0; i6 < techLevel; i6++) {
                UnitMovementData unitMovementData = this.movementLevels[i6];
                TurretConfig turretConfig2 = this.unitConfig.turrets[i6];
                if (turretConfig2 != null) {
                    if (unitMovementData.rotation > turretConfig2.m) {
                        unitMovementData.rotation = turretConfig2.m;
                    }
                    if (unitMovementData.speed > turretConfig2.warmup) {
                        unitMovementData.speed = turretConfig2.warmup;
                    }
                }
            }
        }
        if (!z) {
            if (!this.unitConfig.hasSetRallyAction) {
                this.factoryQueueManager.b = null;
            }
            if (this.unitConfig.movementType != customUnitConfig2.movementType) {
                clearPathData();
            }
        }
        if (this.unitConfig.convertToNeutralIfNotTransporting && this.transportedUnits.size() == 0) {
            setUnitTeam(PlayerTeam.TEAM_ALL);
        }
        if (this.isSelected && !gameEngine.gameUI.canUnitBeSelected(this)) {
            gameEngine.gameUI.deselectUnit(this);
        }
        if (!z) {
            Object[] objArrA = this.unitConfig.onCreateListeners.a();
            for (int i7 = this.unitConfig.onCreateListeners.size - 1; i7 >= 0; i7--) {
                ((CustomUnitRenderHook) objArrA[i7]).a(this, customUnitConfig2);
            }
            if (this.buildProgress >= 1.0f) {
                if (this.y.fogOfWarSightRange > unitStats.fogOfWarSightRange) {
                    c(false);
                }
            } else {
                if ((this.unitConfig.fogOfWarSightRangeWhileNotBuilt != -1 ? this.unitConfig.fogOfWarSightRangeWhileNotBuilt : this.y.fogOfWarSightRange) > (customUnitConfig2.fogOfWarSightRangeWhileNotBuilt != -1 ? customUnitConfig2.fogOfWarSightRangeWhileNotBuilt : unitStats.fogOfWarSightRange)) {
                    c(false);
                }
            }
        }
        if (this.frameAnimationReverse && this.unitConfig.createdAnimation != null) {
            this.animationController.a(this.unitConfig.createdAnimation, 7, true);
        }
    }

    public CustomUnit(boolean z, CustomUnitConfig customUnitConfig) {
        super(z);
        this.animationController = new CustomUnitAnimationController(this);
        this.currentFrameTime = 1.0f;
        this.frameAnimationPlaying = true;
        this.frameAnimationReverse = true;
        this.moveSpeedMultiplier = 1.0f;
        this.hasProcessedDeathGroundCollision = false;
        this.p = true;
        this.transportedUnits = new FastArrayList();
        this.C = null;
        this.E = null;
        this.F = null;
        this.factoryQueueManager = new FactoryQueueManager(this);
        this.legInstances = null;
        this.eg = new FastArrayList();
        a(customUnitConfig, true, false);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void f_() {
        if (!this.unitConfig.isBuildingUnit && !this.isDead) {
            this.isAlive = true;
            if (this.unitConfig.disableAllUnitCollisions) {
                this.isAlive = false;
            }
        } else {
            this.isAlive = false;
        }
        if (this.parentEntity != null) {
            this.isAlive = false;
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public float getCreditIncomeRate() {
        int i = this.unitConfig.generationTemplate.b;
        if (!this.p) {
            return 0.0f;
        }
        return i * this.unitConfig.generationRate;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cz */
    public StoredResources getResourceGenerationRates() {
        if (!this.p) {
            return StoredResources.a;
        }
        return this.unitConfig.generationCondition;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cA */
    public StoredResources getGlobalCustomResourceGenerationRates() {
        if (!this.p) {
            return StoredResources.a;
        }
        return this.unitConfig.generationTagTemplate;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean a(int i, BaseUnit baseUnit, boolean z, boolean z2) {
        return a(this.unitConfig.turrets[i], i, baseUnit, z, z2);
    }

    public final boolean a(TurretConfig turretConfig, int i, float f, float f2, boolean z) {
        GameEngine gameEngine = GameEngine.getInstance();
        float fDistanceSq = Utility.distanceSq(this.posX, this.posY, f, f2);
        if (fDistanceSq > turretConfig.effectiveRangeSquared) {
            if (this.team == gameEngine.playerTeam) {
                gameEngine.gameUI.showMediumPriorityMessage("Location too far");
                return false;
            }
            return false;
        }
        if (fDistanceSq < turretConfig.limitingMinRangeSquared) {
            if (this.team == gameEngine.playerTeam) {
                gameEngine.gameUI.showMediumPriorityMessage("Location too close");
                return false;
            }
            return false;
        }
        return true;
    }

    public final boolean a(TurretConfig turretConfig, int i, BaseUnit baseUnit, boolean z, boolean z2) {
        float f;
        if (!z && (turretConfig.hasRangeRestrictions || z2)) {
            float fDistanceSq = Utility.distanceSq(this.posX, this.posY, baseUnit.posX, baseUnit.posY);
            if (fDistanceSq > turretConfig.effectiveRangeSquared || fDistanceSq < turretConfig.limitingMinRangeSquared) {
                return false;
            }
        }
        if (!turretConfig.hasAttackRestrictions) {
            return true;
        }
        if (turretConfig.limitingAngle != -1.0f) {
            if (turretConfig.linkedTurretIndex != -1) {
                f = this.movementLevels[turretConfig.linkedTurretIndex].targetX + turretConfig.idleDir;
            } else {
                f = this.rotationSpeed + turretConfig.idleDir;
            }
            if (Utility.abs(Utility.rotateTowardsAngle(f, Utility.getAngleBetweenPoints(this.posX, this.posY, baseUnit.posX, baseUnit.posY), 360.0f)) > turretConfig.limitingAngle) {
                return false;
            }
        }
        if (turretConfig.canAttackCondition != null && !turretConfig.canAttackCondition.read(this)) {
            return false;
        }
        if (turretConfig.canOnlyAttackUnitsWithTags != null && !AnimationTag.a(turretConfig.canOnlyAttackUnitsWithTags, baseUnit.getTags())) {
            return false;
        }
        if (turretConfig.canOnlyAttackUnitsWithoutTags != null && AnimationTag.a(turretConfig.canOnlyAttackUnitsWithoutTags, baseUnit.getTags())) {
            return false;
        }
        if (baseUnit.i()) {
            if (turretConfig.canAttackFlyingUnits != null) {
                return turretConfig.canAttackFlyingUnits.read(this);
            }
            return true;
        }
        if (baseUnit.Q()) {
            if (turretConfig.canAttackUnderwaterUnits != null) {
                return turretConfig.canAttackUnderwaterUnits.read(this);
            }
            return true;
        }
        if (turretConfig.canAttackNotTouchingWaterUnits != null && !turretConfig.canAttackNotTouchingWaterUnits.read(this) && !baseUnit.isTouchingWater()) {
            return false;
        }
        if (turretConfig.canAttackLandUnits != null) {
            return turretConfig.canAttackLandUnits.read(this);
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void a(BaseUnit baseUnit, int i) {
        TurretConfig turretConfig = this.unitConfig.turrets[i];
        if (!turretConfig.canShoot || !a(turretConfig, i, baseUnit, false, false)) {
            return;
        }
        for (int i2 = 0; i2 < this.unitConfig.turrets.length; i2++) {
            TurretConfig turretConfig2 = this.unitConfig.turrets[i2];
            if (turretConfig2 != null && i2 != i && turretConfig2.linkedTurret != null && turretConfig2.linkedTurret == turretConfig && this.movementLevels[i2].rotation < 9000.0f - ((b(i) * 0.5f) - turretConfig2.warmup)) {
                this.movementLevels[i2].rotation = 0.0f;
            }
        }
        if (turretConfig.linkedTurretIndex != -1) {
            TurretConfig turretConfig3 = this.unitConfig.turrets[turretConfig.linkedTurretIndex];
            if (!turretConfig3.canShoot && turretConfig3.p != 0.0f) {
                this.movementLevels[turretConfig.linkedTurretIndex].rotation = b(turretConfig.linkedTurretIndex) + t(turretConfig.linkedTurretIndex);
            }
        }
        this.animationController.a(this.unitConfig.attackAnimation, 11, true);
        b(turretConfig);
        a(baseUnit, -1.0f, -1.0f, i, (CustomProjectileTemplate) null, 0);
    }

    public boolean a(TurretConfig turretConfig) {
        if (turretConfig.u > 0.0f && (turretConfig.u > this.currentEnergy || this.v)) {
            return false;
        }
        if (turretConfig.resourceUsage != null && !turretConfig.resourceUsage.b(this)) {
            return false;
        }
        return true;
    }

    public void b(TurretConfig turretConfig) {
        if (turretConfig.u > 0.0f) {
            this.currentEnergy -= turretConfig.u;
            if (this.currentEnergy < turretConfig.u && this.unitConfig.energyNeedsToRechargeToFull) {
                this.v = true;
            }
        }
        if (turretConfig.resourceUsage != null) {
            turretConfig.resourceUsage.a(this);
        }
    }

    public static Projectile a(BaseUnit baseUnit, int i, CustomProjectileTemplate customProjectileTemplate, float f, float f2, float f3, float f4) {
        Projectile projectileA = Projectile.a(baseUnit, f, f2, f3, i);
        a(projectileA, baseUnit, i, customProjectileTemplate, f, f2, f3, f4);
        return projectileA;
    }

    public static void a(Projectile projectile, BaseUnit baseUnit, int i, CustomProjectileTemplate customProjectileTemplate, float f, float f2, float f3, float f4) {
        Effect effectCreateLightEffect;
        GameEngine gameEngine = GameEngine.getInstance();
        projectile.angle = f4;
        projectile.renderAngle = f4;
        if (baseUnit == null) {
            throw new RuntimeException("Source cannot be null");
        }
        projectile.template = customProjectileTemplate;
        projectile.spawnEmitterOnHit = customProjectileTemplate.flameWeapon;
        projectile.playsHitSound = customProjectileTemplate.hitSound;
        projectile.damage = DamageRegistry.directDamageAmount(customProjectileTemplate, customProjectileTemplate.b);
        projectile.splashDamage = DamageRegistry.areaDamageAmount(customProjectileTemplate, customProjectileTemplate.c);
        if (!customProjectileTemplate.ignoreParentShootDamageMultiplier && (baseUnit instanceof CustomUnit)) {
            CustomUnit customUnit = (CustomUnit) baseUnit;
            projectile.damage *= customUnit.y.shootDamageMultiplier;
            projectile.splashDamage *= customUnit.y.shootDamageMultiplier;
        }
        projectile.explosionRadius = customProjectileTemplate.i;
        if (customProjectileTemplate.l) {
            projectile.damageEnemiesOnly = false;
            projectile.noFriendlyFire = true;
        } else {
            projectile.damageEnemiesOnly = !customProjectileTemplate.k;
        }
        projectile.canHitSubmerged = customProjectileTemplate.n;
        if (customProjectileTemplate.m) {
            projectile.excludesAir = true;
            projectile.matchesTargetAltitude = false;
        }
        projectile.isNuke = customProjectileTemplate.p;
        projectile.revealsFog = customProjectileTemplate.q;
        projectile.visibilityChecked = customProjectileTemplate.r;
        if (customProjectileTemplate.o < 0.5d) {
            projectile.C = true;
        } else {
            projectile.damageMultiplier = customProjectileTemplate.o;
        }
        projectile.lifeTimer = customProjectileTemplate.v;
        projectile.initialDelay = customProjectileTemplate.u;
        projectile.speed = customProjectileTemplate.w;
        if (customProjectileTemplate.speedSpread != 0.0f) {
            projectile.speed += Utility.getDeterministicRandomInt((GameObject) baseUnit, (int) ((-customProjectileTemplate.speedSpread) * 100.0f), (int) (customProjectileTemplate.speedSpread * 100.0f), 1) / 100.0f;
        }
        if (customProjectileTemplate.T && i != -1) {
            projectile.followObject = baseUnit;
            if (baseUnit instanceof OrderableUnit) {
                projectile.followNodeIndex = i;
                Vector3D vector3DD = ((OrderableUnit) baseUnit).D(i);
                projectile.followLastX = vector3DD.x;
                projectile.followLastY = vector3DD.y;
                projectile.followLastZ = baseUnit.posZ + vector3DD.z;
            } else {
                projectile.followLastX = baseUnit.posX;
                projectile.followLastY = baseUnit.posY;
                projectile.followLastZ = baseUnit.posZ;
            }
        }
        projectile.velocityZ = customProjectileTemplate.D;
        projectile.velocityX = customProjectileTemplate.E;
        projectile.velocityY = customProjectileTemplate.F;
        projectile.explodesOnTimerEnd = customProjectileTemplate.explodeOnEndOfLife;
        projectile.pushForce = customProjectileTemplate.pushForce;
        projectile.pushBase = customProjectileTemplate.pushVelocity;
        projectile.damageToBuildings = customProjectileTemplate.buildingDamageMultiplier;
        projectile.targetDamageMultiplier = customProjectileTemplate.shieldDamageMultiplier;
        projectile.splashDamageMultiplier = customProjectileTemplate.shieldDeflectionMultiplier;
        projectile.globalDamageMultiplier = customProjectileTemplate.hullDamageMultiplier;
        projectile.armourIgnore = customProjectileTemplate.armourIgnoreAmount;
        if (customProjectileTemplate.areaExpandTime > 0.0f) {
            projectile.trackHitUnits = true;
            projectile.explosionAnimTimer = customProjectileTemplate.areaExpandTime;
            projectile.explosionAnimDuration = projectile.explosionAnimTimer;
        }
        projectile.color = customProjectileTemplate.color;
        if (customProjectileTemplate.teamColorRatio != 0.0f) {
            float f5 = customProjectileTemplate.teamColorRatioSourceRatio;
            int iA = ArgbColor.a(projectile.color);
            int iB = (int) (ArgbColor.b(projectile.color) * f5);
            int iC = (int) (ArgbColor.c(projectile.color) * f5);
            int iD = (int) (ArgbColor.d(projectile.color) * f5);
            int teamColorArgb = baseUnit.team.getTeamColorArgb();
            projectile.color = ArgbColor.a(iA, Utility.distance((int) (iB + (ArgbColor.b(teamColorArgb) * customProjectileTemplate.teamColorRatio)), 0, 255), Utility.distance((int) (iC + (ArgbColor.c(teamColorArgb) * customProjectileTemplate.teamColorRatio)), 0, 255), Utility.distance((int) (iD + (ArgbColor.d(teamColorArgb) * customProjectileTemplate.teamColorRatio)), 0, 255));
        }
        projectile.textureFrame = customProjectileTemplate.x;
        projectile.textureType = customProjectileTemplate.y;
        projectile.hasExploded = !customProjectileTemplate.A;
        projectile.frameIndex = customProjectileTemplate.z;
        if (customProjectileTemplate.B != null) {
            projectile.textureFrame = (short) 0;
            projectile.textureType = (short) 0;
        }
        projectile.renderScale = customProjectileTemplate.drawSize;
        projectile.hasFixedTarget = customProjectileTemplate.s;
        projectile.isInstantHit = customProjectileTemplate.I;
        projectile.renderJitter = customProjectileTemplate.V;
        projectile.persistsAfterExplosion = customProjectileTemplate.W;
        projectile.isBallistic = customProjectileTemplate.ae;
        projectile.autoRetarget = customProjectileTemplate.autoTargetingOnDeadTarget;
        projectile.hasTrail = customProjectileTemplate.af;
        if (customProjectileTemplate.effectOnCreate != null) {
            customProjectileTemplate.effectOnCreate.a(projectile.posX, projectile.posY, projectile.posZ, projectile.angle, projectile);
        }
        if (customProjectileTemplate.lightColor != -1) {
            boolean z = false;
            Effect effect = projectile.aP;
            if (effect != null && effect.parentObject == projectile && effect.isLight && effect != null) {
                if (effect.lifeTimer < 150.0f) {
                    effect.lifeTimer = 200.0f;
                }
                z = true;
            }
            if (!z && (effectCreateLightEffect = gameEngine.effectManager.createLightEffect(projectile, customProjectileTemplate.lightColor, customProjectileTemplate.lightSize)) != null) {
                if (customProjectileTemplate.lightCastOnGround) {
                    effectCreateLightEffect.ignoreParentZ = true;
                }
                if (customProjectileTemplate.L) {
                    projectile.aP = effectCreateLightEffect;
                }
            }
        }
        projectile.isSmallExplosion = customProjectileTemplate.largeHitEffect;
        if (customProjectileTemplate.ballisticDelayMoveHeight != -1.0f) {
            projectile.minHeight = customProjectileTemplate.ballisticDelayMoveHeight;
        }
        if (customProjectileTemplate.ballisticHeight != -1.0f) {
            projectile.maxHeight = customProjectileTemplate.ballisticHeight;
        }
        projectile.verticalVelocity = -1.0f;
        if (customProjectileTemplate.targetSpeed != -1.0f) {
            projectile.targetSpeed = customProjectileTemplate.targetSpeed;
        }
        projectile.acceleration = customProjectileTemplate.targetSpeedAcceleration;
        if (customProjectileTemplate.spawnUnit != null) {
        }
        projectile.animationSet = customProjectileTemplate.tags;
        projectile.drawLayer = baseUnit.drawLayer;
        if (projectile.drawLayer < 4 && f3 >= -1.0f) {
            projectile.drawLayer = 4;
        }
        if (customProjectileTemplate.U) {
            projectile.drawLayer = 1;
        }
    }

    public Projectile a(BaseUnit baseUnit, float f, float f2, int i, CustomProjectileTemplate customProjectileTemplate, int i2) {
        CustomProjectileTemplate customProjectileTemplate2;
        GameEngine gameEngine = GameEngine.getInstance();
        TurretConfig turretConfig = this.unitConfig.turrets[i];
        if (turretConfig.onShootPlayAnimation != null) {
            this.animationController.a(turretConfig.onShootPlayAnimation.b(), 6, true);
        }
        if (turretConfig.onShootFreezeBodyMovementFor > 0.0f) {
            setBodyMovementFreezeTimer(turretConfig.onShootFreezeBodyMovementFor);
        }
        if (turretConfig.onShootTriggerActions != null) {
            turretConfig.onShootTriggerActions.a(this, new PointF(f, f2), baseUnit, i2 + 1, 0);
        }
        if (customProjectileTemplate == null) {
            customProjectileTemplate2 = this.unitConfig.projectileTemplatesById[turretConfig.a(this)];
        } else {
            customProjectileTemplate2 = customProjectileTemplate;
        }
        PointF shadowTexture = getShadowOffsetForLevel(i);
        Vector3D vector3DD = D(i);
        if (turretConfig.unloadUnitsAndGiveAttackOrderCount > 0) {
            for (int i3 = 0; i3 < turretConfig.unloadUnitsAndGiveAttackOrderCount; i3++) {
                if (this.transportedUnits != null && this.transportedUnits.size() > 0) {
                    BaseUnit baseUnit2 = (BaseUnit) this.transportedUnits.remove(this.transportedUnits.size() - 1);
                    UnitMovementData unitMovementData = this.movementLevels[i];
                    GameViewUtils.a(baseUnit2, this);
                    baseUnit2.posX = vector3DD.x;
                    baseUnit2.posY = vector3DD.y;
                    baseUnit2.rotationSpeed = unitMovementData.targetX;
                    if (baseUnit2 instanceof OrderableUnit) {
                        OrderableUnit orderableUnit = (OrderableUnit) baseUnit2;
                        orderableUnit.clearAllWaypoints();
                        if (baseUnit != null) {
                            orderableUnit.appendAttackWaypoint(baseUnit);
                        } else {
                            orderableUnit.appendAttackMoveWaypoint(f, f2);
                        }
                    }
                }
            }
        }
        Projectile projectileA = null;
        float reusedRemainingLife = 0.0f;
        if (customProjectileTemplate2.L && customProjectileTemplate == null) {
            if (this.G == null) {
                this.G = new Projectile[31];
            }
            projectileA = findReusableProjectile(this.G, i, customProjectileTemplate2);
            if (projectileA != null) {
                reusedRemainingLife = projectileA.lifeTimer;
                projectileA.a(this, vector3DD.x, vector3DD.y, this.posZ + vector3DD.z);
                if (!customProjectileTemplate2.N && projectileA.hitUnits != null) {
                    projectileA.hitUnits.clear();
                }
            }
        }
        this.unitCounter = (int) (((long) this.unitCounter) + 1 + this.objectId);
        float f3 = this.movementLevels[i].targetX;
        boolean z = false;
        if (projectileA == null) {
            projectileA = Projectile.a(this, vector3DD.x, vector3DD.y, this.posZ + vector3DD.z, i);
            if (customProjectileTemplate2.L && customProjectileTemplate == null) {
                this.G[i] = projectileA;
            }
        } else {
            projectileA.template = customProjectileTemplate2;
            z = true;
        }
        a(projectileA, this, i, customProjectileTemplate2, vector3DD.x, vector3DD.y, this.posZ + vector3DD.z, f3);
        if (z && customProjectileTemplate2.reuseAcrossTurrets) {
            projectileA.lifeTimer = reusedRemainingLife;
        }
        customProjectileTemplate2.a(this, projectileA, baseUnit, f, f2, m());
        if (!z && customProjectileTemplate2.R == 0.0f && customProjectileTemplate2.S == 0.0f) {
            projectileA.trackOffsetX = shadowTexture.x;
            projectileA.trackOffsetY = shadowTexture.y;
        }
        if (turretConfig.shootLightColor != null) {
            gameEngine.effectManager.createLightEffect(vector3DD.x, vector3DD.y, this.posZ + vector3DD.z, turretConfig.shootLightColor.intValue());
        }
        if (turretConfig.shootFlameEffect != null) {
            turretConfig.shootFlameEffect.a(vector3DD.x, vector3DD.y, this.posZ + vector3DD.z, this.movementLevels[i].targetX, this);
        }
        if (turretConfig.shootSound != null) {
            turretConfig.shootSound.a(vector3DD.x, vector3DD.y, 1.0f + Utility.randomFloatInRange(-0.07f, 0.07f));
        }
        if (this.unitConfig.stopTargetingAfterFiring) {
            this.attackTarget = null;
        }
        if (turretConfig.clearTurretTargetAfterFiring) {
            this.movementLevels[i].targetUnit = null;
        }
        if (this.unitConfig.dieOnAttack && !this.isDead) {
            bv();
        }
        if (this.unitConfig.removeOnAttack && !this.isDead) {
            remove();
            this.isDead = true;
        }
        return projectileA;
    }

    static Projectile findReusableProjectile(
            Projectile[] cachedProjectiles,
            int turretIndex,
            CustomProjectileTemplate projectileTemplate
    ) {
        Projectile turretProjectile = cachedProjectiles[turretIndex];
        if (turretProjectile != null && !turretProjectile.isDestroyed) {
            return turretProjectile;
        }
        if (!projectileTemplate.reuseAcrossTurrets) {
            return null;
        }
        for (Projectile candidate : cachedProjectiles) {
            if (candidate != null && !candidate.isDestroyed && candidate.template == projectileTemplate) {
                return candidate;
            }
        }
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float m() {
        return this.y.maxAttackRange;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public int y() {
        if (this.y.nanoRange != -1) {
            return this.y.nanoRange;
        }
        return super.y();
    }

    @Override
    // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f) {
        float fAbs;
        Effect effectCreateSmallExplosion;
        float fA;
        UnitType unitTypeC;
        float fAbs2;
        float fAbs3;
        float f2;
        Effect effectCreateSmokeEffect;
        boolean z = this.frameAnimationReverse;
        if (z) {
            this.frameAnimationReverse = false;
            Object[] objArrA = this.unitConfig.onCreateListeners.a();
            for (int i = this.unitConfig.onCreateListeners.size - 1; i >= 0; i--) {
                ((CustomUnitRenderHook) objArrA[i]).a(this);
            }
            b(UnitEventType.created);
        }
        CustomUnitConfig customUnitConfig = this.unitConfig;
        super.update(f);
        TurretFireCycleObserverRegistry.update(this);
        if (this.isDead && !this.isDeathFallComplete) {
            if (this.posZ > 0.0f) {
                if (customUnitConfig.slowDeathFall && !(this.rotation == 0.0f && this.velocityX == 0.0f && this.velocityY == 0.0f)) {
                    this.frameAnimationDelay += 0.017f * f;
                    this.posZ -= this.frameAnimationDelay * f;
                    PointF pointFN = getMovementDeltaOffset(f);
                    this.posX += pointFN.x;
                    this.posY += pointFN.y;
                    if (customUnitConfig.movementType == UnitMovementType.AIR) {
                        GameEngine gameEngine = GameEngine.getInstance();
                        this.damageEffectTimer2 += f;
                        this.damageEffectTimer += f;
                        if (customUnitConfig.slowDeathFallSmoke && this.damageEffectTimer2 > 9.0f) {
                            this.damageEffectTimer2 = Utility.randomFloatInRange(1.0f, 3.0f);
                            Effect effectCreateEffectInternal = gameEngine.effectManager.createEffectInternal(this.posX, this.posY, this.posZ, EffectType.custom, false, EffectQuality.low);
                            if (effectCreateEffectInternal != null) {
                                effectCreateEffectInternal.stripIndex = 0;
                                effectCreateEffectInternal.frameIndex = 0;
                                effectCreateEffectInternal.drawLayer = (short) 2;
                                effectCreateEffectInternal.fadeIn = true;
                                effectCreateEffectInternal.alpha = 0.5f;
                                effectCreateEffectInternal.lifeMax = 60.0f;
                                effectCreateEffectInternal.lifeTimer = 60.0f;
                                effectCreateEffectInternal.scaleFrom = 0.9f;
                                effectCreateEffectInternal.scaleTo = 1.2f;
                                effectCreateEffectInternal.shadow = false;
                                effectCreateEffectInternal.velocityX = 0.0f;
                                effectCreateEffectInternal.velocityY = 0.0f;
                            }
                        }
                        if (this.damageEffectTimer > 7.0f) {
                            this.damageEffectTimer = 0.0f;
                            Effect effectCreateEffectInternal2 = gameEngine.effectManager.createEffectInternal(this.posX, this.posY, this.posZ, EffectType.custom, false, EffectQuality.verylow);
                            if (effectCreateEffectInternal2 != null) {
                                EffectEmitter.setupAlternateFireEffect(effectCreateEffectInternal2, true);
                                effectCreateEffectInternal2.posX = this.posX;
                                effectCreateEffectInternal2.posY = this.posY;
                                effectCreateEffectInternal2.posZ = this.posZ;
                                effectCreateEffectInternal2.velocityX += Utility.randomFloatInRange(-0.1f, 0.1f) + this.velocityX;
                                effectCreateEffectInternal2.velocityY += Utility.randomFloatInRange(-0.1f, 0.1f) + this.velocityY;
                                effectCreateEffectInternal2.posX += Utility.randomFloatInRange(-4.0f, 4.0f);
                                effectCreateEffectInternal2.posY += Utility.randomFloatInRange(-4.0f, 4.0f);
                            }
                        }
                    }
                } else {
                    this.frameAnimationDelay += customUnitConfig.fallingAccelerationDead * f;
                    this.posZ -= this.frameAnimationDelay * f;
                }
            } else if (!this.hasProcessedDeathGroundCollision) {
                this.hasProcessedDeathGroundCollision = true;
                if (customUnitConfig.slowDeathFall) {
                    ds();
                    S(0);
                    if (customUnitConfig.image_wreak != null) {
                        this.baseTexture = customUnitConfig.image_wreak;
                        this.animationFrameIndex = 0;
                        this.ew = true;
                    } else {
                        removeFromGame();
                    }
                }
                if (this.frameAnimationDelay > 0.5d) {
                    if (customUnitConfig.effectOnDeathGroundCollision != null && customUnitConfig.effectOnDeathGroundCollision.a()) {
                        customUnitConfig.effectOnDeathGroundCollision.a(this.posX, this.posY, this.posZ, this.rotationSpeed, null);
                    }
                    if (isOverLiquid()) {
                        if (customUnitConfig.explodeOnDeathGroundCollision) {
                            a(UnitSize.verysmall);
                        }
                        if (isOverWater()) {
                            GameEngine.getInstance().effectManager.createDirectedExplosion(this.posX, this.posY, 0.0f, 0, 0.0f, 0.0f, this.rotationSpeed);
                        }
                    } else if (customUnitConfig.explodeOnDeathGroundCollision) {
                        a(UnitSize.small);
                    }
                }
                this.frameAnimationDelay = 0.0f;
            } else if (!isOverLiquid()) {
                this.posZ = 0.0f;
                this.isDeathFallComplete = true;
            } else if (this.posZ > -10.0f) {
                this.frameAnimationDelay += 8.0E-4f * f;
                this.posZ -= this.frameAnimationDelay * f;
                if (isOverWater()) {
                    this.t += f;
                    if (this.t > 30.0f) {
                        this.t = 0.0f;
                        if (isVisibleOnScreen() && (effectCreateSmokeEffect = GameEngine.getInstance().effectManager.createSmokeEffect(this.posX, this.posY, this.posZ, this.rotationSpeed)) != null) {
                            effectCreateSmokeEffect.velocityX = 0.0f;
                            effectCreateSmokeEffect.velocityY = -0.1f;
                        }
                    }
                }
            } else {
                this.isDeathFallComplete = true;
            }
        }
        if (this.isDead) {
            return;
        }
        GameEngine gameEngine2 = GameEngine.getInstance();
        if (customUnitConfig.selfBuildRate != 0.0f && this.buildProgress < 1.0f) {
            float f3 = this.buildProgress + (customUnitConfig.selfBuildRate * f);
            if (f3 >= 1.0f) {
                PlayerTeam.b((BaseUnit) this);
                this.buildProgress = 1.0f;
                this.paidBuildProgress = 1.0f;
                PlayerTeam.c(this);
            } else {
                this.buildProgress = f3;
                this.paidBuildProgress = f3;
            }
        }
        if (!isAlive()) {
            if (this.buildProgress < 1.0f) {
                if (customUnitConfig.underConstructionAnimation != null) {
                    this.animationController.a(customUnitConfig.underConstructionAnimation, 8);
                    this.animationController.a(f);
                } else if (customUnitConfig.underConstructionWithLinkedBuiltTimeAnimation != null) {
                    this.animationController.a(customUnitConfig.underConstructionWithLinkedBuiltTimeAnimation, 99);
                    this.animationController.currentTime = this.buildProgress;
                    this.animationController.speedMultiplier = 0.0f;
                    this.animationController.a(f);
                }
            }
            Object[] objArrA2 = customUnitConfig.onCreateListeners.a();
            for (int i2 = customUnitConfig.onCreateListeners.size - 1; i2 >= 0; i2--) {
                ((CustomUnitRenderHook) objArrA2[i2]).a(this, f);
            }
            boolean z2 = true;
            if ((this.buildProgress < 1.0f && !customUnitConfig.autoTriggerCheckWhileNotBuilt) || (this.transportContainer != null && !customUnitConfig.canBeBuiltBy)) {
                z2 = false;
            }
            if (z2) {
                b(f, z);
                CustomUnitConfig customUnitConfig2 = this.unitConfig;
                return;
            }
            return;
        }
        Object[] objArrA3 = customUnitConfig.onCreateListeners.a();
        for (int i3 = customUnitConfig.onCreateListeners.size - 1; i3 >= 0; i3--) {
            ((CustomUnitRenderHook) objArrA3[i3]).b(this, f);
        }
        if (this.frameAnimationPlaying) {
            this.frameAnimationPlaying = false;
            a(UnitEventType.completeAndActive);
        }
        if (this.y.selfRegenRate != 0.0f && (this.currentHealth < this.maxHealth || this.y.selfRegenRate < 0.0f)) {
            this.currentHealth += this.y.selfRegenRate * f;
            if (this.currentHealth > this.maxHealth) {
                this.currentHealth = this.maxHealth;
            }
        }
        if (this.y.maxEnergy != 0.0f) {
            if (!this.v) {
                f2 = customUnitConfig.energyRegen;
            } else {
                f2 = customUnitConfig.energyRegenWhenRecharging;
            }
            if (this.currentEnergy < this.y.maxEnergy || f2 < 0.0f) {
                this.currentEnergy += f2 * f;
            }
            if (this.currentEnergy >= this.y.maxEnergy) {
                this.currentEnergy = this.y.maxEnergy;
                this.v = false;
            }
            if (this.currentEnergy <= 0.0f) {
                if (customUnitConfig.dieOnZeroEnergy) {
                    bv();
                    return;
                }
                this.currentEnergy = 0.0f;
            }
        }
        if (this.unitEnergyMax != 0.0f) {
            if (this.y.shieldRegen != 0.0f) {
                this.shield += this.y.shieldRegen * f;
                if (this.shield > this.unitEnergyMax) {
                    this.shield = this.unitEnergyMax;
                }
            }
            if (this.shield < 0.0f) {
                this.shield = 0.0f;
            }
            if (this.unitShieldMax != 0.0f) {
                this.unitShieldMax -= (this.unitShieldMax * 0.02f) * f;
                this.unitShieldMax = Utility.distanceSq(this.unitShieldMax, 0.0f, customUnitConfig.shieldDeflectionDisplayRate * f);
                if (this.unitShieldMax > 50.0f) {
                    this.unitShieldMax = 50.0f;
                }
            }
        }
        this.factoryQueueManager.a(f);
        CustomUnitConfig customUnitConfig3 = this.unitConfig;
        if (customUnitConfig3.isBuilder) {
            BuilderUnit.updateTargetPriorityPoints(f, this);
        }
        if (customUnitConfig3.hasGenerationCondition) {
            this.o += f;
            if (this.o > customUnitConfig3.generationDelay - 0.1f) {
                this.o -= customUnitConfig3.generationDelay;
                boolean z3 = customUnitConfig3.generationActive.read(this);
                if (this.p != z3) {
                    PlayerTeam.a((BaseUnit) this);
                    this.p = z3;
                    PlayerTeam.c(this);
                }
                if (this.p) {
                    customUnitConfig3.generationTemplate.g(this);
                }
            }
        }
        if (customUnitConfig3.updateUnitMemoryWriter != null) {
            this.q += f;
            if (this.q >= customUnitConfig3.updateUnitMemoryRate) {
                this.q = 0.0f;
                customUnitConfig3.updateUnitMemoryWriter.writeToUnit(this);
            }
        }
        if (this.isMoving) {
            this.animationController.a(customUnitConfig3.movingAnimation, 3);
        } else if (!this.animationController.animationPlaying || this.animationController.currentAnimation == customUnitConfig3.idleAnimation) {
            this.animationController.a(customUnitConfig3.idleAnimation, 2);
        }
        if (customUnitConfig3.hasMovementEffects) {
            if (customUnitConfig3.splashEffect && gameEngine2.shouldDrawMediumDetailEffects) {
                if (this.rotation > 0.0f || (this.rotation < 0.0f && customUnitConfig3.splashEffectReverse)) {
                    this.t += f;
                }
                if (this.t > 10.0f) {
                    this.t = 0.0f;
                    if (this.shouldDraw && isOverWater()) {
                        float f4 = this.rotationSpeed + 180.0f;
                        if (this.rotation < 0.0f) {
                            f4 += 180.0f;
                        }
                        float f5 = this.radius - 6.0f;
                        if (f5 < 4.0f) {
                            f5 = 4.0f;
                        }
                        gameEngine2.effectManager.createSmokeEffect(this.posX + (Utility.fastCos(f4) * f5), this.posY + (Utility.fastSin(f4) * f5), 0.0f, f4);
                    }
                }
            }
            if (gameEngine2.shouldDrawHighDetailEffects && ((this.rotation > 0.0f || this.rotation < 0.0f) && this.shouldDraw)) {
                this.frameAnimationSpeed += f;
                if (this.frameAnimationSpeed > customUnitConfig3.movementEffectRate) {
                    this.frameAnimationSpeed = 0.0f;
                    if (this.rotation > 0.0f) {
                        if (customUnitConfig3.movementEffect != null) {
                            customUnitConfig3.movementEffect.a(this.posX, this.posY, this.posZ, this.rotationSpeed, this);
                        }
                    } else if (customUnitConfig3.movementEffectReverse != null) {
                        float f6 = this.rotationSpeed;
                        if (customUnitConfig3.movementEffectReverseFlipEffects) {
                            f6 += 180.0f;
                        }
                        customUnitConfig3.movementEffectReverse.a(this.posX, this.posY, this.posZ, f6, this);
                    }
                    if (customUnitConfig3.dustEffect && ((this.rotation > 0.0f || customUnitConfig3.dustEffectReverse) && !isOverWater())) {
                        int i4 = 0;
                        while (i4 <= 1) {
                            float f7 = i4 == 0 ? -20 : 20;
                            float f8 = this.rotationSpeed + 180.0f;
                            if (this.rotation < 0.0f) {
                                f7 += 180.0f;
                                f8 += 180.0f;
                            }
                            Effect effectCreateMuzzleFlash = gameEngine2.effectManager.createMuzzleFlash(this.posX + (Utility.fastCos(this.rotationSpeed + 180.0f + f7) * (this.radius - 1.0f)), this.posY + (Utility.fastSin(this.rotationSpeed + 180.0f + f7) * (this.radius - 1.0f)), this.posZ, f8 + Utility.randomFloatInRange(-7.0f, 7.0f), 0);
                            if (effectCreateMuzzleFlash != null) {
                                effectCreateMuzzleFlash.velocityX += Utility.randomFloatInRange(-0.15f, 0.15f);
                                effectCreateMuzzleFlash.velocityY += Utility.randomFloatInRange(-0.15f, 0.15f);
                            }
                            i4++;
                        }
                    }
                }
            }
        }
        if (customUnitConfig3.maxTransportingUnits > 0) {
            if (customUnitConfig3.transportUnitsHealBy != 0.0f && this.transportedUnits.size > 0) {
                Object[] objArrA4 = this.transportedUnits.a();
                for (int i5 = 0; i5 < this.transportedUnits.size; i5++) {
                    BaseUnit baseUnit = (BaseUnit) objArrA4[i5];
                    if (baseUnit.currentHealth < baseUnit.maxHealth || customUnitConfig3.transportUnitsHealBy < 0.0f) {
                        baseUnit.currentHealth += customUnitConfig3.transportUnitsHealBy * f;
                        if (baseUnit.currentHealth > baseUnit.maxHealth) {
                            baseUnit.currentHealth = baseUnit.maxHealth;
                        }
                    }
                }
            }
            if (this.isUnloading && customUnitConfig3.transportUnitsCanUnloadUnits.read(this)) {
                this.transportUnloadTimer = Utility.moveTowardsZero(this.transportUnloadTimer, f);
                if (this.transportUnloadTimer == 0.0f) {
                    this.transportUnloadTimer = customUnitConfig3.transportUnitsUnloadDelayBetweenEachUnit;
                    if (this.transportedUnits.size() == 0) {
                        this.isUnloading = false;
                    } else {
                        g(false);
                        if (this.transportedUnits.size() == 0) {
                            this.isUnloading = false;
                        }
                    }
                }
            }
        }
        if (this.isMoving) {
            this.s = 0.0f;
        } else {
            this.s += f;
        }
        if (customUnitConfig3.movementType != UnitMovementType.AIR && this.parentEntity == null) {
            dF();
        }
        if ((customUnitConfig3.movementType == UnitMovementType.OVER_CLIFF || customUnitConfig3.movementType == UnitMovementType.OVER_CLIFF_WATER) && this.isMoving) {
            if (isOverCliff()) {
                this.moveSpeedMultiplier = 0.7f;
            } else {
                this.moveSpeedMultiplier = 1.0f;
            }
        }
        if (customUnitConfig3.movementType == UnitMovementType.AIR) {
            this.frameAnimationTimer += 2.0f * f;
            if (this.frameAnimationTimer > 360.0f) {
                this.frameAnimationTimer -= 360.0f;
            }
            boolean zI = i();
            boolean z4 = false;
            if (customUnitConfig3.landOnGround) {
                boolean zCK = isOverLiquid();
                if (!this.isMoving && !zCK && this.s > 3.0f && (!customUnitConfig3.landOnGroundOnlyIdle || hasNoCurrentWaypoint())) {
                    z4 = true;
                }
            }
            if (this.parentEntity == null) {
                if (z4) {
                    if (customUnitConfig3.heightChangeRate < 0.0f) {
                        fAbs3 = (Utility.abs(this.posZ - 2.0f) * 0.05f * 0.4f) + 0.2f;
                    } else {
                        fAbs3 = customUnitConfig3.heightChangeRate;
                    }
                    this.posZ = Utility.distanceSq(this.posZ, 2.0f, fAbs3 * f);
                } else {
                    float fFastSin = this.y.targetHeight + (Utility.fastSin(this.frameAnimationTimer) * customUnitConfig3.targetHeightDrift);
                    if (customUnitConfig3.heightChangeRate < 0.0f) {
                        fAbs2 = (Utility.abs(this.posZ - 2.0f) * 0.05f * 0.4f) + 0.2f;
                    } else {
                        fAbs2 = customUnitConfig3.heightChangeRate;
                    }
                    this.posZ = Utility.distanceSq(this.posZ, fFastSin, Utility.clamp(fAbs2, (Utility.abs(this.posZ - fFastSin) * 0.05f * 0.3f) + 0.1f) * f);
                }
                if (zI != i()) {
                    this.movementActiveThisFrame = true;
                    dF();
                }
            }
        } else {
            float f9 = this.y.targetHeight - customUnitConfig3.targetHeightDrift;
            if (this.posZ < f9) {
                this.posZ += 0.2f * f;
                if (this.posZ >= f9) {
                    this.posZ = f9;
                }
            }
            if ((this.y.targetHeight != 0.0f || customUnitConfig3.targetHeightDrift != 0.0f || this.posZ > 0.0f) && this.parentEntity == null) {
                float fFastSin2 = this.y.targetHeight;
                if (customUnitConfig3.targetHeightDrift != 0.0f) {
                    this.frameAnimationTimer += 2.0f * f;
                    if (this.frameAnimationTimer > 360.0f) {
                        this.frameAnimationTimer -= 360.0f;
                    }
                    fFastSin2 += Utility.fastSin(this.frameAnimationTimer) * customUnitConfig3.targetHeightDrift;
                }
                if (customUnitConfig3.heightChangeRate < 0.0f) {
                    fAbs = (Utility.abs(this.posZ - 2.0f) * 0.05f * 0.4f) + 0.2f;
                } else {
                    fAbs = customUnitConfig3.heightChangeRate;
                }
                this.posZ = Utility.distanceSq(this.posZ, fFastSin2, Utility.clamp(fAbs, (Utility.abs(this.posZ - fFastSin2) * 0.05f * 0.3f) + 0.1f) * f);
                boolean z5 = false;
                if (this.posZ > this.y.targetHeight + customUnitConfig3.targetHeightDrift + 1.0f) {
                    this.frameAnimationDelay += customUnitConfig3.fallingAcceleration * f;
                    if (this.posZ < 0.0f) {
                        this.frameAnimationDelay = Utility.clamp(this.frameAnimationDelay, 0.2f);
                    }
                    this.posZ -= this.frameAnimationDelay * f;
                    if (this.frameAnimationDelay > 1.5d) {
                        this.damageEffectTimer2 += f;
                        if (this.damageEffectTimer2 > 0.5d) {
                            this.damageEffectTimer2 = 0.0f;
                            Effect effectCreateEffectInternal3 = gameEngine2.effectManager.createEffectInternal(this.posX + Utility.randomFloatInRange(-this.radius, this.radius), this.posY + Utility.randomFloatInRange(-this.radius, this.radius), this.posZ, EffectType.custom, false, EffectQuality.high);
                            if (effectCreateEffectInternal3 != null) {
                                effectCreateEffectInternal3.stripIndex = 0;
                                effectCreateEffectInternal3.frameIndex = 0;
                                effectCreateEffectInternal3.drawLayer = (short) 2;
                                effectCreateEffectInternal3.fadeIn = true;
                                effectCreateEffectInternal3.fadeOut = true;
                                effectCreateEffectInternal3.fadeDuration = 40.0f;
                                effectCreateEffectInternal3.isCentered = true;
                                effectCreateEffectInternal3.velocityX = 0.1f;
                                effectCreateEffectInternal3.velocityZ = 0.0f;
                                effectCreateEffectInternal3.useGravity = true;
                                effectCreateEffectInternal3.alpha = 0.4f;
                                effectCreateEffectInternal3.lifeMax = 380.0f;
                                effectCreateEffectInternal3.lifeTimer = effectCreateEffectInternal3.lifeMax;
                                effectCreateEffectInternal3.scaleFrom = 0.8f;
                                effectCreateEffectInternal3.scaleTo = 1.7f;
                                effectCreateEffectInternal3.shadow = false;
                                effectCreateEffectInternal3.velocityX += Utility.randomFloatInRange(-0.04f, 0.04f);
                                effectCreateEffectInternal3.velocityY += Utility.randomFloatInRange(-0.04f, 0.04f);
                            }
                        }
                    }
                    if (this.posZ <= this.y.targetHeight + customUnitConfig3.targetHeightDrift + 1.0f) {
                        if (this.frameAnimationDelay > 2.0f) {
                            z5 = true;
                        }
                        if (this.posZ < this.y.targetHeight + customUnitConfig3.targetHeightDrift) {
                            this.posZ = this.y.targetHeight + customUnitConfig3.targetHeightDrift;
                        }
                        this.frameAnimationDelay = 0.0f;
                    }
                } else {
                    if (this.frameAnimationDelay > 2.0f) {
                        z5 = true;
                    }
                    this.frameAnimationDelay = 0.0f;
                }
                if (z5 && (effectCreateSmallExplosion = gameEngine2.effectManager.createSmallExplosion(this.posX, this.posY, this.posZ, 0)) != null) {
                    effectCreateSmallExplosion.scaleFrom = 0.8f;
                    effectCreateSmallExplosion.scaleTo = 1.4f;
                    effectCreateSmallExplosion.lifeTimer = 60.0f;
                    effectCreateSmallExplosion.lifeMax = effectCreateSmallExplosion.lifeTimer;
                }
            }
        }
        boolean z6 = false;
        boolean z7 = false;
        if (customUnitConfig3.isFactory) {
            z7 = true;
        }
        if (this.factoryUnitConfig != null && this.factoryUnitConfig.isFactory) {
            z7 = true;
        }
        if (z7) {
            AbstractUnitAction abstractUnitActionD = this.factoryQueueManager.d();
            boolean zL = false;
            if (abstractUnitActionD != null) {
                if (abstractUnitActionD instanceof CustomAction) {
                    CustomAction customAction = (CustomAction) abstractUnitActionD;
                    CustomActionDef customActionDef = customAction.actionDef;
                    boolean z8 = false;
                    zL = customAction.L();
                    if (customActionDef.whenBuildingTemporarilyConvertTo != null && (unitTypeC = customActionDef.whenBuildingTemporarilyConvertTo.c()) != null && (unitTypeC instanceof CustomUnitConfig)) {
                        z6 = true;
                        if (unitTypeC != customUnitConfig3) {
                            if (this.factoryUnitConfig != null) {
                                PlayerTeam.b((BaseUnit) this);
                                a(this.factoryUnitConfig, false, false, this.attachmentPoints);
                                this.factoryUnitConfig = null;
                                this.attachmentPoints = null;
                                customUnitConfig3 = this.unitConfig;
                                PlayerTeam.c(this);
                            }
                            PlayerTeam.b((BaseUnit) this);
                            this.factoryUnitConfig = customUnitConfig3;
                            this.attachmentPoints = customActionDef.whenBuildingTemporarilyConvertToKeepFields;
                            a((CustomUnitConfig) unitTypeC, false, false, customActionDef.whenBuildingTemporarilyConvertToKeepFields);
                            customUnitConfig3 = this.unitConfig;
                            PlayerTeam.c(this);
                        }
                    }
                    if (customActionDef.whenBuildingRotateTo != null) {
                        float fFloatValue = customActionDef.whenBuildingRotateTo.floatValue();
                        if (customActionDef.whenBuildingRotateToAimAtActionTarget) {
                            float f10 = this.posX;
                            float f11 = this.posY;
                            com.corrodinggames.rts.game.units.buildings.Projectile projectileB = this.factoryQueueManager.b();
                            if (projectileB != null) {
                                float f12 = Float.MIN_VALUE;
                                float f13 = Float.MIN_VALUE;
                                if (projectileB.targetUnit != null) {
                                    f12 = projectileB.targetUnit.posX;
                                    f13 = projectileB.targetUnit.posY;
                                } else if (projectileB.targetPoint != null) {
                                    f12 = projectileB.targetPoint.x;
                                    f13 = projectileB.targetPoint.y;
                                }
                                if (f12 > Float.MIN_VALUE) {
                                    fFloatValue += Utility.getAngleBetweenPoints(f10, f11, f12, f13);
                                }
                            }
                        }
                        if (customActionDef.whenBuildingRotateToRotateTurretX == null) {
                            fA = rotateToward(f, fFloatValue, true, customActionDef.whenBuildingRotateToOrBackwards);
                        } else {
                            int i6 = customActionDef.whenBuildingRotateToRotateTurretX.turretIndex;
                            fA = a(f, fFloatValue, i6);
                            UnitMovementData unitMovementData = this.movementLevels[i6];
                            unitMovementData.b(5);
                            unitMovementData.targetY = unitMovementData.targetX;
                        }
                        if (customActionDef.whenBuildingRotateToWaitTillRotated && Utility.abs(fA) > 5.0f) {
                            z8 = true;
                        }
                    }
                    if (customActionDef.whenBuildingPlayAnimation != null && !z8) {
                        this.animationController.a(customActionDef.whenBuildingPlayAnimation.b(), 10);
                    }
                    if (z8) {
                        this.factoryQueueManager.e = 0.0f;
                    }
                }
                if (customUnitConfig3.queuedUnitsAnimation != null && abstractUnitActionD.getUnitType() != null && this.factoryQueueManager.e >= customUnitConfig3.queuedUnitsAnimation.q) {
                    this.animationController.a(customUnitConfig3.queuedUnitsAnimation, 5);
                }
            }
            this.frameAnimationLooping = zL;
            if (this.frameAnimationLooping) {
                this.velocityX = 0.0f;
                this.velocityY = 0.0f;
                this.rotation = 0.0f;
            }
        }
        if (this.factoryUnitConfig != null && !z6) {
            PlayerTeam.b((BaseUnit) this);
            a(this.factoryUnitConfig, false, false, this.attachmentPoints);
            this.factoryUnitConfig = null;
            this.attachmentPoints = null;
            CustomUnitConfig customUnitConfig4 = this.unitConfig;
            PlayerTeam.c(this);
        }
        this.animationController.a(f);
        b(f, z);
        CustomUnitConfig customUnitConfig5 = this.unitConfig;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: u */
    public int getRepairRange(BaseUnit baseUnit) {
        int iA = baseUnit.r().a(this);
        if (this.unitConfig.nanoRangeForRepair != -1) {
            if (this.unitConfig.nanoRangeForRepairIsMelee) {
                int i = (int) (this.unitConfig.nanoRangeForRepair + this.radius);
                if (baseUnit != null) {
                    i = (int) (i + baseUnit.radius);
                }
                return i + iA;
            }
            return this.unitConfig.nanoRangeForRepair + iA;
        }
        return y() + iA;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean w(BaseUnit baseUnit) {
        return this.unitConfig.nanoRangeForReclaimIsMelee;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean x(BaseUnit baseUnit) {
        return this.unitConfig.nanoRangeForRepairIsMelee;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cx */
    public float getNanoFactorySpeed() {
        return this.y.nanoFactorySpeed;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public float c(BaseUnit baseUnit) {
        return this.unitConfig.nanoRepairSpeed;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: z */
    public float calculateUnitSpeed(BaseUnit baseUnit) {
        float unitHealthPercent = this.unitConfig.nanoReclaimSpeed;
        if (baseUnit.getResourceRate() > 0.0f) {
            unitHealthPercent = baseUnit.getResourceRate() * this.unitConfig.resourceReclaimMultiplier;
        }
        return unitHealthPercent;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: f */
    public float getReclaimRate(BaseUnit baseUnit) {
        return this.unitConfig.nanoUnbuildSpeed;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: b */
    public float getDistanceToTarget(BaseUnit baseUnit) {
        return this.unitConfig.nanoBuildSpeed;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: z */
    public float getMoveSpeed() {
        return this.y.moveSpeed * this.moveSpeedMultiplier;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: aZ */
    public float getMoveYAxisScaling() {
        return this.unitConfig.moveYAxisScaling;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: ba */
    public float getMoveYAxisScalingInverse() {
        return this.unitConfig.inverseMoveYAxisScaling;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: A */
    public float getMaxTurnSpeed() {
        return this.y.maxTurnSpeed;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float c(int i) {
        TurretConfig turretConfig = this.unitConfig.turrets[i];
        if (turretConfig.turnSpeed != null) {
            return turretConfig.turnSpeed.floatValue();
        }
        return this.unitConfig.turretTurnSpeed;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float x(int i) {
        return this.unitConfig.turrets[i].canAttackMaxAngle;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float w(int i) {
        return this.unitConfig.turrets[i].turnSpeedAcceleration;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float y(int i) {
        return this.unitConfig.turrets[i].turnSpeedDeceleration;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float B() {
        return this.unitConfig.turnAcceleration;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cD */
    public float getRenderScale() {
        return this.unitConfig.imageScale * this.currentFrameTime;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float p(int i) {
        return this.unitConfig.turretImageScale;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: d */
    public float getRenderRotation(boolean z) {
        if (!this.unitConfig.rotateWithDirection) {
            return 0.0f;
        }
        if (z && this.unitConfig.lockShadowRotationWithMainTurret) {
            return this.movementLevels[this.unitConfig.mainTurretIndex].targetX + 90.0f;
        }
        if (this.unitConfig.lockBodyRotationWithMainTurret) {
            return this.movementLevels[this.unitConfig.mainTurretIndex].targetX + 90.0f;
        }
        return super.getRenderRotation(z);
    }

    static {
        for (int i = 0; i < 10; i++) {
            dX.add(new CustomUnitEventData());
        }
        dY = true;
        dZ = new HashMap();
        ea = 0;
        eb = VariableScope.nullOrMissingString;
        ec = new PointF();
        ee = new Vector3D();
        ef = new PointF();
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cY */
    public PointF getRenderOffset() {
        PointF pointF = H;
        CustomUnitConfig customUnitConfig = this.unitConfig;
        if (customUnitConfig.lockBodyRotationWithMainTurret && this.movementLevels[customUnitConfig.mainTurretIndex].rotation != 0.0f && customUnitConfig.mainTurret.p != 0.0f) {
            pointF.a(G(customUnitConfig.mainTurretIndex));
            pointF.b(-this.posX, -this.posY);
            return pointF;
        }
        pointF.x = 0.0f;
        pointF.y = 0.0f;
        return pointF;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: aP */
    public PointF getShadowOffset() {
        CustomUnitConfig customUnitConfig = this.unitConfig;
        PointF unitAIPosition = getRenderOffset();
        I.x = unitAIPosition.x + customUnitConfig.shadowOffsetX;
        I.y = unitAIPosition.y + customUnitConfig.shadowOffsetY;
        return I;
    }

    @Override
    // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public boolean c(float f) {
        Texture textureT;
        float fClamp;
        float f2;
        CustomUnitConfig customUnitConfig = this.unitConfig;
        boolean z = this.isDead;
        if (this.legInstances != null && !z) {
            CustomUnitLegController.a(this, f, false, false);
        }
        GameEngine gameEngine = GameEngine.getInstance();
        GraphicsEngine graphicsEngine = gameEngine.renderGraphicsEngine;
        Paint renderPaint = getRenderPaint();
        float maxHealth = getRenderScale();
        PointF unitAIPosition = getRenderOffset();
        drawShadow();
        RenderRegistry.drawUnit(this, gameEngine, UnitRenderLayer.UNDER_UNIT);
        int i = customUnitConfig.onDestroyListeners.size;
        if (i > 0) {
            Object[] objArrA = customUnitConfig.onDestroyListeners.a();
            for (int i2 = i - 1; i2 >= 0; i2--) {
                ((CustomUnitRenderHook) objArrA[i2]).d(this, f);
            }
        }
        if (!RenderRegistry.drawUnit(this, gameEngine, UnitRenderLayer.BODY)) {
        if (this.ew) {
            float f3 = (this.posX + unitAIPosition.x) - gameEngine.viewpointXSnapped;
            float f4 = ((this.posY + unitAIPosition.y) - gameEngine.viewpointYSnapped) - this.posZ;
            if (maxHealth != 1.0f) {
                graphicsEngine.k();
                graphicsEngine.a(maxHealth, maxHealth, f3, f4);
            }
            graphicsEngine.a(this.baseTexture, f3, f4, getRenderRotation(false) - 90.0f, renderPaint);
            if (maxHealth != 1.0f) {
                graphicsEngine.l();
            }
        } else {
            RectF unitBounds = getUnitBounds();
            float f5 = unitAIPosition.x;
            float f6 = unitAIPosition.y - this.posZ;
            unitBounds.a += f5;
            unitBounds.b += f6;
            unitBounds.c += f5;
            unitBounds.d += f6;
            Rect rectA_ = a_(false);
            float f7 = (unitBounds.a + unitBounds.c) * 0.5f;
            float f8 = (unitBounds.b + unitBounds.d) * 0.5f;
            graphicsEngine.k();
            if (maxHealth != 1.0f) {
                graphicsEngine.a(maxHealth, maxHealth, f7, f8);
            }
            graphicsEngine.a(getRenderRotation(false), f7, f8);
            graphicsEngine.a(this.baseTexture, rectA_, unitBounds, renderPaint);
            graphicsEngine.l();
        }
        }
        if (i > 0) {
            Object[] objArrA2 = customUnitConfig.onDestroyListeners.a();
            for (int i3 = i - 1; i3 >= 0; i3--) {
                ((CustomUnitRenderHook) objArrA2[i3]).e(this, f);
            }
        }
        RenderRegistry.drawUnit(this, gameEngine, UnitRenderLayer.OVER_UNIT);
        RenderRegistry.drawPreFire(this, gameEngine);
        RenderRegistry.drawPostFire(this, gameEngine);
        GameViewUtils.a((OrderableUnit) this);
        if (this.legInstances != null && !z && customUnitConfig.drawLegsOverBody) {
            CustomUnitLegController.a(this, f, true, false);
        }
        if (canMove() && customUnitConfig.mainNanoTurret != null && !z) {
            float fE = this.movementLevels[customUnitConfig.mainNanoTurret.turretIndex].speed / e(customUnitConfig.mainNanoTurret.turretIndex);
            if (fE != 0.0f) {
                boolean z2 = true;
                boolean zY = isCurrentCommandReclaim();
                if (zY && customUnitConfig.hasReclaimEffect) {
                    z2 = false;
                } else if (!zY && customUnitConfig.showActionsAndWaypoints) {
                    z2 = false;
                }
                if (z2 && customUnitConfig.turrets[customUnitConfig.mainNanoTurret.turretIndex].chargeEffectImage == null) {
                    Vector3D vector3DBn = bn();
                    gameEngine.renderGraphicsEngine.k();
                    gameEngine.renderGraphicsEngine.b(vector3DBn.x - gameEngine.viewpointXSnapped, ((vector3DBn.y - vector3DBn.z) - gameEngine.viewpointYSnapped) - this.posZ);
                    gameEngine.renderGraphicsEngine.a(fE, fE);
                    if (zY) {
                        gameEngine.renderGraphicsEngine.a(BuilderUnit.builderDechargeTexture, 0.0f, 0.0f, (Paint) null);
                    } else {
                        gameEngine.renderGraphicsEngine.a(BuilderUnit.builderChargeTexture, 0.0f, 0.0f, (Paint) null);
                    }
                    gameEngine.renderGraphicsEngine.l();
                }
            }
        }
        if (customUnitConfig.hasTurretChargeEffectImage && !z) {
            int techLevel = getTechLevel();
            for (int i4 = 0; i4 < techLevel; i4++) {
                float fE2 = this.movementLevels[i4].speed / e(i4);
                TurretConfig turretConfig = customUnitConfig.turrets[i4];
                if (turretConfig != null && fE2 != 0.0f && turretConfig.chargeEffectImage != null) {
                    GameViewUtils.a(this, turretConfig.chargeEffectImage, fE2, i4);
                }
            }
        }
        if (!z && this.shield > 0.0f && this.energy == 0.0f && (textureT = T()) != null) {
            if (!customUnitConfig.shieldDisplayOnlyDeflection) {
                fClamp = 0.0f + 0.09f + ((this.shield / this.unitEnergyMax) * 0.4f) + ((Utility.clamp(this.unitShieldMax, 50.0f) / 50.0f) * 0.5f);
            } else {
                float fClamp2 = 0.0f + ((Utility.clamp(this.unitShieldMax, 50.0f) / 50.0f) * 0.5f);
                float f9 = this.unitShieldMax;
                if (f9 > 5.0f) {
                    f9 = 5.0f;
                }
                fClamp = fClamp2 + ((f9 / 5.0f) * 0.2f);
            }
            if (fClamp > 0.0f) {
                if (fClamp > 1.0f) {
                    fClamp = 1.0f;
                }
                if (this.J == null) {
                    this.J = GameViewUtils.a();
                }
                Paint paint = this.J;
                paint.a((int) (fClamp * 255.0f), 255, 255, 255);
                float f10 = this.posX - gameEngine.viewpointXSnapped;
                float f11 = (this.posY - gameEngine.viewpointYSnapped) - this.posZ;
                if (!customUnitConfig.hasCustomShieldImage) {
                    f2 = ((customUnitConfig.shieldRenderRadius * 2) / 87.0f) * 1.25f;
                } else {
                    f2 = ((customUnitConfig.shieldRenderRadius * 2) / textureT.p) * 1.25f;
                }
                gameEngine.renderGraphicsEngine.k();
                gameEngine.renderGraphicsEngine.a(f2, f2, f10, f11);
                gameEngine.renderGraphicsEngine.a(textureT, f10, f11, getRenderRotation(false) - 90.0f, paint);
                gameEngine.renderGraphicsEngine.l();
            }
        }
        if (i > 0) {
            Object[] objArrA3 = customUnitConfig.onDestroyListeners.a();
            for (int i5 = i - 1; i5 >= 0; i5--) {
                ((CustomUnitRenderHook) objArrA3[i5]).c(this, f);
            }
            return true;
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture T() {
        return this.unitConfig.shieldTexture;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: C */
    public float getMoveAccelerationSpeed() {
        return this.unitConfig.moveAccelerationSpeed;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float D() {
        return this.unitConfig.moveDecelerationSpeed;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: bi */
    public boolean isSlidingMovement() {
        return this.unitConfig.moveSlidingMode;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: bj */
    public boolean isIgnoreMoveOrders() {
        return this.unitConfig.moveIgnoringBody;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: l */
    public boolean canAttack() {
        return this.unitConfig.canAttack;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: ag */
    public boolean canAttackSurfaceUnits() {
        return this.unitConfig.canAttackLandUnits.read(this);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: af */
    public boolean canAttackFlyingUnits() {
        return this.unitConfig.canAttackFlyingUnits.read(this);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: ae */
    public boolean canAttackSubmergedUnits() {
        return this.unitConfig.canAttackUnderwaterUnits.read(this);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean ah() {
        if (this.unitConfig.canAttackNotTouchingWaterUnits == null) {
            return true;
        }
        return this.unitConfig.canAttackNotTouchingWaterUnits.read(this);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: k */
    public boolean canAttackUnitType(BaseUnit baseUnit) {
        CustomUnitConfig customUnitConfig = this.unitConfig;
        if (customUnitConfig.hasAttackTagRestrictions) {
            if (customUnitConfig.canOnlyAttackUnitsWithTags != null && !AnimationTag.a(customUnitConfig.canOnlyAttackUnitsWithTags, baseUnit.getTags())) {
                return false;
            }
            if (customUnitConfig.canOnlyAttackUnitsWithoutTags != null && AnimationTag.a(customUnitConfig.canOnlyAttackUnitsWithoutTags, baseUnit.getTags())) {
                return false;
            }
            if (customUnitConfig.allFiringTurretsHaveTagRestrictions) {
                boolean z = false;
                for (int i = 0; i < customUnitConfig.turrets.length; i++) {
                    TurretConfig turretConfig = customUnitConfig.turrets[i];
                    if ((turretConfig.canOnlyAttackUnitsWithoutTags == null || !AnimationTag.a(turretConfig.canOnlyAttackUnitsWithoutTags, baseUnit.getTags())) && (turretConfig.canOnlyAttackUnitsWithTags == null || AnimationTag.a(turretConfig.canOnlyAttackUnitsWithTags, baseUnit.getTags()))) {
                        z = true;
                        break;
                    }
                }
                if (!z) {
                    return false;
                }
            }
        }
        if (baseUnit.i()) {
            return canAttackFlyingUnits();
        }
        if (baseUnit.Q()) {
            return canAttackSubmergedUnits();
        }
        if (!ah() && !baseUnit.isTouchingWater()) {
            return false;
        }
        return canAttackSurfaceUnits();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean E() {
        return this.unitConfig.isFixedFiring;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float g(int i) {
        return this.unitConfig.turrets[i].barrelY;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: z */
    public float getTurretTargetSearchRange(int i) {
        return this.unitConfig.turrets[i].limitingRange;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float A(int i) {
        return this.unitConfig.turrets[i].limitingMinRangeSquared;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float B(int i) {
        return this.unitConfig.turrets[i].idleDir;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float C(int i) {
        float f;
        float f2;
        TurretConfig turretConfig = this.unitConfig.turrets[i];
        if (turretConfig.linkedTurretIndex != -1) {
            f = this.movementLevels[turretConfig.linkedTurretIndex].targetX;
        } else {
            f = this.rotationSpeed;
        }
        if (this.isRotating && bc() > 0.95d) {
            f2 = f + turretConfig.idleDirReversing;
        } else {
            f2 = f + turretConfig.idleDir;
        }
        if (turretConfig.idleSpin != 0.0f) {
            return 999.0f;
        }
        return f2;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean bm() {
        return this.unitConfig.turretRotateWithBody;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float q(int i) {
        TurretConfig turretConfig = this.unitConfig.turrets[i];
        if (!turretConfig.canShoot) {
            return 0.0f;
        }
        CustomProjectileTemplate customProjectileTemplate = this.unitConfig.projectileTemplatesById[turretConfig.a(this)];
        float f = 0.0f;
        if (!customProjectileTemplate.s) {
            f = 0.0f + DamageRegistry.directDamageAmount(customProjectileTemplate, customProjectileTemplate.b);
        }
        float f2 = f + DamageRegistry.areaDamageAmount(customProjectileTemplate, customProjectileTemplate.c);
        if (!customProjectileTemplate.ignoreParentShootDamageMultiplier) {
            f2 *= this.y.shootDamageMultiplier;
        }
        return f2;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float b(int i) {
        return this.unitConfig.turrets[i].m * this.y.shootDelayMultiplier;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float e(int i) {
        return this.unitConfig.turrets[i].warmup;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float f(int i) {
        return this.unitConfig.turrets[i].warmupCallDownRate;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean s(int i) {
        return this.unitConfig.turrets[i].warmupNoReset;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float t(int i) {
        TurretConfig turretConfig = this.unitConfig.turrets[i];
        if (turretConfig.warmupShootDelayTransfer == 0.0f || turretConfig.warmup == 0.0f) {
            return 0.0f;
        }
        return -(turretConfig.warmupShootDelayTransfer * (this.movementLevels[i].speed / turretConfig.warmup));
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean r(int i) {
        if (this.unitConfig.turrets[i].canShoot) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void b(BaseUnit baseUnit, int i) {
        TurretConfig turretConfig = this.unitConfig.turrets[i];
        if (turretConfig.warmupStartEffect != null) {
            PointF pointFE = E(i);
            turretConfig.warmupStartEffect.a(pointFE.x, pointFE.y, this.posZ, this.movementLevels[i].targetX, this);
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean u(int i) {
        if (!a(this.unitConfig.turrets[i])) {
            return false;
        }
        return super.u(i);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: s */
    public int getSoftCollisionDivisor(BaseUnit baseUnit) {
        return this.unitConfig.softCollisionOnAll;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean bO() {
        return this.unitConfig.isBio;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean bP() {
        return this.unitConfig.isBug;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: bN */
    public float getPushMass() {
        return this.y.mass;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cG */
    public boolean hasShadowFrames() {
        return this.unitConfig.hasShadowFrames;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public Rect a_(boolean z) {
        float f;
        if (z && !this.unitConfig.hasShadowFrames) {
            return super.a_(z);
        }
        if (this.isDead) {
            return super.a_(z);
        }
        CustomUnitConfig customUnitConfig = this.unitConfig;
        int i = this.animationFrameIndex;
        int i2 = 0;
        CustomUnitDirectionConfig customUnitDirectionConfig = customUnitConfig.directionConfig;
        if (this.animationController.currentAnimation != null && this.animationController.currentAnimation.k != null) {
            customUnitDirectionConfig = this.animationController.currentAnimation.k;
        }
        if (customUnitDirectionConfig != null) {
            float f2 = customUnitDirectionConfig.directionUnits;
            if (f2 < 0.0f) {
                f2 = -f2;
                f = -this.rotationSpeed;
                if (customUnitDirectionConfig.useMainTurret) {
                    f = -this.movementLevels[customUnitConfig.mainTurretIndex].targetX;
                }
            } else {
                f = this.rotationSpeed;
                if (customUnitDirectionConfig.useMainTurret) {
                    f = this.movementLevels[customUnitConfig.mainTurretIndex].targetX;
                }
            }
            int i3 = (int) (360.0f / f2);
            int i4 = ((int) (((f - customUnitDirectionConfig.startingDirection) - (f2 * 0.5f)) / f2)) % i3;
            if (i4 < 0) {
                i4 += i3;
            }
            if (customUnitDirectionConfig.strideX > 0) {
                i += i4 * customUnitDirectionConfig.strideX;
            }
            if (customUnitDirectionConfig.strideY > 0) {
                i2 = 0 + (i4 * customUnitDirectionConfig.strideY);
            }
        }
        if (i >= customUnitConfig.frameColumns) {
            i2 += i / customUnitConfig.frameColumns;
            i %= customUnitConfig.frameColumns;
        }
        return super.a(z, i, i2);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cF */
    public RectF getUnitBounds() {
        RectF rectFCF = super.getUnitBounds();
        if (this.unitConfig.hasImageOffset) {
            rectFCF.a(this.unitConfig.image_offsetX, this.unitConfig.image_offsetY - this.unitConfig.image_offsetH);
        }
        return rectFCF;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: bl */
    public int getTechLevel() {
        if (this.unitConfig == null) {
            return 1;
        }
        return this.unitConfig.turrets.length;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: v */
    public int getLinkedTurretIndex(int i) {
        return this.unitConfig.turrets[i].x;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Vector3D F(int i) {
        return a(i, false);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public PointF G(int i) {
        Vector3D vector3DA = a(i, false);
        K.x = vector3DA.x;
        K.y = vector3DA.y;
        return K;
    }

    public Vector3D a(int i, boolean z) {
        float fFastCos;
        float fFastSin;
        float f;
        float f2;
        TurretConfig turretConfig = this.unitConfig.turrets[i];
        if (turretConfig.linkedTurretIndex == -1) {
            fFastCos = this.posX;
            fFastSin = this.posY;
            f = 0.0f;
            f2 = this.rotationSpeed;
        } else {
            if (z) {
                throw new RuntimeException("Turret can not be attached to turret that is not attached to the body");
            }
            Vector3D vector3DA = a(turretConfig.linkedTurretIndex, true);
            fFastCos = vector3DA.x;
            fFastSin = vector3DA.y;
            f = vector3DA.z;
            f2 = this.movementLevels[turretConfig.linkedTurretIndex].targetX;
        }
        if (this.movementLevels[i].rotation > 0.0f && turretConfig.p != 0.0f) {
            float f3 = 0.0f;
            float fB = (b(i) + t(i)) - this.movementLevels[i].rotation;
            if (fB < turretConfig.q) {
                f3 = (fB / turretConfig.q) * turretConfig.p;
            } else if (fB < turretConfig.q + turretConfig.r) {
                f3 = turretConfig.p - (((fB - turretConfig.q) / turretConfig.r) * turretConfig.p);
            }
            if (f3 != 0.0f) {
                fFastCos += Utility.fastCos(this.movementLevels[i].targetX) * f3;
                fFastSin += Utility.fastSin(this.movementLevels[i].targetX) * f3;
            }
        }
        float f4 = turretConfig.offsetX;
        float f5 = turretConfig.offsetY;
        float f6 = turretConfig.offsetHeight;
        if (f4 != 0.0f || f5 != 0.0f) {
            float fFastSin2 = Utility.fastSin(f2);
            float fFastCos2 = Utility.fastCos(f2);
            fFastCos += (fFastCos2 * f5) - (fFastSin2 * f4);
            fFastSin += ((fFastSin2 * f5) + (fFastCos2 * f4)) * turretConfig.i;
        }
        dK.x = fFastCos;
        dK.y = fFastSin;
        dK.z = f + f6;
        return dK;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: N */
    public ArrayList getAvailableActions() {
        if (this.factoryUnitConfig != null) {
            return this.factoryUnitConfig.a(getUpgradeLevel());
        }
        return this.unitConfig.a(getUpgradeLevel());
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: a */
    public AbstractUnitAction validateActionId(ActionId actionId) {
        CustomUnitConfig customUnitConfig;
        if (this.factoryUnitConfig != null) {
            customUnitConfig = this.factoryUnitConfig;
        } else {
            customUnitConfig = this.unitConfig;
        }
        return customUnitConfig.findActionById(actionId);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: V */
    public int getUpgradeLevel() {
        return this.unitConfig.techLevel;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: e */
    public AbstractUnitAction getUnitAction(UnitType unitType) {
        return this.factoryQueueManager.b(unitType);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void a(AbstractUnitAction abstractUnitAction, boolean z, PointF pointF, BaseUnit baseUnit) {
        if (abstractUnitAction == HovercraftUnit.i) {
            if (!z) {
                startUnloading();
                return;
            } else {
                finishUnloading();
                return;
            }
        }
        if (abstractUnitAction == HovercraftUnit.j) {
            if (!z) {
                finishUnloading();
                return;
            }
            return;
        }
        if (!z) {
            if (pointF != null && !a(abstractUnitAction, pointF.x, pointF.y)) {
                return;
            }
            if (abstractUnitAction instanceof CustomAction) {
                CustomAction customAction = (CustomAction) abstractUnitAction;
                if (customAction.actionDef.playSoundToPlayerOnQueue != null) {
                    GameEngine gameEngine = GameEngine.getInstance();
                    if (this.team == gameEngine.playerTeam && !gameEngine.isMenuBackgroundMapActive()) {
                        customAction.actionDef.playSoundToPlayerOnQueue.a();
                    }
                }
                if (customAction.actionDef.spawnEffectsOnQueue != null) {
                    customAction.actionDef.spawnEffectsOnQueue.a(this.posX, this.posY, this.posZ, this.rotationSpeed, this);
                }
            }
        }
        if (z && (abstractUnitAction instanceof CustomAction) && !((CustomAction) abstractUnitAction).actionDef.canPlayerCancel) {
            return;
        }
        com.corrodinggames.rts.game.units.buildings.Projectile projectileA = this.factoryQueueManager.a(abstractUnitAction, z, pointF, baseUnit);
        if (!z) {
            if (projectileA != null) {
                a(UnitEventType.queueItemAdded, (BaseUnit) null, abstractUnitAction.getAnimationSet(), (VariableScope) null);
                return;
            }
            return;
        }
        if (projectileA != null) {
            a(UnitEventType.queueItemCancelled, (BaseUnit) null, abstractUnitAction.getAnimationSet(), (VariableScope) null);
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: a */
    public void performUnitAction(AbstractUnitAction abstractUnitAction, boolean z) {
        a(abstractUnitAction, z, (PointF) null, (BaseUnit) null);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: b */
    public void stopMoving(AbstractUnitAction abstractUnitAction, boolean z) {
        this.factoryQueueManager.a(abstractUnitAction, z);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void a(AbstractUnitAction abstractUnitAction) {
        this.factoryQueueManager.a(abstractUnitAction);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean a(AbstractUnitAction abstractUnitAction, float f, float f2) {
        if (abstractUnitAction instanceof CustomAction) {
            GameEngine gameEngine = GameEngine.getInstance();
            CustomAction customAction = (CustomAction) abstractUnitAction;
            if (customAction.actionDef.fireTurretAtGroundIndex != null && customAction.actionDef.fireTurretAtGroundOffset == null) {
                if (customAction.actionDef.fireTurretAtGroundIndex.intValue() >= this.unitConfig.turrets.length) {
                    a("checkTargetedActionOrder: " + customAction.actionDef.fireTurretAtGroundIndex + " larger than max turret size");
                    return true;
                }
                if (!a(this.unitConfig.turrets[customAction.actionDef.fireTurretAtGroundIndex.intValue()], customAction.actionDef.fireTurretAtGroundIndex.intValue(), f, f2, true)) {
                    return false;
                }
                if (customAction.actionDef.fireTurretAtGroundTerrainFilter != null && GameViewUtils.a(f, f2, customAction.actionDef.fireTurretAtGroundTerrainFilter)) {
                    if (this.team == gameEngine.playerTeam) {
                        gameEngine.gameUI.showMediumPriorityMessage("Invalid map location (Must be passable by " + customAction.actionDef.fireTurretAtGroundTerrainFilter.name() + ")");
                        return false;
                    }
                    return false;
                }
                return true;
            }
            return true;
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void a(AbstractUnitAction abstractUnitAction, boolean z, float f, float f2) {
        AbstractUnitAction abstractUnitActionValidateActionId;
        if ((abstractUnitAction instanceof CustomAction) && (abstractUnitActionValidateActionId = validateActionId(abstractUnitAction.getActionId())) != null) {
            CustomAction customAction = (CustomAction) abstractUnitActionValidateActionId;
            Integer num = customAction.actionDef.fireTurretAtGroundIndex;
            if (num != null && customAction.actionDef.fireTurretAtGroundOffset == null && num.intValue() < this.unitConfig.turrets.length) {
                TurretConfig turretConfig = this.unitConfig.turrets[num.intValue()];
                if (turretConfig.limitingMinRange > 0.0f) {
                    GameViewUtils.b((BaseUnit) this, turretConfig.limitingMinRange, true);
                }
                GameViewUtils.a((BaseUnit) this, turretConfig.effectiveRange, true, true);
            }
            if (z && customAction.actionDef.fireTurretAtGroundGuideDecals != null) {
                customAction.actionDef.fireTurretAtGroundGuideDecals.a(this, f, f2);
            }
        }
        super.a(abstractUnitAction, z, f, f2);
    }

    public boolean a(AbstractUnitAction abstractUnitAction, PointF pointF, BaseUnit baseUnit, int i, int i2) {
        PointF pointF2 = null;
        BaseUnit baseUnit2 = null;
        int i3 = 0;
        if (i > 0) {
            pointF2 = dM;
            baseUnit2 = dN;
            i3 = dO;
        }
        dM = pointF;
        dN = baseUnit;
        dO = i2;
        boolean zA = a(abstractUnitAction, pointF, baseUnit, i);
        dM = pointF2;
        dN = baseUnit2;
        dO = i3;
        return zA;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public int v(BaseUnit baseUnit) {
        int iA = baseUnit.r().a(this);
        if (this.unitConfig.nanoRangeForReclaim != -1) {
            if (this.unitConfig.nanoRangeForReclaimIsMelee) {
                int i = (int) (this.unitConfig.nanoRangeForReclaim + this.radius);
                if (baseUnit != null) {
                    i = (int) (i + baseUnit.radius);
                }
                return i + iA;
            }
            return this.unitConfig.nanoRangeForReclaim + iA;
        }
        return y() + iA;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface
    public void b(com.corrodinggames.rts.game.units.buildings.Projectile projectile) {
        AbstractUnitAction abstractUnitActionValidateActionId = validateActionId(projectile.j);
        if (abstractUnitActionValidateActionId != null && (abstractUnitActionValidateActionId instanceof CustomAction)) {
            CustomActionDef customActionDef = ((CustomAction) abstractUnitActionValidateActionId).actionDef;
            if (customActionDef.whenBuildingTriggerAction != null) {
                ec.x = this.posX;
                ec.y = this.posY;
                customActionDef.whenBuildingTriggerAction.a(this, ec, null, 0, 0);
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface
    public boolean c(com.corrodinggames.rts.game.units.buildings.Projectile projectile) {
        return true;
    }

    public void i(boolean z) {
        this.factoryQueueManager.a(z);
    }

    public boolean a(AbstractUnitAction abstractUnitAction, PointF pointF, BaseUnit baseUnit, int i) {
        BaseUnit unit;
        GameEngine gameEngine = GameEngine.getInstance();
        if (i > 10) {
            return false;
        }
        abstractUnitAction.onTargetSelected(this, baseUnit);
        if (abstractUnitAction instanceof CustomAction) {
            CustomAction customAction = (CustomAction) abstractUnitAction;
            CustomUnitConfig actionUnitConfig = this.unitConfig;
            CustomActionDef customActionDef = customAction.actionDef;
            if (customActionDef.requireConditional != null && !customActionDef.requireConditional.read(this)) {
                return true;
            }
            boolean z = false;
            if (customActionDef.addEnergy != null) {
                this.currentEnergy += customAction.actionDef.addEnergy.floatValue();
                z = true;
            }
            if (customActionDef.addResources != null) {
                customActionDef.addResources.h(this);
                z = true;
            }
            if (customActionDef.addResourcesScaledByAIHandicaps != null) {
                customActionDef.addResourcesScaledByAIHandicaps.a((BaseUnit) this, this.team.getSpectatorEnergyFactor(), true);
                z = true;
            }
            if (customActionDef.resetCustomTimer != null) {
                if (customActionDef.resetCustomTimer.read(this)) {
                    this.customTimerStamp = gameEngine.gameTimeMillis;
                }
                z = true;
            }
            if (customActionDef.fireTurretAtGroundIndex != null) {
                PointF pointF2 = pointF;
                for (int i2 = 0; i2 < customActionDef.fireTurretAtGroundCount; i2++) {
                    if (customActionDef.fireTurretAtGroundOffset != null) {
                        pointF2 = new PointF();
                        if (customActionDef.fireTurretAtGroundTarget != null && (unit = customActionDef.fireTurretAtGroundTarget.readUnit(this)) != null) {
                            pointF2.a(unit.posX, unit.posY);
                        } else {
                            pointF2.a(this.posX, this.posY);
                        }
                        float fFastCos = Utility.fastCos(this.rotationSpeed);
                        float fFastSin = Utility.fastSin(this.rotationSpeed);
                        float f = customActionDef.fireTurretAtGroundOffset.x;
                        float f2 = customActionDef.fireTurretAtGroundOffset.y;
                        pointF2.b((fFastCos * f2) - (fFastSin * f), (fFastSin * f2) + (fFastCos * f));
                    }
                    if (pointF2 == null) {
                        NetworkEngine.reportDesync("completeQueueItem:" + customAction.getActionId() + " for fireTurretXAtGround needs points but it is missing");
                    } else {
                        a((BaseUnit) null, pointF2.x, pointF2.y, customAction.actionDef.fireTurretAtGroundIndex.intValue(), customAction.actionDef.fireTurretAtGroundProjectile, i);
                    }
                }
                z = true;
            }
            if (customActionDef.spawnEffects != null) {
                customActionDef.spawnEffects.a(this.posX, this.posY, this.posZ, this.rotationSpeed, this);
                z = true;
            }
            if (customActionDef.spawnListAtActionTarget != null) {
                if (pointF == null) {
                    NetworkEngine.reportDesync(
                            "completeQueueItem:" + customAction.getActionId()
                                    + " for spawnEffectsAtActionTarget needs a point but it is missing"
                    );
                } else {
                    customActionDef.spawnListAtActionTarget.a(
                            pointF.x,
                            pointF.y,
                            this.posZ,
                            this.rotationSpeed,
                            this
                    );
                }
                z = true;
            }
            if (customActionDef.playSoundAtUnit != null) {
                customActionDef.playSoundAtUnit.a(this.posX, this.posY, 1.0f);
                z = true;
            }
            if (customActionDef.playSoundGlobally != null && !gameEngine.isMenuBackgroundMapActive()) {
                customActionDef.playSoundGlobally.a();
                z = true;
            }
            if (customActionDef.playSoundToPlayer != null) {
                if (this.team == gameEngine.playerTeam && !gameEngine.isMenuBackgroundMapActive()) {
                    customActionDef.playSoundToPlayer.a();
                }
                z = true;
            }
            if (customActionDef.logicActions.size > 0) {
                Object[] objArrA = customActionDef.logicActions.a();
                for (int i3 = 0; i3 < customActionDef.logicActions.size; i3++) {
                    if (((LogicAction) objArrA[i3]).doAction(this, abstractUnitAction, pointF, baseUnit, i)) {
                        z = true;
                    }
                }
            }
            PointF pointF3 = pointF;
            BaseUnit unit2 = baseUnit;
            if ((customActionDef.alsoTriggerAction != null || customAction.actionDef.alsoQueueAction != null) && customActionDef.alsoTriggerOrQueueActionTarget != null) {
                unit2 = customActionDef.alsoTriggerOrQueueActionTarget.readUnit(this);
                pointF3 = new PointF();
                if (unit2 != null) {
                    pointF3.x = unit2.posX;
                    pointF3.y = unit2.posY;
                } else {
                    pointF3.x = this.posX;
                    pointF3.y = this.posY;
                }
            }
            if (customActionDef.alsoTriggerAction != null) {
                if (customActionDef.alsoTriggerOrQueueActionCondition == null || customActionDef.alsoTriggerOrQueueActionCondition.read(this)) {
                    int number = 1;
                    if (customActionDef.alsoTriggerActionRepeat != null) {
                        number = (int) customActionDef.alsoTriggerActionRepeat.readNumber(this);
                        if (number > 10000) {
                            number = 10000;
                        }
                    }
                    for (int i4 = 0; i4 < number; i4++) {
                        customAction.actionDef.alsoTriggerAction.a(this, pointF3, unit2, i + 1, i4);
                    }
                }
                z = true;
            }
            if (customAction.actionDef.alsoQueueAction != null) {
                if (customActionDef.alsoTriggerOrQueueActionCondition == null || customActionDef.alsoTriggerOrQueueActionCondition.read(this)) {
                    customAction.actionDef.alsoQueueAction.a(this, pointF3, unit2);
                }
                z = true;
            }
            UnitType unitTypeC = null;
            if (customActionDef.convertTo != null) {
                unitTypeC = customActionDef.convertTo.c();
            }
            if (unitTypeC != null) {
                if (GameEngine.isReplayDebugMode) {
                    GameEngine.logColored(getUnitShortName() + ": converting unit: " + abstractUnitAction.getActionIdString());
                }
                if (!(unitTypeC instanceof CustomUnitConfig)) {
                    BaseUnit baseUnitA = unitTypeC.a();
                    baseUnitA.posX = this.posX;
                    baseUnitA.posY = this.posY;
                    if (!baseUnitA.bI()) {
                        baseUnitA.rotationSpeed = this.rotationSpeed;
                    }
                    baseUnitA.f(this.team);
                    baseUnitA.setCommandTargetUnit(null);
                    float f3 = this.maxHealth;
                    float f4 = baseUnitA.maxHealth;
                    if (f3 == 0.0f) {
                        baseUnitA.o(baseUnitA.maxHealth);
                    } else {
                        baseUnitA.o((this.currentHealth / f3) * f4);
                    }
                    if (this.isSelected) {
                        GameEngine.getInstance().gameUI.addToSelection(baseUnitA);
                    }
                    PlayerTeam.c(baseUnitA);
                    removeFromGame();
                } else {
                    AnimationSet unitCombatAnimation = null;
                    if (customActionDef.convertToKeepCurrentTags) {
                        unitCombatAnimation = getTags();
                    }
                    PlayerTeam.b((BaseUnit) this);
                    this.factoryUnitConfig = null;
                    a((CustomUnitConfig) unitTypeC, false, false, customActionDef.convertToKeepCurrentFields);
                    if (unitCombatAnimation != null) {
                        a(unitCombatAnimation, true);
                    }
                    S();
                    this.factoryQueueManager.e();
                    this.lastConvertedStamp = GameEngine.getInstance().gameTimeMillis;
                    PlayerTeam.c(this);
                }
                z = true;
                if (!customAction.getPrice().c()) {
                    W();
                }
            }
            UnitEventRuntime.enqueueActionCompleted(
                    this,
                    actionUnitConfig,
                    customAction.getActionIdString(),
                    pointF != null ? pointF.x : null,
                    pointF != null ? pointF.y : null,
                    baseUnit
            );
            if (!z && customActionDef.addToBuildQueue) {
                GameEngine.log("completeQueueItem:" + customAction.getActionId() + " had no effect (but should have)");
                return true;
            }
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: E */
    public void ejectUnit(BaseUnit baseUnit) {
        float fFloatValue = 0.0f;
        if (this.unitConfig.isBuildingUnit && this.unitConfig.exit_dirOffset != null) {
            fFloatValue = this.rotationSpeed + this.unitConfig.exit_dirOffset.floatValue() + 90.0f;
        }
        baseUnit.rotationSpeed = 90.0f + fFloatValue;
        float fFloatValue2 = 70.0f;
        if (this.unitConfig.exit_moveAwayAmount != null) {
            fFloatValue2 = this.unitConfig.exit_moveAwayAmount.floatValue();
        }
        this.factoryQueueManager.a(baseUnit, fFloatValue2, this.r);
    }

    public void F(BaseUnit baseUnit) {
        baseUnit.posX = this.posX + this.unitConfig.exit_x;
        baseUnit.posY = this.posY + this.unitConfig.exit_y;
        if (!this.unitConfig.isBuildingUnit) {
            float fFloatValue = 180.0f;
            if (this.unitConfig.exit_dirOffset != null) {
                fFloatValue = this.unitConfig.exit_dirOffset.floatValue();
            }
            float fFloatValue2 = 70.0f;
            if (this.unitConfig.exit_moveAwayAmount != null) {
                fFloatValue2 = this.unitConfig.exit_moveAwayAmount.floatValue();
            }
            boolean zA = HovercraftUnit.a(this, baseUnit, this.r, 7.0f, fFloatValue, fFloatValue2, this.unitConfig.exit_x, this.unitConfig.exit_y);
            if (!this.unitConfig.exitHeightIgnoreParent) {
                baseUnit.posZ = this.posZ;
            }
            baseUnit.posZ += this.unitConfig.exit_heightOffset;
            if (baseUnit instanceof CustomUnit) {
                ((CustomUnit) baseUnit).dF();
            }
            if ((i() || !zA || this.unitConfig.transportUnitsKeepBuiltUnits.read(this)) && canTransportUnits()) {
                loadTransportedUnit(baseUnit);
            }
        }
        this.r = !this.r;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public UnitPrice by() {
        FastArrayList fastArrayListG = this.factoryQueueManager.g();
        int size = fastArrayListG.size();
        if (size == 0) {
            return UnitPrice.a;
        }
        UnitPrice unitPrice = new UnitPrice();
        Object[] objArrA = fastArrayListG.a();
        for (int i = 0; i < size; i++) {
            AbstractUnitAction abstractUnitActionValidateActionId = validateActionId(((com.corrodinggames.rts.game.units.buildings.Projectile) objArrA[i]).j);
            if (abstractUnitActionValidateActionId != null && (abstractUnitActionValidateActionId instanceof CustomAction)) {
                CustomAction customAction = (CustomAction) abstractUnitActionValidateActionId;
                if (customAction.actionDef.addEnergy != null) {
                    unitPrice.c += customAction.actionDef.addEnergy.floatValue();
                }
                if (customAction.actionDef.addResources != null) {
                    UnitPrice unitPrice2 = customAction.actionDef.addResources;
                    if (!unitPrice2.c()) {
                        unitPrice = UnitPrice.a(unitPrice, unitPrice2);
                    }
                }
                if (customAction.actionDef.addResourcesScaledByAIHandicaps != null) {
                    UnitPrice unitPrice3 = customAction.actionDef.addResourcesScaledByAIHandicaps;
                    if (!unitPrice3.c()) {
                        unitPrice = UnitPrice.a(unitPrice, unitPrice3);
                    }
                }
            }
        }
        return unitPrice;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface
    public boolean dA() {
        return this.unitConfig.maxTransportingUnits > 0 && dI() > this.unitConfig.maxTransportingUnits;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface
    public int h(UnitType unitType) {
        return this.factoryQueueManager.a(unitType);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface
    public int f(boolean z) {
        return this.factoryQueueManager.a(AbstractUnitAction.NONE_ACTION_ID, z, true);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface
    public int a(ActionId actionId, boolean z) {
        return this.factoryQueueManager.a(actionId, z);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface
    public com.corrodinggames.rts.game.units.buildings.Projectile dw() {
        return this.factoryQueueManager.b();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: bD */
    public UnitPrice getQueuedActionPriceDelta() {
        return this.factoryQueueManager.c();
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface
    public FastArrayList dx() {
        return this.factoryQueueManager.c;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface
    public void dz() {
        this.factoryQueueManager.e = 1.0f;
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface
    public boolean dy() {
        return this.factoryQueueManager.a();
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public int a(AnimationTag animationTag) {
        return this.factoryQueueManager.a(animationTag);
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface
    public void a(PointF pointF) {
        if (this.unitConfig.hasSetRallyAction) {
            this.factoryQueueManager.b = pointF;
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public float x() {
        if (!this.unitConfig.showHealthBar) {
            return -1.0f;
        }
        return super.x();
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean bU() {
        return this.unitConfig.showHealthBarChanges;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public float bV() {
        if (isAlive() && !this.factoryQueueManager.a() && this.unitConfig.showQueueBar) {
            return this.factoryQueueManager.e;
        }
        return super.bV();
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: bW */
    public float getSecondaryBarProgress() {
        if (this.y.maxEnergy > 0.0f && this.currentEnergy < this.y.maxEnergy && this.unitConfig.showEnergyBar) {
            return this.currentEnergy / this.y.maxEnergy;
        }
        if (this.unitEnergyMax > 0.0f && this.shield < this.unitEnergyMax && this.unitConfig.showShieldBar) {
            return this.shield / this.unitEnergyMax;
        }
        if (this.y.maxEnergy == 0.0f && this.unitEnergyMax == 0.0f) {
            if (this.unitConfig.currentTurretIndex != -1 && this.movementLevels[this.unitConfig.currentTurretIndex].rotation > 0.0f) {
                return 1.0f - (this.movementLevels[this.unitConfig.currentTurretIndex].rotation / b(this.unitConfig.currentTurretIndex));
            }
            if (this.unitConfig.warmupBarTurretIndex != -1 && this.movementLevels[this.unitConfig.warmupBarTurretIndex].speed != 0.0f) {
                return this.movementLevels[this.unitConfig.warmupBarTurretIndex].speed / e(this.unitConfig.warmupBarTurretIndex);
            }
        }
        return super.getSecondaryBarProgress();
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public boolean f(float f) {
        return super.f(f);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public void p(float f) {
        int i = this.unitConfig.onDestroyListeners.size;
        if (i > 0) {
            Object[] objArrA = this.unitConfig.onDestroyListeners.a();
            for (int i2 = i - 1; i2 >= 0; i2--) {
                ((CustomUnitRenderHook) objArrA[i2]).f(this, f);
            }
        }
        super.p(f);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public void e(float f) {
        super.e(f);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: ca */
    public void drawRallyPoint() {
        if (this.factoryQueueManager.b != null) {
            final GameEngine instance = GameEngine.getInstance();
            instance.renderGraphicsEngine.a((float) (int) (this.posX - instance.viewpointXSnapped), (float) (int) (this.posY - instance.viewpointYSnapped), (float) (int) (this.factoryQueueManager.b.x - instance.viewpointXSnapped), (float) (int) (this.factoryQueueManager.b.y - instance.viewpointYSnapped), FactoryWithQueue.y);
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void cb() {
        boolean z = false;
        if ((this.y.maxAttackRange > 70.0f && this.unitConfig.canAttack && this.unitConfig.showRangeUIGuide == null) || (this.unitConfig.showRangeUIGuide != null && this.unitConfig.showRangeUIGuide.booleanValue())) {
            GameViewUtils.a((BaseUnit) this, m(), true);
            z = true;
        } else if (this.unitConfig.isBuildingUnit && this.y.nanoRange > 50 && !this.unitConfig.canAttack) {
            GameViewUtils.a((BaseUnit) this, this.y.nanoRange, true);
            z = true;
        }
        if (!this.unitConfig.customArms.isEmpty()) {
            for (Object object : this.unitConfig.customArms) {
                GameViewUtils.a((BaseUnit) this, ((CustomLimitedRange) object).rangeValue, true);
                z = true;
            }
        }
        if (this.unitConfig.hasProjectileInterceptorTurret) {
            int techLevel = getTechLevel();
            for (int i = 0; i < techLevel; i++) {
                TurretConfig turretConfig = this.unitConfig.turrets[i];
                if (turretConfig.interceptProjectilesWithTags != null && turretConfig.interceptProjectilesAndTargetingGroundUnderDistance > 0.0f) {
                    int i2 = 90;
                    if (z) {
                        i2 = 40;
                    }
                    GameViewUtils.a((BaseUnit) this, turretConfig.interceptProjectilesAndTargetingGroundUnderDistance, ArgbColor.a(i2, 35, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_TERRESTRIAL_ANALOG, 35), 1, true);
                }
            }
        }
    }

    @Override
    // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public void d(float f) {
        super.d(f);
        if (this.isDead) {
            return;
        }
        this.unitCustomComponents.a(f, this);
        if (this.unitConfig.image_back != null) {
            GameEngine gameEngine = GameEngine.getInstance();
            float f2 = this.posX - gameEngine.viewpointXSnapped;
            float f3 = (this.posY - gameEngine.viewpointYSnapped) - this.posZ;
            float maxHealth = getRenderScale();
            if (maxHealth != 1.0f) {
                gameEngine.renderGraphicsEngine.k();
                gameEngine.renderGraphicsEngine.a(maxHealth, maxHealth, f2, f3);
            }
            if (this.unitConfig.image_back_always_use_full_image) {
                int i = this.unitConfig.image_back.p;
                int i2 = this.unitConfig.image_back.q;
                int i3 = i / 2;
                int i4 = i2 / 2;
                du.a(f2 - i3, f3 - i4, f2 + i3, f3 + i4);
                dv.a(0, 0, 0 + i, 0 + i2);
            } else {
                du.a(f2 - this.eu, f3 - this.ev, f2 + this.eu, f3 + this.ev);
                dv.a(0, 0, 0 + this.es, 0 + this.et);
            }
            gameEngine.renderGraphicsEngine.a(this.unitConfig.image_back, dv, du, getRenderPaint());
            if (maxHealth != 1.0f) {
                gameEngine.renderGraphicsEngine.l();
            }
        }
        if (this.unitConfig.drawLegsUnderAllUnits && this.legInstances != null && !this.isDead) {
            CustomUnitLegController.a(this, f, false, true);
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: aM */
    public float getPathStepScale() {
        return this.unitConfig.whenBeingBuiltMakeTransparentTill;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: aN */
    public Paint getRenderPaint() {
        return super.getRenderPaint();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: aV */
    public boolean useVelocityExtendedRange() {
        return this.unitConfig.isMelee;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean bI() {
        return this.unitConfig.isBuildingUnit;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean q() {
        if (this.parentEntity != null && this.parentEntity.buildProgress < 1.0f) {
            return true;
        }
        return this.unitConfig.isUnrepairableUnit;
    }

    /* JADX INFO: renamed from: H */
    private boolean canRepairUnit(BaseUnit baseUnit) {
        if (baseUnit.q() || baseUnit == this) {
            return false;
        }
        if (baseUnit.bI()) {
            if (this.unitConfig.canRepairBuildings) {
                return true;
            }
            return false;
        }
        if (this.unitConfig.canRepairUnits) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: a */
    public boolean canRepairTarget(BaseUnit baseUnit) {
        if (this.unitConfig.canRepairUnitsOnlyWithTags != null && !AnimationTag.a(this.unitConfig.canRepairUnitsOnlyWithTags, baseUnit.getTags())) {
            return false;
        }
        return canRepairUnit(baseUnit);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: l */
    public boolean canReclaimTarget(BaseUnit baseUnit) {
        if (baseUnit.getResourceRate() != 0.0f && h(baseUnit, true)) {
            return true;
        }
        if (this.unitConfig.canReclaimUnitsOnlyWithTags != null && !AnimationTag.a(this.unitConfig.canReclaimUnitsOnlyWithTags, baseUnit.getTags())) {
            return false;
        }
        return canRepairUnit(baseUnit);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void m(BaseUnit baseUnit) {
        FormationGroup formationGroup;
        if (this.unitConfig.dieOnConstruct) {
            UnitCommand unitCommandAr = getCurrentWaypoint();
            if (unitCommandAr != null && (formationGroup = unitCommandAr.transportTarget) != null) {
                formationGroup.a(unitCommandAr);
            }
            if (this.isSelected && baseUnit != null) {
                GameEngine.getInstance().gameUI.addToSelection(baseUnit);
            }
            removeFromGame();
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: al */
    public boolean isTurretUnit() {
        if (this.unitConfig.dieOnConstruct) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: aj */
    public boolean canUnitAttack() {
        return this.unitConfig.useAsBuilder;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cu */
    public boolean isNotPassivelyTargetedByOtherUnits() {
        return this.unitConfig.notPassivelyTargetedByOtherUnits;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: ak */
    public boolean canMove() {
        return this.unitConfig.isBuilder;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean g(BaseUnit baseUnit, boolean z) {
        if (!h(baseUnit, z)) {
            return false;
        }
        if (z && baseUnit.c((OrderableUnit) this)) {
            return false;
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean h(BaseUnit baseUnit, boolean z) {
        if (!this.unitConfig.canReclaimResources) {
            return false;
        }
        if (this.unitConfig.canReclaimResourcesOnlyWithTags != null && !AnimationTag.a(this.unitConfig.canReclaimResourcesOnlyWithTags, baseUnit.getTags())) {
            return false;
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cS */
    public int getReclaimSearchRange() {
        return this.unitConfig.canReclaimResourcesNextSearchRange;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean bJ() {
        return this.unitConfig.canBuildUnits;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public void a(float f, boolean z) {
        super.a(f, z);
        if (!this.isDead && canMove()) {
            if (isCurrentCommandReclaim()) {
                if (!this.unitConfig.hasReclaimEffect) {
                    BuilderUnit.drawTargetPriorityPoints(f, this);
                }
            } else if (!this.unitConfig.showActionsAndWaypoints) {
                BuilderUnit.drawTargetPriorityPoints(f, this);
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean o() {
        return this.unitConfig.stayNeutral;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean p() {
        return this.unitConfig.createNeutral;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cO */
    public boolean canBeCapturedByAI() {
        return this.unitConfig.allowCaptureWhenNeutralByAI;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void f(PlayerTeam playerTeam) {
        if (this.unitConfig.createOnAggressiveTeam) {
            setUnitTeam(PlayerTeam.TEAM_UNKNOWN);
        } else if (this.unitConfig.convertToNeutralIfNotTransporting && this.transportedUnits.size() == 0) {
            setUnitTeam(PlayerTeam.TEAM_ALL);
        } else {
            super.f(playerTeam);
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: B */
    public void setCommandTargetUnit(BaseUnit baseUnit) {
        super.setCommandTargetUnit(baseUnit);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: g */
    public float getResourceRate() {
        return this.unitConfig.resourceRate;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cQ */
    public int getMaxConcurrentReclaimers() {
        return this.unitConfig.resourceMaxConcurrentReclaimingThis;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cR */
    public AnimationSet getSimilarResourcesTag() {
        return this.unitConfig.similarResourcesHaveTag;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cP */
    public void onUnitSpawned() {
        if (this.unitConfig.selfBuildRate == 0.0f && getResourceRate() > 0.0f) {
            PlayerTeam.b((BaseUnit) this);
            this.buildProgress = 1.0f;
            PlayerTeam.c(this);
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cp */
    public ActionId getUnloadActionId() {
        if (this.unitConfig.maxTransportingUnits != 0) {
            return HovercraftUnit.i.getActionId();
        }
        return super.getUnloadActionId();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float L(int i) {
        return this.unitConfig.turrets[i].aimOffsetSpread;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: K */
    public PointF getShadowOffsetForLevel(int i) {
        Projectile projectile;
        PointF pointFK = super.getShadowOffsetForLevel(i);
        if (this.unitConfig.moveYAxisScaleInverted) {
            if (this.unitConfig.projectileTemplatesById[this.unitConfig.turrets[i].a(this)].M && this.G != null && (projectile = this.G[i]) != null && !projectile.isDestroyed) {
                pointFK.x += projectile.trackOffsetX;
                pointFK.y += projectile.trackOffsetY;
            }
        }
        return pointFK;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit
    public float bd() {
        return this.y.maxEnergy;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public UnitBehaviorType be() {
        return this.unitConfig.attackMovementType;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean bf() {
        if (this.unitConfig.disablePassiveTargeting) {
            return false;
        }
        if (this.unitConfig.attackMovementType == UnitBehaviorType.bomber) {
            UnitCommand unitCommandAr = getCurrentWaypoint();
            if ((unitCommandAr != null && (unitCommandAr.getCommandType() == UnitCommandType.attackMove || unitCommandAr.getCommandType() == UnitCommandType.patrol)) || this.attackMode == AttackMode.outOfRange) {
                return true;
            }
            return false;
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: bX */
    public boolean isSecondaryBarRecharging() {
        return this.v;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean bg() {
        return this.unitConfig.joinsGroupFormations;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float bc() {
        return this.unitConfig.reverseSpeedPercentage;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void f(float f, float f2) {
        super.f(f, f2);
        a(UnitEventType.teleported);
        float f3 = this.rotationSpeed;
        if (this.unitConfig.lockLegRotationWithMainTurret) {
            f3 = this.movementLevels[this.unitConfig.mainTurretIndex].targetX;
        }
        this.dP = this.posX;
        this.dQ = this.posY;
        this.dR = this.posZ;
        this.dS = f3;
    }

    public void du() {
        if (this.unitConfig.legConfig == null && this.legInstances == null) {
            return;
        }
        if (this.unitConfig.legConfig == null || this.unitConfig.legConfig.length == 0) {
            this.legInstances = null;
            return;
        }
        if (this.legInstances != null && this.legInstances.length == this.unitConfig.legConfig.length) {
            return;
        }
        this.legInstances = new LegInstance[this.unitConfig.legConfig.length];
        for (int i = 0; i < this.unitConfig.legConfig.length; i++) {
            LegInstance legInstance = new LegInstance();
            this.legInstances[i] = legInstance;
            legInstance.a = i;
            legInstance.s = this.unitConfig.legConfig[i].r;
        }
        float f = this.rotationSpeed;
        if (this.unitConfig.lockLegRotationWithMainTurret) {
            f = this.movementLevels[this.unitConfig.mainTurretIndex].targetX;
        }
        this.dP = this.posX;
        this.dQ = this.posY;
        this.dR = this.posZ;
        this.dS = f;
        dv();
        for (int i2 = 0; i2 < this.unitConfig.legConfig.length; i2++) {
            this.legInstances[i2].m = true;
        }
    }

    public void dv() {
        CustomUnitLegController.a.b(this, 0.0f);
    }

    public void dB() {
        if (this.legInstances != null) {
            for (int i = 0; i < this.legInstances.length; i++) {
                this.legInstances[i].n = true;
                this.legInstances[i].m = true;
            }
            dv();
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: aT */
    public int getDefaultTurretIndex() {
        if (this.unitConfig.mainNanoTurret == null) {
            return -1;
        }
        return this.unitConfig.mainNanoTurret.turretIndex;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public int s() {
        return this.y.fogOfWarSightRange;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit
    public void c(boolean z) {
        CustomUnitConfig customUnitConfig = this.unitConfig;
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.transportContainer != null || this.parentEntity != null) {
            return;
        }
        int i = this.y.fogOfWarSightRange;
        if (this.buildProgress < 1.0f && customUnitConfig.fogOfWarSightRangeWhileNotBuilt != -1) {
            i = customUnitConfig.fogOfWarSightRangeWhileNotBuilt;
        }
        if (i > 0) {
            gameEngine.tileMap.updateFogVisibilityForTeamsAtWorldPoint(this.posX, this.posY, i, this.team, z);
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public Rect cc() {
        return this.unitConfig.footprint;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: ce */
    public Rect getDisplayFootprint() {
        return this.unitConfig.displayFootprint;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public Rect cd() {
        return this.unitConfig.constructionFootprint;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean b(int i, float f) {
        float fC;
        TurretConfig turretConfig = this.unitConfig.turrets[i];
        if (turretConfig.idleSweepAngle != 0.0f) {
            boolean z = true;
            if (turretConfig.idleSweepCondition != null && !turretConfig.idleSweepCondition.read(this)) {
                z = false;
            }
            if (z) {
                UnitMovementData unitMovementData = this.movementLevels[i];
                if (turretConfig.idleSpin != 0.0f) {
                    fC = unitMovementData.targetX;
                } else if (!turretConfig.shouldResetTurret) {
                    fC = unitMovementData.targetY;
                } else {
                    fC = C(i);
                }
                unitMovementData.idleSweepTimer += f;
                float f2 = f * turretConfig.idleSweepSpeed;
                if (unitMovementData.idleSweepAngle > 0.0f) {
                    if (unitMovementData.idleSweepAngle < Float.POSITIVE_INFINITY && a(f2, fC + unitMovementData.idleSweepAngle, i) == 0.0f) {
                        unitMovementData.idleSweepAngle = Float.POSITIVE_INFINITY;
                    }
                } else if (unitMovementData.idleSweepAngle > Float.NEGATIVE_INFINITY && a(f2, fC + unitMovementData.idleSweepAngle, i) == 0.0f) {
                    unitMovementData.idleSweepAngle = Float.NEGATIVE_INFINITY;
                }
                if (unitMovementData.idleSweepTimer > turretConfig.idleSweepDelay) {
                    unitMovementData.idleSweepTimer = -Utility.getDeterministicRandomInt(this, 0, (int) turretConfig.idleSweepAddRandomDelay);
                    float fClamp = turretConfig.idleSweepAngle;
                    if (turretConfig.idleSweepAddRandomAngle > 0.0f) {
                        fClamp += Utility.getDeterministicRandomFloatForUnit(this, 0.0f, turretConfig.idleSweepAddRandomAngle, i);
                    }
                    unitMovementData.idleSweepAngle = unitMovementData.idleSweepAngle > 0.0f ? -fClamp : fClamp;
                    return false;
                }
                return false;
            }
        }
        if (turretConfig.idleSpin != 0.0f) {
            this.movementLevels[i].targetX += turretConfig.idleSpin * f;
            if (this.movementLevels[i].targetX > 180.0f) {
                this.movementLevels[i].targetX -= 360.0f;
            }
            if (this.movementLevels[i].targetX < -180.0f) {
                this.movementLevels[i].targetX += 360.0f;
                return false;
            }
            return false;
        }
        return turretConfig.shouldResetTurret;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cw */
    public int getTransportSlotsNeeded() {
        return this.unitConfig.transportSlotsNeeded;
    }

    public ArrayList dC() {
        dU.clear();
        ArrayList<AbstractUnitAction> availableActions = getAvailableActions();
        if (!availableActions.isEmpty()) {
            for (AbstractUnitAction abstractUnitAction : availableActions) {
                if (abstractUnitAction instanceof CustomAction) {
                    CustomAction customAction = (CustomAction) abstractUnitAction;
                    if (customAction.actionTypeForUnit == ActionType.upgrade) {
                        dU.add(customAction);
                    }
                }
            }
        }
        return dU;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public ActionId cm() {
        ArrayList arrayListDC = dC();
        if (arrayListDC.size() > 0) {
            return ((AbstractUnitAction) arrayListDC.get(0)).getActionId();
        }
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: a */
    public void clearAndAddAction(ArrayList arrayList) {
        arrayList.clear();
        ArrayList arrayListDC = dC();
        if (arrayListDC.size() < 2) {
            return;
        }
        arrayListDC.remove(0);
        Iterator it = arrayListDC.iterator();
        while (it.hasNext()) {
            arrayList.add(((AbstractUnitAction) it.next()).getActionId());
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cZ */
    public float getTileOffsetX() {
        return this.unitConfig.buildingToFootprintOffsetX;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: da */
    public float getTileOffsetY() {
        return this.unitConfig.buildingToFootprintOffsetY;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit
    public void bv() {
        PlayerTeam.a((BaseUnit) this);
        this.factoryQueueManager.a(true);
        super.bv();
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: dc */
    public void startFalling() {
        this.posZ = 170.0f;
        this.frameAnimationDelay = 1.5f;
        dB();
        super.startFalling();
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: dd */
    public boolean isExperimental() {
        return this.unitConfig.experimental;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public int bp() {
        return this.unitConfig.numBitsOnDeath;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void a(int i, float f) {
        this.movementLevels[i].targetX += f;
        if (this.unitConfig.hasAttachedTurrets) {
            for (int i2 = 0; i2 < this.unitConfig.turrets.length; i2++) {
                if (this.unitConfig.turrets[i2].linkedTurretIndex == i) {
                    this.movementLevels[i2].targetX += f;
                    this.movementLevels[i2].a(2);
                }
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: db */
    public float getSelectionRadius() {
        return super.getSelectionRadius() + this.unitConfig.buildingSelectionOffset;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float H(int i) {
        return this.unitConfig.turrets[i].p;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float I(int i) {
        return this.unitConfig.turrets[i].q;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float J(int i) {
        return this.unitConfig.turrets[i].r;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: a */
    public float applyDamage(BaseUnit baseUnit, float f, Projectile projectile) {
        AttachmentSlotDefinition attachmentSlotDefinitionDn = dn();
        if (attachmentSlotDefinitionDn != null && this.parentEntity != null && attachmentSlotDefinitionDn.j) {
            int i = 0;
            if (projectile != null) {
                i = projectile.depth;
            }
            if (i >= 0) {
                Projectile projectileA = Projectile.a(projectile);
                if (attachmentSlotDefinitionDn.k) {
                    projectileA.globalDamageMultiplier = 0.0f;
                }
                f = this.parentEntity.applyDamage(baseUnit, f, projectileA);
                if (f < 0.0f) {
                    f = 0.0f;
                }
            }
        }
        if (isDamageImmune()) {
            f = 0.0f;
        }
        if (this.y.armour > 0.0f && f > this.unitConfig.armourMinDamageToKeep) {
            float f2 = this.y.armour;
            if (projectile != null) {
                f2 -= projectile.armourIgnore;
            }
            if (f2 < 0.0f) {
                f2 = 0.0f;
            }
            f -= f2;
            if (f < this.unitConfig.armourMinDamageToKeep) {
                f = this.unitConfig.armourMinDamageToKeep;
            }
        }
        if (projectile != null) {
            UnitEventRuntime.enqueueDamage(this, baseUnit, projectile.animationSet, f);
            a(UnitEventType.tookDamage, baseUnit, projectile.animationSet, (VariableScope) null);
        } else {
            UnitEventRuntime.enqueueDamage(this, baseUnit, null, f);
            a(UnitEventType.tookDamage, baseUnit);
        }
        return super.applyDamage(baseUnit, f, projectile);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: aU */
    public float getBasePathingRange() {
        return this.unitConfig.maxAttackRange;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: ac */
    public boolean supportsIndependentTurretTargets() {
        if (!this.unitConfig.turretMultiTargeting) {
            return false;
        }
        return super.supportsIndependentTurretTargets();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean a(CommandType commandType) {
        if (commandType == CommandType.attack) {
            return this.unitConfig.soundOnAttackOrder.a();
        }
        if (commandType == CommandType.move) {
            return this.unitConfig.soundOnMoveOrder.a();
        }
        if (commandType == CommandType.newSelection) {
            return this.unitConfig.soundOnNewSelection.a();
        }
        return false;
    }

    public void b(UnitEventType unitEventType) {
        UnitEventRuntime.enqueue(this, unitEventType, null, null);
        CustomUnitConfig customUnitConfig = this.unitConfig;
        if (customUnitConfig.eventBindings.size == 0) {
            return;
        }
        Object[] objArrA = customUnitConfig.eventBindings.a();
        for (int i = customUnitConfig.eventBindings.size - 1; i >= 0; i--) {
            CustomEventBinding customEventBinding = (CustomEventBinding) objArrA[i];
            if (customEventBinding.a == unitEventType) {
                ec.x = this.posX;
                ec.y = this.posY;
                a(customEventBinding.b, ec, (BaseUnit) null, 0, 0);
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void a(UnitEventType unitEventType, BaseUnit baseUnit, AnimationSet animationSet, VariableScope variableScope) {
        CustomUnitEventData customUnitEventData;
        UnitEventRuntime.enqueue(this, unitEventType, baseUnit, animationSet);
        CustomUnitConfig customUnitConfig = this.unitConfig;
        if (customUnitConfig.eventBindings.size == 0) {
            return;
        }
        Object[] objArrA = customUnitConfig.eventBindings.a();
        for (int i = customUnitConfig.eventBindings.size - 1; i >= 0; i--) {
            CustomEventBinding customEventBinding = (CustomEventBinding) objArrA[i];
            if (customEventBinding.a == unitEventType && (customEventBinding.d == null || AnimationTag.a(customEventBinding.d, animationSet))) {
                if (dX.size() > 0) {
                    customUnitEventData = (CustomUnitEventData) dX.b();
                } else {
                    customUnitEventData = new CustomUnitEventData();
                }
                customUnitEventData.eventInfo = customEventBinding;
                customUnitEventData.customUnit = this;
                customUnitEventData.unit = baseUnit;
                customUnitEventData.animationSet = animationSet;
                customUnitEventData.variableScope = variableScope;
                dW.add(customUnitEventData);
            }
        }
    }

    public static void s(float f) {
    }

    public static void dD() {
        if (dW.size == 0) {
            return;
        }
        dW = new FastArrayList();
    }

    @Override // com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface
    public void a(com.corrodinggames.rts.game.units.buildings.Projectile projectile) {
        float fFloatValue;
        AbstractUnitAction abstractUnitActionValidateActionId = validateActionId(projectile.j);
        if (abstractUnitActionValidateActionId != null && a(abstractUnitActionValidateActionId, projectile.targetPoint, projectile.targetUnit, 0, 0)) {
            return;
        }
        float fFloatValue2 = 0.0f;
        if (this.unitConfig.isBuildingUnit && this.unitConfig.exit_dirOffset != null) {
            fFloatValue2 = this.rotationSpeed + this.unitConfig.exit_dirOffset.floatValue() + 90.0f;
        }
        if (this.unitConfig.exit_moveAwayAmount != null) {
            fFloatValue = this.unitConfig.exit_moveAwayAmount.floatValue();
        } else if (this.factoryQueueManager.b != null) {
            fFloatValue = this.radius * 3.0f;
        } else {
            fFloatValue = this.radius * 2.0f;
        }
        BaseUnit baseUnitA = this.factoryQueueManager.a(projectile, fFloatValue, this.r, fFloatValue2);
        if (baseUnitA != null) {
            F(baseUnitA);
            PlayerTeam.c(baseUnitA);
            a(UnitEventType.queuedUnitFinished, baseUnitA);
        }
    }

    public static void dE() {
    }

    public void b(float f, boolean z) {
        CustomUnitConfig customUnitConfig = this.unitConfig;
        if (!customUnitConfig.hasAutoTriggerConditions) {
            return;
        }
        if (this.w != 0.0f) {
            this.w = Utility.moveTowardsZero(this.w, f);
            if (this.w == 0.0f) {
                z = true;
            } else {
                return;
            }
        }
        CustomUnitCondition[] customUnitConditionArr = customUnitConfig.autoTriggerConditionsEveryFrame;
        if (customUnitConditionArr != null) {
            a(f, customUnitConditionArr);
            if (customUnitConfig != this.unitConfig) {
                return;
            }
        }
        CustomUnitCondition[] customUnitConditionArr2 = customUnitConfig.autoTriggerConditionsEvery4Frames;
        if (customUnitConditionArr2 != null && (((int) (((long) GameEngine.getInstance().currentTick) + this.objectId)) % 4 == 0 || z)) {
            a(f, customUnitConditionArr2);
            if (customUnitConfig != this.unitConfig) {
                return;
            }
        }
        CustomUnitCondition[] customUnitConditionArr3 = customUnitConfig.autoTriggerConditionsEvery8Frames;
        if (customUnitConditionArr3 != null) {
            if (((int) (((long) GameEngine.getInstance().currentTick) + this.objectId)) % 8 == 0 || z) {
                a(f, customUnitConditionArr3);
                if (customUnitConfig != this.unitConfig) {
                }
            }
        }
    }

    public void a(float f, CustomUnitCondition[] customUnitConditionArr) {
        CustomUnitConfig customUnitConfig = this.unitConfig;
        GameEngine gameEngine = GameEngine.getInstance();
        for (CustomUnitCondition customUnitCondition : customUnitConditionArr) {
            if (customUnitCondition.logicBoolean.read(this)) {
                if (gameEngine.isDebugTempMode && gameEngine.isTriggerDebugMode && this.isSelected) {
                    if (customUnitCondition.action != null) {
                        String str = VariableScope.nullOrMissingString + customUnitCondition.action.getActionIdString();
                    }
                    String str2 = "autoTrigger fired on: " + getUnitDebugName() + " details: " + customUnitCondition.logicBoolean.getDebugDetails(this);
                    GameEngine.log(str2);
                    gameEngine.gameUI.warLogDisplay.logMessage(str2, 2000);
                }
                ec.x = this.posX;
                ec.y = this.posY;
                a(customUnitCondition.action, ec, (BaseUnit) null, 0, 0);
                this.w = this.unitConfig.autoTriggerCooldownTime;
                if (customUnitConfig != this.unitConfig) {
                    return;
                }
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: de */
    public AnimationSet getTags() {
        return this.currentActionHandler;
    }

    public void a(AnimationSet animationSet, boolean z) {
        if (z) {
            this.currentActionHandler = animationSet;
            return;
        }
        PlayerTeam.b((BaseUnit) this);
        this.currentActionHandler = animationSet;
        PlayerTeam.c(this);
    }

    public void j(boolean z) {
        a(this.unitConfig.tags, z);
    }

    public void a(AnimationSet animationSet) {
        AnimationSet unitCombatAnimation = getTags();
        if (unitCombatAnimation == null || unitCombatAnimation.b() == 0) {
            a(animationSet, false);
        } else {
            if (AnimationTag.b(unitCombatAnimation, animationSet)) {
                return;
            }
            CustomUnitAnimationTags customUnitAnimationTags = new CustomUnitAnimationTags(unitCombatAnimation);
            if (customUnitAnimationTags.a(animationSet)) {
                a(customUnitAnimationTags.a(), false);
            }
        }
    }

    public void b(AnimationSet animationSet) {
        AnimationSet unitCombatAnimation = getTags();
        if (unitCombatAnimation == null || unitCombatAnimation.b() == 0 || !AnimationTag.a(animationSet, unitCombatAnimation)) {
            return;
        }
        CustomUnitAnimationTags customUnitAnimationTags = new CustomUnitAnimationTags(unitCombatAnimation);
        if (customUnitAnimationTags.b(animationSet)) {
            a(customUnitAnimationTags.a(), false);
        }
    }

    public final void dF() {
        if (this.unitConfig.movementType == UnitMovementType.AIR) {
            if (i()) {
                S(5);
            } else if (canTransportUnits() && this.spawnExitLockTimer == 0.0f) {
                S(3);
            } else {
                S(2);
            }
        } else if (this.spawnExitLockTimer == 0.0f) {
            S(this.unitConfig.drawLayer);
        } else {
            S(2);
        }
        this.drawOrder = 0;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: ck */
    public boolean isUpgradeable() {
        if (!this.unitConfig.hasBuildCostActions) {
            return false;
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.TransportUnitInterface
    public boolean f() {
        return this.unitConfig.transportUnitsCanUnloadCondition.read(this);
    }

    @Override // com.corrodinggames.rts.game.units.TransportUnitInterface
    public boolean j() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Vector3D D(int i) {
        UnitMovementData unitMovementData = this.movementLevels[i];
        TurretConfig turretConfig = this.unitConfig.turrets[i];
        float f = turretConfig.barrelY;
        float f2 = turretConfig.barrelX;
        if (turretConfig.barrelOffsetXOnOddShots != 0.0f && unitMovementData.isOddShot) {
            f2 += turretConfig.barrelOffsetXOnOddShots;
        }
        float f3 = E() ? this.rotationSpeed : unitMovementData.targetX;
        Vector3D vector3DF = F(i);
        float fFastCos = Utility.fastCos(f3);
        float fFastSin = Utility.fastSin(f3);
        float f4 = vector3DF.x;
        float f5 = vector3DF.y;
        float f6 = vector3DF.z;
        ee.x = f4 + ((fFastCos * f) - (fFastSin * f2));
        ee.y = f5 + (fFastSin * f) + (fFastCos * f2);
        ee.z = f6 + turretConfig.barrelHeight;
        return ee;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public PointF E(int i) {
        Vector3D vector3DD = D(i);
        ef.x = vector3DD.x;
        ef.y = vector3DD.y;
        return ef;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cl */
    public boolean hasAiHighPriorityAction() {
        return this.unitConfig.hasAiHighPriorityAction;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cn */
    public float getAiUpgradePriority() {
        return this.unitConfig.aiUpgradePriority;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void a(BaseUnit baseUnit, float f, int i) {
        if (this.unitConfig.repairAnimation != null) {
            this.animationController.a(this.unitConfig.repairAnimation, 5);
        }
        if (this.unitConfig.showActionsAndWaypoints) {
            this.turretTurnSpeed = Utility.moveTowardsZero(this.turretTurnSpeed, f);
            if (this.turretTurnSpeed == 0.0f) {
                this.turretTurnSpeed = this.unitConfig.repairEffectRate;
                if (this.unitConfig.repairEffect != null) {
                    UnitMovementData unitMovementData = this.movementLevels[i];
                    PointF pointFE = E(i);
                    this.unitConfig.repairEffect.a(pointFE.x, pointFE.y, this.posZ, unitMovementData.targetX, this);
                }
                if (this.unitConfig.repairEffectAtTarget != null) {
                    this.unitConfig.repairEffectAtTarget.a(baseUnit.posX, baseUnit.posY, baseUnit.posZ, baseUnit.rotationSpeed, baseUnit);
                    return;
                }
                return;
            }
            return;
        }
        super.a(baseUnit, f, i);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void b(BaseUnit baseUnit, float f, int i) {
        if (this.unitConfig.reclaimAnimation != null) {
            this.animationController.a(this.unitConfig.reclaimAnimation, 5);
        }
        if (this.unitConfig.hasReclaimEffect) {
            this.turretTurnSpeed = Utility.moveTowardsZero(this.turretTurnSpeed, f);
            if (this.turretTurnSpeed == 0.0f) {
                this.turretTurnSpeed = this.unitConfig.reclaimEffectRate;
                if (this.unitConfig.reclaimEffect != null) {
                    UnitMovementData unitMovementData = this.movementLevels[i];
                    PointF pointFE = E(i);
                    this.unitConfig.reclaimEffect.a(pointFE.x, pointFE.y, this.posZ, unitMovementData.targetX, this);
                }
                if (this.unitConfig.reclaimEffectAtTarget != null) {
                    this.unitConfig.reclaimEffectAtTarget.a(baseUnit.posX, baseUnit.posY, baseUnit.posZ, baseUnit.rotationSpeed, baseUnit);
                    return;
                }
                return;
            }
            return;
        }
        super.b(baseUnit, f, i);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cg */
    public boolean isVisibleToEnemies() {
        return this.y.isVisibleToEnemies;
    }

    public boolean dG() {
        if (this.unitConfig.isVisible != null && !this.unitConfig.isVisible.read(this)) {
            return false;
        }
        if (!isVisibleToEnemies()) {
            GameEngine gameEngine = GameEngine.getInstance();
            if (gameEngine.playerTeam.c(this.team) && !gameEngine.playerTeam.isSpectatorTeamColor()) {
                return false;
            }
        }
        AttachmentSlotDefinition attachmentSlotDefinitionDn = dn();
        if (attachmentSlotDefinitionDn != null && !attachmentSlotDefinitionDn.o) {
            return false;
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean t() {
        AttachmentSlotDefinition attachmentSlotDefinitionDn = dn();
        if (attachmentSlotDefinitionDn != null && attachmentSlotDefinitionDn.m) {
            return true;
        }
        return this.unitConfig.isUnselectable;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cV */
    public boolean isUnselectableAsTarget() {
        AttachmentSlotDefinition attachmentSlotDefinitionDn = dn();
        if (attachmentSlotDefinitionDn != null && attachmentSlotDefinitionDn.n) {
            return true;
        }
        return this.unitConfig.isUnselectableAsTarget;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean d(BaseUnit baseUnit) {
        CustomUnitConfig customUnitConfig = this.unitConfig;
        return ((customUnitConfig.deathAnimation != null && !AnimationTag.a(customUnitConfig.deathAnimation, baseUnit.getTags())) || dH() || customUnitConfig.canNotBeDirectlyAttacked) ? false : true;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cW */
    public boolean canNotBeGivenOrdersByPlayer() {
        return this.unitConfig.canNotBeGivenOrdersByPlayer;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cT */
    public boolean isExcludedFromDefeatCheck() {
        return this.unitConfig.canNotBeDirectlyAttacked || u() || (this.buildProgress < 1.0f && this.unitConfig.selfBuildRate <= 0.0f);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: dh */
    public AnimationSet getTrackingTags() {
        return this.unitConfig.trackingTags;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: am */
    public float getPassiveTargetSearchRangeBonus() {
        return this.unitConfig.meleeAttackRange;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: an */
    public boolean isChaseAttackMode() {
        return super.isChaseAttackMode() || this.unitConfig.isMelee;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public boolean a(GameEngine gameEngine) {
        if (!gameEngine.bufferedVisibleWorldRectF.b(this.posX, this.posY)) {
            if (!this.unitConfig.isHover) {
                return false;
            }
            boolean z = false;
            if (this.unitConfig.largeImageBounds != null) {
                CustomUnitConfig.tempRect.a(this.unitConfig.largeImageBounds);
                CustomUnitConfig.tempRect.a((int) this.posX, (int) this.posY);
                if (gameEngine.visibleWorldRect.b(CustomUnitConfig.tempRect)) {
                    z = true;
                }
            }
            if (gameEngine.bufferedVisibleWorldRectF.b(this.posX, this.posY - this.posZ)) {
                z = true;
            }
            if (!z) {
                return false;
            }
        }
        if (this.transportContainer != null) {
            return false;
        }
        if ((this.attachmentData != null && this.attachmentData.I) || !d(gameEngine.playerTeam) || !dG()) {
            return false;
        }
        return true;
    }

    public OrderableUnit a(AttachmentSlotDefinition attachmentSlotDefinition) {
        return AttachmentManagerHook.a(this, attachmentSlotDefinition);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public AttachmentSlotDefinition a(short s) {
        return AttachmentManagerHook.a(this, s);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean a(OrderableUnit orderableUnit, AttachmentSlotDefinition attachmentSlotDefinition) {
        if (orderableUnit == this) {
            return false;
        }
        if (attachmentSlotDefinition == null) {
            GameEngine.logColored("attachRequest: No attachedSlotData");
            return false;
        }
        OrderableUnit orderableUnitA = AttachmentManagerHook.a(this, attachmentSlotDefinition);
        if (orderableUnitA != null) {
            GameEngine.logColored("attachRequest: a unit is already in slot (parent:" + getUnitDebugName() + " slot:" + attachmentSlotDefinition.b() + " existing:" + orderableUnitA.getUnitDebugDetails() + ")");
            return false;
        }
        GameEngine gameEngine = GameEngine.getInstance();
        AttachmentManagerHook.a(this, attachmentSlotDefinition, orderableUnit);
        orderableUnit.parentEntity = this;
        orderableUnit.attachmentData = attachmentSlotDefinition;
        orderableUnit.attachmentStartTimeMillis = gameEngine.gameTimeMillis;
        orderableUnit.isAlive = false;
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean b(OrderableUnit orderableUnit) {
        if (orderableUnit.parentEntity != this) {
            GameEngine.logColored("deattachRequest: unit is not attached");
            return false;
        }
        AttachmentSlotDefinition attachmentSlotDefinition = orderableUnit.attachmentData;
        if (attachmentSlotDefinition == null) {
            GameEngine.logColored("deattachRequest: unit has no attachedSlotData");
            return false;
        }
        OrderableUnit orderableUnitA = AttachmentManagerHook.a(this, attachmentSlotDefinition);
        if (orderableUnitA == null) {
            GameEngine.logColored("deattachRequest: Failed, no unit in slot");
            GameEngine.logWarningAndStack("deattachRequest");
            return false;
        }
        if (orderableUnit != orderableUnitA) {
            String strCB = "null";
            if (orderableUnitA != null) {
                strCB = orderableUnitA.getUnitDebugName();
            }
            GameEngine.logColored("deattachRequest: unit and slot don't match - requested:" + orderableUnit.getUnitDebugName() + " current:" + strCB);
            return false;
        }
        if (this.transportedUnits.remove(orderableUnit)) {
            unloadTransportedUnit(orderableUnit);
        }
        AttachmentManagerHook.a(this, attachmentSlotDefinition, (OrderableUnit) null);
        orderableUnit.parentEntity = null;
        orderableUnit.attachmentData = null;
        orderableUnit.f_();
        a(UnitEventType.attachmentRemoved, this);
        return true;
    }

    public boolean dH() {
        AttachmentSlotDefinition attachmentSlotDefinitionDn = dn();
        if (attachmentSlotDefinitionDn != null && !attachmentSlotDefinitionDn.l) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: J */
    public boolean isDamageImmune() {
        if (dH() || this.unitConfig.canNotBeDamaged) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: di */
    public void applyBorrowedResources() {
        CustomUnitConfig customUnitConfig = this.unitConfig;
        if (!customUnitConfig.borrowResourcesWhileAlive.c()) {
            customUnitConfig.borrowResourcesWhileAlive.a(this);
        }
        if (!customUnitConfig.borrowResourcesWhileBuilt.c() && this.buildProgress >= 1.0f) {
            customUnitConfig.borrowResourcesWhileBuilt.a(this);
        }
        super.applyBorrowedResources();
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: dj */
    public void restoreBorrowedResources() {
        CustomUnitConfig customUnitConfig = this.unitConfig;
        if (!customUnitConfig.borrowResourcesWhileAlive.c()) {
            customUnitConfig.borrowResourcesWhileAlive.h(this);
        }
        if (!customUnitConfig.borrowResourcesWhileBuilt.c() && this.buildProgress >= 1.0f) {
            customUnitConfig.borrowResourcesWhileBuilt.h(this);
        }
        super.restoreBorrowedResources();
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void a(UnitCommand unitCommand) {
        UnitEventRuntime.enqueueWaypoint(this, unitCommand);
        a(UnitEventType.newWaypointGivenByPlayer);
        AttachmentSlotDefinition attachmentSlotDefinitionDn = dn();
        if (attachmentSlotDefinitionDn != null && attachmentSlotDefinitionDn.H) {
            UnitCommandType commandType = unitCommand.getCommandType();
            if (commandType == UnitCommandType.attackMove || commandType == UnitCommandType.move) {
                bx();
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean c_() {
        CustomUnitConfig customUnitConfig = this.unitConfig;
        if (!customUnitConfig.showOnMinimapToEnemies && GameEngine.getInstance().playerTeam.c(this.team)) {
            return false;
        }
        return customUnitConfig.showOnMinimap;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean dk() {
        return this.unitConfig.imageSmoothingWhenZoomedIn;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean dl() {
        return this.unitConfig.disableLowHpFire;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean dm() {
        return this.unitConfig.disableLowHpSmoke;
    }

    /* JADX INFO: renamed from: G */
    public boolean hasTransportSpaceFor(BaseUnit baseUnit) {
        int iDI = dI();
        int unitAIPathfindIterations = baseUnit.getTransportSlotsNeeded();
        if (this.unitConfig.transportUnitsEachUnitAlwaysUsesSingleSlot) {
            unitAIPathfindIterations = 1;
        }
        if (iDI + unitAIPathfindIterations <= this.unitConfig.maxTransportingUnits) {
            return true;
        }
        return false;
    }

    public int dI() {
        int unitAIPathfindIterations = 0;
        if (this.unitConfig.transportUnitsEachUnitAlwaysUsesSingleSlot) {
            unitAIPathfindIterations = 0 + this.transportedUnits.size();
        } else if (this.transportedUnits.size > 0) {
            Iterator it = this.transportedUnits.iterator();
            while (it.hasNext()) {
                unitAIPathfindIterations += ((BaseUnit) it.next()).getTransportSlotsNeeded();
            }
        }
        return unitAIPathfindIterations;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: e */
    public void changeTeam(PlayerTeam playerTeam) {
        AttachmentSlotDefinition attachmentSlotDefinitionDn;
        if (this.team == playerTeam) {
            return;
        }
        super.changeTeam(playerTeam);
        if (this.transportedUnits != null && !this.unitConfig.transportUnitsOnTeamChangeKeepCurrentTeam) {
            Iterator it = this.transportedUnits.iterator();
            while (it.hasNext()) {
                ((BaseUnit) it.next()).changeTeam(playerTeam);
            }
        }
        if (this.C != null) {
            for (BaseUnit baseUnit : this.C) {
                if (baseUnit != null && (attachmentSlotDefinitionDn = baseUnit.dn()) != null && !attachmentSlotDefinitionDn.z) {
                    baseUnit.changeTeam(playerTeam);
                }
            }
        }
        a(UnitEventType.teamChanged);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: cN */
    public UnitPrice getReclaimPrice() {
        return this.unitConfig.reclaimPrice;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: ch */
    public void checkDeathOnZeroHp() {
        if (!this.unitConfig.disableDeathOnZeroHp) {
            super.checkDeathOnZeroHp();
        } else if (this.currentHealth <= -1.0f) {
            this.currentHealth = -1.0f;
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: bz */
    public FastArrayList getTransportedUnitList() {
        return this.transportedUnits;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public FastArrayList e(boolean z) {
        this.eg.clear();
        if (this.unitConfig.attachmentSlotDefinitions.size > 0) {
            AttachmentManagerHook.a(this, this.eg, z);
        }
        if (this.eg.size > 0) {
            return this.eg;
        }
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: do */
    public float mo145do() {
        return this.unitConfig.uiTargetRadius;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean dp() {
        return this.unitConfig.showSelectionIndicator;
    }

    public void dJ() {
        if (this.y.a) {
            this.y = this.y.b();
        }
    }
}
