package com.corrodinggames.rts.game.units;

import com.corrodinggames.rts.game.units.actions.ActionDisplayType;
import com.corrodinggames.rts.game.units.actions.ActionType;
import com.corrodinggames.rts.game.units.actions.NoneAction;
import com.corrodinggames.rts.gameFramework.ui.GameUI;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.j */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/j.class */
class ModFilterAction extends NoneAction {
    /* JADX INFO: renamed from: a */
    boolean isPrevious;

    /* JADX INFO: renamed from: b */
    boolean isInfoOnly;

    public ModFilterAction(boolean z, boolean z2) {
        super("changeModFilter" + z + "d:" + z2);
        this.isPrevious = z;
        this.isInfoOnly = z2;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    public boolean b(BaseUnit baseUnit) {
        EditorOrBuilder editorOrBuilderL = EditorOrBuilder.L();
        return editorOrBuilderL == null || editorOrBuilderL.G == EditorTab.modded;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: b */
    public String getDisplayName() {
        if (this.isInfoOnly) {
            EditorOrBuilder editorOrBuilderL = EditorOrBuilder.L();
            if (editorOrBuilderL != null) {
                if (editorOrBuilderL.E != null) {
                    return editorOrBuilderL.E.getDisplayTitle();
                }
                return "All mods";
            }
            return "Mod Filter";
        }
        if (this.isPrevious) {
            return "<- Set mod";
        }
        return "Set mod ->";
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    public String d() {
        if (!this.isInfoOnly) {
            if (this.isPrevious) {
                return "<-";
            }
            return "->";
        }
        EditorOrBuilder editorOrBuilderL = EditorOrBuilder.L();
        if (editorOrBuilderL == null) {
            return "NA";
        }
        if (editorOrBuilderL.E == null) {
            return "All mods";
        }
        return editorOrBuilderL.E.getPaddedTitle();
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: a */
    public String getDescription() {
        return "Change filtered mod";
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: l */
    public float getBuildSpeed() {
        if (!GameUI.showModernActionIcons) {
            return 0.8f;
        }
        return 0.5f;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: m */
    public int getKeyBinding() {
        if (this.isInfoOnly) {
            return 2;
        }
        return 4;
    }

    @Override // com.corrodinggames.rts.game.units.actions.NoneAction, com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: f */
    public ActionDisplayType getActionDisplayType() {
        if (this.isInfoOnly) {
            return ActionDisplayType.infoOnly;
        }
        return super.getActionDisplayType();
    }

    @Override // com.corrodinggames.rts.game.units.actions.NoneAction, com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    public ActionType getActionType() {
        if (this.isInfoOnly) {
            return ActionType.infoOnly;
        }
        return super.getActionType();
    }
}
