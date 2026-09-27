package com.corrodinggames.rts.gameFramework.statistics;

import com.corrodinggames.rts.gameFramework.IntLookupTable;
import com.corrodinggames.rts.gameFramework.Point2i;
import com.corrodinggames.rts.gameFramework.StatisticType;
import com.corrodinggames.rts.gameFramework.ui.StatHistory;
import com.corrodinggames.rts.gameFramework.ui.TeamHistoryChart;

import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.ab */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/ab.class */
public class StatHistoryBuilder {
    public StatisticType a;
    public int b;
    public int c;
    public int d;
    public ArrayList e = new ArrayList();

    public StatHistoryBuilder(StatisticType statisticType, ArrayList arrayList) {
        this.a = statisticType;
        ArrayList arrayList2 = new ArrayList();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            IntLookupTable<Point2i> intLookupTableA = ((TeamHistoryChart) it.next()).teamHistory.a(statisticType);
            arrayList2.add(intLookupTableA);
            for (Point2i point2i : intLookupTableA) {
                if (point2i.y > this.b) {
                    this.b = point2i.y;
                }
                if (point2i.y < this.c) {
                    this.c = point2i.y;
                }
                if (point2i.x > this.d) {
                    this.d = point2i.x;
                }
            }
        }
        a(arrayList2);
    }

    private void a(ArrayList arrayList) {
        boolean z;
        int size = arrayList.size();
        StatHistory statHistory = new StatHistory(size);
        int[] iArr = new int[size];
        int i = 0;
        do {
            i++;
            if (i > 1000000) {
                throw new RuntimeException("loopIndex: " + i);
            }
            boolean z2 = true;
            for (int i2 = 0; i2 < size; i2++) {
                IntLookupTable intLookupTable = (IntLookupTable) arrayList.get(i2);
                if (iArr[i2] < intLookupTable.size()) {
                    Point2i point2i = (Point2i) intLookupTable.get(iArr[i2]);
                    if (point2i.x <= statHistory.historySize) {
                        statHistory.setValue(i2, point2i.y);
                        int i3 = i2;
                        iArr[i3] = iArr[i3] + 1;
                        z2 = false;
                    }
                }
            }
            z = z2;
            int i4 = Integer.MAX_VALUE;
            if (z2) {
                this.e.add(statHistory);
                for (int i5 = 0; i5 < size; i5++) {
                    IntLookupTable intLookupTable2 = (IntLookupTable) arrayList.get(i5);
                    if (iArr[i5] < intLookupTable2.size()) {
                        Point2i point2i2 = (Point2i) intLookupTable2.get(iArr[i5]);
                        if (point2i2.x < i4) {
                            i4 = point2i2.x;
                            z = false;
                        }
                    }
                }
                statHistory = new StatHistory(i4, statHistory);
            }
        } while (!z);
    }
}
