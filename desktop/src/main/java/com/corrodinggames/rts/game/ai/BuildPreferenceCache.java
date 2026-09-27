package com.corrodinggames.rts.game.ai;

import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.UnitType;
import java.util.HashMap;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.a.c */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/a/c.class */
public class BuildPreferenceCache {
    /* JADX INFO: renamed from: a */
    HashMap unitCountWithQueueCache = new HashMap();
    /* JADX INFO: renamed from: b */
    HashMap unitCountCache = new HashMap();
    /* JADX INFO: renamed from: c */
    HashMap buildingUnitCountCache = new HashMap();

    /* JADX INFO: renamed from: a */
    public Integer getCachedCount(boolean z, UnitType unitType, boolean z2) {
        if (z) {
            return (Integer) this.buildingUnitCountCache.get(unitType);
        }
        if (!z2) {
            return (Integer) this.unitCountCache.get(unitType);
        }
        return (Integer) this.unitCountWithQueueCache.get(unitType);
    }

    /* JADX INFO: renamed from: a */
    public void putCachedCount(boolean z, UnitType unitType, boolean z2, Integer num) {
        if (z) {
            this.buildingUnitCountCache.put(unitType, num);
        } else if (!z2) {
            this.unitCountCache.put(unitType, num);
        } else {
            this.unitCountWithQueueCache.put(unitType, num);
        }
    }

    /* JADX INFO: renamed from: a */
    public void clearUnitCountCaches() {
        this.unitCountWithQueueCache.clear();
        this.unitCountCache.clear();
    }

    /* JADX INFO: renamed from: a */
    public void invalidateUnitType(UnitType unitType) {
        this.unitCountWithQueueCache.put(unitType, null);
        this.unitCountCache.put(unitType, null);
    }

    /* JADX INFO: renamed from: a */
    public void invalidateBuiltUnit(OrderableUnit orderableUnit) {
        this.buildingUnitCountCache.put(orderableUnit.unitType, null);
    }

    /* JADX INFO: renamed from: b */
    public void clearBuildingCaches() {
        this.buildingUnitCountCache.clear();
    }
}
