package com.corrodinggames.rts.appFramework;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.GameMode;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.local.Locale;
import com.corrodinggames.rts.gameFramework.steam.DisabledSteamEngine;

/* JADX INFO: renamed from: com.corrodinggames.rts.appFramework.g */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/appFramework/g.class */
public class InGameActivity extends TaskQueueActivity {

    /* JADX INFO: renamed from: c */
    GameView gameView;

    /* JADX INFO: renamed from: e */
    ProgressDialog progressDialog;

    /* JADX INFO: renamed from: d */
    final Handler handler = new Handler(Looper.b());

    /* JADX INFO: renamed from: f */
    boolean running = true;

    @Override // android.app.Activity
    public void b() {
        GameEngine.log("IngameActivity: finish");
        super.b();
        AppFrameworkUtils.onActivitySetContentView((Activity) this, true);
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (z) {
            AppFrameworkUtils.onActivityNewIntent((Activity) this, false, true);
        }
        this.gameView.resume(z);
    }

    @Override // android.app.Activity
    public boolean a(Menu menu) {
        super.a(menu);
        menu.clear();
        GameEngine gameEngine = GameEngine.getInstance();
        menu.add(0, 12, 0, Locale.get("menus.ingame.save", new Object[0])).setIcon(R.drawable.ic_menu_save);
        if (gameEngine.isGameStarted && !GameEngine.isIOSVersion) {
            menu.add(0, 18, 0, Locale.get("menus.ingame.exportMap", new Object[0])).setIcon(R.drawable.ic_menu_save);
        }
        menu.add(0, 2, 0, Locale.get("menus.ingame.settings", new Object[0])).setIcon(R.drawable.ic_menu_preferences);
        if (!gameEngine.isNetworkConnected()) {
        }
        if (gameEngine.replayEngine != null && gameEngine.replayEngine.j()) {
            menu.add(0, 22, 0, Locale.get("menus.ingame.hideInterface", new Object[0])).setIcon(R.drawable.ic_menu_send);
        }
        if (gameEngine.isNetworkConnected()) {
            menu.add(0, 13, 0, Locale.get("menus.ingame.chat", new Object[0])).setIcon(R.drawable.ic_menu_send);
            menu.add(0, 14, 0, Locale.get("menus.ingame.players", new Object[0])).setIcon(R.drawable.ic_menu_sort_by_size);
            if (gameEngine.networkEngine.isServer && DisabledSteamEngine.a().e()) {
                menu.add(0, 17, 0, Locale.get("menus.ingame.steam_reinvite", new Object[0])).setIcon(R.drawable.ic_menu_send);
            }
            boolean z = false;
            if (gameEngine.playerTeam != null && gameEngine.playerTeam.isTeamWipedOut) {
                z = true;
            }
            if (!z && !gameEngine.hasWonGame) {
                menu.add(0, 19, 0, Locale.get("menus.ingame.surrender", new Object[0])).setIcon(R.drawable.ic_lock_power_off);
            }
            if (!gameEngine.networkEngine.isServer) {
                menu.add(0, 10, 0, Locale.get("menus.ingame.disconnect", new Object[0])).setIcon(R.drawable.ic_lock_power_off);
            } else {
                menu.add(0, 10, 0, Locale.get("menus.ingame.exitGame", new Object[0])).setIcon(R.drawable.ic_lock_power_off);
            }
        } else {
            if (gameEngine.missionEngine != null && gameEngine.missionEngine.introText != null) {
                menu.add(0, 11, 0, Locale.get("menus.ingame.briefing", new Object[0])).setIcon(R.drawable.ic_dialog_info);
            }
            menu.add(0, 15, 0, Locale.get("menus.ingame.exitGame", new Object[0])).setIcon(R.drawable.ic_lock_power_off);
        }
        if (gameEngine != null && gameEngine.settingsEngine.allowGameRecording) {
            if (!gameEngine.isGameRecording) {
                menu.add(0, 9, 0, "Start Recording");
                return true;
            }
            menu.add(0, 9, 0, "Stop Recording");
            return true;
        }
        return true;
    }

    /* JADX INFO: renamed from: c */
    public void selectMenuOption(final int i) {
        GameEngine.log("outer selectMenuOption: " + i);
        this.handler.a(new Runnable() { // from class: com.corrodinggames.rts.appFramework.g.1
            @Override // java.lang.Runnable
            public void run() {
                GameEngine.log("inner selectMenuOption: " + i);
                InGameActivity.this.onSelectMenuOption(i);
            }
        });
    }

    /* JADX INFO: renamed from: d */
    public void onSelectMenuOption(int i) {
        switch (i) {
            case 2:
                a(new Intent(k(), (Class<?>) SelectFolderActivity.class), 0);
                break;
            case 3:
                new AlertDialog.Builder(this).setIcon(R.drawable.ic_dialog_alert).setTitle("Skip?").setMessage("Are you sure you want to skip this level?").setPositiveButton("Yes", new DialogInterface.OnClickListener() { // from class: com.corrodinggames.rts.appFramework.g.9
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        GameEngine.getInstance().shouldAdvanceAfterGameEnd = true;
                    }
                }).setNegativeButton("No", (DialogInterface.OnClickListener) null).show();
                break;
            case 4:
                GameEngine.getInstance().isLookModeEnabled = !GameEngine.getInstance().isLookModeEnabled;
                break;
            case 5:
                new AlertDialog.Builder(this).setIcon(R.drawable.ic_dialog_alert).setTitle("Restart?").setMessage("Are you sure you want to restart this level?").setPositiveButton("Yes", new DialogInterface.OnClickListener() { // from class: com.corrodinggames.rts.appFramework.g.10
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        GameEngine gameEngine = GameEngine.getInstance();
                        gameEngine.stopGameThread();
                        gameEngine.loadGame(true, GameMode.normal);
                        gameEngine.startGameThread();
                    }
                }).setNegativeButton("No", (DialogInterface.OnClickListener) null).show();
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
                GameEngine gameEngine3 = GameEngine.getInstance();
                String str = Locale.get("menus.ingame.multiplayerClose.titleDisconnect", new Object[0]);
                String str2 = Locale.get("menus.ingame.multiplayerClose.messageDisconnect", new Object[0]);
                String str3 = Locale.get("menus.ingame.multiplayerClose.disconnectButton", new Object[0]);
                if (gameEngine3.networkEngine.isServer) {
                    str = Locale.get("menus.ingame.multiplayerClose.title", new Object[0]);
                    str2 = Locale.get("menus.ingame.multiplayerClose.messageEndGame", new Object[0]);
                    str3 = Locale.get("menus.ingame.exitGame", new Object[0]);
                }
                AlertDialog.Builder negativeButton = new AlertDialog.Builder(this).setIcon(R.drawable.ic_dialog_alert).setTitle(str).setMessage(str2).setPositiveButton(str3, new DialogInterface.OnClickListener() { // from class: com.corrodinggames.rts.appFramework.g.13
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        GameEngine.getInstance().networkEngine.disconnectNetworking("exited");
                        InGameActivity.this.b();
                    }
                }).setNegativeButton(Locale.get("menus.common.back", new Object[0]), (DialogInterface.OnClickListener) null);
                if (gameEngine3.networkEngine.isServer) {
                    negativeButton.setNeutralButton(Locale.get("menus.ingame.multiplayerClose.returnToBattleroom", new Object[0]), new DialogInterface.OnClickListener() { // from class: com.corrodinggames.rts.appFramework.g.14
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i2) {
                            GameEngine.log("Returning to battleroom clicked.");
                            GameEngine gameEngine4 = GameEngine.getInstance();
                            gameEngine4.networkEngine.scheduleDefaultReturnToBattleroom();
                            gameEngine4.gameUI.isDraggingSelection = false;
                        }
                    });
                }
                negativeButton.show();
                break;
            case 11:
                GameEngine gameEngine4 = GameEngine.getInstance();
                if (gameEngine4.missionEngine != null && gameEngine4.missionEngine.introText != null) {
                    gameEngine4.showMessageBox("Briefing", gameEngine4.missionEngine.introText);
                    break;
                }
                break;
            case 12:
                Runnable runnable = new Runnable() { // from class: com.corrodinggames.rts.appFramework.g.11
                    @Override // java.lang.Runnable
                    public void run() {
                        if (AppFrameworkUtils.requestStoragePermission(InGameActivity.this)) {
                            InGameActivity.this.showSaveGameDialog(null);
                        }
                    }
                };
                if (!AppFrameworkUtils.askForStoragePermission(this, runnable)) {
                    runnable.run();
                }
                break;
            case 13:
                showChatDialog(false);
                break;
            case 14:
                GameEngine gameEngine5 = GameEngine.getInstance();
                if (gameEngine5.networkEngine != null) {
                    gameEngine5.networkEngine.showPlayerListPopup();
                }
                break;
            case 15:
                new AlertDialog.Builder(this).setIcon(R.drawable.ic_dialog_alert).setTitle("Exit?").setMessage("Are you sure you want to exit this game?").setPositiveButton("Yes", new DialogInterface.OnClickListener() { // from class: com.corrodinggames.rts.appFramework.g.15
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        InGameActivity.this.b();
                    }
                }).setNegativeButton("No", (DialogInterface.OnClickListener) null).show();
                break;
            case 16:
                showChatDialog(true);
                break;
            case 18:
                if (AppFrameworkUtils.requestStoragePermission((Activity) this)) {
                    showExportMapDialog(null);
                }
                break;
            case 19:
                new AlertDialog.Builder(this).setIcon(R.drawable.ic_dialog_alert).setTitle("Disconnect?").setMessage("Are you sure you want to surrender this game?").setPositiveButton("Surrender", new DialogInterface.OnClickListener() { // from class: com.corrodinggames.rts.appFramework.g.12
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        GameEngine.getInstance().networkEngine.sendChatMessage("-surrender");
                    }
                }).setNegativeButton("No", (DialogInterface.OnClickListener) null).show();
                break;
            case 20:
                b();
                break;
            case 21:
                b();
                MultiplayerBattleroomActivity.updateUI();
                MultiplayerBattleroomActivity.refreshChatLog();
                break;
            case 22:
                GameEngine gameEngine6 = GameEngine.getInstance();
                gameEngine6.isMenuOpen = true;
                gameEngine6.gameUI.isDraggingSelection = false;
                break;
            case 23:
                GameEngine.log("TODO display leaderboard settings");
                break;
        }
    }

    /* JADX INFO: renamed from: a */
    private void showChatDialog(final boolean z) {
        GameEngine gameEngine = GameEngine.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (!z) {
            builder.setTitle("Send Message");
        } else {
            builder.setTitle("Send Team Message");
        }
        View viewInflate = LayoutInflater.from(this).inflate(com.corrodinggames.rts.R.layout.alert_chat, (ViewGroup) null);
        builder.setView(viewInflate);
        TextView textView = (TextView) viewInflate.findViewById(com.corrodinggames.rts.R.id.chat_messages);
        final EditText editText = (EditText) viewInflate.findViewById(com.corrodinggames.rts.R.id.chat_text);
        textView.setText(gameEngine.networkEngine.chatLog.getAllMessagesText());
        editText.setText(VariableScope.nullOrMissingString);
        editText.requestFocus();
        builder.setPositiveButton(z ? "Send Team" : "Send", new DialogInterface.OnClickListener() { // from class: com.corrodinggames.rts.appFramework.g.16
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                String string = editText.getText().toString();
                GameEngine gameEngine2 = GameEngine.getInstance();
                if (!string.trim().equals(VariableScope.nullOrMissingString)) {
                    if (z) {
                        gameEngine2.networkEngine.l(string);
                    } else {
                        gameEngine2.networkEngine.sendChatMessage(string);
                    }
                }
                gameEngine2.gameUI.isDraggingSelection = false;
            }
        });
        builder.setNeutralButton("Send & Ping Map", new DialogInterface.OnClickListener() { // from class: com.corrodinggames.rts.appFramework.g.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                String string = editText.getText().toString();
                GameEngine gameEngine2 = GameEngine.getInstance();
                if (!string.trim().equals(VariableScope.nullOrMissingString)) {
                    if (z) {
                        gameEngine2.networkEngine.l(string);
                    } else {
                        gameEngine2.networkEngine.sendChatMessage(string);
                    }
                }
                gameEngine2.gameUI.isDraggingSelection = false;
                gameEngine2.gameUI.activatePingMapMode();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.corrodinggames.rts.appFramework.g.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: e */
    public void showExportMapDialog(String str) {
        final GameEngine gameEngine = GameEngine.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Export Map");
        builder.setMessage("Enter a name to export the map as");
        final EditText editText = new EditText(this);
        if (str == null) {
            editText.setText(("New " + gameEngine.getCurrentMapName() + " (" + Utility.formatCurrentDate("d MMM yyyy").replace(".", VariableScope.nullOrMissingString) + " " + Utility.formatCurrentDate("HH.mm.ss") + ")").replace("  ", " "));
        } else {
            editText.setText(str);
        }
        builder.setView(editText);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() { // from class: com.corrodinggames.rts.appFramework.g.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                final String string = editText.getText().toString();
                if (string.contains("/") || string.contains("\\") || string.contains(":") || string.contains("*") || string.contains("?") || string.contains("\"") || string.contains("<") || string.contains(">")) {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(InGameActivity.this);
                    builder2.setTitle("Bad Map Name");
                    builder2.setMessage("The characters /\\:*?\"<> are not allowed (fat32 formatting)");
                    builder2.setPositiveButton("Ok", new DialogInterface.OnClickListener() { // from class: com.corrodinggames.rts.appFramework.g.4.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface2, int i2) {
                            InGameActivity.this.showExportMapDialog(string);
                        }
                    });
                    builder2.show();
                    return;
                }
                gameEngine.tileMap.exportMap(gameEngine.currentMapPath, "/SD/rustedWarfare/maps/" + string + ".tmx");
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.corrodinggames.rts.appFramework.g.5
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: f */
    public void showSaveGameDialog(String str) {
        GameEngine gameEngine = GameEngine.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Game");
        builder.setMessage("Enter a name to save the game under");
        final EditText editText = new EditText(this);
        if (str == null) {
            editText.setText(gameEngine.getCurrentMapName() + " (" + Utility.formatCurrentDate("d MMM yyyy").replace(".", VariableScope.nullOrMissingString) + " " + Utility.formatCurrentDate("HH.mm.ss") + ")");
        } else {
            editText.setText(str);
        }
        builder.setView(editText);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() { // from class: com.corrodinggames.rts.appFramework.g.6
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                final String string = editText.getText().toString();
                if (string.contains("/") || string.contains("\\") || string.contains(":") || string.contains("*") || string.contains("?") || string.contains("\"") || string.contains("<") || string.contains(">")) {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(InGameActivity.this);
                    builder2.setTitle("Bad Save Name");
                    builder2.setMessage("The characters /\\:*?\"<> are not allowed (fat32 formatting)");
                    builder2.setPositiveButton("Ok", new DialogInterface.OnClickListener() { // from class: com.corrodinggames.rts.appFramework.g.6.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface2, int i2) {
                            InGameActivity.this.showSaveGameDialog(string);
                        }
                    });
                    builder2.show();
                    return;
                }
                InGameActivity.this.d(string);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: com.corrodinggames.rts.appFramework.g.7
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }

    public void d(String str) {
        a(0);
        LoadGameRunnable loadGameRunnable = new LoadGameRunnable(this);
        loadGameRunnable.saveName = str;
        new Thread(loadGameRunnable).start();
    }

    public void l() {
        this.handler.a(new Runnable() { // from class: com.corrodinggames.rts.appFramework.g.8
            @Override // java.lang.Runnable
            public void run() {
                InGameActivity.this.closeAndRestart();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: n */
    public void closeAndRestart() {
        try {
            a(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.corrodinggames.rts")));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(g(), "Failed to open Android Market", 0).show();
        }
    }

    public void m() {
    }
}
