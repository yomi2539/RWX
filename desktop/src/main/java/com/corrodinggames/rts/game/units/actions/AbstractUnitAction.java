package com.corrodinggames.rts.game.units.actions;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.UnitAction;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.game.units.custom.AnimationSet;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.game.units.custom.price.UnitPrice;
import com.corrodinggames.rts.game.units.g.SpecialActionBlockEffect;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.KeyBinding;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.ui.GameInterfaceRenderer;
import com.corrodinggames.rts.gameFramework.ui.TextRenderQueue;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.a.s */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/a/s.class */
public abstract class AbstractUnitAction implements Comparable<AbstractUnitAction> {

    /* JADX INFO: renamed from: g */
    public float sortOrder = -999.0f;

    /* JADX INFO: renamed from: h */
    public UnitAction unitAction = UnitAction.a;

    /* JADX INFO: renamed from: i */
    public static final ActionId NONE_ACTION_ID = ActionId.NONE;

    /* JADX INFO: renamed from: a */
    private ActionId actionId;

    /* JADX INFO: renamed from: b */
    private UnitPrice cost;

    /* JADX INFO: renamed from: b */
    public abstract String getDisplayName();

    /* JADX INFO: renamed from: a */
    public abstract String getDescription();

    /* JADX INFO: renamed from: c */
    public abstract int getCostAmount();

    /* JADX INFO: renamed from: b */
    public abstract int getActiveCount(BaseUnit baseUnit, boolean z);

    /* JADX INFO: renamed from: i */
    public abstract UnitType getUnitType();

    /* JADX INFO: renamed from: g */
    public abstract boolean isHighPriority();

    public abstract ActionType getActionType();

    /* JADX INFO: renamed from: f */
    public abstract ActionDisplayType getActionDisplayType();

    /* JADX INFO: renamed from: m_ */
    public float getSortOrder() {
        if (this instanceof SetRallyAction) {
            return -100.0f;
        }
        if (this.sortOrder != -999.0f) {
            return this.sortOrder;
        }
        final UnitType unitType = this.getUnitType();
        if (unitType != null && this.isHighPriority()) {
            return (float)unitType.g();
        }
        return 1.0f;
    }

    @Override // java.lang.Comparable
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public int compareTo(AbstractUnitAction abstractUnitAction) {
        if (abstractUnitAction == null) {
            return 0;
        }
        float sortOrder = getSortOrder() - abstractUnitAction.getSortOrder();
        if (sortOrder < 0.0f) {
            return -1;
        }
        return sortOrder > 0.0f ? 1 : 0;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass() || !this.actionId.equals(((AbstractUnitAction) obj).actionId)) {
            return false;
        }
        return true;
    }

    /* JADX INFO: renamed from: b */
    public static final boolean isNullOrNoneActionId(ActionId actionId) {
        if (actionId == null || actionId == NONE_ACTION_ID) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: c */
    public static final boolean isActionIdSpecified(ActionId actionId) {
        return !isNullOrNoneActionId(actionId);
    }

    /* JADX INFO: renamed from: a */
    public static boolean isSameActionInstance(AbstractUnitAction abstractUnitAction, AbstractUnitAction abstractUnitAction2) {
        return abstractUnitAction == abstractUnitAction2;
    }

    /* JADX INFO: renamed from: d */
    public final boolean matchesActionId(ActionId actionId) {
        return this.actionId == actionId;
    }

    public AbstractUnitAction(int i) {
        setActionId(String.valueOf(i));
    }

    public AbstractUnitAction(String str) {
        setActionId(str);
    }

    public AbstractUnitAction(ActionId actionId) {
        setActionId(actionId);
    }

    /* JADX INFO: renamed from: a */
    public final void setActionId(String str) {
        this.actionId = ActionId.intern(str);
    }

    /* JADX INFO: renamed from: e */
    public final void setActionId(ActionId actionId) {
        this.actionId = actionId;
    }

    /* JADX INFO: renamed from: N */
    public final ActionId getActionId() {
        return this.actionId;
    }

    /* JADX INFO: renamed from: z */
    public ActionId getQueueId() {
        return getActionId();
    }

    /* JADX INFO: renamed from: O */
    public final String getActionIdString() {
        if (this.actionId == null) {
            return "<null index>";
        }
        return this.actionId.getId();
    }

    /* JADX INFO: renamed from: P */
    public AnimationSet getAnimationSet() {
        return null;
    }

    /* JADX INFO: renamed from: d */
    public String getDisplayName(BaseUnit baseUnit) {
        return getDisplayName();
    }

    /* JADX INFO: renamed from: e */
    public String getDescriptionForUnit(BaseUnit baseUnit) {
        return getDescription();
    }

    /* JADX INFO: renamed from: B */
    public UnitPrice getPrice() {
        UnitPrice unitPriceA = this.unitAction.a();
        if (unitPriceA != null) {
            return unitPriceA;
        }
        int iIsConfirmed = getCostAmount();
        if (iIsConfirmed == 0) {
            return UnitPrice.a;
        }
        if (this.cost == null || this.cost.a() != iIsConfirmed) {
            this.cost = UnitPrice.a(iIsConfirmed);
        }
        return this.cost;
    }

    /* JADX INFO: renamed from: r_ */
    public UnitPrice getAdditionalCost() {
        if (this.unitAction.b() != null) {
            return this.unitAction.b();
        }
        return null;
    }

    /* JADX INFO: renamed from: n_ */
    public boolean isQueuable() {
        return false;
    }

    /* JADX INFO: renamed from: g */
    public boolean isNotAvailable(BaseUnit baseUnit) {
        return this.unitAction.b(baseUnit);
    }

    /* JADX INFO: renamed from: j */
    public String getIcon(BaseUnit baseUnit) {
        return this.unitAction.c(baseUnit);
    }

    /* JADX INFO: renamed from: a */
    public void onTargetSelected(BaseUnit baseUnit, BaseUnit baseUnit2) {
        this.unitAction.a(baseUnit, baseUnit2);
    }

    /* JADX INFO: renamed from: d */
    public boolean canPlayerCancel(BaseUnit baseUnit, boolean z) {
        return true;
    }

    /* JADX INFO: renamed from: k */
    public boolean isAlwaysSinglePress(BaseUnit baseUnit) {
        return false;
    }

    /* JADX INFO: renamed from: l */
    public boolean shouldHideQueueInterface(BaseUnit baseUnit) {
        return false;
    }

    /* JADX INFO: renamed from: a */
    public boolean canAfford(BaseUnit baseUnit, boolean z) {
        if (isNotAvailable(baseUnit) || SpecialActionBlockEffect.a(baseUnit, getActionId()) > 0) {
            return false;
        }
        if (z) {
            return getPrice().c(baseUnit, usesExtraLagHidingInUI());
        }
        return getPrice().b(baseUnit);
    }

    /* JADX INFO: renamed from: r */
    public boolean isAvailable(BaseUnit baseUnit) {
        return b(baseUnit);
    }

    /* JADX INFO: renamed from: u */
    public boolean isActivated(BaseUnit baseUnit) {
        return this.unitAction.a(baseUnit);
    }

    public boolean b(BaseUnit baseUnit) {
        return this.unitAction.a(baseUnit, false);
    }

    /* JADX INFO: renamed from: a */
    public boolean appendTooltip(BaseUnit baseUnit, PlayerTeam playerTeam) {
        return false;
    }

    /* JADX INFO: renamed from: u */
    public boolean isGuiBlinking() {
        return false;
    }

    /* JADX INFO: renamed from: h */
    public boolean alwaysShowsTooltip() {
        return false;
    }

    /* JADX INFO: renamed from: C */
    public boolean selectsUnitOnClick() {
        return false;
    }

    /* JADX INFO: renamed from: D */
    public boolean shouldShowUnitPreview() {
        return true;
    }

    /* JADX INFO: renamed from: A */
    public boolean usesActionTarget() {
        return false;
    }

    /* JADX INFO: renamed from: y */
    public UnitType getAttachedUnitType() {
        return null;
    }

    /* JADX INFO: renamed from: E */
    public UnitType getAiConsiderSameAsBuildingUnitType() {
        return null;
    }

    /* JADX INFO: renamed from: F */
    public boolean isDisplayOnly() {
        return false;
    }

    /* JADX INFO: renamed from: t */
    public int getQueueSize() {
        return 1;
    }

    /* JADX INFO: renamed from: o */
    public boolean isCancel() {
        return false;
    }

    /* JADX INFO: renamed from: m */
    public boolean isAiDisabled(BaseUnit baseUnit) {
        return false;
    }

    /* JADX INFO: renamed from: n */
    public boolean isAiHighPriority(BaseUnit baseUnit) {
        return false;
    }

    /* JADX INFO: renamed from: v */
    public com.corrodinggames.rts.game.units.custom.logic.ActionType getActionTypeForUnit(BaseUnit baseUnit) {
        return null;
    }

    public String d() {
        String costForUnit = null;
        GameEngine gameEngine = GameEngine.getInstance();
        int i = 0;
        BaseUnit[] baseUnitArrA = gameEngine.gameUI.selectedUnitsList.a();
        int size = gameEngine.gameUI.selectedUnitsList.size();
        for (int i2 = 0; i2 < size; i2++) {
            BaseUnit baseUnit = baseUnitArrA[i2];
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (costForUnit == null) {
                    costForUnit = getDisplayName(orderableUnit);
                }
                int iIsActive = getActiveCount(orderableUnit, true);
                if (iIsActive != -1 && iIsActive != 0) {
                    i += iIsActive;
                }
            }
        }
        if (costForUnit == null) {
            costForUnit = getDisplayName();
        }
        if (i != -1 && i != 0) {
            costForUnit = costForUnit + " (" + i + ")";
        }
        return costForUnit;
    }

    /* JADX INFO: renamed from: h_ */
    public boolean shouldShowDisplayText() {
        return true;
    }

    /* JADX INFO: renamed from: w */
    public String getDisplayTextForUnitWithQueueCount(BaseUnit baseUnit) {
        return getDisplayName(baseUnit);
    }

    /* JADX INFO: renamed from: a */
    public void renderDisplayText(BaseUnit baseUnit, TextRenderQueue textRenderQueue, Paint paint, Paint paint2) {
        String displayTextForUnitWithQueueCount;
        Paint paint3 = textRenderQueue.currentPaint;
        if (paint != null) {
            textRenderQueue.setCurrentPaint(paint);
        }
        if (shouldShowDisplayText() && (displayTextForUnitWithQueueCount = getDisplayTextForUnitWithQueueCount(baseUnit)) != null && !displayTextForUnitWithQueueCount.equals(VariableScope.nullOrMissingString)) {
            textRenderQueue.addText(displayTextForUnitWithQueueCount);
        }
        if (paint != null) {
            textRenderQueue.setCurrentPaint(paint3);
        }
        ActionDisplayType actionDisplayTypeIsAlsoSelected = getActionDisplayType();
        UnitPrice displayText = getPrice();
        if (!displayText.c() && actionDisplayTypeIsAlsoSelected != ActionDisplayType.infoOnlyStockpile) {
            textRenderQueue.addText(" (");
            BaseUnit baseUnit2 = null;
            int iE = 0;
            if (paint2 != null) {
                baseUnit2 = baseUnit;
                iE = paint2.e();
            }
            displayText.a(textRenderQueue, false, true, 5, true, baseUnit2, iE);
            textRenderQueue.addText(")");
        }
        UnitPrice additionalCost = getAdditionalCost();
        if (additionalCost != null && !additionalCost.c() && actionDisplayTypeIsAlsoSelected != ActionDisplayType.infoOnlyStockpile) {
            textRenderQueue.addText(" (");
            additionalCost.a(textRenderQueue, false, true, 5, true, null, 0);
            textRenderQueue.addText(")");
        }
    }

    /* JADX INFO: renamed from: a */
    public void onPurchase(BaseUnit baseUnit, TextRenderQueue textRenderQueue) {
        String strA = GameInterfaceRenderer.isUnitOnScreen(this, false);
        if (strA != null && !VariableScope.nullOrMissingString.equals(strA)) {
            textRenderQueue.addText("\n" + strA.trim());
        }
        String producedUnitType = getDescriptionForUnit(baseUnit);
        if (producedUnitType != null && !VariableScope.nullOrMissingString.equals(producedUnitType)) {
            textRenderQueue.addText("\n" + producedUnitType.trim());
        }
    }

    /* JADX INFO: renamed from: c */
    public boolean onClicked(BaseUnit baseUnit, boolean z) {
        return false;
    }

    /* JADX INFO: renamed from: f */
    public void onConfirmed(BaseUnit baseUnit) {
    }

    /* JADX INFO: renamed from: j */
    public Texture getIconTexture() {
        if (getActionDisplayType() == ActionDisplayType.upgrade) {
            return GameEngine.getInstance().gameUI.iconUpgradeTexture;
        }
        return null;
    }

    /* JADX INFO: renamed from: h */
    public Texture getExtraIconTexture(BaseUnit baseUnit) {
        return null;
    }

    /* JADX INFO: renamed from: J */
    public int getExtraIconColor() {
        return Color.a(100, 255, 255, 255);
    }

    /* JADX INFO: renamed from: v */
    public Rect getIconRect() {
        return null;
    }

    /* JADX INFO: renamed from: i */
    public BaseUnit getUnitShownInUI(BaseUnit baseUnit) {
        return null;
    }

    /* JADX INFO: renamed from: s */
    public boolean shouldShowUnitHealthBar(BaseUnit baseUnit) {
        return true;
    }

    /* JADX INFO: renamed from: t */
    public boolean shouldShowUnitProgressBar(BaseUnit baseUnit) {
        return true;
    }

    /* JADX INFO: renamed from: a */
    public boolean isTargetingGround(BaseUnit baseUnit) {
        return this.unitAction.d(baseUnit);
    }

    /* JADX INFO: renamed from: s */
    public boolean isWaitingForTarget() {
        return false;
    }

    /* JADX INFO: renamed from: o */
    public boolean isAvailableAndVisible(BaseUnit baseUnit) {
        return true;
    }

    /* JADX INFO: renamed from: G */
    public boolean isBuildOption() {
        return false;
    }

    public void c(BaseUnit baseUnit) {
    }

    /* JADX INFO: renamed from: l */
    public float getBuildSpeed() {
        return 1.0f;
    }

    /* JADX INFO: renamed from: m */
    public int getKeyBinding() {
        return -1;
    }

    /* JADX INFO: renamed from: H */
    public boolean isHighPriorityQueue() {
        return false;
    }

    /* JADX INFO: renamed from: I */
    public boolean isOnlyOneUnitAtATime() {
        return false;
    }

    /* JADX INFO: renamed from: x */
    public boolean isRightClickAction() {
        return false;
    }

    /* JADX INFO: renamed from: p */
    public float getProgress(BaseUnit baseUnit) {
        return -1.0f;
    }

    /* JADX INFO: renamed from: q */
    public ArrayList getCandidateActionList(BaseUnit baseUnit) {
        return null;
    }

    /* JADX INFO: renamed from: M */
    public KeyBinding getPrimaryKeyBinding() {
        return null;
    }

    /* JADX INFO: renamed from: o_ */
    public boolean isLockedAndDisabled() {
        return false;
    }

    /* JADX INFO: renamed from: Q */
    public boolean usesExtraLagHidingInUI() {
        return false;
    }

    public void a(OrderableUnit orderableUnit) {
    }

    public boolean a(float f, float f2) {
        return false;
    }

    /* JADX INFO: renamed from: p */
    public boolean isInstant() {
        return false;
    }
}
