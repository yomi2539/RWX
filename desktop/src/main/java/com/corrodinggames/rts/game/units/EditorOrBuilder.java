package com.corrodinggames.rts.game.units;

import android.graphics.Paint;
import android.graphics.PointF;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.Projectile;
import com.corrodinggames.rts.game.ai.AIController;
import com.corrodinggames.rts.game.units.actions.*;
import com.corrodinggames.rts.game.units.buildings.FactoryQueueInterface;
import com.corrodinggames.rts.game.units.buildings.NukeLauncher;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfigParser;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.game.units.custom.price.UnitPrice;
import com.corrodinggames.rts.game.units.land.BuilderUnit;
import com.corrodinggames.rts.game.units.land.LandUnit;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.GameObject;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.audio.SoundEngine;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.local.Locale;
import com.corrodinggames.rts.gameFramework.mod.ModInfo;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.network.NetworkEngine;
import com.corrodinggames.rts.gameFramework.network.PasswordHandler;
import com.corrodinggames.rts.gameFramework.ui.GameUI;
import com.corrodinggames.rts.gameFramework.ui.widgets.MenuDialog;
import com.corrodinggames.rts.gameFramework.ui.widgets.UIEvent;
import com.corrodinggames.rts.gameFramework.ui.widgets.UIEventHandler;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.h */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/h.class */
public class EditorOrBuilder extends LandUnit implements UnitPathPoints {

    /* JADX INFO: renamed from: a */
    PointF[] controlPoints;

    /* JADX INFO: renamed from: b */
    PointF[] targetPoints;

    /* JADX INFO: renamed from: f */
    static Paint editorSearchPaint;

    /* JADX INFO: renamed from: d */
    static Paint targetPointPaints;

    /* JADX INFO: renamed from: e */
    static Paint editorSelectionPaint;
    /* JADX INFO: renamed from: h */
    static AbstractUnitAction reloadUnitsAction = new AbstractUnitAction("reloadUnits") { // from class: com.corrodinggames.rts.game.units.h.1
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z2) {
            if (EditorOrBuilder.w()) {
                return false;
            }
            return true;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Reload all unit data from disk (for modding)";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return "Reload units";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return 0;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public int getActiveCount(BaseUnit baseUnit, boolean z2) {
            return -1;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g_, reason: merged with bridge method [inline-methods] */
        public UnitTypeEnum getUnitType() {
            return null;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public ActionType getActionType() {
            return ActionType.none;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public ActionDisplayType getActionDisplayType() {
            return ActionDisplayType.action;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: h */
        public boolean alwaysShowsTooltip() {
            return true;
        }
    };

    /* JADX INFO: renamed from: g */
    static Texture editorSelectionTexture2;
    /* JADX INFO: renamed from: i */
    static AbstractUnitAction reloadOnlyActiveUnitsAction = new AbstractUnitAction("reloadOnlyActiveUnits") { // from class: com.corrodinggames.rts.game.units.h.12
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z2) {
            if (EditorOrBuilder.w()) {
                return false;
            }
            return true;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Reload data only for active units on map (for modding). This is a faster than reload but will be incomplete.";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return "Quick reload";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return 0;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public int getActiveCount(BaseUnit baseUnit, boolean z2) {
            return -1;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: k, reason: merged with bridge method [inline-methods] */
        public UnitTypeEnum getUnitType() {
            return null;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public ActionType getActionType() {
            return ActionType.none;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public ActionDisplayType getActionDisplayType() {
            return ActionDisplayType.action;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: h */
        public boolean alwaysShowsTooltip() {
            return true;
        }
    };
    static ArrayList D;
    ModInfo E;
    EditorUnitTypeFilter F;
    EditorTab G;
    String H;
    boolean I;
    String J;
    /* JADX INFO: renamed from: j */
    static AbstractUnitAction unitCloneAction = new AbstractUnitAction("unitClone") { // from class: com.corrodinggames.rts.game.units.h.17
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Clones units at point x50";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return "Unit Clone";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return 0;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public int getActiveCount(BaseUnit baseUnit, boolean z2) {
            return -1;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: k, reason: merged with bridge method [inline-methods] */
        public UnitTypeEnum getUnitType() {
            return null;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public ActionType getActionType() {
            return ActionType.targetGround;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public ActionDisplayType getActionDisplayType() {
            return ActionDisplayType.action;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z2) {
            return true;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: h */
        public boolean alwaysShowsTooltip() {
            return true;
        }
    };
    /* JADX INFO: renamed from: k */
    static AbstractUnitAction removeUnitsAction = new AbstractUnitAction("removeUnits") { // from class: com.corrodinggames.rts.game.units.h.18
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Delete all units at a point";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return "Delete units at";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: h_ */
        public boolean shouldShowDisplayText() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return 0;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public int getActiveCount(BaseUnit baseUnit, boolean z2) {
            return -1;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: i_, reason: merged with bridge method [inline-methods] */
        public UnitTypeEnum getUnitType() {
            return null;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public ActionType getActionType() {
            return ActionType.targetGround;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public ActionDisplayType getActionDisplayType() {
            return ActionDisplayType.action;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z2) {
            return true;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: h */
        public boolean alwaysShowsTooltip() {
            return true;
        }
    };
    /* JADX INFO: renamed from: l */
    static AbstractUnitAction killUnitsAction = new AbstractUnitAction("killUnits") { // from class: com.corrodinggames.rts.game.units.h.19
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Kill units at a point";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return "Kill units at";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: h_ */
        public boolean shouldShowDisplayText() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return 0;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public int getActiveCount(BaseUnit baseUnit, boolean z2) {
            return -1;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: j_, reason: merged with bridge method [inline-methods] */
        public UnitTypeEnum getUnitType() {
            return null;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public ActionType getActionType() {
            return ActionType.targetGround;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public ActionDisplayType getActionDisplayType() {
            return ActionDisplayType.action;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z2) {
            return true;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: h */
        public boolean alwaysShowsTooltip() {
            return true;
        }
    };
    /* JADX INFO: renamed from: m */
    static AbstractUnitAction finishQueueAction = new AbstractUnitAction("finishQueue") { // from class: com.corrodinggames.rts.game.units.h.20
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Finish all unit queues at";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return "Finish queue at";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: h_ */
        public boolean shouldShowDisplayText() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return 0;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public int getActiveCount(BaseUnit baseUnit, boolean z2) {
            return -1;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: k_, reason: merged with bridge method [inline-methods] */
        public UnitTypeEnum getUnitType() {
            return null;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public ActionType getActionType() {
            return ActionType.targetGround;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public ActionDisplayType getActionDisplayType() {
            return ActionDisplayType.action;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z2) {
            return true;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: h */
        public boolean alwaysShowsTooltip() {
            return true;
        }
    };
    /* JADX INFO: renamed from: n */
    static AbstractUnitAction nukeAtAction = new AbstractUnitAction("nukeAt") { // from class: com.corrodinggames.rts.game.units.h.21
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Create a nuke at a point";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return "Nuke at";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: h_ */
        public boolean shouldShowDisplayText() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public int getCostAmount() {
            return 0;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public int getActiveCount(BaseUnit baseUnit, boolean z2) {
            return -1;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: l_, reason: merged with bridge method [inline-methods] */
        public UnitTypeEnum getUnitType() {
            return null;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public ActionType getActionType() {
            return ActionType.targetGround;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: f */
        public ActionDisplayType getActionDisplayType() {
            return ActionDisplayType.action;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: g */
        public boolean isHighPriority() {
            return false;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z2) {
            return true;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: h */
        public boolean alwaysShowsTooltip() {
            return true;
        }
    };
    /* JADX INFO: renamed from: o */
    static AbstractUnitAction freezeAIAction = new NoneAction("freezeAI") { // from class: com.corrodinggames.rts.game.units.h.22
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Freeze high level AI logic (120secs)";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return "Freeze AI";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public String d() {
            String str = "Freeze AI";
            GameEngine.getInstance();
            EditorOrBuilder editorOrBuilderL = EditorOrBuilder.L();
            if (editorOrBuilderL != null) {
                int i = -1;
                if (editorOrBuilderL.team instanceof AIController) {
                    i = ((int) ((AIController) editorOrBuilderL.team).aiUnitManagementTimer) / 60;
                }
                if (i > 0) {
                    str = str + "(" + i + ")";
                }
            }
            return str;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z2) {
            return baseUnit.team instanceof AIController;
        }
    };
    /* JADX INFO: renamed from: p */
    static AbstractUnitAction changeAllianceAction = new NoneAction("changeAlliance") { // from class: com.corrodinggames.rts.game.units.h.23
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Change selected player's alliance (players with the same letter are allied)";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return "Ally:";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public String d() {
            String str = "Ally";
            EditorOrBuilder editorOrBuilderL = EditorOrBuilder.L();
            if (editorOrBuilderL != null) {
                str = "Ally: " + editorOrBuilderL.team.getTeamSlotLabel();
            }
            return str;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z2) {
            return true;
        }
    };
    /* JADX INFO: renamed from: q */
    static AbstractUnitAction startRecordingAction = new NoneAction("startRecording") { // from class: com.corrodinggames.rts.game.units.h.2
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Start recording a replay to file";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return "Start Recording";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public String d() {
            String str;
            if (!GameEngine.getInstance().replayEngine.k()) {
                str = "Start Recording";
            } else {
                str = "Stop Recording";
            }
            return str;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z2) {
            if (GameEngine.getInstance().replayEngine.j()) {
                return false;
            }
            return true;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean isTargetingGround(BaseUnit baseUnit) {
            return GameEngine.getInstance().replayEngine.k();
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public boolean onClicked(BaseUnit baseUnit, boolean z2) {
            GameEngine gameEngine = GameEngine.getInstance();
            GameEngine.log("Start recording clicked");
            if (gameEngine.replayEngine.j()) {
                GameEngine.log("Already in a replay");
                return false;
            }
            gameEngine.queueGameThreadTask(new Runnable() { // from class: com.corrodinggames.rts.game.units.h.2.1
                @Override // java.lang.Runnable
                public void run() {
                    GameEngine gameEngine2 = GameEngine.getInstance();
                    if (!gameEngine2.replayEngine.k()) {
                        EditorOrBuilder editorOrBuilderL = EditorOrBuilder.L();
                        gameEngine2.gameUI.unusedFlag2 = true;
                        if (!gameEngine2.networkEngine.networkGameActive) {
                            long j = gameEngine2.networkEngine.nextUnitId;
                            gameEngine2.networkEngine.requireActiveMods = true;
                            int i = gameEngine2.networkEngine.roomSettings.fogMode;
                            gameEngine2.networkEngine.startSandboxServer();
                            gameEngine2.networkEngine.roomSettings.fogMode = i;
                            gameEngine2.networkEngine.nextUnitId = j;
                            gameEngine2.networkEngine.gameHasBeenStarted = true;
                            gameEngine2.currentTick = 0;
                            gameEngine2.networkEngine.nextBlockingFrame = gameEngine2.currentTick + 1;
                            gameEngine2.networkEngine.w();
                        }
                        String str = "[sandbox]" + gameEngine2.getCurrentMapName() + " [v" + gameEngine2.getVersionString() + "] (" + Utility.formatCurrentDate("d MMM yyyy HH.mm.ss") + ").replay";
                        gameEngine2.replayEngine.d(str);
                        gameEngine2.gameUI.unusedFlag2 = false;
                        GameEngine.addUIMessage(null, "Replay started as: " + str);
                        EditorOrBuilder editorOrBuilderL2 = EditorOrBuilder.L();
                        if (editorOrBuilderL2 != null && editorOrBuilderL != null) {
                            editorOrBuilderL2.a(editorOrBuilderL);
                            editorOrBuilderL2.lastSavedReplayPath = str;
                            return;
                        } else {
                            GameEngine.logColored("Failed copySettingsFromAnotherEditor");
                            return;
                        }
                    }
                    gameEngine2.replayEngine.e();
                }
            });
            return false;
        }
    };
    static AbstractUnitAction s = new NoneAction("startReplayPlayback") { // from class: com.corrodinggames.rts.game.units.h.3
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Start playback of last a replay";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return "Start Playback";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public String d() {
            String str;
            if (!GameEngine.getInstance().replayEngine.j()) {
                str = "Start Playback";
            } else {
                str = "Stop Playback";
            }
            return str;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z2) {
            boolean zK = GameEngine.getInstance().replayEngine.k();
            EditorOrBuilder editorOrBuilderL = EditorOrBuilder.L();
            return (editorOrBuilderL == null || editorOrBuilderL.lastSavedReplayPath == null || zK) ? false : true;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public boolean b(BaseUnit baseUnit) {
            EditorOrBuilder editorOrBuilderL = EditorOrBuilder.L();
            return (editorOrBuilderL == null || editorOrBuilderL.lastSavedReplayPath == null) ? false : true;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean isTargetingGround(BaseUnit baseUnit) {
            return GameEngine.getInstance().replayEngine.j();
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public boolean onClicked(BaseUnit baseUnit, boolean z2) {
            final GameEngine gameEngine = GameEngine.getInstance();
            final String str = EditorOrBuilder.L().lastSavedReplayPath;
            if (str == null) {
                gameEngine.alert("No last replay found");
                return false;
            }
            if (!gameEngine.replayEngine.j()) {
                final Runnable runnable = new Runnable() { // from class: com.corrodinggames.rts.game.units.h.3.1
                    @Override // java.lang.Runnable
                    public void run() {
                        GameEngine gameEngine2 = GameEngine.getInstance();
                        if (!gameEngine2.replayEngine.j()) {
                            boolean z3 = gameEngine2.tileMap.fogEnabled;
                            EditorOrBuilder editorOrBuilderL = EditorOrBuilder.L();
                            boolean z4 = gameEngine2.hasWonGame;
                            boolean z5 = gameEngine2.isContinuingAfterGameEnd;
                            gameEngine2.replayEngine.h = true;
                            gameEngine2.replayEngine.loadReplay(str);
                            gameEngine2.replayEngine.h = false;
                            gameEngine2.hasWonGame = z4;
                            gameEngine2.isContinuingAfterGameEnd = z5;
                            EditorOrBuilder editorOrBuilderL2 = EditorOrBuilder.L();
                            if (editorOrBuilderL2 != null && editorOrBuilderL != null) {
                                editorOrBuilderL2.a(editorOrBuilderL);
                            } else {
                                GameEngine.logColored("Failed copySettingsFromAnotherEditor");
                            }
                            gameEngine2.isGameStarted = true;
                            if (gameEngine2.tileMap != null) {
                                gameEngine2.tileMap.fogEnabled = z3;
                            }
                            gameEngine2.isMenuOpen = true;
                            if (editorOrBuilderL2 != null) {
                                editorOrBuilderL2.M();
                                return;
                            }
                            return;
                        }
                        GameEngine.log("stopPlaybackRunnable: Already started");
                    }
                };
                final MenuDialog menuDialogA = MenuDialog.create("Start playback of last recording?", true);
                menuDialogA.addButton(Locale.get("menus.common.ok", new Object[0]), new UIEventHandler() { // from class: com.corrodinggames.rts.game.units.h.3.2
                    @Override // com.corrodinggames.rts.gameFramework.ui.widgets.UIEventHandler
                    public boolean handleEvent(UIEvent uIEvent) {
                        menuDialogA.removeFromParent();
                        gameEngine.queueGameThreadTask(runnable);
                        return true;
                    }
                });
                gameEngine.gameUI.centerAndShowDialog(menuDialogA);
                return false;
            }
            gameEngine.queueGameThreadTask(new Runnable() { // from class: com.corrodinggames.rts.game.units.h.3.3
                @Override // java.lang.Runnable
                public void run() {
                    GameEngine gameEngine2 = GameEngine.getInstance();
                    if (!gameEngine2.replayEngine.j()) {
                        GameEngine.log("stopPlaybackRunnable: Already stopped");
                        return;
                    }
                    gameEngine2.replayEngine.e();
                    gameEngine2.gameSpeed = 1.0f;
                    gameEngine2.isGameStarted = true;
                    EditorOrBuilder editorOrBuilderL = EditorOrBuilder.L();
                    if (editorOrBuilderL != null) {
                        gameEngine2.playerTeam = editorOrBuilderL.team;
                    }
                }
            });
            return false;
        }
    };
    static AbstractUnitAction u = new NoneAction("freezeAllAI") { // from class: com.corrodinggames.rts.game.units.h.5
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Freeze full high level logic for all AI forever";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return "Freeze AI";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public String d() {
            String str = "Freeze AI";
            EditorOrBuilder editorOrBuilderL = EditorOrBuilder.L();
            if (editorOrBuilderL != null && editorOrBuilderL.freezeAllAIs) {
                str = "Unfreeze AIs";
            }
            return str;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public boolean canAfford(BaseUnit baseUnit, boolean z2) {
            return true;
        }
    };
    static ActionFilter K = new ActionFilter() { // from class: com.corrodinggames.rts.game.units.h.16
        @Override // com.corrodinggames.rts.game.units.actions.ActionFilter
        public boolean isAvailable(AbstractUnitAction abstractUnitAction, BaseUnit baseUnit) {
            if (abstractUnitAction instanceof FilteredUnitAction) {
                abstractUnitAction = ((FilteredUnitAction) abstractUnitAction).q_();
            }
            EditorOrBuilder editorOrBuilderL = EditorOrBuilder.L();
            if (editorOrBuilderL == null) {
                return true;
            }
            EditorTab editorTab = editorOrBuilderL.G;
            if (editorTab == null) {
                editorTab = EditorTab.all;
            }
            if (editorTab == EditorTab.all && EditorOrBuilder.a(abstractUnitAction, baseUnit)) {
                return false;
            }
            if (editorTab == EditorTab.modded && abstractUnitAction == EditorOrBuilder.reloadUnitsAction) {
                return true;
            }
            if (editorTab == EditorTab.modded && abstractUnitAction == EditorOrBuilder.reloadOnlyActiveUnitsAction) {
                return true;
            }
            if (editorTab == EditorTab.search && abstractUnitAction == EditorOrBuilder.y) {
                return true;
            }
            if (abstractUnitAction == EditorOrBuilder.B && !EditorOrBuilder.B.b(baseUnit)) {
                return false;
            }
            if (abstractUnitAction == EditorOrBuilder.C && !EditorOrBuilder.C.b(baseUnit)) {
                return false;
            }
            return editorTab.a(abstractUnitAction.getUnitType());
        }
    };
    static AbstractUnitAction t = new NoneAction("hideInterface") { // from class: com.corrodinggames.rts.game.units.h.4
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            String str = "Hide interface till the screen is clicked/pressed";
            if (GameEngine.isPC()) {
                str = str + "\n-Enable mouse capture to also hide the mouse";
            }
            return str;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return "Hide interface";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public boolean onClicked(BaseUnit baseUnit, boolean z2) {
            GameEngine.getInstance().isMenuOpen = true;
            return false;
        }
    };
    /* JADX INFO: renamed from: c */
    boolean freezeAllAIs;
    static AbstractUnitAction v = new NoneAction("pauseGame") { // from class: com.corrodinggames.rts.game.units.h.6
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Pause Game";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            if (GameEngine.getInstance().gameSpeed != 0.0f) {
                return "Pause: Off";
            }
            return "Pause: On";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public boolean onClicked(BaseUnit baseUnit, boolean z2) {
            GameEngine gameEngine = GameEngine.getInstance();
            if (gameEngine.replayEngine.j()) {
            }
            if (gameEngine.gameSpeed != 0.0f) {
                gameEngine.gameSpeed = 0.0f;
                return false;
            }
            gameEngine.gameSpeed = 1.0f;
            return false;
        }
    };
    static AbstractUnitAction w = new NoneAction("slowGame") { // from class: com.corrodinggames.rts.game.units.h.7
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Slow motion";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            if (GameEngine.getInstance().gameSpeed != 0.1f) {
                return "Slow motion: Off";
            }
            return "Slow motion: On";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public boolean onClicked(BaseUnit baseUnit, boolean z2) {
            GameEngine gameEngine = GameEngine.getInstance();
            if (gameEngine.replayEngine.j()) {
            }
            if (gameEngine.gameSpeed == 1.0f) {
                gameEngine.gameSpeed = 0.1f;
                return false;
            }
            gameEngine.gameSpeed = 1.0f;
            return false;
        }
    };
    static AbstractUnitAction x = new NoneAction("fastForward") { // from class: com.corrodinggames.rts.game.units.h.8
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Fast Forward 1-5x";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            return "Fast Forward: " + GameEngine.getInstance().gameSpeed;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public boolean onClicked(BaseUnit baseUnit, boolean z2) {
            GameEngine gameEngine = GameEngine.getInstance();
            if (gameEngine.replayEngine.j()) {
            }
            if (gameEngine.gameSpeed == 1.0f) {
                gameEngine.gameSpeed = 2.0f;
                return false;
            }
            if (gameEngine.gameSpeed == 2.0f) {
                gameEngine.gameSpeed = 3.0f;
                return false;
            }
            if (gameEngine.gameSpeed == 3.0f) {
                gameEngine.gameSpeed = 4.0f;
                return false;
            }
            if (gameEngine.gameSpeed == 4.0f) {
                gameEngine.gameSpeed = 5.0f;
                return false;
            }
            if (gameEngine.gameSpeed == 5.0f) {
                gameEngine.gameSpeed = 10.0f;
                return false;
            }
            gameEngine.gameSpeed = 1.0f;
            return false;
        }
    };
    static AbstractUnitAction y = new NoneAction("search") { // from class: com.corrodinggames.rts.game.units.h.9
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: j */
        public Texture getIconTexture() {
            return EditorOrBuilder.editorSelectionTexture2;
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Search for units";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            GameEngine.getInstance();
            EditorOrBuilder editorOrBuilderL = EditorOrBuilder.L();
            if (editorOrBuilderL != null && editorOrBuilderL.G == EditorTab.search) {
                return "Search: " + Utility.truncateWithEllipsis(editorOrBuilderL.H, 8);
            }
            return "Search units";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: c */
        public boolean onClicked(BaseUnit baseUnit, boolean z2) {
            GameEngine gameEngine = GameEngine.getInstance();
            if (gameEngine.replayEngine.i()) {
                gameEngine.showMessageBox("Reply active", "Changing search filter is currently not supported while recording a replay");
                return false;
            }
            PasswordHandler passwordHandler = new PasswordHandler() { // from class: com.corrodinggames.rts.game.units.h.9.1
                @Override // com.corrodinggames.rts.gameFramework.network.PasswordHandler
                /* JADX INFO: renamed from: a */
                public void submitPassword(String str) {
                    GameEngine.log("Searching for: " + str);
                    GameEngine gameEngine2 = GameEngine.getInstance();
                    if (gameEngine2.replayEngine.i()) {
                        gameEngine2.showMessageBox("Reply active", "Changing search filter is currently not supported while recording a replay");
                        return;
                    }
                    EditorOrBuilder editorOrBuilderL = EditorOrBuilder.L();
                    if (editorOrBuilderL == null) {
                        GameEngine.log("search: No editor");
                        return;
                    }
                    if (str == null || str.trim().equals(VariableScope.nullOrMissingString)) {
                        GameEngine.log("search: No text entered");
                        if (editorOrBuilderL.G == EditorTab.search) {
                            editorOrBuilderL.G = EditorTab.all;
                        }
                        editorOrBuilderL.H = null;
                        editorOrBuilderL.I = true;
                        GameUI.notifySelectionChanged();
                        return;
                    }
                    editorOrBuilderL.G = EditorTab.search;
                    editorOrBuilderL.H = str;
                    editorOrBuilderL.I = true;
                    GameUI.notifySelectionChanged();
                }

                @Override // com.corrodinggames.rts.gameFramework.network.PasswordHandler
                /* JADX INFO: renamed from: a */
                public void cancelPasswordEntry() {
                }
            };
            passwordHandler.promptMessage = "Search units by internal name or text title.";
            passwordHandler.dialogTitle = "Search units";
            passwordHandler.confirmButtonLabel = "Search";
            passwordHandler.cancelButtonLabel = "Cancel";
            NetworkEngine.a(passwordHandler);
            return false;
        }
    };
    static AbstractUnitAction z = new NoneAction("enableDebug") { // from class: com.corrodinggames.rts.game.units.h.10
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Show hidden unit information in tooltips including flags, ammo, tags and resources";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            if (!GameEngine.getInstance().isDebugTempMode) {
                return "Debug: Off";
            }
            return "Debug: On";
        }
    };
    static AbstractUnitAction A = new NoneAction("enableAIDebug") { // from class: com.corrodinggames.rts.game.units.h.11
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "AI debug view";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            GameEngine.getInstance();
            if (!AIController.unitCountsUpdated) {
                return "AI Debug: Off";
            }
            return "AI Debug: On";
        }
    };
    static AbstractUnitAction B = new NoneAction("enableTriggerDebug") { // from class: com.corrodinggames.rts.game.units.h.13
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "For debugging autoTriggers. When enabled will log a message when any auto triggers fire on any selected units";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            if (!GameEngine.getInstance().isTriggerDebugMode) {
                return "Trigger Debug: Off";
            }
            return "Trigger Debug: On";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public boolean b(BaseUnit baseUnit) {
            return GameEngine.getInstance().isDebugTempMode;
        }
    };
    static AbstractUnitAction C = new NoneAction("clearSaveHistory") { // from class: com.corrodinggames.rts.game.units.h.14
        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: a */
        public String getDescription() {
            return "Clear save history";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        /* JADX INFO: renamed from: b */
        public String getDisplayName() {
            GameEngine.getInstance();
            return "Clear history";
        }

        @Override // com.corrodinggames.rts.game.units.actions.AbstractUnitAction
        public boolean b(BaseUnit baseUnit) {
            return GameEngine.getInstance().isDebugTempMode;
        }
    };
    /* JADX INFO: renamed from: r */
    String lastSavedReplayPath;

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: f, reason: merged with bridge method [inline-methods] */
    public UnitTypeEnum r() {
        return UnitTypeEnum.editorOrBuilder;
    }

    public static boolean w() {
        if (GameEngine.getInstance().replayEngine.i()) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.UnitPathPoints
    public PointF[] b() {
        return this.controlPoints;
    }

    @Override // com.corrodinggames.rts.game.units.UnitPathPoints
    public PointF[] e_() {
        return this.targetPoints;
    }

    @Override // com.corrodinggames.rts.game.units.land.LandUnit, com.corrodinggames.rts.game.units.BaseUnit
    public Texture v() {
        if (this.team.teamId == -1) {
            return null;
        }
        return landUnitIconTextures[this.team.getTeamColorIndex()];
    }

    public static void K() {
        editorSelectionTexture2 = GameEngine.getInstance().renderGraphicsEngine.a(R.drawable.icon_search);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: a */
    public boolean canRepairTarget(BaseUnit baseUnit) {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture d() {
        if (this.isDead) {
            return BuilderUnit.builderTexture_dead;
        }
        return BuilderUnit.builderTexture_teamColors[this.team.getTeamColorIndex()];
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture k() {
        return null;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public Texture d(int i) {
        return null;
    }

    public EditorOrBuilder(boolean z2) {
        super(z2);
        this.controlPoints = new PointF[6];
        this.targetPoints = new PointF[this.controlPoints.length];
        targetPointPaints = new Paint();
        targetPointPaints.a(40, 0, 255, 0);
        targetPointPaints.a(true);
        targetPointPaints.a(2.0f);
        targetPointPaints.a(Paint.Cap.ROUND);
        editorSelectionPaint = new Paint();
        editorSelectionPaint.a(targetPointPaints);
        editorSelectionPaint.a(55, 255, 60, 60);
        editorSearchPaint = new Paint();
        editorSearchPaint.a(60, 255, 255, 255);
        this.E = null;
        this.F = EditorUnitTypeFilter.land;
        this.G = EditorTab.all;
        this.I = true;
        T(20);
        U(20);
        this.radius = 10.0f;
        this.posX = -1000.0f;
        this.posY = -1000.0f;
        this.displayRadius = this.radius;
        this.maxHealth = 170000.0f;
        this.currentHealth = this.maxHealth;
        this.baseTexture = BuilderUnit.builderTexture_dead;
        for (int i = 0; i < this.controlPoints.length; i++) {
            this.controlPoints[i] = new PointF();
            this.targetPoints[i] = new PointF();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static void a(float f, UnitPathPoints unitPathPoints) {
        OrderableUnit orderableUnit = (OrderableUnit) unitPathPoints;
        PointF[] pointFArrB = unitPathPoints.b();
        PointF[] pointFArrE_ = unitPathPoints.e_();
        BaseUnit currentRepairOrReclaimTarget = orderableUnit.getCurrentRepairOrReclaimTarget();
        orderableUnit.aN = currentRepairOrReclaimTarget != null;
        if (currentRepairOrReclaimTarget == null) {
            if (pointFArrB[0].x != 0.0f || pointFArrB[0].y != 0.0f) {
                for (int i = 0; i < pointFArrB.length; i++) {
                    PointF pointF = pointFArrB[i];
                    PointF pointF2 = pointFArrE_[i];
                    pointF.x = 0.0f;
                    pointF.y = 0.0f;
                    pointF2.x = 0.0f;
                    pointF2.y = 0.0f;
                }
                return;
            }
            return;
        }
        for (int i2 = 0; i2 < pointFArrB.length; i2++) {
            PointF pointF3 = pointFArrB[i2];
            PointF pointF4 = pointFArrE_[i2];
            pointF3.x = Utility.distanceSq(pointF3.x, pointF4.x, 0.1f * f);
            pointF3.y = Utility.distanceSq(pointF3.y, pointF4.y, 0.1f * f);
            pointF3.x += (pointF4.x - pointF3.x) * 0.04f * f;
            pointF3.y += (pointF4.y - pointF3.y) * 0.04f * f;
            float f2 = currentRepairOrReclaimTarget.radius * 0.75f;
            if (Utility.abs(pointF3.x - pointF4.x) < 1.0f) {
                pointF4.x = Utility.randomRepairTargetOffset(-f2, f2);
            }
            if (Utility.abs(pointF3.y - pointF4.y) < 1.0f) {
                pointF4.y = Utility.randomRepairTargetOffset(-f2, f2);
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean e() {
        GameEngine gameEngine = GameEngine.getInstance();
        gameEngine.effectManager.createSmallExplosion(this.posX, this.posY, this.posZ);
        this.baseTexture = BuilderUnit.builderTexture_dead;
        S(0);
        this.isAlive = false;
        gameEngine.soundEngine.playSound(SoundEngine.unitExplodeSound, 0.8f, this.posX, this.posY);
        bq();
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.land.LandUnit, com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f) {
        if (f < 0.3f) {
            f = 0.3f;
        }
        if (this.needsRecalculation && this.team.isSpectatorTeamColor()) {
            int i = 0;
            while (true) {
                if (i < PlayerTeam.TEAM_NEUTRAL) {
                    PlayerTeam playerTeamK = PlayerTeam.k(i);
                    if (playerTeamK == null || playerTeamK.isSpectatorTeamColor()) {
                        i++;
                    } else {
                        changeTeam(playerTeamK);
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        super.update(f);
        if (!this.isDead) {
            a(f, this);
        }
        this.currentHealth = this.maxHealth;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public void a(float f, boolean z2) {
        if (!this.isDead) {
        }
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float e(int i) {
        return 0.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float f(int i) {
        return 0.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.GameObject
    public boolean c(float f) {
        if (!super.c(f)) {
            return false;
        }
        GameEngine.getInstance();
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public void a(BaseUnit baseUnit, int i) {
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: b_ */
    public boolean requiresFacingForActions() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public int y() {
        return 850000;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: b */
    public float getDistanceToTarget(BaseUnit baseUnit) {
        return 1.0E7f;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public float c(BaseUnit baseUnit) {
        return 1.0E7f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float m() {
        return 30.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float b(int i) {
        return 100.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: z */
    public float getMoveSpeed() {
        return 0.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: A */
    public float getMaxTurnSpeed() {
        return 9.8f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float B() {
        return 9.35f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float c(int i) {
        return 99.0f;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: l */
    public boolean canAttack() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: C */
    public float getMoveAccelerationSpeed() {
        return 0.04f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float D() {
        return 0.1f;
    }

    public static boolean a(AbstractUnitAction abstractUnitAction, BaseUnit baseUnit) {
        if (abstractUnitAction instanceof FilteredUnitAction) {
            abstractUnitAction = ((FilteredUnitAction) abstractUnitAction).q_();
        }
        if (abstractUnitAction == freezeAIAction || abstractUnitAction == w || abstractUnitAction == x || abstractUnitAction == finishQueueAction || abstractUnitAction == removeUnitsAction || abstractUnitAction == unitCloneAction || abstractUnitAction == z || abstractUnitAction == changeAllianceAction || abstractUnitAction == startRecordingAction || abstractUnitAction == s || abstractUnitAction == t || abstractUnitAction == B || abstractUnitAction == C) {
            return true;
        }
        return false;
    }

    static EditorOrBuilder L() {
        return GameEngine.getInstance().gameUI.getEditorOrBuilder();
    }

    public static void a(ArrayList arrayList, int i) {
        if (i != 1) {
            return;
        }
        D = new ArrayList();
        D.add(new TeamChangeAction(true, false));
        D.add(new TeamChangeAction(true, true));
        D.add(new TeamChangeAction(false, false));
        D.add(new ChangeEditorTabAction(true, false));
        D.add(new ChangeEditorTabAction(true, true));
        D.add(new ChangeEditorTabAction(false, false));
        D.add(new ModFilterAction(true, false));
        D.add(new ModFilterAction(true, true));
        D.add(new ModFilterAction(false, false));
        D.add(new ChangeTypeFilterAction(true, false));
        D.add(new ChangeTypeFilterAction(true, true));
        D.add(new ChangeTypeFilterAction(false, false));
        D.add(new SetTerrainTypeAction(EditorTerrainType.grass));
        D.add(new SetTerrainTypeAction(EditorTerrainType.sea));
        D.add(new SetTerrainTypeAction(EditorTerrainType.sand));
        D.add(new SetTerrainTypeAction(EditorTerrainType.dust));
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(new AddCreditsAction());
        arrayList2.add(y);
        arrayList2.add(reloadUnitsAction);
        arrayList2.add(reloadOnlyActiveUnitsAction);
        arrayList2.add(removeUnitsAction);
        arrayList2.add(unitCloneAction);
        arrayList2.add(killUnitsAction);
        arrayList2.add(nukeAtAction);
        arrayList2.add(finishQueueAction);
        arrayList2.add(u);
        arrayList2.add(v);
        arrayList2.add(w);
        arrayList2.add(x);
        arrayList2.add(z);
        arrayList2.add(changeAllianceAction);
        arrayList2.add(startRecordingAction);
        arrayList2.add(s);
        arrayList2.add(t);
        if (GameEngine.isTestingBuild) {
            arrayList2.add(A);
        }
        arrayList2.add(B);
        arrayList2.add(C);
        Iterator it = arrayList2.iterator();
        while (it.hasNext()) {
            D.add(new FilteredUnitAction((AbstractUnitAction) it.next(), K, true));
        }
        ArrayList<UnitType> arrayList3 = new ArrayList();
        arrayList3.addAll(UnitTypeEnum.ae);
        // from class: com.corrodinggames.rts.game.units.h.15
// java.util.Comparator
        /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
        arrayList3.sort((unitType, unitType2) -> {
            BaseUnit baseUnitCanAttack = BaseUnit.getPrototypeForUnitType(unitType);
            BaseUnit baseUnitCanAttack2 = BaseUnit.getPrototypeForUnitType(unitType2);
            int iCompareTo = Boolean.valueOf(baseUnitCanAttack.bP()).compareTo(Boolean.valueOf(baseUnitCanAttack2.bP()));
            if (iCompareTo != 0) {
                return iCompareTo;
            }
            int iCompareTo2 = Boolean.valueOf(unitType.isBuildingUnit()).compareTo(Boolean.valueOf(unitType2.isBuildingUnit()));
            if (iCompareTo2 != 0) {
                return iCompareTo2;
            }
            int iCompareTo3 = Boolean.valueOf(baseUnitCanAttack.bO()).compareTo(Boolean.valueOf(baseUnitCanAttack2.bO()));
            if (iCompareTo3 != 0) {
                return iCompareTo3;
            }
            UnitPrice unitPriceU = unitType.u();
            UnitPrice unitPriceU2 = unitType2.u();
            UnitPrice unitPriceB = unitType.B();
            UnitPrice unitPriceB2 = unitType2.B();
            if (unitPriceB != null) {
                unitPriceU = UnitPrice.a(unitPriceU, unitPriceB);
            }
            if (unitPriceB2 != null) {
                unitPriceU2 = UnitPrice.a(unitPriceU2, unitPriceB2);
            }
            int iCompareTo4 = unitPriceU.compareTo(unitPriceU2);
            if (iCompareTo4 != 0) {
                return iCompareTo4;
            }
            return 0;
        });
        for (UnitType unitType : arrayList3) {
            if (unitType != UnitTypeEnum.wall_v && !unitType.getUnitTypeDescriptionShort().equals("test_tank") && !unitType.getUnitTypeDescriptionShort().equals("missing") && unitType != UnitTypeEnum.tankDestroyer && unitType != UnitTypeEnum.megaTank && unitType != UnitTypeEnum.fogRevealer && unitType != UnitTypeEnum.crystalResource && unitType != UnitTypeEnum.damagingBorder && unitType != UnitTypeEnum.zoneMarker && unitType != UnitTypeEnum.editorOrBuilder && unitType != UnitTypeEnum.dummyNonUnitWithTeam && unitType != UnitTypeEnum.supplyDepot && (BaseUnit.getPrototypeForUnitType(unitType) instanceof OrderableUnit) && (!(unitType instanceof CustomUnitConfig) || ((CustomUnitConfig) unitType).showInEditor)) {
                FilteredUnitAction filteredUnitAction = new FilteredUnitAction(new PlaceBuildingAction(unitType, 1, null), K);
                boolean z2 = false;
                Iterator it2 = D.iterator();
                while (it2.hasNext()) {
                    if (((AbstractUnitAction) it2.next()).equals(filteredUnitAction)) {
                        z2 = true;
                    }
                }
                if (!z2) {
                    D.add(filteredUnitAction);
                }
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: a */
    public void performUnitAction(AbstractUnitAction abstractUnitAction, boolean z2) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (abstractUnitAction instanceof TeamChangeAction) {
            TeamChangeAction teamChangeAction = (TeamChangeAction) abstractUnitAction;
            boolean z3 = true;
            if (z2) {
                z3 = 1 == 0;
            }
            if (teamChangeAction.isPrevious) {
                z3 = !z3;
            }
            if (z3) {
                PlayerTeam playerTeamK = null;
                int i = this.team.teamId + 1;
                while (true) {
                    if (i < PlayerTeam.TEAM_NEUTRAL) {
                        PlayerTeam playerTeamK2 = PlayerTeam.k(i);
                        if (playerTeamK2 == null || playerTeamK2.isSpectatorTeamColor()) {
                            i++;
                        } else {
                            playerTeamK = playerTeamK2;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (playerTeamK == null && this.team.teamId < 4) {
                    playerTeamK = PlayerTeam.k(this.team.teamId + 1);
                    if (playerTeamK == null) {
                        GameEngine.log("Sandbox adding new team:" + this.team.teamId);
                        AIController aIController = new AIController(this.team.teamId + 1);
                        playerTeamK = aIController;
                        playerTeamK.teamColorId = 1;
                        playerTeamK.isTeamDefeatedTech = true;
                        playerTeamK.isTeamWipedOut = true;
                        if (!this.freezeAllAIs) {
                            aIController.aiUnitManagementTimer = 0.0f;
                        } else {
                            aIController.aiUnitManagementTimer = Float.MAX_VALUE;
                        }
                    }
                }
                if (playerTeamK == null) {
                    int i2 = 0;
                    while (true) {
                        if (i2 < PlayerTeam.TEAM_NEUTRAL) {
                            PlayerTeam playerTeamK3 = PlayerTeam.k(i2);
                            if (playerTeamK3 == null || playerTeamK3.isSpectatorTeamColor()) {
                                i2++;
                            } else {
                                playerTeamK = playerTeamK3;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
                if (playerTeamK != null) {
                    changeTeam(playerTeamK);
                    if (!gameEngine.replayEngine.j()) {
                        gameEngine.playerTeam = playerTeamK;
                    }
                }
            } else {
                PlayerTeam playerTeam = null;
                int i3 = this.team.teamId - 1;
                while (true) {
                    if (i3 >= 0) {
                        PlayerTeam playerTeamK4 = PlayerTeam.k(i3);
                        if (playerTeamK4 == null || playerTeamK4.isSpectatorTeamColor()) {
                            i3--;
                        } else {
                            playerTeam = playerTeamK4;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (playerTeam == null) {
                    int i4 = PlayerTeam.TEAM_NEUTRAL - 1;
                    while (true) {
                        if (i4 >= 0) {
                            PlayerTeam playerTeamK5 = PlayerTeam.k(i4);
                            if (playerTeamK5 == null || playerTeamK5.isSpectatorTeamColor()) {
                                i4--;
                            } else {
                                playerTeam = playerTeamK5;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
                if (playerTeam != null) {
                    changeTeam(playerTeam);
                    if (!gameEngine.replayEngine.j()) {
                        gameEngine.playerTeam = playerTeam;
                    }
                }
            }
        }
        if (abstractUnitAction instanceof ModFilterAction) {
            ModFilterAction modFilterAction = (ModFilterAction) abstractUnitAction;
            boolean z4 = true;
            if (z2) {
                z4 = 1 == 0;
            }
            if (modFilterAction.isPrevious) {
                z4 = !z4;
            }
            ArrayList activeMods = gameEngine.modManager.getActiveMods();
            if (activeMods.size() == 0) {
                this.E = null;
            } else if (z4) {
                if (this.E == null) {
                    this.E = (ModInfo) activeMods.get(0);
                } else {
                    ModInfo modInfo = null;
                    boolean z5 = false;
                    Iterator it = activeMods.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        ModInfo modInfo2 = (ModInfo) it.next();
                        if (z5) {
                            modInfo = modInfo2;
                            break;
                        } else if (modInfo2 == this.E) {
                            z5 = true;
                        }
                    }
                    this.E = modInfo;
                }
            } else if (this.E == null) {
                this.E = (ModInfo) activeMods.get(activeMods.size() - 1);
            } else {
                ModInfo modInfo3 = null;
                boolean z6 = false;
                ArrayList arrayList = new ArrayList();
                arrayList.addAll(activeMods);
                Collections.reverse(arrayList);
                Iterator it2 = arrayList.iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    ModInfo modInfo4 = (ModInfo) it2.next();
                    if (z6) {
                        modInfo3 = modInfo4;
                        break;
                    } else if (modInfo4 == this.E) {
                        z6 = true;
                    }
                }
                this.E = modInfo3;
            }
        }
        if (abstractUnitAction instanceof ChangeTypeFilterAction) {
            ChangeTypeFilterAction changeTypeFilterAction = (ChangeTypeFilterAction) abstractUnitAction;
            boolean z7 = true;
            if (z2) {
                z7 = 1 == 0;
            }
            if (changeTypeFilterAction.isPrevious) {
                z7 = !z7;
            }
            this.F = this.F.a(!z7);
        }
        if (abstractUnitAction instanceof AddCreditsAction) {
            this.team.d(10000.0f);
        }
        if (abstractUnitAction instanceof ChangeEditorTabAction) {
            ((ChangeEditorTabAction) abstractUnitAction).n();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void a(AbstractUnitAction abstractUnitAction, boolean z2, PointF pointF, BaseUnit baseUnit) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (abstractUnitAction instanceof FilteredUnitAction) {
            abstractUnitAction = ((FilteredUnitAction) abstractUnitAction).q_();
        }
        if (abstractUnitAction == reloadUnitsAction) {
            if (w()) {
                GameEngine.logColored("Not reloading units: Need to keep network sync");
                gameEngine.gameUI.showMediumPriorityMessage("Not reloading units: Need to keep network sync");
                return;
            }
            if (z2) {
                return;
            }
            if (gameEngine.modManager.getActiveModCount() == 0) {
                gameEngine.gameUI.showMediumPriorityMessage("No custom units to reload");
                return;
            }
            GameEngine.log("Reload units requested");
            gameEngine.modManager.applyMods(true, false);
            for (PlayerTeam playerTeam : PlayerTeam.getTeams()) {
                if (playerTeam instanceof AIController) {
                    ((AIController) playerTeam).initializeBuildStrategies();
                }
            }
            gameEngine.gameUI.showMediumPriorityMessage("All custom unit files reloaded");
        }
        if (abstractUnitAction == reloadOnlyActiveUnitsAction) {
            if (w()) {
                GameEngine.logColored("Not reloading units: Need to keep network sync");
                return;
            }
            if (z2) {
                return;
            }
            if (gameEngine.modManager.getActiveModCount() == 0) {
                gameEngine.gameUI.showMediumPriorityMessage("No custom units to reload");
                return;
            }
            GameEngine.log("Reload active only requested");
            gameEngine.modManager.applyMods(true, true);
            for (PlayerTeam playerTeam2 : PlayerTeam.getTeams()) {
                if (playerTeam2 instanceof AIController) {
                    ((AIController) playerTeam2).initializeBuildStrategies();
                }
            }
            int i = CustomUnitConfigParser.unitsRegeneratedCount;
            int i2 = 100;
            String str = "Quick reloaded changed data for " + i + " units active on map.";
            if (gameEngine.settingsEngine.liveReloading && i == 0) {
                str = str + " (Note: Live reloading is currently enabled, so changed units may have already be reloaded)";
                i2 = 170;
            }
            gameEngine.gameUI.showInfoMessageWithPriority(str, i2);
        }
        if (abstractUnitAction == removeUnitsAction || abstractUnitAction == killUnitsAction || abstractUnitAction == unitCloneAction) {
            int i3 = 0;
            if (z2) {
                return;
            }
            for (BaseUnit baseUnit2 : BaseUnit.getGlobalUnitList()) {
                if ((baseUnit2 instanceof BaseUnit) && Utility.distanceSq(baseUnit2.posX, baseUnit2.posY, pointF.x, pointF.y) < 2500.0f) {
                    if (abstractUnitAction == removeUnitsAction) {
                        if (baseUnit2.transportContainer == null && baseUnit2.parentEntity == null) {
                            baseUnit2.removeFromGame();
                            if ((baseUnit2 instanceof OrderableUnit) && baseUnit2.bI()) {
                                gameEngine.pathfindingEngine.a((OrderableUnit) baseUnit2);
                            }
                        }
                    } else if (abstractUnitAction == killUnitsAction) {
                        if (baseUnit2.transportContainer == null && baseUnit2.parentEntity == null) {
                            baseUnit2.currentHealth = -1.0f;
                        }
                    } else if (abstractUnitAction != unitCloneAction) {
                        continue;
                    } else if (i3 <= 4) {
                        if (!baseUnit2.bI() && !(baseUnit2 instanceof Tree) && !baseUnit2.isDead && baseUnit2.transportContainer == null && baseUnit2.parentEntity == null) {
                            i3++;
                            UnitType unitTypeR = baseUnit2.r();
                            for (int i4 = -25; i4 < 25; i4++) {
                                BaseUnit baseUnitA = unitTypeR.a();
                                baseUnitA.posX = baseUnit2.posX + (i4 * 0.5f) + 1.0f;
                                baseUnitA.posY = baseUnit2.posY + (i4 * 0.5f) + 1.0f;
                                baseUnitA.setUnitTeam(baseUnit2.team);
                                PlayerTeam.c(baseUnitA);
                                baseUnitA.rotationSpeed = Utility.getDeterministicRandomInt((GameObject) baseUnit2, -180, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_STB_INPUT, 25 + i4);
                                if (baseUnitA instanceof OrderableUnit) {
                                    ((OrderableUnit) baseUnitA).movementActiveThisFrame = true;
                                }
                            }
                        }
                    } else {
                        return;
                    }
                }
            }
            return;
        }
        if (abstractUnitAction == nukeAtAction) {
            if (z2) {
                return;
            }
            Projectile projectileA = NukeLauncher.a(this, pointF.x, pointF.y, pointF.x, pointF.y);
            if (projectileA != null) {
                projectileA.posZ = 100.0f;
                projectileA.sourceUnit = null;
            }
        }
        if (abstractUnitAction == finishQueueAction) {
            if (z2) {
                return;
            }
            for (GameObject gameObject : BaseUnit.getGlobalUnitList()) {
                if ((gameObject instanceof OrderableUnit) && (gameObject instanceof FactoryQueueInterface) && Utility.distanceSq(gameObject.posX, gameObject.posY, pointF.x, pointF.y) < 2500.0f) {
                    ((FactoryQueueInterface) gameObject).dz();
                }
            }
            return;
        }
        if (abstractUnitAction == freezeAIAction) {
            PlayerTeam playerTeam3 = this.team;
            if (playerTeam3 instanceof AIController) {
                AIController aIController = (AIController) playerTeam3;
                if (aIController.aiUnitManagementTimer > 0.0f) {
                    aIController.aiUnitManagementTimer = 0.0f;
                } else {
                    aIController.aiUnitManagementTimer = 10800.0f;
                }
            }
        }
        if (abstractUnitAction == changeAllianceAction) {
            PlayerTeam playerTeam4 = this.team;
            playerTeam4.teamColorId++;
            if (playerTeam4.teamColorId > 4) {
                playerTeam4.teamColorId = 0;
            }
        }
        if (abstractUnitAction == u) {
            boolean z3 = false;
            boolean z4 = false;
            for (PlayerTeam playerTeam5 : PlayerTeam.getTeams()) {
                if (playerTeam5 instanceof AIController) {
                    if (((AIController) playerTeam5).aiUnitManagementTimer > 0.0f) {
                        z3 = true;
                    }
                    z4 = true;
                }
            }
            boolean z5 = !z3;
            if (!z4) {
                z5 = !this.freezeAllAIs;
            }
            this.freezeAllAIs = z5;
            M();
        }
        if (abstractUnitAction == v) {
        }
        if (abstractUnitAction == w) {
        }
        if (abstractUnitAction == x) {
        }
        if (abstractUnitAction == z) {
            gameEngine.isDebugTempMode = !gameEngine.isDebugTempMode;
        }
        if (abstractUnitAction == A) {
            AIController.unitCountsUpdated = !AIController.unitCountsUpdated;
        }
        if (abstractUnitAction == B) {
            gameEngine.isTriggerDebugMode = !gameEngine.isTriggerDebugMode;
        }
        if (abstractUnitAction == C) {
            gameEngine.gameStatistics.a();
        }
        if (abstractUnitAction instanceof SetTerrainTypeAction) {
            TerrainAutotiler.a(((SetTerrainTypeAction) abstractUnitAction).terrainType, pointF);
        }
        super.a(abstractUnitAction, z2, pointF, baseUnit);
    }

    public void M() {
        for (PlayerTeam playerTeam : PlayerTeam.getTeams()) {
            if (playerTeam instanceof AIController) {
                AIController aIController = (AIController) playerTeam;
                if (!this.freezeAllAIs) {
                    aIController.aiUnitManagementTimer = 0.0f;
                } else {
                    aIController.aiUnitManagementTimer = Float.MAX_VALUE;
                }
            }
        }
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: N */
    public ArrayList getAvailableActions() {
        GameEngine.getInstance();
        return D;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public boolean E() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    public float g(int i) {
        return 10.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: F */
    public boolean canDrawShadow() {
        return GameEngine.getInstance().settingsEngine.renderExtraShadows && !this.isDead;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: G */
    public float getShadowOffsetX() {
        return 1.0f;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit
    /* JADX INFO: renamed from: H */
    public float getShadowOffsetY() {
        return 1.0f;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean u() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.MovableUnit, com.corrodinggames.rts.game.units.OrderableUnit
    public boolean I() {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean d(BaseUnit baseUnit) {
        return false;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: J */
    public boolean isDamageImmune() {
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    /* JADX INFO: renamed from: a */
    public float applyDamage(BaseUnit baseUnit, float f, Projectile projectile) {
        return super.applyDamage(baseUnit, 0.0f, projectile);
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public void O() {
    }

    @Override // com.corrodinggames.rts.game.units.BaseUnit
    public boolean P() {
        return true;
    }

    public void a(EditorOrBuilder editorOrBuilder) {
        this.lastSavedReplayPath = editorOrBuilder.lastSavedReplayPath;
        this.freezeAllAIs = editorOrBuilder.freezeAllAIs;
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeByte(1);
        gameOutputStream.writeEnumOrdinal(this.G);
        gameOutputStream.writeStringNullable(this.H);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.game.units.OrderableUnit, com.corrodinggames.rts.game.units.BaseUnit, com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject
    public void a(GameInputStream gameInputStream) throws IOException {
        byte b = gameInputStream.readByte();
        this.G = (EditorTab) gameInputStream.readEnumOrdinalOrNull(EditorTab.class);
        if (this.G == null) {
            this.G = EditorTab.all;
        }
        if (b >= 1) {
            this.H = gameInputStream.readNullableString();
        }
        super.a(gameInputStream);
    }
}
