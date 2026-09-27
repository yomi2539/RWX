package com.corrodinggames.rts.gameFramework.mission;

import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.ai.AIController;
import com.corrodinggames.rts.game.map.MapLoadException;
import com.corrodinggames.rts.game.map.MapObject;
import com.corrodinggames.rts.game.map.MapObjectLayer;
import com.corrodinggames.rts.game.units.*;
import com.corrodinggames.rts.game.units.buildings.CommandCenter;
import com.corrodinggames.rts.game.units.buildings.ResourceExtractor;
import com.corrodinggames.rts.game.units.buildings.turrets.TurretFactory;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig;
import com.corrodinggames.rts.game.units.custom.LocaleString;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Serializable;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.effects.EffectManager;
import com.corrodinggames.rts.gameFramework.effects.SpriteSheet;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.network.NetworkEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.gameFramework.n.f */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/gameFramework/n/f.class */
public class MissionEngine extends Serializable {
    public static boolean a = false;
        /* JADX INFO: renamed from: h */
    public LocaleString introText;
        /* JADX INFO: renamed from: k */
    public boolean isSurvival;
        /* JADX INFO: renamed from: l */
    public boolean survivalWavesClassic;
        /* JADX INFO: renamed from: q */
    public boolean hasCameraStartMarker;
        /* JADX INFO: renamed from: b */
    int lastSaveTime;
        /* JADX INFO: renamed from: c */
    int lastAutosaveTime;
        /* JADX INFO: renamed from: d */
    PlayerTeam team3;
        /* JADX INFO: renamed from: e */
    WaveSpawnMode waveSpawnMode;
        /* JADX INFO: renamed from: i */
    boolean hasMapText;
        /* JADX INFO: renamed from: j */
    boolean showIntro;
        /* JADX INFO: renamed from: m */
    boolean isDefeatWave;
        /* JADX INFO: renamed from: n */
    boolean isSkirmish;
        /* JADX INFO: renamed from: o */
    boolean shareFogWithAllies;
        /* JADX INFO: renamed from: p */
    boolean isTutorial;
    public Paint E;
    public Paint F;
    public Paint G;
    public Paint H;
    public boolean N;
    WaveSpawnMode f = WaveSpawnMode.allUnitsAndBuildings;
    public ArrayList g = new ArrayList();
    public int r = 0;
    String s = null;
    String t = null;
    int u = 0;
    int v = 2;
    int w = 1;
    int x = 0;
    public int y = 0;
    float z = 3000.0f;
    float A = 0.0f;
    float B = 0.0f;
    MissionMode C = MissionMode.normal;
    ArrayList D = new ArrayList();
    final boolean I = true;
    public ArrayList<MapTrigger> J = new ArrayList();
    PointF K = new PointF();
    int L = 0;
    float M = 0.0f;
    public ArrayList O = new ArrayList();
    PointF P = new PointF();
    boolean Q = false;
    boolean R = false;
    ArrayList S = new ArrayList();
    ArrayList T = new ArrayList();

    public void a(String str) {
        GameEngine.log("MissionEngine", "Map warning: " + str);
        NetworkEngine.a((String) null, "Map error: " + str);
    }

    @Override // com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeBoolean(this.showIntro);
        gameOutputStream.writeInt(this.r);
        gameOutputStream.writeInt(this.u);
        gameOutputStream.writeInt(this.v);
        gameOutputStream.writeInt(this.w);
        gameOutputStream.writeInt(this.x);
        gameOutputStream.writeFloat(this.z);
        gameOutputStream.writeFloat(this.A);
        gameOutputStream.writeFloat(this.B);
        gameOutputStream.writeBoolean(this.isDefeatWave);
        gameOutputStream.writeInt(6);
        gameOutputStream.writeInt(this.J.size());
        for (MapTrigger mapTrigger : this.J) {
            gameOutputStream.writeStringUTF(mapTrigger.uniqueId);
            gameOutputStream.writeBoolean(mapTrigger.isActive);
            gameOutputStream.writeInt(mapTrigger.activationTime);
            gameOutputStream.writeInt(mapTrigger.l);
            gameOutputStream.writeBoolean(mapTrigger.hasCompleted);
            gameOutputStream.writeInt(mapTrigger.n);
        }
        gameOutputStream.writeInt(this.y);
        gameOutputStream.writeBoolean(this.survivalWavesClassic);
    }

    public void a(GameInputStream gameInputStream) throws IOException {
        this.showIntro = gameInputStream.readBoolean();
        this.r = gameInputStream.readInt();
        this.u = gameInputStream.readInt();
        this.v = gameInputStream.readInt();
        this.w = gameInputStream.readInt();
        this.x = gameInputStream.readInt();
        this.z = gameInputStream.readFloat();
        this.A = gameInputStream.readFloat();
        this.B = gameInputStream.readFloat();
        this.isDefeatWave = gameInputStream.readBoolean();
        int i = gameInputStream.readInt();
        if (i >= 1) {
            int i2 = gameInputStream.readInt();
            for (int i3 = 0; i3 < i2; i3++) {
                String utf = gameInputStream.readUTF();
                boolean z = gameInputStream.readBoolean();
                int i4 = 0;
                int i5 = 0;
                boolean z2 = false;
                int i6 = 0;
                if (i >= 2) {
                    i4 = gameInputStream.readInt();
                    i5 = gameInputStream.readInt();
                }
                if (i >= 3) {
                    z2 = gameInputStream.readBoolean();
                }
                if (i >= 4) {
                    i6 = gameInputStream.readInt();
                }
                MapTrigger mapTriggerE = e(utf);
                if (mapTriggerE == null) {
                    GameEngine.logColored("MissionEngine:readIn: Could not find saved trigger:" + utf + " for de/activation");
                } else {
                    mapTriggerE.isActive = z;
                    mapTriggerE.activationTime = i4;
                    mapTriggerE.l = i5;
                    mapTriggerE.hasCompleted = z2;
                    mapTriggerE.n = i6;
                }
            }
        }
        if (i >= 5) {
            this.y = gameInputStream.readInt();
        }
        if (i >= 6) {
            this.survivalWavesClassic = gameInputStream.readBoolean();
        } else {
            this.survivalWavesClassic = true;
        }
    }

    public void b(String str) {
        GameEngine.log("MissionEngine:triggerLog", str);
    }

    public boolean a() {
        return this.isSkirmish;
    }

    public boolean b() {
        return this.shareFogWithAllies;
    }

    public void a(final boolean boolean1) throws MapLoadException {
        final GameEngine instance = GameEngine.getInstance();
        this.hasCameraStartMarker = false;
        this.lastSaveTime = instance.gameTimeMillis - 1000;
        this.lastAutosaveTime = instance.gameTimeMillis - 1000;
        (this.E = new Paint()).a(255, 255, 255, 255);
        this.E.a(true);
        this.E.a(Paint.Align.CENTER);
        this.E.a(Typeface.a(Typeface.c, 1));
        instance.updatePaintTextSize(this.E, 24.0f);
        (this.G = new Paint()).a(255, 255, 255, 255);
        this.G.a(true);
        this.G.a(Paint.Align.CENTER);
        instance.updatePaintTextSize(this.G, 18.0f);
        (this.H = new Paint()).a(255, 255, 255, 255);
        this.H.a(true);
        this.H.a(Paint.Align.CENTER);
        instance.updatePaintTextSize(this.H, 14.0f);
        (this.F = new Paint()).a(this.H);
        instance.updatePaintTextSize(this.F, 18.0f);
        this.showIntro = true;
        boolean b = false;
        MapObject objectByName = null;
        if (instance.tileMap.objectsLayer == null) {
            GameEngine.log("MissionEngine", "Error: 'triggers' object layer is missing from this map");
            b = true;
        }
        else {
            objectByName = instance.tileMap.objectsLayer.findObjectByName("map_info");
        }
        if (objectByName == null) {
            GameEngine.log("MissionEngine", "Error: map_info is missing from this map");
            b = true;
        }
        if (objectByName != null && objectByName.getDescription("type") == null) {
            this.a("type is missing from map_info");
            b = true;
        }
        if (b) {
            GameEngine.log("MissionEngine", "Defaulting to skirmish");
            this.isSkirmish = true;
            this.waveSpawnMode = WaveSpawnMode.noConstructionOrTech;
            return;
        }
        this.isSurvival = "survival".equalsIgnoreCase(objectByName.getDescription("type"));
        if (this.isSurvival) {
            this.survivalWavesClassic = "true".equalsIgnoreCase(objectByName.getDescription("survivalWavesClassic"));
            if (this.survivalWavesClassic) {
                GameEngine.log("Classic survial waves selected");
            }
            this.f();
            this.isTutorial = false;
            this.y = instance.settingsEngine.aiDifficulty;
            if (!this.survivalWavesClassic) {
                this.z = 1200.0f;
                if (this.y < 0) {
                    this.z = 3000.0f;
                }
            }
            else {
                this.z = 3000.0f;
            }
        }
        final String description = objectByName.getDescription("survivalWaves");
        if (description != null) {
            this.g(description);
        }
        final String description2 = objectByName.getDescription("startWithMusic");
        if (description2 != null) {
            instance.musicManager.playMusic(description2);
        }
        this.isSkirmish = "skirmish".equalsIgnoreCase(objectByName.getDescription("type"));
        if (this.isSkirmish) {
            this.waveSpawnMode = WaveSpawnMode.noConstructionOrTech;
        }
        this.shareFogWithAllies = "true".equalsIgnoreCase(objectByName.getDescription("shareFogWithAllies"));
        final String description3 = objectByName.getDescription("winCondition");
        if (description3 == null && !this.isSkirmish) {
            throw new MapLoadException("win condition not set");
        }
        if (description3 != null) {
            if (description3.equalsIgnoreCase("none")) {
                this.waveSpawnMode = WaveSpawnMode.none;
            }
            else if (description3.equalsIgnoreCase("allUnitsAndBuildings")) {
                this.waveSpawnMode = WaveSpawnMode.allUnitsAndBuildings;
            }
            else if (description3.equalsIgnoreCase("allBuildings")) {
                this.waveSpawnMode = WaveSpawnMode.allBuildings;
            }
            else if (description3.equalsIgnoreCase("mainBuilings")) {
                this.waveSpawnMode = WaveSpawnMode.mainBuildings;
            }
            else if (description3.equalsIgnoreCase("mainBuildings")) {
                this.waveSpawnMode = WaveSpawnMode.mainBuildings;
            }
            else if (description3.equalsIgnoreCase("commandCenter")) {
                this.waveSpawnMode = WaveSpawnMode.commandCenter;
            }
            else {
                if (!description3.equalsIgnoreCase("requiredObjectives")) {
                    throw new MapLoadException("unknown win condition:" + description3);
                }
                this.waveSpawnMode = WaveSpawnMode.requiredObjectives;
            }
        }
        if (this.isSkirmish) {
            this.f = this.waveSpawnMode;
        }
        this.introText = objectByName.createLocaleStringFromProperty("introText", null);
        if (this.introText != null) {
            this.introText.replaceAll("\\\\n", "\n");
            if (this.introText.isEmpty()) {
                this.introText = null;
            }
        }
        if (!instance.isInNetworkOrReplay() && !this.isSkirmish) {
            this.team3 = PlayerTeam.k(3);
            if (this.team3 != null) {
                this.team3.teamColorId = 0;
            }
        }
        if (instance.isInNetworkOrReplay()) {}
        for (final MapObject mapObject : instance.tileMap.objectsLayer.mapObjects) {
            if ("team_info".equalsIgnoreCase(mapObject.type)) {
                final int int1 = Integer.parseInt(mapObject.getPropertyOrDefault("team", "-2"));
                if (int1 == -2) {
                    throw new RuntimeException("cannot find team for:" + mapObject.name);
                }
                final PlayerTeam k = PlayerTeam.k(int1);
                if (k == null) {
                    GameEngine.logColored("No team loaded for:" + int1 + " skipping");
                    continue;
                }
                final Integer integerProperty = mapObject.getIntegerProperty("credits");
                if (integerProperty != null) {
                    k.credits = integerProperty;
                }
                if (mapObject.getDescription("basicAI") != null && instance.isSinglePlayerGame() && k instanceof AIController) {
                    GameEngine.logColored("Using basic AI:" + int1 + " by map request");
                    ((AIController)k).isAggressive = true;
                }
                final String description4 = mapObject.getDescription("lockAiDifficulty");
                if (description4 != null && k instanceof AIController) {
                    GameEngine.logColored("Locking lockAiDifficulty:" + int1 + " by map request to: " + description4);
                    final AIController aiController = (AIController)k;
                    aiController.teamPingTime = Integer.parseInt(description4);
                    aiController.isTeamLocked = true;
                    instance.networkEngine.updateAiTeamNames();
                }
                if (mapObject.getDescription("disabledAI") != null && instance.isSinglePlayerGame() && k instanceof AIController) {
                    GameEngine.logColored("Disabling AI:" + int1 + " by map request");
                    ((AIController)k).canBuild = true;
                }
                final String description5 = mapObject.getDescription("allyGroup");
                if (description5 != null && instance.isSinglePlayerGame()) {
                    k.teamColorId = Integer.parseInt(description5);
                }
                final String description6 = mapObject.getDescription("ai");
                if (description6 != null) {
                    k.isTeamObserver = description6.equalsIgnoreCase("survival");
                }
            }
            if ("camera_start".equalsIgnoreCase(mapObject.name) && !boolean1) {
                instance.centerViewpoint(mapObject.x, mapObject.y);
                this.hasCameraStartMarker = true;
                final Integer integerProperty2 = mapObject.getIntegerProperty("zoomTo");
                if (integerProperty2 != null) {
                    instance.targetZoom = integerProperty2;
                }
            }
            if ("attack_point".equalsIgnoreCase(mapObject.name)) {
                this.D.add(new PointF(mapObject.x, mapObject.y));
            }
            if ("rotate".equalsIgnoreCase(mapObject.type)) {
                final float float1 = Float.parseFloat(mapObject.getDescription("dir"));
                for (final BaseUnit am : BaseUnit.bE) {
                    if (am instanceof OrderableUnit && !am.bI() && mapObject.containsUnitPosition(am)) {
                        am.rotationSpeed = float1;
                    }
                }
            }
            if ("fall".equalsIgnoreCase(mapObject.type)) {
                for (final BaseUnit am2 : BaseUnit.bE) {
                    if (am2 instanceof OrderableUnit && !am2.bI() && mapObject.containsUnitPosition(am2)) {
                        am2.startFalling();
                    }
                }
            }
            if ("set_team".equalsIgnoreCase(mapObject.type)) {
                final int unitSelected = Integer.parseInt(mapObject.getDescription("team"));
                for (final BaseUnit am3 : BaseUnit.bE) {
                    if (am3 instanceof OrderableUnit && mapObject.containsUnitPosition(am3)) {
                        am3.setUnitSelected(unitSelected);
                    }
                }
            }
            if ("ai_allow_full_use".equalsIgnoreCase(mapObject.type)) {
                for (final BaseUnit am4 : BaseUnit.bE) {
                    if (am4 instanceof OrderableUnit && mapObject.containsUnitPosition(am4)) {
                        ((OrderableUnit)am4).isActive = false;
                    }
                }
            }
            if ("disable_unit_ai".equalsIgnoreCase(mapObject.type)) {
                for (final BaseUnit am5 : BaseUnit.bE) {
                    if (am5 instanceof OrderableUnit && mapObject.containsUnitPosition(am5)) {
                        am5.isAIUnit = true;
                    }
                }
            }
        }
        for (final BaseUnit baseUnit : BaseUnit.bE) {
            if (!baseUnit.u() && !(baseUnit instanceof Tree) && !baseUnit.bI() && baseUnit.transportContainer == null && baseUnit.parentEntity == null) {
                BaseUnit baseUnit2 = null;
                float float1 = 4900.0f;
                for (final BaseUnit baseUnit3 : BaseUnit.bE) {
                    if (baseUnit3.canTransportUnits() && baseUnit != baseUnit3 && (baseUnit.team == PlayerTeam.TEAM_ALL || baseUnit3.team.d(baseUnit.team))) {
                        final float distanceSq = Utility.distanceSq(baseUnit3.posX, baseUnit3.posY, baseUnit.posX, baseUnit.posY);
                        if (distanceSq >= float1 || !baseUnit3.d(baseUnit, true)) {
                            continue;
                        }
                        baseUnit2 = baseUnit3;
                        float1 = distanceSq;
                    }
                }
                if (baseUnit2 == null) {
                    continue;
                }
                baseUnit2.e(baseUnit, true);
            }
        }
        this.J.clear();
        for (final MapObject a : instance.tileMap.objectsLayer.mapObjects) {
            if (!"team_info".equalsIgnoreCase(a.type) && !"point".equalsIgnoreCase(a.type) && !"camera_pan".equalsIgnoreCase(a.type) && !"camera_start".equalsIgnoreCase(a.name) && !"map_info".equalsIgnoreCase(a.name) && !"attack_point".equalsIgnoreCase(a.name) && !"rotate".equalsIgnoreCase(a.type) && !"fall".equalsIgnoreCase(a.type) && !"set_team".equalsIgnoreCase(a.type) && !"ai_allow_full_use".equalsIgnoreCase(a.type) && !"disable_unit_ai".equalsIgnoreCase(a.type)) {
                if ("info".equalsIgnoreCase(a.type)) {
                    continue;
                }
                if (a.properties == null) {
                    c("Error: Skipping trigger:" + a.name + " - no properties found");
                }
                else {
                    final MapTrigger a2 = TriggerFactory.a(this, a);
                    if (a2 == null) {
                        continue;
                    }
                    this.J.add(a2);
                }
            }
        }
        for (final MapTrigger a3 : this.J) {
            String s = a3.b("activateIds");
            if (s == null) {
                s = a3.b("alsoActivate");
            }
            if (s != null) {
                final String[] split = s.split(",");
                for (int length = split.length, i = 0; i < length; ++i) {
                    final MapTrigger d = this.d(split[i]);
                    if (d == null) {
                        a3.g("linkedTo target not found: " + s);
                        GameEngine.log("Possible IDs:");
                        for (final MapTrigger mapTrigger : this.J) {
                            if (mapTrigger.rawId != null) {
                                GameEngine.log(mapTrigger.rawId);
                            }
                        }
                        GameEngine.log("--------");
                    }
                    else {
                        d.d.a(a3);
                    }
                }
            }
            String s2 = a3.b("whenActivatedIds");
            if (s2 == null) {
                s2 = a3.b("activatedBy");
            }
            if (s2 != null) {
                for (final String string : s2.split(",")) {
                    final MapTrigger d2 = this.d(string);
                    if (d2 == null) {
                        a3.g("linkedFrom target not found: " + string);
                    }
                    else {
                        a3.d.a(d2);
                    }
                }
            }
            final String b2 = a3.b("deactivatedBy");
            if (b2 != null) {
                for (final String string2 : b2.split(",")) {
                    final MapTrigger d3 = this.d(string2);
                    if (d3 == null) {
                        a3.g("deactivatedBy: target not found: " + string2);
                    }
                    else {
                        a3.e.a(d3);
                    }
                }
            }
        }
        GameEngine.log("Found " + this.J.size() + " map triggers");
        for (final MapTrigger mapTrigger2 : this.J) {
            final String[] unvisitedPropertyNames = mapTrigger2.mapObject.getUnvisitedPropertyNames();
            for (int unitSelected = unvisitedPropertyNames.length, l = 0; l < unitSelected; ++l) {
                mapTrigger2.g("Key was not used: " + unvisitedPropertyNames[l]);
            }
        }
        this.c();
    }

    public void c() {
        for (MapTrigger mapTrigger : this.J) {
            if (mapTrigger.triggerType == TriggerType.objective) {
                boolean z = false;
                Iterator it = this.g.iterator();
                while (it.hasNext()) {
                    if (((TriggerWrapper) it.next()).trigger == mapTrigger) {
                        z = true;
                    }
                }
                if (!z) {
                    TriggerWrapper triggerWrapper = new TriggerWrapper();
                    triggerWrapper.trigger = mapTrigger;
                    this.g.add(triggerWrapper);
                    GameEngine.log("Found objective: " + triggerWrapper.a());
                }
            }
        }
    }

    public static void c(String str) {
        GameEngine.getInstance();
        GameEngine.log("MissionEngine", str);
        NetworkEngine.reportDesync(str);
    }

    public MapTrigger d(String str) {
        String strTrim = str.trim();
        for (MapTrigger mapTrigger : this.J) {
            if (mapTrigger.rawId != null && mapTrigger.rawId.equalsIgnoreCase(strTrim)) {
                return mapTrigger;
            }
        }
        return null;
    }

    public MapTrigger e(String str) {
        String strTrim = str.trim();
        for (MapTrigger mapTrigger : this.J) {
            if (mapTrigger.uniqueId.equalsIgnoreCase(strTrim)) {
                return mapTrigger;
            }
        }
        return null;
    }

    public PointF f(String str) {
        MapObjectLayer mapObjectLayer = GameEngine.getInstance().tileMap.objectsLayer;
        if (mapObjectLayer != null) {
            for (MapObject mapObject : mapObjectLayer.mapObjects) {
                if ("point".equalsIgnoreCase(mapObject.type) && mapObject.normalizedName != null && mapObject.normalizedName.equalsIgnoreCase(str)) {
                    this.K.a(mapObject.x, mapObject.y);
                    return this.K;
                }
            }
            return null;
        }
        return null;
    }

    public void a(float f) {
        GameEngine.getInstance();
    }

    public void b(final float float1) {
        final GameEngine instance = GameEngine.getInstance();
        if (this.hasMapText) {
            for (final MapTrigger mapTrigger : this.J) {
                if (mapTrigger.triggerType == TriggerType.mapText && mapTrigger.isActive) {
                    float n = mapTrigger.b() - instance.viewpointXSnapped;
                    float n2 = mapTrigger.c() - instance.viewpointYSnapped;
                    n *= instance.zoom;
                    n2 *= instance.zoom;
                    n += mapTrigger.textOffsetX;
                    n2 += mapTrigger.textOffsetY;
                    if (mapTrigger.C) {
                        final SpriteSheet spriteSheet = EffectManager.effectTemplates[9];
                        spriteSheet.drawSprite(2, n, n2, mapTrigger.B);
                        n2 -= spriteSheet.frameHeight - 2;
                    }
                    if (mapTrigger.text == null) {
                        continue;
                    }
                    final String resolveText = mapTrigger.text.resolveText();
                    if (resolveText == null) {
                        continue;
                    }
                    if (resolveText.equals("")) {
                        continue;
                    }
                    instance.renderGraphicsEngine.a(resolveText, n, n2, mapTrigger.B);
                }
            }
        }
        if (this.isSurvival && !this.N) {
            final boolean b = true;
            boolean b2 = false;
            this.B = Utility.moveTowardsZero(this.B, float1);
            if (this.B == 0.0f && this.A != 0.0f) {
                this.A = Utility.moveTowardsZero(this.A, float1);
                b2 = true;
            }
            if (b) {
                if (b2) {
                    final int n3 = (int)(23.0f + this.E.k() / 2.0f);
                    instance.renderGraphicsEngine.a("- Wave " + this.r + " -", instance.currentScreenWidthPixels / 2.0f, (float)n3, this.E);
                    if (this.s != null) {
                        instance.renderGraphicsEngine.a(this.s, instance.currentScreenWidthPixels / 2.0f, n3 + this.E.k() + 2.0f, this.F);
                    }
                }
                else {
                    final int n3 = (int)(23.0f + this.G.k() / 2.0f);
                    String string = "Wave " + (this.r + 1) + " in " + Utility.padRight(String.valueOf((int)(this.z / 60.0)), 3);
                    if (this.isDefeatWave) {
                        string = "Defeat - Wave " + this.r;
                    }
                    instance.renderGraphicsEngine.a(string, instance.currentScreenWidthPixels / 2.0f, (float)n3, this.G);
                    if (this.t == null) {
                        WaveUnitGroup waveunits;
                        if (!this.survivalWavesClassic) {
                            waveunits = this.b(false);
                        }
                        else {
                            waveunits = this.c(false);
                        }
                        this.t = waveunits.toString();
                    }
                    instance.renderGraphicsEngine.a(this.t, instance.currentScreenWidthPixels / 2.0f, n3 + this.G.k() + 2.0f, this.H);
                }
            }
        }
        if (this.isSurvival && this.N) {
            final MissionWave d = this.d();
            if (d != null) {
                final int n4 = d.e - instance.gameTimeMillis / 1000;
                final int n3 = (int)(23.0f + this.G.k() / 2.0f);
                String string2 = "Wave " + (this.r + 1) + " in " + Utility.padRight(String.valueOf(n4), 3);
                if (this.isDefeatWave) {
                    string2 = "Defeat - Wave " + this.r;
                }
                instance.renderGraphicsEngine.a(string2, instance.currentScreenWidthPixels / 2.0f, (float)n3, this.G);
                final String f = d.messageText;
                if (f != null) {
                    instance.renderGraphicsEngine.a(f, instance.currentScreenWidthPixels / 2.0f, n3 + this.G.k() + 2.0f, this.H);
                }
            }
        }
    }

    public void g(String str) throws MapLoadException {
        GameEngine.log("Loading survival waves");
        this.N = true;
        int i = 0;
        int i2 = 0;
        for (String str2 : str.split("\n")) {
            i2++;
            MissionWave waveVar = new MissionWave(this);
            if (waveVar.a(str2)) {
                waveVar.e = i + ((int) waveVar.timeSeconds);
                i = waveVar.e;
                GameEngine.log("Adding wave " + i2 + " at " + waveVar.e);
                this.O.add(waveVar);
            }
        }
    }

    public MissionWave d() {
        if (this.r < this.O.size()) {
            return (MissionWave) this.O.get(this.r);
        }
        return null;
    }

    public void e() {
        this.R = true;
        this.P.a((PointF) this.D.get(Utility.getDeterministicRandomIntInRange(0, this.D.size() - 1, this.r)));
    }

    public void f() {
        this.S.clear();
        a(this.S, "scout", 0.7f);
        a(this.S, UnitTypeEnum.tank, 2.1f);
        a(this.S, "mechGun", 1.0f);
        a(this.S, "lightGunship", 2.8f);
        a(this.S, UnitTypeEnum.hoverTank, 1.9f);
        a(this.S, UnitTypeEnum.helicopter, 0.8f);
        a(this.S, UnitTypeEnum.heavyTank, 1.0f);
        a(this.S, UnitTypeEnum.heavyHoverTank, 0.8f);
        a(this.S, UnitTypeEnum.gunShip, 0.7f);
        a(this.S, "plasmaTank", 0.6f);
        a(this.S, "missileAirship", 0.4f);
        this.T.clear();
        a(this.T, UnitTypeEnum.experimentalTank, 1.0f);
        a(this.T, UnitTypeEnum.experimentalHoverTank, 0.5f);
    }

    public void a(ArrayList arrayList, String str, float f) {
        a(arrayList, CustomUnitConfig.getUnitTypeByName(str), f);
    }

    public void a(ArrayList arrayList, UnitType unitType, float f) {
        if (unitType == null) {
            unitType = UnitTypeEnum.tank;
        }
        UnitType unitTypeC = CustomUnitConfig.c(unitType);
        if (unitTypeC != null) {
            unitType = unitTypeC;
        }
        WaveUnitSpawner waveUnitSpawner = new WaveUnitSpawner(this);
        waveUnitSpawner.unitType = unitType;
        waveUnitSpawner.difficultyMultiplier = f;
        arrayList.add(waveUnitSpawner);
    }

    public void a(WaveUnitGroup waveunitsVar, int i, float f) {
        if (i < 0) {
            i = 0;
        }
        int size = this.S.size();
        if (size == 0) {
            GameEngine.logColored("error maxTypeNum: " + size);
            return;
        }
        WaveUnitSpawner waveUnitSpawner = (WaveUnitSpawner) this.S.get(i % size);
        int waveUnitCount = (int) Utility.pow((int) (((double) (i + 3)) * 0.5d * ((double) waveUnitSpawner.difficultyMultiplier) * ((double) f)), 0.8f);
        if (waveUnitCount < 1) {
            waveUnitCount = 1;
        }
        waveunitsVar.b(waveUnitSpawner.unitType, waveUnitCount);
    }

    public WaveUnitGroup b(boolean z) {
        WaveUnitGroup waveunitsVar = new WaveUnitGroup(this);
        boolean z2 = false;
        if (this.u > 50 && (this.u + 1) % 100 == 0) {
            int size = this.T.size();
            int i = this.u / 100;
            if (size == 0) {
                GameEngine.logColored("error maxTypeNum: " + size);
            } else {
                WaveUnitSpawner waveUnitSpawner = (WaveUnitSpawner) this.T.get(i % size);
                int i2 = (int) (i * waveUnitSpawner.difficultyMultiplier);
                if (i2 < 1) {
                    i2 = 1;
                }
                waveunitsVar.b(waveUnitSpawner.unitType, i2);
            }
            z2 = true;
        }
        int i3 = 0;
        if (this.y > 0) {
            i3 = this.y;
        }
        a(waveunitsVar, this.u + i3, 1.0f);
        if (this.u > 15 && !z2) {
            a(waveunitsVar, ((int) ((this.u + i3) * 1.1f)) - 11, 0.5f);
        }
        if (z) {
            this.u++;
            this.v++;
        }
        return waveunitsVar;
    }

    public WaveUnitGroup c(boolean z) {
        WaveUnitGroup waveunitsVar = new WaveUnitGroup(this);
        waveunitsVar.isBossWave = false;
        int i = this.v;
        UnitTypeEnum unitTypeEnum = null;
        if (this.isTutorial) {
            unitTypeEnum = UnitTypeEnum.ladybug;
        } else {
            if (this.u == 0) {
                i++;
                unitTypeEnum = UnitTypeEnum.tank;
            }
            if (this.u == 1) {
                unitTypeEnum = UnitTypeEnum.hoverTank;
            }
            if (this.u == 2) {
                unitTypeEnum = UnitTypeEnum.helicopter;
            }
            if (this.u == 3) {
                i = this.w;
                unitTypeEnum = UnitTypeEnum.heavyTank;
            }
            if (this.u == 4) {
                i = this.w;
                unitTypeEnum = UnitTypeEnum.heavyHoverTank;
                if (this.w % 2 == 0) {
                    unitTypeEnum = UnitTypeEnum.gunShip;
                }
            }
            if (this.u == 5) {
                waveunitsVar.isBossWave = true;
                i = 1;
                unitTypeEnum = UnitTypeEnum.experimentalTank;
            }
            if (z) {
                this.u++;
                boolean z2 = false;
                if (this.w == 1) {
                    if (this.u > 2) {
                        z2 = true;
                    }
                } else if (this.w < 5) {
                    if (this.u > 4) {
                        z2 = true;
                    }
                } else {
                    if (this.u > 5) {
                        z2 = true;
                    }
                    if (this.u > 4 && this.w % 2 == 0) {
                        z2 = true;
                    }
                }
                if (z2) {
                    this.u = 0;
                    this.v += 2;
                    this.w++;
                }
            }
        }
        waveunitsVar.a(unitTypeEnum, i);
        return waveunitsVar;
    }

    public void c(float f) {
        WaveUnitGroup waveunitsVarC;
        GameEngine gameEngine = GameEngine.getInstance();
        int i = gameEngine.gameTimeMillis;
        this.M = Utility.moveTowardsZero(this.M, f);
        if (gameEngine.isStopped && gameEngine.isMenuBackgroundMap) {
            MapObject mapObject = null;
            if (gameEngine.tileMap.objectsLayer != null) {
                for (MapObject mapObject2 : gameEngine.tileMap.objectsLayer.mapObjects) {
                    if ("camera_pan".equalsIgnoreCase(mapObject2.type) && this.L == Integer.parseInt(mapObject2.getPropertyOrDefault("index", "-1"))) {
                        mapObject = mapObject2;
                    }
                }
            }
            if (mapObject == null) {
                this.L = 0;
            } else {
                float worldWidth = mapObject.x;
                float worldHeight = mapObject.y;
                if (worldWidth < gameEngine.halfVisibleWorldWidth + 2.0f) {
                    worldWidth = gameEngine.halfVisibleWorldWidth + 2.0f;
                }
                if (worldHeight < gameEngine.halfVisibleWorldHeight + 2.0f) {
                    worldHeight = gameEngine.halfVisibleWorldHeight + 2.0f;
                }
                if (worldWidth > (gameEngine.tileMap.getWorldWidth() - gameEngine.halfVisibleWorldWidth) - 2.0f) {
                    worldWidth = (gameEngine.tileMap.getWorldWidth() - gameEngine.halfVisibleWorldWidth) - 2.0f;
                }
                if (worldHeight > (gameEngine.tileMap.getWorldHeight() - gameEngine.halfVisibleWorldHeight) - 2.0f) {
                    worldHeight = (gameEngine.tileMap.getWorldHeight() - gameEngine.halfVisibleWorldHeight) - 2.0f;
                }
                float angleBetweenPoints = Utility.getAngleBetweenPoints(gameEngine.viewpointX + gameEngine.halfVisibleWorldWidth, gameEngine.viewpointY + gameEngine.halfVisibleWorldHeight, worldWidth, worldHeight);
                float fDistanceSq = Utility.distanceSq(gameEngine.viewpointX + gameEngine.halfVisibleWorldWidth, gameEngine.viewpointY + gameEngine.halfVisibleWorldHeight, worldWidth, worldHeight);
                if (this.M == 0.0f && (fDistanceSq < 225.0f || gameEngine.wasCameraClamped)) {
                    this.L++;
                    this.M = 50.0f;
                }
                float f2 = 0.45f * f;
                gameEngine.viewpointX += Utility.fastCos(angleBetweenPoints) * f2;
                gameEngine.viewpointY += Utility.fastSin(angleBetweenPoints) * f2;
                gameEngine.setViewpoint(gameEngine.viewpointX, gameEngine.viewpointY);
                gameEngine.clampCameraPosition();
            }
        }
        if (this.isSurvival) {
            if (!this.N) {
                if (!this.isDefeatWave) {
                    this.z = Utility.moveTowardsZero(this.z, f);
                }
                if (this.z == 0.0f && !this.isDefeatWave) {
                    this.r++;
                    this.A = 180.0f;
                    PointF pointF = (PointF) this.D.get(Utility.getDeterministicRandomIntInRange(0, this.D.size() - 1, this.r));
                    if (!this.survivalWavesClassic) {
                        this.s = b(false).toString();
                        waveunitsVarC = b(true);
                    } else {
                        this.s = c(false).toString();
                        waveunitsVarC = c(true);
                    }
                    this.z = 1800.0f;
                    if (!this.survivalWavesClassic) {
                        if (this.y > 0) {
                            this.z -= (this.y * 3) * 60;
                        } else {
                            this.z -= (this.y * 9) * 60;
                        }
                    }
                    waveunitsVarC.a(pointF.x, pointF.y);
                    this.t = null;
                }
            } else if (!this.isDefeatWave) {
                MissionWave waveVarD = d();
                if (waveVarD != null) {
                    if (waveVarD.e * 1000 < gameEngine.gameTimeMillis) {
                        waveVarD.a();
                        this.r++;
                    }
                } else if (!gameEngine.hasWonGame && !gameEngine.replayEngine.j()) {
                    gameEngine.gameUI.startGameEndSequence();
                }
            }
        }
        if (this.showIntro) {
            this.showIntro = false;
            if (this.introText != null) {
                gameEngine.showMessageBox("Briefing", this.introText);
            }
        }
        if (i > this.lastSaveTime + 250) {
            this.lastSaveTime = i;
            a(i);
        }
        if (i > this.lastAutosaveTime + 1000) {
            this.lastAutosaveTime = i;
            if (h()) {
                h();
                h();
            }
            boolean z = false;
            if (gameEngine.playerTeam != null) {
                if (gameEngine.playerTeam.hasTeamVictory()) {
                }
                if (gameEngine.playerTeam.isSpectatorTeamColor()) {
                    z = true;
                }
            }
            if (!gameEngine.hasWonGame && !gameEngine.hasLostGame && !gameEngine.replayEngine.j() && !z) {
                boolean z2 = true;
                boolean z3 = true;
                if (this.waveSpawnMode == WaveSpawnMode.none) {
                    z2 = false;
                } else if (this.waveSpawnMode == WaveSpawnMode.requiredObjectives) {
                    Iterator it = this.g.iterator();
                    while (it.hasNext()) {
                        if (!((TriggerWrapper) it.next()).b()) {
                            z2 = false;
                        }
                    }
                } else if (gameEngine.playerTeam != null) {
                    Iterator it2 = BaseUnit.bE.iterator();
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        }
                        BaseUnit baseUnit = (BaseUnit) it2.next();
                        if (gameEngine.playerTeam.c(baseUnit.team) && a(this.waveSpawnMode, baseUnit)) {
                            z2 = false;
                            break;
                        }
                    }
                }
                if (this.f == WaveSpawnMode.none || this.f == WaveSpawnMode.requiredObjectives) {
                    z3 = false;
                } else if (gameEngine.playerTeam != null) {
                    Iterator it3 = BaseUnit.bE.iterator();
                    while (true) {
                        if (!it3.hasNext()) {
                            break;
                        }
                        BaseUnit baseUnit2 = (BaseUnit) it3.next();
                        if (gameEngine.playerTeam.d(baseUnit2.team) && a(this.f, baseUnit2)) {
                            z3 = false;
                            break;
                        }
                    }
                }
                if (z3 && !z2) {
                    gameEngine.gameUI.endGameSequence();
                }
                if (z2) {
                    gameEngine.gameUI.startGameEndSequence();
                    if (gameEngine.gameTimeMillis > 1500) {
                        gameEngine.settingsEngine.numberOfWins++;
                        gameEngine.settingsEngine.save();
                    }
                }
            }
            if (this.isSurvival && !this.isDefeatWave) {
                boolean z4 = true;
                for (BaseUnit baseUnit3 : BaseUnit.bE) {
                    if ((baseUnit3 instanceof CommandCenter) || baseUnit3.isTargetable) {
                        if (!baseUnit3.isDead && !baseUnit3.u() && baseUnit3.team == gameEngine.playerTeam) {
                            z4 = false;
                        }
                    }
                }
                if (z4) {
                    this.isDefeatWave = true;
                    gameEngine.gameUI.endGameSequence();
                }
            }
        }
    }

    public boolean a(WaveSpawnMode waveSpawnMode, BaseUnit baseUnit) {
        if (!(baseUnit instanceof OrderableUnit) || baseUnit.isDead || baseUnit.isExcludedFromDefeatCheck() || waveSpawnMode == WaveSpawnMode.none) {
            return false;
        }
        if (waveSpawnMode == WaveSpawnMode.allUnitsAndBuildings) {
            return true;
        }
        if (waveSpawnMode == WaveSpawnMode.allBuildings) {
            return baseUnit.bI();
        }
        if (waveSpawnMode == WaveSpawnMode.commandCenter) {
            return (baseUnit instanceof CommandCenter) || baseUnit.isTargetable;
        }
        if (waveSpawnMode == WaveSpawnMode.mainBuildings) {
            return baseUnit.bI() && baseUnit.bJ() && !(baseUnit instanceof TurretFactory) && !(baseUnit instanceof ResourceExtractor);
        }
        if (waveSpawnMode == WaveSpawnMode.noConstructionOrTech) {
            if (baseUnit.bJ() || baseUnit.canMove()) {
                return true;
            }
            return false;
        }
        if (waveSpawnMode == WaveSpawnMode.requiredObjectives) {
            return false;
        }
        return false;
    }

    public void h(String str) {
        GameEngine.log("Map Script: " + str);
    }

    public void a(MapTrigger mapTrigger) {
        if (g()) {
            h("Activiated trigger:" + mapTrigger.name + " (id:" + mapTrigger.rawId + ")");
        }
    }

    public boolean g() {
        return a && GameEngine.getInstance().isDebugTempMode;
    }

    public static void i(String str) {
        NetworkEngine.reportDesync("Map ScriptError: " + str);
    }

    public void a(int i) {
        for (MapTrigger mapTrigger : this.J) {
            if (mapTrigger.isActive && mapTrigger.q != -1 && i >= mapTrigger.activationTime + mapTrigger.q) {
                mapTrigger.isActive = false;
                mapTrigger.u = false;
            }
            if (!mapTrigger.isActive && !mapTrigger.u && mapTrigger.d()) {
                mapTrigger.u = true;
            }
            if ((mapTrigger.isActive || mapTrigger.u) && mapTrigger.e.b()) {
                mapTrigger.isActive = false;
                mapTrigger.u = false;
                mapTrigger.hasCompleted = true;
            }
            if (mapTrigger.isActive && mapTrigger.repeatDelay > 0 && i >= mapTrigger.activationTime + mapTrigger.repeatDelay) {
                mapTrigger.u = true;
            }
            if (mapTrigger.u) {
                mapTrigger.u = false;
                try {
                    TriggerExecutor.a(this, mapTrigger);
                } catch (MapLoadException e) {
                    e.printStackTrace();
                    mapTrigger.g("Error activating trigger: " + e.getMessage());
                }
            }
        }
    }


    public boolean h() {
        boolean var1 = false;
        GameEngine var2 = GameEngine.getInstance();
        BaseUnit[] var3 = BaseUnit.bE.a();
        int var4 = 0;

        for (int var5 = BaseUnit.bE.size(); var4 < var5; var4++) {
            BaseUnit var6 = var3[var4];
            if (var6.team == PlayerTeam.TEAM_ALL && var6 instanceof OrderableUnit && var6.isAlive() && !var6.o()) {
                int var7 = 0;

                for (int var8 = BaseUnit.bE.size(); var7 < var8; var7++) {
                    BaseUnit var9 = var3[var7];
                    boolean var10;
                    if (!var2.isInNetworkOrReplay()) {
                        var10 = var9.team == var2.playerTeam;
                    } else {
                        var10 = !var9.team.isTeamSpectator;
                        if (var6.canBeCapturedByAI()) {
                            var10 = true;
                        }
                    }

                    if (var9.team != null && var9.team.teamId < 0) {
                        var10 = false;
                    }

                    if (var10
                            && var9.team != var6.team
                            && var9 instanceof OrderableUnit
                            && !var9.i()
                            && var9.isAlive()
                            && Utility.distanceSq(var9.posX, var9.posY, var6.posX, var6.posY) < 28900.0F) {
                        var6.changeTeam(var9.team);
                        var6.selectionFlashTimer = 60.0F;
                        var1 = true;
                        break;
                    }
                }
            }
        }

        return var1;
    }
}
