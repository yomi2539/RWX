package com.corrodinggames.rts.game.ai;

import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.UnitCommand;
import com.corrodinggames.rts.game.units.UnitCommandType;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.game.units.actions.AbstractUnitAction;
import com.corrodinggames.rts.game.units.custom.AnimationTag;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig;
import com.corrodinggames.rts.game.units.custom.logic.ActionType;
import com.corrodinggames.rts.gameFramework.Utility;
import java.util.AbstractList;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.a.f */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/a/f.class */
public class AIUnitActionUtils {
    /* JADX INFO: renamed from: a */
    static boolean isIdleOrReclaiming(OrderableUnit orderableUnit) {
        UnitCommand currentWaypoint;
        boolean z = false;
        if (orderableUnit.hasNoCurrentWaypoint()) {
            z = true;
        }
        if (!z && (currentWaypoint = orderableUnit.getCurrentWaypoint()) != null && currentWaypoint.getCommandType() == UnitCommandType.reclaim) {
            z = true;
        }
        return z;
    }

    /* JADX INFO: renamed from: b */
    static boolean isIdle(OrderableUnit orderableUnit) {
        boolean z = false;
        if (orderableUnit.hasNoCurrentWaypoint()) {
            z = true;
        }
        return z;
    }

    /* JADX INFO: renamed from: a */
    public static Object getRandomElement(AbstractList abstractList) {
        int size = abstractList.size();
        if (size == 0) {
            return null;
        }
        return abstractList.get(Utility.getRandomIntInRange(0, size - 1));
    }

    /* JADX INFO: renamed from: a */
    public static boolean hasAiTag(OrderableUnit orderableUnit, AnimationTag animationTag) {
        UnitType unitTypeR = orderableUnit.r();
        if ((unitTypeR instanceof CustomUnitConfig) && AnimationTag.a(animationTag, ((CustomUnitConfig) unitTypeR).aiTags)) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public static AbstractUnitAction findAvailableAction(AIController aIController, OrderableUnit orderableUnit, ActionType actionType) {
        ArrayList<AbstractUnitAction> arrayListN = orderableUnit.getAvailableActions();
        ArrayList reusableList = aIController.getReusableList();
        for (AbstractUnitAction abstractUnitAction : arrayListN) {
            if (abstractUnitAction.getActionTypeForUnit(orderableUnit) == actionType) {
                reusableList.add(abstractUnitAction);
            }
        }
        if (reusableList.size() > 0) {
            return (AbstractUnitAction) getRandomElement(reusableList);
        }
        return null;
    }
}
