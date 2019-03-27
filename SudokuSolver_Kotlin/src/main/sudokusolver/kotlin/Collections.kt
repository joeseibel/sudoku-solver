package sudokusolver.kotlin

import java.util.EnumSet

infix fun <T : Enum<T>> EnumSet<T>.intersect(other: EnumSet<T>): EnumSet<T> =
    EnumSet.copyOf(this).apply { retainAll(other) }

fun <T> List<T>.zipEvery(): List<Pair<T, T>> =
    mapIndexed { firstIndex, first -> drop(firstIndex + 1).map { second -> first to second } }.flatten()