package com.corrodinggames.rts.game.units.custom.logic;

import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.game.units.actions.ActionDisplayType;
import com.corrodinggames.rts.game.units.actions.PopupQueueAction;
import com.corrodinggames.rts.game.units.custom.AnimationSet;
import com.corrodinggames.rts.game.units.custom.CustomUnit;
import com.corrodinggames.rts.game.units.custom.UnitTypeReference;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.game.units.custom.price.UnitPrice;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.ui.GameInterfaceRenderer;
import com.corrodinggames.rts.gameFramework.ui.LagHidingManager;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.custom.a.g */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/custom/a/g.class */
public class CustomAction extends PopupQueueAction {
    public CustomActionDef actionDef;
    public UnitTypeReference unitTypeReference;
    public ActionType actionTypeForUnit;

    public CustomAction(CustomActionDef customActionDef, UnitTypeReference unitTypeReference) {
        super((String) null);
        this.actionTypeForUnit = ActionType.disabled;
        String str = VariableScope.nullOrMissingString;
        setActionId(customActionDef.name != null ? customActionDef.name : (customActionDef.stringId != null ? str + customActionDef.stringId : str) + "_" + customActionDef.id);
        this.actionDef = customActionDef;
        this.unitTypeReference = unitTypeReference;
        if (customActionDef.guiBuildUnit != null) {
            this.unitTypeReference = customActionDef.guiBuildUnit;
        }
        this.actionTypeForUnit = customActionDef.aiUse;
        if (this.actionTypeForUnit == ActionType.auto) {
            boolean z = false;
            boolean z2 = false;
            if (customActionDef.fireTurretAtGroundIndex != null && customActionDef.fireTurretAtGroundOffset == null) {
                z2 = true;
            }
            if (customActionDef.price.d()) {
                z = true;
                this.actionTypeForUnit = ActionType.upgrade;
            }
            if (z && !z2) {
                this.actionTypeForUnit = ActionType.upgrade;
            } else {
                this.actionTypeForUnit = ActionType.movementChange;
            }
            if (customActionDef.aiConsiderSameAsBuilding != null) {
                this.actionTypeForUnit = ActionType.sameAsBuilding;
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: P */
    public AnimationSet getAnimationSet() {
        return this.actionDef.tags;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: F */
    public boolean isDisplayOnly() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: d */
    public boolean canPlayerCancel(BaseUnit baseUnit, boolean z) {
        return this.actionDef.canPlayerCancel;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: k */
    public boolean isAlwaysSinglePress(BaseUnit baseUnit) {
        return this.actionDef.alwaysSinglePress;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: l */
    public boolean shouldHideQueueInterface(BaseUnit baseUnit) {
        return this.actionDef.hideQueueInterface;
    }

    @Override // com.corrodinggames.rts.game.units.actions.PopupQueueAction, com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: u */
    public boolean isGuiBlinking() {
        return super.isGuiBlinking();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: a */
    public boolean canAfford(BaseUnit baseUnit, boolean z) {
        CustomUnit customUnit = (CustomUnit) baseUnit;
        if (!this.actionDef.allowMultipleInQueue && customUnit.a(getActionId(), z) > 0) {
            return false;
        }
        if (this.actionDef.isActive != null) {
            if (z && usesExtraLagHidingInUI()) {
                if (!LagHidingManager.readLogicBooleanWithSnapshot(this.actionDef.isActive, customUnit)) {
                    return false;
                }
            } else if (!this.actionDef.isActive.read(customUnit)) {
                return false;
            }
        }
        return super.canAfford(baseUnit, z);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: g */
    public boolean isNotAvailable(BaseUnit baseUnit) {
        if (a(baseUnit, -1)) {
            return true;
        }
        return super.isNotAvailable(baseUnit);
    }

    public boolean a(BaseUnit baseUnit, int i) {
        if (this.actionDef.isLocked != null && (i == -1 || i == 1)) {
            if (!(baseUnit instanceof CustomUnit)) {
                GameEngine.reportProblem("CustomActionConfig lockedInGame:" + baseUnit.r().getUnitTypeDescriptionShort() + " is not a custom unit");
                return false;
            }
            if (this.actionDef.isLocked.read((CustomUnit) baseUnit)) {
                return true;
            }
        }
        if (this.actionDef.isLockedAlt != null && (i == -1 || i == 2)) {
            if (!(baseUnit instanceof CustomUnit)) {
                GameEngine.reportProblem("CustomActionConfig lockedInGame:" + baseUnit.r().getUnitTypeDescriptionShort() + " is not a custom unit");
                return false;
            }
            if (this.actionDef.isLockedAlt.read((CustomUnit) baseUnit)) {
                return true;
            }
        }
        if (this.actionDef.isLockedAlt2 == null) {
            return false;
        }
        if (i == -1 || i == 3) {
            if (!(baseUnit instanceof CustomUnit)) {
                GameEngine.reportProblem("CustomActionConfig lockedInGame:" + baseUnit.r().getUnitTypeDescriptionShort() + " is not a custom unit");
                return false;
            }
            if (this.actionDef.isLockedAlt2.read((CustomUnit) baseUnit)) {
                return true;
            }
            return false;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: j */
    public String getIcon(BaseUnit baseUnit) {
        if (a(baseUnit, 1) && this.actionDef.isLockedMessage != null) {
            return this.actionDef.isLockedMessage.b(baseUnit);
        }
        if (a(baseUnit, 2) && this.actionDef.isLockedAltMessage != null) {
            return this.actionDef.isLockedAltMessage.b(baseUnit);
        }
        if (a(baseUnit, 3) && this.actionDef.isLockedAlt2Message != null) {
            return this.actionDef.isLockedAlt2Message.b(baseUnit);
        }
        return super.getIcon(baseUnit);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: r */
    public boolean isAvailable(BaseUnit baseUnit) {
        CustomUnit customUnit = (CustomUnit) baseUnit;
        if (this.actionDef.isVisible != null) {
            if (usesExtraLagHidingInUI()) {
                return LagHidingManager.readLogicBooleanWithSnapshot(this.actionDef.isVisible, customUnit);
            }
            return this.actionDef.isVisible.read(customUnit);
        }
        return super.b(baseUnit);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    public boolean b(BaseUnit baseUnit) {
        CustomUnit customUnit = (CustomUnit) baseUnit;
        if (this.actionDef.isVisible != null) {
            return this.actionDef.isVisible.read(customUnit);
        }
        return super.b(baseUnit);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: a */
    public boolean appendTooltip(BaseUnit baseUnit, PlayerTeam playerTeam) {
        if (!this.actionDef.isAlsoViewableByAllies && !this.actionDef.isAlsoViewableByEnemies) {
            return false;
        }
        if (baseUnit.team.d(playerTeam)) {
            return this.actionDef.isAlsoViewableByAllies;
        }
        return this.actionDef.isAlsoViewableByEnemies;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: r_ */
    public UnitPrice getAdditionalCost() {
        UnitPrice unitPriceB = this.unitAction.b();
        if (unitPriceB != null) {
            return unitPriceB;
        }
        return this.actionDef.streamingCost;
    }

    @Override // com.corrodinggames.rts.game.units.actions.PopupQueueAction, com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: b */
    public int getActiveCount(BaseUnit baseUnit, boolean z) {
        if (this.actionDef.displayRemainingStockpile) {
            return this.actionDef.price.a(baseUnit, true);
        }
        return super.getActiveCount(baseUnit, z);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    public String d() {
        return super.d();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: b */
    public String getDisplayName() {
        String strB = null;
        if (this.actionDef.text != null) {
            strB = this.actionDef.text.b();
        }
        return strB;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: d */
    public String getDisplayName(BaseUnit baseUnit) {
        UnitType typeOrNull;
        String strB = null;
        if (this.actionDef.text != null) {
            strB = this.actionDef.text.b(baseUnit);
        }
        if (this.actionDef.textAddUnitName != null && (typeOrNull = this.actionDef.textAddUnitName.getTypeOrNull(baseUnit)) != null) {
            if (strB == null) {
                strB = VariableScope.nullOrMissingString;
            } else if (!strB.equals(VariableScope.nullOrMissingString)) {
                strB = strB + " ";
            }
            strB = strB + typeOrNull.getUnitName();
        }
        if (this.actionDef.textPostFix != null) {
            if (strB == null) {
                strB = VariableScope.nullOrMissingString;
            } else if (!strB.equals(VariableScope.nullOrMissingString)) {
                strB = strB + " ";
            }
            strB = strB + this.actionDef.textPostFix.resolveText();
        }
        return strB;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: a */
    public String getDescription() {
        String strB = null;
        if (this.actionDef.description != null) {
            strB = this.actionDef.description.b();
        }
        return strB;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: e */
    public String getDescriptionForUnit(BaseUnit baseUnit) {
        UnitType typeOrNull;
        String strB = null;
        if (this.actionDef.description != null) {
            strB = this.actionDef.description.b(baseUnit);
        }
        if (this.actionDef.descriptionAddFromUnit != null && (typeOrNull = this.actionDef.descriptionAddFromUnit.getTypeOrNull(baseUnit)) != null) {
            if (strB == null) {
                strB = VariableScope.nullOrMissingString;
            } else if (!strB.equals(VariableScope.nullOrMissingString)) {
                strB = strB + " ";
            }
            strB = strB + typeOrNull.f();
        }
        if (this.actionDef.descriptionAddUnitStats != null) {
            BaseUnit unitReferenceOrNull = this.actionDef.descriptionAddUnitStats.getUnitReferenceOrNull(baseUnit);
            if (unitReferenceOrNull != null) {
                if (strB == null) {
                    strB = VariableScope.nullOrMissingString;
                } else if (!strB.equals(VariableScope.nullOrMissingString)) {
                    strB = strB + "\n\n";
                }
                strB = strB + GameInterfaceRenderer.clearMessageIfSame(unitReferenceOrNull, false, false, false);
            } else {
                BaseUnit unitOrSharedUnit = this.actionDef.descriptionAddUnitStats.getUnitOrSharedUnit(baseUnit);
                if (unitOrSharedUnit != null) {
                    if (strB == null) {
                        strB = VariableScope.nullOrMissingString;
                    } else if (!strB.equals(VariableScope.nullOrMissingString)) {
                        strB = strB + "\n\n";
                    }
                    strB = strB + GameInterfaceRenderer.clearMessageIfSame(unitOrSharedUnit, false, false, true);
                }
            }
        }
        return strB;
    }

    public boolean L() {
        return this.actionDef.whenBuildingCannotMove;
    }

    @Override // com.corrodinggames.rts.game.units.actions.PopupQueueAction
    public float K() {
        if (this.actionDef.buildSpeed >= 1.0f) {
            return 1000.0f;
        }
        return this.actionDef.buildSpeed;
    }

    @Override // com.corrodinggames.rts.game.units.actions.PopupQueueAction, com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    public com.corrodinggames.rts.game.units.actions.ActionType getActionType() {
        return this.actionDef.queueType;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: B */
    public UnitPrice getPrice() {
        UnitPrice unitPriceA = this.unitAction.a();
        if (unitPriceA != null) {
            return unitPriceA;
        }
        return this.actionDef.price;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: c */
    public int getCostAmount() {
        return getPrice().a();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: i */
    public UnitType getUnitType() {
        if (this.unitTypeReference == null) {
            return null;
        }
        return this.unitTypeReference.c();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: y */
    public UnitType getAttachedUnitType() {
        if (this.actionDef.guiBuildUnit != null) {
            return this.actionDef.guiBuildUnit.c();
        }
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: E */
    public UnitType getAiConsiderSameAsBuildingUnitType() {
        if (this.actionDef.aiConsiderSameAsBuilding != null) {
            return this.actionDef.aiConsiderSameAsBuilding.c();
        }
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: A */
    public boolean usesActionTarget() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: g */
    public boolean isHighPriority() {
        if (this.actionDef.guiBuildUnit != null) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: f */
    public ActionDisplayType getActionDisplayType() {
        return this.actionDef.displayType;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: m */
    public boolean isAiDisabled(BaseUnit baseUnit) {
        return this.actionDef.aiDisabledCondition.read((CustomUnit) baseUnit);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: n */
    public boolean isAiHighPriority(BaseUnit baseUnit) {
        if (this.actionDef.aiHighPriorityCondition == null) {
            return false;
        }
        if (!(baseUnit instanceof CustomUnit)) {
            GameEngine.logColored("ai_isHighPriority non customUnit:" + baseUnit.r().getUnitTypeDescriptionShort());
            return false;
        }
        return this.actionDef.aiHighPriorityCondition.read((CustomUnit) baseUnit);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: v */
    public ActionType getActionTypeForUnit(BaseUnit baseUnit) {
        return this.actionTypeForUnit;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: H */
    public boolean isHighPriorityQueue() {
        return this.actionDef.highPriorityQueue;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: I */
    public boolean isOnlyOneUnitAtATime() {
        return this.actionDef.onlyOneUnitAtATime;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: j */
    public Texture getIconTexture() {
        return this.actionDef.iconImage;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: h */
    public Texture getExtraIconTexture(BaseUnit baseUnit) {
        if (this.actionDef.iconExtraIsVisible != null && (baseUnit instanceof CustomUnit) && !LagHidingManager.readLogicBooleanWithSnapshot(this.actionDef.iconExtraIsVisible, (CustomUnit) baseUnit)) {
            return null;
        }
        return this.actionDef.iconExtraImage;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: J */
    public int getExtraIconColor() {
        return this.actionDef.iconExtraColor;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: i */
    public BaseUnit getUnitShownInUI(BaseUnit baseUnit) {
        if (this.actionDef.unitShownInUI != null) {
            return this.actionDef.unitShownInUI.getUnitOrSharedUnit(baseUnit);
        }
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: s */
    public boolean shouldShowUnitHealthBar(BaseUnit baseUnit) {
        return this.actionDef.unitShownInUIWithHpBar;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: t */
    public boolean shouldShowUnitProgressBar(BaseUnit baseUnit) {
        return this.actionDef.unitShownInUIWithProgressBar;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: a */
    public boolean isTargetingGround(BaseUnit baseUnit) {
        if (this.actionDef.isGuiBlinking != null) {
            return LagHidingManager.readLogicBooleanWithSnapshot(this.actionDef.isGuiBlinking, (CustomUnit) baseUnit);
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: Q */
    public boolean usesExtraLagHidingInUI() {
        return this.actionDef.extraLagHidingInUI;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    public void a(OrderableUnit orderableUnit) {
        if (this.actionDef.addResources != null) {
            LagHidingManager.removeResourcesFromSnapshot(orderableUnit, this.actionDef.addResources);
        }
    }
}
