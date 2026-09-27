package com.corrodinggames.rts.game.units.custom.hooks;

import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.actions.AbstractUnitAction;
import com.corrodinggames.rts.game.units.actions.WrapperUnitAction;
import com.corrodinggames.rts.game.units.custom.ConfigParseException;
import com.corrodinggames.rts.game.units.custom.CustomUnit;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig;
import com.corrodinggames.rts.game.units.custom.UnitSpawner;
import com.corrodinggames.rts.game.units.custom.logicBooleans.LogicBoolean;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.ui.LagHidingManager;
import com.corrodinggames.rts.gameFramework.utility.FastArrayList;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;
import com.corrodinggames.rts.gameFramework.utility.IniFile;

import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.custom.b.m */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/custom/b/m.class */
public final class AttachmentManagerHook extends CustomUnitRenderHook {
    public static final AttachmentManagerHook a = new AttachmentManagerHook();

    public static void a(CustomUnitConfig customUnitConfig, IniFile iniFile) throws ConfigParseException {
        FastArrayList<String> sectionsStartingWith = iniFile.getSectionsStartingWith("attachment_");
        if (sectionsStartingWith.size() > 0) {
            customUnitConfig.a(a);
            short s = 0;
            for (String str : sectionsStartingWith) {
                String strSubstring = str.substring("attachment_".length());
                AttachmentSlotDefinition attachmentSlotDefinition = new AttachmentSlotDefinition();
                a(attachmentSlotDefinition, customUnitConfig, iniFile, str, strSubstring);
                attachmentSlotDefinition.b = strSubstring;
                attachmentSlotDefinition.a = s;
                s = (short) (s + 1);
                customUnitConfig.attachmentSlotDefinitions.add(attachmentSlotDefinition);
            }
        }
    }

    public static void a(AttachmentSlotDefinition attachmentSlotDefinition, CustomUnitConfig customUnitConfig, IniFile iniFile, String str, String str2) throws ConfigParseException {
        attachmentSlotDefinition.c = iniFile.getFloatStrictRaw(str, "x");
        attachmentSlotDefinition.d = iniFile.getFloatStrictRaw(str, "y");
        attachmentSlotDefinition.e = iniFile.getFloat(str, "height", Float.valueOf(attachmentSlotDefinition.e)).floatValue();
        attachmentSlotDefinition.i = iniFile.getBoolean(str, "lockDir", Boolean.valueOf(attachmentSlotDefinition.i)).booleanValue();
        attachmentSlotDefinition.j = iniFile.getBoolean(str, "redirectDamageToParent", Boolean.valueOf(attachmentSlotDefinition.j)).booleanValue();
        attachmentSlotDefinition.k = iniFile.getBoolean(str, "redirectDamageToParent_shieldOnly", Boolean.valueOf(attachmentSlotDefinition.k)).booleanValue();
        if (!attachmentSlotDefinition.j && attachmentSlotDefinition.k) {
            throw new ConfigParseException("[" + str + "] redirectDamageToParent_shieldOnly requires redirectDamageToParent");
        }
        attachmentSlotDefinition.l = iniFile.getBoolean(str, "canBeAttackedAndDamaged", Boolean.valueOf(attachmentSlotDefinition.l)).booleanValue();
        attachmentSlotDefinition.m = iniFile.getBoolean(str, "isUnselectable", Boolean.valueOf(attachmentSlotDefinition.m)).booleanValue();
        attachmentSlotDefinition.n = iniFile.getBoolean(str, "isUnselectableAsTarget", Boolean.valueOf(attachmentSlotDefinition.m)).booleanValue();
        attachmentSlotDefinition.o = iniFile.getBoolean(str, "isVisible", Boolean.valueOf(attachmentSlotDefinition.o)).booleanValue();
        attachmentSlotDefinition.p = iniFile.getBoolean(str, "showMiniHp", Boolean.valueOf(attachmentSlotDefinition.p)).booleanValue();
        attachmentSlotDefinition.q = iniFile.getBoolean(str, "hideHp", Boolean.valueOf(attachmentSlotDefinition.q)).booleanValue();
        attachmentSlotDefinition.N = iniFile.getLogicBoolean(customUnitConfig, str, "showAllActionsFrom", (LogicBoolean) null);
        if (LogicBoolean.isStaticFalse(attachmentSlotDefinition.N)) {
            attachmentSlotDefinition.N = null;
        }
        Float f = iniFile.getFloat(str, "idleDir", (Float) null);
        Float f2 = iniFile.getFloat(str, "idleDirReversing", (Float) null);
        if (f != null) {
            attachmentSlotDefinition.f = f.floatValue();
            attachmentSlotDefinition.g = f.floatValue();
        }
        if (f2 != null) {
            attachmentSlotDefinition.g = f2.floatValue();
        } else {
            attachmentSlotDefinition.g = attachmentSlotDefinition.f;
        }
        attachmentSlotDefinition.h = iniFile.getBoolean(str, "resetRotationWhenNotAttacking", (Boolean) false).booleanValue();
        attachmentSlotDefinition.r = iniFile.getBoolean(str, "rotateWithParent", Boolean.valueOf(attachmentSlotDefinition.r)).booleanValue();
        attachmentSlotDefinition.s = iniFile.getBoolean(str, "lockLegMovement", Boolean.valueOf(attachmentSlotDefinition.s)).booleanValue();
        attachmentSlotDefinition.t = iniFile.getBoolean(str, "freezeLegMovement", Boolean.valueOf(attachmentSlotDefinition.t)).booleanValue();
        attachmentSlotDefinition.u = iniFile.getBoolean(str, "lockRotation", Boolean.valueOf(attachmentSlotDefinition.u)).booleanValue();
        if (attachmentSlotDefinition.u && attachmentSlotDefinition.h) {
            throw new ConfigParseException("[" + str + "] Cannot use lockRotation and resetRotationWhenIdle at same time");
        }
        attachmentSlotDefinition.v = iniFile.getBoolean(str, "keepAliveWhenParentDies", Boolean.valueOf(attachmentSlotDefinition.v)).booleanValue();
        attachmentSlotDefinition.w = UnitSpawner.b(customUnitConfig, iniFile, str, "onCreateSpawnUnitOf");
        if (attachmentSlotDefinition.w.b()) {
            attachmentSlotDefinition.w = null;
        }
        attachmentSlotDefinition.x = iniFile.getBoolean(str, "createIncompleteIfParentIs", Boolean.valueOf(attachmentSlotDefinition.x)).booleanValue();
        attachmentSlotDefinition.y = iniFile.getBoolean(str, "onConvertKeepExistingUnitInSameSlot", Boolean.valueOf(attachmentSlotDefinition.y)).booleanValue();
        attachmentSlotDefinition.z = iniFile.getBoolean(str, "onParentTeamChangeKeepCurrentTeam", Boolean.valueOf(attachmentSlotDefinition.z)).booleanValue();
        attachmentSlotDefinition.B = iniFile.getBoolean(str, "setDrawLayerOnBottom", Boolean.valueOf(attachmentSlotDefinition.B)).booleanValue();
        if (attachmentSlotDefinition.B) {
            attachmentSlotDefinition.A = false;
        }
        attachmentSlotDefinition.A = iniFile.getBoolean(str, "setDrawLayerOnTop", Boolean.valueOf(attachmentSlotDefinition.A)).booleanValue();
        if (attachmentSlotDefinition.A && attachmentSlotDefinition.B) {
            throw new ConfigParseException("[" + str + "] Cannot use setDrawLayerOnTop and setDrawLayerOnBottom at same time");
        }
        attachmentSlotDefinition.D = iniFile.getBoolean(str, "addTransportedUnits", Boolean.valueOf(attachmentSlotDefinition.D)).booleanValue();
        attachmentSlotDefinition.E = iniFile.getBoolean(str, "unloadInCurrentPosition", Boolean.valueOf(attachmentSlotDefinition.E)).booleanValue();
        attachmentSlotDefinition.F = iniFile.getBoolean(str, "smoothlyBlendPositionWhenExistingUnitAdded", Boolean.valueOf(attachmentSlotDefinition.F)).booleanValue();
        if (attachmentSlotDefinition.F) {
            attachmentSlotDefinition.G = 500.0f;
        } else {
            attachmentSlotDefinition.G = 0.0f;
        }
        attachmentSlotDefinition.H = iniFile.getBoolean(str, "deattachIfWantingToMove", Boolean.valueOf(attachmentSlotDefinition.H)).booleanValue();
        attachmentSlotDefinition.I = iniFile.getBoolean(str, "hidden", Boolean.valueOf(attachmentSlotDefinition.I)).booleanValue();
        attachmentSlotDefinition.J = iniFile.getBoolean(str, "prioritizeParentsMainTarget", Boolean.valueOf(attachmentSlotDefinition.J)).booleanValue();
        attachmentSlotDefinition.K = iniFile.getBoolean(str, "onlyAttackParentsMainTarget", Boolean.valueOf(attachmentSlotDefinition.K)).booleanValue();
        attachmentSlotDefinition.L = iniFile.getBoolean(str, "alwaysAllowedToAttackParentsMainTarget", Boolean.valueOf(attachmentSlotDefinition.L)).booleanValue();
        attachmentSlotDefinition.M = iniFile.getBoolean(str, "canAttack", Boolean.valueOf(attachmentSlotDefinition.M)).booleanValue();
        attachmentSlotDefinition.O = iniFile.getBoolean(str, "keepWaypointsNeedingMovement", Boolean.valueOf(attachmentSlotDefinition.O)).booleanValue();
        if (attachmentSlotDefinition.D) {
            customUnitConfig.attachmentBool = true;
        }
    }

    @Override // com.corrodinggames.rts.game.units.custom.hooks.CustomUnitRenderHook
    public void a(CustomUnit customUnit, float f) {
        b(customUnit, f);
    }

    public static AttachmentSlotDefinition a(CustomUnit customUnit, short s) {
        FastArrayList fastArrayList = customUnit.unitConfig.attachmentSlotDefinitions;
        if (fastArrayList.size <= s) {
            return null;
        }
        return (AttachmentSlotDefinition) fastArrayList.get(s);
    }

    public static boolean a(CustomUnit customUnit, AttachmentSlotDefinition attachmentSlotDefinition, OrderableUnit orderableUnit) {
        CustomUnitConfig customUnitConfig = customUnit.unitConfig;
        short s = attachmentSlotDefinition.a;
        if (customUnitConfig.attachmentSlotDefinitions.size <= s && orderableUnit != null) {
            GameEngine.logColored("setAttachedUnitLookup: slot:" + ((int) s) + " larger than max slot size:" + customUnitConfig.attachmentSlotDefinitions.size);
            return false;
        }
        if (customUnit.C == null) {
            customUnit.C = new FastArrayList();
        }
        FastArrayList fastArrayList = customUnit.C;
        if (fastArrayList.size() == 0) {
            customUnit.D = customUnit.rotationSpeed;
        }
        if (orderableUnit == null && s >= fastArrayList.size()) {
            return true;
        }
        while (fastArrayList.size() <= s) {
            fastArrayList.add(null);
        }
        fastArrayList.set(s, orderableUnit);
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.custom.hooks.CustomUnitRenderHook
    public void b(CustomUnit customUnit) {
        a(customUnit, true);
    }

    @Override // com.corrodinggames.rts.game.units.custom.hooks.CustomUnitRenderHook
    public void c(CustomUnit customUnit) {
        a(customUnit, true);
    }

    @Override // com.corrodinggames.rts.game.units.custom.hooks.CustomUnitRenderHook
    public void b(CustomUnit customUnit, float f) {
        float f2;
        GameEngine gameEngine = GameEngine.getInstance();
        CustomUnitConfig customUnitConfig = customUnit.unitConfig;
        FastArrayList fastArrayList = customUnitConfig.attachmentSlotDefinitions;
        if (fastArrayList.size == 0) {
            return;
        }
        if (customUnitConfig.attachmentBool) {
            Object[] objArrA = fastArrayList.a();
            for (int i = 0; i < fastArrayList.size; i++) {
                AttachmentSlotDefinition attachmentSlotDefinition = (AttachmentSlotDefinition) objArrA[i];
                if (attachmentSlotDefinition.D && customUnit.transportedUnits.size > 0 && a(customUnit, attachmentSlotDefinition) == null) {
                    Iterator it = customUnit.transportedUnits.iterator();
                    while (it.hasNext()) {
                        BaseUnit baseUnit = (BaseUnit) it.next();
                        if ((baseUnit instanceof OrderableUnit) && baseUnit.parentEntity == null && customUnit.a((OrderableUnit) baseUnit, attachmentSlotDefinition)) {
                            baseUnit.transportContainer = null;
                            break;
                        }
                    }
                }
            }
        }
        FastArrayList fastArrayList2 = customUnit.C;
        if (fastArrayList2 == null) {
            return;
        }
        float f3 = customUnit.rotationSpeed - customUnit.D;
        customUnit.D = customUnit.rotationSpeed;
        Object[] objArrA2 = fastArrayList2.a();
        for (int i2 = fastArrayList2.size - 1; i2 >= 0; i2--) {
            OrderableUnit orderableUnit = (OrderableUnit) objArrA2[i2];
            if (orderableUnit != null) {
                if (orderableUnit.isDead) {
                    orderableUnit.bx();
                    objArrA2[i2] = null;
                } else {
                    if (customUnit.transportContainer != null) {
                        if (orderableUnit.transportContainer == null) {
                            orderableUnit.transportContainer = customUnit.transportContainer;
                            gameEngine.gameUI.deselectUnit(orderableUnit);
                        }
                    } else if (orderableUnit.transportContainer != null && orderableUnit.transportContainer != customUnit) {
                        orderableUnit.transportContainer = null;
                    }
                    AttachmentSlotDefinition attachmentSlotDefinition2 = (AttachmentSlotDefinition) fastArrayList.get(i2);
                    float fFastCos = Utility.fastCos(customUnit.rotationSpeed);
                    float fFastSin = Utility.fastSin(customUnit.rotationSpeed);
                    float f4 = (fFastCos * attachmentSlotDefinition2.d) - (fFastSin * attachmentSlotDefinition2.c);
                    float f5 = (fFastSin * attachmentSlotDefinition2.d) + (fFastCos * attachmentSlotDefinition2.c);
                    float f6 = f4 + customUnit.posX;
                    float f7 = f5 + customUnit.posY;
                    float f8 = customUnit.posZ + attachmentSlotDefinition2.e;
                    if (GameViewUtils.b(orderableUnit.attachmentStartTimeMillis, (int) attachmentSlotDefinition2.G)) {
                        orderableUnit.posX += (f6 - orderableUnit.posX) * 0.05f;
                        orderableUnit.posY += (f7 - orderableUnit.posY) * 0.05f;
                        orderableUnit.posZ += (f8 - orderableUnit.posZ) * 0.05f;
                    } else {
                        orderableUnit.posX = f6;
                        orderableUnit.posY = f7;
                        orderableUnit.posZ = f8;
                    }
                    if (orderableUnit.buildProgress < 1.0f && attachmentSlotDefinition2.x) {
                        orderableUnit.r(customUnit.buildProgress);
                        orderableUnit.paidBuildProgress = customUnit.buildProgress;
                    }
                    if (attachmentSlotDefinition2.A) {
                        if (orderableUnit.drawLayer <= customUnit.drawLayer) {
                            int i3 = 0;
                            if (orderableUnit instanceof CustomUnit) {
                                i3 = ((CustomUnit) orderableUnit).unitConfig.drawLayer;
                            }
                            orderableUnit.drawLayer = customUnit.drawLayer;
                            orderableUnit.drawOrder = customUnit.drawOrder + 1 + i3;
                        }
                    } else if (attachmentSlotDefinition2.B && orderableUnit.drawLayer >= customUnit.drawLayer) {
                        orderableUnit.drawLayer = customUnit.drawLayer;
                        orderableUnit.drawOrder = customUnit.drawOrder - 1;
                    }
                    if (customUnit.isRotating) {
                        f2 = customUnit.rotationSpeed + attachmentSlotDefinition2.g;
                    } else {
                        f2 = customUnit.rotationSpeed + attachmentSlotDefinition2.f;
                    }
                    if (!orderableUnit.bI()) {
                        if (attachmentSlotDefinition2.u) {
                            orderableUnit.h(f2);
                        } else {
                            if (f3 != 0.0f && attachmentSlotDefinition2.r) {
                                orderableUnit.addRotation(f3);
                            }
                            if (attachmentSlotDefinition2.h && orderableUnit.attackTarget == null) {
                                orderableUnit.faceTowardPosition(f, f2);
                            }
                        }
                    }
                    if (attachmentSlotDefinition2.K) {
                        orderableUnit.attackTarget = customUnit.attackTarget;
                        orderableUnit.turretRotation = 5.0f;
                    }
                    if (attachmentSlotDefinition2.L && orderableUnit.attackTarget == null) {
                        orderableUnit.attackTarget = customUnit.attackTarget;
                    }
                    if (attachmentSlotDefinition2.J && customUnit.attackTarget != null && orderableUnit.attackTarget != customUnit.attackTarget) {
                        boolean z = false;
                        if (attachmentSlotDefinition2.L) {
                            z = true;
                        }
                        if (orderableUnit.a(customUnit.attackTarget, z)) {
                            orderableUnit.attackTarget = customUnit.attackTarget;
                            orderableUnit.turretRotation = 5.0f;
                        }
                    }
                    if (orderableUnit instanceof CustomUnit) {
                        CustomUnit customUnit2 = (CustomUnit) orderableUnit;
                        if (attachmentSlotDefinition2.s) {
                            customUnit2.dP = customUnit2.posX;
                            customUnit2.dP = customUnit2.posY;
                            customUnit2.dR = customUnit2.posZ;
                        }
                    }
                }
            }
        }
    }

    public void a(CustomUnit customUnit, boolean z) {
        FastArrayList fastArrayList = customUnit.C;
        if (fastArrayList == null) {
            return;
        }
        FastArrayList fastArrayList2 = customUnit.unitConfig.attachmentSlotDefinitions;
        Object[] objArrA = fastArrayList.a();
        for (int i = fastArrayList.size - 1; i >= 0; i--) {
            OrderableUnit orderableUnit = (OrderableUnit) objArrA[i];
            if (orderableUnit != null) {
                AttachmentSlotDefinition attachmentSlotDefinition = (AttachmentSlotDefinition) fastArrayList2.get(i);
                orderableUnit.bx();
                objArrA[i] = null;
                if (z && !attachmentSlotDefinition.v) {
                    orderableUnit.removeFromGame();
                }
            }
        }
    }

    @Override
    public void a(CustomUnit j) {
        boolean var2 = false;
        FastArrayList var3 = j.unitConfig.attachmentSlotDefinitions;
        Object[] var4 = var3.a();

        for (int var5 = var3.size - 1; var5 >= 0; var5--) {
            AttachmentSlotDefinition var6 = (AttachmentSlotDefinition) var4[var5];
            if (var6.w != null) {
                OrderableUnit var7 = a(j, var6);
                if (var7 != null) {
                    if (var6.y) {
                        continue;
                    }

                    var7.removeFromGame();
                }

                FastArrayList var8 = new FastArrayList();
                var6.w.a(var8, j.team, j, true);
                if (var8.size() > 1) {
                    GameEngine.logColored("onCreateSpawnUnitOf: created an extra " + (var8.size() - 1) + " units");

                    for (int var9 = 1; var9 < var8.size(); var9++) {
                        ((BaseUnit) var8.get(var9)).removeFromGame();
                    }
                }

                if (var8.size() == 0) {
                    GameEngine.logColored("onCreateSpawnUnitOf: Warning no units created");
                } else {
                    BaseUnit var11 = (BaseUnit) var8.get(0);
                    if (!(var11 instanceof OrderableUnit)) {
                        GameEngine.logColored("onCreateSpawnUnitOf: Warning " + var11.r().getUnitTypeDescriptionShort() + " not an orderable unit type, cannot attach");
                        var11.removeFromGame();
                    } else {
                        OrderableUnit var10 = (OrderableUnit) var11;
                        if (j.a(var10, var6)) {
                            var10.attachmentStartTimeMillis = -9999;
                            if (j.buildProgress < 1.0F && var6.x) {
                                var10.r(j.buildProgress);
                                var10.paidBuildProgress = j.buildProgress;
                            }

                            var2 = true;
                        }
                    }
                }
            }
        }

        if (var2) {
            this.b(j, 0.0F);
        }
    }

    public static OrderableUnit a(CustomUnit customUnit, AttachmentSlotDefinition attachmentSlotDefinition) {
        short s;
        FastArrayList fastArrayList = customUnit.C;
        if (fastArrayList == null || fastArrayList.size <= (s = attachmentSlotDefinition.a)) {
            return null;
        }
        return (OrderableUnit) fastArrayList.get(s);
    }

    @Override // com.corrodinggames.rts.game.units.custom.hooks.CustomUnitRenderHook
    public void a(CustomUnit customUnit, CustomUnitConfig customUnitConfig) {
        FastArrayList fastArrayList = customUnit.C;
        FastArrayList fastArrayList2 = customUnit.unitConfig.attachmentSlotDefinitions;
        if (fastArrayList2.size() == 0) {
            customUnit.C = null;
            return;
        }
        if (fastArrayList == null) {
            return;
        }
        for (int size = fastArrayList.size() - 1; size >= 0; size--) {
            OrderableUnit orderableUnit = (OrderableUnit) fastArrayList.get(size);
            if (orderableUnit != null && size >= fastArrayList2.size()) {
                orderableUnit.removeFromGame();
                fastArrayList.remove(size);
            }
        }
        for (int size2 = fastArrayList.size() - 1; size2 >= 0; size2--) {
            OrderableUnit orderableUnit2 = (OrderableUnit) fastArrayList.get(size2);
            if (orderableUnit2 != null) {
                orderableUnit2.attachmentData = (AttachmentSlotDefinition) fastArrayList2.get(size2);
            }
        }
    }

    public static void a(CustomUnit customUnit, FastArrayList fastArrayList, boolean z) {
        AttachmentSlotDefinition attachmentSlotDefinitionDn;
        boolean zA;
        FastArrayList<BaseUnit> fastArrayList2 = customUnit.C;
        if (fastArrayList2 != null) {
            for (BaseUnit baseUnit : fastArrayList2) {
                if (baseUnit != null && (baseUnit instanceof OrderableUnit) && (attachmentSlotDefinitionDn = baseUnit.dn()) != null && attachmentSlotDefinitionDn.N != null) {
                    for (AbstractUnitAction abstractUnitAction : baseUnit.getAvailableActions()) {
                        if (z) {
                            zA = LagHidingManager.readLogicBooleanWithSnapshot(attachmentSlotDefinitionDn.N, customUnit);
                        } else {
                            zA = attachmentSlotDefinitionDn.N.read(customUnit);
                        }
                        if (zA) {
                            fastArrayList.add(new WrapperUnitAction(abstractUnitAction, (OrderableUnit) baseUnit, abstractUnitAction.getActionId()));
                        }
                    }
                }
            }
        }
    }
}
