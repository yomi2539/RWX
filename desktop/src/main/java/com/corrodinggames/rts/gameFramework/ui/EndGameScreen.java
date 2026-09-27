package com.corrodinggames.rts.gameFramework.ui;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import com.corrodinggames.rts.appFramework.GameView;
import com.corrodinggames.rts.appFramework.InGameActivity;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.effects.EffectQuality;
import com.corrodinggames.rts.gameFramework.local.Locale;
import com.corrodinggames.rts.gameFramework.ui.widgets.UIStyle;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;
import java.util.ArrayList;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.f */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/f.class */
public class EndGameScreen {

    /* JADX INFO: renamed from: a */
    Paint textPaint;

    /* JADX INFO: renamed from: n */
    boolean savedSelectionBoxActive;

    /* JADX INFO: renamed from: o */
    boolean savedMousePressed;

    /* JADX INFO: renamed from: p */
    static String rateGameText = Locale.get("gui.rategame.text", new Object[0]);

    /* JADX INFO: renamed from: q */
    static String rateGameYesText = Locale.get("gui.rategame.yes", new Object[0]);

    /* JADX INFO: renamed from: r */
    static String rateGameNoText = Locale.get("gui.rategame.no", new Object[0]);

    /* JADX INFO: renamed from: b */
    boolean hasCheckedRateGamePrompt = false;

    /* JADX INFO: renamed from: c */
    float timer = 0.0f;

    /* JADX INFO: renamed from: d */
    float fireworkTimer = 0.0f;

    /* JADX INFO: renamed from: e */
    Rect textBounds = new Rect();

    /* JADX INFO: renamed from: f */
    Rect tempRect = new Rect();

    /* JADX INFO: renamed from: g */
    Rect rateGamePopupRect = new Rect();

    /* JADX INFO: renamed from: h */
    StatsHistoryChart statsView = null;

    /* JADX INFO: renamed from: i */
    ArrayList backgroundTasks = new ArrayList();

    /* JADX INFO: renamed from: j */
    int buttonHeight = 30;

    /* JADX INFO: renamed from: k */
    int buttonWidth = 140;

    /* JADX INFO: renamed from: l */
    int buttonSpacing = 30;

    /* JADX INFO: renamed from: m */
    final Rect screenBounds = new Rect();

    /* JADX INFO: renamed from: s */
    boolean showRateGamePopup = false;

    /* JADX INFO: renamed from: t */
    float unusedFloat1 = 0.0f;

    public EndGameScreen() {
        GameEngine gameEngine = GameEngine.getInstance();
        setupButtonActions();
        this.textPaint = new Paint();
        this.textPaint.a(true);
        this.textPaint.a(Paint.Align.CENTER);
        this.textPaint.a(255, 0, 255, 0);
        gameEngine.updatePaintTextSize(this.textPaint, 34.0f);
    }

    /* JADX INFO: renamed from: a */
    void setupButtonActions() {
        this.backgroundTasks.clear();
        this.backgroundTasks.add(new BackgroundTask("Finish game") { // from class: com.corrodinggames.rts.gameFramework.f.f.1
            @Override // com.corrodinggames.rts.gameFramework.ui.BackgroundTask
            /* JADX INFO: renamed from: b */
            void run() {
                GameEngine.getInstance().shouldAdvanceAfterGameEnd = true;
            }
        });
        this.backgroundTasks.add(new BackgroundTask("Keep playing") { // from class: com.corrodinggames.rts.gameFramework.f.f.2
            @Override // com.corrodinggames.rts.gameFramework.ui.BackgroundTask
            /* JADX INFO: renamed from: b */
            void run() {
                GameEngine.getInstance().isContinuingAfterGameEnd = true;
            }
        });
    }

    /* JADX INFO: renamed from: b */
    boolean isGameOver() {
        GameEngine gameEngine = GameEngine.getInstance();
        if ((gameEngine.hasWonGame || gameEngine.hasLostGame) && !gameEngine.isContinuingAfterGameEnd) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public void update(float f) {
        GameEngine gameEngine = GameEngine.getInstance();
        GameUI gameUI = gameEngine.gameUI;
        boolean zIsGameOver = isGameOver();
        this.screenBounds.h();
        this.savedSelectionBoxActive = false;
        if (zIsGameOver && !gameUI.isDraggingSelection) {
            int screenPixels = gameEngine.toScreenPixels(this.buttonHeight);
            int screenPixels2 = gameEngine.toScreenPixels(this.buttonWidth);
            int screenPixels3 = screenPixels + gameEngine.toScreenPixels(this.buttonSpacing);
            int screenPixels4 = screenPixels2 + gameEngine.toScreenPixels(this.buttonSpacing);
            int size = this.backgroundTasks.size();
            float realAssetPath = 0.0f;
            if (gameUI.isCompactEndGameUi) {
                gameUI.endGameFadeIn += (2.0f * f) / 60.0f;
                realAssetPath = Utility.easeInOutQuad(Utility.clampTo255(gameUI.endGameFadeIn, 0.0f, 1.0f));
            }
            int screenPixels5 = gameEngine.toScreenPixels(40) + (screenPixels4 * size);
            int screenPixels6 = gameEngine.toScreenPixels(140);
            if (gameUI.showEndGameStatsButton) {
                screenPixels6 += gameEngine.toScreenPixels(50);
            }
            if (gameUI.isCompactEndGameUi) {
                screenPixels5 = (int) Utility.lerp(screenPixels5, gameEngine.currentScreenWidthPixels * 0.9f, realAssetPath);
                screenPixels6 = (int) Utility.lerp(screenPixels6, gameEngine.currentScreenHeightPixels * 0.9f, realAssetPath);
            }
            float fFromHexString = gameEngine.halfScreenHeight - (screenPixels6 / 2);
            if (!gameUI.isCompactEndGameUi) {
                fFromHexString = Utility.lerp(fFromHexString, fFromHexString / 2.0f, 1.0f - realAssetPath);
            }
            if (fFromHexString < 20.0f) {
                fFromHexString = 20.0f;
            }
            this.rateGamePopupRect.b = (int) fFromHexString;
            this.rateGamePopupRect.d = this.rateGamePopupRect.b + screenPixels6;
            this.rateGamePopupRect.a = (int) ((gameEngine.currentScreenWidthPixels / 2.0f) - (screenPixels5 / 2));
            this.rateGamePopupRect.c = (int) ((gameEngine.currentScreenWidthPixels / 2.0f) + (screenPixels5 / 2));
            this.screenBounds.a(this.rateGamePopupRect);
            if (this.screenBounds.b((int) gameUI.selectionBoxMinWidth, (int) gameUI.selectionBoxMinHeight)) {
                this.savedSelectionBoxActive = gameUI.isSelectionBoxActive;
                gameUI.isSelectionBoxActive = false;
                this.savedMousePressed = gameUI.isMousePressed;
                gameUI.isMousePressed = false;
            }
            gameUI.forceModifiersInsideRect(this.screenBounds);
        }
    }

    /* JADX INFO: renamed from: b */
    public void draw(float f) {
        GameEngine gameEngine = GameEngine.getInstance();
        GameUI gameUI = gameEngine.gameUI;
        boolean zIsGameOver = isGameOver();
        if (!gameEngine.hasWonGame) {
            this.hasCheckedRateGamePrompt = false;
        } else if (!this.hasCheckedRateGamePrompt) {
            this.hasCheckedRateGamePrompt = true;
            if (!gameEngine.isDemo && gameEngine.settingsEngine.numberOfWins >= 5 && !gameEngine.settingsEngine.rateGameShown && GameEngine.isRateGamePromptEnabled) {
                this.showRateGamePopup = true;
                gameEngine.settingsEngine.rateGameShown = true;
                gameEngine.settingsEngine.save();
            }
        }
        if (!zIsGameOver) {
            this.timer = 0.0f;
        }
        if (zIsGameOver && !gameUI.isDraggingSelection) {
            this.timer += f;
            if (gameEngine.currentTick < 120) {
                this.timer = 100000.0f;
            }
            if (this.savedSelectionBoxActive) {
                gameUI.isSelectionBoxActive = true;
            }
            if (this.savedMousePressed) {
                gameUI.isMousePressed = true;
            }
            boolean z = this.timer > 80.0f;
            boolean z2 = this.timer > 100.0f;
            boolean z3 = this.timer > 110.0f;
            int screenPixels = gameEngine.toScreenPixels(this.buttonHeight);
            int screenPixels2 = gameEngine.toScreenPixels(this.buttonWidth);
            int screenPixels3 = screenPixels + gameEngine.toScreenPixels(this.buttonSpacing);
            int screenPixels4 = screenPixels2 + gameEngine.toScreenPixels(this.buttonSpacing);
            int size = this.backgroundTasks.size();
            int i = (int) ((gameEngine.currentScreenWidthPixels / 2.0f) - (((screenPixels2 * size) + ((size - 1) * screenPixels3)) / 2));
            float f2 = 0.0f;
            if (gameUI.isCompactEndGameUi) {
                f2 = Utility.easeInOutQuad(Utility.clampTo255(gameUI.endGameFadeIn, 0.0f, 1.0f)) >= 1.0f ? 1.0f : 0.0f;
            }
            if (z) {
                float f3 = gameUI.ninePatchStyle5.patchScale;
                gameUI.ninePatchStyle5.patchScale = f2;
                gameUI.ninePatchStyle5.drawNormal(gameEngine.renderGraphicsEngine, this.screenBounds);
                gameUI.ninePatchStyle5.patchScale = f3;
            }
            int screenPixels5 = this.screenBounds.b + gameEngine.toScreenPixels(40);
            int i2 = (int) (gameEngine.currentScreenWidthPixels / 2.0f);
            int screenPixels6 = this.screenBounds.d - gameEngine.toScreenPixels(45);
            int iA = Color.a(140, 100, 100, 100);
            Paint paint = this.textPaint;
            String str = "Victory!";
            if (gameEngine.hasLostGame) {
                str = "Defeat";
            }
            float f4 = 1.0f;
            if (this.timer < 95.0f) {
                f4 = this.timer / 95.0f;
            }
            int iFastCos = (int) (screenPixels5 - (Utility.fastCos(f4 * 90.0f) * 100.0f));
            paint.a(str, 0, str.length(), this.textBounds);
            gameEngine.renderGraphicsEngine.a(str, i2, iFastCos - ((paint.l() + paint.m()) / 2.0f), paint);
            if (this.timer < 100.0f && !gameEngine.hasLostGame) {
                this.fireworkTimer += f;
                if (this.fireworkTimer > 0.5f) {
                    this.fireworkTimer = 0.0f;
                    gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                    gameEngine.effectManager.setForceHighQuality();
                    Effect effectCreateLightEffectInternal = gameEngine.effectManager.createLightEffectInternal(0.0f, 0.0f, 0.0f, Color.a(255, Utility.getRandomIntInRange(0, 255), Utility.getRandomIntInRange(0, 255), Utility.getRandomIntInRange(0, 255)));
                    if (effectCreateLightEffectInternal != null) {
                        effectCreateLightEffectInternal.drawLayer = (short) 4;
                        effectCreateLightEffectInternal.posX = i2 + Utility.randomFloatInRange(-70.0f, 70.0f);
                        effectCreateLightEffectInternal.posY = iFastCos + Utility.randomFloatInRange(-15.0f, 15.0f);
                        effectCreateLightEffectInternal.posY += gameEngine.halfScreenHeight / 2.0f;
                        effectCreateLightEffectInternal.posZ += gameEngine.halfScreenHeight / 2.0f;
                        effectCreateLightEffectInternal.lifeTimer = Utility.randomFloatInRange(140.0f, 380.0f);
                        effectCreateLightEffectInternal.lifeMax = effectCreateLightEffectInternal.lifeTimer;
                        effectCreateLightEffectInternal.fadeIn = true;
                        effectCreateLightEffectInternal.fadeOut = true;
                        effectCreateLightEffectInternal.fadeDuration = 5.0f;
                        effectCreateLightEffectInternal.alpha = 2.0f;
                        effectCreateLightEffectInternal.velocityY = Utility.randomFloatInRange(-2.7f, 2.7f);
                        effectCreateLightEffectInternal.velocityX = Utility.randomFloatInRange(-12.7f, 12.7f);
                        effectCreateLightEffectInternal.scaleFrom = 0.4f;
                        effectCreateLightEffectInternal.scaleTo = 0.2f;
                        effectCreateLightEffectInternal.velocityZ = Utility.randomFloatInRange(2.0f, 4.0f);
                        effectCreateLightEffectInternal.physicsGravity = 2.0f;
                        effectCreateLightEffectInternal.useBounce = true;
                        effectCreateLightEffectInternal.isUiEffect = true;
                    }
                }
            }
            int i3 = iFastCos + 60;
            if (z3) {
                Rect rect = this.textBounds;
                Rect rect2 = this.tempRect;
                rect.a(this.screenBounds.a + gameEngine.toScreenPixels(10), this.screenBounds.b + gameEngine.toScreenPixels(60), this.screenBounds.c - gameEngine.toScreenPixels(10), screenPixels6 - gameEngine.toScreenPixels(10));
                rect2.a(rect);
                if (!gameUI.isCompactEndGameUi) {
                    rect.b = this.screenBounds.d + gameEngine.toScreenPixels(15);
                    rect.d = rect.b + gameEngine.toScreenPixels(200);
                }
                boolean z4 = gameUI.endGameFadeIn >= 1.0f;
                if (this.statsView != null) {
                    this.statsView.drawChart(rect, rect2, f, z4, gameUI.showEndGameStatsButton);
                }
            }
            for (int i4 = 0; i4 < this.backgroundTasks.size(); i4++) {
                if (z2) {
                    BackgroundTask backgroundTask = (BackgroundTask) this.backgroundTasks.get(i4);
                    if (gameUI.isButtonPressedStyled(i, screenPixels6, screenPixels2, screenPixels, backgroundTask.getTaskName(), IconGroup.none, false, iA, gameUI.buildingPreviewInvalidPaint, (UIStyle) gameUI.ninePatchStyle3)) {
                        this.showRateGamePopup = false;
                        backgroundTask.run();
                    }
                }
                i += screenPixels3 + screenPixels2;
            }
            if (this.showRateGamePopup) {
                drawRateGamePopup(f);
            }
            if (this.screenBounds.b((int) gameUI.selectionBoxMinWidth, (int) gameUI.selectionBoxMinHeight)) {
            }
            gameUI.forceModifiersInsideRect(this.screenBounds);
        }
    }

    /* JADX INFO: renamed from: c */
    void drawRateGamePopup(float f) {
        GameEngine gameEngine = GameEngine.getInstance();
        GameUI gameUI = gameEngine.gameUI;
        int screenPixels = gameEngine.toScreenPixels(SlickToAndroidKeycodes.AndroidCodes.KEYCODE_STB_INPUT);
        int i = (int) ((gameEngine.currentScreenWidthPixels / 2.0f) - (screenPixels / 2));
        int screenPixels2 = gameEngine.toScreenPixels(120);
        int i2 = (int) (gameEngine.currentScreenHeightPixels - screenPixels2);
        this.rateGamePopupRect.a(i, i2, screenPixels, screenPixels2);
        gameEngine.renderGraphicsEngine.b(this.rateGamePopupRect, gameUI.minimapViewportBorderPaint);
        int i3 = i + (screenPixels / 2);
        Paint paint = this.textPaint;
        String str = rateGameText;
        paint.a(str, 0, str.length(), this.textBounds);
        gameEngine.renderGraphicsEngine.a(str, i3, i2 - ((paint.l() + paint.m()) / 2.0f), paint);
        int iC = i2 + this.textBounds.c();
        int screenPixels3 = gameEngine.toScreenPixels(70);
        int screenPixels4 = gameEngine.toScreenPixels(30);
        int screenPixels5 = ((i + (screenPixels / 2)) - gameEngine.toScreenPixels(10)) - screenPixels3;
        int iA = Color.a(140, 100, 100, 100);
        if (gameUI.isButtonPressedStyled(screenPixels5, iC, screenPixels3, screenPixels4, rateGameYesText, IconGroup.none, false, iA, gameUI.buildingPreviewInvalidPaint, (UIStyle) null)) {
            this.showRateGamePopup = false;
            GameView gameView = gameEngine.activeGameView;
            if (gameView == null) {
                GameEngine.logColored("showRateNow: gameView==null");
                return;
            }
            InGameActivity surfaceHolder = gameView.getSurfaceHolder();
            if (surfaceHolder == null) {
                GameEngine.logColored("showRateNow: inGameActivity==null");
                return;
            }
            surfaceHolder.l();
        }
        if (gameUI.isButtonPressedStyled(i + (screenPixels / 2) + gameEngine.toScreenPixels(10), iC, screenPixels3, screenPixels4, rateGameNoText, IconGroup.none, false, iA, gameUI.buildingPreviewInvalidPaint, (UIStyle) null)) {
            this.showRateGamePopup = false;
        }
    }

    /* JADX INFO: renamed from: c */
    public void loadStats() {
        this.statsView = StatsHistoryChart.create();
    }
}
