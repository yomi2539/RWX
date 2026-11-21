package io.github.rwx.mod.api.specs

import io.github.rwx.mod.api.requireIniMutatorName

/**
 * Properties for [projectile_NAME] section
 * 
 * Generated from Rusted_Warfare__Beta_Modding_Reference.xlsx
 */
data class ProjectileSpec(
    // ===== Core =====
    // Example: life: 300
    /**
     * How long till this projectile gets removed if it hasn't hit a target, 300 might be a good starting point, change depending on speed and range
     */
    var life: Int = 300,
    // Example: deflectionPower: -1
    /**
     * Defaults to 1. Energy needed for laser defense to deflect. -1 to disable deflection (only disable for special weapons like flames)
     */
    var deflectionPower: Float = 1f,
    // Example: explodeOnEndOfLife: true
    /**
     * Default to false. True to explode at end of life with all side effects and area damage instead of disappearing. Good for making area-denial weapons.
     */
    var explodeOnEndOfLife: Boolean? = false,
    // Example: autoTargetingOnDeadTarget: true
    /** Retarget to nearby unit if target dies while in transit */
    var autoTargetingOnDeadTarget: Boolean? = null,
    // Example: autoTargetingOnDeadTargetRange: 100
    /** The range which it will select a new target if old target has died */
    var autoTargetingOnDeadTargetRange: Int? = null,
    // Example: autoTargetingOnDeadTargetLead: 300
    /** The lead it will try to have on the new target */
    var autoTargetingOnDeadTargetLead: Float? = null,
    // Example: unloadUpToXUnitsFromSource: 2
    /** Unload X units from source unit, to projectile explode location */
    var unloadUpToXUnitsFromSource: Int? = null,
    // Example: teleportSource: true
    /** Move unit that shot this projectile to projectile explode location */
    var teleportSource: Boolean? = null,
    // Example: eg: spawnUnit: heavyTank, tank*5, hoverTank(offsetX=10)
    /** Spawn new units of this type at projectile explode location */
    var spawnUnit: String? = null,
    // Example: spawnProjectilesOnEndOfLife: torpedo_split(offsetDir=90), torpedo_split(offsetDir=-90)
    /** Spawns new projectiles on end of life, useful for secondary projectiles */
    var spawnProjectilesOnEndOfLife: String? = null,
    // Example: spawnProjectilesOnExplode: flachette*4
    /** Projectiles to spawns when this projectile hits it's target */
    var spawnProjectilesOnExplode: String? = null,
    // Example: spawnProjectilesOnCreate: stage2
    /**
     * Spawns projectiles on creation of this projectile, useful for making true shotgun-like projectile spread
     */
    var spawnProjectilesOnCreate: String? = null,
    // Example: convertHitToSourceTeam: true
    /**
     * Convert units hit to the team that fired this projectile. Useful to make capturing systems
     */
    var convertHitToSourceTeam: Boolean? = null,
    // Example: tags: nuke, missile
    /** Useful for projectile interceptions (e.g. Nuke and Anti-Nuke Interaction) */
    var tags: List<String>? = null,
    // Example: flameWeapon: true
    /** Generates small flames on hit (only cosmetic) */
    var flameWeapon: Boolean? = null,
    // Example: delayedStartTimer: 2s
    /** Hide for x time before showing and updating effect. */
    var delayedStartTimer: Float? = null,

    // ===== Damage =====
    // Example: directDamage: 30
    /**
     * Damage to target unit on hit. Does not work with targetGround:true as it won't have a clear unit to target
     */
    var directDamage: Int? = null,
    // Example: areaDamage: 10
    /**
     * Damages on arrivar of target with an area effect, use areaRadius to adjust size of damage. targetGround needs this to damage
     */
    var areaDamage: Int? = null,
    // Example: areaRadius: 100
    /**
     * How wide areaDamage effects. Note this drops off (unless areaDamageNoFalloff is used)
     */
    var areaRadius: Int? = null,
    // Example: areaDamageNoFalloff: true
    /** Removes the falloff from areaDamage */
    var areaDamageNoFalloff: Boolean? = null,
    // Example: areaRadiusFromEdge: true
    /**
     * Applies damage from edge of units instead of center. Mostly effects large units.
     */
    var areaRadiusFromEdge: Boolean? = null,
    // Example: areaExpandTime: 1s
    /**
     * Applies area damage as an expanding blast wave rather than instantly. Useful for nuke projectiles
     */
    var areaExpandTime: Float? = null,
    // Example: areaHitAirAndLandAtSameTime: true
    /** Defaults to false */
    var areaHitAirAndLandAtSameTime: Boolean? = null,
    // Example: areaHitUnderwaterAlways: true
    /** Defaults to false */
    var areaHitUnderwaterAlways: Boolean? = null,
    // Example: areaIgnoreUnitsCloserThan: 10
    /**
     * Units closer than this range aren't effected. Rarely needed. Not recommended for normal projectiles.
     */
    var areaIgnoreUnitsCloserThan: Int? = null,
    // Example: buildingDamageMultiplier: 3
    /** Defaults to 1 */
    var buildingDamageMultiplier: Float? = null,
    // Example: shieldDamageMultiplier: 1.4
    /** Defaults to 1.  0 to do no damage to shields and 2 to do double damage */
    var shieldDamageMultiplier: Float? = null,
    // Example: shieldDefectionMultiplier: 1.55
    /**
     * Defaults to 1. The amount of shield to bypass. 0 to ignore shields and directly damage hull
     */
    var shieldDefectionMultiplier: Float? = null,
    // Example: hullDamageMultiplier: 2
    /**
     * Defaults to 1. Can be used to create EMP weapons that affect shields only. 0 to ignore hull and only damage shields
     */
    var hullDamageMultiplier: Float? = null,
    // Example: ignoreParentShootDamageMultiplier: true
    /**
     * Sets whether multipliers are applied or not regardless of multipliers set in attack or setUnitStats
     */
    var ignoreParentShootDamageMultiplier: Boolean? = null,
    // Example: armourIgnoreAmount
    /**
     * Amount of armour to ignore on target and do damage as if this armour was not there
     */
    var armourIgnoreAmount: Int? = null,
    // Example: friendlyFire: false / friendlyFire: true / friendlyFire: only-ignoreEnemy
    /**
     * Lets area effect projectiles damage own team units (can't damage allies). Useful for nuke-like weapons
     */
    var friendlyFire: String? = null,
    // Example: mutator1_ifUnitWithTags: infantry
    /** Applies mutators to this projectile if target has corresponding tags */
    var mutatorXIfUnitWithTags: List<String>? = null,
    // Example: mutator1_ifUnitWithoutTags: strongArmour
    /** Same as ifUnitWithTags, but applies if target doesn't have the set tags */
    var mutatorXIfUnitWithoutTags: List<String>? = null,
    // Example: mutatorX_directDamageMultiplier: 5
    /**
     * Changes directDamage. Defaults to 1. Be careful not to confuse players using this as the effect may not be clear. Use amour instead when possible. Replace X with desired name
     */
    var mutatorXDirectDamageMultiplier: Float? = null,
    // Example: mutatorX_areaDamageMultiplier: 1.2
    /** Same as directDamageMultiplier but for areaDamage. Defaults to 1. */
    var mutatorXAreaDamageMultiplier: Float? = null,
    // Example: mutatorX_changedExplodeEffect: CUSTOM:specialBlast
    /**
     * Change explode effect if this mutator is active. Eg make a bounce off amour effect. Helps to make the damage change more clear to players (Doesn't work with targetGround.)
     */
    var mutatorXChangedExplodeEffect: String? = null,
    // Example: mutatorX_addResourcesDirectHit: oil=1
    /**
     * Add resource to all direct hit units. Warning: Be careful not be break units from other mods by adding random resources or energy to them that they don't expect.
     */
    var mutatorXAddResourcesDirectHit: ResourceBundle? = null,
    // Example: mutatorX_addResourcesAreaHit: rust=4
    /**
     * Add resource to all area hit units. Warning: Be careful not be break units from other mods by adding random resources or energy to them that they don't expect.
     */
    var mutatorXAddResourcesAreaHit: ResourceBundle? = null,

    // ===== Movement =====
    // Example: targetGround: true
    /**
     * Target ground, and don't home in on target. Note: only areaDamage is applied if targeting ground.
     */
    var targetGround: Boolean? = null,
    // Example: targetGround_includeTargetHeight: 40
    /** Default false. for area affect AA weapons */
    var targetGroundIncludeTargetHeight: Boolean = false,
    // Example: targetGroundHeightOffset: 10
    /**
     * Default 0. for shooting over or under a target. Useful for projectiles that split and rain down.
     */
    var targetGroundHeightOffset: Float = 0f,
    // Example: speed: 3
    /** Projectile default travel speed */
    var speed: Float? = null,
    // Example: targetSpeed: 5
    /** Accelerate to this speed */
    var targetSpeed: Float? = null,
    // Example: targetSpeedAcceleration: 0.3
    /** Controls the speed rampup for targetSpeed */
    var targetSpeedAcceleration: Float? = null,
    // Example: ballistic: true
    /**
     * Makes projectiles fly up into the air and come down, instead of going in a straight line
     */
    var ballistic: Boolean? = null,
    // Example: ballistic_delaymove_height: 20
    /** Sets up to how high the projectile needs to be before moving normally */
    var ballisticDelaymoveHeight: Float? = null,
    // Example: ballistic_height: 10
    /** Sets the target height of the projectile */
    var ballisticHeight: Float? = null,
    // Example: targetGroundSpread: 20
    /**
     * Randomly makes the shot inaccurate by this amount. Also used by weapons like the flamethrower
     */
    var targetGroundSpread: Float? = null,
    // Example: speedSpread: 3
    /** Randomly change the starting projectile speed by this amount */
    var speedSpread: Float? = null,
    // Example: instant: true
    /** Hit target instantly */
    var instant: Boolean? = null,
    // Example: instantReuseLast: true
    /**
     * Recycles last projectile fired, only one projectile ever exists. Can turn lasers into beam weapons by using lower rate of fire and setting this to true
     */
    var instantReuseLast: Boolean? = null,
    // Example: instantReuseLast_alsoChangeTurretAim: true
    /**
     * Make turret's aim include last projectile's spread and sweep offsets, useful for beam weapons
     */
    var instantReuseLastAlsoChangeTurretAim: Boolean? = null,
    // Example: instantReuseLast_acrossTurrets: true
    /** Reuses one beam projectile across multiple turret sections. Requires instantReuseLast. */
    var instantReuseLastAcrossTurrets: Boolean? = null,
    // Example: instantReuseLast_keepAreaDamageList: true
    /**
     * Default false. Keeping the list was the normal behaviour in 1.13 making area damage not apply a second time but this is not useful. Use this only if you want the old behaviour.
     */
    var instantReuseLastKeepAreaDamageList: Boolean? = null,
    // Example: interceptProjectile_removeTargetLifeOnly: true
    /**
     * Defaults to false. When false projectiles are just removed. Could be true to make hit projectiles explode or split when hit
     */
    var interceptProjectileRemoveTargetLifeOnly: Boolean? = null,
    // Example: disableLeadTargeting: true
    /**
     * Disable the lead targeting calculations when aiming at a moving target. Defaults false.
     */
    var disableLeadTargeting: Boolean? = null,
    // Example: leadTargetingSpeedCalculation: 2
    /**
     * The expected speed of this projectile for targetGround lead target calculation. Defaults to 'targetSpeed' if set otherwise 'speed'.
     */
    var leadTargetingSpeedCalculation: Float? = null,
    // Example: initialUnguidedSpeedHeight: 30
    /**
     * Sets vertical speed for projectiles with targetGround. Use gravity to make smooth arching projectiles. Better have gravity value slightly slower than this key to produce the arches.
     */
    var initialUnguidedSpeedHeight: String? = null,
    // Example: gravity: 29
    /**
     * Controls the pull for projectiles that target ground. Use together with initialUnguidedSpeedHeight
     */
    var gravity: String? = null,
    // Example: turnSpeed: 0
    /**
     * Limits the turn speed of a projectile, making them inaccurate even with directDamage. Zero value will make it act like if targetGround is set true, but air target friendly option.
     */
    var turnSpeed: Float? = null,
    // Example: wobbleAmplitude: 10
    /** How wide the projectile will wobble */
    var wobbleAmplitude: Float? = null,
    // Example: wobbleFrequency: 4
    /** How often the projectile will wobble */
    var wobbleFrequency: Float? = null,
    // Example: pushForce: 2
    /**
     * Push (or pull with a negative value) the units that get hit. Divided by target mass
     */
    var pushForce: Float? = null,
    // Example: pushVelocity: 5
    /**
     * Push (or pull with a negative value) the units that get hit. Ignores target mass
     */
    var pushVelocity: Float? = null,
    // Example: moveWithParent: true
    /**
     * Move projectile as parent moves. Useful for beam effects that need to stick to source turret.
     */
    var moveWithParent: Boolean? = null,
    // Example: sweepOffset: 0.5
    /** Useful for beam effects. */
    var sweepOffset: Float? = null,
    // Example: sweepOffsetFromTargetRadius: 0.4
    /** Add to sweep offset by factor of target's radius. 0.4 would be 40% */
    var sweepOffsetFromTargetRadius: Float? = null,
    // Example: sweepSpeed: 1.5
    /** Useful for beam effects. */
    var sweepSpeed: Float? = null,
    // Example: retargetingInFlight: true
    /**
     * Can retarget a new target mid-flight, perfect for flak-style weapons and projectiles that collide
     */
    var retargetingInFlight: Boolean? = null,
    // Example: retargetingInFlightSearchDelay: 1s
    /** How long between searching for new targets. Default 5 */
    var retargetingInFlightSearchDelay: Float? = null,
    // Example: retargetingInFlightSearchRange: 300
    /** Range which targets are reselected. Default 120 */
    var retargetingInFlightSearchRange: Int? = null,
    // Example: retargetingInFlightSearchLead: 30
    /** The lead of the projectile to try to hit the target. Default 15 */
    var retargetingInFlightSearchLead: Float? = null,
    // Example: retargetingInFlightSearchOnlyTags: lightArmor
    /** Only retarget units with these tags */
    var retargetingInFlightSearchOnlyTags: String? = null,

    // ===== Graphics and effects =====
    // Example: color: #bebe50
    /** Recolors this projectile using a hex value. */
    var color: Color? = null,
    // Example: invisible: true
    /** When true, the projectile is not rendered but still functional. */
    var invisible: Boolean? = null,
    // Example: image: bullet
    /** Use custom image. Overrides drawType and frame */
    var image: Texture? = null,
    // Example: drawType:1
    /**
     * Built-in image to use. 0:projectiles.png 1:projectiles_large.png 2:projectiles2.png. Refer to end of sheet for the projectile images.
     */
    var drawType: Int = 0,
    // Example: drawSize: 1.5
    /** Scale image. Defaults to 1 */
    var drawSize: Float = 1f,
    // Example: frame: 0
    /** Built-in image frame to use, starts at zero. */
    var frame: Int = 0,
    // Example: hitSound: true
    /** Default true */
    var hitSound: Boolean? = null,
    // Example: explodeEffect: smallExplosion, CUSTOM:myExplodeEffect
    /** Produces the specified effects upon explosion. */
    var explodeEffect: List<String>? = null,
    // Example: explodeEffectOnShield: CUSTOM:EMPwave
    /** Use this effect if shield is active on target */
    var explodeEffectOnShield: List<String>? = null,
    // Example: teamColorRatio: 1
    /** Mix 0-1 of team colour into color field */
    var teamColorRatio: Float? = null,
    // Example: teamColorRatio_sourceRatio: 1
    /**
     * default is (1-teamColorRatio). Keep more of color when mixing. Note this might saturate colors.
     */
    var teamColorRatioSourceRatio: Float? = null,
    // Example: drawUnderUnits: true
    /** If true, renders the projectile under units. Great for torpedos. */
    var drawUnderUnits: Boolean? = null,
    // Example: effectOnCreate: CUSTOM:puff
    /** Produces specified effects upon creation of the projectile. */
    var effectOnCreate: List<String>? = null,
    // Example: shouldRevealFog: true
    /** Reveal fog to player on explode */
    var shouldRevealFog: Boolean? = null,
    // Example: alwaysVisibleInFog: false
    /** Renders the projectile even when fog is present */
    var alwaysVisibleInFog: Boolean? = null,
    // Example: nukeWeapon: true
    /**
     * Shows on mini-map when fired. Some other side effects as well, like nuke explosion effect
     */
    var nukeWeapon: Boolean? = null,
    // Example: trailEffect: true, trailEffect: CUSTOM:rocketThrust
    /** true for built-in defaults, but can also point to any custom effects */
    var trailEffect: String? = null,
    // Example: trailEffectRate: 4
    /** Defaults to 3 */
    var trailEffectRate: Float? = null,
    // Example: lightCastOnGround: true
    /** Renders light under the projectile */
    var lightCastOnGround: Boolean? = null,
    // Example: lightSize: 1.5
    /** Sets the size of the light emitted by the projectile. 1 value = 1 tile */
    var lightSize: Float? = null,
    // Example: lightColor: #ffe92b
    /** Sets the color of the light emitted by the projectile. */
    var lightColor: Color? = null,
    // Example: largeHitEffect: true
    /** Creates a large explosion and accompanying sound on hit (only cosmetic) */
    var largeHitEffect: Boolean? = null,
    // Example: lightingEffect: true
    /** Draw as lighting works best with instant:true */
    var lightingEffect: Boolean? = null,
    // Example: laserEffect: true
    /** Draw as laser works best with instant:true */
    var laserEffect: Boolean? = null,
    // Example: beamImage: beam.png
    /**
     * Image to use for beam and laser effect type projectiles. Image is repeated vertically depending on the distance to target. Beam image should be 20 pixels or longer. Width does not matter, only the height.
     */
    var beamImage: String? = null,
    // Example: beamImageOffsetRate: 1
    /** Sets how fast the beam image moves towards or away from the target */
    var beamImageOffsetRate: Float? = null,
    // Example: beamImageStart: beamStart.png
    /** Sprite for the origin point of the custom beam */
    var beamImageStart: String? = null,
    // Example: beamImageStartRotated: true
    /** Defaults false. True to rotate with turret angle */
    var beamImageStartRotated: Boolean? = null,
    // Example: beamImageEnd: beamEnd.png
    /** Sprite for the end point of the custom beam */
    var beamImageEnd: String? = null,
    // Example: beamImageEndRotated: true
    /** Defaults false */
    var beamImageEndRotated: Boolean? = null,
    var mutators: MutableMap<String, ProjectileMutatorSpec>? = null,
) {
    fun mutator(name: String, configure: ProjectileMutatorSpec.() -> Unit) {
        requireIniMutatorName(name)
        val entries = mutators ?: linkedMapOf<String, ProjectileMutatorSpec>().also { mutators = it }
        entries.getOrPut(name, ::ProjectileMutatorSpec).configure()
    }
}

data class ProjectileMutatorSpec(
    var ifUnitWithTags: List<String>? = null,
    var ifUnitWithoutTags: List<String>? = null,
    var directDamageMultiplier: Float? = null,
    var areaDamageMultiplier: Float? = null,
    var changedExplodeEffect: String? = null,
    var addResourcesDirectHit: ResourceBundle? = null,
    var addResourcesAreaHit: ResourceBundle? = null,
)
