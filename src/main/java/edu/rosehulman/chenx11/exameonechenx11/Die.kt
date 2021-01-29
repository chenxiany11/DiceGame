package edu.rosehulman.chenx11.exameonechenx11

import kotlin.random.Random

class Die {
    var frozen = false
    var value = 0

    init {
        roll()
    }

    fun roll() {
        if (!frozen) {
            value = Random.nextInt(6)+1
        }
    }

    fun toggleFrozen() {
        frozen = !frozen
    }

}

