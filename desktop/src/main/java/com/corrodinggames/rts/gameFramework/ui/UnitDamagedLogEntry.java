package com.corrodinggames.rts.gameFramework.ui;

import com.corrodinggames.rts.gameFramework.local.Locale;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.as */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/as.class */
class UnitDamagedLogEntry extends WarLogEntry {

    /* JADX INFO: renamed from: a */
    private boolean isBaseDamaged;

    public UnitDamagedLogEntry(float f, float f2, boolean z) {
        super(f, f2);
        this.isBaseDamaged = z;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.WarLogEntry
    /* JADX INFO: renamed from: a */
    public boolean canMergeWith(WarLogEntry warLogEntry) {
        return super.canMergeWith(warLogEntry) && (warLogEntry instanceof UnitDamagedLogEntry) && ((UnitDamagedLogEntry) warLogEntry).isBaseDamaged == this.isBaseDamaged;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.WarLogEntry
    /* JADX INFO: renamed from: b */
    public void mergeWith(WarLogEntry warLogEntry) {
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.WarLogEntry
    protected long getDisplayDurationMs() {
        return 20000L;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.WarLogEntry
    public String getDisplayText() {
        if (this.text == null) {
            if (this.isBaseDamaged) {
                this.text = Locale.get("gui.log.baseDamaged", new Object[0]);
            } else {
                this.text = Locale.get("gui.log.unitDamaged", new Object[0]);
            }
        }
        return this.text;
    }
}
