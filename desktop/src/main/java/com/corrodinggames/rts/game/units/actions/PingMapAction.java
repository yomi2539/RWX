package com.corrodinggames.rts.game.units.actions;

import android.graphics.Rect;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.UnitTypeEnum;
import com.corrodinggames.rts.gameFramework.effects.EffectManager;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.a.j */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/a/j.class */
public class PingMapAction extends AbstractUnitAction {

    /* JADX INFO: renamed from: a */
    public PingType pingType;
    static ArrayList<AbstractUnitAction> b = new ArrayList();
    static Rect c;

    public PingMapAction() {
        this(PingType.normal);
    }

    public PingMapAction(PingType pingType) {
        super("c_6_" + pingType.name());
        this.pingType = pingType;
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
    /* JADX INFO: renamed from: w, reason: merged with bridge method [inline-methods] */
    public UnitTypeEnum getUnitType() {
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    public ActionType getActionType() {
        return ActionType.pingMap;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: f */
    public ActionDisplayType getActionDisplayType() {
        return ActionDisplayType.none;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: g */
    public boolean isHighPriority() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: a */
    public String getDescription() {
        return "Ping Map" + this.pingType.a();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: b */
    public String getDisplayName() {
        return this.pingType.b();
    }

    public String K() {
        return this.pingType.c();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: h_ */
    public boolean shouldShowDisplayText() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: s */
    public boolean isWaitingForTarget() {
        return true;
    }

    static {
        for (PingType pingType : PingType.values()) {
            b.add(new PingMapAction(pingType));
        }
        c = new Rect();
    }

    public static PingMapAction a(ActionId actionId) {
        for (AbstractUnitAction abstractUnitAction : b) {
            if (abstractUnitAction.matchesActionId(actionId)) {
                return (PingMapAction) abstractUnitAction;
            }
        }
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: q */
    public ArrayList getCandidateActionList(BaseUnit baseUnit) {
        return b;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: j */
    public Texture getIconTexture() {
        return EffectManager.effectTemplates[9].texture;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: v */
    public Rect getIconRect() {
        int iOrdinal = 7 + this.pingType.ordinal();
        c.a(29 * iOrdinal, 0, (29 * iOrdinal) + 28, 28);
        return c;
    }
}
