package com.corrodinggames.rts.gameFramework.ui;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.aq */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/aq.class */
class StringLogEntry extends WarLogEntry {
    public StringLogEntry(String str) {
        super(-1000.0f, -1000.0f);
        this.text = str;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.WarLogEntry
    /* JADX INFO: renamed from: a */
    public boolean canMergeWith(WarLogEntry warLogEntry) {
        return false;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.WarLogEntry
    /* JADX INFO: renamed from: b */
    public void mergeWith(WarLogEntry warLogEntry) {
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.WarLogEntry
    public String getDisplayText() {
        return this.text;
    }
}
