package com.corrodinggames.rts.gameFramework;

import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.gameFramework.file.FileHelper;
import com.corrodinggames.rts.gameFramework.utility.Log;
import com.corrodinggames.rts.gameFramework.utility.saf.SafFileLoader;
import io.github.rwx.Preference;
import io.github.rwx.PreferenceStorage;
import io.github.rwx.SafPlatformBridge;

import java.lang.reflect.Field;
import java.util.HashMap;

import static io.github.rwx.PreferenceStorageKt.PREFERENCE_NAME;

/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/SettingsEngine.class */
public class SettingsEngine {
    public boolean enableSounds;
    public boolean enableMouseCapture;
    public boolean androidNoSoundPrioritiesDebug;
    public boolean disableDigitGrouping;
    public int settingsGameVersion;
    public int settingsGameVersionFirst;
    public boolean upgradedToNoPublicStorage;
    public boolean upgradedToNoPublicStorageWarningShown;
    public boolean autosaving;
    public float masterVolume;
    public float gameVolume;
    public float interfaceVolume;
    public float musicVolume;
    public float scrollSpeed;
    public float edgeScrollSpeed;

    public boolean hasPlayedGameOrSeenHelp;

    public boolean onscreenControls;
    public boolean trackpad;
    public boolean dpad;

    public boolean batterySaving;

    public boolean highRefreshRate;
    public boolean slick2dFullScreen;

    public boolean renderBackground;
    public boolean renderExtraLayers;

    public boolean immersiveFullScreen;
    public boolean displayOverCutout;

    public boolean unlockedScreenRotation;

    public boolean renderDoubleScale;
    public float uiRenderScale;
    public boolean renderClouds;

    public boolean renderWithLineWidth;

    public boolean softFogFading;

    public boolean showUnitGroups;

    public boolean allowGameRecording;

    public boolean renderControls;

    public boolean showHp;

    public boolean showHpChanges;

    public boolean showUnitIcons;

    public boolean gestureZoom;

    public boolean useCircleSelect;

    public boolean showZoomButton;

    public boolean showFps;

    public boolean newRender;

    public boolean shaderEffects;

    public boolean teamShaders;

    public boolean showUnitWaypoints;

    public boolean useMinimapAllyColors;

    public boolean showWarLogOnScreen;

    public boolean classicInterface;

    public boolean quickRally;

    public boolean doubleClickToAttackMove;

    public boolean showMapPingsOnBattlefield;
    public boolean showMapPingsOnMinimap;

    public boolean showPlayerChatInGame;
    public boolean sendReports;

    public boolean shownAudioWarning;

    public boolean mouseSupport;

    public boolean keyboardSupport;

    public boolean forceEnglish;
    public String overrideLanguageCode;

    public boolean saveMultiplayerReplays;

    public boolean replaysShowRecordedChat;
    public int nextBackgroundMap;
    public String lastNetworkPlayerName;
    public String lastNetworkIP;
    public String lastP2PRoomId;
    public String lastDebugOption;
    public boolean landscapeOrientation;
    public int aiDifficulty;
    public int locationAction;
    public int locationDpad;
    public int keyAction;
    public int keyJump;
    public int keyLeft;
    public int keyRight;
    public int keyDown;
    public String uuid;
    public String networkClientId;
    public String networkClientIdMachineKey;
    public String networkServerId;
    public int numIncompleteLoadAttempts;
    public int numLoadsSinceRunningGameOrNormalExit;
    public int lastSeenMessageId;
    public String lastSeenMessageIds;
    public int networkPort;

    public boolean udpInMultiplayer;
    public int banTimeInSecondsAfterKick;
    public int numberOfWins;
    public boolean rateGameShown;
    public int mouseOrders;
    public int mousePlacement;

    public boolean liveReloading;

    public boolean renderVsync;

    public boolean renderSmoothDelta;
    public int teamUnitCapSinglePlayer;
    public int teamUnitCapHostedGame;

    public boolean showChatAndPingShortcuts;
    public String modSettings;
    public int modSettingsVersion;
    public int storageType;
    public boolean hasSelectedAStorageType;
    public boolean hadStoragePermissionInPast;

    public boolean loadDisabledModData;
    public int lastModCount;
    public String modSAFlinks;
    public boolean externalSAFWorking;
    public String externalSAFLink;
    public String externalSAFPathShown;
    public String externalSAFPathExtra;

    public boolean smartSelection_v2;
    public boolean replayTracing;
    Preference prefs;
    static SettingsEngine settingsEngine = null;

    public boolean resizeFontWithUIScale = true;
    public float renderDensity = 1.0f;

    public boolean renderExtraShadows = true;

    public boolean renderFancyWater = false;

    public boolean showActionInfoHoverNearMouse = true;

    public boolean disableModLazyLoad = false;

    public boolean renderAntiAlias = true;

    public boolean showSelectedUnitsList = true;
    public String teamColors = "#00ff00,#d02013,#0463f3,#ffff40,#00ffff,#d0f8f7,#000000,#ff00ea,#ff7f18,#9368c4";
    public String teamColorsNames = "GREEN,RED,BLUE,YELLOW,CYAN,WHITE,BLACK,PINK,ORANGE,PURPLE";

    public boolean highGraphics = true;

    HashMap<String, Field> settingFields = new HashMap<>();

    public static SettingsEngine getInstance() {
        if (settingsEngine == null) {
            settingsEngine = new SettingsEngine();
        }
        return settingsEngine;
    }

    private static Preference loadPreferenceStorage() {
        try {
            PreferenceStorage bridge = org.koin.java.KoinJavaComponent.get(PreferenceStorage.class);
            return bridge.preference(PREFERENCE_NAME);
        } catch (Throwable e) {
            GameEngine.log("SettingsEngine: preference storage unavailable: " + e.getMessage());
            return null;
        }
    }

    public boolean getBooleanPref(String str, boolean z) {
        return this.prefs != null ? this.prefs.getBoolean(str, z) : z;
    }

    public int getIntPref(String str, int i) {
        return this.prefs != null ? this.prefs.getInt(str, i) : i;
    }

    public float getFloatPref(String str, float f) {
        return this.prefs != null ? this.prefs.getFloat(str, f) : f;
    }

    public String getStringPref(String str, String str2) {
        return this.prefs != null ? this.prefs.getString(str, str2) : str2;
    }

    private SettingsEngine() {
        this.autosaving = true;
        this.uiRenderScale = 1.0f;
        this.renderClouds = false;
        this.softFogFading = false;
        this.doubleClickToAttackMove = false;
        this.showMapPingsOnBattlefield = true;
        this.showMapPingsOnMinimap = true;
        this.showPlayerChatInGame = true;
        this.banTimeInSecondsAfterKick = 60;
        this.teamUnitCapSinglePlayer = 1000;
        this.teamUnitCapHostedGame = 250;
        this.showChatAndPingShortcuts = true;
        for (Field field : getClass().getFields()) {
            String name = field.getName();
            if (this.settingFields.get(name) != null) {
                GameEngine.log("SettingsEngine: fields: " + name + " already exists");
            }
            if (GameEngine.isIOSVersion) {
                GameEngine.log("SettingsEngine: field:" + name);
            }
            this.settingFields.put(name, field);
        }
        this.prefs = loadPreferenceStorage();
        int intPref = getIntPref("settingVersion", 1);
        this.settingsGameVersion = getIntPref("settingsGameVersion", 0);
        this.settingsGameVersionFirst = getIntPref("settingsGameVersionFirst", 0);
        if (this.settingsGameVersionFirst == 0) {
            if (this.settingsGameVersion != 0) {
                this.settingsGameVersionFirst = this.settingsGameVersion;
            } else {
                this.settingsGameVersionFirst = 176;
            }
        }
        this.upgradedToNoPublicStorage = getBooleanPref("upgradedToNoPublicStorage", this.settingsGameVersionFirst <= 160);
        this.upgradedToNoPublicStorageWarningShown = getBooleanPref("upgradedToNoPublicStorageWarningShown", false);
        this.hasPlayedGameOrSeenHelp = getBooleanPref("hasPlayedGameOrSeenHelp", false);
        this.enableSounds = getBooleanPref("enableSounds", true);
        this.enableMouseCapture = getBooleanPref("enableMouseCapture", false);
        this.androidNoSoundPrioritiesDebug = getBooleanPref("androidNoSoundPrioritiesDebug", false);
        this.disableDigitGrouping = getBooleanPref("disableDigitGrouping", false);
        this.musicVolume = getFloatPref("musicVolume", 0.25f);
        this.masterVolume = getFloatPref("masterVolume", GameEngine.isPC() ? 0.5f : 1.0f);
        this.gameVolume = getFloatPref("gameVolume", 1.0f);
        this.interfaceVolume = getFloatPref("interfaceVolume", 0.8f);
        this.scrollSpeed = getFloatPref("scrollSpeed", 1.0f);
        this.edgeScrollSpeed = getFloatPref("edgeScrollSpeed", 1.0f);
        this.onscreenControls = getBooleanPref("onscreenControls", true);
        this.trackpad = getBooleanPref("trackpad", true);
        this.dpad = getBooleanPref("dpad", true);
        this.batterySaving = getBooleanPref("batterySaving", false);
        this.highRefreshRate = getBooleanPref("highRefreshRate", GameEngine.isPC());
        this.slick2dFullScreen = getBooleanPref("slick2dFullScreen", GameEngine.isPC());
        this.renderVsync = getBooleanPref("renderVsync", false);
        this.renderSmoothDelta = getBooleanPref("renderSmoothDelta", false);
        this.unlockedScreenRotation = getBooleanPref("unlockedScreenRotation", false);
        this.renderBackground = getBooleanPref("renderBackground", true);
        this.renderExtraLayers = getBooleanPref("renderExtraLayers", true);
        this.renderControls = getBooleanPref("renderControls", true);
        this.immersiveFullScreen = getBooleanPref("immersiveFullScreen", true);
        this.displayOverCutout = getBooleanPref("displayOverCutout", false);
        this.renderDoubleScale = getBooleanPref("renderDoubleScale", false);
        this.showUnitGroups = getBooleanPref("showUnitGroups", true);
        this.renderClouds = getBooleanPref("renderClouds", GameEngine.isIOSVersion ? true : GameEngine.isPC());
        this.renderWithLineWidth = getBooleanPref("renderWithLineWidth", true);
        this.softFogFading = getBooleanPref("softFogFading", GameEngine.isIOSVersion ? true : GameEngine.isPC());
        this.showUnitWaypoints = getBooleanPref("showUnitWaypoints", true);
        this.useMinimapAllyColors = getBooleanPref("useMinimapAllyColors", true);
        this.showWarLogOnScreen = getBooleanPref("showWarLogOnScreen", GameEngine.isPC());
        this.classicInterface = getBooleanPref("classicInterface", false);
        boolean z = GameEngine.isPC();
        this.quickRally = getBooleanPref("quickRally", z);
        if (intPref <= 1 && !GameEngine.isPC()) {
            this.quickRally = z;
        }
        this.doubleClickToAttackMove = getBooleanPref("doubleClickToAttackMove", true);
        this.showMapPingsOnBattlefield = getBooleanPref("showMapPingsOnBattlefield", true);
        this.showMapPingsOnMinimap = getBooleanPref("showMapPingsOnMinimap", true);
        this.showPlayerChatInGame = getBooleanPref("showPlayerChatInGame", true);
        this.allowGameRecording = false;
        this.showHp = getBooleanPref("showHp", true);
        this.showHpChanges = getBooleanPref("showHpChanges", true);
        this.showUnitIcons = getBooleanPref("showUnitIcons", true);
        this.gestureZoom = getBooleanPref("gestureZoom", true);
        this.useCircleSelect = getBooleanPref("useCircleSelect", false);
        this.showZoomButton = getBooleanPref("showZoomButton", true);
        this.showFps = getBooleanPref("showFps", false);
        this.newRender = getBooleanPref("newRender", false);
        this.shaderEffects = getBooleanPref("shaderEffects", false);
        this.teamShaders = getBooleanPref("teamShaders", false);
        this.sendReports = getBooleanPref("sendReports", true);
        this.shownAudioWarning = getBooleanPref("shownAudioWarning", false);
        this.mouseSupport = getBooleanPref("mouseSupport", !GameEngine.isBlueStacks());
        this.keyboardSupport = getBooleanPref("keyboardSupport", true);
        this.forceEnglish = getBooleanPref("forceEnglish", GameEngine.isIOSVersion);
        boolean z2 = GameEngine.isPC();
        this.saveMultiplayerReplays = getBooleanPref("saveMultiplayerReplays", z2);
        if (intPref <= 1) {
            this.saveMultiplayerReplays = z2;
        }
        this.replaysShowRecordedChat = getBooleanPref("replaysShowRecordedChat", true);
        this.lastNetworkPlayerName = getStringPref("lastNetworkPlayerName", null);
        this.lastNetworkIP = getStringPref("lastNetworkIP", null);
        this.lastP2PRoomId = getStringPref("lastP2PRoomId", null);
        this.lastDebugOption = getStringPref("lastDebugOption", null);
        this.aiDifficulty = getIntPref("aiDifficulty", 0);
        this.locationDpad = getIntPref("locationDpad", 0);
        this.locationAction = getIntPref("locationAction", 3);
        this.keyAction = getIntPref("keyAction", 23);
        this.keyJump = getIntPref("keyJump", 19);
        this.keyLeft = getIntPref("keyLeft", 21);
        this.keyRight = getIntPref("keyRight", 22);
        this.keyDown = getIntPref("keyDown", 20);
        this.landscapeOrientation = getBooleanPref("landscapeOrientation", true);
        this.networkPort = getIntPref("networkPort", 5123);
        if (this.networkPort < 1024 || this.networkPort > 65535) {
            this.networkPort = 5123;
        }
        this.udpInMultiplayer = getBooleanPref("udpInMultiplayer", false);
        this.banTimeInSecondsAfterKick = getIntPref("banTimeInSecondsAfterKick", 60);
        this.numIncompleteLoadAttempts = getIntPref("numIncompleteLoadAttempts", 0);
        this.numLoadsSinceRunningGameOrNormalExit = getIntPref("numLoadsSinceRunningGameOrNormalExit", 0);
        this.numberOfWins = getIntPref("numberOfWins", 0);
        this.rateGameShown = getBooleanPref("rateGameShown", false);
        this.uuid = getStringPref("uuid", null);
        this.networkClientId = getStringPref("networkClientId", null);
        this.networkServerId = getStringPref("networkServerId", null);
        this.lastSeenMessageId = getIntPref("lastSeenMessageId", -1);
        this.lastSeenMessageIds = getStringPref("lastSeenMessageIds", VariableScope.nullOrMissingString);
        this.nextBackgroundMap = getIntPref("nextBackgroundMap", 1);
        this.showChatAndPingShortcuts = getBooleanPref("showChatAndPingShortcuts", true);
        this.teamUnitCapSinglePlayer = getIntPref("teamUnitCapSinglePlayer", 1000);
        this.teamUnitCapHostedGame = getIntPref("teamUnitCapHostedGame", 250);
        this.modSettings = getStringPref("modSettings", VariableScope.nullOrMissingString);
        this.modSettingsVersion = getIntPref("modSettingsVersion", 0);
        boolean z3 = GameEngine.isAndroidPlatform();
        this.storageType = getIntPref("storageType", z3 ? 2 : 0);
        this.hadStoragePermissionInPast = getBooleanPref("hadStoragePermissionInPast", false);
        if (z3) {
            this.hadStoragePermissionInPast = true;
        }
        this.hasSelectedAStorageType = getBooleanPref("hasSelectedAStorageType", false);
        this.loadDisabledModData = getBooleanPref("loadDisabledModData", false);
        this.lastModCount = getIntPref("lastModCount", -1);
        this.modSAFlinks = getStringPref("modSAFlinks", null);
        this.externalSAFWorking = getBooleanPref("externalSAFWorking", false);
        this.externalSAFLink = getStringPref("externalSAFLink", null);
        this.externalSAFPathShown = getStringPref("externalSAFPathShown", null);
        this.externalSAFPathExtra = getStringPref("externalSAFPathExtra", null);
        this.smartSelection_v2 = getBooleanPref("smartSelection_v2", true);
        this.mouseOrders = getIntPref("mouseOrders", 1);
        this.mousePlacement = getIntPref("mousePlacement", 1);
        this.autosaving = getBooleanPref("autosaving", true);
        if (this.settingsGameVersion < 174) {
            this.uiRenderScale = 1.0f;
        }
    }

    public String getValueDynamic(String str) {
        try {
            Field field = this.settingFields.get(str);
            if (field == null) {
                throw new RuntimeException("Could not find: " + str);
            }
            Object obj = field.get(this);
            if (obj == null) {
                return null;
            }
            return obj.toString();
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean setValueDynamic(String str, String str2) {
        try {
            Field field = this.settingFields.get(str);
            Object objValueOf = str2;
            if (field.getType().equals(Boolean.TYPE)) {
                if (str2 == null) {
                    throw new RuntimeException("value==null");
                }
                objValueOf = Boolean.parseBoolean(str2);
            }
            if (field.getType().equals(Float.TYPE)) {
                if (str2 != null && str2.contains(",")) {
                    str2 = str2.replace(",", ".");
                }
                objValueOf = Float.parseFloat(str2);
            }
            if (field.getType().equals(Integer.TYPE)) {
                objValueOf = Integer.parseInt(str2);
            }
            field.set(this, objValueOf);
            return true;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized boolean save() {
        this.settingsGameVersion = 176;
        if (this.prefs == null) {
            this.prefs = loadPreferenceStorage();
        }
        Preference editorEdit = this.prefs;
        if (editorEdit == null) {
            return true;
        }
        editorEdit.putInt("settingVersion", 2);
        editorEdit.putInt("settingsGameVersion", this.settingsGameVersion);
        editorEdit.putInt("settingsGameVersionFirst", this.settingsGameVersionFirst);
        editorEdit.putBoolean("upgradedToNoPublicStorage", this.upgradedToNoPublicStorage);
        editorEdit.putBoolean("upgradedToNoPublicStorageWarningShown", this.upgradedToNoPublicStorageWarningShown);
        editorEdit.putBoolean("hasPlayedGameOrSeenHelp", this.hasPlayedGameOrSeenHelp);
        editorEdit.putBoolean("enableSounds", this.enableSounds);
        editorEdit.putBoolean("enableMouseCapture", this.enableMouseCapture);
        editorEdit.putBoolean("androidNoSoundPrioritiesDebug", this.androidNoSoundPrioritiesDebug);
        editorEdit.putBoolean("disableDigitGrouping", this.disableDigitGrouping);
        Log.d("RustedWarfare", "put mv:" + this.musicVolume);
        editorEdit.putFloat("musicVolume", this.musicVolume);
        editorEdit.putFloat("masterVolume", this.masterVolume);
        editorEdit.putFloat("gameVolume", this.gameVolume);
        editorEdit.putFloat("interfaceVolume", this.interfaceVolume);
        editorEdit.putFloat("scrollSpeed", this.scrollSpeed);
        editorEdit.putFloat("edgeScrollSpeed", this.edgeScrollSpeed);
        editorEdit.putBoolean("onscreenControls", this.onscreenControls);
        editorEdit.putBoolean("trackpad", this.trackpad);
        editorEdit.putBoolean("dpad", this.dpad);
        editorEdit.putBoolean("batterySaving", this.batterySaving);
        editorEdit.putBoolean("highRefreshRate", this.highRefreshRate);
        editorEdit.putBoolean("slick2dFullScreen", this.slick2dFullScreen);
        editorEdit.putBoolean("renderVsync", this.renderVsync);
        editorEdit.putBoolean("renderSmoothDelta", this.renderSmoothDelta);
        editorEdit.putBoolean("unlockedScreenRotation", this.unlockedScreenRotation);
        editorEdit.putBoolean("renderBackground", this.renderBackground);
        editorEdit.putBoolean("renderExtraLayers", this.renderExtraLayers);
        editorEdit.putBoolean("renderControls", this.renderControls);
        editorEdit.putBoolean("immersiveFullScreen", this.immersiveFullScreen);
        editorEdit.putBoolean("displayOverCutout", this.displayOverCutout);
        editorEdit.putBoolean("renderDoubleScale", this.renderDoubleScale);
        editorEdit.putBoolean("showUnitGroups", this.showUnitGroups);
        editorEdit.putBoolean("renderWithLineWidth", this.renderWithLineWidth);
        editorEdit.putBoolean("renderClouds", this.renderClouds);
        editorEdit.putBoolean("softFogFading", this.softFogFading);
        editorEdit.putBoolean("showUnitWaypoints", this.showUnitWaypoints);
        editorEdit.putBoolean("useMinimapAllyColors", this.useMinimapAllyColors);
        editorEdit.putBoolean("showWarLogOnScreen", this.showWarLogOnScreen);
        editorEdit.putBoolean("classicInterface", this.classicInterface);
        editorEdit.putBoolean("quickRally", this.quickRally);
        editorEdit.putBoolean("doubleClickToAttackMove", this.doubleClickToAttackMove);
        editorEdit.putBoolean("showMapPingsOnBattlefield", this.showMapPingsOnBattlefield);
        editorEdit.putBoolean("showMapPingsOnMinimap", this.showMapPingsOnMinimap);
        editorEdit.putBoolean("showPlayerChatInGame", this.showPlayerChatInGame);
        editorEdit.putBoolean("allowGameRecording", this.allowGameRecording);
        editorEdit.putBoolean("showHp", this.showHp);
        editorEdit.putBoolean("showHpChanges", this.showHpChanges);
        editorEdit.putBoolean("showUnitIcons", this.showUnitIcons);
        editorEdit.putBoolean("gestureZoom", this.gestureZoom);
        editorEdit.putBoolean("useCircleSelect", this.useCircleSelect);
        editorEdit.putBoolean("showZoomButton", this.showZoomButton);
        editorEdit.putBoolean("showFps", this.showFps);
        editorEdit.putBoolean("newRender", this.newRender);
        editorEdit.putBoolean("shaderEffects", this.shaderEffects);
        editorEdit.putBoolean("teamShaders", this.teamShaders);
        editorEdit.putBoolean("sendReports", this.sendReports);
        editorEdit.putBoolean("shownAudioWarning", this.shownAudioWarning);
        editorEdit.putBoolean("mouseSupport", this.mouseSupport);
        editorEdit.putBoolean("keyboardSupport", this.keyboardSupport);
        editorEdit.putBoolean("forceEnglish", this.forceEnglish);
        editorEdit.putBoolean("saveMultiplayerReplays", this.saveMultiplayerReplays);
        editorEdit.putBoolean("replaysShowRecordedChat", this.replaysShowRecordedChat);
        editorEdit.putString("lastNetworkPlayerName", this.lastNetworkPlayerName);
        editorEdit.putString("lastNetworkIP", this.lastNetworkIP);
        editorEdit.putString("lastP2PRoomId", this.lastP2PRoomId);
        editorEdit.putString("lastDebugOption", this.lastDebugOption);
        editorEdit.putInt("aiDifficulty", this.aiDifficulty);
        editorEdit.putInt("locationDpad", this.locationDpad);
        editorEdit.putInt("locationAction", this.locationAction);
        editorEdit.putInt("keyAction", this.keyAction);
        editorEdit.putInt("keyJump", this.keyJump);
        editorEdit.putInt("keyLeft", this.keyLeft);
        editorEdit.putInt("keyRight", this.keyRight);
        editorEdit.putInt("keyDown", this.keyDown);
        editorEdit.putBoolean("landscapeOrientation", this.landscapeOrientation);
        editorEdit.putInt("networkPort", this.networkPort);
        editorEdit.putBoolean("udpInMultiplayer", this.udpInMultiplayer);
        editorEdit.putInt("banTimeInSecondsAfterKick", this.banTimeInSecondsAfterKick);
        editorEdit.putInt("numIncompleteLoadAttempts", this.numIncompleteLoadAttempts);
        editorEdit.putInt("numLoadsSinceRunningGameOrNormalExit", this.numLoadsSinceRunningGameOrNormalExit);
        editorEdit.putInt("numberOfWins", this.numberOfWins);
        editorEdit.putBoolean("rateGameShown", this.rateGameShown);
        editorEdit.putString("uuid", this.uuid);
        editorEdit.putString("networkClientId", this.networkClientId);
        editorEdit.putString("networkServerId", this.networkServerId);
        editorEdit.putInt("lastSeenMessageId", this.lastSeenMessageId);
        editorEdit.putString("lastSeenMessageIds", this.lastSeenMessageIds);
        editorEdit.putInt("nextBackgroundMap", this.nextBackgroundMap);
        editorEdit.putBoolean("showChatAndPingShortcuts", this.showChatAndPingShortcuts);
        editorEdit.putString("modSettings", this.modSettings);
        editorEdit.putInt("modSettingsVersion", this.modSettingsVersion);
        editorEdit.putInt("storageType", this.storageType);
        editorEdit.putBoolean("hasSelectedAStorageType", this.hasSelectedAStorageType);
        editorEdit.putBoolean("hadStoragePermissionInPast", this.hadStoragePermissionInPast);
        editorEdit.putInt("teamUnitCapSinglePlayer", this.teamUnitCapSinglePlayer);
        editorEdit.putInt("teamUnitCapHostedGame", this.teamUnitCapHostedGame);
        editorEdit.putBoolean("loadDisabledModData", this.loadDisabledModData);
        editorEdit.putInt("lastModCount", this.lastModCount);
        editorEdit.putString("modSAFlinks", this.modSAFlinks);
        editorEdit.putBoolean("externalSAFWorking", this.externalSAFWorking);
        editorEdit.putString("externalSAFLink", this.externalSAFLink);
        editorEdit.putString("externalSAFPathShown", this.externalSAFPathShown);
        editorEdit.putString("externalSAFPathExtra", this.externalSAFPathExtra);
        editorEdit.putBoolean("smartSelection_v2", this.smartSelection_v2);
        editorEdit.putInt("mouseOrders", this.mouseOrders);
        editorEdit.putInt("mousePlacement", this.mousePlacement);
        editorEdit.putBoolean("autosaving", this.autosaving);
        saveKeyBindingsToPreferences(editorEdit);
        PreferenceStorage bridge = org.koin.java.KoinJavaComponent.get(PreferenceStorage.class);
        bridge.flush();
        return true;
    }

    public void loadKeyBindingsFromPreferences() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine == null) {
            return;
        }
        loadKeyBindingsInto(gameEngine.inputController);
    }

    public void loadKeyBindingsInto(InputController inputController) {
        if (this.prefs == null) {
            this.prefs = loadPreferenceStorage();
        }
        if (this.prefs == null) {
            return;
        }
        if (inputController == null || inputController.al == null) {
            return;
        }
        for (KeyBinding keyBinding : inputController.al) {
            if (keyBinding.isDefault && !keyBinding.d()) {
                String strE = keyBinding.e();
                String string = this.prefs.getString(keyPreferenceName(strE), null);
                if (string != null && !VariableScope.nullOrMissingString.equals(string)) {
                    inputController.a(strE, string);
                }
            }
        }
    }

    public void saveKeyBindingsFromInputController(InputController inputController) {
        if (this.prefs == null) {
            this.prefs = loadPreferenceStorage();
        }
        if (this.prefs == null || inputController == null) {
            return;
        }
        saveKeyBindingsToPreferences(this.prefs, inputController);
        PreferenceStorage bridge = org.koin.java.KoinJavaComponent.get(PreferenceStorage.class);
        bridge.flush();
    }

    private void saveKeyBindingsToPreferences(Preference preference) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine == null) {
            return;
        }
        saveKeyBindingsToPreferences(preference, gameEngine.inputController);
    }

    private void saveKeyBindingsToPreferences(Preference preference, InputController inputController) {
        if (inputController == null || inputController.al == null) {
            return;
        }
        for (KeyBinding keyBinding : inputController.al) {
            if (keyBinding.isDefault && !keyBinding.d()) {
                preference.putString(keyPreferenceName(keyBinding.e()), inputController.a(keyBinding));
            }
        }
    }

    private static String keyPreferenceName(String str) {
        return "key." + str;
    }

    public boolean loadMainExternalFolder(boolean z) {
        if (!GameEngine.isAndroidPlatform()) {
            return false;
        }
        GameEngine.log("loadMainExternalFolder..");
        GameEngine gameEngine = GameEngine.getInstance();
        if (z && gameEngine.isExtraSafeModeActive()) {
            GameEngine.log("Not loading due to extra safe mode");
            return false;
        }
        String str = gameEngine.settingsEngine.externalSAFLink;
        String str2 = gameEngine.settingsEngine.externalSAFPathShown;
        String str3 = gameEngine.settingsEngine.externalSAFPathExtra;
        if (str == null) {
            GameEngine.log("No external folder set");
            return false;
        }
        GameEngine.log("External saf link: " + str);
        GameEngine.log("External saf shown path: " + str2);
        GameEngine.log("External saf extra: " + str3);
        String strA = SafFileLoader.a(str, true);
        GameEngine.log("safVirualPathBase: " + strA);
        if (strA == null) {
            gameEngine.settingsEngine.externalSAFWorking = false;
            return false;
        }
        String storageRoot = SafPlatformBridge.resolveStorageRoot(strA, str3);
        if (storageRoot == null) {
            GameEngine.log("Unable to read external SAF folder");
            gameEngine.settingsEngine.externalSAFWorking = false;
            return false;
        }
        FileHelper.overriddenExternalPath = storageRoot;
        gameEngine.settingsEngine.externalSAFWorking = true;
        return true;
    }
}
