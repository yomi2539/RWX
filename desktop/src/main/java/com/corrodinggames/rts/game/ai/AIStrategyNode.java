package com.corrodinggames.rts.game.ai;

import android.graphics.PointF;
import com.corrodinggames.rts.game.units.*;
import com.corrodinggames.rts.game.units.buildings.BaseBuilding;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Serializable;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;

import java.io.IOException;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.a.o */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/a/o.class */
public abstract class AIStrategyNode extends Serializable {

    /* JADX INFO: renamed from: Q */
    public int strategyId;

    /* JADX INFO: renamed from: R */
    protected final AIController aiController;

    /* JADX INFO: renamed from: S */
    public float posX;

    /* JADX INFO: renamed from: T */
    public float posY;

    /* JADX INFO: renamed from: U */
    public float radius;

    /* JADX INFO: renamed from: V */
    public boolean isDestroyed;

    /* JADX INFO: renamed from: W */
    static final ArrayList tempPointList = new ArrayList();

    @Override // com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeFloat(this.posX);
        gameOutputStream.writeFloat(this.posY);
        gameOutputStream.writeFloat(this.radius);
    }

    /* JADX INFO: renamed from: a */
    public void readFromInputStream(GameInputStream gameInputStream) throws IOException {
        this.posX = gameInputStream.readFloat();
        this.posY = gameInputStream.readFloat();
        this.radius = gameInputStream.readFloat();
    }

    public AIStrategyNode(AIController aIController) {
        aIController.nextStrategyId++;
        this.strategyId = aIController.nextStrategyId;
        this.aiController = aIController;
        this.aiController.strategyNodes.add(this);
        this.aiController.activeStrategies.add(this);
    }

    public AIStrategyNode(AIController aIController, float f, float f2) {
        this(aIController);
        this.posX = f;
        this.posY = f2;
    }

    /* JADX INFO: renamed from: p */
    public void destroy() {
        this.aiController.strategyNodes.remove(this);
        this.aiController.activeStrategies.remove(this);
        this.isDestroyed = true;
    }

    /* JADX INFO: renamed from: c */
    public boolean isPointInside(float f, float f2) {
        float fDistanceSq = Utility.distanceSq(this.posX, this.posY, f, f2);
        float f3 = this.radius;
        if (fDistanceSq < f3 * f3) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: b */
    public boolean isUnitInside(BaseUnit baseUnit) {
        float fDistanceSq = Utility.distanceSq(this.posX, this.posY, baseUnit.posX, baseUnit.posY);
        float f = this.radius + baseUnit.radius;
        if (fDistanceSq < f * f) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public boolean isUnitInRange(BaseUnit baseUnit, float f) {
        float fDistanceSq = Utility.distanceSq(this.posX, this.posY, baseUnit.posX, baseUnit.posY);
        float f2 = this.radius + baseUnit.radius + f;
        if (fDistanceSq < f2 * f2) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: c */
    public float getDistanceSqToUnit(BaseUnit baseUnit) {
        return Utility.distanceSq(this.posX, this.posY, baseUnit.posX, baseUnit.posY);
    }

    /* JADX INFO: renamed from: a */
    public float getDistanceSqToZone(BaseZone baseZone) {
        return Utility.distanceSq(this.posX, this.posY, baseZone.posX, baseZone.posY);
    }

    /* JADX INFO: renamed from: d */
    public float getDistanceSqToPoint(float f, float f2) {
        return Utility.distanceSq(this.posX, this.posY, f, f2);
    }

    /* JADX INFO: renamed from: w */
    public PointF getRandomPointInside() {
        PointF pointF = new PointF();
        float fRandom = (float) (Math.random() * 360.0d);
        float fRandom2 = (float) (Math.random() * ((double) this.radius));
        pointF.a(this.posX + (Utility.fastCos(fRandom) * fRandom2), this.posY + (Utility.fastSin(fRandom) * fRandom2));
        return pointF;
    }

    /* JADX INFO: renamed from: e */
    public PointF findBuildLocation(UnitType unitType) {
        int iC;
        GameEngine gameEngine = GameEngine.getInstance();
        PointF pointF = new PointF();
        float f = this.radius;
        UnitMovementType unitMovementType = UnitMovementType.LAND;
        BaseUnit baseUnitCanAttack = null;
        if (unitType == UnitTypeEnum.seaFactory) {
            f = 600.0f;
            unitMovementType = UnitMovementType.WATER;
        }
        for (int i = 0; i < 15; i++) {
            UnitTypeEnum unitTypeEnum = null;
            boolean z = false;
            boolean z2 = false;
            if (this instanceof BaseZone) {
                BaseZone baseZone = (BaseZone) this;
                if (i < 6 && unitType == UnitTypeEnum.fabricator) {
                    unitTypeEnum = UnitTypeEnum.fabricator;
                }
                if (unitTypeEnum != null) {
                    OrderableUnit orderableUnit = null;
                    if (baseUnitCanAttack == null) {
                        baseUnitCanAttack = BaseUnit.getPrototypeForUnitType(unitType);
                    }
                    if (baseUnitCanAttack instanceof OrderableUnit) {
                        orderableUnit = (OrderableUnit) baseUnitCanAttack;
                    }
                    int iCountUnitsOfType = baseZone.countUnitsOfType(unitTypeEnum);
                    if (orderableUnit != null && iCountUnitsOfType > 1) {
                        int i2 = -1;
                        int randomIntInRange = Utility.getRandomIntInRange(0, iCountUnitsOfType - 1);
                        BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
                        int size = BaseUnit.bE.size();
                        for (int i3 = 0; i3 < size; i3++) {
                            BaseUnit baseUnit = baseUnitArrA[i3];
                            if (baseUnit.team == this.aiController && baseZone.countUnitsForStrategy(baseUnit) && baseUnit.isAlive() && this.aiController.isEligibleUnitForRandomSelection(baseUnit) && baseUnit.r() == unitTypeEnum) {
                                i2++;
                                if (i2 == randomIntInRange) {
                                    float f2 = baseUnit.posX;
                                    float f3 = baseUnit.posY;
                                    float fRandomFloatInRange = f2;
                                    float fRandomFloatInRange2 = f3;
                                    if (Utility.getRandomIntInRange(0, 1) == 0) {
                                        fRandomFloatInRange2 += Utility.randomFloatInRange(-150.0f, 150.0f);
                                    } else {
                                        fRandomFloatInRange += Utility.randomFloatInRange(-150.0f, 150.0f);
                                    }
                                    tempPointList.clear();
                                    gameEngine.gameUI.drawUnitMovePreview(orderableUnit, f2, f3, fRandomFloatInRange, fRandomFloatInRange2, false, tempPointList, (BaseUnit) null);
                                    if (tempPointList.size() > 0) {
                                        PointF pointF2 = (PointF) tempPointList.get(0);
                                        pointF.a(pointF2.x, pointF2.y);
                                        z = true;
                                    } else {
                                        z2 = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!z2) {
                if (!z) {
                    float fRandom = (float) (Math.random() * 360.0d);
                    float fRandom2 = (float) (Math.random() * ((double) f));
                    pointF.a(this.posX + (Utility.fastCos(fRandom) * fRandom2), this.posY + (Utility.fastSin(fRandom) * fRandom2));
                }
                gameEngine.tileMap.setCursorTileIndexFromWorldPoint(pointF.x, pointF.y);
                int i4 = gameEngine.tileMap.cursorTileX;
                int i5 = gameEngine.tileMap.cursorTileY;
                if (gameEngine.tileMap.isInBounds(i4, i5) && (((iC = GameViewUtils.c(i4, i5, unitMovementType)) > 5 || iC == 0) && BaseBuilding.canBuildingBePlacedAt(unitType, pointF.x, pointF.y, this.aiController))) {
                    return pointF;
                }
                if (unitType == UnitTypeEnum.seaFactory) {
                    f += 100.0f;
                }
            }
        }
        return null;
    }
}
