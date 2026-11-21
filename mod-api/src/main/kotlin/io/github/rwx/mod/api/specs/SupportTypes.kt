package io.github.rwx.mod.api.specs

/**
 * Type aliases for common RW types
 */

/** Resource bundle for prices (e.g., "credits=100, gold=5") */
typealias ResourceBundle = String

/** Logic boolean expression (e.g., "if self.hp >= 100") */
typealias LogicBoolean = String

/** Logic number expression (e.g., "self.hp * 0.5") */
typealias LogicNumber = String

/** Unit reference (e.g., "self", "parent", "customTarget1") */
typealias UnitRef = String

/** Projectile reference */
typealias ProjectileRef = String

/** File path to image texture */
typealias Texture = String

/** Color in hex format (e.g., "#FF0000") */
typealias Color = String

enum class MovementType {
    NONE,
    LAND,
    BUILDING,
    AIR,
    WATER,
    HOVER,
    OVER_CLIFF,
    OVER_CLIFF_WATER
}

/** Time duration (e.g., "3s", "500ms") */
typealias Duration = String
