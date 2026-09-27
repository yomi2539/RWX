package com.corrodinggames.rts.gameFramework.effects;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.graphics.Texture;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.d.g */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/d/g.class */
public final class SpriteSheet {
    /* JADX INFO: renamed from: l */
    static final Rect tempRect = new Rect();
    /* JADX INFO: renamed from: m */
    static final RectF tempRectF = new RectF();
    /* JADX INFO: renamed from: a */
    public String name;
    /* JADX INFO: renamed from: b */
    public int frameWidth = 25;
    /* JADX INFO: renamed from: c */
    public int frameHeight = 25;
    /* JADX INFO: renamed from: d */
    public int offsetX = 1;
    /* JADX INFO: renamed from: e */
    public int offsetY = 1;
    /* JADX INFO: renamed from: f */
    public int stepX = 26;
    /* JADX INFO: renamed from: g */
    public int stepY = 26;
    /* JADX INFO: renamed from: h */
    public int framesPerRow = Integer.MAX_VALUE;
    /* JADX INFO: renamed from: i */
    public Texture texture = null;
    /* JADX INFO: renamed from: j */
    public Texture shadowTexture = null;
    /* JADX INFO: renamed from: k */
    public boolean singleFrame = false;

    /* JADX INFO: renamed from: a */
    public void createOutline() {
        this.shadowTexture = this.texture.clone();
        this.shadowTexture.j();
        for (int i = 0; i < this.shadowTexture.m(); i++) {
            for (int i2 = 0; i2 < this.shadowTexture.l(); i2++) {
                this.shadowTexture.a(i, i2, Color.a(Color.a(this.shadowTexture.a(i, i2)), 0, 0, 0));
            }
        }
        this.shadowTexture.p();
        this.shadowTexture.s();
    }

    /* JADX INFO: renamed from: a */
    public void drawSprite(int i, float f, float f2, Paint paint) {
        Rect rect = tempRect;
        RectF rectF = tempRectF;
        GameEngine gameEngine = GameEngine.getInstance();
        int i2 = i;
        int i3 = 0;
        if (i2 >= this.framesPerRow) {
            i3 = 0 + (i2 / this.framesPerRow);
            i2 %= this.framesPerRow;
        }
        int i4 = this.offsetX + (i2 * this.stepX);
        int i5 = this.offsetY + (i3 * this.stepY);
        tempRect.a(i4, i5, i4 + this.frameWidth, i5 + this.frameHeight);
        rectF.a(f, f2, f + rect.b(), f2 + rect.c());
        if (1 != 0) {
            rectF.a((-rectF.b()) / 2.0f, (-rectF.c()) / 2.0f);
        }
        gameEngine.renderGraphicsEngine.a(this.texture, rect, rectF, paint);
    }
}
