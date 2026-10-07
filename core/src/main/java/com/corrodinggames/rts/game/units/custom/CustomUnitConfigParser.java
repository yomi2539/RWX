package com.corrodinggames.rts.game.units.custom;

import com.corrodinggames.rts.game.ColorMode;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.units.*;
import com.corrodinggames.rts.game.units.actions.*;
import com.corrodinggames.rts.game.units.actions.ActionType;
import com.corrodinggames.rts.game.units.air.AirUnit;
import com.corrodinggames.rts.game.units.buildings.BaseBuilding;
import com.corrodinggames.rts.game.units.custom.condition.ResourceDefinition;
import com.corrodinggames.rts.game.units.custom.condition.StoredResourceEntry;
import com.corrodinggames.rts.game.units.custom.condition.StoredResources;
import com.corrodinggames.rts.game.units.custom.condition.resources.Resource;
import com.corrodinggames.rts.game.units.custom.hooks.*;
import com.corrodinggames.rts.game.units.custom.logic.*;
import com.corrodinggames.rts.game.units.custom.logic.actions.*;
import com.corrodinggames.rts.game.units.custom.logicBooleans.LogicBoolean;
import com.corrodinggames.rts.game.units.custom.logicBooleans.UnitReference;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.game.units.custom.price.UnitPrice;
import com.corrodinggames.rts.game.units.custom.variables.VariableSubstitutionParser;
import com.corrodinggames.rts.game.units.land.BuilderUnit;
import com.corrodinggames.rts.game.units.land.ExperimentalHoverTank;
import com.corrodinggames.rts.game.units.land.HoverLandUnit;
import com.corrodinggames.rts.game.units.land.LandUnit;
import com.corrodinggames.rts.game.units.sea.WaterUnit;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.PerformanceProfiler;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.audio.Sound;
import com.corrodinggames.rts.gameFramework.file.FileHelper;
import com.corrodinggames.rts.gameFramework.graphics.TeamColorTexture;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.mod.ModInfo;
import com.corrodinggames.rts.gameFramework.ui.GameUI;
import com.corrodinggames.rts.gameFramework.utility.*;
import io.github.rwx.geometry.PointF;
import io.github.rwx.geometry.Rect;
import io.github.rwx.mod.asset.JvmModAssetBridge;
import io.github.rwx.render.canvas.ArgbColor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.custom.ag */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/custom/ag.class */
public class CustomUnitConfigParser {

    /* JADX INFO: renamed from: b */
    static int totalUnitFilesLoaded;

    /* JADX INFO: renamed from: c */
    static int totalModUnitFilesLoaded;

    /* JADX INFO: renamed from: d */
    public static int unitsRegeneratedCount;

    /* JADX INFO: renamed from: e */
    static ModInfo currentModInfo;

    /* JADX INFO: renamed from: f */
    static boolean isLoadingCoreUnit;

    /* JADX INFO: renamed from: i */
    static int imageCacheMissCount;

    /* JADX INFO: renamed from: j */
    static int imageCacheHitCount;

    /* JADX INFO: renamed from: k */
    static boolean logImageCacheMiss;

    /* JADX INFO: renamed from: l */
    static int oomImageErrorCount;

    /* JADX INFO: renamed from: a */
    static boolean debugLogging = false;

    /* JADX INFO: renamed from: g */
    public static HashMap<String, Texture> imageCache = new HashMap();

    /* JADX INFO: renamed from: h */
    public static HashMap soundCache = new HashMap();

    /* JADX INFO: renamed from: m */
    public static FastArrayList tempUnitList = new FastArrayList();

    /* JADX INFO: renamed from: n */
    static HashMap iniCache = new HashMap();

    /* JADX INFO: renamed from: o */
    static final Object unitConfigLock = new Object();

    /* JADX INFO: renamed from: p */
    public static float maxUnitRadius = 50.0f;

    /* JADX INFO: renamed from: q */
    public static float maxBuildingRadius = 50.0f;

    /* JADX INFO: renamed from: r */
    static ModInfo lastModUsedForInit = null;

    /* JADX INFO: renamed from: s */
    static String lastErrorMessage = null;

    /* JADX INFO: renamed from: a */
    public static void addImageMemory(int i) {
        if (currentModInfo != null) {
            currentModInfo.totalImageMemory += (long) i;
        }
    }

    /* JADX INFO: renamed from: a */
    public static void logAndResetTimingStats() {
        logTimingStats();
        resetTimingStats();
    }

    /* JADX INFO: renamed from: a */
    public static void trackImageMemory(Texture texture) {
        if (texture != null && !texture.v) {
            if (texture instanceof TeamColorTexture) {
                return;
            }
            texture.v = true;
            addImageMemory(texture.u());
        }
    }

    /* JADX INFO: renamed from: a */
    public static void trackImageMemoryForTextures(Texture[] textureArr) {
        if (textureArr != null) {
            Texture texture = null;
            for (Texture texture2 : textureArr) {
                if (texture2 != texture) {
                    trackImageMemory(texture2);
                }
                if (texture == null) {
                    texture = texture2;
                }
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public static void trackSoundMemory(Sound sound) {
        if (!sound.g) {
            sound.g = true;
            if (currentModInfo != null) {
                currentModInfo.totalSoundMemory += (long) sound.a();
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public static boolean reloadCustomUnitList(FastArrayList fastArrayList) {
        GameEngine gameEngine = GameEngine.getInstance();
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        ArrayList arrayList = new ArrayList(CustomUnitConfig.allConfigs);
        ArrayList arrayList2 = new ArrayList(CustomUnitConfig.activeConfigs);
        FastArrayList fastArrayList2 = new FastArrayList();
        String str = null;
        Iterator it = fastArrayList.iterator();
        while (it.hasNext()) {
            CustomUnitConfig customUnitConfig = (CustomUnitConfig) it.next();
            CustomUnitConfig pathForModError = reloadUnitConfigForModChange(customUnitConfig);
            if (pathForModError == null) {
                GameEngine.log("Failed to apply changes to unit type: " + customUnitConfig.name);
                z = true;
                if (str == null && lastErrorMessage != null) {
                    str = lastErrorMessage;
                }
            } else {
                GameEngine.log("Changes applied to unit type: " + customUnitConfig.name);
                z2 = true;
                fastArrayList2.add(pathForModError);
            }
        }
        if (str != null && GameEngine.isAndroidPlatform()) {
            gameEngine.showMessageBox("Unit errors", str);
        }
        if (z2 && !validateCustomUnitSet(false)) {
            z = true;
        }
        if (z2 && !z) {
            CustomUnitConfig.validUnitsForSync = null;
            rebuildUnitTypeListsAsync();
            lastErrorMessage = null;
            PlayerTeam.markTeamStatsDirtyFromMetadataChange();
            GameUI.notifySelectionChanged();
            z3 = true;
            if (!z) {
                Iterator it2 = fastArrayList2.iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    CustomUnitConfig customUnitConfig2 = (CustomUnitConfig) it2.next();
                    if (!customUnitConfig2.warnings.isEmpty()) {
                        gameEngine.alert(customUnitConfig2.warnings.size() + " Warning(s) loading: " + customUnitConfig2.getConfigDisplayPath() + " \n" + ((String) customUnitConfig2.warnings.get(0)), 1);
                        customUnitConfig2.warnings.clear();
                        z3 = false;
                        break;
                    }
                }
            }
        }
        if (z) {
            GameEngine.log("Failed to load some units, keeping old config");
            synchronized (CustomUnitConfig.allConfigs) {
                CustomUnitConfig.allConfigs.clear();
                CustomUnitConfig.allConfigs.addAll(arrayList);
            }
            CustomUnitConfig.activeConfigs = arrayList2;
        }
        return z3;
    }

    /* JADX INFO: renamed from: b */
    public static void reloadAllActiveCustomUnits() {
        FastArrayList fastArrayList = new FastArrayList();
        totalUnitFilesLoaded = 0;
        totalModUnitFilesLoaded = 0;
        unitsRegeneratedCount = 0;
        for (Object o : BaseUnit.getGlobalUnitList()) {
            UnitType unitTypeR = ((BaseUnit) o).r();
            if ((unitTypeR instanceof CustomUnitConfig) && !fastArrayList.contains(unitTypeR)) {
                fastArrayList.add(unitTypeR);
            }
        }
        if (!fastArrayList.isEmpty()) {
            reloadCustomUnitList(fastArrayList);
        }
    }

    /* JADX INFO: renamed from: c */
    public static void reloadChangedUnitConfigs() {
        boolean z = false;
        FastArrayList fastArrayList = new FastArrayList();
        for (CustomUnitConfig customUnitConfig : CustomUnitConfig.allConfigs) {
            boolean z2 = false;
            for (FileWatcher fileWatcher : customUnitConfig.fileWatchers) {
                long jA = fileWatcher.a(false);
                if (jA != fileWatcher.a) {
                    z2 = true;
                    fileWatcher.a = jA;
                }
            }
            if (z2) {
                if (!z) {
                    GameEngine.log("Detected unit changes");
                    z = true;
                }
                fastArrayList.add(customUnitConfig);
            }
        }
        if (!fastArrayList.isEmpty()) {
            reloadCustomUnitList(fastArrayList);
        }
    }

    /* JADX INFO: renamed from: d */
    public static void applyPendingNetworkUnits() {
        if (CustomUnitConfig.validUnitsForSync != null) {
            GameEngine.log("applyPendingNetworkUnits: Applying new network units from server (" + CustomUnitConfig.validUnitsForSync.size() + " units)");
            CustomUnitConfig.activeConfigs = CustomUnitConfig.validUnitsForSync;
            CustomUnitConfig.validUnitsForSync = null;
            rebuildUnitTypeListsAsync();
            return;
        }
        GameEngine.log("applyPendingNetworkUnits: no server units list found");
    }

    /* JADX INFO: renamed from: a */
    public static ArrayList getActiveCustomUnits(boolean z) {
        ArrayList arrayList = new ArrayList();
        synchronized (CustomUnitConfig.allConfigs) {
            for (CustomUnitConfig customUnitConfig : CustomUnitConfig.allConfigs) {
                if (customUnitConfig.modInfo == null || (customUnitConfig.modInfo.isEnabled() && z)) {
                    arrayList.add(customUnitConfig);
                }
            }
        }
        return arrayList;
    }

    /* JADX INFO: renamed from: a */
    public static IniFile getEditableCustomUnits(String str) throws ConfigParseException {
        synchronized (iniCache) {
            IniFile iniFile = (IniFile) iniCache.get(str);
            if (iniFile != null) {
                return iniFile;
            }
            AssetInputStream assetInputStreamLoadTextureInternal = openUnitConfigFile(str);
            if (assetInputStreamLoadTextureInternal == null) {
                return null;
            }
            try {
                IniFile iniFile2 = new IniFile(new BufferedInputStream(assetInputStreamLoadTextureInternal), str);
                iniFile2.enableStrict();
                iniFile2.path = assetInputStreamLoadTextureInternal.getPath();
                iniCache.put(str, iniFile2);
                return iniFile2;
            } catch (IOException e) {
                e.printStackTrace();
                throw new ConfigParseException("Load of '" + str + "' failed: " + e.getMessage());
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public static void mergeAndApplyUnitChanges(CustomUnitConfig customUnitConfig, IniFile iniFile, String str, String str2, boolean z) throws ConfigParseException {
        IniFile editableCustomUnits = getEditableCustomUnits(str);
        if (editableCustomUnits == null) {
            if (z) {
            } else {
                throw new ConfigParseException("[" + str2 + "] Could not find conf target:" + str);
            }
        } else {
            customUnitConfig.registerConfigWatcher(editableCustomUnits.path);
            iniFile.merge(editableCustomUnits);
            reloadSingleUnitConfig(customUnitConfig, iniFile, editableCustomUnits, str, 1);
        }
    }

    /* JADX INFO: renamed from: a */
    public static void reloadSingleUnitConfig(CustomUnitConfig customUnitConfig, IniFile iniFile, IniFile iniFile2, String str, int i) throws ConfigParseException {
        String strTrackImageMemory;
        String str2;
        if (i > 10) {
            throw new ConfigParseException("copyFrom can only be 10 levels deep, maybe you have a loop?");
        }
        String string = iniFile2.getString("core", "copyFrom", (String) null);
        if (string != null) {
            String[] strArrSplit = string.split(",");
            Collections.reverse(Arrays.asList(strArrSplit));
            for (String str3 : strArrSplit) {
                String strTrim = str3.trim();
                if (!strTrim.equals(VariableScope.nullOrMissingString)) {
                    if (strTrim.contains("..")) {
                        throw new ConfigParseException("'..' not supported in copyFrom");
                    }
                    if (strTrim.startsWith("ROOT:")) {
                        String strSubstring = strTrim.substring("ROOT:".length());
                        if (customUnitConfig.modInfo == null) {
                            str2 = "units/common.ini";
                        } else {
                            str2 = customUnitConfig.modInfo.sourceFolder + "/common.ini";
                        }
                        strTrackImageMemory = joinAssetPath(Utility.getParentPath(str2), strSubstring);
                    } else if (strTrim.startsWith("CORE:")) {
                        strTrackImageMemory = joinAssetPath(Utility.getParentPath("units/common.ini"), strTrim.substring("CORE:".length()));
                    } else {
                        strTrackImageMemory = joinAssetPath(Utility.getParentPath(str), strTrim);
                    }
                    IniFile editableCustomUnits = getEditableCustomUnits(strTrackImageMemory);
                    if (editableCustomUnits == null) {
                        String str4 = "Could not find copyFrom target:" + strTrackImageMemory;
                        if (i != 0) {
                            str4 = str4 + " (while loading: " + str + ")";
                        }
                        throw new ConfigParseException(str4);
                    }
                    customUnitConfig.registerConfigWatcher(editableCustomUnits.path);
                    iniFile.merge(editableCustomUnits);
                    reloadSingleUnitConfig(customUnitConfig, iniFile, editableCustomUnits, strTrackImageMemory, i + 1);
                }
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public static void applyCopyFromSection(CustomUnitConfig customUnitConfig, IniFile iniFile, String str, String str2, int i) throws ConfigParseException {
        if (i > 10) {
            throw new ConfigParseException("@copyFromSection can only be 10 levels deep, maybe you have a loop?");
        }
        String string = iniFile.getString(str2, "@copyFromSection", (String) null);
        if (string == null || string.equals(VariableScope.nullOrMissingString)) {
            return;
        }
        String[] strArrSplit = string.split(",");
        Collections.reverse(Arrays.asList(strArrSplit));
        for (String str3 : strArrSplit) {
            String strTrim = str3.trim();
            if (!strTrim.equals(VariableScope.nullOrMissingString)) {
                FastArrayList<String> keysStartingWith = iniFile.getKeysStartingWith(strTrim, VariableScope.nullOrMissingString);
                if (keysStartingWith.size() == 0) {
                    throw new ConfigParseException("[" + str2 + "]@copyFromSection: Could not find keys in target section: " + strTrim);
                }
                for (String str4 : keysStartingWith) {
                    String value = iniFile.getValue(strTrim, str4);
                    if (value != null) {
                        iniFile.setValueIfMissing(str, str4, value);
                    }
                }
                applyCopyFromSection(customUnitConfig, iniFile, str, strTrim, i + 1);
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public static LocaleString getLocaleString(IniFile iniFile, String str, String str2, String str3) {
        return iniFile.getLocaleString(str, str2, str3, false);
    }

    /* JADX INFO: renamed from: a */
    public static LocalizedText getUnitReference(CustomUnitConfig customUnitConfig, IniFile iniFile, String str, String str2, String str3) throws ConfigParseException {
        return iniFile.getUnitReference(customUnitConfig, str, str2, str3);
    }

    /* JADX INFO: renamed from: a */
    public static CustomUnitConfig reloadUnitConfigForModChange(CustomUnitConfig customUnitConfig) {
        String str = customUnitConfig.configPath;
        GameEngine gameEngine = GameEngine.getInstance();
        CustomUnitConfig unitReference = null;
        String str2 = null;
        if (customUnitConfig.modInfo != null) {
            str2 = customUnitConfig.modInfo.firstError;
        }
        synchronized (iniCache) {
            iniCache.clear();
        }
        lastErrorMessage = null;
        try {
            unitReference = loadUnitConfigFile(str, customUnitConfig.modInfo, customUnitConfig.onNewMapSpawn_ifUnitIsMissing, customUnitConfig.onNewMapSpawn_ifUnitIsPresent);
        } catch (RuntimeException e) {
            e.printStackTrace();
            if (lastErrorMessage == null) {
                gameEngine.alert("Error loading unit:" + getModRelativePath(customUnitConfig.modInfo, str, true) + "\n" + e.getMessage(), 1);
            }
        }
        if (unitReference == null && customUnitConfig.modInfo != null) {
            customUnitConfig.modInfo.firstError = str2;
        }
        if (unitReference != null) {
            synchronized (CustomUnitConfig.allConfigs) {
                CustomUnitConfig.allConfigs.remove(customUnitConfig);
            }
            refreshCustomUnitsForConfig((UnitType) customUnitConfig, unitReference, true);
            if (CustomUnitConfig.activeConfigs.remove(customUnitConfig)) {
                CustomUnitConfig.activeConfigs.add(unitReference);
                if (customUnitConfig.configHash != unitReference.configHash) {
                    unitsRegeneratedCount++;
                }
            } else {
                GameEngine.log("Changed unit was not enabled (original not found in customUnitTypes)");
            }
            PlayerTeam.markTeamStatsDirtyFromMetadataChange();
            GameUI.notifySelectionChanged();
        }
        return unitReference;
    }

    /* JADX INFO: renamed from: a */
    public static void refreshCustomUnitsForConfig(UnitType unitType, CustomUnitConfig customUnitConfig, boolean z) {
        for (BaseUnit baseUnit : BaseUnit.getGlobalUnitList()) {
            if (baseUnit instanceof CustomUnit) {
                CustomUnit customUnit = (CustomUnit) baseUnit;
                if (customUnit.unitConfig == unitType) {
                    PlayerTeam.b((BaseUnit) customUnit);
                    customUnit.a(customUnitConfig, false, z);
                    customUnit.S();
                    if (customUnit.getTrackingManager() != null) {
                        customUnit.getTrackingManager().a(customUnitConfig);
                    }
                    PlayerTeam.c(customUnit);
                }
                if (customUnit.factoryUnitConfig == unitType) {
                    customUnit.factoryUnitConfig = customUnitConfig;
                }
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public static String describeUnitCountsByMod(ArrayList arrayList) {
        int iValueOf;
        HashMap<ModInfo, Integer> map = new HashMap();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            ModInfo modInfo = ((CustomUnitConfig) it.next()).modInfo;
            if (modInfo != null) {
                Integer num = (Integer) map.get(modInfo);
                if (num == null) {
                    iValueOf = 1;
                } else {
                    iValueOf = Integer.valueOf(num.intValue() + 1);
                }
                map.put(modInfo, iValueOf);
            }
        }
        String str = VariableScope.nullOrMissingString;
        for (ModInfo modInfo2 : map.keySet()) {
            str = str + modInfo2.getDisplayTitle() + "(unitCount: " + ((Integer) map.get(modInfo2)) + (modInfo2.isEnabled() ? VariableScope.nullOrMissingString : "[disabled]") + "), ";
        }
        return str;
    }

    /* JADX INFO: renamed from: b */
    public static String enableAllCustomUnits(boolean z) {
        ArrayList activeCustomUnits = getActiveCustomUnits(z);
        CustomUnitConfig.validUnitsForSync = null;
        CustomUnitConfig.activeConfigs = activeCustomUnits;
        lastErrorMessage = null;
        GameEngine.log("enableAll: " + describeUnitCountsByMod(CustomUnitConfig.activeConfigs));
        rebuildUnitTypeListsAsync();
        GameEngine.getInstance().modManager.verifyJvmModApiDeclarations();
        return lastErrorMessage;
    }

    /* JADX INFO: renamed from: c */
    public static boolean validateCustomUnitSet(boolean z) {
        ArrayList activeCustomUnits;
        ArrayList arrayList = CustomUnitConfig.activeConfigs;
        if (z) {
            activeCustomUnits = getActiveCustomUnits(true);
        } else {
            activeCustomUnits = CustomUnitConfig.activeConfigs;
        }
        boolean z2 = true;
        lastErrorMessage = null;
        CustomUnitConfig.activeConfigs = activeCustomUnits;
        rebuildOverridesAndTriggersAsync();
        if (lastErrorMessage != null) {
            z2 = false;
        }
        CustomUnitConfig.activeConfigs = arrayList;
        rebuildOverridesAndTriggersAsync();
        return z2;
    }

    /* JADX INFO: renamed from: e */
    public static void rebuildUnitTypeListsAsync() {
        synchronized (unitConfigLock) {
            rebuildUnitTypeRegistry();
        }
    }

    /* JADX INFO: renamed from: n */
    private static void rebuildUnitTypeRegistry() {
        CustomUnitConfig customUnitConfig = null;
        ArrayList arrayList = new ArrayList();
        if (GameEngine.getInstance().usesCoreUnitTypes()) {
            for (UnitTypeEnum unitTypeEnum : UnitTypeEnum.values()) {
                arrayList.add(unitTypeEnum);
            }
        }
        for (CustomUnitConfig customUnitConfig2 : CustomUnitConfig.activeConfigs) {
            arrayList.add(customUnitConfig2);
            if (customUnitConfig2.name.equals("missing") && customUnitConfig2.modInfo == null) {
                customUnitConfig = customUnitConfig2;
            }
        }
        UnitTypeEnum.ae = arrayList;
        BaseUnit.rebuildUnitTypePrototypeCaches();
        rebuildOverridesAndTriggersAsync();
        updateMaxRadii();
        Resource.e();
        if (customUnitConfig == null) {
            GameEngine.log("missingPlaceHolder is not an active unit, searching for new target");
            for (CustomUnitConfig customUnitConfig3 : CustomUnitConfig.activeConfigs) {
                if (customUnitConfig3.name.equals("missing")) {
                    GameEngine.log("Found a missing placeholder");
                    customUnitConfig = customUnitConfig3;
                }
            }
        }
        CustomUnitConfig.instance = customUnitConfig;
    }

    public static void updateMaxRadii() {
        float f = 50.0f;
        float f2 = 50.0f;
        for (CustomUnitConfig customUnitConfig : CustomUnitConfig.activeConfigs) {
            float f3 = customUnitConfig.radius;
            if (f3 > 250.0f) {
                f3 = 250.0f;
            }
            if (f < f3) {
                f = f3;
            }
            if (customUnitConfig.isBuildingUnit && f2 < f3) {
                f2 = f3;
            }
        }
        maxUnitRadius = f;
        maxBuildingRadius = f2;
    }

    /* JADX INFO: renamed from: b */
    public static AssetInputStream openUnitConfigFile(String str) {
        return FileHelper.openFileByPath(VariableScope.nullOrMissingString + str);
    }

    /* JADX INFO: renamed from: b */
    public static void sortUnitActions(ArrayList arrayList) {
        Collections.sort(arrayList);
    }

    /* JADX INFO: renamed from: a */
    public static void applyCopyFromChain(UnitType unitType) {
        AbstractUnitAction customAction;
        AbstractUnitAction placeBuildingAction;
        GameEngine gameEngine = GameEngine.getInstance();
        try {
            unitType.h();
            if (unitType instanceof CustomUnitConfig) {
                CustomUnitConfig customUnitConfig = (CustomUnitConfig) unitType;
                if (customUnitConfig.aiUpgradedFrom != null) {
                    UnitType unitTypeByName = CustomUnitConfig.getUnitTypeByName(customUnitConfig.aiUpgradedFrom);
                    if (unitTypeByName == null) {
                        throw new ConfigParseException("Could not find [ai]upgradedFrom target:" + customUnitConfig.aiUpgradedFrom);
                    }
                    customUnitConfig.b(unitTypeByName);
                }
                Iterator it = customUnitConfig.unitTypeReferences.iterator();
                while (it.hasNext()) {
                    ((UnitTypeReference) it.next()).a();
                }
                if (customUnitConfig.isPickableStartingUnit) {
                    CustomUnitConfig.configsById.add(customUnitConfig);
                }
            }
            for (CustomUnitConfig customUnitConfig2 : CustomUnitConfig.activeConfigs) {
                if (unitType instanceof CustomUnitConfig) {
                    CustomUnitConfig customUnitConfig3 = (CustomUnitConfig) unitType;
                    if (customUnitConfig2.aiUpgradedFrom != null && customUnitConfig2.aiUpgradedFrom.equalsIgnoreCase(customUnitConfig3.getUnitTypeDescriptionShort())) {
                        customUnitConfig3.b(customUnitConfig2);
                    }
                }
                for (CustomUnitTrigger customUnitTrigger : customUnitConfig2.customUnitTriggers) {
                    if (customUnitTrigger.triggerName.equalsIgnoreCase(unitType.getUnitTypeDescriptionShort())) {
                        customUnitTrigger.runOnce = true;
                        boolean z = false;
                        for (int i = customUnitConfig2.techLevel; i <= 3; i++) {
                            ArrayList arrayListA = unitType.a(i);
                            if (customUnitConfig2.isBuildingUnit || customUnitTrigger.enabled) {
                                placeBuildingAction = new PlaceBuildingAction(customUnitConfig2);
                            } else {
                                placeBuildingAction = new QueueUnitAction(customUnitConfig2);
                            }
                            if (customUnitTrigger.delay != -999.0f) {
                                placeBuildingAction.sortOrder = customUnitTrigger.delay;
                            }
                            if (customUnitTrigger.logicCondition != null) {
                                boolean z2 = false;
                                if (!(unitType instanceof CustomUnitConfig) && !(BaseUnit.findTurretPosition(unitType) instanceof OrderableUnit)) {
                                    z2 = true;
                                }
                                if (!z2) {
                                    placeBuildingAction.unitAction = ConfigurableCustomAction.a(customUnitTrigger);
                                } else if (!z) {
                                    z = true;
                                    customUnitConfig2.logWarningToMod("builtFrom isLocked currently cannot be used when targeting old-style unit:" + unitType.getUnitTypeDescriptionShort());
                                }
                            }
                            boolean z3 = false;
                            Iterator it2 = arrayListA.iterator();
                            while (it2.hasNext()) {
                                if (placeBuildingAction.equals((AbstractUnitAction) it2.next())) {
                                    z3 = true;
                                }
                            }
                            if (!z3) {
                                arrayListA.add(placeBuildingAction);
                            }
                            sortUnitActions(arrayListA);
                        }
                    }
                }
            }
            if (unitType instanceof CustomUnitConfig) {
                CustomUnitConfig customUnitConfig4 = (CustomUnitConfig) unitType;
                for (CustomActionDef customActionDef : customUnitConfig4.customActionDefs) {
                    if (customActionDef.stringId != null && customActionDef.stringId.equalsIgnoreCase("setRally")) {
                        for (int i2 = 1; i2 <= 3; i2++) {
                            ArrayList arrayListA2 = unitType.a(i2);
                            SetRallyAction setRallyAction = new SetRallyAction();
                            if (customActionDef.pos != -999.0f) {
                                setRallyAction.sortOrder = customActionDef.pos;
                            }
                            arrayListA2.add(setRallyAction);
                            customUnitConfig4.hasSetRallyAction = true;
                            sortUnitActions(arrayListA2);
                        }
                    } else if (customActionDef.stringId != null && customActionDef.stringId.equalsIgnoreCase("reclaim")) {
                        for (int i3 = 1; i3 <= 3; i3++) {
                            ArrayList arrayListA3 = unitType.a(i3);
                            ReclaimTargetAction reclaimTargetAction = new ReclaimTargetAction(true);
                            if (customActionDef.pos != -999.0f) {
                                reclaimTargetAction.sortOrder = customActionDef.pos;
                            }
                            arrayListA3.add(reclaimTargetAction);
                            sortUnitActions(arrayListA3);
                        }
                    } else if (customActionDef.stringId != null && customActionDef.stringId.equalsIgnoreCase("repair")) {
                        for (int i4 = 1; i4 <= 3; i4++) {
                            ArrayList arrayListA4 = unitType.a(i4);
                            RepairTargetAction repairTargetAction = new RepairTargetAction();
                            if (customActionDef.pos != -999.0f) {
                                repairTargetAction.sortOrder = customActionDef.pos;
                            }
                            arrayListA4.add(repairTargetAction);
                            sortUnitActions(arrayListA4);
                        }
                    } else {
                        UnitType unitTypeByName2 = null;
                        if (customActionDef.stringId != null) {
                            unitTypeByName2 = UnitTypeEnum.getUnitTypeByName(customActionDef.stringId);
                            if (unitTypeByName2 == null) {
                                throw new ConfigParseException("Could not find canBuild target:" + customActionDef.stringId);
                            }
                        } else if (customActionDef.actionType != BuildType.convert) {
                            throw new ConfigParseException("'Target' required for action:" + customActionDef.a());
                        }
                        for (int i5 = 1; i5 <= 3; i5++) {
                            ArrayList arrayListA5 = unitType.a(i5);
                            if (customActionDef.actionType == BuildType.build) {
                                if (unitTypeByName2.isBuildingUnit() || customActionDef.forceNano) {
                                    customAction = new PlaceBuildingAction(unitTypeByName2, customActionDef.techLevel, null);
                                    customAction.unitAction = ConfigurableCustomAction.a(customActionDef);
                                } else {
                                    customAction = new QueueUnitAction(unitTypeByName2);
                                    customAction.unitAction = ConfigurableCustomAction.a(customActionDef);
                                }
                            } else if (customActionDef.actionType == BuildType.convert) {
                                customAction = new CustomAction(customActionDef, CustomUnitConfig.a(unitTypeByName2));
                            } else {
                                throw new ConfigParseException("Could not find actionType:" + customActionDef.actionType);
                            }
                            if (customActionDef.pos != -999.0f) {
                                customAction.sortOrder = customActionDef.pos;
                            }
                            boolean z4 = false;
                            Iterator it3 = arrayListA5.iterator();
                            while (it3.hasNext()) {
                                if (customAction.equals((AbstractUnitAction) it3.next())) {
                                    z4 = true;
                                }
                            }
                            if (!z4) {
                                arrayListA5.add(customAction);
                            }
                            sortUnitActions(arrayListA5);
                        }
                    }
                }
            }
            if (unitType instanceof CustomUnitConfig) {
                CustomUnitConfig customUnitConfig5 = (CustomUnitConfig) unitType;
                customUnitConfig5.canBuildUnits = false;
                for (int i6 = 1; i6 <= 3; i6++) {
                    for (AbstractUnitAction abstractUnitAction : unitType.a(i6)) {
                        if (!(abstractUnitAction instanceof CustomAction) && abstractUnitAction.getUnitType() != null) {
                            customUnitConfig5.canBuildUnits = true;
                        }
                    }
                }
                Iterator it4 = customUnitConfig5.unitTypeReferences.iterator();
                while (it4.hasNext()) {
                    ((UnitTypeReference) it4.next()).b();
                }
            }
            boolean z5 = gameEngine.isInGameOrLobby() && gameEngine.networkEngine.roomSettings.useDisplayedCostAsResourceCost;
            for (int i7 = 1; i7 <= 3; i7++) {
                for (AbstractUnitAction abstractUnitAction2 : unitType.a(i7)) {
                    if (abstractUnitAction2.unitAction instanceof ActionWithCost) {
                        GameEngine.logErrorColored("=== ChainedActionConfig already on: " + unitType.getUnitTypeDescriptionShort() + " action:" + abstractUnitAction2.getDisplayName());
                        abstractUnitAction2.unitAction = ((ActionWithCost) abstractUnitAction2.unitAction).wrappedAction;
                    }
                    if (z5) {
                        UnitPrice displayText = abstractUnitAction2.getPrice();
                        UnitPrice additionalCost = abstractUnitAction2.getAdditionalCost();
                        if (!displayText.c() && additionalCost == null) {
                            ActionWithCost actionWithCost = new ActionWithCost(abstractUnitAction2.unitAction);
                            abstractUnitAction2.unitAction = actionWithCost;
                            actionWithCost.buildCost = UnitPrice.a;
                            actionWithCost.resourceCost = displayText;
                        }
                    }
                }
            }
        } catch (ConfigParseException e) {
            reportUnitLoadErrorForType(unitType.getUnitTypeDescriptionShort(), e, unitType);
        } catch (RuntimeException e2) {
            reportUnitLoadErrorForType(unitType.getUnitTypeDescriptionShort(), e2, unitType);
        }
    }

    /* JADX INFO: renamed from: g */
    public static void rebuildOverridesAndTriggersAsync() {
        synchronized (unitConfigLock) {
            applyOverridesAndTriggers();
        }
    }

    /* JADX INFO: renamed from: o */

    private static void applyOverridesAndTriggers() {
        CustomUnitConfig.configsById.clear();
        CustomUnitConfig.unitTypeOverrides.clear();

        for (CustomUnitConfig var1 : CustomUnitConfig.activeConfigs) {
            if (var1.modInfo != null) {
                String var2 = var1.modInfo.firstError;
                if (var2 != null) {
                    GameEngine.logColored(var1.getUnitTypeDescriptionShort() + "(mod:" + var1.getOwningModDisplayTitle() + "): Getting setup while mod has error: " + var2);
                }
            }

            for (CustomUnitTrigger var3 : var1.customUnitTriggers) {
                var3.runOnce = false;
            }

            var1.relatedUnits.clear();
        }

        for (CustomUnitConfig var16 : CustomUnitConfig.activeConfigs) {
            try {
                if (var16.image_shadow != null) {
                    String[] var22 = var16.image_shadow.split(",");

                    for (String var6 : var22) {
                        var6 = var6.trim();
                        boolean var7 = false;
                        UnitType var8 = CustomUnitConfig.a(var6, var7);
                        if (var8 == null) {
                            throw new ConfigParseException("Could not find overrideAndReplace target:" + var6);
                        }

                        if (var8 instanceof CustomUnitConfig) {
                            GameEngine.log("Replacing:" + var8.getUnitTypeDescriptionShort() + " with " + var16.getUnitTypeDescriptionShort());
                        }

                        CustomUnitConfig.unitTypeOverrides.put(var8, var16);
                    }
                }
            } catch (ConfigParseException var10) {
                reportUnitLoadErrorForType(var16.getUnitTypeDescriptionShort(), var10, var16);
            }
        }

        for (UnitTypeEnum var27 : UnitTypeEnum.values()) {
            applyCopyFromChain(var27);
        }

        for (CustomUnitConfig var18 : CustomUnitConfig.activeConfigs) {
            applyCopyFromChain((UnitType) var18);
        }

        for (CustomUnitConfig var19 : CustomUnitConfig.activeConfigs) {
            for (CustomUnitTrigger var28 : var19.customUnitTriggers) {
                if (!var28.runOnce) {
                    String var30 = var28.condition + " failed to find target:" + var28.triggerName;
                    var19.logWarning(var30);
                    if (var19.strictLevel >= 1) {
                        GameEngine.log("Converting warning to error (meta.strictLevel=" + var19.strictLevel + ")");
                        var19.throwConfigError(var30);
                    }
                }
            }

            if (var19.actionHandlers != null && var19.actionHandlers.size() > 0) {
                for (CustomUnitActionHandler var29 : var19.actionHandlers) {
                    try {
                        var29.b(var19);
                    } catch (ConfigParseException var9) {
                        reportUnitLoadErrorForType(var19.getUnitTypeDescriptionShort(), var9, var19);
                    }
                }
            }
        }

        for (CustomUnitConfig var20 : CustomUnitConfig.activeConfigs) {
            var20.rebuildActionIdCache();
        }

        Collections.sort(CustomUnitConfig.configsById, new CustomUnitConfigComparator());
    }

    /* JADX INFO: renamed from: a */
    public static CustomUnitConfig loadUnitConfigFile(String str, ModInfo modInfo, String str2, String str3) {
        try {
            long jA = PerformanceProfiler.a();
            AssetInputStream assetInputStreamLoadTextureInternal = openUnitConfigFile(str);
            if (assetInputStreamLoadTextureInternal == null) {
                throw new RuntimeException("Failed to open unit config file:" + str);
            }
            BufferedInputStream bufferedInputStream = new BufferedInputStream(assetInputStreamLoadTextureInternal);
            recordLoadPhaseTime(jA, LoadPhase.iniOpen);
            totalUnitFilesLoaded++;
            if (modInfo != null) {
                totalModUnitFilesLoaded++;
            }
            GameEngine gameEngine = GameEngine.getInstance();
            String displayTitle = "core units";
            if (modInfo != null) {
                displayTitle = modInfo.getDisplayTitle();
            }
            gameEngine.loadLevel("Loading units - " + totalUnitFilesLoaded + " (" + displayTitle + ")");
            CustomUnitConfig customUnitConfig = parse(str, bufferedInputStream, assetInputStreamLoadTextureInternal.lastModified(), modInfo, assetInputStreamLoadTextureInternal, str2, str3);
            long jA2 = PerformanceProfiler.a();
            try {
                bufferedInputStream.close();
                assetInputStreamLoadTextureInternal.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            recordLoadPhaseTime(jA2, LoadPhase.iniClose);
            return customUnitConfig;
        } catch (RuntimeException e2) {
            reportUnitLoadError(str, e2, modInfo);
            return null;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v167 */
    /* JADX WARN: Type inference failed for: r0v169 */
    /* JADX WARN: Type inference failed for: r0v66 */
    /* JADX WARN: Type inference failed for: r0v71 */
    /* JADX WARN: Type inference failed for: r0v72 */
    /* JADX WARN: Type inference failed for: r12v0 */
    /* JADX WARN: Type inference failed for: r12v1 */
    /* JADX WARN: Type inference failed for: r12v2 */
    /* JADX WARN: Type inference failed for: r12v4 */
    /* JADX WARN: Type inference failed for: r12v5 */
    /* JADX WARN: Type inference failed for: r12v6 */
    /* JADX INFO: renamed from: h */
    public static void loadAllCustomUnitsAndMods() {
        final GameEngine instance = GameEngine.getInstance();
        final ArrayList<ModInfo> loadAllMods = instance.modManager.loadAllMods();
        CustomUnitConfigParser.imageCacheHitCount = 0;
        CustomUnitConfigParser.imageCacheMissCount = 0;
        CustomUnitConfigParser.oomImageErrorCount = 0;
        CustomUnitConfigParser.logImageCacheMiss = false;
        final long a = PerformanceProfiler.a();
        for (final Texture texture : CustomUnitConfigParser.imageCache.values()) {
            texture.v = false;
            if (texture.a != null) {
                final Texture[] a2 = texture.a;
                for (int n = a2.length, i = 0; i < n; ++i) {
                    a2[i].v = false;
                }
            }
            if (texture.b != null) {
                final Texture[] b = texture.b;
                for (int n = b.length, i = 0; i < n; ++i) {
                    b[i].v = false;
                }
            }
            if (texture.c != null) {
                final Texture[] c = texture.c;
                for (int n = c.length, i = 0; i < n; ++i) {
                    c[i].v = false;
                }
            }
        }
        final Iterator iterator2 = CustomUnitConfigParser.soundCache.values().iterator();
        while (iterator2.hasNext()) {
            ((Sound) iterator2.next()).g = false;
        }
        byte[] array = null;
        byte[][] array2 = null;
        ByteBuffer[] array3 = null;
        try {
            array = new byte[8000000];
            array[0] = instance.memoryProbeWriteByte;
            instance.memoryProbeReadByte = array[1];
            array2 = new byte[][]{new byte[3000000], new byte[3000000]};
            array2[0][0] = instance.memoryProbeWriteByte;
            array2[1][0] = instance.memoryProbeWriteByte;
            if (!GameEngine.isAndroidPlatform()) {
                array3 = new ByteBuffer[]{ByteBuffer.allocateDirect(5000000), ByteBuffer.allocateDirect(5000000), ByteBuffer.allocateDirect(5000000), ByteBuffer.allocateDirect(5000000)};
            }
        } catch (final OutOfMemoryError outOfMemoryError) {
            System.gc();
            GameEngine.log("Failed to reserve memory pre-mod load");
        }
        synchronized (CustomUnitConfig.allConfigs) {
            CustomUnitConfig.allConfigs.clear();
        }
        CustomUnitConfig.activeConfigs.clear();
        CustomUnitConfig.validUnitsForSync = null;
        CustomUnitConfig.unitTypeOverrides.clear();
        instance.modManager.clearInvalidMods();
        CustomUnitConfigParser.totalUnitFilesLoaded = 0;
        CustomUnitConfigParser.totalModUnitFilesLoaded = 0;
        synchronized (CustomUnitConfigParser.iniCache) {
            CustomUnitConfigParser.iniCache.clear();
        }
        readAllCustomUnitConfigs(FileHelper.mapPath("units"), 1, false, null, FileHelper.mapPath("units"), null);
        if (!GameEngine.isModsDisabled && !instance.isDemo) {
            final String defaultUserModsFolder = getDefaultUserModsFolder();
            if (!FileHelper.isDirectoryNonZip(defaultUserModsFolder)) {
                GameEngine.log("Modded Custom '" + defaultUserModsFolder + "' directory not found");
            }
            for (final ModInfo b2 : loadAllMods) {
                if (!b2.isFromSteam && b2.sourceFolder != null) {
                    String s = b2.getSourceFolderRaw();
                    if (b2.isCoreMod) {
                        s = FileHelper.mapPath(s);
                    }
                    if (b2.disabled) {
                        GameEngine.log("Disabled mod at:" + s + " (name:" + b2.getDisplayTitle() + ")");
                    } else {
                        GameEngine.log("Loading mod at:" + s + " (name:" + b2.getDisplayTitle() + ")");
                    }
                    readAllCustomUnitConfigs(s, 2, true, b2, s, null);
                }
            }
            for (final ModInfo b3 : loadAllMods) {
                if (b3.isFromSteam && b3.sourceFolder != null) {
                    final String absolutePath = b3.getAbsolutePath();
                    if (b3.disabled) {
                        GameEngine.log("Disabled workshop mod at:" + absolutePath + " (name:" + b3.getDisplayTitle() + ")");
                    } else {
                        GameEngine.log("Loading workshop mod at:" + absolutePath + " (name:" + b3.getDisplayTitle() + ")");
                    }
                    readAllCustomUnitConfigs(absolutePath, 2, true, b3, absolutePath, null);
                }
            }
        }
        logAndResetTimingStats();
        enableAllCustomUnits(true);
        GameEngine.log("Done loading custom units. image cacheHits:" + CustomUnitConfigParser.imageCacheHitCount + " image cacheMisses:" + CustomUnitConfigParser.imageCacheMissCount + " (in: " + PerformanceProfiler.a(a) + "ms)");
        GameEngine.log("========= Mods data loaded ===========");
        GameEngine.log("Number of mods:" + loadAllMods.size());
        for (ModInfo loadAllMod : loadAllMods) {
            loadAllMod.logMemoryUsage();
        }
        GameEngine.log("================================");
        if (array3 != null) {
            array3[1] = (array3[0] = null);
            array3[3] = (array3[2] = null);
        }
        if (array2 != null) {
            array2[1] = (array2[0] = null);
            final byte[][] array4 = null;
        }
        if (array != null) {
            array[1] = instance.memoryProbeWriteByte;
            instance.memoryProbeReadByte = array[1];
            System.gc();
            System.gc();
        }
    }

    /* JADX INFO: renamed from: a */
    public static void readAllCustomUnitConfigs(String str, int i, boolean z, ModInfo modInfo, String str2, String str3) {
        boolean z2 = z && i == 1;
        GameEngine gameEngine = GameEngine.getInstance();
        if (modInfo != null) {
            if (modInfo.disabled && !gameEngine.settingsEngine.loadDisabledModData) {
                modInfo.disabledOrNotLoaded = true;
                return;
            }
            modInfo.disabledOrNotLoaded = false;
        }
        if (modInfo != null && modInfo.disabled) {
            GameEngine.log("Note: Loading disabled mod: " + str);
        }
        FileHelper.getLastError();
        String[] strArrListFiles = FileHelper.listFiles(str);
        if (strArrListFiles == null) {
            String readPath = FileHelper.getLastError();
            GameEngine.logColored("readAllCustomUnitConfigs: ERROR");
            GameEngine.logColored("readAllCustomUnitConfigs: Failed to load:" + str);
            if (modInfo != null) {
                if (!modInfo.hasError) {
                    if (readPath == null) {
                        modInfo.firstError = "Failed to list directory, check file permissions";
                        return;
                    } else {
                        modInfo.firstError = "Failed to list directory: " + readPath;
                        return;
                    }
                }
                modInfo.firstWarning = "Failed to list subdirectory: '" + str + "' check file permissions";
                if (readPath != null) {
                    modInfo.firstWarning += ": " + readPath;
                    return;
                }
                return;
            }
            return;
        }
        if (modInfo != null) {
            modInfo.hasError = true;
        }
        if (!z2) {
            for (String str4 : strArrListFiles) {
                if (str4.equalsIgnoreCase("all-units.template")) {
                    str3 = str;
                }
            }
        }
        for (String str5 : strArrListFiles) {
            if (!str5.equals("custom_units_here.txt") && !str5.equals("mods_here_will_be_enabled_by_default.txt") && !str5.equals("__MACOSX")) {
                boolean z3 = false;
                ModInfo modByShortName = modInfo;
                if (z && i == 1 && modByShortName == null) {
                    modByShortName = gameEngine.modManager.getModByShortName(str5);
                    if (modByShortName == null) {
                        GameEngine.logColored("readAllCustomUnitConfigs: Could not find linked mod:" + str5);
                        modByShortName = gameEngine.modManager.modInfo;
                    }
                    z3 = true;
                }
                if (str5.toLowerCase(Locale.ENGLISH).endsWith(".ini") && !z2) {
                    String str6 = str + "/" + str5;
                    if (lastModUsedForInit != modByShortName && modByShortName != null) {
                        lastModUsedForInit = modByShortName;
                        logAndResetTimingStats();
                        GameEngine.log("Loading units from mod: " + modByShortName.dirName);
                    }
                    if (str5.equalsIgnoreCase("desktop.ini")) {
                        GameEngine.log("Skipping possible system file: " + str6);
                    } else {
                        long jA = PerformanceProfiler.a();
                        loadUnitConfigFile(str6, modByShortName, str2, str3);
                        recordLoadPhaseTime(jA, LoadPhase.unitParse);
                    }
                } else if (str5.toLowerCase(Locale.ENGLISH).endsWith(".tmx")) {
                    String str7 = str + "/" + str5;
                    GameEngine.log("Found map: " + str7);
                    if (modByShortName != null && modByShortName.canBeDisabled) {
                        gameEngine.modManager.addInvalidMod(str7, modByShortName);
                    } else {
                        GameEngine.log("Skipping map due to mod settings");
                    }
                } else {
                    String str8 = str + "/" + str5;
                    if (i < 10) {
                        if (FileHelper.isDirectoryNonZip(str8)) {
                            String str9 = str2;
                            if (str9 == null) {
                                str9 = str8;
                            }
                            long jA2 = -1;
                            if (z3) {
                                jA2 = PerformanceProfiler.a();
                                GameEngine.log("============");
                                GameEngine.log(">>> Mod '" + modByShortName.getPaddedTitle40() + "'" + (modByShortName.isEnabled() ? VariableScope.nullOrMissingString : " (disabled)"));
                            }
                            readAllCustomUnitConfigs(str8, i + 1, z, modByShortName, str9, str3);
                            if (z3 && modByShortName != null && modByShortName.isEnabled()) {
                                GameEngine.log("Mod '" + modByShortName.getPaddedTitle40() + "' load took:" + PerformanceProfiler.a(PerformanceProfiler.a(jA2)));
                            }
                        }
                    } else {
                        GameEngine.log("Too many levels:" + str8);
                    }
                }
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public static CustomUnitConfig parse(String str, InputStream inputStream, long j, ModInfo modInfo, AssetInputStream assetInputStream, String str2, String str3) {
        boolean z;
        String strSubstring;
        boolean z2;
        GameEngine gameEngine = GameEngine.getInstance();
        try {
            if (debugLogging) {
                String sourceFolderRaw = "CORE";
                if (modInfo != null) {
                    sourceFolderRaw = modInfo.getSourceFolderRaw();
                }
                GameEngine.log("Loading unit config: " + str + " [" + sourceFolderRaw + "]");
            }
            long jA = PerformanceProfiler.a();
            try {
                IniFile iniFile = new IniFile(inputStream, str);
                recordLoadPhaseTime(jA, LoadPhase.iniParse);
                CustomUnitConfig customUnitConfig = new CustomUnitConfig();
                if (iniFile.getBoolean("core", "dont_load", (Boolean) false).booleanValue()) {
                    return null;
                }
                customUnitConfig.configPath = str;
                customUnitConfig.sourceFilePath = assetInputStream.getPath();
                customUnitConfig.resourceLoadPath = customUnitConfig.configPath;
                customUnitConfig.modInfo = modInfo;
                customUnitConfig.onNewMapSpawn_ifUnitIsMissing = str2;
                customUnitConfig.onNewMapSpawn_ifUnitIsPresent = str3;
                currentModInfo = modInfo;
                isLoadingCoreUnit = false;
                if (customUnitConfig.modInfo != null) {
                }
                long jA2 = PerformanceProfiler.a();
                reloadSingleUnitConfig(customUnitConfig, iniFile, iniFile, str, 0);
                if (customUnitConfig.onNewMapSpawn_ifUnitIsPresent != null) {
                    mergeAndApplyUnitChanges(customUnitConfig, iniFile, customUnitConfig.onNewMapSpawn_ifUnitIsPresent + "/all-units.template", "AUTO units.template", true);
                }
                iniFile.trackRead("core", "copyFrom");
                customUnitConfig.strictLevel = iniFile.getInt("core", "strictLevel", (Integer) 0).intValue();
                if (customUnitConfig.strictLevel < 0) {
                    throw new ConfigParseException("[core]strictLevel cannot be < 0");
                }
                if (customUnitConfig.strictLevel > 1) {
                    throw new ConfigParseException("[core]strictLevel cannot yet be > 1");
                }
                customUnitConfig.debugCreditResourceUsage = iniFile.getBoolean("core", "logIfCreditResourceUsed", (Boolean) false).booleanValue();
                iniFile.trackRead("core", "dont_load");
                iniFile.getString("core", "class", "CustomUnitMetadata");
                Iterator it = iniFile.getSectionsWithKey("@copyFrom_skipThisSection").iterator();
                while (it.hasNext()) {
                    iniFile.trackRead((String) it.next(), "@copyFrom_skipThisSection");
                }
                for (String str4 : iniFile.getSectionsWithKey("@copyFromSection")) {
                    applyCopyFromSection(customUnitConfig, iniFile, str4, str4, 0);
                }
                VariableSubstitutionParser.a(customUnitConfig, iniFile);
                String string = iniFile.getString("core", "overrideResourceLoadPath", (String) null);
                if (string != null) {
                    customUnitConfig.resourceLoadPath = resolveTexturePath(customUnitConfig, str, string);
                }
                recordLoadPhaseTime(jA2, LoadPhase.iniSetup);
                customUnitConfig.name = iniFile.getValueStrict("core", "name");
                customUnitConfig.configHash = iniFile.getHash();
                if (customUnitConfig.name.equals("self")) {
                    throw new ConfigParseException("Unit name: " + customUnitConfig.name + " is reserved");
                }
                if (customUnitConfig.name.startsWith("self.")) {
                    throw new ConfigParseException("Unit name cannot start with self.");
                }
                String string2 = iniFile.getString("core", "altNames", (String) null);
                if (string2 != null && !string2.equalsIgnoreCase("NONE")) {
                    for (String str5 : string2.split(",")) {
                        customUnitConfig.autoTriggerAction.add(str5.trim());
                    }
                }
                customUnitConfig.tags = AnimationTag.a(iniFile.getString("core", "tags", (String) null));
                if (customUnitConfig.strictLevel >= 1 && customUnitConfig.tags != null) {
                    for (AnimationTag animationTag : customUnitConfig.tags.a) {
                        if (animationTag.tagName.contains(" ")) {
                            throw new ConfigParseException("(strictLevel 1) [core]tags: space in tag: '" + animationTag.tagName + "'");
                        }
                    }
                }
                customUnitConfig.image_shadow = iniFile.getString("core", "overrideAndReplace", (String) null);
                if (customUnitConfig.image_shadow != null && customUnitConfig.image_shadow.equalsIgnoreCase("NONE")) {
                    customUnitConfig.image_shadow = null;
                }
                String string3 = iniFile.getString("core", "defineUnitMemory", (String) null);
                if (string3 != null) {
                    customUnitConfig.variableMapping.addDefineValue(customUnitConfig, "core", "defineUnitMemory", string3);
                    if (customUnitConfig.variableMapping.hasArrays()) {
                        customUnitConfig.a("1.15p11", 115011, "core", "Memory arrays (in defineUnitMemory)");
                    }
                }
                for (String str6 : iniFile.getKeysStartingWith("core", "@memory ")) {
                    String strTrim = str6.substring("@memory ".length()).trim();
                    String string4 = iniFile.getString("core", str6, (String) null);
                    if (string4 != null) {
                        if (!string4.contains(",")) {
                            customUnitConfig.variableMapping.addSingleDefine(customUnitConfig, strTrim, string4, "core", str6);
                            if (customUnitConfig.variableMapping.hasArrays()) {
                                customUnitConfig.a("1.15p11", 115011, "core", "Memory arrays (in " + str6 + ")");
                            }
                        } else {
                            throw new ConfigParseException("[core]" + str6 + ": Only a single variable can be defined per @memory");
                        }
                    }
                }
                customUnitConfig.onNewMapSpawn = (SpawnPointType) iniFile.getEnum("core", "onNewMapSpawn", (Enum) null, SpawnPointType.class);
                customUnitConfig.globalScale = iniFile.getFloat("core", "globalScale", Float.valueOf(1.0f)).floatValue();
                customUnitConfig.registerConfigWatcher(customUnitConfig.sourceFilePath);
                if (customUnitConfig.name.equals("missing")) {
                    if (modInfo == null) {
                        GameEngine.log("Setting missingPlaceHolder");
                        CustomUnitConfig.instance = customUnitConfig;
                    } else {
                        GameEngine.log("Not setting missingPlaceHolder, as we are in a mod");
                    }
                }
                customUnitConfig.displayLocaleKey = iniFile.getString("core", "displayLocaleKey", (String) null);
                customUnitConfig.displayText = getLocaleString(iniFile, "core", "displayText", (String) null);
                customUnitConfig.displayDescription = getLocaleString(iniFile, "core", "displayDescription", (String) null);
                customUnitConfig.isBio = iniFile.getBoolean("core", "isBio", (Boolean) false).booleanValue();
                customUnitConfig.isBug = iniFile.getBoolean("core", "isBug", (Boolean) false).booleanValue();
                customUnitConfig.isPickableStartingUnit = iniFile.getBoolean("core", "isPickableStartingUnit", (Boolean) false).booleanValue();
                customUnitConfig.startFallingWhenStartingUnit = iniFile.getBoolean("core", "startFallingWhenStartingUnit", (Boolean) false).booleanValue();
                customUnitConfig.stayNeutral = iniFile.getBoolean("core", "stayNeutral", (Boolean) false).booleanValue();
                customUnitConfig.createNeutral = iniFile.getBoolean("core", "createNeutral", (Boolean) false).booleanValue();
                customUnitConfig.allowCaptureWhenNeutralByAI = iniFile.getBoolean("core", "allowCaptureWhenNeutralByAI", (Boolean) false).booleanValue();
                if (iniFile.getBoolean("core", "createOnNeutralTeam", (Boolean) false).booleanValue()) {
                    customUnitConfig.createNeutral = true;
                }
                customUnitConfig.whileNeutralTransportAnyTeam = iniFile.getBoolean("core", "whileNeutralTransportAnyTeam", (Boolean) false).booleanValue();
                customUnitConfig.whileNeutralConvertToTransportedTeam = iniFile.getBoolean("core", "whileNeutralConvertToTransportedTeam", (Boolean) false).booleanValue();
                customUnitConfig.convertToNeutralIfNotTransporting = iniFile.getBoolean("core", "convertToNeutralIfNotTransporting", (Boolean) false).booleanValue();
                if (customUnitConfig.convertToNeutralIfNotTransporting) {
                    customUnitConfig.stayNeutral = true;
                }
                customUnitConfig.createOnAggressiveTeam = iniFile.getBoolean("core", "createOnAggressiveTeam", (Boolean) false).booleanValue();
                customUnitConfig.showInEditor = iniFile.getBoolean("core", "showInEditor", (Boolean) true).booleanValue();
                customUnitConfig.total_frames = iniFile.getInt("graphics", "total_frames", (Integer) 1).intValue();
                if (customUnitConfig.total_frames < 1) {
                    throw new ConfigParseException("TOTAL_FRAMES cannot be: " + customUnitConfig.total_frames + " (must be 1 or more)");
                }
                customUnitConfig.frame_width = iniFile.getInt("graphics", "frame_width", (Integer) (-1)).intValue();
                customUnitConfig.frame_height = iniFile.getInt("graphics", "frame_height", (Integer) (-1)).intValue();
                customUnitConfig.default_frame = iniFile.getInt("graphics", "default_frame", (Integer) 0).intValue();
                customUnitConfig.image_offsetX = iniFile.getInt("graphics", "image_offsetX", (Integer) 0).intValue();
                customUnitConfig.image_offsetY = iniFile.getInt("graphics", "image_offsetY", (Integer) 0).intValue();
                customUnitConfig.image_offsetH = iniFile.getFloat("graphics", "image_offsetH", Float.valueOf(0.0f)).floatValue();
                if (customUnitConfig.image_offsetX != 0 || customUnitConfig.image_offsetY != 0 || customUnitConfig.image_offsetH != 0.0f) {
                    customUnitConfig.hasImageOffset = true;
                }
                customUnitConfig.teamColoringMode = ColorMode.pureGreen;
                if (iniFile.getBoolean("graphics", "teamColorsUseHue", (Boolean) false).booleanValue()) {
                    customUnitConfig.teamColoringMode = ColorMode.hueAdd;
                }
                String string5 = iniFile.getString("graphics", "teamColoringMode", (String) null);
                if (string5 != null) {
                    if (iniFile.getBoolean("graphics", "teamColorsUseHue", (Boolean) null) != null) {
                        throw new ConfigParseException("Cannot use teamColoringMode and teamColorsUseHue at the same time");
                    }
                    if (string5.equalsIgnoreCase("pureGreen")) {
                        customUnitConfig.teamColoringMode = ColorMode.pureGreen;
                    } else if (string5.equalsIgnoreCase("hueAdd")) {
                        customUnitConfig.teamColoringMode = ColorMode.hueAdd;
                    } else if (string5.equalsIgnoreCase("hueShift")) {
                        customUnitConfig.teamColoringMode = ColorMode.hueShift;
                    } else if (string5.equalsIgnoreCase("disabled")) {
                        customUnitConfig.teamColoringMode = ColorMode.disabled;
                    } else {
                        throw new ConfigParseException("Unknown teamColoringMode:" + string5);
                    }
                }
                customUnitConfig.imageSmoothing = iniFile.getBoolean("graphics", "imageSmoothing", (Boolean) false).booleanValue();
                customUnitConfig.imageSmoothingWhenZoomedIn = iniFile.getBoolean("graphics", "imageSmoothingWhenZoomedIn", (Boolean) false).booleanValue();
                customUnitConfig.isVisible = iniFile.getLogicBoolean(customUnitConfig, "graphics", "isVisible", null);
                if (customUnitConfig.isVisible == LogicBoolean.trueBoolean) {
                    customUnitConfig.isVisible = null;
                }
                customUnitConfig.unitStats.isVisibleToEnemies = iniFile.getBoolean("graphics", "isVisibleToEnemies", (Boolean) true).booleanValue();
                customUnitConfig.baseTexture = customUnitConfig.a(customUnitConfig.resourceLoadPath, iniFile.getValueStrict("graphics", "image"), customUnitConfig.imageSmoothing, "graphics", "image");
                if (customUnitConfig.baseTexture == null) {
                    throw new ConfigParseException("Main unit image must be set on custom unit");
                }
                customUnitConfig.imageFloatingPointSize = iniFile.getBoolean("graphics", "image_floatingPointSize", (Boolean) false).booleanValue();
                customUnitConfig.frameWidth = customUnitConfig.baseTexture.m() / customUnitConfig.total_frames;
                customUnitConfig.frameHeight = customUnitConfig.baseTexture.l();
                if (customUnitConfig.frameWidth < 1) {
                    customUnitConfig.frameWidth = 1;
                }
                if (customUnitConfig.frame_width > 0) {
                    customUnitConfig.frameWidth = customUnitConfig.frame_width;
                }
                if (customUnitConfig.frame_height > 0) {
                    customUnitConfig.frameHeight = customUnitConfig.frame_height;
                    if (customUnitConfig.frameHeight < customUnitConfig.baseTexture.l()) {
                        customUnitConfig.frameColumns = customUnitConfig.baseTexture.m() / customUnitConfig.frameWidth;
                        if (customUnitConfig.frameColumns < 1) {
                            customUnitConfig.frameColumns = 1;
                        }
                    }
                }
                customUnitConfig.image_back = customUnitConfig.a(iniFile, "graphics", "image_back");
                customUnitConfig.image_back_always_use_full_image = iniFile.getBoolean("graphics", "image_back_always_use_full_image", (Boolean) false).booleanValue();
                customUnitConfig.image_wreak = customUnitConfig.a(iniFile, "graphics", "image_wreak");
                customUnitConfig.image_turret = customUnitConfig.a(iniFile, "graphics", "image_turret");
                customUnitConfig.teamColoredIconTextures = LandUnit.landUnitIconTextures;
                String string6 = iniFile.getString("graphics", "image_shadow", "NONE");
                if (string6.equalsIgnoreCase("AUTO")) {
                    String str7 = "[autoShadow:" + customUnitConfig.frameWidth + "," + customUnitConfig.frameHeight + "]" + customUnitConfig.baseTexture.d + "-" + customUnitConfig.baseTexture.e;
                    Texture textureFromCache = getTextureFromCache(str7);
                    if (textureFromCache != null) {
                        customUnitConfig.shadowTexture = textureFromCache;
                    } else {
                        customUnitConfig.shadowTexture = BaseUnit.attackUnit(customUnitConfig.baseTexture, customUnitConfig.frameWidth, customUnitConfig.frameHeight);
                        trackImageMemory(customUnitConfig.shadowTexture);
                        if (customUnitConfig.shadowTexture != null) {
                            putTextureInCache(str7, customUnitConfig.shadowTexture);
                        }
                    }
                } else if (string6.equalsIgnoreCase("AUTO_ANIMATED")) {
                    String str8 = "[autoShadowAnimated:" + customUnitConfig.frameWidth + "," + customUnitConfig.frameHeight + "]" + customUnitConfig.baseTexture.d + "-" + customUnitConfig.baseTexture.e;
                    Texture textureFromCache2 = getTextureFromCache(str8);
                    if (textureFromCache2 != null) {
                        customUnitConfig.shadowTexture = textureFromCache2;
                    } else {
                        customUnitConfig.shadowTexture = BaseUnit.attackUnit(customUnitConfig.baseTexture, customUnitConfig.baseTexture.m(), customUnitConfig.baseTexture.l());
                        trackImageMemory(customUnitConfig.shadowTexture);
                        if (customUnitConfig.shadowTexture != null) {
                            putTextureInCache(str8, customUnitConfig.shadowTexture);
                        }
                    }
                    customUnitConfig.hasShadowFrames = true;
                } else {
                    customUnitConfig.shadowTexture = customUnitConfig.a(customUnitConfig.resourceLoadPath, string6, customUnitConfig.imageSmoothing, "graphics", "image_shadow");
                }
                if (iniFile.getBoolean("graphics", "image_shadow_frames", (Boolean) false).booleanValue()) {
                    customUnitConfig.hasShadowFrames = true;
                }
                customUnitConfig.teamColoredBaseTextures = customUnitConfig.a(customUnitConfig.baseTexture, customUnitConfig.teamColoringMode);
                customUnitConfig.teamColorsOnTurret = iniFile.getBoolean("graphics", "teamColorsOnTurret", (Boolean) false).booleanValue();
                if (customUnitConfig.teamColorsOnTurret && customUnitConfig.image_turret != null) {
                    customUnitConfig.teamColoredTurretTextures = customUnitConfig.a(customUnitConfig.image_turret, customUnitConfig.teamColoringMode);
                }
                float fFloatValue = iniFile.getFloat("graphics", "scaleImagesTo", Float.valueOf(-1.0f)).floatValue();
                if (fFloatValue > 0.0f) {
                    customUnitConfig.imageScale = (fFloatValue * customUnitConfig.globalScale) / customUnitConfig.frameWidth;
                }
                float fFloatValue2 = iniFile.getFloat("graphics", "imageScale", Float.valueOf(1.0f)).floatValue();
                if (fFloatValue2 != 1.0f) {
                    customUnitConfig.imageScale *= fFloatValue2;
                }
                float fFloatValue3 = iniFile.getFloat("graphics", "scaleTurretImagesTo", Float.valueOf(-1.0f)).floatValue();
                if (fFloatValue3 > 0.0f) {
                    float f = fFloatValue3 * customUnitConfig.globalScale;
                    if (customUnitConfig.image_turret == null) {
                        throw new RuntimeException("scaleTurretImagesTo needs image_turret set");
                    }
                    customUnitConfig.turretImageScale = f / customUnitConfig.image_turret.p;
                }
                float fFloatValue4 = iniFile.getFloat("graphics", "turretImageScale", Float.valueOf(1.0f)).floatValue();
                if (fFloatValue4 != 1.0f) {
                    customUnitConfig.turretImageScale *= fFloatValue4;
                }
                customUnitConfig.shieldTexture = ExperimentalHoverTank.e;
                Texture textureA = customUnitConfig.a(iniFile, "graphics", "image_shield");
                if (textureA != null) {
                    customUnitConfig.shieldTexture = textureA;
                    customUnitConfig.hasCustomShieldImage = true;
                }
                customUnitConfig.icon_build = customUnitConfig.a(iniFile, "graphics", "icon_build", false);
                float fM = customUnitConfig.baseTexture.m() * customUnitConfig.imageScale;
                float fL = customUnitConfig.baseTexture.l() * customUnitConfig.imageScale;
                if (fM / 2.0f > 90.0f || fL / 2.0f > 90.0f) {
                    customUnitConfig.largeImageBounds = new Rect();
                    customUnitConfig.largeImageBounds.a = (int) ((-fM) / 2.0f);
                    customUnitConfig.largeImageBounds.c = (int) (fM / 2.0f);
                    customUnitConfig.largeImageBounds.b = (int) ((-fL) / 2.0f);
                    customUnitConfig.largeImageBounds.d = (int) (fL / 2.0f);
                    customUnitConfig.isHover = true;
                }
                for (String str9 : iniFile.getSectionsStartingWithOr("resource_", "global_resource_")) {
                    if (str9.startsWith("resource_")) {
                        strSubstring = str9.substring("resource_".length());
                        z2 = false;
                    } else {
                        strSubstring = str9.substring("global_resource_".length());
                        z2 = true;
                    }
                    String strTrim2 = strSubstring.trim();
                    if (strTrim2.contains(" ")) {
                        throw new RuntimeException("[" + str9 + "] resource codename cannot contain a space");
                    }
                    if (strTrim2.contains("=") || strTrim2.contains("|") || strTrim2.contains(":") || strTrim2.contains(",") || strTrim2.contains("(") || strTrim2.contains(")") || strTrim2.contains("<") || strTrim2.contains(">") || strTrim2.contains("$")) {
                        throw new RuntimeException("[" + str9 + "] resource codename cannot contain the symbols: =|:,()<>$");
                    }
                    ResourceDefinition resourceDefinition = new ResourceDefinition(z2);
                    resourceDefinition.a(customUnitConfig, iniFile, str9, strTrim2);
                    if (customUnitConfig.findCustomResourceInList(resourceDefinition.a) != null) {
                        throw new RuntimeException("[" + str9 + "] resource with name:" + resourceDefinition.a + " already exists in this file");
                    }
                    customUnitConfig.customResourcesList.add(resourceDefinition);
                }
                for (ResourceDefinition o : customUnitConfig.customResourcesList) {
                    o.a(customUnitConfig);
                }
                if (gameEngine.isModdingEnabled()) {
                    RandomMovementHook.a(customUnitConfig, iniFile);
                    RepelFromUnitsMovementHook.a(customUnitConfig, iniFile);
                }
                AttachmentManagerHook.a(customUnitConfig, iniFile);
                customUnitConfig.autoTriggerCooldownTime = iniFile.getTime("core", "autoTriggerCooldownTime", Float.valueOf(60.0f)).floatValue();
                if (customUnitConfig.autoTriggerCooldownTime < 0.0f) {
                    throw new RuntimeException("autoTriggerCooldownTime cannot be < 0");
                }
                if (customUnitConfig.autoTriggerCooldownTime > 120.0f) {
                    throw new RuntimeException("autoTriggerCooldownTime cannot be more than 2 seconds");
                }
                if (!iniFile.getBoolean("core", "autoTriggerCooldownTime_allowDangerousHighCPU", (Boolean) false).booleanValue() && customUnitConfig.autoTriggerCooldownTime < 5.0f) {
                    throw new RuntimeException("autoTriggerCooldownTime cannot be this low (without override). Note this cooldown is only applied after triggering an action not for the detection.");
                }
                customUnitConfig.autoTriggerCheckRate = (UpdateFrequency) iniFile.getEnum("core", "autoTriggerCheckRate", UpdateFrequency.everyFrame, UpdateFrequency.class);
                customUnitConfig.autoTriggerCheckWhileNotBuilt = iniFile.getBoolean("core", "autoTriggerCheckWhileNotBuilt", (Boolean) false).booleanValue();
                customUnitConfig.unitStats.mass = iniFile.getIntStrict("core", "mass");
                customUnitConfig.availableInDemo = iniFile.getBoolean("core", "availableInDemo", (Boolean) true).booleanValue();
                customUnitConfig.isLocked = iniFile.getBoolean("core", "isLocked", (Boolean) false).booleanValue();
                customUnitConfig.isLockedIfGameModeNoNuke = iniFile.getBoolean("core", "isLockedIfGameModeNoNuke", (Boolean) false).booleanValue();
                customUnitConfig.price = UnitPrice.a(customUnitConfig, iniFile, "core", "price", false);
                customUnitConfig.reclaimPrice = UnitPrice.a(customUnitConfig, iniFile, "core", "reclaimPrice", (UnitPrice) null);
                customUnitConfig.streamingCost = UnitPrice.b(customUnitConfig, iniFile, "core", "streamingCost", null);
                boolean zBooleanValue = iniFile.getBoolean("core", "switchPriceWithStreamingCost", (Boolean) false).booleanValue();
                if (zBooleanValue) {
                    if (customUnitConfig.streamingCost != null) {
                        throw new RuntimeException("[core]streamingCost and switchPriceWithStreamingCost=true cannot be used at the same time");
                    }
                    customUnitConfig.streamingCost = UnitPrice.b(customUnitConfig, iniFile, "core", "price", null);
                    customUnitConfig.price = UnitPrice.a;
                }
                customUnitConfig.buildSpeed = iniFile.getInvertedTime("core", "buildSpeed", Float.valueOf(1.0f)).floatValue();
                customUnitConfig.techLevel = iniFile.getInt("core", "techLevel", (Integer) 1).intValue();
                if (customUnitConfig.techLevel > 3) {
                    throw new RuntimeException("techLevel cannot be greater than max tech level of:3");
                }
                if (customUnitConfig.techLevel < 1) {
                    throw new RuntimeException("techLevel cannot be less than 1, it is:" + customUnitConfig.techLevel);
                }
                customUnitConfig.experimental = iniFile.getBoolean("core", "experimental", (Boolean) false).booleanValue();
                customUnitConfig.borrowResourcesWhileAlive = UnitPrice.a(customUnitConfig, iniFile, "core", "borrowResourcesWhileAlive", true);
                customUnitConfig.borrowResourcesWhileBuilt = UnitPrice.a(customUnitConfig, iniFile, "core", "borrowResourcesWhileBuilt", true);
                customUnitConfig.generationTemplate = UnitPrice.a(customUnitConfig, iniFile, "core", "generation_resources", true);
                int iIntValue = iniFile.getInt("core", "generation_credits", (Integer) 0).intValue();
                if (iIntValue != 0) {
                    customUnitConfig.generationTemplate = UnitPrice.a(customUnitConfig.generationTemplate, UnitPrice.a(iIntValue));
                }
                customUnitConfig.generationDelay = iniFile.getInt("core", "generation_delay", (Integer) 40).intValue();
                if (customUnitConfig.generationDelay == 0) {
                    customUnitConfig.generationDelay = 1;
                }
                if (customUnitConfig.generationDelay < 0) {
                    throw new RuntimeException("[core]generation_delay cannot be < 0");
                }
                customUnitConfig.generationRate = 40.0f / customUnitConfig.generationDelay;
                if (!customUnitConfig.generationTemplate.c()) {
                    customUnitConfig.generationCondition = new StoredResources();
                    customUnitConfig.generationCondition.a(customUnitConfig.generationTemplate);
                    customUnitConfig.generationCondition.a(customUnitConfig.generationRate);
                    customUnitConfig.hasGenerationCondition = true;
                }
                if (!customUnitConfig.generationCondition.c()) {
                    for (StoredResourceEntry storedResourceEntry : customUnitConfig.generationCondition.b) {
                        if (!storedResourceEntry.a.c() && storedResourceEntry.a.d()) {
                            if (customUnitConfig.generationTagTemplate == StoredResources.a) {
                                customUnitConfig.generationTagTemplate = new StoredResources();
                            }
                            customUnitConfig.generationTagTemplate.b(storedResourceEntry.a, storedResourceEntry.b);
                        }
                    }
                }
                customUnitConfig.generationActive = iniFile.getLogicBoolean(customUnitConfig, "core", "generation_active", LogicBoolean.trueBoolean);
                customUnitConfig.a(customUnitConfig.generationTemplate);
                customUnitConfig.resourceRate = iniFile.getFloat("core", "resourceRate", Float.valueOf(0.0f)).floatValue();
                if (zBooleanValue && customUnitConfig.resourceRate != 0.0f) {
                    throw new RuntimeException("To avoid mistakes [core]resourceRate cannot be used with switchPriceWithStreamingCost=true");
                }
                String string7 = iniFile.getString("core", "updateUnitMemory", (String) null);
                if (string7 != null) {
                    customUnitConfig.updateUnitMemoryWriter = VariableScope.createMemoryWriter(string7, customUnitConfig, "core", "updateUnitMemory");
                }
                customUnitConfig.updateUnitMemoryRate = iniFile.getTime("core", "updateUnitMemoryRate", Float.valueOf(60.0f)).floatValue();
                customUnitConfig.resourceMaxConcurrentReclaimingThis = iniFile.getInt("core", "resourceMaxConcurrentReclaimingThis", (Integer) Integer.MAX_VALUE).intValue();
                customUnitConfig.similarResourcesHaveTag = iniFile.getAnimationSet(customUnitConfig, "core", "similarResourcesHaveTag", (AnimationSet) null);
                customUnitConfig.soundOnAttackOrder = SoundList.a(customUnitConfig, iniFile.getString("core", "soundOnAttackOrder", (String) null));
                customUnitConfig.soundOnMoveOrder = SoundList.a(customUnitConfig, iniFile.getString("core", "soundOnMoveOrder", (String) null));
                customUnitConfig.soundOnNewSelection = SoundList.a(customUnitConfig, iniFile.getString("core", "soundOnNewSelection", (String) null));
                String string8 = iniFile.getString("graphics", "drawLayer", (String) null);
                if (string8 != null) {
                    if (string8.equals("experimentals")) {
                        customUnitConfig.drawLayer = 4;
                    } else if (string8.equals("underwater") || string8.equals("bottom")) {
                        customUnitConfig.drawLayer = 1;
                    } else if (string8.equals("ground")) {
                        customUnitConfig.drawLayer = 2;
                    } else if (string8.equals("ground2")) {
                        customUnitConfig.drawLayer = 3;
                    } else if (string8.equals("air")) {
                        customUnitConfig.drawLayer = 5;
                    } else if (string8.equals("top")) {
                        customUnitConfig.drawLayer = 10;
                    } else if (string8.equals("wreaks")) {
                        customUnitConfig.drawLayer = 0;
                    } else {
                        throw new RuntimeException("unknown drawLayer:" + string8);
                    }
                }
                customUnitConfig.shadowOffsetX = iniFile.getFloat("graphics", "shadowOffsetX", Float.valueOf(0.0f)).floatValue();
                customUnitConfig.shadowOffsetY = iniFile.getFloat("graphics", "shadowOffsetY", Float.valueOf(0.0f)).floatValue();
                customUnitConfig.rotateWithDirection = iniFile.getBoolean("graphics", "rotate_with_direction", (Boolean) true).booleanValue();
                customUnitConfig.lockBodyRotationWithMainTurret = iniFile.getBoolean("graphics", "lock_body_rotation_with_main_turret", (Boolean) false).booleanValue();
                customUnitConfig.lockShadowRotationWithMainTurret = iniFile.getBoolean("graphics", "lock_shadow_rotation_with_main_turret", Boolean.valueOf(customUnitConfig.lockBodyRotationWithMainTurret)).booleanValue();
                customUnitConfig.lockLegRotationWithMainTurret = iniFile.getBoolean("graphics", "lock_leg_rotation_with_main_turret", (Boolean) false).booleanValue();
                customUnitConfig.whenBeingBuiltMakeTransparentTill = iniFile.getFloat("graphics", "whenBeingBuiltMakeTransparentTill", Float.valueOf(1.0f)).floatValue();
                customUnitConfig.directionConfig = CustomUnitDirectionConfig.a(customUnitConfig, iniFile, "graphics", "animation_", false);
                for (String str10 : iniFile.getSectionsStartingWith("effect_")) {
                    EffectTemplate effectTemplate = new EffectTemplate(str10.substring("effect_".length()));
                    effectTemplate.a(customUnitConfig, iniFile, str10);
                    customUnitConfig.customEffects.add(effectTemplate);
                }
                for (EffectTemplate effectTemplate2 : customUnitConfig.customEffects) {
                    if (effectTemplate2.alsoEmitEffects != null) {
                        effectTemplate2.alsoEmitEffects.c();
                    }
                    if (effectTemplate2.alsoEmitEffectsOnDeath != null) {
                        effectTemplate2.alsoEmitEffectsOnDeath.c();
                    }
                    if (effectTemplate2.ifSpawnFailsEmitEffects != null) {
                        effectTemplate2.ifSpawnFailsEmitEffects.c();
                    }
                    if (effectTemplate2.trailEffect != null) {
                        effectTemplate2.trailEffect.c();
                    }
                }
                customUnitConfig.splashEffect = iniFile.getBoolean("graphics", "splastEffect", (Boolean) false).booleanValue();
                customUnitConfig.dustEffect = iniFile.getBoolean("graphics", "dustEffect", (Boolean) false).booleanValue();
                customUnitConfig.splashEffectReverse = iniFile.getBoolean("graphics", "splastEffectReverse", (Boolean) true).booleanValue();
                customUnitConfig.dustEffectReverse = iniFile.getBoolean("graphics", "dustEffectReverse", (Boolean) true).booleanValue();
                customUnitConfig.hasMovementEffects = customUnitConfig.dustEffect || customUnitConfig.splashEffect;
                String string9 = iniFile.getString("graphics", "movementEffect", (String) null);
                if (string9 != null) {
                    customUnitConfig.movementEffect = customUnitConfig.addConfigExtension(string9, null);
                    if (customUnitConfig.movementEffect != null && customUnitConfig.movementEffect.a()) {
                        customUnitConfig.hasMovementEffects = true;
                    }
                }
                String string10 = iniFile.getString("graphics", "movementEffectReverse", (String) null);
                if (string10 != null) {
                    customUnitConfig.movementEffectReverse = customUnitConfig.addConfigExtension(string10, (CustomUnitSpawnList) null);
                    if (customUnitConfig.movementEffectReverse != null && customUnitConfig.movementEffectReverse.a()) {
                        customUnitConfig.hasMovementEffects = true;
                    }
                }
                customUnitConfig.movementEffectRate = iniFile.getFloat("graphics", "movementEffectRate", Float.valueOf(11.0f)).floatValue();
                customUnitConfig.movementEffectReverseFlipEffects = iniFile.getBoolean("graphics", "movementEffectReverseFlipEffects", (Boolean) false).booleanValue();
                customUnitConfig.repairEffectRate = iniFile.getFloat("graphics", "repairEffectRate", Float.valueOf(5.0f)).floatValue();
                String string11 = iniFile.getString("graphics", "repairEffect", (String) null);
                if (string11 != null) {
                    customUnitConfig.repairEffect = customUnitConfig.addConfigExtension(string11, null);
                    if (customUnitConfig.repairEffect != null && customUnitConfig.repairEffect.b()) {
                        customUnitConfig.showActionsAndWaypoints = true;
                    }
                }
                String string12 = iniFile.getString("graphics", "repairEffectAtTarget", (String) null);
                if (string12 != null) {
                    customUnitConfig.repairEffectAtTarget = customUnitConfig.addConfigExtension(string12, (CustomUnitSpawnList) null);
                    if (customUnitConfig.repairEffectAtTarget != null && customUnitConfig.repairEffectAtTarget.b()) {
                        customUnitConfig.showActionsAndWaypoints = true;
                    }
                }
                customUnitConfig.reclaimEffectRate = iniFile.getFloat("graphics", "reclaimEffectRate", Float.valueOf(5.0f)).floatValue();
                String string13 = iniFile.getString("graphics", "reclaimEffect", (String) null);
                if (string13 != null) {
                    customUnitConfig.reclaimEffect = customUnitConfig.addConfigExtension(string13, (CustomUnitSpawnList) null);
                    if (customUnitConfig.reclaimEffect != null && customUnitConfig.reclaimEffect.b()) {
                        customUnitConfig.hasReclaimEffect = true;
                    }
                }
                String string14 = iniFile.getString("graphics", "reclaimEffectAtTarget", (String) null);
                if (string14 != null) {
                    customUnitConfig.reclaimEffectAtTarget = customUnitConfig.addConfigExtension(string14, (CustomUnitSpawnList) null);
                    if (customUnitConfig.reclaimEffectAtTarget != null && customUnitConfig.reclaimEffectAtTarget.b()) {
                        customUnitConfig.hasReclaimEffect = true;
                    }
                }
                customUnitConfig.movingAnimation.a(customUnitConfig, iniFile, "graphics", "animation_" + customUnitConfig.movingAnimation.animationName + "_");
                customUnitConfig.idleAnimation.a(customUnitConfig, iniFile, "graphics", "animation_" + customUnitConfig.idleAnimation.animationName + "_");
                customUnitConfig.attackAnimation.a(customUnitConfig, iniFile, "graphics", "animation_" + customUnitConfig.attackAnimation.animationName + "_");
                for (String str11 : iniFile.getSectionsStartingWith("animation_")) {
                    AnimationConfig animationConfig = new AnimationConfig(str11.substring("animation_".length()));
                    animationConfig.a(customUnitConfig, iniFile, str11, VariableScope.nullOrMissingString);
                    customUnitConfig.animations.add(animationConfig);
                }
                customUnitConfig.movingAnimation = customUnitConfig.resolveAnimationForAction(CustomUnitAction.move, customUnitConfig.movingAnimation, true);
                customUnitConfig.idleAnimation = customUnitConfig.resolveAnimationForAction(CustomUnitAction.idle, customUnitConfig.idleAnimation, true);
                customUnitConfig.attackAnimation = customUnitConfig.resolveAnimationForAction(CustomUnitAction.attack, customUnitConfig.attackAnimation, true);
                customUnitConfig.underConstructionAnimation = customUnitConfig.findAnimationForAction(CustomUnitAction.underConstruction);
                customUnitConfig.underConstructionWithLinkedBuiltTimeAnimation = customUnitConfig.findAnimationForAction(CustomUnitAction.underConstructionWithLinkedBuiltTime);
                if (customUnitConfig.underConstructionAnimation != null && customUnitConfig.underConstructionWithLinkedBuiltTimeAnimation != null) {
                    throw new RuntimeException("Cannot use underConstruction and underConstructionWithLinkedBuiltTime animations at the same time");
                }
                customUnitConfig.createdAnimation = customUnitConfig.findAnimationForAction(CustomUnitAction.created);
                customUnitConfig.queuedUnitsAnimation = customUnitConfig.findAnimationForAction(CustomUnitAction.queuedUnits);
                if (customUnitConfig.queuedUnitsAnimation != null) {
                    customUnitConfig.isFactory = true;
                }
                customUnitConfig.repairAnimation = customUnitConfig.findAnimationForAction(CustomUnitAction.repair);
                customUnitConfig.reclaimAnimation = customUnitConfig.findAnimationForAction(CustomUnitAction.reclaim);
                customUnitConfig.unitStats.maxHp = iniFile.getIntStrict("core", "maxHp");
                customUnitConfig.unitStats.maxShield = iniFile.getInt("core", "maxShield", (Integer) 0).intValue();
                customUnitConfig.startShieldAtZero = iniFile.getBoolean("core", "startShieldAtZero", (Boolean) false).booleanValue();
                customUnitConfig.unitStats.shieldRegen = iniFile.getFloat("core", "shieldRegen", Float.valueOf(0.25f)).floatValue();
                customUnitConfig.shieldDisplayOnlyDeflection = iniFile.getBoolean("core", "shieldDisplayOnlyDeflection", (Boolean) false).booleanValue();
                customUnitConfig.shieldDeflectionDisplayRate = iniFile.getFloat("core", "shieldDeflectionDisplayRate", Float.valueOf(4.0f)).floatValue();
                customUnitConfig.unitStats.armour = iniFile.getFloat("core", "armour", Float.valueOf(0.0f)).floatValue();
                customUnitConfig.armourMinDamageToKeep = iniFile.getFloat("core", "armourMinDamageToKeep", Float.valueOf(1.0f)).floatValue();
                customUnitConfig.unitStats.maxEnergy = iniFile.getFloat("core", "energyMax", Float.valueOf(0.0f)).floatValue();
                customUnitConfig.startEnergyAtZero = iniFile.getBoolean("core", "startEnergyAtZero", (Boolean) false).booleanValue();
                customUnitConfig.energyRegen = iniFile.getFloat("core", "energyRegen", Float.valueOf(0.0f)).floatValue();
                customUnitConfig.energyStartingPercentage = iniFile.getFloat("core", "energyStartingPercentage", Float.valueOf(1.0f)).floatValue();
                customUnitConfig.energyNeedsToRechargeToFull = iniFile.getBoolean("core", "energyNeedsToRechargeToFull", (Boolean) false).booleanValue();
                customUnitConfig.energyRegenWhenRecharging = iniFile.getFloat("core", "energyRegenWhenRecharging", Float.valueOf(customUnitConfig.energyRegen)).floatValue();
                customUnitConfig.energyDisplayName = getLocaleString(iniFile, "core", "energyDisplayName", (String) null);
                customUnitConfig.radius = iniFile.getIntStrict("core", "radius");
                customUnitConfig.displayRadius = iniFile.getInt("core", "displayRadius", Integer.valueOf(customUnitConfig.radius)).intValue();
                float f2 = customUnitConfig.radius;
                if (f2 < 6.0f) {
                    f2 = 6.0f;
                }
                customUnitConfig.uiTargetRadius = iniFile.getFloat("core", "uiTargetRadius", Float.valueOf(f2)).floatValue();
                customUnitConfig.shieldRenderRadius = iniFile.getInt("core", "shieldRenderRadius", Integer.valueOf(customUnitConfig.radius)).intValue();
                customUnitConfig.buildingSelectionOffset = iniFile.getInt("core", "buildingSelectionOffset", (Integer) 0).intValue();
                customUnitConfig.footprint = iniFile.getRect("core", "footprint", customUnitConfig.footprint);
                customUnitConfig.constructionFootprint = iniFile.getRect("core", "constructionFootprint", customUnitConfig.constructionFootprint);
                customUnitConfig.displayFootprint.a(customUnitConfig.footprint);
                customUnitConfig.displayFootprint = iniFile.getRect("core", "displayFootprint", customUnitConfig.displayFootprint);
                customUnitConfig.buildingToFootprintOffsetX = iniFile.getFloat("core", "buildingToFootprintOffsetX", Float.valueOf(10.0f)).floatValue();
                customUnitConfig.buildingToFootprintOffsetY = iniFile.getFloat("core", "buildingToFootprintOffsetY", Float.valueOf(10.0f)).floatValue();
                customUnitConfig.radius = (int) (customUnitConfig.radius * customUnitConfig.globalScale);
                customUnitConfig.displayRadius = (int) (customUnitConfig.displayRadius * customUnitConfig.globalScale);
                customUnitConfig.unitStats.fogOfWarSightRange = iniFile.getInt("core", "fogOfWarSightRange", (Integer) 15).intValue();
                customUnitConfig.fogOfWarSightRangeWhileNotBuilt = iniFile.getInt("core", "fogOfWarSightRangeWhileNotBuilt", (Integer) (-1)).intValue();
                customUnitConfig.exit_x = iniFile.getFloat("core", "exit_x", Float.valueOf(0.0f)).floatValue();
                customUnitConfig.exit_y = iniFile.getFloat("core", "exit_y", Float.valueOf(9.0f)).floatValue();
                customUnitConfig.exit_dirOffset = iniFile.getFloat("core", "exit_dirOffset", (Float) null);
                customUnitConfig.exit_heightOffset = iniFile.getFloat("core", "exit_heightOffset", Float.valueOf(0.0f)).floatValue();
                customUnitConfig.exitHeightIgnoreParent = iniFile.getBoolean("core", "exitHeightIgnoreParent", (Boolean) false).booleanValue();
                customUnitConfig.exit_moveAwayAmount = iniFile.getFloat("core", "exit_moveAwayAmount", Float.valueOf(70.0f));
                customUnitConfig.softCollisionOnAll = iniFile.getInt("core", "softCollisionOnAll", (Integer) 0).intValue();
                customUnitConfig.disableAllUnitCollisions = iniFile.getBoolean("core", "disableAllUnitCollisions", (Boolean) false).booleanValue();
                if (customUnitConfig.disableAllUnitCollisions) {
                    customUnitConfig.footprint.a(0, 0, -1, -1);
                }
                customUnitConfig.hideScorchMark = iniFile.getBoolean("core", "hideScorchMark", (Boolean) false).booleanValue();
                customUnitConfig.disableLowHpFire = iniFile.getBoolean("graphics", "disableLowHpFire", Boolean.valueOf(customUnitConfig.isBio)).booleanValue();
                customUnitConfig.disableLowHpSmoke = iniFile.getBoolean("graphics", "disableLowHpSmoke", Boolean.valueOf(customUnitConfig.isBio)).booleanValue();
                customUnitConfig.isBuildingUnit = iniFile.getBoolean("core", "isBuilding", (Boolean) false).booleanValue();
                customUnitConfig.ignoreInUnitCapCalculation = iniFile.getBoolean("core", "ignoreInUnitCapCalculation", Boolean.valueOf(customUnitConfig.isBuildingUnit)).booleanValue();
                customUnitConfig.placeOnlyOnResPool = iniFile.getBoolean("core", "placeOnlyOnResPool", (Boolean) false).booleanValue();
                customUnitConfig.isUnrepairableUnit = iniFile.getBoolean("core", "isUnrepairableUnit", (Boolean) false).booleanValue();
                customUnitConfig.extraBuildRangeWhenBuildingThis = iniFile.getFloat("core", "extraBuildRangeWhenBuildingThis", Float.valueOf(0.0f)).floatValue();
                customUnitConfig.isUnselectable = iniFile.getBoolean("core", "isUnselectable", (Boolean) false).booleanValue();
                customUnitConfig.isUnselectableAsTarget = iniFile.getBoolean("core", "isUnselectableAsTarget", Boolean.valueOf(customUnitConfig.isUnselectable)).booleanValue();
                customUnitConfig.showActionsWithMixedSelectionIfOtherUnitsHaveTag = iniFile.getAnimationSet(customUnitConfig, "core", "showActionsWithMixedSelectionIfOtherUnitsHaveTag", (AnimationSet) null);
                customUnitConfig.canNotBeDirectlyAttacked = iniFile.getBoolean("core", "canNotBeDirectlyAttacked", (Boolean) false).booleanValue();
                customUnitConfig.canNotBeDamaged = iniFile.getBoolean("core", "canNotBeDamaged", Boolean.valueOf(customUnitConfig.canNotBeDirectlyAttacked)).booleanValue();
                customUnitConfig.showOnMinimap = iniFile.getBoolean("core", "showOnMinimap", (Boolean) true).booleanValue();
                customUnitConfig.showOnMinimapToEnemies = iniFile.getBoolean("core", "showOnMinimapToEnemies", Boolean.valueOf(customUnitConfig.unitStats.isVisibleToEnemies)).booleanValue();
                customUnitConfig.deathAnimation = iniFile.getAnimationSet(customUnitConfig, "core", "canOnlyBeAttackedByUnitsWithTags", (AnimationSet) null);
                if (customUnitConfig.canNotBeDirectlyAttacked && customUnitConfig.deathAnimation != null) {
                    throw new RuntimeException("canNotBeDirectlyAttacked and canOnlyBeAttackedByUnitsWithTags cannot be used at the same time");
                }
                customUnitConfig.canNotBeGivenOrdersByPlayer = iniFile.getBoolean("core", "canNotBeGivenOrdersByPlayer", (Boolean) false).booleanValue();
                customUnitConfig.canRepairBuildings = iniFile.getBoolean("core", "canRepairBuildings", (Boolean) false).booleanValue();
                customUnitConfig.canRepairUnits = iniFile.getBoolean("core", "canRepairUnits", false).booleanValue();
                customUnitConfig.autoRepair = iniFile.getBoolean("core", "autoRepair", false).booleanValue();
                if (customUnitConfig.autoRepair) {
                    customUnitConfig.a(AutoRepairRenderHook.a);
                }
                customUnitConfig.unitStats.nanoRange = iniFile.getInt("core", "nanoRange", (Integer) (-1)).intValue();
                if (customUnitConfig.unitStats.nanoRange != -1) {
                    customUnitConfig.unitStats.nanoRange = (int) (customUnitConfig.unitStats.nanoRange * customUnitConfig.globalScale);
                }
                customUnitConfig.nanoRangeForRepairIsMelee = iniFile.getBoolean("core", "nanoRangeForRepairIsMelee", (Boolean) false).booleanValue();
                if (customUnitConfig.nanoRangeForRepairIsMelee) {
                    customUnitConfig.nanoRangeForRepair = 5;
                }
                int iIntValue2 = iniFile.getInt("core", "nanoRangeForRepair", (Integer) (-1)).intValue();
                if (iIntValue2 != -1) {
                    customUnitConfig.nanoRangeForRepair = iIntValue2;
                    customUnitConfig.nanoRangeForRepair = (int) (customUnitConfig.nanoRangeForRepair * customUnitConfig.globalScale);
                }
                customUnitConfig.nanoRangeForReclaimIsMelee = iniFile.getBoolean("core", "nanoRangeForReclaimIsMelee", (Boolean) false).booleanValue();
                if (customUnitConfig.nanoRangeForReclaimIsMelee) {
                    customUnitConfig.nanoRangeForReclaim = 5;
                }
                int iIntValue3 = iniFile.getInt("core", "nanoRangeForReclaim", (Integer) (-1)).intValue();
                if (iIntValue3 != -1) {
                    customUnitConfig.nanoRangeForReclaim = iIntValue3;
                    customUnitConfig.nanoRangeForReclaim = (int) (customUnitConfig.nanoRangeForReclaim * customUnitConfig.globalScale);
                }
                customUnitConfig.nanoRepairSpeed = iniFile.getFloat("core", "nanoRepairSpeed", Float.valueOf(0.2f)).floatValue();
                customUnitConfig.nanoReclaimSpeed = iniFile.getFloat("core", "nanoReclaimSpeed", Float.valueOf(customUnitConfig.nanoRepairSpeed * 5.1f)).floatValue();
                customUnitConfig.resourceReclaimMultiplier = iniFile.getFloat("core", "resourceReclaimMultiplier", Float.valueOf(1.0f)).floatValue();
                customUnitConfig.nanoUnbuildSpeed = iniFile.getFloat("core", "nanoUnbuildSpeed", Float.valueOf(1.0f)).floatValue() * 0.001f * 5.1f;
                customUnitConfig.nanoBuildSpeed = iniFile.getFloat("core", "nanoBuildSpeed", Float.valueOf(1.0f)).floatValue();
                customUnitConfig.unitStats.nanoFactorySpeed = iniFile.getFloat("core", "nanoFactorySpeed", Float.valueOf(1.0f)).floatValue();
                customUnitConfig.unitStats.selfRegenRate = iniFile.getFloat("core", "selfRegenRate", Float.valueOf(0.0f)).floatValue();
                customUnitConfig.selfBuildRate = iniFile.getInvertedTime("core", "selfBuildRate", Float.valueOf(0.0f)).floatValue();
                customUnitConfig.dieOnConstruct = iniFile.getBoolean("core", "dieOnConstruct", (Boolean) false).booleanValue();
                customUnitConfig.dieOnZeroEnergy = iniFile.getBoolean("core", "dieOnZeroEnergy", (Boolean) false).booleanValue();
                int i = 4;
                if (customUnitConfig.unitStats.mass > 30000.0f) {
                    i = 8;
                }
                if (customUnitConfig.isBuildingUnit) {
                    i = 7;
                }
                customUnitConfig.numBitsOnDeath = iniFile.getInt("core", "numBitsOnDeath", Integer.valueOf(i)).intValue();
                customUnitConfig.nukeOnDeath = iniFile.getBoolean("core", "nukeOnDeath", (Boolean) false).booleanValue();
                customUnitConfig.nukeOnDeathRange = iniFile.getFloat("core", "nukeOnDeathRange", Float.valueOf(250.0f)).floatValue();
                customUnitConfig.nukeOnDeathDamage = iniFile.getFloat("core", "nukeOnDeathDamage", Float.valueOf(5400.0f)).floatValue();
                customUnitConfig.nukeOnDeathDisableWhenNoNuke = iniFile.getBoolean("core", "nukeOnDeathDisableWhenNoNuke", (Boolean) false).booleanValue();
                customUnitConfig.fireOnDeath = iniFile.getInt("core", "fireOnDeath", (Integer) 0).intValue();
                customUnitConfig.explodeTypeOnDeath = (UnitSize) iniFile.getEnum("core", "explodeTypeOnDeath", (Enum) null, UnitSize.class);
                customUnitConfig.explodeOnDeath = iniFile.getBoolean("core", "explodeOnDeath", (Boolean) true).booleanValue();
                customUnitConfig.disableDeathOnZeroHp = iniFile.getBoolean("core", "disableDeathOnZeroHp", (Boolean) false).booleanValue();
                customUnitConfig.explodeOnDeathGroundCollision = iniFile.getBoolean("core", "explodeOnDeathGroundCollision", Boolean.valueOf(iniFile.getBoolean("core", "explodeOnDeathGroundCollosion", (Boolean) true).booleanValue())).booleanValue();
                customUnitConfig.effectOnDeath = customUnitConfig.addConfigExtension(iniFile.getString("core", "effectOnDeath", (String) null), (CustomUnitSpawnList) null);
                customUnitConfig.effectOnDeathIfUnbuilt = customUnitConfig.addConfigExtension(iniFile.getString("core", "effectOnDeathIfUnbuilt", (String) null), (CustomUnitSpawnList) null);
                customUnitConfig.soundOnDeath = SoundList.a(customUnitConfig, iniFile.getString("core", "soundOnDeath", (String) null));
                customUnitConfig.effectOnDeathGroundCollision = customUnitConfig.addConfigExtension(iniFile.getString("core", "effectOnDeathGroundCollision", iniFile.getString("core", "effectOnDeathGroundCollosion", (String) null)), (CustomUnitSpawnList) null);
                customUnitConfig.unitsSpawnedOnDeath = UnitSpawner.a(customUnitConfig, iniFile, "core", "unitsSpawnedOnDeath");
                customUnitConfig.unitsSpawnedOnDeath_setToTeamOfLastAttacker = iniFile.getBoolean("core", "unitsSpawnedOnDeath_setToTeamOfLastAttacker", (Boolean) false).booleanValue();
                customUnitConfig.canReclaimResources = iniFile.getBoolean("core", "canReclaimResources", false).booleanValue();
                customUnitConfig.canReclaimResourcesOnlyWithTags = iniFile.getAnimationSet(customUnitConfig, "core", "canReclaimResourcesOnlyWithTags", (AnimationSet) null);
                customUnitConfig.canReclaimResourcesNextSearchRange = iniFile.getInt("core", "canReclaimResourcesNextSearchRange", (Integer) 500).intValue();
                customUnitConfig.canReclaimUnitsOnlyWithTags = iniFile.getAnimationSet(customUnitConfig, "core", "canReclaimUnitsOnlyWithTags", (AnimationSet) null);
                customUnitConfig.canRepairUnitsOnlyWithTags = iniFile.getAnimationSet(customUnitConfig, "core", "canRepairUnitsOnlyWithTags", (AnimationSet) null);
                if (customUnitConfig.canReclaimUnitsOnlyWithTags != null && !customUnitConfig.canRepairUnits && !customUnitConfig.canRepairBuildings) {
                    throw new RuntimeException("canReclaimUnitsOnlyWithTags requires canRepairUnits:true or canRepairBuildings:true");
                }
                if (customUnitConfig.canRepairUnitsOnlyWithTags != null && !customUnitConfig.canRepairUnits && !customUnitConfig.canRepairBuildings) {
                    throw new RuntimeException("canRepairUnitsOnlyWithTags requires canRepairUnits:true or canRepairBuildings:true");
                }
                customUnitConfig.maxTransportingUnits = iniFile.getInt("core", "maxTransportingUnits", (Integer) 0).intValue();
                if (customUnitConfig.maxTransportingUnits < 0) {
                    throw new RuntimeException("maxTransportingUnits cannot be < 0");
                }
                customUnitConfig.transportUnitsUnloadDelayBetweenEachUnit = iniFile.getTime("core", "transportUnitsUnloadDelayBetweenEachUnit", Float.valueOf(30.0f)).floatValue();
                customUnitConfig.transportUnitsRequireTag = AnimationTag.a(iniFile.getString("core", "transportUnitsRequireTag", (String) null));
                String string15 = iniFile.getString("core", "transportUnitsRequireMovementType", (String) null);
                if (string15 != null) {
                    for (String str12 : string15.split(",")) {
                        customUnitConfig.transportUnitsRequireMovementType.add(UnitMovementType.a(str12.trim(), "transportUnitsRequireMovementType"));
                    }
                }
                customUnitConfig.transportUnitsEachUnitAlwaysUsesSingleSlot = iniFile.getBoolean("core", "transportUnitsEachUnitAlwaysUsesSingleSlot", (Boolean) false).booleanValue();
                customUnitConfig.transportUnitsBlockAirAndWaterUnits = iniFile.getBoolean("core", "transportUnitsBlockAirAndWaterUnits", Boolean.valueOf(customUnitConfig.transportUnitsRequireMovementType.size() == 0)).booleanValue();
                customUnitConfig.transportUnitsBlockOtherTransports = iniFile.getBoolean("core", "transportUnitsBlockOtherTransports", (Boolean) true).booleanValue();
                customUnitConfig.transportUnitsKeepBuiltUnits = iniFile.getLogicBoolean(customUnitConfig, "core", "transportUnitsKeepBuiltUnits", LogicBoolean.falseBoolean);
                customUnitConfig.transportUnitsKillOnDeath = iniFile.getLogicBoolean(customUnitConfig, "core", "transportUnitsKillOnDeath", LogicBoolean.trueBoolean);
                customUnitConfig.transportUnitsKeepWaypoints = iniFile.getLogicBoolean(customUnitConfig, "core", "transportUnitsKeepWaypoints", LogicBoolean.falseBoolean);
                customUnitConfig.transportUnitsHealBy = iniFile.getFloat("core", "transportUnitsHealBy", Float.valueOf(0.0f)).floatValue();
                customUnitConfig.transportUnitsCanUnloadUnits = iniFile.getLogicBoolean(customUnitConfig, "core", "transportUnitsCanUnloadUnits", (LogicBoolean) null);
                if (customUnitConfig.transportUnitsCanUnloadUnits != null) {
                    customUnitConfig.transportUnitsCanUnloadCondition = customUnitConfig.transportUnitsCanUnloadUnits;
                } else {
                    customUnitConfig.transportUnitsCanUnloadUnits = CustomUnitConfig.logic_notOverLiquidAndNotMoving;
                    customUnitConfig.transportUnitsCanUnloadCondition = CustomUnitConfig.logic_notOverLiquid;
                }
                customUnitConfig.transportUnitsAddUnloadOption = iniFile.getBoolean("core", "transportUnitsAddUnloadOption", Boolean.valueOf(customUnitConfig.transportUnitsCanUnloadUnits != LogicBoolean.falseBoolean)).booleanValue();
                customUnitConfig.transportUnitsOnTeamChangeKeepCurrentTeam = iniFile.getBoolean("core", "transportUnitsOnTeamChangeKeepCurrentTeam", Boolean.valueOf(customUnitConfig.transportUnitsOnTeamChangeKeepCurrentTeam)).booleanValue();
                customUnitConfig.transportSlotsNeeded = iniFile.getInt("core", "transportSlotsNeeded", (Integer) 1).intValue();
                for (int i2 = -1; i2 <= 29; i2++) {
                    String str13 = "builtFrom_" + i2 + "_";
                    if (i2 == -1) {
                        str13 = "builtFrom_";
                    }
                    String str14 = str13 + "name";
                    String string16 = iniFile.getString("core", str14, (String) null);
                    if (string16 != null) {
                        for (String str15 : string16.split(",")) {
                            String strTrim3 = str15.trim();
                            if (!strTrim3.equals(VariableScope.nullOrMissingString)) {
                                CustomUnitTrigger customUnitTrigger = new CustomUnitTrigger();
                                customUnitTrigger.triggerName = strTrim3;
                                customUnitTrigger.delay = iniFile.getFloat("core", str13 + "pos", Float.valueOf(999.0f)).floatValue();
                                customUnitTrigger.enabled = iniFile.getBoolean("core", str13 + "forceNano", (Boolean) false).booleanValue();
                                customUnitTrigger.condition = "[core]" + str14;
                                customUnitTrigger.logicCondition = iniFile.getLogicBoolean(customUnitConfig, "core", str13 + "isLocked", (LogicBoolean) null);
                                customUnitTrigger.action = getLocaleString(iniFile, "core", str13 + "isLockedMessage", (String) null);
                                if (customUnitTrigger.logicCondition == LogicBoolean.falseBoolean) {
                                    customUnitTrigger.logicCondition = null;
                                }
                                if (!"NONE".equalsIgnoreCase(strTrim3)) {
                                    customUnitConfig.customUnitTriggers.add(customUnitTrigger);
                                }
                            }
                        }
                    }
                }
                for (int i3 = 0; i3 <= 50; i3++) {
                    if (iniFile.getString("core", "canBuild_" + i3 + "_name", (String) null) != null) {
                        parseCanBuildEntry(customUnitConfig, iniFile, "core", "canBuild_" + i3 + "_", false);
                    }
                }
                for (String o : iniFile.getSectionsStartingWith("canBuild_")) {
                    parseCanBuildEntry(customUnitConfig, iniFile, o, VariableScope.nullOrMissingString, true);
                }
                customUnitConfig.customUnitMetadata = PlacementRules.a(customUnitConfig, iniFile);
                customUnitConfig.movementType = UnitMovementType.a(iniFile.getValueStrict("movement", "movementType"), "movementType");
                if (!customUnitConfig.isBuildingUnit) {
                    customUnitConfig.effectiveMovementType = customUnitConfig.movementType;
                } else {
                    customUnitConfig.effectiveMovementType = UnitMovementType.NONE;
                }
                Boolean bool = iniFile.getBoolean("ai", "useAsBuilder", (Boolean) null);
                customUnitConfig.useAsAttacker = iniFile.getBoolean("ai", "useAsAttacker", (Boolean) true).booleanValue();
                Boolean bool2 = iniFile.getBoolean("core", "isBuilder", (Boolean) null);
                if (bool2 == null) {
                    bool2 = bool == null ? false : bool;
                } else if (bool == null) {
                    bool = bool2;
                }
                if (bool == null) {
                    bool = false;
                }
                customUnitConfig.isBuilder = bool2.booleanValue();
                customUnitConfig.useAsBuilder = bool.booleanValue();
                if (!customUnitConfig.isBuilder && customUnitConfig.useAsBuilder) {
                    throw new RuntimeException("Cannot tell AI to use a non-builder as builder [ai]useAsBuilder:" + customUnitConfig.useAsBuilder + " [core]isBuilder:" + customUnitConfig.isBuilder);
                }
                if (customUnitConfig.canReclaimResources) {
                    customUnitConfig.useAsHarvester = true;
                }
                Boolean bool3 = iniFile.getBoolean("ai", "useAsHarvester", (Boolean) null);
                if (bool3 != null) {
                    customUnitConfig.useAsHarvester = bool3.booleanValue();
                }
                Boolean boolValueOf = iniFile.getBoolean("ai", "useAsTransport", (Boolean) null);
                if (boolValueOf == null) {
                    boolValueOf = Boolean.valueOf((customUnitConfig.maxTransportingUnits <= 0 || customUnitConfig.useAsBuilder || customUnitConfig.isBuildingUnit) ? false : true);
                    if (!customUnitConfig.transportUnitsAddUnloadOption) {
                        boolValueOf = false;
                    }
                }
                customUnitConfig.useAsTransport = boolValueOf.booleanValue();
                if (customUnitConfig.isBuildingUnit) {
                    customUnitConfig.teamColoredIconTextures = BaseBuilding.teamColoredIconTextures;
                } else if (customUnitConfig.movementType == UnitMovementType.AIR) {
                    customUnitConfig.teamColoredIconTextures = AirUnit.n;
                } else if (customUnitConfig.movementType == UnitMovementType.WATER) {
                    customUnitConfig.teamColoredIconTextures = WaterUnit.waterUnitIconTextures;
                } else if (customUnitConfig.movementType == UnitMovementType.HOVER) {
                    if (customUnitConfig.experimental) {
                        customUnitConfig.teamColoredIconTextures = LandUnit.landUnitIconTexturesExp;
                    } else if (customUnitConfig.l()) {
                        customUnitConfig.teamColoredIconTextures = BuilderUnit.builderIconTexture_teamColors;
                    } else {
                        customUnitConfig.teamColoredIconTextures = HoverLandUnit.n;
                    }
                } else if (customUnitConfig.experimental) {
                    customUnitConfig.teamColoredIconTextures = LandUnit.landUnitIconTexturesExp;
                } else if (customUnitConfig.l()) {
                    customUnitConfig.teamColoredIconTextures = BuilderUnit.builderIconTexture_teamColors;
                } else {
                    customUnitConfig.teamColoredIconTextures = LandUnit.landUnitIconTextures;
                }
                Texture textureA2 = customUnitConfig.a(iniFile, "graphics", "icon_zoomed_out", false);
                if (textureA2 != null) {
                    customUnitConfig.teamColoredIconTextures = customUnitConfig.a(textureA2, customUnitConfig.teamColoringMode);
                }
                if (iniFile.getBoolean("graphics", "icon_zoomed_out_neverShow", (Boolean) false).booleanValue()) {
                    customUnitConfig.teamColoredIconTextures = null;
                }
                customUnitConfig.showHealthBar = iniFile.getBoolean("graphics", "showHealthBar", (Boolean) true).booleanValue();
                customUnitConfig.showHealthBarChanges = iniFile.getBoolean("graphics", "showHealthBarChanges", (Boolean) true).booleanValue();
                customUnitConfig.showEnergyBar = iniFile.getBoolean("graphics", "showEnergyBar", (Boolean) true).booleanValue();
                customUnitConfig.showShotDelayBar = iniFile.getBoolean("graphics", "showShotDelayBar", (Boolean) true).booleanValue();
                customUnitConfig.showTransportBar = iniFile.getBoolean("graphics", "showTransportBar", (Boolean) true).booleanValue();
                customUnitConfig.showShieldBar = iniFile.getBoolean("graphics", "showShieldBar", (Boolean) true).booleanValue();
                customUnitConfig.showQueueBar = iniFile.getBoolean("graphics", "showQueueBar", (Boolean) true).booleanValue();
                customUnitConfig.showSelectionIndicator = iniFile.getBoolean("graphics", "showSelectionIndicator", (Boolean) true).booleanValue();
                customUnitConfig.slowDeathFall = iniFile.getBoolean("movement", "slowDeathFall", (Boolean) false).booleanValue();
                customUnitConfig.slowDeathFallSmoke = iniFile.getBoolean("movement", "slowDeathFallSmoke", (Boolean) true).booleanValue();
                customUnitConfig.unitStats.moveSpeed = iniFile.getFloat("movement", "moveSpeed", Float.valueOf(1.0f)).floatValue() * customUnitConfig.globalScale;
                customUnitConfig.moveAccelerationSpeed = iniFile.getFloat("movement", "moveAccelerationSpeed", Float.valueOf(1.0f)).floatValue() * customUnitConfig.globalScale;
                customUnitConfig.moveDecelerationSpeed = iniFile.getFloat("movement", "moveDecelerationSpeed", Float.valueOf(1.0f)).floatValue() * customUnitConfig.globalScale;
                Boolean bool4 = iniFile.getBoolean("movement", "ignoreMoveOrders", (Boolean) null);
                if (customUnitConfig.isBuildingUnit) {
                    customUnitConfig.ignoreMoveOrders = true;
                }
                if (bool4 != null) {
                    if (bool4.booleanValue()) {
                        customUnitConfig.ignoreMoveOrders = true;
                        if (customUnitConfig.unitStats.moveSpeed > 0.0f) {
                            throw new RuntimeException("[movement]ignoreMoveOrders expects moveSpeed=0");
                        }
                    } else if (customUnitConfig.isBuildingUnit) {
                        throw new RuntimeException("[movement]ignoreMoveOrders=false not yet supported on buildings");
                    }
                }
                customUnitConfig.moveYAxisScaling = iniFile.getFloat("movement", "moveYAxisScaling", Float.valueOf(1.0f)).floatValue();
                if (customUnitConfig.moveYAxisScaling <= 0.0f) {
                    throw new RuntimeException("[movement]moveYAxisScaling must be > 0");
                }
                customUnitConfig.inverseMoveYAxisScaling = 1.0f / customUnitConfig.moveYAxisScaling;
                customUnitConfig.reverseSpeedPercentage = iniFile.getFloat("movement", "reverseSpeedPercentage", Float.valueOf(0.6f)).floatValue();
                String string17 = iniFile.getString("movement", "landOnGround", "false");
                if (string17.equalsIgnoreCase("false")) {
                    customUnitConfig.landOnGround = false;
                } else if (string17.equalsIgnoreCase("onlyIdle")) {
                    customUnitConfig.landOnGround = true;
                    customUnitConfig.landOnGroundOnlyIdle = true;
                } else if (string17.equalsIgnoreCase("true")) {
                    customUnitConfig.landOnGround = true;
                } else {
                    throw new RuntimeException("landOnGround expected:true, false, onlyIdle, not:" + string17);
                }
                float f3 = 0.0f;
                float f4 = 0.0f;
                if (customUnitConfig.movementType == UnitMovementType.AIR) {
                    f3 = 35.0f;
                    f4 = 1.5f;
                }
                customUnitConfig.startingHeightOffset = iniFile.getFloat("movement", "startingHeightOffset", Float.valueOf(0.0f)).floatValue();
                customUnitConfig.unitStats.targetHeight = iniFile.getFloat("movement", "targetHeight", Float.valueOf(f3)).floatValue();
                customUnitConfig.targetHeightDrift = iniFile.getFloat("movement", "targetHeightDrift", Float.valueOf(f4)).floatValue();
                if (customUnitConfig.unitStats.targetHeight > 80.0f) {
                    customUnitConfig.isHover = true;
                }
                customUnitConfig.heightChangeRate = iniFile.getFloat("movement", "heightChangeRate", Float.valueOf(customUnitConfig.heightChangeRate)).floatValue();
                customUnitConfig.fallingAcceleration = iniFile.getFloat("movement", "fallingAcceleration", Float.valueOf(customUnitConfig.fallingAcceleration)).floatValue();
                customUnitConfig.fallingAccelerationDead = iniFile.getFloat("movement", "fallingAccelerationDead", Float.valueOf(customUnitConfig.fallingAccelerationDead)).floatValue();
                customUnitConfig.unitStats.maxTurnSpeed = iniFile.getFloat("movement", "maxTurnSpeed", Float.valueOf(1.0f)).floatValue();
                customUnitConfig.turnAcceleration = iniFile.getFloat("movement", "turnAcceleration", Float.valueOf(1.0f)).floatValue();
                customUnitConfig.moveSlidingMode = iniFile.getBoolean("movement", "moveSlidingMode", (Boolean) false).booleanValue();
                customUnitConfig.moveIgnoringBody = iniFile.getBoolean("movement", "moveIgnoringBody", (Boolean) false).booleanValue();
                customUnitConfig.moveSlidingDir = iniFile.getInt("movement", "moveSlidingDir", (Integer) (-1)).intValue();
                customUnitConfig.joinsGroupFormations = iniFile.getBoolean("movement", "joinsGroupFormations", (Boolean) true).booleanValue();
                customUnitConfig.turretSize = iniFile.getFloat("attack", "turretSize", Float.valueOf(1.0f)).floatValue() * customUnitConfig.globalScale;
                customUnitConfig.turretTurnSpeed = iniFile.getFloat("attack", "turretTurnSpeed", Float.valueOf(8.0f)).floatValue();
                customUnitConfig.turretRotateWithBody = iniFile.getBoolean("attack", "turretRotateWithBody", (Boolean) true).booleanValue();
                String string18 = iniFile.getString("attack", "attackMovement", "normal");
                customUnitConfig.attackMovementType = UnitBehaviorType.normal;
                if (string18.equalsIgnoreCase("normal")) {
                    customUnitConfig.attackMovementType = UnitBehaviorType.normal;
                }
                if (string18.equalsIgnoreCase("strafing")) {
                    customUnitConfig.attackMovementType = UnitBehaviorType.strafing;
                }
                if (string18.equalsIgnoreCase("bomber")) {
                    customUnitConfig.attackMovementType = UnitBehaviorType.bomber;
                }
                customUnitConfig.disablePassiveTargeting = iniFile.getBoolean("attack", "disablePassiveTargeting", (Boolean) false).booleanValue();
                customUnitConfig.stopTargetingAfterFiring = iniFile.getBoolean("attack", "stopTargetingAfterFiring", (Boolean) false).booleanValue();
                customUnitConfig.turretMultiTargeting = iniFile.getBoolean("attack", "turretMultiTargeting", (Boolean) false).booleanValue();
                customUnitConfig.attackMovementSpeed = iniFile.getFloat("attack", "attackMovementSpeed", Float.valueOf(1.0f)).floatValue();
                customUnitConfig.attackMovementSpread = iniFile.getFloat("attack", "attackMovementSpread", Float.valueOf(1.0f)).floatValue();
                Float f5 = iniFile.getFloat("attack", "maxAttackRange", (Float) null);
                if (f5 != null) {
                    z = true;
                    customUnitConfig.unitStats.maxAttackRange = f5 * customUnitConfig.globalScale;
                } else {
                    z = false;
                    customUnitConfig.unitStats.maxAttackRange = 100.0f * customUnitConfig.globalScale;
                }
                customUnitConfig.aimOffsetSpread = iniFile.getFloat("attack", "aimOffsetSpread", Float.valueOf(0.6f)).floatValue();
                customUnitConfig.shootDelay = iniFile.getTime("attack", "shootDelay", Float.valueOf(50.0f)).floatValue();
                customUnitConfig.unitStats.shootDelayMultiplier = iniFile.getFloat("attack", "shootDelayMultiplier", Float.valueOf(1.0f)).floatValue();
                customUnitConfig.unitStats.shootDamageMultiplier = iniFile.getFloat("attack", "shootDamageMultiplier", Float.valueOf(1.0f)).floatValue();
                customUnitConfig.showRangeUIGuide = iniFile.getBoolean("attack", "showRangeUIGuide", (Boolean) null);
                customUnitConfig.isMelee = iniFile.getBoolean("attack", "isMelee", (Boolean) false).booleanValue();
                customUnitConfig.meleeAttackRange = 0.0f;
                Float f6 = iniFile.getFloat("attack", "meleeEngangementDistance", (Float) null);
                if (customUnitConfig.isMelee) {
                    customUnitConfig.meleeAttackRange = 250.0f;
                    if (f6 != null) {
                        customUnitConfig.meleeAttackRange = f6.floatValue();
                    }
                } else if (f6 != null) {
                    throw new RuntimeException("[attack]meleeEngangementDistance can only be used with isMelee:true");
                }
                recordLoadPhaseTime(jA, LoadPhase.unitParsePartA);
                for (String str16 : iniFile.getSectionsStartingWith("projectile_")) {
                    String strSubstring2 = str16.substring("projectile_".length());
                    if (customUnitConfig.findProjectileTemplateByName(strSubstring2) != null) {
                        throw new RuntimeException("Two projectiles found with the same name:" + strSubstring2);
                    }
                    CustomProjectileTemplate customProjectileTemplate = new CustomProjectileTemplate();
                    customProjectileTemplate.projectileName = strSubstring2;
                    customProjectileTemplate.customUnitConfig = customUnitConfig;
                    CustomProjectileTemplate.a(customProjectileTemplate, customUnitConfig, iniFile, str16);
                }
                int size = customUnitConfig.projectileTemplates.size();
                if (size < 1) {
                    size = 1;
                }
                customUnitConfig.projectileTemplatesById = new CustomProjectileTemplate[size];
                for (int i4 = 0; i4 < customUnitConfig.projectileTemplates.size(); i4++) {
                    CustomProjectileTemplate customProjectileTemplate2 = customUnitConfig.projectileTemplates.get(i4);
                    customProjectileTemplate2.projectileId = i4;
                    customUnitConfig.projectileTemplatesById[i4] = customProjectileTemplate2;
                }
                for (int i5 = 0; i5 < customUnitConfig.projectileTemplatesById.length; i5++) {
                    CustomProjectileTemplate customProjectileTemplate3 = customUnitConfig.projectileTemplatesById[i5];
                    if (customProjectileTemplate3 != null) {
                        customProjectileTemplate3.w *= customUnitConfig.globalScale;
                        customProjectileTemplate3.targetSpeed *= customUnitConfig.globalScale;
                        customProjectileTemplate3.drawSize *= customUnitConfig.globalScale;
                    }
                }
                if (customUnitConfig.projectileTemplatesById[0] == null) {
                    CustomProjectileTemplate customProjectileTemplate4 = new CustomProjectileTemplate();
                    customProjectileTemplate4.projectileId = 0;
                    customProjectileTemplate4.projectileName = "1";
                    customProjectileTemplate4.b = 10;
                    customUnitConfig.projectileTemplates.add(customProjectileTemplate4);
                    customUnitConfig.projectileTemplatesById[0] = customProjectileTemplate4;
                }
                ArrayList<TurretConfig> arrayList = customUnitConfig.projectileConfigs;
                for (String str17 : iniFile.getSectionsStartingWith("turret_")) {
                    String strSubstring3 = str17.substring("turret_".length());
                    if (customUnitConfig.findProjectileConfigByName(strSubstring3) != null) {
                        throw new RuntimeException("Two turrets found with the same name:" + strSubstring3);
                    }
                    TurretConfig turretConfig = new TurretConfig();
                    turretConfig.name = strSubstring3;
                    turretConfig.copyFrom = str17;
                    arrayList.add(turretConfig);
                }
                for (TurretConfig turretConfig2 : arrayList) {
                    TurretConfig.a(turretConfig2, customUnitConfig, iniFile, turretConfig2.copyFrom);
                }
                if (arrayList.isEmpty()) {
                    TurretConfig turretConfig3 = new TurretConfig();
                    turretConfig3.offsetX = 0.0f;
                    turretConfig3.offsetY = 0.0f;
                    turretConfig3.name = "1";
                    turretConfig3.m = customUnitConfig.shootDelay;
                    arrayList.add(turretConfig3);
                }
                for (int size2 = arrayList.size() - 1; size2 >= 0; size2--) {
                    if (arrayList.get(size2) != null) {
                        arrayList.get(size2).turretIndex = size2;
                    }
                }
                for (int size3 = arrayList.size() - 1; size3 >= 0; size3--) {
                    if (arrayList.get(size3) != null) {
                        TurretConfig turretConfig4 = arrayList.get(size3);
                        if (turretConfig4.parentTurret != null) {
                            turretConfig4.linkedTurretIndex = turretConfig4.parentTurret.turretIndex;
                            if (turretConfig4.parentTurret.parentTurret != null) {
                                throw new RuntimeException(turretConfig4.name + ": Turret can not be attached to turret that is also attached to a turret");
                            }
                        }
                        if (turretConfig4.childTurret != null) {
                            turretConfig4.x = turretConfig4.childTurret.turretIndex;
                        }
                        if (turretConfig4.turnSpeedDeceleration < 0.0f) {
                            turretConfig4.turnSpeedDeceleration = turretConfig4.turnSpeedAcceleration;
                        }
                    }
                }
                if (arrayList.size() > 31) {
                    throw new RuntimeException("Turret max count per unit is: 31");
                }
                customUnitConfig.turrets = arrayList.toArray(new TurretConfig[0]);
                customUnitConfig.maxAttackRange = customUnitConfig.unitStats.maxAttackRange;
                float f7 = -1.0f;
                boolean z3 = true;
                boolean z4 = false;
                for (TurretConfig turretConfig5 : arrayList) {
                    turretConfig5.barrelY *= customUnitConfig.globalScale;
                    turretConfig5.offsetX *= customUnitConfig.globalScale;
                    turretConfig5.offsetY *= customUnitConfig.globalScale;
                    turretConfig5.barrelX *= customUnitConfig.globalScale;
                    turretConfig5.barrelOffsetXOnOddShots *= customUnitConfig.globalScale;
                    boolean zBooleanValue2 = false;
                    if (turretConfig5.canShoot) {
                        if (turretConfig5.limitingRange >= 99999.0f) {
                            z3 = false;
                        } else {
                            z4 = true;
                            if (customUnitConfig.maxAttackRange > turretConfig5.limitingRange) {
                                customUnitConfig.maxAttackRange = turretConfig5.limitingRange;
                            }
                            if (f7 < turretConfig5.limitingRange) {
                                f7 = turretConfig5.limitingRange;
                            }
                            if (Utility.abs(turretConfig5.limitingRange - customUnitConfig.unitStats.maxAttackRange) > 5.0f) {
                                boolean z5 = false;
                                Iterator it4 = customUnitConfig.customArms.iterator();
                                while (it4.hasNext()) {
                                    if (Utility.abs(turretConfig5.limitingRange - ((CustomLimitedRange) it4.next()).rangeValue) < 5.0f) {
                                        z5 = true;
                                    }
                                }
                                if (!z5) {
                                    zBooleanValue2 = true;
                                }
                            }
                        }
                    }
                    if (turretConfig5.showRangeUIGuide != null) {
                        zBooleanValue2 = turretConfig5.showRangeUIGuide.booleanValue();
                    }
                    if (zBooleanValue2) {
                        CustomLimitedRange customLimitedRange = new CustomLimitedRange();
                        customLimitedRange.rangeValue = turretConfig5.limitingRange;
                        customUnitConfig.customArms.add(customLimitedRange);
                    }
                }
                if (z4 && z3) {
                    if (!z) {
                        customUnitConfig.unitStats.maxAttackRange = f7;
                    } else if (f7 < customUnitConfig.unitStats.maxAttackRange) {
                        throw new RuntimeException("limitingRange as been applied to all turrets but is less than maxAttackRange (hint: unset maxAttackRange or a limitingRange, or make values match)");
                    }
                }
                String string19 = iniFile.getString("attack", "setMainTurretAs", (String) null);
                if (string19 != null) {
                    customUnitConfig.mainTurret = customUnitConfig.findProjectileConfigByName(string19);
                    if (customUnitConfig.mainTurret == null) {
                        throw new RuntimeException("[attack] Could not find setMainTurretAs with name: " + string19);
                    }
                } else {
                    customUnitConfig.mainTurret = customUnitConfig.findProjectileConfigByName("1");
                    if (customUnitConfig.mainTurret == null) {
                        customUnitConfig.mainTurret = customUnitConfig.turrets[0];
                    }
                }
                customUnitConfig.mainTurretIndex = customUnitConfig.mainTurret.turretIndex;
                recordLoadPhaseTime(jA, LoadPhase.unitParsePartB);
                long jA3 = PerformanceProfiler.a();
                if (iniFile.hasKeyStartingWith("core", "action_")) {
                    for (int i6 = 0; i6 <= 50; i6++) {
                        parseCustomActionDef(customUnitConfig, iniFile, "core", "action_" + i6 + "_", VariableScope.nullOrMissingString + i6, false, false);
                    }
                }
                for (String str18 : iniFile.getSectionsStartingWith("action_")) {
                    String strSubstring4 = str18.substring("action_".length());
                    if (customUnitConfig.findCustomActionDefByDisplayName(strSubstring4) != null) {
                        throw new RuntimeException("Two actions found with the same name:" + strSubstring4);
                    }
                    parseCustomActionDef(customUnitConfig, iniFile, str18, VariableScope.nullOrMissingString, strSubstring4, true, false);
                }
                for (String str19 : iniFile.getSectionsStartingWith("hiddenAction_")) {
                    String strSubstring5 = str19.substring("hiddenAction_".length());
                    if (customUnitConfig.findCustomActionDefByDisplayName(strSubstring5) != null) {
                        throw new RuntimeException("Two actions found with the same name:" + strSubstring5);
                    }
                    parseCustomActionDef(customUnitConfig, iniFile, str19, VariableScope.nullOrMissingString, strSubstring5, true, true);
                }
                recordLoadPhaseTime(jA3, LoadPhase.actionParse);
                ArrayList<LegConfig> arrayList2 = new ArrayList();
                ArrayList<LegConfig> arrayList3 = new ArrayList();
                int i7 = 0;
                while (i7 <= 1) {
                    boolean z6 = i7 == 0;
                    ArrayList arrayList4 = z6 ? arrayList2 : arrayList3;
                    for (int i8 = 1; i8 < 21; i8++) {
                        String str20 = z6 ? "leg_" + i8 : "arm_" + i8;
                        if (iniFile.isSectionNotEmpty(str20)) {
                            LegConfig legConfig = new LegConfig();
                            LegConfig.a(legConfig, customUnitConfig, iniFile, str20, z6, arrayList4);
                            arrayList4.add(legConfig);
                        } else {
                            arrayList4.add(null);
                        }
                    }
                    i7++;
                }
                ArrayList arrayList5 = new ArrayList();
                for (LegConfig legConfig2 : arrayList2) {
                    if (legConfig2 != null) {
                        arrayList5.add(legConfig2);
                    }
                }
                for (LegConfig legConfig3 : arrayList3) {
                    if (legConfig3 != null) {
                        arrayList5.add(legConfig3);
                    }
                }
                for (int size4 = arrayList5.size() - 1; size4 >= 0; size4--) {
                    ((LegConfig) arrayList5.get(size4)).a = size4;
                }
                customUnitConfig.legConfig = (LegConfig[]) arrayList5.toArray(new LegConfig[0]);
                if (customUnitConfig.legConfig.length > 0) {
                    customUnitConfig.a(CustomUnitLegController.a);
                }
                Iterator it5 = customUnitConfig.animations.iterator();
                while (it5.hasNext()) {
                    ((AnimationConfig) it5.next()).a(customUnitConfig);
                }
                computeLegAdjacency(customUnitConfig);
                String string20 = iniFile.getString("core", "fireTurretXAtSelfOnDeath", (String) null);
                if (string20 != null && !"NONE".equalsIgnoreCase(string20)) {
                    TurretConfig turretConfigFindProjectileConfigByName = customUnitConfig.findProjectileConfigByName(string20);
                    if (turretConfigFindProjectileConfigByName == null) {
                        throw new RuntimeException("Cannot find turret:" + string20 + " for [core]fireTurretXAtSelfOnDeath");
                    }
                    customUnitConfig.fireTurretAtSelfOnDeathIndex = turretConfigFindProjectileConfigByName.turretIndex;
                }
                CustomUnitDecalRenderer.a(customUnitConfig, iniFile);
                customUnitConfig.dieOnAttack = iniFile.getBoolean("attack", "dieOnAttack", (Boolean) false).booleanValue();
                customUnitConfig.removeOnAttack = iniFile.getBoolean("attack", "removeOnAttack", (Boolean) false).booleanValue();
                customUnitConfig.canAttack = iniFile.getBooleanStrict("attack", "canAttack");
                if (customUnitConfig.canAttack) {
                    customUnitConfig.canAttackFlyingUnits = iniFile.getLogicBoolean(customUnitConfig, "attack", "canAttackFlyingUnits");
                    customUnitConfig.canAttackLandUnits = iniFile.getLogicBoolean(customUnitConfig, "attack", "canAttackLandUnits");
                    customUnitConfig.canAttackUnderwaterUnits = iniFile.getLogicBoolean(customUnitConfig, "attack", "canAttackUnderwaterUnits");
                } else {
                    customUnitConfig.canAttackFlyingUnits = iniFile.getLogicBoolean(customUnitConfig, "attack", "canAttackFlyingUnits", LogicBoolean.falseBoolean);
                    customUnitConfig.canAttackLandUnits = iniFile.getLogicBoolean(customUnitConfig, "attack", "canAttackLandUnits", LogicBoolean.falseBoolean);
                    customUnitConfig.canAttackUnderwaterUnits = iniFile.getLogicBoolean(customUnitConfig, "attack", "canAttackUnderwaterUnits", LogicBoolean.falseBoolean);
                }
                customUnitConfig.canAttackNotTouchingWaterUnits = iniFile.getLogicBoolean(customUnitConfig, "attack", "canAttackNotTouchingWaterUnits", (LogicBoolean) null);
                if (LogicBoolean.isStaticTrue(customUnitConfig.canAttackNotTouchingWaterUnits)) {
                    customUnitConfig.canAttackNotTouchingWaterUnits = null;
                }
                customUnitConfig.canOnlyAttackUnitsWithTags = iniFile.getAnimationSet(customUnitConfig, "attack", "canOnlyAttackUnitsWithTags", (AnimationSet) null);
                customUnitConfig.canOnlyAttackUnitsWithoutTags = iniFile.getAnimationSet(customUnitConfig, "attack", "canOnlyAttackUnitsWithoutTags", (AnimationSet) null);
                if (customUnitConfig.canOnlyAttackUnitsWithTags != null || customUnitConfig.canOnlyAttackUnitsWithoutTags != null) {
                    customUnitConfig.hasAttackTagRestrictions = true;
                }
                boolean z7 = false;
                boolean z8 = false;
                for (TurretConfig turretConfig6 : arrayList) {
                    if (turretConfig6.canOnlyAttackUnitsWithTags != null && turretConfig6.canOnlyAttackUnitsWithTags.a(customUnitConfig.canOnlyAttackUnitsWithTags)) {
                        turretConfig6.canOnlyAttackUnitsWithTags = null;
                    }
                    if (turretConfig6.canOnlyAttackUnitsWithoutTags != null && turretConfig6.canOnlyAttackUnitsWithoutTags.a(customUnitConfig.canOnlyAttackUnitsWithoutTags)) {
                        turretConfig6.canOnlyAttackUnitsWithoutTags = null;
                    }
                    if (turretConfig6.canShoot) {
                        if (turretConfig6.canOnlyAttackUnitsWithTags != null || turretConfig6.canOnlyAttackUnitsWithoutTags != null) {
                            z7 = true;
                        } else {
                            z8 = true;
                        }
                    }
                }
                if (z7 && !z8) {
                    customUnitConfig.allFiringTurretsHaveTagRestrictions = true;
                    customUnitConfig.hasAttackTagRestrictions = true;
                }
                customUnitConfig.isFixedFiring = iniFile.getBoolean("attack", "isFixedFiring", (Boolean) false).booleanValue();
                customUnitConfig.lowPriorityTargetForOtherUnits = iniFile.getBoolean("ai", "lowPriorityTargetForOtherUnits", (Boolean) false).booleanValue();
                customUnitConfig.notPassivelyTargetedByOtherUnits = iniFile.getBoolean("ai", "notPassivelyTargetedByOtherUnits", (Boolean) false).booleanValue();
                if (customUnitConfig.canAttack && customUnitConfig.notPassivelyTargetedByOtherUnits) {
                    throw new RuntimeException("[ai]notPassivelyTargetedByOtherUnits is cannot currently supported on units that can attack");
                }
                customUnitConfig.aiTags = iniFile.getAnimationSet(customUnitConfig, "ai", "aiTags", (AnimationSet) null);
                customUnitConfig.disableUse = iniFile.getBoolean("ai", "disableUse", (Boolean) false).booleanValue();
                customUnitConfig.buildPriority = iniFile.getFloat("ai", "buildPriority", Float.valueOf(0.05f)).floatValue();
                customUnitConfig.recommendedInEachBaseNum = iniFile.getInt("ai", "recommendedInEachBaseNum", (Integer) 0).intValue();
                customUnitConfig.recommendedInEachBasePriorityIfUnmet = iniFile.getFloat("ai", "recommendedInEachBasePriorityIfUnmet", Float.valueOf(0.5f)).floatValue();
                customUnitConfig.maxEachBase = iniFile.getInt("ai", "maxEachBase", Integer.valueOf(Utility.max(2, customUnitConfig.recommendedInEachBaseNum))).intValue();
                customUnitConfig.maxGlobal = iniFile.getInt("ai", "maxGlobal", (Integer) (-1)).intValue();
                if (customUnitConfig.maxEachBase < customUnitConfig.recommendedInEachBaseNum) {
                    throw new RuntimeException("[ai]recommendedInEachBaseNum is smaller than maxEachBase");
                }
                if (!customUnitConfig.isBuildingUnit) {
                    if (iniFile.hasKey("ai", "recommendedInEachBaseNum")) {
                        throw new RuntimeException("[ai]recommendedInEachBaseNum currently only applies to buildings");
                    }
                    if (iniFile.hasKey("ai", "recommendedInEachBasePriorityIfUnmet")) {
                        throw new RuntimeException("[ai]recommendedInEachBasePriorityIfUnmet currently only applies to buildings");
                    }
                }
                customUnitConfig.whenUsingAsHarvester_recommendedInEachBase = iniFile.getInt("ai", "whenUsingAsHarvester_recommendedInEachBase", (Integer) (-1)).intValue();
                customUnitConfig.whenUsingAsHarvester_recommendedGlobal = iniFile.getInt("ai", "whenUsingAsHarvester_recommendedGlobal", (Integer) (-1)).intValue();
                customUnitConfig.whenUsingAsHarvester_includeOtherHarvesterCounts = iniFile.getBoolean("ai", "whenUsingAsHarvester_includeOtherHarvesterCounts", (Boolean) false).booleanValue();
                customUnitConfig.onlyUseAsHarvester_ifBaseHasUnitTagged = iniFile.getAnimationSet(customUnitConfig, "ai", "onlyUseAsHarvester_ifBaseHasUnitTagged", (AnimationSet) null);
                customUnitConfig.nonInBaseExtraPriority = iniFile.getFloat("ai", "nonInBaseExtraPriority", Float.valueOf(0.04f)).floatValue();
                customUnitConfig.nonInBaseExtraPriority = iniFile.getFloat("ai", "noneInBaseExtraPriority", Float.valueOf(customUnitConfig.nonInBaseExtraPriority)).floatValue();
                customUnitConfig.nonGlobalExtraPriority = iniFile.getFloat("ai", "nonGlobalExtraPriority", Float.valueOf(0.0f)).floatValue();
                customUnitConfig.nonGlobalExtraPriority = iniFile.getFloat("ai", "noneGlobalExtraPriority", Float.valueOf(customUnitConfig.nonGlobalExtraPriority)).floatValue();
                customUnitConfig.aiUpgradedFrom = iniFile.getString("ai", "upgradedFrom", (String) null);
                Float f8 = iniFile.getFloat("ai", "ai_upgradePriority", (Float) null);
                if (f8 != null && f8.floatValue() != -1.0f) {
                    if (f8.floatValue() >= 0.0f && f8.floatValue() <= 1.0f) {
                        customUnitConfig.aiUpgradePriority = f8.floatValue() * 100.0f;
                    } else {
                        throw new RuntimeException("[ai]ai_upgradePriority: " + customUnitConfig.aiUpgradePriority + " must be between 0-1 or -1 for default");
                    }
                }
                if (customUnitConfig.canAttack) {
                    for (int i9 = 0; i9 < customUnitConfig.turrets.length; i9++) {
                        TurretConfig turretConfig7 = customUnitConfig.turrets[i9];
                        if (turretConfig7.canShoot && turretConfig7.linkedTurret == null && customUnitConfig.showShotDelayBar) {
                            if (turretConfig7.m > 140.0f && (customUnitConfig.currentTurretIndex == -1 || customUnitConfig.turrets[customUnitConfig.currentTurretIndex].m < turretConfig7.m)) {
                                customUnitConfig.currentTurretIndex = i9;
                            }
                            if (turretConfig7.warmup > 80.0f) {
                                customUnitConfig.warmupBarTurretIndex = i9;
                            }
                        }
                    }
                }
                if (customUnitConfig.drawLayer == -2) {
                    if (customUnitConfig.movementType == UnitMovementType.AIR) {
                        customUnitConfig.drawLayer = 5;
                    } else if (customUnitConfig.isBuildingUnit()) {
                        if (customUnitConfig.image_back != null) {
                            customUnitConfig.drawLayer = 3;
                        } else {
                            customUnitConfig.drawLayer = 2;
                        }
                    } else if (customUnitConfig.unitStats.targetHeight < -2.0f) {
                        customUnitConfig.drawLayer = 1;
                    } else if (customUnitConfig.maxTransportingUnits > 0) {
                        customUnitConfig.drawLayer = 3;
                    } else {
                        customUnitConfig.drawLayer = 2;
                    }
                }
                if (customUnitConfig.autoTriggerConditions.size() > 0) {
                    customUnitConfig.hasAutoTriggerConditions = true;
                    FastArrayList fastArrayList = new FastArrayList();
                    FastArrayList fastArrayList2 = new FastArrayList();
                    FastArrayList fastArrayList3 = new FastArrayList();
                    for (CustomUnitCondition customUnitCondition : customUnitConfig.autoTriggerConditions) {
                        if (customUnitCondition.triggerType == UpdateFrequency.everyFrame) {
                            fastArrayList.add(customUnitCondition);
                        } else if (customUnitCondition.triggerType == UpdateFrequency.every4Frames) {
                            fastArrayList2.add(customUnitCondition);
                        } else if (customUnitCondition.triggerType == UpdateFrequency.every8Frames) {
                            fastArrayList3.add(customUnitCondition);
                        } else {
                            throw new RuntimeException("Unknown check rate:" + customUnitCondition.triggerType);
                        }
                    }
                    customUnitConfig.autoTriggerConditionsEveryFrame = (CustomUnitCondition[]) fastArrayList.toArray(new CustomUnitCondition[0]);
                    customUnitConfig.autoTriggerConditionsEvery4Frames = (CustomUnitCondition[]) fastArrayList2.toArray(new CustomUnitCondition[0]);
                    customUnitConfig.autoTriggerConditionsEvery8Frames = (CustomUnitCondition[]) fastArrayList3.toArray(new CustomUnitCondition[0]);
                }
                if (customUnitConfig.actionHandlers != null && customUnitConfig.actionHandlers.size() > 0) {
                    Iterator it6 = customUnitConfig.actionHandlers.iterator();
                    while (it6.hasNext()) {
                        ((CustomUnitActionHandler) it6.next()).a(customUnitConfig);
                    }
                }
                if (customUnitConfig.configProcessors.size > 0) {
                    Iterator it7 = customUnitConfig.configProcessors.iterator();
                    while (it7.hasNext()) {
                        ((CustomUnitConfigProcessor) it7.next()).a(customUnitConfig);
                    }
                    customUnitConfig.configProcessors.clear();
                }
                recordLoadPhaseTime(jA, LoadPhase.unitParsePartC);
                iniFile.checkForUnusedKeys();
                for (ConfigKeyValue configKeyValue : iniFile.duplicateKeys) {
                    if (configKeyValue.a() != null && (configKeyValue.a().startsWith("hiddenAction_") || configKeyValue.a().startsWith("canBuild_"))) {
                        throw new RuntimeException("Error [" + configKeyValue.a() + "]" + configKeyValue.b() + " has been repeated");
                    }
                    String str21 = "Repeated key " + configKeyValue;
                    customUnitConfig.logWarningToMod(str21);
                    if (customUnitConfig.strictLevel >= 1) {
                        GameEngine.log("Converting warning to error (meta.strictLevel=" + customUnitConfig.strictLevel + ")");
                        throw new ConfigParseException(str21);
                    }
                }
                Iterator it8 = iniFile.errors.iterator();
                while (it8.hasNext()) {
                    String str22 = "Skipping line, unexpected format: '" + ((String) it8.next()) + "'";
                    customUnitConfig.logWarningToMod(str22);
                    if (customUnitConfig.strictLevel >= 1) {
                        GameEngine.log("Converting warning to error (meta.strictLevel=" + customUnitConfig.strictLevel + ")");
                        throw new ConfigParseException(str22);
                    }
                }
                if (modInfo != null) {
                    modInfo.imageMemory++;
                }
                synchronized (CustomUnitConfig.allConfigs) {
                    CustomUnitConfig.allConfigs.add(customUnitConfig);
                }
                recordLoadPhaseTime(jA, LoadPhase.unitParsePartD);
                return customUnitConfig;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (ConfigParseException e2) {
            reportUnitLoadError(str, e2, modInfo);
            return null;
        } catch (OutOfMemoryError e3) {
            oomImageErrorCount++;
            reportUnitLoadError(str, new RuntimeException(e3), modInfo);
            return null;
        } catch (RuntimeException e4) {
            reportUnitLoadError(str, e4, modInfo);
            return null;
        }
    }

    /* JADX INFO: renamed from: a */
    public static void reportUnitLoadErrorForType(String str, Exception exc, UnitType unitType) {
        ModInfo modInfo = null;
        if (unitType instanceof CustomUnitConfig) {
            modInfo = ((CustomUnitConfig) unitType).modInfo;
        }
        reportUnitLoadError(str, exc, modInfo);
    }

    /* JADX INFO: renamed from: a */
    public static String getModRelativePath(ModInfo modInfo, String str, boolean z) {
        if (modInfo != null) {
            String strMapPath = FileHelper.fixPath(modInfo.sourceFolder);
            str = FileHelper.fixPath(str);
            if (str.startsWith(strMapPath)) {
                str = str.substring(strMapPath.length());
                if (str.startsWith("/")) {
                    str = str.substring(1);
                }
                if (str.startsWith("\\")) {
                    str = str.substring(1);
                }
            }
            if (z) {
                str = str + " (in mod " + modInfo.getDisplayTitle() + ")";
            }
        }
        return str;
    }

    /* JADX INFO: renamed from: a */
    public static void reportUnitLoadError(String str, Exception exc, ModInfo modInfo) {
        String errorMessage;
        String str2;
        GameEngine.logColored("Error while loading unit:" + str);
        GameEngine.printStackTrace(exc);
        if (str == null) {
            str = "<null>";
        }
        if (exc instanceof ConfigParseException) {
            errorMessage = exc.getMessage();
        } else {
            errorMessage = Utility.formatExceptionMessage(exc);
        }
        if (errorMessage == null) {
            errorMessage = "<No error cause>";
        }
        if (!errorMessage.contains("unit config file")) {
            errorMessage = errorMessage.replace(str + ": ", VariableScope.nullOrMissingString).replace(str, VariableScope.nullOrMissingString);
        }
        String strApplyCopyFromSectionChain = getModRelativePath(modInfo, str, true);
        if (modInfo != null) {
            str2 = "Error loading unit: " + strApplyCopyFromSectionChain + ": \n" + errorMessage;
        } else if (errorMessage.contains("Error loading core unit")) {
            str2 = errorMessage;
        } else {
            str2 = "Error loading core unit: " + strApplyCopyFromSectionChain + ": \n" + errorMessage + " (This might be from placing a mod in 'assets/', they should go under 'mods/')";
        }
        if (exc instanceof ConfigParseException) {
            ConfigParseException configParseException = (ConfigParseException) exc;
            if (configParseException.filePath != null || configParseException.errorContext != null) {
                str2 = str2 + " (section:" + configParseException.filePath + ", key:" + configParseException.errorContext + ")";
            }
        }
        boolean z = false;
        if (modInfo != null) {
            z = modInfo.disabled;
        }
        if (!z) {
        }
        if (lastErrorMessage != null) {
            lastErrorMessage = str2;
        }
        if (modInfo != null) {
            modInfo.addError(str2);
        } else {
            try {
                Thread.sleep(2L);
            } catch (InterruptedException e) {
            }
            throw new RuntimeException(str2, exc);
        }
    }

    /* JADX INFO: renamed from: b */
    public static void parseCanBuildEntry(CustomUnitConfig customUnitConfig, IniFile iniFile, String str, String str2, boolean z) throws ConfigParseException {
        String string = iniFile.getString(str, str2 + "name", (String) null);
        if (string == null) {
            return;
        }
        for (String str3 : string.split(",")) {
            String strTrim = str3.trim();
            CustomActionDef customActionDef = new CustomActionDef();
            customActionDef.stringId = strTrim;
            customActionDef.extraLagHidingInUI = iniFile.getBoolean(str, str2 + "extraLagHidingInUI", (Boolean) false).booleanValue();
            customActionDef.pos = iniFile.getFloat(str, str2 + "pos", Float.valueOf(999.0f)).floatValue();
            customActionDef.techLevel = iniFile.getInt(str, str2 + "tech", (Integer) 1).intValue();
            customActionDef.forceNano = iniFile.getBoolean(str, str2 + "forceNano", (Boolean) false).booleanValue();
            customActionDef.buildType = iniFile.getString(str, str2 + "type", (String) null);
            customActionDef.price = UnitPrice.a(customUnitConfig, iniFile, str, str2 + "price", (UnitPrice) null);
            customActionDef.isGuiBlinking = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "isGuiBlinking", (LogicBoolean) null);
            customActionDef.isVisible = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "isVisible", (LogicBoolean) null);
            customActionDef.isLocked = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "isLocked", (LogicBoolean) null);
            customActionDef.isLockedMessage = getUnitReference(customUnitConfig, iniFile, str, str2 + "isLockedMessage", (String) null);
            if (customActionDef.isLocked != null) {
                customActionDef.hideInBuildMenu = true;
            }
            if (customActionDef.isLocked == LogicBoolean.falseBoolean) {
                customActionDef.isLocked = null;
            }
            customActionDef.isLockedAlt = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "isLockedAlt", (LogicBoolean) null);
            customActionDef.isLockedAltMessage = getUnitReference(customUnitConfig, iniFile, str, str2 + "isLockedAltMessage", (String) null);
            if (customActionDef.isLockedAlt != null) {
                customActionDef.hideInBuildMenu = true;
            }
            if (customActionDef.isLockedAlt == LogicBoolean.falseBoolean) {
                customActionDef.isLockedAlt = null;
            }
            customActionDef.isLockedAlt2 = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "isLockedAlt2", (LogicBoolean) null);
            customActionDef.isLockedAlt2Message = getUnitReference(customUnitConfig, iniFile, str, str2 + "isLockedAlt2Message", (String) null);
            if (customActionDef.isLockedAlt2 != null) {
                customActionDef.hideInBuildMenu = true;
            }
            if (customActionDef.isLockedAlt2 == LogicBoolean.falseBoolean) {
                customActionDef.isLockedAlt2 = null;
            }
            UnitPrice unitPriceA = UnitPrice.a(customUnitConfig, iniFile, str, str2 + "addResources", true);
            if (unitPriceA != null && unitPriceA.d()) {
                customActionDef.addResources = unitPriceA;
            }
            customActionDef.actionType = BuildType.build;
            if (!"NONE".equalsIgnoreCase(strTrim)) {
                customUnitConfig.customActionDefs.add(customActionDef);
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public static void parseCustomActionDef(CustomUnitConfig customUnitConfig, IniFile iniFile, String str, String str2, String str3, boolean z, boolean z2) throws ConfigParseException {
        ArrayList<CustomEventParseEntry> arrayListCreateLocalizedString;
        CustomActionDef customActionDef = new CustomActionDef();
        String string = iniFile.getString(str, str2 + "convertTo", (String) null);
        String string2 = iniFile.getString(str, str2 + "whenBuilding_temporarilyConvertTo", (String) null);
        CustomUnitDataField[] customUnitDataFieldArrA = UnitStats.a(iniFile, str, str2 + "whenBuilding_temporarilyConvertTo_keepFields", (CustomUnitDataField[]) null);
        Float f = iniFile.getFloat(str, str2 + "addEnergy", (Float) null);
        UnitPrice unitPriceA = UnitPrice.a(customUnitConfig, iniFile, str, str2 + "addResources", true);
        customUnitConfig.a(unitPriceA);
        UnitPrice unitPriceA2 = UnitPrice.a(customUnitConfig, iniFile, str, str2 + "addResourcesScaledByAIHandicaps", true);
        customUnitConfig.a(unitPriceA2);
        String string3 = iniFile.getString(str, str2 + "fireTurretXAtGround", (String) null);
        LogicBoolean logicBoolean = iniFile.getInt(customUnitConfig, str, str2 + "alsoTriggerOrQueueActionWithTarget", null);
        LogicBoolean logicBoolean2 = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "alsoTriggerOrQueueActionConditional", (LogicBoolean) null);
        String string4 = iniFile.getString(str, str2 + "alsoTriggerAction", (String) null);
        LogicBoolean logicBooleanNumber = iniFile.getLogicBooleanNumber(customUnitConfig, str, str2 + "alsoTriggerActionRepeat", null);
        String string5 = iniFile.getString(str, str2 + "alsoQueueAction", (String) null);
        String string6 = iniFile.getString(str, str2 + "spawnEffects", (String) null);
        String spawnEffectsAtActionTarget = iniFile.getString(
                str,
                str2 + "spawnEffectsAtActionTarget",
                (String) null
        );
        String string7 = iniFile.getString(str, str2 + "spawnEffectsOnQueue", (String) null);
        String string8 = iniFile.getString(str, str2 + "playSoundAtUnit", (String) null);
        String string9 = iniFile.getString(str, str2 + "playSoundGlobally", (String) null);
        String string10 = iniFile.getString(str, str2 + "playSoundToPlayer", (String) null);
        String string11 = iniFile.getString(str, str2 + "playSoundToPlayerOnQueue", (String) null);
        TransportAction.a(customUnitConfig, iniFile, str, str2, customActionDef, str3, z);
        AttachmentAction.a(customUnitConfig, iniFile, str, str2, customActionDef, str3, z);
        MemoryAction.a(customUnitConfig, iniFile, str, str2, customActionDef, str3, z);
        ResourceAction.a(customUnitConfig, iniFile, str, str2, customActionDef, str3, z);
        SpawnUnitAction.a(customUnitConfig, iniFile, str, str2, customActionDef, str3, z);
        WaypointAction.a(customUnitConfig, iniFile, str, str2, customActionDef, str3, z);
        AnimationAction.a(customUnitConfig, iniFile, str, str2, customActionDef, str3, z);
        TagAction.a(customUnitConfig, iniFile, str, str2, customActionDef, str3, z);
        ConvertResourceAction.a(customUnitConfig, iniFile, str, str2, customActionDef, str3, z);
        TakeResourcesAction.a(customUnitConfig, iniFile, str, str2, customActionDef, str3, z);
        MessageAction.a(customUnitConfig, iniFile, str, str2, customActionDef, str3, z);
        UnitStatsAction.a(customUnitConfig, iniFile, str, str2, customActionDef, str3, z);
        SendMessageAction.a(customUnitConfig, iniFile, str, str2, customActionDef, str3, z);
        LogicBoolean logicBoolean3 = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "resetCustomTimer", (LogicBoolean) null);
        boolean z3 = false;
        if (z) {
            z3 = true;
        } else {
            if (string != null || string2 != null || f != null || string3 != null) {
                z3 = true;
            }
            if (unitPriceA.d() || unitPriceA2.d()) {
                z3 = true;
            }
            if (string4 != null || string5 != null || string6 != null || spawnEffectsAtActionTarget != null) {
                z3 = true;
            }
            if (string8 != null || string9 != null || string10 != null || string11 != null) {
                z3 = true;
            }
            if (customActionDef.logicActions.size() > 0) {
                z3 = true;
            }
        }
        if (z3) {
            if ("NONE".equalsIgnoreCase(string)) {
                string = null;
            }
            if ("NONE".equalsIgnoreCase(string2)) {
                string2 = null;
            }
            if (string3 != null && string3.equalsIgnoreCase("NONE")) {
                string3 = null;
            }
            customActionDef.id = customUnitConfig.customActionDefs.size();
            String string12 = iniFile.getString(str, str2 + "id", (String) null);
            if (string12 != null) {
                customActionDef.name = "c" + string12;
                if (customActionDef.name.contains(" ")) {
                    throw new RuntimeException("[" + str + "]id cannot contain space");
                }
                if (customActionDef.name.contains(",")) {
                    throw new RuntimeException("[" + str + "]id cannot contain ,");
                }
                if (customActionDef.name.contains(":")) {
                    throw new RuntimeException("[" + str + "]id cannot contain :");
                }
                if (customActionDef.name.contains("(")) {
                    throw new RuntimeException("[" + str + "]id cannot contain (");
                }
                if (customActionDef.name.contains("\u0000")) {
                    throw new RuntimeException("[" + str + "]id cannot contain null");
                }
                if (customActionDef.name.length() > 15) {
                    throw new RuntimeException("[" + str + "]id cannot be longer than 15 characters");
                }
                Iterator it = customUnitConfig.customActionDefs.iterator();
                while (it.hasNext()) {
                    if (customActionDef.name.equalsIgnoreCase(((CustomActionDef) it.next()).name)) {
                        throw new RuntimeException("[" + str + "]id more than one action exists with id: " + string12);
                    }
                }
            }
            customActionDef.displayName = str3;
            customActionDef.extraLagHidingInUI = iniFile.getBoolean(str, str2 + "extraLagHidingInUI", (Boolean) false).booleanValue();
            customActionDef.tags = AnimationTag.a(iniFile.getString(str, str2 + "tags", (String) null));
            customActionDef.pos = iniFile.getFloat(str, str2 + "pos", Float.valueOf(999.0f)).floatValue();
            customActionDef.price = UnitPrice.a(customUnitConfig, iniFile, str, str2 + "price", true);
            customActionDef.streamingCost = UnitPrice.b(customUnitConfig, iniFile, str, str2 + "streamingCost", null);
            if (iniFile.getBoolean(str, str2 + "switchPriceWithStreamingCost", (Boolean) false).booleanValue()) {
                if (customActionDef.streamingCost != null) {
                    throw new RuntimeException("[" + str + "]streamingCost and switchPriceWithStreamingCost=true cannot be used at the same time");
                }
                customActionDef.streamingCost = UnitPrice.b(customUnitConfig, iniFile, str, str2 + "price", null);
                customActionDef.price = UnitPrice.a;
            }
            customUnitConfig.a(customActionDef.price);
            if (customActionDef.streamingCost != null) {
                customUnitConfig.a(customActionDef.streamingCost);
            }
            customActionDef.highPriorityQueue = iniFile.getBoolean(str, str2 + "highPriorityQueue", (Boolean) false).booleanValue();
            customActionDef.onlyOneUnitAtATime = iniFile.getBoolean(str, str2 + "onlyOneUnitAtATime", (Boolean) false).booleanValue();
            customActionDef.canPlayerCancel = iniFile.getBoolean(str, str2 + "canPlayerCancel", (Boolean) true).booleanValue();
            customActionDef.alwaysSinglePress = iniFile.getBoolean(str, str2 + "alwaysSinglePress", (Boolean) false).booleanValue();
            customActionDef.allowMultipleInQueue = iniFile.getBoolean(str, str2 + "allowMultipleInQueue", (Boolean) true).booleanValue();
            if (!customActionDef.canPlayerCancel && !customActionDef.allowMultipleInQueue && customActionDef.alwaysSinglePress) {
                customActionDef.hideQueueInterface = true;
            }
            if (!customActionDef.canPlayerCancel) {
                customActionDef.queueType = ActionType.none;
            } else {
                customActionDef.queueType = ActionType.popupQueue;
            }
            customActionDef.requireConditional = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "requireConditional", (LogicBoolean) null);
            customActionDef.isActive = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "isActive", (LogicBoolean) null);
            customActionDef.isVisible = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "isVisible", (LogicBoolean) null);
            customActionDef.isAlsoViewableByEnemies = iniFile.getBoolean(str, str2 + "isAlsoViewableByEnemies", (Boolean) false).booleanValue();
            customActionDef.isAlsoViewableByAllies = iniFile.getBoolean(str, str2 + "isAlsoViewableByAllies", Boolean.valueOf(customActionDef.isAlsoViewableByEnemies)).booleanValue();
            if (z2) {
                if (customActionDef.isVisible != null && !LogicBoolean.isStaticFalse(customActionDef.isVisible)) {
                    throw new RuntimeException("[" + str + "]isVisible doesn't make sense to use in hidden actions");
                }
                customActionDef.isVisible = LogicBoolean.falseBoolean;
            }
            customActionDef.isLocked = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "isLocked", (LogicBoolean) null);
            customActionDef.isLockedMessage = getUnitReference(customUnitConfig, iniFile, str, str2 + "isLockedMessage", (String) null);
            if (customActionDef.isLocked != null) {
                customActionDef.hideInBuildMenu = true;
            }
            if (customActionDef.isLocked == LogicBoolean.falseBoolean) {
                customActionDef.isLocked = null;
            }
            customActionDef.isLockedAlt = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "isLockedAlt", (LogicBoolean) null);
            customActionDef.isLockedAltMessage = getUnitReference(customUnitConfig, iniFile, str, str2 + "isLockedAltMessage", (String) null);
            if (customActionDef.isLockedAlt != null) {
                customActionDef.hideInBuildMenu = true;
            }
            if (customActionDef.isLockedAlt == LogicBoolean.falseBoolean) {
                customActionDef.isLockedAlt = null;
            }
            customActionDef.isLockedAlt2 = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "isLockedAlt2", (LogicBoolean) null);
            customActionDef.isLockedAlt2Message = getUnitReference(customUnitConfig, iniFile, str, str2 + "isLockedAlt2Message", (String) null);
            if (customActionDef.isLockedAlt2 != null) {
                customActionDef.hideInBuildMenu = true;
            }
            if (customActionDef.isLockedAlt2 == LogicBoolean.falseBoolean) {
                customActionDef.isLockedAlt2 = null;
            }
            customActionDef.aiHighPriorityCondition = LogicBoolean.create(customUnitConfig, iniFile.getString(str, str2 + "ai_isHighPriority", (String) null), null);
            if (customActionDef.aiHighPriorityCondition == LogicBoolean.falseBoolean) {
                customActionDef.aiHighPriorityCondition = null;
            }
            if (customActionDef.aiHighPriorityCondition != null) {
                customUnitConfig.hasAiHighPriorityAction = true;
            }
            customActionDef.aiDisabledCondition = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "ai_isDisabled", LogicBoolean.falseBoolean);
            customActionDef.aiUse = (com.corrodinggames.rts.game.units.custom.logic.ActionType) iniFile.getEnum(str, str2 + "aiUse", customActionDef.aiUse, com.corrodinggames.rts.game.units.custom.logic.ActionType.class);
            customActionDef.guiBuildUnit = customUnitConfig.createUnitTypeReference(iniFile.getString(str, str2 + "guiBuildUnit", (String) null), str2 + "guiBuildUnit", str);
            if (customActionDef.guiBuildUnit != null) {
                customActionDef.queueType = ActionType.placeBuilding;
                if (string != null) {
                    throw new RuntimeException("[" + str + "]guiBuildUnit and convertTo cannot currently be used the same action");
                }
            }
            customActionDef.aiConsiderSameAsBuilding = customUnitConfig.createUnitTypeReference(iniFile.getString(str, str2 + "ai_considerSameAsBuilding", (String) null), str2 + "ai_considerSameAsBuilding", str);
            customActionDef.isGuiBlinking = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "isGuiBlinking", (LogicBoolean) null);
            customActionDef.iconImage = cacheTexture(customUnitConfig.resourceLoadPath, iniFile.getString(str, str2 + "iconImage", "NONE"), customUnitConfig.imageSmoothing, customUnitConfig, str, str2 + "iconImage");
            customActionDef.iconExtraIsVisible = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "iconExtraIsVisible", (LogicBoolean) null);
            if (customActionDef.iconExtraIsVisible == LogicBoolean.trueBoolean) {
                customActionDef.iconExtraIsVisible = null;
            }
            customActionDef.iconExtraImage = customUnitConfig.a(iniFile, str, str2 + "iconExtraImage");
            customActionDef.iconExtraColor = iniFile.getColorAsInt(str, str2 + "iconExtraColor", Integer.valueOf(ArgbColor.a(100, 255, 255, 255))).intValue();
            customActionDef.unitShownInUI = UnitReference.parseUnitTypeOrReferenceFromConf(customUnitConfig, iniFile, str, str2 + "unitShownInUI", null);
            if (customActionDef.unitShownInUI != null && customActionDef.iconImage != null) {
                throw new RuntimeException("[" + str + "]unitShownInUI and iconImage: doesn't make sense to use both at the same time");
            }
            customActionDef.unitShownInUIWithHpBar = iniFile.getBoolean(str, str2 + "unitShownInUIWithHpBar", (Boolean) true).booleanValue();
            customActionDef.unitShownInUIWithProgressBar = iniFile.getBoolean(str, str2 + "unitShownInUIWithProgressBar", (Boolean) true).booleanValue();
            customActionDef.displayType = (ActionDisplayType) iniFile.getEnum(str, str2 + "displayType", customActionDef.displayType, ActionDisplayType.class);
            customActionDef.displayRemainingStockpile = iniFile.getBoolean(str, str2 + "displayRemainingStockpile", (Boolean) false).booleanValue();
            customActionDef.text = getUnitReference(customUnitConfig, iniFile, str, str2 + "text", VariableScope.nullOrMissingString);
            customActionDef.textAddUnitName = UnitReference.parseUnitTypeOrReferenceFromConf(customUnitConfig, iniFile, str, str2 + "textAddUnitName", null);
            customActionDef.textPostFix = getLocaleString(iniFile, str, str2 + "textPostFix", (String) null);
            customActionDef.descriptionAddFromUnit = UnitReference.parseUnitTypeOrReferenceFromConf(customUnitConfig, iniFile, str, str2 + "descriptionAddFromUnit", null);
            customActionDef.descriptionAddUnitStats = UnitReference.parseUnitTypeOrReferenceFromConf(customUnitConfig, iniFile, str, str2 + "descriptionAddUnitStats", null);
            customActionDef.description = getUnitReference(customUnitConfig, iniFile, str, str2 + "description", VariableScope.nullOrMissingString);
            customActionDef.buildSpeed = iniFile.getInvertedTime(str, str2 + "buildSpeed", Float.valueOf(customActionDef.buildSpeed)).floatValue();
            if (customActionDef.buildSpeed == 0.0f) {
                customActionDef.buildSpeed = 50.0f;
            }
            customActionDef.buildSpeedIgnoreFactorySpeedModifiers = iniFile.getBoolean(str, str2 + "buildSpeed_ignoreFactorySpeedModifiers", Boolean.valueOf(customActionDef.buildSpeedIgnoreFactorySpeedModifiers)).booleanValue();
            boolean z4 = false;
            customActionDef.whenBuildingCannotMove = iniFile.getBoolean(str, str2 + "whenBuilding_cannotMove", Boolean.valueOf(customActionDef.whenBuildingCannotMove)).booleanValue();
            customActionDef.whenBuildingPlayAnimation = customUnitConfig.loadCore(iniFile.getString(str, str2 + "whenBuilding_playAnimation", (String) null), customActionDef.whenBuildingPlayAnimation);
            customActionDef.whenBuildingRotateTo = iniFile.getFloat(str, str2 + "whenBuilding_rotateTo", customActionDef.whenBuildingRotateTo);
            customActionDef.whenBuildingRotateToOrBackwards = iniFile.getBoolean(str, str2 + "whenBuilding_rotateTo_orBackwards", Boolean.valueOf(customActionDef.whenBuildingRotateToOrBackwards)).booleanValue();
            customActionDef.whenBuildingRotateToWaitTillRotated = iniFile.getBoolean(str, str2 + "whenBuilding_rotateTo_waitTillRotated", Boolean.valueOf(customActionDef.whenBuildingRotateToWaitTillRotated)).booleanValue();
            customActionDef.whenBuildingRotateToAimAtActionTarget = iniFile.getBoolean(str, str2 + "whenBuilding_rotateTo_aimAtActionTarget", Boolean.valueOf(customActionDef.whenBuildingRotateToAimAtActionTarget)).booleanValue();
            String string13 = iniFile.getString(str, str2 + "whenBuilding_rotateTo_rotateTurretX", (String) null);
            if (string13 != null) {
                customActionDef.whenBuildingRotateToRotateTurretX = customUnitConfig.findProjectileConfigByName(string13);
                if (customActionDef.whenBuildingRotateToRotateTurretX == null) {
                    throw new RuntimeException("Cannot find turret:" + string13 + " for [" + str + "]" + str2 + "whenBuilding_rotateTo_rotateTurretX");
                }
                if (customActionDef.whenBuildingRotateToOrBackwards) {
                    throw new RuntimeException("whenBuilding_rotateTo_orBackwards:true not supported with [" + str + "]" + str2 + "whenBuilding_rotateTo_rotateTurretX");
                }
            }
            if (customActionDef.whenBuildingRotateToAimAtActionTarget && customActionDef.whenBuildingRotateTo == null) {
                customActionDef.whenBuildingRotateTo = Float.valueOf(0.0f);
            }
            customActionDef.whenBuildingTriggerAction = iniFile.getCustomUnitAction(customUnitConfig, str, str2 + "whenBuilding_triggerAction", (CustomUnitActionHandler) null);
            customActionDef.convertToKeepCurrentTags = iniFile.getBoolean(str, str2 + "convertTo_keepCurrentTags", Boolean.valueOf(customActionDef.convertToKeepCurrentTags)).booleanValue();
            customActionDef.convertToKeepCurrentFields = UnitStats.a(iniFile, str, str2 + "convertTo_keepCurrentFields", (CustomUnitDataField[]) null);
            if (string2 != null && !"NONE".equalsIgnoreCase(string2)) {
                customActionDef.whenBuildingTemporarilyConvertTo = customUnitConfig.createUnitTypeReference(string2, str2 + "whenBuilding_temporarilyConvertTo", str);
                customActionDef.whenBuildingTemporarilyConvertToKeepFields = customUnitDataFieldArrA;
                z4 = true;
            }
            if (customActionDef.whenBuildingCannotMove || customActionDef.whenBuildingPlayAnimation != null || customActionDef.whenBuildingRotateTo != null || customActionDef.whenBuildingTemporarilyConvertTo != null || customActionDef.whenBuildingTriggerAction != null) {
                customUnitConfig.isFactory = true;
            }
            customActionDef.actionType = BuildType.convert;
            if (string != null && !"NONE".equalsIgnoreCase(string)) {
                customActionDef.convertTo = customUnitConfig.createUnitTypeReference(string, str2 + "convertTo", str);
                customActionDef.stringId = string;
                customActionDef.allowMultipleInQueue = false;
                z4 = true;
            }
            if (f != null) {
                customActionDef.addEnergy = f;
                z4 = true;
            }
            if (unitPriceA != null && unitPriceA.d()) {
                customActionDef.addResources = unitPriceA;
                z4 = true;
            }
            if (unitPriceA2 != null && unitPriceA2.d()) {
                customActionDef.addResourcesScaledByAIHandicaps = unitPriceA2;
                z4 = true;
            }
            customActionDef.fireTurretAtGroundOffset = iniFile.getPointF(str, str2 + "fireTurretXAtGround_withOffset", (PointF) null);
            customActionDef.fireTurretAtGroundTarget = iniFile.getInt(customUnitConfig, str, str2 + "fireTurretXAtGround_withTarget", null);
            customActionDef.fireTurretAtGroundCount = iniFile.getInt(str, str2 + "fireTurretXAtGround_count", (Integer) 1).intValue();
            customActionDef.fireTurretAtGroundGuideDecals = CustomUnitDecalRenderer.a(customUnitConfig, iniFile.getString(str, "fireTurretXAtGround_showGuideDecals", (String) null));
            if (customActionDef.fireTurretAtGroundTarget != null && customActionDef.fireTurretAtGroundOffset == null) {
                customActionDef.fireTurretAtGroundOffset = new PointF(0.0f, 0.0f);
            }
            String string14 = iniFile.getString(str, str2 + "fireTurretXAtGround_withProjectile", (String) null);
            if (string14 != null) {
                customActionDef.fireTurretAtGroundProjectile = customUnitConfig.findProjectileTemplateByName(string14);
                if (customActionDef.fireTurretAtGroundProjectile == null) {
                    throw new RuntimeException("Cannot find projectile:" + string14 + " for [" + str + "]" + str2 + "fireTurretXAtGround_withProjectile");
                }
            }
            String string15 = iniFile.getString(str, str2 + "fireTurretXAtGround_onlyOverPassableTileOf", (String) null);
            if (string15 != null) {
                customActionDef.fireTurretAtGroundTerrainFilter = UnitMovementType.a(string15, str2 + "fireTurretXAtGround_overPassableTileOf");
            }
            if (string3 != null) {
                TurretConfig turretConfigFindProjectileConfigByName = customUnitConfig.findProjectileConfigByName(string3);
                if (turretConfigFindProjectileConfigByName == null) {
                    throw new RuntimeException("Cannot find turret:" + string3 + " for [" + str + "]" + str2 + "fireTurretXAtGround");
                }
                customActionDef.fireTurretAtGroundIndex = Integer.valueOf(turretConfigFindProjectileConfigByName.turretIndex);
                if (customActionDef.fireTurretAtGroundOffset == null) {
                    customActionDef.queueType = ActionType.targetGround;
                    if (customActionDef.guiBuildUnit != null) {
                        throw new RuntimeException("[" + str + "]guiBuildUnit and fireTurretXAtGround (without withOffset) cannot be used in the same action");
                    }
                }
                z4 = true;
            }
            customActionDef.alsoTriggerOrQueueActionTarget = logicBoolean;
            customActionDef.alsoTriggerOrQueueActionCondition = logicBoolean2;
            if (string4 != null && !"NONE".equalsIgnoreCase(string4)) {
                customActionDef.alsoTriggerAction = customUnitConfig.addActionHandler(string4, "alsoTriggerAction", str);
                if (logicBooleanNumber != null) {
                    if (LogicBoolean.isStaticNumber(logicBooleanNumber)) {
                        float knownStaticNumber = LogicBoolean.getKnownStaticNumber(logicBooleanNumber);
                        if (knownStaticNumber == 0.0f) {
                            customActionDef.alsoTriggerAction = null;
                        } else if (knownStaticNumber != 1.0f) {
                            customActionDef.alsoTriggerActionRepeat = logicBooleanNumber;
                        }
                    } else {
                        customActionDef.alsoTriggerActionRepeat = logicBooleanNumber;
                    }
                }
                z4 = true;
            }
            if (string5 != null && !"NONE".equalsIgnoreCase(string5)) {
                customActionDef.alsoQueueAction = customUnitConfig.addActionHandler(string5, "alsoQueueAction", str);
                z4 = true;
            }
            if (string6 != null) {
                customActionDef.spawnEffects = customUnitConfig.addConfigExtension(string6, (CustomUnitSpawnList) null);
                z4 = true;
            }
            if (spawnEffectsAtActionTarget != null) {
                customActionDef.spawnListAtActionTarget = customUnitConfig.addConfigExtension(
                        spawnEffectsAtActionTarget,
                        (CustomUnitSpawnList) null
                );
                z4 = true;
            }
            if (string7 != null) {
                customActionDef.spawnEffectsOnQueue = customUnitConfig.addConfigExtension(string7, (CustomUnitSpawnList) null);
                z4 = true;
            }
            if (string8 != null) {
                customActionDef.playSoundAtUnit = SoundList.a(customUnitConfig, string8);
                z4 = true;
            }
            if (string9 != null) {
                customActionDef.playSoundGlobally = SoundList.a(customUnitConfig, string9);
                z4 = true;
            }
            if (string10 != null) {
                customActionDef.playSoundToPlayer = SoundList.a(customUnitConfig, string10);
                z4 = true;
            }
            if (string11 != null) {
                customActionDef.playSoundToPlayerOnQueue = SoundList.a(customUnitConfig, string11);
                z4 = true;
            }
            if (logicBoolean3 != null) {
                customActionDef.resetCustomTimer = logicBoolean3;
                z4 = true;
            }
            if (customActionDef.logicActions.size() > 0) {
                z4 = true;
            }
            ArrayList<CustomEventBinding> arrayList = null;
            String string16 = iniFile.getString(str, str2 + "autoTriggerOnEvent", (String) null);
            Integer logicBooleanUnit = iniFile.getInt(str, str2 + "autoTriggerOnEventRecursionLimit", (Integer) null);
            if (logicBooleanUnit != null) {
                if (logicBooleanUnit.intValue() < 0) {
                    throw new ConfigParseException("[" + str + "]" + str2 + "autoTriggerOnEventRecursionLimit: Cannot be < 0");
                }
                if (logicBooleanUnit.intValue() > 50) {
                    throw new ConfigParseException("[" + str + "]" + str2 + "autoTriggerOnEventRecursionLimit: Cannot be > 100");
                }
            }
            if (string16 != null && (arrayListCreateLocalizedString = parseCustomEventEntries(str, str2 + "autoTriggerOnEvent", string16)) != null) {
                if (arrayListCreateLocalizedString.size() < 1) {
                    throw new ConfigParseException("[" + str + "]" + str2 + "autoTriggerOnEvent: Expected 1 or more options, got:" + arrayListCreateLocalizedString.size());
                }
                for (CustomEventParseEntry customEventParseEntry : arrayListCreateLocalizedString) {
                    try {
                        UnitEventType unitEventType = (UnitEventType) IniFile.parseEnum(customEventParseEntry.a, (Enum) null, UnitEventType.class);
                        if (arrayList == null) {
                            arrayList = new ArrayList();
                        }
                        CustomEventBinding customEventBinding = new CustomEventBinding();
                        customEventBinding.a = unitEventType;
                        if (logicBooleanUnit != null) {
                            customEventBinding.e = logicBooleanUnit.intValue();
                        } else if (customEventBinding.a == UnitEventType.newMessage) {
                            customEventBinding.e = 4;
                        }
                        if (customEventParseEntry.b != null) {
                            for (String str4 : customEventParseEntry.b.keySet()) {
                                String str5 = (String) customEventParseEntry.b.get(str4);
                                boolean z5 = false;
                                if (str4.equalsIgnoreCase("withtag")) {
                                    if (customEventBinding.a != UnitEventType.tookDamage && customEventBinding.a != UnitEventType.newMessage) {
                                        throw new ConfigParseException("[" + str + "]" + str2 + "autoTriggerOnEvent: " + customEventBinding.a.name() + " doesn't support parameter: " + str4);
                                    }
                                    z5 = true;
                                }
                                if (str4.equalsIgnoreCase("withprojectiletag")) {
                                    if (customEventBinding.a != UnitEventType.tookDamage) {
                                        throw new ConfigParseException("[" + str + "]" + str2 + "autoTriggerOnEvent: " + customEventBinding.a.name() + " doesn't support parameter: " + str4);
                                    }
                                    z5 = true;
                                }
                                if (str4.equalsIgnoreCase("withactiontag")) {
                                    if (customEventBinding.a != UnitEventType.queueItemAdded && customEventBinding.a != UnitEventType.queueItemCancelled) {
                                        throw new ConfigParseException("[" + str + "]" + str2 + "autoTriggerOnEvent: " + customEventBinding.a.name() + " doesn't support parameter: " + str4);
                                    }
                                    z5 = true;
                                }
                                if (z5) {
                                    String strSplit = Utility.stripQuotes(str5);
                                    if (strSplit == null) {
                                        throw new ConfigParseException("[" + str + "]" + str2 + "autoTriggerOnEvent: " + customEventBinding.a.name() + " expected quoted string, got: " + str5);
                                    }
                                    if (customEventBinding.d != null) {
                                        throw new ConfigParseException("[" + str + "]" + str2 + "autoTriggerOnEvent: " + customEventBinding.a.name() + " tag was set twice");
                                    }
                                    customEventBinding.d = IniFile.parseAnimationTag(str, str2 + "autoTriggerOnEvent", strSplit);
                                } else {
                                    throw new ConfigParseException("[" + str + "]" + str2 + "autoTriggerOnEvent: Unknown parameter: " + str4);
                                }
                            }
                        }
                        arrayList.add(customEventBinding);
                    } catch (ConfigParseException e) {
                        throw new ConfigParseException("[" + str + "]" + str2 + "autoTriggerOnEvent: " + e.getMessage(), e);
                    }
                }
            }
            LogicBoolean logicBoolean4 = iniFile.getLogicBoolean(customUnitConfig, str, str2 + "autoTrigger", (LogicBoolean) null);
            String string17 = iniFile.getString(str, str2 + "autoTrigger", (String) null);
            UpdateFrequency updateFrequency = (UpdateFrequency) iniFile.getEnum(str, str2 + "autoTriggerCheckRate", customUnitConfig.autoTriggerCheckRate, UpdateFrequency.class);
            customActionDef.addToBuildQueue = z4;
            if (z4 || customActionDef.isVisible != null) {
                if (logicBoolean4 != null && z4) {
                    CustomUnitCondition customUnitCondition = new CustomUnitCondition();
                    customUnitCondition.logicBoolean = logicBoolean4;
                    customUnitCondition.conditionName = string17;
                    customUnitCondition.triggerType = updateFrequency;
                    customUnitCondition.action = new CustomAction(customActionDef, customUnitConfig.createUnitTypeReference(customActionDef.stringId, "[" + str + "]" + str2, str));
                    customUnitConfig.autoTriggerConditions.add(customUnitCondition);
                }
                if (arrayList != null && z4) {
                    CustomAction customAction = new CustomAction(customActionDef, customUnitConfig.createUnitTypeReference(customActionDef.stringId, "[" + str + "]" + str2, str));
                    for (CustomEventBinding customEventBinding2 : arrayList) {
                        customEventBinding2.b = customAction;
                        customEventBinding2.c = customUnitConfig;
                        customUnitConfig.eventBindings.add(customEventBinding2);
                    }
                }
                if (customActionDef.stringId != null && customActionDef.price != null && customActionDef.price.b > 0) {
                    customUnitConfig.hasBuildCostActions = true;
                }
                customUnitConfig.customActionDefs.add(customActionDef);
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public static String resolveTexturePath(CustomUnitConfig customUnitConfig, String str, String str2) {
        if (str2.startsWith("SHARED:")) {
            str2 = str2.substring("SHARED:".length());
            str = "units/shared/common.ini";
        }
        if (str2.startsWith("CORE:")) {
            str2 = str2.substring("CORE:".length());
            str = "units/common.ini";
        }
        if (str2.startsWith("ROOT:")) {
            str2 = str2.substring("ROOT:".length());
            if (customUnitConfig.modInfo == null) {
                str = "units/common.ini";
            } else {
                str = customUnitConfig.modInfo.sourceFolder + "/common.ini";
            }
        }
        String str3 = Utility.getParentPath(str) + "/";
        while (true) {
            if (str2.startsWith("/") || str2.startsWith("\\")) {
                str2 = str2.substring(1);
            } else {
                return str3 + str2;
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public static void recordLoadPhaseTime(long j, LoadPhase loadPhase) {
        loadPhase.o += (double) PerformanceProfiler.a(j);
    }

    /* JADX INFO: renamed from: i */
    public static void logTimingStats() {
        GameEngine.log("==Timing==");
        for (LoadPhase loadPhase : LoadPhase.values()) {
            GameEngine.log(loadPhase.name() + ": " + PerformanceProfiler.a(loadPhase.o));
        }
    }

    /* JADX INFO: renamed from: j */
    public static void resetTimingStats() {
        for (LoadPhase loadPhase : LoadPhase.values()) {
            loadPhase.o = 0.0d;
        }
    }

    /* JADX INFO: renamed from: a */
    public static Texture cacheTexture(String str, String str2, boolean z, CustomUnitConfig customUnitConfig, String str3, String str4) {
        try {
            return loadTexture(str, str2, z, customUnitConfig);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException("[" + str3 + "]" + str4 + ": " + e.getMessage(), e);
        }
    }

    /* JADX INFO: renamed from: a */
    public static Texture loadTexture(String str, String str2, boolean z, CustomUnitConfig customUnitConfig) {
        long jA = PerformanceProfiler.a();
        Texture textureOpenUnitConfigStream = loadTextureInternal(str, str2, z, customUnitConfig);
        recordLoadPhaseTime(jA, LoadPhase.imageLoadOrGet);
        return textureOpenUnitConfigStream;
    }

    /* JADX INFO: renamed from: b */
    public static Texture loadTextureInternal(String str, String str2, boolean z, CustomUnitConfig customUnitConfig) {
        Texture textureA;
        if (str2 == null || str2.equalsIgnoreCase("NONE") || str2.equals(VariableScope.nullOrMissingString)) {
            return null;
        }
        boolean z2 = false;
        if (str2.startsWith("SHADOW:")) {
            str2 = str2.substring("SHADOW:".length());
            z2 = true;
        }
        if (str2.startsWith("SHARED:")) {
            str2 = str2.substring("SHARED:".length());
            str = "units/shared/common.ini";
        }
        if (str2.startsWith("CORE:")) {
            str2 = str2.substring("CORE:".length());
            str = "units/common.ini";
        }
        if (str2.startsWith("ROOT:")) {
            str2 = str2.substring("ROOT:".length());
            if (customUnitConfig.modInfo == null) {
                str = "units/common.ini";
            } else {
                str = customUnitConfig.modInfo.sourceFolder + "/common.ini";
            }
        }
        if (str2.startsWith("SHADOW:")) {
            str2 = str2.substring("SHADOW:".length());
            z2 = true;
        }
        GameEngine gameEngine = GameEngine.getInstance();
        String str3 = Utility.getParentPath(str) + "/";
        String str4 = "[" + z + "," + z2 + "]" + str3 + str2;
        Texture textureFromCache = getTextureFromCache(str4);
        if (textureFromCache != null) {
            return textureFromCache;
        }
        AssetInputStream assetInputStreamOpenAssetStreamForUnit = openAssetStreamForUnit(str3, str2, customUnitConfig);
        int i = 0;
        if (currentModInfo != null) {
            i = currentModInfo.imageCount;
        }
        if (i > 5) {
            GameEngine.log("Fast failing to oom image for this mod");
            textureA = gameEngine.renderGraphicsEngine.r();
        } else {
            long jA = PerformanceProfiler.a();
            try {
                textureA = gameEngine.renderGraphicsEngine.a((InputStream) assetInputStreamOpenAssetStreamForUnit, true);
                recordLoadPhaseTime(jA, LoadPhase.imageLoad);
                if (textureA.A()) {
                    GameEngine.log("oomErrors:" + oomImageErrorCount);
                    oomImageErrorCount++;
                    if (currentModInfo != null) {
                        currentModInfo.imageCount++;
                        currentModInfo.soundCount++;
                    }
                } else if (currentModInfo != null && !currentModInfo.isBuiltIn && GameEngine.isIOSVersion) {
                    textureA.z();
                }
            } catch (RuntimeException e) {
                GameEngine.log("imageStream:" + assetInputStreamOpenAssetStreamForUnit);
                throw new RuntimeException("Error decode image from: " + FileHelper.getFileName(str3 + str2), e);
            }
        }
        try {
            assetInputStreamOpenAssetStreamForUnit.close();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        if (textureA == null) {
            throw new RuntimeException("Failed to decode image: " + FileHelper.convertAbstractPath(str3 + str2));
        }
        textureA.a(z);
        if (z2) {
            textureA = BaseUnit.attackUnit(textureA, textureA.p, textureA.q);
        }
        trackImageMemory(textureA);
        putTextureInCache(str4, textureA);
        return textureA;
    }

    /* JADX INFO: renamed from: a */
    public static void putTextureInCache(String str, Texture texture) {
        imageCache.put(str, texture);
    }

    /* JADX INFO: renamed from: c */
    public static Texture getTextureFromCache(String str) {
        Texture texture = (Texture) imageCache.get(str);
        if (texture != null) {
            imageCacheHitCount++;
            trackImageMemory(texture);
            texture.t();
            return texture;
        }
        if (logImageCacheMiss) {
            GameEngine.log("loadImageInConf: cache miss: " + str);
        }
        imageCacheMissCount++;
        return null;
    }

    /* JADX INFO: renamed from: a */
    public static Sound loadSound(String str, String str2, CustomUnitConfig customUnitConfig) {
        long jA = PerformanceProfiler.a();
        Sound soundComputeLegAdjacency = loadSoundByPath(str, str2, customUnitConfig);
        recordLoadPhaseTime(jA, LoadPhase.soundLoadOrGet);
        return soundComputeLegAdjacency;
    }

    /* JADX INFO: renamed from: b */
    public static Sound loadSoundByPath(String str, String str2, CustomUnitConfig customUnitConfig) {
        if (str2 == null || str2.equalsIgnoreCase("NONE")) {
            return null;
        }
        GameEngine gameEngine = GameEngine.getInstance();
        if (!str2.contains(".")) {
            return gameEngine.soundEngine.getSound(str2);
        }
        if (str2.startsWith("ROOT:")) {
            str2 = str2.substring("ROOT:".length());
            if (customUnitConfig.modInfo == null) {
                str = "units/common.ini";
            } else {
                str = customUnitConfig.modInfo.sourceFolder + "/common.ini";
            }
        }
        if (str2.startsWith("CORE:")) {
            str2 = str2.substring("CORE:".length());
            str = "units/common.ini";
        }
        if (str2.startsWith("SHARED:")) {
            str2 = str2.substring("SHARED:".length());
            str = "units/shared/common.ini";
        }
        String str3 = Utility.getParentPath(str) + "/";
        String str4 = str3 + str2;
        Sound sound = (Sound) soundCache.get(str4);
        if (sound != null) {
            trackSoundMemory(sound);
            return sound;
        }
        if (!str2.toLowerCase(Locale.ROOT).endsWith(".ogg") && !str2.toLowerCase(Locale.ROOT).endsWith(".wav")) {
            throw new RuntimeException("Failed to open sound: " + str3 + VariableScope.nullOrMissingString + str2 + " only the ogg & wav sound formats are supported.");
        }
        AssetInputStream assetInputStreamOpenAssetStreamForUnit = openAssetStreamForUnit(str3, str2, customUnitConfig);
        long jA = PerformanceProfiler.a();
        Sound soundLoadSound = gameEngine.soundEngine.loadSound(str2, assetInputStreamOpenAssetStreamForUnit, false);
        try {
            assetInputStreamOpenAssetStreamForUnit.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        recordLoadPhaseTime(jA, LoadPhase.soundLoad);
        if (soundLoadSound == null) {
            String str5 = "Sound file found but failed to load: " + str4;
            if (str2.toLowerCase(Locale.ROOT).endsWith(".ogg")) {
                str5 = str5 + " - Check if this file is truly a ogg";
            }
            customUnitConfig.logWarningToMod(str5);
            return gameEngine.soundEngine.getNullSound("Failed to load");
        }
        trackSoundMemory(soundLoadSound);
        soundCache.put(str4, soundLoadSound);
        return soundLoadSound;
    }

    /* JADX INFO: renamed from: a */
    public static boolean isPathWithinMod(String str, String str2, String str3, ModInfo modInfo) throws IOException {
        if (io.github.rwx.p2p.transfer.ManagedTransferResources.isManaged(modInfo)) {
            return io.github.rwx.p2p.transfer.ManagedTransferResources.allows(modInfo, str3);
        }
        if (str2 == null || !str2.contains("..") || GameEngine.isAndroidPlatform()) {
            return true;
        }
        String canonicalPath = new File(FileHelper.convertAbstractPath(str3)).getCanonicalPath();
        if (canonicalPath.startsWith(new File(FileHelper.convertAbstractPath("units")).getCanonicalPath())) {
            return true;
        }
        String canonicalPath2 = modInfo.getCanonicalPath();
        boolean zStartsWith = canonicalPath.startsWith(canonicalPath2);
        if (!zStartsWith) {
            GameEngine.logColored("File: '" + canonicalPath + "' is not within mod: '" + canonicalPath2 + "'");
        }
        return zStartsWith;
    }

    /* JADX INFO: renamed from: a */
    public static String joinAssetPath(String str, String str2) {
        if (!str.endsWith("/")) {
            str = str + "/";
        }
        while (true) {
            if (str2.startsWith("/") || str2.startsWith("\\")) {
                str2 = str2.substring(1);
            } else {
                return str + str2;
            }
        }
    }

    /* JADX INFO: renamed from: c */
    public static AssetInputStream openAssetStreamForUnit(String str, String str2, CustomUnitConfig customUnitConfig) {
        String strTrackImageMemory = joinAssetPath(str, str2);
        ModInfo modInfo = null;
        if (customUnitConfig != null) {
            modInfo = customUnitConfig.modInfo;
        } else {
            GameEngine.logWarningAndStack("findAssetSteam meta==null");
        }
        if (modInfo != null) {
            try {
                if (!isPathWithinMod(str, str2, strTrackImageMemory, modInfo)) {
                    throw new RuntimeException("File is outside mod: " + strTrackImageMemory);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        AssetInputStream assetInputStreamOpenFileByPath = JvmModAssetBridge.openAsset(modInfo, strTrackImageMemory);
        if (assetInputStreamOpenFileByPath == null) {
            assetInputStreamOpenFileByPath = FileHelper.openFileByPath(strTrackImageMemory);
        }
        if (assetInputStreamOpenFileByPath == null) {
            GameEngine.log("Orginal path: " + strTrackImageMemory);
            throw new RuntimeException("IO Error: Failed to open: " + getModRelativePath(modInfo, strTrackImageMemory, true));
        }
        return assetInputStreamOpenFileByPath;
    }

    public static void computeLegAdjacency(CustomUnitConfig customUnitConfig) {
        LegConfig[] legConfigArr = customUnitConfig.legConfig;
        for (LegConfig legConfig : legConfigArr) {
            float f = -1.0f;
            LegConfig legConfig2 = null;
            float f2 = 1.0f;
            if (legConfig.o) {
                f2 = 0.1f;
            }
            for (LegConfig legConfig3 : legConfigArr) {
                if (legConfig != legConfig3 && !legConfig3.l) {
                    float fDistanceSq = Utility.distanceSq(legConfig.d * f2, legConfig.e, legConfig3.d * f2, legConfig3.e);
                    if (legConfig2 == null || fDistanceSq < f) {
                        f = fDistanceSq;
                        legConfig2 = legConfig3;
                    }
                }
            }
            float fSortRect = Utility.squareRoot(f) + 2.0f;
            float f3 = fSortRect * fSortRect;
            ArrayList arrayList = new ArrayList();
            for (LegConfig legConfig4 : legConfigArr) {
                if (legConfig != legConfig4 && !legConfig4.l && Utility.distanceSq(legConfig.d * f2, legConfig.e, legConfig4.d * f2, legConfig4.e) <= f3) {
                    arrayList.add(Integer.valueOf(legConfig4.a));
                }
            }
            legConfig.S = new int[arrayList.size()];
            for (int i = 0; i < arrayList.size(); i++) {
                legConfig.S[i] = ((Integer) arrayList.get(i)).intValue();
            }
        }
    }

    /* JADX INFO: renamed from: k */
    public static String getBuiltinModsFolderName() {
        return "builtin_mods";
    }

    /* JADX INFO: renamed from: l */
    public static String getBuiltinModsEnabledFolderName() {
        return "builtin_mods_enabled";
    }

    /* JADX INFO: renamed from: m */
    public static String getDefaultUserModsFolder() {
        String str;
        if (GameEngine.isNonAndroidVersion) {
            str = "/SD/mods/units";
        } else {
            str = "/SD/rustedWarfare/units";
        }
        return str;
    }

    /* JADX INFO: renamed from: a */
    public static ArrayList parseCustomEventEntries(String str, String str2, String str3) throws ConfigParseException {
        if (str3 == null || VariableScope.nullOrMissingString.equals(str3) || "NONE".equalsIgnoreCase(str3)) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (Object o : StringUtils.a(str3, ",", false)) {
            String strTrim = ((String) o).trim();
            if (!VariableScope.nullOrMissingString.equals(strTrim)) {
                String strTrim2 = null;
                if (strTrim.contains("(") && strTrim.contains(")")) {
                    String[] strArrB = StringUtils.b(strTrim, "(");
                    if (strArrB == null) {
                        throw new ConfigParseException("[" + str + "]" + str2 + ": Unexpected format for '" + strTrim + "' of " + str3);
                    }
                    strTrim = strArrB[0];
                    strTrim2 = strArrB[1].trim();
                }
                CustomEventParseEntry customEventParseEntry = new CustomEventParseEntry();
                customEventParseEntry.a = strTrim;
                if (strTrim2 != null) {
                    if (!strTrim2.endsWith(")")) {
                        throw new ConfigParseException("[" + str + "]" + str2 + ": Expected ')' in '" + strTrim + "' of " + str3);
                    }
                    for (String str4 : StringUtils.a(strTrim2.substring(0, strTrim2.length() - 1), ",", false, false)) {
                        if (!str4.trim().equals(VariableScope.nullOrMissingString)) {
                            String[] strArrB2 = StringUtils.b(str4, "=");
                            if (strArrB2 == null) {
                                throw new RuntimeException("[" + str + "]" + str2 + ": Unexpected key format for '" + strTrim + "' of " + str3);
                            }
                            String strTrim3 = strArrB2[0].trim();
                            String strTrim4 = strArrB2[1].trim();
                            if (customEventParseEntry.b == null) {
                                customEventParseEntry.b = new HashMap();
                            }
                            customEventParseEntry.b.put(strTrim3, strTrim4);
                        }
                    }
                }
                arrayList.add(customEventParseEntry);
            }
        }
        return arrayList;
    }
}
