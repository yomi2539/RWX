package com.corrodinggames.rts.game.units.land;

import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.units.UnitMovementType;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.effects.EffectQuality;
import com.corrodinggames.rts.gameFramework.effects.EffectType;
import com.corrodinggames.rts.gameFramework.graphics.Texture;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.e.h */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/e/h.class */
public abstract class HoverLandUnit extends LandUnit {
    /* JADX INFO: renamed from: l */
    float hoverTimer;
    public static Texture m = null;
    public static Texture[] n = new Texture[10];

    public HoverLandUnit(boolean z) {
        super(z);
    }

    @Override // com.corrodinggames.rts.game.units.land.LandUnit, com.corrodinggames.rts.game.units.BaseUnit
    public Texture v() {
        if (this.team.teamId == -1) {
            return null;
        }
        if (isExperimental()) {
            return LandUnit.landUnitIconTexturesExp[this.team.getTeamColorIndex()];
        }
        return n[this.team.getTeamColorIndex()];
    }

    public static void K() {
        m = GameEngine.getInstance().renderGraphicsEngine.a(R.drawable.unit_icon_hover);
        n = PlayerTeam.getTeamColorTextures(m);
    }

    @Override // com.corrodinggames.rts.game.units.land.LandUnit, com.corrodinggames.rts.game.units.BaseUnit
    public UnitMovementType getMovementType() {
        return UnitMovementType.HOVER;
    }

    @Override // com.corrodinggames.rts.game.units.land.LandUnit, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f) {
        super.update(f);
        if (isAlive() && !this.isDead && isOverLiquid()) {
            if (this.rotation > 0.0f) {
                this.hoverTimer += f;
            }
            if (this.hoverTimer > 10.0f) {
                this.hoverTimer = 0.0f;
                if (isVisibleOnScreen()) {
                    Effect effectCreateEffectInternal = GameEngine.getInstance().effectManager.createEffectInternal(this.posX + (Utility.fastCos(this.rotationSpeed) * 4.0f), this.posY + (Utility.fastSin(this.rotationSpeed) * 4.0f), 0.0f, EffectType.custom, false, EffectQuality.low);
                    if (effectCreateEffectInternal != null) {
                        effectCreateEffectInternal.stripIndex = 0;
                        effectCreateEffectInternal.frameIndex = 13;
                        effectCreateEffectInternal.drawLayer = (short) 1;
                        effectCreateEffectInternal.fadeIn = true;
                        effectCreateEffectInternal.alpha = 0.8f;
                        effectCreateEffectInternal.lifeMax = 80.0f;
                        effectCreateEffectInternal.lifeTimer = 80.0f;
                        effectCreateEffectInternal.velocityX = (-Utility.fastCos(this.rotationSpeed)) * 0.1f;
                        effectCreateEffectInternal.velocityY = (-Utility.fastSin(this.rotationSpeed)) * 0.1f;
                        effectCreateEffectInternal.rotation = Utility.randomFloatInRange(-180.0f, 180.0f);
                    }
                }
            }
        }
    }
}
