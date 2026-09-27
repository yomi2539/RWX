package com.corrodinggames.rts.java;

import com.corrodinggames.librocket.GameMainManager;
import com.corrodinggames.librocket.scripts.ScriptEngine;
import com.corrodinggames.rts.appFramework.InGameActivity;
import com.corrodinggames.rts.gameFramework.GameEngine;

/* JADX INFO: renamed from: com.corrodinggames.rts.java.h */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/java/h.class */
public class JavaInGameActivity extends InGameActivity {
    @Override // com.corrodinggames.rts.appFramework.InGameActivity
    /* JADX INFO: renamed from: c */
    public void selectMenuOption(int i) {
        GameEngine.log("InGameActivityJava selectMenuOption: " + i);
        onSelectMenuOption(i);
    }

    @Override // com.corrodinggames.rts.appFramework.InGameActivity
    /* JADX INFO: renamed from: e */
    public void showSaveGameDialog(String str) {
        ScriptEngine.getInstance().getRoot().makeSaveGamePopup(str);
    }

    @Override // com.corrodinggames.rts.appFramework.InGameActivity
    /* JADX INFO: renamed from: f */
    public void showExportMapDialog(String str) {
        ScriptEngine.getInstance().getRoot().makeExportMapGamePopup(str);
    }

    @Override // com.corrodinggames.rts.appFramework.InGameActivity
    /* JADX INFO: renamed from: d */
    public void onSelectMenuOption(int i) {
        switch (i) {
            case 2:
                GameMainManager.getInstance().showSettings();
                break;
            case 3:
                GameEngine.log("TODO");
                break;
            case 4:
                GameEngine.getInstance().isLookModeEnabled = !GameEngine.getInstance().isLookModeEnabled;
                break;
            case 5:
                GameEngine.log("TODO");
                break;
            case 6:
                GameEngine gameEngine = GameEngine.getInstance();
                gameEngine.isDebugTempMode = !gameEngine.isDebugTempMode;
                break;
            case 9:
                GameEngine gameEngine2 = GameEngine.getInstance();
                if (!gameEngine2.isGameRecording) {
                    gameEngine2.isGameRecording = true;
                } else {
                    gameEngine2.isGameRecording = false;
                }
                break;
            case 10:
                ScriptEngine.getInstance().addScriptToQueue("mp.multiplayerExitPrompt();");
                break;
            case 11:
                GameEngine gameEngine3 = GameEngine.getInstance();
                if (gameEngine3.missionEngine != null && gameEngine3.missionEngine.introText != null) {
                    gameEngine3.showMessageBox("Briefing", gameEngine3.missionEngine.introText);
                    break;
                }
                break;
            case 12:
                showSaveGameDialog(null);
                break;
            case 13:
                ScriptEngine.getInstance().addScriptToQueue("makeSendMessagePopup();");
                break;
            case 14:
                GameEngine gameEngine4 = GameEngine.getInstance();
                if (gameEngine4.networkEngine != null) {
                    gameEngine4.networkEngine.showPlayerListPopup();
                }
                break;
            case 15:
                ScriptEngine.getInstance().addScriptToQueue("showMainMenu();");
                break;
            case 16:
                ScriptEngine.getInstance().addScriptToQueue("makeSendTeamMessagePopup();");
                break;
            case 17:
                ScriptEngine.getInstance().addScriptToQueue("mp.reinviteAsk();");
                break;
            case 18:
                showExportMapDialog(null);
                break;
            case 19:
                ScriptEngine.getInstance().addScriptToQueue("mp.surrenderPrompt();");
                break;
            case 20:
                ScriptEngine.getInstance().addScriptToQueue("showMainMenu();");
                break;
            case 21:
                ScriptEngine.getInstance().addScriptToQueue("showBattleroom();");
                break;
            case 22:
                GameEngine gameEngine5 = GameEngine.getInstance();
                gameEngine5.isMenuOpen = true;
                gameEngine5.gameUI.isDraggingSelection = false;
                break;
            case 23:
                GameMainManager.getInstance().showLeaderboard();
                break;
        }
    }

    @Override // com.corrodinggames.rts.appFramework.InGameActivity
    public void m() {
        ScriptEngine.getInstance().addScriptToQueue("showMainMenu();");
    }
}
