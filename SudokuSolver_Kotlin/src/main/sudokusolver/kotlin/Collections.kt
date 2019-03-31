package sudokusolver.kotlin

import java.util.EnumSet

/*
 * I used to call this intersect and that really would be the more natural name for this function. However, the problem
 * with calling it intersect is that it is so easy to miss the import and then end up calling
 * kotlin.collections.intersect. By calling it enumIntersect, I am forced to use the correct import in order to call
 * this.
 */
infix fun <T : Enum<T>> EnumSet<T>.enumIntersect(other: EnumSet<T>): EnumSet<T> =
    EnumSet.copyOf(this).apply { retainAll(other) }

inline fun <reified T : Enum<T>> enumUnion(vararg sets: EnumSet<T>): EnumSet<T> =
    when (sets.size) {
        0 -> EnumSet.noneOf(T::class.java)
        1 -> sets.first()
        else -> EnumSet.noneOf(T::class.java).also { union -> sets.forEach { set -> union += set } }
    }

infix fun <T : Enum<T>> EnumSet<T>.enumMinus(other: EnumSet<T>): EnumSet<T> =
    EnumSet.copyOf(this).apply { this -= other }

fun <T> Array<T>.zipEveryPair(): List<Pair<T, T>> =
    mapIndexed { firstIndex, first -> drop(firstIndex + 1).map { second -> first to second } }.flatten()

fun <T> List<T>.zipEveryPair(): List<Pair<T, T>> =
    mapIndexed { firstIndex, first -> drop(firstIndex + 1).map { second -> first to second } }.flatten()

fun <T> Array<T>.zipEveryTriple(): List<Triple<T, T, T>> =
    mapIndexed { firstIndex, first ->
        withIndex().drop(firstIndex + 1).flatMap { (secondIndex, second) ->
            drop(secondIndex + 1).map { third -> Triple(first, second, third) }
        }
    }.flatten()

fun <T> List<T>.zipEveryTriple(): List<Triple<T, T, T>> =
    mapIndexed { firstIndex, first ->
        withIndex().drop(firstIndex + 1).flatMap { (secondIndex, second) ->
            drop(secondIndex + 1).map { third -> Triple(first, second, third) }
        }
    }.flatten()

data class Quad<out A, out B, out C, out D>(val first: A, val second: B, val third: C, val fourth: D)

fun <T> List<T>.zipEveryQuad(): List<Quad<T, T, T, T>> =
    mapIndexed { firstIndex, first ->
        withIndex().drop(firstIndex + 1).flatMap { (secondIndex, second) ->
            withIndex().drop(secondIndex + 1).flatMap { (thirdIndex, third) ->
                drop(thirdIndex + 1).map { fourth -> Quad(first, second, third, fourth) }
            }
        }
    }.flatten()