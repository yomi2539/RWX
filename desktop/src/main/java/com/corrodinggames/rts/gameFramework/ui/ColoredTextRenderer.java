package com.corrodinggames.rts.gameFramework.ui;

import android.graphics.Paint;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.ag */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/ag.class */
public class ColoredTextRenderer extends TextRenderer {
    public Paint paint;
    /* JADX INFO: renamed from: b */
    public int color;
    final /* synthetic */ TextRenderQueue c;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    ColoredTextRenderer(TextRenderQueue textRenderQueue, String str, Paint paint) {
        super(textRenderQueue, str);
        this.c = textRenderQueue;
        this.color = 0;
        this.paint = paint;
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    ColoredTextRenderer(TextRenderQueue textRenderQueue, String str, Paint paint, int i) {
        super(textRenderQueue, str);
        this.c = textRenderQueue;
        this.color = 0;
        this.paint = paint;
        this.color = i;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.TextRenderer
    /* JADX INFO: renamed from: b */
    public Paint resolvePaint(Paint paint) {
        if (this.paint == null) {
            if (this.color != 0) {
                TextRenderQueue.coloredTextPaint.a(paint);
                TextRenderQueue.coloredTextPaint.b(this.color);
                return TextRenderQueue.coloredTextPaint;
            }
            return paint;
        }
        if (this.color != 0) {
            TextRenderQueue.coloredTextPaint.a(this.paint);
            TextRenderQueue.coloredTextPaint.b(this.color);
            return TextRenderQueue.coloredTextPaint;
        }
        return this.paint;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.TextRenderer
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    /* JADX INFO: renamed from: b */
    public ColoredTextRenderer withText(String str) {
        return new ColoredTextRenderer(this.c, str, this.paint, this.color);
    }
}
