package com.corrodinggames.rts.gameFramework.ui.widgets;

import android.graphics.Paint;
import android.graphics.Rect;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.graphics.opengl.GraphicsUtils;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.a.e */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/a/e.class */
public class NinePatchStyle extends UIStyle {
    /* JADX INFO: renamed from: a */
    int patchWidth;

    /* JADX INFO: renamed from: b */
    int patchHeight;

    /* JADX INFO: renamed from: c */
    float normalizedWidth;

    /* JADX INFO: renamed from: d */
    float normalizedHeight;
    /* JADX INFO: renamed from: h */
    static Rect stretchCheckRect = new Rect();
    /* JADX INFO: renamed from: i */
    static Rect halfSizeRect = new Rect();
    /* JADX INFO: renamed from: e */
    public boolean scalePatchToFit = true;
    /* JADX INFO: renamed from: f */
    public boolean useScaledBlit = false;
    /* JADX INFO: renamed from: g */
    public float patchScale = 1.0f;

    public NinePatchStyle() {
    }

    public NinePatchStyle(Texture texture, int i2, int i3) {
        setBackgroundTexture(texture);
        setPatchSize(texture, i2, i3);
    }

    /* JADX INFO: renamed from: a */
    public void setPatchSize(Texture texture, int i2, int i3) {
        this.patchWidth = i2;
        this.patchHeight = i3;
        this.normalizedWidth = i2 / (float) texture.p;
        this.normalizedHeight = i3 / (float) texture.q;
    }

    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public NinePatchStyle clone() {
        NinePatchStyle ninePatchStyle = new NinePatchStyle();
        ninePatchStyle.copyFrom(this);
        return ninePatchStyle;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIStyle
    /* JADX INFO: renamed from: a */
    public void copyFrom(UIStyle uIStyle) {
        NinePatchStyle ninePatchStyle = (NinePatchStyle) uIStyle;
        this.patchWidth = ninePatchStyle.patchWidth;
        this.patchHeight = ninePatchStyle.patchHeight;
        this.normalizedWidth = ninePatchStyle.normalizedWidth;
        this.normalizedHeight = ninePatchStyle.normalizedHeight;
        this.scalePatchToFit = ninePatchStyle.scalePatchToFit;
        super.copyFrom(ninePatchStyle);
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIStyle
    /* JADX INFO: renamed from: a */
    public void setBackgroundTexture(Texture texture) {
        super.setBackgroundTexture(texture);
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIStyle
    /* JADX INFO: renamed from: a */
    public void drawBackground(GraphicsEngine graphicsEngine, Rect rect) {
        drawNinePatch(graphicsEngine, rect);
        if (this.borderPaint != null) {
        }
    }

    /* JADX INFO: renamed from: b */
    public void drawNinePatch(GraphicsEngine graphicsEngine, Rect rect) {
        drawPatch(graphicsEngine, this.backgroundTexture, this.backgroundPaint, rect);
    }

    /* JADX INFO: renamed from: c */
    private boolean supportsPartialPatches() {
        return true;
    }

    /* JADX INFO: renamed from: a */
    private void drawPatch(GraphicsEngine graphicsEngine, Texture texture, Paint paint, Rect rect) {
        int i2 = rect.a;
        int i3 = rect.b;
        int iB = rect.b();
        int iC = rect.c();
        int i4 = this.patchWidth;
        int i5 = this.patchHeight;
        if (!this.scalePatchToFit) {
            if (i4 > iB / 2) {
                i4 = iB / 2;
            }
            if (i5 > iC / 2) {
                i5 = iC / 2;
            }
        } else {
            float f = 1.0f;
            int i6 = iB / 2;
            int i7 = iC / 2;
            if (i4 * 1.0f > i6) {
                f = i6 / (float) i4;
            }
            if (i5 * f > i7) {
                f = i7 / (float) i5;
            }
            i4 = (int) (this.patchWidth * f);
            i5 = (int) (this.patchHeight * f);
        }
        int i8 = iB - (2 * i4);
        int i9 = iC - (2 * i5);
        float f2 = this.normalizedWidth;
        float f3 = this.normalizedHeight;
        if (supportsPartialPatches()) {
            drawPatchRegion(graphicsEngine, texture, paint, i2 + i4, i3 + 0, i8, i5, f2, 0.0f, 1.0f - f2, f3, this.useScaledBlit);
            drawPatchRegion(graphicsEngine, texture, paint, i2 + 0, i3 + i5, i4, i9, 0.0f, f3, f2, 1.0f - f3, this.useScaledBlit);
            drawPatchRegion(graphicsEngine, texture, paint, i2 + i4, (i3 + iC) - i5, i8, i5, f2, 1.0f - f3, 1.0f - f2, 1.0f, this.useScaledBlit);
            drawPatchRegion(graphicsEngine, texture, paint, (i2 + iB) - i4, i3 + i5, i4, i9, 1.0f - f2, f3, 1.0f, 1.0f - f3, this.useScaledBlit);
            drawPatchRegion(graphicsEngine, texture, paint, i2 + 0, i3 + 0, i4, i5, 0.0f, 0.0f, this.normalizedWidth, this.normalizedHeight);
            drawPatchRegion(graphicsEngine, texture, paint, (i2 + iB) - i4, i3 + 0, i4, i5, 1.0f - this.normalizedWidth, 0.0f, 1.0f, this.normalizedHeight);
            drawPatchRegion(graphicsEngine, texture, paint, i2 + 0, (i3 + iC) - i5, i4, i5, 0.0f, 1.0f - this.normalizedHeight, this.normalizedWidth, 1.0f);
            drawPatchRegion(graphicsEngine, texture, paint, (i2 + iB) - i4, (i3 + iC) - i5, i4, i5, 1.0f - this.normalizedWidth, 1.0f - this.normalizedHeight, 1.0f, 1.0f);
        }
        drawPatchRegion(graphicsEngine, texture, paint, i2 + i4, i3 + i5, i8, i9, f2, f3, 1.0f - f2, 1.0f - f3, this.useScaledBlit);
    }

    /* JADX INFO: renamed from: a */
    public void drawPatchRegion(GraphicsEngine graphicsEngine, Texture texture, Paint paint, int i2, int i3, int i4, int i5, float f, float f2, float f3, float f4) {
        drawPatchRegion(graphicsEngine, texture, paint, i2, i3, i4, i5, f, f2, f3, f4, false);
    }

    /* JADX INFO: renamed from: a */
    public void drawPatchRegion(GraphicsEngine graphicsEngine, Texture texture, Paint paint, int i2, int i3, int i4, int i5, float f, float f2, float f3, float f4, boolean z) {
        Rect rect = stretchCheckRect;
        Rect rect2 = halfSizeRect;
        rect.a((int) (f * texture.p), (int) (f2 * texture.q), (int) (f3 * texture.p), (int) (f4 * texture.q));
        rect2.a(i2, i3, i2 + i4, i3 + i5);
        if (!z) {
            graphicsEngine.a(texture, rect, rect2, paint);
        } else {
            GraphicsUtils.a(graphicsEngine, texture, rect, rect2, paint, 0, 0, 0, 0, this.patchScale);
        }
    }
}
