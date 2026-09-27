package com.corrodinggames.rts.game.units.custom.logic;

import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.UnitAction;
import com.corrodinggames.rts.game.units.custom.CustomUnitTrigger;
import com.corrodinggames.rts.game.units.custom.LocalizedText;
import com.corrodinggames.rts.game.units.custom.logicBooleans.LogicBoolean;
import com.corrodinggames.rts.game.units.custom.price.UnitPrice;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.ui.LagHidingManager;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.custom.a.c */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/custom/a/c.class */
public class ConfigurableCustomAction extends UnitAction {

    /* JADX INFO: renamed from: b */
    public LogicBoolean enabledCondition;

    /* JADX INFO: renamed from: c */
    public LogicBoolean highlightCondition;

    /* JADX INFO: renamed from: d */
    public LocalizedText highlightText;

    /* JADX INFO: renamed from: e */
    public LogicBoolean highlightColorCondition;

    /* JADX INFO: renamed from: f */
    public LocalizedText highlightColor;

    /* JADX INFO: renamed from: g */
    public LogicBoolean iconCondition;

    /* JADX INFO: renamed from: h */
    public LocalizedText icon;

    /* JADX INFO: renamed from: i */
    public boolean hideInBuildMenu;

    /* JADX INFO: renamed from: j */
    public LogicBoolean guiBlinkingCondition;

    /* JADX INFO: renamed from: k */
    public UnitPrice buildCost;

    /* JADX INFO: renamed from: l */
    public UnitPrice energyCost;

    /* JADX INFO: renamed from: m */
    public UnitPrice resourceCost;

    public static UnitAction a(CustomActionDef customActionDef) {
        boolean z = false;
        if (customActionDef.isLocked != null && customActionDef.isLocked != LogicBoolean.falseBoolean) {
            z = true;
        }
        if (customActionDef.isLockedAlt != null && customActionDef.isLockedAlt != LogicBoolean.falseBoolean) {
            z = true;
        }
        if (customActionDef.isLockedAlt2 != null && customActionDef.isLockedAlt2 != LogicBoolean.falseBoolean) {
            z = true;
        }
        if (customActionDef.isVisible != null && customActionDef.isVisible != LogicBoolean.trueBoolean) {
            z = true;
        }
        if (customActionDef.isGuiBlinking != null && customActionDef.isGuiBlinking != LogicBoolean.falseBoolean) {
            z = true;
        }
        if (customActionDef.addResources != null) {
            z = true;
        }
        if (customActionDef.price != null) {
            z = true;
        }
        if (!z) {
            return UnitAction.a;
        }
        ConfigurableCustomAction configurableCustomAction = new ConfigurableCustomAction();
        configurableCustomAction.highlightCondition = customActionDef.isLocked;
        configurableCustomAction.highlightText = customActionDef.isLockedMessage;
        configurableCustomAction.highlightColorCondition = customActionDef.isLockedAlt;
        configurableCustomAction.highlightColor = customActionDef.isLockedAltMessage;
        configurableCustomAction.iconCondition = customActionDef.isLockedAlt2;
        configurableCustomAction.icon = customActionDef.isLockedAlt2Message;
        configurableCustomAction.enabledCondition = customActionDef.isVisible;
        configurableCustomAction.guiBlinkingCondition = customActionDef.isGuiBlinking;
        configurableCustomAction.energyCost = customActionDef.addResources;
        configurableCustomAction.buildCost = customActionDef.price;
        configurableCustomAction.resourceCost = customActionDef.streamingCost;
        configurableCustomAction.hideInBuildMenu = customActionDef.hideInBuildMenu;
        return configurableCustomAction;
    }

    public static UnitAction a(CustomUnitTrigger customUnitTrigger) {
        boolean z = false;
        if (customUnitTrigger.logicCondition != null && customUnitTrigger.logicCondition != LogicBoolean.falseBoolean) {
            z = true;
        }
        if (!z) {
            return UnitAction.a;
        }
        ConfigurableCustomAction configurableCustomAction = new ConfigurableCustomAction();
        configurableCustomAction.highlightCondition = customUnitTrigger.logicCondition;
        configurableCustomAction.highlightText = LocalizedText.a(customUnitTrigger.action);
        return configurableCustomAction;
    }

    @Override // com.corrodinggames.rts.game.units.UnitAction
    public boolean a(BaseUnit baseUnit) {
        return this.hideInBuildMenu;
    }

    @Override // com.corrodinggames.rts.game.units.UnitAction
    public boolean b(BaseUnit baseUnit) {
        return a(baseUnit, -1);
    }

    public boolean a(BaseUnit baseUnit, int i) {
        if (this.highlightCondition != null && (i == -1 || i == 1)) {
            if (!(baseUnit instanceof OrderableUnit)) {
                GameEngine.reportProblem("CustomActionConfig lockedInGame:" + baseUnit.r().getUnitTypeDescriptionShort() + " is not a OrderableUnit unit");
                return false;
            }
            if (this.highlightCondition.read((OrderableUnit) baseUnit)) {
                return true;
            }
        }
        if (this.highlightColorCondition != null && (i == -1 || i == 2)) {
            if (!(baseUnit instanceof OrderableUnit)) {
                GameEngine.reportProblem("CustomActionConfig lockedInGame:" + baseUnit.r().getUnitTypeDescriptionShort() + " is not a OrderableUnit unit");
                return false;
            }
            if (this.highlightColorCondition.read((OrderableUnit) baseUnit)) {
                return true;
            }
        }
        if (this.iconCondition == null) {
            return false;
        }
        if (i == -1 || i == 3) {
            if (!(baseUnit instanceof OrderableUnit)) {
                GameEngine.reportProblem("CustomActionConfig lockedInGame:" + baseUnit.r().getUnitTypeDescriptionShort() + " is not a OrderableUnit unit");
                return false;
            }
            if (this.iconCondition.read((OrderableUnit) baseUnit)) {
                return true;
            }
            return false;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.UnitAction
    public String c(BaseUnit baseUnit) {
        if (a(baseUnit, 1) && this.highlightText != null) {
            return this.highlightText.b(baseUnit);
        }
        if (a(baseUnit, 2) && this.highlightColor != null) {
            return this.highlightColor.b(baseUnit);
        }
        if (a(baseUnit, 3) && this.icon != null) {
            return this.icon.b(baseUnit);
        }
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.UnitAction
    public boolean a(BaseUnit baseUnit, boolean z) {
        if (this.enabledCondition != null) {
            if (!(baseUnit instanceof OrderableUnit)) {
                GameEngine.reportProblem("CustomActionConfig isAvailable:" + baseUnit.r().getUnitTypeDescriptionShort() + " is not a OrderableUnit unit");
                return true;
            }
            if (z) {
                return LagHidingManager.readLogicBooleanWithSnapshot(this.enabledCondition, (OrderableUnit) baseUnit);
            }
            return this.enabledCondition.read((OrderableUnit) baseUnit);
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.UnitAction
    public boolean d(BaseUnit baseUnit) {
        if (this.guiBlinkingCondition != null) {
            if (!(baseUnit instanceof OrderableUnit)) {
                GameEngine.reportProblem("CustomActionConfig isGuiBlinking:" + baseUnit.r().getUnitTypeDescriptionShort() + " is not a OrderableUnit unit");
                return true;
            }
            return this.guiBlinkingCondition.read((OrderableUnit) baseUnit);
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.UnitAction
    public void a(BaseUnit baseUnit, BaseUnit baseUnit2) {
        if (this.energyCost != null) {
            this.energyCost.h(baseUnit);
        }
    }

    @Override // com.corrodinggames.rts.game.units.UnitAction
    public UnitPrice a() {
        return this.buildCost;
    }

    @Override // com.corrodinggames.rts.game.units.UnitAction
    public UnitPrice b() {
        return this.resourceCost;
    }
}
