package sudokusolver.kotlin

import java.util.EnumSet

inline fun <reified T : Enum<T>> enumIntersect(vararg sets: EnumSet<T>): EnumSet<T> =
    when (sets.size) {
        0 -> EnumSet.noneOf(T::class.java)
        1 -> sets.single()

        else -> EnumSet.allOf(T::class.java).also { intersection ->
            sets.forEach { set -> intersection.retainAll(set) }
        }
    }

inline fun <reified T : Enum<T>> enumUnion(vararg sets: EnumSet<T>): EnumSet<T> =
    when (sets.size) {
        0 -> EnumSet.noneOf(T::class.java)
        1 -> sets.single()
        else -> EnumSet.noneOf(T::class.java).also { union -> sets.forEach { set -> union += set } }
    }

infix fun <T : Enum<T>> EnumSet<T>.enumMinus(other: EnumSet<T>): EnumSet<T> =
    EnumSet.copyOf(this).apply { this -= other }

inline fun <reified T : Enum<T>, reified R : Enum<R>> Iterable<Pair<T, R>>.enumUnzip(): Pair<EnumSet<T>, EnumSet<R>> =
    fold(EnumSet.noneOf(T::class.java) to EnumSet.noneOf(R::class.java)) { sets, pair ->
        sets.first += pair.first
        sets.second += pair.second
        sets
    }

fun <T> List<T>.zipEveryPair(): List<Pair<T, T>> =
    mapIndexed { firstIndex, first -> drop(firstIndex + 1).map { second -> first to second } }.flatten()

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