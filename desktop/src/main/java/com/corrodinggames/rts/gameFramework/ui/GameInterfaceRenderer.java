package com.corrodinggames.rts.gameFramework.ui;

import android.graphics.*;
import android.view.Menu;
import android.view.MenuItem;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.appFramework.GameView;
import com.corrodinggames.rts.appFramework.InGameActivity;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.units.*;
import com.corrodinggames.rts.game.units.actions.*;
import com.corrodinggames.rts.game.units.custom.AnimationTag;
import com.corrodinggames.rts.game.units.custom.CustomUnit;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig;
import com.corrodinggames.rts.game.units.custom.condition.StoredResourceEntry;
import com.corrodinggames.rts.game.units.custom.condition.StoredResources;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.game.units.custom.price.UnitPrice;
import com.corrodinggames.rts.game.units.g.SpecialActionBlockEffect;
import com.corrodinggames.rts.gameFramework.*;
import com.corrodinggames.rts.gameFramework.audio.SoundEngine;
import com.corrodinggames.rts.gameFramework.graphics.GamePaint;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.graphics.opengl.GraphicsUtils;
import com.corrodinggames.rts.gameFramework.local.Locale;
import com.corrodinggames.rts.gameFramework.mod.ModInfo;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.platform.PlatformExtension;
import com.corrodinggames.rts.gameFramework.statistics.AndroidMenu;
import com.corrodinggames.rts.gameFramework.stats.TeamStats;
import com.corrodinggames.rts.gameFramework.utility.FastArrayList;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.f.a */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/f/a.class */
public class GameInterfaceRenderer extends Serializable {

    /* JADX INFO: renamed from: a */
    GameUI gameUI;

    /* JADX INFO: renamed from: b */
    GameEngine gameEngine;

    /* JADX INFO: renamed from: e */
    public boolean isSelecting;

    /* JADX INFO: renamed from: f */
    public float selectionTimer;

    /* JADX INFO: renamed from: n */
    GamePaint actionIconPaint;

    /* JADX INFO: renamed from: o */
    GamePaint buildActionIconPaint;

    /* JADX INFO: renamed from: q */
    Paint textPaint;

    /* JADX INFO: renamed from: r */
    Paint textPaint2;

    /* JADX INFO: renamed from: D */
    boolean isZoomButtonPressed;

    /* JADX INFO: renamed from: E */
    float initialTouchY;

    /* JADX INFO: renamed from: F */
    float zoomButtonTimer;

    /* JADX INFO: renamed from: G */
    float mouseWheelZoomAccumulator;
    /* JADX INFO: renamed from: H */
    int nextKeyBindingIndex;

    /* JADX INFO: renamed from: I */
    boolean isMultiTouchZooming;

    /* JADX INFO: renamed from: J */
    float multiTouchCenterX;

    /* JADX INFO: renamed from: K */
    float multiTouchCenterY;

    /* JADX INFO: renamed from: L */
    float multiTouchInitialDistance;

    /* JADX INFO: renamed from: M */
    float multiTouchCurrentX;

    /* JADX INFO: renamed from: N */
    float multiTouchCurrentY;

    /* JADX INFO: renamed from: O */
    float multiTouchCurrentDistance;

    /* JADX INFO: renamed from: P */
    int multiTouchPointerCount;

    /* JADX INFO: renamed from: Q */
    float initialZoomLevel;

    /* JADX INFO: renamed from: R */
    float gestureZoomTimer;

    /* JADX INFO: renamed from: Y */
    static Paint staticPaint = new Paint();

    /* JADX INFO: renamed from: Z */
    static PorterDuffColorFilter staticColorFilter = new PorterDuffColorFilter(Color.a(200, 255, 200), PorterDuff.Mode.MULTIPLY);

    /* JADX INFO: renamed from: aa */
    BaseUnit selectedUnit;

    /* JADX INFO: renamed from: ab */
    AbstractUnitAction unitCommand;

    /* JADX INFO: renamed from: ac */
    float commandTimer;

    /* JADX INFO: renamed from: ad */
    long lastCommandTime;

    /* JADX INFO: renamed from: ae */
    float commandX;

    /* JADX INFO: renamed from: af */
    float commandY;

    /* JADX INFO: renamed from: ag */
    String unselectAllText;

    /* JADX INFO: renamed from: ah */
    String allyUnitText;

    /* JADX INFO: renamed from: ai */
    String enemyUnitText;

    /* JADX INFO: renamed from: aj */
    String neutralUnitText;

    /* JADX INFO: renamed from: ak */
    String ownedByText;

    /* JADX INFO: renamed from: al */
    public String unitCapReachedText;

    /* JADX INFO: renamed from: ao */
    public float infoTextAlpha;

    /* JADX INFO: renamed from: ap */
    public boolean showInfoText;

    /* JADX INFO: renamed from: aC */
    float infoTextTimer;

    /* JADX INFO: renamed from: c */
    public boolean isDragging = false;

    /* JADX INFO: renamed from: d */
    public boolean isDraggingSelectionBox = false;

    /* JADX INFO: renamed from: g */
    Paint paintSelectionBox = new Paint();

    /* JADX INFO: renamed from: h */
    Paint paintHealthBar = new Paint();

    /* JADX INFO: renamed from: i */
    Paint paintUnitInfo = new Paint();

    /* JADX INFO: renamed from: j */
    Paint paintUnitName = new Paint();

    /* JADX INFO: renamed from: k */
    Paint paintUnitType = new Paint();

    /* JADX INFO: renamed from: l */
    Paint paintUnitTeam = new Paint();

    /* JADX INFO: renamed from: m */
    Paint paintUnitStatus = new Paint();

    /* JADX INFO: renamed from: p */
    Paint paintMinimap = new Paint();

    /* JADX INFO: renamed from: s */
    Rect zoomButtonRect = new Rect();

    /* JADX INFO: renamed from: t */
    RectF zoomButtonRectF = new RectF();

    /* JADX INFO: renamed from: u */
    Rect unitRect = new Rect();

    /* JADX INFO: renamed from: v */
    Rect unitRect2 = new Rect();

    /* JADX INFO: renamed from: w */
    Rect unitRect3 = new Rect();

    /* JADX INFO: renamed from: x */
    RectF selectionRectF = new RectF();

    /* JADX INFO: renamed from: y */
    RectF selectionRectF2 = new RectF();

    /* JADX INFO: renamed from: z */
    Rect selectionRect = new Rect();

    /* JADX INFO: renamed from: A */
    RectF selectionRectF3 = new RectF();

    /* JADX INFO: renamed from: B */
    Rect minimapRect = new Rect();

    /* JADX INFO: renamed from: C */
    RectF minimapRectF = new RectF();

    /* JADX INFO: renamed from: S */
    Texture zoomButtonTexture = null;

    /* JADX INFO: renamed from: T */
    Texture lockIconTexture = null;

    /* JADX INFO: renamed from: U */
    Texture pauseTexture = null;

    /* JADX INFO: renamed from: V */
    Texture replayPauseTexture = null;

    /* JADX INFO: renamed from: W */
    Texture fastTexture = null;

    /* JADX INFO: renamed from: X */
    Texture leaderboardTexture = null;

    /* JADX INFO: renamed from: am */
    String messageText = null;

    /* JADX INFO: renamed from: an */
    float messageTimer = 0.0f;

    /* JADX INFO: renamed from: aq */
    ArrayList unitList = new ArrayList();
    /* JADX INFO: renamed from: ar */
    UnitInfoAction unitInfoActionDefault = new UnitInfoAction(false);

    /* JADX INFO: renamed from: as */
    UnitInfoAction unitInfoAction = new UnitInfoAction(true);

    /* JADX INFO: renamed from: at */
    AttackModeAction attackModeAction = new AttackModeAction();

    /* JADX INFO: renamed from: au */
    ArrayList unitList2 = new ArrayList();

    /* JADX INFO: renamed from: av */
    ArrayList<SelectUnitTypeAction> unitList3 = new ArrayList();

    /* JADX INFO: renamed from: aw */
    FastArrayList<WrapperUnitAction> gameObjectList = new FastArrayList();

    /* JADX INFO: renamed from: ax */
    ArrayList unitList4 = new ArrayList();

    /* JADX INFO: renamed from: ay */
    RectF rectF = new RectF();
    /* JADX INFO: renamed from: az */
    HashMap keyBindingActionMap = new HashMap();

    /* JADX INFO: renamed from: aA */
    ArrayList unitGroupMarkers = new ArrayList();

    /* JADX INFO: renamed from: aB */
    Rect rect = new Rect();

    /* JADX INFO: renamed from: aD */
    AndroidMenu vObject = new AndroidMenu();

    GameInterfaceRenderer(GameEngine gameEngine, GameUI gameUI) {
        this.gameUI = gameUI;
        this.gameEngine = gameEngine;
        initializeUI();
    }

    /* JADX INFO: renamed from: a */
    public void initializeStrings() {
        this.unselectAllText = Locale.get("gui.unselectall", new Object[0]);
        this.allyUnitText = Locale.get("gui.common.allyUnit", new Object[0]);
        this.enemyUnitText = Locale.get("gui.common.enemyUnit", new Object[0]);
        this.neutralUnitText = Locale.get("gui.common.neutralUnit", new Object[0]);
        this.ownedByText = Locale.get("gui.infoText.ownedBy", new Object[0]);
        this.unitCapReachedText = Locale.get("gui.infoText.unitCapReached", new Object[0]);
    }

    /* JADX INFO: renamed from: b */
    public void initializeUI() {
        initializeStrings();
        this.zoomButtonTexture = this.gameEngine.renderGraphicsEngine.a(R.drawable.zoom_button);
        this.lockIconTexture = this.gameEngine.renderGraphicsEngine.a(R.drawable.lock_icon_menu);
        this.pauseTexture = this.gameEngine.renderGraphicsEngine.a(R.drawable.pause);
        this.replayPauseTexture = this.gameEngine.renderGraphicsEngine.a(R.drawable.replay_pause);
        this.fastTexture = this.gameEngine.renderGraphicsEngine.a(R.drawable.fast);
        this.leaderboardTexture = this.gameEngine.renderGraphicsEngine.a(R.drawable.replay_leaderboard);
        staticPaint.a(255, 30, 30, 30);
        staticPaint.a(staticColorFilter);
        staticPaint.d(true);
        this.textPaint = new Paint();
        this.textPaint.a(255, 255, 255, 255);
        this.textPaint.a(Paint.Align.LEFT);
        this.textPaint.c(true);
        this.textPaint.a(true);
        this.textPaint2 = new Paint();
        this.textPaint2.a(255, 255, 255, 255);
        this.textPaint2.a(Paint.Align.LEFT);
        this.textPaint2.c(true);
        this.textPaint2.a(true);
        this.actionIconPaint = new GamePaint();
        this.actionIconPaint.b(Color.a(SlickToAndroidKeycodes.AndroidCodes.KEYCODE_BUTTON_3, 255, 255, 255));
        this.actionIconPaint.o();
        this.buildActionIconPaint = new GamePaint();
        this.buildActionIconPaint.b(Color.a(133, 255, 255, 255));
        this.buildActionIconPaint.o();
        this.unitGroupMarkers.clear();
        int i = 0;
        while (i < 10) {
            this.unitGroupMarkers.add(new UnitGroupMarker(this, i < 3));
            i++;
        }
    }

    /* JADX INFO: renamed from: p */
    private float getMaxZoomLevel() {
        float f = 4.6f / this.gameEngine.densityZoomScale;
        if (f > 4.6f) {
            f = 4.6f;
        }
        return f;
    }

    /* JADX INFO: renamed from: q */
    private float getMinZoomLevel() {
        return getScreenRatio() / this.gameEngine.densityZoomScale;
    }

    /* JADX INFO: renamed from: r */
    private float getScreenRatio() {
        if (this.gameEngine.currentScreenWidthPixels / this.gameEngine.tileMap.getWorldWidth() < this.gameEngine.currentScreenHeightPixels / this.gameEngine.tileMap.getWorldHeight()) {
            return this.gameEngine.currentScreenWidthPixels / this.gameEngine.tileMap.getWorldWidth();
        }
        return this.gameEngine.currentScreenHeightPixels / this.gameEngine.tileMap.getWorldHeight();
    }

    public static String clearMessageIfSame(BaseUnit baseUnit, boolean z, boolean z2, boolean z3) {
        String str;
        ModInfo modInfo;
        if (z2) {
            str = "\n";
        } else {
            str = " | ";
        }
        String str2 = VariableScope.nullOrMissingString;
        CustomUnit customUnit = null;
        CustomUnitConfig customUnitConfig = null;
        if (baseUnit instanceof CustomUnit) {
            customUnit = (CustomUnit) baseUnit;
            customUnitConfig = customUnit.unitConfig;
        }
        if (z) {
            str2 = str2 + baseUnit.r().getUnitName() + str;
        }
        if (customUnitConfig == null || !customUnitConfig.canNotBeDirectlyAttacked) {
            if (!z3) {
                str2 = str2 + "HP: " + ((int) Math.ceil(baseUnit.currentHealth)) + "/" + ((int) baseUnit.maxHealth) + str;
            } else {
                str2 = str2 + "HP: " + ((int) baseUnit.maxHealth) + str;
            }
        }
        if (baseUnit.unitEnergyMax != 0.0f) {
            if (!z3) {
                str2 = str2 + "Shield: " + ((int) baseUnit.shield) + "/" + ((int) baseUnit.unitEnergyMax) + str;
            } else {
                str2 = str2 + "Shield: " + ((int) baseUnit.unitEnergyMax) + str;
            }
        }
        if (customUnit != null) {
            float f = customUnit.y.armour;
            if (f >= 1.0f) {
                str2 = str2 + "Armour: " + ((int) f) + str;
            }
        }
        UnitPrice unitPriceDq = baseUnit.dq();
        float fCy = baseUnit.getCreditIncomeRate();
        if (unitPriceDq != null) {
            fCy += unitPriceDq.a();
        }
        if (fCy != 0.0f) {
            if (fCy < 0.0f) {
                str2 = str2 + "Income: -$" + Utility.padString(-fCy, 1) + str;
            } else {
                str2 = str2 + "Income: +$" + Utility.padString(fCy, 1) + str;
            }
        }
        if (baseUnit instanceof OrderableUnit) {
            OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
            if (orderableUnit.bd() != 0.0f && !z3) {
                str2 = str2 + "Energy: " + Utility.padString(baseUnit.currentEnergy) + "/" + Utility.padString(orderableUnit.bd()) + str;
            }
            float moveSpeed = orderableUnit.getMoveSpeed();
            if (!orderableUnit.canExecuteMovementCommands()) {
                moveSpeed = 0.0f;
            }
            if (moveSpeed != 0.0f) {
                str2 = str2 + "Speed: " + Utility.padString(moveSpeed) + str;
            }
            if (orderableUnit.canAttack()) {
                ArrayList<UnitStatistics> arrayListCollectMovementLevelStatistics = orderableUnit.collectMovementLevelStatistics();
                if (arrayListCollectMovementLevelStatistics.size() > 0) {
                    String str3 = str2 + "Attack: ";
                    boolean z4 = true;
                    for (UnitStatistics unitStatistics : arrayListCollectMovementLevelStatistics) {
                        if (!z4) {
                            str3 = str3 + ", ";
                        }
                        z4 = false;
                        String str4 = str3 + Utility.padString(unitStatistics.a);
                        if (unitStatistics.count > 1) {
                            str4 = str4 + "x" + unitStatistics.count;
                        }
                        str3 = str4 + "/" + Utility.padString(unitStatistics.a()) + "s";
                    }
                    str2 = str3 + str;
                }
            }
            float fM = orderableUnit.m();
            if (!orderableUnit.canAttack()) {
                fM = 0.0f;
            }
            if (fM != 0.0f) {
                str2 = str2 + "Range: " + Utility.padString(fM) + str;
            }
            if (z3 && orderableUnit.isUpgradeable()) {
                str2 = str2 + "Upgradable" + str;
            }
        }
        if (!z3 && baseUnit.killCount > 0) {
            str2 = str2 + "Kills: " + baseUnit.killCount + str;
        }
        boolean z5 = false;
        if (GameEngine.getInstance().isDebugTempMode) {
            UnitType unitTypeR = baseUnit.r();
            str2 = ((str2 + "\n") + "--Debug--" + str) + "name: " + unitTypeR.getUnitTypeDescriptionShort() + str;
            if ((unitTypeR instanceof CustomUnitConfig) && (modInfo = ((CustomUnitConfig) unitTypeR).modInfo) != null) {
                str2 = str2 + "(mod: " + Utility.truncateToLength(modInfo.getDisplayTitle(), 30) + ")" + str;
            }
            if (baseUnit.objectId != 0) {
                str2 = str2 + "id: " + baseUnit.objectId + str;
            }
            if (baseUnit.unitFlags != 0) {
                String str5 = VariableScope.nullOrMissingString;
                for (int i = 0; i < 32; i++) {
                    if (UnitPrice.a(baseUnit.unitFlags, i)) {
                        if (str5.length() > 0) {
                            str5 = str5 + ",";
                        }
                        str5 = str5 + i;
                    }
                }
                str2 = str2 + "flags: " + str5 + str;
            }
            if (baseUnit.ammo != 0) {
                str2 = str2 + "ammo: " + baseUnit.ammo + str;
            }
            if (!baseUnit.isUnitParalyzed) {
                str2 = (str2 + "x: " + Utility.padString(baseUnit.posX) + str) + "y: " + Utility.padString(baseUnit.posY) + str;
            }
            if (baseUnit.velocityX != 0.0f || baseUnit.velocityY != 0.0f) {
                str2 = str2 + "x/y speed: " + Utility.padString(baseUnit.velocityX) + ", " + Utility.padString(baseUnit.velocityY) + str;
            }
            if (!baseUnit.isUnitParalyzed) {
                str2 = (str2 + "height: " + Utility.padString(baseUnit.posZ) + str) + "dir: " + Utility.padString(baseUnit.rotationSpeed) + str;
            }
            if (baseUnit.buildProgress < 1.0f) {
                str2 = str2 + "built: " + Utility.padString(baseUnit.buildProgress) + str;
            }
            if (baseUnit instanceof CustomUnit) {
                CustomUnit customUnit2 = (CustomUnit) baseUnit;
                str2 = (str2 + "frame: " + customUnit2.animationFrameIndex + str) + "drawLayer: " + customUnit2.drawLayer + str;
                if (customUnit2.getTags() != null) {
                    str2 = str2 + "tags: " + customUnit2.getTags() + str;
                }
                if (customUnit2.parentEntity != null) {
                    str2 = str2 + "attachedTo: " + customUnit2.parentEntity.getUnitDebugName() + str;
                }
                if (customUnit2.unitTarget2 != null && !customUnit2.unitTarget2.isDead) {
                    str2 = str2 + "customTarget1: " + customUnit2.unitTarget2.getUnitDebugName() + str;
                }
                if (customUnit2.unitTarget3 != null && !customUnit2.unitTarget3.isDead) {
                    str2 = str2 + "customTarget2: " + customUnit2.unitTarget3.getUnitDebugName() + str;
                }
                if (customUnit2.customTimerStamp != -9999) {
                    str2 = str2 + "customTimer: " + Utility.formatSeconds(customUnit2.customTimerStamp / 1000.0f) + str;
                }
                if (customUnit2.unitVariables != null && !customUnit2.unitVariables.isEmpty()) {
                    str2 = str2 + "-- memory --: " + str + customUnit2.unitVariables.debugMemory(true, true) + str;
                }
            }
            z5 = true;
        }
        StoredResources unitAICombatRange = baseUnit.getCustomResources();
        if (unitAICombatRange != null && !unitAICombatRange.c()) {
            String strA = unitAICombatRange.a(z2, true, 10, z5, false);
            if (!strA.equals(VariableScope.nullOrMissingString)) {
                str2 = str2 + strA + str;
            }
        }
        return Utility.removeSuffix(str2, str);
    }

    public static boolean getKeyBindingForAction(AbstractUnitAction abstractUnitAction) {
        return GameEngine.getInstance().isDemo && abstractUnitAction.isQueuable();
    }

    public static boolean isActionVisibleForSelection(BaseUnit baseUnit, BaseUnit baseUnit2) {
        UnitType unitTypeR = baseUnit.r();
        UnitType unitTypeR2 = baseUnit2.r();
        if (unitTypeR == unitTypeR2) {
            return true;
        }
        if ((unitTypeR instanceof CustomUnitConfig) && (unitTypeR2 instanceof CustomUnitConfig)) {
            CustomUnitConfig customUnitConfig = (CustomUnitConfig) unitTypeR;
            CustomUnitConfig customUnitConfig2 = (CustomUnitConfig) unitTypeR2;
            if (customUnitConfig.relatedUnits.contains(unitTypeR2)) {
                return true;
            }
            if (customUnitConfig.showActionsWithMixedSelectionIfOtherUnitsHaveTag != null && AnimationTag.a(customUnitConfig.showActionsWithMixedSelectionIfOtherUnitsHaveTag, customUnitConfig2.x())) {
                return true;
            }
            if (customUnitConfig2.showActionsWithMixedSelectionIfOtherUnitsHaveTag != null && AnimationTag.a(customUnitConfig2.showActionsWithMixedSelectionIfOtherUnitsHaveTag, customUnitConfig.x())) {
                return true;
            }
            return false;
        }
        return false;
    }

    public static String isUnitOnScreen(AbstractUnitAction abstractUnitAction, boolean z) {
        String str;
        if (z) {
            str = "\n";
        } else {
            str = " | ";
        }
        String str2 = VariableScope.nullOrMissingString;
        if (abstractUnitAction instanceof PopupQueueAction) {
            PopupQueueAction popupQueueAction = (PopupQueueAction) abstractUnitAction;
            if (popupQueueAction.K() < 1.0f) {
                GameEngine gameEngine = GameEngine.getInstance();
                float f = -1.0f;
                BaseUnit[] baseUnitArrA = gameEngine.gameUI.selectedUnitsList.a();
                int size = gameEngine.gameUI.selectedUnitsList.size();
                for (int i = 0; i < size; i++) {
                    float unitAIPathfindMemory = baseUnitArrA[i].getNanoFactorySpeed();
                    if (f == -1.0f || unitAIPathfindMemory < f) {
                        f = unitAIPathfindMemory;
                    }
                }
                if (f == -1.0f) {
                    f = 1.0f;
                }
                str2 = str2 + Utility.formatSeconds((1.0f / ((popupQueueAction.K() * f) * 60.0f)) + 1.0E-4f) + str;
            }
        }
        return Utility.removeSuffix(str2, str);
    }

    /* JADX INFO: renamed from: a */
    void handleZoomAndGestures(float f) {
        float touchPointerCount;
        float touchPointerCount2;
        float fDistance;
        float f2;
        if (this.gameEngine.settingsEngine.showZoomButton) {
            float f3 = this.gameEngine.screenScale * 0.7f;
            int i = (int) (50.0f * f3);
            int i2 = (int) this.gameEngine.halfScreenHeight;
            float fC = PlatformExtension.c();
            if (fC > 20.0f) {
                i = (int) (i + (fC - 20.0f));
            }
            if (this.isZoomButtonPressed) {
                this.zoomButtonRect.a(i - 4, (int) (i2 - (50.0f * this.gameEngine.screenScale)), i + 4, (int) (i2 + (50.0f * this.gameEngine.screenScale)));
                this.paintUnitInfo.a();
                this.paintUnitInfo.b(Color.a(255, 0, 0, 0));
                this.gameEngine.renderGraphicsEngine.b(this.zoomButtonRect, this.paintUnitInfo);
            }
            float f4 = i2;
            if (this.gameEngine.targetZoom > 1.0f) {
                f2 = f4 - (((this.gameEngine.targetZoom - 1.0f) * 3.0f) * this.gameEngine.screenScale);
            } else {
                f2 = f4 + (((this.gameEngine.targetZoom * (-20.0f)) + 20.0f + 1.0f) * this.gameEngine.screenScale);
            }
            float f5 = 48.0f * f3;
            float f6 = 54.0f * f3;
            float f7 = f5 / 2.0f;
            float f8 = f6 / 2.0f;
            if (f2 < f8) {
                f2 = f8;
            }
            if (f2 > this.gameEngine.screenHeight - f8) {
                f2 = (int) (this.gameEngine.screenHeight - f8);
            }
            this.zoomButtonRect.a((int) (i - f7), (int) (f2 - f8), (int) (i + f7), (int) (f2 + f8));
            if (!this.isZoomButtonPressed) {
                staticPaint.a(140, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_KATAKANA_HIRAGANA, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_KATAKANA_HIRAGANA, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_KATAKANA_HIRAGANA);
            } else {
                staticPaint.a(255, 255, 255, 255);
            }
            this.gameEngine.renderGraphicsEngine.a(this.zoomButtonTexture, this.zoomButtonRect.a, this.zoomButtonRect.b, staticPaint, 0.0f, f3);
            boolean z = this.isZoomButtonPressed;
            if (!this.isZoomButtonPressed && this.gameUI.isRectHoveredRightClick(this.zoomButtonRect.a, this.zoomButtonRect.b, this.zoomButtonRect.b(), this.zoomButtonRect.c(), IconGroup.zoomButton)) {
                this.isZoomButtonPressed = true;
                this.initialTouchY = this.gameUI.selectionBoxStartY;
            }
            if (!this.gameUI.isMousePressed) {
                this.isZoomButtonPressed = false;
            }
            if (this.isZoomButtonPressed) {
                this.zoomButtonTimer += f;
                this.gameUI.resetMouseState();
                float f9 = this.gameUI.selectionBoxStartY - this.initialTouchY;
                if (f9 > 180.0f) {
                    f9 = 180.0f;
                }
                if (f9 < -180.0f) {
                    f9 = -180.0f;
                }
                float f10 = f9 * this.gameEngine.targetZoom;
                if (f10 > 2.0f) {
                    this.gameEngine.targetZoom -= (5.0E-4f * Utility.abs(f10)) * f;
                    this.gameEngine.isZoomLimitReached = false;
                    if (this.gameEngine.targetZoom < getMinZoomLevel()) {
                        this.gameEngine.targetZoom = getMinZoomLevel();
                        this.gameEngine.isZoomLimitReached = true;
                    }
                } else if (f10 < -2.0f) {
                    this.gameEngine.targetZoom += 5.0E-4f * Utility.abs(f10) * f;
                    this.gameEngine.isZoomLimitReached = false;
                    if (this.gameEngine.targetZoom > getMaxZoomLevel()) {
                        this.gameEngine.targetZoom = getMaxZoomLevel();
                        this.gameEngine.isZoomLimitReached = true;
                    }
                }
            } else {
                if (!z || this.zoomButtonTimer < 12.0f) {
                }
                this.zoomButtonTimer = 0.0f;
            }
        }
        if (this.gameEngine.settingsEngine.mouseSupport) {
            if (this.gameUI.isWorldClickAllowedAt(this.gameEngine.getTouchX(), this.gameEngine.getTouchY()) && !this.gameUI.isKeyboardShiftPressed) {
                int mouseWheelDelta = this.gameEngine.getMouseWheelDelta();
                if (mouseWheelDelta != 0) {
                    this.mouseWheelZoomAccumulator += (mouseWheelDelta / 120.0f) * 0.18f;
                }
                if (this.mouseWheelZoomAccumulator > 1.0f) {
                    this.mouseWheelZoomAccumulator = 1.0f;
                }
                if (this.mouseWheelZoomAccumulator < -1.0f) {
                    this.mouseWheelZoomAccumulator = -1.0f;
                }
            }
            if (this.mouseWheelZoomAccumulator != 0.0f) {
                float f11 = 0.0032f * f;
                if (this.mouseWheelZoomAccumulator < 0.0f) {
                    f11 = -f11;
                }
                float f12 = f11 + (this.mouseWheelZoomAccumulator * 0.18f * f);
                float f13 = this.mouseWheelZoomAccumulator;
                this.mouseWheelZoomAccumulator = Utility.moveTowardsZero(this.mouseWheelZoomAccumulator, Utility.abs(f12));
                if (this.mouseWheelZoomAccumulator == 0.0f) {
                    f12 = f13;
                }
                float f14 = f12 * this.gameEngine.targetZoom;
                this.gameEngine.targetZoom += f14;
                this.gameEngine.shouldRecenterZoomOnPointer = true;
                this.gameEngine.mouseX = this.gameEngine.getTouchX();
                this.gameEngine.mouseY = this.gameEngine.getTouchY();
                if (f14 != 0.0f) {
                    this.gameEngine.isZoomLimitReached = false;
                }
            }
        }
        if (this.gameEngine.settingsEngine.gestureZoom && this.gameEngine.isTouchDown() && this.gameEngine.getTouchPointerCount() >= 3) {
            this.gestureZoomTimer = 20.0f;
        }
        if (this.gestureZoomTimer < 10.0f) {
            this.isMultiTouchZooming = false;
        }
        if (this.gestureZoomTimer > 0.0f) {
            this.gestureZoomTimer = Utility.moveTowardsZero(this.gestureZoomTimer, f);
            boolean z2 = this.gameEngine.isTouchDown() && this.gameEngine.getTouchPointerCount() >= 3;
            this.gameUI.tooltipX = 3.0f;
            float touchX = 0.0f;
            float fLogWarning = 0.0f;
            if (z2) {
                for (int i3 = 0; i3 < this.gameEngine.getTouchPointerCount(); i3++) {
                    touchX += this.gameEngine.getTouchX(i3);
                    fLogWarning += this.gameEngine.getTouchY(i3);
                }
                touchPointerCount = touchX / this.gameEngine.getTouchPointerCount();
                touchPointerCount2 = fLogWarning / this.gameEngine.getTouchPointerCount();
                fDistance = 0.0f;
                for (int i4 = 0; i4 < this.gameEngine.getTouchPointerCount(); i4++) {
                    fDistance += Utility.distance(touchPointerCount, touchPointerCount2, this.gameEngine.getTouchX(i4), this.gameEngine.getTouchY(i4));
                }
            } else {
                touchPointerCount = this.multiTouchCurrentX;
                touchPointerCount2 = this.multiTouchCurrentY;
                fDistance = this.multiTouchCurrentDistance;
            }
            if (this.isMultiTouchZooming && this.multiTouchPointerCount != this.gameEngine.getTouchPointerCount()) {
                this.isMultiTouchZooming = false;
            }
            if (!this.isMultiTouchZooming && z2) {
                this.isMultiTouchZooming = true;
                this.multiTouchCenterX = touchPointerCount;
                this.multiTouchCenterY = touchPointerCount2;
                this.multiTouchInitialDistance = fDistance;
                this.initialZoomLevel = this.gameEngine.targetZoom;
                this.multiTouchCurrentX = touchPointerCount;
                this.multiTouchCurrentY = touchPointerCount2;
                this.multiTouchCurrentDistance = fDistance;
                this.multiTouchPointerCount = this.gameEngine.getTouchPointerCount();
            }
            if (z2) {
                float f15 = (this.multiTouchCurrentY - touchPointerCount2) * 2.0f * this.gameEngine.targetZoom;
                this.gameEngine.targetZoom += (f15 / 250.0f) / this.gameEngine.screenScale;
                this.gameEngine.isZoomLimitReached = false;
                float f16 = this.multiTouchCurrentDistance - fDistance;
                if (0 != 0) {
                    this.gameEngine.targetZoom -= (f16 / 350.0f) / this.gameEngine.screenScale;
                    this.gameEngine.isZoomLimitReached = false;
                }
                this.multiTouchCurrentX = touchPointerCount;
                this.multiTouchCurrentY = touchPointerCount2;
                this.multiTouchCurrentDistance = fDistance;
                this.multiTouchPointerCount = this.gameEngine.getTouchPointerCount();
                for (int i5 = 0; i5 < this.gameEngine.getTouchPointerCount(); i5++) {
                    this.gameEngine.renderGraphicsEngine.a(touchPointerCount, touchPointerCount2, this.gameEngine.getTouchX(i5), this.gameEngine.getTouchY(i5), this.gameUI.minimapUnitPaint);
                }
                this.gameEngine.renderGraphicsEngine.a(touchPointerCount, touchPointerCount2, touchPointerCount, this.multiTouchCenterY, this.gameUI.minimapViewportPaint);
                this.gameEngine.renderGraphicsEngine.a(touchPointerCount, touchPointerCount2, 6.0f, this.gameUI.minimapUnitPaint);
            }
        }
        if (this.gameEngine.targetZoom > getMaxZoomLevel()) {
            this.gameEngine.targetZoom = getMaxZoomLevel();
            this.gameEngine.isZoomLimitReached = true;
        }
        if (this.gameEngine.targetZoom < getMinZoomLevel()) {
            this.gameEngine.targetZoom = getMinZoomLevel();
            this.gameEngine.isZoomLimitReached = true;
        }
    }

    /* JADX INFO: renamed from: b */
    void handleUnitSelection(float f) {
        this.isSelecting = false;
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        int i = 7;
        if (GameEngine.isDesktopMouseInput()) {
            i = 14;
        }
        if (this.gameEngine.isTouchDown() && this.gameUI.currentAction == null) {
            boolean zIsKeyboardSupportAndGraphicsEnabled = this.gameUI.isKeyboardSupportAndGraphicsEnabled(this.gameEngine);
            int i2 = 1;
            if (this.gameEngine.settingsEngine.mouseOrders == 2) {
                i2 = 2;
            }
            int iIsInDebug = this.gameEngine.findTouchPointerIndex(i2);
            if (zIsKeyboardSupportAndGraphicsEnabled || (this.gameEngine.settingsEngine.mouseSupport && iIsInDebug != -1 && !this.gameUI.isRightMousePressed && !this.gameUI.isMiddleMousePressed)) {
                float touchX = this.gameEngine.getTouchX(0);
                float fLogWarning = this.gameEngine.getTouchY(0);
                if (iIsInDebug != -1) {
                    touchX = this.gameEngine.getTouchX(iIsInDebug);
                    fLogWarning = this.gameEngine.getTouchY(iIsInDebug);
                }
                if (!this.isDragging) {
                    z2 = true;
                    this.selectionRectF2.a = (int) touchX;
                    this.selectionRectF2.b = (int) fLogWarning;
                }
                this.selectionRectF2.c = (int) touchX;
                this.selectionRectF2.d = (int) fLogWarning;
                if (Math.abs(this.selectionRectF2.a - this.selectionRectF2.c) > i || Math.abs(this.selectionRectF2.b - this.selectionRectF2.d) > i) {
                    this.isDraggingSelectionBox = true;
                }
                z = true;
            } else if (this.gameEngine.getTouchPointerCount() == 2 && this.gestureZoomTimer == 0.0f) {
                this.selectionRectF2.a = (int) this.gameEngine.getTouchX(0);
                this.selectionRectF2.b = (int) this.gameEngine.getTouchY(0);
                this.selectionRectF2.c = (int) this.gameEngine.getTouchX(1);
                this.selectionRectF2.d = (int) this.gameEngine.getTouchY(1);
                this.isDraggingSelectionBox = false;
                z = true;
            }
            if (z) {
                this.selectionTimer += f;
                if (this.selectionTimer < 18.0f) {
                    z3 = true;
                }
            } else {
                this.selectionTimer = 0.0f;
            }
            if (z) {
                this.isDragging = true;
                if (Math.abs(this.selectionRectF2.a - this.selectionRectF2.c) > i || Math.abs(this.selectionRectF2.b - this.selectionRectF2.d) > i) {
                    this.selectionRect.d = (int) this.selectionRectF2.d;
                    this.selectionRect.b = (int) this.selectionRectF2.b;
                    this.selectionRect.a = (int) this.selectionRectF2.a;
                    this.selectionRect.c = (int) this.selectionRectF2.c;
                    Utility.normalizeRect(this.selectionRect);
                    this.paintSelectionBox.b(Color.a(255, 0, 255, 0));
                    this.paintSelectionBox.a(Paint.Style.STROKE);
                    this.paintSelectionBox.a(1.0f);
                    this.gameEngine.renderGraphicsEngine.b(this.selectionRect, this.paintSelectionBox);
                    this.isSelecting = true;
                }
            }
        }
        boolean z4 = false;
        boolean z5 = false;
        if (this.isDragging && !z) {
            if (z3 && this.gameEngine.getTouchPointerCount() == 3) {
                z5 = true;
            } else {
                z4 = true;
            }
        }
        if (z5) {
            this.isDraggingSelectionBox = false;
            this.isDragging = false;
        }
        if ((z && !z3) || z4) {
            if (z2) {
                for (GameObject gameObject : GameObject.fastGameObjectList) {
                    if (gameObject instanceof UnitBase) {
                        UnitBase unitBase = (UnitBase) gameObject;
                        unitBase.wasSelectedBeforeDrag = unitBase.isSelected;
                    }
                }
            }
            if (z4) {
                this.isDraggingSelectionBox = false;
                this.isDragging = false;
            }
            this.selectionRectF3.a(this.selectionRectF2);
            Utility.normalizeRect(this.selectionRectF3);
            if (Math.abs(this.selectionRectF3.a - this.selectionRectF3.c) > i || Math.abs(this.selectionRectF3.b - this.selectionRectF3.d) > i) {
                this.selectionRectF3.d /= this.gameEngine.zoom;
                this.selectionRectF3.b /= this.gameEngine.zoom;
                this.selectionRectF3.a /= this.gameEngine.zoom;
                this.selectionRectF3.c /= this.gameEngine.zoom;
                this.selectionRectF3.a(this.gameEngine.viewpointXInt, this.gameEngine.viewpointYInt);
                this.gameUI.tooltipX = 4.0f;
                this.gameUI.tooltipY = 40.0f;
                this.gameUI.isSelectionBoxActive = false;
                boolean zDrawRectWithBorder = this.gameUI.isShiftKeyPressed(this.gameEngine);
                boolean zDrawSelectionBox = this.gameUI.isControlKeyPressed(this.gameEngine);
                boolean z6 = true;
                boolean z7 = true;
                boolean z8 = false;
                if (this.gameEngine.settingsEngine.smartSelection_v2) {
                    for (GameObject gameObject2 : GameObject.fastGameObjectList) {
                        if (gameObject2 instanceof OrderableUnit) {
                            OrderableUnit orderableUnit = (OrderableUnit) gameObject2;
                            if (drawUnitCountBadge(orderableUnit) && (!zDrawRectWithBorder || !orderableUnit.wasSelectedBeforeDrag)) {
                                if (!orderableUnit.bI()) {
                                    z6 = false;
                                }
                                if (orderableUnit.aS() && orderableUnit.canAttack()) {
                                    z7 = false;
                                }
                            }
                        }
                    }
                }
                if (zDrawSelectionBox) {
                    z6 = true;
                }
                for (GameObject gameObject3 : GameObject.fastGameObjectList) {
                    if (gameObject3 instanceof UnitBase) {
                        UnitBase unitBase2 = (UnitBase) gameObject3;
                        boolean z9 = false;
                        if (drawUnitCountBadge(unitBase2)) {
                            z9 = true;
                            if (!z6 && unitBase2.bI()) {
                                z9 = false;
                            }
                            if (!z7 && unitBase2.canMove() && !unitBase2.canAttack()) {
                                z9 = false;
                            }
                        }
                        if (zDrawSelectionBox) {
                            if (z9) {
                                z9 = !unitBase2.wasSelectedBeforeDrag;
                            } else if (unitBase2.wasSelectedBeforeDrag) {
                                z9 = true;
                            }
                        } else if (zDrawRectWithBorder && unitBase2.wasSelectedBeforeDrag) {
                            z9 = true;
                        }
                        if (z9) {
                            this.gameUI.selectUnit(unitBase2);
                            if (z4 && unitBase2.lastSelectedTick + 900 < this.gameEngine.renderTimeMillis && ((!zDrawRectWithBorder && !zDrawSelectionBox) || !unitBase2.wasSelectedBeforeDrag)) {
                                z8 = true;
                            }
                        } else {
                            this.gameUI.deselectUnit(unitBase2);
                        }
                    }
                }
                if (z8) {
                    for (GameObject gameObject4 : GameObject.fastGameObjectList) {
                        if (gameObject4 instanceof UnitBase) {
                            ((UnitBase) gameObject4).lastSelectedTick = this.gameEngine.renderTimeMillis;
                        }
                    }
                }
                this.gameUI.emptyMethod();
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public void setMessage(String str, int i) {
        this.messageText = str;
        this.messageTimer = i;
    }

    /* JADX INFO: renamed from: b */
    public void setMessageIfAbsent(String str, int i) {
        if (this.messageTimer <= 0.0f || str.equals(this.messageText)) {
            this.messageText = str;
            this.messageTimer = i;
        }
    }

    public void isDemoQueuableAction(String str) {
        if (this.messageTimer > 0.0f && str.equals(this.messageText)) {
            this.messageTimer = 0.0f;
        }
    }

    /* JADX INFO: renamed from: c */
    public void updateMessageTimer(float f) {
        if (this.messageTimer > 0.0f && this.messageText != null) {
            this.messageTimer = Utility.moveTowardsZero(this.messageTimer, f);
            this.gameEngine.renderGraphicsEngine.a(this.messageText, this.gameEngine.halfScreenWidth, this.gameEngine.halfScreenHeight, this.gameUI.buildingPreviewInvalidPaint, this.gameUI.unitTargetLinePaint, 8.0f);
        }
    }

    /* JADX INFO: renamed from: c */
    public void resetKeyBindingIndex() {
        this.nextKeyBindingIndex = 0;
    }

    public KeyBinding getAvailableActions(AbstractUnitAction abstractUnitAction, int i, ArrayList arrayList) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (!GameEngine.isPC()) {
            return null;
        }
        if (abstractUnitAction.getPrimaryKeyBinding() != null) {
            return abstractUnitAction.getPrimaryKeyBinding();
        }
        if ((abstractUnitAction instanceof RepairTargetAction) || (abstractUnitAction instanceof AttackModeAction)) {
            return null;
        }
        if (abstractUnitAction.getActionDisplayType() == ActionDisplayType.rally) {
            return gameEngine.inputController.T;
        }
        if (abstractUnitAction.getActionType() == ActionType.patrol) {
            return gameEngine.inputController.Q;
        }
        if (abstractUnitAction.getActionType() == ActionType.guardUnit) {
            return gameEngine.inputController.P;
        }
        if (abstractUnitAction.getActionType() == ActionType.reclaimTarget) {
            return gameEngine.inputController.R;
        }
        if (abstractUnitAction.getActionDisplayType() == ActionDisplayType.upgrade) {
            int i2 = 0;
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                AbstractUnitAction abstractUnitAction2 = (AbstractUnitAction) it.next();
                if (abstractUnitAction2 != abstractUnitAction && abstractUnitAction2.getActionDisplayType() == ActionDisplayType.upgrade && this.gameUI.canUpgradeActionForSelectedUnits(abstractUnitAction2)) {
                    i2++;
                }
            }
            if (i2 == 0) {
                return gameEngine.inputController.S;
            }
        }
        ActionDisplayType actionDisplayTypeIsAlsoSelected = abstractUnitAction.getActionDisplayType();
        if (actionDisplayTypeIsAlsoSelected == ActionDisplayType.infoOnly || actionDisplayTypeIsAlsoSelected == ActionDisplayType.infoOnlyNoBox || actionDisplayTypeIsAlsoSelected == ActionDisplayType.infoOnlyStockpile) {
            return null;
        }
        KeyBinding keyBinding = null;
        KeyBinding[] keyBindingArr = gameEngine.inputController.ag;
        if (this.nextKeyBindingIndex < keyBindingArr.length) {
            keyBinding = keyBindingArr[this.nextKeyBindingIndex];
            this.nextKeyBindingIndex++;
        }
        return keyBinding;
    }

    /* JADX INFO: renamed from: d */
    public ArrayList getSelectedUnitTypes() {
        this.unitList2.clear();
        BaseUnit[] baseUnitArrA = this.gameUI.selectedUnitsList.a();
        int size = this.gameUI.selectedUnitsList.size();
        for (int i = 0; i < size; i++) {
            UnitType unitTypeR = baseUnitArrA[i].r();
            if (!this.unitList2.contains(unitTypeR)) {
                this.unitList2.add(unitTypeR);
            }
        }
        return this.unitList2;
    }

    public ArrayList canShowActionsForBothUnits(BaseUnit baseUnit, ArrayList arrayList) {
        FastArrayList<AbstractUnitAction> fastArrayListE;
        ArrayList<AbstractUnitAction> availableActions;
        int size = 0;
        this.unitList.clear();
        int selectedUnitCount = this.gameUI.getSelectedUnitCount();
        if (selectedUnitCount == 0) {
            if (this.gameEngine.settingsEngine.showChatAndPingShortcuts && this.gameEngine.isNetworkGameActive()) {
                this.unitList.add(0, this.gameUI.mapPingToolAction);
                this.unitList.add(0, this.gameUI.teamChatToolAction);
            }
            return this.unitList;
        }
        if (GameUI.showModernSidebar && baseUnit != null && !(baseUnit instanceof EditorOrBuilder)) {
            this.unitList.add(this.unitInfoActionDefault);
            this.unitList.add(this.unitInfoAction);
        }
        if (baseUnit == null) {
        }
        if (baseUnit != null) {
            size = this.unitList.size();
            if (baseUnit.isSelected) {
                if (this.gameUI.canControlUnit(baseUnit)) {
                    ArrayList availableActions2 = baseUnit.getAvailableActions();
                    if (availableActions2 != null) {
                        this.unitList.addAll(availableActions2);
                    }
                } else {
                    ArrayList availableActions3 = baseUnit.getAvailableActions();
                    if (availableActions3 != null) {
                        this.unitList.addAll(availableActions3);
                    }
                }
            }
            int size2 = arrayList.size();
            for (int i = 0; i < size2; i++) {
                BaseUnit baseUnit2 = (BaseUnit) arrayList.get(i);
                if (this.gameUI.canControlUnit(baseUnit2) && ((baseUnit2.r() != baseUnit.r() || baseUnit2.getUpgradeLevel() != baseUnit.getUpgradeLevel()) && (availableActions = baseUnit2.getAvailableActions()) != null)) {
                    for (AbstractUnitAction abstractUnitAction : availableActions) {
                        boolean z = false;
                        Iterator it = this.unitList.iterator();
                        while (it.hasNext()) {
                            if (((AbstractUnitAction) it.next()).getActionId().equals(abstractUnitAction.getActionId())) {
                                z = true;
                            }
                        }
                        if (!z) {
                            this.unitList.add(abstractUnitAction);
                        }
                    }
                }
            }
        }
        boolean z2 = false;
        int size3 = arrayList.size();
        for (int i2 = 0; i2 < size3; i2++) {
            BaseUnit baseUnit3 = (BaseUnit) arrayList.get(i2);
            if (this.gameUI.canControlUnit(baseUnit3) && (baseUnit3 instanceof OrderableUnit) && !((OrderableUnit) baseUnit3).aS()) {
                z2 = true;
            }
        }
        BaseUnit baseUnitE = getFirstSelectedUnit();
        if (!z2 && baseUnitE != null && this.gameUI.canControlUnit(baseUnitE)) {
            this.unitList.add(size, this.gameUI.guardUnitAction);
            this.unitList.add(size, this.gameUI.patrolAction);
        }
        boolean z3 = false;
        if (GameUI.showModernSidebar && (this.gameEngine.settingsEngine.showSelectedUnitsList || selectedUnitCount == 1)) {
            z3 = true;
        }
        if (GameEngine.isAndroidPlatform() && selectedUnitCount > 0) {
            z3 = true;
        }
        if (z3 && !(baseUnit instanceof EditorOrBuilder)) {
            if (selectedUnitCount == 1 && baseUnitE != null && (fastArrayListE = baseUnitE.e(true)) != null && fastArrayListE.size() > 0) {
                for (int i3 = 0; i3 < fastArrayListE.size; i3++) {
                    AbstractUnitAction abstractUnitAction2 = (AbstractUnitAction) fastArrayListE.get(i3);
                    if (abstractUnitAction2 instanceof WrapperUnitAction) {
                        WrapperUnitAction wrapperUnitAction = (WrapperUnitAction) abstractUnitAction2;
                        for (WrapperUnitAction wrapperUnitAction2 : this.gameObjectList) {
                            if (wrapperUnitAction2.a(wrapperUnitAction)) {
                                fastArrayListE.set(i3, wrapperUnitAction2);
                            }
                        }
                    }
                }
                this.gameObjectList.clear();
                for (AbstractUnitAction abstractUnitAction3 : fastArrayListE) {
                    if (abstractUnitAction3 instanceof WrapperUnitAction) {
                        this.gameObjectList.add((WrapperUnitAction) abstractUnitAction3);
                    }
                    this.unitList.add(abstractUnitAction3);
                }
            }
            ArrayList arrayListD = getSelectedUnitTypes();
            this.unitList3.clear();
            Iterator it2 = arrayListD.iterator();
            while (it2.hasNext()) {
                SelectUnitTypeAction selectUnitTypeActionD = ((UnitType) it2.next()).d();
                selectUnitTypeActionD.K();
                this.unitList3.add(selectUnitTypeActionD);
            }
            Collections.sort(this.unitList3);
            if (GameUI.showModernSidebar) {
                Collections.reverse(this.unitList3);
            }
            for (SelectUnitTypeAction selectUnitTypeAction : this.unitList3) {
                if (GameUI.showModernSidebar) {
                    this.unitList.add(0, selectUnitTypeAction);
                } else {
                    this.unitList.add(selectUnitTypeAction);
                }
            }
        }
        return this.unitList;
    }

    /* JADX INFO: renamed from: e */
    BaseUnit getFirstSelectedUnit() {
        if (this.gameUI.selectedUnitsList.size() > 0) {
            return this.gameUI.selectedUnitsList.get(0);
        }
        return null;
    }

    /* JADX INFO: renamed from: f */
    BaseUnit getHighestUpgradeUnitInSelection() {
        BaseUnit baseUnit = null;
        if (this.gameUI.selectedUnitCount > 0) {
            BaseUnit[] baseUnitArrA = this.gameUI.selectedUnitsList.a();
            int i = 0;
            int size = this.gameUI.selectedUnitsList.size();
            while (true) {
                if (i >= size) {
                    break;
                }
                BaseUnit baseUnit2 = baseUnitArrA[i];
                if (baseUnit2.isSelected) {
                    if (baseUnit == null) {
                        baseUnit = baseUnit2;
                    } else {
                        if (!isActionVisibleForSelection(baseUnit, baseUnit2)) {
                            baseUnit = null;
                            break;
                        }
                        if (baseUnit.getUpgradeLevel() > baseUnit2.getUpgradeLevel()) {
                            baseUnit = baseUnit2;
                        }
                    }
                }
                i++;
            }
        }
        return baseUnit;
    }

    /* JADX INFO: renamed from: g */
    ArrayList getSelectedOrderableUnits() {
        this.unitList4.clear();
        BaseUnit[] baseUnitArrA = this.gameUI.selectedUnitsList.a();
        int size = this.gameUI.selectedUnitsList.size();
        for (int i = 0; i < size; i++) {
            BaseUnit baseUnit = baseUnitArrA[i];
            if (baseUnit instanceof OrderableUnit) {
                this.unitList4.add((OrderableUnit) baseUnit);
            }
        }
        return this.unitList4;
    }

    /* JADX INFO: renamed from: h */
    float getActionIconBaseSize() {
        return Utility.clampTo255((this.gameEngine.screenHeight / 14.0f) / this.gameEngine.screenScale, 25.0f * this.gameEngine.screenScale, 40.0f * this.gameEngine.screenScale);
    }

    /* JADX INFO: renamed from: c */
    private boolean isWaitingForTargetOrUnit(AbstractUnitAction abstractUnitAction) {
        if (abstractUnitAction.isWaitingForTarget()) {
            return true;
        }
        if (abstractUnitAction instanceof WrapperUnitAction) {
            return this.gameUI.canControlUnit(((WrapperUnitAction) abstractUnitAction).unit);
        }
        ArrayList<OrderableUnit> arrayListG = getSelectedOrderableUnits();
        ActionId actionId = abstractUnitAction.getActionId();
        for (OrderableUnit orderableUnit : arrayListG) {
            if (orderableUnit.validateActionId(actionId) != null && this.gameUI.canControlUnit(orderableUnit)) {
                return true;
            }
        }
        return false;
    }

    private boolean drawUnselectAllButton(AbstractUnitAction abstractUnitAction, ArrayList arrayList) {
        FilteredUnitAction filteredUnitAction = null;
        if (abstractUnitAction instanceof FilteredUnitAction) {
            filteredUnitAction = (FilteredUnitAction) abstractUnitAction;
        }
        if (filteredUnitAction != null && filteredUnitAction.d == GameUI.globalSelectionCounter) {
            return filteredUnitAction.e;
        }
        boolean zB = isActionAvailableForSelection(abstractUnitAction, arrayList);
        if (filteredUnitAction != null) {
            filteredUnitAction.d = GameUI.globalSelectionCounter;
            filteredUnitAction.e = zB;
        }
        return zB;
    }

    /* JADX INFO: renamed from: b */
    private boolean isActionAvailableForSelection(AbstractUnitAction abstractUnitAction, ArrayList arrayList) {
        if (abstractUnitAction.isWaitingForTarget()) {
            return true;
        }
        if (abstractUnitAction instanceof WrapperUnitAction) {
            WrapperUnitAction wrapperUnitAction = (WrapperUnitAction) abstractUnitAction;
            if (!wrapperUnitAction.isAvailable(wrapperUnitAction.unit)) {
                return false;
            }
            if (this.gameUI.canControlUnit(wrapperUnitAction.unit) || wrapperUnitAction.appendTooltip(wrapperUnitAction.unit, this.gameEngine.playerTeam)) {
                return true;
            }
            return false;
        }
        ActionId actionId = abstractUnitAction.getActionId();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            BaseUnit baseUnit = (OrderableUnit) it.next();
            AbstractUnitAction abstractUnitActionA = baseUnit.validateActionId(actionId);
            if (abstractUnitActionA != null && abstractUnitActionA.isAvailable(baseUnit) && (this.gameUI.canControlUnit(baseUnit) || abstractUnitActionA.appendTooltip(baseUnit, this.gameEngine.playerTeam))) {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: c */
    private boolean canAffordActionForSelection(AbstractUnitAction abstractUnitAction, ArrayList arrayList) {
        if (abstractUnitAction.isWaitingForTarget()) {
            return true;
        }
        if (abstractUnitAction instanceof WrapperUnitAction) {
            WrapperUnitAction wrapperUnitAction = (WrapperUnitAction) abstractUnitAction;
            if (wrapperUnitAction.canAfford((BaseUnit) wrapperUnitAction.unit, true)) {
                return true;
            }
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            BaseUnit baseUnit = (OrderableUnit) it.next();
            AbstractUnitAction abstractUnitActionA = baseUnit.validateActionId(abstractUnitAction.getActionId());
            if (abstractUnitActionA != null && abstractUnitActionA.canAfford(baseUnit, true)) {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: renamed from: d */
    private float getActionProgressForSelection(AbstractUnitAction abstractUnitAction, ArrayList arrayList) {
        int i = 0;
        float f = -1.0f;
        if (abstractUnitAction.isLockedAndDisabled()) {
            return -1.0f;
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            BaseUnit baseUnit = (OrderableUnit) it.next();
            AbstractUnitAction abstractUnitActionA = baseUnit.validateActionId(abstractUnitAction.getActionId());
            if (abstractUnitActionA != null) {
                float progress = abstractUnitActionA.getProgress(baseUnit);
                if (progress > f) {
                    f = progress;
                    i++;
                }
            }
        }
        return f;
    }

    /* JADX INFO: renamed from: d */
    private SpecialActionBlockEffect getActionBlockEffect(AbstractUnitAction abstractUnitAction) {
        float fA = -1.0f;
        SpecialActionBlockEffect specialActionBlockEffect = null;
        if (abstractUnitAction.isLockedAndDisabled()) {
            return null;
        }
        if (abstractUnitAction instanceof WrapperUnitAction) {
            SpecialActionBlockEffect specialActionBlockEffectB = SpecialActionBlockEffect.b(((WrapperUnitAction) abstractUnitAction).unit, abstractUnitAction.getActionId());
            if (specialActionBlockEffectB == null) {
                return null;
            }
            if (-1.0f < specialActionBlockEffectB.getEffectId()) {
                fA = specialActionBlockEffectB.getEffectId();
                specialActionBlockEffect = specialActionBlockEffectB;
            }
        }
        for (BaseUnit baseUnit : this.gameUI.selectedUnitsList) {
            if (baseUnit instanceof OrderableUnit) {
                OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
                if (orderableUnit.validateActionId(abstractUnitAction.getActionId()) == null) {
                    continue;
                } else {
                    SpecialActionBlockEffect specialActionBlockEffectB2 = SpecialActionBlockEffect.b(orderableUnit, abstractUnitAction.getActionId());
                    if (specialActionBlockEffectB2 == null) {
                        return null;
                    }
                    if (fA < specialActionBlockEffectB2.getEffectId()) {
                        fA = specialActionBlockEffectB2.getEffectId();
                        specialActionBlockEffect = specialActionBlockEffectB2;
                    }
                }
            }
        }
        if (specialActionBlockEffect == null) {
            return null;
        }
        return specialActionBlockEffect;
    }

    /* JADX INFO: renamed from: e */
    private float getActionBlockCooldownRatio(AbstractUnitAction abstractUnitAction) {
        SpecialActionBlockEffect specialActionBlockEffectD = getActionBlockEffect(abstractUnitAction);
        if (specialActionBlockEffectD == null) {
            return 0.0f;
        }
        return specialActionBlockEffectD.c();
    }

    /* JADX INFO: renamed from: b */
    float getActionBlockDuration(final AbstractUnitAction s) {
        final SpecialActionBlockEffect drawUnitCommands = this.getActionBlockEffect(s);
        if (drawUnitCommands == null) {
            return 0.0f;
        }
        return (float) drawUnitCommands.d();
    }

    /* JADX INFO: renamed from: d */
    int drawUnitCommands(final float float1) {
        this.showInfoText = false;
        int n = 1;
        if (GameUI.showModernActionIcons) {
            n = 2;
        }
        int n2 = 0;
        boolean b = false;
        UnitActionTimer.updateTimers(float1);
        final ArrayList<BaseUnit> g = this.getSelectedOrderableUnits();
        BaseUnit baseUnit = this.getHighestUpgradeUnitInSelection();
        ArrayList purchase = null;
        if (this.gameUI.currentAction != null) {
            purchase = this.gameUI.currentAction.getCandidateActionList(baseUnit);
        }
        ArrayList getTeamStatusText;
        if (purchase != null) {
            getTeamStatusText = purchase;
        }
        else {
            getTeamStatusText = this.canShowActionsForBothUnits(baseUnit, g);
        }
        if (baseUnit == null && getTeamStatusText.size() > 0) {
            baseUnit = this.getFirstSelectedUnit();
            if (baseUnit == null && CustomUnitConfig.instance != null) {
                baseUnit = BaseUnit.getPrototypeForUnitType(CustomUnitConfig.instance);
            }
        }
        this.gameUI.isUIInitialized = false;
        if (getTeamStatusText.contains(this.gameUI.guardUnitAction)) {
            this.gameUI.isUIInitialized = true;
        }
        if (baseUnit == null) {
            baseUnit = this.getFirstSelectedUnit();
        }
        int n3 = 1;
        if (baseUnit == null) {
            this.lastCommandTime = -1L;
        }
        if (baseUnit != null && getTeamStatusText.size() > 0) {
            final ArrayList<AbstractUnitAction> arrayList = getTeamStatusText;
            float n4 = 2.0f;
            float h = this.getActionIconBaseSize();
            float n5 = 2.0f;
            float n6 = h + n5;
            boolean b2 = false;
            float y;
            float width;
            float n7;
            if (!GameUI.isSidebarOnLeft) {
                y = (float)(this.gameEngine.minimap.getBottomY() + 2);
                width = this.gameEngine.screenWidth - this.gameEngine.minimap.width;
                n7 = this.gameEngine.minimap.width;
            }
            else {
                y = this.gameEngine.minimap.y;
                width = this.gameEngine.minimap.width;
                n7 = this.gameEngine.minimap.width;
                b2 = true;
            }
            if (GameUI.showModernSidebar) {
                h += 15.0f * this.gameEngine.screenScale;
                n6 += 15.0f * this.gameEngine.screenScale;
                n4 = 2.0f * this.gameEngine.screenScale;
                if (GameEngine.isNonPCPlatform()) {
                    n4 = 2.0f * this.gameEngine.screenScale;
                }
                n6 += 2.0f;
                n5 += 2.0f;
                y += 3.0f;
            }
            if (!GameUI.isModernUiLayoutStatic) {
                boolean b3 = true;
                if (this.gameUI.editorOrBuilder != null && this.gameUI.selectedUnitCount == 1 && this.gameUI.editorOrBuilder.isSelected) {
                    b3 = false;
                }
                if (b3) {
                    final float i = this.getActionIconTopOffset();
                    y += i;
                    y += 2.0f;
                }
            }
            int n8 = 0;
            float i = 0.0f;
            float n9 = 0.0f;
            float n10 = 0.0f;
            for (final AbstractUnitAction s : arrayList) {
                if (!this.drawUnselectAllButton(s, g)) {
                    continue;
                }
                ++n8;
                final float currentScreenHeightPixels = n6 * s.getBuildSpeed();
                int keyBinding = n;
                if (s.getKeyBinding() > 0) {
                    keyBinding = s.getKeyBinding();
                }
                final float n11 = n7 / keyBinding;
                int n12 = 0;
                if (n9 + n11 - 0.1f >= n7) {
                    n12 = 1;
                }
                if (n12 == 0 && i > 0.0f && currentScreenHeightPixels + 0.1f < i) {
                    n12 = 1;
                }
                if (n12 != 0) {
                    n10 += i;
                    i = 0.0f;
                    n9 = 0.0f;
                }
                if (i < currentScreenHeightPixels) {
                    i = currentScreenHeightPixels;
                }
                n9 += n11;
            }
            if (n9 > 0.0f) {
                n10 += i;
            }
            final float n13 = y + n10;
            float n14 = y;
            ++y;
            float currentScreenHeightPixels;
            if (this.gameEngine.settingsEngine.showUnitGroups) {
                currentScreenHeightPixels = this.gameEngine.currentScreenHeightPixels - 34.0f * this.gameEngine.screenScale;
            }
            else {
                currentScreenHeightPixels = this.gameEngine.currentScreenHeightPixels;
            }
            this.lastCommandTime = baseUnit.objectId;
            y -= (int)baseUnit.br;
            float n15 = 0.0f;
            final float n11 = 1.0f + h * 0.25f;
            int n12 = (n13 - baseUnit.br > currentScreenHeightPixels + n11) ? 1 : 0;
            final boolean b4 = baseUnit.br > n11;
            this.showInfoText = (n12 != 0 || b4);
            if (this.gameEngine.settingsEngine.mouseSupport && !this.gameUI.isWorldClickAllowedAt(this.gameEngine.getTouchX(), this.gameEngine.getTouchY())) {
                final int mouseWheelDelta = this.gameEngine.getMouseWheelDelta();
                if (mouseWheelDelta != 0) {
                    n15 = -(mouseWheelDelta / 120.0f);
                }
            }
            float n16 = 0.0f;
            if (n15 > 0.0f) {
                this.infoTextAlpha += (float)(0.5 * n6);
            }
            if (n15 < 0.0f) {
                this.infoTextAlpha -= (float)(0.5 * n6);
            }
            if (n12 != 0) {
                final float n17 = 0.4f;
                this.zoomButtonRect.a = (int)(width + 2.0f);
                this.zoomButtonRect.c = (int)(width + n7 - 2.0f);
                this.zoomButtonRect.b = (int)(currentScreenHeightPixels - h * n17);
                this.zoomButtonRect.d = (int)(this.zoomButtonRect.b + h * n17);
                if (this.gameUI.isButtonPressedStyled(this.zoomButtonRect.a, this.zoomButtonRect.b, this.zoomButtonRect.b(), this.zoomButtonRect.c(), "\\/", IconGroup.none, false, Color.a(80, 100, 150, 100), this.gameUI.buildingPreviewPaint, null) && this.gameUI.isInputEnabled()) {
                    n16 += 3.0f * n6;
                    this.gameUI.isSelectionBoxActive = false;
                }
                currentScreenHeightPixels -= n6 * n17 + 2.0f;
            }
            if (b4) {
                final float n17 = 0.4f;
                this.zoomButtonRect.a = (int)(width + 2.0f);
                this.zoomButtonRect.c = (int)(width + n7 - 2.0f);
                this.zoomButtonRect.b = (int)n14;
                this.zoomButtonRect.d = (int)(this.zoomButtonRect.b + h * n17);
                if (this.gameUI.isButtonPressedStyled(this.zoomButtonRect.a, this.zoomButtonRect.b, this.zoomButtonRect.b(), this.zoomButtonRect.c(), "/\\", IconGroup.none, false, Color.a(80, 100, 150, 100), this.gameUI.buildingPreviewPaint, null) && this.gameUI.isInputEnabled()) {
                    n16 -= 3.0f * n6;
                    this.gameUI.isSelectionBoxActive = false;
                }
                n14 += n6 * n17 + 2.0f;
            }
            this.gameEngine.renderGraphicsEngine.i();
            this.rectF.a(0.0f, n14 - 1.0f, this.gameEngine.screenWidth, currentScreenHeightPixels + 1.0f);
            this.gameEngine.renderGraphicsEngine.a(this.rectF);
            if (GameEngine.isNonPCPlatform()) {
                if (this.lastCommandTime != baseUnit.objectId) {
                    this.commandX = 0.0f;
                    this.commandY = baseUnit.br;
                }
                else if (this.infoTextAlpha != 0.0f) {
                    this.commandX = this.infoTextAlpha;
                }
                else {
                    if (!this.gameUI.isMousePressed) {
                        this.infoTextAlpha += this.commandX * float1;
                    }
                    this.commandX = Utility.moveTowardsZero(this.commandX, float1);
                }
            }
            final BaseUnit baseUnit2 = baseUnit;
            baseUnit2.br += this.infoTextAlpha + n16;
            this.infoTextAlpha = 0.0f;
            final float n17 = 0.0f;
            final int n18 = (int)(n13 - currentScreenHeightPixels);
            if (n18 > 0) {
                if (baseUnit.br > n18 + n17) {
                    baseUnit.br = n18 + n17;
                }
                if (baseUnit.br < 0.0f - n17) {
                    baseUnit.br = 0.0f - n17;
                }
            }
            else {
                baseUnit.br = 0.0f;
            }
            int integer = -1;
            float n19 = 0.0f;
            i = 0.0f;
            n9 = 0.0f;
            this.resetKeyBindingIndex();
            for (final AbstractUnitAction unitCommand : arrayList) {
                if (!this.drawUnselectAllButton(unitCommand, g)) {
                    continue;
                }
                ++n2;
                final boolean c = this.canAffordActionForSelection(unitCommand, g);
                ++integer;
                final float n20 = h * unitCommand.getBuildSpeed();
                int keyBinding2 = n;
                if (unitCommand.getKeyBinding() > 0) {
                    keyBinding2 = unitCommand.getKeyBinding();
                }
                final float n21 = n7 / keyBinding2;
                float n22;
                float n23;
                if (!b2) {
                    n22 = n20;
                    n23 = n21;
                }
                else {
                    n22 = n21;
                    n23 = n20;
                }
                int n24 = 0;
                if (n9 + n23 - 0.1f > n7) {
                    n24 = 1;
                }
                if (n24 == 0 && i > 0.0f && n22 + 0.1f < i) {
                    n24 = 1;
                }
                if (n24 != 0) {
                    n19 += i + n5;
                    i = 0.0f;
                    n9 = 0.0f;
                }
                if (i < n22) {
                    i = n22;
                }
                if (!b2) {
                    this.zoomButtonRect.a = (int)(width + n4);
                    this.zoomButtonRect.c = (int)(this.zoomButtonRect.a + n21 - n4 * 2.0f);
                    this.zoomButtonRect.b = (int)(n19 + y);
                    this.zoomButtonRect.d = (int)(this.zoomButtonRect.b + n20);
                    this.zoomButtonRect.a((int)n9, 0);
                }
                else {
                    this.zoomButtonRect.a = (int)(width + n4 + n19);
                    this.zoomButtonRect.c = (int)(this.zoomButtonRect.a + n21 - n4 * 2.0f);
                    this.zoomButtonRect.b = (int)y;
                    this.zoomButtonRect.d = (int)(this.zoomButtonRect.b + n20);
                    this.zoomButtonRect.a(0, (int)n9);
                }
                boolean b5 = true;
                this.zoomButtonRectF.a(this.zoomButtonRect);
                if (!this.zoomButtonRectF.b(this.rectF)) {
                    b5 = false;
                }
                n9 += n23;
                final ActionDisplayType alsoSelected = unitCommand.getActionDisplayType();
                boolean b6 = false;
                if (alsoSelected == ActionDisplayType.infoOnly || alsoSelected == ActionDisplayType.infoOnlyNoBox || alsoSelected == ActionDisplayType.infoOnlyStockpile) {
                    b6 = true;
                }
                final boolean b7 = c;
                final boolean a2 = getKeyBindingForAction(unitCommand);
                final boolean buildOption = unitCommand.isBuildOption();
                Paint paint2 = this.paintUnitName;
                boolean b8 = b7;
                if (alsoSelected == ActionDisplayType.infoOnlyStockpile) {
                    b8 = true;
                }
                if (b8) {
                    paint2.b(Color.a(70, 100, 100, 100));
                }
                else {
                    paint2.b(Color.a(50, 170, 100, 100));
                }
                if (a2) {
                    paint2.b(Color.a(100, 180, 100, 100));
                }
                boolean b9 = false;
                boolean b10 = false;
                if (this.selectedUnit == baseUnit && this.unitCommand == unitCommand) {
                    b9 = true;
                }
                if (this.gameUI.currentAction == unitCommand) {
                    b9 = true;
                    b10 = true;
                }
                if (b9) {
                    paint2.b(Color.a(80, 100, 100, 200));
                }
                if (b10) {
                    paint2.b(Color.a(80, 100, 200, 100));
                }
                GamePaint paint3;
                if (buildOption) {
                    paint2.c((int)(paint2.f() * 0.7f));
                    paint3 = this.buildActionIconPaint;
                }
                else {
                    paint3 = this.actionIconPaint;
                }
                float timerValue = 0.0f;
                if (b5) {
                    timerValue = UnitActionTimer.getTimerValue(baseUnit, unitCommand, false);
                    if (unitCommand.getActionDisplayType() != ActionDisplayType.infoOnlyNoBox) {
                        final boolean setMenuDialog = this.gameUI.isActionTargetingGround(unitCommand);
                        float abs = 0.0f;
                        if (setMenuDialog) {
                            final float n25 = GameEngine.getCurrentTimeMillis() % 1000L / 1000.0f;
                            abs = Utility.abs(Utility.fastCos(n25 * 180.0f));
                        }
                        if (timerValue != 0.0f) {
                            float n25 = Utility.abs(timerValue) * 0.7f - 0.3f;
                            n25 = Utility.clampTo255(n25, 0.0f, 1.0f);
                            int n26;
                            if (timerValue > 0.0f) {
                                n26 = Color.a(110, 210, 210, 210);
                            }
                            else {
                                n26 = Color.a(110, 210, 110, 110);
                            }
                            final int randomIntInRange = Utility.lerpColor(n26, paint2.e(), n25);
                            paint2 = this.paintUnitInfo;
                            paint2.b(randomIntInRange);
                        }
                        this.gameUI.drawTopBarBackground(this.zoomButtonRect, paint2, paint3);
                        float n25 = this.getActionProgressForSelection(unitCommand, g);
                        if (n25 >= 0.0f) {
                            this.paintUnitTeam.a(80, 0, 0, 100);
                            this.minimapRect.a(this.zoomButtonRect);
                            final Rect minimapRect = this.minimapRect;
                            minimapRect.c -= (int)((1.0f - n25) * this.minimapRect.b());
                            this.gameEngine.renderGraphicsEngine.b(this.minimapRect, this.paintUnitTeam);
                            this.paintUnitStatus.a(190, 148, 189, 255);
                            this.gameEngine.renderGraphicsEngine.a((float)this.minimapRect.c, (float)this.minimapRect.b, (float)this.minimapRect.c, (float)this.minimapRect.d, this.paintUnitTeam);
                        }
                        else {
                            final float e = this.getActionBlockCooldownRatio(unitCommand);
                            if (e > 0.0f) {
                                this.paintUnitTeam.a(80, 100, 0, 0);
                                this.minimapRect.a(this.zoomButtonRect);
                                final Rect minimapRect2 = this.minimapRect;
                                minimapRect2.c -= (int)((1.0f - e) * this.minimapRect.b());
                                this.gameEngine.renderGraphicsEngine.b(this.minimapRect, this.paintUnitTeam);
                                this.paintUnitStatus.a(190, 148, 189, 255);
                                this.gameEngine.renderGraphicsEngine.a((float)this.minimapRect.c, (float)this.minimapRect.b, (float)this.minimapRect.c, (float)this.minimapRect.d, this.paintUnitTeam);
                            }
                        }
                        int n26 = Color.a(255, 0, 0, 0);
                        if (GameUI.showModernSidebar) {
                            n26 = Color.a(100, 0, 0, 0);
                            if (buildOption) {
                                n26 = Color.a(50, 155, 155, 155);
                            }
                        }
                        boolean boolean3 = false;
                        if (setMenuDialog) {
                            boolean3 = true;
                            n26 = Color.a((int)(100.0f + 150.0f * abs), 255, 255, 255);
                        }
                        this.gameUI.drawTooltipInset(this.zoomButtonRect, n26, boolean3);
                    }
                }
                final KeyBinding a3 = this.getAvailableActions(unitCommand, integer, arrayList);
                if (a3 != null && b5) {
                    final String c2 = a3.c();
                    final float n25 = (float)this.gameEngine.renderGraphicsEngine.a("A", this.gameUI.selectionBoxPaint);
                    this.gameEngine.renderGraphicsEngine.a(c2, (float)(this.zoomButtonRect.a + 3), this.zoomButtonRect.b + n25 + 1.0f, this.gameUI.selectionBoxPaint);
                }
                boolean b11 = false;
                UnitType as = unitCommand.getUnitType();
                Texture e2 = unitCommand.getIconTexture();
                final BaseUnit showingNotEnoughResources = unitCommand.getUnitShownInUI(baseUnit);
                if (showingNotEnoughResources != null) {
                    as = showingNotEnoughResources.r();
                }
                if (e2 == null && as != null) {
                    e2 = as.z();
                }
                if (e2 != null) {
                    Rect rect = unitCommand.getIconRect();
                    if (rect == null) {
                        rect = this.minimapRect;
                        rect.a(0, 0, e2.m(), e2.l());
                    }
                    final float float2 = this.zoomButtonRect.c() * 0.7f / rect.c();
                    final int n27 = (int)(this.zoomButtonRect.d() - rect.b() * 0.5f * float2);
                    final int a4 = (int)(this.zoomButtonRect.e() - rect.c() * 0.5f * float2);
                    this.paintMinimap.a(100, 255, 255, 255);
                    final RectF minimapRectF = this.minimapRectF;
                    minimapRectF.a((float)n27, (float)a4, n27 + rect.b() * float2, a4 + rect.c() * float2);
                    this.gameEngine.renderGraphicsEngine.a(e2, rect, minimapRectF, this.paintMinimap);
                    b11 = true;
                }
                else if (as != null) {
                    final float float3 = (float)this.zoomButtonRect.d();
                    float float2 = (float)this.zoomButtonRect.e();
                    if (timerValue > 0.5) {
                        ++float2;
                    }
                    if (timerValue < -0.5) {
                        --float2;
                    }
                    float float4 = this.zoomButtonRect.c() * 0.7f;
                    float float5 = this.zoomButtonRect.c() * 0.95f;
                    if (GameUI.showModernSidebar) {
                        float4 = this.zoomButtonRect.c() * 0.4f;
                        float5 = this.zoomButtonRect.c() * 0.85f;
                    }
                    this.selectionRectF.a(this.zoomButtonRect);
                    if (this.selectionRectF.b(this.rectF)) {
                        this.gameEngine.renderGraphicsEngine.i();
                        this.gameEngine.renderGraphicsEngine.a(this.selectionRectF);
                        UnitTypeEnum.drawUnit(as, float3, float2, 0.0f, 0.0f, baseUnit.team, float4, float5, false, false, unitCommand.getQueueSize(), showingNotEnoughResources);
                        if (showingNotEnoughResources != null) {
                            final float n28 = showingNotEnoughResources.x();
                            final float bv = showingNotEnoughResources.bV();
                            if (bv != -1.0f && unitCommand.shouldShowUnitProgressBar(baseUnit)) {
                                final int n29 = 120;
                                final int n30 = Utility.packArgb(200, 0, 0, 150);
                                final int j = Utility.packArgb(120, 0, 0, 230);
                                final Paint a5 = GameViewUtils.a(n30, Paint.Style.FILL);
                                final Paint a6 = GameViewUtils.a(j, Paint.Style.STROKE);
                                final int n31 = 3;
                                final int n32 = (int)(this.selectionRectF.b() / 3.0f) - 3;
                                final int n33 = 0;
                                final int n34 = n32 * 2;
                                this.minimapRectF.a(float3 - n32, float2 + n33, float3 - n32 + n34 * bv, float2 + n33 + n31);
                                this.gameEngine.renderGraphicsEngine.a(this.minimapRectF, a5);
                                this.minimapRectF.a(float3 - n32, float2 + n33, float3 - n32 + n34, float2 + n33 + n31);
                                this.gameEngine.renderGraphicsEngine.a(this.minimapRectF, a6);
                            }
                            else if (n28 != -1.0f && unitCommand.shouldShowUnitHealthBar(baseUnit)) {
                                final int n29 = 120;
                                final int n30 = Utility.packArgb(200, 0, 150, 0);
                                final int j = Utility.packArgb(120, 0, 230, 0);
                                final Paint a7 = GameViewUtils.a(n30, Paint.Style.FILL);
                                final Paint a8 = GameViewUtils.a(j, Paint.Style.STROKE);
                                final int n31 = 3;
                                final int n32 = (int)(this.selectionRectF.b() / 3.0f) - 3;
                                final int n33 = 0;
                                final int n34 = n32 * 2;
                                this.minimapRectF.a(float3 - n32, float2 + n33, float3 - n32 + n34 * n28, float2 + n33 + n31);
                                this.gameEngine.renderGraphicsEngine.a(this.minimapRectF, a7);
                                this.minimapRectF.a(float3 - n32, float2 + n33, float3 - n32 + n34, float2 + n33 + n31);
                                this.gameEngine.renderGraphicsEngine.a(this.minimapRectF, a8);
                            }
                        }
                        this.gameEngine.renderGraphicsEngine.j();
                    }
                    b11 = true;
                }
                final Texture showingNotEnoughEnergy = unitCommand.getExtraIconTexture(baseUnit);
                if (showingNotEnoughEnergy != null) {
                    Rect rect2 = unitCommand.getIconRect();
                    if (rect2 == null) {
                        rect2 = this.minimapRect;
                        rect2.a(0, 0, showingNotEnoughEnergy.m(), showingNotEnoughEnergy.l());
                    }
                    final float float4 = this.zoomButtonRect.c() * 0.7f / rect2.c();
                    final int a4 = (int)(this.zoomButtonRect.d() - rect2.b() * 0.5f * float4);
                    final int n35 = (int)(this.zoomButtonRect.e() - rect2.c() * 0.5f * float4);
                    this.paintMinimap.b(unitCommand.getExtraIconColor());
                    final RectF minimapRectF2 = this.minimapRectF;
                    minimapRectF2.a((float)a4, (float)n35, a4 + rect2.b() * float4, n35 + rect2.c() * float4);
                    this.gameEngine.renderGraphicsEngine.a(showingNotEnoughEnergy, rect2, minimapRectF2, this.paintMinimap);
                    b11 = true;
                }
                if (b5) {
                    final String d = unitCommand.d();
                    if (a2) {
                        this.gameEngine.renderGraphicsEngine.a(this.lockIconTexture, (float)(this.zoomButtonRect.a + 25), this.zoomButtonRect.g(), null);
                    }
                    final float float4 = (float)this.gameEngine.renderGraphicsEngine.b(d, this.gameUI.buildingPreviewPaint);
                    if (float4 > this.zoomButtonRect.b() - 2) {
                        final float float5 = (float)this.gameEngine.renderGraphicsEngine.b(d, this.gameUI.rallyPointPaint);
                        if (float5 > this.zoomButtonRect.b() - 2) {
                            this.paintUnitInfo.a(this.gameUI.selectionBoxBorderPaint);
                        }
                        else {
                            this.paintUnitInfo.a(this.gameUI.rallyPointPaint);
                        }
                    }
                    else {
                        this.paintUnitInfo.a(this.gameUI.buildingPreviewPaint);
                    }
                    if (!b8) {
                        this.paintUnitInfo.b(Color.a(255, 0, 100, 0));
                    }
                    if (alsoSelected == ActionDisplayType.rally) {
                        this.paintUnitInfo.a(255, 255, 255, 255);
                    }
                    else if (alsoSelected == ActionDisplayType.upgrade || alsoSelected == ActionDisplayType.action) {
                        if (!b8) {
                            this.paintUnitInfo.a(255, 19, 101, 94);
                        }
                        else {
                            this.paintUnitInfo.a(255, 39, 202, 189);
                        }
                    }
                    else if (alsoSelected == ActionDisplayType.queueUnit) {
                        final UnitType unitType = unitCommand.getUnitType();
                        if (unitType != null && unitType.g() > 1) {
                            if (!b8) {
                                this.paintUnitInfo.a(255, 117, 120, 15);
                            }
                            else {
                                this.paintUnitInfo.a(255, 235, 240, 30);
                            }
                        }
                    }
                    else if (b6) {
                        this.paintUnitInfo.a(155, 255, 255, 255);
                    }
                    final int a4 = this.gameEngine.renderGraphicsEngine.a(d, this.paintUnitInfo);
                    float n28 = this.zoomButtonRect.g() + a4 / 2;
                    if (b6) {
                        n28 = this.zoomButtonRect.g();
                    }
                    if (b11 && !d.contains((CharSequence)"\n")) {
                        if (b6) {
                            n28 = (float)(this.zoomButtonRect.d - a4 / 2 - 1);
                        }
                        else {
                            n28 = (float)(this.zoomButtonRect.d - 6);
                        }
                    }
                    if (b6) {
                        GraphicsUtils.a(d, this.zoomButtonRect.f(), n28, this.paintUnitInfo);
                    }
                    else {
                        this.gameEngine.renderGraphicsEngine.a(d, this.zoomButtonRect.f(), n28, this.paintUnitInfo);
                    }
                }
                int n36 = 0;
                boolean b12 = false;
                boolean b13 = false;
                if (a3 != null && a3.a()) {
                    n36 = 1;
                    b13 = true;
                }
                this.unitRect.a(this.zoomButtonRect);
                if (GameEngine.isNonPCPlatform()) {
                    Utility.expandRectForTouchTarget(this.unitRect, 2.0f);
                }
                this.gameUI.forceModifiersInsideRect((float)this.unitRect.a, (float)this.unitRect.b, (float)this.unitRect.b(), (float)this.unitRect.c());
                if (!this.isDraggingSelectionBox && this.unitRect.b((int)this.gameUI.selectionBoxMinWidth, (int)this.gameUI.selectionBoxMinHeight) && this.rectF.b((float)(int)this.gameUI.selectionBoxMinWidth, (float)(int)this.gameUI.selectionBoxMinHeight)) {
                    b = true;
                    if (GameEngine.isPC()) {
                        b12 = true;
                    }
                    if ((this.gameUI.isSelectionBoxActive || this.gameUI.isMousePressed) && this.gameUI.isSelectionBoxActive && this.gameUI.isInputEnabled()) {
                        this.gameUI.isSelectionBoxActive = false;
                        n36 = 1;
                    }
                }
                if (GameEngine.isPC() && this.gameUI.currentAction == null) {
                    if (b12) {
                        this.selectedUnit = baseUnit;
                        this.unitCommand = unitCommand;
                        this.commandTimer = n19 + y;
                    }
                    else if (AbstractUnitAction.isSameActionInstance(this.unitCommand, unitCommand)) {
                        this.selectedUnit = null;
                        this.unitCommand = null;
                    }
                }
                boolean boolean4 = false;
                if (n36 != 0 && !b13 && this.gameEngine.settingsEngine.mouseSupport && this.gameEngine.isMouseButtonPressed(2)) {
                    boolean4 = true;
                }
                if (n36 != 0) {
                    GameUI.notifySelectionChanged();
                    if (unitCommand.onClicked(baseUnit, boolean4)) {
                        n36 = 0;
                    }
                    if (this.gameEngine.replayEngine.j()) {
                        n36 = 0;
                    }
                    if (!this.isWaitingForTargetOrUnit(unitCommand)) {
                        n36 = 0;
                    }
                }
                if (n36 != 0) {
                    if (unitCommand.getActionType() == ActionType.none || unitCommand.getActionType() == ActionType.popupQueue) {
                        this.gameUI.currentAction = null;
                        boolean boolean5 = false;
                        if (b13) {
                            boolean5 = true;
                        }
                        else if (!unitCommand.isGuiBlinking()) {
                            boolean5 = true;
                        }
                        else {
                            if (unitCommand.isAlwaysSinglePress(baseUnit)) {
                                boolean5 = true;
                            }
                            else if (this.selectedUnit == baseUnit && AbstractUnitAction.isSameActionInstance(this.unitCommand, unitCommand)) {
                                boolean5 = true;
                            }
                            this.selectedUnit = baseUnit;
                            this.unitCommand = unitCommand;
                            this.commandTimer = n19 + y;
                        }
                        if (boolean5) {
                            int n29 = 1;
                            if (unitCommand.isHighPriority()) {
                                if (this.gameUI.isShiftKeyPressed(this.gameEngine)) {
                                    n29 = 5;
                                }
                                if (this.gameUI.isControlKeyPressed(this.gameEngine)) {
                                    n29 = 10;
                                }
                            }
                            boolean b14 = false;
                            if (!b13) {
                                boolean highPriority = false;
                                if (baseUnit != null && unitCommand.getActiveCount(baseUnit, false) != -1) {
                                    highPriority = true;
                                }
                                if (boolean4 && highPriority) {
                                    b14 = true;
                                }
                            }
                            if (getKeyBindingForAction(unitCommand)) {
                                this.gameEngine.soundEngine.playInterfaceSound(SoundEngine.interfaceErrorSound, 0.8f);
                            }
                            else if (!b7 && !b14) {
                                this.gameEngine.soundEngine.playInterfaceSound(SoundEngine.interfaceErrorSound, 0.8f);
                            }
                            else {
                                final boolean highPriority = unitCommand.isHighPriority();
                                if (highPriority && !b14 && this.gameEngine.playerTeam.getUnitCap() <= this.gameEngine.playerTeam.getNonBuildingUnitCountIncludingQueued()) {
                                    this.gameUI.showMediumPriorityMessage(this.unitCapReachedText);
                                }
                                if (highPriority) {
                                    if (!b14) {
                                        this.gameEngine.soundEngine.playInterfaceSound(SoundEngine.clickAddSound, 0.5f);
                                    }
                                    else {
                                        this.gameEngine.soundEngine.playInterfaceSound(SoundEngine.clickRemoveSound, 0.5f);
                                    }
                                }
                                else {
                                    this.gameEngine.soundEngine.playInterfaceSound(SoundEngine.clickSound, 0.8f);
                                }
                                UnitActionTimer.startTimer(baseUnit, unitCommand, b14, false);
                                for (int j = 0; j < n29; ++j) {
                                    final Command commandForSelectedUnits = this.gameUI.createCommandForSelectedUnits();
                                    if (!unitCommand.isOnlyOneUnitAtATime()) {
                                        this.gameUI.setActionCommandTargets(commandForSelectedUnits, unitCommand);
                                    }
                                    else {
                                        this.gameUI.setActionCommandTarget(commandForSelectedUnits, unitCommand, b14);
                                    }
                                    if (b14) {
                                        commandForSelectedUnits.stopCurrentAction = true;
                                    }
                                    commandForSelectedUnits.setActionId(unitCommand.getQueueId());
                                    if (!b14) {
                                        this.gameUI.prepareUnitActionCommand(unitCommand, null, null, commandForSelectedUnits);
                                    }
                                }
                            }
                        }
                    }
                    else if (unitCommand.getActionType() == ActionType.patrol || unitCommand.getActionType() == ActionType.guardUnit || unitCommand.getActionType() == ActionType.pingMap) {
                        if (boolean4) {
                            if (unitCommand != null && unitCommand.equals(this.gameUI.currentAction)) {
                                this.gameUI.clearCurrentAction();
                            }
                        }
                        else if (!b7) {
                            this.gameEngine.soundEngine.playInterfaceSound(SoundEngine.interfaceErrorSound, 0.8f);
                        }
                        else {
                            UnitActionTimer.startTimer(baseUnit, unitCommand, false, false);
                            this.selectedUnit = null;
                            this.unitCommand = null;
                            this.gameUI.currentAction = unitCommand;
                        }
                    }
                    else if (unitCommand.getActionType() == ActionType.setRally || unitCommand.getActionType() == ActionType.reclaimTarget || unitCommand.getActionType() == ActionType.repairTarget || unitCommand.getActionType() == ActionType.targetGround) {
                        boolean boolean5 = false;
                        boolean b15 = false;
                        if (unitCommand.getActionType() == ActionType.targetGround) {
                            b15 = true;
                        }
                        if (boolean4 && b15) {
                            boolean5 = true;
                        }
                        if (boolean5) {
                            final Command commandForSelectedUnits2 = this.gameUI.createCommandForSelectedUnits();
                            if (!unitCommand.isOnlyOneUnitAtATime()) {
                                this.gameUI.setActionCommandTargets(commandForSelectedUnits2, unitCommand);
                            }
                            else {
                                this.gameUI.setActionCommandTarget(commandForSelectedUnits2, unitCommand, boolean5);
                            }
                            commandForSelectedUnits2.stopCurrentAction = true;
                            commandForSelectedUnits2.setActionId(unitCommand.getQueueId());
                        }
                        else {
                            GameEngine.log("Clicked button: actionActive: " + b7);
                            if (!b7) {
                                this.gameEngine.soundEngine.playInterfaceSound(SoundEngine.interfaceErrorSound, 0.8f);
                            }
                            else {
                                UnitActionTimer.startTimer(baseUnit, unitCommand, false, false);
                                this.selectedUnit = null;
                                this.unitCommand = null;
                                this.gameUI.currentAction = unitCommand;
                            }
                        }
                    }
                    else if (unitCommand.getActionType() == ActionType.placeBuilding) {
                        if (getKeyBindingForAction(unitCommand)) {
                            this.gameEngine.soundEngine.playInterfaceSound(SoundEngine.interfaceErrorSound, 0.8f);
                        }
                        else if (!b7) {
                            this.gameEngine.soundEngine.playInterfaceSound(SoundEngine.interfaceErrorSound, 0.8f);
                        }
                        else {
                            this.gameEngine.soundEngine.playInterfaceSound(SoundEngine.clickSound, 0.8f);
                        }
                        UnitActionTimer.startTimer(baseUnit, unitCommand, false, false);
                        this.selectedUnit = null;
                        this.unitCommand = null;
                        if (this.gameUI.currentAction == null) {
                            this.gameUI.isQueuedBuild = false;
                        }
                        this.gameUI.selectedBuilder = baseUnit;
                        this.gameUI.currentAction = unitCommand;
                        this.gameUI.buildingRotation = 0.0f;
                        this.gameUI.screenFlashRed = -99.0f;
                        this.gameUI.screenFlashGreen = -99.0f;
                        if (!this.gameUI.isBuildingMode) {
                            this.gameUI.buildingPlaceX = this.gameEngine.halfVisibleWorldWidth * this.gameEngine.zoom;
                            this.gameUI.buildingPlaceY = this.gameEngine.halfVisibleWorldHeight * this.gameEngine.zoom;
                        }
                        this.gameUI.isBuildingMode = true;
                        this.gameEngine.tileMap.noop();
                    }
                    else if (unitCommand.getActionType() == ActionType.directToAction) {
                        UnitActionTimer.startTimer(baseUnit, unitCommand, false, false);
                        unitCommand.c(baseUnit);
                    }
                    else {
                        if (unitCommand.getActionType() != ActionType.infoOnly) {
                            throw new RuntimeException("unknown gui action:" + unitCommand.getActionType());
                        }
                        if (unitCommand.selectsUnitOnClick()) {
                            this.selectedUnit = baseUnit;
                            this.unitCommand = unitCommand;
                            this.commandTimer = n19 + y;
                            this.gameUI.currentAction = null;
                        }
                    }
                }
                if (this.unitCommand != unitCommand) {
                    continue;
                }
                n3 = (c ? 1 : 0);
            }
            this.gameEngine.renderGraphicsEngine.j();
            this.rectF.f();
        }
        if (baseUnit != null && baseUnit == this.selectedUnit) {
            if (this.unitCommand != null) {
                boolean boolean6 = true;
                if (GameEngine.isPC()) {
                    boolean6 = false;
                }
                boolean b16 = false;
                if (this.unitCommand.isGuiBlinking()) {
                    b16 = true;
                }
                if (GameEngine.isPC() && this.unitCommand.alwaysShowsTooltip()) {
                    b16 = true;
                }
                if (b16) {
                    boolean b17 = true;
                    if (n3 == 0) {
                        b17 = false;
                    }
                    if (this.gameUI.drawActionTooltipAndHandleInput(this.unitCommand, boolean6, this.selectedUnit, !b17, true, this.commandTimer, false)) {
                        this.selectedUnit = null;
                    }
                }
            }
        }
        else {
            this.selectedUnit = null;
        }
        if (GameEngine.isPC() && !b) {
            this.selectedUnit = null;
            this.unitCommand = null;
        }
        return n2;
    }

    /* JADX INFO: renamed from: i */
    float getActionIconTopOffset() {
        return (float) (((double) Utility.clampTo255((this.gameEngine.screenHeight / 14.0f) / this.gameEngine.screenScale, 25.0f * this.gameEngine.screenScale, 40.0f * this.gameEngine.screenScale)) * 0.9d);
    }

    void getUnitStatusText(float f, int i) {
        boolean z = true;
        if (i == 0) {
            z = true;
        }
        if (GameUI.isModernUiLayoutStatic) {
            z = false;
        }
        if (this.gameUI.selectedUnitCount > 0) {
            if (this.gameUI.editorOrBuilder != null && this.gameUI.selectedUnitCount == 1 && this.gameUI.editorOrBuilder.isSelected) {
                z = false;
            }
            if (z) {
                if (this.gameUI.isButtonPressedWithRightClick((int) ((this.gameEngine.screenWidth - this.gameEngine.minimap.width) + 2.0f), this.gameEngine.minimap.getBottomY() + 2, (int) (this.gameEngine.minimap.width - 4.0f), (int) getActionIconTopOffset(), this.unselectAllText, IconGroup.unselectAllButton, false, Color.a(140, 100, 100, 100)) && !this.gameUI.isInputDisabled) {
                    this.gameUI.resetMouseState();
                    this.gameUI.clearCurrentAction();
                    this.gameUI.clearSelection();
                }
            }
            PlayerTeam playerTeam = null;
            boolean z2 = false;
            this.keyBindingActionMap.clear();
            BaseUnit baseUnit = null;
            BaseUnit[] baseUnitArrA = this.gameUI.selectedUnitsList.a();
            int size = this.gameUI.selectedUnitsList.size();
            for (int i2 = 0; i2 < size; i2++) {
                BaseUnit baseUnit2 = baseUnitArrA[i2];
                if (baseUnit2.isSelected) {
                    baseUnit = baseUnit2;
                    if (this.gameUI.canControlUnit(baseUnit2)) {
                        UnitType unitTypeR = baseUnit2.r();
                        Integer num = (Integer) this.keyBindingActionMap.get(unitTypeR);
                        if (num == null) {
                            this.keyBindingActionMap.put(unitTypeR, 1);
                        } else {
                            this.keyBindingActionMap.put(unitTypeR, Integer.valueOf(num.intValue() + 1));
                        }
                        z2 = true;
                    } else {
                        playerTeam = baseUnit2.team;
                    }
                }
            }
            boolean z3 = this.gameEngine.isGameStarted;
            if (playerTeam != null && this.gameEngine.playerTeam != null && playerTeam.b(this.gameEngine.playerTeam)) {
                z3 = true;
            }
            int iH = (int) getActionIconBaseSize();
            int i3 = iH + 2;
            int i4 = (int) (10.0f * this.gameEngine.screenScale);
            float bottomY = this.gameEngine.minimap.getBottomY() + iH + 30;
            float f2 = (this.gameEngine.screenWidth - this.gameEngine.sidebarWidth) + i4;
            float f3 = bottomY + 5.0f;
            if (baseUnit != null) {
                f3 = f3 + i3 + (i3 * i);
                if (this.gameUI.isUIInitialized) {
                    f3 -= (2 * i3) * 0.4f;
                }
            }
            this.zoomButtonRect.a((int) f2, (int) f3, (int) ((f2 + this.gameEngine.sidebarWidth) - (i4 * 2)), (int) (f3 + iH));
            boolean z4 = false;
            if (!GameUI.showModernTopBar) {
                if (i < 3 && !z2 && playerTeam != null) {
                    Paint paint = this.gameUI.unitRangeBorderPaint;
                    if (this.gameEngine.playerTeam.d(playerTeam)) {
                        paint = this.gameUI.unitPathPaint;
                    }
                    this.gameUI.drawMultilineText(getActionTooltipText(playerTeam), this.zoomButtonRect, paint, paint);
                    z4 = true;
                }
                if (this.gameUI.getSelectedUnitCount() == 1 && baseUnit != null) {
                    if (baseUnit.getAvailableActionCount() <= 3 || (playerTeam != null && !z3)) {
                        String strA = getUnitInfoText(baseUnit, false);
                        if (z4) {
                            strA = "\n" + ("\n" + ("\n" + strA));
                        }
                        Paint paint2 = this.paintUnitInfo;
                        paint2.a();
                        paint2.b(Color.a(50, 100, 100, 100));
                        this.gameUI.drawMultilineText(strA, this.zoomButtonRect, this.gameUI.unitPathBorderPaint, this.gameUI.unitPathBorderPaint);
                    }
                }
            }
        }
    }

    public String getActionTooltipText(PlayerTeam playerTeam) {
        String str = VariableScope.nullOrMissingString;
        boolean z = false;
        if (this.gameEngine.playerTeam.isSpectatorTeamColor()) {
            z = true;
        } else if (this.gameEngine.playerTeam.d(playerTeam)) {
            str = str + this.allyUnitText;
        } else if (this.gameEngine.playerTeam.c(playerTeam)) {
            str = str + this.enemyUnitText;
        } else {
            z = true;
        }
        if (z) {
            if (playerTeam == PlayerTeam.TEAM_ALL) {
                str = str + this.neutralUnitText;
            } else {
                str = str + "Team - " + playerTeam.getTeamSlotLabel();
            }
        }
        String str2 = str + "\n";
        if (playerTeam.teamName != null) {
            str2 = str2 + playerTeam.teamName;
        }
        if (!playerTeam.isTeamSpectator && this.gameEngine.isNetworkConnected() && playerTeam.isTeamDisconnected()) {
            str2 = (str2 + "\n") + "(disconnected)";
        }
        return str2;
    }

    public String getUnitInfoText(BaseUnit baseUnit, boolean z) {
        String str;
        String str2 = VariableScope.nullOrMissingString;
        if (z) {
            str2 = str2 + baseUnit.r().getUnitName() + "\n";
        }
        if (baseUnit.getResourceRate() > 0.0f) {
            str = str2 + UnitPrice.a(baseUnit.getBuildPrice(), baseUnit.currentHealth / baseUnit.maxHealth).a(true, true, 3, false);
        } else {
            str = str2 + ((int) Math.ceil(baseUnit.currentHealth)) + "/" + ((int) baseUnit.maxHealth) + "\n";
        }
        if (baseUnit.unitEnergyMax != 0.0f) {
            str = str + "(" + ((int) baseUnit.shield) + "/" + ((int) baseUnit.unitEnergyMax) + ")\n";
        }
        UnitPrice unitPriceDq = baseUnit.dq();
        StoredResources unitAIPathfindResult = baseUnit.getResourceGenerationRates();
        if (unitPriceDq != null) {
            unitAIPathfindResult = StoredResources.d(unitAIPathfindResult);
            unitAIPathfindResult.a(unitPriceDq);
        }
        if (!unitAIPathfindResult.c()) {
            for (StoredResourceEntry storedResourceEntry : unitAIPathfindResult.b) {
                if (storedResourceEntry.b != 0.0d && !storedResourceEntry.a.a()) {
                    str = str + storedResourceEntry.a.a(storedResourceEntry.b, true, false) + "\n";
                }
            }
        }
        return Utility.removeTrailingNewline(str);
    }

    private boolean drawUnitCountBadge(UnitBase unitBase) {
        if (!unitBase.isDead && unitBase.transportContainer == null) {
            float f = unitBase.posX;
            float f2 = unitBase.posY - unitBase.posZ;
            if (f2 <= 0.0f) {
                f2 += unitBase.posZ;
            }
            if (this.selectionRectF3.b(f, f2)) {
                if ((this.gameUI.canControlUnit(unitBase) || this.gameEngine.playerTeam.isSpectatorTeamColor()) && !unitBase.t()) {
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    /* JADX INFO: renamed from: j */
    void markUnitGroupMarkersValid() {
        Iterator it = this.unitGroupMarkers.iterator();
        while (it.hasNext()) {
            ((UnitGroupMarker) it.next()).isValid = true;
        }
    }

    /* JADX INFO: renamed from: k */
    void clearUnitGroupMarkers() {
        Iterator it = this.unitGroupMarkers.iterator();
        while (it.hasNext()) {
            ((UnitGroupMarker) it.next()).clearUnits();
        }
        this.messageText = null;
        this.messageTimer = 0.0f;
    }

    /* JADX INFO: renamed from: a */
    void drawUnitCountBadge(final int integer1, final int integer2, final int integer3, final String string4, final String string5, final Paint paint, final float float7) {
        final int integer4 = (int)(integer3 * 2.5);
        final int integer5 = (int)(40.0f * this.gameEngine.screenScale);
        final int n = integer1 + integer3 / 2;
        final int integer6 = (int)(integer2 - integer5 - 35.0f * this.gameEngine.screenScale);
        this.rect.a(n - integer4 / 2, integer6, integer4, integer5);
        this.gameUI.drawStyledButtonBackground(this.rect.a, this.rect.b, this.rect.c, this.rect.d, "", Color.a(180, 100, 100, 100), this.gameUI.buildingPreviewPaint, false, null, null);
        this.zoomButtonRect.a(this.rect.a, this.rect.b, this.rect.c, this.rect.d);
        final Rect zoomButtonRect = this.zoomButtonRect;
        zoomButtonRect.c *= (int)float7;
        this.gameEngine.renderGraphicsEngine.c(this.zoomButtonRect, paint);
        this.gameEngine.renderGraphicsEngine.a(string4, (float) n, integer6 + (this.gameUI.buildingPreviewPaint.k() + 5.0f) * 1.0f, this.gameUI.buildingPreviewPaint);
        this.gameEngine.renderGraphicsEngine.a(string5, (float) n, integer6 + (this.gameUI.buildingPreviewPaint.k() + 5.0f) * 2.0f, this.gameUI.buildingPreviewPaint);
    }

    /* JADX INFO: renamed from: a */
    void drawPauseOverlay(final float float1, final boolean boolean2) {
        float float2 = this.gameEngine.screenScale * 0.7f;
        if (GameEngine.isNonPCPlatform() && float2 < 0.7) {
            float2 = 0.7f;
        }
        int n = this.pauseTexture.m();
        int n2 = (int)(n * float2);
        int n3 = 4 + n2 / 2;
        int n4 = 4 + n2 / 2;
        if (this.gameEngine.consumeKeyPress(111)) {
            boolean clearCurrentAction = false;
            if (!this.gameUI.isDraggingSelection) {
                clearCurrentAction = this.gameUI.clearCurrentAction();
            }
            if (!clearCurrentAction) {
                this.gameUI.isDraggingSelection = !this.gameUI.isDraggingSelection;
            }
        }
        if (this.gameUI.isDraggingSelection) {
            this.infoTextTimer += 0.008f * float1;
            if (this.infoTextTimer > 1.0f) {
                this.infoTextTimer = 0.0f;
            }
            this.paintHealthBar.c(150 + (int)(100.0f * Utility.fastSin(this.infoTextTimer * 180.0f)));
        }
        else {
            this.infoTextTimer = 0.0f;
            this.paintHealthBar.c(80);
        }
        this.unitRect2.a(n3, n4, n3 + n2, n4 + n2);
        this.unitRect2.a(-(n2 / 2), -(n2 / 2));
        if (boolean2) {
            this.gameEngine.renderGraphicsEngine.a(this.pauseTexture, (float)this.unitRect2.a, (float)this.unitRect2.b, this.paintHealthBar, 0.0f, float2);
            if (this.gameEngine.settingsEngine.newRender) {
                this.minimapRect.a(this.unitRect2.d() - 4, this.unitRect2.e() - 4, this.unitRect2.d() + 4, this.unitRect2.e() + 4);
                this.paintMinimap.a(100, 0, 155, 0);
                this.gameEngine.renderGraphicsEngine.b(this.minimapRect, this.paintMinimap);
            }
        }
        if (GameEngine.isNonPCPlatform()) {
            Utility.grow(this.unitRect2, 4.0f);
        }
        if (this.gameUI.isSelectionBoxActive && !this.gameUI.isInputDisabled && this.unitRect2.b((int)this.gameUI.selectionBoxStartX, (int)this.gameUI.selectionBoxStartY)) {
            this.gameUI.isSelectionBoxActive = false;
            this.gameUI.isDraggingSelection = !this.gameUI.isDraggingSelection;
        }
        this.gameUI.forceModifiersInsideRect(this.unitRect2);
        if (this.gameEngine.replayEngine.j()) {
            this.paintHealthBar.c(80);
            if (this.gameEngine.replayEngine.v != 1) {
                this.paintHealthBar.c(200);
            }
            n = this.fastTexture.q;
            n2 = (int)(n * this.gameEngine.screenScale * 1.6f);
            n3 = (int)(this.gameEngine.currentScreenWidthPixels / 2.0f);
            n4 = 7 + (int)this.gameUI.unitRangePaint.k();
            this.gameEngine.renderGraphicsEngine.a(Utility.formatDuration(this.gameEngine.gameTimeMillis / 1000), (float)n3, (float)n4, this.gameUI.unitRangePaint);
            n4 += n2 / 2 + 10;
            n3 += n2 / 2 + 5;
            this.unitRect2.a(n3, n4, n3 + n2, n4 + n2);
            this.unitRect2.a(-this.unitRect2.b() / 2, -this.unitRect2.c() / 2);
            if (boolean2) {
                this.gameEngine.renderGraphicsEngine.a(this.fastTexture, (float)this.unitRect2.a, (float)this.unitRect2.b, this.paintHealthBar, 0.0f, (float)(n2 / n));
            }
            if (this.gameUI.isSelectionBoxActive && !this.gameUI.isInputDisabled && this.unitRect2.b((int)this.gameUI.selectionBoxStartX, (int)this.gameUI.selectionBoxStartY)) {
                this.gameUI.isSelectionBoxActive = false;
                this.gameEngine.replayEngine.b();
            }
            if (this.gameEngine.gameSpeed != 1.0f && boolean2) {
                this.gameEngine.renderGraphicsEngine.a("x" + this.gameEngine.gameSpeed, (float)(this.unitRect2.d() + n2 / 2), (float)this.unitRect2.e(), this.gameUI.buildingPreviewPaint);
            }
            final Texture replayPauseTexture = this.replayPauseTexture;
            n = replayPauseTexture.q;
            n2 = (int)(n * this.gameEngine.screenScale * 1.6f);
            n3 -= n2 + 5;
            this.unitRect2.a(n3, n4, n3 + n2, n4 + n2);
            this.unitRect2.a(-this.unitRect2.b() / 2, -this.unitRect2.c() / 2);
            if (boolean2) {
                this.gameEngine.renderGraphicsEngine.a(replayPauseTexture, (float)this.unitRect2.a, (float)this.unitRect2.b, this.paintHealthBar, 0.0f, (float)(n2 / n));
            }
            if (this.gameUI.isSelectionBoxActive && !this.gameUI.isInputDisabled && this.unitRect2.b((int)this.gameUI.selectionBoxStartX, (int)this.gameUI.selectionBoxStartY)) {
                this.gameUI.isSelectionBoxActive = false;
                this.gameEngine.replayEngine.a();
            }
            final Texture leaderboardTexture = this.leaderboardTexture;
            n3 = (int)(this.gameEngine.screenWidth - this.gameEngine.sidebarWidth - (n2 + 5));
            this.unitRect2.a(n3, n4, n3 + n2, n4 + n2);
            this.unitRect2.a(-this.unitRect2.b() / 2, -this.unitRect2.c() / 2);
            if (boolean2) {
                this.gameEngine.renderGraphicsEngine.a(leaderboardTexture, (float)this.unitRect2.a, (float)this.unitRect2.b, this.paintHealthBar, 0.0f, (float)(n2 / n));
            }
            if (this.gameUI.isSelectionBoxActive && !this.gameUI.isInputDisabled && this.unitRect2.b((int)this.gameUI.selectionBoxStartX, (int)this.gameUI.selectionBoxStartY)) {
                final TeamStats teamStats = this.gameEngine.teamStats;
                if (teamStats != null) {
                    teamStats.nextSort();
                }
            }
        }
        if (this.gameUI.isDraggingSelection) {
            this.gameEngine.isMenuOpen = false;
            final int screenPixels = this.gameEngine.toScreenPixels(190);
            this.zoomButtonRect.a = (int)(this.gameEngine.currentScreenWidthPixels / 2.0f - screenPixels / 2);
            this.zoomButtonRect.c = (int)(this.gameEngine.currentScreenWidthPixels / 2.0f + screenPixels / 2);
            final int screenPixels2 = this.gameEngine.toScreenPixels(34);
            final int n5 = screenPixels2 + this.gameEngine.toScreenPixels(15);
            final Menu buildGameMenu = this.getOpenMenuEntries();
            final int n6 = this.gameEngine.toScreenPixels(50) + n5 * (1 + buildGameMenu.size());
            this.zoomButtonRect.b = (int)(this.gameEngine.halfScreenHeight - n6 / 2);
            this.zoomButtonRect.d = (int)(this.gameEngine.halfScreenHeight + n6 / 2);
            if (boolean2) {
                this.gameUI.ninePatchStyle5.drawNormal(this.gameEngine.renderGraphicsEngine, this.zoomButtonRect);
            }
            final int n7 = this.zoomButtonRect.b + this.gameEngine.toScreenPixels(40);
            final int screenPixels3 = this.gameEngine.toScreenPixels(152);
            final int n8 = (int)(this.gameEngine.currentScreenWidthPixels / 2.0f - screenPixels3 / 2);
            int n9 = n7;
            final int a = Color.a(140, 100, 100, 100);
            if (this.gameUI.isButtonPressedStyled(n8, n9, screenPixels3, screenPixels2, Locale.get("menus.ingame.resume"), IconGroup.none, false, a, this.gameUI.buildingPreviewInvalidPaint, this.gameUI.ninePatchStyle3)) {
                this.gameUI.isSelectionBoxActive = false;
                this.gameUI.tooltipY = 40.0f;
                this.gameUI.isDraggingSelection = false;
            }
            n9 += n5;
            for (int i = 0; i < buildGameMenu.size(); ++i) {
                final MenuItem item = buildGameMenu.getItem(i);
                if (this.gameUI.isButtonPressedStyled(n8, n9, screenPixels3, screenPixels2, item.getTitle().toString(), IconGroup.none, false, a, this.gameUI.buildingPreviewInvalidPaint, this.gameUI.ninePatchStyle3)) {
                    this.a(item.getItemId());
                    this.gameUI.isSelectionBoxActive = false;
                    this.gameUI.tooltipY = 40.0f;
                }
                n9 += n5;
            }
            this.gameUI.forceModifiersInsideRect(this.zoomButtonRect);
        }
    }

    /* JADX INFO: renamed from: l */
    public void restartMission() {
        a(20);
    }

    /* JADX INFO: renamed from: m */
    public void quitToMenu() {
        a(21);
    }

    /* JADX INFO: renamed from: n */
    public void showChatDialog() {
        a(16);
    }

    void a(int i) {
        GameView gameView = this.gameEngine.activeGameView;
        if (gameView == null) {
            GameEngine.logColored("selectMenuOption: gameView==null");
            return;
        }
        InGameActivity surfaceHolder = gameView.getSurfaceHolder();
        if (surfaceHolder == null) {
            GameEngine.logColored("selectMenuOption: inGameActivity==null");
        } else {
            surfaceHolder.selectMenuOption(i);
        }
    }

    /* JADX INFO: renamed from: o */
    Menu getOpenMenuEntries() {
        this.vObject.clear();
        GameView gameView = this.gameEngine.activeGameView;
        if (gameView == null) {
            GameEngine.logColored("selectMenuOption: gameView==null");
            return this.vObject;
        }
        InGameActivity surfaceHolder = gameView.getSurfaceHolder();
        if (surfaceHolder == null) {
            GameEngine.logColored("selectMenuOption: inGameActivity==null");
            return this.vObject;
        }
        surfaceHolder.a(this.vObject);
        return this.vObject;
    }

    /* JADX INFO: renamed from: e */
    void updateUnitGroupMarkers(float f) {
        String str;
        int i = (int) (this.gameEngine.currentScreenHeightPixels - (30.0f * this.gameEngine.screenScale));
        int i2 = (int) ((this.gameEngine.screenWidth - this.gameEngine.sidebarWidth) + 10.0f);
        int i3 = ((int) (this.gameEngine.sidebarWidth - 20.0f)) / 3;
        int i4 = i3 - 5;
        for (int i5 = 0; i5 < this.unitGroupMarkers.size(); i5++) {
            UnitGroupMarker unitGroupMarker = (UnitGroupMarker) this.unitGroupMarkers.get(i5);
            if (unitGroupMarker.isValid) {
                unitGroupMarker.refreshUnitReferences();
                unitGroupMarker.isValid = false;
            }
            unitGroupMarker.removeDeadUnits();
            if (this.gameEngine.settingsEngine.keyboardSupport && i5 < this.gameEngine.inputController.ai.length) {
                if (this.gameEngine.inputController.ak[i5].a()) {
                    unitGroupMarker.clearUnits();
                    unitGroupMarker.addSelectedUnits();
                }
                if (this.gameEngine.inputController.aj[i5].a()) {
                    this.gameUI.clearCurrentAction();
                    unitGroupMarker.selectGroup();
                }
                if (this.gameEngine.inputController.ai[i5].a()) {
                    this.gameUI.clearCurrentAction();
                    this.gameUI.clearSelection();
                    unitGroupMarker.selectGroup();
                }
            }
            if (this.gameEngine.settingsEngine.showUnitGroups && i5 < 3) {
                if (unitGroupMarker.units.size() == 0) {
                    if (this.gameUI.isUILoggingEnabled) {
                        str = "Empty";
                    } else {
                        str = "(" + (i5 + 1) + ")";
                    }
                } else {
                    str = VariableScope.nullOrMissingString + unitGroupMarker.units.size();
                }
                boolean z = false;
                unitGroupMarker.posX = Utility.moveTowardsZero(unitGroupMarker.posX, 0.01f * f);
                unitGroupMarker.posY = Utility.moveTowardsZero(unitGroupMarker.posY, 0.01f * f);
                unitGroupMarker.drawScale = Utility.moveTowardsZero(unitGroupMarker.drawScale, 0.01f * f);
                if (this.gameUI.isButtonPressed(i2, i, i4, (int) (31.0f * this.gameEngine.screenScale), str, IconGroup.none, true, Color.a(50, (int) (100.0f + (unitGroupMarker.drawScale * 100.0f)), (int) (100.0f + (unitGroupMarker.posY * 100.0f)), (int) (100.0f + (unitGroupMarker.posX * 100.0f)))) && this.gameUI.currentAction == null && !this.gameUI.isInputDisabled) {
                    z = true;
                    unitGroupMarker.radius += f;
                    this.gameUI.resetMouseState();
                    float f2 = 1.0f;
                    this.paintUnitInfo.a();
                    this.paintUnitInfo.b(Color.a(120, 200, 0, 0));
                    if (unitGroupMarker.radius < 50.0f) {
                        f2 = unitGroupMarker.radius / 50.0f;
                        this.paintUnitInfo.b(Color.a((int) (150.0f + (f2 * 40.0f)), 0, 200, 0));
                        drawUnitCountBadge(i2, i, i4, "Select Group", "(Hold for more..)", this.paintUnitInfo, f2);
                    } else if (unitGroupMarker.radius < 100.0f) {
                        f2 = (unitGroupMarker.radius - 50.0f) / 50.0f;
                        this.paintUnitInfo.b(Color.a((int) (150.0f + (f2 * 40.0f)), 200, 0, 0));
                        drawUnitCountBadge(i2, i, i4, "Add to Group", "(Hold for more..)", this.paintUnitInfo, f2);
                    } else {
                        drawUnitCountBadge(i2, i, i4, "Replace Group", VariableScope.nullOrMissingString, this.paintUnitInfo, 0.0f);
                    }
                    int i6 = (int) (31.0f * this.gameEngine.screenScale);
                    this.zoomButtonRect.a(i2, (int) ((i + i6) - (i6 * f2)), i2 + i4, i + i6);
                    this.gameEngine.renderGraphicsEngine.b(this.zoomButtonRect, this.paintUnitInfo);
                }
                if (!z) {
                    if (unitGroupMarker.radius != 0.0f && !this.gameUI.isMousePressed) {
                        if (unitGroupMarker.radius > 100.0f) {
                            unitGroupMarker.clearUnits();
                            unitGroupMarker.addSelectedUnits();
                            unitGroupMarker.drawScale = 1.0f;
                        } else if (unitGroupMarker.radius > 50.0f) {
                            unitGroupMarker.addSelectedUnits();
                            this.gameUI.clearCurrentAction();
                            this.gameUI.clearSelection();
                            unitGroupMarker.selectGroup();
                            unitGroupMarker.posY = 1.0f;
                        } else if (unitGroupMarker.units.size() != 0) {
                            this.gameUI.clearCurrentAction();
                            this.gameUI.clearSelection();
                            unitGroupMarker.selectGroup();
                            unitGroupMarker.posX = 1.0f;
                        } else {
                            unitGroupMarker.clearUnits();
                            unitGroupMarker.addSelectedUnits();
                            unitGroupMarker.posY = 1.0f;
                        }
                    }
                    if (!z) {
                        unitGroupMarker.radius = 0.0f;
                    }
                }
                i2 += i3;
            }
        }
    }

    @Override // com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeInt(this.unitGroupMarkers.size());
        Iterator it = this.unitGroupMarkers.iterator();
        while (it.hasNext()) {
            ((UnitGroupMarker) it.next()).a(gameOutputStream);
        }
        gameOutputStream.writeByte(0);
    }

    public void a(GameInputStream gameInputStream, boolean z) throws IOException {
        if (!z) {
            this.unitGroupMarkers.clear();
        }
        int i = gameInputStream.readInt();
        int i2 = 0;
        while (i2 < i) {
            UnitGroupMarker unitGroupMarker = new UnitGroupMarker(this, i2 < 3);
            unitGroupMarker.a(gameInputStream);
            if (!z) {
                this.unitGroupMarkers.add(unitGroupMarker);
            }
            i2++;
        }
        gameInputStream.readByte();
    }
}
