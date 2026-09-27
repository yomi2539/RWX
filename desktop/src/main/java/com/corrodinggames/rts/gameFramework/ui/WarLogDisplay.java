package com.corrodinggames.rts.gameFramework.ui;

import android.graphics.Paint;
import android.graphics.Typeface;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.gameFramework.GameEngine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.ap */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/ap.class */
public class WarLogDisplay {
    /* JADX INFO: renamed from: a */
    private GameEngine gameEngine;
    /* JADX INFO: renamed from: b */
    private Paint paint;
    /* JADX INFO: renamed from: c */
    private ArrayList<WarLogEntry> entries = new ArrayList();

    public WarLogDisplay(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        setupPaint();
    }

    /* JADX INFO: renamed from: a */
    public void setupPaint() {
        this.paint = new Paint();
        this.paint.a(255, 255, 255, 255);
        this.paint.a(true);
        this.paint.c(true);
        this.paint.a(Typeface.a(Typeface.c, 1));
        this.gameEngine.updatePaintTextSize(this.paint, 14.0f);
    }

    public synchronized void clearEntries() {
        this.entries.clear();
    }

    /* JADX INFO: renamed from: a */
    public synchronized void logUnitCreated(BaseUnit baseUnit) {
        UnitCreatedLogEntry unitCreatedLogEntry = new UnitCreatedLogEntry(baseUnit.posX, baseUnit.posY, baseUnit.r());
        unitCreatedLogEntry.timestamp = GameEngine.getCurrentTimeMillis();
        addEntry(unitCreatedLogEntry);
    }

    /* JADX INFO: renamed from: b */
    public synchronized void logUnitUpgraded(BaseUnit baseUnit) {
        UnitUpgradedLogEntry unitUpgradedLogEntry = new UnitUpgradedLogEntry(baseUnit.posX, baseUnit.posY, baseUnit.r());
        unitUpgradedLogEntry.timestamp = GameEngine.getCurrentTimeMillis();
        addEntry(unitUpgradedLogEntry);
    }

    /* JADX INFO: renamed from: c */
    public synchronized void logUnitDamaged(BaseUnit baseUnit) {
        UnitDamagedLogEntry unitDamagedLogEntry = new UnitDamagedLogEntry(baseUnit.posX, baseUnit.posY, baseUnit.bI());
        unitDamagedLogEntry.timestamp = GameEngine.getCurrentTimeMillis();
        addEntry(unitDamagedLogEntry);
    }

    /* JADX INFO: renamed from: a */
    public synchronized void logMessage(String str) {
        StringLogEntry stringLogEntry = new StringLogEntry(str);
        stringLogEntry.timestamp = GameEngine.getCurrentTimeMillis();
        addEntry(stringLogEntry);
    }

    /* JADX INFO: renamed from: a */
    public synchronized void logMessage(String str, int i) {
        StringLogEntry stringLogEntry = new StringLogEntry(str);
        stringLogEntry.timestamp = GameEngine.getCurrentTimeMillis();
        stringLogEntry.durationMs = i;
        stringLogEntry.alwaysShow = true;
        addEntry(stringLogEntry);
    }

    /* JADX INFO: renamed from: a */
    private void addEntry(WarLogEntry warLogEntry) {
        boolean z = false;
        Iterator it = this.entries.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            WarLogEntry warLogEntry2 = (WarLogEntry) it.next();
            if (warLogEntry2.canMergeWith(warLogEntry)) {
                warLogEntry2.mergeWith(warLogEntry);
                z = true;
                break;
            }
        }
        if (z) {
            Collections.sort(this.entries);
        } else {
            this.entries.add(0, warLogEntry);
        }
    }

    /* JADX INFO: renamed from: a */
    public synchronized void drawWarLog(float f) {
        removeExpiredEntries();
        GameEngine gameEngine = GameEngine.getInstance();
        int i = (int) (gameEngine.screenHeight - (130.0f * gameEngine.screenScale));
        int i2 = (int) (20.0f * gameEngine.screenScale);
        for (WarLogEntry warLogEntry : this.entries) {
            String strA = warLogEntry.getDisplayText();
            if (gameEngine.settingsEngine.showWarLogOnScreen || warLogEntry.alwaysShow) {
                if (warLogEntry.timestamp + warLogEntry.durationMs >= System.currentTimeMillis()) {
                    if (warLogEntry.hasBeenShown) {
                        this.paint.a(255, 160, 160, 160);
                    } else {
                        this.paint.a(255, 255, 255, 255);
                    }
                    gameEngine.renderGraphicsEngine.a(strA, 20, i, this.paint);
                    i -= i2;
                } else {
                    return;
                }
            }
        }
    }

    /* JADX INFO: renamed from: c */
    public synchronized void removeExpiredEntries() {
        Iterator it = this.entries.iterator();
        while (it.hasNext()) {
            if (((WarLogEntry) it.next()).timestamp + 20000 < System.currentTimeMillis()) {
                it.remove();
            }
        }
    }

    public synchronized void jumpToNextUnshownEntry() {
        if (this.entries.isEmpty()) {
            return;
        }
        for (WarLogEntry warLogEntry : this.entries) {
            if (!warLogEntry.hasBeenShown) {
                warLogEntry.hasBeenShown = true;
                this.gameEngine.centerViewpoint(warLogEntry.x, warLogEntry.y);
                return;
            }
        }
    }
}
