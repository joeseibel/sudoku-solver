package sudokusolver.kotlin

import java.util.*

fun <T : Enum<T>> Iterable<T>.toEnumSet(): EnumSet<T> =
    this as? EnumSet ?: EnumSet.copyOf(this as? Collection ?: toSet())

infix fun <T : Enum<T>> EnumSet<T>.intersect(other: EnumSet<T>): EnumSet<T> =
    EnumSet.copyOf(this).apply { retainAll(other) }