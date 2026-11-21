package io.github.rwx.mod

import io.github.rwx.mod.api.*

internal interface UnitConditionState {
    fun number(property: UnitNumberProperty): Float
    fun boolean(property: UnitBooleanProperty): Boolean
    fun hasTag(tag: String): Boolean
}

internal data class UnitConditionEvaluationContext(
    val self: UnitConditionState,
    val source: UnitConditionState? = null,
    val target: UnitConditionState? = null,
)

internal object UnitConditionEvaluator {
    fun evaluate(condition: UnitCondition, context: UnitConditionEvaluationContext): Boolean = when (condition) {
        UnitCondition.Always -> true
        UnitCondition.Never -> false
        is UnitCondition.SubjectExists -> context.state(condition.subject) != null
        is UnitCondition.BooleanProperty ->
            context.state(condition.subject)?.boolean(condition.property) == condition.expected

        is UnitCondition.HasTag -> context.state(condition.subject)?.hasTag(condition.tag) == true
        is UnitCondition.Compare -> compare(
            evaluate(condition.left, context),
            condition.operator,
            evaluate(condition.right, context),
        )

        is UnitCondition.All -> condition.conditions.all { evaluate(it, context) }
        is UnitCondition.Any -> condition.conditions.any { evaluate(it, context) }
        is UnitCondition.Not -> !evaluate(condition.condition, context)
    }

    private fun evaluate(expression: UnitNumberExpression, context: UnitConditionEvaluationContext): Float =
        when (expression) {
            is UnitNumberExpression.Constant -> expression.value
            is UnitNumberExpression.Property ->
                context.state(expression.subject)?.number(expression.property) ?: Float.NaN

            is UnitNumberExpression.Arithmetic -> arithmetic(
                evaluate(expression.left, context),
                expression.operator,
                evaluate(expression.right, context),
            )
        }

    private fun arithmetic(left: Float, operator: UnitArithmeticOperator, right: Float): Float {
        if (!left.isFinite() || !right.isFinite()) return Float.NaN
        return when (operator) {
            UnitArithmeticOperator.ADD -> left + right
            UnitArithmeticOperator.SUBTRACT -> left - right
            UnitArithmeticOperator.MULTIPLY -> left * right
            UnitArithmeticOperator.DIVIDE -> if (right == 0f) Float.NaN else left / right
        }
    }

    private fun compare(left: Float, operator: UnitComparisonOperator, right: Float): Boolean {
        if (!left.isFinite() || !right.isFinite()) return false
        return when (operator) {
            UnitComparisonOperator.LESS_THAN -> left < right
            UnitComparisonOperator.LESS_THAN_OR_EQUAL -> left <= right
            UnitComparisonOperator.GREATER_THAN -> left > right
            UnitComparisonOperator.GREATER_THAN_OR_EQUAL -> left >= right
            UnitComparisonOperator.EQUAL -> left == right
            UnitComparisonOperator.NOT_EQUAL -> left != right
        }
    }

    private fun UnitConditionEvaluationContext.state(subject: UnitSubject): UnitConditionState? = when (subject) {
        UnitSubject.SELF -> self
        UnitSubject.SOURCE -> source
        UnitSubject.TARGET -> target
    }
}

internal object UnitEventMatcher {
    fun matches(event: UnitEvent, actualKind: UnitEventKind, payload: UnitEventPayload): Boolean {
        if (event.kind != actualKind) return false
        return when (event) {
            is UnitEvent.TookDamage -> payload is UnitEventPayload.Damage &&
                    matchesTags(event.damageTags, payload.damageTags) &&
                    matchesTags(event.projectileTags, payload.projectileTags)

            is UnitEvent.QueueItemAdded -> payload is UnitEventPayload.Queue &&
                    matchesTags(event.actionTags, payload.actionTags)

            is UnitEvent.QueueItemCancelled -> payload is UnitEventPayload.Queue &&
                    matchesTags(event.actionTags, payload.actionTags)

            is UnitEvent.ActionCompleted -> payload is UnitEventPayload.Action &&
                    (event.actionIds.isEmpty() || payload.actionId in event.actionIds)

            is UnitEvent.MessageReceived -> payload is UnitEventPayload.Message &&
                    matchesTags(event.tags, payload.tags)

            else -> true
        }
    }

    private fun matchesTags(required: Set<String>, actual: Set<String>): Boolean =
        required.isEmpty() || required.any(actual::contains)
}
