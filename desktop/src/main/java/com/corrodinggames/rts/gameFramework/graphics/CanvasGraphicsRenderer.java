package com.corrodinggames.rts.gameFramework.graphics;

import android.graphics.*;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.graphics.opengl.GraphicsUtils;

import java.util.concurrent.locks.Lock;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.m.j */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/m/j.class */
public class CanvasGraphicsRenderer implements GraphicsInterface {
    public Canvas a;
    boolean b = false;

    public CanvasGraphicsRenderer(Canvas canvas) {
        this.a = canvas;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(Rect rect) {
        this.a.a(rect);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(RectF rectF) {
        this.a.a(rectF);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(Texture texture, float f, float f2, Paint paint) {
        this.a.a(GraphicsUtils.a(texture), f, f2, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(Texture texture, Rect rect, Rect rect2, Paint paint) {
        this.a.a(GraphicsUtils.a(texture), rect, rect2, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(Texture texture, Rect rect, RectF rectF, Paint paint) {
        this.a.a(GraphicsUtils.a(texture), rect, rectF, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(float f, float f2, float f3, Paint paint) {
        this.a.a(f, f2, f3, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(int i, PorterDuff.Mode mode) {
        this.a.a(i, mode);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(int i) {
        this.a.a(i);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(float f, float f2, float f3, float f4, Paint paint) {
        this.a.a(f, f2, f3, f4, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(float[] fArr, int i, int i2, Paint paint) {
        this.a.a(fArr, i, i2, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(Rect rect, Paint paint) {
        this.a.a(rect, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(RectF rectF, Paint paint) {
        this.a.a(rectF, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(String str, float f, float f2, Paint paint) {
        this.a.a(str, f, f2, paint);
    }

    public boolean equals(Object obj) {
        return this.a.equals(obj);
    }

    public int hashCode() {
        return this.a.hashCode();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a() {
        this.a.b();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(float f, float f2, float f3) {
        this.a.a(f, f2, f3);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void b() {
        this.a.a();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(float f, float f2, float f3, float f4) {
        this.a.a(f, f2, f3, f4);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(float f, float f2) {
        this.a.b(f, f2);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(Texture texture) {
        this.a.a(texture.b());
    }

    public String toString() {
        return this.a.toString();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void b(float f, float f2) {
        this.a.a(f, f2);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(boolean z) {
        this.b = z;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public boolean c() {
        return this.b;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(GraphicsOperation graphicsOperation) {
        graphicsOperation.draw(GameEngine.getInstance().renderGraphicsEngine);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(Bitmap bitmap) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(Lock lock) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void b(Lock lock) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public boolean a(ShaderProgram shaderProgram) {
        return false;
    }
}
