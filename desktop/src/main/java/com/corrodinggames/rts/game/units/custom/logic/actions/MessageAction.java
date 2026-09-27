package com.corrodinggames.rts.game.units.custom.logic.actions;

import android.graphics.PointF;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.actions.AbstractUnitAction;
import com.corrodinggames.rts.game.units.custom.*;
import com.corrodinggames.rts.game.units.custom.logic.CustomActionDef;
import com.corrodinggames.rts.game.units.custom.logic.LogicAction;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.network.NetworkEngine;
import com.corrodinggames.rts.gameFramework.utility.IniFile;
import java.util.regex.Pattern;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.custom.a.a.f */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/custom/a/a/f.class */
public class MessageAction extends LogicAction {

    /* JADX INFO: renamed from: a */
    LocalizedText showMessageToPlayer;

    /* JADX INFO: renamed from: b */
    LocalizedText showMessageToAllPlayers;

    /* JADX INFO: renamed from: c */
    LocalizedText showMessageToAllEnemyPlayers;

    /* JADX INFO: renamed from: d */
    LocalizedText showQuickWarLogToPlayer;

    /* JADX INFO: renamed from: e */
    LocalizedText showQuickWarLogToAllPlayers;

    /* JADX INFO: renamed from: f */
    LocalizedText debugMessage;
    static final Pattern g = Pattern.compile("%\\{([^\\]]*?)\\}");

    public static void a(CustomUnitConfig customUnitConfig, IniFile iniFile, String str, String str2, CustomActionDef customActionDef, String str3, boolean z) throws ConfigParseException {
        LocalizedText localizedTextLoadUnitConfigFile = CustomUnitConfigParser.getUnitReference(customUnitConfig, iniFile, str, "showMessageToPlayer", (String) null);
        LocalizedText localizedTextLoadUnitConfigFile2 = CustomUnitConfigParser.getUnitReference(customUnitConfig, iniFile, str, "showMessageToAllPlayers", (String) null);
        LocalizedText localizedTextLoadUnitConfigFile3 = CustomUnitConfigParser.getUnitReference(customUnitConfig, iniFile, str, "showMessageToAllEnemyPlayers", (String) null);
        LocalizedText localizedTextLoadUnitConfigFile4 = CustomUnitConfigParser.getUnitReference(customUnitConfig, iniFile, str, "showQuickWarLogToPlayer", (String) null);
        LocalizedText localizedTextLoadUnitConfigFile5 = CustomUnitConfigParser.getUnitReference(customUnitConfig, iniFile, str, "showQuickWarLogToAllPlayers", (String) null);
        LocalizedText localizedTextLoadUnitConfigFile6 = CustomUnitConfigParser.getUnitReference(customUnitConfig, iniFile, str, "debugMessage", (String) null);
        if (localizedTextLoadUnitConfigFile != null || localizedTextLoadUnitConfigFile2 != null || localizedTextLoadUnitConfigFile3 != null || localizedTextLoadUnitConfigFile4 != null || localizedTextLoadUnitConfigFile5 != null || localizedTextLoadUnitConfigFile6 != null) {
            MessageAction messageAction = new MessageAction();
            messageAction.showMessageToPlayer = localizedTextLoadUnitConfigFile;
            messageAction.showMessageToAllPlayers = localizedTextLoadUnitConfigFile2;
            messageAction.showMessageToAllEnemyPlayers = localizedTextLoadUnitConfigFile3;
            messageAction.showQuickWarLogToPlayer = localizedTextLoadUnitConfigFile4;
            messageAction.showQuickWarLogToAllPlayers = localizedTextLoadUnitConfigFile5;
            messageAction.debugMessage = localizedTextLoadUnitConfigFile6;
            customActionDef.logicActions.add(messageAction);
        }
    }

    public String a(CustomUnit customUnit, String str) {
        if (str == null) {
            str = null;
        }
        return str;
    }

    @Override // com.corrodinggames.rts.game.units.custom.logic.LogicAction
    /* JADX INFO: renamed from: a */
    public boolean doAction(CustomUnit customUnit, AbstractUnitAction abstractUnitAction, PointF pointF, BaseUnit baseUnit, int i) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.showMessageToPlayer != null && customUnit.team == gameEngine.playerTeam) {
            NetworkEngine.a((String) null, a(customUnit, this.showMessageToPlayer.b(customUnit)));
        }
        if (this.showMessageToAllPlayers != null) {
            NetworkEngine.a((String) null, a(customUnit, this.showMessageToAllPlayers.b(customUnit)));
        }
        if (this.showMessageToAllEnemyPlayers != null && gameEngine.playerTeam != null && customUnit.team.c(gameEngine.playerTeam)) {
            NetworkEngine.a((String) null, a(customUnit, this.showMessageToAllEnemyPlayers.b(customUnit)));
        }
        if (this.showQuickWarLogToPlayer != null && customUnit.team == gameEngine.playerTeam) {
            gameEngine.gameUI.warLogDisplay.logMessage(a(customUnit, this.showQuickWarLogToPlayer.b(customUnit)));
        }
        if (this.showQuickWarLogToAllPlayers != null) {
            gameEngine.gameUI.warLogDisplay.logMessage(a(customUnit, this.showQuickWarLogToAllPlayers.b(customUnit)));
        }
        if (this.debugMessage != null && gameEngine.isGameStarted && gameEngine.isDebugTempMode) {
            NetworkEngine.a((String) null, customUnit.r().getUnitTypeDescriptionShort() + "(" + customUnit.objectId + ") Debug: " + a(customUnit, this.debugMessage.b(customUnit)));
            return true;
        }
        return true;
    }
}
