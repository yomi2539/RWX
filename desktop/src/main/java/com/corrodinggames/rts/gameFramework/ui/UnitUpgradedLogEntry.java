package com.corrodinggames.rts.gameFramework.ui;

import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.gameFramework.local.Locale;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.at */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/at.class */
class UnitUpgradedLogEntry extends UnitCreatedLogEntry {
    public UnitUpgradedLogEntry(float f, float f2, UnitType unitType) {
        super(f, f2, unitType);
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.UnitCreatedLogEntry, com.corrodinggames.rts.gameFramework.ui.WarLogEntry
    /* JADX INFO: renamed from: a */
    public String getDisplayText() {
        if (this.text == null) {
            this.text = String.format(Locale.get("gui.log.upgradeCompleted", new Object[0]), this.unitType.getUnitName(), Integer.valueOf(this.count));
        }
        return this.text;
    }
}
