package com.corrodinggames.rts.gameFramework.ui;

import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.UnitTypeEnum;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.al */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/al.class */
abstract class UnitSelectionFilter {
    /* JADX INFO: renamed from: a */
    static UnitSelectionFilter attackUnits = new UnitSelectionFilter() { // from class: com.corrodinggames.rts.gameFramework.f.al.1
        @Override // com.corrodinggames.rts.gameFramework.ui.UnitSelectionFilter
        public boolean matches(OrderableUnit orderableUnit) {
            if (orderableUnit.canUnitAttack() && !orderableUnit.u() && orderableUnit.transportContainer == null && orderableUnit.hasNoCurrentWaypoint()) {
                return true;
            }
            return false;
        }
    };
    /* JADX INFO: renamed from: b */
    static UnitSelectionFilter attackUnitsIgnoreWaypoints = new UnitSelectionFilter() { // from class: com.corrodinggames.rts.gameFramework.f.al.2
        @Override // com.corrodinggames.rts.gameFramework.ui.UnitSelectionFilter
        public boolean matches(OrderableUnit orderableUnit) {
            if (orderableUnit.canUnitAttack() && !orderableUnit.u() && orderableUnit.transportContainer == null) {
                return true;
            }
            return false;
        }
    };
    /* JADX INFO: renamed from: c */
    static UnitSelectionFilter buildingUnits = new UnitSelectionFilter() { // from class: com.corrodinggames.rts.gameFramework.f.al.3
        @Override // com.corrodinggames.rts.gameFramework.ui.UnitSelectionFilter
        public boolean matches(OrderableUnit orderableUnit) {
            if (orderableUnit.r() != null && orderableUnit.r().p() && orderableUnit.transportContainer == null) {
                return true;
            }
            return false;
        }
    };
    /* JADX INFO: renamed from: d */
    static UnitSelectionFilter fabricatorUnits = new UnitSelectionFilter() { // from class: com.corrodinggames.rts.gameFramework.f.al.4
        @Override // com.corrodinggames.rts.gameFramework.ui.UnitSelectionFilter
        public boolean matches(OrderableUnit orderableUnit) {
            if (orderableUnit.r() == UnitTypeEnum.fabricator && orderableUnit.getUpgradeLevel() < 3 && orderableUnit.transportContainer == null) {
                return true;
            }
            return false;
        }
    };
    /* JADX INFO: renamed from: e */
    static UnitSelectionFilter landFactoryUnits = new UnitSelectionFilter() { // from class: com.corrodinggames.rts.gameFramework.f.al.5
        @Override // com.corrodinggames.rts.gameFramework.ui.UnitSelectionFilter
        public boolean matches(OrderableUnit orderableUnit) {
            if (orderableUnit.r() == UnitTypeEnum.landFactory && orderableUnit.transportContainer == null) {
                return true;
            }
            return false;
        }
    };
    /* JADX INFO: renamed from: f */
    static UnitSelectionFilter airFactoryUnits = new UnitSelectionFilter() { // from class: com.corrodinggames.rts.gameFramework.f.al.6
        @Override // com.corrodinggames.rts.gameFramework.ui.UnitSelectionFilter
        public boolean matches(OrderableUnit orderableUnit) {
            if (orderableUnit.r() == UnitTypeEnum.airFactory && orderableUnit.transportContainer == null) {
                return true;
            }
            return false;
        }
    };

    /* JADX INFO: renamed from: a */
    public static void selectMatchingUnits(ArrayList arrayList, UnitSelectionFilter filter, UnitSelectionFilter filter2) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine.gameUI.getSelectedUnitCount() != 1) {
            arrayList.clear();
        }
        OrderableUnit firstControllableSelectedUnit = gameEngine.gameUI.getFirstControllableSelectedUnit();
        if (firstControllableSelectedUnit != null) {
            if (filter.matches(firstControllableSelectedUnit) || (filter2 != null && filter2.matches(firstControllableSelectedUnit))) {
                if (!arrayList.contains(firstControllableSelectedUnit)) {
                    arrayList.add(firstControllableSelectedUnit);
                }
            } else {
                arrayList.clear();
            }
        }
        OrderableUnit orderableUnitA = findClosestMatchingUnit(arrayList, filter);
        if (orderableUnitA == null && filter2 != null) {
            orderableUnitA = findClosestMatchingUnit(arrayList, filter2);
        }
        if (orderableUnitA == null) {
            arrayList.clear();
            if (firstControllableSelectedUnit != null) {
                arrayList.add(firstControllableSelectedUnit);
            }
            orderableUnitA = findClosestMatchingUnit(arrayList, filter);
            if (orderableUnitA == null && filter2 != null) {
                orderableUnitA = findClosestMatchingUnit(arrayList, filter2);
            }
        }
        if (orderableUnitA != null) {
            gameEngine.gameUI.clearSelection();
            gameEngine.gameUI.selectUnit(orderableUnitA);
            gameEngine.centerViewpoint(orderableUnitA.posX, orderableUnitA.posY);
            arrayList.add(orderableUnitA);
        }
    }

    UnitSelectionFilter() {
    }

    /* JADX INFO: renamed from: a */
    public static OrderableUnit findClosestMatchingUnit(ArrayList arrayList, UnitSelectionFilter filter) {
        GameEngine gameEngine = GameEngine.getInstance();
        OrderableUnit orderableUnit = null;
        float f2 = -1.0f;
        for (BaseUnit baseUnit : BaseUnit.bE) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit2 = (OrderableUnit) baseUnit;
                if (gameEngine.gameUI.canControlUnit(orderableUnit2) && filter.matches(orderableUnit2) && !arrayList.contains(orderableUnit2)) {
                    float fDistanceSq = Utility.distanceSq(gameEngine.viewpointX + gameEngine.halfVisibleWorldWidth, gameEngine.viewpointY + gameEngine.halfVisibleWorldHeight, orderableUnit2.posX, orderableUnit2.posY);
                    if (orderableUnit == null || fDistanceSq < f2) {
                        f2 = fDistanceSq;
                        orderableUnit = orderableUnit2;
                    }
                }
            }
        }
        return orderableUnit;
    }

    /* JADX INFO: renamed from: a */
    public abstract boolean matches(OrderableUnit orderableUnit);
}
