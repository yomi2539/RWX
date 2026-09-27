package com.corrodinggames.rts.gameFramework.ui.widgets;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.a.g */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/a/g.class */
public class LayoutContainer extends UIElement {
    public LayoutContainer() {
    }

    public LayoutContainer(LayoutDirection layoutDirection) {
        this.layoutDirection = layoutDirection;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIElement
    /* JADX INFO: renamed from: a */
    public void draw(float f, float f2) {
        super.draw(f, f2);
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIElement
    /* JADX INFO: renamed from: b */
    public void layout() {
        super.layout();
        getGraphicsEngine();
        this.width = this.layoutWidth;
        this.height = this.layoutHeight;
        this.width += this.paddingLeft + this.paddingRight;
        this.height += this.paddingTop + this.paddingBottom;
    }
}
