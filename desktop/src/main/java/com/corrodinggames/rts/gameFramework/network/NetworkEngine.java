package com.corrodinggames.rts.gameFramework.network;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import com.corrodinggames.rts.appFramework.*;
import com.corrodinggames.rts.game.GameTeam;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.ai.AIController;
import com.corrodinggames.rts.game.map.TileMap;
import com.corrodinggames.rts.game.units.custom.ConfigParseException;
import com.corrodinggames.rts.game.units.custom.ConfigValidationException;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfigParser;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.gameFramework.Command;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.PerformanceProfiler;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.file.FileHelper;
import com.corrodinggames.rts.gameFramework.local.Locale;
import com.corrodinggames.rts.gameFramework.mod.ModInfo;
import com.corrodinggames.rts.gameFramework.steam.DisabledSteamEngine;
import com.corrodinggames.rts.gameFramework.ui.widgets.MenuDialog;
import com.corrodinggames.rts.gameFramework.ui.widgets.UIEvent;
import com.corrodinggames.rts.gameFramework.ui.widgets.UIEventHandler;
import com.corrodinggames.rts.gameFramework.utility.FastArrayList;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;
import net.rudp.ReliableSocket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.j.ad */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/j/ad.class */
public final class NetworkEngine {
    ArrayList f;
    /* JADX INFO: renamed from: g */
    public boolean debugLogging;
    public boolean i;
    public float j;
    public float k;
    /* JADX INFO: renamed from: m */
    public int networkPort;
    /* JADX INFO: renamed from: as */
    public static boolean forceIgnoreDesync;
    /* JADX INFO: renamed from: bl */
    public final Object queuedDisconnectLock = new Object();
    /* JADX INFO: renamed from: p */
    public boolean isSandboxMode;
    /* JADX INFO: renamed from: n */
    public String roomPassword;
    public boolean useMasterServer = true;
    /* JADX INFO: renamed from: s */
    public boolean allowJoinInProgress;
    public String u;

    /* JADX INFO: renamed from: y */
    public String playerName;
    private boolean bG;

    /* JADX INFO: renamed from: z */
    public PlayerTeam localPlayerTeam;
    public boolean A;

    /* JADX INFO: renamed from: C */
    public boolean isServer;
    public boolean D;
    public String E;
    public boolean G;

    /* JADX INFO: renamed from: H */
    public boolean isProxyController;
    /* JADX INFO: renamed from: o */
    public boolean requireActiveMods;
    /* JADX INFO: renamed from: q */
    public boolean publishToMasterServer;
    /* JADX INFO: renamed from: K */
    public Float forcedStepRateOverride;
    /* JADX INFO: renamed from: L */
    public String connectionQueryString;
    /* JADX INFO: renamed from: N */
    public boolean quickResyncRequested;
    /* JADX INFO: renamed from: O */
    public int stepRateSampleCount;
    /* JADX INFO: renamed from: P */
    public int slowStepRateSampleCount;

    /* JADX INFO: renamed from: S */
    public String serverUuid;
    /* JADX INFO: renamed from: Q */
    public int commandFrameInterval;
    /* JADX INFO: renamed from: R */
    public int commandFrameSendAhead;
    /* JADX INFO: renamed from: Y */
    public boolean frameUpdateBlocked;
    /* JADX INFO: renamed from: Z */
    public float allPlayersReadyCountdown;
    /* JADX INFO: renamed from: ab */
    public float allPlayersReadyWaitTimer;
    /* JADX INFO: renamed from: ac */
    public float allPlayersReadyReminderTimer;
    /* JADX INFO: renamed from: ad */
    public boolean catchupSpeedupActive;
    /* JADX INFO: renamed from: ae */
    public float catchupSpeedupTimer;
    public boolean ag;

    /* JADX INFO: renamed from: aj */
    public boolean pauseOnDesyncEnabled;

    /* JADX INFO: renamed from: ak */
    public boolean pausedOnDesync;

    /* JADX INFO: renamed from: al */
    public boolean gamePaused;

    /* JADX INFO: renamed from: an */
    public boolean syncChecksumSentForFrame;
    /* JADX INFO: renamed from: af */
    public boolean catchupFastForwardActive;
    /* JADX INFO: renamed from: ap */
    public int desyncCount;
    /* JADX INFO: renamed from: aq */
    public int desyncPassCount;
    /* JADX INFO: renamed from: ar */
    public int resyncSendOrReceiveCount;
    /* JADX INFO: renamed from: av */
    public boolean gameSetupReceived;
    /* JADX INFO: renamed from: aA */
    public GameInputStream receivedSaveGameStream;
    /* JADX INFO: renamed from: aB */
    public GameInputStream receivedCustomMapStream;
    /* JADX INFO: renamed from: bb */
    public boolean freeForAllMode;

    /* JADX INFO: renamed from: aD */
    Thread connectionAcceptorThread1;

    /* JADX INFO: renamed from: aE */
    ConnectionAcceptor connectionAcceptor1;

    /* JADX INFO: renamed from: aF */
    Thread connectionAcceptorThread2;

    /* JADX INFO: renamed from: aG */
    ConnectionAcceptor connectionAcceptor2;

    /* JADX INFO: renamed from: aH */
    Timer generalTimer;

    /* JADX INFO: renamed from: aI */
    KeepAliveTimer keepAliveTimer;

    /* JADX INFO: renamed from: aJ */
    Thread workerThread;

    /* JADX INFO: renamed from: aK */
    UdpDiscoveryHandler udpDiscoveryHandler;
    boolean aO;

    /* JADX INFO: renamed from: aS */
    String masterServerGameId;

    /* JADX INFO: renamed from: aT */
    public String publicIpAddress;

    /* JADX INFO: renamed from: aU */
    public Boolean publicPortOpen;

    /* JADX INFO: renamed from: aV */
    public Boolean publicIpLookupSuccess;

    /* JADX INFO: renamed from: aW */
    public boolean gameHasBeenStarted;

    /* JADX INFO: renamed from: ba */
    public float returnToBattleroomDelaySeconds;
    /* JADX INFO: renamed from: bc */
    public boolean startGameFailed;
    /* JADX INFO: renamed from: be */
    public boolean gameEndedByServer;
    public boolean bd;
    /* JADX INFO: renamed from: bj */
    public GameTeam spectatorGameTeam;
    public boolean bf;
    public String bg;
    /* JADX INFO: renamed from: bk */
    public GameTeam adminGameTeam;
    /* JADX INFO: renamed from: bx */
    public boolean reconnectDialogShown;
    /* JADX INFO: renamed from: v */
    public boolean chatOnlyMode = false;
    /* JADX INFO: renamed from: B */
    public volatile boolean networkGameActive = false;
    /* JADX INFO: renamed from: F */
    public boolean singleplayerServer = false;
    /* JADX INFO: renamed from: X */
    public int nextBlockingFrame = 0;

    /* JADX INFO: renamed from: bs */
    public long totalBytesSent;

    /* JADX INFO: renamed from: bt */
    public long totalBytesReceived;
    /* JADX INFO: renamed from: ai */
    public int checksumIntervalFrames = 300;

    /* JADX INFO: renamed from: bA */
    static ArrayList engineInstances;
    /* JADX INFO: renamed from: ao */
    public boolean desyncReportingEnabled = true;

    /* JADX INFO: renamed from: bF */
    SocketConnector socketConnector;
    public static final boolean a = false;
    public static boolean b = true;
    public static boolean c = false;
    public static boolean r = true;

    /* JADX INFO: renamed from: bE */
    public static PasswordHandler passwordHandler = new PasswordHandler();

    /* JADX INFO: renamed from: d */
    public NetworkCallbacks callbacks = new NetworkCallbacks();
    public int h = 25;
    public boolean l = false;

    /* JADX INFO: renamed from: t */
    public int udpPort = 5005;
    /* JADX INFO: renamed from: aw */
    public int currentUnitCap = 5;

    /* JADX INFO: renamed from: w */
    public long nextUnitId = 1;
    public boolean x = false;
    private boolean bH = false;
    /* JADX INFO: renamed from: ax */
    public int maxUnitCap = 5;
    /* JADX INFO: renamed from: az */
    public String selectedMapPath = null;
    public int I = 0;

    /* JADX INFO: renamed from: bI */
    private volatile float currentStepRate = 1.0f;
    public volatile float J = 1.0f;

    /* JADX INFO: renamed from: M */
    public ArrayList<BanEntry> banList = new ArrayList();
    public int T = -1;
    public int U = -1;
    public int V = -1;
    public int W = Utility.getRandomIntInRange(1, 9000000);
    /* JADX INFO: renamed from: aX */
    public boolean freeForAllModeChecked = false;

    /* JADX INFO: renamed from: ah */
    public int lastSyncedTick = -1;
    /* JADX INFO: renamed from: bm */
    public boolean queuedDisconnectRequested = false;

    /* JADX INFO: renamed from: am */
    public GameStateChecksum stateChecksum = new GameStateChecksum();
    /* JADX INFO: renamed from: aa */
    boolean allPlayersReady;
    float at = 0.0f;
    /* JADX INFO: renamed from: au */
    long playerUpdatePendingTimestamp;
    /* JADX INFO: renamed from: bn */
    float timeSinceLastResync;

    /* JADX INFO: renamed from: ay */
    public GameRoomSettings roomSettings = new GameRoomSettings();
    /* JADX INFO: renamed from: bo */
    float resyncDelayTimer;

    /* JADX INFO: renamed from: aC */
    public ChatLog chatLog = new ChatLog();

    /* JADX INFO: renamed from: aM */
    public ConcurrentLinkedQueue<NetworkConnection> sendQueue = new ConcurrentLinkedQueue();

    /* JADX INFO: renamed from: aN */
    ConcurrentLinkedQueue recvQueue = new ConcurrentLinkedQueue();
    /* JADX INFO: renamed from: bp */
    int resyncAttemptCount;

    /* JADX INFO: renamed from: aQ */
    Object sessionLock = new Object();
    /* JADX INFO: renamed from: bq */
    int lastResyncTick;

    /* JADX INFO: renamed from: aY */
    boolean returnToBattleroomPending = false;

    /* JADX INFO: renamed from: aZ */
    boolean returnToBattleroomCountdownActive = false;

    /* JADX INFO: renamed from: bh */
    public String masterServerErrorMessage = null;

    /* JADX INFO: renamed from: bi */
    public ConcurrentLinkedQueue<ServerInfo> discoveredServerList = new ConcurrentLinkedQueue();
    /* JADX INFO: renamed from: bD */
    Timer masterServerUpdateTimer;
    /* JADX INFO: renamed from: aP */
    volatile int nextConnectionId = 1;
    /* JADX INFO: renamed from: br */
    boolean quickResyncCommandPending = false;
    /* JADX INFO: renamed from: bu */
    boolean pathfindingPauseActive = false;

    /* JADX INFO: renamed from: bv */
    public Socket socket = null;

    /* JADX INFO: renamed from: bw */
    public String serverAddress = null;
    /* JADX INFO: renamed from: by */
    boolean networkClientIdMachineKeyChecked = false;

    /* JADX INFO: renamed from: bz */
    boolean registerConnectionSent = false;
    /* JADX INFO: renamed from: bB */
    boolean pendingMultiplayerChatNotification = false;

    /* JADX INFO: renamed from: bC */
    final Object connectionLock = new Object();
    public int e = GameEngine.getInstance().getVersionCode(true);

    /* JADX INFO: renamed from: aR */
    String sessionToken = Utility.getRandomAlphanumericString(40);

    /* JADX INFO: renamed from: aL */
    NetworkConnection localConnection = new NetworkConnection(this, null);

    /* JADX INFO: renamed from: a */
    public BanEntry getActiveBanForConnection(NetworkConnection networkConnection) {
        String ipAddress = networkConnection.getIpAddress();
        long jCurrentTimeMillis = System.currentTimeMillis();
        if (ipAddress == null) {
            networkConnection.logDebug("Is banned: No target");
            return null;
        }
        synchronized (this.banList) {
            for (BanEntry banEntry : this.banList) {
                if (ipAddress.equals(banEntry.ipAddress) && banEntry.expiryTimeMs > jCurrentTimeMillis) {
                    return banEntry;
                }
            }
            return null;
        }
    }

    /* JADX INFO: renamed from: a */
    public boolean banConnection(NetworkConnection networkConnection, String str, int i) {
        if (networkConnection == null) {
            GameEngine.logColored("Ban failed: No connection");
            return false;
        }
        String ipAddress = networkConnection.getIpAddress();
        if (ipAddress == null) {
            networkConnection.logDebug("Ban failed: No target");
            return false;
        }
        BanEntry banEntry = new BanEntry();
        banEntry.ipAddress = networkConnection.getIpAddress();
        banEntry.expiryTimeMs = System.currentTimeMillis() + ((long) (i * 1000));
        banEntry.reason = str;
        synchronized (this.banList) {
            pruneExpiredBans();
            this.banList.add(banEntry);
        }
        networkConnection.logInfo("Banned " + ipAddress + " for " + i + "s");
        return true;
    }

    /* JADX INFO: renamed from: a */
    public void clearBans() {
        synchronized (this.banList) {
            this.banList.clear();
        }
    }

    /* JADX INFO: renamed from: b */
    public void pruneExpiredBans() {
        synchronized (this.banList) {
            int i = 0;
            long jCurrentTimeMillis = System.currentTimeMillis();
            Iterator it = this.banList.iterator();
            while (it.hasNext()) {
                i++;
                boolean z = false;
                if (((BanEntry) it.next()).expiryTimeMs < jCurrentTimeMillis) {
                    z = true;
                }
                if (i > 1000) {
                    z = true;
                }
                if (z) {
                    it.remove();
                }
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public String setPlayerNameFromInput(String str) {
        String strReplace = str.trim().replace(" ", "_");
        this.playerName = strReplace;
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.playerName != null && !this.playerName.equals(gameEngine.settingsEngine.lastNetworkPlayerName)) {
            gameEngine.settingsEngine.lastNetworkPlayerName = this.playerName;
            gameEngine.settingsEngine.save();
        }
        return strReplace;
    }

    /* JADX INFO: renamed from: g */
    public static void reportDesync(String str) {
        reportDesyncVerbose(str, false);
    }

    /* JADX INFO: renamed from: c */
    public float getCurrentStepRate() {
        return this.currentStepRate;
    }

    /* JADX INFO: renamed from: d */
    public void resetSyncChecksumState() {
        this.lastSyncedTick = GameEngine.getInstance().currentTick;
        this.stateChecksum.computeChecksums();
        this.syncChecksumSentForFrame = false;
    }

    /* JADX INFO: renamed from: a */
    public void writeGameSetup(GameOutputStream gameOutputStream) throws IOException {
        GameEngine gameEngine = GameEngine.getInstance();
        gameOutputStream.writeByte(0);
        this.roomSettings.writeToStream(gameOutputStream);
        gameOutputStream.writeInt(gameEngine.currentUnitCap);
        gameOutputStream.writeInt(gameEngine.maxUnitCap);
    }

    public void a(GameInputStream gameInputStream) throws IOException {
        GameEngine gameEngine = GameEngine.getInstance();
        gameInputStream.readByte();
        this.roomSettings.readFromStream(gameInputStream);
        gameEngine.currentUnitCap = gameInputStream.readInt();
        gameEngine.maxUnitCap = gameInputStream.readInt();
    }

    public GameRoomSettings getEditableRoomSettings() {
        GameRoomSettings gameRoomSettingsC;
        if (this.isServer) {
            gameRoomSettingsC = this.roomSettings;
        } else if (this.isProxyController) {
            gameRoomSettingsC = this.roomSettings.clone();
        } else {
            gameRoomSettingsC = null;
            GameEngine.log("getChangeableSetup", "Clicked but not server or proxy controller");
        }
        return gameRoomSettingsC;
    }

    public void refreshAIDifficultyForTeams() {
        if (this.singleplayerServer) {
            GameEngine.getInstance().settingsEngine.aiDifficulty = this.roomSettings.aiDifficulty;
        }
        if (!this.isServer && !this.singleplayerServer) {
            return;
        }
        if (this.gameHasBeenStarted) {
            GameEngine.logWarningAndStack("updateAIDifficulty with gameHasBeenStarted=true");
        } else {
            for (int i = 0; i < PlayerTeam.TEAM_NEUTRAL; i++) {
                PlayerTeam playerTeamK = PlayerTeam.k(i);
                if (playerTeamK != null) {
                    a(playerTeamK);
                }
            }
        }
        updateAiTeamNames();
    }

    public void a(PlayerTeam playerTeam) {
        if (playerTeam.isTeamSpectator) {
            playerTeam.c("aiDifficultyOverride=" + playerTeam.teamAIDifficultyOverride);
            if (playerTeam.teamAIDifficultyOverride != null) {
                playerTeam.teamPingTime = playerTeam.teamAIDifficultyOverride.intValue();
            } else {
                playerTeam.teamPingTime = this.roomSettings.aiDifficulty;
            }
        }
    }

    /* JADX INFO: renamed from: b */
    public boolean updateAiTeamName(PlayerTeam playerTeam) {
        boolean z = false;
        if (playerTeam.isTeamSpectator) {
            String str = "AI - " + b(playerTeam.getTeamColorId());
            if (!str.equals(playerTeam.teamName)) {
                playerTeam.teamName = str;
                z = true;
            }
        }
        return z;
    }

    public void a(GameRoomSettings gameRoomSettings) {
        if (this.isServer) {
            refreshAIDifficultyForTeams();
            markPlayerUpdatePending();
            broadcastServerInfoToLargePacketConnections();
            MultiplayerBattleroomActivity.updateUI();
            return;
        }
        if (this.isProxyController) {
            b(gameRoomSettings);
        } else {
            GameEngine.log("applyChangedSetup but not server or proxy controller");
        }
    }

    private void b(GameRoomSettings gameRoomSettings) {
        GameEngine gameEngine = GameEngine.getInstance();
        GameEngine.log("applyProxyControl");
        GameRoomSettings gameRoomSettings2 = this.roomSettings;
        if (!gameRoomSettings2.mapPath.equals(gameRoomSettings.mapPath)) {
            gameEngine.networkEngine.k("-map '" + FileHelper.fixPath(LevelSelectActivity.getMapName(gameRoomSettings.mapPath)) + "'");
        }
        if (gameRoomSettings2.revealedMap != gameRoomSettings.revealedMap) {
            gameEngine.networkEngine.k("-revealedmap " + (!gameRoomSettings.revealedMap ? "true" : "false"));
        }
        if (gameRoomSettings2.fogMode != gameRoomSettings.fogMode) {
            gameEngine.networkEngine.k("-fog " + gameEngine.networkEngine.a(gameRoomSettings.fogMode));
        }
        if (gameRoomSettings2.startingCredits != gameRoomSettings.startingCredits) {
            gameEngine.networkEngine.k("-credits " + gameEngine.networkEngine.e(gameRoomSettings.startingCredits));
        }
        if (!Utility.approximatelyEqualStrict(gameRoomSettings2.incomeMultiplier, gameRoomSettings.incomeMultiplier)) {
            gameEngine.networkEngine.k("-income " + Utility.padString(gameRoomSettings.incomeMultiplier, 1));
        }
        if (gameRoomSettings2.noNukes != gameRoomSettings.noNukes) {
            gameEngine.networkEngine.k("-nukes " + (!gameRoomSettings.noNukes ? "true" : "false"));
        }
        if (gameRoomSettings2.aiDifficulty != gameRoomSettings.aiDifficulty) {
            gameEngine.networkEngine.k("-ai " + gameRoomSettings.aiDifficulty);
        }
        if (gameRoomSettings2.startingUnits != gameRoomSettings.startingUnits) {
            gameEngine.networkEngine.k("-startingunits " + gameRoomSettings.startingUnits);
        }
        if (gameRoomSettings2.sharedControl != gameRoomSettings.sharedControl) {
            gameEngine.networkEngine.k("-sharedControl " + (gameRoomSettings.sharedControl ? "true" : "false"));
        }
    }

    public String g() {
        if (this.roomSettings.fogMode == 0) {
            return "No fog";
        }
        if (this.roomSettings.fogMode == 1) {
            return "Basic fog";
        }
        if (this.roomSettings.fogMode == 2) {
            return "Line of Sight";
        }
        return "Unknown";
    }

    public String a(int i) {
        if (i == 0) {
            return "off";
        }
        if (i == 1) {
            return "basic";
        }
        if (i == 2) {
            return "los";
        }
        return "Unknown";
    }

    public String b(int i) {
        return c(i);
    }

    public String c(int i) {
        if (i == -2) {
            return "Very Easy";
        }
        if (i == -1) {
            return "Easy";
        }
        if (i == 0) {
            return "Medium";
        }
        if (i == 1) {
            return "Hard";
        }
        if (i == 2) {
            return "Very Hard";
        }
        if (i == 3) {
            return "Impossible";
        }
        return "Unknown";
    }

    public String h() {
        return d(this.roomSettings.startingUnits);
    }

    public ArrayList<Integer> i() {
        ArrayList arrayList = new ArrayList();
        for (int i = 1; i <= 4; i++) {
            arrayList.add(Integer.valueOf(i));
        }
        arrayList.addAll(CustomUnitConfig.getAllCustomUnitTypeIds());
        return arrayList;
    }

    public String d(int i) {
        if (i == 1) {
            return "Normal (1 builder)";
        }
        if (i == 2) {
            return "Small Army";
        }
        if (i == 3) {
            return "3 Engineers";
        }
        if (i == 4) {
            return "3 Engineers (No Command Center)";
        }
        if (i == 5) {
            return "Experimental Spider";
        }
        if (i == 9) {
            return "Custom";
        }
        CustomUnitConfig customUnitConfigC = CustomUnitConfig.c(i);
        if (customUnitConfigC != null) {
            return customUnitConfigC.getUnitName();
        }
        return "Unknown";
    }

    public String j() {
        if (this.roomSettings.startingCredits == 0) {
            return "Default ($" + k() + ")";
        }
        return "$" + k();
    }

    public final int k() {
        return e(this.roomSettings.startingCredits);
    }

    public int e(int i) {
        if (i == 0) {
            return 4000;
        }
        if (i == 1) {
            return 0;
        }
        if (i == 2) {
            return 1000;
        }
        if (i == 3) {
            return 2000;
        }
        if (i == 4) {
            return 5000;
        }
        if (i == 5) {
            return 10000;
        }
        if (i == 6) {
            return 50000;
        }
        if (i == 7) {
            return 100000;
        }
        if (i == 8) {
            return 200000;
        }
        return 999;
    }

    public String l() {
        return FileHelper.fixPath(this.selectedMapPath);
    }

    public void m() {
        new FastArrayList();
        Utility.clampTo255(SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_CONTENTS_MENU);
        MasterServerAuth.lerp(5.0f, 6.0f, 7.0f);
        PlatformHelper.a(5);
        this.bg = PlatformHelper.a();
        this.bf = true;
    }

    public boolean n() {
        return this.gameHasBeenStarted;
    }

    public boolean o() {
        return this.callbacks.e();
    }

    /* JADX INFO: renamed from: a */
    public synchronized void setPublicIpInfoResult(boolean z, String str, Boolean bool) {
        this.publicIpLookupSuccess = Boolean.valueOf(z);
        this.publicIpAddress = str;
        this.publicPortOpen = bool;
        MultiplayerBattleroomActivity.updateUI();
    }

    void a(ServerInfo serverInfo) {
        for (ServerInfo serverInfo2 : this.discoveredServerList) {
            if (serverInfo2.isLanServer && serverInfo2.publicHost.equals(serverInfo.publicHost) && serverInfo2.port == serverInfo.port) {
                serverInfo2.firstSeenTimeMs = p();
            }
        }
        serverInfo.firstSeenTimeMs = p();
        this.discoveredServerList.add(serverInfo);
        ServerListActivity.refreshUI();
    }

    public long p() {
        return System.currentTimeMillis();
    }

    public static Socket b(String str, boolean z) throws NetworkException, IOException {
        Socket reliableSocket;
        String str2;
        GameEngine gameEngine = GameEngine.getInstance();
        GameEngine.log("Connect to server: " + str + " (force tcp:" + z + ")");
        boolean z2 = false;
        String strTrim = str.trim();
        if (strTrim.startsWith("get|")) {
            String[] strArrSplit = strTrim.split("\\|");
            try {
                String str3 = strArrSplit[0];
                String str4 = strArrSplit[1];
                int i = Integer.parseInt(strArrSplit[2]);
                boolean z3 = Boolean.parseBoolean(strArrSplit[3]);
                Integer.parseInt(strArrSplit[4]);
                GameEngine.log("[relay-debug] master get string room=" + str4 + " port=" + i + " needsPassword=" + z3 + " token=" + str3);
                if (z3) {
                    gameEngine.networkEngine.roomPassword = null;
                    final Object obj = new Object();
                    PasswordHandler passwordHandler2 = new PasswordHandler() { // from class: com.corrodinggames.rts.gameFramework.j.ad.1
                        @Override // com.corrodinggames.rts.gameFramework.network.PasswordHandler
                        /* JADX INFO: renamed from: a */
                        public void submitPassword(String str5) {
                            GameEngine gameEngine2 = GameEngine.getInstance();
                            GameEngine.log("Entered password");
                            if (gameEngine2.networkEngine.isServer) {
                                GameEngine.logErrorColored("Cannot enter a password when we are a server");
                            } else {
                                gameEngine2.networkEngine.roomPassword = str5;
                            }
                            synchronized (obj) {
                                obj.notify();
                            }
                        }

                        @Override // com.corrodinggames.rts.gameFramework.network.PasswordHandler
                        /* JADX INFO: renamed from: a */
                        public void cancelPasswordEntry() {
                            synchronized (obj) {
                                obj.notify();
                            }
                        }
                    };
                    GameEngine.log("Asking for password..");
                    synchronized (obj) {
                        a(passwordHandler2);
                        try {
                            obj.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (gameEngine.networkEngine.roomPassword == null) {
                        GameEngine.logColored("No password entered");
                        throw new NetworkException();
                    }
                    GameEngine.log("Password has been entered");
                }
                String str5 = null;
                if (z3) {
                    str5 = gameEngine.networkEngine.roomPassword;
                    if (str5 == null) {
                        throw new IOException("This server requires a password but no password was provided");
                    }
                }
                final Object obj2 = new Object();
                ConnectionResult connectionResult = new ConnectionResult() { // from class: com.corrodinggames.rts.gameFramework.j.ad.2
                    @Override // com.corrodinggames.rts.gameFramework.network.ConnectionResult
                    /* JADX INFO: renamed from: a */
                    public void setResolvedAddress(String str6) {
                        super.setResolvedAddress(str6);
                        synchronized (obj2) {
                            obj2.notify();
                        }
                    }

                    @Override // com.corrodinggames.rts.gameFramework.network.ConnectionResult
                    /* JADX INFO: renamed from: a */
                    public void setError(String str6, ConnectionErrorType connectionErrorType, Exception exc) {
                        super.setError(str6, connectionErrorType, exc);
                        synchronized (obj2) {
                            obj2.notify();
                        }
                    }
                };
                synchronized (obj2) {
                    MasterServerClient.getGameServerInfoFromMasterServerAsync(connectionResult, str4, i, str5);
                    try {
                        obj2.wait(15000L);
                    } catch (InterruptedException e2) {
                    }
                }
                if (connectionResult.errorMessage != null) {
                    throw new IOException(connectionResult.errorMessage);
                }
                if (connectionResult.resolvedAddress == null) {
                    throw new IOException("Failed to get game server info.");
                }
                return b(connectionResult.resolvedAddress, z);
            } catch (NumberFormatException e3) {
                e3.printStackTrace();
                throw new IOException("Bad server connect string");
            }
        }
        if (strTrim.toLowerCase(java.util.Locale.ENGLISH).endsWith(".relay")) {
            strTrim = strTrim + ".corrodinggames.com";
        }
        if (strTrim.startsWith("[TCP]")) {
            strTrim = strTrim.substring("[TCP]".length());
            z = true;
        }
        if (strTrim.length() > 4 && !strTrim.contains(":") && !strTrim.contains(".") && !strTrim.equals("localhost") && !strTrim.contains("/") && !strTrim.contains("\\")) {
            String str6 = (VariableScope.nullOrMissingString + strTrim.charAt(0)) + ".relay.corrodinggames.com/" + strTrim;
            GameEngine.log("Converting connect string to: " + str6);
            strTrim = str6;
        }
        gameEngine.networkEngine.connectionQueryString = null;
        if (strTrim.contains("/") || strTrim.contains("\\")) {
            int iIndexOf = strTrim.indexOf("/");
            int iIndexOf2 = strTrim.indexOf("\\");
            if (iIndexOf == -1) {
                iIndexOf = strTrim.length();
            }
            if (iIndexOf2 == -1) {
                iIndexOf2 = strTrim.length();
            }
            int iMin = Utility.min(iIndexOf, iIndexOf2);
            String strTrim2 = strTrim.substring(iMin + 1).trim();
            if (!strTrim2.equals(VariableScope.nullOrMissingString)) {
                gameEngine.networkEngine.connectionQueryString = strTrim2;
            }
            strTrim = strTrim.substring(0, iMin);
        }
        String str7 = strTrim;
        int i2 = 5123;
        String[] strArrSplit2 = strTrim.split(":");
        if (strArrSplit2.length > 1) {
            str7 = null;
            for (int i3 = 0; i3 < strArrSplit2.length - 1; i3++) {
                if (str7 == null) {
                    str2 = VariableScope.nullOrMissingString;
                } else {
                    str2 = str7 + ":";
                }
                str7 = str2 + strArrSplit2[i3];
            }
            String str8 = strArrSplit2[strArrSplit2.length - 1];
            try {
                i2 = Integer.parseInt(str8);
            } catch (NumberFormatException e4) {
                String str9 = "Bad port number:" + str8;
                e4.printStackTrace();
                throw new IOException(str9);
            }
        }
        if (!z && gameEngine.networkEngine.isUdpMultiplayerEnabled()) {
            z2 = true;
        }
        int i4 = 7000;
        GameEngine.log(VariableScope.nullOrMissingString);
        GameEngine.log("===============================");
        GameEngine.log("Connect to: " + strTrim);
        if (!z2) {
            reliableSocket = new Socket();
            GameEngine.log("connecting to Server.. (tcp)");
        } else {
            reliableSocket = new ReliableSocket();
            GameEngine.log("connecting to Server.. (udp)");
            i4 = 5000;
        }
        reliableSocket.setTcpNoDelay(true);
        try {
            try {
                reliableSocket.connect(new InetSocketAddress(InetAddress.getByName(str7), i2), i4);
                return reliableSocket;
            } catch (UnknownHostException e5) {
                String str10 = "Failed to connect to host";
                if (z2) {
                    str10 = str10 + " (udp)";
                }
                GameEngine.log("UnknownHostException.." + str10);
                e5.printStackTrace();
                throw new IOException(str10, e5);
            } catch (IOException e6) {
                String str11 = "Failed to connect to host";
                if (z2) {
                    str11 = str11 + " (udp)";
                }
                String str12 = str11 + " - " + e6.getMessage();
                GameEngine.log("IOException.." + str12);
                e6.printStackTrace();
                throw new IOException(str12, e6);
            }
        } catch (IllegalArgumentException e7) {
            GameEngine.logColored("IllegalArgumentException..Incorrect server format");
            e7.printStackTrace();
            throw new IOException("Incorrect server format", e7);
        }
    }

    public NetworkEngine() {
        this.localConnection.allowLargeIncomingPackets = true;
        this.spectatorGameTeam = new GameTeam(-3, false);
        this.spectatorGameTeam.teamName = "SPECTATOR";
        this.adminGameTeam = new GameTeam(-1, false);
        this.adminGameTeam.teamName = "ADMIN";
    }

    public void q()  {
        a(false);
    }

    public void r()  {
        a(true);
    }

    /* JADX INFO: renamed from: s */
    public void resetNetworkGameState() {
        this.bH = false;
        this.bG = false;
        this.localPlayerTeam = null;
        this.isSandboxMode = false;
        this.totalBytesSent = System.currentTimeMillis();
        this.nextBlockingFrame = 0;
        this.I = 0;
        this.nextUnitId = 1L;
        applyChangedSetup(1.0f, "new");
        this.allPlayersReadyCountdown = 10.0f;
        this.quickResyncRequested = false;
        this.commandFrameInterval = 10;
        this.commandFrameSendAhead = 0;
        this.frameUpdateBlocked = false;
        this.allPlayersReady = false;
        this.gamePaused = false;
        this.pausedOnDesync = false;
        this.allPlayersReadyWaitTimer = 0.0f;
        this.allPlayersReadyReminderTimer = 0.0f;
        this.catchupSpeedupActive = false;
        this.catchupFastForwardActive = false;
        this.gameHasBeenStarted = false;
        this.returnToBattleroomPending = false;
        this.returnToBattleroomCountdownActive = false;
        this.returnToBattleroomDelaySeconds = 0.0f;
        this.freeForAllModeChecked = false;
        this.freeForAllMode = false;
        this.startGameFailed = false;
        this.bd = false;
        this.gameEndedByServer = false;
        this.ag = false;
        this.lastSyncedTick = -1;
        this.stateChecksum.totalChecksum = 0L;
        this.quickResyncCommandPending = false;
        this.stateChecksum.resetFields();
        this.syncChecksumSentForFrame = false;
        this.desyncReportingEnabled = true;
        this.desyncCount = 0;
        this.desyncPassCount = 0;
        this.resyncSendOrReceiveCount = 0;
        this.at = 0.0f;
        this.timeSinceLastResync = 0.0f;
        this.resyncDelayTimer = 0.0f;
        this.resyncAttemptCount = 0;
        this.lastResyncTick = -1000;
        MasterServerAuth.minClientVersion = 55;
        MasterServerAuth.minServerVersion = 66;
    }

    public void a(boolean z)  {
        this.networkGameActive = false;
        this.isServer = false;
        this.f = null;
        this.singleplayerServer = false;
        this.D = false;
        this.E = null;
        this.x = false;
        this.isProxyController = false;
        this.G = false;
        this.gameSetupReceived = false;
        this.A = false;
        resetNetworkGameState();
        this.serverUuid = null;
        this.networkPort = 0;
        this.i = false;
        this.j = 0.0f;
        this.k = 0.0f;
        this.registerConnectionSent = false;
        this.receivedCustomMapStream = null;
        this.maxUnitCap = GameEngine.getInstance().settingsEngine.teamUnitCapHostedGame;
        if (this.maxUnitCap < 1) {
            this.maxUnitCap = 1;
        }
        this.currentUnitCap = this.maxUnitCap;
        this.roomSettings.startingUnits = 1;
        this.roomSettings.incomeMultiplier = 1.0f;
        this.roomSettings.noNukes = false;
        this.roomSettings.j = false;
        this.roomSettings.sharedControl = false;
        this.roomSettings.startingCredits = 0;
        this.roomSettings.teamLock = false;
        this.roomSettings.fixedAllyTeams = false;
        this.roomSettings.allowSpectators = true;
        this.roomSettings.roomLock = false;
        this.roomSettings.randomSeed = 0;
        clearBans();
        this.chatLog.clearMessages();
        GameEngine.getInstance().gameUI.clearMessages();
        if ("<CHAT ONLY>".equals(this.roomSettings.mapPath)) {
            GameEngine.log("Chat only map selection - restarting");
            this.roomSettings.resetToDefaults();
        }
        if (!z) {
            PlayerTeam.resetTeamRegistry();
        }
        CustomUnitConfigParser.enableAllCustomUnits(this.requireActiveMods);
    }

    public void t() {
    }

    public boolean isReturnToBattleroomCountdownActive() {
        return this.returnToBattleroomCountdownActive;
    }

    public void u() {
        synchronized (this.queuedDisconnectLock) {
            if (this.networkGameActive) {
                this.queuedDisconnectRequested = true;
                try {
                    this.queuedDisconnectLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void b(NetworkConnection networkConnection) {
        this.sendQueue.remove(networkConnection);
    }

    private synchronized void removeDisconnectingConnections() {
        Iterator it = this.sendQueue.iterator();
        while (it.hasNext()) {
            if (((NetworkConnection) it.next()).isDisconnecting) {
                it.remove();
            }
        }
    }

    void a(byte[] bArr, NetworkConnection networkConnection) {
        if (!GameEngine.isDedicatedServer()) {
            Log.d("RustedWarfare", "Ignoring incoming resync tagged as debug only");
            return;
        }
        if (networkConnection.isDirectClient) {
            Log.d("RustedWarfare", "Ignoring desync client save, as past desync was already saved");
            return;
        }
        networkConnection.isDirectClient = true;
        Log.d("RustedWarfare", "Saving client save for debugging");
        File file = new File("desyncs/" + ("desync_" + Utility.formatCurrentDate("d MMM yyyy HH.mm.ss") + "_" + networkConnection.connectionId));
        file.getParentFile().mkdirs();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bArr);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void queueQuickResyncCommand() {
        if (this.quickResyncCommandPending) {
            return;
        }
        GameEngine.log("Adding quick resync command");
        GameEngine gameEngine = GameEngine.getInstance();
        Command commandCreateCommand = gameEngine.commandController.createCommand();
        commandCreateCommand.team = PlayerTeam.TEAM_ALL;
        commandCreateCommand.isSystemAction = true;
        commandCreateCommand.systemActionType = 200;
        gameEngine.networkEngine.a(commandCreateCommand);
        this.quickResyncCommandPending = true;
    }

    public void w() {
        GameEngine gameEngine = GameEngine.getInstance();
        GameOutputStream gameOutputStream = new GameOutputStream();
        try {
            gameEngine.gameSaver.writeSaveToStream(gameOutputStream);
            try {
                gameOutputStream.flushAllBuffers();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] byteArray = gameOutputStream.toByteArray();
            gameOutputStream.close();
            if (this.isServer) {
                for (NetworkConnection networkConnection : this.sendQueue) {
                    if (networkConnection.isForwarded) {
                        networkConnection.isForwarded = false;
                        networkConnection.isDirectServer = false;
                        a(networkConnection, byteArray, this.l, false);
                    }
                }
            }
            GameEngine.log("Loading quick resync save data (bytes:" + byteArray.length + ")");
            GameInputStream gameInputStream = new GameInputStream(byteArray);
            gameEngine.loadLevel("Game resync (quick)...", true);
            int i = gameEngine.currentTick;
            int i2 = gameEngine.gameTimeMillis;
            gameEngine.gameSaver.readSaveFromStream(gameInputStream, true, true, true);
            gameEngine.currentTick = i;
            gameEngine.gameTimeMillis = i2;
            this.nextBlockingFrame = gameEngine.currentTick + 1;
            this.ag = false;
            this.lastSyncedTick = this.nextBlockingFrame + 1;
            this.stateChecksum.totalChecksum = 0L;
            for (NetworkConnection networkConnection : this.sendQueue) {
                networkConnection.isDirectServer = false;
            }
            this.quickResyncCommandPending = false;
            this.resyncSendOrReceiveCount++;
            this.timeSinceLastResync = 0.0f;
            this.resyncDelayTimer = 0.0f;
            if (this.resyncAttemptCount < 1) {
                this.resyncAttemptCount++;
            }
            this.lastResyncTick = gameEngine.currentTick;
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }
    }

    public synchronized void x() {
        for (NetworkConnection networkConnection : this.sendQueue) {
            if (networkConnection.isForwarded) {
                throw new RuntimeException("Player: " + networkConnection.getPlayerDisplayName() + " has complete desync");
            }
            if (networkConnection.isDirectServer) {
                throw new RuntimeException("Player: " + networkConnection.getPlayerDisplayName() + " has minor desync");
            }
            if (networkConnection.syncMatchCount == 0) {
                throw new RuntimeException("Player: " + networkConnection.getPlayerDisplayName() + " has no sync matches");
            }
        }
    }

    /* JADX INFO: renamed from: e */
    private synchronized void updateDesyncResyncTimer(float f) {
        GameEngine.getInstance();
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        this.timeSinceLastResync += f;
        for (NetworkConnection networkConnection : this.sendQueue) {
            if (networkConnection.isForwarded) {
                z = true;
            }
            if (networkConnection.isDirectServer) {
                if (this.debugLogging) {
                    GameEngine.log("desync_count:" + networkConnection.desyncCount + " lastResyncTimer:" + this.timeSinceLastResync);
                }
                if (networkConnection.desyncCount < 4 || this.timeSinceLastResync > 3600.0f) {
                    z3 = true;
                }
            }
        }
        if (z3) {
            this.resyncDelayTimer += f;
            if (c && this.resyncDelayTimer > 5.0f) {
                z2 = true;
            }
            if (this.resyncAttemptCount == 0) {
                if (this.resyncDelayTimer > 60.0f) {
                    z2 = true;
                }
            } else if (this.resyncAttemptCount == 1) {
                if (this.resyncDelayTimer > 420.0f) {
                    z2 = true;
                }
            } else if (this.resyncAttemptCount == 2) {
                if (this.resyncDelayTimer > 3600.0f) {
                    z2 = true;
                }
            } else if (this.resyncAttemptCount == 3 && this.resyncDelayTimer > 14400.0f) {
                z2 = true;
            }
        }
        if (forceIgnoreDesync && z2) {
            GameEngine.log("disableDesyncFixing==true, running quick resync instead");
            z2 = false;
            z = true;
        }
        if (!z2 && z) {
            if (b) {
                queueQuickResyncCommand();
            } else {
                z2 = true;
            }
        }
        if (z2) {
            String str = VariableScope.nullOrMissingString;
            for (NetworkConnection networkConnection2 : this.sendQueue) {
                if (networkConnection2.isForwarded || networkConnection2.isDirectServer) {
                    if (!str.equals(VariableScope.nullOrMissingString)) {
                        str = str + ", ";
                    }
                    str = str + networkConnection2.getPlayerDisplayName();
                }
            }
            j("Resyncing game for " + str + "...");
            resetResyncTracking();
            a(this.l, false, true);
        }
    }

    private void resetResyncTracking() {
        GameEngine gameEngine = GameEngine.getInstance();
        this.timeSinceLastResync = 0.0f;
        this.resyncDelayTimer = 0.0f;
        this.resyncAttemptCount++;
        this.lastResyncTick = gameEngine.currentTick;
        for (NetworkConnection networkConnection : this.sendQueue) {
            networkConnection.isForwarded = false;
            networkConnection.isDirectServer = false;
            networkConnection.syncMatchCount = 0;
        }
    }

    public void c(String str) {
        q(str);
    }

    private void q(String str) {
        Iterator it = this.sendQueue.iterator();
        while (it.hasNext()) {
            ((NetworkConnection) it.next()).sendPacket(str);
        }
        this.sendQueue.clear();
        this.recvQueue.clear();
        this.nextConnectionId = 1;
        this.aO = false;
    }

    public long y() {
        if (0 != 0) {
            GameEngine.log("New id set:" + this.nextUnitId + 1);
            GameEngine.printStackTrace();
        }
        long j = this.nextUnitId;
        this.nextUnitId = j + 1;
        if (j == 0) {
            GameEngine.log("getNextUnitId: id==0");
            GameEngine.printStackTrace();
        }
        return j;
    }

    public long z() {
        return this.nextUnitId;
    }

    public void a(long j) {
        this.nextUnitId = j;
    }

    public boolean a(boolean z, int i) {
        for (NetworkConnection networkConnection : this.sendQueue) {
            if (networkConnection.allowLargeIncomingPackets && networkConnection.isConnected() && !networkConnection.isRelayServer && !networkConnection.D) {
                if (z) {
                    j("Still waiting on: " + networkConnection.getPlayerDisplayName());
                    return false;
                }
                return false;
            }
        }
        return true;
    }

    /* JADX INFO: renamed from: A */
    public void resetConnectionStatusFlags() {
        for (NetworkConnection networkConnection : this.sendQueue) {
            networkConnection.C = false;
            networkConnection.D = false;
        }
    }

    /* JADX INFO: renamed from: B */
    public int getPlayerConnectionCount() {
        int i = 0;
        for (NetworkConnection networkConnection : this.sendQueue) {
            if (networkConnection.allowLargeIncomingPackets && networkConnection.isConnected() && !networkConnection.isRelayServer) {
                i++;
            }
        }
        return i;
    }

    /* JADX INFO: renamed from: C */
    public int getConnectedPlayerCount() {
        ArrayList arrayList = new ArrayList();
        int i = 0;
        for (NetworkConnection networkConnection : this.sendQueue) {
            if (networkConnection.allowLargeIncomingPackets && networkConnection.isConnected() && !networkConnection.isRelayServer) {
                GameTeam gameTeam = networkConnection.player;
                if (gameTeam != null) {
                    if (!arrayList.contains(gameTeam)) {
                        arrayList.add(gameTeam);
                    }
                }
                i++;
            }
        }
        return i;
    }

    /* JADX INFO: renamed from: D */
    public int getRegisteredNonRelayConnectionCount() {
        int i = 0;
        for (NetworkConnection networkConnection : this.sendQueue) {
            if (networkConnection.allowLargeIncomingPackets && !networkConnection.isRelayServer) {
                i++;
            }
        }
        return i;
    }

    /* JADX INFO: renamed from: E */
    public int getPlayerCount() {
        int currentStepRate = 0 + getConnectedPlayerCount();
        if (!GameEngine.isDedicatedServer()) {
            currentStepRate++;
        }
        return currentStepRate;
    }

    public void d(String str) {
        Log.b("RustedWarfare", "network:" + str);
    }

    public static void e(String str) {
        GameEngine.log("network debug: " + str);
    }

    public void f(String str) {
        Log.d("RustedWarfare", "reportProblem:" + str);
        if (this.gameHasBeenStarted) {
            b((NetworkConnection) null, -1, (String) null, str);
        } else {
            b((NetworkConnection) null, -1, (String) null, str);
        }
    }

    /* JADX INFO: renamed from: h */
    public static void reportDesyncImportant(String str) {
        reportDesyncVerbose(str, true);
    }

    /* JADX INFO: renamed from: a */
    public static void reportDesyncVerbose(String str, boolean z) {
        String str2;
        NetworkEngine networkEngine = GameEngine.getInstance().networkEngine;
        String str3 = "desync:" + str;
        GameEngine.logColored(str3);
        GameEngine.printStackTrace();
        networkEngine.desyncCount++;
        if (networkEngine.desyncReportingEnabled) {
            if (networkEngine.desyncCount > 2 || forceIgnoreDesync) {
                z = true;
            }
            if (networkEngine.desyncCount > 10) {
                str2 = "<suppressing desync errors>";
                networkEngine.desyncReportingEnabled = false;
                z = true;
            } else {
                str2 = str3;
            }
            if (z) {
                str2 = "-i " + str2;
            }
            networkEngine.sendChatMessage(str2);
        }
    }

    /* JADX INFO: renamed from: a */
    public void applyChangedSetup(float f, String str) {
        if (f < 0.1d) {
            reportDesyncVerbose("setCurrentStepRate:" + f + " is too small, source:" + str, true);
        } else {
            this.currentStepRate = f;
        }
    }

    public static void a(String str, String str2) {
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.replayEngine.a(-1, str, str2, gameEngine.currentTick);
        if (gameEngine.gameUI != null && gameEngine.gameUI.messageManager != null) {
            gameEngine.gameUI.messageManager.addMessage(str, str2);
        } else {
            GameEngine.logWarningAndStack("interfaceEngine/messageInterface==null");
        }
    }

    /* JADX INFO: renamed from: F */
    public void updateAIDifficulty() {
    }

    public void a(Command command) {
        GameEngine gameEngine = GameEngine.getInstance();
        command.scheduledTick = this.nextBlockingFrame;
        command.prepareForNetworkTransfer();
        gameEngine.commandController.pendingCommands.add(command);
    }

    /* JADX INFO: renamed from: G */
    public void checkConnectionPings() {
        for (NetworkConnection networkConnection : this.sendQueue) {
            if (!networkConnection.allowLargeIncomingPackets || networkConnection.getRecentPingMs() == -2 || networkConnection.getRecentPingMs() > 500 || networkConnection.getRecentPingMs() < 0) {
            }
        }
    }

    public void showPlayerListPopup() {
        GameEngine gameEngine = GameEngine.getInstance();
        String str = VariableScope.nullOrMissingString;
        for (PlayerTeam playerTeam : PlayerTeam.getSortedTeams(true)) {
            if (playerTeam != null) {
                String str2 = "unnamed";
                if (playerTeam.teamName != null) {
                    str2 = playerTeam.teamName;
                }
                str = str + "•" + playerTeam.getTeamColorDisplayName().toLowerCase() + " [Team " + playerTeam.getTeamSlotLabel() + "] - " + str2 + (" " + playerTeam.getPlayerListTeamSuffix()) + "\n";
            }
        }
        GameEngine.log("showPlayerListPopup(): Showing playlist messagebox.");
        gameEngine.showMessageBox("Players", str);
    }

    public void a(float f) {
        GameEngine gameEngine = GameEngine.getInstance();
        this.at += f;
        if (this.returnToBattleroomCountdownActive) {
            if (this.returnToBattleroomDelaySeconds > 0.0f) {
                this.returnToBattleroomDelaySeconds -= f / 60.0f;
                GameEngine.getInstance().gameUI.showInfoMessageWithPriority("Returning to battleroom in " + ((int) this.returnToBattleroomDelaySeconds) + "...", 3500);
            } else {
                GameEngine.log("Sending returnToBattleroomEvent...");
                this.returnToBattleroomCountdownActive = false;
                sendReturnToBattleroomEvent((NetworkConnection) null);
            }
        }
        if (this.returnToBattleroomPending) {
            returnToBattleroom();
        }
        if (this.at > 60.0f) {
            checkConnectionPings();
            this.at = 0.0f;
        }
        if (this.gameHasBeenStarted && !this.freeForAllModeChecked) {
            this.freeForAllModeChecked = true;
            int i = 0;
            int i2 = 0;
            Iterator it = PlayerTeam.getTeamColorIds().iterator();
            while (it.hasNext()) {
                int playerCount = PlayerTeam.countPlayersWithTeamColor(((Integer) it.next()).intValue(), false);
                if (playerCount > i2) {
                    i2 = playerCount;
                }
                i++;
            }
            if (i > 2 && i2 <= 1) {
                this.freeForAllMode = true;
            }
        }
        if (!this.isServer && !this.bH) {
            sendClientStatusPacket();
            this.bH = true;
        }
        if (this.isServer) {
            if (!this.allPlayersReady && this.gameHasBeenStarted) {
                if (a(false, 0)) {
                    this.allPlayersReadyCountdown = Utility.moveTowardsZero(this.allPlayersReadyCountdown, f);
                    if (this.allPlayersReadyCountdown == 0.0f) {
                        this.allPlayersReady = true;
                        a(VariableScope.nullOrMissingString, "<All players ready>");
                        this.callbacks.onAllPlayersReady();
                    }
                } else {
                    this.allPlayersReadyWaitTimer += f;
                    this.allPlayersReadyReminderTimer += f;
                    if (this.allPlayersReadyWaitTimer > 900.0f) {
                        this.allPlayersReady = true;
                        a(VariableScope.nullOrMissingString, "Starting game without all players ready!");
                    } else if (this.allPlayersReadyReminderTimer > 180.0f) {
                        this.allPlayersReadyReminderTimer = 0.0f;
                        a(true, (int) ((900.0f - this.allPlayersReadyWaitTimer) / 60.0f));
                    }
                }
            }
            if (this.allPlayersReady) {
                boolean z = false;
                if (this.pausedOnDesync) {
                    z = true;
                }
                if (this.gamePaused) {
                    z = true;
                }
                if (gameEngine.currentTick >= this.nextBlockingFrame - this.commandFrameSendAhead && !z) {
                    int i3 = this.nextBlockingFrame + this.commandFrameInterval;
                    this.stepRateSampleCount++;
                    boolean z2 = false;
                    for (int i4 = 0; i4 < PlayerTeam.TEAM_NEUTRAL; i4++) {
                        PlayerTeam playerTeamK = PlayerTeam.k(i4);
                        if (playerTeamK != null && playerTeamK.teamColorIndex != 0 && !playerTeamK.isTeamDisconnected() && playerTeamK.teamColorIndex < 40) {
                            z2 = true;
                        }
                    }
                    if (gameEngine.getFps() != 0 && gameEngine.getFps() < 40 && !GameEngine.isDedicatedServer()) {
                        z2 = true;
                    }
                    if (z2) {
                        this.slowStepRateSampleCount++;
                    }
                    if (this.stepRateSampleCount > 8) {
                        float fFloatValue = 1.0f;
                        if (this.slowStepRateSampleCount > 4) {
                            fFloatValue = 2.0f;
                        }
                        if (this.forcedStepRateOverride != null) {
                            fFloatValue = this.forcedStepRateOverride.floatValue();
                        }
                        if (fFloatValue != getCurrentStepRate()) {
                            GameEngine.log("Changing step rate to " + fFloatValue);
                            Command commandCreateCommand = gameEngine.commandController.createCommand();
                            commandCreateCommand.team = PlayerTeam.TEAM_ALL;
                            commandCreateCommand.isSystemAction = true;
                            commandCreateCommand.gameSpeedChange = fFloatValue;
                            a(commandCreateCommand);
                        }
                        this.stepRateSampleCount = 0;
                        this.slowStepRateSampleCount = 0;
                    }
                    GameOutputStream gameOutputStream = new GameOutputStream();
                    try {
                        gameOutputStream.writeInt(i3);
                        int i5 = 0;
                        Iterator it2 = gameEngine.commandController.pendingCommands.iterator();
                        while (it2.hasNext()) {
                            if (((Command) it2.next()).scheduledTick == this.nextBlockingFrame) {
                                i5++;
                            }
                        }
                        gameOutputStream.writeInt(i5);
                        for (Command command : gameEngine.commandController.pendingCommands) {
                            if (command.scheduledTick == this.nextBlockingFrame) {
                                command.serializeCommand(gameOutputStream);
                            }
                        }
                        PacketData packetDataBuildPacketData = gameOutputStream.buildPacketData(10);
                        packetDataBuildPacketData.isUrgent = true;
                        d(packetDataBuildPacketData);
                        this.nextBlockingFrame = i3;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if (!gameEngine.commandController.queuedCommands.isEmpty()) {
            Iterator it3 = gameEngine.commandController.queuedCommands.iterator();
            while (it3.hasNext()) {
                Command command2 = (Command) it3.next();
                if (0 != 0) {
                    gameEngine.commandController.executedCommands.add(command2);
                    it3.remove();
                } else {
                    if (!command2.hasProcessedTargets) {
                        command2.precomputeCommandTargets();
                    }
                    if (command2.hasValidCommandTargetPaths()) {
                        gameEngine.commandController.executedCommands.add(command2);
                        it3.remove();
                    }
                }
            }
        }
        if (!this.isServer) {
            if (!gameEngine.commandController.executedCommands.isEmpty()) {
                for (Command command3 : gameEngine.commandController.executedCommands) {
                    if (!command3.isSystemCommand()) {
                        command3.applyCommandToUnits();
                        GameOutputStream gameOutputStream2 = new GameOutputStream();
                        try {
                            command3.serializeCommand(gameOutputStream2);
                            d(gameOutputStream2.buildPacketData(20));
                        } catch (IOException e2) {
                            throw new RuntimeException(e2);
                        }
                    }
                }
                gameEngine.commandController.executedCommands.clear();
            }
        } else if (!gameEngine.commandController.executedCommands.isEmpty()) {
            for (Command command4 : gameEngine.commandController.executedCommands) {
                if (!command4.isSystemCommand()) {
                    if (!command4.prepareAndValidateCommand()) {
                        reportDesync("Skipped command issued from server");
                    } else {
                        command4.applyCommandToUnits();
                        a(command4);
                    }
                }
            }
            gameEngine.commandController.executedCommands.clear();
        }
        while (!this.recvQueue.isEmpty()) {
            PacketData packetData = (PacketData) this.recvQueue.remove();
            try {
                processGamePacket(packetData);
            } catch (IOException e3) {
                String displayIpAddress = "None";
                NetworkConnection networkConnection = packetData.connection;
                if (networkConnection != null) {
                    displayIpAddress = networkConnection.getDisplayIpAddress();
                    String message = e3.getMessage();
                    if (message == null) {
                        message = "IO error";
                    }
                    networkConnection.sendPacket(message);
                    reportDesync("IO error on processGamePacket for " + networkConnection.getPlayerDisplayName());
                }
                GameEngine.log("Error on processGamePacket ip:" + displayIpAddress, (Throwable) e3);
            }
        }
        if (this.isServer) {
            if (!this.networkGameActive) {
                GameEngine.log("Skipping server updates, not networked");
            } else {
                removeDisconnectingConnections();
                if (!this.pauseOnDesyncEnabled) {
                    updateDesyncResyncTimer(f);
                }
            }
        }
        if (this.networkGameActive) {
            if (this.gamePaused) {
                gameEngine.gameUI.showMessageWithPriority("Game paused.", 100);
            } else {
                gameEngine.gameUI.showInfoMessage("Game paused.");
            }
        }
        if (gameEngine.currentTick < this.nextBlockingFrame) {
            this.frameUpdateBlocked = false;
        }
        if (this.queuedDisconnectRequested) {
            disconnectNetworking("queDisconnect");
        }
    }

    public void b(float f) {
        NetworkConnection networkConnectionW;
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine != null && !this.isServer && this.networkGameActive) {
            boolean z = false;
            for (NetworkConnection networkConnection : this.sendQueue) {
                if (networkConnection.allowLargeIncomingPackets && !networkConnection.isDisconnecting) {
                    z = true;
                }
            }
            if (this.gameEndedByServer && n()) {
                gameEngine.gameUI.showMediumPriorityMessage("Game ended by server.");
                MultiplayerBattleroomActivity.updateUI();
            } else if (!z && n()) {
                gameEngine.gameUI.showMediumPriorityMessage("Server Disconnected.");
                MultiplayerBattleroomActivity.updateUI();
            }
            if (z) {
                if ((this.frameUpdateBlocked || this.totalBytesSent + 1000 < System.currentTimeMillis()) && !this.isServer && (networkConnectionW = getActiveServerConnection()) != null && networkConnectionW.bytesReadTotalCurrentPacket > 20000) {
                    String str = "Receiving network data: " + networkConnectionW.bytesReadSoFar + "/" + networkConnectionW.bytesReadTotalCurrentPacket;
                    GameEngine.log(str);
                    gameEngine.gameUI.showDebugMessage(str);
                    if (!this.gameHasBeenStarted && this.totalBytesReceived + 4000 < System.currentTimeMillis()) {
                        this.totalBytesReceived = System.currentTimeMillis();
                        o(str);
                    }
                    a(networkConnectionW, networkConnectionW.bytesReadSoFar, networkConnectionW.bytesReadTotalCurrentPacket);
                }
            }
        }
    }

    /* JADX INFO: renamed from: c */
    public void update(float delta) throws IOException {
        GameEngine gameEngine = GameEngine.getInstance();
        this.totalBytesSent = System.currentTimeMillis();
        if (this.networkGameActive && (this.lastSyncedTick + this.checksumIntervalFrames < gameEngine.currentTick || this.lastSyncedTick == -1)) {
            resetSyncChecksumState();
            gameEngine.replayEngine.a(this.stateChecksum);
        }
        if ((this.networkGameActive || gameEngine.replayEngine.j()) && this.quickResyncRequested) {
            this.quickResyncRequested = false;
            w();
        }
        if (this.networkGameActive && this.isServer && !this.syncChecksumSentForFrame && this.lastSyncedTick + (this.checksumIntervalFrames / 2) < gameEngine.currentTick && this.lastSyncedTick != -1) {
            try {
                GameOutputStream gameOutputStream = new GameOutputStream();
                gameOutputStream.writeInt(this.lastSyncedTick);
                gameOutputStream.writeLong(this.stateChecksum.totalChecksum);
                gameOutputStream.writeInt(this.stateChecksum.fields.size());
                Iterator it = this.stateChecksum.fields.iterator();
                while (it.hasNext()) {
                    gameOutputStream.writeLong(((ChecksumField) it.next()).value);
                }
                h(gameOutputStream.buildPacketData(30));
                if (this.debugLogging) {
                    GameEngine.log("Sent checksum to client [" + this.lastSyncedTick + "]");
                }
                this.syncChecksumSentForFrame = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean shouldGameBePausedForPathfinding() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine.pathfindingEngine.e()) {
            if (!this.pathfindingPauseActive) {
                GameEngine.log("shouldGameBePaused: isGoingToBlockThisFrame()==true: " + gameEngine.pathfindingEngine.f());
            }
            this.pathfindingPauseActive = true;
            return true;
        }
        if (this.pathfindingPauseActive) {
            GameEngine.log("shouldGameBePaused: isGoingToBlockThisFrame()==false");
        }
        this.pathfindingPauseActive = false;
        return false;
    }

    public void a(float f, boolean z) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine.currentTick >= this.nextBlockingFrame) {
            if (gameEngine.currentTick > this.nextBlockingFrame) {
                throw new RuntimeException("game frame:" + gameEngine.currentTick + " is greater then nest step:" + this.nextBlockingFrame);
            }
            this.frameUpdateBlocked = true;
        }
        if (z && shouldGameBePausedForPathfinding()) {
            this.frameUpdateBlocked = true;
        }
    }

    /* JADX INFO: renamed from: b */
    public synchronized void disconnectNetworking(String str) {
        GameEngine gameEngine = GameEngine.getInstance();
        GameEngine.log("Disconnect: " + str);
        if (this.isServer) {
            stopMasterServerUpdateTimer();
            if (this.useMasterServer) {
                MasterServerClient.removeServerAsync();
            }
            if (this.connectionAcceptor1 != null) {
                this.connectionAcceptor1.stop();
                try {
                    if (this.connectionAcceptorThread1 != null) {
                        this.connectionAcceptorThread1.join();
                    }
                } catch (InterruptedException e) {
                }
                this.connectionAcceptor1 = null;
                this.connectionAcceptorThread1 = null;
            }
            if (this.connectionAcceptor2 != null) {
                this.connectionAcceptor2.stop();
                try {
                    if (this.connectionAcceptorThread2 != null) {
                        this.connectionAcceptorThread2.join();
                    }
                } catch (InterruptedException e2) {
                }
                this.connectionAcceptor2 = null;
                this.connectionAcceptorThread2 = null;
            }
            if (this.generalTimer != null) {
                this.generalTimer.cancel();
                this.generalTimer = null;
                this.keepAliveTimer = null;
            }
            if (this.udpDiscoveryHandler != null) {
                this.udpDiscoveryHandler.b();
                this.udpDiscoveryHandler = null;
                this.workerThread = null;
            }
        }
        q(str);
        DisabledSteamEngine.a().j();
        synchronized (this.queuedDisconnectLock) {
            this.networkGameActive = false;
            this.isServer = false;
            this.useMasterServer = true;
            this.singleplayerServer = false;
            this.f = null;
            try {
                wait(50L);
            } catch (InterruptedException e3) {
                e3.printStackTrace();
            }
            this.gameHasBeenStarted = false;
            gameEngine.replayEngine.e();
            gameEngine.stopAndReset();
            updateMultiplayerNotifications();
            this.queuedDisconnectRequested = false;
            this.queuedDisconnectLock.notifyAll();
        }
    }

    public synchronized boolean shouldFilterPacket(PacketData packet) {
        NetworkConnection networkConnection;
        if (this.isServer && (networkConnection = packet.connection) != null && !networkConnection.allowLargeIncomingPackets && packet.packetType != 105 && packet.packetType != 110 && packet.packetType != 111 && packet.packetType != 108 && packet.packetType != 160) {
            return true;
        }
        return false;
    }

    /* JADX INFO: renamed from: a */
    public void processGamePacket(PacketData packet) throws IOException {
        GameEngine gameEngine = GameEngine.getInstance();
        if (shouldFilterPacket(packet)) {
            d("filtered packet (type:" + packet.packetType + ")");
            return;
        }
        switch (packet.packetType) {
            case 10:
                if (this.isServer) {
                    d("we are a server! we don't follow orders");
                } else if (packet.connection.isRelayLinked) {
                    d("ignoring command");
                } else {
                    GameInputStream gameInputStream = new GameInputStream(packet);
                    int i = gameInputStream.readInt();
                    int i2 = gameInputStream.readInt();
                    for (int i3 = 0; i3 < i2; i3++) {
                        Command commandCreateCommand = gameEngine.commandController.createCommand();
                        commandCreateCommand.scheduledTick = this.nextBlockingFrame;
                        commandCreateCommand.deserializeCommand(gameInputStream);
                        a(commandCreateCommand);
                    }
                    if (i < this.nextBlockingFrame) {
                        reportDesync("New nextBlockingFrame:" + i + " is smaller than current step:" + this.nextBlockingFrame);
                    }
                    this.nextBlockingFrame = i;
                }
                break;
            case 20:
                if (!this.isServer) {
                    d("we are not a server! skipping");
                    break;
                } else {
                    GameInputStream gameInputStream2 = new GameInputStream(packet);
                    NetworkConnection networkConnection = packet.connection;
                    if (!networkConnection.isCommandRateLimitExceeded()) {
                        GameTeam gameTeam = networkConnection.player;
                        if (gameTeam == null) {
                            d("Player is null for message ADDCLIENTCOMMAND, skipping");
                        } else {
                            Command commandCreateCommand2 = gameEngine.commandController.createCommand();
                            commandCreateCommand2.deserializeCommand(gameInputStream2);
                            commandCreateCommand2.sourceTeam = gameTeam;
                            if (commandCreateCommand2.isSystemAction) {
                                d("Got system action from client, ignoring (" + networkConnection.connectionId + ")");
                                commandCreateCommand2.isSystemAction = false;
                            }
                            if (commandCreateCommand2.getTeam() == null) {
                                reportDesync("Invalid command from '" + gameTeam.teamName + "', no team found");
                            } else if (!commandCreateCommand2.prepareAndValidateCommand()) {
                                reportDesync("Ignored command from '" + gameTeam.teamName + "', check failed");
                            } else {
                                a(commandCreateCommand2);
                            }
                        }
                        break;
                    }
                }
                break;
            case 30:
                NetworkConnection networkConnection2 = packet.connection;
                GameInputStream gameInputStream3 = new GameInputStream(packet);
                int i4 = gameInputStream3.readInt();
                long j = gameInputStream3.readLong();
                if (this.ag) {
                    d("PACKET_SYNCCHECKSUM: skipping frame:" + i4 + ", we were told to wait for resync");
                } else {
                    GameOutputStream gameOutputStream = new GameOutputStream();
                    gameOutputStream.writeByte(0);
                    gameOutputStream.writeInt(i4);
                    gameOutputStream.writeInt(this.lastSyncedTick);
                    if (this.lastSyncedTick != i4 || this.stateChecksum.totalChecksum == 0) {
                        gameOutputStream.writeBoolean(false);
                        Log.d("RustedWarfare", "got remoteSyncFrame for:" + i4 + " needed:" + this.lastSyncedTick + " lastSyncCheckSum:" + this.stateChecksum.totalChecksum);
                    } else {
                        gameOutputStream.writeBoolean(true);
                        Log.d("RustedWarfare", "Running checksum");
                        gameOutputStream.writeLong(j);
                        gameOutputStream.writeLong(this.stateChecksum.totalChecksum);
                        boolean z = false;
                        if (j != this.stateChecksum.totalChecksum) {
                            reportDesync("Checksum doesn't match. Got:" + j + " expected:" + this.stateChecksum.totalChecksum);
                            z = true;
                            GameEngine.log("--- Desync for frame: " + i4 + " ---");
                            Iterator it = PlayerTeam.getTeams().iterator();
                            while (it.hasNext()) {
                                ((PlayerTeam) it.next()).hasTeamStatsCacheMismatch();
                            }
                        } else {
                            this.desyncPassCount++;
                        }
                        int i5 = gameInputStream3.readInt();
                        if (i5 != this.stateChecksum.fields.size()) {
                            Log.d("RustedWarfare", "checkSumSize!=syncCheckList.size()");
                        }
                        gameOutputStream.startBlock("checkList");
                        gameOutputStream.writeInt(i5);
                        gameOutputStream.writeInt(this.stateChecksum.fields.size());
                        for (ChecksumField checksumField : this.stateChecksum.fields) {
                            long j2 = gameInputStream3.readLong();
                            gameOutputStream.writeLong(j2);
                            gameOutputStream.writeLong(checksumField.value);
                            if (j2 != checksumField.value && checksumField.includeInTotalChecksum) {
                                reportDesync("[" + i4 + "] check(" + checksumField.label + "): " + j2 + "!=" + checksumField.value);
                                z = true;
                            }
                        }
                        gameOutputStream.endBlock("checkList");
                        gameOutputStream.writeBoolean(z);
                    }
                    if (!this.isServer) {
                        a(networkConnection2, gameOutputStream.buildPacketData(31));
                    }
                }
                break;
            case 31:
                if (!this.isServer) {
                    d("we are not a server, but got PACKET_SYNCCHECKSUM_STATUS");
                    break;
                } else {
                    NetworkConnection networkConnection3 = packet.connection;
                    GameInputStream gameInputStream4 = new GameInputStream(packet);
                    gameInputStream4.readByte();
                    int i6 = gameInputStream4.readInt();
                    int i7 = gameInputStream4.readInt();
                    if (gameInputStream4.readBoolean()) {
                        gameInputStream4.readLong();
                        gameInputStream4.readLong();
                        gameInputStream4.startBlockNamed("checkList");
                        gameInputStream4.readInt();
                        if (gameInputStream4.readInt() != this.stateChecksum.fields.size()) {
                            Log.d("RustedWarfare", "checkSumSize!=syncCheckList.size()");
                        }
                        for (ChecksumField checksumField2 : this.stateChecksum.fields) {
                            long j3 = gameInputStream4.readLong();
                            long j4 = gameInputStream4.readLong();
                            if (j3 != j4) {
                                GameEngine.logColored(checksumField2.label + " Checksum [" + i6 + "]. server:" + j3 + " client:" + j4);
                            }
                        }
                        gameInputStream4.d("checkList");
                        boolean z2 = gameInputStream4.readBoolean();
                        if (this.lastResyncTick >= i6) {
                            d("Not marking desync, already resynced before frame: " + this.lastResyncTick + "<=" + i6);
                            break;
                        } else {
                            if (!networkConnection3.isDirectServer && z2) {
                                networkConnection3.desyncCount++;
                            }
                            networkConnection3.isDirectServer = z2;
                            if (!z2) {
                                if (this.debugLogging) {
                                    GameEngine.log("checksum: client checksum match [" + i6 + "]");
                                }
                                networkConnection3.syncMatchCount++;
                                break;
                            } else {
                                GameEngine.log("client:" + networkConnection3.getPlayerDisplayName() + " desync [" + i6 + "]");
                                if (this.pauseOnDesyncEnabled && !this.pausedOnDesync) {
                                    reportDesync("pauseOnDesync is active, pausing");
                                    this.pausedOnDesync = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        if (this.debugLogging) {
                            GameEngine.log("checksum for:" + networkConnection3.getPlayerDisplayName() + " frameMatch==false client:" + i7 + " server:[" + i6 + "]");
                        }
                        break;
                    }
                }
                break;
            case 35:
                GameInputStream gameInputStream5 = new GameInputStream(packet);
                gameInputStream5.readByte();
                int i8 = gameInputStream5.readInt();
                int i9 = gameInputStream5.readInt();
                float f = gameInputStream5.readFloat();
                float f2 = gameInputStream5.readFloat();
                if (!this.isServer && f < 0.1d) {
                    reportDesyncVerbose("resync packet with setCurrentStepRate:" + f + " is too small", true);
                }
                NetworkConnection networkConnection4 = packet.connection;
                if (networkConnection4.isRelayLinked) {
                    d("ignoring resync command");
                } else {
                    boolean z3 = gameInputStream5.readBoolean();
                    if (gameInputStream5.readBoolean()) {
                        if (!this.isServer) {
                            d("we are not a server, but got a debug game save! skipping");
                        } else {
                            a(gameInputStream5.c("gameSave"), networkConnection4);
                        }
                    } else {
                        GameEngine.log("Reloading from network save");
                        if (z3 && !this.isServer) {
                            a(false, true, false);
                        }
                        byte[] bArrC = gameInputStream5.c("gameSave");
                        GameEngine.log("Save size: " + bArrC.length);
                        if (this.l) {
                            a(bArrC, networkConnection4);
                        }
                        gameEngine.replayEngine.a(bArrC, gameEngine.currentTick, i8, i9, f, f2);
                        GameInputStream gameInputStream6 = new GameInputStream(bArrC);
                        gameEngine.loadLevel("Resyncing game from server...", true);
                        gameEngine.gameSaver.readSaveFromStream(gameInputStream6, true, true, true);
                        gameEngine.clearCurrentLoadingStatus();
                        this.resyncSendOrReceiveCount++;
                        gameEngine.currentTick = i8;
                        gameEngine.gameTimeMillis = i9;
                        this.nextBlockingFrame = i8 + 1;
                        this.ag = false;
                        this.lastSyncedTick = this.nextBlockingFrame + 1;
                        this.stateChecksum.totalChecksum = 0L;
                        if (f < 0.1d) {
                            reportDesyncVerbose("resync setCurrentStepRate:" + f + " is too small", true);
                        }
                        applyChangedSetup(f, "rsync");
                        this.J = f2;
                    }
                }
                break;
            default:
                d("we did not handle packet:" + packet.packetType);
                break;
        }
    }

    public static String i(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() > 250) {
            str = str.substring(0, 250);
        }
        if (str.contains("\n")) {
            str = str.replace("\n", "?");
        }
        String strReplace = str.replace("\u0000", ".");
        boolean z = false;
        char[] charArray = strReplace.toCharArray();
        int length = charArray.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            if (!Character.isISOControl(charArray[i])) {
                i++;
            } else {
                z = true;
                break;
            }
        }
        if (z) {
            StringBuilder sb = new StringBuilder();
            for (char c2 : strReplace.toCharArray()) {
                if (!Character.isISOControl(c2)) {
                    sb.append(c2);
                }
            }
            strReplace = sb.toString();
        }
        return strReplace;
    }

    public void J() {
        GameEngine.getInstance().gameUI.interfaceRenderer.quitToMenu();
    }

    public void closeBattleroom() {
        b((String) null, (String) null);
    }

    public void b(String str, String str2) {
        GameEngine.log("closeBattleroom..");
        MultiplayerBattleroomActivity.finishActivity(str, str2);
        this.callbacks.d();
    }

    public synchronized void broadcastServerInfoToLargePacketConnections() {
        for (NetworkConnection networkConnection : this.sendQueue) {
            if (networkConnection.allowLargeIncomingPackets) {
                c(networkConnection);
            }
        }
    }

    public synchronized void c(NetworkConnection networkConnection) {
        if (!this.isServer) {
            d("sendServerInfo: we are not a server!");
            return;
        }
        GameOutputStream gameOutputStream = new GameOutputStream();
        try {
            gameOutputStream.writeStringUTF("com.corrodinggames.rts");
            gameOutputStream.writeInt(this.e);
            gameOutputStream.writeEnumOrdinal(this.roomSettings.gameModeType);
            if (this.chatOnlyMode) {
                gameOutputStream.writeStringUTF("<CHAT ONLY>");
            } else {
                gameOutputStream.writeStringUTF(this.roomSettings.mapPath == null ? "<NULL>" : FileHelper.fixPath(this.roomSettings.mapPath));
            }
            gameOutputStream.writeInt(this.roomSettings.startingCredits);
            gameOutputStream.writeInt(this.roomSettings.fogMode);
            gameOutputStream.writeBoolean(this.roomSettings.revealedMap);
            gameOutputStream.writeInt(this.roomSettings.aiDifficulty);
            gameOutputStream.writeByte(8);
            gameOutputStream.writeBoolean(this.callbacks.a(networkConnection));
            gameOutputStream.writeBoolean(this.callbacks.b(networkConnection));
            gameOutputStream.writeInt(this.currentUnitCap);
            gameOutputStream.writeInt(this.maxUnitCap);
            gameOutputStream.writeInt(this.roomSettings.startingUnits);
            gameOutputStream.writeFloat(this.roomSettings.incomeMultiplier);
            gameOutputStream.writeBoolean(this.roomSettings.noNukes);
            gameOutputStream.writeBoolean(this.roomSettings.j);
            if (this.chatOnlyMode) {
                gameOutputStream.writeBoolean(false);
            } else {
                gameOutputStream.writeBoolean(true);
                CustomUnitConfig.a(gameOutputStream);
            }
            gameOutputStream.writeBoolean(this.roomSettings.sharedControl);
            gameOutputStream.writeBoolean(this.roomSettings.teamLock);
            gameOutputStream.writeBoolean(this.roomSettings.fixedAllyTeams);
            gameOutputStream.writeBoolean(this.roomSettings.allowSpectators);
            gameOutputStream.writeBoolean(this.roomSettings.roomLock);
            gameOutputStream.writeInt(this.roomSettings.randomSeed);
            a(networkConnection, gameOutputStream.buildPacketData(106));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void a(NetworkConnection networkConnection, String str) {
        if (!this.isServer) {
            d("sendKick: we are not a server!");
            return;
        }
        d("kicking client reason:" + str);
        GameOutputStream gameOutputStream = new GameOutputStream();
        try {
            gameOutputStream.writeStringUTF(str);
            a(networkConnection, gameOutputStream.buildPacketData(150));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void d(NetworkConnection networkConnection) {
        if (!this.isServer) {
            d("sendIncorrectPassword: we are not a server!");
            return;
        }
        d("sendIncorrectPassword");
        GameOutputStream gameOutputStream = new GameOutputStream();
        try {
            gameOutputStream.writeInt(0);
            a(networkConnection, gameOutputStream.buildPacketData(113));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void refreshTeamSortAndAiGroups() {
        if (this.isServer) {
            for (int i = 0; i < PlayerTeam.TEAM_ENEMIES; i++) {
                PlayerTeam playerTeamK = PlayerTeam.k(i);
                if (playerTeamK != null) {
                    if (this.chatOnlyMode) {
                        playerTeamK.teamSortIndex = 0;
                    } else if (playerTeamK.isSpectatorTeamColor()) {
                        playerTeamK.teamSortIndex = 100;
                    } else {
                        playerTeamK.teamSortIndex = playerTeamK.teamColorId;
                    }
                    if (playerTeamK.isSpectatorTeamColor()) {
                        playerTeamK.assignedTeamColorIndex = -1;
                    } else {
                        int teamColorIndex = playerTeamK.getDefaultTeamColorIndex();
                        if (playerTeamK.playerColorOverride != null) {
                            teamColorIndex = playerTeamK.playerColorOverride.intValue();
                        } else if (a(teamColorIndex, (PlayerTeam) null)) {
                            teamColorIndex = -1;
                        }
                        playerTeamK.assignedTeamColorIndex = teamColorIndex;
                    }
                }
            }
            for (int i2 = 0; i2 < PlayerTeam.TEAM_ENEMIES; i2++) {
                PlayerTeam playerTeamK2 = PlayerTeam.k(i2);
                if (playerTeamK2 != null && playerTeamK2.assignedTeamColorIndex == -1 && !playerTeamK2.isSpectatorTeamColor()) {
                    playerTeamK2.assignedTeamColorIndex = findUnusedTeamColorIndex();
                }
            }
        }
    }

    public int findUnusedTeamColorIndex() {
        for (int i = 0; i < 10; i++) {
            if (!f(i)) {
                return i;
            }
        }
        return -1;
    }

    public boolean f(int i) {
        for (int i2 = 0; i2 < PlayerTeam.TEAM_ENEMIES; i2++) {
            PlayerTeam playerTeamK = PlayerTeam.k(i2);
            if (playerTeamK != null && playerTeamK.assignedTeamColorIndex == i && !playerTeamK.isSpectatorTeamColor()) {
                return true;
            }
        }
        return false;
    }

    public boolean a(int i, PlayerTeam playerTeam) {
        for (int i2 = 0; i2 < PlayerTeam.TEAM_ENEMIES; i2++) {
            PlayerTeam playerTeamK = PlayerTeam.k(i2);
            if (playerTeamK != null && playerTeamK != playerTeam && playerTeamK.playerColorOverride != null && playerTeamK.playerColorOverride.intValue() == i && !playerTeamK.isSpectatorTeamColor()) {
                return true;
            }
        }
        return false;
    }

    public void updateTeamConnectionStatuses() {
        if (this.isServer) {
            long jCurrentTimeMillis = System.currentTimeMillis();
            int i = GameEngine.getInstance().gameTimeMillis;
            if (this.localPlayerTeam != null && !this.D) {
                this.localPlayerTeam.teamNetworkId = -99;
                this.localPlayerTeam.teamLastPingTime = jCurrentTimeMillis;
            }
            refreshTeamSortAndAiGroups();
            for (int i2 = 0; i2 < PlayerTeam.TEAM_NEUTRAL; i2++) {
                PlayerTeam playerTeamK = PlayerTeam.k(i2);
                if (playerTeamK != null) {
                    playerTeamK.setHostTeam(this.localPlayerTeam == playerTeamK);
                    if (!this.gameHasBeenStarted) {
                    }
                    if (this.gameHasBeenStarted && !this.singleplayerServer && !playerTeamK.isTeamSpectator) {
                        boolean z = false;
                        if (playerTeamK.isTeamDisconnected()) {
                            z = true;
                        }
                        long j = 60000;
                        if (playerTeamK.teamPingCount > 180000) {
                            j = 160000;
                        }
                        boolean z2 = false;
                        if (this.allPlayersReady) {
                            if (playerTeamK.teamLastConnectionTime == -1) {
                                playerTeamK.teamLastConnectionTime = jCurrentTimeMillis;
                                playerTeamK.teamPingCount = i;
                            }
                            if ((this.pausedOnDesync || this.gamePaused) && !playerTeamK.isTeamAutoStartQueued) {
                                playerTeamK.teamLastConnectionTime = jCurrentTimeMillis;
                                playerTeamK.teamPingCount = i;
                            }
                            if (playerTeamK.teamLastConnectionTime + j < jCurrentTimeMillis) {
                                z2 = true;
                            }
                        }
                        if (playerTeamK.isTeamAutoStartQueued != z2) {
                            playerTeamK.isTeamAutoStartQueued = z2;
                        }
                        if (z2) {
                            z = true;
                            if (!playerTeamK.isTeamAutoStart) {
                                if (!(playerTeamK.isTeamWipedOut || playerTeamK.isTeamDefeatedTech || playerTeamK.isTeamNetworkActive || playerTeamK.isSpectatorTeamColor())) {
                                    playerTeamK.isTeamAutoStart = true;
                                }
                            }
                        }
                        if (playerTeamK.isTeamNetworkActive != z) {
                            if (z && !playerTeamK.isTeamWipedOut && !playerTeamK.isTeamDefeatedTech && !playerTeamK.isTeamConnectionActive && !playerTeamK.isSpectatorTeamColor()) {
                                String str = "-t [Sharing control due to disconnect]";
                                if (z2) {
                                    str = "-t [Sharing control due to afk]";
                                }
                                GameEngine.log(playerTeamK.teamName + " - " + str);
                                if (PlayerTeam.countPlayersWithTeamColor(playerTeamK.teamColorId, true) > 1) {
                                    a((NetworkConnection) null, playerTeamK, playerTeamK.teamName, str);
                                }
                            }
                            playerTeamK.isTeamNetworkActive = z;
                        }
                    }
                }
            }
        }
    }

    public void markPlayerUpdatePending() {
        if (this.playerUpdatePendingTimestamp == 0) {
            this.playerUpdatePendingTimestamp = System.currentTimeMillis();
        }
    }

    public void sendPlayerUpdateNow() {
        this.playerUpdatePendingTimestamp = 0L;
        e((NetworkConnection) null);
    }

    public void e(NetworkConnection networkConnection) {
        if (!this.isServer) {
            d("sendUpdatePlayer: we are not a server!");
            return;
        }
        updateTeamConnectionStatuses();
        for (NetworkConnection networkConnection2 : this.sendQueue) {
            if (networkConnection2.allowLargeIncomingPackets) {
                GameOutputStream gameOutputStream = new GameOutputStream(networkConnection2.networkVersion);
                try {
                    gameOutputStream.writeInt(networkConnection2.getPlayerTeamId());
                    int i = PlayerTeam.TEAM_NEUTRAL;
                    boolean z = false;
                    if (gameOutputStream.getStreamVersion() >= 90) {
                        boolean z2 = false;
                        if (gameOutputStream.getStreamVersion() >= 141) {
                            z2 = true;
                            if (this.gameHasBeenStarted && networkConnection2.hasSentFullTeamUpdate) {
                                z = true;
                            }
                            gameOutputStream.writeBoolean(z);
                        }
                        gameOutputStream.writeInt(i);
                        gameOutputStream.beginBlockInternal("teams", z2);
                    } else {
                        i = 8;
                        if (!this.chatOnlyMode) {
                            d("sendUpdatePlayer: warning saving with lower team count");
                        }
                    }
                    for (int i2 = 0; i2 < i; i2++) {
                        PlayerTeam playerTeamK = PlayerTeam.k(i2);
                        gameOutputStream.writeBoolean(playerTeamK != null);
                        if (playerTeamK != null) {
                            int i3 = 0;
                            if (playerTeamK instanceof AIController) {
                                i3 = 1;
                            }
                            gameOutputStream.writeInt(i3);
                            if (z) {
                                playerTeamK.writeNetworkTeamUpdate(gameOutputStream);
                            } else {
                                playerTeamK.writeBasicTeamState(gameOutputStream);
                            }
                        }
                    }
                    if (gameOutputStream.getStreamVersion() >= 90) {
                        gameOutputStream.endBlock("teams");
                    }
                    gameOutputStream.writeInt(this.roomSettings.fogMode);
                    gameOutputStream.writeInt(this.roomSettings.startingCredits);
                    gameOutputStream.writeBoolean(this.roomSettings.revealedMap);
                    gameOutputStream.writeInt(this.roomSettings.aiDifficulty);
                    gameOutputStream.writeByte(5);
                    gameOutputStream.writeInt(this.currentUnitCap);
                    gameOutputStream.writeInt(this.maxUnitCap);
                    gameOutputStream.writeInt(this.roomSettings.startingUnits);
                    gameOutputStream.writeFloat(this.roomSettings.incomeMultiplier);
                    gameOutputStream.writeBoolean(this.roomSettings.noNukes);
                    gameOutputStream.writeBoolean(this.roomSettings.j);
                    gameOutputStream.writeBoolean(false);
                    gameOutputStream.writeBoolean(this.roomSettings.sharedControl);
                    gameOutputStream.writeBoolean(this.gamePaused);
                    int i4 = -1;
                    if (networkConnection == networkConnection2 && networkConnection2.networkVersion <= 26) {
                        i4 = 1000;
                    }
                    networkConnection2.hasSentFullTeamUpdate = true;
                    a(networkConnection2, gameOutputStream.buildPacketData(115, i4));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void a(NetworkConnection networkConnection, int i, int i2) {
        GameOutputStream gameOutputStream = new GameOutputStream();
        try {
            gameOutputStream.writeByte(0);
            gameOutputStream.writeInt(i);
            gameOutputStream.writeInt(i2);
            a(networkConnection, gameOutputStream.buildPacketData(4));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized boolean startSandboxServer()  {
        if (startSingleplayerServer()) {
            this.isSandboxMode = true;
            this.roomSettings.fogMode = 0;
            return true;
        }
        return false;
    }

    public synchronized boolean startSingleplayerServer()  {
        if (this.networkGameActive) {
            disconnectNetworking("Started singleplayer");
        }
        GameEngine gameEngine = GameEngine.getInstance();
        r();
        this.networkGameActive = true;
        this.isServer = true;
        this.singleplayerServer = true;
        this.roomSettings.gameModeType = gameEngine.getGameModeType();
        this.roomSettings.mapPath = gameEngine.getCurrentMapFilename();
        regenerateOwnServerId();
        this.localPlayerTeam = gameEngine.playerTeam;
        MultiplayerBattleroomActivity.updateUI();
        this.networkPort = gameEngine.settingsEngine.networkPort;
        d("singleplayer server started");
        return true;
    }

    private void aA() {
        this.roomSettings.randomSeed = Utility.getRandomIntInRange(1, 1000000000);
    }

    /* JADX INFO: renamed from: c */
    public synchronized void handlePreregisterInfo(PacketData packet) throws IOException {
        int activeTeamCount;
        String nullableString;
        GameEngine gameEngine = GameEngine.getInstance();
        if (shouldFilterPacket(packet)) {
            d("filtered packet (type:" + packet.packetType + ")");
            return;
        }
        switch (packet.packetType) {
            case 4:
                NetworkConnection networkConnection = packet.connection;
                GameInputStream gameInputStream = new GameInputStream(packet);
                gameInputStream.readByte();
                gameInputStream.readInt();
                gameInputStream.readInt();
                return;
            case 105:
                d("got PACKET_GET_SERVER_INFO");
                if (!this.isServer) {
                    d("we are not a server! skipping");
                    return;
                }
                return;
            case 106:
                if (this.isServer) {
                    d("we are a server! we don't follow orders");
                    return;
                }
                GameInputStream gameInputStream2 = new GameInputStream(packet);
                NetworkConnection networkConnection2 = packet.connection;
                gameInputStream2.readUTF();
                gameInputStream2.readInt();
                this.roomSettings.gameModeType = (GameModeType) gameInputStream2.readEnumOrdinalOrNull(GameModeType.class);
                this.roomSettings.mapPath = gameInputStream2.readUTF();
                this.roomSettings.startingCredits = gameInputStream2.readInt();
                this.roomSettings.fogMode = gameInputStream2.readInt();
                this.roomSettings.revealedMap = gameInputStream2.readBoolean();
                this.roomSettings.aiDifficulty = gameInputStream2.readInt();
                byte b2 = gameInputStream2.readByte();
                this.G = gameInputStream2.readBoolean();
                this.isProxyController = gameInputStream2.readBoolean();
                this.gameSetupReceived = true;
                if (b2 >= 1) {
                    this.currentUnitCap = gameInputStream2.readInt();
                    this.maxUnitCap = gameInputStream2.readInt();
                }
                if (b2 >= 2) {
                    this.roomSettings.startingUnits = gameInputStream2.readInt();
                    this.roomSettings.incomeMultiplier = gameInputStream2.readFloat();
                    this.roomSettings.noNukes = gameInputStream2.readBoolean();
                    this.roomSettings.j = gameInputStream2.readBoolean();
                }
                if (b2 >= 3 && gameInputStream2.readBoolean()) {
                    try {
                        CustomUnitConfig.loadAndValidateCustomUnits(gameInputStream2);
                        this.x = true;
                    } catch (ConfigValidationException e) {
                        disconnectNetworking("Missing unit:" + e.getMessage() + " d:" + e.messageDetail);
                        b("Server sync mismatch", e.getMessage());
                        if (!GameEngine.isPC()) {
                            gameEngine.alert(e.getMessage());
                        }
                        String str = "Server sync mismatch";
                        if (e.a != null) {
                            str = e.a;
                        }
                        gameEngine.setPendingMessageBox(str, e.getMessage());
                        return;
                    }
                }
                if (b2 >= 4) {
                    this.roomSettings.sharedControl = gameInputStream2.readBoolean();
                }
                if (b2 >= 5) {
                    this.roomSettings.teamLock = gameInputStream2.readBoolean();
                }
                if (b2 >= 6) {
                    this.roomSettings.fixedAllyTeams = gameInputStream2.readBoolean();
                }
                if (b2 >= 7) {
                    this.roomSettings.allowSpectators = gameInputStream2.readBoolean();
                    this.roomSettings.roomLock = gameInputStream2.readBoolean();
                }
                if (b2 >= 8) {
                    this.roomSettings.randomSeed = gameInputStream2.readInt();
                }
                MultiplayerBattleroomActivity.updateUI();
                return;
            case 108:
                NetworkConnection networkConnection3 = packet.connection;
                GameInputStream gameInputStream3 = new GameInputStream(packet);
                long j = gameInputStream3.readLong();
                gameInputStream3.readByte();
                GameOutputStream gameOutputStream = new GameOutputStream();
                gameOutputStream.writeLong(j);
                gameOutputStream.writeByte(1);
                int fps = gameEngine.getFps();
                if (fps > 130) {
                    fps = 130;
                }
                gameOutputStream.writeByte(fps);
                a(networkConnection3, gameOutputStream.buildPacketData(109));
                return;
            case 109:
                if (!this.isServer) {
                    d("we are not a server! skipping");
                    return;
                }
                long jCurrentTimeMillis = System.currentTimeMillis();
                NetworkConnection networkConnection4 = packet.connection;
                GameInputStream gameInputStream4 = new GameInputStream(packet);
                long j2 = gameInputStream4.readLong();
                byte b3 = 0;
                if (gameInputStream4.readByte() >= 1) {
                    b3 = gameInputStream4.readByte();
                }
                int i = (int) (jCurrentTimeMillis - j2);
                networkConnection4.A = i;
                networkConnection4.B = jCurrentTimeMillis;
                if (networkConnection4.player != null) {
                    networkConnection4.player.teamNetworkId = i;
                    networkConnection4.player.teamLastPingTime = jCurrentTimeMillis;
                    networkConnection4.player.teamColorIndex = b3;
                }
                if (networkConnection4.trackLastPacket && this.isServer && this.D && this.localPlayerTeam != null) {
                    this.localPlayerTeam.teamNetworkId = i;
                    this.localPlayerTeam.teamLastPingTime = jCurrentTimeMillis;
                }
                if (!this.gameHasBeenStarted) {
                    MultiplayerBattleroomActivity.updateUI();
                    return;
                }
                return;
            case 110:
                d("got REGISTER_CONNECTION");
                if (!this.isServer) {
                    d("we are not a server! skipping");
                    return;
                }
                GameInputStream gameInputStream5 = new GameInputStream(packet);
                NetworkConnection networkConnection5 = packet.connection;
                gameInputStream5.readUTF();
                int i2 = gameInputStream5.readInt();
                int i3 = gameInputStream5.readInt();
                int i4 = gameInputStream5.readInt();
                String utf = gameInputStream5.readUTF();
                String nullableString2 = gameInputStream5.readNullableString();
                String utf2 = null;
                networkConnection5.networkVersion = i3;
                if (i2 >= 1) {
                    networkConnection5.connectionLabel = gameInputStream5.readUTF();
                }
                if (i2 >= 2) {
                    utf2 = gameInputStream5.readUTF();
                }
                int i5 = -1;
                if (i2 >= 3) {
                    i5 = gameInputStream5.readInt();
                }
                String utf3 = "MISSING";
                if (i2 >= 4) {
                    utf3 = gameInputStream5.readUTF();
                }
                String utf4 = VariableScope.nullOrMissingString;
                if (i2 >= 5) {
                    utf4 = gameInputStream5.readUTF();
                }
                if (utf.length() > 20) {
                    a(networkConnection5, "Your username is too long");
                    networkConnection5.sendPacket("kicked");
                    return;
                }
                String strP = p(utf);
                if (strP.length() < 2) {
                    a(networkConnection5, "Your username is too short");
                    networkConnection5.sendPacket("kicked");
                    return;
                }
                GameTeam existingGameTeam = null;
                if (utf2 != null) {
                    existingGameTeam = PlayerTeam.findGameTeamBySharedControlId(utf2);
                    if (existingGameTeam != null) {
                        d("Existing player: " + existingGameTeam.teamId + " - " + existingGameTeam.teamName);
                    }
                }
                BanEntry activeBan = getActiveBanForConnection(networkConnection5);
                if (activeBan != null) {
                    GameEngine.log("Connection banned for " + activeBan.getRemainingSeconds() + " more seconds");
                    a(networkConnection5, activeBan.getReasonText());
                    networkConnection5.sendPacket("kicked");
                    return;
                }
                String strA = this.callbacks.a(networkConnection5, strP, i3, i4, networkConnection5.connectionLabel, existingGameTeam);
                if (strA != null) {
                    a(networkConnection5, strA);
                    networkConnection5.sendPacket("kicked");
                    return;
                }
                if (i3 < this.e && !this.chatOnlyMode) {
                    a(networkConnection5, "Game is out of date, please update to v" + gameEngine.getVersion());
                    networkConnection5.sendPacket("kicked");
                    return;
                }
                if (i3 > this.e && !this.chatOnlyMode) {
                    a(networkConnection5, "Your client is newer then the server. Server is on: v" + gameEngine.getVersion());
                    networkConnection5.sendPacket("kicked");
                    return;
                }
                if (!this.chatOnlyMode && i5 != gameEngine.getAllUnitsChecksum()) {
                    GameEngine.log("New Player kicked: Unit checksum mismatch: clientUnitsChecksum=" + i5 + " game.getAllUnitsChecksum():" + gameEngine.getAllUnitsChecksum());
                    a(networkConnection5, "Your core units are different to the server's core units. Game can not be synchronized");
                    networkConnection5.sendPacket("kicked");
                    return;
                }
                if (!this.chatOnlyMode) {
                    String strG = g(networkConnection5.sessionRandomId);
                    if (!strG.equals(utf3)) {
                        GameEngine.log("New Player kicked: Integrity Check Failed: expectedResponse=" + strG + " clientResponse=" + utf3);
                        a(networkConnection5, "Your 'Rusted Warfare' client is different to the server. Game can not be synchronized.");
                        networkConnection5.sendPacket("kicked");
                        return;
                    }
                }
                if (!this.gameHasBeenStarted && this.roomSettings.roomLock) {
                    a(networkConnection5, "Room is locked. New players cannot join this server.");
                    networkConnection5.sendPacket("kicked");
                    return;
                }
                if (this.gameHasBeenStarted && existingGameTeam == null && !this.allowJoinInProgress) {
                    a(networkConnection5, "A game has already been started on this server");
                    networkConnection5.sendPacket("kicked");
                    return;
                }
                if (this.roomPassword != null && existingGameTeam == null && !Utility.sha256Hex(this.roomPassword).equals(nullableString2)) {
                    if (nullableString2 == null) {
                        GameEngine.log("processSystemPacket", "Player tried to join but needs a password");
                    } else {
                        GameEngine.log("processSystemPacket", "Player tried to join but had an incorrect password");
                    }
                    d(networkConnection5);
                    return;
                }
                if (!h(this.W).equals(utf4)) {
                    networkConnection5.logInfo("no extra");
                    networkConnection5.noExtraChecks = true;
                }
                if (networkConnection5.player == null) {
                    synchronized (this.connectionLock) {
                        if (existingGameTeam == null) {
                            activeTeamCount = PlayerTeam.getFirstFreePlayerTeamId();
                        } else {
                            activeTeamCount = existingGameTeam.teamId;
                        }
                        if (activeTeamCount == -1 && !this.chatOnlyMode) {
                            a(networkConnection5, "No free slots on server");
                            networkConnection5.sendPacket("no free slots");
                            return;
                        }
                        String strA2 = this.callbacks.a(networkConnection5, strP);
                        if (strA2 != null) {
                            a(networkConnection5, strA2);
                            networkConnection5.sendPacket("kicked");
                        } else {
                            MasterServerAuth.applyHandshakeTimeoutFlag(networkConnection5);
                            if (!this.chatOnlyMode && networkConnection5.handshakeTimeoutReached) {
                                a(networkConnection5, VariableScope.nullOrMissingString);
                                networkConnection5.sendPacket("kicked");
                                return;
                            }
                            String str2 = null;
                            if (existingGameTeam != null) {
                                networkConnection5.player = existingGameTeam;
                                String str3 = VariableScope.nullOrMissingString;
                                if (this.gameHasBeenStarted) {
                                    if (existingGameTeam.isSpectatorTeamColor()) {
                                        str3 = " (Spectator)";
                                    } else {
                                        str3 = " (Team " + existingGameTeam.getTeamSlotLabel() + ")";
                                    }
                                }
                                j("'" + networkConnection5.player.teamName + "' reconnected. " + str3);
                                networkConnection5.isForwarded = true;
                                str2 = existingGameTeam.teamName;
                                existingGameTeam.teamAIHint = networkConnection5.remoteId;
                            } else {
                                if (this.chatOnlyMode && activeTeamCount == -1) {
                                    networkConnection5.player = new GameTeam(-3);
                                } else {
                                    networkConnection5.player = new GameTeam(activeTeamCount);
                                    networkConnection5.player.teamColorId = activeTeamCount % 2;
                                }
                                if (this.gameHasBeenStarted && this.allowJoinInProgress) {
                                    networkConnection5.isForwarded = true;
                                }
                            }
                            if (existingGameTeam == null && strP != null) {
                                ArrayList activePlayerTeams = getActivePlayerTeamsSnapshot();
                                for (int i6 = 0; i6 < 10; i6++) {
                                    boolean z = false;
                                    String str4 = strP;
                                    if (i6 > 0) {
                                        str4 = str4 + "(" + i6 + ")";
                                    }
                                    Iterator it = activePlayerTeams.iterator();
                                    while (it.hasNext()) {
                                        if (str4.equalsIgnoreCase(((PlayerTeam) it.next()).teamName)) {
                                            z = true;
                                        }
                                    }
                                    if (!z) {
                                        strP = str4;
                                        break;
                                    }
                                }
                            }
                            networkConnection5.player.teamName = strP;
                            networkConnection5.player.teamSharedControlType = utf2;
                            networkConnection5.player.teamAIHint = networkConnection5.remoteId;
                            networkConnection5.networkVersion = i3;
                            GameEngine.log("processSystemPacket", "New player: " + strP + ", networkVersion:" + networkConnection5.networkVersion + " existing:" + (existingGameTeam != null));
                            networkConnection5.allowLargeIncomingPackets = true;
                            if (existingGameTeam == null) {
                                this.callbacks.a(networkConnection5.player);
                            }
                            MultiplayerBattleroomActivity.updateUI();
                            e(networkConnection5);
                            c(networkConnection5);
                            this.callbacks.c(networkConnection5, strP, str2);
                            if ((existingGameTeam != null || this.allowJoinInProgress) && this.gameHasBeenStarted) {
                                a(networkConnection5, true);
                            }
                        }
                        return;
                    }
                }
                GameEngine.log("processSystemPacket", "This connection already has a player");
                return;
            case 111:
                GameInputStream gameInputStream6 = new GameInputStream(packet);
                NetworkConnection networkConnection6 = packet.connection;
                String utf5 = null;
                try {
                    utf5 = gameInputStream6.readUTF();
                    break;
                } catch (IOException e2) {
                    GameEngine.log("Error reading disconnect reason", (Throwable) e2);
                }
                d("Got a disconnect packet:" + utf5);
                if (networkConnection6 != null) {
                    networkConnection6.handleRemoteDisconnect(false, false, utf5);
                }
                if (!this.isServer) {
                }
                return;
            case 112:
                if (!this.isServer) {
                    d("we are not a server! skipping");
                    return;
                }
                NetworkConnection networkConnection7 = packet.connection;
                GameInputStream gameInputStream7 = new GameInputStream(packet);
                networkConnection7.C = gameInputStream7.readBoolean();
                networkConnection7.D = gameInputStream7.readBoolean();
                return;
            case 113:
                if (this.isServer) {
                    d("we are a server! skipping: " + packet.packetType);
                    return;
                } else {
                    a(passwordHandler);
                    return;
                }
            case 115:
                if (this.isServer) {
                    d("we are a server! we don't follow orders");
                    return;
                }
                GameInputStream gameInputStream8 = new GameInputStream(packet);
                gameInputStream8.setStreamVersion(packet.connection.networkVersion);
                NetworkConnection networkConnection8 = packet.connection;
                int i7 = gameInputStream8.readInt();
                PlayerTeam playerTeam = null;
                int i8 = 8;
                boolean z2 = false;
                if (gameInputStream8.getStreamVersion() >= 90) {
                    boolean z3 = false;
                    if (gameInputStream8.getStreamVersion() >= 141) {
                        z3 = true;
                        z2 = gameInputStream8.readBoolean();
                    }
                    i8 = gameInputStream8.readInt();
                    PlayerTeam.setMaxTeamId(i8, false);
                    gameInputStream8.a("teams", z3);
                    if (i8 > PlayerTeam.TEAM_NEUTRAL) {
                        throw new IOException("Cannot load:" + i8 + " teams");
                    }
                } else if (this.gameHasBeenStarted) {
                    reportDesync("Warning old team system used in started game, stream version:" + gameInputStream8.getStreamVersion());
                }
                for (int i9 = 0; i9 < i8; i9++) {
                    PlayerTeam playerTeamK = PlayerTeam.k(i9);
                    if (!gameInputStream8.readBoolean()) {
                        if (playerTeamK != null) {
                            if (this.gameHasBeenStarted) {
                                reportDesync("Warning team:" + i9 + " removed while game is running");
                            }
                            playerTeamK.removeFromTeamRegistry();
                        }
                    } else {
                        gameInputStream8.readInt();
                        if (playerTeamK == null) {
                            if (this.gameHasBeenStarted) {
                                reportDesync("Warning team:" + i9 + " added while game is running");
                            }
                            if (!this.isServer && (playerTeamK instanceof AIController)) {
                                reportDesync("Warning we are a client with an AI team");
                            }
                            playerTeamK = new GameTeam(i9);
                        }
                        if (z2) {
                            playerTeamK.readNetworkTeamUpdate(gameInputStream8);
                        } else {
                            playerTeamK.readBasicTeamState(gameInputStream8, this.gameHasBeenStarted);
                        }
                    }
                    if (playerTeamK != null && playerTeamK.teamId == i7) {
                        playerTeam = playerTeamK;
                    }
                }
                if (gameInputStream8.getStreamVersion() >= 90) {
                    gameInputStream8.d("teams");
                }
                this.localPlayerTeam = playerTeam;
                this.roomSettings.fogMode = gameInputStream8.readInt();
                this.roomSettings.startingCredits = gameInputStream8.readInt();
                this.roomSettings.revealedMap = gameInputStream8.readBoolean();
                this.roomSettings.aiDifficulty = gameInputStream8.readInt();
                byte b4 = gameInputStream8.readByte();
                this.currentUnitCap = gameInputStream8.readInt();
                this.maxUnitCap = gameInputStream8.readInt();
                if (b4 >= 2) {
                    this.roomSettings.startingUnits = gameInputStream8.readInt();
                    this.roomSettings.incomeMultiplier = gameInputStream8.readFloat();
                    this.roomSettings.noNukes = gameInputStream8.readBoolean();
                    this.roomSettings.j = gameInputStream8.readBoolean();
                }
                if (b4 >= 3 && gameInputStream8.readBoolean()) {
                    try {
                        CustomUnitConfig.loadAndValidateCustomUnits(gameInputStream8);
                        this.x = true;
                    } catch (ConfigValidationException e3) {
                        disconnectNetworking("Missing unit:" + e3.getMessage() + " d:" + e3.messageDetail);
                        b("Connection Failed", e3.getMessage());
                        if (!GameEngine.isPC()) {
                            gameEngine.alert(e3.getMessage());
                        }
                        gameEngine.setPendingMessageBox("Connection Failed", e3.getMessage());
                        return;
                    }
                    break;
                }
                if (b4 >= 4) {
                    this.roomSettings.sharedControl = gameInputStream8.readBoolean();
                }
                if (b4 >= 5) {
                    this.gamePaused = gameInputStream8.readBoolean();
                }
                MultiplayerBattleroomActivity.updateUI();
                return;
            case 116:
                if (this.isServer) {
                    d("we are a server! we don't follow orders");
                    return;
                }
                GameInputStream gameInputStream9 = new GameInputStream(packet);
                NetworkConnection networkConnection9 = packet.connection;
                gameInputStream9.readInt();
                boolean z4 = gameInputStream9.readBoolean();
                if (z4 && !this.gameEndedByServer) {
                    this.gameEndedByServer = z4;
                    return;
                }
                return;
            case 117:
                NetworkConnection networkConnection10 = packet.connection;
                if (this.isServer && !networkConnection10.trackLastPacket) {
                    d("we are a server! skipping: " + packet.packetType);
                    return;
                }
                GameInputStream gameInputStream10 = new GameInputStream(packet);
                gameInputStream10.readByte();
                int i10 = gameInputStream10.readInt();
                String utf6 = gameInputStream10.readUTF();
                PasswordHandler passwordHandler2 = new PasswordHandler();
                passwordHandler2.isRequesting = true;
                passwordHandler2.requestId = i10;
                passwordHandler2.promptMessage = utf6;
                a(passwordHandler2);
                return;
            case 118:
                return;
            case 120:
                if (this.isServer) {
                    d("error, we are a server but got: PACKET_START_GAME");
                    return;
                }
                GameInputStream gameInputStream11 = new GameInputStream(packet);
                gameInputStream11.readByte();
                this.roomSettings.gameModeType = (GameModeType) gameInputStream11.readEnumOrdinalOrNull(GameModeType.class);
                if (this.roomSettings.gameModeType == GameModeType.savedGame) {
                    this.receivedSaveGameStream = gameInputStream11.readNestedStream();
                } else if (this.roomSettings.gameModeType == GameModeType.customMap) {
                    this.receivedCustomMapStream = gameInputStream11.readNestedStream();
                }
                this.selectedMapPath = gameInputStream11.readUTF();
                aB();
                return;
            case 122:
                if (this.isServer) {
                    d("error, we are a server but got: PACKET_RETURN_TO_BATTLEROOM");
                    return;
                } else {
                    queueReturnToBattleroom();
                    return;
                }
            case 140:
                if (!this.isServer) {
                    d("we are not a server! skipping");
                    return;
                }
                NetworkConnection networkConnection11 = packet.connection;
                GameInputStream gameInputStream12 = new GameInputStream(packet);
                GameTeam gameTeam = networkConnection11.player;
                if (gameTeam == null) {
                    if (networkConnection11.trackLastPacket) {
                        d("Allowing message from non player on forwarding connection");
                        gameTeam = this.adminGameTeam;
                    } else {
                        d("player is null for message, skipping");
                        return;
                    }
                }
                String utf7 = gameInputStream12.readUTF();
                gameInputStream12.readByte();
                String strI = i(utf7);
                if (this.callbacks.a(networkConnection11, gameTeam.teamName, strI)) {
                    if (this.chatLog.countMessagesFrom(networkConnection11, 60000) > this.h) {
                        if (Utility.elapsedMilliseconds(networkConnection11.lastActivityTime, System.nanoTime()) > 60000) {
                            networkConnection11.lastActivityTime = System.nanoTime();
                            j("Anti-spam: Too many messages from '" + networkConnection11.getPlayerDisplayName() + "'");
                        }
                        if (this.debugLogging) {
                            GameEngine.log("extraDebug:" + strI);
                            return;
                        }
                        return;
                    }
                    a(networkConnection11, gameTeam, gameTeam.teamName, strI);
                    this.callbacks.b(networkConnection11, gameTeam.teamName, strI);
                    b(networkConnection11, gameTeam, gameTeam.teamName, strI);
                    return;
                }
                return;
            case 141:
                if (this.isServer && !packet.connection.trackLastPacket) {
                    d("error, we are a server but got: PACKET_RECEIVE_CHAT_FROM_SERVER");
                    return;
                }
                GameInputStream gameInputStream13 = new GameInputStream(packet);
                String utf8 = gameInputStream13.readUTF();
                byte b5 = gameInputStream13.readByte();
                String nullableString3 = gameInputStream13.readNullableString();
                gameInputStream13.readInt();
                int i11 = -1;
                if (b5 >= 3) {
                    i11 = gameInputStream13.readInt();
                }
                b((NetworkConnection) null, i11, nullableString3, utf8);
                return;
            case 150:
                if (this.isServer) {
                    d("error, we are a server but got: PACKET_SEND_KICK");
                    return;
                }
                String strConvertInlineBlocks = Locale.convertInlineBlocks(new GameInputStream(packet).readUTF());
                d("we got kicked, reason:" + strConvertInlineBlocks);
                disconnectNetworking("I was kicked");
                b("Kicked", "Kicked: " + strConvertInlineBlocks);
                gameEngine.setPendingMessageBox("Kicked", "Kicked: " + strConvertInlineBlocks);
                gameEngine.alert("Kicked: " + strConvertInlineBlocks);
                return;
            case 151:
                NetworkConnection networkConnection12 = packet.connection;
                if (this.isServer && !networkConnection12.trackLastPacket) {
                    d("error, we are a server but got: 151");
                    return;
                }
                long jA = PerformanceProfiler.a();
                GameInputStream gameInputStream14 = new GameInputStream(packet);
                int i12 = gameInputStream14.readInt();
                int i13 = gameInputStream14.readInt();
                if (gameInputStream14.readBoolean()) {
                    MasterServerAuth.minClientVersion = gameInputStream14.readInt();
                }
                if (gameInputStream14.readBoolean()) {
                    MasterServerAuth.minServerVersion = gameInputStream14.readInt();
                }
                String strMd5 = VariableScope.nullOrMissingString;
                if (i13 == 0) {
                    strMd5 = VariableScope.nullOrMissingString + MasterServerAuth.minClientVersion;
                }
                if (i13 == 1) {
                    strMd5 = VariableScope.nullOrMissingString + MasterServerAuth.minServerVersion;
                }
                if (i13 == 2) {
                    strMd5 = g(MasterServerAuth.minClientVersion);
                }
                if (i13 == 3) {
                    strMd5 = Utility.sha256ShortHash(MasterServerAuth.minClientVersion + "|" + MasterServerAuth.minServerVersion);
                }
                if (i13 == 4) {
                    strMd5 = Utility.sha256ShortHash(MasterServerAuth.minClientVersion + "|" + MasterServerAuth.minServerVersion);
                }
                if (i13 == 5 || i13 == 6) {
                    String utf9 = gameInputStream14.readUTF();
                    String utf10 = gameInputStream14.readUTF();
                    int i14 = gameInputStream14.readInt();
                    if (i13 == 6) {
                        utf10 = utf10 + MasterServerAuth.minClientVersion;
                    }
                    if (i14 > 10000000) {
                        strMd5 = "max";
                    } else {
                        strMd5 = "-1";
                        for (int i15 = 0; i15 <= i14; i15++) {
                            if (Utility.sha256ShortHash(utf10 + i15).equals(utf9)) {
                                strMd5 = VariableScope.nullOrMissingString + i15;
                                break;
                            }
                        }
                    }
                }
                if (i13 == 7) {
                    String utf11 = gameInputStream14.readUTF();
                    int i16 = gameInputStream14.readInt();
                    if (i16 > 10000) {
                        strMd5 = "max";
                    } else {
                        strMd5 = VariableScope.nullOrMissingString;
                        for (int i17 = 0; i17 < i16; i17++) {
                            strMd5 = strMd5 + utf11;
                        }
                    }
                }
                float fA = PerformanceProfiler.a(jA);
                GameOutputStream gameOutputStream2 = new GameOutputStream();
                gameOutputStream2.writeInt(i12);
                gameOutputStream2.writeInt(i13);
                gameOutputStream2.writeStringUTF(strMd5);
                gameOutputStream2.writeFloat(fA);
                a(networkConnection12, gameOutputStream2.buildPacketData(152));
                return;
            case 160:
                GameInputStream gameInputStream15 = new GameInputStream(packet);
                NetworkConnection networkConnection13 = packet.connection;
                gameInputStream15.readUTF();
                int i18 = gameInputStream15.readInt();
                gameInputStream15.readInt();
                if (i18 >= 1) {
                    gameInputStream15.readInt();
                }
                if (networkConnection13.isSteam) {
                    GameEngine.log("steam: request info packet");
                }
                if (i18 >= 2 && (nullableString = gameInputStream15.readNullableString()) != null) {
                    networkConnection13.logInfo("Using query string: " + nullableString);
                    networkConnection13.queryString = nullableString;
                }
                if (i18 >= 3) {
                    gameInputStream15.readUTF();
                }
                if (i18 >= 4) {
                    gameInputStream15.readUTF();
                    String utf12 = gameInputStream15.readUTF();
                    if (GameEngine.isDedicatedServer()) {
                        networkConnection13.logInfo("Misc: " + utf12);
                    }
                }
                g(networkConnection13);
                return;
            case 161:
                if (this.isServer) {
                    d("we are a server! we don't PREREGISTER_INFO");
                    return;
                }
                GameInputStream gameInputStream16 = new GameInputStream(packet);
                NetworkConnection networkConnection14 = packet.connection;
                if (networkConnection14.isSteam) {
                    GameEngine.log("steam: got info packet");
                }
                gameInputStream16.readUTF();
                int i19 = gameInputStream16.readInt();
                int i20 = gameInputStream16.readInt();
                gameInputStream16.readInt();
                gameInputStream16.readUTF();
                this.serverUuid = gameInputStream16.readUTF();
                networkConnection14.networkVersion = i20;
                if (i19 >= 1) {
                    this.T = gameInputStream16.readInt();
                }
                if (i19 >= 2) {
                    this.U = gameInputStream16.readInt();
                    this.V = gameInputStream16.readInt();
                }
                if (this.registerConnectionSent) {
                    d("PACKET_SEND_PREREGISTER_INFO: Register connection has already been sent (resending)");
                }
                sendRegisterConnection(networkConnection14);
                return;
            case 163:
                if (this.isServer) {
                    d("we are already a server");
                    return;
                }
                GameInputStream gameInputStream17 = new GameInputStream(packet);
                gameInputStream17.readByte();
                int i21 = gameInputStream17.readInt();
                gameInputStream17.readInt();
                gameInputStream17.readNullableString();
                d("Relay version: " + i21);
                return;
            case 170:
                d("Got 'become server' packet");
                if (this.isServer) {
                    d("we are already a server");
                    return;
                }
                NetworkConnection networkConnection15 = packet.connection;
                GameInputStream gameInputStream18 = new GameInputStream(packet);
                byte b6 = gameInputStream18.readByte();
                boolean z5 = gameInputStream18.readBoolean();
                boolean z6 = gameInputStream18.readBoolean();
                String nullableString4 = gameInputStream18.readNullableString();
                boolean z7 = gameInputStream18.readBoolean();
                boolean z8 = gameInputStream18.readBoolean();
                String nullableString5 = gameInputStream18.readNullableString();
                boolean z9 = false;
                if (b6 >= 1) {
                    z9 = gameInputStream18.readBoolean();
                }
                String nullableString6 = null;
                if (b6 >= 2) {
                    nullableString6 = gameInputStream18.readNullableString();
                }
                d("Multicast:" + z9);
                networkConnection15.optimizeSplitContinuation = z9;
                if (z5) {
                    networkConnection15.trackLastPacket = true;
                }
                if (z6) {
                    networkConnection15.isRelayServer = true;
                }
                this.D = true;
                this.E = nullableString5;
                gameEngine.networkEngine.roomPassword = null;
                gameEngine.networkEngine.requireActiveMods = z7;
                gameEngine.networkEngine.publishToMasterServer = z8;
                ensureKeepAliveTimerStarted(false);
                if (nullableString6 != null) {
                    if (this.localPlayerTeam != null) {
                        this.localPlayerTeam.teamAIHint = nullableString6;
                    } else {
                        GameEngine.log("Become server: No local team");
                    }
                }
                if (gameEngine.networkEngine.publishToMasterServer) {
                }
                if (nullableString4 != null) {
                    gameEngine.settingsEngine.networkServerId = nullableString4;
                }
                if (gameEngine.currentTick > 60) {
                    this.allPlayersReady = true;
                }
                if (!this.x && !this.gameHasBeenStarted) {
                    GameEngine.log("enableAllCustomUnitsPossible mods:" + this.requireActiveMods);
                    CustomUnitConfigParser.enableAllCustomUnits(this.requireActiveMods);
                    this.x = true;
                    return;
                }
                return;
            case 172:
                NetworkConnection networkConnection16 = packet.connection;
                if (!networkConnection16.trackLastPacket) {
                    d("forwarding not allowed on this connection");
                    return;
                }
                d("got FORWARD_CLIENT_ADD");
                GameInputStream gameInputStream19 = new GameInputStream(packet);
                byte b7 = gameInputStream19.readByte();
                int i22 = gameInputStream19.readInt();
                String utf13 = gameInputStream19.readUTF();
                String nullableString7 = gameInputStream19.readNullableString();
                String nullableString8 = null;
                if (b7 >= 1) {
                    nullableString8 = gameInputStream19.readNullableString();
                }
                if (a(networkConnection16, i22) != null) {
                    d("Not adding client:" + i22 + " already exists");
                    return;
                }
                if (a(networkConnection16, i22, utf13, nullableString8) != null && nullableString7 != null) {
                    GameTeam gameTeamB = PlayerTeam.b(utf13);
                    if (gameTeamB == null) {
                        d("PACKET_FORWARD_CLIENT_ADD: Failed to find existing player with id:" + utf13);
                        for (PlayerTeam playerTeam2 : PlayerTeam.getTeams()) {
                            if (playerTeam2 != null) {
                                d("option: " + playerTeam2.teamName + " - " + playerTeam2.teamAIHint + " - localPlayer:" + (this.localPlayerTeam == playerTeam2));
                            }
                        }
                        return;
                    }
                    gameTeamB.teamSharedControlType = nullableString7;
                    return;
                }
                return;
            case 173:
                NetworkConnection networkConnection17 = packet.connection;
                if (!networkConnection17.trackLastPacket) {
                    d("forwarding not allowed on this connection");
                    return;
                }
                d("got FORWARD_CLIENT_REMOVE");
                GameInputStream gameInputStream20 = new GameInputStream(packet);
                gameInputStream20.readByte();
                NetworkConnection networkConnectionA = a(networkConnection17, gameInputStream20.readInt());
                if (networkConnectionA != null) {
                    b(networkConnectionA, (String) null);
                    return;
                }
                return;
            case 174:
                NetworkConnection networkConnection18 = packet.connection;
                if (!networkConnection18.trackLastPacket) {
                    d("forwarding not allowed on this connection");
                    return;
                }
                GameInputStream gameInputStream21 = new GameInputStream(packet);
                int i23 = gameInputStream21.readInt();
                byte[] bytesWithLength = gameInputStream21.readBytesWithLength();
                NetworkConnection networkConnectionA2 = a(networkConnection18, i23);
                if (networkConnectionA2 == null) {
                    d("PACKET_FORWARD_CLIENT_FROM failed, cannot find client");
                    return;
                } else if (!(networkConnectionA2.socket instanceof SteamSocket)) {
                    d("PACKET_FORWARD_CLIENT_FROM failed, socket is wrong type");
                    return;
                } else {
                    ((SteamSocket) networkConnectionA2.socket).inputStream.enqueuePacket(bytesWithLength);
                    return;
                }
            case 175:
                d("got PACKET_FORWARD_CLIENT_TO");
                return;
            case 176:
                d("got PACKET_FORWARD_CLIENT_TO_REPEATED");
                return;
            case SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_INPUT /* 178 */:
                d("got PACKET_RECONNECT_TO");
                NetworkConnection networkConnection19 = packet.connection;
                if (this.isServer && !networkConnection19.trackLastPacket) {
                    d("we are a server, ");
                    return;
                }
                GameInputStream gameInputStream22 = new GameInputStream(packet);
                gameInputStream22.readByte();
                gameInputStream22.readInt();
                boolean z10 = gameInputStream22.readBoolean();
                int i24 = gameInputStream22.readInt();
                ArrayList arrayList = new ArrayList();
                for (int i25 = 0; i25 < i24; i25++) {
                    arrayList.add(gameInputStream22.readUTF());
                }
                a(arrayList, z10);
                return;
            default:
                d("we did not handle packet:" + packet.packetType);
                return;
        }
    }

    /* JADX INFO: renamed from: c */
    public void ensureKeepAliveTimerStarted(boolean z) {
        int activeTeamCount;
        this.isServer = true;
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.localPlayerTeam == null) {
            GameTeam gameTeam = null;
            if (!z) {
                activeTeamCount = PlayerTeam.getFirstFreePlayerTeamId();
                if (activeTeamCount == -1) {
                    throw new RuntimeException("playerId is -1 for server player");
                }
            } else {
                gameTeam = this.adminGameTeam;
                activeTeamCount = this.adminGameTeam.teamId;
            }
            if (gameTeam == null) {
                gameTeam = new GameTeam(activeTeamCount);
                gameTeam.teamName = this.playerName;
                gameEngine.playerTeam = gameTeam;
            }
            this.localPlayerTeam = gameTeam;
        }
        if (this.keepAliveTimer == null) {
            GameEngine.log("pingerTask starting");
            this.keepAliveTimer = new KeepAliveTimer(this);
            this.generalTimer = new Timer();
            this.generalTimer.schedule(this.keepAliveTimer, 100L, 100L);
        } else {
            GameEngine.log("pingerTask already active");
        }
        MultiplayerBattleroomActivity.updateUI();
    }

    public boolean isUdpMultiplayerEnabled() {
        return GameEngine.getInstance().settingsEngine.udpInMultiplayer;
    }

    public SocketConnector a(String str, boolean z, Runnable runnable) {
        SocketConnector socketConnector = new SocketConnector(str, z, runnable);
        socketConnector.b();
        return socketConnector;
    }

    /* JADX INFO: renamed from: b */
    public synchronized boolean startServerHosting(boolean z) throws ConfigParseException {
        if (this.networkGameActive) {
            throw new RuntimeException("networking already started");
        }
        q();
        this.networkGameActive = true;
        this.isServer = true;
        regenerateOwnServerId();
        aA();
        GameEngine gameEngine = GameEngine.getInstance();
        ensureKeepAliveTimerStarted(z);
        MultiplayerBattleroomActivity.updateUI();
        this.networkPort = gameEngine.settingsEngine.networkPort;
        DisabledSteamEngine.a().i();
        this.connectionAcceptor1 = new ConnectionAcceptor(this);
        try {
            this.connectionAcceptor1.startSocket(false);
            this.connectionAcceptorThread1 = new Thread(this.connectionAcceptor1);
            this.connectionAcceptorThread1.setDaemon(true);
            this.connectionAcceptorThread1.start();
            this.connectionAcceptor2 = new ConnectionAcceptor(this);
            try {
                this.connectionAcceptor2.startSocket(true);
                this.connectionAcceptorThread2 = new Thread(this.connectionAcceptor2);
                this.connectionAcceptorThread2.start();
            } catch (IOException e) {
                e.printStackTrace();
                gameEngine.alert("Could not open udp port:" + this.networkPort + ", check this port is not in use or change the port in the game settings", 1);
                disconnectNetworking("Could not open udp port");
                return false;
            }
            updateMultiplayerNotifications();
            if (this.useMasterServer && this.publishToMasterServer) {
                MasterServerClient.createServerAsync();
            }
            this.publicIpLookupSuccess = null;
            if (this.useMasterServer && r) {
                MasterServerClient.getOwnInfoFromMasterServerAsync();
            }
            d("server started");
            return true;
        } catch (IOException e2) {
            e2.printStackTrace();
            gameEngine.alert("Could not open tcp port:" + this.networkPort + ", check this port is not in use or change the port in the game settings", 1);
            disconnectNetworking("Could not open tcp port");
            return false;
        }
    }

    public void showReconnectDialog() {
        final GameEngine gameEngine = GameEngine.getInstance();
        final MenuDialog menuDialogA = MenuDialog.create(Locale.get("menus.ingame.multiplayerReconnect.message", new Object[0]), false);
        menuDialogA.addButton(Locale.get("menus.ingame.resume", new Object[0]), new UIEventHandler() { // from class: com.corrodinggames.rts.gameFramework.j.ad.3
            @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIEventHandler
            public boolean handleEvent(UIEvent uIEvent) {
                menuDialogA.removeFromParent();
                return true;
            }
        });
        menuDialogA.addButton(Locale.get("menus.ingame.reconnect", new Object[0]), new UIEventHandler() { // from class: com.corrodinggames.rts.gameFramework.j.ad.4
            @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIEventHandler
            public boolean handleEvent(UIEvent uIEvent) {
                menuDialogA.removeFromParent();
                NetworkEngine.this.reconnectToServer();
                return true;
            }
        });
        menuDialogA.addButton(Locale.get("menus.ingame.disconnect", new Object[0]), new UIEventHandler() { // from class: com.corrodinggames.rts.gameFramework.j.ad.5
            @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIEventHandler
            public boolean handleEvent(UIEvent uIEvent) {
                menuDialogA.removeFromParent();
                gameEngine.queueGameThreadTask(new Runnable() { // from class: com.corrodinggames.rts.gameFramework.j.ad.5.1
                    @Override // java.lang.Runnable
                    public void run() {
                        NetworkEngine.this.disconnectNetworking("already disconnected");
                        gameEngine.gameUI.interfaceRenderer.restartMission();
                    }
                });
                return true;
            }
        });
        gameEngine.gameUI.centerAndShowDialog(menuDialogA);
        this.reconnectDialogShown = true;
    }

    public synchronized boolean reconnectToServer() {
        Socket socket = this.socket;
        if (socket == null) {
            GameEngine.log("reconnectToServer: lastConnectedTo==null");
            return false;
        }
        GameEngine.log("reconnectToServer attempted");
        if (this.networkGameActive) {
            GameEngine.log("reconnectToServer: disconnecting");
            disconnectNetworking("reconnecting");
        }
        if (socket.getInetAddress() == null) {
            GameEngine.log("reconnectToServer: lastConnectedTo.getInetAddress()==null");
            return false;
        }
        String str = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        GameEngine.log("reconnectToServer: connecting to: " + str);
        try {
            return a(b(str, false));
        } catch (NetworkException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public synchronized boolean a(Socket socket) throws  IOException {
        if (this.networkGameActive) {
            disconnectNetworking("starting new");
        }
        if (socket == null) {
            throw new RuntimeException("connectedSocket==null");
        }
        q();
        GameEngine.getInstance();
        this.networkPort = socket.getPort();
        this.networkGameActive = true;
        this.isServer = false;
        d("connected to Server..");
        NetworkConnection networkConnection = new NetworkConnection(this, socket);
        networkConnection.allowLargeIncomingPackets = true;
        networkConnection.startWorkers();
        this.sendQueue.add(networkConnection);
        f(networkConnection);
        updateMultiplayerNotifications();
        this.socket = socket;
        return true;
    }

    public NetworkConnection c(PlayerTeam playerTeam) {
        for (NetworkConnection networkConnection : this.sendQueue) {
            if (networkConnection.player == playerTeam) {
                return networkConnection;
            }
        }
        return null;
    }

    public NetworkConnection d(PlayerTeam playerTeam) {
        for (NetworkConnection networkConnection : this.sendQueue) {
            if (!networkConnection.isDisconnecting && networkConnection.player == playerTeam) {
                return networkConnection;
            }
        }
        return null;
    }

    public NetworkConnection getActiveServerConnection() {
        if (this.isServer) {
            return null;
        }
        for (NetworkConnection networkConnection : this.sendQueue) {
            if (!networkConnection.isDisconnecting) {
                return networkConnection;
            }
        }
        return null;
    }

    public void d(PacketData packetData) {
        if (!this.networkGameActive) {
            GameEngine.log("Skipping sendPacketToAll, not networked");
        } else {
            i(packetData);
        }
    }

    private void i(PacketData packetData) {
        for (NetworkConnection networkConnection : this.sendQueue) {
            if (networkConnection.allowLargeIncomingPackets && !networkConnection.isDisconnecting && !networkConnection.isRelayServer) {
                networkConnection.enqueuePacket(packetData);
            }
        }
    }

    public void e(PacketData packetData) {
        if (!this.networkGameActive) {
            GameEngine.log("Skipping sendPacketToAllIncludingRelay, not networked");
            return;
        }
        for (NetworkConnection networkConnection : this.sendQueue) {
            if (networkConnection.allowLargeIncomingPackets && !networkConnection.isDisconnecting) {
                networkConnection.enqueuePacket(packetData);
            }
        }
    }

    public void f(PacketData packetData) {
        if (!this.networkGameActive) {
            GameEngine.log("Skipping sendPacketToServer, not networked");
        } else {
            if (this.isServer) {
                throw new RuntimeException("We are a server");
            }
            d(packetData);
        }
    }

    public void g(PacketData packetData) {
        if (!this.networkGameActive) {
            GameEngine.log("Skipping sendPacketToClients, not networked");
        } else {
            if (!this.isServer) {
                throw new RuntimeException("We are not a server");
            }
            e(packetData);
        }
    }

    public void h(PacketData packetData) {
        if (!this.networkGameActive) {
            GameEngine.log("Skipping sendPacketToClients, not networked");
        } else {
            if (!this.isServer) {
                throw new RuntimeException("We are not a server");
            }
            d(packetData);
        }
    }

    public void a(NetworkConnection networkConnection, PacketData packetData) {
        if (!this.networkGameActive) {
            GameEngine.log("Skipping sendPacketOnConnection, not networked");
        } else {
            networkConnection.enqueuePacket(packetData);
        }
    }

    /* JADX INFO: renamed from: X */
    public void sendRegisterConnectionsToAll() {
        if (this.isServer) {
            d("registerConnection: We are a server");
        }
        Iterator it = this.sendQueue.iterator();
        while (it.hasNext()) {
            sendRegisterConnection((NetworkConnection) it.next());
        }
    }

    /* JADX INFO: renamed from: Y */
    public void regenerateOwnClientId() {
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.settingsEngine.networkClientId = null;
        if (this.serverUuid == null) {
            GameEngine.log("generateNewClientId: serverUUID==null");
            this.serverUuid = "x";
        }
        getOwnClientIdHashed();
        gameEngine.settingsEngine.save();
    }

    /* JADX INFO: renamed from: Z */
    public String getOwnClientIdHashed() {
        GameEngine gameEngine = GameEngine.getInstance();
        boolean z = false;
        if (gameEngine.settingsEngine.networkClientId == null) {
            z = true;
        }
        if (!this.networkClientIdMachineKeyChecked) {
            this.networkClientIdMachineKeyChecked = true;
            if (GameEngine.isPC()) {
                String hardwareAddressHash = getHardwareAddressHash();
                if (!hardwareAddressHash.equals(gameEngine.settingsEngine.networkClientIdMachineKey)) {
                    if (gameEngine.settingsEngine.networkClientIdMachineKey != null) {
                        GameEngine.log("Machine appears to have changed: " + gameEngine.settingsEngine.networkClientIdMachineKey + " vs " + hardwareAddressHash);
                    }
                    gameEngine.settingsEngine.networkClientIdMachineKey = hardwareAddressHash;
                    z = true;
                }
            }
        }
        if (z) {
            GameEngine.log("new networkClientId needed");
            gameEngine.settingsEngine.networkClientId = UUID.randomUUID().toString();
            gameEngine.settingsEngine.save();
        }
        String str = gameEngine.settingsEngine.networkClientId;
        if (this.serverUuid == null) {
            throw new RuntimeException("getOwnClientIdHashed: serverUUID==null");
        }
        return Utility.sha256Hex(str + this.serverUuid);
    }

    public void regenerateOwnServerId() {
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.settingsEngine.networkServerId = UUID.randomUUID().toString();
        gameEngine.settingsEngine.save();
    }

    public String getOwnServerId() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine.settingsEngine.networkServerId == null) {
            regenerateOwnServerId();
        }
        return gameEngine.settingsEngine.networkServerId;
    }

    /* JADX INFO: renamed from: ac */
    public String getCurrentServerId() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.isServer) {
            return gameEngine.settingsEngine.networkServerId;
        }
        return this.serverUuid;
    }

    public void f(NetworkConnection networkConnection) {
        GameOutputStream gameOutputStream = new GameOutputStream();
        try {
            int i = 1;
            if (GameEngine.isPC()) {
                i = 2;
            }
            if (GameEngine.isIOSVersion) {
                i = 3;
            }
            gameOutputStream.writeStringUTF("com.corrodinggames.rts");
            gameOutputStream.writeInt(4);
            gameOutputStream.writeInt(this.e);
            gameOutputStream.writeInt(i);
            gameOutputStream.writeStringNullable(this.connectionQueryString);
            gameOutputStream.writeStringUTF(this.playerName);
            gameOutputStream.writeStringUTF(Locale.getLanguage());
            String str = VariableScope.nullOrMissingString;
            if (GameEngine.isAutomatedTestMode) {
                str = str + "d";
            }
            gameOutputStream.writeStringUTF(str);
            a(networkConnection, gameOutputStream.buildPacketData(160));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void g(NetworkConnection networkConnection) {
        GameOutputStream gameOutputStream = new GameOutputStream();
        try {
            GameEngine gameEngine = GameEngine.getInstance();
            gameOutputStream.writeStringUTF("com.corrodinggames.rts");
            gameOutputStream.writeInt(2);
            gameOutputStream.writeInt(this.e);
            gameOutputStream.writeInt(gameEngine.getVersionCode(true));
            gameOutputStream.writeStringUTF(gameEngine.getPackageName());
            gameOutputStream.writeStringUTF(getOwnServerId());
            gameOutputStream.writeInt(networkConnection.sessionRandomId);
            gameOutputStream.writeInt(this.W);
            gameOutputStream.writeInt(0);
            a(networkConnection, gameOutputStream.buildPacketData(161));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* JADX INFO: renamed from: h */
    public void sendRegisterConnection(NetworkConnection networkConnection) {
        GameEngine.log("sendRegisterConnection...");
        GameOutputStream gameOutputStream = new GameOutputStream();
        try {
            gameOutputStream.writeStringUTF("com.corrodinggames.rts");
            gameOutputStream.writeInt(5);
            gameOutputStream.writeInt(this.e);
            GameEngine gameEngine = GameEngine.getInstance();
            gameOutputStream.writeInt(gameEngine.getVersionCode(true));
            gameOutputStream.writeStringUTF(this.playerName);
            String strTruncate = null;
            if (this.roomPassword != null) {
                strTruncate = Utility.sha256Hex(this.roomPassword);
            }
            gameOutputStream.writeStringNullable(strTruncate);
            gameOutputStream.writeStringUTF(gameEngine.getPackageName());
            gameOutputStream.writeStringUTF(getOwnClientIdHashed());
            gameOutputStream.writeInt(gameEngine.getAllUnitsChecksum());
            gameOutputStream.writeStringUTF(g(this.T));
            gameOutputStream.writeStringUTF(h(this.U));
            a(networkConnection, gameOutputStream.buildPacketData(110));
            this.registerConnectionSent = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String g(int i) {
        String str = (((((((((((VariableScope.nullOrMissingString + "c:" + i) + "m:" + ((i * 87) + 24)) + "0:" + (e(0) * 11 * i)) + "1:" + ((e(1) * 12) + i)) + "2:" + (e(2) * 13 * i)) + "3:" + ((e(3) * 14) + i)) + "4:" + (e(4) * 15 * i)) + "5:" + ((e(5) * 16) + i)) + "6:" + (e(6) * 17 * i)) + "7:" + (e(7) * 18 * i)) + "8:" + (e(8) * 19 * i)) + "t1:" + (PlayerTeam.TEAM_SELF.credits * 11.0d * ((double) i));
        int i2 = 5 * i;
        if (k() != e(this.roomSettings.startingCredits)) {
            i2 = 7 * i;
        }
        return str + "d:" + i2;
    }

    public String h(int i) {
        return Utility.toHexString(i);
    }

    public void sendClientStatusPacket() {
        if (this.isServer) {
            throw new RuntimeException("We are a server");
        }
        GameEngine gameEngine = GameEngine.getInstance();
        GameOutputStream gameOutputStream = new GameOutputStream();
        try {
            gameOutputStream.writeBoolean(this.bG);
            gameOutputStream.writeBoolean(gameEngine.isLoading);
            f(gameOutputStream.buildPacketData(112));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void j(String str) {
        if (!this.isServer) {
            d("cannot send sendSystemMessage:" + str + ", we are not a server");
        } else if (!this.networkGameActive || this.singleplayerServer) {
            d("cannot send sendSystemMessage:" + str + ", not networked");
        } else {
            GameEngine.log("sendSystemMessage:" + str);
            a((NetworkConnection) null, (PlayerTeam) null, (String) null, str);
        }
    }

    public void k(String str) {
        sendChatMessage("-qc " + str);
    }

    public void l(String str) {
        boolean z = true;
        String lowerCase = null;
        if (str != null) {
            String strTrim = str.trim();
            if ((strTrim.startsWith("-") || strTrim.startsWith(".") || strTrim.startsWith("_")) && strTrim.length() >= 2) {
                String strTrim2 = strTrim.substring(1).trim();
                int iIndexOf = strTrim2.indexOf(" ");
                if (iIndexOf == -1) {
                    iIndexOf = strTrim2.length();
                }
                lowerCase = strTrim2.substring(0, iIndexOf).toLowerCase(java.util.Locale.ENGLISH);
            }
        }
        if ("share".equals(lowerCase)) {
            z = false;
        }
        if ("t".equals(lowerCase)) {
            z = false;
        }
        if (z) {
            str = "-t " + str;
        }
        sendChatMessage(str);
    }

    public void sendChatMessage(String str) {
        if (!this.networkGameActive) {
            GameEngine.log("sendChatMessage: not networked:" + str);
            b((NetworkConnection) null, -1, (String) null, str);
        } else {
            if (this.isServer) {
                a((NetworkConnection) null, this.localPlayerTeam, this.playerName, str);
                b((NetworkConnection) null, this.localPlayerTeam, this.playerName, str);
                return;
            }
            try {
                GameOutputStream gameOutputStream = new GameOutputStream();
                gameOutputStream.writeStringUTF(str);
                gameOutputStream.writeByte(0);
                f(gameOutputStream.buildPacketData(140));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void a(NetworkConnection networkConnection, PlayerTeam playerTeam, String str, String str2) {
        a(networkConnection, playerTeam, str, str2, null);
    }

    public void a(NetworkConnection networkConnection, PlayerTeam playerTeam, String str, String str2, NetworkConnection networkConnection2) {
        GameTeam gameTeam;
        try {
            boolean z = false;
            boolean z2 = false;
            String strN = n(str2);
            if ("t".equalsIgnoreCase(strN)) {
                if (playerTeam != null) {
                    z = true;
                    str2 = "[TEAM] " + str2.substring("-t".length());
                } else {
                    GameEngine.logColored("toOnlyTeams failed team==null");
                }
            }
            if (playerTeam != null && "surrender".equalsIgnoreCase(strN)) {
                z = true;
                str2 = "[TEAM] " + str2;
            }
            if (playerTeam != null && "i".equalsIgnoreCase(strN)) {
                z2 = true;
                str2 = "[INFO] " + str2.substring("-i".length());
            }
            if (playerTeam != null && "qc".equalsIgnoreCase(strN)) {
                z2 = true;
                str2 = "[COMMAND] " + str2.substring("-qc".length());
            }
            if (!z2 && playerTeam != null && playerTeam != this.spectatorGameTeam && playerTeam != this.adminGameTeam && !this.callbacks.a(networkConnection, playerTeam, str2, z)) {
                z2 = true;
            }
            GameOutputStream gameOutputStream = new GameOutputStream();
            gameOutputStream.writeStringUTF(str2);
            gameOutputStream.writeByte(3);
            gameOutputStream.writeStringNullable(str);
            gameOutputStream.writeConnectionIdInt(networkConnection);
            int i = -1;
            if (playerTeam != null) {
                i = playerTeam.teamId;
            }
            gameOutputStream.writeInt(i);
            PacketData packetDataBuildPacketData = gameOutputStream.buildPacketData(141);
            if (z) {
                for (NetworkConnection networkConnection3 : this.sendQueue) {
                    if (networkConnection3.allowLargeIncomingPackets && !networkConnection3.isDisconnecting && (gameTeam = networkConnection3.player) != null && gameTeam.d(playerTeam)) {
                        networkConnection3.enqueuePacket(packetDataBuildPacketData);
                    }
                }
                PlayerTeam playerTeam2 = this.localPlayerTeam;
                if (playerTeam2 != null && playerTeam2.d(playerTeam)) {
                    b(networkConnection, i, str, str2);
                }
            } else if (z2) {
                GameEngine.logColored("info message:" + c(str, str2));
            } else {
                if (networkConnection2 != null) {
                    a(networkConnection2, packetDataBuildPacketData);
                } else {
                    g(packetDataBuildPacketData);
                }
                b(networkConnection, i, str, str2);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String n(String str) {
        if (str == null) {
            return null;
        }
        String strTrim = str.trim();
        if ((strTrim.startsWith("-") || strTrim.startsWith(".") || strTrim.startsWith("_")) && strTrim.length() >= 2) {
            String strTrim2 = strTrim.substring(1).trim();
            int iIndexOf = strTrim2.indexOf(" ");
            if (iIndexOf == -1) {
                iIndexOf = strTrim2.length();
            }
            return strTrim2.substring(0, iIndexOf).toLowerCase(java.util.Locale.ENGLISH);
        }
        return null;
    }

    public static String c(String str, String str2) {
        if (str != null) {
            return str + ": " + str2;
        }
        return str2;
    }

    public void o(String str) {
        String strConvertInlineBlocks = Locale.convertInlineBlocks(str);
        this.chatLog.addMessage(-1, null, strConvertInlineBlocks, null);
        this.callbacks.a(-1, (String) null, strConvertInlineBlocks, (NetworkConnection) null);
        boolean z = false;
        if (this.gameHasBeenStarted) {
            z = true;
        }
        if (!this.networkGameActive) {
            z = true;
        }
        if (z) {
            a((String) null, strConvertInlineBlocks);
            return;
        }
        String strC = c((String) null, strConvertInlineBlocks);
        if (!GameEngine.isNonAndroidVersion) {
            MultiplayerBattleroomActivity.addMessageToChatLog(strC);
        }
    }

    private void b(NetworkConnection networkConnection, int i, String str, String str2) {
        if (!this.networkGameActive && str2.startsWith("-i ")) {
            return;
        }
        if (!this.networkGameActive && str2.startsWith("-qc ")) {
            return;
        }
        String strConvertInlineBlocks = Locale.convertInlineBlocks(str2);
        if (str != null) {
            if (strConvertInlineBlocks != null) {
                if (strConvertInlineBlocks.equals("-surrender")) {
                }
                if (this.localPlayerTeam == null || i < 0 || this.localPlayerTeam.teamId == i) {
                }
            }
            if (1 != 0) {
                d("New Message", str + ": " + strConvertInlineBlocks);
            }
        }
        NetworkConnection networkConnection2 = null;
        if (this.isServer) {
            networkConnection2 = networkConnection;
        }
        this.chatLog.addMessage(i, str, strConvertInlineBlocks, networkConnection2);
        this.callbacks.a(i, str, strConvertInlineBlocks, networkConnection);
        boolean z = false;
        if (this.gameHasBeenStarted) {
            z = true;
        }
        if (!this.networkGameActive) {
            z = true;
        }
        if (z) {
            a(str, strConvertInlineBlocks);
            return;
        }
        String strC = c(str, strConvertInlineBlocks);
        if (!GameEngine.isNonAndroidVersion) {
            MultiplayerBattleroomActivity.addMessageToChatLog(strC);
        }
    }

    public void a(NetworkConnection networkConnection, byte[] bArr, boolean z, boolean z2) {
        GameEngine gameEngine = GameEngine.getInstance();
        try {
            GameOutputStream gameOutputStream = new GameOutputStream();
            gameOutputStream.writeByte(0);
            gameOutputStream.writeInt(gameEngine.currentTick);
            gameOutputStream.writeInt(gameEngine.gameTimeMillis);
            gameOutputStream.writeFloat(getCurrentStepRate());
            gameOutputStream.writeFloat(1.0f);
            gameOutputStream.writeBoolean(z);
            gameOutputStream.writeBoolean(z2);
            gameOutputStream.startBlock("gameSave");
            gameOutputStream.writeBytesRaw(bArr);
            gameOutputStream.endBlock("gameSave");
            a(networkConnection, gameOutputStream.buildPacketData(35));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void a(boolean z, boolean z2, boolean z3) {
        GameEngine gameEngine = GameEngine.getInstance();
        try {
            GameOutputStream gameOutputStream = new GameOutputStream();
            gameOutputStream.writeByte(0);
            gameOutputStream.writeInt(gameEngine.currentTick);
            gameOutputStream.writeInt(gameEngine.gameTimeMillis);
            gameOutputStream.writeFloat(getCurrentStepRate());
            gameOutputStream.writeFloat(1.0f);
            gameOutputStream.writeBoolean(z);
            gameOutputStream.writeBoolean(z2);
            gameOutputStream.startBlock("gameSave");
            gameEngine.gameSaver.writeSaveToStream(gameOutputStream);
            gameOutputStream.endBlock("gameSave");
            if (z) {
            }
            PacketData packetDataBuildPacketData = gameOutputStream.buildPacketData(35);
            d(packetDataBuildPacketData);
            if (z3) {
                if (!this.isServer) {
                    throw new RuntimeException("sendResyncSave: reloadCreatedSave: We are not a server");
                }
                packetDataBuildPacketData.connection = this.localConnection;
                processGamePacket(packetDataBuildPacketData);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean startBattleRoomGame() {
        sendPlayerUpdateNow();
        broadcastServerInfoToLargePacketConnections();
        return a((NetworkConnection) null, false);
    }

    public boolean a(NetworkConnection networkConnection, boolean z) {
        GameEngine.log("Sending start game....");
        if (!this.isServer) {
            throw new RuntimeException("We are not a server");
        }
        GameEngine gameEngine = GameEngine.getInstance();
        GameOutputStream gameOutputStream = new GameOutputStream();
        try {
            gameOutputStream.writeByte(0);
            gameOutputStream.writeEnumOrdinal(this.roomSettings.gameModeType);
            if (this.roomSettings.gameModeType == GameModeType.savedGame) {
                try {
                    gameEngine.gameSaver.writeSaveFileToStream(this.roomSettings.mapPath, gameOutputStream);
                    gameOutputStream.writeStringUTF("SAVE:" + this.roomSettings.mapPath);
                } catch (IOException e) {
                    e.printStackTrace();
                    gameEngine.showMessageBox("Map error starting game", "Map error: " + e.getMessage());
                    return false;
                }
            } else if (this.roomSettings.gameModeType == GameModeType.customMap) {
                GameEngine.log("Starting with custom map: " + l());
                try {
                    TileMap.writeMapStreamToOutput(this.selectedMapPath, gameOutputStream);
                    gameOutputStream.writeStringUTF("STEAM:" + l());
                } catch (IOException e2) {
                    e2.printStackTrace();
                    gameEngine.showMessageBox("Map error starting game", "Map error: " + e2.getMessage());
                    return false;
                }
            } else {
                gameOutputStream.writeStringUTF(l());
            }
            gameOutputStream.writeBoolean(z);
            PacketData packetDataBuildPacketData = gameOutputStream.buildPacketData(120);
            if (networkConnection == null) {
                g(packetDataBuildPacketData);
            } else {
                a(networkConnection, packetDataBuildPacketData);
            }
            if (!this.gameHasBeenStarted) {
                aB();
                return true;
            }
            return true;
        } catch (IOException e3) {
            throw new RuntimeException(e3);
        }
    }

    public void onStartGameFailed() {
        this.startGameFailed = true;
        GameEngine.log("onStartGameFailed");
        if (this.isServer) {
            this.gameHasBeenStarted = false;
            j("Map load failed.");
        } else {
            disconnectNetworking("Map load failed");
        }
    }

    private void aB() {
        this.returnToBattleroomPending = false;
        this.gameHasBeenStarted = true;
        this.startGameFailed = false;
        this.bd = false;
        GameEngine.log("Starting new network game (" + getCurrentServerId() + ")");
        if (this.useMasterServer && this.publishToMasterServer && this.isServer) {
            MasterServerClient.updateServerAsync();
        }
        if (!GameEngine.isNonAndroidVersion) {
            MultiplayerBattleroomActivity.startGame();
        }
        this.callbacks.onStartGameEvent();
    }

    public void scheduleDefaultReturnToBattleroom() {
        scheduleReturnToBattleroom(5.0f);
    }

    /* JADX INFO: renamed from: d */
    public void scheduleReturnToBattleroom(float f) {
        if (!this.isServer) {
            throw new RuntimeException("We are not a server");
        }
        if (this.returnToBattleroomCountdownActive) {
            return;
        }
        GameEngine.log("Setting up return to battleroom timer...");
        this.returnToBattleroomDelaySeconds = f;
        this.returnToBattleroomCountdownActive = true;
        j("Game ended by host. Returning to battleroom in " + ((int) f) + " seconds...");
    }

    /* JADX INFO: renamed from: i */
    public void sendReturnToBattleroomEvent(NetworkConnection networkConnection) {
        if (!this.isServer) {
            throw new RuntimeException("We are not a server");
        }
        try {
            GameOutputStream gameOutputStream = new GameOutputStream();
            gameOutputStream.writeByte(0);
            PacketData packetDataBuildPacketData = gameOutputStream.buildPacketData(122);
            if (networkConnection == null) {
                h(packetDataBuildPacketData);
            } else {
                a(networkConnection, packetDataBuildPacketData);
            }
            queueReturnToBattleroom();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* JADX INFO: renamed from: aC */
    private void queueReturnToBattleroom() {
        this.returnToBattleroomPending = true;
    }

    /* JADX INFO: renamed from: aD */
    private void returnToBattleroom() {
        GameEngine.log("----- returnToBattleroom -----");
        this.returnToBattleroomPending = false;
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.replayEngine.e();
        PlayerTeam playerTeam = this.localPlayerTeam;
        gameEngine.clearAllObjects();
        resetNetworkGameState();
        this.localPlayerTeam = playerTeam;
        gameEngine.currentTick = 0;
        gameEngine.gameTimeMillis = 0;
        resetConnectionStatusFlags();
        PlayerTeam.resetAllTeamStates();
        if (this.isServer) {
            aA();
        }
        J();
        if (this.useMasterServer && this.publishToMasterServer && this.isServer) {
            MasterServerClient.updateServerAsync();
        }
        if (!GameEngine.isNonAndroidVersion) {
        }
    }

    public String getPrimaryLocalIpAddress() {
        ArrayList localIpAddressList = getLocalIpAddressList();
        if (localIpAddressList == null || localIpAddressList.size() == 0) {
            return null;
        }
        return (String) localIpAddressList.get(0);
    }

    public String getLocalIpAddressSummary() {
        ArrayList<String> localIpAddressList = getLocalIpAddressList();
        if (localIpAddressList == null || localIpAddressList.size() == 0) {
            return null;
        }
        String str = VariableScope.nullOrMissingString;
        boolean z = true;
        for (String str2 : localIpAddressList) {
            if (z) {
                z = false;
            } else {
                str = str + ", ";
            }
            str = str + str2;
        }
        return str;
    }

    public ArrayList getLocalIpAddressList() {
        ArrayList arrayListD;
        if (engineInstances != null) {
            return new ArrayList(engineInstances);
        }
        long jA = PerformanceProfiler.a();
        ArrayList arrayListD2 = d(true);
        if (arrayListD2 != null && arrayListD2.size() > 0) {
            arrayListD = arrayListD2;
        } else {
            arrayListD = d(false);
        }
        double dA = PerformanceProfiler.a(jA);
        if (dA > 2.0d) {
            GameEngine.logColored("getLocalIpAddressList was slow, taking:" + PerformanceProfiler.a(dA));
        }
        if (dA > 10.0d && arrayListD != null && arrayListD.size() > 0) {
            GameEngine.log("getLocalIpAddressList: creating cache");
            engineInstances = new ArrayList(arrayListD);
        }
        return arrayListD;
    }

    public String getHardwareAddressHash() {
        String str = null;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (true) {
                if (!networkInterfaces.hasMoreElements()) {
                    break;
                }
                byte[] hardwareAddress = networkInterfaces.nextElement().getHardwareAddress();
                if (hardwareAddress != null) {
                    String strTrim = new String(hardwareAddress).trim();
                    if (strTrim.length() > 2) {
                        str = strTrim;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (str != null) {
            return Utility.sha256ShortHash(str);
        }
        return "[blank]";
    }

    public ArrayList d(boolean z) {
        ArrayList arrayList = new ArrayList();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                Enumeration<InetAddress> inetAddresses = networkInterfaces.nextElement().getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddressNextElement = inetAddresses.nextElement();
                    if (!inetAddressNextElement.isLoopbackAddress()) {
                        String string = inetAddressNextElement.getHostAddress().toString();
                        if (!string.contains("%")) {
                            if (!z) {
                                arrayList.add(string);
                            } else if (string.contains(".")) {
                                arrayList.add(string);
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Log.d("RustedWarfare", e.toString());
        }
        return arrayList;
    }

    InetAddress getUdpBroadcastAddress() {
        try {
            DhcpInfo dhcpInfo = ((WifiManager) GameEngine.getInstance().appContext.c("wifi")).getDhcpInfo();
            int i = (dhcpInfo.ipAddress & dhcpInfo.netmask) | (dhcpInfo.netmask ^ (-1));
            byte[] bArr = new byte[4];
            for (int i2 = 0; i2 < 4; i2++) {
                bArr[i2] = (byte) ((i >> (i2 * 8)) & 255);
            }
            return InetAddress.getByAddress(bArr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void d(String str, String str2) {
        if (GameEngine.isNonAndroidVersion) {
            return;
        }
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.singleplayerServer || gameEngine.replayEngine.j()) {
            return;
        }
        boolean zIsActivityOpen = MultiplayerBattleroomActivity.isActivityOpen();
        GameView gameView = gameEngine.activeGameView;
        if (gameView != null && !gameView.isContinuousRendering()) {
            zIsActivityOpen = true;
        }
        if (zIsActivityOpen) {
            if (this.pendingMultiplayerChatNotification) {
                i(2);
                return;
            }
            return;
        }
        NotificationManager notificationManager = (NotificationManager) gameEngine.appContext.c("notification");
        PendingIntent activity = PendingIntent.getActivity(gameEngine.appContext, 0, new Intent(gameEngine.appContext, (Class<?>) BaseActivity.class), 2);
        if (Build.VERSION.SDK_INT >= 11) {
            Notification.Builder builder = new Notification.Builder(gameEngine.appContext);
            builder.setContentTitle("Rusted Warfare Multiplayer");
            builder.setContentText(str + ": " + str2);
            builder.setSmallIcon(com.corrodinggames.rts.R.drawable.icon);
            builder.setContentIntent(activity);
            builder.setOngoing(false);
            builder.setAutoCancel(true);
            a(notificationManager);
            a(builder, "multiplayerChatId");
            notificationManager.notify(2, builder.getNotification());
            this.pendingMultiplayerChatNotification = true;
        }
    }

    public void updateMultiplayerNotifications() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.networkGameActive && gameEngine != null && gameEngine.isNetworkGameActive()) {
            aE();
        } else {
            i(1);
            i(2);
        }
    }

    private void a(Notification.Builder builder, String str) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                builder.getClass().getDeclaredMethod("setChannelId", String.class).invoke(builder, str);
            } catch (Exception e) {
                GameEngine.log("setChannelId failed", (Throwable) e);
            }
        }
    }

    private void a(NotificationManager notificationManager) {
        a(notificationManager, "multiplayerChatId", "Multiplayer Chat");
        a(notificationManager, "multiplayerStatusId", "Multiplayer Status");
    }

    private void a(NotificationManager notificationManager, String str, String str2) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                Class<?> cls = Class.forName("android.app.NotificationChannel");
                notificationManager.getClass().getDeclaredMethod("createNotificationChannel", cls).invoke(notificationManager, cls.getDeclaredConstructor(String.class, CharSequence.class, Integer.TYPE).newInstance(str, str2, 3));
            } catch (Exception e) {
                GameEngine.log("Creating notification channel failed", (Throwable) e);
            }
        }
    }

    private void aE() {
        if (GameEngine.isNonAndroidVersion) {
            return;
        }
        GameEngine gameEngine = GameEngine.getInstance();
        PendingIntent activity = PendingIntent.getActivity(gameEngine.appContext, 0, new Intent(gameEngine.appContext, (Class<?>) BaseActivity.class), 2);
        NotificationManager notificationManager = (NotificationManager) gameEngine.appContext.c("notification");
        if (Build.VERSION.SDK_INT >= 11) {
            if (Build.VERSION.SDK_INT >= 26) {
            }
            Notification.Builder builder = new Notification.Builder(gameEngine.appContext);
            builder.setContentTitle("Rusted Warfare Multiplayer");
            builder.setContentText("A multiplayer game is in progress");
            builder.setSmallIcon(com.corrodinggames.rts.R.drawable.icon);
            builder.setContentIntent(activity);
            builder.setOngoing(true);
            a(notificationManager);
            a(builder, "multiplayerStatusId");
            if (Build.VERSION.SDK_INT >= 16) {
                builder.build();
            }
            notificationManager.notify(1, builder.getNotification());
        }
    }

    private void i(int i) {
        if (GameEngine.isNonAndroidVersion) {
            return;
        }
        ((NotificationManager) GameEngine.getInstance().appContext.c("notification")).cancel(i);
    }

    public int getHumanPlayerCount() {
        int i = 0;
        for (int i2 = 0; i2 < PlayerTeam.TEAM_NEUTRAL; i2++) {
            PlayerTeam playerTeamK = PlayerTeam.k(i2);
            if (playerTeamK != null && !playerTeamK.isTeamSpectator) {
                i++;
            }
        }
        return i;
    }

    public int getPlayerAndAiCount() {
        int i = 0;
        for (int i2 = 0; i2 < PlayerTeam.TEAM_NEUTRAL; i2++) {
            if (PlayerTeam.k(i2) != null) {
                i++;
            }
        }
        return i;
    }

    public void e(PlayerTeam playerTeam) {
        if (this.isServer) {
            f(playerTeam);
        } else if (this.isProxyController) {
            k("-kick " + (playerTeam.teamId + 1));
        } else {
            GameEngine.logColored("kickTeamAndAttachedPlayer: but not server or proxy controller");
        }
    }

    public void f(PlayerTeam playerTeam) {
        if (playerTeam instanceof AIController) {
            playerTeam.removeFromTeamRegistry();
        } else {
            if (this.localPlayerTeam == playerTeam) {
                GameEngine.log("kickTeamAndAttachedPlayer", "Cannot kick self");
                return;
            }
            NetworkConnection networkConnectionC = c(playerTeam);
            if (networkConnectionC == null) {
                reportDesync("Kick player: cannot find connection for team");
            } else {
                int i = GameEngine.getInstance().settingsEngine.banTimeInSecondsAfterKick;
                if (i > 0) {
                    banConnection(networkConnectionC, "Temporarily banned due to recent kick", i);
                }
                a(networkConnectionC, "Kicked by host");
                networkConnectionC.sendPacket("Kicked by host");
            }
            playerTeam.removeFromTeamRegistry();
        }
        markPlayerUpdatePending();
        MultiplayerBattleroomActivity.updateUI();
    }

    public void addAIToGame() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (!this.isServer) {
            GameEngine.log("addAIToGame", "We are not a server");
            return;
        }
        int activeTeamCount = PlayerTeam.getFirstFreePlayerTeamId();
        if (activeTeamCount == -1) {
            gameEngine.alert("No free slots for AI", 1);
        }
        AIController aIController = new AIController(activeTeamCount);
        aIController.teamName = "AI";
        aIController.teamColorId = activeTeamCount % 2;
        aIController.teamPingTime = this.roomSettings.aiDifficulty;
        updateAiTeamNames();
        gameEngine.networkEngine.callbacks.a(aIController);
        gameEngine.networkEngine.e((NetworkConnection) null);
        MultiplayerBattleroomActivity.updateUI();
    }

    public boolean updateAiTeamNames() {
        if (!this.isServer && this.networkGameActive) {
            GameEngine.log("updateNamesOfAI", "We are not a server");
            return false;
        }
        boolean z = false;
        for (int i = 0; i < PlayerTeam.TEAM_NEUTRAL; i++) {
            PlayerTeam playerTeamK = PlayerTeam.k(i);
            if (playerTeamK != null && updateAiTeamName(playerTeamK)) {
                z = true;
            }
        }
        return z;
    }

    public void a(PlayerTeam playerTeam, int i) {
        synchronized (this.connectionLock) {
            c(playerTeam, i);
        }
    }

    private void c(PlayerTeam playerTeam, int i) {
        if (playerTeam.teamId != i) {
            int i2 = playerTeam.teamId;
            int i3 = playerTeam.teamColorId;
            boolean z = false;
            if (i == -3) {
                z = true;
                i = PlayerTeam.getFirstFreeTeamSlotId();
                if (i == -1) {
                    e("No free spectator slots");
                    return;
                }
            }
            PlayerTeam playerTeamK = PlayerTeam.k(i);
            playerTeam.setTeamId(i);
            playerTeam.teamColorId = i3;
            if (z) {
                playerTeam.teamColorId = -3;
            }
            if (playerTeamK != null) {
                int i4 = playerTeamK.teamColorId;
                playerTeamK.setTeamId(i2);
                if (i4 == -3) {
                    playerTeamK.teamColorId = -3;
                } else {
                    playerTeamK.teamColorId = i3;
                }
            }
            refreshTeamSortAndAiGroups();
            markPlayerUpdatePending();
        }
    }

    public void a(TeamLayoutType teamLayoutType) {
        synchronized (this.connectionLock) {
            b(teamLayoutType);
        }
    }

    private synchronized void b(TeamLayoutType teamLayoutType) {
        if (!GameEngine.getInstance().networkEngine.isServer) {
            GameEngine.log("Not server");
            return;
        }
        if (teamLayoutType == TeamLayoutType.layout_2sides) {
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < PlayerTeam.TEAM_NEUTRAL; i++) {
                PlayerTeam playerTeamK = PlayerTeam.k(i);
                if (playerTeamK != null) {
                    arrayList.add(playerTeamK);
                }
            }
            Collections.shuffle(arrayList);
            int size = arrayList.size() / 2;
            if (arrayList.size() % 2 != 0) {
                size += Utility.getRandomIntInRange(0, 1);
            }
            if (size >= arrayList.size()) {
                size = arrayList.size();
            }
            int i2 = 0;
            for (int i3 = 0; i3 < size; i3++) {
                ((PlayerTeam) arrayList.get(i3)).setTeamId(i2);
                i2 += 2;
                ((PlayerTeam) arrayList.get(i3)).teamColorId = 0;
            }
            int i4 = 1;
            for (int i5 = 0 + size; i5 < arrayList.size(); i5++) {
                ((PlayerTeam) arrayList.get(i5)).setTeamId(i4);
                i4 += 2;
                ((PlayerTeam) arrayList.get(i5)).teamColorId = 1;
            }
        } else if (teamLayoutType == TeamLayoutType.layout_3sides) {
            ArrayList arrayList2 = new ArrayList();
            for (int i6 = 0; i6 < PlayerTeam.TEAM_NEUTRAL; i6++) {
                PlayerTeam playerTeamK2 = PlayerTeam.k(i6);
                if (playerTeamK2 != null) {
                    arrayList2.add(playerTeamK2);
                }
            }
            Collections.shuffle(arrayList2);
            int size2 = arrayList2.size() / 3;
            if (size2 >= arrayList2.size()) {
                size2 = arrayList2.size();
            }
            int i7 = 0;
            for (int i8 = 0; i8 < size2; i8++) {
                PlayerTeam playerTeam = (PlayerTeam) arrayList2.get(i8);
                playerTeam.setTeamId(i7);
                playerTeam.teamColorId = 0;
                i7 += 3;
                arrayList2.set(i8, null);
            }
            int size3 = 0 + size2;
            int size4 = size3 + (arrayList2.size() / 3);
            if (size4 >= arrayList2.size()) {
                size4 = arrayList2.size();
            }
            if (size3 >= arrayList2.size()) {
                size3 = arrayList2.size();
            }
            int i9 = 1;
            for (int i10 = size3; i10 < size4; i10++) {
                PlayerTeam playerTeam2 = (PlayerTeam) arrayList2.get(i10);
                playerTeam2.setTeamId(i9);
                playerTeam2.teamColorId = 1;
                i9 += 3;
                arrayList2.set(i10, null);
            }
            int size5 = size3 + size2;
            if (size5 >= arrayList2.size()) {
                size5 = arrayList2.size();
            }
            int i11 = 2;
            for (int i12 = size5; i12 < arrayList2.size(); i12++) {
                PlayerTeam playerTeam3 = (PlayerTeam) arrayList2.get(i12);
                if (i11 >= PlayerTeam.TEAM_NEUTRAL) {
                    playerTeam3.setTeamId(i11);
                    playerTeam3.teamColorId = 2;
                    i11 += 3;
                    arrayList2.set(i12, null);
                }
            }
            for (int i13 = 0; i13 < arrayList2.size(); i13++) {
                PlayerTeam playerTeam4 = (PlayerTeam) arrayList2.get(i13);
                if (playerTeam4 != null) {
                    for (int i14 = 0; i14 < PlayerTeam.TEAM_NEUTRAL; i14++) {
                        if (PlayerTeam.k(i14) == null) {
                            playerTeam4.setTeamId(i14);
                            playerTeam4.teamColorId = 2;
                            arrayList2.set(i13, null);
                        }
                    }
                }
            }
        } else if (teamLayoutType == TeamLayoutType.layout_ffa) {
            ArrayList arrayList3 = new ArrayList();
            for (int i15 = 0; i15 < PlayerTeam.TEAM_NEUTRAL; i15++) {
                PlayerTeam playerTeamK3 = PlayerTeam.k(i15);
                if (playerTeamK3 != null) {
                    arrayList3.add(playerTeamK3);
                }
            }
            Collections.shuffle(arrayList3);
            int i16 = 0;
            for (int i17 = 0; i17 < arrayList3.size(); i17++) {
                ((PlayerTeam) arrayList3.get(i17)).setTeamId(i16);
                ((PlayerTeam) arrayList3.get(i17)).teamColorId = i16;
                i16++;
            }
        } else if (teamLayoutType == TeamLayoutType.layout_spectators) {
            ArrayList arrayList4 = new ArrayList();
            for (int i18 = 0; i18 < PlayerTeam.TEAM_NEUTRAL; i18++) {
                PlayerTeam playerTeamK4 = PlayerTeam.k(i18);
                if (playerTeamK4 != null) {
                    arrayList4.add(playerTeamK4);
                }
            }
            Collections.shuffle(arrayList4);
            int i19 = 0;
            for (int i20 = 0; i20 < arrayList4.size(); i20++) {
                int freeTeamSlotId = PlayerTeam.getFirstFreeTeamSlotId();
                if (freeTeamSlotId != -1) {
                    ((PlayerTeam) arrayList4.get(i20)).setTeamId(freeTeamSlotId);
                }
                ((PlayerTeam) arrayList4.get(i20)).teamColorId = -3;
                i19++;
            }
        } else {
            throw new RuntimeException("overrideTeamLayout: unhandled layout: " + teamLayoutType);
        }
        refreshTeamSortAndAiGroups();
    }

    public void a(PlayerTeam playerTeam, int i, Integer num) {
        String str = VariableScope.nullOrMissingString;
        if (num != null) {
            str = " " + num;
        }
        if (!this.isProxyController && this.localPlayerTeam == playerTeam) {
            k("-self_move " + (i + 1) + str);
        } else {
            k("-move " + (playerTeam.teamId + 1) + " " + (i + 1) + str);
        }
    }

    public void b(PlayerTeam playerTeam, int i) {
        if (i != -1) {
            i++;
        }
        if (!this.isProxyController && this.localPlayerTeam == playerTeam) {
            k("-self_team " + i);
        } else {
            k("-team " + (playerTeam.teamId + 1) + " " + i);
        }
    }

    public void g(PlayerTeam playerTeam) {
        if (!playerTeam.isTeamAlliedVictory) {
            playerTeam.isTeamAlliedVictory = true;
            String str = playerTeam.teamName;
            if (str == null) {
                str = "Player - " + (playerTeam.teamId + 1) + VariableScope.nullOrMissingString;
            }
            j(str + " is victorious!");
        }
    }

    public void h(PlayerTeam playerTeam) {
        String str;
        GameEngine gameEngine = GameEngine.getInstance();
        boolean z = false;
        String str2 = playerTeam.teamName;
        if (str2 == null) {
            str2 = "Player - " + (playerTeam.teamId + 1) + VariableScope.nullOrMissingString;
        }
        String str3 = str2 + " was defeated";
        if (!this.freeForAllMode) {
            str = str3 + " (Team: " + playerTeam.getTeamSlotLabel() + ")";
        } else {
            int remainingPlayerCount = PlayerTeam.getRemainingPlayerCount();
            str = str3 + " (" + remainingPlayerCount + " players remaining)";
            if (remainingPlayerCount == 1) {
                z = true;
            }
        }
        if (!gameEngine.isNetworkConnected() && gameEngine.currentTick < 60) {
            GameEngine.log("Not showing defeated message: " + str);
            str = null;
        }
        if (playerTeam.isTeamVictory) {
            str = null;
        }
        if (str != null) {
            j(str);
        }
        if (z) {
            PlayerTeam.markRemainingTeamsVictorious();
        }
    }

    public void i(PlayerTeam playerTeam) {
        String str;
        String str2;
        GameEngine gameEngine = GameEngine.getInstance();
        String str3 = playerTeam.teamName;
        if (str3 == null) {
            str3 = "Player - " + (playerTeam.teamId + 1) + VariableScope.nullOrMissingString;
        }
        boolean z = false;
        if (gameEngine.currentTick < 10) {
            str = str3 + " had no starting units";
        } else {
            str = str3 + " has been wiped out";
        }
        if (!this.freeForAllMode) {
            str2 = str + " (Team: " + playerTeam.getTeamSlotLabel() + ")";
        } else {
            int remainingPlayerCount = PlayerTeam.getRemainingPlayerCount();
            str2 = str + " (" + remainingPlayerCount + " players remaining)";
            if (remainingPlayerCount == 1) {
                z = true;
            }
        }
        if (!gameEngine.isNetworkConnected() && gameEngine.currentTick < 60) {
            GameEngine.log("Not showing defeated message: " + str2);
            str2 = null;
        }
        if (playerTeam.isTeamVictory) {
            str2 = null;
        }
        if (playerTeam.isSpectatorTeamColor()) {
            str2 = null;
        }
        if (str2 != null) {
            j(str2);
        }
        if (z) {
            PlayerTeam.markRemainingTeamsVictorious();
        }
    }

    public synchronized void stopMasterServerUpdateTimer() {
        if (this.masterServerUpdateTimer != null) {
            this.masterServerUpdateTimer.cancel();
            this.masterServerUpdateTimer = null;
        }
    }

    public synchronized void startMasterServerUpdateTimer() {
        if (this.useMasterServer && this.publishToMasterServer && this.isServer && this.masterServerUpdateTimer == null) {
            this.masterServerUpdateTimer = new Timer();
            this.masterServerUpdateTimer.schedule(new TimerTask() { // from class: com.corrodinggames.rts.gameFramework.j.ad.6
                @Override // java.util.TimerTask, java.lang.Runnable
                public void run() {
                    MasterServerClient.updateServerAsync();
                }
            }, 60000L, 60000L);
        }
    }

    /* JADX INFO: renamed from: at */
    public String getPublicIpStatusText() {
        GameEngine gameEngine = GameEngine.getInstance();
        String str = VariableScope.nullOrMissingString;
        if (gameEngine.networkEngine.isServer && !gameEngine.networkEngine.singleplayerServer) {
            String localIpAddressSummary = gameEngine.networkEngine.getLocalIpAddressSummary();
            if (this.D) {
                if (this.E != null) {
                    str = str + this.E;
                }
            } else if (localIpAddressSummary != null) {
                String str2 = "Local IP address: " + localIpAddressSummary + " port: " + gameEngine.networkEngine.networkPort;
                if (gameEngine.networkEngine.publicIpLookupSuccess != null) {
                    if (!gameEngine.networkEngine.publicIpLookupSuccess.booleanValue()) {
                        str2 = str2 + "\nUnable to get a public IP address, check your internet connection";
                    } else if (gameEngine.networkEngine.publicIpAddress != null && gameEngine.networkEngine.publicPortOpen != null) {
                        str2 = str2 + "\nYour public address is " + (gameEngine.networkEngine.publicPortOpen.booleanValue() ? "<Open>" : "<CLOSED>") + " to the internet";
                    }
                } else {
                    str2 = str2 + "\nRetrieving your public IP...";
                }
                str = str + str2;
            } else {
                str = str + "You do not have a network connection";
            }
        }
        if (gameEngine.isSinglePlayerGame()) {
            if (this.isSandboxMode) {
                str = str + "SandBox Mode!\nPlace any unit, Control all teams, Special powers";
            } else {
                str = str + "Local skirmish";
            }
        }
        boolean z = true;
        if (GameEngine.isAndroidPlatform() && gameEngine.networkEngine.isServer) {
            z = false;
        }
        if (str.length() != 0) {
            str = str + "\n";
            if (GameEngine.isPC()) {
                str = str + "\n";
            }
        }
        if (gameEngine.networkEngine.gameSetupReceived || gameEngine.networkEngine.isServer) {
            if (z) {
                if (gameEngine.networkEngine.roomSettings.gameModeType != null) {
                    str = str + "Game Mode: " + gameEngine.networkEngine.roomSettings.gameModeType.a();
                }
                if (gameEngine.networkEngine.roomSettings.mapPath != null) {
                    str = str + "\nMap: " + LevelSelectActivity.getMapName(gameEngine.networkEngine.roomSettings.mapPath);
                }
            }
            str = (str + "\nStarting Credits: " + gameEngine.networkEngine.j()) + "\nFog: " + gameEngine.networkEngine.g();
            if (gameEngine.networkEngine.roomSettings.startingUnits != 1) {
                str = str + "\nStarting Units: " + gameEngine.networkEngine.h();
            }
            if (gameEngine.networkEngine.roomSettings.incomeMultiplier != 1.0f) {
                str = str + "\n" + Utility.padString(gameEngine.networkEngine.roomSettings.incomeMultiplier, 1) + "X income";
            }
            if (gameEngine.networkEngine.roomSettings.noNukes) {
                str = str + "\nNo nukes";
            }
            if (gameEngine.networkEngine.roomSettings.sharedControl) {
                str = str + "\nShared control: On";
            }
            if (this.isServer) {
                if (gameEngine.networkEngine.roomPassword != null) {
                    str = str + "\nPassword Protection: On";
                }
                if (!gameEngine.networkEngine.publishToMasterServer && !gameEngine.networkEngine.singleplayerServer) {
                    str = str + "\nServer Visibility: Hidden";
                }
                if (gameEngine.networkEngine.requireActiveMods && !gameEngine.networkEngine.singleplayerServer) {
                    ArrayList activeMods = gameEngine.modManager.getActiveMods();
                    str = str + "\n-- Required Mods: --\n";
                    int i = 0;
                    Iterator it = activeMods.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        ModInfo modInfo = (ModInfo) it.next();
                        if (i > 2 && i < activeMods.size() - 1) {
                            str = str + VariableScope.nullOrMissingString + (activeMods.size() - i) + " more mods...";
                            break;
                        }
                        i++;
                        String paddedTitle = modInfo.getPaddedTitle();
                        paddedTitle.replace("\"", "'");
                        paddedTitle.replace(";", ".");
                        str = str + " mod: \"" + paddedTitle + "\"\n";
                    }
                }
            }
        }
        return str;
    }

    public String getRequiredModsSummary() {
        if (!this.requireActiveMods) {
            return null;
        }
        ArrayList activeMods = GameEngine.getInstance().modManager.getActiveMods();
        String str = VariableScope.nullOrMissingString;
        int i = 0;
        Iterator it = activeMods.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ModInfo modInfo = (ModInfo) it.next();
            if (i != 0) {
                str = str + "; ";
            }
            if (i > 1 && i < activeMods.size() - 1) {
                str = str + VariableScope.nullOrMissingString + (activeMods.size() - i) + " more...";
                break;
            }
            i++;
            String paddedTitle = modInfo.getPaddedTitle();
            paddedTitle.replace(";", ".");
            str = str + paddedTitle;
        }
        return str;
    }

    public String getNetworkMapPath() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine.networkEngine.roomSettings.mapPath == null || gameEngine.networkEngine.roomSettings.gameModeType == null) {
            return null;
        }
        if (gameEngine.networkEngine.roomSettings.gameModeType == GameModeType.skirmishMap) {
            return "maps/skirmish/" + gameEngine.networkEngine.roomSettings.mapPath;
        }
        if (gameEngine.networkEngine.roomSettings.gameModeType == GameModeType.customMap) {
            return "/SD/rusted_warfare_maps/" + gameEngine.networkEngine.roomSettings.mapPath;
        }
        GameEngine.log("getNetworkMapPath: unhandled type:" + gameEngine.networkEngine.roomSettings.gameModeType);
        return null;
    }

    public boolean isServerOrProxyController() {
        return this.isServer || this.isProxyController;
    }

    public void a(String str, NetworkConnection networkConnection) {
        GameEngine.log("sendCommandError: " + str);
        if (networkConnection == null) {
            b((NetworkConnection) null, -1, (String) null, str);
        } else {
            a(null, null, null, str, networkConnection);
        }
    }

    public boolean b(NetworkConnection networkConnection, PlayerTeam playerTeam, String str, String str2) {
        String str3;
        int i;
        PlayerTeam playerTeamK;
        String lowerCase = null;
        String strTrim = VariableScope.nullOrMissingString;
        String[] strArrSplit = new String[0];
        String strTrim2 = str2.trim();
        boolean z = false;
        if (strTrim2.startsWith("-qc ")) {
            strTrim2 = strTrim2.substring("-qc ".length()).trim();
            z = true;
        }
        if ((strTrim2.startsWith("-") || strTrim2.startsWith(".") || strTrim2.startsWith("_")) && strTrim2.length() >= 2) {
            String strTrim3 = strTrim2.substring(1).trim();
            int iIndexOf = strTrim3.indexOf(" ");
            if (iIndexOf == -1) {
                iIndexOf = strTrim3.length();
            }
            lowerCase = strTrim3.substring(0, iIndexOf).toLowerCase(java.util.Locale.ENGLISH);
            if (iIndexOf != -1 && strTrim3.length() >= iIndexOf + 1) {
                strTrim = strTrim3.substring(iIndexOf + 1).trim();
                strArrSplit = strTrim.split(" ");
            }
        }
        if (lowerCase == null) {
            return false;
        }
        if (z && !"self_move".equals(lowerCase) && !"self_team".equals(lowerCase)) {
            return false;
        }
        if ("pause".equals(lowerCase) || "unpause".equals(lowerCase)) {
            if (playerTeam == null) {
                a("[Could not find player]", networkConnection);
                return true;
            }
            if ((!this.isServer || playerTeam != this.localPlayerTeam) && !this.callbacks.b(networkConnection)) {
                a("[Only the host can change pause state]", networkConnection);
                return true;
            }
            if (!this.gameHasBeenStarted) {
                a("[Game not yet started]", networkConnection);
                return true;
            }
            boolean z2 = !this.gamePaused;
            if ("unpause".equals(lowerCase)) {
                z2 = false;
            }
            setGamePaused(z2);
            return true;
        }
        if ("endgame".equals(lowerCase)) {
            if (playerTeam == null) {
                a("[Could not find player]", networkConnection);
                return true;
            }
            if (!this.isServer || playerTeam != this.localPlayerTeam) {
                a("[Only the host can end game]", networkConnection);
                return true;
            }
            if (!this.gameHasBeenStarted) {
                a("[Game not yet started]", networkConnection);
                return true;
            }
            scheduleDefaultReturnToBattleroom();
            return true;
        }
        if ("teamlock".equals(lowerCase)) {
            if (playerTeam == null) {
                a("[Could not find player]", networkConnection);
                return true;
            }
            if ((!this.isServer || playerTeam != this.localPlayerTeam) && !this.callbacks.b(networkConnection)) {
                a("[Only the host can change teamlock]", networkConnection);
                return true;
            }
            if ("true".equalsIgnoreCase(strTrim) || "on".equalsIgnoreCase(strTrim)) {
                this.roomSettings.teamLock = true;
                a("[teams are locked]", networkConnection);
                return true;
            }
            if ("false".equalsIgnoreCase(strTrim) || "off".equalsIgnoreCase(strTrim)) {
                this.roomSettings.teamLock = false;
                a("[teams are unlocked]", networkConnection);
                return true;
            }
            a("[Expected true or false]", networkConnection);
            return true;
        }
        if ("roomlock".equals(lowerCase)) {
            if (playerTeam == null) {
                a("[Could not find player]", networkConnection);
                return true;
            }
            if (!this.isServer || playerTeam != this.localPlayerTeam) {
                a("[Only the host can change roomlock]", networkConnection);
                return true;
            }
            if ("true".equalsIgnoreCase(strTrim) || "on".equalsIgnoreCase(strTrim)) {
                this.roomSettings.roomLock = true;
                a("[room is locked]", networkConnection);
                return true;
            }
            if ("false".equalsIgnoreCase(strTrim) || "off".equalsIgnoreCase(strTrim)) {
                this.roomSettings.roomLock = false;
                a("[room is unlocked]", networkConnection);
                return true;
            }
            a("[Expected true or false]", networkConnection);
            return true;
        }
        if ("share".equals(lowerCase)) {
            if (playerTeam == null) {
                a("[Could not find player]", networkConnection);
                return true;
            }
            if (!this.roomSettings.sharedControl) {
                a("[Shared control is not enabled in this game]", networkConnection);
                return true;
            }
            if ("true".equalsIgnoreCase(strTrim) || "on".equalsIgnoreCase(strTrim)) {
                if (!playerTeam.isTeamConnectionActive) {
                    playerTeam.isTeamConnectionActive = true;
                    j("[shared control now on for " + str + "]");
                    return true;
                }
                j("[shared control already on for " + str + "]");
                return true;
            }
            if ("false".equalsIgnoreCase(strTrim) || "off".equalsIgnoreCase(strTrim)) {
                if (playerTeam.isTeamConnectionActive) {
                    playerTeam.isTeamConnectionActive = false;
                    j("[shared control now off for " + str + "]");
                    return true;
                }
                j("[shared control already off for " + str + "]");
                return true;
            }
            a("[Expected true or false]", networkConnection);
            return true;
        }
        if ("self_move".equals(lowerCase)) {
            if (playerTeam == null) {
                a("[Cannot Move - Player not found]", networkConnection);
                return true;
            }
            if (this.gameHasBeenStarted) {
                a("[Cannot Move '" + playerTeam.teamName + "' - Game has been started]", networkConnection);
                return true;
            }
            if (o()) {
                a("[Cannot Move '" + playerTeam.teamName + "' - Game is starting]", networkConnection);
                return true;
            }
            if (this.roomSettings.teamLock) {
                a("[Cannot Move '" + playerTeam.teamName + "' - Teams locked]", networkConnection);
                return true;
            }
            if (strArrSplit.length > 0) {
                try {
                    int iIntValue = Integer.valueOf(strArrSplit[0]).intValue();
                    Integer numValueOf = null;
                    if (strArrSplit.length > 1) {
                        try {
                            numValueOf = Integer.valueOf(strArrSplit[1]);
                            if (numValueOf.intValue() != -1 && (numValueOf.intValue() < 1 || numValueOf.intValue() > 99)) {
                                a("[Cannot Move Team - Ally group - Out of range]", networkConnection);
                                return true;
                            }
                        } catch (NumberFormatException e) {
                            a("[Cannot Move '" + playerTeam.teamName + "' - ally group '" + strArrSplit[1] + "' is not a number]", networkConnection);
                            return true;
                        }
                    }
                    boolean z3 = false;
                    if (iIntValue - 1 == -3) {
                        if (!this.roomSettings.allowSpectators) {
                            a("[Spectators are disabled on this server]", networkConnection);
                            return true;
                        }
                        synchronized (this.connectionLock) {
                            iIntValue = PlayerTeam.getFirstFreeTeamSlotId();
                            if (iIntValue != -1) {
                                a(playerTeam, -3);
                            }
                        }
                        z3 = true;
                    }
                    int i2 = playerTeam.teamColorId;
                    boolean z4 = i2 == -3;
                    if (!z3) {
                        if (iIntValue < 1 || iIntValue > PlayerTeam.TEAM_NEUTRAL) {
                            a("[Cannot Move '" + playerTeam.teamName + "' - target slotId must between 1-" + PlayerTeam.TEAM_NEUTRAL + "]", networkConnection);
                            return true;
                        }
                        synchronized (this.connectionLock) {
                            if (this.localPlayerTeam != playerTeam && (playerTeamK = PlayerTeam.k(iIntValue - 1)) != null && !playerTeamK.isTeamSpectator && !playerTeamK.isSpectatorTeamColor()) {
                                a("[Cannot move '" + playerTeam.teamName + "' to slot: " + iIntValue + " - Player: " + playerTeamK.teamName + " is in that slot.]", networkConnection);
                                return true;
                            }
                            a(playerTeam, iIntValue - 1);
                        }
                    }
                    playerTeam.teamColorId = i2;
                    if (numValueOf != null) {
                        if (numValueOf.intValue() == -1) {
                            playerTeam.teamColorId = playerTeam.teamId % 2;
                        } else {
                            playerTeam.teamColorId = numValueOf.intValue();
                        }
                    }
                    if (this.roomSettings.fixedAllyTeams) {
                        playerTeam.teamColorId = playerTeam.teamId % 2;
                    }
                    if (z3) {
                        playerTeam.teamColorId = -3;
                    }
                    if (z3) {
                        if (!z4) {
                            j("Player '" + playerTeam.teamName + "' is now a spectator");
                        }
                    } else {
                        j("Player '" + playerTeam.teamName + "' moved themselves to: " + iIntValue);
                    }
                    markPlayerUpdatePending();
                    MultiplayerBattleroomActivity.updateUI();
                    return true;
                } catch (NumberFormatException e2) {
                    a("[Cannot Move '" + playerTeam.teamName + "' - team '" + strArrSplit[0] + "' is not a number]", networkConnection);
                    return true;
                }
            }
            a("[Cannot Move '" + playerTeam.teamName + "' - No target]", networkConnection);
            return true;
        }
        if ("self_team".equals(lowerCase)) {
            if (playerTeam == null) {
                a("[Cannot Set Team - Player not found]", networkConnection);
                return true;
            }
            if (this.gameHasBeenStarted) {
                a("[" + playerTeam.teamName + ": Cannot Set Team - Game has been started]", networkConnection);
                return true;
            }
            if (o()) {
                a("[" + playerTeam.teamName + ": Cannot Set Team - Game is starting]", networkConnection);
                return true;
            }
            if (this.roomSettings.teamLock) {
                a("[" + playerTeam.teamName + ": Cannot Set Team - Teams locked]", networkConnection);
                return true;
            }
            if (this.roomSettings.fixedAllyTeams) {
                return true;
            }
            try {
                int iIntValue2 = Integer.valueOf(strTrim).intValue();
                if (iIntValue2 == -1) {
                    i = playerTeam.teamId % 2;
                } else {
                    if (iIntValue2 < 1 || iIntValue2 > 99) {
                        a("[Cannot Set Team - Out of range]", networkConnection);
                        return true;
                    }
                    i = iIntValue2 - 1;
                }
                if (playerTeam.teamColorId != i) {
                    playerTeam.teamColorId = i;
                    a("Player '" + playerTeam.teamName + "' team changed to: " + iIntValue2, networkConnection);
                }
                markPlayerUpdatePending();
                MultiplayerBattleroomActivity.updateUI();
                return true;
            } catch (NumberFormatException e3) {
                sendChatMessage("'" + strTrim + "' is not a number");
                return true;
            }
        }
        if ("surrender".equals(lowerCase)) {
            if (!this.gameHasBeenStarted) {
                a("[Cannot Surrender - Game has not started]", networkConnection);
                return true;
            }
            if (playerTeam == null) {
                a("[Could not find player]", networkConnection);
                return true;
            }
            if (!playerTeam.hasSurrenderVote()) {
                playerTeam.recordSurrenderVote();
                boolean canSurrender = playerTeam.canVoteToSurrender();
                GameEngine.log(str + ": Is voting to surrender (can surrender:" + canSurrender + ", afk:" + playerTeam.isTeamAutoStartQueued + ", defeated:" + playerTeam.isTeamWipedOut + ", disconnected:" + playerTeam.isTeamDisconnected() + ")");
                if (canSurrender) {
                    str3 = VariableScope.nullOrMissingString;
                } else {
                    str3 = "(Cannot vote) ";
                }
            } else {
                GameEngine.log(str + ": Is already voting to surrender but updating timestamp");
                playerTeam.recordSurrenderVote();
                str3 = "(Already voted) ";
            }
            a(networkConnection, playerTeam, str, "-t " + str3 + "[Votes to surrender " + (PlayerTeam.getSurrenderVoteCount(playerTeam.teamColorId) + "/" + PlayerTeam.getSurrenderEligibleCount(playerTeam.teamColorId)) + "]");
            return true;
        }
        return false;
    }

    public static void a(final PasswordHandler passwordHandler2) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine.networkEngine != null) {
            gameEngine.networkEngine.callbacks.onPasswordPrompt(passwordHandler2);
        }
        if (GameEngine.isNonAndroidVersion) {
            return;
        }
        AppFrameworkUtils.postRunnable(new Runnable() { // from class: com.corrodinggames.rts.gameFramework.j.ad.7
            @Override // java.lang.Runnable
            public void run() {
                MultiplayerBattleroomActivity.showPasswordDialog(passwordHandler2);
            }
        });
    }

    public ArrayList getActivePlayerTeamsSnapshot() {
        ArrayList activePlayerTeams;
        synchronized (this.connectionLock) {
            activePlayerTeams = PlayerTeam.getTeams();
        }
        return activePlayerTeams;
    }

    /* JADX INFO: renamed from: e */
    public void setGamePaused(boolean z) {
        this.gamePaused = z;
        if (this.gamePaused) {
            j("Game Paused");
        } else {
            j("Game unpaused");
        }
    }

    public void b(NetworkConnection networkConnection, String str) {
        networkConnection.handleRemoteDisconnect(false, false, str);
    }

    public void c(NetworkConnection networkConnection, String str) {
        for (NetworkConnection networkConnection2 : this.sendQueue) {
            if (networkConnection2.relayConnection == networkConnection) {
                b(networkConnection2, str);
            }
        }
    }

    public NetworkConnection a(NetworkConnection networkConnection, int i, String str, String str2) {
        GameEngine gameEngine = GameEngine.getInstance();
        NetworkConnection networkConnection2 = new NetworkConnection(this, new SteamSocket(networkConnection, i));
        networkConnection2.relayChannelId = i;
        networkConnection2.relayConnection = networkConnection;
        networkConnection2.remoteId = str;
        networkConnection2.forwardedIpAddress = str2;
        try {
            networkConnection2.startWorkers();
            gameEngine.networkEngine.sendQueue.add(networkConnection2);
            gameEngine.networkEngine.sendPlayerUpdateNow();
            return networkConnection2;
        } catch (Exception e) {
            e.printStackTrace();
            networkConnection2.sendPacket("crash");
            return null;
        }
    }

    public NetworkConnection a(NetworkConnection networkConnection, int i) {
        for (NetworkConnection networkConnection2 : this.sendQueue) {
            if (networkConnection2.relayChannelId == i && networkConnection2.relayConnection == networkConnection) {
                return networkConnection2;
            }
        }
        return null;
    }

    public static String p(String str) {
        String str2;
        String strReplace = str.trim().replace("\n", ".").replace("\r", ".").replace("\t", ".").replace("\u0000", ".").replace(" ", "_");
        while (true) {
            str2 = strReplace;
            if (!str2.startsWith(".") && !str2.startsWith("-") && !str2.startsWith(" ")) {
                break;
            }
            strReplace = str2.substring(1);
        }
        StringBuilder sb = new StringBuilder();
        for (char c2 : str2.toCharArray()) {
            if (!Character.isISOControl(c2)) {
                sb.append(c2);
            }
        }
        return sb.toString();
    }

    public void a(ArrayList arrayList, final boolean z) {
        if (this.socketConnector != null) {
            GameEngine.log("startJoinServerInternalThread: Already joining");
        } else if (arrayList.size() == 0) {
            GameEngine.log("startJoinServerInternalThread: no servers");
        } else {
            this.socketConnector = a((String) arrayList.get(0), false, new Runnable() { // from class: com.corrodinggames.rts.gameFramework.j.ad.8
                @Override // java.lang.Runnable
                public void run() {
                    GameEngine gameEngine = GameEngine.getInstance();
                    GameEngine.log("startJoinServerInternalThread callback");
                    SocketConnector socketConnector = NetworkEngine.this.socketConnector;
                    NetworkEngine.this.socketConnector = null;
                    if (socketConnector == null) {
                        GameEngine.log("startJoinServerInternalThread callback gameConnector==null");
                        return;
                    }
                    if (socketConnector.errorMessage != null) {
                        GameEngine.log("startJoinServerInternalThread failed to connect: " + socketConnector.errorMessage);
                        if (z) {
                            gameEngine.networkEngine.disconnectNetworking("Reconnect failed: " + socketConnector.errorMessage);
                            NetworkEngine.this.b("Reconnect failed", "reconnect failed");
                            gameEngine.setPendingMessageBox("Reconnect failed", "Reconnect failed: " + socketConnector.errorMessage);
                            gameEngine.alert("Reconnect failed: " + socketConnector.errorMessage);
                            return;
                        }
                        return;
                    }
                    try {
                        gameEngine.networkEngine.disconnectNetworking("starting new");
                        gameEngine.networkEngine.a(socketConnector.connectedSocket);
                    } catch (Exception e) {
                        gameEngine.showMessageBox(e.getMessage(), "Connection failed");
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
