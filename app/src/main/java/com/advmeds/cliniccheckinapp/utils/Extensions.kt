package com.advmeds.cliniccheckinapp.utils

fun <T, U, V> Array<T>.zipWith(other1: Array<U>, other2: Array<V>): List<Triple<T, U, V>> {
    val minSize = minOf(this.size, other1.size, other2.size)
    return List(minSize) { index ->
        Triple(this[index], other1[index], other2[index])
    }
}