package com.corrodinggames.rts.game.ai.behaviors;

import android.graphics.PointF;
import com.corrodinggames.rts.game.ai.AIController;
import com.corrodinggames.rts.game.ai.AIUnitActionUtils;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.actions.AbstractUnitAction;
import com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface;
import com.corrodinggames.rts.game.units.custom.AnimationTag;
import com.corrodinggames.rts.game.units.custom.logic.ActionType;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.a.a.d */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/a/a/d.class */
public class NukeBehavior extends UnitAIBehavior {
    /* JADX INFO: renamed from: c */
    static final AnimationTag nukeLauncherTag = AnimationTag.c("nukeLauncher");
    /* JADX INFO: renamed from: b */
    public final boolean isNukeBehavior = true;

    @Override // com.corrodinggames.rts.game.ai.behaviors.AIBehavior
    /* JADX INFO: renamed from: a */
    public AIBehaviorType getBehaviorType() {
        return AIBehaviorType.nuking;
    }

    @Override // com.corrodinggames.rts.game.ai.behaviors.UnitAIBehavior
    /* JADX INFO: renamed from: c */
    public boolean isApplicableToUnit(AIController aIController, OrderableUnit orderableUnit) {
        return isNukeLauncher(orderableUnit);
    }

    /* JADX INFO: renamed from: d */
    public PointF getNukeTargetPosition(AIController aIController, OrderableUnit orderableUnit) {
        return aIController.getRandomEligibleUnitPosition();
    }

    /* JADX INFO: renamed from: e */
    public void tryLaunchNuke(AIController aIController, OrderableUnit orderableUnit) {
        AbstractUnitAction abstractUnitActionA = AIUnitActionUtils.findAvailableAction(aIController, orderableUnit, ActionType.launch);
        if (abstractUnitActionA != null) {
            if (abstractUnitActionA.b(orderableUnit) && abstractUnitActionA.canAfford((BaseUnit) orderableUnit, false)) {
                PointF pointFD = getNukeTargetPosition(aIController, orderableUnit);
                if (pointFD != null) {
                    aIController.c("nuke: launching at:" + pointFD.x + ", " + pointFD.y);
                    aIController.pathCheck(orderableUnit, abstractUnitActionA, pointFD, (BaseUnit) null);
                    return;
                } else {
                    aIController.c("nuke: no target");
                    return;
                }
            }
            aIController.c("nuke: not ready");
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: renamed from: f */
    public void tryLaunchAmmo(AIController aIController, OrderableUnit orderableUnit) {
        AbstractUnitAction abstractUnitActionA;
        if ((orderableUnit instanceof FactoryQueueInterface) && ((FactoryQueueInterface) orderableUnit).dy() && (abstractUnitActionA = AIUnitActionUtils.findAvailableAction(aIController, orderableUnit, ActionType.launchAmmo)) != null && aIController.isPathPossibleBetweenPoints(abstractUnitActionA.getPrice(), orderableUnit)) {
            aIController.c("ai nuke building");
            aIController.issueUnitAction(orderableUnit, abstractUnitActionA);
        }
    }

    /* JADX INFO: renamed from: a */
    public boolean isNukeLauncher(OrderableUnit orderableUnit) {
        if (AIUnitActionUtils.hasAiTag(orderableUnit, nukeLauncherTag)) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.ai.behaviors.AIBehavior
    /* JADX INFO: renamed from: b */
    public void updateCore(float f, AIController aIController) {
        BaseUnit[] baseUnitArrA = this.managedUnits.a();
        int size = this.managedUnits.size();
        for (int i = 0; i < size; i++) {
            OrderableUnit orderableUnit = (OrderableUnit) baseUnitArrA[i];
            tryLaunchAmmo(aIController, orderableUnit);
            tryLaunchNuke(aIController, orderableUnit);
        }
    }
}
