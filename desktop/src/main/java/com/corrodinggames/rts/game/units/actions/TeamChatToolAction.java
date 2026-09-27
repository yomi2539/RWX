package com.corrodinggames.rts.game.units.actions;

import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.KeyBinding;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.a.q */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/a/q.class */
public class TeamChatToolAction extends CustomUnitToolAction {
    public TeamChatToolAction() {
        super("c__cut_chat");
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: b */
    public String getDisplayName() {
        return "Team Chat";
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: a */
    public String getDescription() {
        return "Send a team chat message to your allies";
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: c */
    public boolean onClicked(BaseUnit baseUnit, boolean z) {
        GameEngine.getInstance().gameUI.interfaceRenderer.showChatDialog();
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
    /* JADX INFO: renamed from: M */
    public KeyBinding getPrimaryKeyBinding() {
        return GameEngine.getInstance().inputController.u;
    }
}
