package com.corrodinggames.rts.game.units.actions;

import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.EditorOrBuilder;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.ui.GameUI;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.a.z */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/a/z.class */
public class SelectUnitTypeAction extends AbstractUnitAction {
    UnitType a;
    ArrayList b;
    int c;
    boolean d;
    OrderableUnit e;
    int f;

    public SelectUnitTypeAction(UnitType unitType) {
        super("s_" + unitType.v());
        this.b = new ArrayList();
        this.c = 0;
        this.e = null;
        this.sortOrder = -9999.0f;
        this.a = unitType;
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
        return this.a;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    public ActionType getActionType() {
        return ActionType.infoOnly;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: f */
    public ActionDisplayType getActionDisplayType() {
        if (GameEngine.isAndroidPlatform() && !GameUI.showModernSidebar) {
            return ActionDisplayType.infoOnlyNoBox;
        }
        return ActionDisplayType.infoOnly;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: g */
    public boolean isHighPriority() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: c */
    public boolean onClicked(BaseUnit baseUnit, boolean z) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (!z) {
            if (gameEngine.gameUI.getSelectedUnitCount() == 1) {
                return false;
            }
            boolean z2 = false;
            for (BaseUnit baseUnit2 : BaseUnit.bE) {
                if (baseUnit2.isSelected && baseUnit2.r() != this.a) {
                    gameEngine.gameUI.deselectUnit(baseUnit2);
                    z2 = true;
                }
            }
            if (!z2) {
                return false;
            }
            return true;
        }
        for (BaseUnit baseUnit3 : BaseUnit.bE) {
            if (baseUnit3.isSelected && baseUnit3.r() == this.a) {
                gameEngine.gameUI.deselectUnit(baseUnit3);
            }
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    public String d() {
        GameEngine.getInstance();
        if (this.e instanceof EditorOrBuilder) {
            return "Editor";
        }
        return VariableScope.nullOrMissingString + this.a.getUnitName() + " x" + this.c;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: b */
    public String getDisplayName() {
        return "UnitInfo";
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: w */
    public String getDisplayTextForUnitWithQueueCount(BaseUnit baseUnit) {
        if (this.e instanceof EditorOrBuilder) {
            return "Editor";
        }
        return this.a.getUnitName();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: h_ */
    public boolean shouldShowDisplayText() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: s */
    public boolean isWaitingForTarget() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: u */
    public boolean isGuiBlinking() {
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
        String str = VariableScope.nullOrMissingString;
        if (this.e instanceof EditorOrBuilder) {
            return VariableScope.nullOrMissingString;
        }
        if (this.d) {
            str = "(Left click to exclusively select / Right click to unselect)\n";
        }
        return str + this.a.f();
    }

    public void K() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.f == gameEngine.gameUI.selectionChangeCounter) {
            return;
        }
        this.f = gameEngine.gameUI.selectionChangeCounter;
        this.c = 0;
        this.d = false;
        this.e = null;
        BaseUnit[] baseUnitArrA = gameEngine.gameUI.selectedUnitsList.a();
        int size = gameEngine.gameUI.selectedUnitsList.size();
        for (int i = 0; i < size; i++) {
            BaseUnit baseUnit = baseUnitArrA[i];
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (orderableUnit.isSelected) {
                    if (orderableUnit.r() == this.a) {
                        this.c++;
                        if (this.e == null) {
                            this.e = orderableUnit;
                        }
                    } else {
                        this.d = true;
                    }
                }
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: m_ */
    public float getSortOrder() {
        return this.sortOrder - this.c;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: G */
    public boolean isBuildOption() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: o_ */
    public boolean isLockedAndDisabled() {
        return true;
    }
}
