package com.corrodinggames.rts.gameFramework.ui;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.gameFramework.*;
import com.corrodinggames.rts.gameFramework.graphics.GamePaint;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.local.Locale;
import com.corrodinggames.rts.gameFramework.statistics.StatHistoryBuilder;
import com.corrodinggames.rts.gameFramework.statistics.ValueDisplayMode;
import com.corrodinggames.rts.gameFramework.stats.TeamStats;

import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.y */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/y.class */
public class StatsHistoryChart {
        /* JADX INFO: renamed from: a */
    TeamHistoryChart currentTeamChart;
    /* JADX INFO: renamed from: c */
    Paint leftTextPaint;
    /* JADX INFO: renamed from: d */
    Paint rightTextPaint;
        /* JADX INFO: renamed from: e */
    private ArrayList<GameStatistic> statistics;
    /* JADX INFO: renamed from: b */
    Rect workRect = new Rect();
        /* JADX INFO: renamed from: l */
    private ArrayList<TeamHistoryChart> displayedTeamCharts;
        /* JADX INFO: renamed from: m */
    private StatHistoryBuilder[] statBuilders;
    /* JADX INFO: renamed from: o */
    private Texture teamTexture;
        /* JADX INFO: renamed from: n */
    private long lastTabSwitchTime;
        /* JADX INFO: renamed from: p */
    private Texture[] tabTextures;
        /* JADX INFO: renamed from: r */
    private Rect tabRect;
    /* JADX INFO: renamed from: q */
    private Rect teamTextureRect;
    /* JADX INFO: renamed from: f */
    private StatsTab currentTab = StatsTab.overallStats;
    /* JADX INFO: renamed from: g */
    private ValueDisplayMode valueDisplayMode = ValueDisplayMode.absolute;
    /* JADX INFO: renamed from: h */
    private ArrayList teamCharts = new ArrayList();
    /* JADX INFO: renamed from: i */
    private StatHistoryBuilder[] perTeamBuilders = new StatHistoryBuilder[StatisticType.values().length];
    /* JADX INFO: renamed from: j */
    private ArrayList combinedTeamCharts = new ArrayList();
    /* JADX INFO: renamed from: k */
    private StatHistoryBuilder[] combinedBuilders = new StatHistoryBuilder[StatisticType.values().length];
    /* JADX INFO: renamed from: s */
    private ArrayList<String> markerLines = new ArrayList();
    /* JADX INFO: renamed from: t */
    private ArrayList markerColors = new ArrayList();
    /* JADX INFO: renamed from: u */
    private int markerTime = -1;
    /* JADX INFO: renamed from: v */
    private int markerX = -1;
    /* JADX INFO: renamed from: w */
    private int markerY = -1;

    /* JADX INFO: renamed from: <init> */
    private StatsHistoryChart(ArrayList arrayList, ArrayList arrayList2) {
        this.statistics = arrayList2;
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            StatisticsData statisticsData = (StatisticsData) it.next();
            PlayerTeam playerTeamK = PlayerTeam.k(statisticsData.teamHistory.b());
            this.teamCharts.add(new TeamHistoryChart(statisticsData.teamHistory, playerTeamK.teamName, playerTeamK.getTeamColorArgb()));
        }
        for (Integer num : PlayerTeam.getTeamColorIds()) {
            ArrayList arrayList3 = new ArrayList();
            Iterator it2 = arrayList.iterator();
            while (it2.hasNext()) {
                StatisticsData statisticsData2 = (StatisticsData) it2.next();
                if (PlayerTeam.k(statisticsData2.teamHistory.b()).teamColorId == num.intValue()) {
                    arrayList3.add(statisticsData2);
                }
            }
            if (!arrayList3.isEmpty()) {
                this.combinedTeamCharts.add(new TeamHistoryChart(new TeamStatistics(arrayList3).teamHistory, "Team " + PlayerTeam.getTeamSlotLabel(num.intValue()), PlayerTeam.i(num.intValue())));
            }
        }
        for (StatisticType statisticType : StatisticType.values()) {
            this.perTeamBuilders[statisticType.ordinal()] = new StatHistoryBuilder(statisticType, this.teamCharts);
            this.combinedBuilders[statisticType.ordinal()] = new StatHistoryBuilder(statisticType, this.combinedTeamCharts);
        }
        this.displayedTeamCharts = this.teamCharts;
        this.statBuilders = this.perTeamBuilders;
        resetView();
    }

    /* JADX INFO: renamed from: a */
    public static StatsHistoryChart create() {
        return new StatsHistoryChart(GameEngine.getInstance().gameStatistics.getActiveTeamStatistics(), GameStatistic.getGameStatistics());
    }

    /* JADX INFO: renamed from: b */
    public void resetView() {
        this.currentTab = StatsTab.overallStats;
        GameEngine gameEngine = GameEngine.getInstance();
        this.leftTextPaint = new Paint();
        this.leftTextPaint.a(true);
        this.leftTextPaint.a(Paint.Align.LEFT);
        this.leftTextPaint.a(255, 0, 255, 0);
        gameEngine.setScaledTextSize(this.leftTextPaint, 16.0f);
        this.rightTextPaint = new Paint();
        this.rightTextPaint.a(true);
        this.rightTextPaint.a(Paint.Align.RIGHT);
        this.rightTextPaint.a(255, 0, 255, 0);
        gameEngine.setScaledTextSize(this.rightTextPaint, 16.0f);
        loadTabTextures();
    }

    /* JADX INFO: renamed from: c */
    private void loadTabTextures() {
        GameEngine gameEngine = GameEngine.getInstance();
        this.tabTextures = new Texture[StatsTab.values().length + 2];
        this.tabTextures[0] = gameEngine.renderGraphicsEngine.a(R.drawable.stats_button_info);
        this.tabTextures[1] = gameEngine.renderGraphicsEngine.a(R.drawable.stats_button_income);
        this.tabTextures[2] = gameEngine.renderGraphicsEngine.a(R.drawable.stats_button_armyvalue);
        this.tabTextures[3] = gameEngine.renderGraphicsEngine.a(R.drawable.stats_button_buildingvalue);
        this.tabTextures[4] = gameEngine.renderGraphicsEngine.a(R.drawable.stats_button_totalvalue);
        this.tabTextures[5] = gameEngine.renderGraphicsEngine.a(R.drawable.stats_toggle_relative);
        this.tabTextures[6] = gameEngine.renderGraphicsEngine.a(R.drawable.stats_toggle_teams);
        this.tabRect = new Rect(0, 0, this.tabTextures[0].m(), this.tabTextures[0].l());
    }

    /* JADX INFO: renamed from: a */
    public void drawChart(Rect rect, Rect rect2, float f, boolean z, boolean z2) {
        GameEngine gameEngine = GameEngine.getInstance();
        GameUI gameUI = gameEngine.gameUI;
        boolean z3 = true;
        if (z2) {
            int length = StatsTab.values().length;
            int screenPixels = gameEngine.toScreenPixels(30);
            int i = screenPixels * 2;
            int screenPixels2 = gameEngine.toScreenPixels(20);
            int i2 = (rect2.d - screenPixels) - screenPixels2;
            int i3 = gameUI.isCompactEndGameUi ? length + 2 : length - 1;
            int i4 = (int) ((gameEngine.currentScreenWidthPixels / 2.0f) - (((i * i3) + (screenPixels2 * (i3 - 1))) / 2));
            Paint paint = new Paint();
            Paint paint2 = new Paint();
            paint2.a(100, 255, 255, 255);
            for (int i5 = 0; i5 < length; i5++) {
                StatsTab statsTab = StatsTab.values()[i5];
                if (gameUI.isCompactEndGameUi || statsTab != StatsTab.overallStats) {
                    if (gameUI.isRectPressed(i4, i2, i, screenPixels, IconGroup.none, false)) {
                        if (this.currentTab != statsTab) {
                            this.currentTab = statsTab;
                            this.lastTabSwitchTime = System.currentTimeMillis();
                            this.markerTime = -1;
                            this.markerX = -1;
                            this.markerY = -1;
                        }
                        if (this.currentTab != StatsTab.overallStats) {
                            gameUI.isCompactEndGameUi = true;
                        }
                    }
                    this.workRect.a(i4, i2, i4 + i, i2 + screenPixels);
                    gameEngine.renderGraphicsEngine.a(gameEngine.gameUI.uiTexture1, this.tabRect, this.workRect, paint);
                    Paint paint3 = paint2;
                    if (!gameUI.isCompactEndGameUi || this.currentTab == statsTab) {
                        paint3 = paint;
                    }
                    gameEngine.renderGraphicsEngine.a(this.tabTextures[i5], this.tabRect, this.workRect, paint3);
                    i4 += screenPixels2 + i;
                }
            }
            int i6 = i4 + screenPixels2;
            if (gameUI.isCompactEndGameUi) {
                boolean z4 = this.valueDisplayMode != ValueDisplayMode.absolute;
                if (gameUI.isRectPressed(i6, i2, i, screenPixels, IconGroup.none, false)) {
                    this.valueDisplayMode = !z4 ? ValueDisplayMode.relative : ValueDisplayMode.absolute;
                    this.lastTabSwitchTime = System.currentTimeMillis();
                }
                this.workRect.a(i6, i2, i6 + i, i2 + screenPixels);
                Paint paint4 = paint;
                if (this.currentTab == StatsTab.overallStats) {
                    paint4 = paint2;
                }
                gameEngine.renderGraphicsEngine.a(gameEngine.gameUI.uiTexture1, this.tabRect, this.workRect, paint4);
                Paint paint5 = paint;
                if (!z4 || this.currentTab == StatsTab.overallStats) {
                    paint5 = paint2;
                }
                gameEngine.renderGraphicsEngine.a(this.tabTextures[5], this.tabRect, this.workRect, paint5);
                int i7 = i6 + screenPixels2 + i;
                boolean z5 = this.displayedTeamCharts == this.combinedTeamCharts;
                if (gameUI.isRectPressed(i7, i2, i, screenPixels, IconGroup.none, false)) {
                    if (!z5) {
                        this.displayedTeamCharts = this.combinedTeamCharts;
                        this.statBuilders = this.combinedBuilders;
                    } else {
                        this.displayedTeamCharts = this.teamCharts;
                        this.statBuilders = this.perTeamBuilders;
                    }
                    this.lastTabSwitchTime = System.currentTimeMillis();
                }
                this.workRect.a(i7, i2, i7 + i, i2 + screenPixels);
                Paint paint6 = paint;
                if (this.currentTab == StatsTab.overallStats) {
                    paint6 = paint2;
                }
                gameEngine.renderGraphicsEngine.a(gameEngine.gameUI.uiTexture1, this.tabRect, this.workRect, paint6);
                Paint paint7 = paint;
                if (!z5 || this.currentTab == StatsTab.overallStats) {
                    paint7 = paint2;
                }
                gameEngine.renderGraphicsEngine.a(this.tabTextures[6], this.tabRect, this.workRect, paint7);
                int i8 = i7 + screenPixels2 + i;
            }
            if (this.currentTab == StatsTab.overallStats) {
                z3 = true;
            } else {
                z3 = false;
                rect.d = i2 - gameEngine.toScreenPixels(10);
                if (z) {
                    drawTeamStatsChart(this.currentTab.getStatType(), this.valueDisplayMode, rect);
                }
            }
        }
        if (z3) {
            drawOverallStats(rect, f);
        }
    }

    /* JADX INFO: renamed from: a */
    private void drawOverallStats(Rect rect, float f) {
        String str;
        GameEngine gameEngine = GameEngine.getInstance();
        float f2 = 1.5f;
        int screenPixels = rect.b + gameEngine.toScreenPixels(25);
        int iD = rect.d();
        this.leftTextPaint.a("123|", 0, "123|".length(), this.workRect);
        float fC = this.workRect.c() + 6;
        for (GameStatistic gameStatistic : this.statistics) {
            if (gameStatistic.revealProgress != 1.0f && f2 > 0.0f) {
                gameStatistic.revealProgress = Utility.distanceSq(gameStatistic.revealProgress, 1.0f, 0.01f * f2 * f);
                f2 -= 1.0f - gameStatistic.revealProgress;
            }
            float fClampTo255 = Utility.clampTo255(gameStatistic.revealProgress, 0.0f, 1.0f);
            if (gameStatistic.stringValue != null) {
                str = gameStatistic.stringValue;
            } else {
                str = VariableScope.nullOrMissingString + ((int) (gameStatistic.numericValue * fClampTo255));
                if (fClampTo255 <= 0.0f) {
                    str = " ";
                }
            }
            String str2 = gameStatistic.name;
            float fClampTo2552 = Utility.clampTo255(gameStatistic.revealProgress * 2.2f, 0.0f, 1.0f);
            int length = 0;
            if (fClampTo2552 > 0.0f) {
                length = (int) (str2.length() * fClampTo2552);
            }
            int iDistance = Utility.distance(length, 0, str2.length());
            String str3 = VariableScope.nullOrMissingString;
            if (iDistance > 0 && iDistance < str2.length() - 1) {
                str3 = "_";
            }
            gameEngine.renderGraphicsEngine.a(str2.substring(0, iDistance) + str3 + Utility.repeat(" ", (str2.length() + str3.length()) - iDistance), iD - (8.0f * this.leftTextPaint.k()), screenPixels, this.leftTextPaint);
            gameEngine.renderGraphicsEngine.a(str, iD + (8.0f * this.leftTextPaint.k()), screenPixels, this.rightTextPaint);
            screenPixels = (int) (screenPixels + fC);
        }
    }

    /* JADX INFO: renamed from: a */
    private void drawTeamStatsChart(StatisticType statisticType, ValueDisplayMode valueDisplayMode, Rect rect) {
        drawTeamStatsChart(GameEngine.getInstance().renderGraphicsEngine, statisticType, valueDisplayMode, rect);
    }


    /* JADX INFO: renamed from: a */
    private void drawTeamStatsChart(GraphicsEngine y, StatisticType bj, ValueDisplayMode z, Rect rect) {
        GameEngine var5 = GameEngine.getInstance();
        GameUI var6 = var5.gameUI;
        StatHistoryBuilder var7 = this.statBuilders[bj.ordinal()];
        float var8 = (float)(System.currentTimeMillis() - this.lastTabSwitchTime) / 250.0F;
        Paint var9 = new Paint();
        var9.a(255, 0, 255, 0);
        var9.a(true);
        var9.c(true);
        var9.a(Typeface.a(Typeface.c, 0));
        var5.setScaledTextSize(var9, 14.0F);
        Paint var10 = new Paint(var9);
        var10.a(Paint.Align.CENTER);
        var5.setScaledTextSize(var10, 14.0F);
        Paint var11 = new Paint();
        var11.a(2.0F);
        if (GameEngine.isIOSVersion) {
            var11.a(3.0F);
        }

        var11.a(Paint.Cap.ROUND);
        Rect var12 = new Rect();
        Paint var14 = var6.buildingPreviewInvalidPaint;
        String var15 = Locale.get("gui.leaderboard.type." + bj.name());
        var14.a(var15, 0, var15.length(), this.workRect);
        y.a(var15, (float)rect.d(), (float)(rect.b + this.workRect.c()), var14);
        var12.b = rect.b + this.workRect.c() + 3;
        var12.d = rect.d - this.workRect.c() - 3;
        int var37 = Math.max(1, var7.b - var7.c);
        float var38 = (float)var12.c() / var37;
        String var16 = Utility.formatDuration(0L);
        int var13 = y.b(var16, var10);
        y.a(var16, (float)(rect.a + var13 / 2), (float)rect.d, var10);
        var12.a = rect.a + var13 / 2;
        String var17 = "123|";
        var9.a(var17, 0, var17.length(), this.workRect);
        int var18 = this.workRect.c();
        if (z == ValueDisplayMode.absolute) {
            String var19 = TeamStats.formatValue(var7.a.a(), var7.b);
            String var20 = TeamStats.formatValue(var7.a.a(), var7.c);
            var13 = Math.max(y.b(var19, var9), y.b(var20, var9));
            var12.c = rect.c - var13 - 2;
            int var21 = var18 / 2;
            y.b(var12, var6.minimapBorderPaint);
            var11.b(-13619152);

            for (int var22 = 0; var22 <= 4; var22++) {
                float var23 = var7.c + (float) var37 * var22 / 4.0F;
                float var24 = var12.d - (var23 - var7.c) * var38;
                String var25 = TeamStats.formatValue(var7.a.a(), (int)var23);
                y.a(var25, (float)(var12.c + 2), var24 + var21, var9);
                if (var22 > 0 && var22 < 4) {
                    y.a(var12.a, var24, var12.c, var24, var11);
                }
            }
        } else {
            var12.c = rect.c - var5.toScreenPixels(10);
        }

        String var39 = Utility.formatDuration((long)(var7.d / 1000));
        var13 = y.b(var39, var10);
        y.a(var39, (float)var12.c, (float)rect.d, var10);
        float var40 = (float)var12.b() / var7.d;
        if (z == ValueDisplayMode.absolute) {
            label170:
            for (int var41 = 0; var41 <= 2; var41++) {
                Iterator var44 = this.displayedTeamCharts.iterator();

                while (true) {
                    boolean var26;
                    TeamHistoryChart var47;
                    IntLookupTable var50;
                    short var54;
                    while (true) {
                        if (!var44.hasNext()) {
                            continue label170;
                        }

                        var47 = (TeamHistoryChart)var44.next();
                        var50 = var47.teamHistory.a(bj);
                        var26 = var41 == 0;
                        if (!var26) {
                            var54 = 220;
                            if (this.currentTeamChart != null) {
                                if (var47 == this.currentTeamChart) {
                                    var54 = 255;
                                } else {
                                    var54 = 50;
                                }
                            }
                            break;
                        }

                        if (var47.teamColor == -16777216) {
                            var54 = 255;
                            if (this.currentTeamChart != null) {
                                if (var47 == this.currentTeamChart) {
                                    var54 = 255;
                                } else {
                                    var54 = 50;
                                }
                            }
                            break;
                        }
                    }

                    if (var41 == 2 ? var47 == this.currentTeamChart : var41 != 1 || var47 != this.currentTeamChart) {
                        Point2i var27 = (Point2i)var50.get(0);
                        float var28 = var12.a;
                        float var29 = var12.d - var38 * (var27.y - var7.c);

                        for (int var30 = 1; var30 < var50.size(); var30++) {
                            var27 = (Point2i)var50.get(var30);
                            float var31 = var12.a + var40 * var27.x;
                            float var32 = var12.d - var38 * (var27.y - var7.c);
                            int var33 = (int)(var54 * Math.min(1.0F, Math.max(0.0F, var8 - (float)var27.x / var7.d)));
                            GamePaint var34 = var47.getPaintForAlpha(var33, var26);
                            y.a(var28, var29, var31, var29, var34);
                            y.a(var31, var29, var31, var32, var34);
                            var28 = var31;
                            var29 = var32;
                        }
                    }
                }
            }
        } else {
            ArrayList var42 = var7.e;
            StatHistory var45 = (StatHistory)var42.get(0);

            for (int var48 = 1; var48 < var42.size(); var48++) {
                StatHistory var51 = (StatHistory)var42.get(var48);
                float var55 = var12.a + var40 * var45.historySize;
                float var60 = var12.a + var40 * var51.historySize;
                float var66 = var12.d;

                for (int var70 = 0; var70 < this.displayedTeamCharts.size(); var70++) {
                    float var73 = var45.getRatio(var70);
                    float var76 = var66 - var12.c() * var73;
                    if (var73 > 0.0F) {
                        TeamHistoryChart var79 = this.displayedTeamCharts.get(var70);
                        float var82 = Math.min(1.0F, Math.max(0.0F, var8 - (float) var45.historySize / var7.d));
                        GamePaint var83 = var79.getPaintForAlpha((int)(var82 * 255.0F), false);
                        this.workRect.a((int)var55, (int)(var76 + 0.5F), (int)var60, (int)(var66 + 0.5F));
                        if (this.teamTexture != null) {
                            y.a(this.teamTexture, this.teamTextureRect, this.workRect, var83);
                        } else {
                            y.b(this.workRect, var83);
                        }
                    }

                    var66 = var76;
                }

                var45 = var51;
            }
        }

        if (var12.b((int)var6.selectionBoxStartX, (int)var6.selectionBoxStartY)) {
            var6.forceModifiersInsideRect(var12.a, var12.b, var12.b(), var12.c());
            var11.b(-1);
            y.a(var6.selectionBoxStartX, var12.b, var6.selectionBoxStartX, var12.d, var11);
            int var43 = (int)var6.selectionBoxStartX;
            int var46 = (int)var6.selectionBoxStartY;
            int var49 = (int)((var6.selectionBoxStartX - var12.a) / var40);
            if (this.markerX != var43 || this.markerY != var46) {
                this.markerX = var43;
                this.markerY = var46;
                this.markerTime = var49;
                this.markerLines.clear();
                this.markerColors.clear();
                this.markerLines.add(Utility.formatDuration((long)(this.markerTime / 1000)));
                this.markerColors.add(-1);
                TeamHistoryChart var52 = null;
                if (z == ValueDisplayMode.absolute) {
                    float var56 = 30.0F;

                    for (TeamHistoryChart var67 : this.displayedTeamCharts) {
                        TeamHistory var71 = var67.teamHistory;
                        int var74 = var71.a(bj, this.markerTime);
                        float var77 = var12.d - var38 * (var74 - var7.c);
                        float var80 = Utility.abs(var77 - var6.selectionBoxStartY);
                        if (var80 < var56) {
                            var56 = var80;
                            var52 = var67;
                        }
                    }
                }

                this.currentTeamChart = var52;

                for (TeamHistoryChart var62 : this.displayedTeamCharts) {
                    TeamHistory var68 = var62.teamHistory;
                    int var72 = var68.a(bj, this.markerTime);
                    String var75 = TeamStats.formatValue(var7.a.a(), var72) + " " + var62.teamName;
                    this.markerLines.add(var75);
                    int var78 = var62.teamColor;
                    if (this.currentTeamChart != null && this.currentTeamChart != var62) {
                        byte var81 = 60;
                        var78 = Color.a(var81, Color.b(var78), Color.c(var78), Color.d(var78));
                    }

                    this.markerColors.add(var78);
                }
            }

            this.workRect.a = var12.a + var5.toScreenPixels(5);
            this.workRect.b = var12.b + var5.toScreenPixels(5);
            this.workRect.d = this.workRect.b + var5.toScreenPixels(5) + var18 * this.markerLines.size();
            String var53 = "";

            for (String var63 : this.markerLines) {
                if (var53.length() < var63.length()) {
                    var53 = var63;
                }
            }

            int var59 = y.b(var53, var9);
            this.workRect.c = this.workRect.a + var5.toScreenPixels(10) + var59;
            y.b(this.workRect, var6.minimapPaint);
            int var64 = this.workRect.b + var18 + 3;

            for (int var69 = 0; var69 < this.markerLines.size(); var69++) {
                var9.b((Integer)this.markerColors.get(var69));
                y.a((String)this.markerLines.get(var69), (float)(this.workRect.a + 3), (float)var64, var9);
                var64 += var18;
            }
        } else {
            this.currentTeamChart = null;
        }
    }}
