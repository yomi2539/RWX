package com.corrodinggames.rts.gameFramework.ui.widgets;

import android.graphics.Color;
import com.corrodinggames.rts.gameFramework.local.Locale;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.a.f */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/a/f.class */
public class MenuDialog extends PopupWindow {
    /* JADX INFO: renamed from: a */
    LayoutContainer layoutContainer;

    /* JADX INFO: renamed from: a */
    public static MenuDialog create(String str, boolean z) {
        MenuDialog menuDialog = new MenuDialog();
        menuDialog.style = UIStyle.solidPanelStyle;
        menuDialog.width = 200.0f;
        menuDialog.height = 200.0f;
        TextLabel textLabel = new TextLabel();
        textLabel.setText(str);
        textLabel.setMargin(5.0f);
        textLabel.setPadding(5.0f);
        textLabel.setTextColor(-1);
        menuDialog.addChild(textLabel);
        menuDialog.layoutContainer = new LayoutContainer(LayoutDirection.horizontal);
        menuDialog.addChild(menuDialog.layoutContainer);
        if (z) {
            menuDialog.addButton(Locale.get("menus.common.cancel")).setEventHandler(new UIEventHandler() { // from class: com.corrodinggames.rts.gameFramework.f.a.f.1
                @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIEventHandler
                public boolean handleEvent(UIEvent uIEvent) {
                    menuDialog.removeFromParent();
                    return true;
                }
            });
        }
        return menuDialog;
    }

    /* JADX INFO: renamed from: a */
    public MenuButton createButton(String str) {
        MenuButton menuButton = new MenuButton();
        menuButton.setText(str);
        menuButton.setMargin(5.0f);
        menuButton.setPadding(5.0f);
        menuButton.setTextColor(Color.a(255, 30, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE, 30));
        return menuButton;
    }

    /* JADX INFO: renamed from: b */
    public MenuButton addButton(String str) {
        return addButton(str, (UIEventHandler) null);
    }

    /* JADX INFO: renamed from: a */
    public MenuButton addButton(String str, UIEventHandler uIEventHandler) {
        MenuButton menuButtonA = createButton(str);
        menuButtonA.setEventHandler(uIEventHandler);
        this.layoutContainer.addChild(menuButtonA);
        return menuButtonA;
    }

    /* JADX INFO: renamed from: u_ */
    public void layoutIfNeeded() {
        if (!this.needsLayout) {
            return;
        }
        layout();
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
