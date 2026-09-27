package com.corrodinggames.rts.game;

import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.FogRevealer;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.UnitMovementType;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.game.units.buildings.BaseBuilding;
import com.corrodinggames.rts.game.units.custom.AnimationSet;
import com.corrodinggames.rts.game.units.custom.AnimationTag;
import com.corrodinggames.rts.game.units.custom.BuiltInEffectType;
import com.corrodinggames.rts.game.units.custom.CustomUnit;
import com.corrodinggames.rts.game.units.custom.CustomUnitSpawnList;
import com.corrodinggames.rts.game.units.custom.EffectTemplate;
import com.corrodinggames.rts.game.units.custom.UnitEventType;
import com.corrodinggames.rts.game.units.custom.UnitSpawner;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.GameObject;
import com.corrodinggames.rts.gameFramework.PositionedObject;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.audio.SoundEngine;
import com.corrodinggames.rts.gameFramework.effects.Effect;
import com.corrodinggames.rts.gameFramework.effects.EffectEmitter;
import com.corrodinggames.rts.gameFramework.effects.EffectQuality;
import com.corrodinggames.rts.gameFramework.effects.EffectType;
import com.corrodinggames.rts.gameFramework.effects.SpriteSheet;
import com.corrodinggames.rts.gameFramework.graphics.GamePaint;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.network.ConnectionStatus;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.utility.FastArrayList;
import com.corrodinggames.rts.gameFramework.utility.GameViewUtils;
import com.corrodinggames.rts.gameFramework.utility.SlickToAndroidKeycodes;
import com.corrodinggames.rts.gameFramework.utility.UnitList;
import com.corrodinggames.rts.gameFramework.utility.Vector3D;
import java.io.IOException;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.f */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/f.class */
public class Projectile extends PositionedObject {
    /* JADX INFO: renamed from: g */
    public ProjectileTemplate template;
    /* JADX INFO: renamed from: h */
    public float lifeTimer;
    /* JADX INFO: renamed from: i */
    public float initialDelay;
    /* JADX INFO: renamed from: j */
    public BaseUnit sourceUnit;
    /* JADX INFO: renamed from: k */
    public short techLevel;
    /* JADX INFO: renamed from: l */
    public BaseUnit targetUnit;
    /* JADX INFO: renamed from: m */
    public boolean hasFixedTarget;
    /* JADX INFO: renamed from: n */
    public float targetX;
    /* JADX INFO: renamed from: o */
    public float targetY;
    /* JADX INFO: renamed from: p */
    public float targetZ;
    /* JADX INFO: renamed from: q */
    public Projectile parentProjectile;
    /* JADX INFO: renamed from: r */
    public float targetSpeed;
    /* JADX INFO: renamed from: s */
    public float acceleration;
    /* JADX INFO: renamed from: t */
    public float speed;
    /* JADX INFO: renamed from: u */
    public float velocityX;
    /* JADX INFO: renamed from: v */
    public float velocityY;
    /* JADX INFO: renamed from: w */
    public float velocityZ;
    /* JADX INFO: renamed from: x */
    public float renderScale;
    /* JADX INFO: renamed from: y */
    public float glowScale;
    /* JADX INFO: renamed from: z */
    public boolean renderShadow;
    /* JADX INFO: renamed from: A */
    public boolean isInstantHit;
    /* JADX INFO: renamed from: B */
    public boolean persistsAfterExplosion;
    public boolean C;
    /* JADX INFO: renamed from: D */
    public boolean isNuke;
    /* JADX INFO: renamed from: E */
    public boolean isBeam;
    /* JADX INFO: renamed from: F */
    public float maxLifeTime;
    /* JADX INFO: renamed from: G */
    public boolean spawnEmitterOnHit;
    /* JADX INFO: renamed from: H */
    public float damageMultiplier;
    /* JADX INFO: renamed from: I */
    public float randomSeed;
    /* JADX INFO: renamed from: J */
    public float age;
    /* JADX INFO: renamed from: K */
    public float trackOffsetX;
    /* JADX INFO: renamed from: L */
    public float trackOffsetY;
    /* JADX INFO: renamed from: M */
    public boolean renderJitter;
    /* JADX INFO: renamed from: N */
    public float jitterTimer;
    /* JADX INFO: renamed from: O */
    public float[] jitterOffsets;
    /* JADX INFO: renamed from: P */
    public short textureFrame;
    /* JADX INFO: renamed from: Q */
    public short frameIndex;
    /* JADX INFO: renamed from: R */
    public short textureType;
    /* JADX INFO: renamed from: S */
    public boolean hasExploded;
    public boolean T;
    /* JADX INFO: renamed from: U */
    public float damage;
    /* JADX INFO: renamed from: V */
    public boolean removalComplete;
    /* JADX INFO: renamed from: W */
    public float explosionAnimTimer;
    /* JADX INFO: renamed from: X */
    public float explosionAnimDuration;
    /* JADX INFO: renamed from: Y */
    public float splashDamage;
    /* JADX INFO: renamed from: Z */
    public float explosionRadius;
    /* JADX INFO: renamed from: aa */
    public boolean damageEnemiesOnly;
    /* JADX INFO: renamed from: ab */
    public boolean noFriendlyFire;
    /* JADX INFO: renamed from: ac */
    public boolean canHitSubmerged;
    /* JADX INFO: renamed from: ad */
    public boolean excludesAir;
    /* JADX INFO: renamed from: ae */
    public boolean matchesTargetAltitude;
    /* JADX INFO: renamed from: af */
    public boolean explodesOnTimerEnd;
    /* JADX INFO: renamed from: ag */
    public float pushForce;
    /* JADX INFO: renamed from: ah */
    public float pushBase;
    /* JADX INFO: renamed from: ai */
    public float damageToBuildings;
    /* JADX INFO: renamed from: aj */
    public float damageToAir;
    /* JADX INFO: renamed from: ak */
    public float targetDamageMultiplier;
    /* JADX INFO: renamed from: al */
    public float splashDamageMultiplier;
    /* JADX INFO: renamed from: am */
    public float globalDamageMultiplier;
    /* JADX INFO: renamed from: an */
    public float armourIgnore;
    /* JADX INFO: renamed from: ao */
    public boolean trackHitUnits;
    /* JADX INFO: renamed from: ap */
    public FastArrayList hitUnits;
    /* JADX INFO: renamed from: ar */
    public int color;
    /* JADX INFO: renamed from: as */
    public boolean isAreaDamage;
    /* JADX INFO: renamed from: at */
    public boolean collidesWithTerrain;
    /* JADX INFO: renamed from: au */
    public GameObject followObject;
    /* JADX INFO: renamed from: av */
    public int followNodeIndex;
    /* JADX INFO: renamed from: aw */
    public float followLastX;
    /* JADX INFO: renamed from: ax */
    public float followLastY;
    /* JADX INFO: renamed from: ay */
    public float followLastZ;
    /* JADX INFO: renamed from: az */
    public float angle;
    /* JADX INFO: renamed from: aA */
    public float explosionSearchRadius;
    public boolean aB;
    /* JADX INFO: renamed from: aC */
    public boolean fliesToPosition;
    /* JADX INFO: renamed from: aD */
    public int depth;
    /* JADX INFO: renamed from: aE */
    public AnimationSet animationSet;
    /* JADX INFO: renamed from: aF */
    public float retargetTimer;
    /* JADX INFO: renamed from: aG */
    public boolean autoRetarget;
    /* JADX INFO: renamed from: aH */
    public boolean isBallistic;
    /* JADX INFO: renamed from: aI */
    public float minHeight;
    /* JADX INFO: renamed from: aJ */
    public float maxHeight;
    /* JADX INFO: renamed from: aK */
    public boolean reachedApex;
    /* JADX INFO: renamed from: aL */
    public float verticalVelocity;
    /* JADX INFO: renamed from: aM */
    public boolean hasTrail;
    /* JADX INFO: renamed from: aN */
    public float trailTimer;
    /* JADX INFO: renamed from: aO */
    public float explosionParticleTimer;
    public Effect aP;
    /* JADX INFO: renamed from: aQ */
    public boolean isSmallExplosion;
    /* JADX INFO: renamed from: aR */
    public boolean playsHitSound;
    /* JADX INFO: renamed from: aS */
    public boolean removeRequested;
    /* JADX INFO: renamed from: aT */
    public float renderAngle;
    /* JADX INFO: renamed from: aU */
    public boolean angleInitialized;
    /* JADX INFO: renamed from: aY */
    public boolean revealsFog;
    /* JADX INFO: renamed from: aZ */
    public boolean visibilityChecked;
    /* JADX INFO: renamed from: aV */
    float hitX;
    /* JADX INFO: renamed from: aW */
    float hitY;
    /* JADX INFO: renamed from: aX */
    float hitZ;
    /* JADX INFO: renamed from: bn */
    private boolean hasHit;
    public static final UnitList bi;
    public GamePaint bj;
    public static GamePaint bk;
    public static int bl;
    public static final FastArrayList<Projectile> a = new FastArrayList();
    private static final Projectile bm = new Projectile(true);
    static Texture b = null;
    static Texture c = null;
    static Texture d = null;
    static final Rect e = new Rect();
    static final RectF f = new RectF();
    static final int aq = Color.a(255, 255, 255, 255);
    public static final GamePaint ba = new GamePaint();
    public static final Paint bb = new Paint();
    public static final Paint bd = new Paint();
    public static final Paint be = new Paint();
    public static final Paint bf = new Paint();
    public static final Paint bg = new Paint();
    public static final Paint bh = new Paint();
    public static final Paint bc = new GamePaint();

    static {
        bc.b(-16777216);
        bc.c(108);
        bd.a(80, 255, 0, 0);
        bd.a(true);
        bd.a(5.0f);
        be.a(30, 255, 0, 0);
        be.a(true);
        be.a(8.0f);
        bf.a(80, 128, 166, 255);
        bf.a(true);
        bf.a(5.0f);
        bg.a(150, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_WAKEUP, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_CS, 255);
        bg.a(true);
        bg.a(3.0f);
        bh.a(110, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_WAKEUP, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_CS, 255);
        bh.a(true);
        bh.a(8.0f);
        bi = new UnitList();
        bk = null;
        bl = 0;
    }

    public Projectile(boolean z) {
        super(z);
        this.template = ProjectileTemplate.a;
        this.techLevel = (short) -1;
        this.targetSpeed = -1.0f;
        this.acceleration = 0.1f;
        this.renderScale = 2.0f;
        this.glowScale = -1.0f;
        this.renderShadow = true;
        this.damageMultiplier = 1.0f;
        this.textureFrame = (short) -1;
        this.frameIndex = (short) -1;
        this.textureType = (short) 0;
        this.hasExploded = true;
        this.removalComplete = false;
        this.explosionAnimTimer = 0.0f;
        this.explosionAnimDuration = 0.0f;
        this.noFriendlyFire = false;
        this.canHitSubmerged = false;
        this.excludesAir = false;
        this.matchesTargetAltitude = true;
        this.damageToBuildings = 1.0f;
        this.damageToAir = 1.0f;
        this.targetDamageMultiplier = 1.0f;
        this.splashDamageMultiplier = 1.0f;
        this.globalDamageMultiplier = 1.0f;
        this.color = aq;
        this.followNodeIndex = -1;
        this.minHeight = 40.0f;
        this.maxHeight = 60.0f;
        this.reachedApex = false;
        this.verticalVelocity = 2.0f;
        this.playsHitSound = true;
        this.renderAngle = 0.0f;
        if (!z) {
            a.add(this);
        }
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void remove() {
        a.remove(this);
        super.remove();
    }

    public static Projectile a(Projectile projectile) {
        Projectile projectile2 = bm;
        projectile2.depth = -1;
        if (projectile == null) {
            projectile2.globalDamageMultiplier = 1.0f;
            projectile2.targetDamageMultiplier = 1.0f;
            projectile2.splashDamageMultiplier = 1.0f;
            projectile2.armourIgnore = 0.0f;
        } else {
            projectile2.globalDamageMultiplier = projectile.globalDamageMultiplier;
            projectile2.targetDamageMultiplier = projectile.targetDamageMultiplier;
            projectile2.splashDamageMultiplier = projectile.splashDamageMultiplier;
            projectile2.armourIgnore = projectile.armourIgnore;
        }
        return projectile2;
    }

    public static Projectile a(BaseUnit baseUnit, float f2, float f3) {
        Projectile projectile = new Projectile(false);
        projectile.sourceUnit = baseUnit;
        projectile.posX = f2;
        projectile.posY = f3;
        projectile.color = Color.a(255, 100, 30, 30);
        projectile.drawOrder = baseUnit.drawOrder + 1;
        projectile.drawLayer = 4;
        return projectile;
    }

    public static Projectile a(BaseUnit baseUnit, float f2, float f3, float f4, int i) {
        Projectile projectileA = a(baseUnit, f2, f3);
        projectileA.posZ = f4;
        projectileA.techLevel = (short) i;
        projectileA.randomSeed = Utility.getDeterministicRandomFloatForUnit(baseUnit, 0.0f, 1.0f, baseUnit.unitCounter);
        baseUnit.unitCounter++;
        return projectileA;
    }

    public static void a(BaseUnit baseUnit, BaseUnit baseUnit2, float f2, Projectile projectile, boolean z) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine.isUnitInvincibilityEnabled && f2 > 0.0f) {
            f2 = 0.0f;
        }
        if (baseUnit2 != null && !baseUnit2.isDead) {
            if (projectile != null && projectile.template.convertHitToSourceTeam && baseUnit != null) {
                baseUnit2.changeTeam(baseUnit.team);
            }
            if (projectile != null) {
                if (projectile.damageToBuildings != 1.0f && baseUnit2.bI()) {
                    f2 *= projectile.damageToBuildings;
                }
                if (projectile.damageToAir != 1.0f && baseUnit2.i()) {
                    f2 *= projectile.damageToAir;
                }
            }
            if (f2 < 0.0f) {
                baseUnit2.calculateTurnSpeed(baseUnit, -f2, projectile);
            } else {
                boolean z2 = !baseUnit2.isDead && baseUnit2.currentHealth > 0.0f;
                baseUnit2.applyDamage(baseUnit, f2, projectile);
                float f3 = f2;
                if (baseUnit2.isDamageImmune()) {
                    f3 = 0.0f;
                }
                if (f3 > 0.0f) {
                    gameEngine.gameStatistics.a(baseUnit, baseUnit2, f3);
                }
                if (baseUnit != null) {
                    baseUnit.totalDamageDealt += f3;
                    if (z2 && (baseUnit2.isDead || baseUnit2.currentHealth < 0.0f)) {
                        baseUnit.killCount++;
                        baseUnit.a(UnitEventType.killedAnyUnit, baseUnit2);
                    }
                }
            }
            if (projectile != null && !baseUnit2.isDead) {
                float fBQ = baseUnit2.bQ();
                if (fBQ != -1.0f) {
                    float angleBetweenPoints = Utility.getAngleBetweenPoints(projectile.posX, projectile.posY, baseUnit2.posX, baseUnit2.posY);
                    float f4 = 100.0f / fBQ;
                    baseUnit2.velocityX += Utility.fastCos(angleBetweenPoints) * f4;
                    baseUnit2.velocityY += Utility.fastSin(angleBetweenPoints) * f4;
                }
            }
        }
    }

    public void a(BaseUnit baseUnit, float f2, float f3, float f4) {
        this.sourceUnit = baseUnit;
        this.posX = f2;
        this.posY = f3;
        this.posZ = f4;
        this.hasHit = false;
        this.removalComplete = false;
    }

    public static void c() {
        GameEngine gameEngine = GameEngine.getInstance();
        b = gameEngine.renderGraphicsEngine.a(com.corrodinggames.rts.R.drawable.projectiles);
        c = gameEngine.renderGraphicsEngine.a(com.corrodinggames.rts.R.drawable.projectiles2);
        d = gameEngine.renderGraphicsEngine.a(com.corrodinggames.rts.R.drawable.projectiles_large);
    }

    public void b() {
        if (this.isNuke) {
            GameEngine gameEngine = GameEngine.getInstance();
            Effect effectCreateSmallExplosionInternal = gameEngine.effectManager.createSmallExplosionInternal(this.posX, this.posY, this.posZ, 0);
            if (effectCreateSmallExplosionInternal != null) {
                effectCreateSmallExplosionInternal.scaleFrom = 0.7f;
                effectCreateSmallExplosionInternal.scaleTo = 2.1f;
                effectCreateSmallExplosionInternal.drawLayer = (short) 2;
                effectCreateSmallExplosionInternal.lifeTimer = 90.0f;
                effectCreateSmallExplosionInternal.lifeMax = effectCreateSmallExplosionInternal.lifeTimer;
            }
            gameEngine.soundEngine.playSound(SoundEngine.buildingExplodeSound, 0.8f, this.posX, this.posY);
        }
        remove();
    }

    @Override // com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeFloat(this.lifeTimer);
        gameOutputStream.writeUnitIdOrNullBaseUnit(this.sourceUnit);
        gameOutputStream.writeUnitIdOrNullBaseUnit(this.targetUnit);
        gameOutputStream.writeFloat(this.speed);
        gameOutputStream.writeInt(99);
        gameOutputStream.writeBoolean(this.isInstantHit);
        gameOutputStream.writeBoolean(this.persistsAfterExplosion);
        gameOutputStream.writeBoolean(this.hasExploded);
        gameOutputStream.writeBoolean(this.T);
        gameOutputStream.writeFloat(this.damage);
        gameOutputStream.writeFloat(this.splashDamage);
        gameOutputStream.writeFloat(this.explosionRadius);
        gameOutputStream.writeInt(this.color);
        gameOutputStream.writeBoolean(this.isBallistic);
        gameOutputStream.writeFloat(this.minHeight);
        gameOutputStream.writeFloat(this.maxHeight);
        gameOutputStream.writeBoolean(this.reachedApex);
        gameOutputStream.writeFloat(this.verticalVelocity);
        gameOutputStream.writeBoolean(this.hasTrail);
        gameOutputStream.writeFloat(this.trailTimer);
        gameOutputStream.writeBoolean(this.isSmallExplosion);
        gameOutputStream.writeBoolean(this.playsHitSound);
        gameOutputStream.writeBoolean(this.hasHit);
        gameOutputStream.writeBoolean(this.removeRequested);
        gameOutputStream.writeBoolean(this.renderJitter);
        gameOutputStream.writeShort(this.textureFrame);
        gameOutputStream.writeFloat(this.targetSpeed);
        gameOutputStream.writeFloat(this.acceleration);
        gameOutputStream.writeBoolean(this.isAreaDamage);
        gameOutputStream.writeBoolean(this.collidesWithTerrain);
        gameOutputStream.writeFloat(this.angle);
        gameOutputStream.writeFloat(this.explosionSearchRadius);
        gameOutputStream.writeBoolean(this.aB);
        gameOutputStream.writeBoolean(this.fliesToPosition);
        gameOutputStream.writeBoolean(false);
        gameOutputStream.writeFloat(0.0f);
        gameOutputStream.writeFloat(0.0f);
        gameOutputStream.writeBoolean(this.isBeam);
        gameOutputStream.writeFloat(this.maxLifeTime);
        gameOutputStream.writeFloat(this.age);
        gameOutputStream.writeFloat(this.trackOffsetX);
        gameOutputStream.writeFloat(this.trackOffsetY);
        gameOutputStream.writeBoolean(this.hasFixedTarget);
        gameOutputStream.writeFloat(this.targetX);
        gameOutputStream.writeFloat(this.targetY);
        gameOutputStream.writeBoolean(this.C);
        gameOutputStream.writeBoolean(this.isNuke);
        gameOutputStream.writeObjectId(this.parentProjectile);
        gameOutputStream.writeFloat(this.hitX);
        gameOutputStream.writeFloat(this.hitY);
        gameOutputStream.writeFloat(this.hitZ);
        gameOutputStream.writeBoolean(this.removalComplete);
        gameOutputStream.writeFloat(this.explosionAnimTimer);
        gameOutputStream.writeFloat(this.explosionAnimDuration);
        gameOutputStream.writeBoolean(this.angleInitialized);
        gameOutputStream.writeShort(this.textureType);
        gameOutputStream.writeBoolean(this.trackHitUnits);
        gameOutputStream.writeGameObjectList(this.hitUnits);
        gameOutputStream.writeShort(this.frameIndex);
        gameOutputStream.writeFloat(this.renderScale);
        gameOutputStream.writeBoolean(this.damageEnemiesOnly);
        gameOutputStream.writeBoolean(this.excludesAir);
        gameOutputStream.writeBoolean(this.spawnEmitterOnHit);
        gameOutputStream.writeFloat(this.damageMultiplier);
        gameOutputStream.writeBoolean(this.matchesTargetAltitude);
        gameOutputStream.writeBoolean(this.autoRetarget);
        gameOutputStream.writeBoolean(this.renderShadow);
        gameOutputStream.writeFloat(this.glowScale);
        gameOutputStream.writeFloat(this.explosionParticleTimer);
        gameOutputStream.writeFloat(this.initialDelay);
        gameOutputStream.writeBoolean(this.revealsFog);
        gameOutputStream.writeBoolean(this.explodesOnTimerEnd);
        gameOutputStream.writeFloat(this.pushForce);
        gameOutputStream.writeFloat(this.pushBase);
        gameOutputStream.writeFloat(this.damageToBuildings);
        gameOutputStream.writeFloat(this.damageToAir);
        gameOutputStream.writeInt(0);
        gameOutputStream.writeFloat(0.0f);
        gameOutputStream.writeFloat(0.0f);
        gameOutputStream.writeUnitTypeId((UnitType) null);
        gameOutputStream.writeInt(0);
        gameOutputStream.writeBoolean(false);
        AnimationTag.a(this.animationSet, gameOutputStream);
        gameOutputStream.writeFloat(this.targetDamageMultiplier);
        gameOutputStream.writeFloat(this.splashDamageMultiplier);
        gameOutputStream.writeBoolean(this.noFriendlyFire);
        gameOutputStream.writeBoolean(this.canHitSubmerged);
        gameOutputStream.writeFloat(this.armourIgnore);
        gameOutputStream.writeBoolean(false);
        ProjectileTemplate.a(this.template, gameOutputStream);
        boolean z = (this.followObject == null || this.followObject.isDestroyed) ? false : true;
        gameOutputStream.writeBoolean(z);
        if (z) {
            gameOutputStream.writeObjectId(this.followObject);
            gameOutputStream.writeFloat(this.followLastX);
            gameOutputStream.writeFloat(this.followLastY);
            gameOutputStream.writeFloat(this.followLastZ);
        }
        gameOutputStream.writeShort(this.techLevel);
        gameOutputStream.writeInt(this.depth);
        gameOutputStream.writeFloat(this.globalDamageMultiplier);
        gameOutputStream.writeFloat(this.targetZ);
        gameOutputStream.writeInt(this.followNodeIndex);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.gameFramework.PositionedObject, com.corrodinggames.rts.gameFramework.GameObject
    public void a(GameInputStream gameInputStream) throws IOException {
        this.lifeTimer = gameInputStream.readFloat();
        this.sourceUnit = gameInputStream.readBaseUnit();
        this.targetUnit = gameInputStream.readBaseUnit(ConnectionStatus.EXPECTED);
        this.speed = gameInputStream.readFloat();
        this.renderScale = gameInputStream.readInt();
        this.isInstantHit = gameInputStream.readBoolean();
        this.persistsAfterExplosion = gameInputStream.readBoolean();
        this.hasExploded = gameInputStream.readBoolean();
        this.T = gameInputStream.readBoolean();
        this.damage = gameInputStream.readFloat();
        this.splashDamage = gameInputStream.readFloat();
        this.explosionRadius = gameInputStream.readFloat();
        this.color = gameInputStream.readInt();
        this.isBallistic = gameInputStream.readBoolean();
        this.minHeight = gameInputStream.readFloat();
        this.maxHeight = gameInputStream.readFloat();
        this.reachedApex = gameInputStream.readBoolean();
        this.verticalVelocity = gameInputStream.readFloat();
        this.hasTrail = gameInputStream.readBoolean();
        this.trailTimer = gameInputStream.readFloat();
        this.isSmallExplosion = gameInputStream.readBoolean();
        this.playsHitSound = gameInputStream.readBoolean();
        this.hasHit = gameInputStream.readBoolean();
        if (gameInputStream.getProtocolVersion() >= 7) {
            this.removeRequested = gameInputStream.readBoolean();
        }
        if (gameInputStream.getProtocolVersion() >= 13) {
            this.renderJitter = gameInputStream.readBoolean();
            this.textureFrame = gameInputStream.readShortValue();
        }
        if (gameInputStream.getProtocolVersion() >= 16) {
            this.targetSpeed = gameInputStream.readFloat();
            this.acceleration = gameInputStream.readFloat();
        }
        if (gameInputStream.getProtocolVersion() >= 17) {
            this.isAreaDamage = gameInputStream.readBoolean();
            this.collidesWithTerrain = gameInputStream.readBoolean();
            this.angle = gameInputStream.readFloat();
            this.explosionSearchRadius = gameInputStream.readFloat();
            this.aB = gameInputStream.readBoolean();
            this.fliesToPosition = gameInputStream.readBoolean();
        }
        if (gameInputStream.getProtocolVersion() >= 18) {
            gameInputStream.readBoolean();
            gameInputStream.readFloat();
            gameInputStream.readFloat();
        }
        if (gameInputStream.getProtocolVersion() >= 28) {
            this.isBeam = gameInputStream.readBoolean();
            this.maxLifeTime = gameInputStream.readFloat();
            this.age = gameInputStream.readFloat();
            this.trackOffsetX = gameInputStream.readFloat();
            this.trackOffsetY = gameInputStream.readFloat();
        }
        if (gameInputStream.getProtocolVersion() >= 29) {
            this.hasFixedTarget = gameInputStream.readBoolean();
            this.targetX = gameInputStream.readFloat();
            this.targetY = gameInputStream.readFloat();
            this.C = gameInputStream.readBoolean();
            this.isNuke = gameInputStream.readBoolean();
            this.parentProjectile = (Projectile) gameInputStream.readGameObject(Projectile.class);
            this.hitX = gameInputStream.readFloat();
            this.hitY = gameInputStream.readFloat();
            this.hitZ = gameInputStream.readFloat();
            this.removalComplete = gameInputStream.readBoolean();
            this.explosionAnimTimer = gameInputStream.readFloat();
            this.explosionAnimDuration = gameInputStream.readFloat();
            this.angleInitialized = gameInputStream.readBoolean();
            this.textureType = gameInputStream.readShortValue();
            this.trackHitUnits = gameInputStream.readBoolean();
            FastArrayList fastArrayList = new FastArrayList();
            gameInputStream.readGameObjectList(fastArrayList, BaseUnit.class);
            if (fastArrayList.size() > 0) {
                this.hitUnits = fastArrayList;
            }
            this.frameIndex = gameInputStream.readShortValue();
        }
        if (gameInputStream.getProtocolVersion() >= 35) {
            this.renderScale = gameInputStream.readFloat();
            this.damageEnemiesOnly = gameInputStream.readBoolean();
            this.excludesAir = gameInputStream.readBoolean();
            this.spawnEmitterOnHit = gameInputStream.readBoolean();
        }
        if (gameInputStream.getProtocolVersion() >= 38) {
            this.damageMultiplier = gameInputStream.readFloat();
        }
        if (gameInputStream.getProtocolVersion() >= 39) {
            this.matchesTargetAltitude = gameInputStream.readBoolean();
        }
        if (gameInputStream.getProtocolVersion() >= 41) {
            this.autoRetarget = gameInputStream.readBoolean();
        }
        if (gameInputStream.getProtocolVersion() >= 43) {
            this.renderShadow = gameInputStream.readBoolean();
            this.glowScale = gameInputStream.readFloat();
        }
        if (gameInputStream.getProtocolVersion() >= 44) {
            this.explosionParticleTimer = gameInputStream.readFloat();
        }
        if (gameInputStream.getProtocolVersion() >= 47) {
            this.initialDelay = gameInputStream.readFloat();
        }
        if (gameInputStream.getProtocolVersion() >= 48) {
            this.revealsFog = gameInputStream.readBoolean();
        }
        if (gameInputStream.getProtocolVersion() >= 59) {
            this.explodesOnTimerEnd = gameInputStream.readBoolean();
            this.pushForce = gameInputStream.readFloat();
            this.pushBase = gameInputStream.readFloat();
            this.damageToBuildings = gameInputStream.readFloat();
        }
        if (gameInputStream.getProtocolVersion() >= 60) {
            this.damageToAir = gameInputStream.readFloat();
            gameInputStream.readInt();
            gameInputStream.readFloat();
            gameInputStream.readFloat();
        }
        if (gameInputStream.getProtocolVersion() >= 62) {
            gameInputStream.q();
            gameInputStream.readInt();
            gameInputStream.readBoolean();
        }
        if (gameInputStream.getProtocolVersion() >= 63) {
            this.animationSet = AnimationTag.a(gameInputStream);
        }
        if (gameInputStream.getProtocolVersion() >= 64) {
            this.targetDamageMultiplier = gameInputStream.readFloat();
            this.splashDamageMultiplier = gameInputStream.readFloat();
        }
        if (gameInputStream.getProtocolVersion() >= 66) {
            this.noFriendlyFire = gameInputStream.readBoolean();
            this.canHitSubmerged = gameInputStream.readBoolean();
        }
        if (gameInputStream.getProtocolVersion() >= 67 && gameInputStream.getProtocolVersion() < 78) {
            UnitSpawner.a(gameInputStream, true);
        }
        if (gameInputStream.getProtocolVersion() >= 68) {
            this.armourIgnore = gameInputStream.readFloat();
        }
        if (gameInputStream.getProtocolVersion() >= 77) {
            gameInputStream.readBoolean();
        }
        if (gameInputStream.getProtocolVersion() >= 78) {
            this.template = ProjectileTemplate.a(gameInputStream);
        }
        if (gameInputStream.getProtocolVersion() >= 81 && gameInputStream.readBoolean()) {
            this.followObject = gameInputStream.readGameObject(GameObject.class);
            this.followLastX = gameInputStream.readFloat();
            this.followLastY = gameInputStream.readFloat();
            this.followLastZ = gameInputStream.readFloat();
        }
        if (gameInputStream.getProtocolVersion() >= 83) {
            this.techLevel = gameInputStream.readShortValue();
            this.depth = gameInputStream.readInt();
        }
        if (gameInputStream.getProtocolVersion() >= 88) {
            this.globalDamageMultiplier = gameInputStream.readFloat();
        }
        if (gameInputStream.getProtocolVersion() >= 89) {
            this.targetZ = gameInputStream.readFloat();
            this.followNodeIndex = gameInputStream.readInt();
        }
        super.a(gameInputStream);
    }

    public void d() {
        this.removeRequested = true;
    }

    public void a(BaseUnit baseUnit) {
        float angleBetweenPoints;
        if ((this.pushForce == 0.0f && this.pushBase == 0.0f) || baseUnit.bI()) {
            return;
        }
        if (Utility.distanceSq(this.hitX, this.hitY, baseUnit.posX, baseUnit.posY) > 100.0f) {
            angleBetweenPoints = Utility.getAngleBetweenPoints(this.hitX, this.hitY, baseUnit.posX, baseUnit.posY);
        } else {
            angleBetweenPoints = this.angle;
        }
        float pushMass = this.pushBase + (this.pushForce / baseUnit.getPushMass());
        baseUnit.velocityX += Utility.fastCos(angleBetweenPoints) * pushMass;
        baseUnit.velocityY += Utility.fastSin(angleBetweenPoints) * pushMass;
    }

    public float e() {
        float f2 = 1.0f;
        if (this.age < this.maxLifeTime) {
            f2 = this.age / this.maxLifeTime;
        }
        return f2;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    /* JADX INFO: renamed from: a */
    public void update(float f2) {
        float angleBetweenPoints;
        float fDistanceSq;
        float f3;
        float f4;
        Effect effectCreateSmallExplosionInternal;
        Effect effectCreateSmallExplosionInternal2;
        CustomUnitSpawnList customUnitSpawnListA;
        Effect effectCreateEffectInternal;
        float f5;
        float f6;
        float f7;
        float f8;
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.removeRequested) {
            remove();
        }
        if (this.targetUnit == null && !this.fliesToPosition) {
            remove();
            return;
        }
        if (this.initialDelay > 0.0f) {
            this.initialDelay = Utility.moveTowardsZero(this.initialDelay, f2);
            if (this.initialDelay > 0.0f) {
                return;
            }
        }
        ProjectileTemplate projectileTemplate = this.template;
        if (this.initialDelay == 0.0f) {
            this.initialDelay = -1.0f;
            if (projectileTemplate.spawnProjectilesOnCreate != null) {
                projectileTemplate.spawnProjectilesOnCreate.a(this.posX, this.posY, this.posZ, this.angle, this.sourceUnit, null, false, this.depth + 1, this, null);
            }
        }
        this.lifeTimer = Utility.moveTowardsZero(this.lifeTimer, f2);
        boolean z = false;
        if (this.autoRetarget && (this.targetUnit == null || this.targetUnit.isDead)) {
            z = true;
        }
        if (z) {
            a(projectileTemplate.autoTargetingOnDeadTargetRange, projectileTemplate.autoTargetingOnDeadTargetLead, (AnimationSet) null);
        }
        if (projectileTemplate.retargetingInFlight) {
            this.retargetTimer = Utility.moveTowardsZero(this.retargetTimer, f2);
            if (this.retargetTimer == 0.0f) {
                this.retargetTimer = projectileTemplate.retargetingInFlightSearchDelay;
                a(projectileTemplate.retargetingInFlightSearchRange, projectileTemplate.retargetingInFlightSearchLead, projectileTemplate.retargetingInFlightSearchOnlyTags);
            }
        }
        if (projectileTemplate.R != 0.0f || projectileTemplate.S != 0.0f) {
            float f9 = projectileTemplate.R;
            if (this.targetUnit != null) {
                f9 += this.targetUnit.radius * projectileTemplate.S;
            }
            this.trackOffsetX = Utility.fastSin(((360.0f * this.randomSeed) + (this.age * 1.0f)) % 360.0f) * f9;
            this.trackOffsetY = Utility.fastSin(((360.0f * this.randomSeed) + (this.age * 1.5f)) % 360.0f) * f9;
        }
        if (this.isBeam && this.targetUnit != null) {
            this.trackOffsetX = Utility.fastSin((this.age * 1.0f) % 360.0f) * this.targetUnit.radius * 0.4f;
            this.trackOffsetY = Utility.fastSin((this.age * 1.5f) % 360.0f) * this.targetUnit.radius * 0.4f;
            float f10 = this.targetUnit.posX + this.trackOffsetX;
            float f11 = this.targetUnit.posY + this.trackOffsetY;
            if (this.shouldDraw) {
                this.trailTimer += f2;
                this.explosionParticleTimer += f2;
                if (this.trailTimer > 11.0f) {
                    this.trailTimer = Utility.randomFloatInRange(1.0f, 4.0f);
                    Effect effectCreateEffectInternal2 = gameEngine.effectManager.createEffectInternal(f10, f11, this.targetUnit.posZ, EffectType.custom, false, EffectQuality.low);
                    if (effectCreateEffectInternal2 != null) {
                        effectCreateEffectInternal2.stripIndex = 0;
                        effectCreateEffectInternal2.frameIndex = 0;
                        effectCreateEffectInternal2.drawLayer = (short) 2;
                        effectCreateEffectInternal2.fadeIn = true;
                        effectCreateEffectInternal2.alpha = 0.5f;
                        effectCreateEffectInternal2.lifeMax = 60.0f;
                        effectCreateEffectInternal2.lifeTimer = 60.0f;
                        effectCreateEffectInternal2.scaleFrom = 0.7f;
                        effectCreateEffectInternal2.scaleTo = 0.3f;
                        effectCreateEffectInternal2.shadow = false;
                        effectCreateEffectInternal2.velocityX = Utility.randomFloatInRange(-0.3f, 0.3f);
                        effectCreateEffectInternal2.velocityY = (-0.9f) + Utility.randomFloatInRange(-0.3f, 0.3f);
                    }
                }
                if (this.explosionParticleTimer > 75.0f) {
                    this.explosionParticleTimer = Utility.randomFloatInRange(1.0f, 20.0f);
                    gameEngine.effectManager.createSmallExplosion(f10, f11, this.targetUnit.posZ);
                }
            }
        }
        float f12 = 5.0f;
        boolean z2 = false;
        boolean z3 = false;
        if (!this.fliesToPosition) {
            float f13 = this.targetUnit.posX + this.trackOffsetX;
            float f14 = this.targetUnit.posY + this.trackOffsetY;
            float f15 = this.targetUnit.posZ;
            angleBetweenPoints = Utility.getAngleBetweenPoints(this.posX, this.posY, f13, f14);
            fDistanceSq = Utility.distanceSq(this.posX, this.posY, f13, f14);
            f3 = f15;
            f4 = f3 - this.posZ;
            f12 = this.targetUnit.radius;
            z2 = this.targetUnit instanceof BaseBuilding;
            z3 = this.targetUnit.shield > 10.0f + this.damage;
        } else {
            float f16 = this.angle;
            if (this.parentProjectile != null) {
                float f17 = this.parentProjectile.posX + this.trackOffsetX;
                float f18 = this.parentProjectile.posY + this.trackOffsetY;
                float f19 = this.parentProjectile.posZ;
                angleBetweenPoints = Utility.getAngleBetweenPoints(this.posX, this.posY, f17, f18);
                fDistanceSq = Utility.distanceSq(this.posX, this.posY, f17, f18);
                f3 = f19;
                f4 = f3 - this.posZ;
            } else if (this.targetUnit != null) {
                float f20 = this.targetUnit.posX + this.trackOffsetX;
                float f21 = this.targetUnit.posY + this.trackOffsetY;
                float f22 = this.targetUnit.posZ;
                angleBetweenPoints = Utility.getAngleBetweenPoints(this.posX, this.posY, f20, f21);
                fDistanceSq = Utility.distanceSq(this.posX, this.posY, f20, f21);
                f3 = f22;
                f4 = f3 - this.posZ;
                f12 = this.targetUnit.radius;
                z2 = this.targetUnit instanceof BaseBuilding;
                z3 = this.targetUnit.shield > 10.0f + this.damage;
            } else if (this.hasFixedTarget) {
                float f23 = this.targetX + this.trackOffsetX;
                float f24 = this.targetY + this.trackOffsetY;
                float f25 = this.targetZ;
                angleBetweenPoints = Utility.getAngleBetweenPoints(this.posX, this.posY, f23, f24);
                fDistanceSq = Utility.distanceSq(this.posX, this.posY, f23, f24);
                f3 = f25;
                f4 = f3 - this.posZ;
            } else {
                float f26 = this.targetX + this.trackOffsetX;
                float f27 = this.targetY + this.trackOffsetY;
                angleBetweenPoints = Utility.getAngleBetweenPoints(this.posX, this.posY, f26, f27);
                fDistanceSq = Utility.distanceSq(this.posX, this.posY, f26, f27);
                f3 = 0.0f;
                f4 = 0.0f - this.posZ;
            }
        }
        float f28 = projectileTemplate.O;
        if (fDistanceSq < 225.0f) {
            f28 = projectileTemplate.P;
        }
        if (f28 >= 0.0f) {
            this.angle += Utility.rotateTowardsAngle(this.angle, angleBetweenPoints, f28 * f2);
            angleBetweenPoints = this.angle;
        } else {
            this.angle = angleBetweenPoints;
        }
        boolean z4 = false;
        boolean z5 = false;
        float f29 = angleBetweenPoints;
        if (this.followObject != null && !this.followObject.isDestroyed) {
            if (this.followNodeIndex >= 0) {
                OrderableUnit orderableUnit = (OrderableUnit) this.followObject;
                if (this.followNodeIndex >= orderableUnit.getTechLevel()) {
                    this.followNodeIndex = 0;
                }
                Vector3D vector3DD = orderableUnit.D(this.followNodeIndex);
                f6 = vector3DD.x;
                f7 = vector3DD.y;
                f8 = this.sourceUnit.posZ + vector3DD.z;
            } else {
                f6 = this.followObject.posX;
                f7 = this.followObject.posY;
                f8 = this.followObject.posZ;
            }
            float f30 = f6 - this.followLastX;
            float f31 = f7 - this.followLastY;
            float f32 = f8 - this.followLastZ;
            this.posX += f30;
            this.posY += f31;
            this.posZ += f32;
            this.followLastX = f6;
            this.followLastY = f7;
            this.followLastZ = f8;
        }
        if (!this.isInstantHit) {
            this.posX += this.velocityX * f2;
            this.posY += this.velocityY * f2;
            if (this.velocityZ != 0.0f) {
                this.posZ += this.velocityZ * f2;
                f4 = f3 - this.posZ;
            }
            if (this.posZ > 0.0f) {
                if (projectileTemplate.G != 0.0f) {
                    this.posZ -= projectileTemplate.G * f2;
                    f4 = f3 - this.posZ;
                }
                if (projectileTemplate.H != 0.0f) {
                    this.velocityZ -= projectileTemplate.H * f2;
                }
            }
            if (!this.isBallistic || this.minHeight < this.posZ || this.reachedApex) {
                float fSortRect = this.speed * f2;
                z4 = true;
                if (fDistanceSq < fSortRect * fSortRect) {
                    fSortRect = Utility.squareRoot(fDistanceSq);
                    fDistanceSq = 0.0f;
                }
                this.posX += Utility.fastCos(angleBetweenPoints) * fSortRect;
                this.posY += Utility.fastSin(angleBetweenPoints) * fSortRect;
            }
            if (this.isBallistic) {
                if (this.verticalVelocity < 0.0f) {
                    f5 = this.speed * f2;
                    z4 = true;
                } else {
                    f5 = this.verticalVelocity * f2;
                }
                if (!this.reachedApex) {
                    this.posZ = Utility.distanceSq(this.posZ, this.maxHeight, f5);
                    if (this.posZ < this.minHeight) {
                        f29 = -90.0f;
                    }
                    if (this.posZ >= this.maxHeight) {
                        this.reachedApex = true;
                    }
                } else if (fDistanceSq < 400.0f) {
                    this.posZ = Utility.distanceSq(this.posZ, f3, f5);
                    if (Utility.abs(this.posZ - f3) > 0.5f) {
                        f29 = 90.0f;
                        z5 = true;
                    }
                }
            } else {
                float f33 = f4;
                float fClamp = this.speed * f2;
                z4 = true;
                if (f33 != 0.0f) {
                    if (fDistanceSq > 0.1d) {
                        fClamp = Utility.clamp((Utility.abs(f33) / Utility.squareRoot(fDistanceSq)) * this.speed * f2, this.speed * f2);
                    }
                    this.posZ += Utility.clamp(f4, fClamp);
                    f4 = f3 - this.posZ;
                }
            }
        }
        if (z4 && this.targetSpeed > 0.0f) {
            this.speed = Utility.distanceSq(this.speed, this.targetSpeed, this.acceleration * f2);
        }
        if (projectileTemplate.wobbleAmplitude != 0.0f) {
            float fFastSin = Utility.fastSin((((this.age * 360.0f) / projectileTemplate.wobbleFrequency) + (360.0f * this.randomSeed)) % 360.0f) * projectileTemplate.wobbleAmplitude * f2;
            this.posX += Utility.fastCos(angleBetweenPoints + 90.0f) * fFastSin;
            this.posY += Utility.fastSin(angleBetweenPoints + 90.0f) * fFastSin;
        }
        if (this.shouldDraw && ((this.hasTrail || projectileTemplate.trailEffect != null) && !this.hasHit)) {
            this.trailTimer += f2;
            if (this.trailTimer > projectileTemplate.ag) {
                this.trailTimer = 0.0f;
                boolean z6 = false;
                if (this.isNuke) {
                    z6 = true;
                }
                if (projectileTemplate.trailEffect != null) {
                    projectileTemplate.trailEffect.a(this.posX, this.posY, this.posZ, this.renderAngle, this);
                }
                if (this.hasTrail && (effectCreateEffectInternal = gameEngine.effectManager.createEffectInternal(this.posX, this.posY, this.posZ, EffectType.custom, z6, EffectQuality.low)) != null) {
                    if (this.posZ >= 0.0f) {
                        effectCreateEffectInternal.stripIndex = 0;
                        effectCreateEffectInternal.frameIndex = 0;
                        effectCreateEffectInternal.drawLayer = (short) 2;
                        effectCreateEffectInternal.fadeIn = true;
                        effectCreateEffectInternal.alpha = 0.5f;
                        effectCreateEffectInternal.lifeTimer = 70.0f;
                        effectCreateEffectInternal.lifeMax = effectCreateEffectInternal.lifeTimer;
                        effectCreateEffectInternal.shadow = true;
                        if (z5) {
                            effectCreateEffectInternal.shadow = false;
                        }
                        effectCreateEffectInternal.velocityY = 0.1f;
                        effectCreateEffectInternal.fadeOut = true;
                        effectCreateEffectInternal.fadeDuration = 5.0f;
                        effectCreateEffectInternal.scaleFrom = 0.5f;
                        effectCreateEffectInternal.scaleTo = 1.2f;
                        effectCreateEffectInternal.rotation = Utility.randomFloatInRange(-180.0f, 180.0f);
                        if (this.isNuke) {
                            effectCreateEffectInternal.scaleFrom = 0.5f;
                            effectCreateEffectInternal.scaleTo = 2.1f;
                        }
                    } else {
                        effectCreateEffectInternal.stripIndex = 9;
                        effectCreateEffectInternal.frameIndex = 1;
                        effectCreateEffectInternal.drawLayer = (short) 1;
                        effectCreateEffectInternal.fadeIn = true;
                        effectCreateEffectInternal.alpha = 0.5f;
                        effectCreateEffectInternal.lifeMax = 60.0f;
                        effectCreateEffectInternal.lifeTimer = 60.0f;
                        effectCreateEffectInternal.velocityY = 0.1f;
                    }
                }
            }
        }
        if (!this.hasHit) {
            boolean z7 = false;
            BaseUnit baseUnit = null;
            boolean z8 = false;
            float f34 = 6.0f;
            if (z2) {
                f34 = f12 * 0.8f;
                if (f34 < 6.0f) {
                    f34 = 6.0f;
                }
            }
            if (z3) {
                f34 = f12 * 1.1f;
            }
            float fAbs = 3.0f;
            if (this.velocityZ != 0.0f || projectileTemplate.G != 0.0f) {
                fAbs = 3.0f + Utility.abs(this.velocityZ * f2) + Utility.abs(projectileTemplate.G * f2);
            }
            if (fDistanceSq < f34 * f34 && Utility.abs(f4) < fAbs) {
                z7 = true;
                baseUnit = this.targetUnit;
            }
            if (this.isInstantHit) {
                z7 = true;
                baseUnit = this.targetUnit;
            }
            if (this.explodesOnTimerEnd && this.lifeTimer == 0.0f) {
                z7 = true;
            }
            if (this.isAreaDamage) {
                float f35 = this.explosionSearchRadius + 50.0f;
                BaseUnit[] baseUnitArrA = BaseUnit.bE.a();
                int size = BaseUnit.bE.size();
                for (int i = 0; i < size; i++) {
                    BaseUnit baseUnit2 = baseUnitArrA[i];
                    if (baseUnit2.posX + f35 > this.posX && baseUnit2.posX - f35 < this.posX && baseUnit2.posY + f35 > this.posY && baseUnit2.posY - f35 < this.posY && baseUnit2.isAlive && false == baseUnit2.i() && baseUnit2.transportContainer == null) {
                        float fDistanceSq2 = Utility.distanceSq(this.posX, this.posY, baseUnit2.posX, baseUnit2.posY);
                        float f36 = this.explosionSearchRadius + baseUnit2.radius;
                        if (fDistanceSq2 < f36 * f36) {
                            z7 = true;
                            baseUnit = baseUnit2;
                        }
                    }
                }
            }
            if (this.collidesWithTerrain) {
                gameEngine.tileMap.setCursorTileIndexFromWorldPoint(this.posX, this.posY);
                if (gameEngine.pathfindingEngine.isTileBlockedForMovement(UnitMovementType.HOVER, gameEngine.tileMap.cursorTileX, gameEngine.tileMap.cursorTileY)) {
                    z7 = true;
                    z8 = true;
                }
            }
            if (this.fliesToPosition) {
            }
            if (this.revealsFog && (((this.isBallistic && z5 && this.posZ < 30.0f) || z7) && this.sourceUnit != null)) {
                this.revealsFog = false;
                FogRevealer fogRevealer = new FogRevealer(false);
                fogRevealer.posX = this.posX;
                fogRevealer.posY = this.posY;
                fogRevealer.setUnitTeam(this.sourceUnit.team);
                fogRevealer.sightRange = 15;
                fogRevealer.lifeTimer = 360.0f;
                PlayerTeam.c(fogRevealer);
            }
            if (z7) {
                this.hasHit = true;
                this.hitX = this.posX;
                this.hitY = this.posY;
                this.hitZ = this.posZ;
                if (this.isInstantHit) {
                    if (this.fliesToPosition) {
                        this.hitX = this.targetX;
                        this.hitY = this.targetY;
                        this.hitZ = 0.0f;
                    }
                    if (this.targetUnit != null) {
                        this.hitX = this.targetUnit.posX + this.trackOffsetX;
                        this.hitY = this.targetUnit.posY + this.trackOffsetY;
                        this.hitZ = this.targetUnit.posZ;
                    }
                }
                if (!this.persistsAfterExplosion && !this.renderJitter && !projectileTemplate.X) {
                    this.hasExploded = false;
                }
                boolean z9 = false;
                if (this.targetUnit != null) {
                    z9 = this.targetUnit.shield > 10.0f;
                }
                CustomUnitSpawnList customUnitSpawnList = projectileTemplate.explodeEffect;
                if (z9) {
                    customUnitSpawnList = projectileTemplate.explodeEffectOnShield;
                }
                if (this.targetUnit != null && (customUnitSpawnListA = projectileTemplate.a(this.targetUnit)) != null) {
                    customUnitSpawnList = customUnitSpawnListA;
                }
                if (customUnitSpawnList != null) {
                    customUnitSpawnList.a(this.hitX, this.hitY, this.hitZ, this.renderAngle, this.targetUnit);
                }
                if (projectileTemplate.spawnProjectilesOnExplode != null) {
                    projectileTemplate.spawnProjectilesOnExplode.a(this.posX, this.posY, this.posZ, this.angle, this.sourceUnit, null, false, this.depth + 1, this, this.targetUnit);
                }
                if (projectileTemplate.spawnUnit != null && this.sourceUnit != null) {
                    projectileTemplate.spawnUnit.a(this.hitX, this.hitY, 0.0f, this.angle, this.sourceUnit.team, false, this.sourceUnit);
                }
                if (projectileTemplate.unloadUpToXUnitsFromSource > 0 && this.sourceUnit != null && (this.sourceUnit instanceof CustomUnit)) {
                    CustomUnit customUnit = (CustomUnit) this.sourceUnit;
                    for (int i2 = 0; i2 < projectileTemplate.unloadUpToXUnitsFromSource; i2++) {
                        if (customUnit.transportedUnits != null && customUnit.transportedUnits.size() > 0) {
                            BaseUnit baseUnit3 = (BaseUnit) customUnit.transportedUnits.remove(customUnit.transportedUnits.size() - 1);
                            GameViewUtils.a(baseUnit3, customUnit);
                            baseUnit3.posX = this.hitX;
                            baseUnit3.posY = this.hitY;
                            baseUnit3.rotationSpeed = this.angle;
                            baseUnit3.velocityY = 0.0f;
                            baseUnit3.velocityX = 0.0f;
                            baseUnit3.worldX = 0.0f;
                            baseUnit3.worldY = 0.0f;
                            if (baseUnit3 instanceof OrderableUnit) {
                                OrderableUnit orderableUnit2 = (OrderableUnit) baseUnit3;
                                orderableUnit2.clearAllWaypoints();
                                orderableUnit2.j(baseUnit3.rotationSpeed);
                                if (baseUnit3 instanceof CustomUnit) {
                                    ((CustomUnit) baseUnit3).dF();
                                }
                            }
                            customUnit.unloadTransportedUnit(baseUnit3);
                        }
                    }
                }
                if (projectileTemplate.teleportSource && this.sourceUnit != null) {
                    this.sourceUnit.f(this.hitX, this.hitY);
                }
                if (!z8 && baseUnit != null) {
                    if (this.isBeam) {
                        this.hasHit = false;
                        float fE = (this.damage / 60.0f) * f2 * e();
                        if (this.explosionRadius == 0.0f) {
                            a(baseUnit);
                        }
                        a(this.sourceUnit, baseUnit, projectileTemplate.a(baseUnit, fE, true), this, false);
                    } else {
                        if (this.explosionRadius == 0.0f) {
                            a(baseUnit);
                        }
                        a(this.sourceUnit, baseUnit, projectileTemplate.a(baseUnit, this.damage, false), this, false);
                    }
                }
                if (this.parentProjectile != null) {
                    if (projectileTemplate.killParentOnHit) {
                        this.parentProjectile.lifeTimer = 0.0f;
                    } else {
                        this.parentProjectile.b();
                    }
                    remove();
                }
                if (!this.isBeam) {
                    boolean z10 = true;
                    if (this.targetUnit != null && this.targetUnit.shield > 10.0f) {
                        z10 = false;
                        if (projectileTemplate.explodeEffectOnShield == null && (effectCreateSmallExplosionInternal2 = gameEngine.effectManager.createSmallExplosionInternal(this.hitX, this.hitY, this.hitZ, -1127220)) != null) {
                            effectCreateSmallExplosionInternal2.lifeTimer = 10.0f;
                            effectCreateSmallExplosionInternal2.scaleTo = 0.5f;
                            if (this.isSmallExplosion) {
                                effectCreateSmallExplosionInternal2.lifeTimer = 25.0f;
                                effectCreateSmallExplosionInternal2.scaleTo = 1.0f;
                            }
                            effectCreateSmallExplosionInternal2.drawLayer = (short) 2;
                            effectCreateSmallExplosionInternal2.lifeMax = effectCreateSmallExplosionInternal2.lifeTimer;
                        }
                    }
                    if (this.spawnEmitterOnHit) {
                        z10 = false;
                        EffectEmitter.createAlternateFireEmitter(this.posX, this.posY).duration = 21.0f;
                    }
                    if (z10) {
                        if (!this.isSmallExplosion) {
                            if (projectileTemplate.explodeEffect == null) {
                                gameEngine.effectManager.createLargeExplosion(this.hitX, this.hitY, this.hitZ);
                            }
                        } else if (projectileTemplate.explodeEffect == null) {
                            if (this.explosionRadius > 10.0f && (effectCreateSmallExplosionInternal = gameEngine.effectManager.createSmallExplosionInternal(this.hitX, this.hitY, this.hitZ, 0)) != null) {
                                effectCreateSmallExplosionInternal.scaleTo = this.explosionRadius / 25.0f;
                                effectCreateSmallExplosionInternal.alpha = 0.7f;
                                if (this.hitZ > 5.0f) {
                                    effectCreateSmallExplosionInternal.drawLayer = (short) 2;
                                }
                            }
                            gameEngine.effectManager.createSmallExplosion(this.hitX, this.hitY, this.hitZ);
                            if (this.playsHitSound && !this.isNuke) {
                                gameEngine.soundEngine.playSoundAt(SoundEngine.missileHitSound, 0.5f, 1.0f + Utility.randomFloatInRange(-0.06f, 0.06f), this.hitX, this.hitY);
                            }
                        }
                        if (this.isNuke && projectileTemplate.explodeEffect == null) {
                            gameEngine.soundEngine.playSoundAt(SoundEngine.nukeExplodeSound, 1.6f, 0.7f, this.hitX, this.hitY);
                            gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                            Effect effectCreateLightEffect = gameEngine.effectManager.createLightEffect(this.hitX, this.hitY, this.posZ, Color.a(255, 255, 255, 255));
                            if (effectCreateLightEffect != null) {
                                effectCreateLightEffect.scaleFrom = 14.0f;
                                effectCreateLightEffect.scaleTo = 8.0f;
                                effectCreateLightEffect.alpha = 0.9f;
                                effectCreateLightEffect.lifeTimer = 35.0f;
                                effectCreateLightEffect.lifeMax = effectCreateLightEffect.lifeTimer;
                                effectCreateLightEffect.fadeIn = true;
                            }
                            gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                            Effect effectCreateSmallExplosion = gameEngine.effectManager.createSmallExplosion(this.hitX, this.hitY, this.hitZ, -1127220);
                            if (effectCreateSmallExplosion != null) {
                                effectCreateSmallExplosion.scaleFrom = 1.5f;
                                effectCreateSmallExplosion.scaleTo = 3.0f;
                                effectCreateSmallExplosion.drawLayer = (short) 2;
                                effectCreateSmallExplosion.lifeTimer = 20.0f;
                                effectCreateSmallExplosion.lifeMax = effectCreateSmallExplosion.lifeTimer;
                                effectCreateSmallExplosion.delayedStartTimer = 0.0f;
                            }
                            gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                            Effect effectCreateSmallExplosion2 = gameEngine.effectManager.createSmallExplosion(this.hitX, this.hitY, this.hitZ, -1127220);
                            if (effectCreateSmallExplosion2 != null) {
                                effectCreateSmallExplosion2.scaleFrom = 0.2f;
                                effectCreateSmallExplosion2.scaleTo = 5.0f;
                                effectCreateSmallExplosion2.drawLayer = (short) 2;
                                effectCreateSmallExplosion2.lifeTimer = 65.0f;
                                effectCreateSmallExplosion2.lifeMax = effectCreateSmallExplosion2.lifeTimer;
                                effectCreateSmallExplosion2.delayedStartTimer = 0.0f;
                            }
                            gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                            Effect effectCreateLightEffect2 = gameEngine.effectManager.createLightEffect(this.hitX, this.hitY, this.posZ, Color.a(255, 255, 255, 255));
                            if (effectCreateLightEffect2 != null) {
                                effectCreateLightEffect2.scaleFrom = 3.0f;
                                effectCreateLightEffect2.scaleTo = 6.0f;
                                effectCreateLightEffect2.alpha = 0.9f;
                                effectCreateLightEffect2.lifeTimer = 290.0f;
                                effectCreateLightEffect2.lifeMax = effectCreateLightEffect2.lifeTimer;
                            }
                            gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                            Effect effectCreateLightEffect3 = gameEngine.effectManager.createLightEffect(this.hitX, this.hitY, this.posZ, Color.a(255, 255, 244, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_DATA_SERVICE));
                            if (effectCreateLightEffect3 != null) {
                                effectCreateLightEffect3.scaleFrom = 2.0f;
                                effectCreateLightEffect3.scaleTo = 6.0f;
                                effectCreateLightEffect3.alpha = 0.5f;
                                effectCreateLightEffect3.lifeTimer = 370.0f;
                                effectCreateLightEffect3.lifeMax = effectCreateLightEffect3.lifeTimer;
                                effectCreateLightEffect3.delayedStartTimer = 10.0f;
                            }
                            for (int i3 = 0; i3 < 1; i3++) {
                                gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                                Effect effectCreateLightEffect4 = gameEngine.effectManager.createLightEffect(this.hitX, this.hitY, this.posZ, Color.a(255, 255, 244, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_DATA_SERVICE));
                                if (effectCreateLightEffect4 != null) {
                                    effectCreateLightEffect4.scaleFrom = 0.2f;
                                    effectCreateLightEffect4.scaleTo = 9.0f;
                                    effectCreateLightEffect4.alpha = 0.7f;
                                    effectCreateLightEffect4.lifeTimer = 210.0f;
                                    effectCreateLightEffect4.lifeMax = effectCreateLightEffect4.lifeTimer;
                                    effectCreateLightEffect4.delayedStartTimer = 20 + (i3 * 110);
                                }
                            }
                            gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                            Effect effectCreateLightEffect5 = gameEngine.effectManager.createLightEffect(this.hitX, this.hitY, this.posZ, Color.a(255, 255, 255, 255));
                            if (effectCreateLightEffect5 != null) {
                                effectCreateLightEffect5.scaleFrom = 3.0f;
                                effectCreateLightEffect5.scaleTo = 4.0f;
                                effectCreateLightEffect5.alpha = 0.2f;
                                effectCreateLightEffect5.lifeTimer = 870.0f;
                                effectCreateLightEffect5.lifeMax = effectCreateLightEffect5.lifeTimer;
                                effectCreateLightEffect5.fadeIn = true;
                                effectCreateLightEffect5.delayedStartTimer = 70.0f;
                            }
                            gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                            Effect effectCreateLightEffect6 = gameEngine.effectManager.createLightEffect(this.hitX, this.hitY, this.posZ, Color.a(255, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_3D_MODE, 255, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_SATELLITE_CS));
                            if (effectCreateLightEffect6 != null) {
                                effectCreateLightEffect6.scaleFrom = 4.0f;
                                effectCreateLightEffect6.scaleTo = 1.0f;
                                effectCreateLightEffect6.alpha = 0.9f;
                                effectCreateLightEffect6.lifeTimer = 320.0f;
                                effectCreateLightEffect6.lifeMax = effectCreateLightEffect6.lifeTimer;
                            }
                            gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                            Effect effectCreateLightEffect7 = gameEngine.effectManager.createLightEffect(this.hitX, this.hitY, this.posZ, Color.a(255, 255, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_ANTENNA_CABLE, 129));
                            if (effectCreateLightEffect7 != null) {
                                effectCreateLightEffect7.scaleFrom = 2.0f;
                                effectCreateLightEffect7.scaleTo = 1.0f;
                                effectCreateLightEffect7.alpha = 1.0f;
                                effectCreateLightEffect7.lifeTimer = 340.0f;
                                effectCreateLightEffect7.lifeMax = effectCreateLightEffect7.lifeTimer;
                                effectCreateLightEffect7.fadeOut = true;
                                effectCreateLightEffect7.fadeDuration = 20.0f;
                            }
                            gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                            Effect effectCreateLightEffect8 = gameEngine.effectManager.createLightEffect(this.hitX, this.hitY, this.posZ, Color.a(245, 255, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_AVR_INPUT, 110));
                            if (effectCreateLightEffect8 != null) {
                                effectCreateLightEffect8.scaleFrom = 1.5f;
                                effectCreateLightEffect8.scaleTo = 1.5f;
                                effectCreateLightEffect8.alpha = 0.3f;
                                effectCreateLightEffect8.lifeTimer = 1340.0f;
                                effectCreateLightEffect8.lifeMax = effectCreateLightEffect8.lifeTimer;
                                effectCreateLightEffect8.fadeOut = true;
                                effectCreateLightEffect8.fadeDuration = 40.0f;
                                effectCreateLightEffect8.delayedStartTimer = 140.0f;
                            }
                            for (int i4 = 0; i4 < 4; i4++) {
                                gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                                Effect effectCreateLightEffect9 = gameEngine.effectManager.createLightEffect(this.hitX, this.hitY, this.posZ, Color.a(SlickToAndroidKeycodes.AndroidCodes.KEYCODE_PAIRING, 255, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_ANTENNA_CABLE, 129));
                                if (effectCreateLightEffect9 != null) {
                                    effectCreateLightEffect9.scaleFrom = 1.5f;
                                    effectCreateLightEffect9.scaleTo = 1.4f;
                                    effectCreateLightEffect9.alpha = 1.3f;
                                    effectCreateLightEffect9.lifeTimer = 340.0f;
                                    effectCreateLightEffect9.lifeMax = effectCreateLightEffect9.lifeTimer;
                                    effectCreateLightEffect9.velocityY = -0.29f;
                                    effectCreateLightEffect9.fadeOut = true;
                                    effectCreateLightEffect9.fadeDuration = 50.0f;
                                    effectCreateLightEffect9.delayedStartTimer = 30 + (i4 * 40);
                                }
                            }
                            for (int i5 = 0; i5 < 2; i5++) {
                                gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                                Effect effectCreateLightEffect10 = gameEngine.effectManager.createLightEffect(this.hitX, this.hitY, this.posZ, Color.a(SlickToAndroidKeycodes.AndroidCodes.KEYCODE_PROG_YELLOW, 255, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_ANTENNA_CABLE, 129));
                                if (effectCreateLightEffect10 != null) {
                                    effectCreateLightEffect10.scaleFrom = 1.3f;
                                    effectCreateLightEffect10.scaleTo = 1.0f;
                                    effectCreateLightEffect10.alpha = 1.0f;
                                    effectCreateLightEffect10.lifeTimer = 340.0f;
                                    effectCreateLightEffect10.lifeMax = effectCreateLightEffect10.lifeTimer;
                                    effectCreateLightEffect10.velocityY = -0.14f;
                                    effectCreateLightEffect10.fadeOut = true;
                                    effectCreateLightEffect10.fadeDuration = 50.0f;
                                    effectCreateLightEffect10.delayedStartTimer = 70 + (i5 * 70);
                                }
                            }
                            for (int i6 = 0; i6 < 4; i6++) {
                                gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                                Effect effectCreateLightEffect11 = gameEngine.effectManager.createLightEffect(this.hitX, this.hitY - 30.0f, this.posZ, -16711936);
                                if (effectCreateLightEffect11 != null) {
                                    effectCreateLightEffect11.scaleFrom = 1.5f;
                                    effectCreateLightEffect11.scaleTo = 2.6f;
                                    effectCreateLightEffect11.alpha = 1.3f;
                                    effectCreateLightEffect11.lifeTimer = 510.0f;
                                    effectCreateLightEffect11.lifeMax = effectCreateLightEffect11.lifeTimer;
                                    effectCreateLightEffect11.velocityY = -0.2f;
                                    effectCreateLightEffect11.fadeOut = true;
                                    effectCreateLightEffect11.fadeDuration = 50.0f;
                                    effectCreateLightEffect11.lightingColorFilter = null;
                                    effectCreateLightEffect11.startColor = Color.a(175, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_TERRESTRIAL_ANALOG, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_TERRESTRIAL_ANALOG, SlickToAndroidKeycodes.AndroidCodes.KEYCODE_TV_TERRESTRIAL_ANALOG);
                                    effectCreateLightEffect11.delayedStartTimer = 20 + (i6 * 40);
                                }
                            }
                            for (int i7 = 0; i7 < 2; i7++) {
                                gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                                Effect effectCreateLightEffect12 = gameEngine.effectManager.createLightEffect(this.hitX, this.hitY - 30.0f, this.posZ, -16711936);
                                if (effectCreateLightEffect12 != null) {
                                    effectCreateLightEffect12.scaleFrom = 1.5f;
                                    effectCreateLightEffect12.scaleTo = 3.8f;
                                    effectCreateLightEffect12.alpha = 0.8f;
                                    effectCreateLightEffect12.lifeTimer = 590.0f;
                                    effectCreateLightEffect12.lifeMax = effectCreateLightEffect12.lifeTimer;
                                    effectCreateLightEffect12.velocityY = -0.2f;
                                    effectCreateLightEffect12.fadeOut = true;
                                    effectCreateLightEffect12.fadeDuration = 50.0f;
                                    effectCreateLightEffect12.lightingColorFilter = null;
                                    effectCreateLightEffect12.startColor = Color.a(105, 115, 115, 115);
                                    effectCreateLightEffect12.delayedStartTimer = 20 + (i7 * 40);
                                }
                            }
                            for (int i8 = 0; i8 < 1; i8++) {
                                EffectEmitter effectEmitterA = EffectEmitter.createDefaultFireEmitter(this.hitX + Utility.getDeterministicRandomFloat(-10.0f, 10.0f, (int) this.objectId), this.hitY + Utility.getDeterministicRandomFloat(-10.0f, 10.0f, ((int) this.objectId) + i8));
                                if (effectEmitterA != null) {
                                    effectEmitterA.startDelay = 200 + (i8 * 70);
                                    effectEmitterA.duration = 980 + (i8 * 800);
                                }
                            }
                            if (!GameViewUtils.d(this.hitX, this.hitY)) {
                                ScorchMark.a(this.hitX, this.hitY, ExplosionType.nuke);
                            }
                            if (GameEngine.isFancyWaterSupported()) {
                                if (gameEngine.effectManager.shockwaveTexture == null) {
                                    gameEngine.effectManager.shockwaveTexture = gameEngine.renderGraphicsEngine.a(com.corrodinggames.rts.R.drawable.shockwave_normal_256, true);
                                }
                                gameEngine.effectManager.setOverrideEffectQuality(EffectQuality.critical);
                                Effect effectCreateLightEffect13 = gameEngine.effectManager.createLightEffect(this.hitX, this.hitY, this.posZ, -1);
                                if (effectCreateLightEffect13 != null && gameEngine.effectManager.shockwaveTexture != null) {
                                    effectCreateLightEffect13.template = new EffectTemplate((BuiltInEffectType) null);
                                    effectCreateLightEffect13.template.imageStrip = new SpriteSheet();
                                    effectCreateLightEffect13.template.imageStrip.singleFrame = true;
                                    effectCreateLightEffect13.template.imageStrip.texture = gameEngine.effectManager.shockwaveTexture;
                                    effectCreateLightEffect13.template.imageStrip.frameWidth = effectCreateLightEffect13.template.imageStrip.texture.m();
                                    effectCreateLightEffect13.template.imageStrip.frameHeight = effectCreateLightEffect13.template.imageStrip.texture.l();
                                    effectCreateLightEffect13.drawLayer = (short) 3;
                                    effectCreateLightEffect13.scaleFrom = 0.5f;
                                    effectCreateLightEffect13.scaleTo = 3.5f;
                                    effectCreateLightEffect13.alpha = 0.5f;
                                    effectCreateLightEffect13.lifeTimer = 60.0f;
                                    effectCreateLightEffect13.lifeMax = effectCreateLightEffect13.lifeTimer;
                                    effectCreateLightEffect13.velocityY = -0.2f;
                                    effectCreateLightEffect13.fadeOut = true;
                                    effectCreateLightEffect13.fadeDuration = 1.0f;
                                    effectCreateLightEffect13.lightingColorFilter = null;
                                    effectCreateLightEffect13.delayedStartTimer = 0.0f;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (this.hasHit && !this.removalComplete) {
            this.explosionAnimTimer = Utility.moveTowardsZero(this.explosionAnimTimer, f2);
            if (this.trackHitUnits) {
                b(1.0f - (this.explosionAnimTimer / this.explosionAnimDuration));
            }
            if (this.explosionAnimTimer == 0.0f) {
                this.removalComplete = true;
                b(1.0f);
                if (!this.persistsAfterExplosion && !this.renderJitter && !projectileTemplate.X) {
                    remove();
                }
            }
        }
        this.age += f2;
        if (this.lifeTimer == 0.0f && (!this.hasHit || this.removalComplete)) {
            if (projectileTemplate.spawnProjectilesOnEndOfLife != null) {
                projectileTemplate.spawnProjectilesOnEndOfLife.a(this.posX, this.posY, this.posZ, this.angle, this.sourceUnit, null, false, this.depth + 1, this, null);
            }
            remove();
        }
        if (!this.angleInitialized) {
            this.renderAngle = f29;
            this.angleInitialized = true;
        }
        this.renderAngle += Utility.rotateTowardsAngle(this.renderAngle, f29, 12.0f * f2);
    }

    public void b(float f2) {
        boolean z = false;
        if (this.template.noExplosion) {
            return;
        }
        if (this.template.hasSplashDamage) {
            z = true;
        }
        if (!z) {
            if (this.splashDamage != 0.0f && this.explosionRadius > 0.0f) {
                z = true;
            }
            if ((this.pushForce != 0.0f || this.pushBase != 0.0f) && this.explosionRadius > 0.0f) {
                z = true;
            }
        }
        if (!z) {
            return;
        }
        float f3 = this.explosionRadius * f2;
        float f4 = f3;
        if (this.template.splashRadiusBonus) {
            f4 += 150.0f;
        }
        GameEngine gameEngine = GameEngine.getInstance();
        bi.clear();
        gameEngine.unitSpatialIndex.b(this.hitX, this.hitY, f4, bi);
        BaseUnit[] baseUnitArrA = bi.a();
        int size = bi.size();
        for (int i = 0; i < size; i++) {
            b(baseUnitArrA[i], f2, f3);
        }
        bi.clear();
    }

    public void b(BaseUnit baseUnit, float f2, float f3) {
        if (baseUnit.transportContainer != null) {
            return;
        }
        if (this.hitUnits != null && this.hitUnits.contains(baseUnit)) {
            return;
        }
        if (this.sourceUnit != null) {
            PlayerTeam playerTeam = this.sourceUnit.team;
            PlayerTeam playerTeam2 = baseUnit.team;
            if (playerTeam2 != playerTeam && playerTeam.d(playerTeam2)) {
                return;
            }
            if (this.damageEnemiesOnly && !playerTeam.c(playerTeam2)) {
                return;
            }
            if (this.noFriendlyFire && playerTeam.c(playerTeam2)) {
                return;
            }
        }
        if (baseUnit.posZ < -5.0f && this.hitZ >= -2.0f && !this.canHitSubmerged) {
            return;
        }
        if (this.matchesTargetAltitude) {
            if (baseUnit.i() != (this.hitZ >= 5.0f)) {
                return;
            }
        } else if (!this.excludesAir && baseUnit.i()) {
            return;
        }
        float fDistanceSq = Utility.distanceSq(this.hitX, this.hitY, baseUnit.posX, baseUnit.posY);
        if (fDistanceSq > f3 * f3 && !this.template.splashRadiusBonus) {
            return;
        }
        float fSqrt = (float) StrictMath.sqrt(fDistanceSq);
        if (this.template.splashRadiusBonus) {
            fSqrt -= baseUnit.radius;
            if (fSqrt < 0.0f) {
                fSqrt = 0.0f;
            }
        }
        if (fSqrt > f3 || fSqrt < this.template.j) {
            return;
        }
        a(f2, baseUnit, fSqrt);
    }

    public void a(float f2, BaseUnit baseUnit, float f3) {
        float f4 = (float) (((double) (1.0f - (f3 / this.explosionRadius))) + 0.1d);
        if (f4 > 1.0f) {
            f4 = 1.0f;
        }
        if (this.template.fullSplashDamage) {
            f4 = 1.0f;
        }
        float f5 = f4 * this.splashDamage;
        a(baseUnit);
        a(this.sourceUnit, baseUnit, this.template.a(baseUnit, f5, true), this, true);
        if (this.trackHitUnits) {
            if (this.hitUnits == null) {
                this.hitUnits = new FastArrayList();
            }
            this.hitUnits.add(baseUnit);
        }
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public boolean a(GameEngine gameEngine) {
        if (gameEngine.bufferedVisibleWorldRectF.b(this.posX, this.posY)) {
            return true;
        }
        if ((this.persistsAfterExplosion || this.isBeam || this.template.X) && this.targetUnit != null && gameEngine.bufferedVisibleWorldRectF.b(this.targetUnit.posX, this.targetUnit.posY)) {
            return true;
        }
        return false;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public boolean c(float f2) {
        float f3;
        float f4;
        float f5;
        if (!this.hasExploded || this.initialDelay > 0.0f) {
            return false;
        }
        ProjectileTemplate projectileTemplate = this.template;
        GameEngine gameEngine = GameEngine.getInstance();
        GraphicsEngine graphicsEngine = gameEngine.renderGraphicsEngine;
        float f6 = this.posX - gameEngine.viewpointXSnapped;
        float f7 = this.posY - gameEngine.viewpointYSnapped;
        if (this.targetUnit != null) {
            f3 = this.targetUnit.posX;
            f4 = this.targetUnit.posY;
            f5 = this.targetUnit.posZ;
        } else {
            f3 = this.targetX;
            f4 = this.targetY;
            f5 = this.targetZ;
        }
        if (!this.visibilityChecked && !this.isNuke) {
            boolean z = false;
            if (this.isInstantHit) {
                if (this.targetUnit != null) {
                    if (!gameEngine.tileMap.isWorldPointVisibleForTeam(this.targetUnit.posX, this.targetUnit.posY, gameEngine.playerTeam)) {
                        z = true;
                    }
                } else if (this.hasFixedTarget && !gameEngine.tileMap.isWorldPointVisibleForTeam(this.targetX, this.targetY, gameEngine.playerTeam)) {
                    z = true;
                }
            }
            if (!gameEngine.tileMap.isWorldPointVisibleForTeam(this.posX, this.posY, gameEngine.playerTeam) && !z) {
                return false;
            }
            this.visibilityChecked = true;
        }
        if (this.isBeam || projectileTemplate.X) {
            if (projectileTemplate.Y != null) {
                Paint paintF = f();
                float f8 = 0.0f;
                if (projectileTemplate.beamImageOffsetRate != 0.0f) {
                    f8 = 0.0f + (projectileTemplate.beamImageOffsetRate * this.age);
                }
                float f9 = this.posX - gameEngine.viewpointXSnapped;
                float f10 = (this.posY - gameEngine.viewpointYSnapped) - this.posZ;
                float f11 = (f3 - gameEngine.viewpointXSnapped) + this.trackOffsetX;
                float f12 = ((f4 - f5) - gameEngine.viewpointYSnapped) + this.trackOffsetY;
                float f13 = (f11 + f9) * 0.5f;
                float f14 = (f12 + f10) * 0.5f;
                float fDistance = Utility.distance(f13, f14, f11, f12);
                float angleBetweenPoints = Utility.getAngleBetweenPoints(f13, f14, f11, f12);
                graphicsEngine.k();
                f.a(f13 - projectileTemplate.Y.r, f14 - fDistance, f13 + projectileTemplate.Y.r, f14 + fDistance);
                graphicsEngine.a(angleBetweenPoints + 90.0f, f13, f14);
                graphicsEngine.a(projectileTemplate.Y, f, paintF, 0.0f, f8, 0, 0);
                graphicsEngine.l();
                if (projectileTemplate.Z != null) {
                    if (projectileTemplate.aa) {
                        graphicsEngine.k();
                        graphicsEngine.a(angleBetweenPoints + 90.0f, f9, f10);
                        graphicsEngine.a(projectileTemplate.Z, f9, f10, paintF);
                        graphicsEngine.l();
                    } else {
                        graphicsEngine.a(projectileTemplate.Z, f9, f10, paintF);
                    }
                }
                if (projectileTemplate.ab != null) {
                    if (projectileTemplate.ac) {
                        graphicsEngine.k();
                        graphicsEngine.a(angleBetweenPoints + 90.0f, f11, f12);
                        graphicsEngine.a(projectileTemplate.ab, f11, f12, paintF);
                        graphicsEngine.l();
                        return true;
                    }
                    graphicsEngine.a(projectileTemplate.ab, f11, f12, paintF);
                    return true;
                }
                return true;
            }
            bf.c((int) (60.0f + (e() * 60.0f)));
            float f15 = (f3 - gameEngine.viewpointXSnapped) + this.trackOffsetX;
            float f16 = ((f4 - f5) - gameEngine.viewpointYSnapped) + this.trackOffsetY;
            bf.a(6.0f);
            graphicsEngine.a(this.posX - gameEngine.viewpointXSnapped, (this.posY - gameEngine.viewpointYSnapped) - this.posZ, f15, f16, bf);
            bf.a(3.0f);
            graphicsEngine.a(this.posX - gameEngine.viewpointXSnapped, (this.posY - gameEngine.viewpointYSnapped) - this.posZ, f15, f16, bf);
            graphicsEngine.a(f15, f16, 8.0f, bf);
            graphicsEngine.a(f15, f16, 5.0f, bf);
            return true;
        }
        if (this.persistsAfterExplosion) {
            float f17 = (f3 - gameEngine.viewpointXSnapped) + this.trackOffsetX;
            float f18 = ((f4 - f5) - gameEngine.viewpointYSnapped) + this.trackOffsetY;
            bd.b(this.color);
            be.b(this.color);
            be.c((int) (be.f() * 0.5f));
            graphicsEngine.a(this.posX - gameEngine.viewpointXSnapped, (this.posY - gameEngine.viewpointYSnapped) - this.posZ, f17, f18, be);
            graphicsEngine.a(this.posX - gameEngine.viewpointXSnapped, (this.posY - gameEngine.viewpointYSnapped) - this.posZ, f17, f18, bd);
            graphicsEngine.a(f17, f18, 5.0f, bd);
            return true;
        }
        if (this.renderJitter) {
            this.jitterTimer = Utility.moveTowardsZero(this.jitterTimer, f2);
            if (this.jitterOffsets == null) {
                this.jitterOffsets = new float[20];
                this.jitterTimer = 0.0f;
            }
            if (this.jitterTimer == 0.0f) {
                this.jitterTimer = 4.0f;
                for (int i = 0; i < this.jitterOffsets.length; i++) {
                    this.jitterOffsets[i] = Utility.randomFloatInRange(-10.0f, 10.0f);
                }
            }
            float f19 = this.posX - gameEngine.viewpointXSnapped;
            float f20 = (this.posY - gameEngine.viewpointYSnapped) - this.posZ;
            float f21 = f3 - gameEngine.viewpointXSnapped;
            float f22 = (f4 - f5) - gameEngine.viewpointYSnapped;
            float fDistanceInt = Utility.distanceInt(f19, f20, f21, f22);
            int length = this.jitterOffsets.length;
            if (fDistanceInt < 200.0f) {
                length = Utility.max(0, length - 5);
            } else if (fDistanceInt < 100.0f) {
                length = Utility.max(0, length - 10);
            }
            float f23 = fDistanceInt / (length - 1);
            float angleBetweenPoints2 = Utility.getAngleBetweenPoints(f19, f20, f21, f22);
            float f24 = f19;
            float f25 = f20;
            float fFastCos = Utility.fastCos(angleBetweenPoints2);
            float fFastSin = Utility.fastSin(angleBetweenPoints2);
            for (int i2 = 0; i2 < length; i2++) {
                float f26 = this.jitterOffsets[i2];
                float f27 = f19 + (fFastCos * i2 * f23);
                float f28 = f20 + (fFastSin * i2 * f23);
                if (i2 != length - 1) {
                    f27 -= fFastSin * f26;
                    f28 += fFastCos * f26;
                }
                graphicsEngine.a(f24, f25, f27, f28, bg);
                f24 = f27;
                f25 = f28;
            }
            return true;
        }
        if (this.textureFrame != -1) {
            Texture texture = b;
            int i3 = 20;
            int i4 = 20;
            if (this.textureType == 1) {
                texture = d;
                i3 = 60;
                i4 = 60;
            } else if (this.textureType == 2) {
                texture = c;
                i3 = 20;
                i4 = 20;
            }
            if (projectileTemplate.C != null) {
                GameViewUtils.a(projectileTemplate.C, f6, f7, 0.0f, this.renderAngle, this.renderScale, bc, projectileTemplate.C.p, projectileTemplate.C.q, 0);
            } else if (this.frameIndex != -1 && this.renderShadow) {
                GameViewUtils.a(texture, f6, f7, 0.0f, this.renderAngle, this.renderScale, bc, i3, i4, this.frameIndex);
            }
            if (projectileTemplate.B != null) {
                texture = projectileTemplate.B;
                i3 = projectileTemplate.B.p;
                i4 = projectileTemplate.B.q;
            }
            GameViewUtils.a(texture, f6, f7, this.posZ, this.renderAngle, this.renderScale, f(), i3, i4, this.textureFrame);
            return true;
        }
        bb.b(this.color);
        if (this.posZ > 0.0f && this.renderShadow) {
            graphicsEngine.a(f6, f7, this.renderScale, bc);
        }
        graphicsEngine.a(f6, f7 - this.posZ, this.renderScale, bb);
        if (this.glowScale > 0.0f) {
            bb.c(bb.f() / 3);
            graphicsEngine.a(f6, f7 - this.posZ, this.glowScale, bb);
            return true;
        }
        return true;
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public void a(float f2, boolean z) {
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public void d(float f2) {
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public void e(float f2) {
    }

    @Override // com.corrodinggames.rts.gameFramework.GameObject
    public boolean f(float f2) {
        return false;
    }

    public Paint f() {
        Paint paintA;
        if (this.color != aq) {
            if (GameEngine.isAndroidPlatform()) {
                paintA = a(this.color);
            } else {
                paintA = bb;
                paintA.b(this.color);
            }
        } else {
            paintA = ba;
        }
        return paintA;
    }

    public GamePaint a(int i) {
        if (this.bj != null) {
            return this.bj;
        }
        if (bk != null && bl == i) {
            this.bj = bk;
            return this.bj;
        }
        GamePaint gamePaint = new GamePaint();
        gamePaint.a(new LightingColorFilter(i, 0));
        gamePaint.b(i);
        bk = gamePaint;
        bl = i;
        this.bj = gamePaint;
        return this.bj;
    }

    public void a(float f2, float f3, AnimationSet animationSet) {
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.sourceUnit == null) {
            GameEngine.logColored("Projectile: cannot Retarget: source==null");
            return;
        }
        float fFastCos = this.posX + (Utility.fastCos(this.angle) * f3);
        float fFastSin = this.posY + (Utility.fastSin(this.angle) * f3);
        float f4 = f2 * f2;
        float f5 = -1.0f;
        OrderableUnit orderableUnit = null;
        BaseUnit commandOrAttackTarget = null;
        if (this.sourceUnit instanceof OrderableUnit) {
            orderableUnit = (OrderableUnit) this.sourceUnit;
            commandOrAttackTarget = orderableUnit.getCommandOrAttackTarget();
        }
        for (BaseUnit baseUnit : gameEngine.unitSpatialIndex.a(fFastCos, fFastSin, f2)) {
            if (this.sourceUnit.team != baseUnit.team) {
                boolean zB = true;
                if (orderableUnit != null) {
                    zB = orderableUnit.b(baseUnit, true);
                }
                if (zB && this.techLevel >= 0 && orderableUnit != null && this.techLevel < orderableUnit.getTechLevel() && !orderableUnit.a((int) this.techLevel, baseUnit, true, false)) {
                    zB = false;
                }
                if (animationSet != null && !AnimationTag.a(animationSet, baseUnit.getTags())) {
                    zB = false;
                }
                if (zB) {
                    float fDistanceSq = Utility.distanceSq(fFastCos, fFastSin, baseUnit.posX, baseUnit.posY);
                    boolean z = false;
                    if (f5 == -1.0f || fDistanceSq < f5) {
                        z = true;
                    }
                    if (commandOrAttackTarget == baseUnit) {
                        z = true;
                    }
                    if (z && fDistanceSq < f4) {
                        f5 = fDistanceSq;
                        this.targetUnit = baseUnit;
                    }
                }
            }
        }
    }
}
