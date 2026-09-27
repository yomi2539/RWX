package com.corrodinggames.rts.game.units;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.effects.EffectQuality;
import com.corrodinggames.rts.gameFramework.effects.EffectType;
import com.corrodinggames.rts.gameFramework.graphics.opengl.GraphicsUtils;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;
import java.io.IOException;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.f */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/f.class */
public class DamageZone extends DummyUnit {
        /* JADX INFO: renamed from: a */
    public float currentRadius;
        /* JADX INFO: renamed from: b */
    public float contractionSpeed;
    public float c;
        /* JADX INFO: renamed from: d */
    public float targetRadius;
        /* JADX INFO: renamed from: e */
    public float targetX;
        /* JADX INFO: renamed from: f */
    public float targetY;
        /* JADX INFO: renamed from: g */
    public boolean isFirstUpdate;
        /* JADX INFO: renamed from: h */
    public float damageMultiplier;
        /* JADX INFO: renamed from: i */
    public boolean isContracting;
    public float j;
    static Paint k = new Paint();
    static Paint l;
    static Paint m;
    static Paint n;
    static Paint o;
    static Paint p;
        /* JADX INFO: renamed from: q */
    boolean isZoneMarker;
    static final PointF r;

    static {
        k.a(10.0f);
        k.b(Color.a(100, 160, 0, 0));
        k.a(Paint.Style.STROKE);
        m = new Paint();
        m.a(k);
        m.b(Color.a(SlickToAndroidKeycodes.AndroidCodes.KEYCODE_STB_INPUT, 160, 0, 0));
        l = new Paint();
        l.a(2.0f);
        l.b(Color.a(100, 160, 0, 0));
        l.a(Paint.Style.STROKE);
        n = new Paint();
        n.a(l);
        n.b(Color.a(SlickToAndroidKeycodes.AndroidCodes.KEYCODE_STB_INPUT, 160, 0, 0));
        o = new Paint();
        o.a(2.0f);
        o.b(Color.a(50, 255, 255, 255));
        o.a(Paint.Style.STROKE);
        p = new Paint(o);
        r = new PointF();
    }

    public DamageZone(boolean z) {
        super(z);
        this.currentRadius = 2000.0f;
        this.contractionSpeed = 0.0f;
        this.c = 0.0f;
        this.targetRadius = 2000.0f;
        this.isFirstUpdate = true;
        this.damageMultiplier = 1.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeByte(0);
        gameOutputStream.writeFloat(this.currentRadius);
        gameOutputStream.writeFloat(this.contractionSpeed);
        gameOutputStream.writeFloat(this.c);
        gameOutputStream.writeFloat(this.targetRadius);
        gameOutputStream.writeFloat(this.targetX);
        gameOutputStream.writeFloat(this.targetY);
        gameOutputStream.writeBoolean(this.isFirstUpdate);
        gameOutputStream.writeFloat(this.damageMultiplier);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject
    public void a(GameInputStream gameInputStream) throws IOException {
        gameInputStream.readByte();
        this.currentRadius = gameInputStream.readFloat();
        this.contractionSpeed = gameInputStream.readFloat();
        this.c = gameInputStream.readFloat();
        this.targetRadius = gameInputStream.readFloat();
        this.targetX = gameInputStream.readFloat();
        this.targetY = gameInputStream.readFloat();
        this.isFirstUpdate = gameInputStream.readBoolean();
        this.damageMultiplier = gameInputStream.readFloat();
        super.a(gameInputStream);
        if (!this.isDead) {
            GameEngine.getInstance().minimap.addGraphicsOperation(this);
        }
    }

    public static void d_() {
        GameEngine.getInstance();
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: b, reason: merged with bridge method [inline-methods] */
    public UnitTypeEnum r() {
        if (this.isZoneMarker) {
            return UnitTypeEnum.zoneMarker;
        }
        return UnitTypeEnum.damagingBorder;
    }

    public DamageZone f() {
        for (BaseUnit baseUnit : BaseUnit.getGlobalUnitList()) {
            if ((baseUnit instanceof DamageZone) && !baseUnit.isDead && baseUnit != this) {
                DamageZone damageZone = (DamageZone) baseUnit;
                if (damageZone.isZoneMarker == this.isZoneMarker) {
                    return damageZone;
                }
            }
        }
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.DummyUnit, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f) {
        super.update(f);
        if (this.isDead) {
            return;
        }
        if (this.isFirstUpdate) {
            this.isFirstUpdate = false;
            DamageZone damageZoneF = f();
            if (damageZoneF != null) {
                damageZoneF.targetX = this.posX;
                damageZoneF.targetY = this.posY;
                damageZoneF.targetRadius = this.targetRadius;
                removeFromGame();
            } else {
                this.targetX = this.posX;
                this.targetY = this.posY;
                if (!this.isZoneMarker) {
                    GameEngine.log("DamagingBorder created " + this.targetX + "," + this.targetY + " size:" + this.targetRadius);
                }
                GameEngine.getInstance().minimap.addGraphicsOperation(this);
            }
        }
        if (this.isZoneMarker) {
            this.currentRadius = this.targetRadius;
            this.posX = this.targetX;
            this.posY = this.targetY;
        } else if (this.currentRadius > this.targetRadius) {
            this.contractionSpeed += 2.5E-4f * f;
            this.currentRadius -= this.contractionSpeed;
            this.isContracting = true;
            float fDistance = Utility.distance(this.posX, this.posY, this.targetX, this.targetY);
            float angleBetweenPoints = Utility.getAngleBetweenPoints(this.posX, this.posY, this.targetX, this.targetY);
            if (fDistance > 1.0f) {
                float f2 = this.contractionSpeed;
                if (f2 > fDistance * f) {
                    f2 = fDistance * f;
                }
                this.posX += f2 * Utility.fastCos(angleBetweenPoints) * f;
                this.posY += f2 * Utility.fastSin(angleBetweenPoints) * f;
            }
        } else {
            this.isContracting = false;
            this.posX = (float) (((double) this.posX) + (((double) (this.targetX - this.posX)) * 0.003d * ((double) f)));
            this.posY = (float) (((double) this.posY) + (((double) (this.targetY - this.posY)) * 0.003d * ((double) f)));
        }
        if (this.currentRadius < this.targetRadius) {
            this.currentRadius = this.targetRadius;
            this.contractionSpeed = 0.0f;
        }
        if (this.targetRadius < 0.0f) {
            removeFromGame();
            return;
        }
        this.c -= f;
        if (!this.isDead && this.c <= 0.0f && !this.isZoneMarker) {
            this.c = 2.0f;
            float fFastCos = this.currentRadius * Utility.fastCos(45.0f);
            float f3 = this.posX - fFastCos;
            float f4 = this.posX + fFastCos;
            float f5 = this.posY - fFastCos;
            float f6 = this.posY + fFastCos;
            float f7 = this.currentRadius * this.currentRadius;
            for (BaseUnit baseUnit : BaseUnit.getGlobalUnitList()) {
                if (baseUnit.posX <= f3 || baseUnit.posX >= f4 || baseUnit.posY <= f5 || baseUnit.posY >= f6) {
                    if (Utility.distanceSq(this.posX, this.posY, baseUnit.posX, baseUnit.posY) >= f7 && !baseUnit.isDead && !(baseUnit instanceof Tree) && !baseUnit.u() && baseUnit.transportContainer == null) {
                        baseUnit.applyDamage(this, (0.5f + (baseUnit.currentHealth * 0.002f) + (baseUnit.maxHealth * 0.001f)) * this.damageMultiplier, (Projectile) null);
                    }
                }
            }
        }
        if (!this.isZoneMarker) {
            GameEngine gameEngine = GameEngine.getInstance();
            this.j += f;
            if (this.j > 3.0f) {
                this.j = 0.0f;
                int randomIntInRange = gameEngine.viewpointXInt + Utility.getRandomIntInRange(0, (int) gameEngine.visibleWorldWidth);
                int randomIntInRange2 = gameEngine.viewpointYInt + Utility.getRandomIntInRange(0, (int) gameEngine.visibleWorldHeight);
                if (Utility.distanceSq(this.posX, this.posY, randomIntInRange, randomIntInRange2) > (this.currentRadius + 30.0f) * (this.currentRadius + 30.0f)) {
                    gameEngine.tileMap.setCursorTileIndexFromWorldPoint(randomIntInRange, randomIntInRange2);
                    gameEngine.tileMap.setCursorTileIndexFromTileIndex(gameEngine.tileMap.cursorTileX, gameEngine.tileMap.cursorTileY);
                    Effect effectCreateEffectInternal = gameEngine.effectManager.createEffectInternal(gameEngine.tileMap.cursorTileX + 10, (gameEngine.tileMap.cursorTileY - 10) + 10, 0.0f, EffectType.custom, true, EffectQuality.verylow);
                    if (effectCreateEffectInternal != null) {
                        effectCreateEffectInternal.stripIndex = 19;
                        effectCreateEffectInternal.rotation = Utility.randomFloatInRange(-180.0f, 180.0f);
                        effectCreateEffectInternal.fadeIn = true;
                        effectCreateEffectInternal.drawLayer = (short) 1;
                        effectCreateEffectInternal.alpha = 0.7f;
                        effectCreateEffectInternal.lifeTimer = 30.0f;
                        effectCreateEffectInternal.lifeMax = effectCreateEffectInternal.lifeTimer;
                        effectCreateEffectInternal.scaleFrom = 0.2f;
                        effectCreateEffectInternal.scaleTo = 1.2f;
                        effectCreateEffectInternal.startColor = Color.a(255, 173, 12, 12);
                    }
                }
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public int s() {
        return 0;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean t() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.DummyUnit, com.corrodinggames.rts.game.units.BaseUnit
    public boolean u() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public boolean a(GameEngine gameEngine) {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.DummyUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public void a(float f, boolean z) {
        DamageZone damageZoneF;
        GameEngine gameEngine = GameEngine.getInstance();
        float f2 = this.posX - gameEngine.viewpointXSnapped;
        float f3 = this.posY - gameEngine.viewpointYSnapped;
        Paint paint = this.isContracting ? m : k;
        if (this.isZoneMarker) {
            paint = o;
        }
        float f4 = this.currentRadius;
        if (this.isFirstUpdate && (damageZoneF = f()) != null) {
            f4 = damageZoneF.targetRadius - 300.0f;
        }
        gameEngine.renderGraphicsEngine.a(f2, f3, f4, paint);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean a(int i, int i2) {
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.renderGraphicsEngine.i();
        gameEngine.renderGraphicsEngine.a(gameEngine.minimap.minimapBoundsRect);
        float fWorldToMinimapX = gameEngine.minimap.worldToMinimapX(this.currentRadius);
        Paint paint = this.isContracting ? n : l;
        if (this.isZoneMarker) {
            paint = p;
        }
        GraphicsUtils.a(gameEngine.renderGraphicsEngine, i, i2, fWorldToMinimapX, paint);
        gameEngine.renderGraphicsEngine.j();
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void a(int i) {
        this.currentRadius = i * 100;
        this.targetRadius = i * 100;
    }

    public boolean a(float f, float f2) {
        return Utility.distanceSq(this.targetX, this.targetY, f, f2) >= this.targetRadius * this.targetRadius;
    }

    public PointF a(float f, float f2, float f3) {
        if (f3 > this.targetRadius) {
            f3 = this.targetRadius;
        }
        float angleBetweenPoints = Utility.getAngleBetweenPoints(this.posX, this.posY, f, f2);
        float f4 = this.targetRadius - f3;
        float fFastCos = this.posX + (Utility.fastCos(angleBetweenPoints) * f4);
        float fFastSin = this.posY + (Utility.fastSin(angleBetweenPoints) * f4);
        r.x = fFastCos;
        r.y = fFastSin;
        return r;
    }
}
