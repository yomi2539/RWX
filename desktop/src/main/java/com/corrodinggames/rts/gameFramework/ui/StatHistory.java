package com.corrodinggames.rts.gameFramework.ui;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.ad */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/ad.class */
public class StatHistory {

    /* JADX INFO: renamed from: a */
    public int historySize;

    /* JADX INFO: renamed from: b */
    private int[] values;

    /* JADX INFO: renamed from: c */
    private int totalCache;

    public StatHistory(int i) {
        this.totalCache = -1;
        this.historySize = 0;
        this.values = new int[i];
    }

    public StatHistory(int i, StatHistory statHistory) {
        this.totalCache = -1;
        this.historySize = i;
        this.values = new int[statHistory.values.length];
        for (int i2 = 0; i2 < this.values.length; i2++) {
            this.values[i2] = statHistory.values[i2];
        }
    }

    /* JADX INFO: renamed from: a */
    public void setValue(int i, int i2) {
        this.values[i] = i2;
    }

    public float getRatio(int i) {
        if (this.totalCache < 0) {
            this.totalCache = 0;
            for (int i2 = 0; i2 < this.values.length; i2++) {
                if (this.values[i2] > 0) {
                    this.totalCache += this.values[i2];
                }
            }
        }
        if (this.totalCache == 0 || this.values[i] <= 0) {
            return 0.0f;
        }
        return this.values[i] / (float) this.totalCache;
    }
}
