package com.corrodinggames.rts.gameFramework.graphics;

import java.util.concurrent.locks.Lock;

import android.graphics.*;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.gl.MatrixCalculator;
import com.corrodinggames.rts.gameFramework.graphics.opengl.GraphicsUtils;
import com.corrodinggames.rts.gameFramework.gl.TextureAtlas;
import com.corrodinggames.rts.gameFramework.gl.ShaderInterface;
import com.corrodinggames.rts.gameFramework.gl.PaintStyle;
import com.corrodinggames.rts.gameFramework.gl.OpenGLRenderer;
import com.corrodinggames.rts.gameFramework.gl.TextureManager;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.m.k */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/m/k.class */
public class OpenGLGraphicsRenderer implements GraphicsInterface {
    public TextureManager a;
    public OpenGLRenderer b;
    PaintStyle c;
    Rect e;
    RectF f;
    ShaderInterface g;
    RectF h;
    float[] i;
    static Texture j;
    Texture n;
    boolean o;
    public static boolean d = false;
    static Rect k = new Rect(0, 0, 1, 1);
    static Paint l = new Paint();
    static Rect m = new Rect();

    public void b(Texture texture) {
        this.a.a(this.a.a(texture.b(), texture, this.g));
    }

    public void d() {
        this.a.a();
    }

    public PaintStyle a(Paint paint) {
        this.b.a((ShaderProgram) null);
        if (paint == null) {
            return null;
        }
        this.c.a(paint.d());
        this.c.a(paint.e());
        this.c.a(paint.g());
        return this.c;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(Rect rect) {
        this.a.a(rect.a, rect.b, rect.c, rect.d);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(RectF rectF) {
        this.a.a((int) rectF.a, (int) rectF.b, (int) rectF.c, (int) rectF.d);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(Texture texture, float f, float f2, Paint paint) {
        this.e.a(0, 0, texture.m(), texture.l());
        this.f.a(f, f2, f + texture.m(), f2 + texture.l());
        b(texture, this.e, this.f, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(Texture texture, Rect rect, Rect rect2, Paint paint) {
        this.f.a(rect2);
        b(texture, rect, this.f, paint);
    }

    public com.corrodinggames.rts.gameFramework.gl.Texture a(final Bitmap bitmap, final Texture e) {
        final OpenGLRenderer b = this.b;
        if (b.a == null) {
            b.a = new TextureAtlas(b, 1024, 1024);
        }
        if (bitmap.b() < 450 && bitmap.c() < 100) {
            final com.corrodinggames.rts.gameFramework.gl.Texture a = b.a.a(bitmap);
            if (a != null) {
                return a;
            }
        }
        return this.a.a(bitmap, e, this.g);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(Texture texture, Rect rect, RectF rectF, Paint paint) {
        b(texture, rect, rectF, paint);
    }

    public void b(Texture texture, Rect rect, RectF rectF, Paint paint) {
        Bitmap bitmapA = GraphicsUtils.a(texture);
        this.h.a(rect);
        if (bitmapA == null) {
            throw new RuntimeException("bitmap==null. sourceImage: " + texture.a());
        }
        com.corrodinggames.rts.gameFramework.gl.Texture textureA = a(bitmapA, texture);
        OpenGLRenderer openGLRenderer = this.b;
        boolean zC = true;
        if (paint == null) {
            openGLRenderer.w = -1;
        } else {
            int iE = paint.e();
            if (iE != -1 && paint.h() == null) {
                iE = Color.a(Color.a(iE), 255, 255, 255);
            }
            openGLRenderer.w = iE;
            if (paint instanceof GamePaint) {
                zC = ((GamePaint) paint).p();
            } else {
                zC = paint.c();
            }
        }
        openGLRenderer.a(textureA, zC ? 9729 : 9728);
        ShaderProgram shaderProgramB = null;
        if (paint instanceof GamePaint) {
            shaderProgramB = ((GamePaint) paint).q();
        }
        if (texture != null && shaderProgramB == null) {
            shaderProgramB = texture.B();
        }
        if (shaderProgramB != null) {
            boolean zA = shaderProgramB.a(paint, texture);
            this.b.a(shaderProgramB);
            if (zA) {
                this.b.e();
                this.b.o();
            }
        } else {
            this.b.a((ShaderProgram) null);
        }
        openGLRenderer.a(textureA, this.h, rectF, this.g, (MatrixCalculator) null);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(Bitmap bitmap) {
        this.a.a(bitmap);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(float f, float f2, float f3, Paint paint) {
        this.a.a(f, f2, f3, a(paint));
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(int i, PorterDuff.Mode mode) {
        this.b.a(b(i));
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(int i) {
        this.b.a(b(i));
    }

    float[] b(int i) {
        float f = ((i >>> 24) & 255) * 0.003921569f * 1.0f;
        float f2 = ((i >>> 16) & 255) * 0.003921569f * f;
        this.i[0] = f;
        this.i[1] = f2;
        this.i[2] = ((i >>> 8) & 255) * 0.003921569f * f;
        this.i[3] = (i & 255) * 0.003921569f * f;
        return this.i;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(float f, float f2, float f3, float f4, Paint paint) {
        this.a.a(f, f2, f3, f4, a(paint));
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(float[] fArr, int i, int i2, Paint paint) {
        this.a.a(fArr, i, i2, a(paint));
    }

    public void b(float f, float f2, float f3, float f4, Paint paint) {
        if (j == null) {
            Bitmap bitmapA = Bitmap.a(1, 1, Bitmap.Config.ARGB_8888);
            bitmapA.a(0, 0, -1);
            Texture texture = new Texture();
            texture.a(bitmapA);
            j = texture;
            l.a(false);
            l.a(new LightingColorFilter(-1, -16777216));
        }
        l.b(paint.e());
        if (paint.d() == Paint.Style.STROKE) {
            float fG = paint.g();
            if (fG == 0.0f) {
                fG = 1.0f;
            }
            this.f.a(f, f2, f3, f2 + fG);
            b(j, k, this.f, l);
            this.f.a(f, f4, f3, f4 + fG);
            b(j, k, this.f, l);
            this.f.a(f, f2, f + fG, f4);
            b(j, k, this.f, l);
            this.f.a(f3, f2, f3 + fG, f4);
            b(j, k, this.f, l);
            return;
        }
        this.f.a(f, f2, f3, f4);
        b(j, k, this.f, l);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(Rect rect, Paint paint) {
        b(rect.a, rect.b, rect.c, rect.d, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(RectF rectF, Paint paint) {
        b(rectF.a, rectF.b, rectF.c, rectF.d, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(String str, float f, float f2, Paint paint) {
        this.b.b((ShaderProgram) null);
        OpenGLRenderer.E = this;
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
        this.b.c();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(float f, float f2, float f3) {
        OpenGLRenderer openGLRenderer = this.b;
        openGLRenderer.a(f2, f3);
        openGLRenderer.a(f);
        openGLRenderer.a(-f2, -f3);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void b() {
        this.b.b();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(float f, float f2, float f3, float f4) {
        OpenGLRenderer openGLRenderer = this.b;
        openGLRenderer.a(f3, f4);
        openGLRenderer.a(f, f2, 1.0f);
        openGLRenderer.a(-f3, -f4);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(float f, float f2) {
        this.b.a(f, f2, 1.0f);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(Texture texture) {
        if (this.n != null) {
            d();
        }
        if (texture != null) {
            b(texture);
        }
        this.n = texture;
    }

    public String toString() {
        return this.a.toString();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void b(float f, float f2) {
        this.b.a(f, f2);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(boolean z) {
        this.o = z;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public boolean c() {
        return this.o;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(GraphicsOperation graphicsOperation) {
        graphicsOperation.draw(GameEngine.getInstance().renderGraphicsEngine);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void a(Lock lock) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public void b(Lock lock) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface
    public boolean a(ShaderProgram shaderProgram) {
        return this.b.d(shaderProgram);
    }
}
