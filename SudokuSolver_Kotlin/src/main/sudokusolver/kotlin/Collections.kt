package sudokusolver.kotlin

import java.util.EnumSet

/*
 * I used to call this intersect and that really would be the more natural name for this function. However, the problem
 * with calling it intersect is that it is so easy miss the import and then end up calling kotlin.collections.intersect.
 * By calling it enumIntersect, I am forced to use the correct import in order to call this.
 */
infix fun <T : Enum<T>> EnumSet<T>.enumIntersect(other: EnumSet<T>): EnumSet<T> =
    EnumSet.copyOf(this).apply { retainAll(other) }

fun <T> List<T>.zipEvery(): List<Pair<T, T>> =
    mapIndexed { firstIndex, first -> drop(firstIndex + 1).map { second -> first to second } }.flatten()