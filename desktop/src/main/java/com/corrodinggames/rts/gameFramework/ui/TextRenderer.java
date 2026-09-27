package com.corrodinggames.rts.gameFramework.ui;

import android.graphics.Paint;
import com.corrodinggames.rts.gameFramework.GameEngine;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.ai */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/ai.class */
public class TextRenderer extends RenderElement {

    /* JADX INFO: renamed from: d */
    String text;

    final /* synthetic */ TextRenderQueue e;

    TextRenderer(TextRenderQueue textRenderQueue, String str) {
        this.e = textRenderQueue;
        this.text = str;
    }

    /* JADX INFO: renamed from: b */
    public Paint resolvePaint(Paint paint) {
        return paint;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.RenderElement
    /* JADX INFO: renamed from: a */
    public int measureWidth(Paint paint) {
        GameEngine gameEngine = GameEngine.getInstance();
        int iB = gameEngine.renderGraphicsEngine.b(this.text, resolvePaint(paint));
        if (GameEngine.isAndroidPlatform()) {
        }
        return iB;
    }

    /* JADX INFO: renamed from: b */
    public TextRenderer withText(String str) {
        return new TextRenderer(this.e, str);
    }
}
