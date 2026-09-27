package com.corrodinggames.rts.gameFramework.ui;

import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.gameFramework.local.Locale;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.ar */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/ar.class */
class UnitCreatedLogEntry extends WarLogEntry {

    /* JADX INFO: renamed from: a */
    UnitType unitType;

    /* JADX INFO: renamed from: b */
    int count;

    public UnitCreatedLogEntry(float f, float f2, UnitType unitType) {
        super(f, f2);
        this.unitType = unitType;
        this.count = 1;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.WarLogEntry
    /* JADX INFO: renamed from: a */
    public boolean canMergeWith(WarLogEntry warLogEntry) {
        return super.canMergeWith(warLogEntry) && (warLogEntry instanceof UnitCreatedLogEntry) && ((UnitCreatedLogEntry) warLogEntry).unitType == this.unitType;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.WarLogEntry
    /* JADX INFO: renamed from: b */
    public void mergeWith(WarLogEntry warLogEntry) {
        this.timestamp = warLogEntry.timestamp;
        this.count++;
        this.text = null;
        this.hasBeenShown = false;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.WarLogEntry
    public String getDisplayText() {
        if (this.text == null) {
            String str = "gui.log.unitCreated";
            if (this.unitType.isBuildingUnit()) {
                str = "gui.log.buildingConstructed";
            }
            this.text = String.format(Locale.get(str, new Object[0]), this.unitType.getUnitName(), Integer.valueOf(this.count));
        }
        return this.text;
    }
}
