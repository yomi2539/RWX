package com.corrodinggames.rts.game.units.air;

import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.units.MovableUnit;
import com.corrodinggames.rts.game.units.UnitMovementType;
import com.corrodinggames.rts.game.units.UnitSize;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import java.io.IOException;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.b.b */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/b/b.class */
public abstract class AirUnit extends MovableUnit {
    float h;
    boolean i;
    float j;
    Boolean k;
    Boolean l;
    public static Texture m = null;
    public static Texture[] n = new Texture[10];

    public AirUnit(boolean z) {
        super(z);
        this.i = false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeFloat(this.h);
        gameOutputStream.writeBoolean(this.i);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject
    public void a(GameInputStream gameInputStream) throws IOException {
        this.h = gameInputStream.readFloat();
        this.i = gameInputStream.readBoolean();
        super.a(gameInputStream);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public Texture v() {
        if (this.team.teamId == -1) {
            return null;
        }
        return n[this.team.getTeamColorIndex()];
    }

    /* JADX INFO: renamed from: K */
    public static void loadAirUnitTextures() {
        m = GameEngine.getInstance().renderGraphicsEngine.a(R.drawable.unit_icon_air);
        n = PlayerTeam.getTeamColorTextures(m);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public UnitMovementType getMovementType() {
        return UnitMovementType.AIR;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f) {
        Effect effectCreateSmokeEffect;
        super.update(f);
        if (this.isDead) {
            if (this.posZ > 0.0f) {
                this.h += 0.06f * f;
                this.posZ -= this.h * f;
                return;
            }
            if (this.k == null) {
                this.k = Boolean.valueOf(isOverLiquid());
            }
            if (this.l == null) {
                this.l = Boolean.valueOf(isOverWater());
            }
            if (!this.i) {
                this.i = true;
                if (this.k.booleanValue()) {
                    a(UnitSize.verysmall);
                    if (this.l.booleanValue()) {
                        GameEngine.getInstance().effectManager.createDirectedExplosion(this.posX, this.posY, 0.0f, 0, 0.0f, 0.0f, this.rotationSpeed);
                    }
                } else {
                    a(UnitSize.small);
                }
                this.h = 0.0f;
                return;
            }
            if (!this.k.booleanValue()) {
                this.posZ = 0.0f;
                return;
            }
            if (this.posZ > -10.0f) {
                this.h += 8.0E-4f * f;
                this.posZ -= this.h * f;
                if (this.l.booleanValue()) {
                    this.j += f;
                    if (this.j > 30.0f) {
                        this.j = 0.0f;
                        if (isVisibleOnScreen() && (effectCreateSmokeEffect = GameEngine.getInstance().effectManager.createSmokeEffect(this.posX, this.posY, this.posZ, this.rotationSpeed)) != null) {
                            effectCreateSmokeEffect.velocityX = 0.0f;
                            effectCreateSmokeEffect.velocityY = -0.1f;
                        }
                    }
                }
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean e() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.posZ > -1.0f) {
            for (int i = 0; i < 3; i++) {
                gameEngine.effectManager.createBloodEffect2(this.posX, this.posY, this.posZ);
            }
        }
        return super.e();
    }
}
