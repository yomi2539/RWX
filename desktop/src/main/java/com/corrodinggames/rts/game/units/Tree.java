package com.corrodinggames.rts.game.units;

import android.graphics.Paint;
import android.graphics.Rect;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.effects.EffectQuality;
import com.corrodinggames.rts.gameFramework.effects.EffectType;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import java.io.IOException;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.al */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/al.class */
public class Tree extends NaturalUnit {
    static Texture[] a = new Texture[3];
    static Texture b = null;
        /* JADX INFO: renamed from: c */
    Texture texture;
        /* JADX INFO: renamed from: d */
    int treeType;
        /* JADX INFO: renamed from: e */
    int subType;
        /* JADX INFO: renamed from: f */
    int frameIndex;
    float g;
    boolean h;
    float i;
        /* JADX INFO: renamed from: j */
    int textureOffsetX;
        /* JADX INFO: renamed from: k */
    int textureOffsetY;
        /* JADX INFO: renamed from: l */
    float scale;
        /* JADX INFO: renamed from: m */
    boolean hasSubType;

    public static void b() {
        GameEngine gameEngine = GameEngine.getInstance();
        a[0] = gameEngine.renderGraphicsEngine.a(R.drawable.palm_tree);
        a[1] = gameEngine.renderGraphicsEngine.a(R.drawable.trees);
        a[2] = gameEngine.renderGraphicsEngine.a(R.drawable.trees_snow);
        b = gameEngine.renderGraphicsEngine.a(R.drawable.palm_leaves);
    }

    public Tree(boolean z) {
        super(z);
        this.frameIndex = 0;
        this.textureOffsetX = 0;
        this.textureOffsetY = 0;
        this.scale = 1.0f;
        this.hasSubType = false;
        b(1, -1);
        this.radius = 3.0f;
        this.displayRadius = this.radius + 1.0f;
        this.maxHealth = 100.0f;
        this.currentHealth = this.maxHealth;
        this.rotationSpeed = -90.0f;
        S(3);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeInt(this.treeType);
        gameOutputStream.writeInt(this.frameIndex);
        gameOutputStream.writeFloat(this.g);
        gameOutputStream.writeBoolean(this.h);
        gameOutputStream.writeFloat(this.i);
        gameOutputStream.writeByte(2);
        gameOutputStream.writeFloat(this.scale);
        gameOutputStream.writeInt(this.subType);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject
    public void a(GameInputStream gameInputStream) throws IOException {
        this.treeType = gameInputStream.readInt();
        this.frameIndex = gameInputStream.readInt();
        this.g = gameInputStream.readFloat();
        this.h = gameInputStream.readBoolean();
        this.i = gameInputStream.readFloat();
        byte b2 = gameInputStream.readByte();
        if (b2 >= 1) {
            this.scale = gameInputStream.readFloat();
        } else {
            this.scale = 1.0f;
        }
        if (b2 >= 2) {
            this.subType = gameInputStream.readInt();
        } else {
            this.subType = 0;
        }
        b(this.treeType, this.subType);
        super.a(gameInputStream);
        if (this.isDead) {
            this.hasSubType = false;
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean e() {
        k();
        return true;
    }

    public Texture d() {
        return this.texture;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void a_(String str) {
        int i = -1;
        String[] strArrSplit = str.split("\\.");
        if (strArrSplit.length != 0 && strArrSplit.length != 1) {
            if (strArrSplit.length == 2) {
                str = strArrSplit[0];
                try {
                    i = Integer.parseInt(strArrSplit[1]);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Tree sub type format error:" + strArrSplit[1]);
                }
            } else {
                throw new RuntimeException("Tree sub unknown format with parts:" + strArrSplit.length);
            }
        }
        try {
            b(Integer.parseInt(str), i);
        } catch (NumberFormatException e2) {
            throw new RuntimeException("Tree type format error:" + str);
        }
    }

    public void b(int i, int i2) {
        this.treeType = i;
        this.subType = i2;
        if (this.treeType == 0) {
            T(27);
            U(41);
            this.textureOffsetX = 1;
            this.textureOffsetY = 1;
            this.texture = a[0];
            return;
        }
        if (this.treeType == 1 || this.treeType == 2) {
            if (i2 == -1) {
                i2 = Utility.getDeterministicRandomIntInRange(0, 4, (int) this.objectId);
            }
            if (i2 < 0 || i2 > 4) {
                throw new RuntimeException("Tree subType out of range:" + i2);
            }
            T(25);
            U(30);
            if (this.treeType == 1) {
                this.texture = a[1];
            } else {
                this.texture = a[2];
            }
            this.textureOffsetX = 0;
            this.textureOffsetY = 30 * i2;
            if (i2 == 0) {
                this.scale = Utility.getDeterministicRandomFloat(1.0f, 1.2f, ((int) this.objectId) + 1);
            } else {
                this.scale = Utility.getDeterministicRandomFloat(1.0f, 2.0f, ((int) this.objectId) + 1);
            }
            this.hasSubType = true;
            return;
        }
        throw new RuntimeException("Tree type:" + this.treeType + " is not supported");
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f) {
        if (this.treeType == 0) {
            if (this.h) {
                if (this.frameIndex < 4) {
                    this.g += f;
                    if (this.g > 5.0f) {
                        this.g = 0.0f;
                        this.frameIndex++;
                        return;
                    }
                    return;
                }
                return;
            }
            if (this.i != 0.0f) {
                this.i = Utility.moveTowardsZero(this.i, 0.1f * f);
                this.frameIndex = 2;
            } else if (this.frameIndex > 1) {
                this.frameIndex = 1;
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public Rect a_(boolean z) {
        int i = this.textureOffsetX;
        int i2 = this.textureOffsetY;
        int i3 = i + (this.frameIndex * (this.es + 1));
        dC.a(i3, i2, i3 + this.es, i2 + this.et);
        return dC;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public boolean c(float f) {
        Texture textureD = d();
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine.zoom < 0.15d) {
            return false;
        }
        du.a(getUnitBounds());
        du.a(0.0f, (int) (-this.posZ));
        float fD = du.d();
        float fE = du.e();
        dv.a(a_(false));
        GraphicsEngine graphicsEngine = gameEngine.renderGraphicsEngine;
        graphicsEngine.k();
        if (this.scale != 1.0f) {
            graphicsEngine.a(this.scale, this.scale, fD, fE);
        }
        if (this.hasSubType) {
            dv.a(this.es, 0);
            gameEngine.renderGraphicsEngine.a(textureD, dv, du, (Paint) null);
            dv.a(-this.es, 0);
        }
        graphicsEngine.a(getRenderRotation(false), fD, fE);
        graphicsEngine.a(textureD, dv, du, (Paint) null);
        graphicsEngine.l();
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
    /* JADX INFO: renamed from: aj */
    public boolean canUnitAttack() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: ak */
    public boolean canMove() {
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
    /* JADX INFO: renamed from: f, reason: merged with bridge method [inline-methods] */
    public UnitTypeEnum r() {
        return UnitTypeEnum.tree;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean a(BaseUnit baseUnit, float f) {
        if (!this.h) {
            if (this.i == 0.0f) {
            }
            this.currentHealth -= (((baseUnit.getPushMass() / 3000.0f) * this.maxHealth) * 0.06f) * f;
            this.i = 1.0f;
            this.damageEffectDurationTimer = 1000.0f;
            if (this.currentHealth <= 0.0f) {
                this.rotationSpeed = Utility.getAngleBetweenPoints(this.posX, this.posY, baseUnit.posX, baseUnit.posY) + 180.0f;
                k();
            }
            if (!this.h) {
                return false;
            }
            return true;
        }
        return true;
    }

    public void k() {
        if (!this.h) {
            GameEngine gameEngine = GameEngine.getInstance();
            this.frameIndex = 2;
            this.g = 0.0f;
            S(0);
            this.isAlive = false;
            this.isDead = true;
            this.unitCreationTime = gameEngine.gameTimeMillis;
            this.h = true;
            this.hasSubType = false;
            for (int i = 0; i < 1; i++) {
                gameEngine.effectManager.setOnlyOnScreen();
                Effect effectCreateEffectInternal = gameEngine.effectManager.createEffectInternal(this.posX + Utility.randomFloatInRange(-12.0f, 12.0f), this.posY + Utility.randomFloatInRange(-12.0f, 12.0f), this.posZ, EffectType.custom, false, EffectQuality.high);
                if (effectCreateEffectInternal != null) {
                    effectCreateEffectInternal.stripIndex = 9;
                    effectCreateEffectInternal.frameIndex = Utility.getRandomIntInRange(4, 5);
                    effectCreateEffectInternal.rotation = Utility.randomFloatInRange(-180.0f, 180.0f);
                    effectCreateEffectInternal.isCentered = true;
                    effectCreateEffectInternal.posZ = 5.0f + Utility.randomFloatInRange(0.0f, 3.0f);
                    effectCreateEffectInternal.velocityX = Utility.randomFloatInRange(-0.05f, 0.05f) + (Utility.fastCos(this.rotationSpeed) * 0.4f);
                    effectCreateEffectInternal.velocityY = Utility.randomFloatInRange(-0.05f, 0.05f) + (Utility.fastSin(this.rotationSpeed) * 0.4f);
                    effectCreateEffectInternal.useBounce = true;
                    effectCreateEffectInternal.physicsGravity = 0.2f;
                    effectCreateEffectInternal.scaleFrom = 0.4f * this.scale;
                    effectCreateEffectInternal.scaleTo = 0.4f * this.scale;
                    effectCreateEffectInternal.lifeTimer = 90 + Utility.getRandomIntInRange(0, 40);
                    effectCreateEffectInternal.lifeMax = effectCreateEffectInternal.lifeTimer;
                    effectCreateEffectInternal.fadeIn = true;
                    effectCreateEffectInternal.drawLayer = (short) 2;
                }
            }
            float fFastCos = this.posX + (Utility.fastCos(this.rotationSpeed) * (this.et - 5));
            float fFastSin = this.posY + (Utility.fastSin(this.rotationSpeed) * (this.et - 5));
            boolean z = true;
            for (int i2 = 0; i2 < 1; i2++) {
                gameEngine.effectManager.setOnlyOnScreen();
                Effect effectCreateEffectInternal2 = gameEngine.effectManager.createEffectInternal(fFastCos + Utility.randomFloatInRange(-17, 17), fFastSin + Utility.randomFloatInRange(-17, 17), this.posZ, EffectType.custom, false, EffectQuality.high);
                if (effectCreateEffectInternal2 != null) {
                    effectCreateEffectInternal2.stripIndex = 9;
                    effectCreateEffectInternal2.frameIndex = Utility.getRandomIntInRange(4, 5);
                    if (z) {
                        z = false;
                        effectCreateEffectInternal2.frameIndex = 3;
                    }
                    effectCreateEffectInternal2.rotation = Utility.randomFloatInRange(-180.0f, 180.0f);
                    effectCreateEffectInternal2.isCentered = true;
                    if (effectCreateEffectInternal2.frameIndex == 3) {
                        effectCreateEffectInternal2.velocityX = Utility.randomFloatInRange(-0.05f, 0.05f);
                        effectCreateEffectInternal2.velocityY = Utility.randomFloatInRange(-0.05f, 0.05f);
                        effectCreateEffectInternal2.scaleFrom = 1.5f * this.scale;
                        effectCreateEffectInternal2.scaleTo = 2.2f * this.scale;
                        effectCreateEffectInternal2.lifeTimer = 90 + Utility.getRandomIntInRange(0, 40);
                        effectCreateEffectInternal2.drawLayer = (short) 2;
                    } else {
                        effectCreateEffectInternal2.velocityX = Utility.randomFloatInRange(-0.05f, 0.05f);
                        effectCreateEffectInternal2.velocityY = Utility.randomFloatInRange(-0.05f, 0.0f);
                        effectCreateEffectInternal2.scaleFrom = 1.3f;
                        effectCreateEffectInternal2.scaleTo = 1.3f;
                        effectCreateEffectInternal2.lifeTimer = 60 + Utility.getRandomIntInRange(0, 40);
                        effectCreateEffectInternal2.drawLayer = (short) 1;
                    }
                    effectCreateEffectInternal2.lifeMax = effectCreateEffectInternal2.lifeTimer;
                    effectCreateEffectInternal2.fadeIn = true;
                }
            }
            if (this.treeType == 1 || this.treeType == 2) {
                this.posX += Utility.fastCos(this.rotationSpeed) * ((this.et / 2) - 3);
                this.posY += Utility.fastSin(this.rotationSpeed) * ((this.et / 2) - 3);
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void n() {
        super.n();
        this.rotationSpeed = Utility.normalizeAngle((this.posY * 5.0f) + (this.posX * 3.0f), false);
        if (this.treeType == 0) {
            this.frameIndex = ((int) ((this.posY * 5.0f) + (this.posX * 3.0f))) % 1;
        }
        if (this.treeType == 1) {
        }
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
    /* JADX INFO: renamed from: a */
    public float applyDamage(BaseUnit baseUnit, float f, Projectile projectile) {
        boolean z = this.isDead;
        float fA = super.applyDamage(baseUnit, f, projectile);
        if (!z && this.isDead && projectile != null) {
            this.rotationSpeed = Utility.getAngleBetweenPoints(this.posX, this.posY, projectile.posX, projectile.posY) + 180.0f;
        }
        return fA;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean q() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean t() {
        return true;
    }
}
