package com.corrodinggames.rts.game.units.custom.hooks;

import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import com.corrodinggames.rts.game.units.custom.CustomUnit;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig;
import com.corrodinggames.rts.game.units.custom.LegConfig;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.custom.b.h */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/custom/b/h.class */
public class CustomUnitLegController extends CustomUnitRenderHook {
    public static final CustomUnitRenderHook a = new CustomUnitLegController();
    static final Rect b = new Rect();
    static final RectF c = new RectF();
    static final Paint d = new Paint();

    public static void a(CustomUnit customUnit, float f, boolean z, boolean z2) {
        LegInstance[] legInstanceArr = customUnit.legInstances;
        if (legInstanceArr == null) {
            return;
        }
        CustomUnitConfig customUnitConfig = customUnit.unitConfig;
        float f2 = customUnit.rotationSpeed;
        if (customUnitConfig.lockLegRotationWithMainTurret) {
            f2 = customUnit.movementLevels[customUnitConfig.mainTurretIndex].targetX;
        }
        GameEngine gameEngine = GameEngine.getInstance();
        if (customUnit.isUnitInvulnerable || customUnit.isUnitParalyzed) {
            for (int i = 0; i < customUnitConfig.legConfig.length; i++) {
                legInstanceArr[i].m = true;
            }
            customUnit.dv();
        }
        float maxHealth = customUnit.getRenderScale();
        Paint renderPaint = null;
        boolean z3 = gameEngine.shouldDrawUnitLegDetails || customUnit.isUnitParalyzed;
        for (int i2 = 0; i2 < legInstanceArr.length; i2++) {
            LegConfig legConfig = customUnitConfig.legConfig[i2];
            if ((legConfig.P == z || legConfig.D != null) && legConfig.Q == z2 && !legConfig.p && (legConfig.q == null || !legConfig.q.read(customUnit))) {
                LegInstance legInstance = legInstanceArr[i2];
                if (legInstance.s > 0.0f) {
                    float f3 = customUnit.posZ + legInstance.d;
                    if (renderPaint == null) {
                        renderPaint = customUnit.getRenderPaint();
                    }
                    Paint paint = renderPaint;
                    float fL = 1.0f;
                    if (f3 < -0.3f) {
                        fL = customUnit.getSubmergedRenderAlpha(f3) * 0.003921569f;
                    }
                    if (legInstance.s < 1.0f) {
                        fL *= legInstance.s;
                    }
                    if (fL < 1.0f) {
                        int i3 = (int) (255.0f * fL);
                        if (paint.f() != i3) {
                            d.a(paint);
                            int iF = d.f();
                            if (iF < i3) {
                                i3 = iF;
                            }
                            d.c(i3);
                            paint = d;
                        }
                    }
                    float f4 = (customUnit.posX + legInstance.b) - gameEngine.viewpointXSnapped;
                    float f5 = (((customUnit.posY + legInstance.c) - gameEngine.viewpointYSnapped) - legInstance.d) - customUnit.posZ;
                    GraphicsEngine graphicsEngine = gameEngine.renderGraphicsEngine;
                    if (maxHealth != 1.0f) {
                        graphicsEngine.k();
                        graphicsEngine.a(maxHealth, maxHealth, f4, f5);
                    }
                    if (legConfig.D != null && !z && gameEngine.shouldDrawUnitShadows && legInstance.d + customUnit.posZ > 0.0f) {
                        graphicsEngine.a(legConfig.D, f4, f5 + legInstance.d + customUnit.posZ, legInstance.i + legInstance.r + legConfig.R, customUnit.getSelectionPaint());
                    }
                    if (legConfig.P == z) {
                        Texture texture = legConfig.B;
                        if (legConfig.C != null) {
                            texture = legConfig.C[customUnit.team.getTeamColorIndex()];
                        }
                        if (!legConfig.H && ((z3 || legConfig.G) && texture != null)) {
                            graphicsEngine.a(texture, f4, f5, legInstance.i + legInstance.r + legConfig.R, paint);
                        }
                        Texture texture2 = legConfig.x;
                        if (legConfig.y != null) {
                            texture2 = legConfig.y[customUnit.team.getTeamColorIndex()];
                        }
                        if (texture2 != null && (z3 || legConfig.F)) {
                            float f6 = texture2.u;
                            float fSqrt = f6;
                            float fFastCos = Utility.fastCos(f2);
                            float fFastSin = Utility.fastSin(f2);
                            float f7 = (fFastCos * legConfig.k) - (fFastSin * legConfig.j);
                            float f8 = (fFastSin * legConfig.k) + (fFastCos * legConfig.j);
                            float angleBetweenPoints = Utility.getAngleBetweenPoints(legInstance.b, legInstance.c, f7, f8);
                            float fDistanceSq = Utility.distanceSq(legInstance.b, legInstance.c, f7, f8);
                            if (fDistanceSq < (f6 - 2.0f) * (f6 - 2.0f)) {
                                fSqrt = Utility.fastSquareRootInt((int) fDistanceSq);
                            }
                            graphicsEngine.k();
                            graphicsEngine.a(angleBetweenPoints + 90.0f, f4, f5);
                            b.a(0, (int) (f6 - fSqrt), texture2.p, (int) (f6 + fSqrt));
                            c.a(f4 - texture2.r, f5 - fSqrt, f4 + texture2.r, f5 + fSqrt);
                            graphicsEngine.a(texture2, b, c, paint);
                            graphicsEngine.l();
                        }
                        if (legConfig.H && ((z3 || legConfig.G) && texture != null)) {
                            graphicsEngine.a(texture, f4, f5, legInstance.i + legInstance.r + legConfig.R, paint);
                        }
                    }
                    if (maxHealth != 1.0f) {
                        graphicsEngine.l();
                    }
                }
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.custom.hooks.CustomUnitRenderHook
    public void b(CustomUnit customUnit, float f) {
        Effect effectCreateMuzzleFlash;
        CustomUnitConfig customUnitConfig = customUnit.unitConfig;
        LegInstance[] legInstanceArr = customUnit.legInstances;
        if (legInstanceArr == null) {
            return;
        }
        if (f != 0.0f && customUnit.frameAnimationDelay > 0.3d) {
            return;
        }
        AttachmentSlotDefinition attachmentSlotDefinitionDn = customUnit.dn();
        if (attachmentSlotDefinitionDn != null && attachmentSlotDefinitionDn.t) {
            return;
        }
        if (customUnit.transportContainer != null && attachmentSlotDefinitionDn == null) {
            for (LegInstance legInstance : legInstanceArr) {
                legInstance.m = true;
            }
            return;
        }
        GameEngine gameEngine = GameEngine.getInstance();
        float f2 = customUnit.rotationSpeed;
        if (customUnitConfig.lockLegRotationWithMainTurret) {
            f2 = customUnit.movementLevels[customUnitConfig.mainTurretIndex].targetX;
        }
        float f3 = customUnit.posX - customUnit.dP;
        float f4 = customUnit.posY - customUnit.dQ;
        float f5 = customUnit.posZ - customUnit.dR;
        float f6 = f2 - customUnit.dS;
        boolean z = (f3 == 0.0f && f4 == 0.0f && f6 == 0.0f) ? false : true;
        customUnit.dP = customUnit.posX;
        customUnit.dQ = customUnit.posY;
        customUnit.dR = customUnit.posZ;
        customUnit.dS = f2;
        PointF pointFN = customUnit.getMovementDeltaOffset(15.0f);
        float fFastCos = pointFN.x;
        float fFastSin = pointFN.y;
        if (fFastCos != 0.0f || fFastSin != 0.0f) {
            float fDistanceSq = Utility.distanceSq(0.0f, 0.0f, fFastCos, fFastSin);
            float angleBetweenPoints = Utility.getAngleBetweenPoints(0.0f, 0.0f, fFastCos, fFastSin);
            float f7 = fDistanceSq * 240.0f;
            if (f7 > 15.0f) {
                f7 = 15.0f;
            }
            fFastCos = Utility.fastCos(angleBetweenPoints) * f7;
            fFastSin = Utility.fastSin(angleBetweenPoints) * f7;
        }
        int i = 0;
        float f8 = 0.0f;
        int i2 = 0;
        for (int i3 = 0; i3 < legInstanceArr.length; i3++) {
            LegInstance legInstance2 = legInstanceArr[i3];
            LegConfig legConfig = customUnitConfig.legConfig[i3];
            boolean z2 = false;
            boolean z3 = false;
            if (legInstance2.m) {
                z2 = true;
                legInstance2.m = false;
                legInstance2.o = true;
                if (legInstance2.n) {
                    z3 = true;
                }
                legInstance2.n = false;
            }
            if (!legConfig.h) {
                legInstance2.d -= f5;
            }
            if (!legConfig.l) {
                if (z) {
                    legInstance2.b -= f3;
                    legInstance2.c -= f4;
                    legInstance2.o = true;
                }
            } else if (z && f6 != 0.0f) {
                Utility.c.a(legInstance2.b, legInstance2.c);
                Utility.rotatePoint(0.0f, 0.0f, f6, Utility.c);
                legInstance2.b = Utility.c.x;
                legInstance2.c = Utility.c.y;
                legInstance2.i += f6;
                legInstance2.o = true;
            }
            if (!legConfig.p) {
                if (legConfig.T != 0.0f) {
                    legInstance2.r += legConfig.T * f;
                    legInstance2.r %= 360.0f;
                }
                if (legInstance2.o) {
                    float fFastCos2 = Utility.fastCos(f2);
                    float fFastSin2 = Utility.fastSin(f2);
                    float f9 = legConfig.d + legInstance2.p;
                    float f10 = legConfig.e + legInstance2.q;
                    legInstance2.f = (fFastCos2 * f10) - (fFastSin2 * f9);
                    legInstance2.g = (fFastSin2 * f10) + (fFastCos2 * f9);
                    if (z2) {
                        legInstance2.b = legInstance2.f;
                        legInstance2.c = legInstance2.g;
                        legInstance2.i = f2 + legConfig.i;
                        legInstance2.o = true;
                        if (z3) {
                            legInstance2.b *= 0.6f;
                            legInstance2.c *= 0.6f;
                            legInstance2.d = -3.0f;
                        }
                    }
                    if (!legConfig.l) {
                        legInstance2.f += fFastCos * legConfig.m;
                        legInstance2.g += fFastSin * legConfig.m;
                    }
                    legInstance2.h = Utility.distanceSq(legInstance2.b, legInstance2.c, legInstance2.f, legInstance2.g);
                    if (legInstance2.h > f8) {
                        i2 = i3;
                        f8 = legInstance2.h;
                    }
                    if (legInstance2.k && !legConfig.l) {
                        i++;
                    }
                }
            }
        }
        for (int i4 = 0; i4 < legInstanceArr.length; i4++) {
            LegInstance legInstance3 = legInstanceArr[i4];
            LegConfig legConfig2 = customUnitConfig.legConfig[i4];
            if (!legConfig2.p) {
                float f11 = legConfig2.g;
                if (!legConfig2.h) {
                    f11 -= customUnit.posZ;
                }
                float f12 = f11 + legConfig2.f;
                if (legInstance3.h > 90000.0f) {
                    legInstance3.b = legConfig2.d;
                    legInstance3.c = legConfig2.e;
                } else if (legInstance3.h > legConfig2.O * legConfig2.O) {
                    float angleBetweenPoints2 = Utility.getAngleBetweenPoints(legInstance3.f, legInstance3.g, legInstance3.b, legInstance3.c);
                    legInstance3.b = legInstance3.f + (Utility.fastCos(angleBetweenPoints2) * legConfig2.O);
                    legInstance3.c = legInstance3.g + (Utility.fastSin(angleBetweenPoints2) * legConfig2.O);
                    legInstance3.k = true;
                }
                if (!legInstance3.k && legInstance3.d <= f11 + 0.1f && i < legConfig2.L && (i4 == i2 || !legConfig2.M)) {
                    boolean z4 = false;
                    if (legConfig2.n) {
                        for (int i5 = 0; i5 < legConfig2.S.length; i5++) {
                            if (legInstanceArr[legConfig2.S[i5]].k) {
                                z4 = true;
                            }
                        }
                    }
                    float f13 = legConfig2.K;
                    if (z4) {
                        f13 = legConfig2.N;
                    }
                    if (legInstance3.h > f13 * f13) {
                        legInstance3.k = true;
                        i++;
                    }
                }
                if (legConfig2.l) {
                    legInstance3.k = true;
                }
                if (!legInstance3.k) {
                    legInstance3.e = 0.0f;
                    if (legInstance3.d > f11) {
                        legInstance3.d -= f * legConfig2.v;
                        if (legInstance3.d <= f11) {
                            legInstance3.d = f11;
                            float f14 = customUnit.posX + legInstance3.b;
                            float f15 = customUnit.posY + legInstance3.c;
                            legInstance3.l = GameViewUtils.c(f14, f15);
                            if (legConfig2.I && !legInstance3.j) {
                                legInstance3.j = true;
                                if (legInstance3.l) {
                                    if (gameEngine.shouldDrawMediumDetailEffects && customUnit.shouldDraw) {
                                        gameEngine.effectManager.createRedLaserEffect(f14, f15, legInstance3.d, 0, 0.0f, 0.0f);
                                    }
                                } else if (gameEngine.shouldDrawHighDetailEffects && customUnit.shouldDraw && (effectCreateMuzzleFlash = gameEngine.effectManager.createMuzzleFlash(f14, f15, legInstance3.d, legInstance3.i, 0)) != null) {
                                    effectCreateMuzzleFlash.velocityX = 0.0f;
                                    effectCreateMuzzleFlash.velocityY = 0.0f;
                                    effectCreateMuzzleFlash.scaleFrom = 1.6f;
                                    effectCreateMuzzleFlash.scaleTo = 2.8f;
                                }
                            }
                        }
                    } else if (legInstance3.l && legInstance3.d > (-3.0f) + f11) {
                        legInstance3.d -= f * 0.3f;
                    }
                } else if (legInstance3.d > f12 || legConfig2.l) {
                    float f16 = f * legConfig2.s;
                    if (legInstance3.h <= f16 * f16) {
                        legInstance3.b = legInstance3.f;
                        legInstance3.c = legInstance3.g;
                        legInstance3.o = true;
                        legInstance3.k = false;
                    } else {
                        float angleBetweenPoints3 = Utility.getAngleBetweenPoints(legInstance3.b, legInstance3.c, legInstance3.f, legInstance3.g);
                        legInstance3.b += Utility.fastCos(angleBetweenPoints3) * f16;
                        legInstance3.c += Utility.fastSin(angleBetweenPoints3) * f16;
                        legInstance3.o = true;
                    }
                    if (legConfig2.l && legInstance3.d > f11) {
                        legInstance3.d -= f * legConfig2.v;
                        if (legInstance3.d <= f11) {
                            legInstance3.d = f11;
                        }
                    }
                    legInstance3.i += Utility.rotateTowardsAngle(legInstance3.i, f2 + legConfig2.i, legConfig2.u * f);
                    legInstance3.j = false;
                } else if (legInstance3.e < legConfig2.t) {
                    legInstance3.e += f;
                } else {
                    legInstance3.d += f * legConfig2.v;
                }
            }
        }
    }
}
