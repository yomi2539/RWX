package com.corrodinggames.rts.gameFramework.ui;

import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.actions.AbstractUnitAction;
import com.corrodinggames.rts.game.units.custom.condition.StoredResources;
import com.corrodinggames.rts.game.units.custom.logicBooleans.LogicBoolean;
import com.corrodinggames.rts.game.units.custom.price.UnitPrice;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.statistics.PlaceholderUnit;
import com.corrodinggames.rts.gameFramework.utility.FastArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.an */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/an.class */
public class LagHidingManager {
    /* JADX INFO: renamed from: b */
    static final PlaceholderUnit placeholderUnit = new PlaceholderUnit();
    /* JADX INFO: renamed from: a */
    static FastArrayList snapshots = new FastArrayList();

    /* JADX INFO: renamed from: a */
    public static UnitSnapshot getSnapshotByUnitId(long j) {
        Object[] objArrA = snapshots.a();
        for (int i = snapshots.size - 1; i >= 0; i--) {
            UnitSnapshot unitSnapshot = (UnitSnapshot) objArrA[i];
            if (unitSnapshot.unitId == j) {
                return unitSnapshot;
            }
        }
        return null;
    }

    /* JADX INFO: renamed from: a */
    public static UnitSnapshot getOrCreateSnapshot(BaseUnit baseUnit) {
        UnitSnapshot unitSnapshotA = getSnapshotByUnitId(baseUnit.objectId);
        if (unitSnapshotA == null) {
            unitSnapshotA = new UnitSnapshot();
            unitSnapshotA.unitId = baseUnit.objectId;
            unitSnapshotA.ammo = baseUnit.ammo;
            unitSnapshotA.unitFlags = baseUnit.unitFlags;
            unitSnapshotA.blockingFrame = GameEngine.getInstance().networkEngine.nextBlockingFrame;
            snapshots.add(unitSnapshotA);
        }
        return unitSnapshotA;
    }

    /* JADX INFO: renamed from: a */
    public static void addResourcesToSnapshot(BaseUnit baseUnit, UnitPrice unitPrice) {
        if (!GameEngine.getInstance().networkEngine.networkGameActive) {
            return;
        }
        UnitSnapshot unitSnapshotA = getOrCreateSnapshot(baseUnit);
        unitSnapshotA.ammo += unitPrice.f;
        unitSnapshotA.unitFlags = unitPrice.c(unitSnapshotA.unitFlags);
        if (!unitPrice.k.c()) {
            unitSnapshotA.resources = StoredResources.b(unitSnapshotA.resources, unitPrice.k);
        }
    }

    /* JADX INFO: renamed from: b */
    public static void removeResourcesFromSnapshot(BaseUnit baseUnit, UnitPrice unitPrice) {
        if (!GameEngine.getInstance().networkEngine.networkGameActive) {
            return;
        }
        UnitSnapshot unitSnapshotA = getOrCreateSnapshot(baseUnit);
        unitSnapshotA.ammo -= unitPrice.f;
        unitSnapshotA.unitFlags = unitPrice.c(unitSnapshotA.unitFlags);
        if (!unitPrice.k.c()) {
            unitSnapshotA.resources = StoredResources.a(unitSnapshotA.resources, unitPrice.k);
        }
        if (snapshots.size > 0) {
        }
    }

    /* JADX INFO: renamed from: c */
    public static boolean canAffordWithSnapshots(BaseUnit baseUnit, UnitPrice unitPrice) {
        UnitSnapshot unitSnapshotA = getSnapshotByUnitId(baseUnit.objectId);
        if (unitSnapshotA != null) {
            placeholderUnit.team = baseUnit.team;
            placeholderUnit.ammo = unitSnapshotA.ammo;
            placeholderUnit.unitFlags = unitSnapshotA.unitFlags;
            StoredResources unitAICombatRange = placeholderUnit.getCustomResources();
            placeholderUnit.a(unitSnapshotA.resources);
            boolean zB = unitPrice.b(placeholderUnit);
            placeholderUnit.a(unitAICombatRange);
            return zB;
        }
        return unitPrice.b(baseUnit);
    }

    /* JADX INFO: renamed from: a */
    public static boolean readLogicBooleanWithSnapshot(LogicBoolean logicBoolean, OrderableUnit orderableUnit) {
        UnitSnapshot unitSnapshotA = getSnapshotByUnitId(orderableUnit.objectId);
        if (unitSnapshotA != null) {
            int i = orderableUnit.ammo;
            int i2 = orderableUnit.unitFlags;
            orderableUnit.ammo = unitSnapshotA.ammo;
            orderableUnit.unitFlags = unitSnapshotA.unitFlags;
            boolean z = logicBoolean.read(orderableUnit);
            orderableUnit.ammo = i;
            orderableUnit.unitFlags = i2;
            return z;
        }
        return logicBoolean.read(orderableUnit);
    }

    /* JADX INFO: renamed from: a */
    public static void clearSnapshots() {
        if (snapshots.size > 0) {
            GameEngine.log("LagHiding: clearing: " + snapshots.size);
        }
        snapshots.clear();
    }

    /* JADX INFO: renamed from: a */
    public static void onUnitActionStarted(OrderableUnit orderableUnit, AbstractUnitAction abstractUnitAction) {
        if (snapshots.size() == 0) {
            return;
        }
        int i = GameEngine.getInstance().networkEngine.nextBlockingFrame;
        for (int size = snapshots.size() - 1; size >= 0; size--) {
            UnitSnapshot unitSnapshot = (UnitSnapshot) snapshots.get(size);
            if (unitSnapshot.unitId == orderableUnit.objectId) {
                snapshots.remove(size);
                return;
            } else {
                if (unitSnapshot.blockingFrame < i + 80) {
                    snapshots.remove(size);
                    return;
                }
            }
        }
    }
}
