package io.github.rwx.platform;

import io.github.rwx.input.MultiTouchPointerState;
import io.github.rwx.ui.InGameMenuController;

public interface CoreGameView {
    void pause();

    boolean isActive();

    boolean isContinuousRendering();

    boolean isRendering();

    InGameMenuController getInGameMenuController();

    void onResume();

    MultiTouchPointerState getSettings();

    void onSizeChanged();

    void stopRender();
}
