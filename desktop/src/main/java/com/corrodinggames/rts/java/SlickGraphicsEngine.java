package com.corrodinggames.rts.java;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.gameFramework.AssetType;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.graphics.AudioRenderer;
import com.corrodinggames.rts.gameFramework.graphics.BlendMode;
import com.corrodinggames.rts.gameFramework.graphics.GamePaint;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsInterface;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsOperation;
import com.corrodinggames.rts.gameFramework.graphics.ShaderProgram;
import com.corrodinggames.rts.gameFramework.graphics.ShaderUniform;
import com.corrodinggames.rts.gameFramework.graphics.TeamColorFilter;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.graphics.opengl.AtlasRegion;
import com.corrodinggames.rts.gameFramework.graphics.opengl.GraphicsUtils;
import com.corrodinggames.rts.gameFramework.graphics.opengl.TextureAtlas;
import com.corrodinggames.rts.gameFramework.utility.AssetInputStream;
import com.corrodinggames.rts.gameFramework.utility.FastArrayList;
import com.corrodinggames.rts.java.image.SlickImageDataWrapper;
import java.awt.FontFormatException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.GlyphPage;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.imageout.ImageOut;
import org.newdawn.slick.opengl.ImageData;
import org.newdawn.slick.opengl.ImageIOImageData;
import org.newdawn.slick.opengl.PNGImageData;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.renderer.LineStripRenderer;
import org.newdawn.slick.opengl.renderer.Renderer;
import org.newdawn.slick.opengl.renderer.SGL;
import org.newdawn.slick.util.FastTrig;
import org.newdawn.slick.util.ResourceLoader;

/* JADX INFO: renamed from: com.corrodinggames.rts.java.e */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/java/e.class */
public final class SlickGraphicsEngine implements GraphicsEngine {
    public boolean b;
    public Graphics slickGraphics;
    public Texture renderTarget;
    public int h;
    public int i;
    public TextureAtlas textureAtlas;
    public static SlickTextureWrapper outOfMemoryImage;
    public static SlickTextureWrapper generalErrorImage;
    public static SlickTextureWrapper largeThumbnailImage;
    boolean y;
    static RectF J;
    static RectF K;
    static RectF M;
    float circleAngleStep;
    float cachedCosAngle;
    float cachedSinAngle;
    private static LineStripRenderer lineRenderer;
    public static final Color c = new Color(0, 0, 0, 255);
    public static final Color d = new Color(0, 0, 0, 255);
    public static final Color e = new Color(0, 0, 0, 255);
    public static Graphics lastGraphics = null;
    static SlickGraphicsEngine activeInstance = null;
    public static ShaderProgram currentShader = null;
    private static SGL W = Renderer.get();
    public static final Color A = new Color(0, 0, 0, 255);
    static float lastLineWidth = -1.0f;
    static ArrayList<SlickTexture> texturesToReload = new ArrayList<>();
    static Paint I = new GamePaint();
    static Paint H = new Paint();
    public boolean shaderSupport = true;
    final Rect n = new Rect();
    final Rect o = new Rect();
    final RectF p = new RectF();
    final PointF q = new PointF();
    ArrayList<FontKey> u = new ArrayList<>();
    int lastDrawMode = -1;
    Paint lastPaint = null;
    SlickTexture lastTexture = null;
    final Paint z = new Paint();
    FontKey C = new FontKey(this);
    byte[] D = new byte[4];
    int F = 0;
    RectF G = new RectF();
    public float uiScale = 1.0f;
    FloatBuffer N = BufferUtils.createFloatBuffer(3);
    float[] O = new float[0];
    int P = -1;
    GraphicsTransform currentTransform = new GraphicsTransform();
    FastArrayList<GraphicsTransform> transformStack = new FastArrayList<>();
    FastArrayList<GraphicsTransform> transformPool = new FastArrayList<>();

    static {
        H.a(255, 255, 0, 0);
        H.a(Paint.Style.STROKE);
        J = new RectF();
        K = new RectF();
        lineRenderer = Renderer.getLineStripRenderer();
    }

    public static void c() {
        W = Renderer.get();
    }

    public Color t() {
        return c;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    /* JADX INFO: renamed from: c, reason: merged with bridge method [inline-methods] */
    public SlickGraphicsEngine a(Texture texture) {
        SlickGraphicsEngine slickGraphicsEngineB = b(texture);
        slickGraphicsEngineB.textureAtlas = this.textureAtlas;
        return slickGraphicsEngineB;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    /* JADX INFO: renamed from: d, reason: merged with bridge method [inline-methods] */
    public SlickGraphicsEngine b(Texture texture) {
        SlickGraphicsEngine slickGraphicsEngine = new SlickGraphicsEngine();
        try {
            slickGraphicsEngine.slickGraphics = e(texture).C().getGraphics();
            slickGraphicsEngine.renderTarget = texture;
            if (texture != null) {
                slickGraphicsEngine.h = texture.m();
                slickGraphicsEngine.i = texture.l();
            }
            return slickGraphicsEngine;
        } catch (SlickException e2) {
            throw new RuntimeException((Throwable) e2);
        }
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public int m() {
        if (this.renderTarget != null) {
            return this.h;
        }
        return (int) GameEngine.getInstance().screenWidth;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public int n() {
        if (this.renderTarget != null) {
            return this.i;
        }
        return (int) GameEngine.getInstance().screenHeight;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(int i, int i2) {
        this.h = i;
        this.i = i2;
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
        outOfMemoryImage = new SlickTextureWrapper((SlickTexture) a(com.corrodinggames.rts.R.drawable.error_outmem));
        outOfMemoryImage.a("Out of memory");
        generalErrorImage = new SlickTextureWrapper((SlickTexture) a(com.corrodinggames.rts.R.drawable.error_general));
        generalErrorImage.a("General Error");
        largeThumbnailImage = new SlickTextureWrapper((SlickTexture) a(com.corrodinggames.rts.R.drawable.error_toolargethumb));
        generalErrorImage.a("Too Large Thumbnail Error");
        if (!GameEngine.isTextureAtlasDisabled) {
            this.textureAtlas = new TextureAtlas(1024, 1024);
        }
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

    public static boolean a(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.codePointAt(i) > 255) {
                return true;
            }
        }
        return false;
    }

    public static ImageData a(InputStream inputStream) throws IOException {
        ImageData slickImageDataWrapper;
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        try {
            try {
                bufferedInputStream.mark(Integer.MAX_VALUE);
                PNGImageData pNGImageData = new PNGImageData();
                pNGImageData.loadImage(bufferedInputStream);
                slickImageDataWrapper = pNGImageData;
            } catch (IOException e2) {
                bufferedInputStream.reset();
                GameEngine.log("PNG load failed: " + e2.getMessage());
                GameEngine.log("Attempting load with ImageIO..");
                ImageIOImageData imageIOImageData = new ImageIOImageData();
                slickImageDataWrapper = new SlickImageDataWrapper(imageIOImageData, imageIOImageData.loadImage(bufferedInputStream, false, (int[]) null));
            }
            return slickImageDataWrapper;
        } finally {
            bufferedInputStream.close();
        }
    }

    Font a(FontKey fontKey, String str, boolean z) {
        FontKey fontKeyA = a(fontKey, z);
        if (fontKeyA.a(str)) {
            return fontKeyA.d;
        }
        UnicodeFont unicodeFont = (UnicodeFont) fontKeyA.d;
        int size = 0;
        for (Object object : unicodeFont.getGlyphPages()) {
            size += ((GlyphPage) object).getGlyphs().size();
        }
        for (int i = 0; i < str.length(); i++) {
            str.charAt(i);
            if (0 == 0) {
            }
        }
        unicodeFont.getGlyphPages().size();
        unicodeFont.addGlyphs(str);
        try {
            unicodeFont.loadGlyphs();
            int size2 = 0;
            for (Object object : unicodeFont.getGlyphPages()) {
                size2 += ((GlyphPage) object).getGlyphs().size();
            }
            unicodeFont.getGlyphPages().size();
            if (size != size2) {
                GameEngine.log("new glypth, " + size2 + " " + fontKeyA.toString() + " for text:" + str);
            }
            fontKeyA.b(str);
            return fontKeyA.d;
        } catch (SlickException e2) {
            throw new RuntimeException((Throwable) e2);
        }
    }

    public void a(Paint paint, String str) {
        a(paint, true, str, (SlickTexture) null, (Texture) null);
    }

    public void b(Paint paint) {
        a(paint, false, (String) null, (SlickTexture) null, (Texture) null);
    }

    public void a(Paint paint, SlickTexture slickTexture, Texture texture) {
        a(paint, false, (String) null, slickTexture, texture);
    }

    public void u() {
        y();
        Graphics.setCurrent(this.slickGraphics);
        b(true);
        this.b = true;
        lastLineWidth = -1.0f;
        Color.setRebindRequired();
        this.lastPaint = this.z;
        activeInstance = this;
    }

    public void a(Paint paint, boolean z, String str, SlickTexture slickTexture, Texture texture) {
        boolean zC;
        boolean z2 = false;
        if (lastGraphics != this.slickGraphics) {
            u();
            z2 = true;
            lastGraphics = this.slickGraphics;
        }
        if ((paint == null || (paint instanceof GamePaint)) && this.lastPaint == paint && this.lastTexture == slickTexture && !z) {
            ShaderProgram shaderProgramB = null;
            if (this.shaderSupport) {
                if (paint != null && (paint instanceof GamePaint)) {
                    shaderProgramB = ((GamePaint) paint).q();
                }
                if (texture != null && shaderProgramB == null) {
                    shaderProgramB = texture.B();
                }
            }
            if (currentShader == shaderProgramB) {
                if (currentShader != null && currentShader.a(paint, texture)) {
                    this.slickGraphics.flushBuffer();
                    b(currentShader);
                    return;
                }
                return;
            }
        }
        this.lastPaint = paint;
        this.lastTexture = slickTexture;
        boolean z3 = slickTexture == null && !z;
        if (this.lastDrawMode != Graphics.MODE_NORMAL) {
            this.lastDrawMode = Graphics.MODE_NORMAL;
            this.slickGraphics.setDrawMode(this.lastDrawMode);
        }
        if (z2 && this.renderTarget != null) {
            W.glEnable(3042);
            W.glColorMask(true, true, true, true);
            GL14.glBlendFuncSeparate(770, 771, 770, 1);
        }
        if (paint == null) {
            zC = false;
            a(Color.white);
            if (z3) {
                a(1.0f);
            }
            if (z) {
                this.slickGraphics.resetFont();
            }
        } else {
            zC = paint.c();
        }
        if (this.shaderSupport) {
            ShaderProgram shaderProgramB2 = null;
            if (paint != null && (paint instanceof GamePaint)) {
                shaderProgramB2 = ((GamePaint) paint).q();
            }
            if (texture != null && shaderProgramB2 == null) {
                shaderProgramB2 = texture.B();
            }
            if (currentShader != shaderProgramB2) {
                this.slickGraphics.flushBuffer();
                if (shaderProgramB2 == null) {
                    v();
                } else {
                    shaderProgramB2.f();
                    if (!c(shaderProgramB2)) {
                        if (currentShader != null) {
                            v();
                        }
                    } else {
                        shaderProgramB2.a(paint, texture);
                        b(shaderProgramB2);
                    }
                }
                currentShader = shaderProgramB2;
            } else if (currentShader != null && currentShader.a(paint, texture)) {
                this.slickGraphics.flushBuffer();
                b(currentShader);
            }
        }
        if (slickTexture != null) {
            if ((slickTexture.E == 1) != zC) {
                this.slickGraphics.flushBuffer();
                int i = zC ? 1 : 2;
                slickTexture.C().setFilter(i);
                slickTexture.E = i;
            }
        }
        if (paint != null) {
            boolean z4 = true;
            ColorFilter colorFilterH = paint.h();
            if (colorFilterH != null) {
                if (colorFilterH instanceof LightingColorFilter) {
                    LightingColorFilter lightingColorFilter = (LightingColorFilter) colorFilterH;
                    if (lightingColorFilter.a != 0 && lightingColorFilter.a != -1) {
                        int i2 = lightingColorFilter.a;
                        d.r = android.graphics.Color.b(i2) * 0.003921569f;
                        d.g = android.graphics.Color.c(i2) * 0.003921569f;
                        d.b = android.graphics.Color.d(i2) * 0.003921569f;
                        d.a = android.graphics.Color.a(i2) * 0.003921569f;
                        a(paint.e(), e);
                        d.r *= e.r;
                        d.g *= e.g;
                        d.b *= e.b;
                        d.a *= e.a;
                        a(d);
                        this.lastDrawMode = Graphics.MODE_ADD;
                        this.slickGraphics.setDrawMode(this.lastDrawMode);
                        W.glEnable(3042);
                        W.glColorMask(true, true, true, true);
                        W.glBlendFunc(770, 1);
                        z4 = false;
                    }
                } else if (colorFilterH instanceof TeamColorFilter) {
                    TeamColorFilter teamColorFilter = (TeamColorFilter) colorFilterH;
                    if (teamColorFilter.a == BlendMode.copy) {
                        f(paint.e());
                        this.lastDrawMode = 99;
                        W.glEnable(3042);
                        W.glColorMask(true, true, true, true);
                        W.glBlendFunc(1, 1);
                        z4 = false;
                    } else if (teamColorFilter.a == BlendMode.additive) {
                        f(paint.e());
                        this.lastDrawMode = 99;
                        W.glEnable(3042);
                        W.glColorMask(true, true, true, true);
                        W.glBlendFunc(774, 771);
                        z4 = false;
                    }
                }
            }
            if (z4) {
                f(paint.e());
            }
            if (z3) {
                if (paint.g() != 0.0f) {
                    a(paint.g());
                } else {
                    a(1.0f);
                }
            }
            if (z) {
                this.slickGraphics.setFont(a(paint, str, true));
            }
        }
    }

    public void v() {
        GL20.glUseProgram(0);
    }

    public void b(ShaderProgram shaderProgram) {
        for (ShaderUniform shaderUniform : shaderProgram.uniforms) {
            if (shaderUniform.isDirty) {
                shaderUniform.isDirty = false;
                if (shaderUniform.b == -1) {
                    shaderUniform.b = GL20.glGetUniformLocation(shaderProgram.n, shaderUniform.name);
                    if (shaderUniform.b == -1 && !shaderUniform.d) {
                        shaderUniform.d = true;
                        shaderProgram.b("Unknown parameter: " + shaderUniform.name);
                        int iGlGetProgrami = GL20.glGetProgrami(shaderProgram.n, 35718);
                        int iGlGetProgrami2 = GL20.glGetProgrami(shaderProgram.n, 35719);
                        for (int i = 0; i < iGlGetProgrami; i++) {
                            shaderProgram.b("Possible parameter: " + GL20.glGetActiveUniform(shaderProgram.n, i, iGlGetProgrami2));
                        }
                        return;
                    }
                }
                if (shaderUniform.texture != null) {
                    org.newdawn.slick.opengl.Texture texture = e(shaderUniform.texture).C().getTexture();
                    if (shaderUniform.g) {
                        GL20.glUniform2f(shaderUniform.b, texture.getTextureWidth(), texture.getTextureHeight());
                    } else {
                        int textureID = texture.getTextureID();
                        shaderProgram.b("Updating texture to:" + textureID);
                        GL20.glUniform1i(shaderUniform.b, 1);
                        GL13.glActiveTexture(33985);
                        GL11.glBindTexture(3553, textureID);
                        GL13.glActiveTexture(33984);
                    }
                } else if (shaderUniform.floatValues.length == 1) {
                    GL20.glUniform1f(shaderUniform.b, shaderUniform.floatValues[0]);
                } else if (shaderUniform.floatValues.length == 2) {
                    GL20.glUniform2f(shaderUniform.b, shaderUniform.floatValues[0], shaderUniform.floatValues[1]);
                } else if (shaderUniform.floatValues.length == 4) {
                    GL20.glUniform4f(shaderUniform.b, shaderUniform.floatValues[0], shaderUniform.floatValues[1], shaderUniform.floatValues[2], shaderUniform.floatValues[3]);
                } else {
                    shaderProgram.b("Unhandled parameter size: " + shaderUniform.name + " - " + shaderUniform.floatValues.length);
                }
            }
        }
    }

    public boolean c(ShaderProgram shaderProgram) {
        if (shaderProgram.programStatus != 0) {
            return false;
        }
        if (shaderProgram.n != 0 && !shaderProgram.m) {
            GL20.glUseProgram(shaderProgram.n);
            return true;
        }
        shaderProgram.m = false;
        shaderProgram.b("Compiling shader");
        shaderProgram.vertexShaderId = a(shaderProgram, 35633, shaderProgram.vertexSource);
        shaderProgram.fragmentShaderId = a(shaderProgram, 35632, shaderProgram.fragmentSource);
        if (shaderProgram.programStatus != 0) {
            return false;
        }
        shaderProgram.n = GL20.glCreateProgram();
        if (shaderProgram.n == 0) {
            shaderProgram.c("could not create program; check ShaderProgram.isSupported()");
            return false;
        }
        GL20.glAttachShader(shaderProgram.n, shaderProgram.vertexShaderId);
        GL20.glAttachShader(shaderProgram.n, shaderProgram.fragmentShaderId);
        GL20.glLinkProgram(shaderProgram.n);
        int iGlGetProgrami = GL20.glGetProgrami(shaderProgram.n, 35714);
        String strGlGetProgramInfoLog = GL20.glGetProgramInfoLog(shaderProgram.n, GL20.glGetProgrami(shaderProgram.n, 35716));
        if (strGlGetProgramInfoLog != null && strGlGetProgramInfoLog.length() != 0) {
            shaderProgram.d = strGlGetProgramInfoLog + "\n" + shaderProgram.d;
        }
        if (shaderProgram.d != null) {
            shaderProgram.d = shaderProgram.d.trim();
        }
        if (iGlGetProgrami == 0) {
            shaderProgram.c(shaderProgram.d.length() != 0 ? shaderProgram.d : "Could not link program");
            return false;
        }
        GL20.glUseProgram(shaderProgram.n);
        return true;
    }

    protected int a(ShaderProgram shaderProgram, int i, String str) {
        int iGlCreateShader = GL20.glCreateShader(i);
        if (iGlCreateShader == 0) {
            shaderProgram.c("could not create shader object; check ShaderProgram.isSupported()");
        }
        GL20.glShaderSource(iGlCreateShader, str);
        GL20.glCompileShader(iGlCreateShader);
        int iGlGetShaderi = GL20.glGetShaderi(iGlCreateShader, 35713);
        int iGlGetShaderi2 = GL20.glGetShaderi(iGlCreateShader, 35716);
        String strE = e(i);
        String strGlGetShaderInfoLog = GL20.glGetShaderInfoLog(iGlCreateShader, iGlGetShaderi2);
        if (strGlGetShaderInfoLog != null && strGlGetShaderInfoLog.length() != 0) {
            shaderProgram.d += strE + " compile log:\n" + strGlGetShaderInfoLog + "\n";
        }
        if (iGlGetShaderi == 0) {
            shaderProgram.c(shaderProgram.d.length() != 0 ? shaderProgram.d : "Could not compile " + e(i));
        }
        return iGlCreateShader;
    }

    private String e(int i) {
        if (i == 35632) {
            return "FRAGMENT_SHADER";
        }
        if (i == 35633) {
            return "VERTEX_SHADER";
        }
        return "shader";
    }

    private void f(int i) {
        a(i, A);
        a(A);
    }

    private void a(Color color) {
        Color color2 = c;
        if (this.b) {
            this.b = false;
        } else if (color2.r == color.r && color2.g == color.g && color2.b == color.b && color2.a == color.a) {
            return;
        }
        color2.a = color.a;
        color2.r = color.r;
        color2.g = color.g;
        color2.b = color.b;
        this.slickGraphics.setColor(color2);
    }

    public void a(float f) {
        if (lastLineWidth != f) {
            lastLineWidth = f;
            this.slickGraphics.setLineWidth(f);
        }
    }

    public Font a(Paint paint, String str, boolean z) {
        FontKey fontKey = this.C;
        fontKey.a = (int) paint.k();
        if (x()) {
            fontKey.a = (int) (fontKey.a * this.uiScale);
        }
        Typeface typefaceI = paint.i();
        fontKey.b = false;
        if (typefaceI != null) {
            fontKey.b = typefaceI.a();
        }
        fontKey.c = false;
        if (a(str)) {
            fontKey.c = true;
        }
        return a(fontKey, str, z);
    }

    public static void a(ImageData imageData, ByteBuffer byteBuffer, int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        int texWidth = (i + (i2 * imageData.getTexWidth())) * i7;
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            byteBuffer.put(texWidth, (byte) i5);
            byteBuffer.put(texWidth + 1, (byte) i4);
            byteBuffer.put(texWidth + 2, (byte) i3);
            byteBuffer.put(texWidth + 3, (byte) i6);
            return;
        }
        byteBuffer.put(texWidth, (byte) i3);
        byteBuffer.put(texWidth + 1, (byte) i4);
        byteBuffer.put(texWidth + 2, (byte) i5);
        byteBuffer.put(texWidth + 3, (byte) i6);
    }

    public static int a(ImageData imageData, ByteBuffer byteBuffer, int i, int i2, int i3) {
        int i4;
        int i5;
        int i6;
        int i7;
        int texWidth = (i + (i2 * imageData.getTexWidth())) * i3;
        if (i3 == 4) {
        }
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            i6 = byteBuffer.get(texWidth) & 255;
            i5 = byteBuffer.get(texWidth + 1) & 255;
            i4 = byteBuffer.get(texWidth + 2) & 255;
        } else {
            i4 = byteBuffer.get(texWidth) & 255;
            i5 = byteBuffer.get(texWidth + 1) & 255;
            i6 = byteBuffer.get(texWidth + 2) & 255;
        }
        if (i3 < 4) {
            i7 = 255;
        } else {
            i7 = byteBuffer.get(texWidth + 3) & 255;
        }
        return a(i7, i4, i5, i6);
    }

    public static final int a(int i, int i2, int i3, int i4) {
        return (i << 24) | (i2 << 16) | (i3 << 8) | i4;
    }

    public static Color a(int i, Color color) {
        color.r = ((i >> 16) & 255) * 0.003921569f;
        color.g = ((i >> 8) & 255) * 0.003921569f;
        color.b = (i & 255) * 0.003921569f;
        color.a = (i >>> 24) * 0.003921569f;
        return color;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public Texture a(int i) {
        return a(i, true);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void e() {
        w();
    }

    public static void w() {
        if (texturesToReload.size() == 0) {
            return;
        }
        Iterator it = texturesToReload.iterator();
        while (it.hasNext()) {
            ((SlickTexture) it.next()).I();
        }
        texturesToReload.clear();
    }

    public static void a(SlickTexture slickTexture) {
        texturesToReload.add(slickTexture);
        if (texturesToReload.size() > 15) {
            w();
        }
    }

    public static SlickTexture b(int i, boolean z) {
        String number = Utility.getResourcePath(i);
        try {
            FileInputStream fileInputStream = new FileInputStream(number);
            ImageData imageDataA = a(fileInputStream);
            fileInputStream.close();
            return a(imageDataA, number);
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        } catch (OutOfMemoryError e3) {
            GameEngine.reportOOM(AssetType.gameImage, e3);
            if (outOfMemoryImage == null) {
                throw new RuntimeException("outOfMemoryErrorImage==null", e3);
            }
            return outOfMemoryImage;
        }
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public Texture a(int i, boolean z) {
        return b(i, z);
    }

    FontKey a(FontKey fontKey, boolean z) {
        for (FontKey fontKey2 : this.u) {
            if (fontKey2.a == fontKey.a && fontKey2.b == fontKey.b && fontKey2.c == fontKey.c) {
                return fontKey2;
            }
        }
        FontKey fontKeyA = fontKey.clone();
        GameEngine.log("New font:" + fontKeyA.a + " bold:" + fontKeyA.b);
        if (z) {
        }
        String str = "font/Roboto-Regular.ttf";
        if (fontKeyA.b) {
            str = "font/Roboto-Bold.ttf";
        }
        if (fontKeyA.c) {
            str = "font/DroidSansFallback.ttf";
        }
        try {
            UnicodeFont unicodeFont = new UnicodeFont(java.awt.Font.createFont(0, ResourceLoader.getResourceAsStream(str)).deriveFont((float) fontKeyA.a));
            unicodeFont.addAsciiGlyphs();
            unicodeFont.getEffects().add(new ColorEffect(new java.awt.Color(255, 255, 255)));
            try {
                unicodeFont.loadGlyphs();
            } catch (SlickException e2) {
                throw new RuntimeException((Throwable) e2);
            } catch (OutOfMemoryError e3) {
                GameEngine.reportOOM(AssetType.gameImage, e3);
            }
            GameEngine.log("loadGlyphs");
            fontKeyA.d = unicodeFont;
            this.u.add(fontKeyA);
            return fontKeyA;
        } catch (FontFormatException e4) {
            throw new RuntimeException((Throwable) e4);
        } catch (IOException e5) {
            throw new RuntimeException(e5);
        }
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public Texture a(InputStream inputStream, boolean z) {
        try {
            String path = null;
            if (inputStream instanceof AssetInputStream) {
                path = ((AssetInputStream) inputStream).getPath();
            } else {
                GameEngine.logColored("loadImage InputStream is not AssetInputStream");
                GameEngine.printStackTrace();
            }
            this.F++;
            return a(a(inputStream), path);
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        } catch (OutOfMemoryError e3) {
            GameEngine.reportOOM(AssetType.gameImage, e3);
            if (outOfMemoryImage == null) {
                throw new RuntimeException("outOfMemoryErrorImage==null", e3);
            }
            return outOfMemoryImage;
        }
    }

    public static SlickTexture a(ImageData imageData, String str) {
        SlickTexture slickTexture = new SlickTexture();
        slickTexture.a(imageData, str, false);
        a(slickTexture);
        return slickTexture;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public Texture a(int i, int i2, boolean z) {
        SlickTexture slickTexture = new SlickTexture();
        try {
            slickTexture.a(new Image(i, i2), (String) null);
            a(slickTexture);
            return slickTexture;
        } catch (SlickException e2) {
            throw new RuntimeException((Throwable) e2);
        } catch (OutOfMemoryError e3) {
            GameEngine.reportOOM(AssetType.gameImageCreate, e3);
            if (outOfMemoryImage == null) {
                throw new RuntimeException("outOfMemoryErrorImage==null", e3);
            }
            return outOfMemoryImage;
        }
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public Texture b(int i, int i2, boolean z) {
        return a((ImageData) new ImageBuffer(i, i2), (String) null);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, float f, float f2, float f3, Paint paint) {
        k();
        a(f3 + 90.0f, f, f2);
        c(texture, f - texture.r, f2 - texture.s, paint);
        l();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, Rect rect, float f, float f2, float f3, Paint paint) {
        k();
        a(f3, f, f2);
        this.G.a(f - texture.r, f2 - texture.s, texture.p, texture.q);
        a(texture, rect, this.G, paint);
        l();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, Rect rect, Rect rect2, Paint paint) {
        this.G.a(rect2);
        a(texture, rect, this.G, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void b(Texture texture, Rect rect, Rect rect2, Paint paint) {
        this.G.a(rect2);
        a(texture, rect, this.G, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Rect rect, Paint paint) {
        this.G.a(rect);
        a(this.G, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(boolean z) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void f() {
    }

    private final SlickTexture e(Texture texture) {
        return (SlickTexture) texture.c();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, Rect rect, RectF rectF, Paint paint) {
        a(texture, rectF.a, rectF.b, rectF.c, rectF.d, rect.a, rect.b, rect.c, rect.d, paint);
    }

    private void c(Texture texture, float f, float f2, Paint paint) {
        float fM = texture.m();
        float fL = texture.l();
        a(texture, f, f2, f + fM, f2 + fL, 0.0f, 0.0f, fM, fL, paint);
    }

    private void a(Texture texture, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, Paint paint) {
        AtlasRegion atlasRegionA;
        GraphicsTransform graphicsTransform = this.currentTransform;
        float f9 = f3 - f;
        float f10 = f4 - f2;
        if (graphicsTransform.c != -90.0f) {
            float f11 = f9 / 2.0f;
            float f12 = f10 / 2.0f;
            float f13 = (f + f11) - graphicsTransform.g;
            float f14 = (f2 + f12) - graphicsTransform.h;
            if ((f13 != 0.0f || f14 != 0.0f) && (f13 > 0.01f || f14 > 0.01f || f13 < -0.01f || f14 < -0.01f)) {
                PointF pointF = this.q;
                pointF.x = f13;
                pointF.y = f14;
                a(graphicsTransform.c + 180.0f, pointF);
                float f15 = (pointF.x + graphicsTransform.g) - f11;
                float f16 = (pointF.y + graphicsTransform.h) - f12;
                f3 += f15 - f;
                f4 += f16 - f2;
                f = f15;
                f2 = f16;
            }
        }
        SlickTexture slickTextureE = e(texture);
        if (this.textureAtlas != null && slickTextureE.m() < 450 && slickTextureE.l() < 100 && (atlasRegionA = this.textureAtlas.a(slickTextureE)) != null) {
            slickTextureE = e(atlasRegionA.a);
            if (f5 < 0.0f) {
                f += -f5;
                f5 = 0.0f;
            }
            if (f6 < 0.0f) {
                f2 += -f6;
                f6 = 0.0f;
            }
            if (f7 > atlasRegionA.d) {
                f3 += -(atlasRegionA.d - f7);
                f7 = atlasRegionA.d;
            }
            if (f8 > atlasRegionA.e) {
                f4 += -(atlasRegionA.e - f8);
                f8 = atlasRegionA.e;
            }
            f5 += atlasRegionA.b;
            f7 += atlasRegionA.b;
            f6 += atlasRegionA.c;
            f8 += atlasRegionA.c;
        }
        float f17 = f * graphicsTransform.d;
        float f18 = f2 * graphicsTransform.e;
        float f19 = (f3 - f) * graphicsTransform.d;
        float f20 = (f4 - f2) * graphicsTransform.e;
        float f21 = f17 + graphicsTransform.a;
        float f22 = f18 + graphicsTransform.b;
        float f23 = f19 / 2.0f;
        float f24 = f20 / 2.0f;
        a(paint, slickTextureE, texture);
        Image imageC = slickTextureE.C();
        if (imageC == null) {
            slickTextureE.G();
            throw new RuntimeException("getSlickImage==null");
        }
        a(imageC, f21 + f23, f22 + f24, f19, f20, f5, f6, f7, f8, t(), graphicsTransform.c);
    }

    private void a(Image image, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, Color color, float f9) {
        float f10;
        float f11;
        float f12;
        float f13;
        float f14;
        float f15;
        float f16;
        float f17;
        Graphics.setCurrent(this.slickGraphics);
        image.startUse();
        if (color != null) {
            color.bind();
        }
        float f18 = f3 * 0.5f;
        float f19 = f4 * 0.5f;
        float textureWidth = image.getTextureWidth() / image.getWidth();
        float textureHeight = image.getTextureHeight() / image.getHeight();
        float f20 = f5 * textureWidth;
        float f21 = f6 * textureHeight;
        float f22 = (f7 - f5) * textureWidth;
        float f23 = (f8 - f6) * textureHeight;
        float f24 = f9 + 90.0f;
        if (f24 == 0.0f) {
            f10 = (-f18) + f;
            f11 = (-f19) + f2;
            f12 = f18 + f;
            f13 = (-f19) + f2;
            f14 = (-f18) + f;
            f15 = f19 + f2;
            f16 = f18 + f;
            f17 = f19 + f2;
        } else {
            float fFastCos = Utility.fastCos(f24);
            float fFastSin = Utility.fastSin(f24);
            float f25 = f18 * fFastCos;
            float f26 = f19 * fFastCos;
            float f27 = f18 * fFastSin;
            float f28 = f19 * fFastSin;
            f10 = (-f25) + f28 + f;
            f11 = ((-f27) - f26) + f2;
            f12 = f25 + f28 + f;
            f13 = (f27 - f26) + f2;
            f14 = ((-f25) - f28) + f;
            f15 = (-f27) + f26 + f2;
            f16 = (f25 - f28) + f;
            f17 = f27 + f26 + f2;
        }
        W.glTexCoord2f(f20, f21);
        W.glVertex3f(f10, f11, 0.0f);
        W.glTexCoord2f(f20, f21 + f23);
        W.glVertex3f(f14, f15, 0.0f);
        W.glTexCoord2f(f20 + f22, f21 + f23);
        W.glVertex3f(f16, f17, 0.0f);
        W.glTexCoord2f(f20 + f22, f21);
        W.glVertex3f(f12, f13, 0.0f);
        image.endUse();
        this.slickGraphics.getColor().bind();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, float f, float f2, Paint paint) {
        b(texture, f - texture.t, f2 - texture.u, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, float f, float f2, Paint paint, float f3, float f4) {
        k();
        b(f, f2);
        a(f4, f4);
        a(f3, f, f2);
        c(texture, 0.0f, 0.0f, paint);
        l();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void b(Texture texture, float f, float f2, Paint paint) {
        c(texture, f, f2, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, Rect rect, Paint paint) {
        a(texture, rect, paint, 0, 0, 0, 0);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, Rect rect, Paint paint, int i, int i2, int i3, int i4) {
        GraphicsUtils.a(this, texture, rect, paint, i, i2, i3, i4);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, RectF rectF, Paint paint, float f, float f2, int i, int i2) {
        GraphicsUtils.a(this, texture, rectF, paint, f, f2, i, i2);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void b(int i) {
        if (activeInstance != this) {
            u();
        }
        b(false);
        this.lastPaint = null;
        this.slickGraphics.setBackground(a(i, e));
        this.slickGraphics.clear();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void o() {
        if (activeInstance != this) {
            u();
        }
        this.lastPaint = null;
        this.slickGraphics.clearAlphaMap();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(int i, PorterDuff.Mode mode) {
        this.lastPaint = null;
        if (mode != PorterDuff.Mode.CLEAR) {
            b(i);
        } else {
            b(i);
            this.slickGraphics.clearAlphaMap();
        }
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(String str, float f, float f2, Paint paint, Paint paint2, float f3) {
        float fB = b(str, paint);
        J.a(f, f2, f + fB, f2 + a(str, paint));
        Utility.grow(J, f3);
        K.a(J);
        if (paint.j() == Paint.Align.CENTER) {
            J.a(-(fB / 2.0f), 0.0f);
        }
        a(J, paint2);
        a(str, K.a + f3, K.d - f3, paint);
    }

    boolean x() {
        if (!GameEngine.getInstance().settingsEngine.resizeFontWithUIScale || this.uiScale == 1.0f) {
            return false;
        }
        if (this.uiScale < 1.0f) {
            return true;
        }
        return true;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(String str, float f, float f2, Paint paint) {
        if (x()) {
            k();
            float f3 = 1.0f / this.uiScale;
            a(f3, f3);
            f *= this.uiScale;
            f2 *= this.uiScale;
        }
        float f4 = f * this.currentTransform.d;
        float f5 = f2 * this.currentTransform.e;
        float f6 = f4 + this.currentTransform.a;
        float f7 = f5 + this.currentTransform.b;
        a(paint, str);
        int width = 0;
        if (paint.j() == Paint.Align.CENTER) {
            width = 0 - (this.slickGraphics.getFont().getWidth(str) / 2);
        } else if (paint.j() == Paint.Align.RIGHT) {
            width = 0 - this.slickGraphics.getFont().getWidth(str);
        }
        this.slickGraphics.drawString(str, (int) (f6 + width), (int) (f7 + (0 - this.slickGraphics.getFont().getLineHeight())));
        if (x()) {
            l();
        }
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void b(Rect rect, Paint paint) {
        this.G.a(rect);
        a(this.G, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(RectF rectF, Paint paint) {
        b(paint);
        if (paint.d() == Paint.Style.FILL || paint.d() == Paint.Style.FILL_AND_STROKE) {
            TextureImpl.bindNone();
            W.glBegin(7);
            float f = rectF.a;
            float f2 = rectF.b;
            float f3 = rectF.c;
            float f4 = rectF.d;
            float f5 = f * this.currentTransform.d;
            float f6 = f2 * this.currentTransform.e;
            float f7 = f5 + this.currentTransform.a;
            float f8 = f6 + this.currentTransform.b;
            float f9 = f3 * this.currentTransform.d;
            float f10 = f4 * this.currentTransform.e;
            float f11 = f9 + this.currentTransform.a;
            float f12 = f10 + this.currentTransform.b;
            W.glVertex2f(f7, f8);
            W.glVertex2f(f11, f8);
            W.glVertex2f(f11, f12);
            W.glVertex2f(f7, f12);
            W.glEnd();
            return;
        }
        float f13 = rectF.a;
        float f14 = rectF.b;
        float fB = rectF.b();
        float fC = rectF.c();
        float f15 = f13 * this.currentTransform.d;
        float f16 = f14 * this.currentTransform.e;
        this.slickGraphics.drawRect(f15 + this.currentTransform.a, f16 + this.currentTransform.b, fB * this.currentTransform.d, fC * this.currentTransform.e);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void g() {
        e();
        M = null;
        if (this.textureAtlas != null) {
            this.textureAtlas.c();
        }
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void h() {
        y();
        if (this.textureAtlas != null) {
            this.textureAtlas.d();
        }
        if (this.shaderSupport && currentShader != null) {
            v();
            currentShader = null;
        }
        this.lastPaint = null;
        M = null;
        this.b = true;
        lastLineWidth = -1.0f;
        this.y = false;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void c(Rect rect, Paint paint) {
        this.o.a(rect.a, rect.b, rect.a + rect.c, rect.b + rect.d);
        b(this.o, paint);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Rect rect) {
        if (rect != null) {
            this.currentTransform.f = new RectF(rect);
            this.currentTransform.f.a *= this.currentTransform.d;
            this.currentTransform.f.c *= this.currentTransform.d;
            this.currentTransform.f.b *= this.currentTransform.e;
            this.currentTransform.f.d *= this.currentTransform.e;
            this.currentTransform.f.a(this.currentTransform.a, this.currentTransform.b);
        } else {
            this.currentTransform.f = null;
        }
        b(false);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(RectF rectF) {
        if (rectF != null) {
            this.currentTransform.f = new RectF(rectF);
            this.currentTransform.f.a *= this.currentTransform.d;
            this.currentTransform.f.c *= this.currentTransform.d;
            this.currentTransform.f.b *= this.currentTransform.e;
            this.currentTransform.f.d *= this.currentTransform.e;
            this.currentTransform.f.a(this.currentTransform.a, this.currentTransform.b);
        } else {
            this.currentTransform.f = null;
        }
        b(false);
    }

    public void b(boolean z) {
        RectF rectF = this.currentTransform.f;
        if (M == rectF && !z) {
            return;
        }
        y();
        if (rectF != null) {
            W.glEnable(3089);
            W.glScissor((int) rectF.a, (int) ((n() * this.uiScale) - rectF.d), (int) rectF.b(), (int) rectF.c());
        } else {
            W.glDisable(3089);
        }
        M = rectF;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void b(float f, float f2, float f3, Paint paint) {
        float f4 = f * this.currentTransform.d;
        float f5 = f2 * this.currentTransform.e;
        float f6 = f4 + this.currentTransform.a;
        float f7 = f5 + this.currentTransform.b;
        float f8 = f3 * this.currentTransform.d;
        b(paint);
        if (paint.d() == Paint.Style.STROKE) {
            int i = 40;
            if (f8 > 100.0f) {
                i = 60;
            }
            a(f6, f7, f8, i);
            return;
        }
        this.slickGraphics.fillOval(f6 - f8, f7 - f8, f8 * 2.0f, f8 * 2.0f);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(float f, float f2, float f3, Paint paint) {
        float f4 = this.currentTransform.d;
        if (f3 * f4 < 25.0f && paint.d() == Paint.Style.STROKE) {
            GraphicsUtils.a(this, f, f2, f3, paint, f4);
        } else {
            b(f, f2, f3, paint);
        }
    }

    public FloatBuffer c(int i) {
        if (this.N.capacity() < i) {
            this.N = BufferUtils.createFloatBuffer(i);
        }
        return this.N;
    }

    public FloatBuffer a(float[] fArr, int i) {
        FloatBuffer floatBufferC = c(i);
        floatBufferC.clear();
        floatBufferC.put(fArr, 0, i);
        floatBufferC.flip();
        return floatBufferC;
    }

    public float[] d(int i) {
        if (this.O.length < i) {
            this.O = new float[i];
        }
        return this.O;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(float[] fArr, int i, int i2, Paint paint) {
        if (i2 == 0) {
            return;
        }
        boolean z = true;
        if (Main.b) {
            z = false;
        }
        float fG = paint.g();
        float f = 1.0f;
        float f2 = 0.0f;
        if (fG > 1.0f) {
            f = 1.0f + ((fG - 1.0f) * 0.5f);
            f2 = 0.0f + ((fG - 1.0f) * 0.5f);
        }
        float f3 = this.currentTransform.d;
        float f4 = this.currentTransform.e;
        float f5 = this.currentTransform.a;
        float f6 = this.currentTransform.b;
        if (z) {
            float[] fArrD = d(i2 * 4);
            int i3 = i2 * 4;
            int i4 = 0;
            for (int i5 = 0; i5 < i3; i5 += 8) {
                float f7 = fArr[i4];
                float f8 = fArr[i4 + 1];
                float f9 = f7 - f2;
                float f10 = f7 + f;
                float f11 = f8 - f2;
                float f12 = f8 + f;
                fArrD[i5 + 0] = f9;
                fArrD[i5 + 1] = f11;
                fArrD[i5 + 2] = f10;
                fArrD[i5 + 3] = f11;
                fArrD[i5 + 4] = f10;
                fArrD[i5 + 5] = f12;
                fArrD[i5 + 6] = f9;
                fArrD[i5 + 7] = f12;
                i4 += 2;
            }
            b(fArrD, 0, i2 * 4, paint);
            return;
        }
        b(paint);
        TextureImpl.bindNone();
        W.glBegin(7);
        int i6 = i + i2;
        for (int i7 = i; i7 < i6; i7 += 2) {
            float f13 = fArr[i7] * f3;
            float f14 = fArr[i7 + 1] * f4;
            float f15 = f13 + f5;
            float f16 = f14 + f6;
            float f17 = f15 - f2;
            float f18 = f15 + f;
            float f19 = f16 - f2;
            float f20 = f16 + f;
            W.glVertex2f(f17, f19);
            W.glVertex2f(f18, f19);
            W.glVertex2f(f18, f20);
            W.glVertex2f(f17, f20);
        }
        W.glEnd();
    }

    public void b(float[] fArr, int i, int i2, Paint paint) {
        boolean z = Main.a;
        if (z) {
            GL11.glDisableClientState(32886);
        }
        b(paint);
        TextureImpl.bindNone();
        GL11.glEnableClientState(32884);
        GL11.glVertexPointer(2, 0, a(fArr, i2));
        GL11.glDrawArrays(7, i, i2 / 2);
        if (z) {
            GL11.glEnableClientState(32886);
        }
    }

    public void a(float f, float f2, float f3, int i) {
        Graphics.setCurrent(this.slickGraphics);
        TextureImpl.bindNone();
        if (this.P != i) {
            this.P = i;
            this.circleAngleStep = 6.283185f / i;
            this.cachedCosAngle = (float) FastTrig.cos(this.circleAngleStep);
            this.cachedSinAngle = (float) FastTrig.sin(this.circleAngleStep);
        }
        float f4 = this.cachedCosAngle;
        float f5 = this.cachedSinAngle;
        float f6 = f3;
        float f7 = 0.0f;
        lineRenderer.start();
        int i2 = i + 1;
        float f8 = f6 + f;
        float f9 = 0.0f + f2;
        for (int i3 = 0; i3 < i2; i3++) {
            lineRenderer.vertex(f6 + f, f7 + f2);
            float f10 = f6;
            f6 = (f4 * f6) - (f5 * f7);
            f7 = (f5 * f10) + (f4 * f7);
        }
        lineRenderer.end();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void i() {
        z();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void j() {
        A();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void k() {
        z();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void l() {
        A();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(float f, float f2, float f3) {
        this.currentTransform.c += f;
        this.currentTransform.g = f2;
        this.currentTransform.h = f3;
    }

    public static void a(float f, PointF pointF) {
        float fFastSin = Utility.fastSin(f);
        float fFastCos = Utility.fastCos(f);
        float f2 = pointF.x;
        float f3 = pointF.y;
        pointF.x = (fFastCos * f3) - (fFastSin * f2);
        pointF.y = (fFastSin * f3) + (fFastCos * f2);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(float f, float f2) {
        this.currentTransform.d *= f;
        this.currentTransform.e *= f2;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(float f, float f2, float f3, float f4) {
        b(f3, f4);
        a(f, f2);
        b(-f3, -f4);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void b(float f, float f2) {
        this.currentTransform.a += f * this.currentTransform.d;
        this.currentTransform.b += f2 * this.currentTransform.e;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(GraphicsOperation graphicsOperation) {
        graphicsOperation.draw(this);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(float f, float f2, float f3, float f4, Paint paint) {
        b(paint);
        float f5 = f * this.currentTransform.d;
        float f6 = f2 * this.currentTransform.e;
        float f7 = f5 + this.currentTransform.a;
        float f8 = f6 + this.currentTransform.b;
        float f9 = f3 * this.currentTransform.d;
        float f10 = f4 * this.currentTransform.e;
        this.slickGraphics.drawLine(f7, f8, f9 + this.currentTransform.a, f10 + this.currentTransform.b);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Paint paint) {
        a(paint, VariableScope.nullOrMissingString, false);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(ShaderProgram shaderProgram) {
        if (this.shaderSupport) {
            c(shaderProgram);
            v();
            currentShader = null;
        }
    }

    public void y() {
        this.slickGraphics.flushBuffer();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void p() {
        this.slickGraphics.flushBuffer();
        this.lastPaint = null;
        this.slickGraphics.flush();
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void q() {
        if (this.slickGraphics != null) {
            this.slickGraphics.destroy();
        }
        this.slickGraphics = null;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public int a(String str, Paint paint) {
        a(paint, str);
        int lineHeight = this.slickGraphics.getFont().getLineHeight();
        if (x()) {
            lineHeight = (int) (lineHeight / this.uiScale);
        }
        return lineHeight;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public int b(String str, Paint paint) {
        a(paint, str);
        int width = this.slickGraphics.getFont().getWidth(str);
        if (x()) {
            width = (int) (width / this.uiScale);
        }
        return width;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public Texture r() {
        return outOfMemoryImage;
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Texture texture, File file) {
        SlickTexture slickTextureE = e(texture);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            ImageOut.write(slickTextureE.C(), "png", bufferedOutputStream);
            bufferedOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        } catch (SlickException e3) {
            throw new RuntimeException((Throwable) e3);
        }
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void a(Lock lock) {
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public void b(Lock lock) {
    }

    public void z() {
        GraphicsTransform graphicsTransform;
        this.transformStack.add(this.currentTransform);
        if (this.transformPool.size == 0) {
            graphicsTransform = new GraphicsTransform();
        } else {
            graphicsTransform = (GraphicsTransform) this.transformPool.c();
        }
        this.currentTransform.a(graphicsTransform);
        this.currentTransform = graphicsTransform;
    }

    public void A() {
        if (this.transformStack.size() == 0) {
            throw new RuntimeException("tranform stack is empty");
        }
        this.transformPool.add(this.currentTransform);
        this.currentTransform = (GraphicsTransform) this.transformStack.c();
        b(false);
    }

    @Override // com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine
    public float s() {
        return this.uiScale;
    }
}
