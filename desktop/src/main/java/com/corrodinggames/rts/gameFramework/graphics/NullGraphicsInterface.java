package com.corrodinggames.rts.gameFramework.graphics;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.locks.Lock;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.m.z */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/m/z.class */
public class NullGraphicsInterface implements GraphicsEngine {
    Texture a;
    Texture b;

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public GraphicsEngine a(Texture texture) {
        return b(texture);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public GraphicsEngine b(Texture texture) {
        return new NullGraphicsInterface();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public boolean a() {
        return false;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Context context) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void b() {
        this.a = new Texture();
        this.b = new Texture();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public GraphicsInterface d() {
        return null;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(GraphicsInterface graphicsInterface) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(AudioRenderer audioRenderer) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public Texture a(int i) {
        return a(i, true);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public Texture a(int i, boolean z) {
        return new Texture();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public Texture a(InputStream inputStream, boolean z) {
        return new Texture();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public Texture a(int i, int i2, boolean z) {
        return b(i, i2, z);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public Texture b(int i, int i2, boolean z) {
        return new Texture();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void e() {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, float f, float f2, float f3, Paint paint) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, Rect rect, float f, float f2, float f3, Paint paint) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, Rect rect, Rect rect2, Paint paint) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void b(Texture texture, Rect rect, Rect rect2, Paint paint) {
        a(texture, rect, rect2, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Rect rect, Paint paint) {
        b(rect, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(boolean z) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void f() {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, Rect rect, RectF rectF, Paint paint) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, float f, float f2, Paint paint) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, float f, float f2, Paint paint, float f3, float f4) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void b(Texture texture, float f, float f2, Paint paint) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, Rect rect, Paint paint) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, Rect rect, Paint paint, int i, int i2, int i3, int i4) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, RectF rectF, Paint paint, float f, float f2, int i, int i2) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void b(int i) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(int i, PorterDuff.Mode mode) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(String str, float f, float f2, Paint paint, Paint paint2, float f3) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(String str, float f, float f2, Paint paint) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void b(Rect rect, Paint paint) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(RectF rectF, Paint paint) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void g() {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void h() {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void c(Rect rect, Paint paint) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Rect rect) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(RectF rectF) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(float f, float f2, float f3, Paint paint) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void b(float f, float f2, float f3, Paint paint) {
        a(f, f2, f3, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(float[] fArr, int i, int i2, Paint paint) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void i() {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void j() {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void k() {
        i();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void l() {
        j();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(float f, float f2, float f3) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(float f, float f2) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(float f, float f2, float f3, float f4) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void b(float f, float f2) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(GraphicsOperation graphicsOperation) {
        graphicsOperation.draw(this);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(float f, float f2, float f3, float f4, Paint paint) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public int m() {
        return 0;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public int n() {
        return 0;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(int i, int i2) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void o() {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Paint paint) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(ShaderProgram shaderProgram) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void p() {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void q() {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public int a(String str, Paint paint) {
        return 1;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public int b(String str, Paint paint) {
        return 1;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public Texture r() {
        return this.b;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, File file) {
        throw new RuntimeException("writeImageToFile not yet supported");
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Lock lock) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void b(Lock lock) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public float s() {
        return 1.0f;
    }
}
