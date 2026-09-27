package com.corrodinggames.rts.gameFramework;

import android.content.Context;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.ai.AIController;
import com.corrodinggames.rts.game.units.EditorOrBuilder;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.actions.AbstractUnitAction;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.game.units.custom.price.UnitPrice;
import com.corrodinggames.rts.gameFramework.file.FileHelper;
import com.corrodinggames.rts.gameFramework.network.ChecksumField;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.network.GameStateChecksum;
import com.corrodinggames.rts.gameFramework.network.NetworkEngine;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.ba */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/ba.class */
public class ReplayEngine {
    public static boolean b = true;
    public static boolean c = true;
    public static boolean d = false;
    public static boolean e = true;
    public static boolean f = false;
    public boolean h;
    int i;
        /* JADX INFO: renamed from: o */
    public int lastCommandTick;
        /* JADX INFO: renamed from: p */
    public int commandCount;
        /* JADX INFO: renamed from: q */
    public int replayVersion;
        /* JADX INFO: renamed from: r */
    public String replayName;
    /* JADX INFO: renamed from: O */
    public boolean hasGameSetupRead;
        /* JADX INFO: renamed from: j */
    int checksumCounter;
    /* JADX INFO: renamed from: k */
    boolean desyncWarningShown;
        /* JADX INFO: renamed from: l */
    int desyncCount;
        /* JADX INFO: renamed from: m */
    boolean checksumOkLogged;
    public volatile boolean P;
        /* JADX INFO: renamed from: s */
    boolean isStopped;
    /* JADX INFO: renamed from: t */
    String replayFilePath;
        /* JADX INFO: renamed from: u */
    boolean isReplaying;
    /* JADX INFO: renamed from: w */
    ReplayCommand nextCommand;
    /* JADX INFO: renamed from: x */
    ReplayCommand lastLoggedCommand;
        /* JADX INFO: renamed from: y */
    int commandId;
        /* JADX INFO: renamed from: z */
    int executedCommandCount;
        /* JADX INFO: renamed from: A */
    int recordedCommandCount;
        /* JADX INFO: renamed from: B */
    int recordedResyncCount;
    /* JADX INFO: renamed from: C */
    InputStream fileInputStream;
    /* JADX INFO: renamed from: D */
    BufferedInputStream bufferedInput;
    /* JADX INFO: renamed from: E */
    DataInputStream dataInputStream;
    /* JADX INFO: renamed from: F */
    GameInputStream gameInputStream;
    /* JADX INFO: renamed from: G */
    OutputStream fileOutput;
    /* JADX INFO: renamed from: H */
    BufferedOutputStream bufferedOutput;
    /* JADX INFO: renamed from: I */
    DataOutputStream dataOutputStream;
    /* JADX INFO: renamed from: J */
    GameOutputStream gameOutputStream;
    /* JADX INFO: renamed from: K */
    ReplayWriter replayWriter;
    /* JADX INFO: renamed from: L */
    Thread writerThread;
    String a = "replays/";
    public GameStateChecksum g = new GameStateChecksum();
    public boolean n = false;
    public int v = 1;
    Object M = new Object();
    public boolean N = false;

    public static void a(String str) {
        GameEngine.log("Replay: " + str);
    }

    public static void b(String str) {
        GameEngine.logColored("Replay: " + str);
    }

    public static void a(String str, Exception exc) {
        GameEngine.log("Replay: " + str, (Throwable) exc);
    }

    public File a(String str, boolean z) {
        return FileHelper.getRWFile(str, this.a, z);
    }

    public void a(Context context) {
    }

    public void a() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine.gameSpeed != 0.0f) {
            gameEngine.gameSpeed = 0.0f;
        } else {
            gameEngine.gameSpeed = 1.0f;
        }
    }

    public void b() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine.gameSpeed == 1.0f) {
            gameEngine.gameSpeed = 2.0f;
            return;
        }
        if (gameEngine.gameSpeed == 2.0f) {
            gameEngine.gameSpeed = 4.0f;
            return;
        }
        if (gameEngine.gameSpeed == 4.0f) {
            gameEngine.gameSpeed = 8.0f;
            return;
        }
        if (gameEngine.gameSpeed == 8.0f) {
            gameEngine.gameSpeed = 16.0f;
            return;
        }
        if (gameEngine.gameSpeed == 16.0f) {
            gameEngine.gameSpeed = 32.0f;
            return;
        }
        if (gameEngine.gameSpeed == 32.0f) {
            gameEngine.gameSpeed = 64.0f;
        } else if (gameEngine.gameSpeed == 64.0f) {
            gameEngine.gameSpeed = 1.0f;
        } else {
            gameEngine.gameSpeed = 1.0f;
        }
    }

    public void a(int i, String str, String str2, int i2) {
        ReplayWriter replayWriter = this.replayWriter;
        if (this.P && !this.isReplaying) {
            if (str2.startsWith("-t ")) {
            }
            ReplayCommand replayCommand = new ReplayCommand();
            replayCommand.tick = i2;
            replayCommand.chatMessage = new ChatMessage();
            replayCommand.chatMessage.a = i;
            replayCommand.chatMessage.b = str;
            replayCommand.chatMessage.c = str2;
            if (replayWriter == null) {
                GameEngine.logWarningAndStack("Failed to record chat message, replay might have already stopped");
            } else {
                replayWriter.a(replayCommand);
            }
        }
    }

    public void a(byte[] bArr, int i, int i2, int i3, float f2, float f3) {
        ReplayWriter replayWriter = this.replayWriter;
        if (this.P && !this.isReplaying) {
            ReplayCommand replayCommand = new ReplayCommand();
            replayCommand.tick = i;
            replayCommand.resyncData = bArr;
            replayCommand.resyncTick = i2;
            replayCommand.resyncGameTimeMillis = i3;
            replayCommand.resyncStepRate = f2;
            replayCommand.k = f3;
            if (replayWriter == null) {
                GameEngine.logWarningAndStack("Failed to save resync, replay might have already stopped");
            } else {
                replayWriter.a(replayCommand);
            }
        }
    }

    public void c() throws IOException {
        if (f) {
            d();
        }
    }

    public void a(Command command, int i) {
        ReplayWriter replayWriter = this.replayWriter;
        if (this.P && !this.isReplaying) {
            if (replayWriter == null) {
                GameEngine.logWarningAndStack("Failed to record command, replay might have already stopped");
                return;
            }
            ReplayCommand replayCommand = new ReplayCommand();
            replayCommand.command = command.cloneCommand();
            replayCommand.tick = i;
            replayWriter.a(replayCommand);
            this.checksumCounter++;
            if (this.checksumCounter > 5) {
                this.checksumCounter = 0;
                GameEngine gameEngine = GameEngine.getInstance();
                ReplayCommand replayCommand2 = new ReplayCommand();
                replayCommand2.checksum = f();
                replayCommand2.tick = gameEngine.currentTick;
                replayWriter.a(replayCommand2);
            }
        }
    }

    public void d() throws IOException {
        if (this.P && !this.isReplaying) {
            this.g.computeChecksums();
            a(this.g, true);
        }
    }

    public void a(GameStateChecksum gameStateChecksum) throws IOException {
        a(gameStateChecksum, false);
    }

    public void a(GameStateChecksum gameStateChecksum, boolean z) throws IOException {
        if (this.P && !this.isReplaying) {
            GameEngine gameEngine = GameEngine.getInstance();
            ReplayCommand replayCommand = new ReplayCommand();
            GameOutputStream gameOutputStream = new GameOutputStream();
            int i = 0;
            if (z) {
                i = 0 + 1;
            }
            gameOutputStream.writeByte(i);
            gameOutputStream.writeInt(gameStateChecksum.fields.size());
            Iterator it = gameStateChecksum.fields.iterator();
            while (it.hasNext()) {
                gameOutputStream.writeLong(((ChecksumField) it.next()).value);
            }
            replayCommand.checksumData = gameOutputStream.toByteArray();
            replayCommand.tick = gameEngine.currentTick;
            this.replayWriter.a(replayCommand);
        }
    }

    /* JADX WARN: Finally extract failed */
    public void e() {
        synchronized (this.M) {
            try {
                try {
                    if (this.replayWriter != null) {
                        this.replayWriter.a();
                        try {
                            this.writerThread.join();
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                        this.P = false;
                        this.replayWriter = null;
                        this.writerThread = null;
                    }
                    if (this.fileOutput != null) {
                        this.dataOutputStream.flush();
                        this.dataOutputStream.close();
                        this.bufferedOutput.flush();
                        this.bufferedOutput.close();
                        this.fileOutput.flush();
                        this.fileOutput.close();
                    }
                    this.fileOutput = null;
                    this.bufferedOutput = null;
                    this.dataOutputStream = null;
                    this.gameOutputStream = null;
                } catch (IOException e3) {
                    e3.printStackTrace();
                    this.fileOutput = null;
                    this.bufferedOutput = null;
                    this.dataOutputStream = null;
                    this.gameOutputStream = null;
                }
                this.isStopped = false;
                this.P = false;
                this.isReplaying = false;
                this.replayFilePath = null;
                this.i = 0;
                this.checksumCounter = 0;
                this.desyncWarningShown = false;
                this.desyncCount = 0;
                this.checksumOkLogged = false;
                this.commandId = 0;
                this.v = 1;
                this.executedCommandCount = 0;
                this.recordedCommandCount = 0;
                this.recordedResyncCount = 0;
                this.lastCommandTick = -1;
                this.commandCount = 0;
                this.replayVersion = -1;
                this.replayName = null;
                try {
                    try {
                        if (this.fileInputStream != null) {
                            this.dataInputStream.close();
                            this.bufferedInput.close();
                            this.fileInputStream.close();
                        }
                        this.fileInputStream = null;
                        this.bufferedInput = null;
                        this.dataInputStream = null;
                        this.gameInputStream = null;
                    } catch (IOException e4) {
                        e4.printStackTrace();
                        this.fileInputStream = null;
                        this.bufferedInput = null;
                        this.dataInputStream = null;
                        this.gameInputStream = null;
                    }
                } catch (Throwable th) {
                    this.fileInputStream = null;
                    this.bufferedInput = null;
                    this.dataInputStream = null;
                    this.gameInputStream = null;
                    throw th;
                }
            } catch (Throwable th2) {
                this.fileOutput = null;
                this.bufferedOutput = null;
                this.dataOutputStream = null;
                this.gameOutputStream = null;
                throw th2;
            }
        }
    }

    public long f() {
        long j = 0;
        for (GameObject gameObject : GameObject.fastGameObjectList) {
            if (gameObject instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) gameObject;
                j = ((long) (((long) (((long) (j + (orderableUnit.posX * 1000.0f))) + (orderableUnit.posY * 1000.0f))) + (orderableUnit.currentHealth * 1.0f))) + orderableUnit.objectId;
            }
        }
        return j;
    }

    public void g() {
        if (!this.N) {
            e();
        }
    }

    public boolean loadReplay(String str)  {
        return a(str, a(str, false));
    }

    private void l() {
        for (int i = 0; i < PlayerTeam.TEAM_NEUTRAL; i++) {
            PlayerTeam playerTeamK = PlayerTeam.k(i);
            if (playerTeamK != null && (playerTeamK instanceof AIController)) {
                ((AIController) playerTeamK).canBuild = true;
            }
        }
    }

    public boolean a(String str, File file) {
        if (this.P) {
            if (this.isReplaying) {
                GameEngine.logColored("startReplayingFile: A replay is already playing");
            } else {
                GameEngine.logColored("startReplayingFile: A replay is already saving");
            }
        }
        e();
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.stopAndReset();
        gameEngine.networkEngine.q();
        this.nextCommand = null;
        this.isStopped = false;
        this.P = true;
        this.isReplaying = true;
        this.replayFilePath = str;
        try {
            if (file.isDirectory()) {
                GameEngine.log("File is a directory: " + file.getAbsolutePath());
                GameEngine.log("Cannot load replay: Target is a folder, instead of a file");
                gameEngine.alert("Cannot load replay: Target is a folder, instead of a file", 1);
                return false;
            }
            this.fileInputStream = FileHelper.openFile(file);
            if (this.fileInputStream == null) {
                GameEngine.log("Cannot load replay: Failed to read replay file");
                gameEngine.alert("Cannot load replay: Failed to read replay file", 1);
                return false;
            }
            this.bufferedInput = new BufferedInputStream(this.fileInputStream);
            this.dataInputStream = new DataInputStream(this.bufferedInput);
            this.gameInputStream = new GameInputStream(this.dataInputStream);
            String utf = this.gameInputStream.readUTF();
            if (!utf.equals("rustedWarfareReplay")) {
                GameEngine.log("Header is not correct:" + utf);
                GameEngine.log("Cannot load replay: File is missing header (check if this file is a replay)");
                gameEngine.alert("Cannot load replay: File is missing header (check if this file is a replay)", 1);
                return false;
            }
            int i = this.gameInputStream.readInt();
            int i2 = this.gameInputStream.readInt();
            a("Loading save from version: " + i2);
            this.gameInputStream.setProtocolVersion(i2);
            String utf2 = this.gameInputStream.readUTF();
            if ((i2 != 96 || i != gameEngine.getVersionCode(true)) && !this.n) {
                String str2 = "Cannot load replay: This replay was recording with a different version: " + utf2;
                if (GameEngine.isPC()) {
                    str2 = str2 + " (You can use the beta tab in steam to switch to old versions)";
                }
                gameEngine.alert(str2, 1);
                a("Replay version: " + i2 + " (" + i + ")");
                a("GameSaver.thisSaveVersion: 96 (" + gameEngine.getVersionCode(true) + ")");
                if (!GameEngine.isOldReplayMode) {
                    this.P = false;
                    return false;
                }
            }
            this.replayVersion = i2;
            this.replayName = utf2;
            this.gameInputStream.readBoolean();
            this.gameInputStream.startBlockNamed("gamesave");
            this.hasGameSetupRead = false;
            this.N = true;
            a("Loading replay initial save");
            gameEngine.gameSaver.readSaveFromStream(this.gameInputStream, false, false, false);
            this.N = false;
            this.gameInputStream.d("gamesave");
            if (!this.hasGameSetupRead) {
                a("ReplayEngine: --- No game setup read ----");
                gameEngine.networkEngine.roomSettings.noNukes = true;
                gameEngine.maxUnitCap = gameEngine.settingsEngine.teamUnitCapHostedGame;
                gameEngine.currentUnitCap = gameEngine.maxUnitCap;
            }
            if (!this.h) {
                l();
            }
            a("--- Reply settings ---");
            a("Unit cap: " + gameEngine.maxUnitCap);
            a(gameEngine.networkEngine.roomSettings.getSettingsSummary());
            a("Starting frame:" + gameEngine.currentTick);
            if (!this.h) {
                for (int i3 = 0; i3 < PlayerTeam.TEAM_NEUTRAL; i3++) {
                    PlayerTeam playerTeamK = PlayerTeam.k(i3);
                    if (playerTeamK != null && playerTeamK.teamName != null) {
                        gameEngine.gameUI.messageManager.addMessage(VariableScope.nullOrMissingString, "Player '" + playerTeamK.teamName + "' playing as " + playerTeamK.getTeamColorDisplayName().toLowerCase() + " (team:" + playerTeamK.getTeamSlotLabel() + ")");
                    }
                }
            }
            if (GameEngine.isReplayDebugMode) {
                NetworkEngine.reportDesync("Warning: editor will desync checksums.");
                gameEngine.isGameStarted = true;
                gameEngine.isDebugTempMode = true;
                gameEngine.isTriggerDebugMode = true;
            }
            return true;
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }
    }

    public void a(boolean z) {
        if (GameEngine.isPCOrIOSVersion) {
            if (!GameEngine.isReplayRecordingEnabledOnPCOrIOS) {
                return;
            }
        } else if (!GameEngine.isReplayRecordingEnabledOnNonPC) {
            return;
        }
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine.networkEngine.networkGameActive && !z && !this.N && gameEngine.settingsEngine.saveMultiplayerReplays) {
            d(gameEngine.getCurrentMapName() + " [v" + gameEngine.getVersionString() + "] (" + Utility.formatCurrentDate("d MMM yyyy HH.mm.ss") + ").replay");
        }
    }

    public void d(String str) {
        a("Recording replay to: " + str);
        if (this.P) {
            if (this.isReplaying) {
                b("startSaving: A replay is already playing");
            } else {
                b("startSaving: A replay is already saving");
            }
        }
        e();
        GameEngine gameEngine = GameEngine.getInstance();
        f = gameEngine.settingsEngine.replayTracing;
        if (f) {
            gameEngine.networkEngine.j("Warning traceChecksumsWriting is on. Large replay file size will be created.");
        }
        this.isStopped = false;
        this.P = true;
        this.isReplaying = false;
        this.replayFilePath = str;
        try {
            File fileA = a(str, true);
            this.fileOutput = FileHelper.openOutputStream(fileA, false);
            if (this.fileOutput == null) {
                b("Failed to create replay file at:" + fileA.getAbsolutePath());
                GameEngine.getInstance().alert("Failed to create replay file (Replay recording will be disabled)");
                e();
                return;
            }
            this.bufferedOutput = new BufferedOutputStream(this.fileOutput);
            this.dataOutputStream = new DataOutputStream(this.bufferedOutput);
            this.gameOutputStream = new GameOutputStream(this.dataOutputStream);
            this.gameOutputStream.writeStringUTF("rustedWarfareReplay");
            this.gameOutputStream.writeInt(gameEngine.getVersionCode(true));
            this.gameOutputStream.writeInt(96);
            this.gameOutputStream.writeStringUTF(gameEngine.getVersionString());
            this.gameOutputStream.writeBoolean(gameEngine.isDemo);
            this.gameOutputStream.startBlock("gamesave");
            gameEngine.gameSaver.writeSaveToStream(this.gameOutputStream);
            this.gameOutputStream.endBlock("gamesave");
            this.dataOutputStream.flush();
            this.replayWriter = new ReplayWriter(this);
            this.writerThread = new Thread(this.replayWriter);
            this.writerThread.start();
        } catch (IOException e2) {
            a("Failed to start recording replay", e2);
            GameEngine.getInstance().alert("Failed to start recording replay: " + e2.getMessage());
            e();
        } catch (Exception e3) {
            a("Failed to start recording replay (Non IOException)", e3);
            GameEngine.getInstance().alert("Failed to start recording replay (Non IOException): " + e3.getMessage());
            e();
        }
    }

    public boolean h() throws IOException {
        GameEngine gameEngine = GameEngine.getInstance();
        String strStartBlockAndGetName = this.gameInputStream.startBlockAndGetName();
        if ("rc".equals(strStartBlockAndGetName)) {
            this.commandId++;
            ReplayCommand replayCommand = new ReplayCommand();
            replayCommand.tick = this.gameInputStream.readInt();
            Command commandCreateCommand = gameEngine.commandController.createCommand();
            commandCreateCommand.deserializeCommand(this.gameInputStream);
            commandCreateCommand.isReplayCommand = true;
            replayCommand.command = commandCreateCommand;
            this.gameInputStream.d("rc");
            this.nextCommand = replayCommand;
            this.commandCount++;
            this.lastCommandTick = replayCommand.tick;
            if (c) {
                a("updateGameFrame: Command: " + commandCreateCommand.team.teamName + " (" + commandCreateCommand.team.teamId + ") count:" + commandCreateCommand.getAffectedUnitCount() + " id:" + this.commandId);
                if (commandCreateCommand.unitCommand != null) {
                    a("updateGameFrame: Waypoint: " + commandCreateCommand.unitCommand.getCommandType().name());
                    if (commandCreateCommand.unitCommand.getBuildUnitType() != null) {
                        a("updateGameFrame: Build Type: " + commandCreateCommand.unitCommand.getBuildUnitType().getUnitTypeDescriptionShort());
                    }
                }
                if (AbstractUnitAction.isActionIdSpecified(commandCreateCommand.actionId)) {
                    a("updateGameFrame: SpecialAction: " + commandCreateCommand.actionId.getId());
                }
                if (commandCreateCommand.attackMode != null) {
                    a("updateGameFrame: SetAttackMode: " + commandCreateCommand.attackMode);
                }
                if (commandCreateCommand.stopCurrentAction) {
                    a("updateGameFrame: stopOrUndo is set");
                }
                if (commandCreateCommand.isSystemAction) {
                    if (commandCreateCommand.gameSpeedChange != 0.0f) {
                        a("updateGameFrame: changeStepRate:" + commandCreateCommand.gameSpeedChange);
                    }
                    if (commandCreateCommand.systemActionType != 0) {
                        a("updateGameFrame: systemAction_action:" + commandCreateCommand.systemActionType);
                    }
                }
                a("updateGameFrame: ------");
                return true;
            }
            return true;
        }
        if ("wait".equals(strStartBlockAndGetName)) {
            ReplayCommand replayCommand2 = new ReplayCommand();
            replayCommand2.tick = this.gameInputStream.readInt();
            replayCommand2.isChecksumCommand = true;
            this.nextCommand = replayCommand2;
            this.gameInputStream.d("wait");
            return true;
        }
        if ("cs".equals(strStartBlockAndGetName)) {
            int i = this.gameInputStream.readInt();
            long j = this.gameInputStream.readLong();
            if (!this.n) {
                if (gameEngine.currentTick != i) {
                    GameEngine.log("replay:updateGameFrame", "expected:" + i + " got:" + gameEngine.currentTick);
                }
                if (f() != j) {
                    b("checksum: checksums don't match!!");
                    b("checksum: game frameNumber:" + gameEngine.currentTick);
                    b("checksum: Replay checksum:" + j);
                    b("checksum: Game checksum  :" + f());
                    this.desyncCount++;
                    if (!this.desyncWarningShown) {
                        this.desyncWarningShown = true;
                        gameEngine.gameUI.messageManager.addMessage(VariableScope.nullOrMissingString, "Error: This replay might be out of sync");
                    }
                } else {
                    a("checksum: checksums are matching frameNumber:" + gameEngine.currentTick);
                }
            }
            this.gameInputStream.d("cs");
            return true;
        }
        if ("es".equals(strStartBlockAndGetName)) {
            int i2 = this.gameInputStream.readInt();
            if (!this.n) {
                if (gameEngine.currentTick != i2) {
                    GameEngine.logColored("replay.updateGameFrame: expected:" + i2 + " got:" + gameEngine.currentTick);
                }
                GameInputStream gameInputStream = new GameInputStream(this.gameInputStream.readBytesWithLength());
                boolean z = false;
                if (UnitPrice.a(gameInputStream.readByte(), 1)) {
                    z = true;
                }
                if (z) {
                    GameEngine.log("replay: -trace checksum-");
                } else {
                    GameEngine.log("replay: -long checksum-");
                }
                gameEngine.networkEngine.resetSyncChecksumState();
                gameInputStream.readInt();
                for (ChecksumField checksumField : gameEngine.networkEngine.stateChecksum.fields) {
                    long j2 = gameInputStream.readLong();
                    if (!this.checksumOkLogged && j2 == checksumField.value) {
                        a("extraChecksum: " + checksumField.label + " Checksum [" + i2 + "]. " + j2 + " == " + checksumField.value + " (ok)");
                    }
                    if (j2 != checksumField.value) {
                        if (this.desyncCount < 150) {
                            b("extraChecksum: " + checksumField.label + " Checksum [" + i2 + "]. " + j2 + " != " + checksumField.value + " (failed)");
                        }
                        this.desyncCount++;
                    }
                }
            }
            this.checksumOkLogged = true;
            this.gameInputStream.d("es");
            return true;
        }
        if ("resync".equals(strStartBlockAndGetName)) {
            int i3 = this.gameInputStream.readInt();
            GameEngine.log("Loading resync from replay");
            if (gameEngine.currentTick != i3) {
                GameEngine.log("replay:resync", "expected:" + i3 + " got:" + gameEngine.currentTick);
            }
            int i4 = this.gameInputStream.readInt();
            int i5 = this.gameInputStream.readInt();
            float f2 = this.gameInputStream.readFloat();
            float f3 = this.gameInputStream.readFloat();
            gameEngine.gameSaver.readSaveFromStream(new GameInputStream(this.gameInputStream.readBytesWithLength()), true, true, true);
            l();
            gameEngine.currentTick = i4;
            gameEngine.gameTimeMillis = i5;
            gameEngine.networkEngine.stateChecksum.totalChecksum = 0L;
            if (f2 < 0.1d) {
                NetworkEngine.reportDesyncVerbose("replay setCurrentStepRate:" + f2 + " is too small", true);
            }
            gameEngine.networkEngine.applyChangedSetup(f2, "replay");
            gameEngine.networkEngine.J = f3;
            this.gameInputStream.d("resync");
            return true;
        }
        if ("chat".equals(strStartBlockAndGetName)) {
            ReplayCommand replayCommand3 = new ReplayCommand();
            replayCommand3.tick = this.gameInputStream.readInt();
            replayCommand3.chatMessage = new ChatMessage();
            replayCommand3.chatMessage.a = this.gameInputStream.readInt();
            replayCommand3.chatMessage.b = this.gameInputStream.readNullableString();
            replayCommand3.chatMessage.c = this.gameInputStream.readNullableString();
            this.nextCommand = replayCommand3;
            this.gameInputStream.d("chat");
            return true;
        }
        if ("end".equals(strStartBlockAndGetName)) {
            GameEngine.log("replay:updateGameFrame", "end of replay block found");
            gameEngine.gameUI.messageManager.addMessage(VariableScope.nullOrMissingString, "Replay has ended");
            if (!gameEngine.isGameStarted) {
                this.isStopped = true;
                gameEngine.gameSpeed = 0.25f;
                GameEngine.getInstance().gameUI.startGameEndSequence();
            } else {
                this.isStopped = false;
                this.P = false;
                this.isReplaying = false;
                EditorOrBuilder editorOrBuilder = gameEngine.gameUI.getEditorOrBuilder();
                if (editorOrBuilder != null) {
                    gameEngine.playerTeam = editorOrBuilder.team;
                }
            }
            this.gameInputStream.d("end");
            GameEngine.log("number of replay commands issued:" + this.executedCommandCount);
            return false;
        }
        if ("endReplayMetaData".equals(strStartBlockAndGetName)) {
            this.gameInputStream.d("endReplayMetaData");
            return true;
        }
        GameEngine.log("updateGameFrame", "Unknown command block:" + strStartBlockAndGetName);
        this.gameInputStream.d(strStartBlockAndGetName);
        return true;
    }

    /* JADX INFO: renamed from: a */
    public void update(float f2) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.isStopped || !this.P || !this.isReplaying) {
            return;
        }
        while (true) {
            if (this.nextCommand == null) {
                try {
                    if (!h()) {
                        return;
                    }
                } catch (IOException e2) {
                    GameEngine.log("updateGameFrame", "IOException, read of replay?");
                    e2.printStackTrace();
                    gameEngine.gameSpeed = 0.25f;
                    if (!this.isStopped && this.P) {
                        gameEngine.gameUI.messageManager.addMessage(VariableScope.nullOrMissingString, "Replay ended (unexpected)");
                    }
                    this.isStopped = true;
                    return;
                }
            }
            if (this.nextCommand != null) {
                if (this.n) {
                    this.nextCommand = null;
                } else {
                    if (b && this.nextCommand != null && this.lastLoggedCommand != this.nextCommand) {
                        this.lastLoggedCommand = this.nextCommand;
                        GameEngine.log("replay: upcoming in " + (this.nextCommand.tick - gameEngine.currentTick) + " command:" + (this.nextCommand.command != null));
                    }
                    if (this.nextCommand.isChecksumCommand && this.executedCommandCount == 0) {
                        GameEngine.log("updateGameFrame: replay: Skipping wait on first resync without commands to avoid delay");
                        this.nextCommand = null;
                    } else if (gameEngine.currentTick >= this.nextCommand.tick) {
                        if (this.nextCommand.command != null) {
                            if (gameEngine.currentTick > this.nextCommand.tick) {
                                GameEngine.logColored("updateGameFrame: replay incorrect frameNumber, skipping command:" + gameEngine.currentTick + " vs " + this.nextCommand.tick);
                            } else {
                                if (d) {
                                    if (this.nextCommand.command.sourceTeam == null) {
                                        GameEngine.log("Precommand Team: commandingPlayer==null");
                                        if (this.nextCommand.command.team != null) {
                                            GameEngine.log("Precommand Team id:" + this.nextCommand.command.team.teamId + " credits:" + this.nextCommand.command.team.credits);
                                        }
                                    } else {
                                        GameEngine.log("Precommand Team id:" + this.nextCommand.command.sourceTeam.teamId + " credits:" + this.nextCommand.command.sourceTeam.credits + " count:" + this.nextCommand.command.sourceTeam.getNonBuildingUnitCountIncludingQueued() + " max:" + this.nextCommand.command.sourceTeam.getUnitCap());
                                    }
                                }
                                if (this.nextCommand.command.isSystemAction && this.nextCommand.command.systemActionType != 0) {
                                    GameEngine.log("replay:issueCommand", "systemAction_action:" + this.nextCommand.command.systemActionType);
                                }
                                this.nextCommand.command.executeCommand();
                                if (d) {
                                    if (this.nextCommand.command.sourceTeam != null) {
                                        GameEngine.log("Postcommand credits:" + this.nextCommand.command.sourceTeam.credits + " count:" + this.nextCommand.command.sourceTeam.getNonBuildingUnitCountIncludingQueued() + " max:" + this.nextCommand.command.sourceTeam.getUnitCap());
                                    } else if (this.nextCommand.command.team != null) {
                                        GameEngine.log("Postcommand Team id:" + this.nextCommand.command.team.teamId + " credits:" + this.nextCommand.command.team.credits);
                                    }
                                }
                                this.executedCommandCount++;
                            }
                        } else if (this.nextCommand.chatMessage != null) {
                            ChatMessage chatMessage = this.nextCommand.chatMessage;
                            boolean z = false;
                            if (chatMessage.c == null) {
                                z = true;
                            } else {
                                if (chatMessage.c.startsWith("-i ")) {
                                    z = true;
                                }
                                if (chatMessage.c.equals("<All players ready>")) {
                                    z = true;
                                }
                                if (chatMessage.c.equals("--too many desync errors, suppressing output--")) {
                                    z = true;
                                }
                                if (chatMessage.c.startsWith("desync:")) {
                                    z = true;
                                }
                            }
                            if (!gameEngine.settingsEngine.replaysShowRecordedChat) {
                                z = true;
                            }
                            if (z) {
                                GameEngine.log("replay:updateGameFrame", "Skipping message: " + chatMessage.b + ":" + chatMessage.c);
                            } else {
                                GameEngine.log("replay:updateGameFrame", "message: " + chatMessage.b + ":" + chatMessage.c);
                                gameEngine.gameUI.messageManager.addMessage(chatMessage.b, chatMessage.c);
                            }
                        } else if (this.nextCommand.isChecksumCommand) {
                            if (c) {
                            }
                        } else {
                            GameEngine.log("updateGameFrame", "error: lastReadCommand null action");
                        }
                        this.nextCommand = null;
                    }
                }
            }
            if (this.nextCommand != null) {
                return;
            }
        }
    }

    public void e(String str) {
        GameEngine.log("ReplayEngine deleteGame: " + str);
        String strMapPath = FileHelper.fixPath(str);
        if (strMapPath.contains("\\") || strMapPath.contains("/")) {
            GameEngine.log("Cannot get replay with path: " + str);
            return;
        }
        File fileA = a(str, true);
        GameEngine.log("ReplayEngine path: " + fileA.getAbsolutePath());
        if (!fileA.exists()) {
            GameEngine.log("ReplayEngine deleteGame: file doesn't exist");
        }
        if (!FileHelper.deleteDirectory(fileA)) {
            GameEngine.log("ReplayEngine deleteGame: failed to delete: " + fileA.getAbsolutePath());
        }
        File fileA2 = a(str + ".map", true);
        if (fileA2.exists()) {
            FileHelper.deleteDirectory(fileA2);
        }
    }

    public boolean i() {
        return this.P;
    }

    public boolean j() {
        return this.P && this.isReplaying;
    }

    public boolean k() {
        return this.P && !this.isReplaying;
    }
}
