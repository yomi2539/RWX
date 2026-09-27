package com.corrodinggames.rts.gameFramework.ui.widgets;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.a.b */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/a/b.class */
public class MenuButton extends TextLabel {
    public MenuButton() {
        this.style = UIStyle.defaultStyle;
    }

    @Override // com.corrodinggames.rts.gameFramework.ui.widgets.TextLabel, com.corrodinggames.rts.gameFramework.ui.widgets.UIElement
    /* JADX INFO: renamed from: a */
    public void draw(float f, float f2) {
        if (this.isHovered) {
            this.style = UIStyle.hoveredStyle;
        } else {
            this.style = UIStyle.defaultStyle;
        }
        super.draw(f, f2);
    }
}
