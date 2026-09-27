package com.corrodinggames.rts.gameFramework.ui;

import android.graphics.Paint;
import com.corrodinggames.rts.gameFramework.graphics.Texture;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.ah */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/ah.class */
public class TextureRenderer extends RenderElement {

    /* JADX INFO: renamed from: a */
    Texture texture;

    /* JADX INFO: renamed from: b */
    float scale = 1.0f;

    /* JADX INFO: renamed from: c */
    int width;

    /* JADX INFO: renamed from: d */
    int height;
    final /* synthetic */ TextRenderQueue e;

    public TextureRenderer(TextRenderQueue textRenderQueue) {
        this.e = textRenderQueue;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.RenderElement
    /* JADX INFO: renamed from: a */
    public int measureWidth(Paint paint) {
        return this.width;
    }
}
