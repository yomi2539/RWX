package com.corrodinggames.rts.gameFramework.ui;

import com.corrodinggames.rts.gameFramework.utility.FastArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.ak */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/ak.class */
public class TextRenderLine {

    /* JADX INFO: renamed from: a */
    FastArrayList<RenderElement> elements = new FastArrayList();

    int widthPx;

    /* JADX INFO: renamed from: a */
    public void addRenderElement(RenderElement renderElement) {
        this.elements.add(renderElement);
    }
}
