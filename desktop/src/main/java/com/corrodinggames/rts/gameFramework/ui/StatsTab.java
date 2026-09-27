package com.corrodinggames.rts.gameFramework.ui;

import com.corrodinggames.rts.gameFramework.StatisticType;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.ac */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/ac.class */
public enum StatsTab {
    overallStats("A", null),
    incomeChart("B", StatisticType.income),
    armyValueChart("C", StatisticType.armyValue),
    buildingValueChart("D", StatisticType.buildingValue),
    totalValueChart("E", StatisticType.totalValue);

    /* JADX INFO: renamed from: f */
    private final String displayName;

    /* JADX INFO: renamed from: g */
    private final StatisticType statType;

    StatsTab(String str, StatisticType statisticType) {
        this.displayName = str;
        this.statType = statisticType;
    }

    /* JADX INFO: renamed from: a */
    public StatisticType getStatType() {
        return this.statType;
    }
}
