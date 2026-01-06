package com.example.greatkingdom.game

enum class Stone {
    EMPTY,
    BLACK,
    WHITE,
    NEUTRAL // The special center stone
}

data class Point(val x: Int, val y: Int)
