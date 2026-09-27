package com.corrodinggames.rts.gameFramework.ui;

import android.content.Context;
import android.graphics.*;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.ai.AIController;
import com.corrodinggames.rts.game.units.*;
import com.corrodinggames.rts.game.units.actions.*;
import com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfigParser;
import com.corrodinggames.rts.game.units.custom.LocaleString;
import com.corrodinggames.rts.game.units.custom.condition.StoredResourceEntry;
import com.corrodinggames.rts.game.units.custom.condition.StoredResources;
import com.corrodinggames.rts.game.units.custom.condition.resources.CreditsResource;
import com.corrodinggames.rts.game.units.custom.condition.resources.Resource;
import com.corrodinggames.rts.game.units.custom.logic.CustomAction;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.gameFramework.*;
import com.corrodinggames.rts.gameFramework.audio.SoundEngine;
import com.corrodinggames.rts.gameFramework.effects.BuildPreview;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.effects.EffectQuality;
import com.corrodinggames.rts.gameFramework.effects.EffectType;
import com.corrodinggames.rts.gameFramework.graphics.GamePaint;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.local.Locale;
import com.corrodinggames.rts.gameFramework.mission.MissionEngine;
import com.corrodinggames.rts.gameFramework.mod.ModInfo;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.network.NetworkEngine;
import com.corrodinggames.rts.gameFramework.statistics.a.BasicUIElement;
import com.corrodinggames.rts.gameFramework.ui.widgets.*;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;
import com.corrodinggames.rts.gameFramework.utility.UnitList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.g */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/g.class */
public final class GameUI extends Serializable {

    /* JADX INFO: renamed from: f */
    EditorOrBuilder editorOrBuilder;

    /* JADX INFO: renamed from: g */
    public GameInterfaceRenderer interfaceRenderer;

    /* JADX INFO: renamed from: h */
    public MessageManager messageManager;

    /* JADX INFO: renamed from: i */
    public WarLogDisplay warLogDisplay;

    /* JADX INFO: renamed from: j */
    public Leaderboard leaderboard;

    /* JADX INFO: renamed from: k */
    public EndGameScreen endGameScreen;

    /* JADX INFO: renamed from: v */
    double gameTime;

    /* JADX INFO: renamed from: C */
    boolean isGamePaused;

    /* JADX INFO: renamed from: D */
    boolean isDoubleClick;

    /* JADX INFO: renamed from: E */
    float touchStartTime;

    /* JADX INFO: renamed from: F */
    public float touchX;

    /* JADX INFO: renamed from: G */
    public float touchY;

    /* JADX INFO: renamed from: W */
    public BaseUnit lastSelectedUnit;

    /* JADX INFO: renamed from: X */
    public float lastSelectionTime;

    /* JADX INFO: renamed from: Y */
    public int selectionChangeCounter;

    /* JADX INFO: renamed from: Z */
    public float selectionBoxStartTime;

    /* JADX INFO: renamed from: aa */
    public BaseUnit selectedBuilder;

    /* JADX INFO: renamed from: ac */
    public AbstractUnitAction currentAction;

    /* JADX INFO: renamed from: ad */
    public int buildQueueId;

    /* JADX INFO: renamed from: ae */
    public boolean isBuildingMode;

    /* JADX INFO: renamed from: af */
    public float buildingRotation;

    /* JADX INFO: renamed from: ag */
    public float buildingPlaceX;

    /* JADX INFO: renamed from: ah */
    public float buildingPlaceY;

    /* JADX INFO: renamed from: ai */
    public boolean isQueuedBuild;

    /* JADX INFO: renamed from: aj */
    public float cameraShakeX;

    /* JADX INFO: renamed from: ak */
    public float cameraShakeY;

    /* JADX INFO: renamed from: al */
    public float cameraShakeIntensity;

    /* JADX INFO: renamed from: am */
    public float cameraShakeDecay;

    /* JADX INFO: renamed from: an */
    public float screenFlashIntensity;

    /* JADX INFO: renamed from: ao */
    public float screenFlashDecay;

    /* JADX INFO: renamed from: ap */
    public boolean isScreenFlashActive;

    /* JADX INFO: renamed from: aq */
    public float screenFlashRed;

    /* JADX INFO: renamed from: ar */
    public float screenFlashGreen;

    /* JADX INFO: renamed from: as */
    public int screenFlashBlue;

    /* JADX INFO: renamed from: au */
    public Paint unitHealthBarPaint;

    /* JADX INFO: renamed from: av */
    public Paint unitHealthBarBackgroundPaint;

    /* JADX INFO: renamed from: aw */
    public Paint unitShieldBarPaint;

    /* JADX INFO: renamed from: ax */
    public Paint unitSelectionPaint;

    /* JADX INFO: renamed from: ay */
    public Paint unitSelectionBorderPaint;

    /* JADX INFO: renamed from: az */
    public Paint selectionBoxPaint;

    /* JADX INFO: renamed from: aA */
    public Paint selectionBoxBorderPaint;

    /* JADX INFO: renamed from: aB */
    public Paint rallyPointPaint;

    /* JADX INFO: renamed from: aC */
    public Paint buildingPreviewPaint;

    /* JADX INFO: renamed from: aD */
    public Paint buildingPreviewInvalidPaint;

    /* JADX INFO: renamed from: aE */
    public Paint unitRangePaint;

    /* JADX INFO: renamed from: aF */
    public Paint unitRangeBorderPaint;

    /* JADX INFO: renamed from: aG */
    public Paint unitPathPaint;

    /* JADX INFO: renamed from: aH */
    public Paint unitPathBorderPaint;

    /* JADX INFO: renamed from: aI */
    public Paint unitTargetLinePaint;

    /* JADX INFO: renamed from: aJ */
    public Paint unitTargetLineBorderPaint;

    /* JADX INFO: renamed from: aK */
    Paint fogOfWarPaint;

    /* JADX INFO: renamed from: aL */
    Paint minimapPaint;

    /* JADX INFO: renamed from: aM */
    Paint minimapBorderPaint;

    /* JADX INFO: renamed from: aN */
    Paint minimapUnitPaint;

    /* JADX INFO: renamed from: aO */
    Paint minimapViewportPaint;

    /* JADX INFO: renamed from: aP */
    Paint minimapViewportBorderPaint;

    /* JADX INFO: renamed from: aQ */
    GamePaint unitIconPaint;

    /* JADX INFO: renamed from: aR */
    GamePaint buildingIconPaint;

    /* JADX INFO: renamed from: aS */
    GamePaint effectIconPaint;

    /* JADX INFO: renamed from: aT */
    public float tooltipDelay;

    /* JADX INFO: renamed from: aX */
    int selectedUnitCount;

    /* JADX INFO: renamed from: aZ */
    public boolean showDebugInfo;

    /* JADX INFO: renamed from: bd */
    boolean isTooltipVisible;

    /* JADX INFO: renamed from: be */
    float tooltipTimer;

    /* JADX INFO: renamed from: bf */
    Paint tooltipBackgroundPaint;

    /* JADX INFO: renamed from: bg */
    Paint tooltipBorderPaint;

    /* JADX INFO: renamed from: bn */
    public Texture uiTexture1;

    /* JADX INFO: renamed from: bo */
    public Texture uiTexture2;

    /* JADX INFO: renamed from: bp */
    NinePatchStyle ninePatchStyle1;

    /* JADX INFO: renamed from: bq */
    NinePatchStyle ninePatchStyle2;

    /* JADX INFO: renamed from: br */
    NinePatchStyle ninePatchStyle3;

    /* JADX INFO: renamed from: bs */
    NinePatchStyle ninePatchStyle4;

    /* JADX INFO: renamed from: bt */
    NinePatchStyle ninePatchStyle5;

    /* JADX INFO: renamed from: bu */
    NinePatchStyle ninePatchStyle6;

    /* JADX INFO: renamed from: bG */
    String notAvailableInDemoText;

    /* JADX INFO: renamed from: bH */
    String lockedText;

    /* JADX INFO: renamed from: bI */
    LocaleString notEnoughResourcesText;

    /* JADX INFO: renamed from: bJ */
    String cannotPlaceGeneralText;

    /* JADX INFO: renamed from: bK */
    String cannotPlaceNeedsResourcePoolText;

    /* JADX INFO: renamed from: bL */
    String cannotPlaceNeedsWaterText;
    /* JADX INFO: renamed from: bR */
    public static boolean isSidebarOnLeft;
    /* JADX INFO: renamed from: a */
    public static boolean isModernUiLayoutStatic = false;
    /* JADX INFO: renamed from: bO */
    public static boolean showModernSidebar = false;
    /* JADX INFO: renamed from: bP */
    public static boolean showModernActionIcons = false;

    /* JADX INFO: renamed from: cj */
    private int highlightOffsetX;
    /* JADX INFO: renamed from: bQ */
    public static boolean showModernTopBar = false;
    /* JADX INFO: renamed from: ab */
    public final boolean unusedFlag3 = true;
    /* JADX INFO: renamed from: at */
    public final Paint tempTooltipPaint = new Paint();

    /* JADX INFO: renamed from: bW */
    long lastUpdateTime;

    /* JADX INFO: renamed from: bX */
    boolean isFirstUpdate;

    /* JADX INFO: renamed from: ca */
    public static BaseUnit lastSelectedNonBuilderUnit;

    /* JADX INFO: renamed from: ce */
    static boolean selectionChanged;
    /* JADX INFO: renamed from: bD */
    public final Paint textureDrawPaint = new GamePaint();
    /* JADX INFO: renamed from: bv */
    final Rect actionTooltipRect = new Rect();
    /* JADX INFO: renamed from: bw */
    final Rect actionTooltipBorderRect = new Rect();
    /* JADX INFO: renamed from: bx */
    final Rect tempDrawRect = new Rect();

    /* JADX INFO: renamed from: cd */
    static int globalSelectionCounter = 1;
    /* JADX INFO: renamed from: by */
    final Rect tempButtonRect = new Rect();
    /* JADX INFO: renamed from: bz */
    final Rect tempInsetRect = new Rect();
    /* JADX INFO: renamed from: bA */
    final Paint selectionOverlayPaint = new Paint();
    /* JADX INFO: renamed from: bB */
    final Paint unusedPaint1 = new Paint();

    /* JADX INFO: renamed from: l */
    AttackMoveAction attackMoveAction = new AttackMoveAction();

    /* JADX INFO: renamed from: m */
    GuardUnitAction guardUnitAction = new GuardUnitAction();

    /* JADX INFO: renamed from: n */
    PatrolAction patrolAction = new PatrolAction();

    /* JADX INFO: renamed from: o */
    AttackModeAction attackModeAction = new AttackModeAction();

    /* JADX INFO: renamed from: p */
    public PingMapAction pingMapAction = new PingMapAction();

    /* JADX INFO: renamed from: q */
    MapPingToolAction mapPingToolAction = new MapPingToolAction();

    /* JADX INFO: renamed from: r */
    TeamChatToolAction teamChatToolAction = new TeamChatToolAction();

    /* JADX INFO: renamed from: s */
    UIElement rootUIElement = new BasicUIElement();

    /* JADX INFO: renamed from: t */
    boolean isUIInitialized = false;

    /* JADX INFO: renamed from: u */
    public boolean isDraggingSelection = false;

    /* JADX INFO: renamed from: w */
    float uiScale = 0.0f;

    /* JADX INFO: renamed from: x */
    public float selectionBoxStartX = 0.0f;

    /* JADX INFO: renamed from: y */
    public float selectionBoxStartY = 0.0f;

    /* JADX INFO: renamed from: z */
    float selectionBoxMinWidth = 40.0f;

    /* JADX INFO: renamed from: A */
    float selectionBoxMinHeight = 40.0f;

    /* JADX INFO: renamed from: B */
    int lastTouchCount = 0;

    /* JADX INFO: renamed from: H */
    boolean isMouseOverUI = false;

    /* JADX INFO: renamed from: I */
    boolean isMousePressed = false;

    /* JADX INFO: renamed from: J */
    boolean isRightMousePressed = false;

    /* JADX INFO: renamed from: K */
    boolean isMiddleMousePressed = false;

    /* JADX INFO: renamed from: L */
    boolean isKeyboardShiftPressed = false;

    /* JADX INFO: renamed from: M */
    boolean isKeyboardCtrlPressed = false;

    /* JADX INFO: renamed from: N */
    float mouseWorldX = 0.0f;

    /* JADX INFO: renamed from: O */
    float mouseWorldY = 0.0f;

    /* JADX INFO: renamed from: P */
    float mouseScreenX = 0.0f;

    /* JADX INFO: renamed from: Q */
    float mouseScreenY = 0.0f;

    /* JADX INFO: renamed from: R */
    float lastMouseX = 0.0f;

    /* JADX INFO: renamed from: S */
    float lastMouseY = 0.0f;

    /* JADX INFO: renamed from: T */
    boolean isInputDisabled = false;

    /* JADX INFO: renamed from: U */
    boolean isSelectionBoxActive = false;

    /* JADX INFO: renamed from: V */
    boolean isRightClickDrag = false;
    /* JADX INFO: renamed from: bC */
    final Paint unusedPaint2 = new GamePaint();
    /* JADX INFO: renamed from: bE */
    final Paint screenFlashBorderPaint = new GamePaint();

    /* JADX INFO: renamed from: aU */
    public float tooltipX = 0.0f;

    /* JADX INFO: renamed from: aV */
    public float tooltipY = 0.0f;

    /* JADX INFO: renamed from: aW */
    public float tooltipWidth = 0.0f;

    /* JADX INFO: renamed from: aY */
    public float tooltipHeight = 0.0f;
    /* JADX INFO: renamed from: bF */
    final Paint tempStrokePaint = new Paint();
    /* JADX INFO: renamed from: b */
    public boolean showEndGameStatsButton = true;
    /* JADX INFO: renamed from: c */
    public boolean isCompactEndGameUi = false;
    /* JADX INFO: renamed from: d */
    public float endGameFadeIn = 0.0f;
    /* JADX INFO: renamed from: e */
    public boolean unusedFlag2 = false;
    /* JADX INFO: renamed from: bj */
    public Texture iconRallyTexture = null;
    /* JADX INFO: renamed from: bk */
    public Texture iconUpgradeTexture = null;
    /* JADX INFO: renamed from: bl */
    public Texture metalDarkTexture = null;
    /* JADX INFO: renamed from: ba */
    Texture buttonNoTexture = null;
    /* JADX INFO: renamed from: bb */
    Texture buttonYesTexture = null;
    /* JADX INFO: renamed from: bc */
    Texture buttonMoreTexture = null;
    /* JADX INFO: renamed from: bh */
    Texture buttonAddTexture = null;
    /* JADX INFO: renamed from: bi */
    Texture buttonSubtractTexture = null;
    /* JADX INFO: renamed from: bm */
    Texture touchIndicatorTexture = null;
    /* JADX INFO: renamed from: bS */
    UIEvent lastUIEvent = UIEvent.createMouseMove(-1, -1);
    /* JADX INFO: renamed from: cf */
    private int sidebarPanelOffsetY;
    /* JADX INFO: renamed from: cg */
    private int sidebarPanelStartY;
    /* JADX INFO: renamed from: ch */
    private int sidebarPanelIndex;
    /* JADX INFO: renamed from: ci */
    private float sidebarRowHeight;
    /* JADX INFO: renamed from: ck */
    private int sidebarLastPanelWidth;

    /* JADX INFO: renamed from: bM */
    public ArrayList selectedUnits = new ArrayList();

    /* JADX INFO: renamed from: bN */
    public boolean isUILoggingEnabled = false;
    /* JADX INFO: renamed from: cl */
    private int sidebarPanelMaxWidth;

    /* JADX INFO: renamed from: bT */
    StoredResources temporaryResources = new StoredResources();

    /* JADX INFO: renamed from: bU */
    long lastActionConfirmTime = -1;

    /* JADX INFO: renamed from: bV */
    long lastActionCancelTime = -1;

    /* JADX INFO: renamed from: bY */
    public UnitList tempUnitList = new UnitList();

    /* JADX INFO: renamed from: bZ */
    public UnitList selectedUnitsList = new UnitList();

    /* JADX INFO: renamed from: cb */
    Paint debugTextPaint = new Paint();

    /* JADX INFO: renamed from: cc */
    Rect debugTextRect = new Rect();

    /* JADX INFO: renamed from: a */
    public boolean shouldUseCircleSelect() {
        if (GameEngine.isDesktopMouseInput()) {
            return false;
        }
        return GameEngine.getInstance().settingsEngine.useCircleSelect;
    }

    /* JADX INFO: renamed from: b */
    float calculateUIWidth() {
        return Math.min(this.uiScale * 2.5f, 290.0f) + 10.0f;
    }

    /* JADX INFO: renamed from: c */
    float calculateCameraOpacity() {
        GameEngine gameEngine = GameEngine.getInstance();
        float f = 0.7f;
        if (GameEngine.isPC()) {
            f = 0.9f;
        }
        if (gameEngine.zoom < 1.0f) {
            float f2 = gameEngine.zoom;
            if (f2 < 0.4d) {
                f2 = 0.4f;
            }
            f *= f2;
        }
        return f;
    }

    /* JADX INFO: renamed from: a */
    public void showInfoMessageWithPriority(String str, int i) {
        this.interfaceRenderer.setMessage(str, i);
    }

    /* JADX INFO: renamed from: b */
    public void showMessageWithPriority(String str, int i) {
        this.interfaceRenderer.setMessageIfAbsent(str, i);
    }

    /* JADX INFO: renamed from: a */
    public void showInfoMessage(String str) {
        this.interfaceRenderer.isDemoQueuableAction(str);
    }

    /* JADX INFO: renamed from: b */
    public void showMediumPriorityMessage(String str) {
        this.interfaceRenderer.setMessage(str, 100);
    }

    /* JADX INFO: renamed from: c */
    public void showHighPriorityMessage(String str) {
        this.interfaceRenderer.setMessage(str, 50);
    }

    /* JADX INFO: renamed from: d */
    public void showDebugMessage(String str) {
        this.interfaceRenderer.setMessage(str, 5);
    }

    /* JADX INFO: renamed from: d */
    public void resetMouseState() {
        this.isSelectionBoxActive = false;
        this.isRightClickDrag = false;
        this.isMousePressed = false;
    }

    /* JADX INFO: renamed from: a */
    public boolean isWorldClickAllowedAt(float f, float f2) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (!showModernSidebar || this.interfaceRenderer.showInfoText) {
            return f < gameEngine.screenWidth - gameEngine.sidebarWidth;
        }
        if (gameEngine.minimap.screenToWorld(f, f2) != null) {
            return false;
        }
        return true;
    }

    /* JADX INFO: renamed from: e */
    public void initializeLocalizedStrings() {
        if (this.interfaceRenderer != null) {
            this.interfaceRenderer.initializeStrings();
        }
    }

    /* JADX INFO: renamed from: a */
    public void toggleGameAndUIState(boolean z) {
        if (z) {
            this.interfaceRenderer.markUnitGroupMarkersValid();
            return;
        }
        GameEngine gameEngine = GameEngine.getInstance();
        this.interfaceRenderer.clearUnitGroupMarkers();
        clearCurrentAction();
        this.isDraggingSelection = false;
        this.isCompactEndGameUi = false;
        this.endGameFadeIn = 0.0f;
        this.selectedUnits.clear();
        if (!z) {
            gameEngine.gameSpeed = 1.0f;
            gameEngine.isUnitInvincibilityEnabled = false;
            gameEngine.isGameStarted = false;
            gameEngine.isDebugTempMode = false;
            gameEngine.isTriggerDebugMode = false;
        }
        if (gameEngine.isNetworkConnected() && gameEngine.isSinglePlayerGame()) {
            gameEngine.isGameStarted = gameEngine.networkEngine.isSandboxMode;
        }
        LagHidingManager.clearSnapshots();
        notifySelectionChanged();
    }

    /* JADX INFO: renamed from: f */
    public void setupInterfaceFlags() {
        showModernSidebar = false;
        showModernActionIcons = false;
        showModernTopBar = false;
        if (GameEngine.isPC()) {
            showModernSidebar = true;
            showModernActionIcons = true;
            isModernUiLayoutStatic = true;
            showModernTopBar = true;
        }
        if (GameEngine.isGDXVersion) {
            showModernSidebar = true;
            showModernActionIcons = true;
            showModernTopBar = true;
        }
        if (GameEngine.isAndroidPlatform() && !GameEngine.getInstance().settingsEngine.classicInterface) {
            showModernSidebar = true;
            showModernActionIcons = true;
            showModernTopBar = true;
        }
    }

    /* JADX INFO: renamed from: a */
    public void initializeUIResources(Context context) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (GameEngine.isSpaceGame()) {
            this.isUILoggingEnabled = true;
        }
        setupInterfaceFlags();
        this.notAvailableInDemoText = Locale.get("gui.notAvailableInDemoText", new Object[0]);
        this.lockedText = "Locked";
        this.notEnoughResourcesText = LocaleString.wrapLocaleKey("gui.notEnoughResources");
        this.cannotPlaceGeneralText = Locale.get("gui.cannotPlace.general", new Object[0]);
        this.cannotPlaceNeedsResourcePoolText = Locale.get("gui.cannotPlace.needsResourcePool", new Object[0]);
        this.cannotPlaceNeedsWaterText = Locale.get("gui.cannotPlace.needsWater", new Object[0]);
        this.interfaceRenderer = new GameInterfaceRenderer(gameEngine, this);
        initializeLocalizedStrings();
        this.messageManager = new MessageManager(gameEngine, this);
        this.warLogDisplay = new WarLogDisplay(gameEngine);
        this.leaderboard = new Leaderboard(gameEngine, this);
        this.endGameScreen = new EndGameScreen();
        if (GameEngine.isNonPCPlatform()) {
            this.showEndGameStatsButton = true;
        }
        this.buttonNoTexture = gameEngine.renderGraphicsEngine.a(R.drawable.button_no);
        this.buttonYesTexture = gameEngine.renderGraphicsEngine.a(R.drawable.button_yes);
        this.buttonMoreTexture = gameEngine.renderGraphicsEngine.a(R.drawable.button_more);
        this.tooltipBackgroundPaint = new Paint();
        this.tooltipBackgroundPaint.d(true);
        this.tooltipBorderPaint = new Paint();
        this.tooltipBorderPaint.d(true);
        this.tooltipBorderPaint.a(40, 255, 255, 255);
        this.buttonAddTexture = gameEngine.renderGraphicsEngine.a(R.drawable.button_add);
        this.buttonSubtractTexture = gameEngine.renderGraphicsEngine.a(R.drawable.button_subtract);
        this.iconRallyTexture = gameEngine.renderGraphicsEngine.a(R.drawable.icon_rally);
        this.uiTexture1 = gameEngine.renderGraphicsEngine.a(R.drawable.rounded_glow_button);
        this.uiTexture2 = gameEngine.renderGraphicsEngine.a(R.drawable.rounded_white_button);
        this.ninePatchStyle1 = new NinePatchStyle(this.uiTexture1, 32, 27);
        this.ninePatchStyle2 = new NinePatchStyle(gameEngine.renderGraphicsEngine.a(R.drawable.rounded_glow_highlight_button), 32, 27);
        this.ninePatchStyle3 = this.ninePatchStyle1.clone();
        this.ninePatchStyle3.hoverStyle = this.ninePatchStyle2;
        this.ninePatchStyle4 = new NinePatchStyle(gameEngine.renderGraphicsEngine.a(R.drawable.rounded_dark_box), 32, 27);
        this.ninePatchStyle5 = new NinePatchStyle(gameEngine.renderGraphicsEngine.a(R.drawable.rounded_dark_box_titled), 36, 36);
        this.ninePatchStyle5.normalStyle = new NinePatchStyle(gameEngine.renderGraphicsEngine.a(R.drawable.rounded_shadow), 36, 36);
        this.ninePatchStyle5.useScaledBlit = true;
        this.ninePatchStyle6 = new NinePatchStyle(gameEngine.renderGraphicsEngine.a(R.drawable.rounded_green), 36, 36);
        this.ninePatchStyle6.normalStyle = this.ninePatchStyle5.normalStyle;
        this.ninePatchStyle6.paddingSize = 20;
        this.iconUpgradeTexture = gameEngine.renderGraphicsEngine.a(R.drawable.icon_upgrade);
        this.metalDarkTexture = gameEngine.renderGraphicsEngine.a(R.drawable.metal_dark, false);
        this.touchIndicatorTexture = gameEngine.renderGraphicsEngine.a(R.drawable.touch_indicator, false);
        UIStyle.createStyles();
        this.screenFlashBorderPaint.a(145, 0, 175, 0);
        this.screenFlashBorderPaint.a(6.0f);
        GamePaint.b(this.screenFlashBorderPaint);
        this.textureDrawPaint.a(true);
        this.unitHealthBarPaint = new Paint();
        this.unitHealthBarBackgroundPaint = new GamePaint();
        this.unitHealthBarBackgroundPaint.a(255, 0, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE, 0);
        this.unitHealthBarBackgroundPaint.a(true);
        this.unitHealthBarBackgroundPaint.c(true);
        this.unitHealthBarBackgroundPaint.a(Typeface.a(Typeface.c, 1));
        gameEngine.updatePaintTextSize(this.unitHealthBarBackgroundPaint, 20.0f);
        this.unitHealthBarBackgroundPaint.a(Paint.Align.LEFT);
        this.unitSelectionBorderPaint = new GamePaint();
        this.unitSelectionBorderPaint.a(255, 0, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE, 0);
        this.unitSelectionBorderPaint.a(true);
        this.unitSelectionBorderPaint.c(true);
        this.unitSelectionBorderPaint.a(Typeface.a(Typeface.c, 1));
        gameEngine.updatePaintTextSize(this.unitSelectionBorderPaint, 18.0f);
        this.unitSelectionBorderPaint.a(Paint.Align.LEFT);
        this.unitShieldBarPaint = new GamePaint();
        this.unitShieldBarPaint.a(this.unitHealthBarBackgroundPaint);
        this.unitShieldBarPaint.a(255, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE, 0);
        this.unitSelectionPaint = new GamePaint();
        this.unitSelectionPaint.b(Color.a(100, 0, 0, 0));
        this.unitSelectionPaint.a(Paint.Style.FILL_AND_STROKE);
        this.selectionBoxPaint = new GamePaint();
        this.selectionBoxPaint.a(100, 30, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE, 30);
        this.selectionBoxPaint.a(Paint.Align.LEFT);
        this.selectionBoxPaint.c(true);
        this.selectionBoxPaint.a(true);
        gameEngine.updatePaintTextSize(this.selectionBoxPaint, 12.0f);
        GamePaint.b(this.selectionBoxPaint);
        this.buildingPreviewPaint = new GamePaint();
        if (this.isUILoggingEnabled) {
            this.buildingPreviewPaint.a(255, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE);
        } else {
            this.buildingPreviewPaint.a(255, 30, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE, 30);
        }
        this.buildingPreviewPaint.a(Paint.Align.CENTER);
        this.buildingPreviewPaint.c(true);
        this.buildingPreviewPaint.a(true);
        gameEngine.updatePaintTextSize(this.buildingPreviewPaint, 12.0f);
        GamePaint.b(this.buildingPreviewPaint);
        GameEngine.log("smallTextPaint size: " + this.buildingPreviewPaint.k());
        this.rallyPointPaint = new GamePaint();
        this.rallyPointPaint.a(this.buildingPreviewPaint);
        gameEngine.updatePaintTextSize(this.rallyPointPaint, 10.0f);
        GamePaint.b(this.rallyPointPaint);
        this.selectionBoxBorderPaint = new GamePaint();
        this.selectionBoxBorderPaint.a(this.buildingPreviewPaint);
        gameEngine.updatePaintTextSize(this.selectionBoxBorderPaint, 8.0f);
        GamePaint.b(this.selectionBoxBorderPaint);
        this.buildingPreviewInvalidPaint = new GamePaint();
        if (this.isUILoggingEnabled) {
            this.buildingPreviewInvalidPaint.a(255, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE);
        } else {
            this.buildingPreviewInvalidPaint.a(255, 30, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE, 30);
        }
        this.buildingPreviewInvalidPaint.a(Paint.Align.CENTER);
        this.buildingPreviewInvalidPaint.c(true);
        this.buildingPreviewInvalidPaint.a(true);
        gameEngine.updatePaintTextSize(this.buildingPreviewInvalidPaint, 20.0f);
        GamePaint.b(this.buildingPreviewInvalidPaint);
        this.unitRangePaint = new GamePaint();
        this.unitRangePaint.a(255, 30, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE, 30);
        this.unitRangePaint.a(Paint.Align.CENTER);
        this.unitRangePaint.c(true);
        this.unitRangePaint.a(true);
        gameEngine.updatePaintTextSize(this.unitRangePaint, 20.0f);
        GamePaint.b(this.unitRangePaint);
        this.unitTargetLinePaint = new GamePaint();
        this.unitTargetLinePaint.a(150, 20, 20, 20);
        gameEngine.updatePaint(this.unitTargetLinePaint);
        GamePaint.b(this.unitTargetLinePaint);
        this.unitRangeBorderPaint = new GamePaint();
        this.unitRangeBorderPaint.a(this.buildingPreviewInvalidPaint);
        this.unitRangeBorderPaint.a(255, 128, 0, 0);
        gameEngine.updatePaintTextSize(this.unitRangeBorderPaint, 14.0f);
        this.unitRangeBorderPaint.a(Paint.Align.CENTER);
        GamePaint.b(this.unitRangeBorderPaint);
        this.unitPathPaint = new GamePaint();
        this.unitPathPaint.a(this.unitRangeBorderPaint);
        this.unitPathPaint.a(255, 220, 222, 49);
        this.unitPathBorderPaint = new GamePaint();
        this.unitPathBorderPaint.a(this.buildingPreviewInvalidPaint);
        gameEngine.updatePaintTextSize(this.unitPathBorderPaint, 12.0f);
        this.unitPathBorderPaint.a(125, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_DATA_SERVICE, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_DATA_SERVICE, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_DATA_SERVICE);
        this.unitPathBorderPaint.a(Paint.Align.CENTER);
        GamePaint.b(this.unitPathBorderPaint);
        this.unitIconPaint = new GamePaint();
        this.unitIconPaint.b(-16777216);
        this.unitIconPaint.a(true);
        this.unitIconPaint.c(true);
        this.unitIconPaint.a(Typeface.a(Typeface.c, 0));
        gameEngine.updatePaintTextSize(this.unitIconPaint, 14.0f);
        this.buildingIconPaint = new GamePaint();
        this.buildingIconPaint.a(this.unitIconPaint);
        this.buildingIconPaint.a(Typeface.a(Typeface.c, 1));
        gameEngine.updatePaintTextSize(this.buildingIconPaint, 16.0f);
        this.effectIconPaint = new GamePaint();
        this.effectIconPaint.a(this.buildingIconPaint);
        this.effectIconPaint.b(Color.a(SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_RADIO_SERVICE, 63, 80));
        gameEngine.updatePaintTextSize(this.effectIconPaint, 16.0f);
        this.fogOfWarPaint = new GamePaint();
        this.fogOfWarPaint.b(-16777216);
        this.fogOfWarPaint.a(Paint.Align.CENTER);
        this.fogOfWarPaint.a(true);
        this.fogOfWarPaint.c(true);
        this.fogOfWarPaint.a(Typeface.a(Typeface.c, 0));
        gameEngine.updatePaintTextSize(this.fogOfWarPaint, 20.0f);
        this.minimapPaint = new GamePaint();
        this.minimapPaint.b(-1);
        this.minimapPaint.c(160);
        if (GameEngine.isPC()) {
            this.minimapPaint.c(140);
        }
        gameEngine.updatePaint(this.minimapPaint);
        this.minimapBorderPaint = new GamePaint();
        this.minimapBorderPaint.b(-16777216);
        this.minimapBorderPaint.c(210);
        gameEngine.updatePaint(this.minimapBorderPaint);
        this.minimapViewportBorderPaint = new GamePaint();
        this.minimapViewportBorderPaint.b(-7829368);
        this.minimapViewportBorderPaint.c(SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE);
        this.minimapViewportBorderPaint.a(Paint.Style.STROKE);
        this.minimapViewportBorderPaint.a(1.0f);
        gameEngine.updatePaint(this.minimapViewportBorderPaint);
        this.minimapUnitPaint = new GamePaint();
        this.minimapUnitPaint.b(-16711936);
        this.minimapUnitPaint.c(80);
        this.minimapUnitPaint.a(Paint.Style.FILL);
        this.minimapUnitPaint.a(4.0f);
        gameEngine.updatePaint(this.minimapUnitPaint);
        this.minimapViewportPaint = new GamePaint();
        this.minimapViewportPaint.b(Color.a(120, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_TERRESTRIAL_ANALOG, 167, 49));
        this.minimapViewportPaint.a(Paint.Style.FILL);
        this.minimapViewportPaint.a(8.0f);
        gameEngine.updatePaint(this.minimapViewportPaint);
        this.unitTargetLineBorderPaint = new GamePaint();
        this.unitTargetLineBorderPaint.c(true);
        this.unitTargetLineBorderPaint.a(true);
        gameEngine.updatePaintTextSize(this.unitTargetLineBorderPaint, 12.0f);
        GamePaint.b(this.unitTargetLineBorderPaint);
    }

    /* JADX INFO: renamed from: g */
    public void clearMessages() {
        this.messageManager.clear();
        this.warLogDisplay.clearEntries();
        this.isFirstUpdate = false;
    }

    /* JADX INFO: renamed from: a */
    public void updateInput(float f) {
        GameEngine gameEngine = GameEngine.getInstance();
        this.gameTime += (double) f;
        this.tooltipX = Utility.moveTowardsZero(this.tooltipX, f);
        this.tooltipY = Utility.moveTowardsZero(this.tooltipY, f);
        this.tooltipHeight = Utility.moveTowardsZero(this.tooltipHeight, 0.08f * f);
        this.tooltipWidth = Utility.moveTowardsZero(this.tooltipWidth, f);
        this.touchStartTime += f;
        this.tooltipDelay += 0.05f * f;
        if (this.tooltipDelay > 1.0f) {
            this.tooltipDelay -= 1.0f;
            if (this.tooltipDelay > 1.0f) {
                this.tooltipDelay = 0.0f;
            }
        }
        gameEngine.unitSelectionFadeBase = 4.0f * this.tooltipHeight;
        float f2 = 1.0f * f;
        if (!this.isMousePressed) {
            float f3 = this.lastMouseX * f;
            float f4 = this.lastMouseY * f;
            float fMin = Utility.min(80.0f, f3);
            float fMin2 = Utility.min(80.0f, f4);
            gameEngine.viewpointX += fMin;
            gameEngine.viewpointY += fMin2;
        } else {
            f2 *= 4.0f;
        }
        float fDistance = Utility.distance(0.0f, 0.0f, this.lastMouseX, this.lastMouseY);
        float angleBetweenPoints = Utility.getAngleBetweenPoints(0.0f, 0.0f, this.lastMouseX, this.lastMouseY);
        if (fDistance > 30.0f) {
            fDistance = 30.0f;
        }
        float fMoveTowardsZero = Utility.moveTowardsZero(fDistance, f2);
        this.lastMouseX = Utility.fastCos(angleBetweenPoints) * fMoveTowardsZero;
        this.lastMouseY = Utility.fastSin(angleBetweenPoints) * fMoveTowardsZero;
        this.showDebugInfo = false;
        this.isMousePressed = gameEngine.isTouchDown() && gameEngine.touchPointerEnabled[0] && this.tooltipX == 0.0f;
        if (this.tooltipY != 0.0f) {
            if (!this.isMousePressed) {
                this.tooltipY = 0.0f;
            }
            this.isMousePressed = false;
            this.isMouseOverUI = false;
        }
        boolean z = false;
        if (this.tooltipWidth > 0.0f) {
            z = true;
        }
        if (gameEngine.isTouchDown() && gameEngine.getTouchPointerCount() > 1) {
            z = true;
            this.tooltipWidth = 4.0f;
        }
        if (z) {
            this.isMousePressed = false;
            this.isMouseOverUI = false;
            this.isInputDisabled = false;
            this.uiScale = 0.0f;
        }
        this.isKeyboardCtrlPressed = false;
        this.isKeyboardShiftPressed = gameEngine.getTouchX() > gameEngine.currentScreenWidthPixels;
        this.isSelectionBoxActive = !this.isMousePressed && this.isMouseOverUI;
        this.isRightClickDrag = this.isMousePressed && !this.isMouseOverUI;
        if (GameEngine.isPC() && gameEngine.settingsEngine.mouseSupport) {
            this.selectionBoxMinWidth = gameEngine.getTouchX();
            this.selectionBoxMinHeight = gameEngine.getTouchY();
        }
        if (!this.isMousePressed && !this.isSelectionBoxActive) {
            this.isDoubleClick = false;
        }
        if (this.isMousePressed) {
            this.uiScale += f;
            this.selectionBoxStartX = gameEngine.getTouchX(0);
            this.selectionBoxStartY = gameEngine.getTouchY(0);
            this.selectionBoxMinWidth = this.selectionBoxStartX;
            this.selectionBoxMinHeight = this.selectionBoxStartY;
            this.lastTouchCount = gameEngine.getTouchPointerId(0);
            this.isGamePaused = isWorldClickAllowedAt(this.selectionBoxStartX, this.selectionBoxStartY);
            boolean z2 = false;
            if (this.isGamePaused && !this.isMouseOverUI) {
                if (this.touchStartTime < 30.0f) {
                    float fDistanceSq = Utility.distanceSq(this.touchX, this.touchY, this.selectionBoxStartX, this.selectionBoxStartY);
                    float f5 = 10.0f * gameEngine.screenScale;
                    if (GameEngine.isNonPCPlatform()) {
                        f5 = (float) (((double) f5) * 1.5d);
                    }
                    if (fDistanceSq < f5 * f5) {
                        z2 = true;
                    }
                }
                this.touchStartTime = 0.0f;
                this.touchX = this.selectionBoxStartX;
                this.touchY = this.selectionBoxStartY;
            }
            if (z2) {
                this.isDoubleClick = true;
            }
            if (!this.isMouseOverUI) {
                this.isInputDisabled = false;
                this.mouseWorldX = this.selectionBoxStartX;
                this.mouseWorldY = this.selectionBoxStartY;
                this.mouseScreenX = this.selectionBoxStartX;
                this.mouseScreenY = this.selectionBoxStartY;
                this.isRightMousePressed = gameEngine.minimap.screenToWorld(this.selectionBoxStartX, this.selectionBoxStartY) != null;
                this.isMiddleMousePressed = false;
                if (!this.isRightMousePressed) {
                    this.isMiddleMousePressed = this.selectionBoxStartX > gameEngine.currentScreenWidthPixels;
                }
            }
            this.isMouseOverUI = true;
        }
        if (this.isMousePressed && (this.uiScale <= 20.0f || !shouldUseCircleSelect())) {
            float fDistanceSq2 = Utility.distanceSq(this.mouseWorldX, this.mouseWorldY, this.selectionBoxStartX, this.selectionBoxStartY);
            if (!this.isRightMousePressed) {
                float f6 = 30.0f * gameEngine.screenScale;
                if (GameEngine.isPC() && gameEngine.settingsEngine.mouseSupport && gameEngine.isMouseButtonPressed(3)) {
                    f6 = 0.0f;
                }
                if (!this.isInputDisabled && fDistanceSq2 > f6 * f6) {
                    boolean z3 = false;
                    int i = 1;
                    if (gameEngine.settingsEngine.mouseOrders == 2) {
                        i = 2;
                    }
                    if ((!gameEngine.settingsEngine.mouseSupport || this.lastTouchCount != i) && !isKeyboardSupportAndGraphicsEnabled(gameEngine)) {
                        z3 = true;
                    }
                    if (z3) {
                        this.isInputDisabled = true;
                    }
                    this.mouseScreenX = this.selectionBoxStartX;
                    this.mouseScreenY = this.selectionBoxStartY;
                }
            }
        }
        if (GameEngine.isPC() && !gameEngine.isStopped && gameEngine.activeGameView != null && ((gameEngine.activeGameView.isRendering() || GameEngine.isMouseCaptured) && (!this.isMousePressed || this.interfaceRenderer.isDragging))) {
            float f7 = (24.0f * gameEngine.settingsEngine.edgeScrollSpeed) / gameEngine.zoom;
            float f8 = gameEngine.viewpointX;
            float f9 = gameEngine.viewpointY;
            float f10 = 0.0f;
            float f11 = 0.0f;
            if (this.selectionBoxMinWidth <= 1.0f) {
                f10 = 0.0f - (f7 * f);
            }
            if (this.selectionBoxMinWidth >= gameEngine.screenWidth - 1.0f) {
                f10 += f7 * f;
            }
            if (this.selectionBoxMinHeight <= 1.0f) {
                f11 = 0.0f - (f7 * f);
            }
            if (this.selectionBoxMinHeight >= gameEngine.screenHeight - 1.0f) {
                f11 += f7 * f;
            }
            gameEngine.viewpointX += f10;
            gameEngine.viewpointY += f11;
            gameEngine.clampCameraPosition();
            this.interfaceRenderer.selectionRectF2.a -= (gameEngine.viewpointX - f8) * gameEngine.zoom;
            this.interfaceRenderer.selectionRectF2.b -= (gameEngine.viewpointY - f9) * gameEngine.zoom;
        }
        InputController inputController = gameEngine.inputController;
        if (gameEngine.settingsEngine.keyboardSupport) {
            if (gameEngine.isKeyboardCameraScrollAllowed()) {
                float f12 = 12.0f * gameEngine.settingsEngine.scrollSpeed;
                if (inputController.p.b()) {
                    gameEngine.viewpointX -= f12 * f;
                }
                if (inputController.q.b()) {
                    gameEngine.viewpointX += f12 * f;
                }
                if (inputController.n.b()) {
                    gameEngine.viewpointY -= f12 * f;
                }
                if (inputController.o.b()) {
                    gameEngine.viewpointY += f12 * f;
                }
                if (inputController.r.b()) {
                    gameEngine.targetZoom += 0.1f;
                }
                if (inputController.s.b()) {
                    gameEngine.targetZoom -= 0.1f;
                }
            }
            if (inputController.y.a()) {
                clearCurrentAction();
                clearSelection();
            }
            if (inputController.z.a()) {
                this.warLogDisplay.jumpToNextUnshownEntry();
            }
            if (inputController.A.a()) {
                clearCurrentAction();
                clearSelection();
                for (GameObject gameObject : GameObject.fastGameObjectList) {
                    if (gameObject instanceof OrderableUnit) {
                        OrderableUnit orderableUnit = (OrderableUnit) gameObject;
                        if (!orderableUnit.isDead && orderableUnit.team == gameEngine.playerTeam && orderableUnit.canAttack() && !orderableUnit.canMove() && orderableUnit.aS() && orderableUnit.transportContainer == null) {
                            selectUnit(orderableUnit);
                        }
                    }
                }
            }
            if (inputController.B.a()) {
                clearCurrentAction();
                clearSelection();
                for (GameObject gameObject2 : GameObject.fastGameObjectList) {
                    if (gameObject2 instanceof OrderableUnit) {
                        OrderableUnit orderableUnit2 = (OrderableUnit) gameObject2;
                        if (orderableUnit2.team == gameEngine.playerTeam && orderableUnit2.r() == UnitTypeEnum.commandCenter && !orderableUnit2.isDead && orderableUnit2.transportContainer == null) {
                            selectUnit(orderableUnit2);
                            gameEngine.centerViewpoint(orderableUnit2.posX, orderableUnit2.posY);
                        }
                    }
                }
            }
            if (inputController.C.a()) {
                UnitSelectionFilter.selectMatchingUnits(this.selectedUnits, UnitSelectionFilter.attackUnits, UnitSelectionFilter.attackUnitsIgnoreWaypoints);
            }
            if (inputController.D.a()) {
                UnitSelectionFilter.selectMatchingUnits(this.selectedUnits, UnitSelectionFilter.buildingUnits, null);
            }
            if (inputController.E.a()) {
                UnitSelectionFilter.selectMatchingUnits(this.selectedUnits, UnitSelectionFilter.fabricatorUnits, null);
            }
            if (inputController.F.a()) {
                UnitSelectionFilter.selectMatchingUnits(this.selectedUnits, UnitSelectionFilter.landFactoryUnits, null);
            }
            if (inputController.G.a()) {
                UnitSelectionFilter.selectMatchingUnits(this.selectedUnits, UnitSelectionFilter.airFactoryUnits, null);
            }
            if (inputController.x.a()) {
                this.interfaceRenderer.a(12);
            }
            if (inputController.N.a() && hasCombatUnitsSelected() && hasMovableUnitsSelected()) {
                clearCurrentAction();
                this.selectedBuilder = null;
                this.currentAction = this.attackMoveAction;
                return;
            }
            if (inputController.P.a() && hasUpgradedUnitsSelected()) {
                clearCurrentAction();
                this.selectedBuilder = null;
                this.currentAction = this.guardUnitAction;
                return;
            }
            if (inputController.Q.a() && hasMovableUnitsSelected()) {
                clearCurrentAction();
                this.selectedBuilder = null;
                this.currentAction = this.patrolAction;
                return;
            }
            if (inputController.O.a()) {
                stopSelectedUnits();
            }
            if (inputController.v.a()) {
                activatePingMapMode();
            }
            if (inputController.t.a() && gameEngine.isNetworkConnected()) {
                GameEngine.log("showing send chat");
                this.interfaceRenderer.a(13);
            }
            if (inputController.u.a() && gameEngine.isNetworkConnected()) {
                GameEngine.log("showing send team chat");
                this.interfaceRenderer.a(16);
            }
            if (gameEngine.isSinglePlayerGame() || gameEngine.replayEngine.j()) {
                if (inputController.L.a()) {
                    if (gameEngine.gameSpeed != 0.0f) {
                        if (!gameEngine.replayEngine.j()) {
                            NetworkEngine.a((String) null, "Game paused");
                        }
                        gameEngine.gameSpeed = 0.0f;
                    } else {
                        gameEngine.gameSpeed = 1.0f;
                    }
                }
                boolean zA = inputController.J.a();
                boolean zA2 = inputController.K.a();
                if (zA || zA2) {
                    if (zA) {
                        boolean z4 = gameEngine.gameSpeed > 1.0f;
                        if (gameEngine.gameSpeed < 2.0f) {
                            gameEngine.gameSpeed = (float) (((double) gameEngine.gameSpeed) - 0.25d);
                        } else if (gameEngine.gameSpeed < 6.0f) {
                            gameEngine.gameSpeed = (float) (((double) gameEngine.gameSpeed) - 0.5d);
                        } else if (gameEngine.gameSpeed < 16.0f) {
                            gameEngine.gameSpeed -= 2.0f;
                        } else {
                            gameEngine.gameSpeed -= 4.0f;
                        }
                        if (gameEngine.gameSpeed < 0.0f) {
                            gameEngine.gameSpeed = 0.0f;
                        }
                        if (z4 && gameEngine.gameSpeed < 1.0f) {
                            gameEngine.gameSpeed = 1.0f;
                        }
                    } else if (zA2) {
                        boolean z5 = gameEngine.gameSpeed < 1.0f;
                        if (gameEngine.gameSpeed < 2.0f) {
                            gameEngine.gameSpeed = (float) (((double) gameEngine.gameSpeed) + 0.25d);
                        } else if (gameEngine.gameSpeed < 6.0f) {
                            gameEngine.gameSpeed = (float) (((double) gameEngine.gameSpeed) + 0.5d);
                        } else if (gameEngine.gameSpeed < 16.0f) {
                            gameEngine.gameSpeed += 2.0f;
                        } else {
                            gameEngine.gameSpeed += 4.0f;
                        }
                        if (gameEngine.replayEngine.j()) {
                            if (gameEngine.gameSpeed > 64.0f) {
                                gameEngine.gameSpeed = 64.0f;
                            }
                        } else if (gameEngine.gameSpeed > 5.0f) {
                            gameEngine.gameSpeed = 5.0f;
                        }
                        if (z5 && gameEngine.gameSpeed > 1.0f) {
                            gameEngine.gameSpeed = 1.0f;
                        }
                    }
                    if (!gameEngine.replayEngine.j()) {
                        NetworkEngine.a((String) null, "Game speed now: " + gameEngine.gameSpeed);
                    }
                }
            } else if (inputController.L.a() && gameEngine.networkEngine.isServer && gameEngine.networkEngine.gameHasBeenStarted) {
                gameEngine.networkEngine.setGamePaused(!gameEngine.networkEngine.gamePaused);
            }
            gameEngine.pauseTransition = Utility.moveTowardsZero(gameEngine.pauseTransition, f);
            if (inputController.Y.a()) {
                gameEngine.pauseTransition = 180.0f;
            }
            if (gameEngine.isGameStarted && inputController.ab.a()) {
                gameEngine.isDebugTempMode = !gameEngine.isDebugTempMode;
                GameEngine.log("debugTempMode now: " + gameEngine.isDebugTempMode);
                showMediumPriorityMessage("debug: " + gameEngine.isDebugTempMode);
            }
            if (gameEngine.isGameStarted && gameEngine.isDebugTempMode && inputController.ac.a()) {
                AIController.unitCountsUpdated = !AIController.unitCountsUpdated;
                showMediumPriorityMessage("AI debug view: " + AIController.unitCountsUpdated);
            }
            if (gameEngine.isGameStarted && gameEngine.isDebugTempMode && inputController.ad.a()) {
                MissionEngine.a = !MissionEngine.a;
                showMediumPriorityMessage("Map debug: " + MissionEngine.a);
            }
            if (gameEngine.isSinglePlayerGame() || gameEngine.replayEngine.j()) {
                if (gameEngine.isGameStarted) {
                    if (inputController.V.a()) {
                        gameEngine.isShowingDialog = !gameEngine.isShowingDialog;
                    }
                    if (inputController.W.a()) {
                        if (gameEngine.gameSpeed == 1.0f) {
                            gameEngine.gameSpeed = 0.1f;
                        } else {
                            gameEngine.gameSpeed = 1.0f;
                        }
                    }
                    if (inputController.X.a()) {
                        GameEngine.log("Adding test popup");
                        gameEngine.networkEngine.showReconnectDialog();
                    }
                    if (inputController.Z.a()) {
                        gameEngine.isUnitInvincibilityEnabled = !gameEngine.isUnitInvincibilityEnabled;
                    }
                    if (inputController.aa.a()) {
                        for (GameObject gameObject3 : GameObject.fastGameObjectList) {
                            if (gameObject3 instanceof OrderableUnit) {
                                OrderableUnit orderableUnit3 = (OrderableUnit) gameObject3;
                                if (orderableUnit3.isSelected) {
                                    orderableUnit3.U();
                                }
                            }
                        }
                    }
                }
                if (inputController.U.a()) {
                    gameEngine.isGameStarted = !gameEngine.isGameStarted;
                    if (gameEngine.isGameStarted) {
                        clearSelection();
                    }
                }
            }
        }
        if (gameEngine.isGameStarted && !gameEngine.isSinglePlayerGame() && !gameEngine.replayEngine.j()) {
            gameEngine.isGameStarted = false;
        }
        if (gameEngine.isGameStarted) {
            if (this.editorOrBuilder != null && (this.editorOrBuilder.isDestroyed || this.editorOrBuilder.isDead)) {
                this.editorOrBuilder = null;
            }
            if (this.editorOrBuilder == null) {
                GameEngine.log("Creating new debug editor");
                this.editorOrBuilder = new EditorOrBuilder(false);
                this.editorOrBuilder.setUnitTeam(gameEngine.playerTeam);
            }
            if (getSelectedUnitCount() == 0) {
                clearSelection();
                selectUnit(this.editorOrBuilder);
            }
            if (gameEngine.settingsEngine.liveReloading && gameEngine.currentTick % 100 == 0 && !gameEngine.replayEngine.i()) {
                CustomUnitConfigParser.reloadChangedUnitConfigs();
            }
        } else {
            if (this.editorOrBuilder != null && (this.editorOrBuilder.isDestroyed || this.editorOrBuilder.isDead)) {
                this.editorOrBuilder = null;
            }
            if (this.editorOrBuilder != null && !gameEngine.replayEngine.j()) {
                clearEditorOrBuilder();
            }
        }
        if (this.isInputDisabled) {
            if (this.isMiddleMousePressed) {
                this.interfaceRenderer.infoTextAlpha = this.mouseScreenY - this.selectionBoxStartY;
            } else {
                int i2 = 1;
                if (gameEngine.settingsEngine.mouseOrders == 2) {
                    i2 = 2;
                }
                if ((!gameEngine.settingsEngine.mouseSupport || this.lastTouchCount != i2) && !isKeyboardSupportAndGraphicsEnabled(gameEngine)) {
                    SettingsEngine settingsEngine = gameEngine.settingsEngine;
                    double d = this.mouseScreenX - this.selectionBoxStartX;
                    double d2 = this.mouseScreenY - this.selectionBoxStartY;
                    float fDistance2 = Utility.distance(0.0f, 0.0f, (float) d, (float) d2);
                    double d3 = (d * ((double) settingsEngine.scrollSpeed)) / ((double) gameEngine.zoom);
                    double d4 = (d2 * ((double) settingsEngine.scrollSpeed)) / ((double) gameEngine.zoom);
                    if (f != 0.0f && fDistance2 > 50.0d * ((double) f)) {
                        float f13 = 0.7f;
                        if (GameEngine.isPC()) {
                            f13 = 1.7f;
                        }
                        this.lastMouseX = (float) (d3 * ((double) f13));
                        this.lastMouseY = (float) (d4 * ((double) f13));
                    }
                    gameEngine.viewpointX = (float) (((double) gameEngine.viewpointX) + (d3 * 2.0d));
                    gameEngine.viewpointY = (float) (((double) gameEngine.viewpointY) + (d4 * 2.0d));
                }
            }
            this.mouseScreenX = this.selectionBoxStartX;
            this.mouseScreenY = this.selectionBoxStartY;
        }
        if (gameEngine.settingsEngine.mouseSupport && (this.lastUIEvent.x != ((int) gameEngine.getTouchX()) || this.lastUIEvent.y != ((int) gameEngine.getTouchY()))) {
            this.lastUIEvent.x = (int) gameEngine.getTouchX();
            this.lastUIEvent.y = (int) gameEngine.getTouchY();
            this.rootUIElement.dispatchEvent(this.lastUIEvent);
        }
        if (this.isSelectionBoxActive && isInputEnabled()) {
            this.rootUIElement.dispatchEvent(UIEvent.createMouseClick((int) this.selectionBoxMinWidth, (int) this.selectionBoxMinHeight));
        }
        this.rootUIElement.update(f);
        this.endGameScreen.update(f);
    }

    /* JADX INFO: renamed from: h */
    public void clearEditorOrBuilder() {
        if (this.editorOrBuilder != null) {
            deselectUnit(this.editorOrBuilder);
            this.editorOrBuilder.removeFromGame();
            this.editorOrBuilder = null;
        }
    }

    /* JADX INFO: renamed from: i */
    public EditorOrBuilder getEditorOrBuilder() {
        return this.editorOrBuilder;
    }

    /* JADX INFO: renamed from: a */
    public void setEditorOrBuilder(EditorOrBuilder editorOrBuilder) {
        this.editorOrBuilder = editorOrBuilder;
    }

    /* JADX INFO: renamed from: a */
    public boolean isShiftKeyPressed(GameEngine gameEngine) {
        if (!gameEngine.settingsEngine.keyboardSupport) {
            return false;
        }
        return gameEngine.isAnyKeyPressed(59, 60);
    }

    /* JADX INFO: renamed from: b */
    public boolean isControlKeyPressed(GameEngine gameEngine) {
        if (!gameEngine.settingsEngine.keyboardSupport) {
            return false;
        }
        return gameEngine.isAnyKeyPressed(113, 114);
    }

    /* JADX INFO: renamed from: c */
    public boolean isKeyboardSupportAndGraphicsEnabled(GameEngine gameEngine) {
        if (!gameEngine.settingsEngine.keyboardSupport) {
            return false;
        }
        return gameEngine.isAnyKeyPressed(57, 58);
    }

    /* JADX INFO: renamed from: b */
    public void processTouchInput(float f) {
        GameEngine gameEngine = GameEngine.getInstance();
        this.selectionBoxStartTime += 0.2f * f;
        if (this.selectionBoxStartTime > 360.0f) {
            this.selectionBoxStartTime -= 360.0f;
        }
        this.tempDrawRect.a((int) (gameEngine.screenWidth - gameEngine.sidebarWidth), 0, (int) gameEngine.screenWidth, (int) gameEngine.screenHeight);
        if (!showModernSidebar) {
            if (this.isUILoggingEnabled) {
                this.selectionOverlayPaint.a();
                this.selectionOverlayPaint.b(Color.a(255, 33, 40, 52));
                this.selectionOverlayPaint.a(Paint.Style.FILL);
                gameEngine.renderGraphicsEngine.b(this.tempDrawRect, this.selectionOverlayPaint);
            } else {
                gameEngine.renderGraphicsEngine.a(this.metalDarkTexture, this.tempDrawRect, (Paint) null);
            }
            this.selectionOverlayPaint.a();
            this.selectionOverlayPaint.b(Color.a(255, 0, 0, 0));
            this.selectionOverlayPaint.a(Paint.Style.STROKE);
            gameEngine.renderGraphicsEngine.b(this.tempDrawRect, this.selectionOverlayPaint);
        }
        this.sidebarPanelOffsetY = 0;
        this.sidebarPanelIndex = 0;
        this.sidebarPanelStartY = 0;
        this.sidebarLastPanelWidth = this.sidebarPanelMaxWidth;
        this.sidebarPanelMaxWidth = 0;
        if (gameEngine.replayEngine.j() || (gameEngine.playerTeam != null && gameEngine.playerTeam.isSpectatorTeamColor())) {
            OrderableUnit firstSelectedUnit = getFirstSelectedUnit();
            if (firstSelectedUnit != null) {
                drawTeamResources(gameEngine, firstSelectedUnit.team, false, true);
            }
        } else {
            OrderableUnit firstControllableSelectedUnit = getFirstControllableSelectedUnit();
            if (gameEngine.playerTeam != null && gameEngine.playerTeam != PlayerTeam.TEAM_ALL && !gameEngine.playerTeam.isSpectatorTeamColor() && !gameEngine.replayEngine.j()) {
                drawTeamResources(gameEngine, gameEngine.playerTeam, false, true);
            }
            if (firstControllableSelectedUnit != null && gameEngine.playerTeam != firstControllableSelectedUnit.team && canControlUnit(firstControllableSelectedUnit)) {
                drawTeamResources(gameEngine, firstControllableSelectedUnit.team, true, true);
            }
        }
        if (gameEngine.isGameStarted && !gameEngine.replayEngine.j()) {
            String str = VariableScope.nullOrMissingString;
            if (gameEngine.isGameStarted) {
                str = str + "Editor Active\n";
            }
            if (gameEngine.gameSpeed != 1.0f) {
                str = str + "Game Speed: " + gameEngine.gameSpeed + "x\n";
            }
            if (gameEngine.isUnitInvincibilityEnabled) {
                str = str + "Invincible Units\n";
            }
            boolean z = false;
            for (PlayerTeam playerTeam : PlayerTeam.getTeams()) {
                if (playerTeam instanceof AIController) {
                    z = ((AIController) playerTeam).aiUnitManagementTimer > 0.0f;
                }
            }
            if (z) {
                str = str + "AIs frozen\n";
            }
            this.selectionOverlayPaint.a();
            this.selectionOverlayPaint.b(Color.a(0, 0, 0, 0));
            this.selectionOverlayPaint.a(Paint.Style.FILL);
            float f2 = 70.0f * gameEngine.screenScale;
            float f3 = 40.0f;
            if (gameEngine.screenWidth < 600.0f && gameEngine.screenHeight > 650.0f) {
                f2 = 10.0f;
                f3 = 60.0f * gameEngine.screenScale;
            }
            gameEngine.renderGraphicsEngine.a(str, f2, f3, this.unitSelectionBorderPaint, this.selectionOverlayPaint, 6.0f);
        }
        emptyGameEngineCall();
        this.rootUIElement.render();
    }

    /* JADX INFO: renamed from: j */
    public void emptyGameEngineCall() {
        GameEngine.getInstance();
    }

    /* JADX INFO: renamed from: a */
    public void drawTeamResources(GameEngine gameEngine, PlayerTeam playerTeam, boolean isAily, boolean showDetails) {
        if (playerTeam.isTeamControlledByAI) {
            highlightRect(gameEngine, playerTeam, isAily, CreditsResource.D, playerTeam.getDisplayedCreditsTotal(), (StoredResources) null, 0, (Resource) null);
        }
        if (showDetails) {
            this.temporaryResources.g(playerTeam.getDisplayedCustomResources());
            for (Resource resource : Resource.f()) {
                if (resource.d() && (resource.p || resource.j)) {
                    this.temporaryResources.c(resource);
                }
            }
            this.temporaryResources.e();
            drawRect(gameEngine, playerTeam, isAily, this.temporaryResources);
        }
    }

    /* JADX INFO: renamed from: a */
    public void drawRect(GameEngine gameEngine, PlayerTeam playerTeam, boolean z, StoredResources storedResources) {
        for (StoredResourceEntry storedResourceEntry : storedResources.b) {
            if (!storedResourceEntry.a.a()) {
                highlightRect(gameEngine, playerTeam, z, storedResourceEntry.a, storedResourceEntry.b, storedResources, 0, (Resource) null);
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public boolean highlightRect(final GameEngine l, final PlayerTeam n, final boolean boolean3, final Resource a4, final double double5, final StoredResources f, final int integer, final Resource a8) {
        if (integer == 0) {
            this.highlightOffsetX = 0;
        }
        boolean b = false;
        if (integer < 6 && f != null) {
            final Resource i = a4.i;
            if (i != null && (a4.j || double5 != 0.0) && this.highlightRect(l, n, boolean3, i, f.a(i), f, integer + 1, a4)) {
                b = true;
            }
        }
        if ((double5 == 0.0 && !a4.p) || (integer == 0 && !a4.l)) {
            return b;
        }
        final int n2 = 6;
        String string = a4.a(double5, true);
        final int b2 = n.b(a4);
        final int resourceDrainRate = n.getResourceDrainRate(a4);
        if (resourceDrainRate != 0) {
            string = string + "(+" + b2 + ")(-" + resourceDrainRate + ")";
        }
        else if (b2 != 0) {
            if (b2 >= 0) {
                string = string + "(+" + b2 + ")";
            }
            else {
                string = string + "(" + b2 + ")";
            }
        }
        int n3 = (int)(l.screenWidth - l.sidebarWidth);
        n3 -= this.highlightOffsetX;
        Paint paint4 = this.unitHealthBarBackgroundPaint;
        if (boolean3) {
            paint4 = this.unitShieldBarPaint;
        }
        else {
            final Integer h = a4.h();
            if (h != null) {
                this.tempTooltipPaint.a(paint4);
                paint4 = this.tempTooltipPaint;
                paint4.b(h);
            }
        }
        final float n4 = (float)l.renderGraphicsEngine.b(string, paint4);
        final float n5 = (float)l.renderGraphicsEngine.a(string, paint4);
        this.sidebarRowHeight = n5 + n2;
        if (this.sidebarPanelMaxWidth < n4) {
            this.sidebarPanelMaxWidth = (int)n4;
        }
        int ch = this.sidebarPanelIndex;
        if (a4.w) {
            ch = 0;
        }
        int cg = 0;
        int n6 = 0;
        if (ch == 0) {
            cg = this.sidebarPanelStartY;
        }
        else {
            n6 = this.sidebarPanelOffsetY;
        }
        int b3 = 0;
        int n7 = n2;
        int n8 = n2;
        int n9 = n2;
        final int n10 = n2;
        boolean b4 = false;
        float n11 = n4 + n9 + n8;
        if (a4.k) {
            n11 += 80.0f;
        }
        if (n3 < n11 && a4.i != null) {
            b4 = true;
            this.sidebarPanelOffsetY += (int)this.sidebarRowHeight;
            n6 = this.sidebarPanelOffsetY;
            n3 += this.highlightOffsetX;
            this.highlightOffsetX = 0;
        }
        if (ch != 0) {
            n7 = 0;
        }
        if (a8 != null && !a8.k) {
            n8 = 0;
        }
        if (b && !a4.k) {
            n3 += n9;
            n9 = 0;
        }
        if (b && a4.k && !b4) {
            b3 = l.renderGraphicsEngine.b("AA", paint4);
        }
        n3 -= b3;
        final Texture k = a4.k();
        float scale = 1.0f;
        float n12;
        if (k != null) {
            float float3 = n5 - 3.0f;
            if (float3 < 3.0f) {
                float3 = 3.0f;
            }
            scale = TextUtils.getScale(k, n5 * 3.0f, float3);
            n12 = k.p * scale + 3.0f;
            n8 += (int)n12;
        }
        else {
            n12 = 0.0f;
        }
        float float3 = n3 - n4 - cg;
        TextUtils.drawTextWithBackground(string, float3 - n2, (float)(n6 + n2), paint4, this.unitSelectionPaint, (float)n8, (float)n7, (float)n9, (float)n10);
        if (k != null) {
            l.renderGraphicsEngine.a(k, (float)(int)(float3 - n12 / 2.0f - k.r * scale - 3.0f), (float)(int)(n6 + n2 + n5 / 2.0f - k.s * scale), this.textureDrawPaint, 0.0f, scale);
        }
        if (integer == 0) {
            if (ch == 0) {
                this.sidebarPanelStartY += (int)(n4 + n9 + n8);
            }
            if (this.sidebarPanelIndex == ch) {
                this.sidebarPanelOffsetY += (int)this.sidebarRowHeight;
                ++this.sidebarPanelIndex;
            }
        }
        this.highlightOffsetX += (int)(n4 + n9 + n8 + b3);
        return true;
    }

    /* JADX INFO: renamed from: k */
    public boolean cancelCurrentAction() {
        return drawInteractiveLabel(ConfirmationResult.no, true);
    }

    /* JADX INFO: renamed from: b */
    public boolean removeFromSelection(boolean z) {
        return drawInteractiveLabel(z ? ConfirmationResult.yes : ConfirmationResult.no, false);
    }

    /* JADX INFO: renamed from: a */
    public boolean drawLabelInBox(ConfirmationResult confirmationResult) {
        return drawInteractiveLabel(confirmationResult, false);
    }

    /* JADX INFO: renamed from: a */
    public boolean drawInteractiveLabel(ConfirmationResult confirmationResult, boolean z) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (GameEngine.isPC() && !z) {
            return false;
        }
        this.isTooltipVisible = true;
        float f = gameEngine.screenScale * 0.6f;
        int i = (int) (100.0f * f);
        int i2 = (int) (10.0f * f);
        int i3 = (int) ((gameEngine.screenHeight - ((int) (9.0f * f))) - (i * this.tooltipTimer));
        if (isSidebarOnLeft) {
            i3 = (int) (i3 - gameEngine.minimap.height);
        }
        if (confirmationResult == ConfirmationResult.more) {
            int i4 = ((int) (20.0f * f)) + i + ((int) (20.0f * f)) + i;
            this.tempButtonRect.a(i2 + i4, i3, i2 + i4 + i, i3 + i);
            gameEngine.renderGraphicsEngine.a(this.buttonMoreTexture, this.tempButtonRect.a, this.tempButtonRect.b, this.tooltipBackgroundPaint, 0.0f, f);
        } else if (confirmationResult == ConfirmationResult.yes) {
            this.tempButtonRect.a(i2, i3, i2 + i, i3 + i);
            gameEngine.renderGraphicsEngine.a(this.buttonYesTexture, this.tempButtonRect.a, this.tempButtonRect.b, this.tooltipBackgroundPaint, 0.0f, f);
        } else {
            int i5 = ((int) (20.0f * f)) + i;
            this.tempButtonRect.a(i2 + i5, i3, i2 + i5 + i, i3 + i);
            gameEngine.renderGraphicsEngine.a(this.buttonNoTexture, this.tempButtonRect.a, this.tempButtonRect.b, this.tooltipBackgroundPaint, 0.0f, f);
        }
        boolean z2 = false;
        Utility.grow(this.tempButtonRect, 10.0f * f);
        if (this.isSelectionBoxActive && !this.isInputDisabled && this.tempButtonRect.b((int) this.selectionBoxStartX, (int) this.selectionBoxStartY)) {
            z2 = true;
        }
        forceModifiersInsideRect(this.tempButtonRect.a, this.tempButtonRect.b, this.tempButtonRect.b(), this.tempButtonRect.c());
        return z2;
    }

    /* JADX INFO: renamed from: l */
    public boolean clearCurrentAction() {
        GameEngine.getInstance();
        if (this.currentAction != null) {
            if (this.currentAction.getActionType() == ActionType.placeBuilding) {
                this.currentAction = null;
                this.isBuildingMode = false;
                this.isQueuedBuild = false;
                this.selectedBuilder = null;
                this.isScreenFlashActive = false;
                this.buildQueueId++;
            } else {
                this.currentAction = null;
            }
            this.screenFlashBlue = 0;
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: c */
    public void handleTouchGestures(float f) {
        float f2;
        float f3;
        GameEngine gameEngine = GameEngine.getInstance();
        Point pointScreenToWorld = gameEngine.minimap.screenToWorld(this.selectionBoxStartX, this.selectionBoxStartY);
        if (pointScreenToWorld != null) {
            f2 = pointScreenToWorld.worldX;
            f3 = pointScreenToWorld.worldY;
        } else {
            f2 = (this.selectionBoxStartX / gameEngine.zoom) + gameEngine.viewpointXSnapped;
            f3 = (this.selectionBoxStartY / gameEngine.zoom) + gameEngine.viewpointYSnapped;
        }
        this.buildingRotation = Utility.moveTowardsZero(this.buildingRotation, f);
        this.tempDrawRect.a((int) (gameEngine.screenWidth - gameEngine.sidebarWidth), 0, (int) gameEngine.screenWidth, (int) gameEngine.screenHeight);
        if (!showModernSidebar && ((this.isSelectionBoxActive || this.isMousePressed) && this.tempDrawRect.b((int) this.selectionBoxStartX, (int) this.selectionBoxStartY))) {
            this.showDebugInfo = true;
        }
        this.interfaceRenderer.handleZoomAndGestures(f);
        this.interfaceRenderer.handleUnitSelection(f);
        this.lastSelectionTime += f;
        if (!gameEngine.isGamePaused()) {
            this.interfaceRenderer.getUnitStatusText(f, this.interfaceRenderer.drawUnitCommands(f));
            this.interfaceRenderer.updateUnitGroupMarkers(f);
            this.messageManager.draw(f, MessageManager.MAX_MESSAGES);
            this.warLogDisplay.drawWarLog(f);
            this.leaderboard.draw(f, Math.max((int) (this.sidebarPanelOffsetY + (this.sidebarRowHeight * 2.0f)), 130));
            if (this.isDraggingSelection) {
                this.interfaceRenderer.updateMessageTimer(f);
            }
            this.endGameScreen.draw(f);
            this.interfaceRenderer.drawPauseOverlay(f, true);
        }
        drawActionPreview(f, f2, f3, pointScreenToWorld);
        if (!gameEngine.isGamePaused() && !this.isDraggingSelection) {
            this.interfaceRenderer.updateMessageTimer(f);
        }
        boolean z = false;
        if (!this.isInputDisabled) {
            boolean z2 = true;
            boolean z3 = true;
            boolean z4 = true;
            if (GameEngine.isPC() && gameEngine.settingsEngine.mouseSupport) {
                if (gameEngine.settingsEngine.mouseOrders == 0) {
                    z2 = true;
                } else {
                    z2 = false;
                    z3 = false;
                    z4 = false;
                    if (gameEngine.settingsEngine.mouseOrders == 1) {
                        if (gameEngine.isMouseButtonPressed(1)) {
                            z3 = true;
                        } else if (gameEngine.isMouseButtonPressed(2)) {
                            z4 = true;
                        }
                    } else if (gameEngine.isMouseButtonPressed(2)) {
                        z3 = true;
                    } else if (gameEngine.isMouseButtonPressed(1)) {
                        z4 = true;
                    }
                }
            }
            float f4 = f2;
            float f5 = f3;
            if (this.isMousePressed && pointScreenToWorld != null && this.isRightMousePressed) {
                boolean z5 = false;
                if (!z2 && !z4) {
                    z5 = true;
                }
                if (getSelectedUnitCount() == 0 || !hasMovableUnitsSelected()) {
                    z5 = true;
                }
                if (z2 && this.uiScale > 20.0f) {
                    z5 = true;
                }
                if (z5) {
                    gameEngine.centerViewpoint(f4, f5);
                    z = true;
                }
            }
            if ((this.isGamePaused || pointScreenToWorld != null || z3 || z4) && !z && this.currentAction == null && this.isSelectionBoxActive) {
                if (this.uiScale > 30.0f) {
                    if (shouldUseCircleSelect() && pointScreenToWorld == null) {
                        float fCalculateUIWidth = calculateUIWidth() / gameEngine.zoom;
                        clearSelection();
                        selectUnitsInArea(f4, f5, fCalculateUIWidth);
                        emptyMethod();
                    }
                } else {
                    gameEngine.isMenuOpen = false;
                    if (!z2) {
                        if (z3) {
                            BaseUnit baseUnitFindUnitAtPosition = null;
                            if (pointScreenToWorld == null) {
                                baseUnitFindUnitAtPosition = findUnitAtPosition(f4, f5, true);
                            }
                            handleUnitSelectionClick(baseUnitFindUnitAtPosition);
                        } else if (z4) {
                            BaseUnit baseUnitFindUnitAtPosition2 = null;
                            if (pointScreenToWorld == null) {
                                baseUnitFindUnitAtPosition2 = findUnitAtPosition(f4, f5, false);
                            }
                            boolean z6 = false;
                            if (baseUnitFindUnitAtPosition2 == null || !handleUnitTargetClick(baseUnitFindUnitAtPosition2, false, f4, f5, pointScreenToWorld)) {
                                z6 = true;
                            }
                            if (z6) {
                                issueMarchOrMoveCommand(f4, f5, pointScreenToWorld);
                            }
                        }
                    } else {
                        BaseUnit baseUnitFindUnitAtPosition3 = null;
                        BaseUnit baseUnitFindUnitAtPosition4 = null;
                        if (pointScreenToWorld == null) {
                            baseUnitFindUnitAtPosition3 = findUnitAtPosition(f4, f5, true);
                            baseUnitFindUnitAtPosition4 = findUnitAtPosition(f4, f5, false);
                        }
                        if (baseUnitFindUnitAtPosition3 == null && baseUnitFindUnitAtPosition4 == null) {
                            issueMarchOrMoveCommand(f4, f5, pointScreenToWorld);
                        } else if (baseUnitFindUnitAtPosition4 != null) {
                            if (!handleUnitTargetClick(baseUnitFindUnitAtPosition4, true, f4, f5, pointScreenToWorld)) {
                                if (!baseUnitFindUnitAtPosition4.t()) {
                                    handleUnitSelectionClick(baseUnitFindUnitAtPosition4);
                                } else if (baseUnitFindUnitAtPosition3 != null) {
                                    handleUnitSelectionClick(baseUnitFindUnitAtPosition3);
                                }
                            }
                        } else {
                            handleUnitSelectionClick(baseUnitFindUnitAtPosition3);
                        }
                    }
                }
            }
        }
        if (this.currentAction == null && this.isMousePressed && !this.isInputDisabled && !this.isRightMousePressed && !this.showDebugInfo) {
            this.unitHealthBarPaint.a(Paint.Style.FILL);
            this.unitHealthBarPaint.a(1.0f);
            if (this.uiScale > 20.0f && shouldUseCircleSelect()) {
                float fCalculateUIWidth2 = calculateUIWidth();
                this.unitHealthBarPaint.a(100, 0, 255, 0);
                gameEngine.renderGraphicsEngine.a(this.selectionBoxStartX, this.selectionBoxStartY, fCalculateUIWidth2, this.unitHealthBarPaint);
                this.unitHealthBarPaint.a(Paint.Style.STROKE);
                this.unitHealthBarPaint.a(1.0f);
                this.unitHealthBarPaint.a(200, 0, 255, 0);
                gameEngine.renderGraphicsEngine.a(this.selectionBoxStartX, this.selectionBoxStartY, fCalculateUIWidth2, this.unitHealthBarPaint);
            }
        }
        if (gameEngine.isGamePausedOrMinimized && gameEngine.isTouchDown() && gameEngine.getTouchPointerCount() > 0) {
            Paint paint = new Paint();
            paint.c(100);
            for (int i = 0; i < gameEngine.getTouchPointerCount(); i++) {
                gameEngine.renderGraphicsEngine.i();
                gameEngine.renderGraphicsEngine.a(0.7f, 0.7f, gameEngine.getTouchX(i), gameEngine.getTouchY(i));
                gameEngine.renderGraphicsEngine.a(this.touchIndicatorTexture, gameEngine.getTouchX(i), gameEngine.getTouchY(i), paint);
                gameEngine.renderGraphicsEngine.j();
            }
        }
        if (!this.isMousePressed) {
            this.uiScale = 0.0f;
            this.isInputDisabled = false;
        }
        this.isMouseOverUI = this.isMousePressed;
        gameEngine.updateTouchInput();
        if (selectionChanged) {
            notifySelectionChanged();
            selectionChanged = false;
        }
    }

    /* JADX INFO: renamed from: a */
    public void drawActionPreview(float f, float f2, float f3, Point point) {
        GameEngine gameEngine = GameEngine.getInstance();
        BaseUnit baseUnitF = this.interfaceRenderer.getHighestUpgradeUnitInSelection();
        if (this.isTooltipVisible) {
            this.tooltipTimer = Utility.distanceSq(this.tooltipTimer, 1.0f, 0.05f * f);
            this.tooltipTimer = (float) (((double) this.tooltipTimer) + (0.08d * ((double) (1.0f - this.tooltipTimer))));
        } else {
            this.tooltipTimer = Utility.distanceSq(this.tooltipTimer, 0.0f, 0.3f * f);
        }
        this.isTooltipVisible = false;
        if (this.currentAction != null) {
            if (this.currentAction instanceof WrapperUnitAction) {
                WrapperUnitAction wrapperUnitAction = (WrapperUnitAction) this.currentAction;
                if (wrapperUnitAction.unit != null) {
                    baseUnitF = wrapperUnitAction.unit;
                }
            }
            if (this.currentAction.getActionType() == ActionType.reclaimTarget) {
                drawActionTooltip(this.currentAction, false, baseUnitF, false, true);
                if (removeFromSelection(false) || isMouseSelectionActive()) {
                    clearCurrentAction();
                    this.isSelectionBoxActive = false;
                    return;
                }
                if (this.isSelectionBoxActive && !this.isInputDisabled && !shouldShowMouseCursor()) {
                    BaseUnit baseUnitFindUnitAtPosition = findUnitAtPosition(f2, f3, false);
                    if (baseUnitFindUnitAtPosition != null && this.currentAction.isAvailableAndVisible(baseUnitFindUnitAtPosition)) {
                        issueReclaimCommand(baseUnitFindUnitAtPosition);
                        if (!isShiftKeyPressed(gameEngine)) {
                            clearCurrentAction();
                        }
                    } else {
                        showCommandFeedback(f2, f3, 0.0f);
                    }
                    this.isSelectionBoxActive = false;
                    return;
                }
                return;
            }
            if (this.currentAction.getActionType() == ActionType.repairTarget) {
                drawActionTooltip(this.currentAction, false, baseUnitF, false, true);
                if (removeFromSelection(false) || isMouseSelectionActive()) {
                    clearCurrentAction();
                    this.isSelectionBoxActive = false;
                    return;
                }
                if (this.isSelectionBoxActive && !this.isInputDisabled && !shouldShowMouseCursor()) {
                    BaseUnit baseUnitFindUnitAtPosition2 = findUnitAtPosition(f2, f3, true);
                    if (baseUnitFindUnitAtPosition2 != null && this.currentAction.isAvailableAndVisible(baseUnitFindUnitAtPosition2)) {
                        issueRepairCommand(baseUnitFindUnitAtPosition2);
                        if (!isShiftKeyPressed(gameEngine)) {
                            clearCurrentAction();
                        }
                    } else {
                        showCommandFeedback(f2, f3, 0.0f);
                    }
                    this.isSelectionBoxActive = false;
                    return;
                }
                return;
            }
            if (this.currentAction.getActionType() == ActionType.setRally) {
                drawActionTooltip(this.currentAction, false, baseUnitF, false, true);
                if (removeFromSelection(false) || isMouseSelectionActive()) {
                    clearCurrentAction();
                    this.isSelectionBoxActive = false;
                    return;
                } else {
                    if (this.isSelectionBoxActive && !this.isInputDisabled && !shouldShowMouseCursor()) {
                        issueRallyPointCommand(f2, f3);
                        clearCurrentAction();
                        this.isSelectionBoxActive = false;
                        return;
                    }
                    return;
                }
            }
            if (this.currentAction.getActionType() == ActionType.targetGround) {
                drawActionTooltip(this.currentAction, false, baseUnitF, false, true);
                BaseUnit baseUnitF2 = this.interfaceRenderer.getHighestUpgradeUnitInSelection();
                AbstractUnitAction abstractUnitAction = this.currentAction;
                if (this.currentAction instanceof WrapperUnitAction) {
                    WrapperUnitAction wrapperUnitAction2 = (WrapperUnitAction) abstractUnitAction;
                    if (wrapperUnitAction2.unit != null) {
                        baseUnitF2 = wrapperUnitAction2.unit;
                    }
                    abstractUnitAction = wrapperUnitAction2.wrappedAction;
                }
                boolean zCancelCurrentAction = cancelCurrentAction();
                boolean z = (!this.isSelectionBoxActive || this.isKeyboardCtrlPressed || !this.isGamePaused || this.isInputDisabled || shouldShowMouseCursor()) ? false : true;
                if (this.currentAction.isInstant()) {
                    if (GameEngine.isDesktopMouseInput()) {
                        z = isMouseInputActive() && !this.isKeyboardCtrlPressed && this.isGamePaused && !this.isMiddleMousePressed && isInputEnabled();
                    } else {
                        z = this.isMousePressed && !this.isKeyboardCtrlPressed && this.isGamePaused && !this.isMiddleMousePressed && isInputEnabled();
                    }
                }
                if (baseUnitF2 != null && (baseUnitF2 instanceof OrderableUnit)) {
                    gameEngine.renderGraphicsEngine.i();
                    gameEngine.applyZoomTransform();
                    boolean z2 = (!this.isMousePressed || this.isInputDisabled || this.isKeyboardCtrlPressed || this.isMiddleMousePressed || point != null) ? false : true;
                    float touchX = f2;
                    float touchY = f3;
                    if (GameEngine.isDesktopMouseInput() && gameEngine.settingsEngine.mouseSupport) {
                        touchX = (gameEngine.getTouchX() / gameEngine.zoom) + gameEngine.viewpointXSnapped;
                        touchY = (gameEngine.getTouchY() / gameEngine.zoom) + gameEngine.viewpointYSnapped;
                        z2 = true;
                        if (this.isKeyboardCtrlPressed) {
                            z2 = false;
                        }
                    }
                    if (!isWorldClickAllowedAt(this.selectionBoxMinWidth, this.selectionBoxMinHeight)) {
                        z2 = false;
                    }
                    ((OrderableUnit) baseUnitF2).a(abstractUnitAction, z2, touchX, touchY);
                    gameEngine.renderGraphicsEngine.j();
                }
                if (zCancelCurrentAction || isMouseSelectionActive()) {
                    clearCurrentAction();
                    this.isSelectionBoxActive = false;
                    return;
                }
                if (z && point == null) {
                    boolean z3 = false;
                    if (hasInvalidActionTarget(this.currentAction, f2, f3)) {
                        z3 = true;
                    }
                    if (!z3) {
                        executeUnitAction(this.currentAction, f2, f3);
                        if (!isShiftKeyPressed(gameEngine) && !this.currentAction.isCancel()) {
                            clearCurrentAction();
                        }
                    } else {
                        showCommandFeedback(f2, f3, 0.0f);
                    }
                    this.isSelectionBoxActive = false;
                    return;
                }
                return;
            }
            if (this.currentAction.getActionType() == ActionType.attackMove) {
                drawActionTooltip(this.currentAction, false, baseUnitF, false, true);
                if (removeFromSelection(false) || isMouseSelectionActive()) {
                    clearCurrentAction();
                    this.isSelectionBoxActive = false;
                    return;
                } else {
                    if (this.isSelectionBoxActive && !this.isInputDisabled && !shouldShowMouseCursor()) {
                        issueAttackCommand(f2, f3, point);
                        if (!isShiftKeyPressed(gameEngine)) {
                            clearCurrentAction();
                            this.isSelectionBoxActive = false;
                            return;
                        }
                        return;
                    }
                    return;
                }
            }
            if (this.currentAction.getActionType() == ActionType.guardUnit) {
                drawActionTooltip(this.currentAction, false, baseUnitF, false, true);
                if (removeFromSelection(false) || isMouseSelectionActive()) {
                    clearCurrentAction();
                    this.isSelectionBoxActive = false;
                    return;
                }
                if (this.isSelectionBoxActive && !this.isInputDisabled && !shouldShowMouseCursor()) {
                    BaseUnit baseUnitFindUnitAtPosition3 = findUnitAtPosition(f2, f3, true);
                    if (baseUnitFindUnitAtPosition3 != null && this.currentAction.isAvailableAndVisible(baseUnitFindUnitAtPosition3)) {
                        issueGuardCommand(baseUnitFindUnitAtPosition3);
                        clearCurrentAction();
                    } else {
                        showCommandFeedback(f2, f3, 0.0f);
                    }
                    this.isSelectionBoxActive = false;
                    return;
                }
                return;
            }
            if (this.currentAction.getActionType() == ActionType.patrol) {
                drawActionTooltip(this.currentAction, false, baseUnitF, false, true);
                if (drawInteractiveLabel(ConfirmationResult.yes, true) || isMouseSelectionActive()) {
                    clearCurrentAction();
                    this.isSelectionBoxActive = false;
                    return;
                } else {
                    if (this.isSelectionBoxActive && !this.isInputDisabled && !shouldShowMouseCursor()) {
                        executeUnitAction(f2, f3, point, this.screenFlashBlue == 0);
                        this.screenFlashBlue++;
                        return;
                    }
                    return;
                }
            }
            if (this.currentAction.getActionType() == ActionType.pingMap) {
                drawActionTooltip(this.currentAction, false, baseUnitF, false, true);
                if (cancelCurrentAction() || isMouseSelectionActive()) {
                    clearCurrentAction();
                    this.isSelectionBoxActive = false;
                    return;
                } else {
                    if (this.isSelectionBoxActive && !this.isInputDisabled && !shouldShowMouseCursor() && point == null) {
                        if (this.currentAction instanceof PingMapAction) {
                            issueMapPingCommand(f2, f3, point, (PingMapAction) this.currentAction);
                        } else {
                            GameEngine.logColored("orderBuildingSpecialAction is not a PingMapAction, it is: " + this.currentAction.getClass().getName());
                        }
                        clearCurrentAction();
                        this.isSelectionBoxActive = false;
                        return;
                    }
                    return;
                }
            }
            if (this.currentAction.getUnitType() != null && this.currentAction.getActionType() == ActionType.placeBuilding) {
                handleBuildingPlacement(f2, f3, point);
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public void handleBuildingPlacement(final float float1, final float float2, final Point point) {
        final GameEngine instance = GameEngine.getInstance();
        final BaseUnit f = this.interfaceRenderer.getHighestUpgradeUnitInSelection();
        boolean b = false;
        if (f != null && f.validateActionId(this.currentAction.getActionId()) != null) {
            b = (this.currentAction.canAfford(f, true) && !GameInterfaceRenderer.getKeyBindingForAction(this.currentAction));
            if (!this.currentAction.b(f)) {
                b = false;
            }
        }
        this.drawActionTooltip(this.currentAction, false, f, !b, true);
        final float n = this.selectionBoxMinWidth / instance.zoom;
        final float n2 = this.selectionBoxMinHeight / instance.zoom;
        float screenFlashRed = n;
        float screenFlashGreen = n2;
        boolean b2 = false;
        boolean b3 = false;
        boolean b4 = false;
        boolean isKeyboardShiftPressed = false;
        if (GameEngine.isDesktopMouseInput() && instance.settingsEngine.mouseSupport) {
            b2 = true;
            isKeyboardShiftPressed = this.isKeyboardShiftPressed;
        }
        if (b2) {
            if (this.isMouseInputActive()) {
                if (!this.isScreenFlashActive) {
                    this.isScreenFlashActive = true;
                    this.screenFlashIntensity = screenFlashRed + instance.viewpointXSnapped;
                    this.screenFlashDecay = screenFlashGreen + instance.viewpointYSnapped;
                }
            }
            else {
                this.isScreenFlashActive = false;
            }
            if (this.isScreenFlashActive) {
                final float float3 = screenFlashRed - (this.screenFlashIntensity - instance.viewpointXSnapped);
                final float float4 = screenFlashGreen - (this.screenFlashDecay - instance.viewpointYSnapped);
                if (Utility.abs(float3) > 4.0f || Utility.abs(float4) > 4.0f) {
                    b3 = true;
                }
            }
        }
        boolean b5 = false;
        boolean b6 = false;
        boolean b7 = false;
        boolean b8 = false;
        boolean b9 = true;
        boolean b10 = false;
        if (GameEngine.isPC() && instance.settingsEngine.mouseSupport) {
            b10 = true;
        }
        if (this.isBuildingMode && !b10) {
            if (b) {
                if (!this.isQueuedBuild && this.drawLabelInBox(ConfirmationResult.yes)) {
                    instance.soundEngine.playInterfaceSound(SoundEngine.clickAddSound, 0.5f);
                    this.isSelectionBoxActive = false;
                    b5 = true;
                }
                if (this.drawLabelInBox(ConfirmationResult.more)) {
                    instance.soundEngine.playInterfaceSound(SoundEngine.clickAddSound, 0.5f);
                    this.isSelectionBoxActive = false;
                    b7 = true;
                }
            }
            if (this.drawLabelInBox(ConfirmationResult.no)) {
                instance.soundEngine.playInterfaceSound(SoundEngine.clickRemoveSound, 0.7f);
                this.isSelectionBoxActive = false;
                b6 = true;
            }
        }
        boolean b11 = false;
        if (GameEngine.isDesktopMouseInput() && instance.settingsEngine.mouseSupport) {
            b11 = true;
        }
        if (this.isSelectionBoxActive && !this.isInputDisabled) {
            b11 = true;
        }
        if (GameEngine.isNonPCPlatform()) {
            b4 = true;
            if (instance.getTouchPointerCount() == 2) {
                b11 = true;
                screenFlashRed = instance.getTouchX(0) / instance.zoom;
                screenFlashGreen = instance.getTouchY(0) / instance.zoom;
                final float screenFlashIntensity = instance.getTouchX(1) / instance.zoom;
                final float screenFlashDecay = instance.getTouchY(1) / instance.zoom;
                this.isScreenFlashActive = true;
                this.screenFlashIntensity = screenFlashIntensity;
                this.screenFlashDecay = screenFlashDecay;
            }
            else if (this.isSelectionBoxActive && !this.isInputDisabled) {
                this.isScreenFlashActive = false;
            }
            if (this.isScreenFlashActive) {
                b3 = true;
            }
        }
        if (b11) {
            this.isBuildingMode = true;
            this.buildingPlaceX = screenFlashRed * instance.zoom;
            this.buildingPlaceY = screenFlashGreen * instance.zoom;
            if (!this.isWorldClickAllowedAt(instance.getTouchX(), instance.getTouchY())) {
                this.isBuildingMode = false;
                b9 = false;
            }
        }
        float screenFlashIntensity = this.screenFlashIntensity;
        float screenFlashDecay = this.screenFlashDecay;
        if (b4) {
            screenFlashIntensity += instance.viewpointXSnapped + instance.scrollDeltaX;
            screenFlashDecay += instance.viewpointYSnapped + instance.scrollDeltaY;
        }
        final UnitType unitType = this.currentAction.getUnitType();
        final int queueSize = this.currentAction.getQueueSize();
        boolean b12 = false;
        if (GameEngine.isDesktopMouseInput() && instance.settingsEngine.mouseSupport && !instance.screenClipRect.b((int)this.selectionBoxMinWidth, (int)this.selectionBoxMinHeight)) {
            b12 = true;
        }
        BaseUnit baseUnit = BaseUnit.getPrototypeForUnitType(unitType);
        if ((baseUnit == null || !(baseUnit instanceof OrderableUnit)) && CustomUnitConfig.instance != null) {
            baseUnit = BaseUnit.getPrototypeForUnitType(CustomUnitConfig.instance);
        }
        if (this.isBuildingMode && !b12) {
            final OrderableUnit y = (OrderableUnit)baseUnit;
            instance.tileMap.updateCursorTileIndexFromWorldPoint(this.buildingPlaceX / instance.zoom + instance.viewpointXSnapped, this.buildingPlaceY / instance.zoom + instance.viewpointYSnapped);
            y.posX = (float)instance.tileMap.cursorTileX;
            y.posY = (float)instance.tileMap.cursorTileY;
            if (unitType.p()) {
                b3 = false;
                final Point closestOpenPlacement = PlacementFinder.findClosestOpenPlacement((int)y.posX, (int)y.posY, 3);
                if (closestOpenPlacement != null) {
                    y.posX = (float)closestOpenPlacement.worldX;
                    y.posY = (float)closestOpenPlacement.worldY;
                }
            }
            if (!y.bI()) {
                y.rotationSpeed = 0.0f;
            }
            else {
                y.rotationSpeed = -90.0f;
            }
            final OrderableUnit orderableUnit = y;
            orderableUnit.posX += y.getTileOffsetX();
            final OrderableUnit orderableUnit2 = y;
            orderableUnit2.posY += y.getTileOffsetY();
            y.setUnitTeam(this.selectedBuilder.team);
            y.a(queueSize);
            y.isUnitParalyzed = true;
            String string = y.getPlacementFailureReason(false, instance.playerTeam);
            if (BuildPreview.isUnitOverBlueprint(instance.playerTeam, y, this.buildQueueId)) {
                string = "{0}";
            }
            if (this.getSelectedUnitCount() == 1 && f != null && f instanceof OrderableUnit) {
                final OrderableUnit orderableUnit3 = (OrderableUnit)f;
                if (!orderableUnit3.canExecuteMovementCommands()) {
                    final float float5 = Utility.distanceSq(orderableUnit3.posX, orderableUnit3.posY, y.posX, y.posY);
                    final float float6 = orderableUnit3.f(y.r());
                    if (float6 <= 800000.0f && float5 > float6 * float6) {
                        string = "{0}";
                    }
                }
            }
            if (b3) {}
            BaseUnit baseUnit2 = null;
            if (this.getSelectedUnitCount() == 1) {
                baseUnit2 = f;
            }
            if (b) {
                if (b3) {
                    if (GameEngine.isPC() || (GameEngine.isNonPCPlatform() && instance.getTouchPointerCount() == 2)) {
                        instance.renderGraphicsEngine.a(screenFlashRed * instance.zoom, screenFlashGreen * instance.zoom, (screenFlashIntensity - instance.viewpointXSnapped) * instance.zoom, (screenFlashDecay - instance.viewpointYSnapped) * instance.zoom, this.screenFlashBorderPaint);
                    }
                    else {
                        instance.renderGraphicsEngine.a((y.posX - instance.viewpointXSnapped) * instance.zoom, (y.posY - instance.viewpointYSnapped) * instance.zoom, (screenFlashIntensity - instance.viewpointXSnapped) * instance.zoom, (screenFlashDecay - instance.viewpointYSnapped) * instance.zoom, this.screenFlashBorderPaint);
                    }
                    final boolean boolean6 = true;
                    string = null;
                    this.drawUnitMovePreview(y, screenFlashIntensity, screenFlashDecay, y.posX, y.posY, boolean6, null, baseUnit2);
                }
                else {
                    this.validateBuildingPlacement(y, y.posX, y.posY, true, isKeyboardShiftPressed, baseUnit2);
                }
            }
            y.a(1);
            if (b9 && this.canUseMouseSelection()) {
                this.isSelectionBoxActive = false;
                if (this.isShiftKeyPressed(instance)) {
                    b7 = true;
                    b8 = true;
                }
                else {
                    b5 = true;
                }
            }
            if (this.isMouseSelectionActive()) {
                this.isSelectionBoxActive = false;
                b6 = true;
            }
            if (this.isSelectionBoxActive && !this.isInputDisabled) {
                final float float5 = screenFlashRed;
                final float float6 = screenFlashGreen;
                final float screenFlashRed2 = this.screenFlashRed;
                final float screenFlashGreen2 = this.screenFlashGreen;
                final float n3 = 15.0f;
                if (Utility.abs(screenFlashRed2 - float5) < n3 && Utility.abs(screenFlashGreen2 - float6) < n3 && this.buildingRotation != 0.0f) {
                    this.isSelectionBoxActive = false;
                    instance.soundEngine.playInterfaceSound(SoundEngine.clickAddSound, 0.5f);
                    if (this.isQueuedBuild) {
                        b7 = true;
                    }
                    else {
                        b5 = true;
                    }
                }
                this.buildingRotation = 80.0f;
                this.screenFlashRed = screenFlashRed;
                this.screenFlashGreen = screenFlashGreen;
            }
            if (b5 || b7) {
                if (!b) {
                    instance.soundEngine.playInterfaceSound(SoundEngine.interfaceErrorSound, 0.7f);
                    if (string == null && f != null && this.currentAction != null && f.validateActionId(this.currentAction.getActionId()) != null) {
                        string = this.currentAction.getIcon(f);
                        if (string == null && this.getActionDisplayText(this.currentAction) != null) {
                            string = this.notEnoughResourcesText.resolveText();
                        }
                    }
                    if (string != "{0}") {
                        this.showHighPriorityMessage(string);
                    }
                }
                else if (string != null) {
                    instance.soundEngine.playInterfaceSound(SoundEngine.interfaceErrorSound, 0.7f);
                    if (string != "{0}") {
                        String string2 = string;
                        if (string2 == "{2}") {
                            string2 = this.cannotPlaceNeedsResourcePoolText;
                        }
                        if (string2 == "{3}") {
                            string2 = this.cannotPlaceNeedsWaterText;
                        }
                        if (string2 == "{1}") {
                            string2 = this.cannotPlaceGeneralText;
                        }
                        this.showHighPriorityMessage(string2);
                    }
                }
                else {
                    final float float5 = y.posX;
                    final float float6 = y.posY;
                    final ArrayList<PointF> arrayList = new ArrayList<PointF>();
                    if (b3) {
                        this.drawUnitMovePreview(y, screenFlashIntensity, screenFlashDecay, y.posX, y.posY, false, arrayList, null);
                    }
                    else {
                        arrayList.add(new PointF(float5, float6));
                    }
                    int n4 = 0;
                    int n5 = 1;
                    for (final PointF pointF : arrayList) {
                        if (this.currentAction.usesActionTarget()) {
                            final Command commandForSelectedUnits = this.createCommandForSelectedUnits();
                            this.addSelectedUnitsToCommand(commandForSelectedUnits);
                            commandForSelectedUnits.setActionTarget(this.currentAction.getActionId(), pointF, null);
                        }
                        else {
                            final Command commandForSelectedUnits2 = this.createCommandForSelectedUnits();
                            if (n5 != 0) {
                                n5 = 0;
                                if (b7) {
                                    if (!commandForSelectedUnits2.isQueued) {
                                        commandForSelectedUnits2.isInstantCommand = true;
                                    }
                                    this.isQueuedBuild = true;
                                }
                            }
                            else {
                                commandForSelectedUnits2.isQueued = true;
                            }
                            OrderableUnit firstControllableSelectedUnit = this.getFirstControllableSelectedUnit();
                            if (this.currentAction instanceof WrapperUnitAction) {
                                final OrderableUnit b13 = ((WrapperUnitAction)this.currentAction).unit;
                                commandForSelectedUnits2.addUnitToCommand(b13);
                                firstControllableSelectedUnit = b13;
                            }
                            else {
                                this.addSelectedUnitsToCommand(commandForSelectedUnits2);
                            }
                            commandForSelectedUnits2.setBuildTarget(pointF.x, pointF.y, unitType, queueSize);
                            if (firstControllableSelectedUnit != null) {
                                final BuildPreview buildPreview = new BuildPreview();
                                buildPreview.unitType = unitType;
                                buildPreview.worldX = pointF.x;
                                buildPreview.worldY = pointF.y;
                                buildPreview.isBuilding = true;
                                buildPreview.builder = firstControllableSelectedUnit;
                                buildPreview.team = instance.playerTeam;
                                buildPreview.previewUnitLevel = queueSize;
                                buildPreview.placingTeam = instance.playerTeam;
                                buildPreview.buildQueueId = this.buildQueueId;
                                buildPreview.fadeInProgress = 1.0f + 0.15f * n4;
                                if (firstControllableSelectedUnit.getWaypointCount() >= 29) {
                                    buildPreview.forceDraw = true;
                                }
                            }
                            ++n4;
                        }
                    }
                    this.tooltipX = 5.0f;
                    if (GameEngine.isDesktopMouseInput()) {
                        this.tooltipX = 1.0f;
                    }
                    this.isScreenFlashActive = false;
                    if (!b7) {
                        if (n4 > 0) {
                            boolean b14 = true;
                            if (y != null && !this.canSelectedUnitsRepair(y)) {
                                b14 = false;
                            }
                            this.currentAction = null;
                            this.isBuildingMode = false;
                            this.isQueuedBuild = false;
                            this.selectedBuilder = null;
                            if (b14) {
                                this.clearSelection();
                            }
                            ++this.buildQueueId;
                        }
                    }
                    else if (!b8) {
                        final float posX = y.posX;
                        final float posY = y.posY;
                        int n6 = 0;
                        if (Utility.abs(posX - this.cameraShakeX) < y.cd().b() * instance.tileMap.tileWorldSizeX * 2.0f + 3 * instance.tileMap.tileWorldSizeX && Utility.abs(posY - this.cameraShakeY) < y.cd().c() * instance.tileMap.tileWorldSizeY * 2.0f + 3 * instance.tileMap.tileWorldSizeY) {
                            this.cameraShakeIntensity = posX - this.cameraShakeX;
                            this.cameraShakeDecay = posY - this.cameraShakeY;
                            if (Utility.abs(this.cameraShakeIntensity) > Utility.abs(this.cameraShakeDecay)) {
                                this.cameraShakeDecay = 0.0f;
                            }
                            else {
                                this.cameraShakeIntensity = 0.0f;
                            }
                        }
                        if (y.cd().c() > y.cd().b() + 1) {
                            this.cameraShakeDecay = 0.0f;
                        }
                        this.cameraShakeX = posX;
                        this.cameraShakeY = posY;
                        float n7 = 0.0f;
                        float n8 = 0.0f;
                        if (this.cameraShakeDecay < 0.0f) {
                            n7 = -1.0f;
                        }
                        if (this.cameraShakeIntensity < 0.0f) {
                            n8 = -1.0f;
                        }
                        if (this.cameraShakeDecay > 0.0f) {
                            n7 = 1.0f;
                        }
                        if (this.cameraShakeIntensity > 0.0f) {
                            n8 = 1.0f;
                        }
                        if (n8 == 0.0f && n7 == 0.0f) {
                            n8 = 1.0f;
                        }
                        final ArrayList<PointF> list = new ArrayList<PointF>();
                        float n9 = posX + 200.0f * n8;
                        float n10 = posY + 200.0f * n7;
                        final float n11 = -y.getTileOffsetX() + 1.0f;
                        final float n12 = -y.getTileOffsetY() + 1.0f;
                        final boolean b15 = false;
                        this.drawUnitMovePreview(y, posX + n11, posY + n12, n9 + n11, n10 + n12, b15, list, null);
                        if (list.size() > 0) {
                            y.posX = ((PointF)list.get(0)).x;
                            y.posY = ((PointF)list.get(0)).y;
                            n6 = 1;
                        }
                        if (n6 == 0) {
                            n9 = posX + 200.0f * -n8;
                            n10 = posY + 200.0f * -n7;
                            this.drawUnitMovePreview(y, posX + n11, posY + n12, n9 + n11, n10 + n12, b15, list, null);
                            if (list.size() > 0) {
                                y.posX = ((PointF)list.get(0)).x;
                                y.posY = ((PointF)list.get(0)).y;
                                n6 = 1;
                            }
                        }
                        if (n6 == 0) {
                            final OrderableUnit orderableUnit4 = y;
                            orderableUnit4.posX += 3 * instance.tileMap.tileWorldSizeX;
                            final OrderableUnit orderableUnit5 = y;
                            orderableUnit5.posY += instance.tileMap.tileWorldSizeX;
                        }
                        if (n6 != 0) {
                            final float n13 = y.posX - posX;
                            final float n14 = y.posY - posY;
                            final float viewpointX = instance.viewpointX;
                            final float viewpointY = instance.viewpointY;
                            final GameEngine gameEngine = instance;
                            gameEngine.scrollDeltaX += n13;
                            final GameEngine gameEngine2 = instance;
                            gameEngine2.scrollDeltaY += n14;
                            final GameEngine gameEngine3 = instance;
                            gameEngine3.viewpointX += instance.scrollDeltaX;
                            final GameEngine gameEngine4 = instance;
                            gameEngine4.viewpointY += instance.scrollDeltaY;
                            final float viewpointX2 = instance.viewpointX;
                            final float viewpointY2 = instance.viewpointY;
                            instance.clampCameraPosition();
                            final float n15 = instance.viewpointX - viewpointX2;
                            final float n16 = instance.viewpointY - viewpointY2;
                            final GameEngine gameEngine5 = instance;
                            gameEngine5.scrollDeltaX += n15;
                            final GameEngine gameEngine6 = instance;
                            gameEngine6.scrollDeltaY += n16;
                            final float float7 = viewpointX + n13 - instance.viewpointX;
                            final float float8 = viewpointY + n14 - instance.viewpointY;
                            if (Utility.abs(float7) > 1.0f) {
                                this.buildingPlaceX += float7 * instance.zoom;
                            }
                            if (Utility.abs(float8) > 1.0f) {
                                this.buildingPlaceY += float8 * instance.zoom;
                            }
                            final GameEngine gameEngine7 = instance;
                            gameEngine7.viewpointX -= instance.scrollDeltaX;
                            final GameEngine gameEngine8 = instance;
                            gameEngine8.viewpointY -= instance.scrollDeltaY;
                        }
                    }
                }
            }
            if (b6) {
                this.clearCurrentAction();
                if (this.isQueuedBuild) {
                    this.clearSelection();
                }
            }
        }
    }

    /* JADX INFO: renamed from: m */
    public boolean shouldShowMouseCursor() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (GameEngine.isDesktopMouseInput() && gameEngine.settingsEngine.mouseSupport && !isMouseSelectionActive() && !canUseMouseSelection()) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: n */
    public boolean isMouseSelectionActive() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (GameEngine.isDesktopMouseInput() && gameEngine.settingsEngine.mouseSupport && this.isSelectionBoxActive && !this.isInputDisabled && !this.showDebugInfo) {
            int i = 1;
            int i2 = 2;
            if (gameEngine.settingsEngine.mousePlacement == 2) {
                i = 2;
                i2 = 1;
            }
            if (gameEngine.isMouseButtonPressed(i)) {
            }
            if (gameEngine.isMouseButtonPressed(i2)) {
                return true;
            }
            return false;
        }
        return false;
    }

    /* JADX INFO: renamed from: o */
    public boolean isMouseInputActive() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (!GameEngine.isPC() || !gameEngine.settingsEngine.mouseSupport) {
            return false;
        }
        if (this.isSelectionBoxActive || this.isMousePressed) {
            int i = 1;
            int i2 = 2;
            if (gameEngine.settingsEngine.mousePlacement == 2) {
                i = 2;
                i2 = 1;
            }
            if (gameEngine.isMouseButtonPressed(i)) {
                return true;
            }
            if (gameEngine.isMouseButtonPressed(i2)) {
            }
            return false;
        }
        return false;
    }

    /* JADX INFO: renamed from: p */
    public boolean canUseMouseSelection() {
        if (this.isSelectionBoxActive && !this.isInputDisabled && !this.showDebugInfo) {
            return isMouseInputActive();
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public void handleUnitSelectionClick(BaseUnit baseUnit) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (baseUnit != null && this.lastSelectedUnit == baseUnit && this.lastSelectionTime < 40.0f && !isControlKeyPressed(gameEngine)) {
            if (!isShiftKeyPressed(gameEngine)) {
                clearSelection();
            }
            selectAllSimilarUnits(baseUnit);
        } else if (baseUnit != null) {
            if (!isShiftKeyPressed(gameEngine) && !isControlKeyPressed(gameEngine)) {
                clearSelection();
            }
            drawTeamResources(baseUnit, isControlKeyPressed(gameEngine));
            this.lastSelectedUnit = baseUnit;
            this.lastSelectionTime = 0.0f;
        }
    }

    /* JADX INFO: renamed from: a */
    public boolean handleUnitTargetClick(BaseUnit baseUnit, boolean z, float f, float f2, Point point) {
        GameEngine.getInstance();
        PlayerTeam selectedUnitsTeam = getSelectedUnitsTeam();
        boolean zC = selectedUnitsTeam.c(baseUnit.team);
        if (zC && hasCombatUnitsSelected() && canSelectedUnitsReachTargetByPathfinding(baseUnit)) {
            issueAttackTargetCommand(baseUnit);
            return true;
        }
        if (selectedUnitsTeam.d(baseUnit.team) && ((baseUnit.currentHealth < baseUnit.maxHealth || baseUnit.buildProgress < 1.0f) && getSelectedUnitCount() != 0)) {
            boolean z2 = true;
            boolean z3 = false;
            boolean z4 = false;
            boolean z5 = false;
            if (baseUnit.canTransportUnits() && canSelectedUnitsLoadInto(baseUnit)) {
                z3 = true;
            }
            Iterator it = this.selectedUnitsList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                GameObject gameObject = (GameObject) it.next();
                if (gameObject instanceof OrderableUnit) {
                    OrderableUnit orderableUnit = (OrderableUnit) gameObject;
                    if (!orderableUnit.isSelected) {
                        continue;
                    } else {
                        if (!canControlUnit(orderableUnit)) {
                            z2 = false;
                            break;
                        }
                        if (!orderableUnit.canRepairTarget(baseUnit)) {
                            z2 = false;
                            break;
                        }
                        if (orderableUnit.aS()) {
                            z5 = true;
                        }
                        UnitCommand currentWaypoint = orderableUnit.getCurrentWaypoint();
                        if (currentWaypoint != null && currentWaypoint.getCommandType() == UnitCommandType.repair) {
                            z4 = true;
                        }
                    }
                }
            }
            if (z2 && (!z4 || !z3)) {
                if (z5) {
                    issueRepairCommand(baseUnit);
                    return true;
                }
                issueRepairCommand(baseUnit);
                return true;
            }
        }
        if (baseUnit.getResourceRate() > 0.0f && getSelectedUnitCount() != 0) {
            boolean z6 = true;
            Iterator it2 = GameObject.fastGameObjectList.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                GameObject gameObject2 = (GameObject) it2.next();
                if (gameObject2 instanceof OrderableUnit) {
                    OrderableUnit orderableUnit2 = (OrderableUnit) gameObject2;
                    if (!orderableUnit2.isSelected) {
                        continue;
                    } else {
                        if (!canControlUnit(orderableUnit2)) {
                            z6 = false;
                            break;
                        }
                        if (!orderableUnit2.h(baseUnit, true)) {
                            z6 = false;
                            break;
                        }
                    }
                }
            }
            if (z6) {
                issueReclaimCommand(baseUnit);
                return true;
            }
        }
        if (baseUnit.canTransportUnits() && canSelectedUnitsLoadInto(baseUnit)) {
            issueLoadIntoCommand(baseUnit);
            return true;
        }
        if (GameEngine.isPC() && hasMovableUnitsSelected() && canSelectedUnitsLoadUp(baseUnit)) {
            issueAttackCommand(baseUnit);
            return true;
        }
        boolean z7 = false;
        if ((!z || baseUnit.t()) && !selectedUnitsTeam.c(baseUnit.team)) {
            if (baseUnit.bI()) {
                if (baseUnit.cc().a()) {
                    z7 = true;
                }
            } else if (!baseUnit.isAlive) {
                z7 = true;
            }
            if (!z7 && !baseUnit.i() && areAllSelectedUnitsAirborne()) {
                z7 = true;
            }
        }
        if (!z7 && zC && hasCombatUnitsSelected()) {
            showCommandFeedback(baseUnit.posX, baseUnit.posY, baseUnit.posZ);
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: q */
    public int getSelectedUnitCount() {
        return this.selectedUnitCount;
    }

    /* JADX INFO: renamed from: a */
    void addSelectedUnitsToCommand(Command command) {
        for (GameObject gameObject : GameObject.fastGameObjectList) {
            if (gameObject instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) gameObject;
                if (orderableUnit.isSelected && canControlUnit(orderableUnit)) {
                    command.addUnitToCommand(orderableUnit);
                }
            }
        }
    }

    /* JADX INFO: renamed from: r */
    public PlayerTeam getSelectedUnitsTeam() {
        GameEngine gameEngine = GameEngine.getInstance();
        for (BaseUnit baseUnit : this.selectedUnitsList) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (orderableUnit.team == gameEngine.playerTeam) {
                    return orderableUnit.team;
                }
            }
        }
        for (BaseUnit baseUnit2 : this.selectedUnitsList) {
            if (baseUnit2 instanceof OrderableUnit) {
                OrderableUnit orderableUnit2 = (OrderableUnit) baseUnit2;
                if (canControlUnit(orderableUnit2)) {
                    return orderableUnit2.team;
                }
            }
        }
        return gameEngine.playerTeam;
    }

    /* JADX INFO: renamed from: s */
    public OrderableUnit getFirstSelectedUnit() {
        for (BaseUnit baseUnit : this.selectedUnitsList) {
            if (baseUnit instanceof OrderableUnit) {
                return (OrderableUnit) baseUnit;
            }
        }
        return null;
    }

    /* JADX INFO: renamed from: t */
    public OrderableUnit getFirstControllableSelectedUnit() {
        for (BaseUnit baseUnit : this.selectedUnitsList) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (canControlUnit(orderableUnit)) {
                    return orderableUnit;
                }
            }
        }
        return null;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: renamed from: a */
    void setActionCommandTarget(final Command e, final AbstractUnitAction s, final boolean boolean3) {
        if (s instanceof WrapperUnitAction) {
            e.addUnitToCommand(((WrapperUnitAction)s).unit);
            return;
        }
        final ActionId actionId = s.getActionId();
        OrderableUnit targetUnit = null;
        int n = -99;
        for (final GameObject gameObject : GameObject.fastGameObjectList) {
            if (gameObject instanceof OrderableUnit) {
                final OrderableUnit am = (OrderableUnit)gameObject;
                if (!am.isSelected || !this.canControlUnit(am)) {
                    continue;
                }
                final AbstractUnitAction validateActionId = am.validateActionId(actionId);
                if (validateActionId == null || !validateActionId.b(am) || (!validateActionId.canAfford(am, true) && !boolean3)) {
                    continue;
                }
                int a = 0;
                if (am instanceof FactoryQueueInterface) {
                    a = ((FactoryQueueInterface)am).a(actionId, true);
                    if (targetUnit != null) {
                        if (!boolean3) {
                            if (a >= n) {
                                break;
                            }
                        }
                        else if (a <= n) {
                            break;
                        }
                    }
                }
                targetUnit = am;
                n = a;
            }
        }
        if (targetUnit != null) {
            e.addUnitToCommand(targetUnit);
        }
    }

    /* JADX INFO: renamed from: a */
    boolean hasInvalidActionTarget(AbstractUnitAction abstractUnitAction, float f, float f2) {
        AbstractUnitAction abstractUnitActionA;
        if (abstractUnitAction instanceof WrapperUnitAction) {
            WrapperUnitAction wrapperUnitAction = (WrapperUnitAction) abstractUnitAction;
            OrderableUnit orderableUnit = wrapperUnitAction.unit;
            AbstractUnitAction abstractUnitActionP_ = wrapperUnitAction.p_();
            boolean z = false;
            if (abstractUnitActionP_.b(orderableUnit) && abstractUnitActionP_.canAfford((BaseUnit) orderableUnit, true) && !orderableUnit.a(abstractUnitActionP_, f, f2)) {
                z = true;
            }
            return z;
        }
        boolean z2 = false;
        for (GameObject gameObject : GameObject.fastGameObjectList) {
            if (gameObject instanceof OrderableUnit) {
                OrderableUnit orderableUnit2 = (OrderableUnit) gameObject;
                if (orderableUnit2.isSelected && canControlUnit(orderableUnit2) && (abstractUnitActionA = orderableUnit2.validateActionId(abstractUnitAction.getActionId())) != null && abstractUnitActionA.b(orderableUnit2) && abstractUnitActionA.canAfford((BaseUnit) orderableUnit2, true)) {
                    if (!orderableUnit2.a(abstractUnitActionA, f, f2)) {
                        z2 = true;
                    } else {
                        return false;
                    }
                }
            }
        }
        if (!z2) {
            return false;
        }
        return true;
    }

    /* JADX INFO: renamed from: a */
    void setActionCommandTargets(Command command, AbstractUnitAction abstractUnitAction) {
        AbstractUnitAction abstractUnitActionA;
        if (abstractUnitAction instanceof WrapperUnitAction) {
            command.addUnitToCommand(((WrapperUnitAction) abstractUnitAction).unit);
            return;
        }
        ActionId actionId = abstractUnitAction.getActionId();
        for (GameObject gameObject : GameObject.fastGameObjectList) {
            if (gameObject instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) gameObject;
                if (orderableUnit.isSelected && canControlUnit(orderableUnit) && (abstractUnitActionA = orderableUnit.validateActionId(actionId)) != null && abstractUnitActionA.b(orderableUnit)) {
                    command.addUnitToCommand(orderableUnit);
                }
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public boolean canAffordActionForSelectedUnits(AbstractUnitAction abstractUnitAction, boolean z) {
        AbstractUnitAction abstractUnitActionA;
        if (abstractUnitAction instanceof WrapperUnitAction) {
            WrapperUnitAction wrapperUnitAction = (WrapperUnitAction) abstractUnitAction;
            return wrapperUnitAction.canAfford((BaseUnit) wrapperUnitAction.unit, true);
        }
        ActionId actionId = abstractUnitAction.getActionId();
        for (BaseUnit baseUnit : this.selectedUnitsList) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (orderableUnit.isSelected && canControlUnit(orderableUnit) && (abstractUnitActionA = orderableUnit.validateActionId(actionId)) != null && abstractUnitActionA.canAfford(orderableUnit, z)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public boolean isActionTargetingGround(AbstractUnitAction abstractUnitAction) {
        AbstractUnitAction abstractUnitActionA;
        ActionId actionId = abstractUnitAction.getActionId();
        if (abstractUnitAction.isLockedAndDisabled()) {
            return false;
        }
        if (abstractUnitAction instanceof WrapperUnitAction) {
            WrapperUnitAction wrapperUnitAction = (WrapperUnitAction) abstractUnitAction;
            return wrapperUnitAction.isTargetingGround((BaseUnit) wrapperUnitAction.unit);
        }
        for (BaseUnit baseUnit : this.selectedUnitsList) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (orderableUnit.isSelected && canControlUnit(orderableUnit) && (abstractUnitActionA = orderableUnit.validateActionId(actionId)) != null && abstractUnitActionA.isTargetingGround((BaseUnit) orderableUnit)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: b */
    public boolean canUpgradeActionForSelectedUnits(AbstractUnitAction abstractUnitAction) {
        AbstractUnitAction abstractUnitActionA;
        if (abstractUnitAction instanceof WrapperUnitAction) {
            WrapperUnitAction wrapperUnitAction = (WrapperUnitAction) abstractUnitAction;
            return wrapperUnitAction.b(wrapperUnitAction.unit);
        }
        ActionId actionId = abstractUnitAction.getActionId();
        for (BaseUnit baseUnit : this.selectedUnitsList) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (orderableUnit.isSelected && canControlUnit(orderableUnit) && (abstractUnitActionA = orderableUnit.validateActionId(actionId)) != null && abstractUnitActionA.b(orderableUnit)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: c */
    public boolean isActionUnavailableForSelectedUnits(AbstractUnitAction abstractUnitAction) {
        AbstractUnitAction abstractUnitActionA;
        boolean z = false;
        if (abstractUnitAction instanceof WrapperUnitAction) {
            WrapperUnitAction wrapperUnitAction = (WrapperUnitAction) abstractUnitAction;
            return wrapperUnitAction.isNotAvailable(wrapperUnitAction.unit);
        }
        ActionId actionId = abstractUnitAction.getActionId();
        for (BaseUnit baseUnit : this.selectedUnitsList) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (orderableUnit.isSelected && canControlUnit(orderableUnit) && (abstractUnitActionA = orderableUnit.validateActionId(actionId)) != null) {
                    if (!abstractUnitActionA.isNotAvailable(orderableUnit)) {
                        return false;
                    }
                    z = true;
                }
            }
        }
        if (!z) {
            return false;
        }
        return true;
    }

    /* JADX INFO: renamed from: d */
    public String getActionIcon(AbstractUnitAction abstractUnitAction) {
        AbstractUnitAction abstractUnitActionA;
        String icon;
        if (abstractUnitAction instanceof WrapperUnitAction) {
            WrapperUnitAction wrapperUnitAction = (WrapperUnitAction) abstractUnitAction;
            return wrapperUnitAction.getIcon(wrapperUnitAction.unit);
        }
        ActionId actionId = abstractUnitAction.getActionId();
        for (BaseUnit baseUnit : this.selectedUnitsList) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (orderableUnit.isSelected && canControlUnit(orderableUnit) && (abstractUnitActionA = orderableUnit.validateActionId(actionId)) != null && abstractUnitActionA.isNotAvailable(orderableUnit) && (icon = abstractUnitActionA.getIcon(orderableUnit)) != null) {
                    return icon;
                }
            }
        }
        return null;
    }

    /* JADX INFO: renamed from: e */
    public UnitList getUnitsForAction(AbstractUnitAction abstractUnitAction) {
        if (abstractUnitAction instanceof WrapperUnitAction) {
            WrapperUnitAction wrapperUnitAction = (WrapperUnitAction) abstractUnitAction;
            this.tempUnitList.clear();
            if (wrapperUnitAction.unit != null) {
                this.tempUnitList.add(wrapperUnitAction.unit);
            }
            return this.tempUnitList;
        }
        return this.selectedUnitsList;
    }

    /* JADX INFO: renamed from: f */
    public String getActionDisplayText(AbstractUnitAction abstractUnitAction) {
        AbstractUnitAction abstractUnitActionA;
        UnitList unitsForAction = getUnitsForAction(abstractUnitAction);
        ActionId actionId = abstractUnitAction.getActionId();
        String str = null;
        boolean z = false;
        for (BaseUnit baseUnit : unitsForAction) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (canControlUnit(orderableUnit) && (abstractUnitActionA = orderableUnit.validateActionId(actionId)) != null) {
                    if (abstractUnitActionA.getPrice() != null && !abstractUnitActionA.getPrice().b(orderableUnit)) {
                        String strA = abstractUnitActionA.getPrice().a((BaseUnit) orderableUnit, 4, true);
                        if (strA != null) {
                            str = strA;
                        }
                    } else {
                        z = true;
                    }
                }
            }
        }
        if (z) {
            return null;
        }
        return str;
    }

    /* JADX INFO: renamed from: u */
    public boolean canSetRallyPoint() {
        if (this.selectedUnitCount == 0) {
            return false;
        }
        for (BaseUnit baseUnit : this.selectedUnitsList) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (!orderableUnit.isSelected) {
                    continue;
                } else {
                    if (!canControlUnit(orderableUnit)) {
                        return false;
                    }
                    ArrayList arrayListN = orderableUnit.getAvailableActions();
                    boolean z = false;
                    if (arrayListN != null) {
                        Iterator it = arrayListN.iterator();
                        while (it.hasNext()) {
                            if (((AbstractUnitAction) it.next()).getActionType() == ActionType.setRally) {
                                z = true;
                            }
                        }
                    }
                    if (!z) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /* JADX INFO: renamed from: a */
    public boolean playCommandSoundForSelectedUnits(CommandType commandType) {
        for (BaseUnit baseUnit : this.selectedUnitsList) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (canControlUnit(orderableUnit) && playCommandSoundForUnit(commandType, orderableUnit)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public boolean playCommandSoundForUnit(CommandType commandType, BaseUnit baseUnit) {
        if (baseUnit instanceof OrderableUnit) {
            OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
            if ((commandType == CommandType.attack || commandType == CommandType.move) && !GameEngine.hasTimeElapsed(this.lastActionConfirmTime, 1000L)) {
                return true;
            }
            if (commandType == CommandType.newSelection) {
                if (GameEngine.getInstance().currentTick < 10) {
                    return false;
                }
                if (!GameEngine.hasTimeElapsed(this.lastActionCancelTime, 1000L)) {
                    return true;
                }
            }
            if (orderableUnit.a(commandType)) {
                if (commandType == CommandType.attack || commandType == CommandType.move) {
                    this.lastActionConfirmTime = GameEngine.getCurrentTimeMillis();
                }
                if (commandType == CommandType.newSelection) {
                    this.lastActionCancelTime = GameEngine.getCurrentTimeMillis();
                    return true;
                }
                return true;
            }
            return false;
        }
        return false;
    }

    /* JADX INFO: renamed from: b */
    public void issueMoveCommand(float worldX, float worldY, Point ScreenPoint) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (!hasMovableUnitsSelected()) {
            if (gameEngine.settingsEngine.quickRally && canSetRallyPoint()) {
                issueRallyPointCommand(worldX, worldY);
                return;
            }
            return;
        }
        Command commandCreateCommandForSelectedUnits = createCommandForSelectedUnits();
        commandCreateCommandForSelectedUnits.isHighPriority = true;
        commandCreateCommandForSelectedUnits.setMoveTarget(worldX, worldY);
        addSelectedUnitsToCommand(commandCreateCommandForSelectedUnits);
        if (!playCommandSoundForSelectedUnits(CommandType.move)) {
            gameEngine.soundEngine.playInterfaceSound(SoundEngine.moveSound, 0.2f);
        }
        Effect effectCreateEffect = gameEngine.effectManager.createEffect(worldX, worldY, 0.0f, EffectType.custom, true, EffectQuality.critical);
        if (effectCreateEffect != null) {
            effectCreateEffect.frameIndex = 8;
            effectCreateEffect.lifeTimer = 30.0f;
            effectCreateEffect.lifeMax = effectCreateEffect.lifeTimer;
            effectCreateEffect.fadeIn = true;
            effectCreateEffect.alpha = 2.0f;
            effectCreateEffect.scaleFrom = 2.8f * calculateCameraOpacity();
            effectCreateEffect.scaleTo = 1.6f * calculateCameraOpacity();
            effectCreateEffect.scaleWithZoom = true;
        }
        if (ScreenPoint != null) {
            Point pointWorldToScreen = gameEngine.minimap.worldToScreen(ScreenPoint.worldX, ScreenPoint.worldY);
            Effect effectCreateEffect2 = gameEngine.effectManager.createEffect(pointWorldToScreen.worldX, pointWorldToScreen.worldY, 0.0f, EffectType.custom, true, EffectQuality.critical);
            if (effectCreateEffect2 != null) {
                effectCreateEffect2.drawLayer = (short) 4;
                effectCreateEffect2.frameIndex = 8;
                effectCreateEffect2.lifeTimer = 35.0f;
                effectCreateEffect2.lifeMax = effectCreateEffect.lifeTimer;
                effectCreateEffect2.fadeIn = true;
                effectCreateEffect2.alpha = 2.0f;
                effectCreateEffect2.scaleFrom = 1.3f;
                effectCreateEffect2.scaleTo = 0.6f;
            }
        }
    }

    /* JADX INFO: renamed from: c */
    public void issueMarchOrMoveCommand(float worldX, float worldY, Point screennPoint) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.isDoubleClick && gameEngine.settingsEngine.doubleClickToAttackMove && hasCombatUnitsSelected() && hasMovableUnitsSelected()) {
            issueAttackCommand(worldX, worldY, screennPoint);
        } else {
            issueMoveCommand(worldX, worldY, screennPoint);
        }
    }

    /* JADX INFO: renamed from: d */
    public void issueAttackCommand(float worldX, float worldY, Point screenPoint) {
        GameEngine gameEngine = GameEngine.getInstance();
        Command commandCreateCommandForSelectedUnits = createCommandForSelectedUnits();
        commandCreateCommandForSelectedUnits.isHighPriority = true;
        commandCreateCommandForSelectedUnits.setAttackMoveTarget(worldX, worldY);
        addSelectedUnitsToCommand(commandCreateCommandForSelectedUnits);
        if (!playCommandSoundForSelectedUnits(CommandType.move)) {
            gameEngine.soundEngine.playInterfaceSound(SoundEngine.moveSound, 0.2f);
        }
        Effect effectCreateEffect = gameEngine.effectManager.createEffect(worldX, worldY, 0.0f, EffectType.custom, true, EffectQuality.critical);
        if (effectCreateEffect != null) {
            effectCreateEffect.stripIndex = 17;
            effectCreateEffect.frameIndex = 2;
            effectCreateEffect.lifeTimer = 30.0f;
            effectCreateEffect.lifeMax = effectCreateEffect.lifeTimer;
            effectCreateEffect.fadeIn = true;
            effectCreateEffect.alpha = 2.0f;
            effectCreateEffect.rotationSpeed = 1.0f;
            effectCreateEffect.scaleFrom = 1.9f * calculateCameraOpacity();
            effectCreateEffect.scaleTo = 3.5f * calculateCameraOpacity();
            effectCreateEffect.scaleWithZoom = true;
        }
        if (screenPoint != null) {
            Point pointWorldToScreen = gameEngine.minimap.worldToScreen(screenPoint.worldX, screenPoint.worldY);
            Effect effectCreateEffect2 = gameEngine.effectManager.createEffect(pointWorldToScreen.worldX, pointWorldToScreen.worldY, 0.0f, EffectType.custom, true, EffectQuality.critical);
            if (effectCreateEffect2 != null) {
                effectCreateEffect2.drawLayer = (short) 4;
                effectCreateEffect2.frameIndex = 9;
                effectCreateEffect2.lifeTimer = 35.0f;
                effectCreateEffect2.lifeMax = effectCreateEffect.lifeTimer;
                effectCreateEffect2.fadeIn = true;
                effectCreateEffect2.alpha = 2.0f;
                effectCreateEffect2.scaleFrom = 1.3f;
                effectCreateEffect2.scaleTo = 0.6f;
            }
        }
    }

    /* JADX INFO: renamed from: v */
    public void stopSelectedUnits() {
        GameEngine gameEngine = GameEngine.getInstance();
        Command commandCreateCommandForSelectedUnits = createCommandForSelectedUnits();
        commandCreateCommandForSelectedUnits.setClearExistingOrders();
        addSelectedUnitsToCommand(commandCreateCommandForSelectedUnits);
        gameEngine.soundEngine.playInterfaceSound(SoundEngine.moveSound, 0.2f);
    }

    /* JADX INFO: renamed from: a */
    public void prepareUnitActionCommand(AbstractUnitAction abstractUnitAction, PointF pointF, BaseUnit baseUnit, Command command) {
        if (abstractUnitAction instanceof CustomAction) {
            GameEngine.getInstance();
        }
    }

    /* JADX INFO: renamed from: b */
    public void executeUnitAction(AbstractUnitAction abstractUnitAction, float f, float f2) {
        GameEngine gameEngine = GameEngine.getInstance();
        PointF pointF = new PointF(f, f2);
        Command commandCreateCommandForSelectedUnits = createCommandForSelectedUnits();
        if (!abstractUnitAction.isOnlyOneUnitAtATime()) {
            setActionCommandTargets(commandCreateCommandForSelectedUnits, abstractUnitAction);
        } else {
            setActionCommandTarget(commandCreateCommandForSelectedUnits, abstractUnitAction, false);
        }
        commandCreateCommandForSelectedUnits.setActionTarget(abstractUnitAction.getActionId(), pointF, (BaseUnit) null);
        prepareUnitActionCommand(abstractUnitAction, pointF, (BaseUnit) null, commandCreateCommandForSelectedUnits);
        if (!abstractUnitAction.a(f, f2)) {
            gameEngine.soundEngine.playInterfaceSound(SoundEngine.moveSound, 0.2f);
            Effect effectCreateEffect = gameEngine.effectManager.createEffect(f, f2, 0.0f, EffectType.custom, true, EffectQuality.critical);
            if (effectCreateEffect != null) {
                effectCreateEffect.frameIndex = 9;
                effectCreateEffect.lifeTimer = 60.0f;
                effectCreateEffect.lifeMax = effectCreateEffect.lifeTimer;
                effectCreateEffect.fadeIn = true;
                effectCreateEffect.alpha = 2.0f;
                effectCreateEffect.scaleFrom = 3.8f * calculateCameraOpacity();
                effectCreateEffect.scaleTo = 2.0f * calculateCameraOpacity();
                effectCreateEffect.scaleWithZoom = true;
                effectCreateEffect.rotationSpeed = 1.5f;
            }
        }
    }

    /* JADX INFO: renamed from: b */
    public void issueReclaimCommand(BaseUnit baseUnit) {
        GameEngine gameEngine = GameEngine.getInstance();
        Command commandCreateCommandForSelectedUnits = createCommandForSelectedUnits();
        addSelectedUnitsToCommand(commandCreateCommandForSelectedUnits);
        commandCreateCommandForSelectedUnits.setReclaimTarget(baseUnit);
        gameEngine.soundEngine.playInterfaceSound(SoundEngine.moveSound, 0.2f);
        Effect effectCreateEffect = gameEngine.effectManager.createEffect(baseUnit.posX, baseUnit.posY, baseUnit.posZ, EffectType.custom, true, EffectQuality.critical);
        if (effectCreateEffect != null) {
            effectCreateEffect.frameIndex = 12;
            effectCreateEffect.lifeTimer = 25.0f;
            effectCreateEffect.lifeMax = effectCreateEffect.lifeTimer;
            effectCreateEffect.fadeIn = true;
            effectCreateEffect.alpha = 2.0f;
            effectCreateEffect.scaleWithZoom = true;
            effectCreateEffect.scaleFrom = 1.2f * calculateCameraOpacity();
            effectCreateEffect.scaleTo = 1.8f * calculateCameraOpacity();
        }
    }

    /* JADX INFO: renamed from: b */
    public void issueRallyPointCommand(float f, float f2) {
        GameEngine gameEngine = GameEngine.getInstance();
        Command commandCreateBaseCommand = createBaseCommand();
        addSelectedUnitsToCommand(commandCreateBaseCommand);
        commandCreateBaseCommand.setRallyPoint(new PointF(f, f2));
        gameEngine.soundEngine.playInterfaceSound(SoundEngine.moveSound, 0.2f);
        Effect effectCreateEffect = gameEngine.effectManager.createEffect(f, f2, 0.0f, EffectType.custom, true, EffectQuality.critical);
        if (effectCreateEffect != null) {
            effectCreateEffect.frameIndex = 8;
            effectCreateEffect.lifeTimer = 65.0f;
            effectCreateEffect.lifeMax = effectCreateEffect.lifeTimer;
            effectCreateEffect.fadeIn = true;
            effectCreateEffect.alpha = 2.0f;
            effectCreateEffect.scaleWithZoom = true;
            effectCreateEffect.rotationSpeed = 2.0f;
            effectCreateEffect.scaleFrom = 2.0f * calculateCameraOpacity();
            effectCreateEffect.scaleTo = 1.5f * calculateCameraOpacity();
        }
    }

    /* JADX INFO: renamed from: a */
    public void issueMapPingCommand(float f, float f2, Point point, PingMapAction pingMapAction) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (!gameEngine.settingsEngine.showMapPingsOnBattlefield && !gameEngine.settingsEngine.showMapPingsOnMinimap) {
            showMediumPriorityMessage("Cannot send map ping, you have disabled both battlefield and minimap pings in your settings");
            return;
        }
        createCommandForSelectedUnits().setActionTarget(pingMapAction.getActionId(), new PointF(f, f2), (BaseUnit) null);
        if (this.lastUpdateTime == 0 || this.lastUpdateTime + 15000 < System.currentTimeMillis()) {
            this.lastUpdateTime = System.currentTimeMillis();
            gameEngine.networkEngine.l("MAP PING - [i:" + pingMapAction.K() + "]");
        }
    }

    /* JADX INFO: renamed from: a */
    public void sendMapPing(float f, float f2, PlayerTeam playerTeam, PingMapAction pingMapAction) {
        Effect effectCreateEffect;
        GameEngine gameEngine = GameEngine.getInstance();
        int iOrdinal = 7 + pingMapAction.pingType.ordinal();
        if (!gameEngine.settingsEngine.showMapPingsOnBattlefield && !gameEngine.settingsEngine.showMapPingsOnMinimap) {
            if (!this.isFirstUpdate && !gameEngine.replayEngine.j()) {
                this.isFirstUpdate = true;
                this.messageManager.addMessage((String) null, "[WARNING: A player send a map ping, but you have disabled both battlefield and minimap pings in your settings]");
                return;
            }
            return;
        }
        if (gameEngine.settingsEngine.showMapPingsOnBattlefield) {
            Effect effectCreateEffect2 = gameEngine.effectManager.createEffect(f, f2, 0.0f, EffectType.custom, true, EffectQuality.critical);
            if (effectCreateEffect2 != null) {
                effectCreateEffect2.stripIndex = 9;
                effectCreateEffect2.frameIndex = 6;
                effectCreateEffect2.alpha = 0.7f;
                effectCreateEffect2.lifeTimer = 490.0f;
                effectCreateEffect2.lifeMax = effectCreateEffect2.lifeTimer;
                effectCreateEffect2.fadeIn = true;
                effectCreateEffect2.bobAmplitude = 6.0f;
                effectCreateEffect2.bobPeriod = 60.0f;
                effectCreateEffect2.posY -= effectCreateEffect2.bobAmplitude;
                effectCreateEffect2.scaleFrom = 2.0f * 1.0f;
                effectCreateEffect2.scaleTo = effectCreateEffect2.scaleFrom;
                effectCreateEffect2.verticalAnchorOffset = -0.5f;
                effectCreateEffect2.scaleWithZoom = true;
                if (playerTeam != null) {
                    effectCreateEffect2.startColor = playerTeam.getTeamColorArgb();
                    if (GameEngine.isAndroidPlatform()) {
                        effectCreateEffect2.lightingColorFilter = new LightingColorFilter(effectCreateEffect2.startColor, 0);
                    }
                }
            }
            if (iOrdinal != -1 && (effectCreateEffect = gameEngine.effectManager.createEffect(f, f2, 0.0f, EffectType.custom, true, EffectQuality.critical)) != null) {
                effectCreateEffect.stripIndex = 9;
                effectCreateEffect.frameIndex = iOrdinal;
                effectCreateEffect.lifeTimer = 490.0f;
                effectCreateEffect.lifeMax = effectCreateEffect.lifeTimer;
                effectCreateEffect.fadeIn = true;
                effectCreateEffect.alpha = 1.2f;
                effectCreateEffect.bobAmplitude = 6.0f;
                effectCreateEffect.bobPeriod = 60.0f;
                effectCreateEffect.posY -= effectCreateEffect.bobAmplitude;
                effectCreateEffect.scaleFrom = 2.0f * 1.0f;
                effectCreateEffect.scaleTo = effectCreateEffect.scaleFrom;
                effectCreateEffect.verticalAnchorOffset = -0.7f;
                effectCreateEffect.scaleWithZoom = true;
            }
        }
        if (gameEngine.settingsEngine.showMapPingsOnMinimap) {
            Point pointWorldToScreen = gameEngine.minimap.worldToScreen(f, f2);
            Effect effectCreateEffect3 = gameEngine.effectManager.createEffect(pointWorldToScreen.worldX, pointWorldToScreen.worldY, 0.0f, EffectType.custom, true, EffectQuality.critical);
            if (effectCreateEffect3 != null) {
                effectCreateEffect3.drawLayer = (short) 4;
                effectCreateEffect3.stripIndex = 9;
                effectCreateEffect3.frameIndex = 6;
                effectCreateEffect3.alpha = 0.8f;
                effectCreateEffect3.lifeTimer = 470.0f;
                effectCreateEffect3.lifeMax = effectCreateEffect3.lifeTimer;
                effectCreateEffect3.fadeIn = true;
                effectCreateEffect3.posY -= 2.0f;
                effectCreateEffect3.bobAmplitude = 2.0f;
                effectCreateEffect3.bobPeriod = 60.0f;
                effectCreateEffect3.verticalAnchorOffset = -0.5f;
                if (playerTeam != null) {
                    effectCreateEffect3.startColor = playerTeam.getTeamColorArgb();
                    if (GameEngine.isAndroidPlatform()) {
                        effectCreateEffect3.lightingColorFilter = new LightingColorFilter(effectCreateEffect3.startColor, 0);
                    }
                }
                effectCreateEffect3.scaleFrom = 1.0f;
                effectCreateEffect3.scaleTo = 1.0f;
            }
            Effect effectCreateEffect4 = gameEngine.effectManager.createEffect(pointWorldToScreen.worldX, pointWorldToScreen.worldY, 0.0f, EffectType.custom, true, EffectQuality.critical);
            if (effectCreateEffect4 != null) {
                effectCreateEffect4.drawLayer = (short) 4;
                effectCreateEffect4.stripIndex = 9;
                effectCreateEffect4.frameIndex = iOrdinal;
                effectCreateEffect4.lifeTimer = 470.0f;
                effectCreateEffect4.lifeMax = effectCreateEffect4.lifeTimer;
                effectCreateEffect4.fadeIn = true;
                effectCreateEffect4.alpha = 0.8f;
                effectCreateEffect4.posY -= 2.0f;
                effectCreateEffect4.bobAmplitude = 2.0f;
                effectCreateEffect4.bobPeriod = 60.0f;
                if (playerTeam != null) {
                }
                effectCreateEffect4.scaleFrom = 1.0f;
                effectCreateEffect4.scaleTo = 1.0f;
                effectCreateEffect4.verticalAnchorOffset = -0.7f;
            }
        }
    }

    /* JADX INFO: renamed from: w */
    public Command createBaseCommand() {
        GameEngine gameEngine = GameEngine.getInstance();
        Command commandCreateCommandForTeam = gameEngine.commandController.createCommandForTeam(gameEngine.playerTeam);
        if (gameEngine.networkEngine.networkGameActive) {
            commandCreateCommandForTeam.sourceTeam = gameEngine.playerTeam;
        }
        return commandCreateCommandForTeam;
    }

    /* JADX INFO: renamed from: x */
    public Command createCommandForSelectedUnits() {
        GameEngine gameEngine = GameEngine.getInstance();
        Command commandCreateBaseCommand = createBaseCommand();
        if (isShiftKeyPressed(gameEngine)) {
            commandCreateBaseCommand.isQueued = true;
        }
        return commandCreateBaseCommand;
    }

    /* JADX INFO: renamed from: c */
    public void issueAttackTargetCommand(BaseUnit baseUnit) {
        GameEngine gameEngine = GameEngine.getInstance();
        Command commandCreateCommandForSelectedUnits = createCommandForSelectedUnits();
        commandCreateCommandForSelectedUnits.setAttackTarget(baseUnit);
        addSelectedUnitsToCommand(commandCreateCommandForSelectedUnits);
        if (!playCommandSoundForSelectedUnits(CommandType.attack)) {
            gameEngine.soundEngine.playInterfaceSound(SoundEngine.attack2Sound, 1.0f);
        }
        Effect effectCreateEffect = gameEngine.effectManager.createEffect(baseUnit.posX, baseUnit.posY, baseUnit.posZ, EffectType.custom, true, EffectQuality.critical);
        if (effectCreateEffect != null) {
            effectCreateEffect.parentObject = baseUnit;
            effectCreateEffect.posX = 0.0f;
            effectCreateEffect.posY = 0.0f;
            effectCreateEffect.posZ = 0.0f;
            effectCreateEffect.frameIndex = 9;
            effectCreateEffect.lifeTimer = 35.0f;
            effectCreateEffect.lifeMax = effectCreateEffect.lifeTimer;
            effectCreateEffect.fadeIn = true;
            effectCreateEffect.alpha = 1.5f;
            effectCreateEffect.scaleWithZoom = true;
            effectCreateEffect.rotationSpeed = 0.8f;
            effectCreateEffect.scaleFrom = 1.9f * calculateCameraOpacity();
            effectCreateEffect.scaleTo = 3.3f * calculateCameraOpacity();
        }
        Effect effectCreateEffect2 = gameEngine.effectManager.createEffect(baseUnit.posX, baseUnit.posY, baseUnit.posZ, EffectType.custom, true, EffectQuality.critical);
        if (effectCreateEffect2 != null) {
            effectCreateEffect2.parentObject = baseUnit;
            effectCreateEffect2.posX = 0.0f;
            effectCreateEffect2.posY = 0.0f;
            effectCreateEffect2.posZ = 0.0f;
            effectCreateEffect2.stripIndex = 17;
            effectCreateEffect2.frameIndex = 0;
            effectCreateEffect2.lifeTimer = 25.0f;
            effectCreateEffect2.lifeMax = effectCreateEffect2.lifeTimer;
            effectCreateEffect2.fadeIn = true;
            effectCreateEffect2.alpha = 1.0f;
            effectCreateEffect2.scaleWithZoom = true;
            effectCreateEffect2.rotationSpeed = 0.8f;
            effectCreateEffect2.scaleFrom = 2.2f * calculateCameraOpacity();
            effectCreateEffect2.scaleTo = 1.1f * calculateCameraOpacity();
        }
    }

    /* JADX INFO: renamed from: d */
    public void issueRepairCommand(BaseUnit baseUnit) {
        GameEngine gameEngine = GameEngine.getInstance();
        Command commandCreateCommandForSelectedUnits = createCommandForSelectedUnits();
        addSelectedUnitsToCommand(commandCreateCommandForSelectedUnits);
        commandCreateCommandForSelectedUnits.setRepairTarget(baseUnit);
        gameEngine.soundEngine.playInterfaceSound(SoundEngine.attack2Sound, 1.0f);
        Effect effectCreateEffect = gameEngine.effectManager.createEffect(baseUnit.posX, baseUnit.posY, baseUnit.posZ, EffectType.custom, true, EffectQuality.critical);
        if (effectCreateEffect != null) {
            effectCreateEffect.frameIndex = 10;
            effectCreateEffect.lifeTimer = 35.0f;
            effectCreateEffect.lifeMax = effectCreateEffect.lifeTimer;
            effectCreateEffect.fadeIn = true;
            effectCreateEffect.alpha = 2.0f;
            effectCreateEffect.scaleWithZoom = true;
            effectCreateEffect.scaleFrom = 1.5f * calculateCameraOpacity();
            effectCreateEffect.scaleTo = 2.2f * calculateCameraOpacity();
        }
    }

    /* JADX INFO: renamed from: e */
    public void issueGuardCommand(BaseUnit baseUnit) {
        GameEngine gameEngine = GameEngine.getInstance();
        Command commandCreateCommandForSelectedUnits = createCommandForSelectedUnits();
        addSelectedUnitsToCommand(commandCreateCommandForSelectedUnits);
        commandCreateCommandForSelectedUnits.setGuardTarget(baseUnit);
        gameEngine.soundEngine.playInterfaceSound(SoundEngine.attack2Sound, 1.0f);
        Effect effectCreateEffect = gameEngine.effectManager.createEffect(baseUnit.posX, baseUnit.posY, baseUnit.posZ, EffectType.custom, true, EffectQuality.critical);
        if (effectCreateEffect != null) {
            effectCreateEffect.stripIndex = 17;
            effectCreateEffect.frameIndex = 1;
            effectCreateEffect.lifeTimer = 40.0f;
            effectCreateEffect.lifeMax = effectCreateEffect.lifeTimer;
            effectCreateEffect.fadeIn = true;
            effectCreateEffect.alpha = 1.0f;
            effectCreateEffect.scaleWithZoom = true;
            effectCreateEffect.rotationSpeed = 0.0f;
            effectCreateEffect.scaleFrom = 1.2f * calculateCameraOpacity();
            effectCreateEffect.scaleTo = 1.9f * calculateCameraOpacity();
        }
    }

    /* JADX INFO: renamed from: a */
    public void showCommandFeedback(float f, float f2, float f3) {
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.soundEngine.playInterfaceSound(SoundEngine.interfaceErrorSound, 0.2f);
        Effect effectCreateEffect = gameEngine.effectManager.createEffect(f, f2, f3, EffectType.custom, true, EffectQuality.critical);
        if (effectCreateEffect != null) {
            effectCreateEffect.stripIndex = 9;
            effectCreateEffect.frameIndex = 14;
            effectCreateEffect.lifeTimer = 10.0f;
            effectCreateEffect.lifeMax = effectCreateEffect.lifeTimer;
            effectCreateEffect.fadeIn = true;
            effectCreateEffect.alpha = 2.0f;
            effectCreateEffect.rotationSpeed = 0.0f;
            effectCreateEffect.scaleFrom = 1.1f * calculateCameraOpacity();
            effectCreateEffect.scaleTo = 1.6f * calculateCameraOpacity();
            effectCreateEffect.scaleWithZoom = true;
        }
    }

    /* JADX INFO: renamed from: a */
    public void executeUnitAction(float f, float f2, Point point, boolean z) {
        GameEngine gameEngine = GameEngine.getInstance();
        Command commandCreateCommandForSelectedUnits = createCommandForSelectedUnits();
        addSelectedUnitsToCommand(commandCreateCommandForSelectedUnits);
        commandCreateCommandForSelectedUnits.setPatrolTarget(f, f2);
        if (!z) {
            commandCreateCommandForSelectedUnits.isQueued = true;
        }
        gameEngine.soundEngine.playInterfaceSound(SoundEngine.moveSound, 0.2f);
        Effect effectCreateEffect = gameEngine.effectManager.createEffect(f, f2, 0.0f, EffectType.custom, true, EffectQuality.critical);
        if (effectCreateEffect != null) {
            effectCreateEffect.stripIndex = 17;
            effectCreateEffect.frameIndex = 0;
            effectCreateEffect.lifeTimer = 40.0f;
            effectCreateEffect.lifeMax = effectCreateEffect.lifeTimer;
            effectCreateEffect.fadeIn = true;
            effectCreateEffect.alpha = 2.0f;
            effectCreateEffect.rotationSpeed = 8.0f;
            effectCreateEffect.scaleFrom = 1.1f * calculateCameraOpacity();
            effectCreateEffect.scaleTo = 1.9f * calculateCameraOpacity();
            effectCreateEffect.scaleWithZoom = true;
        }
        if (point != null) {
            Point pointWorldToScreen = gameEngine.minimap.worldToScreen(point.worldX, point.worldY);
            Effect effectCreateEffect2 = gameEngine.effectManager.createEffect(pointWorldToScreen.worldX, pointWorldToScreen.worldY, 0.0f, EffectType.custom, true, EffectQuality.critical);
            if (effectCreateEffect2 != null) {
                effectCreateEffect2.drawLayer = (short) 4;
                effectCreateEffect2.frameIndex = 9;
                effectCreateEffect2.lifeTimer = 35.0f;
                effectCreateEffect2.lifeMax = effectCreateEffect.lifeTimer;
                effectCreateEffect2.fadeIn = true;
                effectCreateEffect2.alpha = 2.0f;
                effectCreateEffect2.scaleFrom = 1.3f;
                effectCreateEffect2.scaleTo = 0.6f;
            }
        }
    }

    /* JADX INFO: renamed from: f */
    public void issueLoadIntoCommand(BaseUnit baseUnit) {
        GameEngine gameEngine = GameEngine.getInstance();
        Command commandCreateCommandForSelectedUnits = createCommandForSelectedUnits();
        addSelectedUnitsToCommand(commandCreateCommandForSelectedUnits);
        commandCreateCommandForSelectedUnits.setLoadIntoTarget(baseUnit);
        gameEngine.soundEngine.playInterfaceSound(SoundEngine.attack2Sound, 1.0f);
        Effect effectCreateEffect = gameEngine.effectManager.createEffect(baseUnit.posX, baseUnit.posY, baseUnit.posZ, EffectType.custom, true, EffectQuality.critical);
        if (effectCreateEffect != null) {
            effectCreateEffect.frameIndex = 11;
            effectCreateEffect.lifeTimer = 25.0f;
            effectCreateEffect.lifeMax = effectCreateEffect.lifeTimer;
            effectCreateEffect.fadeIn = true;
            effectCreateEffect.alpha = 2.0f;
            effectCreateEffect.scaleWithZoom = true;
            effectCreateEffect.scaleFrom = 1.8f * calculateCameraOpacity();
            effectCreateEffect.scaleTo = 1.6f * calculateCameraOpacity();
        }
    }

    /* JADX INFO: renamed from: g */
    public void issueAttackCommand(BaseUnit baseUnit) {
        GameEngine gameEngine = GameEngine.getInstance();
        Command commandCreateCommandForSelectedUnits = createCommandForSelectedUnits();
        addSelectedUnitsToCommand(commandCreateCommandForSelectedUnits);
        commandCreateCommandForSelectedUnits.setLoadUpTarget(baseUnit);
        gameEngine.soundEngine.playInterfaceSound(SoundEngine.attack2Sound, 1.0f);
        Effect effectCreateEffect = gameEngine.effectManager.createEffect(baseUnit.posX, baseUnit.posY, baseUnit.posZ, EffectType.custom, true, EffectQuality.critical);
        if (effectCreateEffect != null) {
            effectCreateEffect.frameIndex = 11;
            effectCreateEffect.lifeTimer = 25.0f;
            effectCreateEffect.lifeMax = effectCreateEffect.lifeTimer;
            effectCreateEffect.fadeIn = true;
            effectCreateEffect.alpha = 2.0f;
            effectCreateEffect.scaleWithZoom = true;
            effectCreateEffect.scaleFrom = 1.8f * calculateCameraOpacity();
            effectCreateEffect.scaleTo = 1.6f * calculateCameraOpacity();
        }
    }

    /* JADX INFO: renamed from: a */
    public BaseUnit findUnitAtPosition(float float1, float float2, boolean boolean3) {
        GameEngine var4 = GameEngine.getInstance();
        BaseUnit var5 = null;
        float var6 = -1.0F;
        float var7 = 10.0F / var4.zoom;
        float var8 = 5.0F / var4.zoom;
        float var9 = 5.0F / var4.zoom;
        PlayerTeam var10 = this.getSelectedUnitsTeam();

        for (BaseUnit var12 : BaseUnit.bE) {
            if ((boolean3 ? !var12.t() : !var12.isUnselectableAsTarget()) && !var12.isDead && var12.transportContainer == null) {
                float var13 = Utility.distanceSq(float1, float2, var12.posX, var12.posY - var12.posZ);
                float var14 = var12.radius;
                if (!var12.isSelected) {
                    var14 += var7;
                } else {
                    var14 += var8;
                }

                boolean var15 = var10.c(var12.team);
                if (var15) {
                    var14 += var9;
                }

                if (var13 < var14 * var14 && (!var15 || var12.isVisibleToEnemies()) && (var5 == null || var13 < var6)) {
                    var5 = var12;
                    var6 = var13;
                }
            }
        }

        return var5 != null && var5.team != var4.playerTeam && !var5.isVisibleToLocalPlayer() ? null : var5;
    }

    /* JADX INFO: renamed from: b */
    public void selectUnitsInArea(float x, float y, float radius) {
        GameEngine gameEngine = GameEngine.getInstance();
        for (GameObject gameObject : GameObject.fastGameObjectList) {
            if (gameObject instanceof BaseUnit) {
                BaseUnit baseUnit = (BaseUnit) gameObject;
                if (!baseUnit.isDead && baseUnit.transportContainer == null && baseUnit.team == gameEngine.playerTeam && Utility.distanceSq(x, y, baseUnit.posX, baseUnit.posY - baseUnit.posZ) < radius * radius) {
                    selectUnit(baseUnit);
                }
            }
        }
    }

    /* JADX INFO: renamed from: h */
    public void selectAllSimilarUnits(BaseUnit baseUnit) {
        this.lastSelectedUnit = null;
        GameEngine gameEngine = GameEngine.getInstance();
        for (GameObject gameObject : GameObject.fastGameObjectList) {
            if (gameObject instanceof BaseUnit) {
                BaseUnit baseUnit2 = (BaseUnit) gameObject;
                if (!baseUnit2.isDead && baseUnit2.transportContainer == null && baseUnit2.team == baseUnit.team && baseUnit2.isVisibleOnScreen() && GameInterfaceRenderer.isActionVisibleForSelection(baseUnit2, baseUnit) && (baseUnit2.team == gameEngine.playerTeam || baseUnit2.isVisibleToLocalPlayer())) {
                    selectUnit(baseUnit2);
                }
            }
        }
    }

    /* JADX INFO: renamed from: y */
    public void clearSelection() {
        this.lastSelectedUnit = null;
        for (GameObject gameObject : GameObject.fastGameObjectList) {
            if (gameObject instanceof BaseUnit) {
                ((BaseUnit) gameObject).isSelected = false;
            }
        }
        this.selectedUnitCount = 0;
        this.selectionChangeCounter++;
        this.selectedUnitsList.clear();
        notifySelectionChanged();
    }

    /* JADX INFO: renamed from: i */
    public boolean canUnitBeSelected(BaseUnit baseUnit) {
        if (baseUnit.t()) {
            return false;
        }
        PlayerTeam playerTeam = GameEngine.getInstance().playerTeam;
        if (playerTeam != null && playerTeam.c(baseUnit.team) && !baseUnit.isVisibleToEnemies()) {
            return false;
        }
        return true;
    }

    /* JADX INFO: renamed from: j */
    public boolean selectUnit(BaseUnit baseUnit) {
        if (baseUnit.isSelected) {
            return true;
        }
        if (!canUnitBeSelected(baseUnit)) {
            return false;
        }
        addToSelection(baseUnit);
        playCommandSoundForUnit(CommandType.newSelection, baseUnit);
        return true;
    }

    /* JADX INFO: renamed from: k */
    public void addToSelection(BaseUnit baseUnit) {
        if (baseUnit.isSelected || !canUnitBeSelected(baseUnit)) {
            return;
        }
        baseUnit.isSelected = true;
        baseUnit.lastSelectedTick = GameEngine.getInstance().renderTimeMillis;
        this.selectedUnitCount++;
        if (!(baseUnit instanceof EditorOrBuilder)) {
            lastSelectedNonBuilderUnit = baseUnit;
        }
        this.selectionChangeCounter++;
        this.selectedUnitsList.add(baseUnit);
        notifySelectionChanged();
    }

    /* JADX INFO: renamed from: z */
    public static ModInfo getLastSelectedUnitModInfo() {
        UnitType unitTypeR;
        BaseUnit baseUnit = lastSelectedNonBuilderUnit;
        if (baseUnit == null || (unitTypeR = baseUnit.r()) == null || !(unitTypeR instanceof CustomUnitConfig)) {
            return null;
        }
        return ((CustomUnitConfig) unitTypeR).modInfo;
    }

    /* JADX INFO: renamed from: a */
    public void drawTeamResources(BaseUnit baseUnit, boolean z) {
        if (!z) {
            selectUnit(baseUnit);
        } else if (baseUnit.isSelected) {
            deselectUnit(baseUnit);
        } else {
            selectUnit(baseUnit);
        }
    }

    /* JADX INFO: renamed from: l */
    public void deselectUnit(BaseUnit baseUnit) {
        if (baseUnit.isSelected) {
            baseUnit.isSelected = false;
            this.selectedUnitCount--;
            this.selectedUnitsList.remove(baseUnit);
            this.selectionChangeCounter++;
            notifySelectionChanged();
        }
    }

    /* JADX INFO: renamed from: A */
    public boolean hasUpgradedUnitsSelected() {
        if (getSelectedUnitCount() == 0) {
            return false;
        }
        for (BaseUnit baseUnit : this.selectedUnitsList) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (orderableUnit.isSelected && canControlUnit(orderableUnit)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: B */
    public boolean hasCombatUnitsSelected() {
        if (getSelectedUnitCount() == 0) {
            return false;
        }
        for (BaseUnit baseUnit : this.selectedUnitsList) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (orderableUnit.isSelected && canControlUnit(orderableUnit) && orderableUnit.canAttack()) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: C */
    public boolean hasMovableUnitsSelected() {
        if (getSelectedUnitCount() == 0) {
            return false;
        }
        for (BaseUnit baseUnit : this.selectedUnitsList) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (orderableUnit.isSelected && orderableUnit.aS() && canControlUnit(orderableUnit)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: D */
    public boolean areAllSelectedUnitsAirborne() {
        if (getSelectedUnitCount() == 0) {
            return true;
        }
        for (BaseUnit baseUnit : this.selectedUnitsList) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (orderableUnit.isSelected && !orderableUnit.i()) {
                    return false;
                }
            }
        }
        return true;
    }

    /* JADX INFO: renamed from: m */
    public boolean canControlUnit(BaseUnit baseUnit) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (baseUnit.canNotBeGivenOrdersByPlayer()) {
            return false;
        }
        if (baseUnit.team == gameEngine.playerTeam) {
            return true;
        }
        if ((baseUnit.team != null && baseUnit.team.b(gameEngine.playerTeam)) || gameEngine.isGameStarted || gameEngine.replayEngine.j()) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: n */
    public boolean canSelectedUnitsLoadInto(BaseUnit baseUnit) {
        if (getSelectedUnitCount() == 0) {
            return false;
        }
        for (BaseUnit baseUnit2 : this.selectedUnitsList) {
            if (baseUnit2 instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit2;
                if (orderableUnit.isSelected && orderableUnit != baseUnit && canControlUnit(orderableUnit) && baseUnit.d(orderableUnit, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: o */
    public boolean canSelectedUnitsLoadUp(BaseUnit baseUnit) {
        if (getSelectedUnitCount() == 0) {
            return false;
        }
        for (BaseUnit baseUnit2 : this.selectedUnitsList) {
            if (baseUnit2 instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit2;
                if (orderableUnit.isSelected && orderableUnit != baseUnit && canControlUnit(orderableUnit) && orderableUnit.d(baseUnit, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: p */
    public boolean canSelectedUnitsRepair(BaseUnit baseUnit) {
        if (getSelectedUnitCount() == 0) {
            return false;
        }
        for (BaseUnit baseUnit2 : this.selectedUnitsList) {
            if (baseUnit2 instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit2;
                if (orderableUnit.isSelected && orderableUnit != baseUnit && canControlUnit(orderableUnit) && orderableUnit.canRepairTarget(baseUnit)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: q */
    public boolean canSelectedUnitsReachTargetByPathfinding(BaseUnit baseUnit) {
        if (getSelectedUnitCount() == 0) {
            return false;
        }
        for (BaseUnit baseUnit2 : this.selectedUnitsList) {
            if (baseUnit2 instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit2;
                if (orderableUnit.isSelected && orderableUnit != baseUnit && canControlUnit(orderableUnit) && PathfindingUtils.a(orderableUnit, baseUnit)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: E */
    public void emptyMethod() {
    }

    /* JADX INFO: renamed from: F */
    public boolean returnsFalse() {
        return false;
    }

    @Override // com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        this.interfaceRenderer.a(gameOutputStream);
        gameOutputStream.writeByte(1);
        gameOutputStream.writeInt(this.buildQueueId);
    }

    public void a(GameInputStream gameInputStream, boolean z) throws IOException {
        this.interfaceRenderer.a(gameInputStream, z);
        if (gameInputStream.readByte() >= 1) {
            this.buildQueueId = gameInputStream.readInt();
        }
    }

    /* JADX INFO: renamed from: a */
    public void drawUnitMovePreview(OrderableUnit orderableUnit, float f, float f2, float f3, float f4, boolean z, ArrayList arrayList, BaseUnit baseUnit) {
        GameEngine gameEngine = GameEngine.getInstance();
        float f5 = orderableUnit.posX;
        float f6 = orderableUnit.posY;
        OrderableUnit orderableUnit2 = null;
        BaseUnit buildingBlockoutUnit = BaseUnit.getBuildingBlockoutUnit(orderableUnit.r());
        if (!(buildingBlockoutUnit instanceof OrderableUnit)) {
            GameEngine.log("buildingBlockoutUnit not OrderableUnit is: " + buildingBlockoutUnit.getClass().getName());
        } else {
            orderableUnit2 = (OrderableUnit) buildingBlockoutUnit;
        }
        boolean z2 = false;
        gameEngine.tileMap.updateCursorTileIndexFromWorldPoint(f, f2);
        float f7 = gameEngine.tileMap.cursorTileX;
        float f8 = gameEngine.tileMap.cursorTileY;
        float fCZ = f7 + orderableUnit.getTileOffsetX();
        float fDa = f8 + orderableUnit.getTileOffsetY();
        float fCZ2 = f3 + orderableUnit.getTileOffsetX();
        float fDa2 = f4 + orderableUnit.getTileOffsetY();
        float fDistance = Utility.distance(fCZ, fDa, fCZ2, fDa2);
        float angleBetweenPoints = Utility.getAngleBetweenPoints(fCZ, fDa, fCZ2, fDa2);
        int i = 0;
        float f9 = 0.0f;
        while (true) {
            float f10 = f9;
            if (f10 <= fDistance) {
                gameEngine.tileMap.updateCursorTileIndexFromWorldPoint((fCZ + (Utility.fastCos(angleBetweenPoints) * f10)) - orderableUnit.getTileOffsetX(), (fDa + (Utility.fastSin(angleBetweenPoints) * f10)) - orderableUnit.getTileOffsetY());
                float f11 = gameEngine.tileMap.cursorTileX;
                float f12 = gameEngine.tileMap.cursorTileY;
                float fCZ3 = f11 + orderableUnit.getTileOffsetX();
                float fDa3 = f12 + orderableUnit.getTileOffsetY();
                orderableUnit.posX = fCZ3;
                orderableUnit.posY = fDa3;
                if ((!z2 || orderableUnit2 == null || (!BuildPreview.doUnitsOverlap(orderableUnit, orderableUnit2) && !orderableUnit.isUnitOverlappingRadius(orderableUnit2))) && 0 == 0) {
                    boolean zValidateBuildingPlacement = validateBuildingPlacement(orderableUnit, fCZ3, fDa3, z, false, baseUnit);
                    if (arrayList != null && zValidateBuildingPlacement) {
                        arrayList.add(new PointF(fCZ3, fDa3));
                    }
                    if (zValidateBuildingPlacement) {
                        i++;
                        if (i >= 29) {
                            return;
                        }
                    }
                    z2 = true;
                    if (orderableUnit2 != null) {
                        orderableUnit2.posX = fCZ3;
                        orderableUnit2.posY = fDa3;
                    }
                }
                f9 = f10 + gameEngine.tileMap.halfTileWorldSizeX;
            } else {
                orderableUnit.posX = f5;
                orderableUnit.posY = f6;
                return;
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public boolean validateBuildingPlacement(OrderableUnit orderableUnit, float f, float f2, boolean z, boolean z2, BaseUnit baseUnit) {
        boolean z3;
        GameEngine gameEngine = GameEngine.getInstance();
        float f3 = orderableUnit.posX;
        float f4 = orderableUnit.posY;
        orderableUnit.posX = f;
        orderableUnit.posY = f2;
        boolean zCanPlaceAtCurrentPosition = orderableUnit.canPlaceAtCurrentPosition(gameEngine.playerTeam);
        if (BuildPreview.isUnitOverBlueprint(gameEngine.playerTeam, orderableUnit, this.buildQueueId)) {
            zCanPlaceAtCurrentPosition = false;
        }
        if (baseUnit != null && baseUnit != null && (baseUnit instanceof OrderableUnit)) {
            OrderableUnit orderableUnit2 = (OrderableUnit) baseUnit;
            if (!orderableUnit2.canExecuteMovementCommands()) {
                float fDistanceSq = Utility.distanceSq(orderableUnit2.posX, orderableUnit2.posY, orderableUnit.posX, orderableUnit.posY);
                float f5 = orderableUnit2.f(orderableUnit.r());
                if (f5 > 800000.0f) {
                    z3 = true;
                } else {
                    z3 = fDistanceSq <= f5 * f5;
                }
                if (!z3) {
                    zCanPlaceAtCurrentPosition = false;
                }
            }
        }
        boolean z4 = orderableUnit.isUnitParalyzed;
        orderableUnit.isUnitParalyzed = true;
        orderableUnit.isUnitDisabled = zCanPlaceAtCurrentPosition;
        orderableUnit.isUnitCapturable = !zCanPlaceAtCurrentPosition;
        orderableUnit.isUnitUntargetable = z2;
        if (z) {
            gameEngine.renderGraphicsEngine.k();
            gameEngine.applyZoomTransform();
            orderableUnit.d(0.0f);
            orderableUnit.c(0.0f);
            orderableUnit.a(0.0f, false);
            float fM = orderableUnit.m();
            if (fM > 30.0f) {
                GameViewUtils.a((BaseUnit) orderableUnit, fM, true, true);
            }
            orderableUnit.cb();
            if (!z2) {
                orderableUnit.drawPlacementOverlay(-1);
            }
            gameEngine.renderGraphicsEngine.l();
        }
        orderableUnit.posX = f3;
        orderableUnit.posY = f4;
        orderableUnit.isUnitUntargetable = false;
        orderableUnit.isUnitParalyzed = z4;
        return zCanPlaceAtCurrentPosition;
    }

    /* JADX INFO: renamed from: G */
    public void startGameEndSequence() {
        clearCurrentAction();
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.hasWonGame = true;
        gameEngine.gameStatistics.c();
        if (gameEngine.gameTimeMillis < 1500 && gameEngine.gameUI.editorOrBuilder != null) {
            gameEngine.isContinuingAfterGameEnd = true;
        }
        this.endGameScreen.update(0.0f);
        this.endGameScreen.loadStats();
    }

    /* JADX INFO: renamed from: H */
    public void endGameSequence() {
        clearCurrentAction();
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.hasLostGame = true;
        gameEngine.gameStatistics.c();
        this.endGameScreen.update(0.0f);
        this.endGameScreen.loadStats();
    }

    /* JADX INFO: renamed from: I */
    public void activatePingMapMode() {
        clearCurrentAction();
        this.selectedBuilder = null;
        this.currentAction = this.pingMapAction;
    }

    /* JADX INFO: renamed from: a */
    public void drawMultilineText(String str, Rect rect, Paint paint, Paint paint2) {
        Paint paint3;
        GameEngine gameEngine = GameEngine.getInstance();
        int i = 0;
        for (String str2 : Utility.splitByChar(str, '\n')) {
            if (i == 0) {
                paint3 = paint;
            } else {
                paint3 = paint2;
            }
            Paint paint4 = paint3;
            int lineHeight = TextUtils.getLineHeight(paint4);
            gameEngine.renderGraphicsEngine.a(str2, rect.d(), rect.b + (lineHeight / 2) + (i * lineHeight), paint4);
            i++;
        }
    }

    /* JADX INFO: renamed from: a */
    public boolean drawActionTooltip(AbstractUnitAction abstractUnitAction, boolean z, BaseUnit baseUnit, boolean z2, boolean z3) {
        return drawActionTooltipAndHandleInput(abstractUnitAction, z, baseUnit, z2, false, -1.0f, z3);
    }

    /* JADX INFO: renamed from: a */
    public boolean drawActionTooltipAndHandleInput(final AbstractUnitAction s, final boolean boolean2, final BaseUnit am, final boolean boolean4, final boolean boolean5, final float float6, final boolean boolean7) {
        final GameEngine instance = GameEngine.getInstance();
        String s2 = null;
        boolean b = false;
        boolean b2 = true;
        if (GameEngine.isDesktopMouseInput()) {
            b2 = false;
        }
        if (am != null && s.shouldHideQueueInterface(am)) {
            b2 = false;
        }
        if (boolean7) {
            b2 = false;
        }
        boolean b3 = false;
        boolean b4 = false;
        if (GameInterfaceRenderer.getKeyBindingForAction(s)) {
            b3 = true;
            b4 = true;
        }
        if (this.isActionUnavailableForSelectedUnits(s)) {
            b3 = true;
            s2 = this.lockedText;
            final String actionIcon = this.getActionIcon(s);
            if (actionIcon != null) {
                s2 = actionIcon;
            }
        }
        if (!b3 && boolean4) {
            final String actionIcon2 = this.getActionIcon(s);
            if (actionIcon2 != null) {
                s2 = actionIcon2;
            }
        }
        if (s2 == null) {
            final float b5 = this.interfaceRenderer.getActionBlockDuration(s);
            if (b5 > 0.0f) {
                s2 = Utility.formatSeconds(b5 / 1000.0f);
            }
        }
        final String actionDisplayText = this.getActionDisplayText(s);
        final boolean b6 = actionDisplayText != null;
        if (actionDisplayText != null) {}
        if (boolean4 && s2 == null && actionDisplayText != null) {
            s2 = this.notEnoughResourcesText.resolveText();
        }
        final TextRenderQueue textRenderQueue = new TextRenderQueue();
        textRenderQueue.defaultPaint = this.unitIconPaint;
        textRenderQueue.highlightPaint = this.buildingIconPaint;
        final Paint paint3 = null;
        Paint effectIconTexture = null;
        if (b6) {
            effectIconTexture = this.effectIconPaint;
        }
        textRenderQueue.setUseHighlightPaint(true);
        s.renderDisplayText(am, textRenderQueue, paint3, effectIconTexture);
        if (s2 != null) {
            textRenderQueue.addText("\n" + s2, this.effectIconPaint);
        }
        textRenderQueue.setUseHighlightPaint(false);
        s.onPurchase(am, textRenderQueue);
        if (b4) {
            textRenderQueue.clearElements();
            textRenderQueue.addText(this.notAvailableInDemoText, this.buildingIconPaint);
        }
        final int a = 20;
        this.actionTooltipRect.a = a;
        final int c = (int)(instance.screenWidth - instance.sidebarWidth - a);
        this.actionTooltipRect.c = c;
        final boolean shouldShowActionInfoHoverNearMouse = instance.settingsEngine.showActionInfoHoverNearMouse;
        int b7;
        if (boolean2) {
            b7 = (int)(instance.halfScreenHeight - 40.0f);
        }
        else {
            b7 = 40;
        }
        if (GameEngine.isNonPCPlatform() && float6 > 0.0f) {
            b7 = (int)float6;
            b = true;
        }
        if (GameEngine.isPC() && shouldShowActionInfoHoverNearMouse && boolean5) {
            b7 = (int)(instance.getTouchY() - 40.0f);
        }
        this.actionTooltipRect.b = b7;
        this.actionTooltipRect.d = this.actionTooltipRect.b;
        boolean boolean8 = true;
        boolean b8 = true;
        final boolean b9 = false;
        int n = 7;
        if (GameEngine.isPC()) {
            if (!shouldShowActionInfoHoverNearMouse) {
                boolean8 = false;
                b8 = false;
            }
            else if (!boolean5) {
                b8 = false;
                boolean8 = true;
                n = 20;
            }
        }
        else if (!boolean2) {
            b8 = false;
        }
        if (!GameEngine.isPC() || boolean2 || !shouldShowActionInfoHoverNearMouse || !boolean5) {}
        if (s2 != null) {}
        final GamePaint buildingIconTexture = this.buildingIconPaint;
        if (boolean4) {
            final GamePaint effectIconTexture2 = this.effectIconPaint;
        }
        final TextRenderLayout a2 = textRenderQueue.createTextLayout(this.actionTooltipRect.b(), boolean8);
        final float n2 = (float)this.actionTooltipRect.d();
        this.actionTooltipRect.a = (int)(n2 - a2.rect.b() / 2);
        this.actionTooltipRect.c = (int)(n2 + a2.rect.b() / 2);
        this.actionTooltipRect.d = this.actionTooltipRect.b + a2.rect.c();
        if (boolean8) {
            final Rect bv = this.actionTooltipRect;
            bv.a -= (int)(n * instance.screenScale);
            final Rect bv2 = this.actionTooltipRect;
            bv2.c += (int)(n * instance.screenScale);
        }
        if (b8) {
            final int active = (int)(c - 7.0f * instance.screenScale - this.actionTooltipRect.c);
            this.actionTooltipRect.a(active, 0);
        }
        this.actionTooltipBorderRect.a(this.actionTooltipRect);
        final Rect bw = this.actionTooltipBorderRect;
        bw.b -= 20;
        final Rect bw2 = this.actionTooltipBorderRect;
        bw2.d += 15;
        int active = -1;
        if (am != null) {
            active = s.getActiveCount(am, true);
        }
        if (am != null && b2 && active != -1) {
            final Rect bw3 = this.actionTooltipBorderRect;
            bw3.d += (int)(55.0f * instance.screenScale);
        }
        if (this.actionTooltipBorderRect.d > instance.screenHeight) {
            final int n3 = (int)(instance.screenHeight - this.actionTooltipBorderRect.d);
            this.actionTooltipRect.a(0, n3);
            this.actionTooltipBorderRect.a(0, n3);
        }
        UnitType unitType = s.getUnitType();
        if (!s.shouldShowUnitPreview()) {
            unitType = null;
        }
        if (unitType != null && am != null) {
            final Rect bw4 = this.actionTooltipBorderRect;
            bw4.b -= (int)(40.0f * instance.screenScale);
        }
        if (b) {
            final int n4 = -this.actionTooltipRect.c();
            this.actionTooltipRect.a(0, n4);
            this.actionTooltipBorderRect.a(0, n4);
        }
        if (b9) {
            final float float7 = instance.screenHeight - 30.0f;
            final int n5 = (int)(float7 - this.actionTooltipBorderRect.d);
            this.actionTooltipBorderRect.a(0, n5);
            this.actionTooltipRect.a(0, n5);
        }
        if (this.actionTooltipBorderRect.b < 0) {
            final int n4 = 0 - this.actionTooltipBorderRect.b;
            this.actionTooltipBorderRect.a(0, n4);
            this.actionTooltipRect.a(0, n4);
        }
        if (this.actionTooltipBorderRect.d > instance.screenHeight - 20.0f) {
            final float float7 = instance.screenHeight - 20.0f;
            final int n5 = (int)(float7 - this.actionTooltipBorderRect.d);
            this.actionTooltipBorderRect.a(0, n5);
            this.actionTooltipRect.a(0, n5);
        }
        instance.renderGraphicsEngine.b(this.actionTooltipBorderRect, this.minimapViewportBorderPaint);
        instance.renderGraphicsEngine.b(this.actionTooltipBorderRect, this.minimapPaint);
        if (b3) {}
        if (unitType != null && am != null) {
            final float float7 = 30.0f * instance.screenScale;
            UnitTypeEnum.drawUnit(unitType, (float)this.actionTooltipBorderRect.d(), this.actionTooltipBorderRect.b + 22.0f * instance.screenScale, this.selectionBoxStartTime, 0.0f, am.team, float7, 100.0f * instance.screenScale, false, false, s.getQueueSize(), null);
        }
        a2.draw((float)this.actionTooltipRect.d(), (float)this.actionTooltipRect.b);
        if (am != null && active != -1 && b2) {
            final float float7 = instance.screenScale * 0.5f;
            final int n5 = (int)(60.0f * float7);
            final float timerValue = UnitActionTimer.getTimerValue(am, s, true);
            if (!b3 || active > 0) {
                this.fogOfWarPaint.b(-16777216);
                if (timerValue != 0.0f) {
                    float clampTo255 = Utility.abs(timerValue) * 0.5f - 0.4f;
                    clampTo255 = Utility.clampTo255(clampTo255, 0.0f, 1.0f);
                    int integer1;
                    if (timerValue > 0.0f) {
                        integer1 = Color.a(110, 30, 240, 30);
                    }
                    else {
                        integer1 = Color.a(110, 240, 30, 30);
                    }
                    Utility.lerpColor(integer1, this.fogOfWarPaint.e(), clampTo255);
                }
                float clampTo255 = this.actionTooltipBorderRect.d - 65.0f * float7 / 2.0f + TextUtils.getCharWidth(this.fogOfWarPaint) / 2;
                if (timerValue > 0.5) {
                    ++clampTo255;
                }
                if (timerValue < -0.5) {
                    --clampTo255;
                }
                instance.renderGraphicsEngine.a("" + active, (float)this.actionTooltipBorderRect.d(), clampTo255, this.fogOfWarPaint);
            }
            boolean b10 = false;
            boolean b11 = false;
            final boolean b12 = !b3 && this.canAffordActionForSelectedUnits(s, true);
            final boolean b13 = active > 0 && s.canPlayerCancel(am, true);
            int n6 = (int)(this.actionTooltipBorderRect.d() + 60.0f * float7);
            int i = (int)(this.actionTooltipBorderRect.d - 65.0f * float7);
            this.tempButtonRect.a(n6, i, n6 + n5, i + n5);
            Paint paint4;
            if (b12) {
                paint4 = this.tooltipBackgroundPaint;
            }
            else {
                paint4 = this.tooltipBorderPaint;
            }
            if (timerValue > 0.0f) {
                float n7 = Utility.abs(timerValue) * 0.7f - 0.3f;
                n7 = Utility.clampTo255(n7, 0.0f, 1.0f);
                int n8;
                if (timerValue > 0.0f) {
                    n8 = Color.a(110, 210, 210, 210);
                }
                else {
                    n8 = Color.a(110, 210, 110, 110);
                }
                final int n9 = Utility.lerpColor(n8, paint4.e(), n7);
                paint4 = this.selectionOverlayPaint;
                paint4.b(n9);
            }
            if (timerValue > 0.5) {
                this.tempButtonRect.a(0, 1);
            }
            instance.renderGraphicsEngine.a(this.buttonAddTexture, (float)this.tempButtonRect.a, (float)this.tempButtonRect.b, paint4, 0.0f, float7);
            Utility.grow(this.tempButtonRect, this.tempButtonRect.b() * 0.8f);
            if (this.isSelectionBoxActive && !this.isInputDisabled && !b4 && this.tempButtonRect.b((int)this.selectionBoxStartX, (int)this.selectionBoxStartY)) {
                this.isSelectionBoxActive = false;
                b10 = true;
            }
            n6 = (int)(this.actionTooltipBorderRect.d() - n5 - 60.0f * float7);
            i = (int)(this.actionTooltipBorderRect.d - 65.0f * float7);
            this.tempButtonRect.a(n6, i, n6 + n5, i + n5);
            Paint paint5;
            if (b13) {
                paint5 = this.tooltipBackgroundPaint;
            }
            else {
                paint5 = this.tooltipBorderPaint;
            }
            if (timerValue < 0.0f) {
                float n7 = Utility.abs(timerValue) * 0.7f - 0.3f;
                n7 = Utility.clampTo255(n7, 0.0f, 1.0f);
                int n8;
                if (timerValue > 0.0f) {
                    n8 = Color.a(110, 210, 210, 210);
                }
                else {
                    n8 = Color.a(110, 210, 110, 110);
                }
                final int n9 = Utility.lerpColor(n8, paint5.e(), n7);
                paint5 = this.selectionOverlayPaint;
                paint5.b(n9);
            }
            if (timerValue < -0.5) {
                this.tempButtonRect.a(0, 1);
            }
            instance.renderGraphicsEngine.a(this.buttonSubtractTexture, (float)this.tempButtonRect.a, (float)this.tempButtonRect.b, paint5, 0.0f, float7);
            Utility.grow(this.tempButtonRect, this.tempButtonRect.b() * 0.8f);
            if (this.isSelectionBoxActive && !this.isInputDisabled && this.tempButtonRect.b((int)this.selectionBoxStartX, (int)this.selectionBoxStartY)) {
                this.isSelectionBoxActive = false;
                b11 = true;
            }
            n6 = 1;
            if ((b10 || b11) && s.isHighPriority()) {
                if (this.isShiftKeyPressed(instance)) {
                    n6 = 5;
                }
                if (this.isControlKeyPressed(instance)) {
                    n6 = 10;
                }
            }
            if (b10) {
                if (s.isHighPriority() && instance.playerTeam.getUnitCap() <= instance.playerTeam.getNonBuildingUnitCountIncludingQueued()) {
                    this.showMediumPriorityMessage(this.interfaceRenderer.unitCapReachedText);
                }
                if (b12) {
                    instance.soundEngine.playInterfaceSound(SoundEngine.clickAddSound, 0.5f);
                    UnitActionTimer.startTimer(am, s, false, true);
                }
                for (i = 0; i < n6; ++i) {
                    final Command baseCommand = this.createBaseCommand();
                    if (this.isShiftKeyPressed(instance)) {
                        baseCommand.isQueued = true;
                    }
                    this.setActionCommandTargets(baseCommand, s);
                    baseCommand.setActionId(s.getQueueId());
                    this.prepareUnitActionCommand(s, null, null, baseCommand);
                }
            }
            if (b11) {
                if (b13) {
                    UnitActionTimer.startTimer(am, s, true, true);
                    instance.soundEngine.playInterfaceSound(SoundEngine.clickRemoveSound, 0.5f);
                }
                for (i = 0; i < n6; ++i) {
                    final Command baseCommand2 = this.createBaseCommand();
                    this.setActionCommandTargets(baseCommand2, s);
                    baseCommand2.stopCurrentAction = true;
                    baseCommand2.setActionId(s.getQueueId());
                }
            }
            if (!b10 && !b11 && this.isSelectionBoxActive && !this.isInputDisabled && !this.actionTooltipBorderRect.b((int)this.selectionBoxStartX, (int)this.selectionBoxStartY)) {
                return true;
            }
        }
        return !b2 && GameEngine.isNonPCPlatform() && this.isSelectionBoxActive && !this.isInputDisabled && !this.actionTooltipBorderRect.b((int)this.selectionBoxStartX, (int)this.selectionBoxStartY);
    }

    /* JADX INFO: renamed from: a */
    public void drawTopBarBackground(Rect rect, Paint paint, Paint paint2) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (showModernSidebar) {
            gameEngine.renderGraphicsEngine.a(this.metalDarkTexture, rect, paint2, rect.a, rect.b, 0, 0);
            if (paint != null) {
                int iF = paint.f();
                if (iF > 255) {
                    iF = 255;
                }
                paint.c(iF);
            }
        }
        if (paint != null) {
            gameEngine.renderGraphicsEngine.b(rect, paint);
        }
    }

    /* JADX INFO: renamed from: a */
    public void drawTooltipInset(Rect rect, int i, boolean z) {
        GameEngine gameEngine = GameEngine.getInstance();
        this.tempStrokePaint.b(i);
        this.tempStrokePaint.a(Paint.Style.STROKE);
        this.tempStrokePaint.a(1.0f);
        gameEngine.renderGraphicsEngine.b(rect, this.tempStrokePaint);
        if (this.isUILoggingEnabled) {
            this.tempStrokePaint.b(Color.a(255, 116, 136, 160));
            int i2 = 1;
            if (z && rect.b() > 100) {
                i2 = 2;
            }
            this.tempStrokePaint.a(i2);
            this.tempInsetRect.a(rect);
            this.tempInsetRect.d -= i2;
            this.tempInsetRect.b += i2;
            this.tempInsetRect.a += i2;
            this.tempInsetRect.c -= i2;
            gameEngine.renderGraphicsEngine.b(this.tempInsetRect, this.tempStrokePaint);
        }
    }

    /* JADX INFO: renamed from: a */
    public void drawStyledButtonBackground(int i, int i2, int i3, int i4, String str, int i5, Paint paint, boolean z, UIStyle uIStyle, UIState uIState) {
        GameEngine gameEngine = GameEngine.getInstance();
        this.tempDrawRect.a(i, i2, i + i3, i2 + i4);
        this.tempStrokePaint.b(i5);
        if (uIStyle != null) {
            uIStyle.draw(gameEngine.renderGraphicsEngine, this.tempDrawRect, uIState);
        } else if (!z) {
            this.tempStrokePaint.a(Paint.Style.FILL);
            gameEngine.renderGraphicsEngine.b(this.tempDrawRect, this.tempStrokePaint);
        } else {
            drawTopBarBackground(this.tempDrawRect, (Paint) null, this.tempStrokePaint);
        }
        if (uIStyle == null) {
            int iA = Color.a(255, 0, 0, 0);
            if (showModernSidebar) {
                iA = Color.a(100, 0, 0, 0);
            }
            drawTooltipInset(this.tempDrawRect, iA, false);
        }
        drawButtonBackground(i, i2, i3, i4, str, i5, paint);
    }

    /* JADX INFO: renamed from: a */
    public void drawButtonBackground(int i, int i2, int i3, int i4, String str, int i5, Paint paint) {
        GameEngine gameEngine = GameEngine.getInstance();
        this.tempDrawRect.a(i, i2, i + i3, i2 + i4);
        if (GameEngine.isPCOrIOSVersion) {
            gameEngine.renderGraphicsEngine.a(str, this.tempDrawRect.d(), this.tempDrawRect.e() + (gameEngine.renderGraphicsEngine.a(str, paint) / 2), paint);
        } else {
            gameEngine.renderGraphicsEngine.a(str, this.tempDrawRect.d(), this.tempDrawRect.e() - ((paint.l() + paint.m()) / 2.0f), paint);
        }
    }

    /* JADX INFO: renamed from: J */
    public boolean isInputEnabled() {
        if (this.isInputDisabled) {
            return false;
        }
        return true;
    }

    /* JADX INFO: renamed from: a */
    public boolean isButtonPressed(int i, int i2, int i3, int i4, String str, IconGroup iconGroup, boolean z, int i5) {
        return isButtonPressedFull(i, i2, i3, i4, str, iconGroup, z, i5, this.buildingPreviewPaint, false, null);
    }

    /* JADX INFO: renamed from: b */
    public boolean isButtonPressedWithRightClick(int i, int i2, int i3, int i4, String str, IconGroup iconGroup, boolean z, int i5) {
        return isButtonPressedFull(i, i2, i3, i4, str, iconGroup, z, i5, this.buildingPreviewPaint, true, null);
    }

    /* JADX INFO: renamed from: a */
    public boolean isButtonPressedStyled(int i, int i2, int i3, int i4, String str, IconGroup iconGroup, boolean z, int i5, Paint paint, UIStyle uIStyle) {
        return isButtonPressedFull(i, i2, i3, i4, str, iconGroup, z, i5, paint, false, uIStyle);
    }

    /* JADX INFO: renamed from: a */
    public boolean isButtonPressedFull(int i, int i2, int i3, int i4, String str, IconGroup iconGroup, boolean z, int i5, Paint paint, boolean z2, UIStyle uIStyle) {
        boolean zA = isRectHovered(i, i2, i3, i4, iconGroup);
        boolean zA2 = isRectPressed(i, i2, i3, i4, iconGroup, z);
        UIState uIState = UIState.normal;
        if (zA) {
            uIState = UIState.hovered;
        }
        drawStyledButtonBackground(i, i2, i3, i4, str, i5, paint, z2, uIStyle, uIState);
        return zA2;
    }

    /* JADX INFO: renamed from: a */
    public void forceModifiersInsideRect(Rect rect) {
        if (rect.b((int) this.selectionBoxMinWidth, (int) this.selectionBoxMinHeight)) {
            this.isKeyboardShiftPressed = true;
            this.isKeyboardCtrlPressed = true;
            if (this.isRightClickDrag) {
                this.isMiddleMousePressed = true;
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public void forceModifiersInsideRect(float f, float f2, float f3, float f4) {
        this.debugTextRect.a((int) f, (int) f2, (int) (f + f3), (int) (f2 + f4));
        forceModifiersInsideRect(this.debugTextRect);
    }

    /* JADX INFO: renamed from: a */
    public boolean isRectPressed(int i, int i2, int i3, int i4, IconGroup iconGroup, boolean z) {
        forceModifiersInsideRect(i, i2, i3, i4);
        this.tempDrawRect.a(i, i2, i + i3, i2 + i4);
        if (((z && this.isMousePressed) || this.isSelectionBoxActive) && this.tempDrawRect.b((int) this.selectionBoxStartX, (int) this.selectionBoxStartY)) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public boolean isRectHovered(int i, int i2, int i3, int i4, IconGroup iconGroup) {
        this.tempDrawRect.a(i, i2, i + i3, i2 + i4);
        GameEngine gameEngine = GameEngine.getInstance();
        if (GameEngine.isDesktopMouseInput() && gameEngine.settingsEngine.mouseSupport && this.tempDrawRect.b((int) gameEngine.getTouchX(), (int) gameEngine.getTouchY())) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: b */
    public boolean isRectHoveredRightClick(int i, int i2, int i3, int i4, IconGroup iconGroup) {
        this.tempDrawRect.a(i, i2, i + i3, i2 + i4);
        if (this.isRightClickDrag && this.tempDrawRect.b((int) this.selectionBoxStartX, (int) this.selectionBoxStartY)) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: r */
    public float getUnitSelectionFadeEffect(BaseUnit baseUnit) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (baseUnit.lastSelectedTick < gameEngine.renderTimeMillis && baseUnit.lastSelectedTick + 200 > gameEngine.renderTimeMillis) {
            return (1.0f - ((gameEngine.renderTimeMillis - baseUnit.lastSelectedTick) / 200.0f)) * 6.0f;
        }
        return GameEngine.getInstance().unitSelectionFadeBase;
    }

    /* JADX INFO: renamed from: a */
    public void centerAndShowDialog(MenuDialog menuDialog) {
        GameEngine gameEngine = GameEngine.getInstance();
        menuDialog.layoutIfNeeded();
        menuDialog.setCenterX(gameEngine.halfScreenWidth);
        menuDialog.setCenterY(gameEngine.halfScreenHeight);
        this.rootUIElement.addChild(menuDialog);
    }

    /* JADX INFO: renamed from: K */
    public static void notifySelectionChanged() {
        globalSelectionCounter++;
        selectionChanged = true;
    }
}
