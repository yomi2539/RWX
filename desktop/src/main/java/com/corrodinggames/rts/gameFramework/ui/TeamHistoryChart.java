package com.corrodinggames.rts.gameFramework.ui;

import android.graphics.Paint;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.TeamHistory;
import com.corrodinggames.rts.gameFramework.graphics.GamePaint;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.aa */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/aa.class */
public class TeamHistoryChart {

    /* JADX INFO: renamed from: a */
    public TeamHistory teamHistory;

    /* JADX INFO: renamed from: b */
    String teamName;

    /* JADX INFO: renamed from: c */
    int teamColor;

    /* JADX INFO: renamed from: d */
    GamePaint[] linePaints = new GamePaint[11];

    /* JADX INFO: renamed from: e */
    GamePaint[] markerPaints = new GamePaint[11];

    public TeamHistoryChart(TeamHistory teamHistory, String str, int i) {
        this.teamHistory = teamHistory;
        this.teamName = str;
        this.teamColor = i;
        int i2 = 0;
        while (i2 < 11) {
            int i3 = i2 == 10 ? 255 : i2 * 25;
            this.linePaints[i2] = new GamePaint();
            this.linePaints[i2].a(2.0f);
            if (GameEngine.isIOSVersion) {
                this.linePaints[i2].a(3.0f);
            }
            this.linePaints[i2].a(Paint.Cap.ROUND);
            this.linePaints[i2].b(i);
            this.linePaints[i2].c(i3);
            this.markerPaints[i2] = new GamePaint();
            this.markerPaints[i2].b(-13162713);
            this.markerPaints[i2].c(i3);
            this.markerPaints[i2].a(5.0f);
            this.markerPaints[i2].a(Paint.Cap.ROUND);
            i2++;
        }
    }

    /* JADX INFO: renamed from: a */
    public GamePaint getPaintForAlpha(int i, boolean z) {
        int i2 = i / 25;
        if (i2 < 0) {
            i2 = 0;
        }
        if (i2 > 10) {
            i2 = 10;
        }
        if (z) {
            return this.markerPaints[i2];
        }
        return this.linePaints[i2];
    }
}
