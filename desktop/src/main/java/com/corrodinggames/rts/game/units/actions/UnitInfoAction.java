package com.corrodinggames.rts.game.units.actions;

import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.EditorOrBuilder;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.network.debug.DebugGameOutputStream;
import com.corrodinggames.rts.gameFramework.ui.GameInterfaceRenderer;
import java.io.IOException;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.a.y */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/a/y.class */
public class UnitInfoAction extends AbstractUnitAction {

    /* JADX INFO: renamed from: a */
    public boolean showInfo;

    public UnitInfoAction(boolean z) {
        super("c_5");
        this.sortOrder = -9990.0f;
        this.showInfo = z;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: b */
    public int getActiveCount(BaseUnit baseUnit, boolean z) {
        return -1;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: c */
    public int getCostAmount() {
        return 0;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: i */
    public UnitType getUnitType() {
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    public ActionType getActionType() {
        return ActionType.infoOnly;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: f */
    public ActionDisplayType getActionDisplayType() {
        return ActionDisplayType.infoOnly;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: g */
    public boolean isHighPriority() {
        return false;
    }

    public OrderableUnit K() {
        GameEngine gameEngine = GameEngine.getInstance();
        BaseUnit[] baseUnitArrA = gameEngine.gameUI.selectedUnitsList.a();
        int size = gameEngine.gameUI.selectedUnitsList.size();
        for (int i = 0; i < size; i++) {
            BaseUnit baseUnit = baseUnitArrA[i];
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (orderableUnit.isSelected) {
                    return orderableUnit;
                }
            }
        }
        return null;
    }

    public boolean L() {
        GameEngine gameEngine = GameEngine.getInstance();
        OrderableUnit orderableUnitK = K();
        if (orderableUnitK != null) {
            return (orderableUnitK instanceof EditorOrBuilder) || gameEngine.playerTeam == orderableUnitK.team;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    public String d() {
        String strA = "UnitInfo";
        GameEngine gameEngine = GameEngine.getInstance();
        OrderableUnit orderableUnitK = K();
        if (orderableUnitK != null) {
            if (orderableUnitK instanceof EditorOrBuilder) {
                return "Editor";
            }
            if (!this.showInfo) {
                strA = gameEngine.gameUI.interfaceRenderer.getUnitInfoText((BaseUnit) orderableUnitK, false);
            } else {
                strA = gameEngine.gameUI.interfaceRenderer.getActionTooltipText(orderableUnitK.team);
            }
        }
        return strA;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: h_ */
    public boolean shouldShowDisplayText() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: b */
    public String getDisplayName() {
        return "UnitInfo";
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: d */
    public String getDisplayName(BaseUnit baseUnit) {
        if (this.showInfo) {
            return VariableScope.nullOrMissingString;
        }
        if (baseUnit != null) {
            return baseUnit.r().getUnitName();
        }
        return "UnitInfo";
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: s */
    public boolean isWaitingForTarget() {
        if (!this.showInfo || !L()) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: u */
    public boolean isGuiBlinking() {
        if (this.showInfo) {
            return false;
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: C */
    public boolean selectsUnitOnClick() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: a */
    public String getDescription() {
        OrderableUnit orderableUnitK;
        if (!this.showInfo && (orderableUnitK = K()) != null) {
            String strA = GameInterfaceRenderer.clearMessageIfSame(orderableUnitK, false, true, false);
            if (0 != 0) {
                DebugGameOutputStream debugGameOutputStream = new DebugGameOutputStream();
                try {
                    orderableUnitK.a(debugGameOutputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                strA = strA + "\n" + debugGameOutputStream.a;
            }
            return strA;
        }
        return VariableScope.nullOrMissingString;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: G */
    public boolean isBuildOption() {
        return true;
    }
}
