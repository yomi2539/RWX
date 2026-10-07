package com.corrodinggames.rts.game;

import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.ai.AIController;
import com.corrodinggames.rts.game.map.MapLoadException;
import com.corrodinggames.rts.game.map.TileMap;
import com.corrodinggames.rts.game.units.*;
import com.corrodinggames.rts.game.units.custom.ConfigParseException;
import com.corrodinggames.rts.game.units.custom.CustomUnit;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfigParser;
import com.corrodinggames.rts.game.units.management.UnitSpatialIndex;
import com.corrodinggames.rts.gameFramework.*;
import com.corrodinggames.rts.gameFramework.GameObject;
import com.corrodinggames.rts.gameFramework.audio.SoundEngine;
import com.corrodinggames.rts.gameFramework.debug.DebugServer;
import com.corrodinggames.rts.gameFramework.effects.BuildPreview;
import com.corrodinggames.rts.gameFramework.effects.CloudRenderer;
import com.corrodinggames.rts.gameFramework.effects.EffectEmitter;
import com.corrodinggames.rts.gameFramework.effects.EffectManager;
import com.corrodinggames.rts.gameFramework.file.FileHelper;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.local.Locale;
import com.corrodinggames.rts.gameFramework.mod.ModManager;
import com.corrodinggames.rts.gameFramework.network.NetworkEngine;
import com.corrodinggames.rts.gameFramework.path.PathEngine;
import com.corrodinggames.rts.gameFramework.stats.StatGroup;
import com.corrodinggames.rts.gameFramework.stats.StatType;
import com.corrodinggames.rts.gameFramework.steam.DisabledSteamEngine;
import com.corrodinggames.rts.gameFramework.ui.GameUI;
import com.corrodinggames.rts.gameFramework.ui.Minimap;
import com.corrodinggames.rts.gameFramework.utility.*;
import io.github.rwx.AppMetadataBridge;
import io.github.rwx.PlatformBridge;
import io.github.rwx.geometry.Rect;
import io.github.rwx.geometry.RectF;
import io.github.rwx.mod.CommandQueue;
import io.github.rwx.mod.UnitEventRuntime;
import io.github.rwx.mod.impl.ModScheduler;
import io.github.rwx.mod.registry.UiRegistry;
import io.github.rwx.platform.CoreGameView;
import io.github.rwx.render.canvas.ArgbColor;
import io.github.rwx.render.canvas.Paint;
import io.github.rwx.render.canvas.Typeface;
import io.github.rwx.ui.InGameMenuController;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.i */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/i.class */
public class GameLogic extends GameEngine {

    /* JADX INFO: renamed from: a */
    public static String gameVersionName;

    /* JADX INFO: renamed from: b */
    public static boolean isCheatingEnabled;

    /* JADX INFO: renamed from: c */
    public static boolean isSandboxEnabled;

    /* JADX INFO: renamed from: d */
    int allUnitsChecksum;

    /* JADX INFO: renamed from: e */
    public float densityScaleMultiplier;

    /* JADX INFO: renamed from: f */
    public static String safeModeReasonText = null;

    /* JADX INFO: renamed from: i */
    public boolean hasStartedBenchmarkTrace;

    /* JADX INFO: renamed from: j */
    public int cleanupCounter;

    /* JADX INFO: renamed from: k */
    public ConcurrentLinkedQueue gameThreadRunnableQueue;

    public transient Runnable worldFrameRenderedListener;

    Paint l;

    /* JADX INFO: renamed from: m */
    Paint fpsPaint;

    Paint n;

    Paint o;

    Paint p;

    /* JADX INFO: renamed from: q */
    int fpsAccumulator;

    /* JADX INFO: renamed from: r */
    int fpsFrameCounter;

    /* JADX INFO: renamed from: s */
    int fps;

    /* JADX INFO: renamed from: t */
    float averageFrameTime;

    /* JADX INFO: renamed from: u */
    public String fpsString;

    Rect v;

    public ArrayList w;

    Paint x;

    Paint y;

    Paint z;

    public Paint A;

    /* JADX INFO: renamed from: B */
    public GameStateData gameStateData;

    /* JADX INFO: renamed from: C */
    public GameStateManager gameStateManager;

    /* JADX INFO: renamed from: D */
    public CloudRenderer cloudRenderer;

    GameObject E;

    /* JADX INFO: renamed from: F */
    boolean hasCheckedSafeMode;

    /* JADX INFO: renamed from: G */
    float accumulator;

    /* JADX INFO: renamed from: H */
    public float speedMultiplier;

    public float I;

    /* JADX INFO: renamed from: J */
    public float lastDelta;

    /* JADX INFO: renamed from: K */
    FrameBufferHelper postBaseBuffer;

    /* JADX INFO: renamed from: L */
    FrameBufferHelper postDisplacementBuffer;

    /* JADX INFO: renamed from: M */
    boolean postProcessingFailed;

    /* JADX INFO: renamed from: N */
    GraphicsEngine previousRenderGraphicsEngine;

    private GraphicsEngine waterTextureBackend;

    /* JADX INFO: renamed from: O */
    Texture waterCloudTexture;

    /* JADX INFO: renamed from: P */
    Texture waterLayer1Texture;

    /* JADX INFO: renamed from: Q */
    Texture waterLayer2Texture;

    /* JADX INFO: renamed from: R */
    float waterAnimationTimer;

    /* JADX INFO: renamed from: S */
    Rect waterRect;

    /* JADX INFO: renamed from: T */
    RectF waterRectF;

    public Texture U;

    public Texture V;

    /* JADX INFO: renamed from: W */
    GameObjectArrayList renderList;

    /* JADX INFO: renamed from: X */
    GameObjectArrayList renderListBuffer;

    public ArrayList Z;

    public ArrayList aa;

    /* JADX INFO: renamed from: ab */
    Timer gameTimer;

    boolean ac;

    /* JADX INFO: renamed from: ad */
    Object initLock;

    /* JADX INFO: renamed from: ae */
    int menuLoadFailureCount;

    /* JADX INFO: renamed from: af */
    BaseUnit cameraFocusUnit;

    /* JADX INFO: renamed from: ag */
    BaseUnit nextCameraFocusUnit;

    /* JADX INFO: renamed from: ah */
    float cameraFocusTransition;

    /* JADX INFO: renamed from: ai */
    boolean isCameraFocusing;

    public GameLogic() {
        super();
        this.densityScaleMultiplier = 1.0f;
        this.hasStartedBenchmarkTrace = false;
        this.cleanupCounter = 0;
        this.gameThreadRunnableQueue = new ConcurrentLinkedQueue();
        this.fpsAccumulator = 0;
        this.fpsFrameCounter = 0;
        this.fps = 0;
        this.averageFrameTime = 16.0f;
        this.fpsString = "0fps";
        this.v = new Rect();
        this.w = new ArrayList();
        this.A = new Paint();
        this.cloudRenderer = new CloudRenderer();
        this.accumulator = 0.0f;
        this.speedMultiplier = 1.0f;
        this.waterAnimationTimer = 0.0f;
        this.waterRect = new Rect();
        this.waterRectF = new RectF();
        this.U = null;
        this.V = null;
        this.renderList = new GameObjectArrayList("allOnScreenObjects");
        this.renderListBuffer = new GameObjectArrayList("allOnScreenObjectsDirty");
        this.Z = new ArrayList();
        this.aa = new ArrayList();
        this.initLock = new Object();
        this.menuLoadFailureCount = 0;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: a */
    public boolean isMessageDisplayPersistent() {
        if (this.gameUI.isDraggingSelection) {
            return true;
        }
        if (this.platformCallbacks != null && this.platformCallbacks.b()) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: a */
    public boolean shouldSkipUpdate(boolean z) {
        if (!z || this.replayEngine.j()) {
            if (this.gameUI.isDraggingSelection || this.isShowingDialog) {
                return true;
            }
            if (this.isStopped && !this.isMenuBackgroundMap) {
                return true;
            }
            if (this.exitGameThread && this.platformCallbacks != null && this.platformCallbacks.b()) {
                return true;
            }
        }
        if ((z && !this.networkEngine.gameHasBeenStarted) || this.networkEngine.shouldGameBePausedForPathfinding()) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: b */
    public int getFps() {
        return this.fps;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: c */
    public boolean isExtraSafeModeActive() {
        return this.isExtraSafeMode;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: d */
    public boolean isExtraSafeModeLevel2Active() {
        return this.isExtraSafeModeLevel2;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: a */
    public synchronized void init() {
        Log.d("RustedWarfare", "--- ----------------- ----");
        Log.d("RustedWarfare", "--- GameEngine:init() ----");
        Log.d("RustedWarfare", "--- ----------------- ----");
        if (this.isInitialized) {
            Log.d("RustedWarfare", "GameEngine init has already been called");
            return;
        }
        GameEngine.log("Version:" + getVersionNameWithSuffix());
        if (isSpaceGame() && getClass().equals(GameLogic.class)) {
            throw new RuntimeException("inSpace but class is:" + getClass());
        }
        System.gc();
        loadLevel("Asset Index");
        this.assetIndex = new AssetIndex();
        long jA = PerformanceProfiler.a();
        this.performanceProfiler = new PerformanceProfiler(this);
        this.performanceProfiler.a(ProfilerSection.init_total);
        if (isNonAndroidVersion) {
            this.densityScaleRaw = 1.0f;
        } else {
            int widthPixels = this.currentScreenWidthPixels > 0.0f ? (int) this.currentScreenWidthPixels : 800;
            int heightPixels = this.currentScreenHeightPixels > 0.0f ? (int) this.currentScreenHeightPixels : 600;
            PlatformBridge bridge = org.koin.java.KoinJavaComponent.get(PlatformBridge.class);
            this.densityScaleRaw = bridge.getDisplayDensity();
            GameEngine.log("densityScaleRaw: " + this.densityScaleRaw);
            updateDensity(widthPixels, heightPixels);
        }
        this.densityScaleRaw *= this.densityScaleMultiplier;
        GameEngine.log("densityScaleRaw*densityScaleMultiplier: " + this.densityScaleRaw);
        if (GameEngine.detectDemoPackage()) {
            this.isDemo = true;
        }
        //this.gameObjectE = new Unit();
        this.isGameRecording = false;
        loadLevel("InputController");
        this.inputController = new InputController();
        this.inputController.a();
        loadLevel("SettingsEngine");
        this.settingsEngine = SettingsEngine.getInstance();
        this.settingsEngine.loadKeyBindingsFromPreferences();
        this.settingsEngine.loadMainExternalFolder(true);
        FileHelper.initialize();
        int i = 3;
        if (isIOSVersion) {
            i = 1;
        }
        if (this.settingsEngine.numIncompleteLoadAttempts > 1 || this.settingsEngine.numLoadsSinceRunningGameOrNormalExit > i) {
            this.isSafeMode = true;
            if (this.settingsEngine.numIncompleteLoadAttempts > 2 || this.settingsEngine.numLoadsSinceRunningGameOrNormalExit > 4) {
                this.settingsEngine.forceEnglish = true;
                this.isEnglishForcedBySafeMode = true;
            }
            if (this.settingsEngine.numIncompleteLoadAttempts > 3) {
                this.settingsEngine.newRender = false;
            }
            if (this.settingsEngine.numIncompleteLoadAttempts > 4 || this.settingsEngine.numLoadsSinceRunningGameOrNormalExit > 5) {
                GameEngine.log("Extra safe mode");
                this.isExtraSafeMode = true;
            }
            if (this.settingsEngine.numIncompleteLoadAttempts > 5) {
                GameEngine.log("Extra safe mode x2");
                this.isExtraSafeModeLevel2 = true;
            }
            if (this.settingsEngine.numIncompleteLoadAttempts > 6) {
                GameEngine.log("Extra safe mode x3");
                this.settingsEngine.newRender = false;
                this.settingsEngine.shaderEffects = false;
                this.settingsEngine.teamShaders = false;
            }
            if (this.settingsEngine.newRender && this.settingsEngine.numLoadsSinceRunningGameOrNormalExit > 15) {
                GameEngine.log("Disabling opengl mode");
                this.settingsEngine.newRender = false;
            }
            GameEngine.log("starting game in safe mode, numIncompleteLoadAttempts:" + this.settingsEngine.numIncompleteLoadAttempts + " numLoadsSinceRunningGameOrNormalExit:" + this.settingsEngine.numLoadsSinceRunningGameOrNormalExit);
        }
        if (isCommandLineMode) {
            this.isSafeMode = true;
            this.safeModeReason = "<forced by command line>";
        }
        if (isAutomatedTesting) {
            this.isSafeMode = true;
            this.isExtraSafeMode = true;
            this.isExtraSafeModeLevel2 = true;
            this.safeModeReason = "<forced by command line>";
        }
        this.settingsEngine.numLoadsSinceRunningGameOrNormalExit++;
        this.settingsEngine.numIncompleteLoadAttempts++;
        if (!this.settingsEngine.save() && isIOSVersion) {
            GameEngine.log("starting game in safe mode, failed to save settings");
            this.safeModeReason = "failing to write preferences data";
            this.isSafeMode = true;
        }
        DebugServer.a();
        this.screenScale = getScreenScale();
        GameEngine.log("densityScale(): " + this.screenScale);
        long jA2 = PerformanceProfiler.a();
        Locale.initialize();
        PerformanceProfiler.a("Locale.init took:", jA2);
        PlayerTeam.loadTeamColorSettings();
        this.l = new Paint();
        this.fpsPaint = new Paint();
        this.fpsPaint.a(255, 255, 255, 255);
        this.fpsPaint.a(true);
        updatePaintTextSize(this.fpsPaint, 16.0f);
        this.n = new Paint();
        this.n.a(255, 255, 255, 255);
        this.n.a(true);
        updatePaintTextSize(this.n, 16.0f);
        this.o = new Paint();
        this.o.a(100, 255, 0, 0);
        updatePaintTextSize(this.o, 16.0f);
        this.p = new Paint();
        this.p.a(100, 0, 255, 0);
        updatePaintTextSize(this.p, 16.0f);
        this.teamInfoPaint = new Paint();
        this.centeredPaint = new Paint();
        this.centeredPaint.a(Paint.Align.CENTER);
        this.centeredPaint.a(true);
        this.centeredPaint.a(Typeface.a(Typeface.c, 0));
        updatePaintTextSize(this.centeredPaint, 16.0f);
        this.loadingPaint = new Paint();
        this.loadingPaint.a(255, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_DATA_SERVICE, 255, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_DATA_SERVICE);
        this.loadingPaint.a(true);
        this.loadingPaint.a(Paint.Align.CENTER);
        updatePaintTextSize(this.loadingPaint, 18.0f);
        this.x = new Paint();
        this.x.b(-1);
        this.x.c(100);
        this.y = new Paint();
        this.y.b(-7829368);
        this.y.c(SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_SERVICE);
        this.y.a(Paint.Style.STROKE);
        this.y.a(1.0f);
        long jA3 = PerformanceProfiler.a();
        loadLevel("AudioEngine");
        SoundEngine.noop();
        this.soundEngine = new SoundEngine();
        this.soundEngine.loadSounds();
        PerformanceProfiler.a("AudioEngine took:", jA3);
        loadLevel("MusicController");
        this.musicManager = new MusicManager();
        this.musicManager.init();
        if (GameEngine.graphicsEngine != null) {
            log("init(): using Graphics instance");
            this.renderGraphicsEngine = GameEngine.graphicsEngine;
        } else {
            this.renderGraphicsEngine = org.koin.java.KoinJavaComponent.get(GraphicsEngine.class);
        }
        loadLevel("graphics.init");
        FileChangeEngine.a();
        loadLevel("Fonts");
        refreshPaintSizeTrackers();
        loadLevel("effects.init");
        this.effectManager = new EffectManager();
        this.effectManager.loadContent();
        loadLevel("minimapHandler");
        this.minimap = new Minimap();
        this.minimap.init();
        this.minimap.bindGraphicsBackend(this.renderGraphicsEngine);
        TileMap.layerBufferManager.bindGraphicsBackend(this.renderGraphicsEngine);
        if (screenSize != null) {
            GameEngine.log("We have an initial screen size, can do early setup of image buffers");
            loadLevel("Map Buffers");
            updateWindowResolution(screenSize.worldX, screenSize.worldY);
            updateCameraSystem();
            TileMap.initSoftFogFading();
            TileMap.updateLayerBuffers();
            this.minimap.createImageBuffers();
            if (GameEngine.isPostProcessingSupported()) {
                loadLevel("Setting up postprocessing");
                if (!setupPostProcessing()) {
                    GameEngine.log("Failed to setup postprocessing");
                }
            }
        }
        loadLevel("PathEngine");
        this.pathfindingEngine = new PathEngine();
        loadLevel("GroupController");
        this.formationEngine = new FormationEngine();
        loadLevel("CollisionEngine");
        this.collisionEngine = new CollisionEngine();
        loadLevel("InterfaceEngine");
        this.gameUI = new GameUI();
        this.gameUI.initializeUIResources();
        this.gameStateManager = GameStateManager.c();
        loadLevel("NetworkEngine");
        this.networkEngine = new NetworkEngine();
        this.networkEngine.updateAIDifficulty();
        loadLevel("StatsHandler");
        this.gameStatistics = new GameStatistics();
        loadLevel("ModEngine");
        this.modManager = new ModManager();
        io.github.rwx.p2p.transfer.TransferRecovery.beforeInitialScanOrReset(this);
        this.modManager.loadAndApply();
        if (this.isSafeMode) {
            this.modManager.disableAllMods();
        }
        loadLevel("CommandController");
        this.commandController = new CommandController();
        loadLevel("GameSaver");
        this.gameSaver = new GameSaver();
        loadLevel("ReplayEngine");
        this.replayEngine = new ReplayEngine();
        loadLevel("UnitGeoIndex");
        this.unitSpatialIndex = new UnitSpatialIndex();
        loadLevel("Precalculating map fog");
        TileMap.buildFogSmoothAtlas(this.renderGraphicsEngine);
        loadLevel("ScorchMark.load");
        ScorchMark.b();
        loadLevel("Projectile.load");
        Projectile.c();
        loadLevel("Emitter.load");
        EffectEmitter.initFireEffects();
        loadLevel("Unit.loadAllUnits");
        long jA4 = PerformanceProfiler.a();
        BaseUnit.loadAllUnits();
        PerformanceProfiler.a("loadAllUnits took:", jA4);
        loadLevel("Loading custom unit data");
        long jA5 = PerformanceProfiler.a();
        CustomUnitConfigParser.loadAllCustomUnitsAndMods();
        loadLevel("getAllUnitsChecksum");
        PerformanceProfiler.a("CustomUnits took:", jA5);
        long jA6 = PerformanceProfiler.a();
        this.allUnitsChecksum = BaseUnit.bM();
        PerformanceProfiler.a("allUnitsChecksum took:", jA6);
        this.z = new Paint();
        this.z.a(50, 255, 255, 255);
        onInitialContentLoaded();
        System.gc();
        this.isInitialized = true;
        GameEngine.log("Init completed");
        PerformanceProfiler.a("Loading took:", jA);
        this.performanceProfiler.b(ProfilerSection.init_total);
        this.performanceProfiler.a(true, true);
        long jA7 = PerformanceProfiler.a();
        loadLevel("Loading map data");
        if (!GameEngine.isMenuBackgroundDisabled) {
            loadMenuBackground();
        }
        PerformanceProfiler.a("loadAMenuMap took:", jA7);
        loadLevel("Last setup");
        setupANRWatchdog();
        this.networkEngine.m();
        loadLevel("init complete");
        if (isUnitImageGenerationMode) {
            UnitTypeEnum.loadUnitTypeImages();
            System.exit(0);
        }
        if (isUnitValidationMode) {
            UnitTypeEnum.loadAllUnitTypes();
            System.exit(0);
        }
        this.isGameMinimized = true;
    }

    /* JADX INFO: renamed from: a */
    public void updateDensity(int i, int i2) {
        float fDistance = Utility.distance(0.0f, 0.0f, i, i2) / 1131.0f;
        GameEngine.log("defaultViewpointZoomDensity: " + fDistance);
        if (fDistance < 0.5f) {
            fDistance = 0.5f;
        }
        if (fDistance > 3.0f) {
            fDistance = 3.0f;
        }
        GameEngine.log("defaultViewpointZoomDensity after limit: " + fDistance);
        this.densityZoomScale = 1.0f;
        if (Utility.abs(fDistance - 1.0f) > 0.1d) {
            this.densityZoomScale = fDistance;
            if (this.densityZoomScale > 2.0f) {
                this.densityZoomScale = 2.0f;
            }
            if (this.densityZoomScale < 0.5f) {
                this.densityZoomScale = 0.5f;
            }
            this.zoom = this.targetZoom * this.densityZoomScale;
        }
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: e */
    public void stopAndReset() {
        stopGameThreadIfNotInGameThread();
        resetFlags();
    }

    /* JADX INFO: renamed from: f */
    public void resetFlags() {
        resetGame(false);
        this.hasLoadedLevel = false;
        this.isMenuBackgroundMap = false;
        this.exitGameThread = false;
        this.isShowingDialog = false;
        this.gameUI.isDraggingSelection = false;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: a */
    public synchronized void loadGame(boolean z, GameMode gameMode) {
        stopGameThreadIfNotInGameThread();
        loadLevel(z, false, gameMode);
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: a */
    public void loadLevel(boolean z, boolean z2, GameMode gameMode) {
        beginLoadingStatus("Loading map data", 8);
        loadLevel("Loading map data");
        InGameMenuController surfaceHolder;
        this.maxUnitCap = this.settingsEngine.teamUnitCapSinglePlayer;
        if (this.maxUnitCap < 1) {
            this.maxUnitCap = 1;
        }
        this.currentUnitCap = this.maxUnitCap;
        resetGame(z2);
        PlayerTeam.syncAllTeamUnitCaps();
        this.isGameRecording = false;
        System.gc();
        this.fullReload = true;
        this.hasLoadedLevel = false;
        this.isShowingDialog = false;
        this.exitGameThread = false;
        this.gameTimeMillis = 0;
        this.isLookModeEnabled = false;
        this.networkEngine.a(1L);
        this.currentTick = 0;
        this.globalSeed = 0;
        Utility.resetSharedRandomSeed();
        this.networkEngine.t();
        loadLevel("Resetting game state");
        if (!z2) {
            this.hasWonGame = false;
            this.isContinuingAfterGameEnd = false;
            this.touchStartX = 0.0f;
            this.shouldAdvanceAfterGameEnd = false;
            this.hasLostGame = false;
        }
        this.cleanupCounter = 0;
        if (!z2) {
            this.targetZoom = 1.0f;
        }
        this.unitSelectionFadeBase = 0.0f;
        if (!this.replayEngine.j()) {
            loadLevel("Loading unit data");
            if (!this.networkEngine.networkGameActive) {
                CustomUnitConfigParser.enableAllCustomUnits(true);
            } else {
                CustomUnitConfigParser.applyPendingNetworkUnits();
            }
        }
        if (!this.networkEngine.networkGameActive) {
            if (!this.replayEngine.j() && z) {
                this.playerTeam = new GameTeam(0);
                this.playerTeam.teamName = "Player";
                for (int i = 1; i < 8; i++) {
                    new AIController(i);
                }
                this.networkEngine.updateAiTeamNames();
            }
        } else {
            this.playerTeam = this.networkEngine.localPlayerTeam;
            if (this.playerTeam == null) {
                throw new RuntimeException("cannot find player's team");
            }
            if (this.playerTeam != PlayerTeam.k(this.playerTeam.teamId)) {
                GameEngine.logWarningAndStack("Stale playerTeam");
            }
        }
        this.missionEngine = null;
        this.tileMap = new TileMap();
        try {
            loadLevel("Loading map data");
            if (this.remoteMapStream != null) {
                InputStream activeInputStream = this.remoteMapStream.getActiveInputStream();
                try {
                    activeInputStream.reset();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.tileMap.loadMapFromStream(activeInputStream, z2);
            } else {
                this.tileMap.loadMap(getCurrentMapPath(), z2);
            }
            if (!this.tileMap.isCursorActive) {
                log("map did not load, returning");
                this.fullReload = false;
                return;
            }
            this.tileMap.fogRenderActive = false;
            PlayerTeam.refreshTeamInstances();
            loadLevel("Preparing teams");
            for (int i2 = 0; i2 < PlayerTeam.TEAM_NEUTRAL; i2++) {
                PlayerTeam playerTeamK = PlayerTeam.k(i2);
                if (playerTeamK != null) {
                    playerTeamK.refreshTeamColorPaints();
                }
            }
            if (!z2) {
                CustomUnitConfig.spawnOnNewMapAccordingToTeamColors();
            }
            if (!this.networkEngine.networkGameActive && !this.replayEngine.j()) {
                this.networkEngine.roomSettings.incomeMultiplier = 1.0f;
                this.networkEngine.roomSettings.randomSeed = Utility.getRandomIntInRange(1, 1000000000);
            }
            this.globalSeed = this.networkEngine.roomSettings.randomSeed;
            log("global Seed: " + this.globalSeed);
            if (this.networkEngine.networkGameActive || this.replayEngine.j()) {
                if (!this.networkEngine.singleplayerServer) {
                    this.currentUnitCap = this.networkEngine.currentUnitCap;
                    this.maxUnitCap = this.networkEngine.maxUnitCap;
                }
                GameEngine.log("Unit cap is now: " + this.maxUnitCap);
                if (this.networkEngine.roomSettings.fogMode == 0) {
                    this.tileMap.fogEnabled = false;
                    this.tileMap.fogPeriodicMaintenanceEnabled = false;
                } else if (this.networkEngine.roomSettings.fogMode == 1) {
                    this.tileMap.fogEnabled = true;
                    this.tileMap.fogPeriodicMaintenanceEnabled = false;
                } else if (this.networkEngine.roomSettings.fogMode == 2) {
                    this.tileMap.fogEnabled = true;
                    this.tileMap.fogPeriodicMaintenanceEnabled = true;
                }
                this.tileMap.fogRenderActive = this.networkEngine.roomSettings.revealedMap;
                byte b = 10;
                if (this.networkEngine.roomSettings.revealedMap) {
                    b = 10;
                }
                for (int i3 = 0; i3 < PlayerTeam.TEAM_NEUTRAL; i3++) {
                    PlayerTeam playerTeamK2 = PlayerTeam.k(i3);
                    if (playerTeamK2 != null) {
                        if (playerTeamK2.fogOfWarData == null) {
                            GameEngine.log("Fog null for team: " + playerTeamK2.teamId);
                        } else {
                            for (int i4 = 0; i4 < this.tileMap.tileCountX; i4++) {
                                for (int i5 = 0; i5 < this.tileMap.tileCountY; i5++) {
                                    playerTeamK2.fogOfWarData[i4][i5] = b;
                                }
                            }
                        }
                    }
                }
                int iK = this.networkEngine.k();
                for (int i6 = 0; i6 < PlayerTeam.TEAM_NEUTRAL; i6++) {
                    PlayerTeam playerTeamK3 = PlayerTeam.k(i6);
                    if (playerTeamK3 != null) {
                        playerTeamK3.credits = iK;
                        if (playerTeamK3.isTeamSpectator) {
                            if (!playerTeamK3.isTeamLocked) {
                                if (playerTeamK3.teamAIDifficultyOverride != null) {
                                    playerTeamK3.teamPingTime = playerTeamK3.teamAIDifficultyOverride.intValue();
                                } else {
                                    playerTeamK3.teamPingTime = this.networkEngine.roomSettings.aiDifficulty;
                                }
                            } else {
                                playerTeamK3.c("aiDifficulty is locked");
                            }
                        }
                        playerTeamK3.isTeamConnectionActive = this.networkEngine.roomSettings.sharedControl;
                        boolean z3 = false;
                        boolean z4 = false;
                        int iIntValue = this.networkEngine.roomSettings.startingUnits;
                        if (playerTeamK3.startingUnitsOverride != null) {
                            iIntValue = playerTeamK3.startingUnitsOverride.intValue();
                        }
                        if (iIntValue != 1) {
                            boolean z5 = true;
                            boolean z6 = true;
                            Float fValueOf = null;
                            Float fValueOf2 = null;
                            Float fValueOf3 = null;
                            Float fValueOf4 = null;
                            if (iIntValue == 5 || iIntValue == 4 || iIntValue > 10) {
                                z6 = false;
                            }
                            if (iIntValue == 5 || iIntValue == 4 || iIntValue == 3 || iIntValue > 10) {
                                z5 = false;
                            }
                            if (iIntValue == 9) {
                                z6 = false;
                                z5 = false;
                            }
                            for (BaseUnit baseUnit : BaseUnit.getGlobalUnitList()) {
                                if ((baseUnit instanceof BaseUnit) && !baseUnit.isDead && baseUnit.team == playerTeamK3) {
                                    if (baseUnit.isBuilder && !z3) {
                                        z3 = true;
                                        fValueOf = Float.valueOf(baseUnit.posX);
                                        fValueOf2 = Float.valueOf(baseUnit.posY);
                                        if (!z5) {
                                            baseUnit.removeFromGame();
                                        }
                                    }
                                    if (baseUnit.isTargetable && !z4) {
                                        z4 = true;
                                        fValueOf3 = Float.valueOf(baseUnit.posX);
                                        fValueOf4 = Float.valueOf(baseUnit.posY);
                                        if (!z6) {
                                            baseUnit.removeFromGame();
                                        }
                                    }
                                }
                            }
                            if (fValueOf == null) {
                                fValueOf = fValueOf3;
                                fValueOf2 = fValueOf4;
                            }
                            if (fValueOf == null) {
                                GameEngine.log("placementLocation==null for team:" + playerTeamK3.teamId);
                            } else {
                                float fFloatValue = fValueOf.floatValue();
                                float fFloatValue2 = fValueOf2.floatValue();
                                if (iIntValue == 2) {
                                    for (int i7 = 0; i7 <= 2; i7++) {
                                        if (i7 != 1) {
                                            BaseUnit baseUnitA = UnitTypeEnum.builder.a();
                                            baseUnitA.setUnitTeam(playerTeamK3);
                                            baseUnitA.posX = (fFloatValue - 50.0f) + (i7 * 50);
                                            baseUnitA.posY = fFloatValue2;
                                            PlayerTeam.c(baseUnitA);
                                        }
                                    }
                                    for (int i8 = 0; i8 <= 2; i8++) {
                                        BaseUnit baseUnitA2 = UnitTypeEnum.heavyTank.a();
                                        baseUnitA2.setUnitTeam(playerTeamK3);
                                        baseUnitA2.posX = (fFloatValue - 50.0f) + (i8 * 50);
                                        baseUnitA2.posY = fFloatValue2 + 50.0f;
                                        PlayerTeam.c(baseUnitA2);
                                    }
                                } else if (iIntValue == 3 || iIntValue == 4) {
                                    for (int i9 = 0; i9 <= 2; i9++) {
                                        UnitType unitTypeByName = UnitTypeEnum.getUnitTypeByName("combatEngineer");
                                        if (unitTypeByName == null) {
                                            NetworkEngine.reportDesync("Could not find: combatEngineer on network.setup.startingUnits==3");
                                        } else {
                                            BaseUnit baseUnitA3 = unitTypeByName.a();
                                            baseUnitA3.setUnitTeam(playerTeamK3);
                                            baseUnitA3.posX = (fFloatValue - 50.0f) + (i9 * 50);
                                            baseUnitA3.posY = fFloatValue2 + 50.0f;
                                            PlayerTeam.c(baseUnitA3);
                                        }
                                    }
                                } else if (iIntValue == 5) {
                                    UnitType unitTypeByName2 = UnitTypeEnum.getUnitTypeByName("experimentalSpider");
                                    if (unitTypeByName2 == null) {
                                        NetworkEngine.reportDesync("Could not find: experimentalSpider on network.setup.startingUnits==5");
                                    } else {
                                        BaseUnit baseUnitA4 = unitTypeByName2.a();
                                        baseUnitA4.setUnitTeam(playerTeamK3);
                                        baseUnitA4.posX = fFloatValue;
                                        baseUnitA4.posY = fFloatValue2;
                                        baseUnitA4.rotationSpeed = 90.0f;
                                        baseUnitA4.posZ = 2.0f;
                                        baseUnitA4.startFalling();
                                        PlayerTeam.c(baseUnitA4);
                                    }
                                } else if (iIntValue != 9 && iIntValue > 10) {
                                    CustomUnitConfig customUnitConfigC = CustomUnitConfig.c(iIntValue);
                                    if (customUnitConfigC == null) {
                                        NetworkEngine.reportDesync("Could not find starting unit on startingUnits==" + iIntValue);
                                    } else {
                                        BaseUnit baseUnitA5 = customUnitConfigC.a();
                                        baseUnitA5.setUnitTeam(playerTeamK3);
                                        baseUnitA5.posX = fFloatValue;
                                        baseUnitA5.posY = fFloatValue2;
                                        if (!baseUnitA5.bI()) {
                                            baseUnitA5.rotationSpeed = 90.0f;
                                        }
                                        if (customUnitConfigC.startFallingWhenStartingUnit) {
                                            baseUnitA5.startFalling();
                                            if (baseUnitA5 instanceof CustomUnit) {
                                                ((CustomUnit) baseUnitA5).dB();
                                            }
                                        }
                                        PlayerTeam.c(baseUnitA5);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!z2 && (this.missionEngine == null || !this.missionEngine.hasCameraStartMarker)) {
                setViewpoint(0.0f, 0.0f);
                int i10 = 0;
                int i11 = 0;
                boolean z7 = false;
                for (BaseUnit baseUnit2 : BaseUnit.bE) {
                    if (baseUnit2 instanceof Tree) {
                        i11++;
                    } else {
                        i10++;
                    }
                    if (baseUnit2.team == this.playerTeam && baseUnit2.isTargetable) {
                        centerViewpoint(baseUnit2.posX, baseUnit2.posY);
                        z7 = true;
                    }
                }
                if (!z7) {
                    for (BaseUnit baseUnit3 : BaseUnit.bE) {
                        if (baseUnit3.team == this.playerTeam && !baseUnit3.t() && !baseUnit3.u()) {
                            centerViewpoint(baseUnit3.posX, baseUnit3.posY);
                        }
                    }
                }
                log("there are " + i10 + " units on this map and " + i11 + " trees");
            }
            this.gameStateData = GameStateManager.c().b(getCurrentMapPath());
            loadLevel("Preparing map systems");
            this.pathfindingEngine.a(this.tileMap, z2);
            this.minimap.reset(this.tileMap, z2);
            this.commandController.clearAllCommands();
            this.formationEngine.a();
            if (!z2) {
                BuildPreview.clearAll();
            }
            loadLevel("Loading save data");
            this.gameSaver.resetAutosaveTimers(z2);
            this.gameUI.toggleGameAndUIState(z2);
            if (!z2) {
                this.gameUI.clearSelection();
                selectAnyOnScreenBuilder();
                if (this.isGameStarted) {
                    this.gameUI.clearSelection();
                }
            } else {
                this.gameUI.clearSelection();
            }
            this.unitSpatialIndex.a(this.tileMap);
            if (!z2) {
                this.musicManager.onNewGame();
            }
            this.gameStatistics.a();
            for (BaseUnit baseUnit4 : BaseUnit.bE) {
                if (baseUnit4 instanceof OrderableUnit) {
                    ((OrderableUnit) baseUnit4).c(false);
                }
            }
            this.gameStateData.isGameStarted = true;
            this.gameStateManager.a();
            loadLevel("Starting game");
            this.hasLoadedLevel = true;
            this.isMenuBackgroundMap = false;
            this.fullReload = false;
            if (gameMode != GameMode.menu && !this.settingsEngine.hasPlayedGameOrSeenHelp) {
                this.settingsEngine.hasPlayedGameOrSeenHelp = true;
                this.settingsEngine.save();
            }
            for (int i12 = 0; i12 < 5; i12++) {
                System.gc();
            }
            if (!GameEngine.isNonAndroidVersion) {
                Log.a("RustedWarfare", "getNativeHeapSize" + String.valueOf(Debug.getNativeHeapSize()));
                Log.a("RustedWarfare", "getNativeHeapAllocatedSize" + String.valueOf(Debug.getNativeHeapAllocatedSize()));
                Log.a("RustedWarfare", "getNativeHeapFreeSize" + String.valueOf(Debug.getNativeHeapFreeSize()));
                Log.a("RustedWarfare", "Runtime.getRuntime().maxMemory()" + String.valueOf(Runtime.getRuntime().maxMemory()));
            }
            if (this.gameThread != null) {
                this.gameThread.a();
            }
            this.accumulator = 0.0f;
            if (this.networkEngine.singleplayerServer && this.networkEngine.networkGameActive) {
                GameEngine.log("Disabling network for singleplayer");
                this.networkEngine.networkGameActive = false;
            }
            markLoadingStatusComplete("Game ready");
            if (!isDedicatedServer()) {
                if (gameMode == GameMode.normalSave) {
                    GameEngine.log("Not starting replay recording as we are loading a save");
                } else {
                    this.replayEngine.a(z2);
                }
            }
            if (PathEngine.m) {
            }
        } catch (MapLoadException e2) {
            e2.printStackTrace();
            alert("Error loading map: " + e2.getMessage(), 1);
            if (isAutomatedTestMode) {
                GameEngine.log("Crashing on allowed map error because automated testing is active");
                throw new RuntimeException(e2);
            }
            if (!this.networkEngine.networkGameActive && this.activeGameView != null && (surfaceHolder = this.activeGameView.getInGameMenuController()) != null) {
                surfaceHolder.m();
            }
            writeCrashToFile("Map Load Warning", getStackTrace(e2));
            this.fullReload = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* JADX INFO: renamed from: aG */
    private void selectAnyOnScreenBuilder() {
        this.gameUI.clearSelection();
        for (BaseUnit baseUnit : BaseUnit.bE) {
            if (baseUnit.team == this.playerTeam && (baseUnit instanceof OrderableUnit) && baseUnit.canMove() && baseUnit.isVisibleOnScreen() && baseUnit.isAlive() && !baseUnit.u() && !baseUnit.t()) {
                GameEngine.log("selectAnyOnScreenBuilder: found builder");
                this.gameUI.selectUnit(baseUnit);
                return;
            }
        }
        GameEngine.log("selectAnyOnScreenBuilder: no builder found");
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: g */
    public void clearAllObjects() {
        TransactionalArrayList<GameObject> transactionalArrayListDK = GameObject.dK();
        Iterator it = transactionalArrayListDK.iterator();
        while (it.hasNext()) {
            ((GameObject) it.next()).remove();
        }
        BaseUnit.getGlobalUnitList();
        GameObject.dK();
        int size = transactionalArrayListDK.size();
        if (size != 0) {
            GameEngine.logErrorColored("SHOULD_NOT_HAPPEN: we still had " + size + " objects in gameObjectListForLogic after removeAll");
            for (GameObject gameObject : transactionalArrayListDK) {
                String unitShortName = "Object: " + gameObject.objectId;
                if (gameObject instanceof BaseUnit) {
                    unitShortName = ((BaseUnit) gameObject).getUnitShortName();
                }
                GameEngine.logErrorColored("Remaining object: " + unitShortName);
            }
            if (GameEngine.getInstance().isMissionActive()) {
                throw new RuntimeException("We still had " + size + " objects in gameObjectListForLogic after removeAll");
            }
        }
        BaseUnit.getGlobalUnitList().clear();
        GameObject.dK().clear();
        CustomUnit.dD();
        this.renderList.clear();
    }

    /* JADX INFO: renamed from: b */
    public void resetGame(boolean z) {
        synchronized (this.gameStateLock) {
            if (this.activeGameView != null) {
                this.activeGameView.onSizeChanged();
            }
            this.isLoading = false;
            if (!z) {
                this.replayEngine.g();
            }
            this.pathfindingEngine.c();
            clearAllObjects();
            if (!isPC()) {
                this.musicManager.pause();
            }
            this.effectManager.setBitmapQuality(z);
            if (this.tileMap != null) {
                this.tileMap.clearAllMapData();
                this.tileMap = null;
            }
            if (this.missionEngine != null) {
                this.missionEngine = null;
            }
            if (this.unitSpatialIndex != null) {
                this.unitSpatialIndex.b();
            }
            this.cameraFocusUnit = null;
            this.nextCameraFocusUnit = null;
            this.cleanupCounter = 0;
            PlayerTeam.resetSpecialTeamStates();
            setupTeamStats(StatType.none, StatGroup.player);
        }
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: a */
    public void gameLoop(float f, int i) throws ConfigParseException, IOException {
        synchronized (this.gameStateLock) {
            runGameLoopFrame(f, i);
        }
    }

    /* JADX WARN: Finally extract failed */
    /* JADX WARN: Removed duplicated region for block: B:314:0x0948 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX INFO: renamed from: b */
    public void runGameLoopFrame(float float1, int integer) throws IOException, ConfigParseException {
        if (this.currentTick == 2) {
            this.probeLowMemory();
        } else if (this.currentTick % 10000 == 0 && this.currentTick != 0) {
            this.probeLowMemory();
        }

        if (isGameThreadActive && !this.hasLoggedHighNativeHeapUsage && isAndroidPlatform() && Debug.getNativeHeapAllocatedSize() > 209715200L) {
            GameEngine.log("getNativeHeapAllocatedSize: " + Utility.formatByteSize((int) Debug.getNativeHeapAllocatedSize()));
            this.hasLoggedHighNativeHeapUsage = true;
        }

        this.showMemoryWarningsIfNeeded();
        this.recurringGameThreadTasks.a();
        this.pendingGameThreadTasks.b();
        this.performanceProfiler.a(ProfilerSection.total);
        this.networkEngine.b(float1);
        this.activeGameView = this.pendingGameView;
        if (this.activeGameView.isActive()) {
            this.performanceProfiler.a(ProfilerSection.update);

            while (this.gameThreadRunnableQueue.peek() != null) {
                Runnable var3 = (Runnable) this.gameThreadRunnableQueue.poll();
                var3.run();
            }

            if (!this.hasLoadedLevel) {
                if (!this.isStopped) {
                    Log.d("RustedWarfare", "game running without a loaded level!!!");
                    this.stopAndClose();

                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException var21) {
                        var21.printStackTrace();
                    }
                }
            } else {
                this.isLoading = true;
                if (!this.hasCheckedSafeMode && this.currentTick > 5) {
                    this.hasCheckedSafeMode = true;
                    boolean var25 = false;
                    if (this.settingsEngine.numIncompleteLoadAttempts > 1) {
                        var25 = true;
                    }

                    this.settingsEngine.numIncompleteLoadAttempts = 0;
                    if (this.isSafeMode) {
                        this.settingsEngine.numLoadsSinceRunningGameOrNormalExit = 0;
                    }

                    this.settingsEngine.save();
                    if (this.isSafeMode && (this.isEnglishForcedBySafeMode || this.modManager.getStorageModsCount() > 0)) {
                        if (this.safeModeReason != null) {
                            this.showMessageBox("Safe mode", "Started game in safe mode due to " + this.safeModeReason + ". Mods have been disabled.");
                        } else if (var25) {
                            this.showMessageBox("Safe mode", "Started game in safe mode due to failed loading attempts. Mods have been disabled.");
                        } else {
                            this.showMessageBox("Safe mode", "Started game in safe mode due to multiple loads without starting a game or exiting. Mods have been disabled.");
                        }
                    }
                }

                if (!this.isMenuBackgroundMap && this.hasLoadedLevel && this.settingsEngine.numLoadsSinceRunningGameOrNormalExit != 0) {
                    this.settingsEngine.numLoadsSinceRunningGameOrNormalExit = 0;
                    this.settingsEngine.save();
                }

                this.gameSaver.updateAutosaveTimer();
                float var26 = this.targetZoom * this.densityZoomScale;
                if (var26 != this.zoom) {
                    float var4 = this.mouseX / this.zoom + this.viewpointX;
                    float var5 = this.mouseY / this.zoom + this.viewpointY;
                    this.zoom = var26;
                    this.updateCameraSystem();
                    if (this.shouldRecenterZoomOnPointer) {
                        float var6 = this.mouseX / this.zoom + this.viewpointX;
                        float var7 = this.mouseY / this.zoom + this.viewpointY;
                        this.setViewpoint(this.viewpointX - (var6 - var4), this.viewpointY - (var7 - var5));
                        this.shouldRecenterZoomOnPointer = false;
                    }
                }

                if (this.scrollDeltaX != 0.0F || this.scrollDeltaY != 0.0F) {
                    float var27 = 3.0F * float1;
                    float var34 = 0.0F;
                    if (this.scrollDeltaX > 0.0F) {
                        var34 = Utility.min(this.scrollDeltaX, var27);
                    }

                    if (this.scrollDeltaX < 0.0F) {
                        var34 = Utility.max(this.scrollDeltaX, -var27);
                    }

                    var34 += 0.15F * this.scrollDeltaX;
                    float var37 = 0.0F;
                    if (this.scrollDeltaY > 0.0F) {
                        var37 = Utility.min(this.scrollDeltaY, var27);
                    }

                    if (this.scrollDeltaY < 0.0F) {
                        var37 = Utility.max(this.scrollDeltaY, -var27);
                    }

                    var37 += 0.15F * this.scrollDeltaY;
                    if (Utility.abs(this.scrollDeltaX) <= var27) {
                        var34 = this.scrollDeltaX;
                        this.scrollDeltaX = 0.0F;
                    } else {
                        this.scrollDeltaX -= var34;
                    }

                    if (Utility.abs(this.scrollDeltaY) <= var27) {
                        var37 = this.scrollDeltaY;
                        this.scrollDeltaY = 0.0F;
                    } else {
                        this.scrollDeltaY -= var37;
                    }

                    this.viewpointX += var34;
                    this.viewpointY += var37;
                    this.setViewpoint(this.viewpointX, this.viewpointY);
                    this.clampCameraPosition();
                }

                if (this.wasPaused != this.isPaused) {
                    this.updateCameraSystem();
                }

                if (float1 > 3.0F) {
                    float1 = 3.0F;
                }

                if (float1 < 0.0F) {
                    float1 = 0.0F;
                }

                if (this.gameSpeedMultiplier >= 0.0F) {
                    float1 = this.gameSpeedMultiplier;
                }

                this.renderTimeMillis = (int) (this.renderTimeMillis + float1 * 16.666666F);
                this.updateCameraFocus(float1);
                this.fpsAccumulator += integer;
                this.fpsFrameCounter++;
                if (this.fpsFrameCounter >= 40) {
                    if (this.fpsAccumulator == 0) {
                        this.fpsAccumulator = 1;
                    }

                    this.fps = (int) (this.fpsFrameCounter * 1000 / this.fpsAccumulator + 0.5F);
                    this.averageFrameTime = (float) this.fpsAccumulator / this.fpsFrameCounter;
                    this.fpsAccumulator = 0;
                    this.fpsFrameCounter = 0;
                    if (this.settingsEngine.showFps) {
                        this.fpsString = this.fps + "fps";
                    }
                }

                this.processPendingInputEvents();

                for (int var28 = 0; var28 < this.touchPointerEnabled.length; var28++) {
                    this.touchPointerEnabled[var28] = true;
                }

                this.rightMouseHoldTimer = Utility.moveTowardsZero(this.rightMouseHoldTimer, 0.1F * float1);
                this.middleMouseHoldTimer = Utility.moveTowardsZero(this.middleMouseHoldTimer, 0.1F * float1);
                this.rightMouseHoldTimer = Utility.clamp(this.rightMouseHoldTimer, 5.0F);
                this.middleMouseHoldTimer = Utility.clamp(this.middleMouseHoldTimer, 5.0F);
                this.gameUI.updateInput(float1);
                this.clampCameraPosition();
                TileMap.updateLayerBuffers();
                if (this.networkEngine.networkGameActive) {
                    float var29 = float1;
                    if (this.replayEngine.v != 1) {
                        var29 = float1 * this.replayEngine.v;
                    }

                    this.networkEngine.a(var29);
                    if (!this.shouldSkipUpdate(true) && !this.networkEngine.frameUpdateBlocked) {
                        this.accumulator += var29;

                        while (this.accumulator > this.networkEngine.getCurrentStepRate()) {
                            if (this.networkEngine.shouldGameBePausedForPathfinding()) {
                                this.networkEngine.frameUpdateBlocked = true;
                                break;
                            }

                            this.accumulator = this.accumulator - this.networkEngine.getCurrentStepRate();
                            this.networkEngine.a(this.networkEngine.getCurrentStepRate(), false);
                            if (this.networkEngine.frameUpdateBlocked) {
                                break;
                            }

                            this.update(this.networkEngine.getCurrentStepRate());
                        }

                        if (!this.networkEngine.isServer) {
                            if (this.networkEngine.catchupFastForwardActive || this.networkEngine.catchupSpeedupActive) {
                                if (this.networkEngine.catchupFastForwardActive && this.networkEngine.catchupSpeedupActive && this.currentTick < this.networkEngine.nextBlockingFrame - this.networkEngine.commandFrameInterval - 5) {
                                    this.networkEngine.d("nearly within frame range");
                                    this.networkEngine.catchupFastForwardActive = false;
                                }

                                if (this.currentTick > this.networkEngine.nextBlockingFrame - 6) {
                                    this.networkEngine.d("we have back within frame range");
                                    this.networkEngine.catchupFastForwardActive = false;
                                    this.networkEngine.catchupSpeedupActive = false;
                                }
                            }

                            if (!this.networkEngine.catchupSpeedupActive && this.currentTick < this.networkEngine.nextBlockingFrame - this.networkEngine.commandFrameInterval - 10) {
                                this.networkEngine.d("we are slightly out of frame range, speeding up");
                                this.networkEngine.catchupSpeedupActive = true;
                            }

                            if (!this.networkEngine.catchupFastForwardActive && this.currentTick < this.networkEngine.nextBlockingFrame - this.networkEngine.commandFrameInterval - 30) {
                                this.networkEngine.d("we are out of frame range, fast forwarding (" + this.currentTick + "->" + this.networkEngine.nextBlockingFrame + ")");
                                this.networkEngine.catchupFastForwardActive = true;
                            }

                            if (!this.networkEngine.catchupFastForwardActive && this.networkEngine.catchupSpeedupActive) {
                                this.networkEngine.catchupSpeedupTimer += float1;
                                if (this.networkEngine.catchupSpeedupTimer > this.networkEngine.getCurrentStepRate() * 3.0F) {
                                    this.networkEngine.catchupSpeedupTimer = 0.0F;
                                    this.networkEngine.a(this.networkEngine.getCurrentStepRate(), true);
                                    if (!this.networkEngine.frameUpdateBlocked) {
                                        this.update(this.networkEngine.getCurrentStepRate());
                                    }
                                }
                            }

                            if (this.networkEngine.catchupFastForwardActive) {
                                this.networkEngine.a(this.networkEngine.getCurrentStepRate(), true);
                                if (!this.networkEngine.frameUpdateBlocked) {
                                    this.update(this.networkEngine.getCurrentStepRate());
                                }
                            }

                            if (this.currentTick < this.networkEngine.nextBlockingFrame - 90) {
                                this.networkEngine.a(this.networkEngine.getCurrentStepRate(), true);
                                if (!this.networkEngine.frameUpdateBlocked) {
                                    this.update(this.networkEngine.getCurrentStepRate());
                                }
                            }

                            if (this.currentTick < this.networkEngine.nextBlockingFrame - 120) {
                                this.networkEngine.a(this.networkEngine.getCurrentStepRate(), true);
                                if (!this.networkEngine.frameUpdateBlocked) {
                                    this.update(this.networkEngine.getCurrentStepRate());
                                }
                            }

                            if (this.currentTick < this.networkEngine.nextBlockingFrame - 600) {
                                this.networkEngine.a(this.networkEngine.getCurrentStepRate(), true);
                                if (!this.networkEngine.frameUpdateBlocked) {
                                    this.update(this.networkEngine.getCurrentStepRate());
                                }
                            }
                        }
                    }
                } else if (this.replayEngine.i()) {
                    float var30 = float1;
                    if (this.replayEngine.v != 1) {
                        var30 = float1 * this.replayEngine.v;
                    }

                    if (this.gameSpeed != 1.0F) {
                        var30 *= this.gameSpeed;
                    }

                    if (!this.shouldSkipUpdate(false)) {
                        this.accumulator += var30;

                        while (this.accumulator > this.networkEngine.getCurrentStepRate()) {
                            this.accumulator = this.accumulator - this.networkEngine.getCurrentStepRate();
                            if (this.networkEngine.shouldGameBePausedForPathfinding()) {
                                break;
                            }

                            this.update(this.networkEngine.getCurrentStepRate());
                        }
                    }

                    if (this.accumulator > 100.0F) {
                        this.accumulator = 100.0F;
                    }

                    if (this.accumulator < 0.0F) {
                        this.accumulator = 0.0F;
                    }
                } else if (!this.shouldSkipUpdate(false)) {
                    this.update(float1);
                }

                if (this.shouldSkipUpdate(false)) {
                    try {
                        Thread.sleep(2L);
                    } catch (Exception var22) {
                    }
                }

                this.pathfindingEngine.a(float1);
                this.soundEngine.clearPlayingSounds(float1);
                this.musicManager.update(float1);
                this.inputController.b();
                DisabledSteamEngine.a().a(float1);
                this.performanceProfiler.b(ProfilerSection.update);
                this.performanceProfiler.a(ProfilerSection.draw);
                if (!this.shouldSkipNextDraw) {
                    this.drawThreadSafe(float1);
                }

                this.shouldSkipNextDraw = false;
                this.clearCurrentLoadingStatus();
                this.performanceProfiler.b(ProfilerSection.draw);
                if (this.shouldAdvanceAfterGameEnd) {
                    this.shouldAdvanceAfterGameEnd = false;
                    Integer var33 = getMapLevel(this.currentMapPath);
                    String var36 = null;
                    if (var33 != null) {
                        var36 = findNextLevel(this.currentMapPath);
                    }

                    if (this.networkEngine.networkGameActive) {
                        var36 = null;
                        // java.lang.Thread, java.lang.Runnable
                        new Thread(() -> GameLogic.this.networkEngine.disconnectNetworking("gotoNextLevel")).start();
                    }

                    if (var36 != null) {
                        GameEngine.log("gotoNextLevel: Loading next level: " + var36);
                        this.currentMapPath = var36;
                        this.gameUI.messageManager.clear();
                        this.loadLevel(true, false, GameMode.normal);
                    } else {
                        GameEngine.log("gotoNextLevel: No next level, finishing");
                        this.hasLoadedLevel = false;
                        InGameMenuController var40 = this.activeGameView.getInGameMenuController();
                        if (var40 != null) {
                            var40.b();
                            var40.m();
                        } else {
                            GameEngine.log("gotoNextLevel: Error getInGameMenuController==null");
                        }
                    }
                }

                if (!this.isStopped && this.isBenchmarking && !this.hasStartedBenchmarkTrace) {
                    log("starting method trace");
                    Debug.startMethodTracing("lukeTrace", 110000000);
                    this.hasStartedBenchmarkTrace = true;
                }

                this.exitGameThread = true;
                this.endOfFrameTasks.a();
                this.performanceProfiler.b(ProfilerSection.total);
                this.performanceProfiler.b();
            }
        }
    }

    /* JADX INFO: renamed from: com.corrodinggames.rts.game.i$a */
    /* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/i$a.class */
    class a extends Thread {
        a() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            GameLogic.this.networkEngine.disconnectNetworking("gotoNextLevel");
        }
    }

    /* JADX INFO: renamed from: h */
    public void stopAndClose() {
        InGameMenuController surfaceHolder = this.activeGameView.getInGameMenuController();
        if (surfaceHolder != null) {
            if (!surfaceHolder.c()) {
                surfaceHolder.b();
                return;
            } else {
                GameEngine.logColored("stopAndClose: inGameActivity is isFinishing");
                return;
            }
        }
        GameEngine.logColored("stopAndClose: Error getInGameMenuController==null");
    }

    /* JADX INFO: renamed from: a */
    public void update(float deltaSpeed) throws IOException {
        if (isInNetworkOrReplay() && deltaSpeed < 0.1f) {
            NetworkEngine.reportDesync("updateAllGame1: deltaSpeed:" + deltaSpeed + " frame:" + this.currentTick + " network.currentStepRate:" + this.networkEngine.getCurrentStepRate());
        }
        if (this.gameSpeed != 1.0f && !this.networkEngine.networkGameActive && !this.replayEngine.i()) {
            deltaSpeed *= this.gameSpeed;
        }
        float f = deltaSpeed * this.speedMultiplier;
        this.I = f + 2.0f;
        this.lastDelta = f;
        this.networkEngine.update(f);
        this.gameTimeMillis = (int) (this.gameTimeMillis + (f * 16.666666f));
        CommandQueue.drain();
        this.commandController.executeAllCommands();
        this.replayEngine.update(f);
        this.currentTick++;
        ModScheduler.tick();
        PlayerTeam.g(f);
        if (this.tileMap != null) {
            this.tileMap.updateFogLogicFrame(f);
        }
        if (isInNetworkOrReplay() && f < 0.1f) {
            NetworkEngine.reportDesync("updateAllGame2: deltaSpeed:" + f + " frame:" + this.currentTick);
        }
        BaseUnit.getGlobalUnitList();
        TransactionalArrayList transactionalArrayListDK = GameObject.dK();
        Object[] objArrB = transactionalArrayListDK.b();
        int size = transactionalArrayListDK.size();
        boolean zAy = isInNetworkOrReplay();
        for (int i = 0; i < size; i++) {
            GameObject gameObject = (GameObject) objArrB[i];
            if (zAy && f != this.lastDelta) {
                NetworkEngine.reportDesyncImportant("JIT bug detected, attempting to correct. before object:" + gameObject.objectId + " frame:" + this.currentTick + " deltaSpeed:" + f);
                f = this.lastDelta;
            }
            gameObject.update(f);
        }
        if (isInNetworkOrReplay() && f < 0.1f) {
            NetworkEngine.reportDesync("updateAllGame3: deltaSpeed:" + f + " frame:" + this.currentTick);
        }
        int size2 = transactionalArrayListDK.pendingOperations.size();
        for (int i2 = 0; i2 < size2; i2++) {
            ListOperationEntry listOperationEntry = (ListOperationEntry) transactionalArrayListDK.pendingOperations.get(i2);
            if (listOperationEntry.operation == ListOperation.add) {
                GameObject gameObject2 = (GameObject) listOperationEntry.value;
                if (!gameObject2.isDestroyed) {
                    gameObject2.update(f);
                }
            }
        }
        this.performanceProfiler.a(ProfilerSection.update_geo_indexes);
        this.unitSpatialIndex.a();
        this.performanceProfiler.b(ProfilerSection.update_geo_indexes);
        OrderableUnit.updateAllUnitCollisions(f);
        CustomUnit.s(f);
        CustomUnit.a(f, 0);
        UnitEventRuntime.drain();
        this.cleanupCounter++;
        if (this.cleanupCounter >= 1000) {
            this.cleanupCounter = 0;
            int i3 = 0;
            for (BaseUnit baseUnit : BaseUnit.getGlobalUnitList()) {
                if (baseUnit.isDead && !(baseUnit instanceof Tree)) {
                    i3++;
                }
            }
            if (i3 > 70) {
                for (BaseUnit baseUnit2 : BaseUnit.getGlobalUnitList()) {
                    if ((baseUnit2 instanceof BaseUnit) && baseUnit2.isDead && !(baseUnit2 instanceof Tree) && baseUnit2.unitCreationTime < this.gameTimeMillis - 30000 && i3 > 70) {
                        baseUnit2.remove();
                        i3--;
                    }
                }
            }
        }
        this.performanceProfiler.a(ProfilerSection.update_all_team_and_ai);
        PlayerTeam.updateAllTeams(f);
        this.performanceProfiler.b(ProfilerSection.update_all_team_and_ai);
        BuildPreview.updateAll(f);
        this.effectManager.update(f);
        this.cloudRenderer.update(f);
        GameViewUtils.a(f);
        if (this.missionEngine != null) {
            this.missionEngine.c(f);
        }
        this.performanceProfiler.a(ProfilerSection.update_groupcontroller);
        this.formationEngine.a(f);
        this.performanceProfiler.b(ProfilerSection.update_groupcontroller);
        this.performanceProfiler.a(ProfilerSection.update_minimap);
        this.minimap.update(f);
        this.performanceProfiler.b(ProfilerSection.update_minimap);
        this.pathfindingEngine.b(f);
        if (this.teamStats != null) {
            this.teamStats.update();
        }
        this.gameStatistics.b();
    }

    /* JADX INFO: renamed from: a */
    public void drawThreadSafe(float f) {
        synchronized (this.drawLock) {
            draw(f, true);
        }
    }

    /** Draws the world for a background frame without painting the in-game HUD. */
    public void drawWorldOnlyThreadSafe(float f) {
        synchronized (this.drawLock) {
            draw(f, false);
        }
    }

    private void releaseWaterTextures() {
        if (this.waterCloudTexture != null) {
            this.waterCloudTexture.o();
            this.waterCloudTexture = null;
        }
        if (this.waterLayer1Texture != null) {
            this.waterLayer1Texture.o();
            this.waterLayer1Texture = null;
        }
        if (this.waterLayer2Texture != null) {
            this.waterLayer2Texture.o();
            this.waterLayer2Texture = null;
        }
    }

    /* JADX INFO: renamed from: i */
    public boolean setupPostProcessing() {
        if (this.postBaseBuffer == null) {
            this.postBaseBuffer = FrameBufferHelper.postBase();
        }
        if (this.postDisplacementBuffer == null) {
            this.postDisplacementBuffer = FrameBufferHelper.postDisplacement();
        }
        this.postBaseBuffer.a(this.renderGraphicsEngine);
        this.postDisplacementBuffer.a(this.renderGraphicsEngine);
        if (this.postBaseBuffer.g || this.postDisplacementBuffer.g) {
            if (!this.postProcessingFailed) {
                this.postProcessingFailed = true;
                GameEngine.log("setupPostprocessing: failed");
                return false;
            }
            return false;
        }
        return true;
    }

    /* JADX INFO: renamed from: a */
    public void beginPostProcessing(FrameBufferHelper frameBufferHelper) {
        if (this.previousRenderGraphicsEngine != null) {
            throw new RuntimeException("Layer already enabled");
        }
        this.previousRenderGraphicsEngine = this.renderGraphicsEngine;
        this.renderGraphicsEngine = frameBufferHelper.b;
        this.renderGraphicsEngine.i();
        this.renderGraphicsEngine.a(new Rect(0, 0, this.renderGraphicsEngine.m(), this.renderGraphicsEngine.n()));
        this.renderGraphicsEngine.b(frameBufferHelper.f, frameBufferHelper.e);
    }

    /* JADX INFO: renamed from: b */
    public void endPostProcessing(FrameBufferHelper frameBufferHelper) {
        if (this.previousRenderGraphicsEngine == null) {
            throw new RuntimeException("Layer not enabled");
        }
        this.renderGraphicsEngine.j();
        this.renderGraphicsEngine.p();
        this.renderGraphicsEngine = this.previousRenderGraphicsEngine;
        this.previousRenderGraphicsEngine = null;
        this.renderGraphicsEngine.b(frameBufferHelper.f, frameBufferHelper.e);
    }

    /* JADX INFO: renamed from: b */
    public void draw(float f) {
        draw(f, true);
    }

    private void draw(float f, boolean includeUi) {
        if (isHeadlessMode) {
            return;
        }
        this.renderFrameCount++;
        if (this.shouldAdvanceAfterGameEnd) {
            this.renderGraphicsEngine.b(ArgbColor.a(0, 0, 0));
            this.renderGraphicsEngine.a("Loading..", this.halfScreenWidth, this.halfScreenHeight, this.loadingPaint);
            return;
        }
        float f2 = this.renderSurfaceScale;
        if (f2 != 1.0f) {
            this.renderGraphicsEngine.i();
            this.renderGraphicsEngine.a(f2, f2);
        }
        try {
            boolean zIsPostProcessingSupported = GameEngine.isPostProcessingSupported();
            if (zIsPostProcessingSupported && isKeyPressed(113) && isKeyPressed(44)) {
                zIsPostProcessingSupported = false;
            }
            if (zIsPostProcessingSupported && !setupPostProcessing()) {
                zIsPostProcessingSupported = false;
            }
            if (zIsPostProcessingSupported) {
                beginPostProcessing(this.postBaseBuffer);
                try {
                    this.renderGraphicsEngine.b(ArgbColor.a(0, 0, 0));
                    this.performanceProfiler.a(ProfilerSection.draw_game);
                    drawGame(f);
                    this.performanceProfiler.b(ProfilerSection.draw_game);
                    endPostProcessing(this.postBaseBuffer);
                    this.postBaseBuffer.b();
                    if (!this.postDisplacementBuffer.a()) {
                        beginPostProcessing(this.postDisplacementBuffer);
                        try {
                            this.renderGraphicsEngine.b(ArgbColor.a(128, 128, 255));
                            applyZoomTransform();
                            int iDrawEffect = this.effectManager.drawEffect(f, 3);
                            this.effectManager.texture = null;
                            endPostProcessing(this.postDisplacementBuffer);
                            if (iDrawEffect > 0) {
                                this.postDisplacementBuffer.d.configure(this.postBaseBuffer.a, 0.2f * this.zoom);
                                this.postDisplacementBuffer.b();
                            }
                        } catch (Throwable th) {
                            endPostProcessing(this.postDisplacementBuffer);
                            throw th;
                        }
                    }
                } catch (Throwable th2) {
                    endPostProcessing(this.postBaseBuffer);
                    throw th2;
                }
            } else {
                this.performanceProfiler.a(ProfilerSection.draw_game);
                drawGame(f);
                this.performanceProfiler.b(ProfilerSection.draw_game);
            }
            notifyWorldFrameRendered();
            boolean isNativeHudVisible = UiRegistry.isNativeHudVisible();
            if (includeUi && !isGamePaused() && isNativeHudVisible) {
                this.performanceProfiler.a(ProfilerSection.draw_gui);
                drawUI(f);
                this.performanceProfiler.b(ProfilerSection.draw_gui);
            }
            if (includeUi && this.settingsEngine.showFps && this.pauseTransition == 0.0f && !this.isMenuOpen && !this.isPaused) {
                this.renderGraphicsEngine.a(this.fpsString, 100.0f, 35.0f, this.fpsPaint);
            }
            if (includeUi && safeModeReasonText != null) {
                this.renderGraphicsEngine.a(safeModeReasonText, 100.0f, 85.0f, this.fpsPaint);
            }
            if (includeUi && !this.isStopped) {
                this.gameUI.handleTouchGestures(f, isNativeHudVisible);
            }
            if (includeUi && !isGamePaused()) {
                this.effectManager.drawEffect(f, 4);
            }
            CustomUnit.dE();
        } finally {
            if (f2 != 1.0f) {
                this.renderGraphicsEngine.j();
            }
        }
    }

    private void notifyWorldFrameRendered() {
        Runnable listener = this.worldFrameRenderedListener;
        if (listener != null) {
            listener.run();
        }
    }

    /* JADX INFO: renamed from: j */
    public boolean shouldDrawUnitIcons() {
        if (!this.settingsEngine.showUnitIcons) {
            return false;
        }
        if (this.zoom >= 0.7d || this.viewpointWidth < this.tileMap.getWorldWidth() - 5.0f || this.visibleWorldHeight < this.tileMap.getWorldHeight() - 5.0f) {
            return isSpaceGame() ? ((double) this.zoom) < 0.1d : isPC() ? ((double) this.zoom) < 0.27d : ((double) this.zoom) < 0.4d;
        }
        return true;
    }

    /* JADX INFO: renamed from: b */
    public void drawOutOfBoundsBackground(float f) {
        boolean z = false;
        if (this.visibleWorldRect.a < 0 || this.visibleWorldRect.b < 0 || this.visibleWorldRect.c > this.tileMap.getWorldWidth() || this.visibleWorldRect.d > this.tileMap.getWorldHeight()) {
            z = true;
        }
        if (z) {
            this.renderGraphicsEngine.b(ArgbColor.a(0, 0, 0));
        }
    }

    /* JADX INFO: renamed from: c */
    public void handleFloat(float f) {
    }

    /* JADX WARN: Removed duplicated region for block: B:96:0x044a  */
    /* JADX INFO: renamed from: c */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */

    public void drawGame(float float2) {
        if (this.hasLoadedLevel) {
            this.performanceProfiler.a(ProfilerSection.update_game_shouldDraw);
            this.renderListBuffer.b();
            this.selectedWaypointDrawCount = 0;
            boolean var3 = false;
            GameObject[] var4 = BaseUnit.fastGameObjectList.a();
            int var5 = GameObject.fastGameObjectList.size();

            for (int var6 = 0; var6 < var5; var6++) {
                GameObject var7 = var4[var6];
                boolean var8 = var7.shouldDraw;
                boolean var9 = var7.a(this);
                var7.shouldDraw = var9;
                if (var8 != var9) {
                    var3 = true;
                }

                if (var9) {
                    this.renderListBuffer.add(var7);
                }
            }

            if (this.renderList.size() != this.renderListBuffer.size()) {
                var3 = true;
            }

            this.performanceProfiler.b(ProfilerSection.update_game_shouldDraw);
            this.performanceProfiler.a(ProfilerSection.update_game_sortRender);
            if (var3) {
                GameObjectArrayList var12 = this.renderList;
                this.renderList = this.renderListBuffer;
                this.renderListBuffer = var12;
            }

            if (!this.shouldDrawUnitIcons()) {
                Collections.sort(this.renderList, GameObject.ei);
            }

            this.performanceProfiler.b(ProfilerSection.update_game_sortRender);
            this.performanceProfiler.a(ProfilerSection.draw_setup);
            this.performanceProfiler.a(ProfilerSection.draw_setup_clip);
            this.renderGraphicsEngine.i();
            this.renderGraphicsEngine.a(this.screenClipRect);
            this.performanceProfiler.b(ProfilerSection.draw_setup_clip);
            this.performanceProfiler.a(ProfilerSection.draw_setup_fill);
            this.drawOutOfBoundsBackground(float2);
            this.performanceProfiler.b(ProfilerSection.draw_setup_fill);
            if (this.settingsEngine.renderFancyWater) {
                GraphicsEngine rootGraphicsBackend = this.previousRenderGraphicsEngine != null
                        ? this.previousRenderGraphicsEngine
                        : this.renderGraphicsEngine;
                if (this.waterTextureBackend != rootGraphicsBackend) {
                    releaseWaterTextures();
                    this.waterTextureBackend = rootGraphicsBackend;
                }
                if (this.waterCloudTexture == null) {
                    this.waterCloudTexture = rootGraphicsBackend.a(R.drawable.water_cloud);
                }

                if (this.waterLayer1Texture == null) {
                    this.waterLayer1Texture = rootGraphicsBackend.a(R.drawable.water_layer1);
                }

                if (this.waterLayer2Texture == null) {
                    this.waterLayer2Texture = rootGraphicsBackend.a(R.drawable.water_layer2);
                }

                this.waterRect.a(this.screenClipRect);
                this.waterAnimationTimer += 0.05F * float2;
                if (this.waterAnimationTimer > 100.0F) {
                    this.waterAnimationTimer -= 100.0F;
                }

                this.renderGraphicsEngine.a(this.waterCloudTexture, this.waterRect, null, this.viewpointXInt / 6, this.viewpointYInt / 6, 1, 1);
                this.waterRect.a(this.viewportClipRect);
                this.waterRectF.a(this.viewportClipRect);
                this.renderGraphicsEngine.i();
                this.applyZoomTransform();
                this.renderGraphicsEngine.a(this.waterLayer2Texture, this.waterRectF, null, this.viewpointXInt + this.waterAnimationTimer, this.viewpointYInt + this.waterAnimationTimer, 0, 0);
                this.renderGraphicsEngine.a(this.waterLayer1Texture, this.waterRectF, null, (float) this.viewpointXInt, (float) this.viewpointYInt, 0, 0);
                this.renderGraphicsEngine.j();
            }

            this.performanceProfiler.a(ProfilerSection.draw_setup_drawMap);
            if (this.tileMap != null && this.shouldUpdateFogRenderPass()) {
                this.tileMap.updateFogRenderPass(float2);
            }

            this.performanceProfiler.b(ProfilerSection.draw_setup_drawMap);
            this.applyZoomTransform();
            this.renderGraphicsEngine.a(this.viewportClipRect);
            boolean var13 = this.shouldDrawUnitIcons();
            this.pathfindingEngine.c(float2);
            this.performanceProfiler.b(ProfilerSection.draw_setup);
            GameObject[] var14 = this.renderList.a();
            int var15 = this.renderList.size();
            this.shouldDrawHighDetailEffects = true;
            this.shouldDrawMediumDetailEffects = true;
            this.shouldDrawSmallUnitShadows = true;
            this.shouldDrawUnitShadows = true;
            this.shouldDrawUnitLegDetails = true;
            if (this.zoom < 0.45) {
                this.shouldDrawSmallUnitShadows = false;
                this.shouldDrawHighDetailEffects = false;
                this.shouldDrawUnitLegDetails = false;
            }

            if (this.zoom < 0.3) {
                this.shouldDrawUnitShadows = false;
                this.shouldDrawMediumDetailEffects = false;
            }

            if (!var13) {
                for (int var16 = 0; var16 < var15; var16++) {
                    GameObject var10 = var14[var16];
                    if (var10.drawLayer == 0) {
                        var10.c(float2);
                    }
                }
            }

            BuildPreview.drawAll(float2);
            this.performanceProfiler.a(ProfilerSection.draw_game_effects);
            this.effectManager.getEffectCount(float2);
            this.effectManager.drawEffect(float2, 1);
            this.performanceProfiler.b(ProfilerSection.draw_game_effects);
            this.performanceProfiler.a(ProfilerSection.draw_game_unit);
            if (var13) {
                if (this.gameUI.getSelectedUnitCount() == 0) {
                    BaseUnit.bI.a(255, 195, 195, 195);
                    BaseUnit.bJ.a(255, 255, 255, 255);
                } else {
                    BaseUnit.bI.a(175, 175, 175, 175);
                    BaseUnit.bJ.a(255, 255, 255, 255);
                }

                for (int var17 = 0; var17 < var15; var17++) {
                    GameObject var24 = var14[var17];
                    if (!var24.f(float2)) {
                        var24.c(float2);
                    }
                }

                for (int var18 = 0; var18 < var15; var18++) {
                    GameObject var25 = var14[var18];
                    var25.a(float2, true);
                    var25.p(float2);
                }
            } else {
                for (int var19 = 0; var19 < var15; var19++) {
                    GameObject var26 = var14[var19];
                    var26.d(float2);
                }

                for (int var20 = 0; var20 < var5; var20++) {
                    GameObject var27 = var4[var20];
                    if (!var27.shouldDraw) {
                        if (!(var27 instanceof BaseUnit)) {
                            continue;
                        }

                        BaseUnit var11 = (BaseUnit) var27;
                        if (!var11.isSelected || var11.team != this.playerTeam && !var11.isVisibleToLocalPlayer()) {
                            continue;
                        }
                    }

                    var27.e(float2);
                    if (!var27.shouldDraw) {
                        var27.p(float2);
                    }
                }

                for (int var21 = 0; var21 < var15; var21++) {
                    GameObject var28 = var14[var21];
                    if (var28.drawLayer != 0 && var28.drawLayer != 10) {
                        var28.c(float2);
                    }
                }

                for (int var22 = 0; var22 < var15; var22++) {
                    GameObject var29 = var14[var22];
                    var29.a(float2, false);
                    var29.p(float2);
                }

                PlayerTeam.h(float2);
            }

            this.shouldDrawSmallUnitShadows = true;
            this.shouldDrawUnitShadows = true;
            this.performanceProfiler.b(ProfilerSection.draw_game_unit);
            this.performanceProfiler.a(ProfilerSection.draw_game_effects);
            this.effectManager.drawEffect(float2, 2);
            this.performanceProfiler.b(ProfilerSection.draw_game_effects);

            for (int var23 = 0; var23 < var15; var23++) {
                GameObject var30 = var14[var23];
                if (var30.drawLayer == 10) {
                    var30.c(float2);
                }
            }

            this.cloudRenderer.draw(float2);
            if (this.missionEngine != null) {
                this.missionEngine.a(float2);
            }

            this.handleFloat(float2);
            GameViewUtils.b(float2);
            this.unitSpatialIndex.c(float2);
            this.performanceProfiler.a(ProfilerSection.draw_end);
            this.renderGraphicsEngine.j();
            this.performanceProfiler.b(ProfilerSection.draw_end);
        }
    }


    /* JADX INFO: renamed from: d */
    public void drawUI(float f) {
        this.gameUI.processTouchInput(f);
        if (this.missionEngine != null) {
            this.missionEngine.b(f);
        }
        this.minimap.draw(f);
        if (this.settingsEngine.showFps && this.pauseTransition == 0.0f) {
            this.performanceProfiler.c();
        }
        if (this.isLookModeEnabled) {
            this.renderGraphicsEngine.a("Look Mode", this.halfScreenWidth, this.halfScreenHeight, this.loadingPaint);
        }
        if (this.showAITeamInfoOverlay) {
            int i = 20;
            for (int i2 = 0; i2 < PlayerTeam.TEAM_NEUTRAL; i2++) {
                PlayerTeam playerTeamK = PlayerTeam.k(i2);
                if (playerTeamK != null && (playerTeamK instanceof AIController)) {
                    AIController aIController = (AIController) playerTeamK;
                    this.renderGraphicsEngine.a(aIController.teamId + "| c:" + aIController.credits, 20.0f, i, this.teamInfoPaint);
                    i += 20;
                }
            }
        }
    }

    /* JADX INFO: renamed from: k */
    public void updateCameraSystem() {
        this.screenScale = getScreenScale();
        updateDensity();
        this.halfScreenWidth = this.screenWidth / 2.0f;
        this.halfScreenHeight = this.screenHeight / 2.0f;
        this.sidebarWidth = (int) (this.screenHeight / 3.0f);
        if (isPC()) {
            this.sidebarWidth = (int) (this.screenHeight / 2.5f);
        }
        float f = (int) (this.screenWidth / 3.0f);
        if (this.sidebarWidth > f) {
            this.sidebarWidth = f;
        }
        this.sidebarWidth = Utility.clampTo255(this.sidebarWidth, 60.0f, (int) (250.0f * this.screenScale));
        float f2 = this.viewpointX + this.halfVisibleWorldWidth;
        float f3 = this.viewpointY + this.halfVisibleWorldHeight;
        if (this.isPaused) {
            this.currentScreenWidthPixels = this.screenWidth;
            this.currentViewpointWidthPixels = this.screenWidth;
        } else {
            this.currentViewpointWidthPixels = (this.screenWidth - this.sidebarWidth) + 1.0f;
            if (GameUI.showModernSidebar) {
                this.currentScreenWidthPixels = this.screenWidth;
            } else {
                this.currentScreenWidthPixels = this.currentViewpointWidthPixels;
            }
        }
        if (this.currentScreenWidthPixels < 1.0f) {
            this.currentScreenWidthPixels = 1.0f;
        }
        if (this.currentViewpointWidthPixels < 1.0f) {
            this.currentViewpointWidthPixels = 1.0f;
        }
        if (this.wasPaused != this.isPaused) {
            if (!this.isPaused) {
                f2 -= (this.sidebarWidth / 2.0f) / this.zoom;
            } else {
                f2 += (this.sidebarWidth / 2.0f) / this.zoom;
            }
        }
        this.wasPaused = this.isPaused;
        this.currentScreenHeightPixels = this.screenHeight;
        this.visibleWorldWidth = this.currentScreenWidthPixels / this.zoom;
        this.visibleWorldHeight = this.currentScreenHeightPixels / this.zoom;
        this.viewpointWidth = this.currentViewpointWidthPixels / this.zoom;
        this.halfVisibleWorldWidth = this.visibleWorldWidth / 2.0f;
        this.halfVisibleWorldHeight = this.visibleWorldHeight / 2.0f;
        this.screenClipRect.a(0, 0, (int) this.currentScreenWidthPixels, (int) this.currentScreenHeightPixels);
        this.viewportClipRect.a(0, 0, ((int) this.visibleWorldWidth) + 1, ((int) this.visibleWorldHeight) + 1);
        this.visibleScreenRect.a(0.0f, 0.0f, this.visibleWorldWidth + 1.0f, this.visibleWorldHeight + 1.0f);
        setViewpoint(f2 - this.halfVisibleWorldWidth, f3 - this.halfVisibleWorldHeight);
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: b */
    public void updateWindowResolution(int i, int i2) {
        updateWindowResolution(i, i2, 1.0f);
    }

    @Override
    public void updateWindowResolution(int i, int i2, float renderSurfaceScale) {
        updateViewpoint(i, i2, renderSurfaceScale);
    }

    /* JADX INFO: renamed from: a */
    public void updateViewpoint(int i, int i2, float f) {
        this.screenWidth = i;
        this.screenHeight = i2;
        this.renderSurfaceScale = f;
        updateCameraSystem();
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: l */
    public String getPackageName() {
        if (GameEngine.isJavaDesktopVersion) {
            return "com.corrodinggames.rts.java";
        }
        if (GameEngine.isGDXVersion) {
            return "com.corrodinggames.rts.gdx";
        }
        if (isNonAndroidVersion) {
            return "com.corrodinggames.rts.server";
        }
        return AppMetadataBridge.packageName();
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: m */
    public String getInstallerPackageName() {
        if (GameEngine.isJavaDesktopVersion) {
            return "java";
        }
        if (GameEngine.isGDXVersion) {
            return "java-gdx";
        }
        if (isNonAndroidVersion) {
            return "dedicatedServer";
        }
        return AppMetadataBridge.installerPackageName();
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: n */
    public boolean isBetaOrPreview() {
        if (getVersionString().contains("p")) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: c */
    public int getVersionCode(boolean z) {
        if (isNonAndroidVersion || z) {
            return 176;
        }
        return AppMetadataBridge.compatibleCoreVersionCode();
    }

    /* JADX INFO: renamed from: o */
    public String getSignature() {
        if (!isAndroidPlatform()) {
            return null;
        }
        return AppMetadataBridge.signature();
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: p */
    public boolean isModdingEnabled() {
        if (!GameEngine.isIOSVersion) {
            if (isNotObfuscated() || isDebugServerActive) {
                return true;
            }
            return false;
        }
        return false;
    }

    /* JADX INFO: renamed from: q */
    public boolean isNotObfuscated() {
        if (OrderableUnit.class.getSimpleName().equals("OrderableUnit")) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: r */
    public String getVersionNameWithSuffix() {
        String versionName = getVersionName();
        return versionName;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: s */
    public void refreshVersionName() {
        gameVersionName = null;
        getVersionName();
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: t */
    public String getVersionName() {
        if (gameVersionName != null) {
            return gameVersionName;
        }
        String str = "v" + getVersion();
        if (!GameEngine.isGameBeta || isDebugServerActive) {
            str = "DEBUG BUILD - " + str;
        } else if (GameEngine.isTestingBuild) {
            str = "TESTING BUILD - " + str;
        } else if (str.contains("p")) {
            str = "BETA VERSION - " + str;
        }
        if (!GameEngine.isIOSVersion && isNotObfuscated()) {
            str = "RAW - " + str;
        }
        gameVersionName = str;
        return gameVersionName;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: u */
    public String getVersion() {
        return "1.15";
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: v */
    public String getVersionString() {
        return "1.15";
    }

    /* JADX INFO: renamed from: w */
    public synchronized void stopGameTimer() {
        this.ac = false;
        if (this.gameTimer != null) {
            this.gameTimer.cancel();
            this.gameTimer = null;
        }
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: a */
    public synchronized void colorizeLogMessage(CoreGameView gameView, boolean z) {
        synchronized (this.initLock) {
            if (!isNonAndroidVersion) {
                gameView.pause();
            }
            this.isStopped = z;
            this.isPaused = this.isStopped;
            if (z && !this.hasLoadedLevel && !this.fullReload && !GameEngine.isMenuBackgroundDisabled && !this.networkEngine.networkGameActive) {
                loadMenuBackground();
            }
            CoreGameView gameView2 = this.pendingGameView;
            if (this.activeGameView == null) {
                this.activeGameView = gameView;
            }
            this.pendingGameView = gameView;
            if (gameView2 != null && gameView2 != gameView) {
                gameView2.onResume();
            }
            if (gameView != null) {
                gameView.stopRender();
            }
            if (this.gameUI != null) {
                this.gameUI.initializeLocalizedStrings();
            }
            stopGameTimer();
            startGameThread();
        }
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: x */
    public synchronized void loadMenuBackground() {
        if (this.menuLoadFailureCount > 20) {
            return;
        }
        int i = this.settingsEngine.nextBackgroundMap;
        this.settingsEngine.nextBackgroundMap++;
        if (this.settingsEngine.nextBackgroundMap > 3) {
            this.settingsEngine.nextBackgroundMap = 1;
        }
        this.settingsEngine.save();
        int iDistance = Utility.distance(i, 1, 3);
        this.remoteMapStream = null;
        this.currentMapPath = "maps/menu_background/menu" + iDistance + ".tmx";
        try {
            PlayerTeam.setMaxTeamId(10, true);
            for (int i2 = 0; i2 < PlayerTeam.TEAM_NEUTRAL; i2++) {
                AIController aIController = new AIController(i2);
                if (i2 == 0) {
                    this.playerTeam = aIController;
                }
            }
            loadGame(false, GameMode.menu);
            this.isMenuBackgroundMap = true;
            this.gameUI.clearSelection();
            if (!this.hasLoadedLevel) {
                GameEngine.logWarningAndStack("Menu load failed");
                this.menuLoadFailureCount++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* JADX INFO: renamed from: d */
    void updateCameraFocus(float f) {
        if (this.isStopped && !this.isMenuBackgroundMap) {
            if (this.nextCameraFocusUnit == null) {
                this.nextCameraFocusUnit = findCameraFocusUnit();
                if (this.cameraFocusUnit == this.nextCameraFocusUnit) {
                    this.nextCameraFocusUnit = null;
                }
            }
            if (this.cameraFocusUnit == null) {
                this.cameraFocusUnit = this.nextCameraFocusUnit;
                this.nextCameraFocusUnit = null;
            }
            if (this.cameraFocusTransition != 0.0f && this.nextCameraFocusUnit != null) {
                moveCameraTo(f, this.nextCameraFocusUnit.posX, this.nextCameraFocusUnit.posY, this.cameraFocusTransition * 0.5f);
            }
            if (this.cameraFocusUnit != null) {
                boolean zMoveCameraTo = moveCameraTo(f, this.cameraFocusUnit.posX, this.cameraFocusUnit.posY, (1.0f - this.cameraFocusTransition) * 0.5f);
                if (Utility.distanceSq(this.viewpointX + this.halfVisibleWorldWidth, this.viewpointY + this.halfVisibleWorldHeight, this.cameraFocusUnit.posX, this.cameraFocusUnit.posY) < 6400.0f) {
                    zMoveCameraTo = true;
                }
                if (zMoveCameraTo) {
                    this.isCameraFocusing = true;
                }
            }
            if (this.isCameraFocusing) {
                this.cameraFocusTransition += 0.01f * f;
                if (this.cameraFocusTransition >= 1.0f) {
                    this.cameraFocusTransition = 0.0f;
                    this.cameraFocusUnit = null;
                    this.isCameraFocusing = false;
                }
            }
        }
    }

    /* JADX INFO: renamed from: a */
    BaseUnit findRandomUnit(PlayerTeam playerTeam) {
        int i = 0;
        for (BaseUnit baseUnit : BaseUnit.bE) {
            if (!baseUnit.u() && (baseUnit.team == playerTeam || playerTeam == null)) {
                i++;
            }
        }
        if (i > 0) {
            int randomIntInRange = Utility.getRandomIntInRange(0, i - 1);
            int i2 = 0;
            for (BaseUnit baseUnit2 : BaseUnit.bE) {
                if (!baseUnit2.u() && (baseUnit2.team == playerTeam || playerTeam == null)) {
                    if (i2 == randomIntInRange) {
                        return baseUnit2;
                    }
                    i2++;
                }
            }
            return null;
        }
        return null;
    }

    /* JADX INFO: renamed from: y */
    BaseUnit findCameraFocusUnit() {
        BaseUnit baseUnitFindRandomUnit = findRandomUnit(this.playerTeam);
        if (baseUnitFindRandomUnit != null) {
            return baseUnitFindRandomUnit;
        }
        return findRandomUnit((PlayerTeam) null);
    }

    /* JADX INFO: renamed from: a */
    public boolean moveCameraTo(float f, float f2, float f3, float f4) {
        float angleBetweenPoints = Utility.getAngleBetweenPoints(this.viewpointX + this.halfVisibleWorldWidth, this.viewpointY + this.halfVisibleWorldHeight, f2, f3);
        float fDistanceSq = Utility.distanceSq(this.viewpointX + this.halfVisibleWorldWidth, this.viewpointY + this.halfVisibleWorldHeight, f2, f3);
        float f5 = f4 * f;
        float f6 = 15.0f;
        if (15.0f < f5 + 1.0f) {
            f6 = f5 + 1.0f;
        }
        if (fDistanceSq < f6 * f6 || this.wasCameraClamped) {
            return true;
        }
        this.cameraMovementX += Utility.fastCos(angleBetweenPoints) * f5;
        this.cameraMovementY += Utility.fastSin(angleBetweenPoints) * f5;
        if (Utility.abs(this.cameraMovementX) >= 1.0f || Utility.abs(this.cameraMovementY) >= 1.0f) {
            this.viewpointX += this.cameraMovementX;
            this.viewpointY += this.cameraMovementY;
            this.cameraMovementX = 0.0f;
            this.cameraMovementY = 0.0f;
            setViewpoint(this.viewpointX, this.viewpointY);
            return false;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameEngine
    /* JADX INFO: renamed from: z */
    public int getAllUnitsChecksum() {
        return this.allUnitsChecksum;
    }
}
