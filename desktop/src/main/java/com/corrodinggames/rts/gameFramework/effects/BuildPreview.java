package com.corrodinggames.rts.gameFramework.effects;

import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.map.TileMap;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.game.units.UnitTypeEnum;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.graphics.GamePaint;
import com.corrodinggames.rts.gameFramework.utility.TransactionalArrayList;
import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.d.a */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/d/a.class */
public class BuildPreview {

    /* JADX INFO: renamed from: a */
    float timer;

    /* JADX INFO: renamed from: b */
    float lifeTimer;

    /* JADX INFO: renamed from: c */
    public boolean isDead;

    /* JADX INFO: renamed from: d */
    public UnitType unitType;

    /* JADX INFO: renamed from: e */
    public PlayerTeam team;

    /* JADX INFO: renamed from: g */
    public float worldX;

    /* JADX INFO: renamed from: h */
    public float worldY;

    /* JADX INFO: renamed from: i */
    public boolean showFoundation;

    /* JADX INFO: renamed from: j */
    public PlayerTeam placingTeam;

    /* JADX INFO: renamed from: k */
    public boolean hasMinimapPosition;

    /* JADX INFO: renamed from: l */
    public int gridX;

    /* JADX INFO: renamed from: m */
    public int gridY;

    /* JADX INFO: renamed from: n */
    public boolean isBuilding;

    /* JADX INFO: renamed from: o */
    public OrderableUnit builder;

    /* JADX INFO: renamed from: r */
    public int buildQueueId;

    /* JADX INFO: renamed from: s */
    public float fadeInProgress;

    /* JADX INFO: renamed from: u */
    public boolean isVisibleOnMinimap;

    /* JADX INFO: renamed from: v */
    public BaseUnit attachedUnit;

    /* JADX INFO: renamed from: D */
    static Paint foundationPaintInactive;

    /* JADX INFO: renamed from: w */
    public static TransactionalArrayList<BuildPreview> activePreviews = new TransactionalArrayList();
    /* JADX INFO: renamed from: x */
    static Point tempPoint = new Point();

    /* JADX INFO: renamed from: y */
    static RectF tempRectF = new RectF();

    /* JADX INFO: renamed from: z */
    static RectF tempRectF2 = new RectF();

    /* JADX INFO: renamed from: A */
    static RectF tempRectF3 = new RectF();

    /* JADX INFO: renamed from: E */
    static RectF tempRectF4 = new RectF();

    /* JADX INFO: renamed from: C */
    static Paint foundationPaint = new GamePaint();

    /* JADX INFO: renamed from: f */
    public int previewUnitLevel = 1;

    /* JADX INFO: renamed from: p */
    boolean wasEverPlaceable = false;

    /* JADX INFO: renamed from: q */
    public boolean forceDraw = false;

    /* JADX INFO: renamed from: t */
    public float fadeInSpeed = 0.04f;

    /* JADX INFO: renamed from: B */
    Paint paint = new Paint();

    public BuildPreview() {
        activePreviews.add(this);
        activePreviews.a();
    }

    static {
        foundationPaint.a(90, 0, 0, 255);
        foundationPaint.a(Paint.Style.STROKE);
        foundationPaint.a(2.0f);
        foundationPaintInactive = new GamePaint();
        foundationPaintInactive.a(40, 0, 0, 255);
        foundationPaintInactive.a(Paint.Style.STROKE);
        foundationPaintInactive.a(2.0f);
    }

    /* JADX INFO: renamed from: a */
    public static void clearAll() {
        activePreviews.clear();
    }

    /* JADX INFO: renamed from: a */
    public static void updateAll(float f) {
        Iterator it = activePreviews.iterator();
        while (it.hasNext()) {
            ((BuildPreview) it.next()).update(f);
        }
        activePreviews.a();
    }

    /* JADX INFO: renamed from: b */
    public static void drawAll(float f) {
        Object[] objArrB = activePreviews.b();
        int size = activePreviews.size();
        for (int i = 0; i < size; i++) {
            ((BuildPreview) objArrB[i]).draw(f);
        }
    }

    /* JADX INFO: renamed from: a */
    public static boolean isTileOverBlueprint(PlayerTeam playerTeam, int i, int i2, int i3) {
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.tileMap.setCursorTileIndexFromTileIndex(i, i2);
        float f = gameEngine.tileMap.cursorTileX + gameEngine.tileMap.halfTileWorldSizeX;
        float f2 = gameEngine.tileMap.cursorTileY + gameEngine.tileMap.halfTileWorldSizeY;
        tempRectF.a(f, f2, f + 1.0f, f2 + 1.0f);
        return isRectOverBlueprint(playerTeam, tempRectF, i3);
    }

    /* JADX INFO: renamed from: a */
    public static boolean isUnitOverBlueprint(PlayerTeam playerTeam, OrderableUnit orderableUnit, int i) {
        tempRectF = orderableUnit.a(GameEngine.getInstance().tileMap, tempRectF);
        return isRectOverBlueprint(playerTeam, tempRectF, i);
    }

    /* JADX INFO: renamed from: a */
    public static boolean doUnitsOverlap(OrderableUnit orderableUnit, OrderableUnit orderableUnit2) {
        TileMap tileMap = GameEngine.getInstance().tileMap;
        tempRectF = orderableUnit.a(tileMap, tempRectF);
        tempRectF2 = orderableUnit2.a(tileMap, tempRectF2);
        if (Utility.rectanglesOverlap(tempRectF, tempRectF2)) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public static boolean isRectOverBlueprint(PlayerTeam playerTeam, RectF rectF, int i) {
        TileMap tileMap = GameEngine.getInstance().tileMap;
        RectF rectFA = tempRectF3;
        for (BuildPreview buildPreview : activePreviews) {
            if (buildPreview.placingTeam == playerTeam && buildPreview.isBuilding && (i == -1 || i == buildPreview.buildQueueId)) {
                BaseUnit baseUnitFindTurretPosition = BaseUnit.findTurretPosition(buildPreview.unitType);
                if (baseUnitFindTurretPosition == null) {
                    GameEngine.log("isTileRectOverBlueprint: Failed to get shared unit for: " + buildPreview.unitType);
                } else {
                    baseUnitFindTurretPosition.posX = buildPreview.worldX;
                    baseUnitFindTurretPosition.posY = buildPreview.worldY;
                    rectFA = baseUnitFindTurretPosition.a(tileMap, rectFA);
                    if (Utility.rectanglesOverlap(rectFA, rectF)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public static BuildPreview findClosestPreview(PlayerTeam playerTeam, float f, float f2) {
        for (BuildPreview buildPreview : activePreviews) {
            if (buildPreview.placingTeam == playerTeam && buildPreview.isBuilding) {
                float fDistanceSq = Utility.distanceSq(buildPreview.worldX, buildPreview.worldY, f, f2);
                float f3 = BaseUnit.findTurretPosition(buildPreview.unitType).radius + 1.0f;
                if (f3 < 20.0f) {
                    f3 = 20.0f;
                }
                if (fDistanceSq < f3 * f3) {
                    return buildPreview;
                }
            }
        }
        return null;
    }

    /* JADX INFO: renamed from: b */
    public boolean isValid() {
        if (this.isBuilding) {
            if (this.builder == null || this.builder.isDead || !UnitTypeEnum.canPlaceUnit(this.unitType, this.worldX, this.worldY, 0.0f, 0.0f, this.team)) {
                return false;
            }
            return true;
        }
        if (this.attachedUnit == null || this.attachedUnit.isVisibleToLocalPlayer()) {
            return false;
        }
        return true;
    }

    /* JADX INFO: renamed from: c */
    public void update(float f) {
        this.timer += 1.0f;
        this.lifeTimer += f;
        boolean z = false;
        this.fadeInProgress = Utility.moveTowardsZero(this.fadeInProgress, this.fadeInSpeed * f);
        if (this.isBuilding) {
            if (this.timer > 6.0f) {
                this.timer = 0.0f;
                boolean zHasBuildWaypointNear = this.builder.hasBuildWaypointNear(this.unitType, this.worldX, this.worldY);
                if (!this.wasEverPlaceable && zHasBuildWaypointNear) {
                    this.wasEverPlaceable = true;
                }
                if (!zHasBuildWaypointNear && (this.wasEverPlaceable || this.lifeTimer > 180.0f)) {
                    z = true;
                }
                if (!isValid()) {
                    z = true;
                }
            }
        } else if (this.timer > 2.0f && !isValid()) {
            z = true;
        }
        if (z) {
            this.isDead = true;
            activePreviews.b(this);
        }
    }

    /* JADX INFO: renamed from: d */
    public void draw(float f) {
        BaseUnit baseUnitCanAttack;
        Rect rectCd;
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine.playerTeam != this.placingTeam || !gameEngine.bufferedVisibleWorldRectF.b(this.worldX, this.worldY)) {
            return;
        }
        if (this.forceDraw && !this.wasEverPlaceable) {
            return;
        }
        float fFastCos = 0.0f;
        float f2 = this.worldX;
        float f3 = this.worldY;
        boolean z = false;
        boolean z2 = false;
        if (this.isBuilding) {
            z2 = true;
        } else {
            z = true;
        }
        boolean z3 = true;
        if (this.showFoundation) {
            z3 = false;
        }
        if (z2) {
            float f4 = this.fadeInProgress;
            if (f4 <= 0.0f) {
                fFastCos = 0.0f;
            } else if (this.fadeInProgress < 1.0f) {
                fFastCos = 1.0f - Utility.fastCos(f4 * 90.0f);
            } else {
                fFastCos = 1.0f;
            }
        }
        if (z2 && this.fadeInProgress < 1.0f && (baseUnitCanAttack = BaseUnit.getPrototypeForUnitType(this.unitType)) != null && baseUnitCanAttack.bI() && (rectCd = baseUnitCanAttack.cd()) != null) {
            tempRectF4.a(rectCd);
            tempRectF4.b *= gameEngine.tileMap.tileWorldSizeY;
            tempRectF4.d *= gameEngine.tileMap.tileWorldSizeY;
            tempRectF4.a *= gameEngine.tileMap.tileWorldSizeX;
            tempRectF4.c *= gameEngine.tileMap.tileWorldSizeX;
            tempRectF4.a(-(baseUnitCanAttack.getTileOffsetX() - gameEngine.tileMap.halfTileWorldSizeX), -(baseUnitCanAttack.getTileOffsetY() - gameEngine.tileMap.halfTileWorldSizeY));
            Utility.grow(tempRectF4, (gameEngine.tileMap.halfTileWorldSizeX - 3) + (fFastCos * 5.0f));
            tempRectF4.a(this.worldX - gameEngine.viewpointXSnapped, (this.worldY - gameEngine.viewpointYSnapped) - 0.0f);
            float f5 = 3.0f + (fFastCos * 7.0f);
            Paint paint = foundationPaint;
            if (this.fadeInProgress <= 0.0f) {
                paint = foundationPaintInactive;
            }
            gameEngine.renderGraphicsEngine.a(tempRectF4.a - f5, tempRectF4.b, tempRectF4.c + f5, tempRectF4.b, paint);
            gameEngine.renderGraphicsEngine.a(tempRectF4.a - f5, tempRectF4.d, tempRectF4.c + f5, tempRectF4.d, paint);
            gameEngine.renderGraphicsEngine.a(tempRectF4.a, tempRectF4.b - f5, tempRectF4.a, tempRectF4.d + f5, paint);
            gameEngine.renderGraphicsEngine.a(tempRectF4.c, tempRectF4.b - f5, tempRectF4.c, tempRectF4.d + f5, paint);
        }
        float f6 = 0.0f;
        if (z2) {
            f6 = 0.0f - (10.0f * fFastCos);
        }
        UnitTypeEnum.drawUnitWithBoolean(this.unitType, f2, f3 + f6, 0.0f, 0.0f, this.team, 1.0f, 500.0f, z, z2, this.previewUnitLevel, z3, null);
    }
}
