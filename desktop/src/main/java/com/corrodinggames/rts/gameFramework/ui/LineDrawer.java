package com.corrodinggames.rts.gameFramework.ui;

import android.graphics.Paint;
import android.graphics.RectF;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsOperation;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.s */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/s.class */
public class LineDrawer extends GraphicsOperation {

    /* JADX INFO: renamed from: a */
    float[] vertices;

    /* JADX INFO: renamed from: c */
    Paint paint;

    /* JADX INFO: renamed from: d */
    int capacity;

    /* JADX INFO: renamed from: e */
    boolean drawAsPoints;

    /* JADX INFO: renamed from: b */
    int vertexIndex = 0;

    /* JADX INFO: renamed from: f */
    private final RectF pointRect = new RectF();

    LineDrawer(int i, Paint paint) {
        this.capacity = i;
        this.vertices = new float[i * 2];
        this.paint = paint;
    }

    /* JADX INFO: renamed from: a */
    public final void addPoint(float f, float f2) {
        this.vertices[this.vertexIndex] = f;
        this.vertices[this.vertexIndex + 1] = f2;
        this.vertexIndex += 2;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsOperation
    /* JADX INFO: renamed from: a */
    public void draw(GraphicsEngine graphicsEngine) {
        if (!this.drawAsPoints) {
            graphicsEngine.a(this.vertices, 0, this.vertexIndex, this.paint);
        } else {
            RectF rectF = this.pointRect;
            float fG = this.paint.g();
            for (int i = 0; i < this.vertexIndex; i++) {
                float f = this.vertices[i];
                float f2 = this.vertices[i + 1];
                rectF.a = f;
                rectF.b = f2;
                rectF.c = f + fG;
                rectF.d = f2 + fG;
                graphicsEngine.a(rectF, this.paint);
            }
        }
        Minimap.returnLineDrawer(this);
    }
}
