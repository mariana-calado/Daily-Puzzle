package br.pucpr.dailypuzzle.model

import kotlin.random.Random

object DailySeed {

    fun indexFor(date: String, size: Int): Int =
        if (size <= 0) 0 else ((date.hashCode().toLong() and 0x7FFFFFFFL) % size).toInt()

    fun randomFor(date: String): Random = Random(date.hashCode().toLong())
}
