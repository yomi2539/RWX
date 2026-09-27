package com.corrodinggames.rts.game.units;

import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.game.map.MapTile;
import com.corrodinggames.rts.game.map.TileMap;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.GameObject;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import java.io.IOException;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.ai */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/ai.class */
public class FireUnit extends NaturalUnit {
    /* JADX INFO: renamed from: b */
    Texture texture;
    /* JADX INFO: renamed from: c */
    int fireType;
    /* JADX INFO: renamed from: d */
    int frameIndex;
    /* JADX INFO: renamed from: e */
    float frameTimer;
    float f;
    /* JADX INFO: renamed from: g */
    int textureOffsetX;
    /* JADX INFO: renamed from: h */
    int textureOffsetY;
    float i;
    float j;
    /* JADX INFO: renamed from: k */
    boolean isInitialized;
    /* JADX INFO: renamed from: l */
    float growthRate;
    /* JADX INFO: renamed from: m */
    float maxGrowth;
    /* JADX INFO: renamed from: n */
    float spreadThreshold;
    /* JADX INFO: renamed from: o */
    float growth;
    float p;
    /* JADX INFO: renamed from: q */
    float spreadTimer;
    /* JADX INFO: renamed from: r */
    boolean isSpreading;
    /* JADX INFO: renamed from: u */
    Rect renderRect;
    static Texture[] a = new Texture[2];
    static Point s = new Point();
    public static FireUnitFinder t = new FireUnitFinder();

    public static void b() {
        a[0] = GameEngine.getInstance().renderGraphicsEngine.a(R.drawable.fire);
    }

    public FireUnit(boolean z) {
        super(z);
        this.frameIndex = 0;
        this.textureOffsetX = 0;
        this.textureOffsetY = 0;
        this.isInitialized = false;
        this.renderRect = new Rect();
        a(0);
        this.radius = 20.0f;
        this.displayRadius = this.radius + 1.0f;
        this.maxHealth = 100.0f;
        this.currentHealth = this.maxHealth;
        this.rotationSpeed = -90.0f;
        this.isAlive = false;
        this.growth = 0.05f;
        this.p = 120.0f;
        S(3);
    }

    public static FireUnit a(float f, float f2) {
        GameEngine gameEngine = GameEngine.getInstance();
        t.a(f, f2);
        gameEngine.unitSpatialIndex.a(f, f2, 30.0f, null, 1.0f, t);
        return t.foundFireUnit;
    }

    public Texture d() {
        return this.texture;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean e() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeInt(this.fireType);
        gameOutputStream.writeInt(this.frameIndex);
        gameOutputStream.writeFloat(this.frameTimer);
        gameOutputStream.writeByte(0);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void f_() {
        this.isAlive = false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject
    public void a(GameInputStream gameInputStream) throws IOException {
        this.fireType = gameInputStream.readInt();
        this.frameIndex = gameInputStream.readInt();
        this.frameTimer = gameInputStream.readFloat();
        gameInputStream.readByte();
        super.a(gameInputStream);
    }

    public void a(int i) {
        this.fireType = i;
        if (this.fireType == 0) {
            T(20);
            U(20);
            this.textureOffsetX = 0;
            this.textureOffsetY = 0;
            this.texture = a[0];
            return;
        }
        throw new RuntimeException("Fire type:" + this.fireType + " is not supported");
    }

    public void f() {
        this.isInitialized = true;
        this.i = Utility.getDeterministicRandomInt((GameObject) this, -5, 5, 1);
        this.j = Utility.getDeterministicRandomInt((GameObject) this, -5, 5, 2);
        this.frameTimer = Utility.getDeterministicRandomInt((GameObject) this, 1, 10, 3);
        this.frameIndex = Utility.getDeterministicRandomInt((GameObject) this, 0, 2, 4);
        this.f = Utility.getDeterministicRandomInt((GameObject) this, 7, 13, 5);
        GameEngine gameEngine = GameEngine.getInstance();
        TileMap tileMap = gameEngine.tileMap;
        gameEngine.tileMap.setCursorTileIndexFromWorldPoint(this.posX, this.posY);
        int i = gameEngine.tileMap.cursorTileX;
        int i2 = gameEngine.tileMap.cursorTileY;
        if (!tileMap.isInBounds(i, i2)) {
            this.growthRate = 0.0f;
            this.maxGrowth = 0.0f;
            this.spreadThreshold = 2.0f;
            return;
        }
        MapTile tileAt = gameEngine.tileMap.groundLayer.getTileAt(i, i2);
        boolean z = false;
        if (tileAt.isWater || tileAt.isCliff || tileAt.hasLargeObject || tileAt.isWaterBridge) {
            z = true;
        }
        if (z) {
            this.growthRate = 0.0f;
            this.maxGrowth = 0.0f;
            this.spreadThreshold = 2.0f;
        } else {
            this.growthRate = 5.0E-4f;
            this.maxGrowth = 1.0f;
            this.spreadThreshold = 0.3f;
            this.growth += Utility.getDeterministicRandomInt((GameObject) this, 0, 10, 10) / 1000.0f;
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f) {
        super.update(f);
        if (!this.isInitialized) {
            f();
        }
        if (this.growth < this.maxGrowth) {
            this.growth += this.growthRate * f;
            if (this.growth > this.maxGrowth) {
                this.growth = this.maxGrowth;
            }
        }
        if (this.growth > this.spreadThreshold) {
            this.spreadTimer = (float) (((double) this.spreadTimer) + (0.01d * ((double) f)));
            if ((!this.isSpreading && this.spreadTimer > 1.0f) || this.spreadTimer > 8.0f) {
                this.spreadTimer = Utility.getDeterministicRandomInt((GameObject) this, 0, 10, 10) / 1000.0f;
                k();
            }
        }
        this.frameTimer += f;
        if (this.frameTimer > 10.0f) {
            this.frameTimer = 0.0f;
            this.frameIndex++;
            if (this.frameIndex > 3) {
                this.frameIndex = 0;
            }
        }
        if (this.growth < 0.0f) {
            bv();
        }
    }

    public void k() {
        this.isSpreading = true;
        b(-1, -1);
        b(0, -1);
        b(1, -1);
        b(-1, 0);
        b(1, 0);
        b(-1, 1);
        b(0, 1);
        b(1, 1);
    }

    public void b(int i, int i2) {
        GameEngine gameEngine = GameEngine.getInstance();
        float f = (int) (this.posX + (i * gameEngine.tileMap.tileWorldSizeX));
        float f2 = (int) (this.posY + (i2 * gameEngine.tileMap.tileWorldSizeY));
        if (a(f, f2) == null) {
            FireUnit fireUnit = new FireUnit(false);
            fireUnit.posX = f;
            fireUnit.posY = f2;
            fireUnit.setUnitTeam(this.team);
            gameEngine.unitSpatialIndex.a(fireUnit);
            PlayerTeam.c(fireUnit);
            this.isSpreading = false;
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public Rect a_(boolean z) {
        int i = this.textureOffsetX;
        int i2 = this.textureOffsetY;
        int i3 = i + (this.frameIndex * this.es);
        dC.a(i3, i2, i3 + this.es, i2 + this.et);
        return dC;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public boolean c(float f) {
        Texture textureD = d();
        GameEngine gameEngine = GameEngine.getInstance();
        du.a(getUnitBounds());
        du.a(0.0f, (int) (-this.posZ));
        du.a(this.i, this.j);
        dv.a(a_(false));
        gameEngine.renderGraphicsEngine.k();
        float fD = du.d();
        float fE = du.e();
        gameEngine.renderGraphicsEngine.a(getRenderRotation(false), fD, fE);
        gameEngine.renderGraphicsEngine.a(this.growth * 2.7f, this.growth * 2.7f, fD, fE);
        gameEngine.renderGraphicsEngine.a(textureD, dv, du, (Paint) null);
        gameEngine.renderGraphicsEngine.l();
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public UnitMovementType getMovementType() {
        return UnitMovementType.NONE;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean i() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean Q() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: ak */
    public boolean canMove() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: aj */
    public boolean canUnitAttack() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: s_ */
    public boolean isVisibleOnScreen() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean c_() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: s, reason: merged with bridge method [inline-methods] */
    public UnitTypeEnum r() {
        return UnitTypeEnum.spreadingFire;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void n() {
        super.n();
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public float x() {
        return -1.0f;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: l */
    public boolean canAttack() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean P() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: a */
    public float applyDamage(BaseUnit baseUnit, float f, Projectile projectile) {
        this.growth -= f / 100.0f;
        return super.applyDamage(baseUnit, 0.0f, projectile);
    }
}
