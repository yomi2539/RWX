package com.corrodinggames.rts.gameFramework.ui.widgets;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.graphics.GamePaint;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine;
import com.corrodinggames.rts.gameFramework.ui.TextUtils;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.a.j */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/a/j.class */
public class TextLabel extends UIElement {

    /* JADX INFO: renamed from: a */
    String text;
    /* JADX INFO: renamed from: b */
    Paint paint = new GamePaint();
    /* JADX INFO: renamed from: c */
    UIStyle style = UIStyle.noBackgroundStyle;
    /* JADX INFO: renamed from: d */
    ArrayList<String> lines;

    public TextLabel() {
        this.paint.a(Paint.Align.CENTER);
        this.paint.b(-16777216);
        setTextSize(18.0f);
    }

    /* JADX INFO: renamed from: a */
    public void setTextSize(float f) {
        GameEngine.getInstance().setScaledTextSize(this.paint, f);
        invalidateLayout();
    }

    /* JADX INFO: renamed from: a */
    public void setTextColor(int i) {
        this.paint.b(i);
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIElement
    /* JADX INFO: renamed from: a */
    public String getTypeName() {
        return super.getTypeName() + " (text:" + this.text + ")";
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIElement
    /* JADX INFO: renamed from: a */
    public void draw(final float float1, final float float2) {
        super.draw(float1, float2);
        final GraphicsEngine graphicsEngine = this.getGraphicsEngine();
        final RectF rectF = this.getRectAt(new RectF(), float1, float2);
        this.style.draw(graphicsEngine, rectF);
        if (this.text == null) {
            return;
        }
        if (this.lines == null) {
            graphicsEngine.a(this.text, rectF.d(), rectF.d - this.paddingBottom, this.paint);
        }
        else {
            int n = 0;
            for (final String string : this.lines) {
                final Paint linePaint = this.paint;
                final int lineHeight = TextUtils.getLineHeight(linePaint);
                graphicsEngine.a(string, rectF.d(), rectF.b + this.paddingTop + lineHeight + n * lineHeight, linePaint);
                ++n;
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public void setText(String str) {
        this.text = str;
        invalidateLayout();
    }

    /* JADX INFO: renamed from: c */
    public Rect getTextBounds() {
        RectF rectFA = getRectAt(new RectF(), 0.0f, 0.0f);
        Rect rect = new Rect();
        rect.d = (int) rectFA.d;
        rect.b = (int) rectFA.b;
        rect.a = (int) rectFA.a;
        rect.c = (int) rectFA.c;
        rect.c = 10000;
        return rect;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIElement
    /* JADX INFO: renamed from: b */
    public void layout() {
        super.layout();
        this.getGraphicsEngine();
        final Rect c = this.getTextBounds();
        this.lines = new ArrayList(TextUtils.wrapText(this.text, c, this.paint, this.paint, true));
        this.width = (float)c.b();
        this.height = (float)c.c();
        this.width += this.paddingLeft + this.paddingRight;
        this.height += this.paddingTop + this.paddingBottom;
    }
}
