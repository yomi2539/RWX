package com.corrodinggames.rts.gameFramework.ui;

import com.corrodinggames.rts.gameFramework.Utility;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.au */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/au.class */
abstract class WarLogEntry implements Comparable<WarLogEntry> {

    /* JADX INFO: renamed from: c */
    long timestamp;

    /* JADX INFO: renamed from: d */
    long durationMs = 5000;

    /* JADX INFO: renamed from: e */
    float x;

    /* JADX INFO: renamed from: f */
    float y;

    /* JADX INFO: renamed from: g */
    String text;

    /* JADX INFO: renamed from: h */
    boolean hasBeenShown;

    /* JADX INFO: renamed from: i */
    boolean alwaysShow;

    /* JADX INFO: renamed from: b */
    public abstract void mergeWith(WarLogEntry warLogEntry);

    /* JADX INFO: renamed from: a */
    public abstract String getDisplayText();

    public WarLogEntry(float f, float f2) {
        this.x = f;
        this.y = f2;
    }

    @Override // java.lang.Comparable
    /* JADX INFO: renamed from: c, reason: merged with bridge method [inline-methods] */
    public int compareTo(WarLogEntry warLogEntry) {
        return (int) (warLogEntry.timestamp - this.timestamp);
    }

    public boolean canMergeWith(WarLogEntry warLogEntry) {
        if (this.timestamp + getDisplayDurationMs() < System.currentTimeMillis() || Utility.distanceSq(this.x, this.y, warLogEntry.x, warLogEntry.y) > 90000.0f) {
            return false;
        }
        return true;
    }

    protected long getDisplayDurationMs() {
        return 5000L;
    }
}
