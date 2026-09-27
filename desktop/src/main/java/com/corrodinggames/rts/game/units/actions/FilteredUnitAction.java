package com.corrodinggames.rts.game.units.actions;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.ui.TextRenderQueue;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.a.h */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/a/h.class */
public class FilteredUnitAction extends AbstractUnitAction {
    AbstractUnitAction a;
    ActionFilter b;
    boolean c;
    public int d;
    public boolean e;
    public final int f;

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: m_ */
    public float getSortOrder() {
        return this.a.getSortOrder();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction, java.lang.Comparable
    /* JADX INFO: renamed from: a */
    public int compareTo(AbstractUnitAction abstractUnitAction) {
        return this.a.compareTo(abstractUnitAction);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: b */
    public String getDisplayName() {
        return this.a.getDisplayName();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: d */
    public String getDisplayName(BaseUnit baseUnit) {
        return this.a.getDisplayName(baseUnit);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: a */
    public String getDescription() {
        return this.a.getDescription();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: e */
    public String getDescriptionForUnit(BaseUnit baseUnit) {
        return this.a.getDescriptionForUnit(baseUnit);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: c */
    public int getCostAmount() {
        return 0;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: b */
    public int getActiveCount(BaseUnit baseUnit, boolean z) {
        return this.a.getActiveCount(baseUnit, z);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: n_ */
    public boolean isQueuable() {
        return this.a.isQueuable();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: a */
    public boolean canAfford(BaseUnit baseUnit, boolean z) {
        if (this.c) {
            return this.a.canAfford(baseUnit, z);
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: t */
    public int getQueueSize() {
        return this.a.getQueueSize();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: f */
    public void onConfirmed(BaseUnit baseUnit) {
        this.a.onConfirmed(baseUnit);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    public boolean equals(Object obj) {
        if (obj instanceof FilteredUnitAction) {
            return this.a.equals(((FilteredUnitAction) obj).a);
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: g */
    public boolean isNotAvailable(BaseUnit baseUnit) {
        return this.a.isNotAvailable(baseUnit);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    public boolean b(BaseUnit baseUnit) {
        if (!this.b.isAvailable(this, baseUnit)) {
            return false;
        }
        return this.a.b(baseUnit);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: u */
    public boolean isGuiBlinking() {
        return this.a.isGuiBlinking();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: h */
    public boolean alwaysShowsTooltip() {
        return this.a.alwaysShowsTooltip();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: i */
    public UnitType getUnitType() {
        return this.a.getUnitType();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: g */
    public boolean isHighPriority() {
        return this.a.isHighPriority();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    public ActionType getActionType() {
        return this.a.getActionType();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: f */
    public ActionDisplayType getActionDisplayType() {
        return this.a.getActionDisplayType();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    public String d() {
        return this.a.d();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: h_ */
    public boolean shouldShowDisplayText() {
        return this.a.shouldShowDisplayText();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: a */
    public void renderDisplayText(BaseUnit baseUnit, TextRenderQueue textRenderQueue, Paint paint, Paint paint2) {
        this.a.renderDisplayText(baseUnit, textRenderQueue, paint, paint2);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: a */
    public void onPurchase(BaseUnit baseUnit, TextRenderQueue textRenderQueue) {
        this.a.onPurchase(baseUnit, textRenderQueue);
        UnitType unitType = this.a.getUnitType();
        if (unitType != null && (unitType instanceof CustomUnitConfig)) {
            CustomUnitConfig customUnitConfig = (CustomUnitConfig) unitType;
            if (customUnitConfig.modInfo != null) {
                textRenderQueue.addColoredTextWithHighlightPaint("\n(mod: " + Utility.truncateToLength(customUnitConfig.modInfo.getDisplayTitle(), 30) + ")", this.f, true);
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: j */
    public Texture getIconTexture() {
        return this.a.getIconTexture();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: h */
    public Texture getExtraIconTexture(BaseUnit baseUnit) {
        return this.a.getExtraIconTexture(baseUnit);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: v */
    public Rect getIconRect() {
        return this.a.getIconRect();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: i */
    public BaseUnit getUnitShownInUI(BaseUnit baseUnit) {
        return this.a.getUnitShownInUI(baseUnit);
    }

    public int hashCode() {
        return this.a.hashCode();
    }

    public String toString() {
        return this.a.toString();
    }

    public FilteredUnitAction(AbstractUnitAction abstractUnitAction, ActionFilter actionFilter) {
        this(abstractUnitAction, actionFilter, false);
    }

    public FilteredUnitAction(AbstractUnitAction abstractUnitAction, ActionFilter actionFilter, boolean z) {
        super(abstractUnitAction.getActionId());
        this.b = ActionFilter.emptyActionFilter;
        this.d = 0;
        this.f = Color.a(255, 50, 50, 50);
        this.a = abstractUnitAction;
        this.b = actionFilter;
        setActionId(this.a.getActionId());
        this.sortOrder = this.a.sortOrder;
        this.c = z;
    }

    public AbstractUnitAction q_() {
        return this.a;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: x */
    public boolean isRightClickAction() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: s */
    public boolean isWaitingForTarget() {
        if (!this.b.isAvailable(this, null)) {
            return false;
        }
        if (this.c) {
            return this.a.isWaitingForTarget();
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: y */
    public UnitType getAttachedUnitType() {
        return this.a.getAttachedUnitType();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: c */
    public boolean onClicked(BaseUnit baseUnit, boolean z) {
        return this.a.onClicked(baseUnit, z);
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: a */
    public boolean isTargetingGround(BaseUnit baseUnit) {
        return this.a.isTargetingGround(baseUnit);
    }
}
