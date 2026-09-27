package com.corrodinggames.rts.gameFramework.ui;

import android.graphics.Paint;
import android.graphics.Rect;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.utility.FastArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.aj */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/aj.class */
public class TextRenderLayout {
    /* JADX INFO: renamed from: a */
    FastArrayList<TextRenderLine> lines;
    /* JADX INFO: renamed from: b */
    Rect rect;
    /* JADX INFO: renamed from: c */
    Paint defaultPaint;
    /* JADX INFO: renamed from: d */
    Paint highlightPaint;

    /* JADX INFO: renamed from: a */
    public void draw(final float float1, final float float2) {
        final GameEngine instance = GameEngine.getInstance();
        int n = 0;
        final int lineHeight = TextUtils.getLineHeight(this.defaultPaint);
        for (final TextRenderLine textRenderLine : this.lines) {
            int n2 = 0;
            RenderElement renderElement = null;
            for (final RenderElement renderElement2 : textRenderLine.elements) {
                if (renderElement != null) {
                    n2 += renderElement.measureWidth(this.defaultPaint);
                }
                int n3 = (int)(float1 + n2 + this.rect.d());
                n3 -= textRenderLine.widthPx / 2;
                final int n4 = (int)(float2 + this.rect.b + lineHeight / 2 + n * lineHeight);
                if (!(renderElement2 instanceof TextRenderer)) {
                    if (renderElement2 instanceof TextureRenderer) {
                        final TextureRenderer textureRenderer = (TextureRenderer)renderElement2;
                        final Texture a = textureRenderer.texture;
                        instance.renderGraphicsEngine.a(a, (float)n3, n4 - a.q * textureRenderer.scale, TextRenderQueue.sharedStrikeThruPaint, 0.0f, textureRenderer.scale);
                    }
                    renderElement = renderElement2;
                }
                else {
                    final TextRenderer textRenderer = (TextRenderer)renderElement2;
                    instance.renderGraphicsEngine.a(textRenderer.text, (float)n3, (float)n4, textRenderer.resolvePaint(this.defaultPaint));
                    renderElement = renderElement2;
                }
            }
            ++n;
        }
    }
}
