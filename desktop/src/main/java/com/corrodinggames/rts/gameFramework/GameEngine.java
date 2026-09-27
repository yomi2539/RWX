package com.corrodinggames.rts.gameFramework;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;
import com.corrodinggames.rts.appFramework.AppFrameworkUtils;
import com.corrodinggames.rts.appFramework.GameView;
import com.corrodinggames.rts.appFramework.LevelSelectActivity;
import com.corrodinggames.rts.game.GameLogic;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.map.TileMap;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.custom.ConfigParseException;
import com.corrodinggames.rts.game.units.custom.LocaleString;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.game.units.management.UnitSpatialIndex;
import com.corrodinggames.rts.gameFramework.audio.SoundEngine;
import com.corrodinggames.rts.gameFramework.effects.EffectManager;
import com.corrodinggames.rts.gameFramework.file.FileHelper;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine;
import com.corrodinggames.rts.gameFramework.mission.MissionEngine;
import com.corrodinggames.rts.gameFramework.mod.ModManager;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameModeType;
import com.corrodinggames.rts.gameFramework.network.MasterServerClient;
import com.corrodinggames.rts.gameFramework.network.NetworkEngine;
import com.corrodinggames.rts.gameFramework.path.PathEngine;
import com.corrodinggames.rts.gameFramework.platform.PlatformExtension;
import com.corrodinggames.rts.gameFramework.stats.StatGroup;
import com.corrodinggames.rts.gameFramework.stats.StatType;
import com.corrodinggames.rts.gameFramework.stats.TeamStats;
import com.corrodinggames.rts.gameFramework.ui.GameUI;
import com.corrodinggames.rts.gameFramework.ui.Minimap;
import com.corrodinggames.rts.gameFramework.utility.ANRCallback;
import com.corrodinggames.rts.gameFramework.utility.ANRException;
import com.corrodinggames.rts.gameFramework.utility.ANRWatchdog;
import com.corrodinggames.rts.gameFramework.utility.AssetIndex;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.l */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/l.class */
public abstract class GameEngine {

    /* JADX INFO: renamed from: an */
    public Context context;

    /* JADX INFO: renamed from: ao */
    public GameView activeGameView;

    /* JADX INFO: renamed from: ap */
    public GameView pendingGameView;

    /* JADX INFO: renamed from: aq */
    public boolean isStopped;

    /* JADX INFO: renamed from: av */
    public static Throwable lastThrowable;

    /* JADX INFO: renamed from: ay */
    public static boolean isMenuBackgroundDisabled;

    /* JADX INFO: renamed from: az */
    public static boolean isDesktopVersion;

    /* JADX INFO: renamed from: aA */
    public static boolean useCurrentThreadExceptionHandler;

    /* JADX INFO: renamed from: aB */
    public static boolean isHeadlessMode;

    /* JADX INFO: renamed from: aC */
    public static boolean isTextureAtlasDisabled;

    /* JADX INFO: renamed from: aD */
    public static boolean isCanvasGLEnabled;

    /* JADX INFO: renamed from: aE */
    public static boolean isUnitImageGenerationMode;

    /* JADX INFO: renamed from: aF */
    public static boolean isUnitValidationMode;

    /* JADX INFO: renamed from: aG */
    public static boolean isOldReplayMode;

    /* JADX INFO: renamed from: aJ */
    public static boolean isModsDisabled;

    /* JADX INFO: renamed from: aR */
    public static boolean isMouseCaptured;

    /* JADX INFO: renamed from: aS */
    public boolean hasLoggedHighNativeHeapUsage;

    /* JADX INFO: renamed from: bg */
    public static Class gameEngineClass;

    /* JADX INFO: renamed from: bh */
    public static GraphicsEngine graphicsEngine;

    /* JADX INFO: renamed from: bj */
    public boolean isGameMinimized;

    /* JADX INFO: renamed from: bp */
    public boolean isShowingDialog;

    /* JADX INFO: renamed from: bs */
    public PlayerTeam playerTeam;

    /* JADX INFO: renamed from: bv */
    public boolean isGameStarted;

    /* JADX INFO: renamed from: bw */
    public boolean isUnitInvincibilityEnabled;

    /* JADX INFO: renamed from: by */
    public int gameTimeMillis;

    /* JADX INFO: renamed from: bz */
    public int renderFrameCount;

    /* JADX INFO: renamed from: bA */
    public int renderTimeMillis;

    /* JADX INFO: renamed from: bB */
    public int currentUnitCap;

    /* JADX INFO: renamed from: bC */
    public int maxUnitCap;

    /* JADX INFO: renamed from: bD */
    public boolean isGameResumed;

    /* JADX INFO: renamed from: bJ */
    public int globalSeed;

    /* JADX INFO: renamed from: bK */
    public AssetIndex assetIndex;

    /* JADX INFO: renamed from: bL */
    public TileMap tileMap;

    /* JADX INFO: renamed from: bM */
    public SoundEngine soundEngine;

    /* JADX INFO: renamed from: bN */
    public MusicManager musicManager;

    /* JADX INFO: renamed from: bO */
    public GraphicsEngine renderGraphicsEngine;

    /* JADX INFO: renamed from: bP */
    public CollisionEngine collisionEngine;

    /* JADX INFO: renamed from: bQ */
    public SettingsEngine settingsEngine;

    /* JADX INFO: renamed from: bR */
    public EffectManager effectManager;

    /* JADX INFO: renamed from: bS */
    public GameUI gameUI;

    /* JADX INFO: renamed from: bT */
    public InputController inputController;

    /* JADX INFO: renamed from: bU */
    public PathEngine pathfindingEngine;

    /* JADX INFO: renamed from: bV */
    public FormationEngine formationEngine;

    /* JADX INFO: renamed from: bW */
    public Minimap minimap;

    /* JADX INFO: renamed from: bX */
    public NetworkEngine networkEngine;

    /* JADX INFO: renamed from: bY */
    public GameStatistics gameStatistics;

    /* JADX INFO: renamed from: bZ */
    public ModManager modManager;

    /* JADX INFO: renamed from: ca */
    public GameSaver gameSaver;

    /* JADX INFO: renamed from: cb */
    public ReplayEngine replayEngine;

    /* JADX INFO: renamed from: cc */
    public UnitSpatialIndex unitSpatialIndex;

    /* JADX INFO: renamed from: cd */
    public PerformanceProfiler performanceProfiler;

    /* JADX INFO: renamed from: ce */
    public MissionEngine missionEngine;

    /* JADX INFO: renamed from: cf */
    public CommandController commandController;

    /* JADX INFO: renamed from: ci */
    public float densityScaleRaw;

    /* JADX INFO: renamed from: cj */
    public float screenScale;

    /* JADX INFO: renamed from: ck */
    public static Point screenSize;

    /* JADX INFO: renamed from: cl */
    public float screenWidth;

    /* JADX INFO: renamed from: cm */
    public float screenHeight;

    /* JADX INFO: renamed from: co */
    public float halfScreenWidth;

    /* JADX INFO: renamed from: cp */
    public float halfScreenHeight;

    /* JADX INFO: renamed from: cq */
    public float sidebarWidth;

    /* JADX INFO: renamed from: cr */
    public float scrollDeltaX;

    /* JADX INFO: renamed from: cs */
    public float scrollDeltaY;

    /* JADX INFO: renamed from: ct */
    public boolean wasCameraClamped;

    /* JADX INFO: renamed from: cu */
    public int viewpointXInt;

    /* JADX INFO: renamed from: cv */
    public int viewpointYInt;

    /* JADX INFO: renamed from: cw */
    public float viewpointXSnapped;

    /* JADX INFO: renamed from: cx */
    public float viewpointYSnapped;

    /* JADX INFO: renamed from: cy */
    public float viewpointX;

    /* JADX INFO: renamed from: cz */
    public float viewpointY;

    /* JADX INFO: renamed from: cA */
    public float visibleWorldWidth;

    /* JADX INFO: renamed from: cB */
    public float visibleWorldHeight;

    /* JADX INFO: renamed from: cC */
    public float cameraMovementX;

    /* JADX INFO: renamed from: cD */
    public float cameraMovementY;

    /* JADX INFO: renamed from: cE */
    public float viewpointWidth;

    /* JADX INFO: renamed from: cF */
    public float currentScreenWidthPixels;

    /* JADX INFO: renamed from: cG */
    public float currentViewpointWidthPixels;

    /* JADX INFO: renamed from: cH */
    public float currentScreenHeightPixels;

    /* JADX INFO: renamed from: cI */
    public float halfVisibleWorldWidth;

    /* JADX INFO: renamed from: cJ */
    public float halfVisibleWorldHeight;

    /* JADX INFO: renamed from: cR */
    public boolean wasPaused;

    /* JADX INFO: renamed from: cS */
    public boolean isPaused;

    /* JADX INFO: renamed from: cT */
    public float pauseTransition;

    /* JADX INFO: renamed from: cU */
    public boolean isMenuOpen;

    /* JADX INFO: renamed from: cZ */
    public boolean shouldRecenterZoomOnPointer;

    /* JADX INFO: renamed from: da */
    public float mouseX;

    /* JADX INFO: renamed from: db */
    public float mouseY;

    /* JADX INFO: renamed from: dl */
    public String currentMapPath;

    /* JADX INFO: renamed from: dm */
    public GameInputStream remoteMapStream;

    /* JADX INFO: renamed from: dn */
    public Paint teamInfoPaint;

    /* JADX INFO: renamed from: do */
    public Paint centeredPaint;

    /* JADX INFO: renamed from: dp */
    public Paint loadingPaint;

    /* JADX INFO: renamed from: dw */
    public int selectedWaypointDrawCount;

    /* JADX INFO: renamed from: dA */
    float lastScreenScale;

    /* JADX INFO: renamed from: dE */
    public String toastMessage;

    /* JADX INFO: renamed from: dF */
    public String dialogTitle;

    /* JADX INFO: renamed from: dG */
    public String dialogMessage;

    /* JADX INFO: renamed from: dK */
    String pendingMessageBody;

    /* JADX INFO: renamed from: dL */
    String pendingMessageTitle;

    /* JADX INFO: renamed from: e */
    private int accumulatedMouseWheelDelta;

    /* JADX INFO: renamed from: dP */
    static byte[] exceptionHandlerMemoryReserve;

    /* JADX INFO: renamed from: dS */
    static ANRWatchdog anrWatchdog;

    /* JADX INFO: renamed from: dV */
    static boolean hasShownOutOfMemoryMessage;

    /* JADX INFO: renamed from: dX */
    static boolean lowMemoryWarningPending;

    /* JADX INFO: renamed from: dY */
    static boolean hasShownLowMemoryWarning;

    /* JADX INFO: renamed from: ee */
    public boolean isSafeMode;

    /* JADX INFO: renamed from: ef */
    public boolean isEnglishForcedBySafeMode;

    /* JADX INFO: renamed from: eg */
    public String safeModeReason;

    /* JADX INFO: renamed from: eh */
    public boolean isExtraSafeMode;

    /* JADX INFO: renamed from: ei */
    public boolean isExtraSafeModeLevel2;

    /* JADX INFO: renamed from: ej */
    static int nonFatalErrorReportCount;

    /* JADX INFO: renamed from: al */
    protected static GameEngine instance = null;

    /* JADX INFO: renamed from: as */
    public static boolean isGameBeta = true;

    /* JADX INFO: renamed from: at */
    public static boolean isTestingBuild = false;

    /* JADX INFO: renamed from: au */
    public static boolean isRateGamePromptEnabled = false;

    /* JADX INFO: renamed from: aw */
    public static boolean isReplayDebugMode = false;

    /* JADX INFO: renamed from: ax */
    public static boolean isLogColorEnabled = false;

    /* JADX INFO: renamed from: aH */
    public static boolean isSteamModeEnabled = false;

    /* JADX INFO: renamed from: aI */
    public static boolean isLaunchSandbox = false;

    /* JADX INFO: renamed from: aK */
    public static String pendingSteamLobbyId = null;

    /* JADX INFO: renamed from: aL */
    public static boolean isGameThreadActive = false;

    /* JADX INFO: renamed from: aM */
    public static boolean isPostProcessingEnabled = false;

    /* JADX INFO: renamed from: aN */
    public static boolean isTeamShadersEnabled = false;

    /* JADX INFO: renamed from: aO */
    public static boolean isCommandLineMode = false;

    /* JADX INFO: renamed from: aP */
    public static boolean isAutomatedTesting = false;

    /* JADX INFO: renamed from: aQ */
    public static String platformName = null;

    /* JADX INFO: renamed from: aT */
    public static boolean isAutomatedTestMode = false;

    /* JADX INFO: renamed from: aU */
    public static boolean isNonAndroidVersion = false;

    /* JADX INFO: renamed from: aV */
    public static boolean isDebugServerActive = false;

    /* JADX INFO: renamed from: aW */
    public static boolean isPCOrIOSVersion = false;

    /* JADX INFO: renamed from: aX */
    public static boolean isJavaDesktopVersion = false;

    /* JADX INFO: renamed from: aY */
    public static boolean isGDXVersion = false;

    /* JADX INFO: renamed from: aZ */
    public static boolean isIOSVersion = false;

    /* JADX INFO: renamed from: ba */
    public static String androidVersion = null;

    /* JADX INFO: renamed from: bb */
    public static boolean isDesktopInitialized = false;

    /* JADX INFO: renamed from: bc */
    public static boolean isReplayRecordingEnabledOnNonPC = true;

    /* JADX INFO: renamed from: bd */
    public static boolean isReplayRecordingEnabledOnPCOrIOS = true;

    /* JADX INFO: renamed from: be */
    public static boolean spaceGameMode = false;

    /* JADX INFO: renamed from: bf */
    public static boolean mapDebugMode = false;

    /* JADX INFO: renamed from: dy */
    public static GameEngineFactory gameEngineFactory = new GameLogicFactory();

    /* JADX INFO: renamed from: dz */
    public static String buildVersion = Build.VERSION.RELEASE;

    /* JADX INFO: renamed from: dO */
    public static boolean hasHandledCrash = false;

    /* JADX INFO: renamed from: dQ */
    static byte[] outOfMemoryReserveBuffer = new byte[1000];

    /* JADX INFO: renamed from: dR */
    static byte[] secondaryOutOfMemoryReserveBuffer = new byte[1000];

    /* JADX INFO: renamed from: dT */
    static boolean hasDetectedANR = false;

    /* JADX INFO: renamed from: dU */
    static int reportedProblemCount = 0;

    /* JADX INFO: renamed from: dW */
    static AssetType outOfMemoryAssetType = null;

    /* JADX INFO: renamed from: aj */
    public final Object gameStateLock = new Object();

    /* JADX INFO: renamed from: ak */
    public final Object drawLock = new Object();

    /* JADX INFO: renamed from: am */
    public Context appContext = null;

    /* JADX INFO: renamed from: ar */
    public boolean isDemo = false;

    /* JADX INFO: renamed from: bi */
    public boolean isInitialized = false;

    /* JADX INFO: renamed from: bk */
    public boolean isGamePausedOrMinimized = false;

    /* JADX INFO: renamed from: bl */
    public boolean isDebugTempMode = false;

    /* JADX INFO: renamed from: bm */
    public boolean showAITeamInfoOverlay = false;

    /* JADX INFO: renamed from: bn */
    public boolean isTriggerDebugMode = false;

    /* JADX INFO: renamed from: bo */
    public boolean isGameRecording = false;

    /* JADX INFO: renamed from: bq */
    public boolean isLoading = false;

    /* JADX INFO: renamed from: br */
    public boolean isSaving = false;

    /* JADX INFO: renamed from: bt */
    public float gameSpeed = 1.0f;

    /* JADX INFO: renamed from: bu */
    public float gameSpeedMultiplier = -1.0f;

    /* JADX INFO: renamed from: bx */
    public int currentTick = 0;

    /* JADX INFO: renamed from: bE */
    public boolean isBenchmarking = false;

    /* JADX INFO: renamed from: bF */
    public volatile boolean exitGameThread = false;

    /* JADX INFO: renamed from: bG */
    public volatile boolean hasLoadedLevel = false;

    /* JADX INFO: renamed from: bH */
    public volatile boolean isMenuBackgroundMap = false;

    /* JADX INFO: renamed from: bI */
    public volatile boolean fullReload = false;

    /* JADX INFO: renamed from: cg */
    public TeamStats teamStats = new TeamStats();

    /* JADX INFO: renamed from: ch */
    public boolean isLookModeEnabled = false;

    /* JADX INFO: renamed from: cn */
    public float viewScale = 1.0f;

    /* JADX INFO: renamed from: cK */
    public final Rect screenClipRect = new Rect();

    /* JADX INFO: renamed from: cL */
    public final Rect viewportClipRect = new Rect();

    /* JADX INFO: renamed from: cM */
    public final RectF visibleScreenRect = new RectF();

    /* JADX INFO: renamed from: cN */
    public final Rect bufferedVisibleWorldRect = new Rect();

    /* JADX INFO: renamed from: cO */
    public final RectF bufferedVisibleWorldRectF = new RectF();

    /* JADX INFO: renamed from: cP */
    public final RectF extendedVisibleWorldRect = new RectF();

    /* JADX INFO: renamed from: cQ */
    public final Rect visibleWorldRect = new Rect();

    /* JADX INFO: renamed from: cV */
    public float targetZoom = 1.0f;

    /* JADX INFO: renamed from: cW */
    public boolean isZoomLimitReached = false;

    /* JADX INFO: renamed from: cX */
    public float zoom = 1.0f;

    /* JADX INFO: renamed from: cY */
    public float densityZoomScale = 1.0f;

    /* JADX INFO: renamed from: dc */
    public boolean shouldDrawHighDetailEffects = true;

    /* JADX INFO: renamed from: dd */
    public boolean shouldDrawMediumDetailEffects = true;

    /* JADX INFO: renamed from: de */
    public boolean shouldDrawSmallUnitShadows = true;

    /* JADX INFO: renamed from: df */
    public boolean shouldDrawUnitShadows = true;

    /* JADX INFO: renamed from: dg */
    public boolean shouldDrawUnitLegDetails = true;

    /* JADX INFO: renamed from: dh */
    public float rightMouseHoldTimer = 0.0f;

    /* JADX INFO: renamed from: di */
    public float middleMouseHoldTimer = 0.0f;

    /* JADX INFO: renamed from: dj */
    public boolean mouseLastClickTime = false;

    /* JADX INFO: renamed from: dk */
    protected GameThread gameThread = null;

    /* JADX INFO: renamed from: dq */
    public boolean hasWonGame = false;

    /* JADX INFO: renamed from: dr */
    public boolean isContinuingAfterGameEnd = false;

    /* JADX INFO: renamed from: ds */
    public float touchStartX = 0.0f;

    /* JADX INFO: renamed from: dt */
    public boolean hasLostGame = false;

    /* JADX INFO: renamed from: du */
    public boolean shouldAdvanceAfterGameEnd = false;

    /* JADX INFO: renamed from: dv */
    public boolean shouldSkipNextDraw = false;

    /* JADX INFO: renamed from: dx */
    public float unitSelectionFadeBase = 0.0f;

    /* JADX INFO: renamed from: dB */
    boolean paintSizeTrackersReady = false;

    /* JADX INFO: renamed from: dC */
    ArrayList paintSizeTrackers = new ArrayList();

    /* JADX INFO: renamed from: dD */
    final Handler uiDispatchHandler = new Handler(Looper.b());

    /* JADX INFO: renamed from: a */
    private Runnable showToastRunnable = new Runnable() { // from class: com.corrodinggames.rts.gameFramework.l.1
        @Override // java.lang.Runnable
        public void run() {
            String str = GameEngine.this.toastMessage;
            try {
                if (str == null) {
                    GameEngine.logColored("Cannot show toast, no message");
                } else {
                    Toast.makeText(GameEngine.this.appContext, str, 1).show();
                }
            } catch (Exception e) {
                GameEngine.logColored("Error showing toast: " + ((Object) str));
                e.printStackTrace();
            }
        }
    };

    /* JADX INFO: renamed from: b */
    private Runnable showMessageBoxRunnable = new Runnable() { // from class: com.corrodinggames.rts.gameFramework.l.2
        @Override // java.lang.Runnable
        public void run() {
            DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() { // from class: com.corrodinggames.rts.gameFramework.l.2.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    GameEngine.this.isShowingDialog = false;
                }
            };
            DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() { // from class: com.corrodinggames.rts.gameFramework.l.2.2
                @Override // android.content.DialogInterface.OnCancelListener
                public void onCancel(DialogInterface dialogInterface) {
                    GameEngine.this.isShowingDialog = false;
                }
            };
            GameEngine.log("showMessageBoxRunnable context:" + GameEngine.this.appContext.getClass().getName());
            try {
                new AlertDialog.Builder(GameEngine.this.appContext).setIcon(R.drawable.ic_dialog_alert).setTitle(GameEngine.this.dialogTitle).setMessage(GameEngine.this.dialogMessage).setOnCancelListener(onCancelListener).setPositiveButton("Ok", onClickListener).show();
            } catch (WindowManager.BadTokenException e) {
                GameEngine.logColored("Failed to show message: " + GameEngine.this.dialogMessage);
                e.printStackTrace();
            }
        }
    };

    /* JADX INFO: renamed from: dH */
    public com.corrodinggames.rts.gameFramework.PlatformCallbacks platformCallbacks = null;

    /* JADX INFO: renamed from: dI */
    transient String currentLoadingStatus = null;

    /* JADX INFO: renamed from: dJ */
    Object pendingMessageLock = new Object();

    /* JADX INFO: renamed from: dM */
    public boolean[] touchPointerEnabled = new boolean[10];

    /* JADX INFO: renamed from: dN */
    protected ConcurrentLinkedQueue pendingInputEvents = new ConcurrentLinkedQueue();

    /* JADX INFO: renamed from: c */
    private boolean[] keyDownStates = new boolean[KeyEvent.a() + 1];

    /* JADX INFO: renamed from: d */
    private boolean[] keyPressPendingStates = new boolean[KeyEvent.a() + 1];

    /* JADX INFO: renamed from: dZ */
    public byte memoryProbeWriteByte = 42;

    /* JADX INFO: renamed from: ea */
    public byte memoryProbeReadByte = 42;

    /* JADX INFO: renamed from: eb */
    public final TaskQueue recurringGameThreadTasks = new TaskQueue();

    /* JADX INFO: renamed from: ec */
    public final TaskQueue pendingGameThreadTasks = new TaskQueue();

    /* JADX INFO: renamed from: ed */
    public final TaskQueue endOfFrameTasks = new TaskQueue();

    /* JADX INFO: renamed from: a */
    public abstract void init(Context context);

    /* JADX INFO: renamed from: a */
    public abstract boolean isMessageDisplayPersistent();

    /* JADX INFO: renamed from: a */
    public abstract boolean shouldSkipUpdate(boolean z);

    /* JADX INFO: renamed from: a */
    public abstract void colorizeLogMessage(Activity activity, GameView gameView, boolean z);

    /* JADX INFO: renamed from: b */
    public abstract void updateWindowResolution(int i, int i2);

    /* JADX INFO: renamed from: c */
    public abstract int getVersionCode(boolean z);

    /* JADX INFO: renamed from: n */
    public abstract boolean isBetaOrPreview();

    /* JADX INFO: renamed from: p */
    public abstract boolean isModdingEnabled();

    /* JADX INFO: renamed from: l */
    public abstract String getPackageName();

    /* JADX INFO: renamed from: m */
    public abstract String getInstallerPackageName();

    /* JADX INFO: renamed from: r */
    public abstract String getVersionNameWithSuffix();

    /* JADX INFO: renamed from: t */
    public abstract String getVersionName();

    /* JADX INFO: renamed from: u */
    public abstract String getVersion();

    /* JADX INFO: renamed from: s */
    public abstract void refreshVersionName();

    /* JADX INFO: renamed from: v */
    public abstract String getVersionString();

    /* JADX INFO: renamed from: a */
    public abstract void loadLevel(boolean z, boolean z2, GameMode gameMode) throws IOException;

    /* JADX INFO: renamed from: a */
    public abstract void loadGame(boolean z, GameMode gameMode);

    /* JADX INFO: renamed from: e */
    public abstract void stopAndReset();

    /* JADX INFO: renamed from: g */
    public abstract void clearAllObjects();

    /* JADX INFO: renamed from: x */
    public abstract void loadMenuBackground();

    /* JADX INFO: renamed from: a */
    public abstract void gameLoop(float f, int i) throws ConfigParseException, IOException;

    /* JADX INFO: renamed from: z */
    public abstract int getAllUnitsChecksum();

    /* JADX INFO: renamed from: b */
    public abstract int getFps();

    /* JADX INFO: renamed from: c */
    public abstract boolean isExtraSafeModeActive();

    /* JADX INFO: renamed from: d */
    public abstract boolean isExtraSafeModeLevel2Active();

    /* JADX INFO: renamed from: b */
    public static boolean detectDemoPackage(Context context) {
        String strH;
        if (isNonAndroidVersion) {
            strH = "dedicatedServer";
        } else {
            strH = context.g().h();
        }
        Log.d("RustedWarfare", "packageName:" + strH);
        if (strH.contains("rtsdemo")) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: A */
    public boolean isGamePaused() {
        return this.isPaused || this.pauseTransition > 0.0f || this.isMenuOpen;
    }

    /* JADX INFO: renamed from: B */
    public static final GameEngine getInstance() {
        return instance;
    }

    /* JADX INFO: renamed from: C */
    public static final boolean isSpaceGame() {
        return spaceGameMode;
    }

    /* JADX INFO: renamed from: D */
    public static final boolean isMapDebugMode() {
        return mapDebugMode;
    }

    /* JADX INFO: renamed from: c */
    public void initContext(Context context) {
        AppFrameworkUtils.setup(context);
        this.appContext = context;
    }

    /* JADX INFO: renamed from: a */
    public static synchronized GameEngine createGameEngine(Context context, com.corrodinggames.rts.gameFramework.PlatformCallbacks platformCallbacks) {
        if (instance != null) {
            if (platformCallbacks != null) {
                instance.platformCallbacks = platformCallbacks;
            }
            instance.initContext(context);
            return instance;
        }
        instance = gameEngineFactory.createGameEngine(context);
        log("Created new gameEngine of:" + instance.getClass().getName());
        if (platformCallbacks != null) {
            instance.platformCallbacks = platformCallbacks;
        }
        instance.init(context);
        return instance;
    }

    public GameEngine(Context context) {
        Log.d("RustedWarfare", "GameEngine:GameEngine()");
        if (instance != null) {
            throw new RuntimeException("gameEngine already created");
        }
        initContext(context);
        instance = this;
    }

    protected void finalize() throws Throwable {
        Log.d("RustedWarfare", "GameEngine:finalize()");
        super.finalize();
    }

    /* JADX INFO: renamed from: E */
    public boolean isKeyboardCameraScrollAllowed() {
        return true;
    }

    /* JADX INFO: renamed from: F */
    public void onInitialContentLoaded() {
    }

    /* JADX INFO: renamed from: G */
    public String getPlatformName() {
        if (isPC()) {
            return "PC";
        }
        if (isIOSVersion) {
            String strA = PlatformExtension.a();
            if (strA != null) {
                return "IOS - " + strA;
            }
            return "IOS";
        }
        if (isNonAndroidVersion) {
            return "SERVER";
        }
        return Build.MODEL;
    }

    /* JADX INFO: renamed from: H */
    public String getBuildVersion() {
        return buildVersion;
    }

    /* JADX INFO: renamed from: I */
    public boolean isMenuBackgroundMapActive() {
        if (this.isMenuBackgroundMap) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public static void log(String str, Exception exc) {
        log(str);
        exc.printStackTrace();
    }

    /* JADX INFO: renamed from: a */
    public static void logErrorColored(String str) {
        log(addColorCodes("--- ERROR: " + str, "\u001b[31m"));
    }

    /* JADX INFO: renamed from: b */
    public static void logColored(String str) {
        log(addColorCodes(str, "\u001b[33m"));
    }

    /* JADX INFO: renamed from: M */
    public boolean isNetworkGameActive() {
        if (this.networkEngine == null || !this.networkEngine.networkGameActive || this.networkEngine.singleplayerServer || this.replayEngine.j()) {
            return false;
        }
        return true;
    }

    /* JADX INFO: renamed from: N */
    public boolean isNetworkConnected() {
        if (this.networkEngine == null) {
            return false;
        }
        return this.networkEngine.networkGameActive;
    }

    /* JADX INFO: renamed from: O */
    public boolean isInGameOrLobby() {
        if (this.networkEngine == null) {
            return false;
        }
        return this.networkEngine.singleplayerServer || this.networkEngine.networkGameActive || this.replayEngine.j();
    }

    /* JADX INFO: renamed from: P */
    public boolean isSinglePlayerGame() {
        if (this.networkEngine == null || this.networkEngine.singleplayerServer) {
            return true;
        }
        return (this.networkEngine.networkGameActive || this.replayEngine.j()) ? false : true;
    }

    /* JADX INFO: renamed from: Q */
    public void clampCameraPosition() {
        this.wasCameraClamped = false;
        if (this.viewpointX < 0.0f) {
            this.viewpointX = 0.0f;
            this.wasCameraClamped = true;
        }
        if (this.viewpointY < 0.0f) {
            this.viewpointY = 0.0f;
            this.wasCameraClamped = true;
        }
        if (this.tileMap != null) {
            if (this.viewpointX > this.tileMap.getWorldWidth() - this.viewpointWidth) {
                this.viewpointX = this.tileMap.getWorldWidth() - this.viewpointWidth;
                this.wasCameraClamped = true;
            }
            if (this.viewpointY > this.tileMap.getWorldHeight() - this.visibleWorldHeight) {
                this.viewpointY = this.tileMap.getWorldHeight() - this.visibleWorldHeight;
                this.wasCameraClamped = true;
            }
            if (this.viewpointWidth > this.tileMap.getWorldWidth()) {
                this.viewpointX = (this.tileMap.getWorldWidth() / 2.0f) - (this.viewpointWidth / 2.0f);
                this.wasCameraClamped = true;
            }
            if (this.visibleWorldHeight > this.tileMap.getWorldHeight()) {
                this.viewpointY = (this.tileMap.getWorldHeight() / 2.0f) - (this.visibleWorldHeight / 2.0f);
                this.wasCameraClamped = true;
            }
        }
        setViewpoint(this.viewpointX, this.viewpointY);
    }

    /* JADX INFO: renamed from: a */
    public void setViewpoint(float f, float f2) {
        this.viewpointX = f;
        this.viewpointY = f2;
        this.viewpointXInt = (int) this.viewpointX;
        this.viewpointYInt = (int) this.viewpointY;
        this.viewpointXSnapped = ((int) (this.viewpointX * this.zoom)) / this.zoom;
        this.viewpointYSnapped = ((int) (this.viewpointY * this.zoom)) / this.zoom;
        int i = 90;
        if (isSpaceGame()) {
            i = 210;
        }
        this.bufferedVisibleWorldRect.a((int) (this.viewpointX - i), (int) (this.viewpointY - i), (int) (this.viewpointX + this.visibleWorldWidth + i), (int) (this.viewpointY + this.visibleWorldHeight + i));
        this.bufferedVisibleWorldRectF.a(this.bufferedVisibleWorldRect);
        this.visibleWorldRect.a((int) this.viewpointX, (int) this.viewpointY, (int) (this.viewpointX + this.visibleWorldWidth), (int) (this.viewpointY + this.visibleWorldHeight));
        this.extendedVisibleWorldRect.a((int) (this.viewpointX - 300), (int) (this.viewpointY - 300), (int) (this.viewpointX + this.visibleWorldWidth + 300), (int) (this.viewpointY + this.visibleWorldHeight + 300));
    }

    /* JADX INFO: renamed from: b */
    public void centerViewpoint(float f, float f2) {
        setViewpoint(f - (this.viewpointWidth / 2.0f), f2 - (this.visibleWorldHeight / 2.0f));
    }

    /* JADX INFO: renamed from: d */
    public static boolean isBlueStacks(Context context) {
        if (isNonAndroidVersion) {
            return false;
        }
        if (Build.MODEL.equals("GT-I9100") || Build.MODEL.equals("GT-I9300")) {
            try {
                WifiInfo connectionInfo = ((WifiManager) context.c("wifi")).getConnectionInfo();
                if (connectionInfo != null) {
                    if ("BlueStacks".equals(connectionInfo.getSSID())) {
                        return true;
                    }
                    return false;
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: R */
    public void applyZoomTransform() {
        if (this.zoom != 1.0f) {
            this.renderGraphicsEngine.a(this.zoom, this.zoom);
        }
    }

    /* JADX INFO: renamed from: S */
    public void restoreZoomTransform() {
        if (this.zoom != 1.0f) {
            this.renderGraphicsEngine.a(1.0f / this.zoom, 1.0f / this.zoom);
        }
    }

    /* JADX INFO: renamed from: a */
    public static void log(String str, Throwable th) {
        logColored(str);
        log(VariableScope.nullOrMissingString + th.toString());
        log("cause:" + th.getCause());
        th.printStackTrace();
    }

    /* JADX INFO: renamed from: a */
    public static String addColorCodes(String str, String str2) {
        if (isLogColorEnabled && !str.contains("\u001b[0m")) {
            str = str2 + str + "\u001b[0m";
        }
        return str;
    }

    /* JADX INFO: renamed from: e */
    public static void log(String str) {
        logError(str);
    }

    /* JADX INFO: renamed from: T */
    public static void printStackTrace() {
        for (StackTraceElement stackTraceElement : new Throwable().getStackTrace()) {
            log(stackTraceElement.toString());
        }
    }

    /* JADX INFO: renamed from: l */
    public static Integer getMapLevel(String str) {
        String filename = getFilename(str);
        log("getMapLevel for :" + str + " file:" + filename);
        Matcher matcher = Pattern.compile("^l(\\d*);.*").matcher(filename);
        if (matcher.matches()) {
            log("getMapLevel:" + str + ":" + Integer.parseInt(matcher.group(1)));
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    /* JADX INFO: renamed from: c */
    public static void logError(String str) {
        if (isJavaDesktopVersion) {
            Log.b("RustedWarfare", str);
        } else {
            Log.b("RustedWarfare", str);
        }
    }

    /* JADX INFO: renamed from: d */
    public static void logDebug(String str) {
        logError(str);
    }

    /* JADX INFO: renamed from: e */
    public static void writeCrashToFile(String str, String str2) {
        try {
            PrintWriter printWriter = new PrintWriter(FileHelper.openOutputStream(getCrashLogFile(), true));
            printWriter.write("\r\n" + str + " (at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + " - 1.15" + VariableScope.nullOrMissingString + ")\n");
            printWriter.write(str2 + "\r\n");
            printWriter.close();
        } catch (Throwable th) {
            log("Exception in writeCrashToFile");
            th.printStackTrace();
        }
    }

    /* JADX INFO: renamed from: b */
    public static void log(String str, String str2) {
        logError(str + ":" + str2);
    }

    /* JADX INFO: renamed from: f */
    public static synchronized void logWithTime(String str) {
        logError(str + " (at " + System.nanoTime() + ")");
    }

    /* JADX INFO: renamed from: a */
    public static void reportOOM(AssetType assetType, Throwable th) {
        outOfMemoryReserveBuffer = null;
        log("reportCaughtOutOfMemory:" + outOfMemoryAssetType);
        if (outOfMemoryAssetType != null) {
            return;
        }
        outOfMemoryAssetType = assetType;
        if (th != null) {
            printStackTrace(th);
        }
        printMemoryInfo();
    }

    /* JADX INFO: renamed from: U */
    public static String getStackTrace() {
        String str = VariableScope.nullOrMissingString;
        for (StackTraceElement stackTraceElement : new Throwable().getStackTrace()) {
            str = str + stackTraceElement.toString() + "\n";
        }
        return str;
    }

    /* JADX INFO: renamed from: g */
    public static void logWarningAndStack(String str) {
        logColored(str);
        printStackTrace();
    }

    /* JADX INFO: renamed from: V */
    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    /* JADX INFO: renamed from: a */
    public static final boolean hasTimeElapsed(long j, long j2) {
        long currentTimeMillis = getCurrentTimeMillis();
        if (j + j2 < currentTimeMillis || currentTimeMillis < j - 1000) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: W */
    public float getScreenScale() {
        float f = this.densityScaleRaw;
        if (this.settingsEngine != null) {
            f = f * this.settingsEngine.renderDensity * this.settingsEngine.uiRenderScale;
            if (this.settingsEngine.renderDoubleScale) {
                return f / 2.0f;
            }
        }
        return f;
    }

    /* JADX INFO: renamed from: e */
    public int toScreenPixels(float f) {
        return (int) ((f * this.screenScale) + 0.5f);
    }

    /* JADX INFO: renamed from: a */
    public int toScreenPixels(int i) {
        return (int) ((i * this.screenScale) + 0.5f);
    }

    /* JADX INFO: renamed from: c */
    public static void printStackTrace(Throwable th) {
        try {
            th.printStackTrace();
        } catch (Throwable th2) {
            log("Failed to print stacktrace");
        }
    }

    /* JADX INFO: renamed from: Y */
    protected void refreshPaintSizeTrackers() {
        Iterator it = this.paintSizeTrackers.iterator();
        while (it.hasNext()) {
            this.renderGraphicsEngine.a(((PaintSizeTracker) it.next()).paint);
        }
        this.paintSizeTrackersReady = true;
    }

    /* JADX INFO: renamed from: a */
    public void updatePaint(Paint paint) {
        updatePaintTextSize(paint, 16.0f);
    }

    /* JADX INFO: renamed from: a */
    public void updatePaintTextSize(Paint paint, float f) {
        PaintSizeTracker paintSizeTracker = new PaintSizeTracker(this);
        paintSizeTracker.textSize = f;
        paintSizeTracker.paint = paint;
        paintSizeTracker.a();
        synchronized (this.paintSizeTrackers) {
            this.paintSizeTrackers.add(paintSizeTracker);
        }
        if (this.paintSizeTrackersReady) {
            this.renderGraphicsEngine.a(paintSizeTracker.paint);
        }
    }

    /* JADX INFO: renamed from: b */
    public void setScaledTextSize(Paint paint, float f) {
        float screenPixels = toScreenPixels(f);
        if (paint.k() != screenPixels) {
            paint.b(screenPixels);
        }
    }

    /* JADX INFO: renamed from: h */
    public void loadLevel(String str) {
        loadLevel(str, true);
    }

    /* JADX INFO: renamed from: a */
    public void loadLevel(String str, boolean z) {
        this.currentLoadingStatus = str;
        if (this.platformCallbacks != null) {
            this.platformCallbacks.a(str, z);
        }
    }

    /* JADX INFO: renamed from: Z */
    public void clearCurrentLoadingStatus() {
        this.currentLoadingStatus = null;
    }

    /* JADX INFO: renamed from: i */
    public void alert(String str) {
        alert(str, 1);
    }

    /* JADX INFO: renamed from: J */
    public synchronized void startGameThread() {
        log("--- setRunning ---");
        if (!isPC() && !isIOSVersion) {
            this.musicManager.resume();
        }
        if (!isPCOrIOSVersion && !isDesktopInitialized && this.gameThread == null) {
            this.gameThread = new GameThread();
            this.gameThread.a(true);
            this.gameThread.start();
        }
    }

    /* JADX INFO: renamed from: aa */
    public boolean isMissionActive() {
        if (this.platformCallbacks != null) {
            return this.platformCallbacks.c();
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public void showMessageBox(String str, LocaleString localeString) {
        String strResolveText = null;
        if (localeString != null) {
            strResolveText = localeString.resolveText();
        }
        showMessageBox(str, strResolveText);
    }

    /* JADX INFO: renamed from: c */
    public void showMessageBox(String str, String str2) {
        if (this.platformCallbacks != null) {
            this.platformCallbacks.a(str, str2);
        }
        if (isNonAndroidVersion) {
            if (this.platformCallbacks == null) {
                logColored("showMessageBox: not showing due to non-android:" + str2);
            }
        } else {
            this.isShowingDialog = true;
            this.dialogTitle = str;
            this.dialogMessage = str2;
            this.uiDispatchHandler.a(this.showMessageBoxRunnable);
        }
    }

    /* JADX INFO: renamed from: ab */
    public void showPendingMessageBox() {
        synchronized (this.pendingMessageLock) {
            if (this.pendingMessageBody != null) {
                showMessageBox(this.pendingMessageTitle, this.pendingMessageBody);
                this.pendingMessageBody = null;
                this.pendingMessageTitle = null;
            }
        }
    }

    /* JADX INFO: renamed from: d */
    public void setPendingMessageBox(String str, String str2) {
        this.pendingMessageTitle = str;
        this.pendingMessageBody = str2;
        if (isPCOrIOSVersion) {
            showPendingMessageBox();
        } else {
            new Thread() { // from class: com.corrodinggames.rts.gameFramework.l.3
                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    try {
                        sleep(3000L);
                    } catch (InterruptedException e) {
                    }
                    GameEngine.this.showPendingMessageBox();
                }
            }.start();
        }
    }

    /* JADX INFO: renamed from: ac */
    public boolean isTouchDown() {
        if (this.isStopped || this.activeGameView.getSettings() == null) {
            return false;
        }
        return this.activeGameView.getSettings().wasDown();
    }

    /* JADX INFO: renamed from: ad */
    public void updateTouchInput() {
        if (this.activeGameView.getSettings() == null) {
            return;
        }
        this.activeGameView.getSettings().updateState();
    }

    /* JADX INFO: renamed from: ae */
    public int getTouchPointerCount() {
        if (this.isStopped) {
            return 0;
        }
        return this.activeGameView.getSettings().getLastNumPointers();
    }

    /* JADX INFO: renamed from: af */
    public float getTouchX() {
        return getTouchX(0);
    }

    /* JADX INFO: renamed from: ag */
    public float getTouchY() {
        return getTouchY(0);
    }

    /* JADX INFO: renamed from: b */
    public float getTouchX(int i) {
        if (this.activeGameView == null) {
            return 0.0f;
        }
        if (this.settingsEngine.renderDoubleScale) {
            return this.activeGameView.getSettings().getX()[i] / 2.0f;
        }
        return this.activeGameView.getSettings().getX()[i];
    }

    /* JADX INFO: renamed from: c */
    public float getTouchY(int i) {
        if (this.activeGameView == null) {
            return 0.0f;
        }
        if (this.settingsEngine.renderDoubleScale) {
            return this.activeGameView.getSettings().getY()[i] / 2.0f;
        }
        return this.activeGameView.getSettings().getY()[i];
    }

    /* JADX INFO: renamed from: d */
    public int getTouchPointerId(int i) {
        return this.activeGameView.getSettings().getPointerIndices()[i];
    }

    /* JADX INFO: renamed from: e */
    public boolean isMouseButtonPressed(int i) {
        if (i != 1 && i != 2 && i != 3) {
            throw new RuntimeException("Unknown mouseButton:" + i);
        }
        if (findTouchPointerIndex(i) != -1) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: f */
    public int findTouchPointerIndex(int i) {
        if (i == 0) {
            throw new RuntimeException("finding state of 0 doesn't make sense");
        }
        int[] pointerIndices = this.activeGameView.getSettings().getPointerIndices();
        for (int i2 = 0; i2 < pointerIndices.length; i2++) {
            if (pointerIndices[i2] == i) {
                return i2;
            }
        }
        return -1;
    }

    /* JADX INFO: renamed from: g */
    public boolean consumeKeyPress(int i) {
        if (i < this.keyDownStates.length && i >= 0 && this.keyDownStates[i] && this.keyPressPendingStates[i]) {
            this.keyPressPendingStates[i] = false;
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: h */
    public boolean isKeyPressed(int i) {
        if (i >= this.keyDownStates.length || i < 0) {
            return false;
        }
        return this.keyDownStates[i];
    }

    /* JADX INFO: renamed from: a */
    public boolean checkModifierKeys(int i, boolean z) {
        boolean z2 = true;
        boolean z3 = true;
        int modifierState = getModifierState();
        if ((i & 2) != 0) {
            if ((modifierState & 2) == 0) {
                z2 = false;
            }
        } else if ((modifierState & 2) != 0) {
            z3 = false;
        }
        if ((i & 1) != 0) {
            if ((modifierState & 1) == 0) {
                z2 = false;
            }
        } else if ((modifierState & 1) != 0) {
            z3 = false;
        }
        if ((i & 4) != 0) {
            if ((modifierState & 4) == 0) {
                z2 = false;
            }
        } else if ((modifierState & 4) != 0) {
            z3 = false;
        }
        if (z) {
            return z2;
        }
        return z2 && z3;
    }

    /* JADX INFO: renamed from: i */
    public boolean isModifierKey(int i) {
        if (i == 59 || i == 60 || i == 113 || i == 114 || i == 57 || i == 58) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: j */
    public static String getModifierString(int i) {
        String str = VariableScope.nullOrMissingString;
        if ((i & 2) != 0) {
            str = str + "shift+";
        }
        if ((i & 1) != 0) {
            str = str + "ctrl+";
        }
        if ((i & 4) != 0) {
            str = str + "alt+";
        }
        return str;
    }

    /* JADX INFO: renamed from: ah */
    public int getModifierState() {
        int i = 0;
        if (isKeyPressed(59) || isKeyPressed(60)) {
            i = 0 + 2;
        }
        if (isKeyPressed(113) || isKeyPressed(114)) {
            i++;
        }
        if (isKeyPressed(57) || isKeyPressed(58)) {
            i += 4;
        }
        return i;
    }

    /* JADX INFO: renamed from: c */
    public boolean isAnyKeyPressed(int i, int i2) {
        boolean z = false;
        boolean z2 = false;
        if (i >= 0 && i < this.keyDownStates.length) {
            z = this.keyDownStates[i];
        }
        if (i2 >= 0 && i2 < this.keyDownStates.length) {
            z2 = this.keyDownStates[i2];
        }
        return z || z2;
    }

    /* JADX INFO: renamed from: K */
    public synchronized void stopGameThreadIfNotInGameThread() {
        log("--- setStoppedIfNotInGameThread ---");
        if (Thread.currentThread() != this.gameThread) {
            stopGameThread();
        }
    }

    /* JADX INFO: renamed from: k */
    public void queueMouseWheelDelta(int i) {
        this.pendingInputEvents.add(new QueuedMouseWheelEvent(this, i));
    }

    /* JADX INFO: renamed from: ai */
    public int getMouseWheelDelta() {
        return this.accumulatedMouseWheelDelta;
    }

    /* JADX INFO: renamed from: aj */
    protected void processPendingInputEvents() {
        this.accumulatedMouseWheelDelta = 0;
        while (true) {
            QueuedInputEvent inputEvent = (QueuedInputEvent) this.pendingInputEvents.poll();
            if (inputEvent != null) {
                if (inputEvent instanceof QueuedKeyStateEvent) {
                    QueuedKeyStateEvent keyStateEvent = (QueuedKeyStateEvent) inputEvent;
                    if (keyStateEvent.keyCode >= this.keyDownStates.length || keyStateEvent.keyCode < 0) {
                        log("updateKeyState", "keyCode (" + keyStateEvent.keyCode + ") is out of range");
                    } else {
                        this.keyDownStates[keyStateEvent.keyCode] = !keyStateEvent.isPressed;
                        this.keyPressPendingStates[keyStateEvent.keyCode] = !keyStateEvent.isPressed;
                    }
                } else if (inputEvent instanceof QueuedMouseWheelEvent) {
                    this.accumulatedMouseWheelDelta += ((QueuedMouseWheelEvent) inputEvent).delta;
                }
            } else {
                return;
            }
        }
    }

    /* JADX INFO: renamed from: j */
    public static String getParentDirectory(String str) {
        int iLastIndexOf = str.lastIndexOf("/");
        if (iLastIndexOf == -1) {
            iLastIndexOf = str.length();
        }
        return str.substring(0, iLastIndexOf);
    }

    /* JADX INFO: renamed from: k */
    public static String getFilename(String str) {
        int i;
        int iLastIndexOf = str.lastIndexOf("/");
        if (iLastIndexOf == -1) {
            i = 0;
        } else {
            i = iLastIndexOf + 1;
        }
        return str.substring(i);
    }

    /* JADX INFO: renamed from: L */
    public synchronized void stopGameThread() {
        log("--- setStopped ---");
        if (this.gameThread == null) {
            Log.d("RustedWarfare", "gameThread already null");
            return;
        }
        if (!isPC()) {
            this.musicManager.pause();
        }
        this.gameThread.a(false);
        if (Thread.currentThread() != this.gameThread) {
            boolean z = true;
            while (z) {
                try {
                    this.gameThread.join();
                    z = false;
                } catch (InterruptedException e) {
                }
            }
            Log.d("RustedWarfare", "thread stop");
        } else {
            logWarningAndStack("currentThread is game thread");
        }
        this.gameThread = null;
        if (this.activeGameView != null) {
            this.activeGameView.onSizeChanged();
        }
        if (this.isBenchmarking) {
            Debug.stopMethodTracing();
        }
    }

    /* JADX INFO: renamed from: m */
    public static String findNextLevel(String str) {
        GameEngine gameEngine = getInstance();
        Integer mapLevel = getMapLevel(str);
        if (mapLevel == null) {
            return null;
        }
        int iLastIndexOf = str.lastIndexOf("/");
        if (iLastIndexOf == -1) {
            iLastIndexOf = str.length();
        }
        String strSubstring = str.substring(0, iLastIndexOf);
        String[] strArrListFilesRecursive = FileHelper.listFilesRecursive(strSubstring, true);
        if (strArrListFilesRecursive == null) {
            return null;
        }
        for (String str2 : strArrListFilesRecursive) {
            Integer mapLevel2 = getMapLevel(str2);
            if (mapLevel2 != null && mapLevel2.intValue() > mapLevel.intValue() && (!gameEngine.isDemo || LevelSelectActivity.isDemoMap(str2, strSubstring + "/" + str2))) {
                return strSubstring + "/" + str2;
            }
        }
        return null;
    }

    /* JADX INFO: renamed from: ak */
    public String getCurrentMapPath() {
        return this.currentMapPath;
    }

    /* JADX INFO: renamed from: al */
    public String getCurrentMapName() {
        String strL = this.currentMapPath;
        if ((this.currentMapPath == null || VariableScope.nullOrMissingString.equals(this.currentMapPath)) && isNetworkConnected()) {
            strL = this.networkEngine.l();
        }
        return LevelSelectActivity.getMapName(LevelSelectActivity.getMapNameFromPath(strL));
    }

    /* JADX INFO: renamed from: am */
    public String getCurrentMapFilename() {
        return LevelSelectActivity.getMapNameFromPath(this.currentMapPath);
    }

    /* JADX INFO: renamed from: an */
    public GameModeType getGameModeType() {
        if (LevelSelectActivity.isFromSdCard(this.currentMapPath)) {
            return GameModeType.customMap;
        }
        return GameModeType.skirmishMap;
    }

    /* JADX INFO: renamed from: a */
    public static String getStackTrace(Throwable th) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        th.printStackTrace(printWriter);
        String string = stringWriter.toString();
        printWriter.close();
        return string;
    }

    /* JADX INFO: renamed from: b */
    public static String getSimpleExceptionMessage(Throwable th) {
        String strReplace;
        Throwable th2;
        String message = th.getMessage();
        if (message == null) {
            strReplace = th.getClass().getName();
        } else {
            strReplace = message.replace("java.lang.RuntimeException: ", VariableScope.nullOrMissingString).replace("java.lang.RuntimeException: ", VariableScope.nullOrMissingString);
        }
        Throwable th3 = th;
        while (true) {
            th2 = th3;
            if (th2 == null) {
                break;
            }
            Throwable cause = th2.getCause();
            if (cause == null || cause == th || cause == th2) {
                break;
            }
            th3 = cause;
        }
        if (th2 != null && th2 != th) {
            String message2 = th2.getMessage();
            if (message2 == null) {
                message2 = th2.getClass().getName();
            }
            if (!message2.equals(strReplace)) {
                strReplace = strReplace + " caused by (" + message2 + ")";
            }
        }
        return strReplace;
    }

    /* JADX INFO: renamed from: ao */
    public static File getCrashLogFile() {
        FileHelper.getGameDataPath();
        String str = "/SD/rustedWarfare/crashes.txt";
        if (isAndroidPlatform()) {
            str = "/SD/rustedWarfare/crashes.txt";
        }
        return new File(FileHelper.convertAbstractPath(str));
    }

    /* JADX INFO: renamed from: X */
    public void updateDensity() {
        if (this.lastScreenScale != this.screenScale) {
            log("Density size changed now: " + this.screenScale + ", refreshing fonts");
            synchronized (this.paintSizeTrackers) {
                Iterator it = this.paintSizeTrackers.iterator();
                while (it.hasNext()) {
                    ((PaintSizeTracker) it.next()).a();
                }
            }
            this.lastScreenScale = this.screenScale;
            if (this.renderGraphicsEngine != null) {
            }
        }
    }

    /* JADX INFO: renamed from: ap */
    public static void setupANRWatchdog() {
        if (!isTestingBuild || isNonAndroidVersion) {
            return;
        }
        if (anrWatchdog != null) {
            logColored("setupANRWatchDog: activeANRWatchDog!=null");
            return;
        }
        anrWatchdog = new ANRWatchdog(4000);
        anrWatchdog.a(new ANRCallback() { // from class: com.corrodinggames.rts.gameFramework.l.4
            @Override // com.corrodinggames.rts.gameFramework.utility.ANRCallback
            public void a(ANRException aNRException) {
                if (GameEngine.hasDetectedANR) {
                    GameEngine.logColored("activeANRWatchDog: ANR already detected");
                }
                GameEngine.hasDetectedANR = true;
                GameEngine.logColored("activeANRWatchDog: ANR detected");
                String stackTrace = GameEngine.getStackTrace(aNRException);
                MasterServerClient.sendErrorReportAsync("detectedANR", stackTrace);
                try {
                    Thread.sleep(400L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(GameSaver.getSaveFile("lastFreeze", VariableScope.nullOrMissingString, true));
                    PrintStream printStream = new PrintStream(fileOutputStream);
                    printStream.print(stackTrace);
                    printStream.close();
                    fileOutputStream.close();
                } catch (IOException e2) {
                    throw new RuntimeException(e2);
                }
            }
        });
        anrWatchdog.start();
        logColored("setupANRWatchDog: running");
    }

    /* JADX INFO: renamed from: aq */
    public static void setupUncaughtExceptionHandler() {
        if (exceptionHandlerMemoryReserve == null && isPC()) {
            exceptionHandlerMemoryReserve = new byte[2500000];
            exceptionHandlerMemoryReserve[0] = 2;
            exceptionHandlerMemoryReserve[exceptionHandlerMemoryReserve.length - 1] = 5;
        }
        if (useCurrentThreadExceptionHandler) {
            Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.currentThread().getUncaughtExceptionHandler();
            if (!(uncaughtExceptionHandler instanceof CustomExceptionHandler)) {
                Thread.currentThread().setUncaughtExceptionHandler(new CustomExceptionHandler(uncaughtExceptionHandler));
                return;
            }
            return;
        }
        Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (!(defaultUncaughtExceptionHandler instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(defaultUncaughtExceptionHandler));
        }
    }

    /* JADX INFO: renamed from: ar */
    public boolean shouldUpdateFogRenderPass() {
        return true;
    }

    /* JADX INFO: renamed from: as */
    public boolean usesCoreUnitTypes() {
        return true;
    }

    /* JADX INFO: renamed from: n */
    public static void reportProblem(String str) {
        GameEngine gameEngine = getInstance();
        if (gameEngine != null) {
            reportedProblemCount++;
            if (reportedProblemCount < 1000) {
                logColored("reportProblem: " + str);
            }
            if (reportedProblemCount < 10) {
                gameEngine.alert(str, 1);
            }
        }
    }

    /* JADX INFO: renamed from: at */
    public static boolean isAndroidPlatform() {
        return !isNonAndroidVersion;
    }

    /* JADX INFO: renamed from: au */
    public static boolean isNonPCPlatform() {
        return !isPCOrIOSVersion || isIOSVersion;
    }

    /* JADX INFO: renamed from: av */
    public static boolean isPC() {
        return isPCOrIOSVersion && !isIOSVersion;
    }

    /* JADX INFO: renamed from: aw */
    public static boolean isDesktopMouseInput() {
        return isPCOrIOSVersion && !isIOSVersion;
    }

    /* JADX INFO: renamed from: ax */
    public static boolean isDedicatedServer() {
        return isNonAndroidVersion && !isPCOrIOSVersion;
    }

    /* JADX INFO: renamed from: ay */
    public boolean isInNetworkOrReplay() {
        return this.networkEngine.networkGameActive || this.replayEngine.j();
    }

    /* JADX INFO: renamed from: a */
    public void pingMinimap(BaseUnit baseUnit, float f) {
        this.minimap.ping((int) baseUnit.posX, (int) baseUnit.posY, f, baseUnit);
        this.gameUI.warLogDisplay.logUnitDamaged(baseUnit);
    }

    /* JADX INFO: renamed from: az */
    public static boolean areShadersSupported() {
        GameEngine gameEngine = getInstance();
        if (gameEngine != null && gameEngine.settingsEngine.teamShaders && (gameEngine.settingsEngine.newRender || !isAndroidPlatform())) {
            return true;
        }
        return isTeamShadersEnabled;
    }

    /* JADX INFO: renamed from: aA */
    public static boolean isPostProcessingSupported() {
        GameEngine gameEngine = getInstance();
        if (gameEngine != null && gameEngine.settingsEngine.shaderEffects && (gameEngine.settingsEngine.newRender || !isAndroidPlatform())) {
            return true;
        }
        return isPostProcessingEnabled;
    }

    /* JADX INFO: renamed from: aB */
    public static boolean isFancyWaterSupported() {
        GameEngine gameEngine = getInstance();
        if (gameEngine != null && gameEngine.settingsEngine.shaderEffects && (gameEngine.settingsEngine.newRender || !isAndroidPlatform())) {
            return true;
        }
        return isPostProcessingEnabled;
    }

    /* JADX INFO: renamed from: aC */
    public static void printMemoryInfo() {
        System.out.println("Free memory (bytes): " + Runtime.getRuntime().freeMemory());
        long jMaxMemory = Runtime.getRuntime().maxMemory();
        System.out.println("Maximum memory (bytes): " + (jMaxMemory == Long.MAX_VALUE ? "no limit" : Long.valueOf(jMaxMemory)));
        System.out.println("Total memory (bytes): " + Runtime.getRuntime().totalMemory());
    }

    /* JADX INFO: renamed from: aD */
    public Context clearGameState() {
        return this.appContext;
    }

    /* JADX INFO: renamed from: f */
    public static void addUIMessage(String str, String str2) {
        GameEngine gameEngine = getInstance();
        if (gameEngine == null) {
            return;
        }
        if (gameEngine.gameUI != null && gameEngine.gameUI.messageManager != null) {
            gameEngine.gameUI.messageManager.addMessage(str, str2);
        } else {
            logWarningAndStack("addMessage: interfaceEngine/messageInterface==null");
        }
    }

    /* JADX INFO: renamed from: a */
    public void alert(String str, int i) {
        if (isNonAndroidVersion) {
            log("alert:" + str);
        } else if (str == null) {
            logWarningAndStack("Cannot show alert, no message text");
        } else {
            this.toastMessage = str;
            this.uiDispatchHandler.a(this.showToastRunnable);
        }
        if (this.platformCallbacks != null) {
            this.platformCallbacks.a(str, i);
        }
    }

    /* JADX INFO: renamed from: b */
    public void setKeyState(int i, boolean z) {
        if (i >= 0 && i < this.keyDownStates.length) {
            this.keyDownStates[i] = z;
            if (z) {
                this.keyPressPendingStates[i] = z;
                return;
            }
            return;
        }
        log("setKeyState: Key out of range:" + i);
    }

    /* JADX INFO: renamed from: aE */
    public void showMemoryWarningsIfNeeded() {
        if (lowMemoryWarningPending && !hasShownLowMemoryWarning) {
            hasShownLowMemoryWarning = true;
            String str = "Warning game has less than 5mb of free space remaining. A larger battle might cause a crash. ";
            int activeModCount = this.modManager.getActiveModCount();
            if (activeModCount > 1) {
                str = str + "This is often caused by large mods, you currently have: " + activeModCount + " mods loaded. ";
            }
            showMessageBox("Warning: Low memory detected", str);
        }
        if (!hasShownOutOfMemoryMessage && outOfMemoryAssetType != null) {
            log("Showing out of memory message");
            hasShownOutOfMemoryMessage = true;
            String str2 = "trying to load data";
            if (outOfMemoryAssetType == AssetType.gameImage) {
                str2 = "trying to load game textures";
            } else if (outOfMemoryAssetType == AssetType.gameImageCreate) {
                str2 = "trying to create a texture";
            } else if (outOfMemoryAssetType == AssetType.gameImageColor) {
                str2 = "trying to colour new texture";
            } else if (outOfMemoryAssetType == AssetType.gameImageFogBuffer) {
                str2 = "trying to create texture buffer for on-screen fog fading";
            } else if (outOfMemoryAssetType == AssetType.gameFont) {
                str2 = "trying to create game fonts";
            } else if (outOfMemoryAssetType == AssetType.gameSound) {
                str2 = "trying to load game sounds";
            } else if (outOfMemoryAssetType == AssetType.uiImage) {
                str2 = "trying to load UI textures";
            }
            String str3 = "The game ran out of memory " + str2 + ". ";
            int activeModCount2 = this.modManager.getActiveModCount();
            if (activeModCount2 > 1) {
                str3 = str3 + "This is often caused by large mods, you currently have: " + activeModCount2 + " mods. ";
            }
            if (isPC() && !GameLogic.isCheatingEnabled) {
                str3 = str3 + "You are also using the 32 bit version, switching to the 64 bit version might help. ";
            }
            showMessageBox("Warning: Out Of Memory", str3);
        }
    }

    /* JADX INFO: renamed from: aF */
    public void probeLowMemory() {
        try {
            byte[] bArr = new byte[5000000];
            bArr[0] = this.memoryProbeWriteByte;
            this.memoryProbeReadByte = bArr[1];
        } catch (OutOfMemoryError e) {
            System.gc();
            log("Low memory detected");
            e.printStackTrace();
            lowMemoryWarningPending = true;
        }
    }

    /* JADX INFO: renamed from: a */
    public void queueGameThreadTask(Runnable runnable) {
        this.pendingGameThreadTasks.a(runnable);
    }

    /* JADX INFO: renamed from: a */
    public final boolean isCircleVisibleInCamera(float f, float f2, float f3) {
        return this.visibleScreenRect.a < f + f3 && f - f3 < this.visibleScreenRect.c && this.visibleScreenRect.b < f2 + f3 && f2 - f3 < this.visibleScreenRect.d;
    }

    /* JADX INFO: renamed from: o */
    public static boolean isPlatformName(String str) {
        if (platformName == null) {
            return false;
        }
        return platformName.contains(str);
    }

    /* JADX INFO: renamed from: p */
    public static void reportNonFatalError(String str) {
        NetworkEngine networkEngine = getInstance().networkEngine;
        String str2 = VariableScope.nullOrMissingString + str;
        logColored(str2);
        printStackTrace();
        nonFatalErrorReportCount++;
        if (nonFatalErrorReportCount < 10 && networkEngine != null) {
            networkEngine.sendChatMessage(str2);
        }
    }

    /* JADX INFO: renamed from: a */
    public void setupTeamStats(StatType statType, StatGroup statGroup) {
        this.teamStats = new TeamStats(statType, statGroup);
        this.teamStats.rebuild();
    }
}
