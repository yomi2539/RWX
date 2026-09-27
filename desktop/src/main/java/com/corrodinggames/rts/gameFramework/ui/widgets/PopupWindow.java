package com.corrodinggames.rts.gameFramework.ui.widgets;

import android.graphics.RectF;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.a.n */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/a/n.class */
public class PopupWindow extends UIElement {
    /* JADX INFO: renamed from: b */
    UIStyle style = UIStyle.defaultStyle;

    @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIElement
    /* JADX INFO: renamed from: a */
    public void draw(float f, float f2) {
        super.draw(f, f2);
        this.style.draw(getGraphicsEngine(), getRectAt(new RectF(), f, f2));
    }
}
