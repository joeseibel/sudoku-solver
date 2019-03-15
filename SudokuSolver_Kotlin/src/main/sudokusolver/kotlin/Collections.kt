package sudokusolver.kotlin

import java.util.*

fun <T : Enum<T>> Iterable<T>.toEnumSet(): EnumSet<T> =
    this as? EnumSet ?: EnumSet.copyOf(this as? Collection ?: toSet())

infix fun <T : Enum<T>> EnumSet<T>.intersect(other: EnumSet<T>): EnumSet<T> =
    EnumSet.copyOf(this).apply { retainAll(other) }

inline fun <reified R> Sequence<IndexedValue<*>>.filterValueIsInstance(): Sequence<IndexedValue<R>> =
    filter { (_, value) -> value is R }.map { (index, value) -> IndexedValue(index, value as R) }

inline fun <reified R> Iterable<IndexedValue<*>>.filterValueIsInstance(): List<IndexedValue<R>> =
    filter { (_, value) -> value is R }.map { (index, value) -> IndexedValue(index, value as R) }