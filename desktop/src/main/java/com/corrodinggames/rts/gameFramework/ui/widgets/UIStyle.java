package com.corrodinggames.rts.gameFramework.ui.widgets;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.graphics.GamePaint;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.a.h */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/a/h.class */
public class UIStyle {
    /* JADX INFO: renamed from: r */
    public UIStyle normalStyle;
    /* JADX INFO: renamed from: u */
    public int paddingSize;
    /* JADX INFO: renamed from: v */
    public UIStyle hoverStyle;
    /* JADX INFO: renamed from: p */
    Texture backgroundTexture;
    /* JADX INFO: renamed from: j */
    public static final UIStyle defaultStyle = new UIStyle();
    /* JADX INFO: renamed from: k */
    public static final UIStyle hoveredStyle = new UIStyle();
    /* JADX INFO: renamed from: l */
    public static final UIStyle noBackgroundStyle = new UIStyle();
    /* JADX INFO: renamed from: m */
    public static final UIStyle debugStyle = new UIStyle();
    /* JADX INFO: renamed from: n */
    public static final UIStyle solidPanelStyle = new UIStyle();
    /* JADX INFO: renamed from: w */
    static Rect workRect = new Rect();
    /* JADX INFO: renamed from: x */
    static Rect intRectFromRectF = new Rect();
    /* JADX INFO: renamed from: y */
    static Rect growRect = new Rect();
    /* JADX INFO: renamed from: s */
    public int shadowOffsetX = 3;
    /* JADX INFO: renamed from: t */
    public int shadowOffsetY = 3;
    /* JADX INFO: renamed from: o */
    Paint backgroundPaint = new GamePaint();
    /* JADX INFO: renamed from: q */
    Paint borderPaint = new GamePaint();

    /* JADX INFO: renamed from: b */
    public static void createStyles() {
        UIStyle uIStyle = defaultStyle;
        uIStyle.backgroundPaint.b(Color.a(140, 100, 100, 100));
        uIStyle.borderPaint.b(-16777216);
        uIStyle.borderPaint.a(Paint.Style.STROKE);
        UIStyle uIStyle2 = hoveredStyle;
        uIStyle2.backgroundPaint.b(Color.a(SlickToAndroidKeycodes.AndroidCodes.KEYCODE_STB_INPUT, 100, 100, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_BUTTON_3));
        uIStyle2.borderPaint.b(-16777216);
        uIStyle2.borderPaint.a(Paint.Style.STROKE);
        UIStyle uIStyle3 = noBackgroundStyle;
        uIStyle3.backgroundPaint = null;
        uIStyle3.borderPaint = null;
        UIStyle uIStyle4 = debugStyle;
        uIStyle4.backgroundPaint = null;
        uIStyle4.borderPaint.b(-65536);
        uIStyle4.borderPaint.c(127);
        uIStyle4.borderPaint.a(Paint.Style.STROKE);
        UIStyle uIStyle5 = solidPanelStyle;
        uIStyle5.backgroundPaint.c(255);
        uIStyle5.backgroundTexture = GameEngine.getInstance().gameUI.metalDarkTexture;
        uIStyle5.borderPaint.b(-7829368);
        uIStyle5.borderPaint.c(255);
        uIStyle5.borderPaint.a(Paint.Style.STROKE);
    }

    /* JADX INFO: renamed from: a */
    public void setBackgroundTexture(Texture texture) {
        this.backgroundTexture = texture;
    }

    /* JADX INFO: renamed from: a */
    public void copyFrom(UIStyle uIStyle) {
        this.backgroundTexture = uIStyle.backgroundTexture;
        if (uIStyle.backgroundPaint != null) {
            this.backgroundPaint = new Paint(uIStyle.backgroundPaint);
        } else {
            this.backgroundPaint = null;
        }
        if (uIStyle.borderPaint != null) {
            this.borderPaint = new Paint(uIStyle.borderPaint);
        } else {
            this.borderPaint = null;
        }
    }

    /* JADX INFO: renamed from: a */
    public void draw(GraphicsEngine graphicsEngine, RectF rectF) {
        intRectFromRectF.a = (int) rectF.a;
        intRectFromRectF.b = (int) rectF.b;
        intRectFromRectF.c = (int) rectF.c;
        intRectFromRectF.d = (int) rectF.d;
        draw(graphicsEngine, intRectFromRectF, UIState.normal);
    }

    /* JADX INFO: renamed from: c */
    public void drawNormal(GraphicsEngine graphicsEngine, Rect rect) {
        draw(graphicsEngine, rect, UIState.normal);
    }

    /* JADX INFO: renamed from: a */
    public void draw(GraphicsEngine graphicsEngine, Rect rect, UIState uIState) {
        if (this.paddingSize > 0) {
            growRect.a(rect);
            rect = growRect;
            Utility.grow(rect, this.paddingSize);
        }
        if (this.normalStyle != null) {
            workRect.a(rect);
            workRect.a(this.shadowOffsetX, this.shadowOffsetY);
            this.normalStyle.drawBackground(graphicsEngine, workRect);
        }
        if (uIState == UIState.hovered && this.hoverStyle != null) {
            this.hoverStyle.drawBackground(graphicsEngine, rect);
        } else {
            drawBackground(graphicsEngine, rect);
        }
    }

    /* JADX INFO: renamed from: a */
    public void drawBackground(GraphicsEngine graphicsEngine, Rect rect) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.backgroundTexture != null) {
            gameEngine.renderGraphicsEngine.a(this.backgroundTexture, rect, this.backgroundPaint, 0, 0, 0, 0);
        } else if (this.backgroundPaint != null) {
            graphicsEngine.b(rect, this.backgroundPaint);
        }
        if (this.borderPaint != null) {
            graphicsEngine.b(rect, this.borderPaint);
        }
    }
}
