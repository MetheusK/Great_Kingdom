package com.example.greatkingdom.game

class GameEngine(val size: Int = 9) {
    var board = Board(size)
    var currentTurn = Stone.BLACK
    var passCount = 0
    var blackCaptured = 0 // Stones captured by Black (White stones removed)
    var whiteCaptured = 0 // Stones captured by White (Black stones removed)
    var isGameOver = false

    // For Ko rule: store hash or simplified board state
    // Simple Ko: just check previous board state.
    private var previousBoard: Board? = null

    fun placeStone(x: Int, y: Int): Boolean {
        if (isGameOver) return false
        val point = Point(x, y)
        if (!board.isValid(point)) return false
        if (board.getStone(point) != Stone.EMPTY) return false

        // Attempt move
        val nextBoard = board.copy()
        nextBoard.setStone(point, currentTurn)

        // Check captures
        val opponent = if (currentTurn == Stone.BLACK) Stone.WHITE else Stone.BLACK
        val capturedPoints = getCapturedPoints(nextBoard, opponent)

        // Remove captured stones
        for (p in capturedPoints) {
            nextBoard.setStone(p, Stone.EMPTY)
        }

        // Suicide check: The placed stone must have liberties OR capture something
        if (capturedPoints.isEmpty()) {
             if (countLiberties(nextBoard, point) == 0) {
                 return false // Suicide is illegal
             }
        }

        // Ko check (Simplified: Cannot repeat immediate previous state)
        // Note: A full implementation would check all history, but simple Ko checks just the last one.
        if (previousBoard != null && areBoardsEqual(nextBoard, previousBoard!!)) {
            return false
        }

        // Commit move
        if (currentTurn == Stone.BLACK) {
            blackCaptured += capturedPoints.size
        } else {
            whiteCaptured += capturedPoints.size
        }

        previousBoard = board
        board = nextBoard
        passCount = 0
        switchTurn()
        return true
    }

    fun pass() {
        if (isGameOver) return
        passCount++
        if (passCount >= 2) {
            isGameOver = true
        } else {
            switchTurn()
        }
    }

    private fun switchTurn() {
        currentTurn = if (currentTurn == Stone.BLACK) Stone.WHITE else Stone.BLACK
    }

    // Helper: Get groups of stones
    // Returns a list of Points that are captured
    fun getCapturedPoints(board: Board, targetColor: Stone): List<Point> {
        val captured = mutableListOf<Point>()
        val visited = Array(board.size) { BooleanArray(board.size) }

        for (i in 0 until board.size) {
            for (j in 0 until board.size) {
                if (board.grid[i][j] == targetColor && !visited[i][j]) {
                    val group = getGroup(board, Point(i, j))
                    // Mark group as visited
                    for (p in group) {
                        visited[p.x][p.y] = true
                    }
                    // Check liberties of the group
                    if (countGroupLiberties(board, group) == 0) {
                        captured.addAll(group)
                    }
                }
            }
        }
        return captured
    }

    private fun getGroup(board: Board, start: Point): List<Point> {
        val color = board.getStone(start)
        val group = mutableListOf<Point>()
        val queue = java.util.ArrayDeque<Point>()
        val visited = mutableSetOf<Point>()

        queue.add(start)
        visited.add(start)

        while (!queue.isEmpty()) {
            val current = queue.poll()
            group.add(current)

            val neighbors = listOf(
                Point(current.x + 1, current.y),
                Point(current.x - 1, current.y),
                Point(current.x, current.y + 1),
                Point(current.x, current.y - 1)
            )

            for (n in neighbors) {
                if (board.isValid(n) && !visited.contains(n) && board.getStone(n) == color) {
                    visited.add(n)
                    queue.add(n)
                }
            }
        }
        return group
    }

    private fun countGroupLiberties(board: Board, group: List<Point>): Int {
        var liberties = 0
        val visitedLiberties = mutableSetOf<Point>()

        for (stone in group) {
             val neighbors = listOf(
                Point(stone.x + 1, stone.y),
                Point(stone.x - 1, stone.y),
                Point(stone.x, stone.y + 1),
                Point(stone.x, stone.y - 1)
            )
            for (n in neighbors) {
                if (board.isValid(n) && board.getStone(n) == Stone.EMPTY && !visitedLiberties.contains(n)) {
                    liberties++
                    visitedLiberties.add(n)
                }
            }
        }
        return liberties
    }

    private fun countLiberties(board: Board, point: Point): Int {
        val group = getGroup(board, point)
        return countGroupLiberties(board, group)
    }

    private fun areBoardsEqual(b1: Board, b2: Board): Boolean {
        for (i in 0 until b1.size) {
            for (j in 0 until b1.size) {
                if (b1.grid[i][j] != b2.grid[i][j]) return false
            }
        }
        return true
    }

    // Scoring: Territory + Prisoners
    // Territory: Empty points completely surrounded by one color.
    // Neutral stones act as walls (neither black nor white).
    fun calculateScore(): Pair<Int, Int> {
        var blackTerritory = 0
        var whiteTerritory = 0

        // Using simple flood fill to identify territories
        val visited = Array(board.size) { BooleanArray(board.size) }

        for (i in 0 until board.size) {
            for (j in 0 until board.size) {
                if (board.grid[i][j] == Stone.EMPTY && !visited[i][j]) {
                    val territoryPoints = mutableListOf<Point>()
                    val region = getRegion(board, Point(i, j))
                    // Check borders of this region
                    var touchesBlack = false
                    var touchesWhite = false
                    var touchesNeutral = false // Neutral counts as boundary? Or neutral invalidates territory?

                    // Standard Go territory logic: Neutral/Opponent/Edge are boundaries.
                    // If a region touches ONLY Black -> Black Territory.
                    // If touches ONLY White -> White Territory.
                    // If touches Both -> No Territory (Dame).
                    // In Great Kingdom, Neutral stone is a wall. It doesn't belong to anyone.
                    // So if it touches Neutral, it doesn't disqualify ownership, right?
                    // Actually, usually territory must be enclosed by YOUR stones.
                    // If a wall is neutral, can you use it to enclose?
                    // In Go, the edge of the board is a neutral wall you use.
                    // The Neutral stone is likely an obstacle.
                    // Hypothesis: It acts like a board edge.

                    // Use a separate set for visiting during this region scan to avoid double counting borders
                    // Actually, 'visited' array handles the global check so we don't re-scan the region.
                    // But we need to iterate over 'region' list properly.

                    for (p in region) {
                        visited[p.x][p.y] = true
                        territoryPoints.add(p)

                        // Check neighbors for boundaries
                        val neighbors = listOf(
                            Point(p.x + 1, p.y),
                            Point(p.x - 1, p.y),
                            Point(p.x, p.y + 1),
                            Point(p.x, p.y - 1)
                        )
                        for (n in neighbors) {
                            if (board.isValid(n)) {
                                when (board.getStone(n)) {
                                    Stone.BLACK -> touchesBlack = true
                                    Stone.WHITE -> touchesWhite = true
                                    else -> {} // Empty is part of region, Neutral is wall
                                }
                            }
                        }
                    }

                    if (touchesBlack && !touchesWhite) {
                        blackTerritory += territoryPoints.size
                    } else if (touchesWhite && !touchesBlack) {
                        whiteTerritory += territoryPoints.size
                    }
                }
            }
        }

        return Pair(blackTerritory + blackCaptured, whiteTerritory + whiteCaptured)
    }

    // Gets a connected region of Empty points
    private fun getRegion(board: Board, start: Point): List<Point> {
        val region = mutableListOf<Point>()
        val queue = java.util.ArrayDeque<Point>()
        val visited = mutableSetOf<Point>()

        queue.add(start)
        visited.add(start)

        while (!queue.isEmpty()) {
            val current = queue.poll()
            region.add(current)

            val neighbors = listOf(
                Point(current.x + 1, current.y),
                Point(current.x - 1, current.y),
                Point(current.x, current.y + 1),
                Point(current.x, current.y - 1)
            )

            for (n in neighbors) {
                if (board.isValid(n) && !visited.contains(n) && board.getStone(n) == Stone.EMPTY) {
                    visited.add(n)
                    queue.add(n)
                }
            }
        }
        return region
    }
}
