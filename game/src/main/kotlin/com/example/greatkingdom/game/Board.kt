package com.example.greatkingdom.game

class Board(val size: Int = 9) {
    val grid: Array<Array<Stone>> = Array(size) { Array(size) { Stone.EMPTY } }

    init {
        // Setup initial board state
        // Place neutral stone at center (4, 4 for 9x9 board)
        if (size == 9) {
            grid[4][4] = Stone.NEUTRAL
        }
    }

    fun getStone(point: Point): Stone {
        if (!isValid(point)) return Stone.EMPTY // Or throw error
        return grid[point.x][point.y]
    }

    fun setStone(point: Point, stone: Stone) {
        if (isValid(point)) {
            grid[point.x][point.y] = stone
        }
    }

    fun isValid(point: Point): Boolean {
        return point.x in 0 until size && point.y in 0 until size
    }

    fun copy(): Board {
        val newBoard = Board(size)
        for (i in 0 until size) {
            for (j in 0 until size) {
                newBoard.grid[i][j] = this.grid[i][j]
            }
        }
        return newBoard
    }
}
