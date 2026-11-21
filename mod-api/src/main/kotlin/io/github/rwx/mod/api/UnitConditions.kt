package io.github.rwx.mod.api

sealed interface UnitNumberExpression {
    data class Constant(val value: Float) : UnitNumberExpression
    data class Property(val subject: UnitSubject, val property: UnitNumberProperty) : UnitNumberExpression
    data class Arithmetic(
        val left: UnitNumberExpression,
        val operator: UnitArithmeticOperator,
        val right: UnitNumberExpression,
    ) : UnitNumberExpression
}

sealed interface UnitCondition {
    data object Always : UnitCondition
    data object Never : UnitCondition
    data class SubjectExists(val subject: UnitSubject) : UnitCondition
    data class BooleanProperty(
        val subject: UnitSubject,
        val property: UnitBooleanProperty,
        val expected: Boolean = true,
    ) : UnitCondition

    data class HasTag(val subject: UnitSubject, val tag: String) : UnitCondition
    data class Compare(
        val left: UnitNumberExpression,
        val operator: UnitComparisonOperator,
        val right: UnitNumberExpression,
    ) : UnitCondition

    data class All(val conditions: List<UnitCondition>) : UnitCondition
    data class Any(val conditions: List<UnitCondition>) : UnitCondition
    data class Not(val condition: UnitCondition) : UnitCondition
}

enum class UnitSubject {
    SELF,
    SOURCE,
    TARGET,
}

enum class UnitNumberProperty {
    HEALTH,
    MAX_HEALTH,
    HEALTH_RATIO,
    SHIELD,
    MAX_SHIELD,
    SHIELD_RATIO,
    ENERGY,
    MAX_ENERGY,
    ENERGY_RATIO,
    X,
    Y,
    SPEED,
    AGE_TICKS,
}

enum class UnitBooleanProperty {
    DESTROYED,
    MOVING,
    ATTACKING,
    BUILD_COMPLETE,
}

enum class UnitArithmeticOperator {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
}

enum class UnitComparisonOperator {
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    EQUAL,
    NOT_EQUAL,
}

class UnitConditionScope internal constructor() {
    val self = UnitSubjectExpression(UnitSubject.SELF)
    val source = UnitSubjectExpression(UnitSubject.SOURCE)
    val target = UnitSubjectExpression(UnitSubject.TARGET)
}

class UnitSubjectExpression internal constructor(private val subject: UnitSubject) {
    val exists: UnitCondition get() = UnitCondition.SubjectExists(subject)
    val health: UnitNumberExpression get() = number(UnitNumberProperty.HEALTH)
    val maxHealth: UnitNumberExpression get() = number(UnitNumberProperty.MAX_HEALTH)
    val healthRatio: UnitNumberExpression get() = number(UnitNumberProperty.HEALTH_RATIO)
    val shield: UnitNumberExpression get() = number(UnitNumberProperty.SHIELD)
    val maxShield: UnitNumberExpression get() = number(UnitNumberProperty.MAX_SHIELD)
    val shieldRatio: UnitNumberExpression get() = number(UnitNumberProperty.SHIELD_RATIO)
    val energy: UnitNumberExpression get() = number(UnitNumberProperty.ENERGY)
    val maxEnergy: UnitNumberExpression get() = number(UnitNumberProperty.MAX_ENERGY)
    val energyRatio: UnitNumberExpression get() = number(UnitNumberProperty.ENERGY_RATIO)
    val x: UnitNumberExpression get() = number(UnitNumberProperty.X)
    val y: UnitNumberExpression get() = number(UnitNumberProperty.Y)
    val speed: UnitNumberExpression get() = number(UnitNumberProperty.SPEED)
    val ageTicks: UnitNumberExpression get() = number(UnitNumberProperty.AGE_TICKS)
    val isDestroyed: UnitCondition get() = boolean(UnitBooleanProperty.DESTROYED)
    val isMoving: UnitCondition get() = boolean(UnitBooleanProperty.MOVING)
    val isAttacking: UnitCondition get() = boolean(UnitBooleanProperty.ATTACKING)
    val isBuildComplete: UnitCondition get() = boolean(UnitBooleanProperty.BUILD_COMPLETE)

    fun hasTag(tag: String): UnitCondition = UnitCondition.HasTag(subject, tag)

    private fun number(property: UnitNumberProperty) = UnitNumberExpression.Property(subject, property)
    private fun boolean(property: UnitBooleanProperty) = UnitCondition.BooleanProperty(subject, property)
}

fun unitCondition(configure: UnitConditionScope.() -> UnitCondition): UnitCondition =
    UnitConditionScope().configure()

operator fun UnitNumberExpression.plus(other: UnitNumberExpression): UnitNumberExpression =
    arithmetic(UnitArithmeticOperator.ADD, other)

operator fun UnitNumberExpression.plus(other: Number): UnitNumberExpression = this + other.asExpression()

operator fun UnitNumberExpression.minus(other: UnitNumberExpression): UnitNumberExpression =
    arithmetic(UnitArithmeticOperator.SUBTRACT, other)

operator fun UnitNumberExpression.minus(other: Number): UnitNumberExpression = this - other.asExpression()

operator fun UnitNumberExpression.times(other: UnitNumberExpression): UnitNumberExpression =
    arithmetic(UnitArithmeticOperator.MULTIPLY, other)

operator fun UnitNumberExpression.times(other: Number): UnitNumberExpression = this * other.asExpression()

operator fun UnitNumberExpression.div(other: UnitNumberExpression): UnitNumberExpression =
    arithmetic(UnitArithmeticOperator.DIVIDE, other)

operator fun UnitNumberExpression.div(other: Number): UnitNumberExpression = this / other.asExpression()

infix fun UnitNumberExpression.lessThan(other: UnitNumberExpression): UnitCondition =
    compare(UnitComparisonOperator.LESS_THAN, other)

infix fun UnitNumberExpression.lessThan(other: Number): UnitCondition = lessThan(other.asExpression())

infix fun UnitNumberExpression.atMost(other: UnitNumberExpression): UnitCondition =
    compare(UnitComparisonOperator.LESS_THAN_OR_EQUAL, other)

infix fun UnitNumberExpression.atMost(other: Number): UnitCondition = atMost(other.asExpression())

infix fun UnitNumberExpression.greaterThan(other: UnitNumberExpression): UnitCondition =
    compare(UnitComparisonOperator.GREATER_THAN, other)

infix fun UnitNumberExpression.greaterThan(other: Number): UnitCondition = greaterThan(other.asExpression())

infix fun UnitNumberExpression.atLeast(other: UnitNumberExpression): UnitCondition =
    compare(UnitComparisonOperator.GREATER_THAN_OR_EQUAL, other)

infix fun UnitNumberExpression.atLeast(other: Number): UnitCondition = atLeast(other.asExpression())

infix fun UnitNumberExpression.equalTo(other: UnitNumberExpression): UnitCondition =
    compare(UnitComparisonOperator.EQUAL, other)

infix fun UnitNumberExpression.equalTo(other: Number): UnitCondition = equalTo(other.asExpression())

infix fun UnitNumberExpression.notEqualTo(other: UnitNumberExpression): UnitCondition =
    compare(UnitComparisonOperator.NOT_EQUAL, other)

infix fun UnitNumberExpression.notEqualTo(other: Number): UnitCondition = notEqualTo(other.asExpression())

infix fun UnitCondition.and(other: UnitCondition): UnitCondition = when {
    this is UnitCondition.All -> UnitCondition.All(conditions + other)
    else -> UnitCondition.All(listOf(this, other))
}

infix fun UnitCondition.or(other: UnitCondition): UnitCondition = when {
    this is UnitCondition.Any -> UnitCondition.Any(conditions + other)
    else -> UnitCondition.Any(listOf(this, other))
}

operator fun UnitCondition.not(): UnitCondition = UnitCondition.Not(this)

fun allOf(vararg conditions: UnitCondition): UnitCondition = UnitCondition.All(conditions.toList())

fun anyOf(vararg conditions: UnitCondition): UnitCondition = UnitCondition.Any(conditions.toList())

private fun UnitNumberExpression.arithmetic(
    operator: UnitArithmeticOperator,
    other: UnitNumberExpression,
): UnitNumberExpression = UnitNumberExpression.Arithmetic(this, operator, other)

private fun UnitNumberExpression.compare(
    operator: UnitComparisonOperator,
    other: UnitNumberExpression,
): UnitCondition = UnitCondition.Compare(this, operator, other)

private fun Number.asExpression(): UnitNumberExpression = UnitNumberExpression.Constant(toFloat())
